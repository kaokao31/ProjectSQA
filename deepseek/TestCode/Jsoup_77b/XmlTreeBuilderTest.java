package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for XmlTreeBuilder, targeting maximum coverage and fault detection.
 * Includes tests for self-closing tags (bug 77), nested elements, attributes,
 * comments, doctype, empty input, and edge cases.
 */
public class XmlTreeBuilderTest {

    private XmlTreeBuilder builder;

    @Before
    public void setUp() {
        builder = new XmlTreeBuilder();
    }

    // Helper to parse XML string using the builder directly
    private Document parseXml(String xml) {
        return builder.parse(new StringReader(xml), "http://example.com");
    }

    // ========== Basic Parsing ==========

    @Test
    public void testSimpleElement() {
        Document doc = parseXml("<root></root>");
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(0, root.childNodeSize());
    }

    @Test
    public void testElementWithText() {
        Document doc = parseXml("<root>Hello</root>");
        Element root = doc.child(0);
        assertEquals("Hello", root.text());
    }

    @Test
    public void testNestedElements() {
        Document doc = parseXml("<parent><child></child></parent>");
        Element parent = doc.child(0);
        assertEquals("parent", parent.tagName());
        assertEquals(1, parent.childNodeSize());
        Element child = parent.child(0);
        assertEquals("child", child.tagName());
    }

    // ========== Self-Closing Tags (Bug 77) ==========

    @Test
    public void testSelfClosingTag() {
        Document doc = parseXml("<root><br/></root>");
        Element root = doc.child(0);
        // The <br/> should be a self-closing tag, not an empty element
        assertEquals(1, root.childNodeSize());
        Element br = root.child(0);
        assertEquals("br", br.tagName());
        assertTrue(br.tag().isSelfClosing());
    }

    @Test
    public void testSelfClosingTagWithAttributes() {
        Document doc = parseXml("<root><img src='test.png'/></root>");
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        Element img = root.child(0);
        assertEquals("img", img.tagName());
        assertEquals("test.png", img.attr("src"));
        assertTrue(img.tag().isSelfClosing());
    }

    @Test
    public void testMultipleSelfClosingTags() {
        Document doc = parseXml("<root><br/><hr/></root>");
        Element root = doc.child(0);
        assertEquals(2, root.childNodeSize());
        assertEquals("br", root.child(0).tagName());
        assertEquals("hr", root.child(1).tagName());
    }

    @Test
    public void testSelfClosingTagFollowedByText() {
        Document doc = parseXml("<root><br/>text</root>");
        Element root = doc.child(0);
        assertEquals(2, root.childNodeSize()); // br and text node
        assertEquals("br", root.child(0).tagName());
        assertEquals("text", root.child(1).toString().trim());
    }

    // ========== Attributes ==========

    @Test
    public void testAttributes() {
        Document doc = parseXml("<root attr1='val1' attr2='val2'></root>");
        Element root = doc.child(0);
        assertEquals("val1", root.attr("attr1"));
        assertEquals("val2", root.attr("attr2"));
    }

    @Test
    public void testBooleanAttribute() {
        Document doc = parseXml("<root disabled></root>");
        Element root = doc.child(0);
        assertEquals("", root.attr("disabled"));
    }

    // ========== Comments ==========

    @Test
    public void testComment() {
        Document doc = parseXml("<root><!-- comment --></root>");
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        Node comment = root.childNode(0);
        assertEquals("#comment", comment.nodeName());
        assertTrue(comment.toString().contains("comment"));
    }

    // ========== Doctype ==========

    @Test
    public void testDoctype() {
        Document doc = parseXml("<!DOCTYPE root><root></root>");
        // Doctype should be a child of the document
        List<Node> children = doc.childNodesCopy();
        boolean hasDoctype = false;
        for (Node node : children) {
            if ("#doctype".equals(node.nodeName())) {
                hasDoctype = true;
                break;
            }
        }
        assertTrue(hasDoctype);
    }

    // ========== Empty Input ==========

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyInput() {
        parseXml("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullInput() {
        builder.parse(null, "http://example.com");
    }

    // ========== Whitespace Handling ==========

    @Test
    public void testWhitespaceOnly() {
        Document doc = parseXml("   ");
        // Should produce an empty document
        assertEquals(0, doc.children().size());
    }

    // ========== Malformed XML (Error Recovery) ==========

    @Test
    public void testUnclosedTag() {
        Document doc = parseXml("<root><child></root>");
        // Parser should recover and close the child implicitly
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        assertEquals(1, root.childNodeSize());
        assertEquals("child", root.child(0).tagName());
    }

    @Test
    public void testExtraClosingTag() {
        Document doc = parseXml("<root></root></extra>");
        // Extra closing tag should be ignored
        assertEquals(1, doc.children().size());
        assertEquals("root", doc.child(0).tagName());
    }

    // ========== Namespace ==========

    @Test
    public void testNamespacedElement() {
        Document doc = parseXml("<ns:root xmlns:ns='http://ns'></ns:root>");
        Element root = doc.child(0);
        assertEquals("ns:root", root.tagName());
        assertEquals("http://ns", root.attr("xmlns:ns"));
    }

    // ========== CDATA ==========

    @Test
    public void testCData() {
        Document doc = parseXml("<root><![CDATA[<greeting>]]></root>");
        Element root = doc.child(0);
        assertEquals(1, root.childNodeSize());
        Node cdata = root.childNode(0);
        assertEquals("#cdata", cdata.nodeName());
        assertTrue(cdata.toString().contains("<greeting>"));
    }

    // ========== Processing Instructions ==========

    @Test
    public void testProcessingInstruction() {
        Document doc = parseXml("<?xml version='1.0'?><root></root>");
        // Processing instruction should be a child of the document
        List<Node> children = doc.childNodesCopy();
        boolean hasPi = false;
        for (Node node : children) {
            if ("#pi".equals(node.nodeName())) {
                hasPi = true;
                break;
            }
        }
        assertTrue(hasPi);
    }

    // ========== Deep Nesting ==========

    @Test
    public void testDeepNesting() {
        StringBuilder sb = new StringBuilder("<root>");
        for (int i = 0; i < 100; i++) {
            sb.append("<a>");
        }
        sb.append("deep");
        for (int i = 0; i < 100; i++) {
            sb.append("</a>");
        }
        sb.append("</root>");
        Document doc = parseXml(sb.toString());
        Element root = doc.child(0);
        // Navigate to the deepest element
        Element current = root;
        for (int i = 0; i < 100; i++) {
            assertEquals("a", current.tagName());
            current = current.child(0);
        }
        assertEquals("deep", current.text());
    }

    // ========== Mixed Content ==========

    @Test
    public void testMixedContent() {
        Document doc = parseXml("<root>text1<child/>text2</root>");
        Element root = doc.child(0);
        assertEquals(3, root.childNodeSize());
        // child nodes: text1, child, text2
        assertTrue(root.childNode(0) instanceof org.jsoup.nodes.TextNode);
        assertEquals("text1", ((org.jsoup.nodes.TextNode) root.childNode(0)).text());
        assertEquals("child", root.child(1).tagName());
        assertTrue(root.childNode(2) instanceof org.jsoup.nodes.TextNode);
        assertEquals("text2", ((org.jsoup.nodes.TextNode) root.childNode(2)).text());
    }

    // ========== Bug 77 Specific: Self-closing tag not creating empty element ==========

    @Test
    public void testBug77SelfClosingNotDouble() {
        // This test ensures that a self-closing tag does not produce an extra empty element
        Document doc = parseXml("<root><br/></root>");
        Element root = doc.child(0);
        // The child should be exactly one element (br), not an empty element after br
        assertEquals(1, root.children().size());
        assertEquals("br", root.child(0).tagName());
        // Ensure no extra empty element
        assertEquals(0, root.child(0).children().size());
    }

    @Test
    public void testBug77SelfClosingWithTextAfter() {
        Document doc = parseXml("<root><br/>text</root>");
        Element root = doc.child(0);
        // Should have two children: br and text node
        assertEquals(2, root.childNodeSize());
        assertEquals("br", root.child(0).tagName());
        assertEquals("text", root.child(1).toString().trim());
    }

    // ========== Edge Cases ==========

    @Test
    public void testMultipleRoots() {
        Document doc = parseXml("<a></a><b></b>");
        // Should have two root elements
        assertEquals(2, doc.children().size());
        assertEquals("a", doc.child(0).tagName());
        assertEquals("b", doc.child(1).tagName());
    }

    @Test
    public void testEscapedCharacters() {
        Document doc = parseXml("<root>&amp;&lt;&gt;</root>");
        Element root = doc.child(0);
        assertEquals("&<>", root.text());
    }

    @Test
    public void testAttributeWithSpecialChars() {
        Document doc = parseXml("<root attr='a\"b'></root>");
        Element root = doc.child(0);
        assertEquals("a\"b", root.attr("attr"));
    }
}