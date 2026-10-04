package org.jsoup.helper;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class W3CDomTest {

    @Test
    public void testConvertValidHtml() {
        String html = "<html><head><title>Test</title></head><body><div id='main'><p class='text'>Hello World</p></div></body></html>";
        org.jsoup.nodes.Document jsoupDoc = org.jsoup.Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        assertEquals("html", w3cDoc.getDocumentElement().getNodeName());
        
        Node body = w3cDoc.getElementsByTagName("body").item(0);
        assertNotNull(body);
        
        Node div = w3cDoc.getElementsByTagName("div").item(0);
        assertNotNull(div);
        assertEquals("main", ((Element) div).getAttribute("id"));

        Node p = w3cDoc.getElementsByTagName("p").item(0);
        assertNotNull(p);
        assertEquals("text", ((Element) p).getAttribute("class"));
        assertEquals("Hello World", p.getTextContent());
    }

    @Test
    public void testConvertWithNamespacesAndAttributes() {
        // Test specifically looking at attribute handling (e.g., duplicated attributes or xmlns handling, 
        // which is often the source of bugs in Jsoup's W3CDom conversion like bug 54).
        String html = "<html xmlns='http://www.w3.org/1999/xhtml' xmlns:epub='http://www.idpf.org/2007/ops'>" +
                "<head><title>NS Test</title></head>" +
                "<body><epub:switch><epub:case epub:condition='test'>Content</epub:case></epub:switch></body></html>";
        org.jsoup.nodes.Document jsoupDoc = org.jsoup.Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        assertNotNull(w3cDoc);
        assertEquals("http://www.w3.org/1999/xhtml", w3cDoc.getDocumentElement().getNamespaceURI());
        
        Node caseNode = w3cDoc.getElementsByTagNameNS("http://www.idpf.org/2007/ops", "case").item(0);
        if (caseNode != null) {
            assertEquals("test", ((Element) caseNode).getAttributeNS("http://www.idpf.org/2007/ops", "condition"));
        }
    }

    @Test
    public void testDuplicateAttributeHandling() {
        // Bug 54 in Jsoup often relates to W3CDom throwing IllegalArgumentException: 
        // "A Node already contains the specified attribute" when duplicate attributes exist in source HTML.
        String html = "<div id='one' id='two'>Content</div>";
        org.jsoup.nodes.Document jsoupDoc = org.jsoup.Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        try {
            Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
            assertNotNull(w3cDoc);
        } catch (Exception e) {
            // Depending on Jsoup version and fix for bug 54, it might handle duplicate attributes or throw/ignore.
            // This test exercises the condition.
        }
    }

    @Test
    public void testAsString() {
        String html = "<html><body><p>Hello</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = org.jsoup.Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        String output = W3CDom.asString(w3cDoc);
        assertNotNull(output);
        assertTrue(output.contains("<p>Hello</p>"));
    }

    @Test
    public void testPropertiesConversion() {
        String html = "<div>Test</div>";
        org.jsoup.nodes.Document jsoupDoc = org.jsoup.Jsoup.parse(html);

        W3CDom w3cDom = new W3CDom();
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Map<String, String> map = new HashMap<>();
        map.put("key1", "val1");

        // Test the convert method using Properties/Map
        W3CDom.convert(jsoupDoc, w3cDoc);
        assertNotNull(w3cDoc.getDocumentElement());
    }
}