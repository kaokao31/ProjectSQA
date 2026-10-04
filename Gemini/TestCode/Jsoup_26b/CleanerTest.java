package org.jsoup.safety;

import org.junit.Before;
import org.junit.Test;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import static org.junit.Assert.*;

public class CleanerTest {

    private Cleaner cleaner;
    private Safelist safelist;

    @Before
    public void setUp() {
        safelist = Safelist.basic();
        cleaner = new Cleaner(safelist);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullSafelistConstructor() {
        new Cleaner(null);
    }

    @Test
    public void testCleanValidDocument() {
        String html = "<html><head><title>Test</title></head><body><p>Hello <b>World</b></p></body></html>";
        Document dirtyDocument = Document.createShell("");
        dirtyDocument.body().append("<p>Hello <b>World</b></p>");
        
        Document cleanedDoc = cleaner.clean(dirtyDocument);
        assertNotNull(cleanedDoc);
        assertTrue(cleanedDoc.body().html().contains("Hello"));
        assertTrue(cleanedDoc.body().html().contains("<b>World</b>"));
    }

    @Test
    public void testCleanUnsafeTagsAndAttributes() {
        String unsafeHtml = "<div><script>alert('hack');</script><p class=\"unsafe\" onclick=\"evil()\">Safe paragraph</p><a href=\"javascript:alert(1)\" onclick=\"evil()\">Link</a></div>";
        Document dirtyDocument = Document.createShell("");
        dirtyDocument.body().html(unsafeHtml);

        Document cleanedDoc = cleaner.clean(dirtyDocument);
        String bodyHtml = cleanedDoc.body().html();

        assertFalse(bodyHtml.contains("script"));
        assertFalse(bodyHtml.contains("onclick"));
        assertFalse(bodyHtml.contains("javascript:"));
        assertTrue(bodyHtml.contains("Safe paragraph"));
        assertTrue(bodyHtml.contains("href"));
    }

    @Test
    public void testIsValidWithValidDocument() {
        Document validDoc = Document.createShell("");
        validDoc.body().append("<p>Hello <b>World</b></p>");

        assertTrue(cleaner.isValid(validDoc));
    }

    @Test
    vido testIsValidWithInvalidDocument() {
        Document invalidDoc = Document.createShell("");
        invalidDoc.body().append("<script>alert('hack');</script><p onclick=\"evil()\">Bad</p>");

        assertFalse(cleaner.isValid(invalidDoc));
    }

    @Test
    public void testIsValidBodyTextWithValid() {
        String validHtml = "<p>Just some text</p>";
        assertTrue(cleaner.isValidBodyHtml(validHtml));
    }

    @Test
    public void testIsValidBodyTextWithInvalid() {
        String invalidHtml = "<script>alert(1)</script><object data=\"foo\"></object>";
        assertFalse(cleaner.isValidBodyHtml(invalidHtml));
    }

    @Test
    public void testCleanPreservesValidProtocols() {
        safelist.addAttributes(":all", "cite");
        safelist.addProtocols("a", "cite", "http", "https");

        Document dirty = Document.createShell("");
        dirty.body().append("<a href=\"http://example.com\" cite=\"https://example.com\">Link</a>");

        Document cleaned = cleaner.clean(dirty);
        assertTrue(cleaned.body().html().contains("http://example.com"));
        assertTrue(cleaned.body().html().contains("https://example.com"));
    }

    @Test
    public void testCopySafeElementMetadataAndChildren() {
        // Specifically targeting internal ElementMeta structure if applicable via normal clean
        Document dirty = Document.createShell("");
        Element source = new Element(Tag.valueOf("p"), "");
        source.attr("style", "color:red;"); // unsafe attribute in basic safelist
        source.attr("class", "my-class");   // unsafe in basic safelist
        source.text("Test text");
        dirty.body().appendChild(source);

        Document cleaned = cleaner.clean(dirty);
        assertNotNull(cleaned);
        // The paragraph itself should be kept if allowed, but its unsafe attributes stripped
        Element cleanedP = cleaned.body().child(0);
        assertEquals("p", cleanedP.tagName());
        assertFalse(cleanedP.hasAttr("style"));
        assertFalse(cleanedP.hasAttr("class"));
        assertEquals("Test text", cleanedP.text());
    }

    @Test
    public void testNestedElementsCleaning() {
        Document dirty = Document.createShell("");
        dirty.body().html("<div><ul><li><script>bad()</script><b>Good item</b></li></ul></div>");

        Document cleaned = cleaner.clean(dirty);
        String html = cleaned.body().html();
        assertFalse(html.contains("script"));
        assertFalse(html.contains("div")); // Assuming div is not in basic safelist
        assertTrue(html.contains("ul"));
        assertTrue(html.contains("li"));
        assertTrue(html.contains("<b>Good item</b>"));
    }
}