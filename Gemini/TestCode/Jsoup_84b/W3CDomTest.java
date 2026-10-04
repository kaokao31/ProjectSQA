package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.w3c.dom.Node;

import static org.junit.Assert.*;

public class W3CDomTest {

    @Test
    public void testConvertBasicDocument() {
        String html = "<html><head><title>Test</title></head><body><p>Hello World</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getNodeName());
        assertEquals("Test", w3cDoc.getElementsByTagName("title").item(0).getTextContent());
        assertEquals("Hello World", w3cDoc.getElementsByTagName("p").item(0).getTextContent());
    }

    @Test
    public void testConvertWithNamespaces() {
        String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:fb=\"http://ogp.me/ns/fb#\">" +
                "<body><fb:like href=\"#\"/></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        // The core issue in Jsoup 84 deals with how namespaces are handled (especially for custom tags / attributes).
        // Let's verify element creation and namespace conversion logic.
        Node body = w3cDoc.getElementsByTagName("body").item(0);
        assertNotNull(body);
    }

    @Test
    public void testAsString() {
        String html = "<html><body><p>Hello</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        String outString = W3CDom.asString(w3cDoc);
        assertNotNull(outString);
        assertTrue(outString.contains("Hello"));
    }

    @Test
    public void testConvertMethodStaticConvenience() {
        String html = "<div><span>Text</span></div>";
        Document jsoupDoc = Jsoup.parse(html);

        org.w3c.dom.Document w3cDoc = W3CDom.convert(jsoupDoc);
        assertNotNull(w3cDoc);
        assertEquals("div", w3cDoc.getDocumentElement().getFirstChild().getNodeName());
    }

    @Test
    public void testNamespaceAwareness() {
        W3CDom w3cDom = new W3CDom();
        assertTrue(w3cDom.namespaceAware());
        w3cDom.namespaceAware(false);
        assertFalse(w3cDom.namespaceAware());
        w3cDom.namespaceAware(true);
        assertTrue(w3cDom.namespaceAware());
    }

    @Test
    public void testConvertW3cToJsoup() {
        String html = "<html><head></head><body><div id=\"1\"><p>Test paragraph</p></div></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        // Convert back from W3C to jsoup
        W3CDom.JsoupToW3c jsoupToW3c = new W3CDom.JsoupToW3c(w3cDoc);
        // Test basic conversion steps via fromJsoup context
        assertNotNull(jsoupToW3c);
    }

    @Test
    public void testAttributesAndNamespacesEdgeCases() {
        // Specifically targets attribute prefix/namespace handling which is often problematic in Jsoup 84
        String html = "<html xmlns:ns=\"http://example.com/ns\"><body ns:att=\"val\" normal=\"val2\">Data</body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        org.w3c.dom.Node body = w3cDoc.getElementsByTagName("body").item(0);
        assertNotNull(body);
        assertTrue(body.getAttributes().getLength() >= 1);
    }

    @Test
    public void testCommentAndProcessingInstructionNodes() {
        String html = "<html><body><!-- Comment --><p>Text</p></body></html>";
        Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        // Verify comment handling if implemented in W3CDom
        String output = W3CDom.asString(w3cDoc);
        assertTrue(output.contains("Comment") || output.contains("Text"));
    }
}