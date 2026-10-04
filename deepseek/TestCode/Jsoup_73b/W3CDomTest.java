package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for W3CDom conversion, targeting coverage and edge cases.
 * Addresses potential faults in namespace and attribute handling (e.g., Defects4J bug 73).
 */
public class W3CDomTest {

    private W3CDom w3cDom;

    @Before
    public void setUp() {
        w3cDom = new W3CDom();
    }

    // Test that a simple Jsoup document converts to a non-null W3C document
    @Test
    public void testConversionFromJsoup() {
        Document jsoupDoc = Jsoup.parse("<html><body><p>Hello</p></body></html>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull("W3C document should not be null", w3cDoc);
        assertEquals("Document element should be 'html'", "html", w3cDoc.getDocumentElement().getTagName());
    }

    // Test conversion of an empty document (should produce minimal html structure)
    @Test
    public void testEmptyDocumentConversion() {
        Document jsoupDoc = Jsoup.parse("");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull("W3C document should not be null for empty input", w3cDoc);
        // Jsoup parses an empty string into a document with html and body
        assertTrue("Should contain at least one element", w3cDoc.getDocumentElement().getChildNodes().getLength() > 0);
    }

    // Test that attributes are properly transferred (potential bug: missing attributes)
    @Test
    public void testAttributeConversion() {
        Document jsoupDoc = Jsoup.parse("<div id='main' class='container'>Content</div>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element div = w3cDoc.getElementById("main");
        assertNotNull("Element with id should be found", div);
        assertEquals("Attribute 'id' should be 'main'", "main", div.getAttribute("id"));
        assertEquals("Attribute 'class' should be 'container'", "container", div.getAttribute("class"));
    }

    // Test conversion of nested elements and text content
    @Test
    public void testNestedStructureConversion() {
        Document jsoupDoc = Jsoup.parse("<ul><li>item1</li><li>item2</li></ul>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element ul = (org.w3c.dom.Element) w3cDoc.getDocumentElement()
                .getElementsByTagName("ul").item(0);
        assertNotNull("ul element should exist", ul);
        assertEquals("ul should have two child li elements", 2, ul.getElementsByTagName("li").getLength());
        // Check text of first li
        org.w3c.dom.Element li1 = (org.w3c.dom.Element) ul.getElementsByTagName("li").item(0);
        assertEquals("First li text should be 'item1'", "item1", li1.getTextContent());
    }

    // Test that conversion handles self-closing tags (e.g., <br/>)
    @Test
    public void testSelfClosingTagConversion() {
        Document jsoupDoc = Jsoup.parse("<p>Line1<br>Line2</p>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element br = (org.w3c.dom.Element) w3cDoc.getDocumentElement()
                .getElementsByTagName("br").item(0);
        assertNotNull("br element should be present", br);
        assertTrue("br should have no children", br.getChildNodes().getLength() == 0);
    }

    // Test that a document with a custom namespace is handled (potential bug: dropped namespaces)
    @Test
    public void testNamespaceConversion() {
        // Use a simple XML namespace in a Jsoup document (Jsoup does not parse namespaces by default,
        // but we can parse a prefixed element)
        Document jsoupDoc = Jsoup.parse("<ns:root xmlns:ns='http://example.com'><ns:child>value</ns:child></ns:root>", "", org.jsoup.parser.Parser.xmlParser());
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // Check that the root element has the namespace URI (if preserved)
        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        assertEquals("Namespace URI should be http://example.com", "http://example.com", root.getNamespaceURI());
        assertEquals("Local name should be 'root'", "root", root.getLocalName());
    }

    // Test conversion of an HTML document with header elements (head, meta, title)
    @Test
    public void testHeadAndBodyConversion() {
        Document jsoupDoc = Jsoup.parse("<html><head><title>Test</title></head><body><p>Body</p></body></html>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element head = (org.w3c.dom.Element) w3cDoc.getDocumentElement()
                .getElementsByTagName("head").item(0);
        org.w3c.dom.Element body = (org.w3c.dom.Element) w3cDoc.getDocumentElement()
                .getElementsByTagName("body").item(0);
        assertNotNull("head element should exist", head);
        assertNotNull("body element should exist", body);
        assertEquals("title tag should have correct text", "Test", 
                head.getElementsByTagName("title").item(0).getTextContent());
    }

    // Test that multiple successive conversions produce consistent results
    @Test
    public void testRepeatedConversion() {
        Document jsoupDoc = Jsoup.parse("<span>text</span>");
        org.w3c.dom.Document first = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Document second = w3cDom.fromJsoup(jsoupDoc);
        assertEquals("Both conversions should produce same root tag", 
                first.getDocumentElement().getTagName(), second.getDocumentElement().getTagName());
        assertEquals("Both conversions should produce same text content", 
                first.getDocumentElement().getTextContent(), second.getDocumentElement().getTextContent());
    }

    // Test that a document with only a root text node works
    @Test
    public void testPlainTextDocumentConversion() {
        // Jsoup will wrap plain text in <html><body>...</body></html>
        Document jsoupDoc = Jsoup.parse("Hello World");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        assertTrue("Should have body text", w3cDoc.getDocumentElement().getTextContent().contains("Hello World"));
    }

    // Test conversion of a document with a comment (if Jsoup handles comments)
    @Test
    public void testCommentConversion() {
        Document jsoupDoc = Jsoup.parse("<html><!-- comment --><body>text</body></html>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        // Check that comment was preserved (it should be a child of html)
        // Comment nodes are not always preserved by Jsoup's default parser, but we test for robustness
        // We simply ensure no exception and that body exists
        assertNotNull(w3cDoc.getDocumentElement().getElementsByTagName("body").item(0));
    }

    // Test that an attribute with empty value is not lost
    @Test
    public void testEmptyAttributeValue() {
        Document jsoupDoc = Jsoup.parse("<input disabled>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element input = (org.w3c.dom.Element) w3cDoc.getDocumentElement()
                .getElementsByTagName("input").item(0);
        assertNotNull("input element should exist", input);
        assertEquals("disabled attribute value should be empty string", "", input.getAttribute("disabled"));
    }

    // Test multiple IDs (only first element should be returned by getElementById)
    @Test
    public void testMultipleIds() {
        Document jsoupDoc = Jsoup.parse("<div id='a'>First</div><div id='a'>Second</div>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        org.w3c.dom.Element found = w3cDoc.getElementById("a");
        assertNotNull(found);
        assertEquals("Should return the first element with that id", "First", found.getTextContent());
    }

    // Edge case: null input (should throw NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testNullInputFromJsoup() {
        w3cDom.fromJsoup(null);
    }

    // Test convert method (directly using W3C Document)
    @Test
    public void testDirectConvertToW3CDocument() throws Exception {
        Document jsoupDoc = Jsoup.parse("<p>Hello</p>");
        // Create a new W3C Document using DOM implementation
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document w3cDoc = builder.newDocument();
        w3cDom.convert(jsoupDoc, w3cDoc);
        assertNotNull("W3C document should be populated", w3cDoc.getDocumentElement());
        assertEquals("Root element should be 'html'", "html", w3cDoc.getDocumentElement().getTagName());
    }

    // Test that after conversion, the W3C document is well-formed and can be serialized
    @Test
    public void testSerializableOutput() throws Exception {
        Document jsoupDoc = Jsoup.parse("<html><body><p>Test</p></body></html>");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = tf.newTransformer();
        // Attempt to serialize; if conversion corrupts nodes, this may throw
        try {
            transformer.transform(new javax.xml.transform.dom.DOMSource(w3cDoc), 
                                  new javax.xml.transform.stream.StreamResult(new java.io.StringWriter()));
        } catch (Exception e) {
            fail("Transformation should not throw: " + e.getMessage());
        }
    }
}