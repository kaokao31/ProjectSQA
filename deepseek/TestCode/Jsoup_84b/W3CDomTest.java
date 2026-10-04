package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.junit.Assert.*;

/**
 * Test suite for W3CDom conversion, targeting maximum coverage and fault detection.
 * Based on Defects4J bug Jsoup-84 (namespace handling).
 */
public class W3CDomTest {

    private W3CDom w3cDom;
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

    @Before
    public void setUp() throws Exception {
        w3cDom = new W3CDom();
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
    }

    // ---------- Basic conversion tests ----------

    @Test
    public void testFromJsoupSimpleDocument() {
        String html = "<html><head></head><body><p>Hello</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull("W3C document should not be null", w3cDoc);
        assertEquals("Root element should be html", "html", w3cDoc.getDocumentElement().getTagName());
    }

    @Test
    public void testFromJsoupEmptyDocument() {
        Document jsoupDoc = Jsoup.parse("");
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull("W3C document should not be null", w3cDoc);
        // Even empty input produces a minimal document with #root
        assertNotNull("Document element should exist", w3cDoc.getDocumentElement());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoupNullDocument() {
        w3cDom.fromJsoup(null);
    }

    // ---------- Namespace awareness tests ----------

    @Test
    public void testNamespaceAwareDefault() {
        assertTrue("W3CDom should be namespace aware by default", w3cDom.namespaceAware());
    }

    @Test
    public void testSetNamespaceAwareFalse() {
        w3cDom.namespaceAware(false);
        assertFalse("Namespace awareness should be disabled", w3cDom.namespaceAware());
    }

    @Test
    public void testSetNamespaceAwareTrue() {
        w3cDom.namespaceAware(true);
        assertTrue("Namespace awareness should be enabled", w3cDom.namespaceAware());
    }

    // ---------- Conversion with namespaces (bug Jsoup-84) ----------

    @Test
    public void testConvertWithNamespacedElement() {
        String html = "<html xmlns:html='http://www.w3.org/1999/xhtml'>" +
                      "<html:div>Content</html:div></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        // The root element should have the namespace declaration
        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        assertEquals("Root element local name should be html", "html", root.getLocalName());
        assertEquals("Root element namespace should be http://www.w3.org/1999/xhtml",
                     "http://www.w3.org/1999/xhtml", root.getNamespaceURI());

        // The child div should have the correct namespace
        NodeList children = root.getChildNodes();
        boolean foundDiv = false;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element childEl = (org.w3c.dom.Element) child;
                if ("div".equals(childEl.getLocalName())) {
                    assertEquals("Div namespace should be http://www.w3.org/1999/xhtml",
                                 "http://www.w3.org/1999/xhtml", childEl.getNamespaceURI());
                    foundDiv = true;
                }
            }
        }
        assertTrue("Should have found the div element", foundDiv);
    }

    @Test
    public void testConvertWithDefaultNamespace() {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><body><p>Text</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        assertEquals("Root namespace should be http://www.w3.org/1999/xhtml",
                     "http://www.w3.org/1999/xhtml", root.getNamespaceURI());

        // Body and p should inherit the default namespace
        NodeList bodyList = root.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "body");
        assertEquals("Should find one body element", 1, bodyList.getLength());
        NodeList pList = root.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "p");
        assertEquals("Should find one p element", 1, pList.getLength());
    }

    @Test
    public void testConvertWithMultipleNamespaces() {
        String html = "<html xmlns:html='http://www.w3.org/1999/xhtml' xmlns:svg='http://www.w3.org/2000/svg'>" +
                      "<html:div><svg:circle cx='10' cy='10' r='5'/></html:div></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        NodeList divList = root.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "div");
        assertEquals("Should find one div", 1, divList.getLength());
        NodeList circleList = root.getElementsByTagNameNS("http://www.w3.org/2000/svg", "circle");
        assertEquals("Should find one circle", 1, circleList.getLength());
        org.w3c.dom.Element circle = (org.w3c.dom.Element) circleList.item(0);
        assertEquals("Circle cx attribute should be 10", "10", circle.getAttribute("cx"));
    }

    // ---------- Conversion with attributes ----------

    @Test
    public void testConvertWithAttributes() {
        String html = "<html><body><a href='http://example.com' class='link'>Click</a></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        NodeList links = w3cDoc.getElementsByTagName("a");
        assertEquals("Should find one anchor", 1, links.getLength());
        org.w3c.dom.Element anchor = (org.w3c.dom.Element) links.item(0);
        assertEquals("href attribute should be http://example.com",
                     "http://example.com", anchor.getAttribute("href"));
        assertEquals("class attribute should be link", "link", anchor.getAttribute("class"));
    }

    @Test
    public void testConvertWithNamespacedAttributes() {
        String html = "<html xmlns:x='http://example.com/ns'><body x:attr='value'>Text</body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element body = (org.w3c.dom.Element) w3cDoc.getElementsByTagName("body").item(0);
        // Namespaced attribute should be present
        assertTrue("Body should have x:attr attribute", body.hasAttributeNS("http://example.com/ns", "attr"));
        assertEquals("x:attr value should be value",
                     "value", body.getAttributeNS("http://example.com/ns", "attr"));
    }

    // ---------- Conversion with text and comments ----------

    @Test
    public void testConvertWithTextNodes() {
        String html = "<html><body>Hello World</body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element body = (org.w3c.dom.Element) w3cDoc.getElementsByTagName("body").item(0);
        Node firstChild = body.getFirstChild();
        assertNotNull("Body should have a child", firstChild);
        assertEquals("First child should be a text node", Node.TEXT_NODE, firstChild.getNodeType());
        assertEquals("Text content should be Hello World", "Hello World", firstChild.getTextContent());
    }

    @Test
    public void testConvertWithComments() {
        String html = "<html><!-- comment --><body>Text</body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        // Comments are not preserved by default in jsoup -> w3c conversion? Actually jsoup does not preserve comments.
        // But we can test that the conversion doesn't crash.
        assertNotNull("W3C document should not be null", w3cDoc);
    }

    // ---------- Edge cases ----------

    @Test
    public void testConvertWithSelfClosingTags() {
        String html = "<html><body><br/><hr/></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        NodeList brList = w3cDoc.getElementsByTagName("br");
        assertEquals("Should find one br element", 1, brList.getLength());
        NodeList hrList = w3cDoc.getElementsByTagName("hr");
        assertEquals("Should find one hr element", 1, hrList.getLength());
    }

    @Test
    public void testConvertWithDoctype() {
        String html = "<!DOCTYPE html><html><body>Text</body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        // Doctype is not preserved in jsoup -> w3c conversion, but should not cause error.
        assertNotNull("W3C document should not be null", w3cDoc);
    }

    // ---------- Convert method (static) ----------

    @Test
    public void testConvertStaticMethod() throws Exception {
        String html = "<html><body><p>Test</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = builder.newDocument();
        W3CDom.convert(jsoupDoc, w3cDoc);

        assertEquals("Root element should be html", "html", w3cDoc.getDocumentElement().getTagName());
        NodeList pList = w3cDoc.getElementsByTagName("p");
        assertEquals("Should find one p element", 1, pList.getLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertStaticNullJsoup() throws Exception {
        org.w3c.dom.Document w3cDoc = builder.newDocument();
        W3CDom.convert(null, w3cDoc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertStaticNullW3c() throws Exception {
        Document jsoupDoc = Jsoup.parse("<html></html>");
        W3CDom.convert(jsoupDoc, null);
    }

    // ---------- Convert element method ----------

    @Test
    public void testConvertElement() throws Exception {
        String html = "<div><span>Inner</span></div>";
        Document jsoupDoc = Jsoup.parse(html);
        Element jsoupDiv = jsoupDoc.select("div").first();
        org.w3c.dom.Document w3cDoc = builder.newDocument();
        org.w3c.dom.Element w3cDiv = w3cDoc.createElement("div");
        w3cDom.convert(jsoupDiv, w3cDiv);

        // The w3cDiv should now have a child span
        NodeList children = w3cDiv.getChildNodes();
        boolean foundSpan = false;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && "span".equals(child.getNodeName())) {
                foundSpan = true;
                break;
            }
        }
        assertTrue("Should have converted span element", foundSpan);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertElementNullJsoup() throws Exception {
        org.w3c.dom.Document w3cDoc = builder.newDocument();
        org.w3c.dom.Element w3cEl = w3cDoc.createElement("dummy");
        w3cDom.convert((Element) null, w3cEl);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertElementNullW3c() throws Exception {
        Document jsoupDoc = Jsoup.parse("<div></div>");
        Element jsoupDiv = jsoupDoc.select("div").first();
        w3cDom.convert(jsoupDiv, null);
    }

    // ---------- Serialization round-trip ----------

    @Test
    public void testRoundTripSerialization() throws Exception {
        String html = "<html xmlns:html='http://www.w3.org/1999/xhtml'><html:div>Content</html:div></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        // Serialize to string
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(w3cDoc), new StreamResult(writer));
        String output = writer.toString();

        // Verify that namespace prefixes are preserved
        assertTrue("Output should contain xmlns:html", output.contains("xmlns:html"));
        assertTrue("Output should contain html:div", output.contains("html:div"));
    }

    // ---------- Bug-specific test (Jsoup-84) ----------

    @Test
    public void testBug84NamespacePreservation() {
        // This test targets the specific bug: namespace URIs not being set correctly
        String html = "<html xmlns:html='http://www.w3.org/1999/xhtml'>" +
                      "<html:div html:attr='val'>Text</html:div></html>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        org.w3c.dom.Element root = w3cDoc.getDocumentElement();
        NodeList divList = root.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "div");
        assertEquals("Should find one div with correct namespace", 1, divList.getLength());
        org.w3c.dom.Element div = (org.w3c.dom.Element) divList.item(0);

        // Check namespace of div element
        assertEquals("Div namespace should be http://www.w3.org/1999/xhtml",
                     "http://www.w3.org/1999/xhtml", div.getNamespaceURI());

        // Check namespace of attribute
        assertTrue("Div should have html:attr attribute", div.hasAttributeNS("http://www.w3.org/1999/xhtml", "attr"));
        assertEquals("html:attr value should be val",
                     "val", div.getAttributeNS("http://www.w3.org/1999/xhtml", "attr"));
    }
}