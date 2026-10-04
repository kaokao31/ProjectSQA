package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

public class DocumentTypeTest {
    private Document.OutputSettings settings;

    @Before
    public void setUp() {
        settings = new Document.OutputSettings();
    }

    // Helper to call outerHtmlHead and return the string
    private String outerHtmlHead(DocumentType doctype) throws IOException {
        StringBuilder sb = new StringBuilder();
        doctype.outerHtmlHead(sb, 0, settings);
        return sb.toString();
    }

    // Test constructor with all parameters
    @Test
    public void testConstructorWithAllParams() {
        DocumentType dt = new DocumentType("html", "publicId", "systemId");
        assertEquals("html", dt.name());
        assertEquals("publicId", dt.publicId());
        assertEquals("systemId", dt.systemId());
    }

    // Test constructor with null values
    @Test
    public void testConstructorWithNulls() {
        DocumentType dt = new DocumentType(null, null, null);
        assertNull(dt.name());
        assertNull(dt.publicId());
        assertNull(dt.systemId());
    }

    // Test constructor with empty strings
    @Test
    public void testConstructorWithEmptyStrings() {
        DocumentType dt = new DocumentType("", "", "");
        assertEquals("", dt.name());
        assertEquals("", dt.publicId());
        assertEquals("", dt.systemId());
    }

    // Test outerHtmlHead with no public/system IDs (bug case: should not have trailing space)
    @Test
    public void testOuterHtmlHeadNoPublicNoSystem() throws IOException {
        DocumentType dt = new DocumentType("html", "", "");
        String result = outerHtmlHead(dt);
        // Expected: "<!DOCTYPE html>" (no space before >)
        assertEquals("<!DOCTYPE html>", result);
    }

    // Test outerHtmlHead with public ID only
    @Test
    public void testOuterHtmlHeadWithPublicOnly() throws IOException {
        DocumentType dt = new DocumentType("html", "PUBLIC", "");
        String result = outerHtmlHead(dt);
        // Expected: "<!DOCTYPE html PUBLIC \"PUBLIC\">"
        assertEquals("<!DOCTYPE html PUBLIC \"PUBLIC\">", result);
    }

    // Test outerHtmlHead with system ID only
    @Test
    public void testOuterHtmlHeadWithSystemOnly() throws IOException {
        DocumentType dt = new DocumentType("html", "", "SYSTEM");
        String result = outerHtmlHead(dt);
        // Expected: "<!DOCTYPE html SYSTEM \"SYSTEM\">"
        assertEquals("<!DOCTYPE html SYSTEM \"SYSTEM\">", result);
    }

    // Test outerHtmlHead with both public and system IDs
    @Test
    public void testOuterHtmlHeadWithBoth() throws IOException {
        DocumentType dt = new DocumentType("html", "PUBLIC", "SYSTEM");
        String result = outerHtmlHead(dt);
        // Expected: "<!DOCTYPE html PUBLIC \"PUBLIC\" \"SYSTEM\">"
        assertEquals("<!DOCTYPE html PUBLIC \"PUBLIC\" \"SYSTEM\">", result);
    }

    // Test outerHtmlHead with null public and system IDs
    @Test
    public void testOuterHtmlHeadWithNullIds() throws IOException {
        DocumentType dt = new DocumentType("html", null, null);
        String result = outerHtmlHead(dt);
        // Expected: "<!DOCTYPE html>" (no space before >)
        assertEquals("<!DOCTYPE html>", result);
    }

    // Test outerHtmlHead with null name
    @Test
    public void testOuterHtmlHeadWithNullName() throws IOException {
        DocumentType dt = new DocumentType(null, "PUBLIC", "SYSTEM");
        String result = outerHtmlHead(dt);
        // Expected: "<!DOCTYPE PUBLIC \"PUBLIC\" \"SYSTEM\">"
        assertEquals("<!DOCTYPE PUBLIC \"PUBLIC\" \"SYSTEM\">", result);
    }

    // Test outerHtmlHead with empty name
    @Test
    public void testOuterHtmlHeadWithEmptyName() throws IOException {
        DocumentType dt = new DocumentType("", "PUBLIC", "SYSTEM");
        String result = outerHtmlHead(dt);
        // Expected: "<!DOCTYPE PUBLIC \"PUBLIC\" \"SYSTEM\">"
        assertEquals("<!DOCTYPE PUBLIC \"PUBLIC\" \"SYSTEM\">", result);
    }

    // Test outerHtmlTail (should do nothing)
    @Test
    public void testOuterHtmlTail() throws IOException {
        DocumentType dt = new DocumentType("html", "", "");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlTail(sb, 0, settings);
        assertEquals("", sb.toString());
    }

    // Test toString (which uses outerHtml)
    @Test
    public void testToString() {
        DocumentType dt = new DocumentType("html", "PUBLIC", "SYSTEM");
        String result = dt.toString();
        // Expected: "<!DOCTYPE html PUBLIC \"PUBLIC\" \"SYSTEM\">"
        assertEquals("<!DOCTYPE html PUBLIC \"PUBLIC\" \"SYSTEM\">", result);
    }

    // Test toString with no IDs (bug case)
    @Test
    public void testToStringNoIds() {
        DocumentType dt = new DocumentType("html", "", "");
        String result = dt.toString();
        assertEquals("<!DOCTYPE html>", result);
    }

    // Test clone method
    @Test
    public void testClone() {
        DocumentType dt = new DocumentType("html", "PUBLIC", "SYSTEM");
        DocumentType cloned = dt.clone();
        assertNotSame(dt, cloned);
        assertEquals(dt.name(), cloned.name());
        assertEquals(dt.publicId(), cloned.publicId());
        assertEquals(dt.systemId(), cloned.systemId());
    }

    // Test clone with nulls
    @Test
    public void testCloneWithNulls() {
        DocumentType dt = new DocumentType(null, null, null);
        DocumentType cloned = dt.clone();
        assertNull(cloned.name());
        assertNull(cloned.publicId());
        assertNull(cloned.systemId());
    }

    // Test setter methods if any (DocumentType likely has no setters, but we can test getters)
    // No setters in DocumentType, so skip.

    // Test edge case: public ID with special characters (quotes)
    @Test
    public void testOuterHtmlHeadWithQuotesInPublicId() throws IOException {
        DocumentType dt = new DocumentType("html", "PUB\"LIC", "");
        String result = outerHtmlHead(dt);
        // Expected: "<!DOCTYPE html PUBLIC \"PUB\"LIC\">" (escaped quotes)
        assertEquals("<!DOCTYPE html PUBLIC \"PUB\"LIC\">", result);
    }

    // Test edge case: system ID with special characters
    @Test
    public void testOuterHtmlHeadWithQuotesInSystemId() throws IOException {
        DocumentType dt = new DocumentType("html", "", "SYS\"TEM");
        String result = outerHtmlHead(dt);
        assertEquals("<!DOCTYPE html SYSTEM \"SYS\"TEM\">", result);
    }

    // Test edge case: both IDs with quotes
    @Test
    public void testOuterHtmlHeadWithQuotesInBothIds() throws IOException {
        DocumentType dt = new DocumentType("html", "PUB\"LIC", "SYS\"TEM");
        String result = outerHtmlHead(dt);
        assertEquals("<!DOCTYPE html PUBLIC \"PUB\"LIC\" \"SYS\"TEM\">", result);
    }

    // Test that outerHtmlHead throws IOException if StringBuilder throws (unlikely, but coverage)
    // Not needed as StringBuilder doesn't throw IOException.

    // Test with Document.OutputSettings that have prettyPrint false (should not affect)
    @Test
    public void testOuterHtmlHeadWithPrettyPrintFalse() throws IOException {
        settings.prettyPrint(false);
        DocumentType dt = new DocumentType("html", "", "");
        String result = outerHtmlHead(dt);
        assertEquals("<!DOCTYPE html>", result);
    }

    // Test with Document.OutputSettings that have outline true (should not affect)
    @Test
    public void testOuterHtmlHeadWithOutlineTrue() throws IOException {
        settings.outline(true);
        DocumentType dt = new DocumentType("html", "", "");
        String result = outerHtmlHead(dt);
        assertEquals("<!DOCTYPE html>", result);
    }
}