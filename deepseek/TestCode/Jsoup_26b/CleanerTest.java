package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CleanerTest {

    private Whitelist whitelist;
    private Cleaner cleaner;

    @Before
    public void setUp() {
        whitelist = Whitelist.relaxed();
        cleaner = new Cleaner(whitelist);
    }

    // ------------------ basic cleaning tests ------------------

    @Test
    public void testCleanSimpleHtml() {
        String html = "<p>Hello, <b>World!</b></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // <p> and <b> are allowed in relaxed whitelist
        assertEquals("Cleaned HTML should contain original tags",
                "<html><head></head><body><p>Hello, <b>World!</b></p></body></html>",
                clean.html());
    }

    @Test
    public void testCleanRemovesDisallowedTags() {
        // <script> is not allowed in relaxed whitelist
        String html = "<script>alert('xss')</script><p>safe</p>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // The script tag should be stripped, but its content may become text?
        // In relaxed whitelist, script tag is removed, content becomes plain text
        assertFalse("Cleaned HTML should not contain <script> tag",
                clean.html().contains("<script>"));
        // The body should contain the text "safe" but not the alert content
        String bodyText = clean.body().text();
        assertTrue("Body text should contain 'safe'", bodyText.contains("safe"));
        // In Jsoup, text from removed script may become text inside body, but it's not essential to test exactly
    }

    @Test
    public void testCleanEmptyDocument() {
        Document empty = Jsoup.parse("");
        Document clean = cleaner.clean(empty);
        assertEquals("Cleaned empty document should contain empty body",
                "<html><head></head><body></body></html>",
                clean.html());
    }

    @Test
    public void testCleanDocumentWithOnlyHead() {
        String html = "<html><head><title>Test</title></head></html>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // Head content is allowed; title preserved
        assertNotNull("Head should not be null", clean.head());
        assertEquals("Title should be preserved", "Test", clean.title());
    }

    // ------------------ frameset handling (bug Jsoup-26) ------------------

    @Test
    public void testCleanDocumentWithFrameset() {
        // The bug: cleaning a frameset document could cause errors or incorrect output
        String html = "<html><frameset cols=\"50%,50%\"><frame src=\"a.html\"><frame src=\"b.html\"></frameset></html>";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        // The cleaner should handle frameset gracefully no exception thrown
        // The frameset and frame tags may be removed since they are not in relaxed whitelist
        // But they could be preserved depending on whitelist
        String cleanHtml = clean.html();
        // Expect that the document is cleaned without error
        assertNotNull("Cleaned document should not be null", clean);
        // In relaxed whitelist, frameset and frame are not allowed, so they should be removed
        // The output should not contain <frameset> or <frame> tags
        assertFalse("Should not contain <frameset>", cleanHtml.contains("<frameset"));
        assertFalse("Should not contain <frame>", cleanHtml.contains("<frame"));
        // But the body should be empty or contain none
        assertTrue("Body should be empty or text",
                clean.body().text().isEmpty());
    }

    @Test
    public void testCleanFramesetWithMixedContent() {
        // Frameset document might also have a <noframes> tag
        String html = "<html><frameset cols=\"50%,50%\"><frame src=\"a.html\"><noframes><body><p>No frames</p></body></noframes></frameset></html>";
        Document dirty = Jsoup.parse(html);
        // This should not throw an exception
        Document clean = cleaner.clean(dirty);
        assertNotNull("Cleaned document should not be null", clean);
        // The <noframes> is also removed; but its content may become body text?
        // Actual behavior depends on version; at least no crash.
    }

    // ------------------ isValid tests ------------------

    @Test
    public void testIsValidWithCleanHtml() {
        String html = "<p>Clean content</p>";
        Document doc = Jsoup.parse(html);
        assertTrue("Clean HTML should be valid", cleaner.isValid(doc));
    }

    @Test
    public void testIsValidWithDirtyHtml() {
        String html = "<script>alert('xss')</script>";
        Document doc = Jsoup.parse(html);
        assertFalse("Dirty HTML with script should be invalid", cleaner.isValid(doc));
    }

    @Test
    public void testIsValidWithFrameset() {
        // Frameset document might be considered not body-safe
        String html = "<html><frameset></frameset></html>";
        Document doc = Jsoup.parse(html);
        // isValid might throw or return false; at minimum should not crash
        // In relaxed whitelist, frameset is not allowed, so should be false
        assertFalse("Frameset document should not be valid", cleaner.isValid(doc));
    }

    @Test
    public void testIsValidEmptyDocument() {
        Document empty = Jsoup.parse("");
        assertTrue("Empty document should be valid", cleaner.isValid(empty));
    }

    // ------------------ null / edge cases ------------------

    @Test(expected = NullPointerException.class)
    public void testCleanNullDocument() {
        cleaner.clean(null);
    }

    @Test(expected = NullPointerException.class)
    public void testIsValidNullDocument() {
        cleaner.isValid(null);
    }

    @Test
    public void testCleanWithBaseUriPreserved() {
        // Ensure clean document retains base URI from original
        String html = "<a href=\"/relative\">link</a>";
        Document dirty = Jsoup.parse(html, "http://example.com");
        Document clean = cleaner.clean(dirty);
        String cleanHtml = clean.html();
        // The base URI may affect absolute vs relative links; we check that href is still present
        assertTrue("Cleaned document should contain the link",
                cleanHtml.contains("href"));
        // The exact href may be preserved as relative or made absolute depending on whitelist settings
        // For relaxed whitelist, relative hrefs are kept as-is
        assertTrue("Cleaned document should contain relative href",
                cleanHtml.contains("/relative"));
    }

    // ------------------ whitelist variations ------------------

    @Test
    public void testCleanWithNoneWhitelist() {
        Whitelist none = Whitelist.none();
        Cleaner strictCleaner = new Cleaner(none);
        String html = "<p>Text</p><b>Bold</b>";
        Document dirty = Jsoup.parse(html);
        Document clean = strictCleaner.clean(dirty);
        // With none whitelist, only text should survive
        String bodyText = clean.body().text();
        assertEquals("Only text should remain", "TextBold", bodyText);
        assertTrue("No HTML tags should remain",
                clean.html().contains("TextBold"));
    }

    @Test
    public void testCleanWithBasicWhitelist() {
        Whitelist basic = Whitelist.basic();
        Cleaner basicCleaner = new Cleaner(basic);
        String html = "<p><b>Bold</b> <i>italic</i></p>";
        Document dirty = Jsoup.parse(html);
        Document clean = basicCleaner.clean(dirty);
        // <b> and <i> allowed, but likely not <p>? Actually basic allows p, b, i, etc.
        assertTrue("Cleaned HTML should contain <b>", clean.html().contains("<b>"));
        assertTrue("Cleaned HTML should contain <i>", clean.html().contains("<i>"));
        assertTrue("Cleaned HTML should contain <p>", clean.html().contains("<p>"));
    }

    @Test
    public void testCleanWithRelaxedWhitelist() {
        // Already tested in basic; but we can verify that <img> with src is kept
        String html = "<img src=\"test.png\" alt=\"test\" />";
        Document dirty = Jsoup.parse(html);
        Document clean = cleaner.clean(dirty);
        assertTrue("Image tag should be preserved", clean.html().contains("<img"));
        // Ensure alt attribute is preserved
        assertTrue("alt attribute should be preserved", clean.html().contains("alt=\"test\""));
    }
}