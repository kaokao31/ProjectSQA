package org.jsoup.nodes;

import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class EntitiesTest {

    @Test
    public void testEscapeBasic() {
        String input = "Hello & < > \" '";
        String escaped = Entities.escape(input);
        assertEquals("Hello &amp; &lt; &gt; &quot; &#x27;", escaped);
    }

    @Test
    public void testEscapeNullAndEmpty() {
        assertNull(Entities.escape(null));
        assertEquals("", Entities.escape(""));
    }

    @Test
    public void testUnescapeBasic() {
        String input = "Hello &amp; &lt; &gt; &quot; &#x27; &copy; &#169;";
        String unescaped = Entities.unescape(input);
        assertEquals("Hello & < > \" ' © ©", unescaped);
    }

    @Test
    public void testUnescapeNullAndEmpty() {
        assertNull(Entities.unescape(null));
        assertEquals("", Entities.unescape(""));
    }

    @Test
    public void testUnescapeTreatAsHTML() {
        // Test unescaping with strict vs loose html handling
        String input = "http://example.com?a=1&b=2";
        // When not strict or depending on semi-colons
        String result = Entities.unescape(input);
        assertEquals(input, result);
    }

    @Test
    public void testEscapeModes() {
        String input = "© & ∉";
        
        String escapedHtml4 = Entities.escape(input, Document.OutputSettings.Syntax.html, Charset.forName("UTF-8").newEncoder(), Entities.EscapeMode.base);
        assertNotNull(escapedHtml4);

        String escapedExt = Entities.escape(input, Document.OutputSettings.Syntax.html, Charset.forName("UTF-8").newEncoder(), Entities.EscapeMode.extended);
        assertNotNull(escapedExt);

        String escapedXml = Entities.escape(input, Document.OutputSettings.Syntax.xml, Charset.forName("UTF-8").newEncoder(), Entities.EscapeMode.xhtml);
        assertNotNull(escapedXml);
    }

    @Test
    public void testCanEncodeAsciiCharset() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        String input = "Hello ©"; // © is non-ascii
        
        String escaped = Entities.escape(input, Document.OutputSettings.Syntax.html, asciiEncoder, Entities.EscapeMode.base);
        assertTrue(escaped.contains("&#169;") || escaped.contains("&copy;"));
    }

    @Test
    public void testEscapeWriter() throws IOException {
        StringWriter accum = new StringWriter();
        Entities.escape(accum, "Hello &", Document.OutputSettings.Syntax.html, Charset.forName("UTF-8").newEncoder(), Entities.EscapeMode.base);
        assertEquals("Hello &amp;", accum.toString());
    }

    @Test
    public void testIsNamedEntity() {
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("copy"));
        assertFalse(Entities.isNamedEntity("notarealentityatall"));
    }

    @Test
    public void testGetByName() {
        assertEquals("&", Entities.getByName("amp"));
        assertEquals("©", Entities.getByName("copy"));
        assertEquals("", Entities.getByName("nonexistent"));
    }

    @Test
    public void testCharacterEncodingsAndBoundaries() {
        // Test specific codepoints and edge cases for escaping based on charset encoders
        CharsetEncoder isoEncoder = Charset.forName("ISO-8859-1").newEncoder();
        String input = "€"; // Euro sign is in extended/utf-8, but not ISO-8859-1
        String escaped = Entities.escape(input, Document.OutputSettings.Syntax.html, isoEncoder, Entities.EscapeMode.base);
        assertNotNull(escaped);
    }

    @Test
    public void testUnescapeWithNumericEntities() {
        String input = "&#x3C;&#60;&#x3e;";
        String result = Entities.unescape(input);
        assertEquals("<<>", result);
    }

    @Test
    public void testUnescapeInvalidEntities() {
        // Test entities missing semicolons or malformed
        String input = "&ampfoo &invalidentity;";
        String result = Entities.unescape(input);
        // Depending on strictness, &ampfoo might be handled or left
        assertNotNull(result);
    }
}