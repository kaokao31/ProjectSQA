package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {

    @Test
    public void testConstructorAndOuterHtml_NoPublicNoSystem() {
        DocumentType doctype = new DocumentType("html", "", "");
        String expected = "<!DOCTYPE html>";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithPublicIdOnly() {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "");
        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithSystemIdOnly() {
        DocumentType doctype = new DocumentType("html", "", "http://www.w3.org/TR/html4/strict.dtd");
        // Bug: In Jsoup 1.8.1 (bug 40), this would incorrectly produce "<!DOCTYPE html PUBLIC \"\" \"http://...\">"
        // Correct output should be "<!DOCTYPE html SYSTEM \"http://...\">"
        String expected = "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/strict.dtd\">";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithBothPublicAndSystem() {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd");
        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithEmptyPublicAndEmptySystem() {
        DocumentType doctype = new DocumentType("html", "", "");
        assertEquals("<!DOCTYPE html>", doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithNullPublicAndNullSystem() {
        // Constructor may accept null; test null handling
        DocumentType doctype = new DocumentType("html", null, null);
        // Expected: treat null as empty string
        assertEquals("<!DOCTYPE html>", doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithNullPublicAndSystemId() {
        DocumentType doctype = new DocumentType("html", null, "http://example.com/dtd");
        String expected = "<!DOCTYPE html SYSTEM \"http://example.com/dtd\">";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithPublicIdAndNullSystem() {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", null);
        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithEmptyPublicAndSystemId() {
        // This is the specific bug scenario: publicId empty, systemId non-empty
        DocumentType doctype = new DocumentType("html", "", "http://www.w3.org/TR/html4/loose.dtd");
        // Buggy output would be "<!DOCTYPE html PUBLIC \"\" \"http://...\">"
        // Correct output is "<!DOCTYPE html SYSTEM \"http://...\">"
        String expected = "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/loose.dtd\">";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithSpecialCharactersInPublicId() {
        DocumentType doctype = new DocumentType("html", "-//W3C//DTD HTML 4.01 Transitional//EN", "");
        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithSpecialCharactersInSystemId() {
        DocumentType doctype = new DocumentType("html", "", "http://www.w3.org/TR/html4/frameset.dtd");
        String expected = "<!DOCTYPE html SYSTEM \"http://www.w3.org/TR/html4/frameset.dtd\">";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithNonHtmlName() {
        DocumentType doctype = new DocumentType("svg", "", "");
        String expected = "<!DOCTYPE svg>";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testOuterHtml_WithNameContainingSpaces() {
        // Name may contain spaces? Not typical but test edge case
        DocumentType doctype = new DocumentType("my custom", "", "");
        String expected = "<!DOCTYPE my custom>";
        assertEquals(expected, doctype.outerHtml());
    }

    @Test
    public void testToString() {
        DocumentType doctype = new DocumentType("html", "", "");
        assertEquals(doctype.outerHtml(), doctype.toString());
    }

    @Test
    public void testOuterHtmlHead() {
        // DocumentType.outerHtmlHead is used for serialization; test it produces correct output
        DocumentType doctype = new DocumentType("html", "", "");
        StringBuilder accum = new StringBuilder();
        try {
            doctype.outerHtmlHead(accum, 0, new Document("").outputSettings());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
        assertEquals("<!DOCTYPE html>", accum.toString());
    }

    @Test
    public void testOuterHtmlTail() {
        DocumentType doctype = new DocumentType("html", "", "");
        StringBuilder accum = new StringBuilder();
        try {
            doctype.outerHtmlTail(accum, 0, new Document("").outputSettings());
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }
        assertEquals("", accum.toString());
    }
}