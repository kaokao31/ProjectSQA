package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.XmlDeclaration;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for XmlTreeBuilder, targeting maximum coverage and fault detection.
 * Designed to exercise all branches, edge cases, and the specific bug (Jsoup bug 80)
 * related to XML declaration handling.
 */
public class XmlTreeBuilderTest {

    private XmlTreeBuilder builder;
    private Parser parser;

    @Before
    public void setUp() {
        builder = new XmlTreeBuilder();
        parser = Parser.xmlParser();
    }

    // Helper to parse XML string using XmlTreeBuilder directly
    private Document parseXml(String xml) {
        return Jsoup.parse(xml, "", parser);
    }

    // ========== Basic XML parsing ==========

    @Test
    public void testSimpleTag() {
        Document doc = parseXml("<root><child>text</child></root>");
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("text", child.text());
    }

    @Test
    public void testSelfClosingTag() {
        Document doc = parseXml("<root><br/></root>");
        Element root = doc.child(0);
        assertEquals(1, root.childrenSize());
        assertEquals("br", root.child(0).tagName());
    }

    @Test
    public void testAttributes() {
        Document doc = parseXml("<root attr1='val1' attr2=\"val2\"></root>");
        Element root = doc.child(0);
        assertEquals("val1", root.attr("attr1"));
        assertEquals("val2", root.attr("attr2"));
    }

    // ========== Processing Instructions (PI) ==========

    @Test
    public void testProcessingInstruction() {
        Document doc = parseXml("<?target data?><root></root>");
        List<Node> children = doc.childNodesCopy();
        // First child should be a DataNode (PI)
        Node first = children.get(0);
        assertTrue(first instanceof DataNode);
        DataNode pi = (DataNode) first;
        assertEquals("target data", pi.getWholeData());
    }

    @Test
    public void testMultipleProcessingInstructions() {
        Document doc = parseXml("<?pi1?><root><?pi2?></root>");
        List<Node> rootChildren = doc.child(0).childNodesCopy();
        assertEquals(1, rootChildren.size());
        assertTrue(rootChildren.get(0) instanceof DataNode);
        assertEquals("pi2", ((DataNode) rootChildren.get(0)).getWholeData());
    }

    // ========== XML Declaration (bug 80) ==========

    @Test
    public void testXmlDeclaration() {
        // This is the specific bug: parsing "<?xml version='1.0' encoding='UTF-8'?>"
        Document doc = parseXml("<?xml version='1.0' encoding='UTF-8'?><root></root>");
        // The declaration should be parsed as a processing instruction (DataNode)
        List<Node> children = doc.childNodesCopy();
        assertTrue(children.size() >= 1);
        Node first = children.get(0);
        assertTrue("Expected DataNode for XML declaration", first instanceof DataNode);
        DataNode decl = (DataNode) first;
        assertTrue(decl.getWholeData().contains("version='1.0'"));
    }

    @Test
    public void testXmlDeclarationWithQuestionMarkInContent() {
        // Edge case: PI with '?' in data
        Document doc = parseXml("<?some?data?><root></root>");
        List<Node> children = doc.childNodesCopy();
        Node first = children.get(0);
        assertTrue(first instanceof DataNode);
        assertEquals("some?data?", ((DataNode) first).getWholeData());
    }

    // ========== Comments ==========

    @Test
    public void testComment() {
        Document doc = parseXml("<!-- comment --><root></root>");
        List<Node> children = doc.childNodesCopy();
        Node first = children.get(0);
        assertTrue(first instanceof Comment);
        assertEquals(" comment ", ((Comment) first).getData());
    }

    @Test
    public void testCommentWithDashes() {
        Document doc = parseXml("<!-- - - --><root></root>");
        List<Node> children = doc.childNodesCopy();
        Node first = children.get(0);
        assertTrue(first instanceof Comment);
        assertEquals(" - - ", ((Comment) first).getData());
    }

    // ========== DOCTYPE ==========

    @Test
    public void testDoctype() {
        Document doc = parseXml("<!DOCTYPE root><root></root>");
        List<Node> children = doc.childNodesCopy();
        Node first = children.get(0);
        assertTrue(first instanceof DocumentType);
        assertEquals("root", ((DocumentType) first).name());
    }

    @Test
    public void testDoctypeWithPublicId() {
        Document doc = parseXml("<!DOCTYPE root PUBLIC \"-//W3C//DTD XHTML 1.0//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><root></root>");
        List<Node> children = doc.childNodesCopy();
        Node first = children.get(0);
        assertTrue(first instanceof DocumentType);
        DocumentType dt = (DocumentType) first;
        assertEquals("root", dt.name());
        assertEquals("-//W3C//DTD XHTML 1.0//EN", dt.publicId());
        assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", dt.systemId());
    }

    // ========== Edge cases ==========

    @Test(expected = IllegalArgumentException.class)
    public void testNullInput() {
        // Jsoup.parse throws IllegalArgumentException for null input
        Jsoup.parse(null, "", parser);
    }

    @Test
    public void testEmptyInput() {
        Document doc = parseXml("");
        assertNotNull(doc);
        assertEquals(0, doc.childrenSize());
    }

    @Test
    public void testOnlyWhitespace() {
        Document doc = parseXml("   ");
        assertNotNull(doc);
        assertEquals(0, doc.childrenSize());
    }

    @Test
    public void testUnclosedTag() {
        // XML parser should handle unclosed tags gracefully (not throw)
        Document doc = parseXml("<root><child>text</root>");
        // The parser may close the child implicitly
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        // child may be present or not depending on error recovery
        // We just ensure no exception
    }

    @Test
    public void testMalformedProcessingInstruction() {
        // Missing '?' at end
        Document doc = parseXml("<?target data<root></root>");
        // Should not throw, may treat as text or PI
        assertNotNull(doc);
    }

    @Test
    public void testMultipleRootElements() {
        // XML requires single root, but parser may accept multiple
        Document doc = parseXml("<a></a><b></b>");
        assertEquals(2, doc.childrenSize());
    }

    // ========== Branch coverage: insertToken for various token types ==========

    @Test
    public void testEndTagWithoutStartTag() {
        // End tag without matching start tag
        Document doc = parseXml("</root>");
        // Should be ignored or create a phantom element? Typically ignored.
        assertEquals(0, doc.childrenSize());
    }

    @Test
    public void testCharacterData() {
        Document doc = parseXml("<root>text</root>");
        Element root = doc.child(0);
        TextNode text = (TextNode) root.childNode(0);
        assertEquals("text", text.text());
    }

    @Test
    public void testNestedElements() {
        Document doc = parseXml("<a><b><c></c></b></a>");
        Element a = doc.child(0);
        Element b = a.child(0);
        Element c = b.child(0);
        assertEquals("a", a.tagName());
        assertEquals("b", b.tagName());
        assertEquals("c", c.tagName());
    }

    @Test
    public void testMixedContent() {
        Document doc = parseXml("<root>text1<child/>text2</root>");
        Element root = doc.child(0);
        assertEquals(3, root.childNodesSize());
        assertTrue(root.childNode(0) instanceof TextNode);
        assertTrue(root.childNode(1) instanceof Element);
        assertTrue(root.childNode(2) instanceof TextNode);
    }

    // ========== Fault detection: potential NullPointerException or incorrect state ==========

    @Test
    public void testProcessingInstructionAtEnd() {
        // PI after root close
        Document doc = parseXml("<root></root><?after?>");
        List<Node> children = doc.childNodesCopy();
        // The PI should be added as a sibling of root
        assertTrue(children.size() >= 2);
        Node last = children.get(children.size() - 1);
        assertTrue(last instanceof DataNode);
        assertEquals("after", ((DataNode) last).getWholeData());
    }

    @Test
    public void testCommentAtEnd() {
        Document doc = parseXml("<root></root><!-- end -->");
        List<Node> children = doc.childNodesCopy();
        assertTrue(children.size() >= 2);
        Node last = children.get(children.size() - 1);
        assertTrue(last instanceof Comment);
        assertEquals(" end ", ((Comment) last).getData());
    }

    @Test
    public void testDoctypeAtEnd() {
        Document doc = parseXml("<root></root><!DOCTYPE foo>");
        List<Node> children = doc.childNodesCopy();
        assertTrue(children.size() >= 2);
        Node last = children.get(children.size() - 1);
        assertTrue(last instanceof DocumentType);
        assertEquals("foo", ((DocumentType) last).name());
    }

    @Test
    public void testEmptyTagWithAttributes() {
        Document doc = parseXml("<root><empty attr='val'/></root>");
        Element empty = doc.child(0).child(0);
        assertEquals("empty", empty.tagName());
        assertEquals("val", empty.attr("attr"));
    }

    @Test
    public void testCaseSensitivity() {
        // XML is case-sensitive
        Document doc = parseXml("<ROOT><Child></Child></ROOT>");
        Element root = doc.child(0);
        assertEquals("ROOT", root.tagName());
        assertEquals("Child", root.child(0).tagName());
    }

    @Test
    public void testNamespace() {
        Document doc = parseXml("<ns:root xmlns:ns='http://example.com'><ns:child/></ns:root>");
        Element root = doc.child(0);
        assertEquals("ns:root", root.tagName());
        assertEquals("http://example.com", root.attr("xmlns:ns"));
        Element child = root.child(0);
        assertEquals("ns:child", child.tagName());
    }

    // ========== Additional edge cases for coverage ==========

    @Test
    public void testDeepNesting() {
        StringBuilder sb = new StringBuilder();
        sb.append("<a>");
        for (int i = 0; i < 100; i++) {
            sb.append("<b>");
        }
        for (int i = 0; i < 100; i++) {
            sb.append("</b>");
        }
        sb.append("</a>");
        Document doc = parseXml(sb.toString());
        assertNotNull(doc);
        Element a = doc.child(0);
        // Should not throw stack overflow
    }

    @Test
    public void testCDataSection() {
        // XML parser may treat <![CDATA[ as text
        Document doc = parseXml("<root><![CDATA[<greeting>]]></root>");
        Element root = doc.child(0);
        // The CDATA content should be a text node or data node
        assertTrue(root.childNode(0) instanceof TextNode || root.childNode(0) instanceof DataNode);
    }

    @Test
    public void testEntityRef() {
        Document doc = parseXml("<root>&amp;&lt;&gt;</root>");
        Element root = doc.child(0);
        String text = root.text();
        assertEquals("&<>", text);
    }

    @Test
    public void testMultipleXmlDeclarations() {
        // Multiple declarations should be handled
        Document doc = parseXml("<?xml version='1.0'?><?xml-stylesheet type='text/xsl' href='style.xsl'?><root></root>");
        List<Node> children = doc.childNodesCopy();
        assertEquals(3, children.size());
        assertTrue(children.get(0) instanceof DataNode);
        assertTrue(children.get(1) instanceof DataNode);
        assertTrue(children.get(2) instanceof Element);
    }
}