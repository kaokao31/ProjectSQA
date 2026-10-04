package org.jsoup.safety;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
        whitelist = Whitelist.basic();
        cleaner = new Cleaner(whitelist);
    }
    
    @Test
    public void testCleanValidHtml() {
        String dirtyHtml = "<p>Hello <b>World</b></p>";
        Document dirty = Jsoup.parse(dirtyHtml);
        Document clean = cleaner.clean(dirty);
        assertEquals("<html><head></head><body><p>Hello <b>World</b></p></body></html>", clean.html());
    }
    
    @Test
    public void testCleanRemovesDisallowedTags() {
        String dirtyHtml = "<script>alert('xss')</script><p>Safe</p>";
        Document dirty = Jsoup.parse(dirtyHtml);
        Document clean = cleaner.clean(dirty);
        assertFalse(clean.text().contains("alert"));
        assertTrue(clean.text().contains("Safe"));
    }
    
    @Test
    public void testCleanRemovesAttributesNotInWhitelist() {
        Whitelist strictWhitelist = Whitelist.none();
        strictWhitelist.addTags("a");
        Cleaner strictCleaner = new Cleaner(strictWhitelist);
        String dirtyHtml = "<a href='http://evil.com' onclick='steal()' style='color:red'>Link</a>";
        Document dirty = Jsoup.parse(dirtyHtml);
        Document clean = strictCleaner.clean(dirty);
        Element link = clean.select("a").first();
        assertNotNull(link);
        assertNull(link.attr("href"));
        assertNull(link.attr("onclick"));
        assertNull(link.attr("style"));
    }
    
    @Test
    public void testCleanHandlesNullDocument() {
        try {
            cleaner.clean(null);
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (NullPointerException e) {
            // Expected
        } catch (IllegalArgumentException e) {
            // Also acceptable
        }
    }
    
    @Test
    public void testCleanEmptyDocument() {
        Document empty = Jsoup.parse("");
        Document clean = cleaner.clean(empty);
        assertNotNull(clean);
        assertEquals("<html><head></head><body></body></html>", clean.html());
    }
    
    @Test
    public void testCleanWithNoBodyContent() {
        Document noBody = Jsoup.parse("<html><head><title>Test</title></head><body></body></html>");
        Document clean = cleaner.clean(noBody);
        assertNotNull(clean);
        assertEquals("<html><head></head><body></body></html>", clean.html());
    }
    
    @Test
    public void testCleanPreservesAllowedAttributes() {
        Whitelist linkWhitelist = Whitelist.none();
        linkWhitelist.addTags("a");
        linkWhitelist.addAttributes("a", "href");
        Cleaner linkCleaner = new Cleaner(linkWhitelist);
        String dirtyHtml = "<a href='http://example.com' title='test'>Link</a>";
        Document dirty = Jsoup.parse(dirtyHtml);
        Document clean = linkCleaner.clean(dirty);
        Element link = clean.select("a").first();
        assertNotNull(link);
        assertEquals("http://example.com", link.attr("href"));
        assertEquals("", link.attr("title"));
    }
    
    @Test
    public void testCleanNestedTags() {
        String nestedHtml = "<div><p><span>Text</span></p></div>";
        Document dirty = Jsoup.parse(nestedHtml);
        Document clean = cleaner.clean(dirty);
        assertEquals("<html><head></head><body><span>Text</span></body></html>", clean.html());
    }
    
    @Test
    public void testCleanTextOnly() {
        String textOnly = "Just text";
        Document dirty = Jsoup.parse(textOnly);
        Document clean = cleaner.clean(dirty);
        assertEquals("<html><head></head><body>Just text</body></html>", clean.html());
    }
    
    @Test
    public void testCleanWithComments() {
        String withComments = "<!-- comment --><p>Visible</p>";
        Document dirty = Jsoup.parse(withComments);
        Document clean = cleaner.clean(dirty);
        assertFalse(clean.html().contains("<!--"));
        assertTrue(clean.text().contains("Visible"));
    }
    
    @Test
    public void testCleanSelfClosingTags() {
        String selfClosingHtml = "<br><hr><img src='test.jpg'>";
        Document dirty = Jsoup.parse(selfClosingHtml);
        Document clean = cleaner.clean(dirty);
        assertFalse(clean.html().contains("<img"));
        assertTrue(clean.html().contains("<br>") || clean.html().contains("<br />"));
    }
    
    @Test
    public void testCleanMalformedHtml() {
        String malformed = "<p>Unclosed <b>Bold";
        Document dirty = Jsoup.parse(malformed);
        Document clean = cleaner.clean(dirty);
        assertNotNull(clean);
        assertTrue(clean.text().contains("Unclosed Bold"));
        assertEquals("<html><head></head><body><p>Unclosed <b>Bold</b></p></body></html>", clean.html());
    }
    
    @Test
    public void testIsValidReturnsTrueForCleanContent() {
        String cleanHtml = "<p>Safe content</p>";
        Document doc = Jsoup.parse(cleanHtml);
        assertTrue(cleaner.isValid(doc));
    }
    
    @Test
    public void testIsValidReturnsFalseForDirtyContent() {
        String dirtyHtml = "<script>alert('xss')</script>";
        Document doc = Jsoup.parse(dirtyHtml);
        assertFalse(cleaner.isValid(doc));
    }
    
    @Test
    public void testIsValidWithNullDocument() {
        try {
            cleaner.isValid(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }
    
    @Test
    public void testIsValidWithEmptyDocument() {
        Document empty = Jsoup.parse("");
        assertTrue(cleaner.isValid(empty));
    }
    
    @Test
    public void testCleanWithSpecialCharacters() {
        String specialChars = "<p>© &amp; <b>test</b></p>";
        Document dirty = Jsoup.parse(specialChars);
        Document clean = cleaner.clean(dirty);
        assertTrue(clean.text().contains("©"));
        assertTrue(clean.text().contains("&"));
        assertTrue(clean.text().contains("test"));
    }
    
    @Test
    public void testCleanAndIsValidWithSameInput() {
        String inputHtml = "<p>Test <script>alert(1)</script></p>";
        Document dirty = Jsoup.parse(inputHtml);
        assertFalse(cleaner.isValid(dirty));
        Document clean = cleaner.clean(dirty);
        assertTrue(cleaner.isValid(clean));
    }
    
    @Test
    public void testCleanWithMultipleLevelsOfNesting() {
        String nestedHtml = "<ul><li><a href='http://example.com'><span>Link</span></a></li></ul>";
        Document dirty = Jsoup.parse(nestedHtml);
        Document clean = cleaner.clean(dirty);
        Element link = clean.select("a").first();
        assertNull(link);
        Element span = clean.select("span").first();
        assertNotNull(span);
        assertEquals("Link", span.text());
    }
    
    @Test
    public void testCleanRemovesStyleAttributes() {
        Whitelist noStyleWhitelist = Whitelist.none();
        noStyleWhitelist.addTags("p", "span");
        Cleaner noStyleCleaner = new Cleaner(noStyleWhitelist);
        String styledHtml = "<p style='color:red;font-size:20px;'>Styled</p>";
        Document dirty = Jsoup.parse(styledHtml);
        Document clean = noStyleCleaner.clean(dirty);
        Element p = clean.select("p").first();
        assertNotNull(p);
        assertFalse(p.html().contains("style"));
        assertEquals("Styled", p.text());
    }
    
    @Test
    public void testCleanPreservesOnlyWhitelistedTagsInNestedContent() {
        Whitelist limitedWhitelist = Whitelist.none();
        limitedWhitelist.addTags("b", "i");
        Cleaner limitedCleaner = new Cleaner(limitedWhitelist);
        String mixedHtml = "<div><b>Bold</b><p>Paragraph</p><i>Italic</i></div>";
        Document dirty = Jsoup.parse(mixedHtml);
        Document clean = limitedCleaner.clean(dirty);
        assertFalse(clean.html().contains("<div>"));
        assertFalse(clean.html().contains("<p>"));
        assertTrue(clean.html().contains("<b>Bold</b>"));
        assertTrue(clean.html().contains("<i>Italic</i>"));
    }
}