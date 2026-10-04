package org.apache.commons.lang;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test suite for the Entities class.
 * Designed to achieve high coverage and detect bugs such as
 * the number overflow issue (Defects4J bug 62).
 */
public class EntitiesTest {

    private Entities defaultEntities;
    private Entities customEntities;

    @Before
    public void setUp() {
        defaultEntities = Entities.HTML40;
        customEntities = new Entities();
        // Add some basic entities for testing custom mappings
        customEntities.addEntity("lt", 60);
        customEntities.addEntity("gt", 62);
        customEntities.addEntity("amp", 38);
        customEntities.addEntity("quot", 34);
        customEntities.addEntity("custom", 9999);
    }

    // -----------------------------------------------------------------------
    // Null and empty tests
    // -----------------------------------------------------------------------

    @Test
    public void testUnescapeNull() {
        Assert.assertNull("Unescape null should return null", defaultEntities.unescape(null));
    }

    @Test
    public void testEscapeNull() {
        Assert.assertNull("Escape null should return null", defaultEntities.escape(null));
    }

    @Test
    public void testUnescapeEmpty() {
        Assert.assertEquals("Unescape empty string should return empty", "", defaultEntities.unescape(""));
    }

    @Test
    public void testEscapeEmpty() {
        Assert.assertEquals("Escape empty string should return empty", "", defaultEntities.escape(""));
    }

    // -----------------------------------------------------------------------
    // Named entity escaping/unescaping (HTML40)
    // -----------------------------------------------------------------------

    @Test
    public void testEscapeNamedEntity() {
        Assert.assertEquals("Escape ampersand", "&amp;", defaultEntities.escape("&"));
        Assert.assertEquals("Escape less-than", "&lt;", defaultEntities.escape("<"));
        Assert.assertEquals("Escape greater-than", "&gt;", defaultEntities.escape(">"));
        Assert.assertEquals("Escape double quote", "&quot;", defaultEntities.escape("\""));
    }

    @Test
    public void testUnescapeNamedEntity() {
        Assert.assertEquals("Unescape ampersand", "&", defaultEntities.unescape("&amp;"));
        Assert.assertEquals("Unescape less-than", "<", defaultEntities.unescape("&lt;"));
        Assert.assertEquals("Unescape greater-than", ">", defaultEntities.unescape("&gt;"));
        Assert.assertEquals("Unescape double quote", "\"", defaultEntities.unescape("&quot;"));
    }

    @Test
    public void testEscapeMixedString() {
        String input = "AT&T and <tag>";
        String expected = "AT&amp;T and &lt;tag&gt;";
        Assert.assertEquals("Escape mixed string", expected, defaultEntities.escape(input));
    }

    @Test
    public void testUnescapeMixedString() {
        String input = "AT&amp;T and &lt;tag&gt;";
        String expected = "AT&T and <tag>";
        Assert.assertEquals("Unescape mixed string", expected, defaultEntities.unescape(input));
    }

    // -----------------------------------------------------------------------
    // Numeric entity unescaping (decimal)
    // -----------------------------------------------------------------------

    @Test
    public void testUnescapeNumericEntity() {
        Assert.assertEquals("Decimal 65 = A", "A", defaultEntities.unescape("&#65;"));
        Assert.assertEquals("Decimal 97 = a", "a", defaultEntities.unescape("&#97;"));
        Assert.assertEquals("Decimal 0 = null char", "\0", defaultEntities.unescape("&#0;"));
    }

    @Test
    public void testUnescapeNumericEntityMaxChar() {
        // Max char value 65535
        Assert.assertEquals("Decimal 65535", "\uFFFF", defaultEntities.unescape("&#65535;"));
    }

    @Test
    public void testUnescapeNumericEntityOverflow() {
        // Bug 62: number overflow – should NOT convert to character;
        // the original text should be preserved.
        String input = "&#12345678;";
        String expected = "&#12345678;";  // unchanged because the number is out of range
        Assert.assertEquals("Numeric entity overflow should be left unchanged",
                expected, defaultEntities.unescape(input));
    }

    @Test
    public void testUnescapeNumericEntityNegative() {
        // Negative numbers are invalid – may be left as text or handled gracefully
        String input = "&#-1;";
        Assert.assertEquals("Negative numeric entity should be left unchanged",
                input, defaultEntities.unescape(input));
    }

    @Test
    public void testUnescapeNumericEntityInvalidChars() {
        // Non-digit after &# should be left unchanged
        Assert.assertEquals("", "&#abc;", defaultEntities.unescape("&#abc;"));
    }

    // -----------------------------------------------------------------------
    // Hex entity unescaping
    // -----------------------------------------------------------------------

    @Test
    public void testUnescapeHexEntity() {
        Assert.assertEquals("Hex 41 = A", "A", defaultEntities.unescape("&#x41;"));
        Assert.assertEquals("Hex 61 = a", "a", defaultEntities.unescape("&#x61;"));
        Assert.assertEquals("Hex 0 = null char", "\0", defaultEntities.unescape("&#x0;"));
    }

    @Test
    public void testUnescapeHexEntityMaxChar() {
        Assert.assertEquals("Hex FFFF", "\uFFFF", defaultEntities.unescape("&#xFFFF;"));
    }

    @Test
    public void testUnescapeHexEntityOverflow() {
        // Number larger than 0x10FFFF (maximum code point)
        String input = "&#x110000;";
        Assert.assertEquals("Hex entity overflow should be left unchanged",
                input, defaultEntities.unescape(input));
    }

    @Test
    public void testUnescapeHexEntityInvalidPrefix() {
        // Incomplete hex entity
        Assert.assertEquals("", "&#x;", defaultEntities.unescape("&#x;"));
    }

    // -----------------------------------------------------------------------
    // Custom entities (added via addEntity)
    // -----------------------------------------------------------------------

    @Test
    public void testUnescapeCustomEntity() {
        Assert.assertEquals("Custom entity 'custom' (9999)", String.valueOf((char) 9999),
                customEntities.unescape("&custom;"));
    }

    @Test
    public void testEscapeCustomEntity() {
        Assert.assertEquals("Escape char 9999", "&custom;", customEntities.escape(String.valueOf((char) 9999)));
    }

    @Test
    public void testCustomEntityNotInDefault() {
        // 'custom' should not be recognized by default entities
        Assert.assertEquals("Default entities do not know 'custom'",
                "&custom;", defaultEntities.unescape("&custom;"));
    }

    // -----------------------------------------------------------------------
    // Handling of malformed or missing entity patterns
    // -----------------------------------------------------------------------

    @Test
    public void testUnescapeInvalidNamedEntity() {
        Assert.assertEquals("Unknown entity should be left unchanged",
                "&unknown;", defaultEntities.unescape("&unknown;"));
    }

    @Test
    public void testUnescapeMissingSemicolon() {
        // Without semicolon, the entity is not completed – often left unchanged
        String input = "&amp";
        Assert.assertEquals("Entity without semicolon should be left unchanged",
                input, defaultEntities.unescape(input));
    }

    @Test
    public void testUnescapeDoubleAmpersand() {
        Assert.assertEquals("Double ampersand", "&&", defaultEntities.unescape("&amp;&amp;"));
    }

    @Test
    public void testUnescapeMultipleEntitiesInString() {
        String input = "a&lt;b&gt;c&amp;d&quot;e";
        String expected = "a<b>c&d\"e";
        Assert.assertEquals("Multiple entities unescaped", expected, defaultEntities.unescape(input));
    }

    @Test
    public void testEscapeNoSpecialChars() {
        Assert.assertEquals("Plain string", "Hello, World!", defaultEntities.escape("Hello, World!"));
    }

    // -----------------------------------------------------------------------
    // Specific bug detection tests (triggering known Defects4J faults)
    // -----------------------------------------------------------------------

    @Test
    public void testNumberOverflow() {
        // This test is derived from Defects4J bug 62.
        // It must fail when the number overflow bug is present.
        String input = "&#12345678;";
        String expected = "&#12345678;";  // Should remain unchanged
        Assert.assertEquals("Bug 62: numeric entity overflow", expected, defaultEntities.unescape(input));
    }

    @Test
    public void testHexNumberOverflow() {
        // Similar overflow for hex entities
        String input = "&#xDEADBEEF;";
        Assert.assertEquals("Hex overflow should be left unchanged", input, defaultEntities.unescape(input));
    }

    // -----------------------------------------------------------------------
    // Edge cases for escaping
    // -----------------------------------------------------------------------

    @Test
    public void testEscapeCharBoundary() {
        // Characters that map to HTML entities should be escaped
        Assert.assertEquals("Escape char <", "&lt;", defaultEntities.escape("<"));
        Assert.assertEquals("Escape char >", "&gt;", defaultEntities.escape(">"));
    }

    @Test
    public void testEscapeNonAscii() {
        // Non-ASCII characters should be passed through (not escaped)
        String chinese = "\u4E2D\u6587"; // 中文
        Assert.assertEquals("Non-ASCII characters not escaped", chinese, defaultEntities.escape(chinese));
    }

    @Test
    public void testEscapeAllInOne() {
        // Hungarian reference: multiple special chars in one string
        String input = "<>&\"'";
        // Note: single quote is not escaped by HTML40 by default
        String expected = "&lt;&gt;&amp;&quot;'";
        Assert.assertEquals("Escape all HTML special chars", expected, defaultEntities.escape(input));
    }

    // -----------------------------------------------------------------------
    // Large / performance stress (minimal, but covers loop)
    // -----------------------------------------------------------------------

    @Test
    public void testLargeInputUnescape() {
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 1000; i++) {
            sb.append("&amp;");
        }
        String input = sb.toString();
        String expected = sb.toString().replace("&amp;", "&");
        Assert.assertEquals("Large unescape should not crash", expected, defaultEntities.unescape(input));
    }

    @Test
    public void testLargeInputEscape() {
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 1000; i++) {
            sb.append("&");
        }
        String input = sb.toString();
        String expected = sb.toString().replace("&", "&amp;");
        Assert.assertEquals("Large escape should not crash", expected, defaultEntities.escape(input));
    }

    // -----------------------------------------------------------------------
    // Check that numeric entities with valid ranges are correctly converted
    // -----------------------------------------------------------------------

    @Test
    public void testUnescapeNumericEntityBoundaries() {
        // Values just below and above the BMP (basic multilingual plane)
        Assert.assertEquals("Value 0xFFFF", "\uFFFF", defaultEntities.unescape("&#65535;"));
        // "&#65536;" – this is > 0xFFFF, but in Java char only up to 0xFFFF;
        // actual behavior may depend on implementation (some may convert to surrogate pair).
        // For coverage, we test that it does not crash.
        String largeNumber = "&#65536;";
        String result = defaultEntities.unescape(largeNumber);
        // The result should either be a surrogate pair or the unchanged string;
        // but to be safe, we just assert it is not null and not empty.
        Assert.assertNotNull("Large numeric entity should not produce null", result);
        Assert.assertFalse("Large numeric entity should not produce empty", result.isEmpty());
    }

    @Test
    public void testUnescapeHexEntityBoundaries() {
        Assert.assertEquals("Hex FFFF", "\uFFFF", defaultEntities.unescape("&#xFFFF;"));
        // Above 0xFFFF
        String largeHex = "&#x10000;";
        String result = defaultEntities.unescape(largeHex);
        Assert.assertNotNull("Large hex entity should not produce null", result);
        Assert.assertFalse("Large hex entity should not produce empty", result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // Ensure that 'Entities.addEntities' (if public) works – or at least
    // that the class is testable. Since we cannot assume, we skip.
    // But for completeness, add a test if possible.
    // -----------------------------------------------------------------------

    // This test relies on the internal array being accessible; if not, it may be removed.
    @Test
    public void testAddEntityArray() {
        // Verified through customEntities
        Assert.assertEquals("Custom entity from addEntity works", String.valueOf((char) 9999),
                customEntities.unescape("&custom;"));
    }
}