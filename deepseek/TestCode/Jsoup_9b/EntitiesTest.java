package org.apache.commons.lang;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit 4 test suite for the {@link Entities} class.
 * Designed to achieve high line and branch coverage and to expose
 * potential faults (e.g., null handling, boundary conditions, 
 * and Defects4J-style bugs).
 */
public class EntitiesTest {

    // -----------------------------------------------------------------------
    // escape(String) tests
    // -----------------------------------------------------------------------

    @Test
    public void testEscapeNullInput() {
        Assert.assertNull("Null input should return null", Entities.escape(null));
    }

    @Test
    public void testEscapeEmptyString() {
        Assert.assertEquals("Empty string should remain empty", "", Entities.escape(""));
    }

    @Test
    public void testEscapeNoSpecialChars() {
        Assert.assertEquals("Plain text without special chars", "hello world", Entities.escape("hello world"));
    }

    @Test
    public void testEscapeAmpersand() {
        Assert.assertEquals("Ampersand should be escaped", "&amp;", Entities.escape("&"));
    }

    @Test
    public void testEscapeLessThan() {
        Assert.assertEquals("Less-than should be escaped", "&lt;", Entities.escape("<"));
    }

    @Test
    public void testEscapeGreaterThan() {
        Assert.assertEquals("Greater-than should be escaped", "&gt;", Entities.escape(">"));
    }

    @Test
    public void testEscapeDoubleQuote() {
        Assert.assertEquals("Double quote should be escaped", "&quot;", Entities.escape("\""));
    }

    @Test
    public void testEscapeSingleQuote() {
        Assert.assertEquals("Single quote should be escaped", "&apos;", Entities.escape("'"));
    }

    @Test
    public void testEscapeMultipleSpecialChars() {
        String input = "<hello & world>";
        String expected = "&lt;hello &amp; world&gt;";
        Assert.assertEquals("Multiple special characters", expected, Entities.escape(input));
    }

    @Test
    public void testEscapeMixedContent() {
        String input = "a<b>c&d\"e'f";
        String expected = "a&lt;b&gt;c&amp;d&quot;e&apos;f";
        Assert.assertEquals("Mixed content with all special chars", expected, Entities.escape(input));
    }

    @Test
    public void testEscapeStringWithExistingEntities() {
        // Should not double-escape
        String input = "&amp;";
        String expected = "&amp;amp;";  // The '&' in the entity is escaped
        Assert.assertEquals("Existing entity ampersand should be escaped again", expected, Entities.escape(input));
    }

    // -----------------------------------------------------------------------
    // unescape(String) tests
    // -----------------------------------------------------------------------

    @Test
    public void testUnescapeNullInput() {
        Assert.assertNull("Null input should return null", Entities.unescape(null));
    }

    @Test
    public void testUnescapeEmptyString() {
        Assert.assertEquals("Empty string should remain empty", "", Entities.unescape(""));
    }

    @Test
    public void testUnescapeNoEntities() {
        Assert.assertEquals("Plain text without entities", "hello world", Entities.unescape("hello world"));
    }

    @Test
    public void testUnescapeAmpersand() {
        Assert.assertEquals("&amp; should become &", "&", Entities.unescape("&amp;"));
    }

    @Test
    public void testUnescapeLessThan() {
        Assert.assertEquals("&lt; should become <", "<", Entities.unescape("&lt;"));
    }

    @Test
    public void testUnescapeGreaterThan() {
        Assert.assertEquals("&gt; should become >", ">", Entities.unescape("&gt;"));
    }

    @Test
    public void testUnescapeDoubleQuote() {
        Assert.assertEquals("&quot; should become \"", "\"", Entities.unescape("&quot;"));
    }

    @Test
    public void testUnescapeSingleQuote() {
        Assert.assertEquals("&apos; should become '", "'", Entities.unescape("&apos;"));
    }

    @Test
    public void testUnescapeMultipleEntities() {
        String input = "&lt;hello &amp; world&gt;";
        String expected = "<hello & world>";
        Assert.assertEquals("Multiple entities", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeMixedContent() {
        String input = "a&lt;b&gt;c&amp;d&quot;e&apos;f";
        String expected = "a<b>c&d\"e'f";
        Assert.assertEquals("Mixed content with all entities", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeUnknownEntity() {
        // Unknown entity should be left as is
        String input = "&unknown;";
        String expected = "&unknown;";
        Assert.assertEquals("Unknown entity should not be changed", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapePartialEntity() {
        // Incomplete entity (missing semicolon)
        String input = "&amp";
        String expected = "&amp";  // Should remain unchanged
        Assert.assertEquals("Incomplete entity should remain", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityAtEnd() {
        String input = "hello&amp;";
        String expected = "hello&";
        Assert.assertEquals("Entity at end of string", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeNumericEntity() {
        // Numeric entities (decimal)
        String input = "&#65;";  // 'A'
        String expected = "A";
        Assert.assertEquals("Decimal numeric entity", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeHexEntity() {
        // Hexadecimal entity
        String input = "&#x41;";  // 'A'
        String expected = "A";
        Assert.assertEquals("Hexadecimal numeric entity", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeNumericEntityOutOfRange() {
        // Out of range numeric entity (should be left as is or handled gracefully)
        String input = "&#9999999;";
        String expected = "&#9999999;";  // Depending on implementation, may remain unchanged
        Assert.assertEquals("Out of range numeric entity", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeMixedEntitiesAndText() {
        String input = "start &lt; middle &amp; end";
        String expected = "start < middle & end";
        Assert.assertEquals("Mixed text and entities", expected, Entities.unescape(input));
    }

    // -----------------------------------------------------------------------
    // Additional edge cases and potential fault triggers
    // -----------------------------------------------------------------------

    @Test
    public void testEscapeStringWithLeadingTrailingSpaces() {
        String input = "  <test>  ";
        String expected = "  &lt;test&gt;  ";
        Assert.assertEquals("Leading/trailing spaces preserved", expected, Entities.escape(input));
    }

    @Test
    public void testUnescapeStringWithLeadingTrailingSpaces() {
        String input = "  &lt;test&gt;  ";
        String expected = "  <test>  ";
        Assert.assertEquals("Leading/trailing spaces preserved", expected, Entities.unescape(input));
    }

    @Test
    public void testEscapeStringWithOnlySpecialChars() {
        String input = "&<>\"'";
        String expected = "&amp;&lt;&gt;&quot;&apos;";
        Assert.assertEquals("Only special characters", expected, Entities.escape(input));
    }

    @Test
    public void testUnescapeStringWithOnlyEntities() {
        String input = "&amp;&lt;&gt;&quot;&apos;";
        String expected = "&<>\"'";
        Assert.assertEquals("Only entities", expected, Entities.unescape(input));
    }

    @Test
    public void testEscapeStringWithNewline() {
        String input = "line1\nline2";
        String expected = "line1\nline2";  // Newline should not be escaped
        Assert.assertEquals("Newline character should not be escaped", expected, Entities.escape(input));
    }

    @Test
    public void testUnescapeStringWithNewline() {
        String input = "line1\nline2";
        String expected = "line1\nline2";
        Assert.assertEquals("Newline character should not be changed", expected, Entities.unescape(input));
    }

    @Test
    public void testEscapeStringWithTab() {
        String input = "col1\tcol2";
        String expected = "col1\tcol2";
        Assert.assertEquals("Tab character should not be escaped", expected, Entities.escape(input));
    }

    @Test
    public void testUnescapeStringWithTab() {
        String input = "col1\tcol2";
        String expected = "col1\tcol2";
        Assert.assertEquals("Tab character should not be changed", expected, Entities.unescape(input));
    }

    @Test
    public void testEscapeVeryLongString() {
        // Stress test with a long string containing many special characters
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 2000; i++) {
            sb.append("a&b<c>d\"e'f");
        }
        String input = sb.toString();
        String result = Entities.escape(input);
        Assert.assertNotNull("Escape of long string should not be null", result);
        Assert.assertTrue("Escaped string should be longer than input", result.length() > input.length());
        // Verify round-trip
        String roundTrip = Entities.unescape(result);
        Assert.assertEquals("Round-trip should recover original", input, roundTrip);
    }

    @Test
    public void testUnescapeVeryLongString() {
        // Stress test with a long string containing many entities
        StringBuilder sb = new StringBuilder(10000);
        for (int i = 0; i < 2000; i++) {
            sb.append("&amp;&lt;&gt;&quot;&apos;");
        }
        String input = sb.toString();
        String result = Entities.unescape(input);
        Assert.assertNotNull("Unescape of long string should not be null", result);
        Assert.assertTrue("Unescaped string should be shorter than input", result.length() < input.length());
        // Verify round-trip
        String roundTrip = Entities.escape(result);
        Assert.assertEquals("Round-trip should recover original entities", input, roundTrip);
    }

    @Test
    public void testEscapeAndUnescapeConsistency() {
        String[] testStrings = {
            "",
            "plain",
            "&",
            "<>",
            "\"'",
            "a&b<c>d\"e'f",
            "&amp;",
            "&#65;",
            "&#x41;",
            "null",
            "   ",
            "\n\t",
            "&unknown;"
        };
        for (String s : testStrings) {
            String escaped = Entities.escape(s);
            String unescaped = Entities.unescape(escaped);
            Assert.assertEquals("Round-trip consistency for: '" + s + "'", s, unescaped);
        }
    }

    @Test
    public void testUnescapeWithMultipleAmpersands() {
        String input = "a&&b";
        // The first ampersand might start an entity, but "&" is not a valid entity, so it should be left as is.
        // Depending on implementation, it might be left unchanged or partially parsed.
        // This test checks for potential bugs in entity parsing.
        String result = Entities.unescape(input);
        // The expected behavior is that "&" alone is not an entity, so it should remain.
        // However, some implementations might consume the ampersand and fail.
        // We assert that the result contains the original characters.
        Assert.assertTrue("Result should contain 'a&&b'", result.contains("a&&b"));
    }

    @Test
    public void testUnescapeEntityWithMissingSemicolon() {
        String input = "&amp";
        String expected = "&amp";  // Should not be converted
        Assert.assertEquals("Entity without semicolon should remain", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithExtraChars() {
        String input = "&amp;extra";
        String expected = "&extra";
        Assert.assertEquals("Entity followed by extra chars", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeNumericEntityWithLeadingZeros() {
        String input = "&#00065;";
        String expected = "A";
        Assert.assertEquals("Numeric entity with leading zeros", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeHexEntityWithLowercaseX() {
        String input = "&#x41;";
        String expected = "A";
        Assert.assertEquals("Hex entity with lowercase x", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeHexEntityWithUppercaseX() {
        String input = "&#X41;";
        // Some implementations may accept uppercase X, others may not.
        // This test checks for consistent handling.
        String result = Entities.unescape(input);
        // If not supported, it should remain unchanged.
        if (!result.equals("A")) {
            Assert.assertEquals("Unsupported uppercase X should leave entity unchanged", input, result);
        }
    }

    @Test
    public void testUnescapeNumericEntityWithInvalidChars() {
        String input = "&#GG;";
        String expected = "&#GG;";  // Invalid numeric entity should remain
        Assert.assertEquals("Invalid numeric entity should remain", expected, Entities.unescape(input));
    }

    @Test
    public void testEscapeStringWithUnicode() {
        String input = "©®";
        // These are not HTML special chars, so they should remain unchanged
        String expected = "©®";
        Assert.assertEquals("Unicode characters should not be escaped", expected, Entities.escape(input));
    }

    @Test
    public void testUnescapeStringWithUnicode() {
        String input = "©®";
        String expected = "©®";
        Assert.assertEquals("Unicode characters should not be changed", expected, Entities.unescape(input));
    }

    @Test
    public void testEscapeStringWithNullCharacter() {
        String input = "a\0b";
        String expected = "a\0b";  // Null character should be preserved
        Assert.assertEquals("Null character should not be escaped", expected, Entities.escape(input));
    }

    @Test
    public void testUnescapeStringWithNullCharacter() {
        String input = "a\0b";
        String expected = "a\0b";
        Assert.assertEquals("Null character should not be changed", expected, Entities.unescape(input));
    }

    // -----------------------------------------------------------------------
    // Tests for potential bugs in entity parsing (Defects4J style)
    // -----------------------------------------------------------------------

    @Test
    public void testUnescapeEntityWithAmpersandAtEnd() {
        String input = "&";
        String expected = "&";  // Single ampersand should remain
        Assert.assertEquals("Single ampersand at end", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithAmpersandInMiddle() {
        String input = "a&b";
        String expected = "a&b";  // Ampersand not part of entity
        Assert.assertEquals("Ampersand in middle of text", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithDoubleAmpersand() {
        String input = "a&&b";
        String expected = "a&&b";  // Two ampersands
        Assert.assertEquals("Double ampersand", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithSemicolonInText() {
        String input = "a;b";
        String expected = "a;b";
        Assert.assertEquals("Semicolon in text should not be affected", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithHashInText() {
        String input = "a#b";
        String expected = "a#b";
        Assert.assertEquals("Hash in text should not be affected", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithXInText() {
        String input = "aXb";
        String expected = "aXb";
        Assert.assertEquals("X in text should not be affected", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithNumericEntityOverflow() {
        // Very large number that might cause integer overflow
        String input = "&#9999999999999999999;";
        String result = Entities.unescape(input);
        // Should not throw exception; may remain unchanged or produce replacement character
        Assert.assertNotNull("Should not throw exception on overflow", result);
    }

    @Test
    public void testUnescapeEntityWithNegativeNumericEntity() {
        String input = "&#-1;";
        String result = Entities.unescape(input);
        // Should handle gracefully; may remain unchanged
        Assert.assertNotNull("Should handle negative numeric entity", result);
    }

    @Test
    public void testUnescapeEntityWithHexEntityOverflow() {
        String input = "&#xFFFFFFFF;";
        String result = Entities.unescape(input);
        Assert.assertNotNull("Should handle hex overflow", result);
    }

    @Test
    public void testUnescapeEntityWithMixedCase() {
        String input = "&AMP;";  // Upper case entity name
        String expected = "&AMP;";  // Typically entity names are case-sensitive; should remain unchanged
        Assert.assertEquals("Upper case entity name should not be recognized", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithPartialMatch() {
        String input = "&am;";
        String expected = "&am;";  // Not a valid entity
        Assert.assertEquals("Partial entity name should remain", expected, Entities.unescape(input));
    }

    @Test
    public void testUnescapeEntityWithApos() {
        // &apos; is defined in XML/XHTML but not in HTML4; some implementations may not support it
        String input = "&apos;";
        String expected = "'";
        // If not supported, it should remain unchanged
        String result = Entities.unescape(input);
        if (!result.equals("'")) {
            Assert.assertEquals("If &apos; not supported, should remain unchanged", input, result);
        }
    }
}