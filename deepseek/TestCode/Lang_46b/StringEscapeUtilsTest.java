package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for StringEscapeUtils, targeting bug 46 (slash escaping).
 */
public class StringEscapeUtilsTest {

    // --- escapeJava tests ---

    @Test
    public void testEscapeJavaWithSlash() {
        // Bug 46: slash should not be escaped, but it is.
        String input = "a string with a slash (/) in it";
        String expected = "a string with a slash (/) in it";
        String actual = StringEscapeUtils.escapeJava(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapeJavaNull() {
        assertNull(StringEscapeUtils.escapeJava(null));
    }

    @Test
    public void testEscapeJavaEmpty() {
        assertEquals("", StringEscapeUtils.escapeJava(""));
    }

    @Test
    public void testEscapeJavaNoSpecialChars() {
        assertEquals("Hello World", StringEscapeUtils.escapeJava("Hello World"));
    }

    @Test
    public void testEscapeJavaSpecialChars() {
        // Tab, newline, carriage return, backslash, single quote, double quote
        String input = "tab\tnewline\ncarriage\rbackslash\\single'quote\"double";
        String expected = "tab\\tnewline\\ncarriage\\rbackslash\\\\single\\'quote\\\"double";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test
    public void testEscapeJavaUnicode() {
        String input = "\u00A0\u00A1";
        String expected = "\\u00A0\\u00A1";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test
    public void testEscapeJavaForwardSlashNotEscaped() {
        // Ensure forward slash is not escaped (bug 46)
        assertEquals("/", StringEscapeUtils.escapeJava("/"));
        assertEquals("a/b", StringEscapeUtils.escapeJava("a/b"));
        assertEquals("///", StringEscapeUtils.escapeJava("///"));
    }

    @Test
    public void testEscapeJavaBackslashEscaped() {
        assertEquals("\\\\", StringEscapeUtils.escapeJava("\\"));
        assertEquals("a\\\\b", StringEscapeUtils.escapeJava("a\\b"));
    }

    // --- escapeJavaScript tests ---

    @Test
    public void testEscapeJavaScriptWithSlash() {
        // Same bug likely present
        String input = "a string with a slash (/) in it";
        String expected = "a string with a slash (/) in it";
        String actual = StringEscapeUtils.escapeJavaScript(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapeJavaScriptNull() {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
    }

    @Test
    public void testEscapeJavaScriptEmpty() {
        assertEquals("", StringEscapeUtils.escapeJavaScript(""));
    }

    @Test
    public void testEscapeJavaScriptSpecialChars() {
        String input = "tab\tnewline\ncarriage\rbackslash\\single'quote\"double";
        String expected = "tab\\tnewline\\ncarriage\\rbackslash\\\\single\\'quote\\\"double";
        assertEquals(expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaScriptForwardSlashNotEscaped() {
        assertEquals("/", StringEscapeUtils.escapeJavaScript("/"));
        assertEquals("a/b", StringEscapeUtils.escapeJavaScript("a/b"));
    }

    // --- escapeHtml tests ---

    @Test
    public void testEscapeHtmlNull() {
        assertNull(StringEscapeUtils.escapeHtml(null));
    }

    @Test
    public void testEscapeHtmlEmpty() {
        assertEquals("", StringEscapeUtils.escapeHtml(""));
    }

    @Test
    public void testEscapeHtmlNoSpecialChars() {
        assertEquals("Hello World", StringEscapeUtils.escapeHtml("Hello World"));
    }

    @Test
    public void testEscapeHtmlSpecialChars() {
        String input = "<>&\"'";
        String expected = "&lt;&gt;&amp;&quot;&#39;";
        assertEquals(expected, StringEscapeUtils.escapeHtml(input));
    }

    @Test
    public void testEscapeHtmlAmpersand() {
        assertEquals("&amp;", StringEscapeUtils.escapeHtml("&"));
    }

    // --- escapeXml tests ---

    @Test
    public void testEscapeXmlNull() {
        assertNull(StringEscapeUtils.escapeXml(null));
    }

    @Test
    public void testEscapeXmlEmpty() {
        assertEquals("", StringEscapeUtils.escapeXml(""));
    }

    @Test
    public void testEscapeXmlSpecialChars() {
        String input = "<>&\"'";
        String expected = "&lt;&gt;&amp;&quot;&apos;";
        assertEquals(expected, StringEscapeUtils.escapeXml(input));
    }

    // --- unescapeJava tests ---

    @Test
    public void testUnescapeJavaNull() {
        assertNull(StringEscapeUtils.unescapeJava(null));
    }

    @Test
    public void testUnescapeJavaEmpty() {
        assertEquals("", StringEscapeUtils.unescapeJava(""));
    }

    @Test
    public void testUnescapeJavaSimple() {
        assertEquals("Hello", StringEscapeUtils.unescapeJava("Hello"));
    }

    @Test
    public void testUnescapeJavaSpecialChars() {
        String input = "tab\\tnewline\\ncarriage\\rbackslash\\\\single\\'quote\\\"double";
        String expected = "tab\tnewline\ncarriage\rbackslash\\single'quote\"double";
        assertEquals(expected, StringEscapeUtils.unescapeJava(input));
    }

    @Test
    public void testUnescapeJavaUnicode() {
        assertEquals("\u00A0", StringEscapeUtils.unescapeJava("\\u00A0"));
    }

    @Test
    public void testUnescapeJavaForwardSlash() {
        assertEquals("/", StringEscapeUtils.unescapeJava("/"));
        assertEquals("a/b", StringEscapeUtils.unescapeJava("a/b"));
    }

    // --- unescapeJavaScript tests ---

    @Test
    public void testUnescapeJavaScriptNull() {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
    }

    @Test
    public void testUnescapeJavaScriptEmpty() {
        assertEquals("", StringEscapeUtils.unescapeJavaScript(""));
    }

    @Test
    public void testUnescapeJavaScriptSpecialChars() {
        String input = "tab\\tnewline\\ncarriage\\rbackslash\\\\single\\'quote\\\"double";
        String expected = "tab\tnewline\ncarriage\rbackslash\\single'quote\"double";
        assertEquals(expected, StringEscapeUtils.unescapeJavaScript(input));
    }

    // --- unescapeHtml tests ---

    @Test
    public void testUnescapeHtmlNull() {
        assertNull(StringEscapeUtils.unescapeHtml(null));
    }

    @Test
    public void testUnescapeHtmlEmpty() {
        assertEquals("", StringEscapeUtils.unescapeHtml(""));
    }

    @Test
    public void testUnescapeHtmlSimple() {
        assertEquals("Hello", StringEscapeUtils.unescapeHtml("Hello"));
    }

    @Test
    public void testUnescapeHtmlEntities() {
        String input = "&lt;&gt;&amp;&quot;&#39;";
        String expected = "<>&\"'";
        assertEquals(expected, StringEscapeUtils.unescapeHtml(input));
    }

    // --- unescapeXml tests ---

    @Test
    public void testUnescapeXmlNull() {
        assertNull(StringEscapeUtils.unescapeXml(null));
    }

    @Test
    public void testUnescapeXmlEmpty() {
        assertEquals("", StringEscapeUtils.unescapeXml(""));
    }

    @Test
    public void testUnescapeXmlEntities() {
        String input = "&lt;&gt;&amp;&quot;&apos;";
        String expected = "<>&\"'";
        assertEquals(expected, StringEscapeUtils.unescapeXml(input));
    }

    // --- Additional edge cases ---

    @Test
    public void testEscapeJavaMixed() {
        String input = "a/b\\c\"d'e\tf\ng";
        String expected = "a/b\\\\c\\\"d\\'e\\tf\\ng";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test
    public void testEscapeJavaScriptMixed() {
        String input = "a/b\\c\"d'e\tf\ng";
        String expected = "a/b\\\\c\\\"d\\'e\\tf\\ng";
        assertEquals(expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test
    public void testEscapeJavaControlChars() {
        // All control characters from 0x00 to 0x1F except those with special escapes
        StringBuilder sb = new StringBuilder();
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i <= 0x1F; i++) {
            char c = (char) i;
            sb.append(c);
            if (c == '\b') expected.append("\\b");
            else if (c == '\t') expected.append("\\t");
            else if (c == '\n') expected.append("\\n");
            else if (c == '\f') expected.append("\\f");
            else if (c == '\r') expected.append("\\r");
            else expected.append("\\u").append(String.format("%04X", i));
        }
        assertEquals(expected.toString(), StringEscapeUtils.escapeJava(sb.toString()));
    }

    @Test
    public void testEscapeJavaUnicodeSurrogate() {
        // High surrogate pair
        String input = "\uD800\uDC00";
        String expected = "\\uD800\\uDC00";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test
    public void testUnescapeJavaInvalidUnicode() {
        // Invalid unicode escape should be left as is
        assertEquals("\\uGGGG", StringEscapeUtils.unescapeJava("\\uGGGG"));
    }

    @Test
    public void testUnescapeJavaUnfinishedEscape() {
        assertEquals("\\", StringEscapeUtils.unescapeJava("\\"));
        assertEquals("\\u", StringEscapeUtils.unescapeJava("\\u"));
        assertEquals("\\u0", StringEscapeUtils.unescapeJava("\\u0"));
    }

    @Test
    public void testEscapeHtmlHighUnicode() {
        String input = "\u00A9";
        String expected = "&#169;";
        assertEquals(expected, StringEscapeUtils.escapeHtml(input));
    }

    @Test
    public void testUnescapeHtmlNumeric() {
        assertEquals("\u00A9", StringEscapeUtils.unescapeHtml("&#169;"));
        assertEquals("\u00A9", StringEscapeUtils.unescapeHtml("&#x00A9;"));
    }
}