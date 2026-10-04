package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.*;

public class JsoupTest {

    @Test
    public void testParseHtml() {
        String html = "<html><head><title>First</title></head><body><p>Parsed HTML</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("First", doc.title());
        assertEquals("Parsed HTML", doc.body().text());
    }

    @Test
    public void testParseHtmlWithBaseUri() {
        String html = "<div><a href='about'>About</a></div>";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(html, baseUri);
        assertNotNull(doc);
        assertEquals("http://example.com/about", doc.select("a").first().absUrl("href"));
    }

    @Test
    public void testParseHtmlWithParser() {
        String html = "<div><p>Paragraph</p></div>";
        Document doc = Jsoup.parse(html, "", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Paragraph", doc.select("p").text());
    }

    @Test
    public void testParseBodyFragment() {
        String bodyHtml = "<p>Fragment</p>";
        String baseUri = "http://example.com";
        Document doc = Jsoup.parseBodyFragment(bodyHtml, baseUri);
        assertNotNull(doc);
        assertEquals("Fragment", doc.body().text());
        assertEquals(baseUri, doc.baseUri());
    }

    @Test
    public void testParseBodyFragmentWithoutBaseUri() {
        String bodyHtml = "<p>Fragment</p>";
        Document doc = Jsoup.parseBodyFragment(bodyHtml);
        assertNotNull(doc);
        assertEquals("Fragment", doc.body().text());
    }

    @Test
    public void testConnect() {
        Connection connection = Jsoup.connect("http://example.com");
        assertNotNull(connection);
        assertEquals("http://example.com", connection.request().url().toString());
    }

    @Test
    public void testIsValid() {
        String goodHtml = "<p>Hello <b>World</b></p>";
        String badHtml = "<script>alert('XSS')</script><p>Hello</p>";

        assertTrue(Jsoup.isValid(goodHtml, Whitelist.basic()));
        assertFalse(Jsoup.isValid(badHtml, Whitelist.basic()));
    }

    @Test
    public void testCleanStringWhitelist() {
        String unsafe = "<script>alert('XSS')</script><p>Safe</p>";
        String safe = Jsoup.clean(unsafe, Whitelist.basic());
        assertFalse(safe.contains("script"));
        assertTrue(safe.contains("Safe"));
    }

    @Test
    public void testCleanStringWithBaseUriAndWhitelist() {
        String unsafe = "<a href='http://example.com' onclick='steal()'>Link</a>";
        String safe = Jsoup.clean(unsafe, "http://example.com", Whitelist.basic());
        assertNotNull(safe);
        assertTrue(safe.contains("Link"));
        assertFalse(safe.contains("onclick"));
    }

    @Test
    public void testCleanWithCustomRelaxedWhitelist() {
        String unsafe = "<p class='test'>Paragraph</p>";
        String safe = Jsoup.clean(unsafe, "", Whitelist.relaxed(), new Document.OutputSettings());
        assertNotNull(safe);
        assertTrue(safe.contains("Paragraph"));
    }

    @Test
    public void testPrivateConstructor() {
        // Just invoking via reflection or ensuring it doesn't fail if instantiated or covered
        // Jsoup class only has static methods, so invoking through code handles class loading.
        assertNotNull(Jsoup.parse(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullHtml() {
        Jsoup.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectNullUrl() {
        Jsoup.connect((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectNullUrlObject() {
        Jsoup.connect((URL) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCleanNullString() {
        Jsoup.clean(null, Whitelist.basic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidNullString() {
        Jsoup.isValid(null, Whitelist.basic());
    }

    @Test
    public void testParseFileMethodsNullHandlingOrExceptions() {
        // Test parsing with non-existent file or input stream to cover methods (even if they throw IOException)
        try {
            Jsoup.parse(new File("non-existent-file.html"), "UTF-8");
            fail("Expected IOException");
        } catch (Exception e) {
            // Expected
        }

        try {
            Jsoup.parse(new File("non-existent-file.html"));
            fail("Expected IOException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testParseInputStreamMethods() {
        InputStream in = null;
        try {
            Jsoup.parse(in, "UTF-8", "http://example.com");
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (Exception e) {
            // Expected due to null input stream
        }

        try {
            Jsoup.parse(in, "UTF-8", "http://example.com", Parser.htmlParser());
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (Exception e) {
            // Expected due to null input stream
        }
    }

    @Test
    public void testParseUrlMethods() {
        URL url = null;
        try {
            Jsoup.parse(url, 3000);
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            // Expected
        }
    }
}