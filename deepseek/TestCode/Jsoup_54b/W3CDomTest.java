package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for W3CDom conversion utility.
 * Targets maximum coverage and fault detection for Defects4J bug 54.
 */
public class W3CDomTest {

    private W3CDom w3cDom;
    private Document jsoupDoc;

    @Before
    public void setUp() {
        w3cDom = new W3CDom();
    }

    // ---------- Basic Conversion Tests ----------

    @Test
    public void testSimpleDocumentConversion() {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull("W3C document should not be null", w3cDoc);
        assertEquals("Root element should be html", "html", w3cDoc.getDocumentElement().getNodeName());
        NodeList bodyChildren = w3cDoc.getDocumentElement().getElementsByTagName("body");
        assertEquals("Should have one body element", 1, bodyChildren.getLength());
        Node pNode = bodyChildren.item(0).getFirstChild();
        assertEquals("First child of body should be p", "p", pNode.getNodeName());
        assertEquals("Text content of p should be Hello", "Hello", pNode.getTextContent());
    }

    @Test
    public void testEmptyDocumentConversion() {
        jsoupDoc = Jsoup.parse("");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull("W3C document should not be null", w3cDoc);
        // Empty Jsoup doc still has html/head/body structure
        assertNotNull("Document element should exist", w3cDoc.getDocumentElement());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullJsoupDocument() {
        w3cDom.fromJsoup(null);
    }

    // ---------- Attribute Handling Tests ----------

    @Test
    public void testAttributesConversion() {
        String html = "<div id='main' class='container' data-value='123'>Content</div>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node divNode = w3cDoc.getDocumentElement().getElementsByTagName("div").item(0);
        org.w3c.dom.NamedNodeMap attrs = divNode.getAttributes();
        assertEquals("Should have 3 attributes", 3, attrs.getLength());
        assertEquals("id attribute value", "main", attrs.getNamedItem("id").getNodeValue());
        assertEquals("class attribute value", "container", attrs.getNamedItem("class").getNodeValue());
        assertEquals("data-value attribute value", "123", attrs.getNamedItem("data-value").getNodeValue());
    }

    @Test
    public void testBooleanAttributeConversion() {
        // HTML boolean attributes like disabled, checked, selected
        String html = "<input type='checkbox' checked disabled>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node inputNode = w3cDoc.getDocumentElement().getElementsByTagName("input").item(0);
        org.w3c.dom.NamedNodeMap attrs = inputNode.getAttributes();
        // In Jsoup, boolean attributes are stored with empty string value
        assertNotNull("checked attribute should exist", attrs.getNamedItem("checked"));
        assertEquals("checked value should be empty string", "", attrs.getNamedItem("checked").getNodeValue());
        assertNotNull("disabled attribute should exist", attrs.getNamedItem("disabled"));
        assertEquals("disabled value should be empty string", "", attrs.getNamedItem("disabled").getNodeValue());
    }

    @Test
    public void testAttributeWithNamespace() {
        // Attributes with namespace prefix (e.g., xml:lang)
        String html = "<div xml:lang='en'>Hello</div>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node divNode = w3cDoc.getDocumentElement().getElementsByTagName("div").item(0);
        org.w3c.dom.NamedNodeMap attrs = divNode.getAttributes();
        // In W3C DOM, namespace attributes are represented with namespace URI
        // Jsoup may or may not preserve namespace; check for attribute existence
        assertNotNull("xml:lang attribute should exist", attrs.getNamedItem("xml:lang"));
        assertEquals("xml:lang value", "en", attrs.getNamedItem("xml:lang").getNodeValue());
    }

    // ---------- Namespace Handling Tests ----------

    @Test
    public void testXhtmlNamespaceConversion() {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><body><p>Test</p></body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // The root element should have the namespace
        String ns = w3cDoc.getDocumentElement().getNamespaceURI();
        assertEquals("Namespace should be XHTML", "http://www.w3.org/1999/xhtml", ns);
    }

    @Test
    public void testCustomNamespaceConversion() {
        String html = "<html xmlns:custom='http://example.com'><custom:div>Content</custom:div></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node customDiv = w3cDoc.getDocumentElement().getElementsByTagNameNS("http://example.com", "div").item(0);
        assertNotNull("Custom namespace element should exist", customDiv);
        assertEquals("Local name should be div", "div", customDiv.getLocalName());
        assertEquals("Namespace URI", "http://example.com", customDiv.getNamespaceURI());
    }

    // ---------- Special Characters and Escaping ----------

    @Test
    public void testSpecialCharactersInText() {
        String html = "<p>AT&T &amp; &lt;test&gt;</p>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node pNode = w3cDoc.getDocumentElement().getElementsByTagName("p").item(0);
        // Text content should be unescaped
        assertEquals("Text content should be unescaped", "AT&T & <test>", pNode.getTextContent());
    }

    @Test
    public void testSpecialCharactersInAttribute() {
        String html = "<a href='http://example.com?a=1&amp;b=2'>Link</a>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node aNode = w3cDoc.getDocumentElement().getElementsByTagName("a").item(0);
        String href = aNode.getAttributes().getNamedItem("href").getNodeValue();
        assertEquals("Attribute value should be unescaped", "http://example.com?a=1&b=2", href);
    }

    // ---------- Edge Cases ----------

    @Test
    public void testDocumentWithOnlyText() {
        String html = "Just text";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // Jsoup wraps text in <html><head></head><body>Just text</body></html>
        Node body = w3cDoc.getDocumentElement().getElementsByTagName("body").item(0);
        assertEquals("Body should contain text node", Node.TEXT_NODE, body.getFirstChild().getNodeType());
        assertEquals("Text content", "Just text", body.getFirstChild().getTextContent());
    }

    @Test
    public void testDocumentWithMultipleChildren() {
        String html = "<ul><li>Item1</li><li>Item2</li></ul>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        NodeList liNodes = w3cDoc.getDocumentElement().getElementsByTagName("li");
        assertEquals("Should have two li elements", 2, liNodes.getLength());
        assertEquals("First li text", "Item1", liNodes.item(0).getTextContent());
        assertEquals("Second li text", "Item2", liNodes.item(1).getTextContent());
    }

    @Test
    public void testConversionOfSelfClosingTags() {
        String html = "<br><hr><img src='test.jpg'>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        NodeList brNodes = w3cDoc.getDocumentElement().getElementsByTagName("br");
        assertEquals("Should have one br element", 1, brNodes.getLength());
        NodeList hrNodes = w3cDoc.getDocumentElement().getElementsByTagName("hr");
        assertEquals("Should have one hr element", 1, hrNodes.getLength());
        NodeList imgNodes = w3cDoc.getDocumentElement().getElementsByTagName("img");
        assertEquals("Should have one img element", 1, imgNodes.getLength());
        assertEquals("img src attribute", "test.jpg", imgNodes.item(0).getAttributes().getNamedItem("src").getNodeValue());
    }

    // ---------- W3CDom Specific Methods ----------

    @Test
    public void testNamespaceAwareConversion() {
        // Test that namespaceAware flag works (default true)
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><body><p>Test</p></body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertTrue("Namespace aware should be true by default", w3cDom.namespaceAware());
        // Check that elements have namespace
        Node pNode = w3cDoc.getDocumentElement().getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "p").item(0);
        assertNotNull("p element should have namespace", pNode);
    }

    @Test
    public void testDisableNamespaceAware() {
        w3cDom.namespaceAware(false);
        assertFalse("Namespace aware should be false", w3cDom.namespaceAware());
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><body><p>Test</p></body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // When namespace aware is false, elements should not have namespace
        Node pNode = w3cDoc.getDocumentElement().getElementsByTagName("p").item(0);
        assertNull("p element should have no namespace when disabled", pNode.getNamespaceURI());
    }

    @Test
    public void testPropertiesConversion() {
        // Test conversion of Jsoup properties to W3C properties (if any)
        // This is a placeholder for potential property handling
        String html = "<html><head><meta charset='utf-8'></head><body></body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // Ensure no exception is thrown
        assertNotNull(w3cDoc);
    }

    // ---------- Potential Bug Trigger (Defects4J #54) ----------

    @Test
    public void testAttributeWithEmptyValue() {
        // Bug scenario: attribute with empty string value might cause issues
        String html = "<div data-empty=''></div>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node divNode = w3cDoc.getDocumentElement().getElementsByTagName("div").item(0);
        org.w3c.dom.NamedNodeMap attrs = divNode.getAttributes();
        assertNotNull("data-empty attribute should exist", attrs.getNamedItem("data-empty"));
        assertEquals("data-empty value should be empty string", "", attrs.getNamedItem("data-empty").getNodeValue());
    }

    @Test
    public void testMultipleAttributesWithSameLocalNameDifferentPrefix() {
        // Test handling of attributes with same local name but different prefixes
        String html = "<div xmlns:a='http://a' xmlns:b='http://b' a:attr='1' b:attr='2'></div>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node divNode = w3cDoc.getDocumentElement().getElementsByTagName("div").item(0);
        org.w3c.dom.NamedNodeMap attrs = divNode.getAttributes();
        // In W3C DOM, attributes with different namespaces are distinct
        org.w3c.dom.Attr attrA = (org.w3c.dom.Attr) attrs.getNamedItemNS("http://a", "attr");
        org.w3c.dom.Attr attrB = (org.w3c.dom.Attr) attrs.getNamedItemNS("http://b", "attr");
        assertNotNull("Attribute with namespace a should exist", attrA);
        assertNotNull("Attribute with namespace b should exist", attrB);
        assertEquals("Value of a:attr", "1", attrA.getValue());
        assertEquals("Value of b:attr", "2", attrB.getValue());
    }

    @Test
    public void testConversionOfCommentNodes() {
        String html = "<html><!-- comment --><body>Text</body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // Comment nodes should be preserved
        NodeList comments = w3cDoc.getDocumentElement().getChildNodes();
        boolean foundComment = false;
        for (int i = 0; i < comments.getLength(); i++) {
            if (comments.item(i).getNodeType() == Node.COMMENT_NODE) {
                foundComment = true;
                assertEquals("Comment content", " comment ", comments.item(i).getTextContent());
                break;
            }
        }
        assertTrue("Comment node should be present", foundComment);
    }

    @Test
    public void testConversionOfProcessingInstructions() {
        // Jsoup does not support processing instructions, but ensure no crash
        String html = "<?xml version='1.0'?><html><body></body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
    }

    // ---------- Additional Edge Cases ----------

    @Test
    public void testDeeplyNestedDocument() {
        StringBuilder sb = new StringBuilder("<html><body>");
        for (int i = 0; i < 100; i++) {
            sb.append("<div>");
        }
        sb.append("Deep");
        for (int i = 0; i < 100; i++) {
            sb.append("</div>");
        }
        sb.append("</body></html>");
        jsoupDoc = Jsoup.parse(sb.toString());
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull("Deeply nested document should convert without error", w3cDoc);
        NodeList divs = w3cDoc.getDocumentElement().getElementsByTagName("div");
        assertEquals("Should have 100 div elements", 100, divs.getLength());
    }

    @Test
    public void testDocumentWithDoctype() {
        String html = "<!DOCTYPE html><html><body>Hello</body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // Doctype should be preserved in W3C document
        org.w3c.dom.DocumentType doctype = w3cDoc.getDoctype();
        assertNotNull("Doctype should not be null", doctype);
        assertEquals("Doctype name should be html", "html", doctype.getName());
    }

    @Test
    public void testConversionOfScriptAndStyleTags() {
        String html = "<html><head><script>alert('test');</script><style>body{color:red}</style></head><body></body></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node scriptNode = w3cDoc.getDocumentElement().getElementsByTagName("script").item(0);
        Node styleNode = w3cDoc.getDocumentElement().getElementsByTagName("style").item(0);
        assertNotNull("Script element should exist", scriptNode);
        assertNotNull("Style element should exist", styleNode);
        assertEquals("Script content", "alert('test');", scriptNode.getTextContent());
        assertEquals("Style content", "body{color:red}", styleNode.getTextContent());
    }

    @Test
    public void testConversionWithMultipleNamespaces() {
        String html = "<html xmlns:ns1='http://one' xmlns:ns2='http://two'><ns1:div ns1:attr='val1' ns2:attr='val2'>Text</ns1:div></html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node divNode = w3cDoc.getDocumentElement().getElementsByTagNameNS("http://one", "div").item(0);
        assertNotNull("Div with namespace one should exist", divNode);
        org.w3c.dom.NamedNodeMap attrs = divNode.getAttributes();
        org.w3c.dom.Attr attr1 = (org.w3c.dom.Attr) attrs.getNamedItemNS("http://one", "attr");
        org.w3c.dom.Attr attr2 = (org.w3c.dom.Attr) attrs.getNamedItemNS("http://two", "attr");
        assertNotNull("Attribute ns1:attr should exist", attr1);
        assertNotNull("Attribute ns2:attr should exist", attr2);
        assertEquals("ns1:attr value", "val1", attr1.getValue());
        assertEquals("ns2:attr value", "val2", attr2.getValue());
    }

    // ---------- Test for W3CDom internal methods (if accessible) ----------

    @Test
    public void testConvertElementWithChildren() {
        // Ensure that child elements are properly converted
        String html = "<div><span>Child</span></div>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node divNode = w3cDoc.getDocumentElement().getElementsByTagName("div").item(0);
        NodeList children = divNode.getChildNodes();
        boolean foundSpan = false;
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeName().equals("span")) {
                foundSpan = true;
                assertEquals("Span text", "Child", children.item(i).getTextContent());
                break;
            }
        }
        assertTrue("Span child should be present", foundSpan);
    }

    @Test
    public void testConversionOfTextNodesWithWhitespace() {
        String html = "<p>   Text with spaces   </p>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node pNode = w3cDoc.getDocumentElement().getElementsByTagName("p").item(0);
        // Jsoup preserves whitespace in text nodes
        assertEquals("Text content should preserve whitespace", "   Text with spaces   ", pNode.getTextContent());
    }

    @Test
    public void testConversionOfMultipleTextNodes() {
        String html = "<p>First <b>bold</b> Second</p>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node pNode = w3cDoc.getDocumentElement().getElementsByTagName("p").item(0);
        NodeList children = pNode.getChildNodes();
        // Should have three children: text, b, text
        assertEquals("Should have 3 child nodes", 3, children.getLength());
        assertEquals("First child should be text", Node.TEXT_NODE, children.item(0).getNodeType());
        assertEquals("First text content", "First ", children.item(0).getTextContent());
        assertEquals("Second child should be b", "b", children.item(1).getNodeName());
        assertEquals("Third child should be text", Node.TEXT_NODE, children.item(2).getNodeType());
        assertEquals("Third text content", " Second", children.item(2).getTextContent());
    }

    // ---------- Test for potential null pointer or exception paths ----------

    @Test
    public void testConversionOfDocumentWithOnlyDoctype() {
        // Edge case: document with only doctype and no content
        String html = "<!DOCTYPE html>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        assertNotNull(w3cDoc.getDoctype());
    }

    @Test
    public void testConversionOfDocumentWithInvalidHtml() {
        // Malformed HTML should still convert without exception
        String html = "<div><p>Unclosed";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
    }

    @Test
    public void testConversionOfDocumentWithMultipleRootElements() {
        // Jsoup will wrap in html, so this should be fine
        String html = "<div>First</div><div>Second</div>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        NodeList divs = w3cDoc.getDocumentElement().getElementsByTagName("div");
        assertEquals("Should have two div elements", 2, divs.getLength());
    }

    @Test
    public void testConversionOfDocumentWithCData() {
        // Jsoup does not support CDATA sections, but ensure no crash
        String html = "<script><![CDATA[var x = 1;]]></script>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        Node scriptNode = w3cDoc.getDocumentElement().getElementsByTagName("script").item(0);
        // CDATA may be converted to text or comment; just check no exception
        assertNotNull(scriptNode);
    }

    @Test
    public void testConversionOfDocumentWithEntityReferences() {
        String html = "<p>&amp; &lt; &gt; &quot; &apos;</p>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node pNode = w3cDoc.getDocumentElement().getElementsByTagName("p").item(0);
        // Entities should be resolved to characters
        assertEquals("Text should contain resolved entities", "& < > \" '", pNode.getTextContent());
    }

    @Test
    public void testConversionOfDocumentWithNonAsciiCharacters() {
        String html = "<p>日本語 émoji 🎉</p>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node pNode = w3cDoc.getDocumentElement().getElementsByTagName("p").item(0);
        assertEquals("Text should contain non-ASCII characters", "日本語 émoji 🎉", pNode.getTextContent());
    }

    // ---------- Test for W3CDom static methods (if any) ----------

    @Test
    public void testSourceNodes() {
        // W3CDom provides a method to retrieve source Jsoup nodes from W3C nodes
        String html = "<p id='test'>Hello</p>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Node pNode = w3cDoc.getDocumentElement().getElementsByTagName("p").item(0);
        // The source Jsoup node should be retrievable via W3CDom.sourceNodes
        Map<Node, org.jsoup.nodes.Node> sourceNodes = w3cDom.sourceNodes(w3cDoc, org.jsoup.nodes.Node.class);
        assertNotNull("Source nodes map should not be null", sourceNodes);
        assertTrue("Map should contain the p node", sourceNodes.containsKey(pNode));
        org.jsoup.nodes.Node jsoupNode = sourceNodes.get(pNode);
        assertNotNull("Jsoup node should not be null", jsoupNode);
        assertEquals("Jsoup node should be a p element", "p", jsoupNode.nodeName());
    }

    @Test
    public void testSourceNodesWithNullDocument() {
        // Should return empty map or handle gracefully
        Map<Node, org.jsoup.nodes.Node> sourceNodes = w3cDom.sourceNodes(null, org.jsoup.nodes.Node.class);
        assertNotNull("Source nodes map should not be null", sourceNodes);
        assertTrue("Map should be empty", sourceNodes.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSourceNodesWithInvalidClass() {
        // If class is not a subclass of Node, should throw
        String html = "<p>Test</p>";
        jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        w3cDom.sourceNodes(w3cDoc, String.class);
    }
}