package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Entities class, targeting high coverage and fault detection.
 * Designed for Jsoup bug 47 context: escape() should not escape non-ASCII characters in HTML5 mode.
 */
public class EntitiesTest {

    // Sample strings for testing
    private static final String ASCII_ONLY = "Hello World!";
    private static final String NON_ASCII = "Héllo Wörld!";
    private static final String CHINESE = "你好世界";
    private static final String SPECIAL_CHARS = "<>&\"'";
    private static final String SUPPLEMENTARY = "\uD83D\uDE00"; // 😀

    @Before
    public void setUp() {
        // No setup required for static methods
    }

    @Test
    public void testEscapeHtml5NonAsciiNotEscaped() {
        // In HTML5 mode, non-ASCII characters should not be escaped
        String escaped = Entities.escape(NON_ASCII, Entities.EscapeMode.html5);
        assertEquals("Non-ASCII should remain unchanged in HTML5 mode", NON_ASCII, escaped);
    }

    @Test
    public void testEscapeHtml5AsciiEscaped() {
        // ASCII special characters should still be escaped in HTML5 mode
        String escaped = Entities.escape(SPECIAL_CHARS, Entities.EscapeMode.html5);
        assertEquals("ASCII special chars should be escaped in HTML5", "&lt;&gt;&amp;&quot;&apos;", escaped);
    }

    @Test
    public void testEscapeHtml4NonAsciiEscaped() {
        // In HTML4 mode, non-ASCII characters should be escaped
        String escaped = Entities.escape(NON_ASCII, Entities.EscapeMode.html4);
        assertTrue("Non-ASCII should be escaped in HTML4 mode", escaped.contains("&eacute;"));
    }

    @Test
    public void testEscapeXmlNonAsciiEscaped() {
        // In XML mode, non-ASCII characters should be escaped
        String escaped = Entities.escape(NON_ASCII, Entities.EscapeMode.xml);
        assertTrue("Non-ASCII should be escaped in XML mode", escaped.contains("&#233;"));
    }

    @Test
    public void testEscapeChineseHtml5() {
        // Chinese characters should not be escaped in HTML5 mode
        String escaped = Entities.escape(CHINESE, Entities.EscapeMode.html5);
        assertEquals("Chinese should remain unchanged in HTML5", CHINESE, escaped);
    }

    @Test
    public void testEscapeChineseHtml4() {
        // Chinese characters should be escaped in HTML4 mode
        String escaped = Entities.escape(CHINESE, Entities.EscapeMode.html4);
        assertFalse("Chinese should be escaped in HTML4", escaped.equals(CHINESE));
    }

    @Test
    public void testEscapeSupplementaryHtml5() {
        // Supplementary characters (emoji) should not be escaped in HTML5 mode
        String escaped = Entities.escape(SUPPLEMENTARY, Entities.EscapeMode.html5);
        assertEquals("Supplementary char should remain unchanged in HTML5", SUPPLEMENTARY, escaped);
    }

    @Test
    public void testEscapeNullInput() {
        // Null input should return null or throw? Typically returns null
        String escaped = Entities.escape(null, Entities.EscapeMode.html5);
        assertNull("Null input should return null", escaped);
    }

    @Test
    public void testEscapeEmptyInput() {
        // Empty string should return empty string
        String escaped = Entities.escape("", Entities.EscapeMode.html5);
        assertEquals("Empty input should return empty", "", escaped);
    }

    @Test
    public void testUnescapeBasic() {
        // Basic unescape of HTML entities
        String unescaped = Entities.unescape("&amp;&lt;&gt;&quot;&apos;");
        assertEquals("Basic unescape should work", "&<>\"'", unescaped);
    }

    @Test
    public void testUnescapeNumeric() {
        // Numeric entity unescape
        String unescaped = Entities.unescape("&#233;");
        assertEquals("Numeric unescape should work", "é", unescaped);
    }

    @Test
    public void testUnescapeHex() {
        // Hex numeric entity unescape
        String unescaped = Entities.unescape("&#xE9;");
        assertEquals("Hex unescape should work", "é", unescaped);
    }

    @Test
    public void testUnescapeInvalidEntity() {
        // Invalid entity should remain as-is
        String unescaped = Entities.unescape("&invalid;");
        assertEquals("Invalid entity should remain unchanged", "&invalid;", unescaped);
    }

    @Test
    public void testUnescapeNullInput() {
        // Null input should return null
        String unescaped = Entities.unescape(null);
        assertNull("Null input should return null", unescaped);
    }

    @Test
    public void testUnescapeEmptyInput() {
        // Empty string should return empty
        String unescaped = Entities.unescape("");
        assertEquals("Empty input should return empty", "", unescaped);
    }

    @Test
    public void testIsBaseNamedEntity() {
        // Known base entity
        assertTrue("amp is a base named entity", Entities.isBaseNamedEntity("amp"));
        assertTrue("lt is a base named entity", Entities.isBaseNamedEntity("lt"));
        assertTrue("gt is a base named entity", Entities.isBaseNamedEntity("gt"));
        assertTrue("quot is a base named entity", Entities.isBaseNamedEntity("quot"));
        assertTrue("apos is a base named entity", Entities.isBaseNamedEntity("apos"));
    }

    @Test
    public void testIsBaseNamedEntityInvalid() {
        // Invalid entity name
        assertFalse("invalid is not a base named entity", Entities.isBaseNamedEntity("invalid"));
        assertFalse("empty string is not a base named entity", Entities.isBaseNamedEntity(""));
        assertFalse("null is not a base named entity", Entities.isBaseNamedEntity(null));
    }

    @Test
    public void testEscapeWithMixedContent() {
        // Mixed ASCII and non-ASCII in HTML5 mode
        String mixed = "Hello <world> 你好";
        String escaped = Entities.escape(mixed, Entities.EscapeMode.html5);
        assertEquals("Mixed content: ASCII special chars escaped, Chinese unchanged",
                "Hello &lt;world&gt; 你好", escaped);
    }

    @Test
    public void testEscapeWithAmpersand() {
        // Ampersand should be escaped
        String escaped = Entities.escape("A&B", Entities.EscapeMode.html5);
        assertEquals("Ampersand should be escaped", "A&amp;B", escaped);
    }

    @Test
    public void testUnescapeWithMultipleEntities() {
        // Multiple entities in one string
        String unescaped = Entities.unescape("&amp;lt;");
        assertEquals("Multiple entities should be unescaped", "&lt;", unescaped);
    }

    @Test
    public void testUnescapeWithSupplementary() {
        // Supplementary character as numeric entity
        String unescaped = Entities.unescape("&#128512;");
        assertEquals("Supplementary char unescape", "\uD83D\uDE00", unescaped);
    }

    @Test
    public void testEscapeModeBaseEntities() {
        // Test that base entities are used when possible
        String escaped = Entities.escape("<", Entities.EscapeMode.html5);
        assertEquals("Base entity for <", "&lt;", escaped);
    }

    @Test
    public void testEscapeModeXmlSpecialChars() {
        // XML mode should escape all special chars
        String escaped = Entities.escape("'\"", Entities.EscapeMode.xml);
        assertEquals("XML mode escape", "&apos;&quot;", escaped);
    }
}