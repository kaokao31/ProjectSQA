package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;

public class StringEscapeUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new StringEscapeUtils());
    }

    @Test
    public void testEscapeJava() {
        assertNull(null);
        assertEquals("", StringEscapeUtils.escapeJava(""));
        assertEquals("\\t", StringEscapeUtils.escapeJava("\t"));
        assertEquals("hello", StringEscapeUtils.escapeJava("hello"));
        assertEquals("He\\\"llo", StringEscapeUtils.escapeJava("He\"llo"));
        assertEquals("He\\'llo", StringEscapeUtils.escapeJava("He'llo"));
        assertEquals("C:\\\\", StringEscapeUtils.escapeJava("C:\\"));
    }

    @Test
    public void testEscapeJavaWriter() throws IOException {
        StringWriter writer = null;
        try {
            StringEscapeUtils.escapeJava(writer, null);
        } catch (IllegalArgumentException e) {
            // expected or handle depending on implementation
        }

        writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, "He\"llo");
        assertEquals("He\\\"llo", writer.toString());
    }

    @Test
    public void testUnescapeJava() {
        assertNull(StringEscapeUtils.unescapeJava(null));
        assertEquals("", StringEscapeUtils.unescapeJava(""));
        assertEquals("\t", StringEscapeUtils.unescapeJava("\\t"));
        assertEquals("hello", StringEscapeUtils.unescapeJava("hello"));
        assertEquals("He\"llo", StringEscapeUtils.unescapeJava("He\\\"llo"));
        assertEquals("C:\\", StringEscapeUtils.unescapeJava("C:\\\\"));
    }

    @Test
    public void testUnescapeJavaWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, "He\\\"llo");
        assertEquals("He\"llo", writer.toString());
    }

    @Test
    public void testEscapeJavaScript() {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
        assertEquals("", StringEscapeUtils.escapeJavaScript(""));
        
        // This targets the specific Defects4J Lang-52 failure regarding forward slash escaping in JS
        // Expected with bug fix: "<\/script>" -> "<\\/script>" or similar depending on the exact implementation,
        // reproducing the exact bug trigger.
        assertEquals("-\\>", StringEscapeUtils.escapeJavaScript("-->"));
        assertEquals("<\\/script>", StringEscapeUtils.escapeJavaScript("</script>"));
        assertEquals("alert(\\'aaa\\');<\\/script>", StringEscapeUtils.escapeJavaScript("alert('aaa');</script>"));
    }

    @Test
    public void testEscapeJavaScriptWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, "</script>");
        assertEquals("<\\/script>", writer.toString());
    }

    @Test
    public void testEscapeHtml() {
        assertNull(StringEscapeUtils.escapeHtml(null));
        assertEquals("", StringEscapeUtils.escapeHtml(""));
        assertEquals("&lt;script&gt;", StringEscapeUtils.escapeHtml("<script>"));
        assertEquals("&amp;", StringEscapeUtils.escapeHtml("&"));
        assertEquals("&quot;", StringEscapeUtils.escapeHtml("\""));
    }

    @Test
    public void testEscapeHtmlWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeHtml(writer, "<script>");
        assertEquals("&lt;script&gt;", writer.toString());
    }

    @Test
    public void testUnescapeHtml() {
        assertNull(StringEscapeUtils.unescapeHtml(null));
        assertEquals("", StringEscapeUtils.unescapeHtml(""));
        assertEquals("<script>", StringEscapeUtils.unescapeHtml("&lt;script&gt;"));
        assertEquals("&", StringEscapeUtils.unescapeHtml("&amp;"));
        assertEquals("\"", StringEscapeUtils.unescapeHtml("&quot;"));
        assertEquals("&#32;", StringEscapeUtils.unescapeHtml("&#32;"));
    }

    @Test
    public void testUnescapeHtmlWriter() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.unescapeHtml(writer, "&lt;script&gt;");
        assertEquals("<script>", writer.toString());
    }

    @Test
    public void testEscapeSql() {
        assertNull(StringEscapeUtils.escapeSql(null));
        assertEquals("", StringEscapeUtils.escapeSql(""));
        assertEquals("Mc''Donald", StringEscapeUtils.escapeSql("Mc'Donald"));
    }
}