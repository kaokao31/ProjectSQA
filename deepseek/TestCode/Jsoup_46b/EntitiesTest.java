package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Entities class, designed to achieve high coverage and detect potential faults.
 */
public class EntitiesTest {

    private Document.OutputSettings settings;

    @Before
    public void setUp() {
        settings = new Document.OutputSettings();
        settings.prettyPrint(false);
        settings.escapeMode(Entities.EscapeMode.base);
    }

    // --- escape tests ---

    @Test
    public void testEscapeNullInput() {
        assertEquals("", Entities.escape(null, settings));
    }

    @Test
    public void testEscapeEmptyString() {
        assertEquals("", Entities.escape("", settings));
    }

    @Test
    public void testEscapeNoSpecialChars() {
        assertEquals("hello world", Entities.escape("hello world", settings));
    }

    @Test
    public void testEscapeAmpersand() {
        assertEquals("&amp;", Entities.escape("&", settings));
    }

    @Test
    public void testEscapeLessThan() {
        assertEquals("&lt;", Entities.escape("<", settings));
    }

    @Test
    public void testEscapeGreaterThan() {
        assertEquals("&gt;", Entities.escape(">", settings));
    }

    @Test
    public void testEscapeDoubleQuote() {
        assertEquals("&quot;", Entities.escape("\"", settings));
    }

    @Test
    public void testEscapeSingleQuote() {
        assertEquals("&apos;", Entities.escape("'", settings));
    }

    @Test
    public void testEscapeMixedChars() {
        assertEquals("&lt;hello&gt;&amp;world&apos;", Entities.escape("<hello>&world'", settings));
    }

    @Test
    public void testEscapeWithExtendedEntities() {
        settings.escapeMode(Entities.EscapeMode.extended);
        assertEquals("&nbsp;", Entities.escape("\u00A0", settings));
    }

    @Test
    public void testEscapeWithBaseEntities() {
        settings.escapeMode(Entities.EscapeMode.base);
        // non-breaking space is not in base set, should be numeric
        assertEquals("&#160;", Entities.escape("\u00A0", settings));
    }

    @Test
    public void testEscapeWithUnicodeSupplementary() {
        // Supplementary character U+1F600 (😀)
        String emoji = new String(Character.toChars(0x1F600));
        assertEquals(emoji, Entities.escape(emoji, settings)); // should not be escaped
    }

    @Test
    public void testEscapeWithNullSettings() {
        // Should handle null settings gracefully (likely uses default)
        assertEquals("&amp;", Entities.escape("&", null));
    }

    // --- unescape tests ---

    @Test
    public void testUnescapeNullInput() {
        assertEquals("", Entities.unescape(null));
    }

    @Test
    public void testUnescapeEmptyString() {
        assertEquals("", Entities.unescape(""));
    }

    @Test
    public void testUnescapeNoEntities() {
        assertEquals("hello world", Entities.unescape("hello world"));
    }

    @Test
    public void testUnescapeAmpersand() {
        assertEquals("&", Entities.unescape("&amp;"));
    }

    @Test
    public void testUnescapeLessThan() {
        assertEquals("<", Entities.unescape("&lt;"));
    }

    @Test
    public void testUnescapeGreaterThan() {
        assertEquals(">", Entities.unescape("&gt;"));
    }

    @Test
    public void testUnescapeDoubleQuote() {
        assertEquals("\"", Entities.unescape("&quot;"));
    }

    @Test
    public void testUnescapeSingleQuote() {
        assertEquals("'", Entities.unescape("&apos;"));
    }

    @Test
    public void testUnescapeNumericDecimal() {
        assertEquals("A", Entities.unescape("&#65;"));
    }

    @Test
    public void testUnescapeNumericHex() {
        assertEquals("A", Entities.unescape("&#x41;"));
    }

    @Test
    public void testUnescapeNumericHexLowercase() {
        assertEquals("A", Entities.unescape("&#x41;"));
    }

    @Test
    public void testUnescapeInvalidEntity() {
        // Invalid entity should remain unchanged
        assertEquals("&unknown;", Entities.unescape("&unknown;"));
    }

    @Test
    public void testUnescapeMissingSemicolon() {
        // Missing semicolon should not be parsed
        assertEquals("&amp", Entities.unescape("&amp"));
    }

    @Test
    public void testUnescapeMultipleEntities() {
        assertEquals("<>&'", Entities.unescape("&lt;&gt;&amp;&apos;"));
    }

    @Test
    public void testUnescapeMixed() {
        assertEquals("a&b<c>d", Entities.unescape("a&amp;b&lt;c&gt;d"));
    }

    @Test
    public void testUnescapeWithSupplementary() {
        String emoji = new String(Character.toChars(0x1F600));
        assertEquals(emoji, Entities.unescape("&#128512;"));
    }

    @Test
    public void testUnescapeWithSupplementaryHex() {
        String emoji = new String(Character.toChars(0x1F600));
        assertEquals(emoji, Entities.unescape("&#x1F600;"));
    }

    // --- isNamedEntity tests ---

    @Test
    public void testIsNamedEntityValid() {
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertTrue(Entities.isNamedEntity("apos"));
    }

    @Test
    public void testIsNamedEntityInvalid() {
        assertFalse(Entities.isNamedEntity("unknown"));
        assertFalse(Entities.isNamedEntity(""));
        assertFalse(Entities.isNamedEntity(null));
    }

    @Test
    public void testIsNamedEntityCaseSensitive() {
        // Entity names are case-sensitive
        assertFalse(Entities.isNamedEntity("AMP"));
        assertFalse(Entities.isNamedEntity("Lt"));
    }

    // --- isBaseNamedEntity tests ---

    @Test
    public void testIsBaseNamedEntityValid() {
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertTrue(Entities.isBaseNamedEntity("apos"));
    }

    @Test
    public void testIsBaseNamedEntityInvalid() {
        assertFalse(Entities.isBaseNamedEntity("unknown"));
        assertFalse(Entities.isBaseNamedEntity(""));
        assertFalse(Entities.isBaseNamedEntity(null));
    }

    @Test
    public void testIsBaseNamedEntityExtendedOnly() {
        // nbsp is in extended set, not base
        assertFalse(Entities.isBaseNamedEntity("nbsp"));
    }

    // --- getMap tests (if accessible) ---
    // Note: getMap is package-private, but we are in the same package, so we can test it.

    @Test
    public void testGetMapBase() {
        Object map = Entities.getMap(Entities.EscapeMode.base);
        assertNotNull(map);
        assertTrue(map instanceof java.util.Map);
    }

    @Test
    public void testGetMapExtended() {
        Object map = Entities.getMap(Entities.EscapeMode.extended);
        assertNotNull(map);
        assertTrue(map instanceof java.util.Map);
    }

    // --- EscapeMode enum tests ---

    @Test
    public void testEscapeModeValues() {
        assertEquals(2, Entities.EscapeMode.values().length);
        assertEquals(Entities.EscapeMode.base, Entities.EscapeMode.valueOf("base"));
        assertEquals(Entities.EscapeMode.extended, Entities.EscapeMode.valueOf("extended"));
    }

    // --- Edge cases for escape with different output settings ---

    @Test
    public void testEscapeWithPrettyPrint() {
        settings.prettyPrint(true);
        // Pretty print may add newlines, but escape should still work
        assertEquals("&lt;div&gt;", Entities.escape("<div>", settings));
    }

    @Test
    public void testEscapeWithCharset() {
        settings.charset("UTF-8");
        assertEquals("&amp;", Entities.escape("&", settings));
    }

    // --- Fault detection: potential bug with incomplete entity ---

    @Test
    public void testUnescapeIncompleteEntity() {
        // Incomplete entity like "&amp" without semicolon should not be unescaped
        assertEquals("&amp", Entities.unescape("&amp"));
    }

    @Test
    public void testUnescapeEntityWithTrailingText() {
        assertEquals("&amp;test", Entities.unescape("&amp;test"));
    }

    @Test
    public void testUnescapeNumericEntityOverflow() {
        // Very large numeric value should be handled gracefully
        assertEquals("&#99999999;", Entities.unescape("&#99999999;"));
    }

    @Test
    public void testUnescapeNumericEntityNegative() {
        // Negative numeric should be ignored
        assertEquals("&#-1;", Entities.unescape("&#-1;"));
    }

    // --- Test for escape with all special characters ---

    @Test
    public void testEscapeAllSpecialChars() {
        String input = "<>&\"'";
        String expected = "&lt;&gt;&amp;&quot;&apos;";
        assertEquals(expected, Entities.escape(input, settings));
    }

    // --- Test for unescape with multiple consecutive entities ---

    @Test
    public void testUnescapeConsecutiveEntities() {
        assertEquals("&&", Entities.unescape("&amp;&amp;"));
    }

    // --- Test for escape with null character ---

    @Test
    public void testEscapeNullCharacter() {
        // Null character should be escaped? Typically not, but test behavior
        assertEquals("\0", Entities.escape("\0", settings));
    }

    // --- Test for unescape with null character ---

    @Test
    public void testUnescapeNullCharacter() {
        assertEquals("\0", Entities.unescape("\0"));
    }
}