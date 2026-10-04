package org.jsoup.nodes;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class EntitiesTest {

    @Test
    public void testEscapeBasicFunctionality() {
        String input = "Hello & <> \" '";
        String escaped = Entities.escape(input, Document.OutputSettings.Syntax.html);
        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&lt;"));
        assertTrue(escaped.contains("&gt;"));
    }

    @Test
    public void testEscapeWithOutputSettings() {
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.charset(Charset.forName("US-ASCII"));
        settings.escapeMode(Entities.EscapeMode.base);

        String input = "A\u00A0B\u0020C\u00C4"; // Non-ASCII and space/nbsp
        String escaped = Entities.escape(input, settings);
        assertNotNull(escaped);
    }

    @Test
    public void testUnescapeBasic() {
        String input = "&lt;div&gt;Hello &amp; welcome&lt;/div&gt;";
        String unescaped = Entities.unescape(input);
        assertEquals("<div>Hello & welcome</div>", unescaped);
    }

    @Test
    public void testUnescapeStrictAndLoose() {
        String input = "&unknownEntity; &amp;";
        // Depending on strict handling, check unescape behavior
        String res = Entities.unescape(input, true);
        assertNotNull(res);

        String resFalse = Entities.unescape(input, false);
        assertNotNull(resFalse);
    }

    @Test
    public void testCanEncodeCharacterCoverage() {
        CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        
        // ASCII character (should be encodable directly, not via escape mode unless strict)
        boolean canAscii = Entities.canEncode(Entities.EscapeMode.base, 'a', asciiEncoder);
        assertTrue(canAscii);

        // Non-ASCII character under ASCII charset
        boolean canNonAscii = Entities.canEncode(Entities.EscapeMode.base, '\u00C4', asciiEncoder);
        // Result depends on escape mode map and encoder
        assertFalse(canNonAscii);
    }

    @Test
    public void testEscapeModeMappings() {
        assertNotNull(Entities.EscapeMode.xhtml.codepointForName("quot"));
        assertEquals(34, Entities.EscapeMode.xhtml.codepointForName("quot"));
        assertEquals("quot", Entities.EscapeMode.xhtml.nameForCodepoint(34));

        assertNotNull(Entities.EscapeMode.base.codepointForName("amp"));
        assertEquals("amp", Entities.EscapeMode.base.nameForCodepoint(38));

        assertNotNull(Entities.EscapeMode.extended.codepointForName("cent"));
        assertEquals(162, Entities.EscapeMode.extended.codepointForName("cent"));
        assertEquals("cent", Entities.EscapeMode.extended.nameForCodepoint(162));
    }

    @Test
    public void testPadNullOrEmptyCases() {
        assertEquals("", Entities.unescape(""));
        assertEquals("", Entities.escape(""));
        
        Document.OutputSettings settings = new Document.OutputSettings();
        assertEquals("", Entities.escape("", settings));
    }

    @Test
    public void testSupplementaryCharactersAndHtmlEncoder() throws IOException {
        // Test supplementary character escaping (e.g., Emoji or high unicode)
        String supp = "\uD83D\uDE00"; // Grinning face
        String escaped = Entities.escape(supp, Document.OutputSettings.Syntax.html);
        assertNotNull(escaped);

        String unescaped = Entities.unescape(escaped);
        assertEquals(supp, unescaped);
    }
}