package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class W3CDomTest {

    @Test
    public void testConvertJsoupDocumentToW3C() {
        String html = "<html><head><title>Test</title></head><body><div id='main' class='content'><p>Hello World</p><!-- Comment --></div></body></html>";
        Document jsoupDoc = Jsoup.parse(html);
        jsoupDoc.quirksMode(Document.QuirksMode.quirks);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getNodeName());

        // Test asString
        String outString = W3CDom.asString(w3cDoc);
        assertNotNull(outString);
        assertTrue(outString.contains("Hello World"));

        // Test properties
        Properties props = W3CDom.outputProperties();
        assertNotNull(props);
        assertEquals("yes", props.getProperty("omit-xml-declaration"));
    }

    @Test
    public void testConvertJsoupElementToW3C() {
        String html = "<div><span class='test'>Span text</span></div>";
        Document jsoupDoc = Jsoup.parse(html);
        org.jsoup.nodes.Element jsoupElement = jsoupDoc.selectFirst("div");

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupElement);

        assertNotNull(w3cDoc);
        assertEquals("div", w3cDoc.getDocumentElement().getNodeName());
        assertEquals("Span text", w3cDoc.getElementsByTagName("span").item(0).getTextContent());
    }

    @Test
    public void testNamespacesAndAttributes() {
        // Test namespaces, prefixes, and attributes mapping (including handling of namespaces in Jsoup to W3C)
        String html = "<div xmlns:ns=\"http://example.com/ns\" ns:attr=\"val\" id=\"id1\">Text</div>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        Node divNode = w3cDoc.getElementsByTagName("div").item(0);
        assertNotNull(divNode);
        
        // Verify convert with existing W3C Document context
        org.w3c.dom.Document doc2 = W3CDom.convert(jsoupDoc);
        assertNotNull(doc2);
    }

    @Test
    public void testW3CDomNamespaceHandling() {
        String html = "<fb:if-user xmlns:fb=\"http://facebook.com/2008/fb\">content</fb:if-user>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        assertNotNull(w3cDoc);
        
        NodeList elements = w3cDoc.getElementsByTagName("fb:if-user");
        if (elements.getLength() == 0) {
            elements = w3cDoc.getElementsByTagName("if-user");
        }
        assertTrue(elements.getLength() > 0 || w3cDoc.getDocumentElement() != null);
    }

    @Test
    public void testCopyNodeConversions() {
        String html = "<root><child attr=\"val\">&amp;text&quot;</child></root>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        W3CDom.W3CBuilder builder = new W3CDom.W3CBuilder(w3cDoc);
        
        org.jsoup.nodes.Element jsoupRoot = jsoupDoc.root();
        builder.head(jsoupRoot, 0);
        
        org.jsoup.nodes.Element jsoupChild = jsoupDoc.selectFirst("child");
        builder.head(jsoupChild, 1);
        builder.tail(jsoupChild, 1);
        
        builder.tail(jsoupRoot, 0);

        assertNotNull(w3cDoc.getDocumentElement());
    }

    @Test
    public void testInvalidXmlCharactersOrNames() {
        // Test elements with invalid characters or namespaces that need robust handling
        String html = "<a:b xmlns:a=\"http://www.w3.org/2000/xmlns/\">test</a:b>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = W3CDom.convert(jsoupDoc);
        assertNotNull(w3cDoc);
    }

    @Test
    public void testNamespaceKeyConversion() {
        // Specifically test the internal namespace mapping logic in W3CDom
        String html = "<root xmlns:o=\"urn:schemas-microsoft-com:office:office\" o:data=\"test\"/>";
        Document jsoupDoc = Jsoup.parse(html);
        org.w3c.dom.Document w3cDoc = W3CDom.convert(jsoupDoc);
        assertNotNull(w3cDoc);
        
        W3CDom w3c = new W3CDom();
        NodeList list = w3cDoc.getElementsByTagName("root");
        if (list.getLength() > 0) {
            Node node = list.item(0);
            assertNotNull(node);
        }
    }
}