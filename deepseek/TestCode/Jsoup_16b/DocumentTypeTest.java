package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * Robust JUnit 4 test suite for the DocumentType class.
 * Designed to achieve high coverage and trigger potential faults,
 * notably the missing trailing newline (Jsoup bug 16).
 */
public class DocumentTypeTest {

    // ---------------------------------------------------------------
    // nodeName() tests
    // ---------------------------------------------------------------
    @Test
    public void nodeNameIsDoctype() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertEquals("#doctype", dt.nodeName());
    }

    @Test
    public void nodeNameWithNullName() {
        DocumentType dt = new DocumentType(null, "", "", "");
        assertEquals("#doctype", dt.nodeName());
    }

    @Test
    public void nodeNameWithAllNulls() {
        DocumentType dt = new DocumentType(null, null, null, null);
        assertEquals("#doctype", dt.nodeName());
    }

    // ---------------------------------------------------------------
    // outerHtmlHead() tests – HTML5 doctype
    // ---------------------------------------------------------------
    @Test
    public void outerHtmlHeadHtml5() throws IOException {
        DocumentType dt = new DocumentType("html", "", "", "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        // Expect "<!DOCTYPE html>\n" (with trailing newline)
        assertEquals("<!DOCTYPE html>\n", out);
    }

    @Test
    public void outerHtmlHeadHtml5NoNewlineBug() throws IOException {
        // Explicitly test that newline IS present (bug 16 was missing it)
        DocumentType dt = new DocumentType("html", "", "", "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        assertTrue("Output must end with newline char",
                out.endsWith("\n"));
    }

    // ---------------------------------------------------------------
    // outerHtmlHead() tests – public identifier
    // ---------------------------------------------------------------
    @Test
    public void outerHtmlHeadWithPublic() throws IOException {
        DocumentType dt = new DocumentType("html",
                "-//W3C//DTD HTML 4.01//EN",
                "",
                "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n";
        assertEquals(expected, out);
    }

    @Test
    public void outerHtmlHeadWithPublicAndSystem() throws IOException {
        DocumentType dt = new DocumentType("html",
                "-//W3C//DTD HTML 4.01//EN",
                "http://www.w3.org/TR/html4/strict.dtd",
                "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n";
        assertEquals(expected, out);
    }

    // ---------------------------------------------------------------
    // outerHtmlHead() tests – system identifier only
    // ---------------------------------------------------------------
    @Test
    public void outerHtmlHeadWithSystemOnly() throws IOException {
        DocumentType dt = new DocumentType("html",
                "",
                "http://example.com/dtd",
                "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        String expected = "<!DOCTYPE html SYSTEM \"http://example.com/dtd\">\n";
        assertEquals(expected, out);
    }

    // ---------------------------------------------------------------
    // outerHtmlHead() tests – edge cases: null and empty strings
    // ---------------------------------------------------------------
    @Test
    public void outerHtmlHeadNullNameEmptyIds() throws IOException {
        DocumentType dt = new DocumentType(null, "", "", "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        // If name is null, Jsoup may convert to empty string producing "<!DOCTYPE  >"
        // We expect at least a newline, and we check structure
        assertTrue("Output must start with '<!DOCTYPE'", out.startsWith("<!DOCTYPE"));
        assertTrue("Output must end with newline", out.endsWith("\n"));
    }

    @Test
    public void outerHtmlHeadAllNulls() throws IOException {
        DocumentType dt = new DocumentType(null, null, null, null);
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        assertTrue("Output must start with '<!DOCTYPE'", out.startsWith("<!DOCTYPE"));
        assertTrue("Output must end with newline", out.endsWith("\n"));
    }

    @Test
    public void outerHtmlHeadAllEmptyStrings() throws IOException {
        DocumentType dt = new DocumentType("", "", "", "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        assertEquals("<!DOCTYPE >\n", out);   // space before '>'
    }

    @Test
    public void outerHtmlHeadNameAllSpaces() throws IOException {
        DocumentType dt = new DocumentType("   ", "", "", "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        String out = sw.toString();
        assertEquals("<!DOCTYPE    >\n", out);
    }

    // ---------------------------------------------------------------
    // outerHtmlHead() tests – baseUri usage (should not affect output)
    // ---------------------------------------------------------------
    @Test
    public void outerHtmlHeadWithNonEmptyBaseUri() throws IOException {
        DocumentType dt = new DocumentType("html", "", "", "http://base.com");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
        assertEquals("<!DOCTYPE html>\n", sw.toString());
    }

    // ---------------------------------------------------------------
    // toString() tests (relies on outerHtml)
    // ---------------------------------------------------------------
    @Test
    public void toStringHtml5() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>\n", dt.toString());
    }

    @Test
    public void toStringPublicSystem() {
        DocumentType dt = new DocumentType("html",
                "-//W3C//DTD HTML 4.01//EN",
                "http://www.w3.org/TR/html4/strict.dtd",
                "");
        String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n";
        assertEquals(expected, dt.toString());
    }

    @Test
    public void toStringAllEmpty() {
        DocumentType dt = new DocumentType("", "", "", "");
        assertEquals("<!DOCTYPE >\n", dt.toString());
    }

    // ---------------------------------------------------------------
    // OuterHtml method (full) consistency check
    // ---------------------------------------------------------------
    @Test
    public void outerHtmlMethod() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>\n", dt.outerHtml());
    }

    // ---------------------------------------------------------------
    // Fault detection: Bug 16 – missing newline in HTML serialization
    // (We already test trailing newline above; add an extra check)
    // ---------------------------------------------------------------
    @Test
    public void outerHtmlHeadAlwaysEndsWithNewline() throws IOException {
        String[] names = {"html", "HTML", "xml", "", null};
        for (String name : names) {
            DocumentType dt = new DocumentType(name, "", "", "");
            StringWriter sw = new StringWriter();
            dt.outerHtmlHead(sw, 0, new Document("").outputSettings());
            String out = sw.toString();
            assertTrue("Output must end with newline for name='" + name + "'",
                    out.endsWith("\n"));
        }
    }

    // ---------------------------------------------------------------
    // Branch coverage: write() method (innerHtml code path)
    // (DocumentType has no children, but cover innerHtml)
    // ---------------------------------------------------------------
    @Test
    public void innerHtmlReturnsEmpty() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertEquals("", dt.innerHtml());
    }

    @Test
    public void outerHtmlHeadDoesNotThrowForNonZeroIndent() throws IOException {
        DocumentType dt = new DocumentType("html", "", "", "");
        StringWriter sw = new StringWriter();
        dt.outerHtmlHead(sw, 3, new Document("").outputSettings().indentAmount(2));
        // The indent is not added to the doctype, but method must not throw
        assertEquals("<!DOCTYPE html>\n", sw.toString());
    }

    // ---------------------------------------------------------------
    // Constructor edge cases: partially null identifiers
    // ---------------------------------------------------------------
    @Test
    public void constructorNullPublicId() {
        DocumentType dt = new DocumentType("html", null, "", "");
        String s = dt.toString();
        assertTrue(s.startsWith("<!DOCTYPE html"));
    }

    @Test
    public void constructorNullSystemId() {
        DocumentType dt = new DocumentType("html", "", null, "");
        String s = dt.toString();
        assertTrue(s.startsWith("<!DOCTYPE html"));
    }

    // ---------------------------------------------------------------
    // Equals and hashCode (inherited from Node) – basic sanity
    // ---------------------------------------------------------------
    @Test
    public void equalsSameObject() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertTrue(dt.equals(dt));
    }

    @Test
    public void equalsNull() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertFalse(dt.equals(null));
    }

    @Test
    public void equalsDifferentType() {
        DocumentType dt = new DocumentType("html", "", "", "");
        assertFalse(dt.equals("not a DocumentType"));
    }

    @Test
    public void equalsDifferentAttributes() {
        DocumentType dt1 = new DocumentType("html", "", "", "");
        DocumentType dt2 = new DocumentType("xml", "", "", "");
        // Two different DocumentType nodes with different names are NOT equal
        assertFalse(dt1.equals(dt2));
    }

    @Test
    public void hashCodeConsistent() {
        DocumentType dt = new DocumentType("html", "", "", "");
        int h1 = dt.hashCode();
        int h2 = dt.hashCode();
        assertEquals(h1, h2);
    }
}