package org.jsoup.safety;

import org.junit.Before;
import org.junit.Test;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import static org.junit.Assert.*;

public class CleanerTest {

    private Safelist safelist;
    private Cleaner cleaner;

    @Before
    public void setUp() {
        safelist = Safelist.basic();
        cleaner = new Cleaner(safelist);
    }

    @Test
    public void testConstructorWithNull() {
        try {
            new Cleaner(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCleanValidDocument() {
        String html = "<html><head><title>First</title></head><body><p>Parsed <b>HTML</b>.</p></body></html>";
        Document dirtyDoc = Document.parse(html);
        Document cleanDoc = cleaner.clean(dirtyDoc);

        assertNotNull(cleanDoc);
        // Safelist.basic() allows p and b, but head/title/html might be stripped or handled depending on whitelist/body mechanics
        // Let's check body content specifically via cleaner.isValid
        assertTrue(cleaner.isValid(dirtyDoc));
    }

    @Test
    public void testCleanRemovesDisallowedTagsAndAttributes() {
        String html = "<p class=\"unsafe\" onclick=\"bad()\">Hello <script>alert(1);</script><b>World</b></p>";
        Document dirtyDoc = Document.parse(html);
        Document cleanDoc = cleaner.clean(dirtyDoc);

        String bodyHtml = cleanDoc.body().html();
        assertFalse(bodyHtml.contains("script"));
        assertFalse(bodyHtml.contains("onclick"));
        assertFalse(bodyHtml.contains("unsafe"));
        assertTrue(bodyHtml.contains("<b>World</b>"));
    }

    @Test
    public void testIsValidWithValidDocument() {
        String html = "<p>Hello <b>World</b></p>";
        Document dirtyDoc = Document.parse(html);
        assertTrue(cleaner.isValid(dirtyDoc));
    }

    @Test
    public void testIsValidWithInvalidTag() {
        String html = "<p>Hello <script>alert(1);</script></p>";
        Document dirtyDoc = Document.parse(html);
        assertFalse(cleaner.isValid(dirtyDoc));
    }

    @Test
    public void testIsValidWithInvalidAttribute() {
        String html = "<p onclick=\"bad()\">Hello</p>";
        Document dirtyDoc = Document.parse(html);
        assertFalse(cleaner.isValid(dirtyDoc));
    }

    @Test
    public void testCleanProtocol() {
        safelist.addAttributes("a", "href");
        safelist.addProtocols("a", "href", "http", "https");
        Cleaner customCleaner = new Cleaner(safelist);

        String html = "<a href=\"javascript:alert(1)\">Bad</a><a href=\"https://example.com\">Good</a>";
        Document dirtyDoc = Document.parse(html);
        Document cleanDoc = customCleaner.clean(dirtyDoc);

        String bodyHtml = cleanDoc.body().html();
        assertFalse(bodyHtml.contains("javascript:alert(1)"));
        assertTrue(bodyHtml.contains("https://example.com"));
    }

    @Test
    public void testCopySafeAttributes() {
        // Test internal element copying logic for safe attributes and protocols via clean
        safelist.addAttributes(":all", "data-id");
        Cleaner customCleaner = new Cleaner(safelist);

        Element source = new Element(Tag.valueOf("p"), "").attr("data-id", "123").attr("style", "color:red;");
        Document dirtyDoc = new Document("");
        dirtyDoc.body().appendChild(source);

        Document cleanDoc = customCleaner.clean(dirtyDoc);
        Element cleanedEl = cleanDoc.body().child(0);

        assertEquals("123", cleanedEl.attr("data-id"));
        assertFalse(cleanedEl.hasAttr("style"));
    }

    @Test
    public void testBogusCommentsAndNodesCleaning() {
        String html = "<p><!-- comment -->Hello <b>World</b></p>";
        Document dirtyDoc = Document.parse(html);
        Document cleanDoc = cleaner.clean(dirtyDoc);
        
        assertNotNull(cleanDoc);
    }
}