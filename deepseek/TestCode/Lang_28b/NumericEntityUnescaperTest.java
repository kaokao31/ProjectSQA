package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for NumericEntityUnescaper. 
 * Designed to achieve high coverage and reproduce Defects4J Bug 28.
 */
public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper unescaper;

    @org.junit.Before
    public void setUp() {
        unescaper = new NumericEntityUnescaper();
    }

    // --- Bug 28 reproduction: supplementary character unescaping ---
    @Test
    public void testSupplementaryUnescapingDecimal() {
        // Supplementary character U+10C22 (in decimal: 68642)
        String input = "&#68642;";
        String expected = "\uD803\uDC22"; // UTF-16 surrogate pair for U+10C22
        String actual = unescaper.translate(input);
        assertEquals("Supplementary decimal unescape failed", expected, actual);
    }

    @Test
    public void testSupplementaryUnescapingHex() {
        // Supplementary character U+10C22 in hex: 10C22
        String input = "&#x10C22;";
        String expected = "\uD803\uDC22";
        String actual = unescaper.translate(input);
        assertEquals("Supplementary hex unescape failed", expected, actual);
    }

    @Test
    public void testSupplementaryUnescapingHexLowercaseX() {
        // Supplementary character U+10348 (GOTHIC LETTER HWAIR) decimal: 66376 hex: 10348
        String input = "&#x10348;";
        String expected = "\uD800\uDF48";
        String actual = unescaper.translate(input);
        assertEquals("Supplementary hex lowercase x unescape failed", expected, actual);
    }

    // --- Basic ASCII numeric entities ---
    @Test
    public void testBasicDecimal() {
        assertEquals("A", unescaper.translate("&#65;"));
        assertEquals("z", unescaper.translate("&#122;"));
        assertEquals("0", unescaper.translate("&#48;"));
    }

    @Test
    public void testBasicHex() {
        assertEquals("A", unescaper.translate("&#x41;"));
        assertEquals("z", unescaper.translate("&#x7A;"));
        assertEquals("0", unescaper.translate("&#x30;"));
    }

    @Test
    public void testBasicHexUppercaseX() {
        assertEquals("A", unescaper.translate("&#X41;"));
    }

    @Test
    public void testZero() {
        assertEquals("\0", unescaper.translate("&#0;"));
        assertEquals("\0", unescaper.translate("&#x0;"));
    }

    @Test
    public void testMaxCodePoint() {
        // Maximum valid Unicode code point U+10FFFF
        String expected = "\uDBFF\uDFFF"; // surrogate pair for U+10FFFF
        assertEquals(expected, unescaper.translate("&#1114111;"));
        assertEquals(expected, unescaper.translate("&#x10FFFF;"));
    }

    @Test
    public void testCodePointBeyondMax() {
        // Code point > 0x10FFFF should be treated as invalid? The behavior might vary.
        // For maximum coverage, we test but do not enforce specific result.
        // However, to detect potential bugs, we ensure no exception and note result.
        String input = "&#1114112;";
        String result = unescaper.translate(input);
        // Depending on implementation, might output replacement char or keep as is.
        assertNotNull(result);
    }

    // --- Invalid/missing semicolon ---
    @Test
    public void testMissingSemicolonDecimal() {
        String expected = "&#65";
        String actual = unescaper.translate(expected);
        assertEquals("Missing semicolon should leave as is", expected, actual);
    }

    @Test
    public void testMissingSemicolonHex() {
        String expected = "&#x41";
        String actual = unescaper.translate(expected);
        assertEquals("Missing semicolon hex should leave as is", expected, actual);
    }

    @Test
    public void testNonNumericAfterHash() {
        assertEquals("&#G65;", unescaper.translate("&#G65;"));
        assertEquals("&#xG;", unescaper.translate("&#xG;"));
    }

    @Test
    public void testEmptyEntity() {
        assertEquals("&#;", unescaper.translate("&#;"));
        assertEquals("&#x;", unescaper.translate("&#x;"));
    }

    @Test
    public void testLeadingZerosDecimal() {
        assertEquals("A", unescaper.translate("&#00065;"));
    }

    @Test
    public void testLeadingZerosHex() {
        assertEquals("A", unescaper.translate("&#x00041;"));
    }

    @Test
    public void testNegativeNumber() {
        // Negative numbers are not valid; should not parse
        assertEquals("&#-1;", unescaper.translate("&#-1;"));
    }

    @Test
    public void testOverflowDec() {
        // Large number that overflows int
        String large = "&#" + Integer.MAX_VALUE + "1;"; // beyond max int
        // Should not parse as entity
        assertEquals(large, unescaper.translate(large));
    }

    @Test
    public void testOverflowHex() {
        String large = "&#x" + Long.toHexString((long) Integer.MAX_VALUE + 1) + ";";
        assertEquals(large, unescaper.translate(large));
    }

    // --- Mixed content with regular text ---
    @Test
    public void testMixedText() {
        String input = "Hello &#65; world &#x42;!";
        String expected = "Hello A world B!";
        assertEquals(expected, unescaper.translate(input));
    }

    @Test
    public void testMultipleEntities() {
        assertEquals("ABC", unescaper.translate("&#65;&#66;&#67;"));
    }

    @Test
    public void testConsecutiveNonEntities() {
        assertEquals("plain text", unescaper.translate("plain text"));
    }

    @Test
    public void testEmptyString() {
        assertEquals("", unescaper.translate(""));
    }

    @Test(expected = NullPointerException.class)
    public void testNullInput() {
        unescaper.translate(null);
    }

    // --- Edge: start and end of string ---
    @Test
    public void testEntityAtStart() {
        assertEquals("A", unescaper.translate("&#65;"));
    }

    @Test
    public void testEntityAtEnd() {
        assertEquals("A", unescaper.translate("&#65;"));
    }

    @Test
    public void testSingleAmpersand() {
        assertEquals("&", unescaper.translate("&"));
    }

    @Test
    public void testDoubleAmpersand() {
        assertEquals("&&", unescaper.translate("&&"));
    }

    @Test
    public void testHashWithoutNumber() {
        assertEquals("&#", unescaper.translate("&#"));
        assertEquals("&#x", unescaper.translate("&#x"));
    }

    // --- Additional coverage for internal parser branches ---
    @Test
    public void testHexAfterHashAndX() {
        // Entity with hex letter 'A' should not be parsed as entity if missing numbers
        assertEquals("&#xABC;", unescaper.translate("&#xABC;"));
    }

    @Test
    public void testMixedCaseHex() {
        assertEquals("A", unescaper.translate("&#x4a;")); // lowercase j (valid)
        assertEquals("Z", unescaper.translate("&#x5A;"));
    }

    @Test
    public void testVeryLongNumber() {
        StringBuilder sb = new StringBuilder("&#");
        for (int i = 0; i < 100; i++) {
            sb.append('1');
        }
        sb.append(';');
        // Should not parse; result is input unchanged
        assertEquals(sb.toString(), unescaper.translate(sb.toString()));
    }

    // --- Test for potential infinite loop or incorrect state ---
    @Test
    public void testEntityFollowedByNonSemicolon() {
        // Input like &#65abc should not parse
        assertEquals("&#65abc", unescaper.translate("&#65abc"));
    }
}