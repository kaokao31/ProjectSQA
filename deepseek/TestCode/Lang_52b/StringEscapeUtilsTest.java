package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for StringEscapeUtils, targeting high coverage and fault detection.
 * Specifically addresses Defects4J bug 52: escapeJavaScript does not escape forward slash.
 */
public class StringEscapeUtilsTest {

    // --- escapeJavaScript tests ---

    @Test
    public void testEscapeJavaScriptWithForwardSlash() {
        // Bug 52: forward slash should be escaped as \/
        String input = "alert('aaa');</script>";
        String expected = "alert('aaa');<\\/script>";
        String actual = StringEscapeUtils.escapeJavaScript(input);
        assertEquals("Forward slash must be escaped in JavaScript", expected, actual);
    }

    @Test
    public void testEscapeJavaScriptWithNull() {
        assertNull("Null input should return null", StringEscapeUtils.escapeJavaScript(null));
    }

    @Test
    public void testEscapeJavaScriptWithEmptyString() {
        assertEquals("Empty string should return empty", "", StringEscapeUtils.escapeJavaScript(""));
    }

    @Test
    public void testEscapeJavaScriptWithSpecialChars() {
        String input = "Hello \"world\" & <script>alert('xss')</script>";
        String expected = "Hello \\\"world\\\" & \\<script>alert(\\'xss\\')<\\/script>";
        String actual = StringEscapeUtils.escapeJavaScript(input);
        assertEquals("JavaScript escaping should handle quotes, angle brackets, and slash", expected, actual);
    }

    @Test
    public void testEscapeJavaScriptWithUnicode() {
        String input = "A\u00e9B";
        String expected = "A\\u00E9B";
        assertEquals("Unicode characters should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithNewline() {
        String input = "line1\nline2";
        String expected = "line1\\nline2";
        assertEquals("Newline should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithCarriageReturn() {
        String input = "line1\rline2";
        String expected = "line1\\rline2";
        assertEquals("Carriage return should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithTab() {
        String input = "col1\tcol2";
        String expected = "col1\\tcol2";
        assertEquals("Tab should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithBackslash() {
        String input = "path\\to\\file";
        String expected = "path\\\\to\\\\file";
        assertEquals("Backslash should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithSingleQuote() {
        String input = "it's";
        String expected = "it\\'s";
        assertEquals("Single quote should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- escapeHtml tests ---

    @Test
    public void testEscapeHtmlWithNull() {
        assertNull("Null input should return null", StringEscapeUtils.escapeHtml(null));
    }

    @Test
    public void testEscapeHtmlWithEmptyString() {
        assertEquals("Empty string should return empty", "", StringEscapeUtils.escapeHtml(""));
    }

    @Test
    public void testEscapeHtmlWithSpecialChars() {
        String input = "<div class=\"test\">& 'single'</div>";
        String expected = "&lt;div class=&quot;test&quot;&gt;&amp; &apos;single&apos;&lt;/div&gt;";
        assertEquals("HTML escaping should handle <, >, &, \", '", expected, StringEscapeUtils.escapeHtml(input));
    }

    @Test
    public void testEscapeHtmlWithNonAscii() {
        String input = "caf\u00e9";
        String expected = "caf\u00e9"; // no escaping for non-ASCII unless entity defined
        // Typically HTML escaping only escapes <, >, &, ", ' and maybe others
        // This test verifies that non-ASCII is left as is
        assertEquals("Non-ASCII characters should not be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- escapeXml tests ---

    @Test
    public void testEscapeXmlWithNull() {
        assertNull("Null input should return null", StringEscapeUtils.escapeXml(null));
    }

    @Test
    public void testEscapeXmlWithEmptyString() {
        assertEquals("Empty string should return empty", "", StringEscapeUtils.escapeXml(""));
    }

    @Test
    public void testEscapeXmlWithSpecialChars() {
        String input = "<tag attr=\"value\">& 'apos'</tag>";
        String expected = "&lt;tag attr=&quot;value&quot;&gt;&amp; &apos;apos&apos;&lt;/tag&gt;";
        assertEquals("XML escaping should handle <, >, &, \", '", expected, StringEscapeUtils.escapeXml(input));
    }

    // --- unescapeHtml tests ---

    @Test
    public void testUnescapeHtmlWithNull() {
        assertNull("Null input should return null", StringEscapeUtils.unescapeHtml(null));
    }

    @Test
    public void testUnescapeHtmlWithEmptyString() {
        assertEquals("Empty string should return empty", "", StringEscapeUtils.unescapeHtml(""));
    }

    @Test
    public void testUnescapeHtmlWithEntities() {
        String input = "&lt;div class=&quot;test&quot;&gt;&amp; &apos;single&apos;&lt;/div&gt;";
        String expected = "<div class=\"test\">& 'single'</div>";
        assertEquals("Unescape HTML should reverse escaping", expected, StringEscapeUtils.unescapeHtml(input));
    }

    @Test
    public void testUnescapeHtmlWithNumericEntities() {
        String input = "&#60;&#62;&#38;";
        String expected = "<>&";
        assertEquals("Numeric entities should be unescaped", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- unescapeXml tests ---

    @Test
    public void testUnescapeXmlWithNull() {
        assertNull("Null input should return null", StringEscapeUtils.unescapeXml(null));
    }

    @Test
    public void testUnescapeXmlWithEmptyString() {
        assertEquals("Empty string should return empty", "", StringEscapeUtils.unescapeXml(""));
    }

    @Test
    public void testUnescapeXmlWithEntities() {
        String input = "&lt;tag attr=&quot;value&quot;&gt;&amp; &apos;apos&apos;&lt;/tag&gt;";
        String expected = "<tag attr=\"value\">& 'apos'</tag>";
        assertEquals("Unescape XML should reverse escaping", expected, StringEscapeUtils.unescapeXml(input));
    }

    // --- escapeSql tests (if present) ---

    @Test
    public void testEscapeSqlWithNull() {
        assertNull("Null input should return null", StringEscapeUtils.escapeSql(null));
    }

    @Test
    public void testEscapeSqlWithEmptyString() {
        assertEquals("Empty string should return empty", "", StringEscapeUtils.escapeSql(""));
    }

    @Test
    public void testEscapeSqlWithSingleQuote() {
        String input = "O'Brien";
        String expected = "O''Brien";
        assertEquals("SQL escaping should double single quotes", expected, StringEscapeUtils.escapeSql(input));
    }

    @Test
    public void testEscapeSqlWithNoSpecialChars() {
        String input = "Hello";
        assertEquals("No special chars should remain unchanged", input, StringEscapeUtils.escapeSql(input));
    }

    // --- Additional edge cases for coverage ---

    @Test
    public void testEscapeJavaScriptWithOnlyForwardSlash() {
        String input = "/";
        String expected = "\\/";
        assertEquals("Single forward slash should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithMultipleSlashes() {
        String input = "///";
        String expected = "\\/\\/\\/";
        assertEquals("Multiple forward slashes should each be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithMixedCharacters() {
        String input = "a/b\\c'd\"e\nf\rg\th";
        String expected = "a\\/b\\\\c\\'d\\\"e\\nf\\rg\\th";
        assertEquals("Mixed special characters should all be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeHtmlWithAmpersandOnly() {
        String input = "&";
        String expected = "&amp;";
        assertEquals("Ampersand should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    @Test
    public void testEscapeHtmlWithLessThanOnly() {
        String input = "<";
        String expected = "&lt;";
        assertEquals("Less-than should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    @Test
    public void testEscapeHtmlWithGreaterThanOnly() {
        String input = ">";
        String expected = "&gt;";
        assertEquals("Greater-than should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    @Test
    public void testEscapeHtmlWithDoubleQuoteOnly() {
        String input = "\"";
        String expected = "&quot;";
        assertEquals("Double quote should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    @Test
    public void testEscapeHtmlWithSingleQuoteOnly() {
        String input = "'";
        String expected = "&apos;";
        assertEquals("Single quote should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    @Test
    public void testUnescapeHtmlWithAmpersand() {
        String input = "&amp;";
        String expected = "&";
        assertEquals("Unescape ampersand", expected, StringEscapeUtils.unescapeHtml(input));
    }

    @Test
    public void testUnescapeHtmlWithInvalidEntity() {
        String input = "&unknown;";
        String expected = "&unknown;"; // should remain unchanged
        assertEquals("Invalid entity should not be unescaped", expected, StringEscapeUtils.unescapeHtml(input));
    }

    @Test
    public void testUnescapeHtmlWithMixedEntities() {
        String input = "&lt;&amp;&gt;&quot;&apos;";
        String expected = "<&>\"'";
        assertEquals("Mixed entities should be unescaped correctly", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Tests for escapeJavaScript with writer (if method exists) ---
    // Note: The class may have overloaded methods that take a Writer. We'll test the String version primarily.

    // --- Additional coverage for escapeJavaScript with empty or whitespace ---

    @Test
    public void testEscapeJavaScriptWithWhitespace() {
        String input = "   ";
        String expected = "   "; // spaces are not escaped
        assertEquals("Whitespace should not be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithNumbers() {
        String input = "12345";
        String expected = "12345";
        assertEquals("Numbers should not be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptWithAlphanumeric() {
        String input = "abcABC123";
        String expected = "abcABC123";
        assertEquals("Alphanumeric should not be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Tests for escapeHtml with non-ASCII characters that might be escaped in some implementations ---
    // This verifies that only the five XML/HTML special chars are escaped.

    @Test
    public void testEscapeHtmlWithNonAsciiNotEscaped() {
        String input = "\u00e9\u00f1\u00fc";
        String expected = "\u00e9\u00f1\u00fc";
        assertEquals("Non-ASCII characters should not be HTML-escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Tests for escapeXml with non-ASCII ---

    @Test
    public void testEscapeXmlWithNonAsciiNotEscaped() {
        String input = "\u00e9\u00f1\u00fc";
        String expected = "\u00e9\u00f1\u00fc";
        assertEquals("Non-ASCII characters should not be XML-escaped", expected, StringEscapeUtils.escapeXml(input));
    }

    // --- Tests for unescapeHtml with hex numeric entities ---

    @Test
    public void testUnescapeHtmlWithHexNumericEntity() {
        String input = "&#x26;";
        String expected = "&";
        assertEquals("Hex numeric entity should be unescaped", expected, StringEscapeUtils.unescapeHtml(input));
    }

    @Test
    public void testUnescapeHtmlWithInvalidNumericEntity() {
        String input = "&#9999999;";
        String expected = "&#9999999;"; // likely unchanged if out of range
        assertEquals("Invalid numeric entity should remain unchanged", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Tests for escapeJavaScript with form feed (rare) ---

    @Test
    public void testEscapeJavaScriptWithFormFeed() {
        String input = "page\fbreak";
        String expected = "page\\fbreak";
        assertEquals("Form feed should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Tests for escapeJavaScript with backspace (rare) ---

    @Test
    public void testEscapeJavaScriptWithBackspace() {
        String input = "back\bspace";
        String expected = "back\\bspace";
        assertEquals("Backspace should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Tests for escapeJavaScript with vertical tab (rare) ---

    @Test
    public void testEscapeJavaScriptWithVerticalTab() {
        String input = "vertical\vtab";
        String expected = "vertical\\vtab";
        assertEquals("Vertical tab should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Tests for escapeJavaScript with null character ---

    @Test
    public void testEscapeJavaScriptWithNullChar() {
        String input = "a\0b";
        String expected = "a\\x00b";
        assertEquals("Null character should be escaped as \\x00", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Tests for escapeJavaScript with control characters ---

    @Test
    public void testEscapeJavaScriptWithControlChars() {
        String input = "\u0001\u0002\u001F";
        String expected = "\\x01\\x02\\x1F";
        assertEquals("Control characters should be escaped as hex", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Tests for escapeHtml with already escaped entities (should not double-escape) ---

    @Test
    public void testEscapeHtmlWithAlreadyEscaped() {
        String input = "&amp;";
        String expected = "&amp;amp;"; // The ampersand in "&amp;" is escaped, so & becomes &amp; and the rest remains
        // Actually, the input contains '&' which will be escaped to &amp; so result is &amp;amp;
        assertEquals("Already escaped entities should be re-escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Tests for unescapeHtml with double-escaped entities ---

    @Test
    public void testUnescapeHtmlWithDoubleEscaped() {
        String input = "&amp;amp;";
        String expected = "&amp;"; // first unescape: &amp; -> &, then amp; remains? Actually unescape is done in one pass, so &amp;amp; becomes &amp; (since &amp; is unescaped to &, then the remaining amp; is literal)
        assertEquals("Double-escaped should be unescaped once", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Tests for escapeSql with null and empty already covered, add edge with backslash? SQL escaping typically only doubles single quotes, but some implementations also escape backslash. We'll test typical behavior.

    @Test
    public void testEscapeSqlWithBackslash() {
        String input = "back\\slash";
        String expected = "back\\slash"; // backslash is not escaped by default in SQL escaping
        assertEquals("Backslash should not be escaped in SQL", expected, StringEscapeUtils.escapeSql(input));
    }

    // --- Additional coverage for escapeJavaScript with writer overload (if present) ---
    // We can't test Writer methods directly without creating a mock, but we can assume the String methods delegate.
    // For completeness, we could add a test that uses a StringWriter, but that would require additional setup.
    // Since the class likely has both, we'll focus on the String methods as per the bug.

    // --- Test for escapeJavaScript with high Unicode (surrogate pairs) ---

    @Test
    public void testEscapeJavaScriptWithSupplementaryUnicode() {
        String input = "\uD83D\uDE00"; // emoji
        String expected = "\\uD83D\\uDE00"; // typically escaped as two surrogates
        assertEquals("Supplementary Unicode should be escaped as surrogate pair", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeHtml with supplementary Unicode ---

    @Test
    public void testEscapeHtmlWithSupplementaryUnicode() {
        String input = "\uD83D\uDE00";
        String expected = "\uD83D\uDE00"; // should not be escaped
        assertEquals("Supplementary Unicode should not be HTML-escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Test for unescapeHtml with supplementary Unicode numeric entity ---

    @Test
    public void testUnescapeHtmlWithSupplementaryNumericEntity() {
        String input = "&#128512;"; // decimal for 😀
        String expected = "\uD83D\uDE00";
        assertEquals("Supplementary numeric entity should be unescaped to surrogate pair", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Test for escapeJavaScript with string containing only special chars ---

    @Test
    public void testEscapeJavaScriptWithOnlySpecialChars() {
        String input = "\"'&<>/";
        String expected = "\\\"\\'&<>\\/";
        assertEquals("Only special chars should all be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeHtml with only special chars ---

    @Test
    public void testEscapeHtmlWithOnlySpecialChars() {
        String input = "<>&\"'";
        String expected = "&lt;&gt;&amp;&quot;&apos;";
        assertEquals("Only special chars should be HTML-escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Test for escapeXml with only special chars ---

    @Test
    public void testEscapeXmlWithOnlySpecialChars() {
        String input = "<>&\"'";
        String expected = "&lt;&gt;&amp;&quot;&apos;";
        assertEquals("Only special chars should be XML-escaped", expected, StringEscapeUtils.escapeXml(input));
    }

    // --- Test for unescapeHtml with empty entity ---

    @Test
    public void testUnescapeHtmlWithEmptyEntity() {
        String input = "&;";
        String expected = "&;"; // invalid, should remain
        assertEquals("Empty entity should remain unchanged", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Test for unescapeHtml with incomplete entity at end ---

    @Test
    public void testUnescapeHtmlWithIncompleteEntity() {
        String input = "&lt";
        String expected = "&lt"; // incomplete, should remain
        assertEquals("Incomplete entity should remain unchanged", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Test for escapeJavaScript with string containing only backslash ---

    @Test
    public void testEscapeJavaScriptWithOnlyBackslash() {
        String input = "\\";
        String expected = "\\\\";
        assertEquals("Single backslash should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only single quote ---

    @Test
    public void testEscapeJavaScriptWithOnlySingleQuote() {
        String input = "'";
        String expected = "\\'";
        assertEquals("Single quote should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only double quote ---

    @Test
    public void testEscapeJavaScriptWithOnlyDoubleQuote() {
        String input = "\"";
        String expected = "\\\"";
        assertEquals("Double quote should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only newline ---

    @Test
    public void testEscapeJavaScriptWithOnlyNewline() {
        String input = "\n";
        String expected = "\\n";
        assertEquals("Newline should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only carriage return ---

    @Test
    public void testEscapeJavaScriptWithOnlyCarriageReturn() {
        String input = "\r";
        String expected = "\\r";
        assertEquals("Carriage return should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only tab ---

    @Test
    public void testEscapeJavaScriptWithOnlyTab() {
        String input = "\t";
        String expected = "\\t";
        assertEquals("Tab should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only form feed ---

    @Test
    public void testEscapeJavaScriptWithOnlyFormFeed() {
        String input = "\f";
        String expected = "\\f";
        assertEquals("Form feed should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only backspace ---

    @Test
    public void testEscapeJavaScriptWithOnlyBackspace() {
        String input = "\b";
        String expected = "\\b";
        assertEquals("Backspace should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only vertical tab ---

    @Test
    public void testEscapeJavaScriptWithOnlyVerticalTab() {
        String input = "\u000B";
        String expected = "\\v";
        assertEquals("Vertical tab should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only null char ---

    @Test
    public void testEscapeJavaScriptWithOnlyNullChar() {
        String input = "\0";
        String expected = "\\x00";
        assertEquals("Null char should be escaped", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only control char ---

    @Test
    public void testEscapeJavaScriptWithOnlyControlChar() {
        String input = "\u001F";
        String expected = "\\x1F";
        assertEquals("Control char should be escaped as hex", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeJavaScript with string containing only unicode non-BMP ---

    @Test
    public void testEscapeJavaScriptWithOnlySupplementaryChar() {
        String input = "\uD83D\uDE00";
        String expected = "\\uD83D\\uDE00";
        assertEquals("Supplementary char should be escaped as two surrogates", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // --- Test for escapeHtml with string containing only ampersand ---

    @Test
    public void testEscapeHtmlWithOnlyAmpersand() {
        String input = "&";
        String expected = "&amp;";
        assertEquals("Ampersand should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Test for escapeHtml with string containing only less-than ---

    @Test
    public void testEscapeHtmlWithOnlyLessThan() {
        String input = "<";
        String expected = "&lt;";
        assertEquals("Less-than should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Test for escapeHtml with string containing only greater-than ---

    @Test
    public void testEscapeHtmlWithOnlyGreaterThan() {
        String input = ">";
        String expected = "&gt;";
        assertEquals("Greater-than should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Test for escapeHtml with string containing only double quote ---

    @Test
    public void testEscapeHtmlWithOnlyDoubleQuote() {
        String input = "\"";
        String expected = "&quot;";
        assertEquals("Double quote should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Test for escapeHtml with string containing only single quote ---

    @Test
    public void testEscapeHtmlWithOnlySingleQuote() {
        String input = "'";
        String expected = "&apos;";
        assertEquals("Single quote should be escaped", expected, StringEscapeUtils.escapeHtml(input));
    }

    // --- Test for escapeXml with string containing only ampersand ---

    @Test
    public void testEscapeXmlWithOnlyAmpersand() {
        String input = "&";
        String expected = "&amp;";
        assertEquals("Ampersand should be escaped", expected, StringEscapeUtils.escapeXml(input));
    }

    // --- Test for escapeXml with string containing only less-than ---

    @Test
    public void testEscapeXmlWithOnlyLessThan() {
        String input = "<";
        String expected = "&lt;";
        assertEquals("Less-than should be escaped", expected, StringEscapeUtils.escapeXml(input));
    }

    // --- Test for escapeXml with string containing only greater-than ---

    @Test
    public void testEscapeXmlWithOnlyGreaterThan() {
        String input = ">";
        String expected = "&gt;";
        assertEquals("Greater-than should be escaped", expected, StringEscapeUtils.escapeXml(input));
    }

    // --- Test for escapeXml with string containing only double quote ---

    @Test
    public void testEscapeXmlWithOnlyDoubleQuote() {
        String input = "\"";
        String expected = "&quot;";
        assertEquals("Double quote should be escaped", expected, StringEscapeUtils.escapeXml(input));
    }

    // --- Test for escapeXml with string containing only single quote ---

    @Test
    public void testEscapeXmlWithOnlySingleQuote() {
        String input = "'";
        String expected = "&apos;";
        assertEquals("Single quote should be escaped", expected, StringEscapeUtils.escapeXml(input));
    }

    // --- Test for unescapeHtml with string containing only a valid entity ---

    @Test
    public void testUnescapeHtmlWithOnlyAmpersandEntity() {
        String input = "&amp;";
        String expected = "&";
        assertEquals("Unescape ampersand entity", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Test for unescapeHtml with string containing only a numeric entity ---

    @Test
    public void testUnescapeHtmlWithOnlyNumericEntity() {
        String input = "&#60;";
        String expected = "<";
        assertEquals("Unescape numeric entity", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Test for unescapeHtml with string containing only a hex entity ---

    @Test
    public void testUnescapeHtmlWithOnlyHexEntity() {
        String input = "&#x3C;";
        String expected = "<";
        assertEquals("Unescape hex entity", expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- Test for unescapeXml with string containing only a valid entity ---

    @Test
    public void testUnescapeXmlWithOnlyAmpersandEntity() {
        String input = "&amp;";
        String expected = "&";
        assertEquals("Unescape XML ampersand entity", expected, StringEscapeUtils.unescapeXml(input));
    }

    // --- Test for unescapeXml with string containing only a numeric entity ---

    @Test
    public void testUnescapeXmlWithOnlyNumericEntity() {
        String input = "&#60;";
        String expected = "<";
        assertEquals("Unescape XML numeric entity", expected, StringEscapeUtils.unescapeXml(input));
    }

    // --- Test for unescapeXml with string containing only a hex entity ---

    @Test
    public void testUnescapeXmlWithOnlyHexEntity() {
        String input = "&#x3C;";
        String expected = "<";
        assertEquals("Unescape XML hex entity", expected, StringEscapeUtils.unescapeXml(input));
    }

    // --- Test for escapeSql with string containing only single quote ---

    @Test
    public void testEscapeSqlWithOnlySingleQuote() {
        String input = "'";
        String expected = "''";
        assertEquals("Single quote should be doubled", expected, StringEscapeUtils.escapeSql(input));
    }

    // --- Test for escapeSql with string containing multiple single quotes ---

    @Test
    public void testEscapeSqlWithMultipleSingleQuotes() {
        String input = "''";
        String expected = "''''";
        assertEquals("Multiple single quotes should each be doubled", expected, StringEscapeUtils.escapeSql(input));
    }

    // --- Test for escapeSql with string containing no single quotes ---

    @Test
    public void testEscapeSqlWithNoSingleQuotes() {
        String input = "Hello World";
        String expected = "Hello World";
        assertEquals("No single quotes should remain unchanged", expected, StringEscapeUtils.escapeSql(input));
    }

    // --- Test for escapeSql with string containing mixed characters ---

    @Test
    public void testEscapeSqlWithMixed() {
        String input = "It's a \"test\"";
        String expected = "It''s a \"test\"";
        assertEquals("Only single quotes should be escaped", expected, StringEscapeUtils.escapeSql(input));
    }
}