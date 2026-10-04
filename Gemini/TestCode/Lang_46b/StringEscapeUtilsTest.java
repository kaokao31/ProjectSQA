package org.apache.commons.lang;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StringEscapeUtilsTest {

    @Test
    public void testConstructor() throws Exception {
        Constructor<StringEscapeUtils> constructor = StringEscapeUtils.class.getDeclaredConstructor();
        assertTrue(Modifier.isPublic(constructor.getModifiers()));
        assertNotNull(new StringEscapeUtils());
    }

    @Test
    public void testEscapeJavaWithSlash() {
        final String input = "String with a slash (/) in it";
        final String expected = input;
        final String actual = StringEscapeUtils.escapeJava(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapeJava() throws IOException {
        assertNull(StringEscapeUtils.escapeJava(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeJava(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.escapeJava(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJava("He didn't say, \"Stop!\""));
        assertEquals("tab\\t carriage return\\r line feed\\n form feed\\f backspace\\b",
                StringEscapeUtils.escapeJava("tab\t carriage return\r line feed\n form feed\f backspace\b"));
        assertEquals("foo\\\\bar", StringEscapeUtils.escapeJava("foo\\bar"));
        assertEquals("\\u0000\\u0001\\u001F", StringEscapeUtils.escapeJava("\u0000\u0001\u001F"));
        assertEquals("test\\u007F", StringEscapeUtils.escapeJava("test\u007F"));
        assertEquals("test\\u00A0", StringEscapeUtils.escapeJava("test\u00A0"));
        assertEquals("test\\u00FF", StringEscapeUtils.escapeJava("test\u00FF"));
        assertEquals("test\\u0100", StringEscapeUtils.escapeJava("test\u0100"));
        assertEquals("test\\u1234", StringEscapeUtils.escapeJava("test\u1234"));
    }

    @Test
    public void testEscapeJavaScript() throws IOException {
        assertNull(StringEscapeUtils.escapeJavaScript(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeJavaScript(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.escapeJavaScript(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJavaScript("He didn't say, \"Stop!\""));
        assertEquals("document.write(\\\"\\/foo\\/\\\");", StringEscapeUtils.escapeJavaScript("document.write(\"/foo/\");"));
        assertEquals("tab\\t carriage return\\r line feed\\n form feed\\f backspace\\b",
                StringEscapeUtils.escapeJavaScript("tab\t carriage return\r line feed\n form feed\f backspace\b"));
    }

    @Test
    public void testUnescapeJava() throws IOException {
        assertNull(StringEscapeUtils.unescapeJava(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeJava(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.unescapeJava(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("He didn't say, \"Stop!\"", StringEscapeUtils.unescapeJava("He didn't say, \"Stop!\""));
        assertEquals("He didn't say, \"Stop!\"", StringEscapeUtils.unescapeJava("He didn\\'t say, \\\"Stop!\\\""));
        assertEquals("tab\t carriage return\r line feed\n form feed\f backspace\b",
                StringEscapeUtils.unescapeJava("tab\\t carriage return\\r line feed\\n form feed\\f backspace\\b"));
        assertEquals("foo\\bar", StringEscapeUtils.unescapeJava("foo\\\\bar"));
        assertEquals("\u0000\u0001\u001F", StringEscapeUtils.unescapeJava("\\u0000\\u0001\\u001F"));
        assertEquals("test\u007F", StringEscapeUtils.unescapeJava("test\\u007F"));
        assertEquals("test\u00A0", StringEscapeUtils.unescapeJava("test\\u00A0"));
        assertEquals("test\u00FF", StringEscapeUtils.unescapeJava("test\\u00FF"));
        assertEquals("test\u0100", StringEscapeUtils.unescapeJava("test\\u0100"));
        assertEquals("test\u1234", StringEscapeUtils.unescapeJava("test\\u1234"));

        // Octal escape sequences
        assertEquals("\0", StringEscapeUtils.unescapeJava("\\0"));
        assertEquals("\1", StringEscapeUtils.unescapeJava("\\1"));
        assertEquals("\7", StringEscapeUtils.unescapeJava("\\7"));
        assertEquals("\07", StringEscapeUtils.unescapeJava("\\07"));
        assertEquals("\377", StringEscapeUtils.unescapeJava("\\377"));
        assertEquals("\7" + "8", StringEscapeUtils.unescapeJava("\\78"));
        assertEquals("\77" + "8", StringEscapeUtils.unescapeJava("\\778"));

        // Unterminated / malformed escapes
        try {
            StringEscapeUtils.unescapeJava("\\u000");
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            // expected
        }

        try {
            StringEscapeUtils.unescapeJava("\\u000Z");
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            // expected
        }

        assertEquals("test\\", StringEscapeUtils.unescapeJava("test\\"));
        assertEquals("test\\k", StringEscapeUtils.unescapeJava("test\\k"));
    }

    @Test
    public void testUnescapeJavaScript() throws IOException {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.unescapeJavaScript(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("He didn't say, \"Stop!\"", StringEscapeUtils.unescapeJavaScript("He didn\\'t say, \\\"Stop!\\\""));
        assertEquals("document.write(\"/foo/\");", StringEscapeUtils.unescapeJavaScript("document.write(\\\"\\/foo\\/\\\");"));
    }

    @Test
    public void testEscapeHtml() throws IOException {
        assertNull(StringEscapeUtils.escapeHtml(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeHtml(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.escapeHtml(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("&quot;bread&quot; &amp; &quot;butter&quot;", StringEscapeUtils.escapeHtml("\"bread\" & \"butter\""));
        assertEquals("&lt;head&gt;", StringEscapeUtils.escapeHtml("<head>"));
        assertEquals("&copy; 2023", StringEscapeUtils.escapeHtml("\u00A9 2023"));
        assertEquals("no-entity", StringEscapeUtils.escapeHtml("no-entity"));
        assertEquals("&#1234;", StringEscapeUtils.escapeHtml("\u04D2"));
    }

    @Test
    public void testUnescapeHtml() throws IOException {
        assertNull(StringEscapeUtils.unescapeHtml(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeHtml(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.unescapeHtml(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("\"bread\" & \"butter\"", StringEscapeUtils.unescapeHtml("&quot;bread&quot; &amp; &quot;butter&quot;"));
        assertEquals("<head>", StringEscapeUtils.unescapeHtml("&lt;head&gt;"));
        assertEquals("\u00A9 2023", StringEscapeUtils.unescapeHtml("&copy; 2023"));
        assertEquals("\u00A9 2023", StringEscapeUtils.unescapeHtml("&#169; 2023"));
        assertEquals("\u00A9 2023", StringEscapeUtils.unescapeHtml("&#x00a9; 2023"));
        assertEquals("&zzzz;", StringEscapeUtils.unescapeHtml("&zzzz;"));
        assertEquals("&#xZZZZ;", StringEscapeUtils.unescapeHtml("&#xZZZZ;"));
        assertEquals("&#9999999999;", StringEscapeUtils.unescapeHtml("&#9999999999;"));
    }

    @Test
    public void testEscapeXml() throws IOException {
        assertNull(StringEscapeUtils.escapeXml(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeXml(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.escapeXml(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("&quot;bread&quot; &amp; &apos;butter&apos;", StringEscapeUtils.escapeXml("\"bread\" & 'butter'"));
        assertEquals("&lt;head&gt;", StringEscapeUtils.escapeXml("<head>"));
        assertEquals("abc", StringEscapeUtils.escapeXml("abc"));
        assertEquals("&#169;", StringEscapeUtils.escapeXml("\u00A9"));
        assertEquals("&#1234;", StringEscapeUtils.escapeXml("\u04D2"));
    }

    @Test
    public void testUnescapeXml() throws IOException {
        assertNull(StringEscapeUtils.unescapeXml(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeXml(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.unescapeXml(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("\"bread\" & 'butter'", StringEscapeUtils.unescapeXml("&quot;bread&quot; &amp; &apos;butter&apos;"));
        assertEquals("<head>", StringEscapeUtils.unescapeXml("&lt;head&gt;"));
        assertEquals("\u00A9", StringEscapeUtils.unescapeXml("&#169;"));
        assertEquals("\u00A9", StringEscapeUtils.unescapeXml("&#xA9;"));
        assertEquals("&unknown;", StringEscapeXml("&unknown;"));
    }

    @Test
    public void testEscapeSql() {
        assertNull(StringEscapeUtils.escapeSql(null));
        assertEquals("Boris''s book", StringEscapeUtils.escapeSql("Boris's book"));
        assertEquals("Boris''s ''book''", StringEscapeUtils.escapeSql("Boris's 'book'"));
        assertEquals("clean", StringEscapeUtils.escapeSql("clean"));
    }

    @Test
    public void testEscapeCsv() throws IOException {
        assertNull(StringEscapeUtils.escapeCsv(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeCsv(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.escapeCsv(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("foo", StringEscapeUtils.escapeCsv("foo"));
        assertEquals("\"foo,bar\"", StringEscapeUtils.escapeCsv("foo,bar"));
        assertEquals("\"foo\rbar\"", StringEscapeUtils.escapeCsv("foo\rbar"));
        assertEquals("\"foo\nbar\"", StringEscapeUtils.escapeCsv("foo\nbar"));
        assertEquals("\"foo\"\"bar\"", StringEscapeUtils.escapeCsv("foo\"bar"));
        assertEquals("\"\"\"foo\"\"\"", StringEscapeUtils.escapeCsv("\"foo\""));
        assertEquals("", StringEscapeUtils.escapeCsv(""));
    }

    @Test
    public void testUnescapeCsv() throws IOException {
        assertNull(StringEscapeUtils.unescapeCsv(null));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.unescapeCsv(sw, null);
        assertEquals("", sw.toString());

        try {
            StringEscapeUtils.unescapeCsv(null, "test");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        assertEquals("foo", StringEscapeUtils.unescapeCsv("foo"));
        assertEquals("foo,bar", StringEscapeUtils.unescapeCsv("\"foo,bar\""));
        assertEquals("foo\rbar", StringEscapeUtils.unescapeCsv("\"foo\rbar\""));
        assertEquals("foo\nbar", StringEscapeUtils.unescapeCsv("\"foo\nbar\""));
        assertEquals("foo\"bar", StringEscapeUtils.unescapeCsv("\"foo\"\"bar\""));
        assertEquals("\"foo\"", StringEscapeUtils.unescapeCsv("\"\"\"foo\"\"\""));
        assertEquals("", StringEscapeUtils.unescapeCsv(""));
        assertEquals("foo\"bar", StringEscapeUtils.unescapeCsv("foo\"bar"));
        assertEquals("\"foo", StringEscapeUtils.unescapeCsv("\"foo"));
        assertEquals("foo\"", StringEscapeUtils.unescapeCsv("foo\""));
        assertEquals("\"foo,bar", StringEscapeUtils.unescapeCsv("\"foo,bar"));
        assertEquals("\"\"", StringEscapeUtils.unescapeCsv("\"\""));
    }
}