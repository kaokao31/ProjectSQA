package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

public class EntitiesTest {

    @Test
    public void testEscapeBasic() {
        String input = "Hello & <world> \" '";
        String escaped = Entities.escape(input, Document.OutputSettings.Syntax.html, Entities.EscapeMode.base);
        assertNotNull(escaped);
        assertTrue(escaped.contains("&amp;"));
        assertTrue(escaped.contains("&lt;"));
        assertTrue(escaped.contains("&gt;"));
    }

    @Test
    public void testEscapeExtended() {
        String input = "\u00A0 \u00A9"; // Non-breaking space, copyright symbol
        String escapedHtml = Entities.escape(input, Document.OutputSettings.Syntax.html, Entities.EscapeMode.extended);
        assertNotNull(escapedHtml);
        assertTrue(escapedHtml.contains("&nbsp;") || escapedHtml.contains("&#xa0;") || escapedHtml.contains("&#160;"));
    }

    @Test
    public void testUnescape() {
        String input = "&amp; &lt; &gt; &quot; &apos; &#39; &#x26; &unknownent;";
        String unescaped = Entities.unescape(input);
        assertEquals("& < > \" ' ' & &unknownent;", unescaped);
    }

    @Test
    public void testUnescapeStrictAndLoose() {
        // Test various forms of unescaping, including numeric and named entities
        String entity = "&lt;div&gt;";
        assertEquals("<div>", Entities.unescape(entity));

        String numeric = "&#x3C;div&#x3E;";
        assertEquals("<div>", Entities.unescape(numeric));

        String decimal = "&#60;div&#62;";
        assertEquals("<div>", Entities.unescape(decimal));
    }

    @Test
    public void testIsNamedEntity() {
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("lt"));
        assertFalse(Entities.isNamedEntity("nonexistententityxyz"));
        assertFalse(Entities.isNamedEntity(null));
        assertFalse(Entities.isNamedEntity(""));
    }

    @Test
    public void testGetCharacterByName() {
        Character c = Entities.getCharacterByName("amp");
        assertNotNull(c);
        assertEquals('&', (char) c);

        assertNull(Entities.getCharacterByName("nonexistententityxyz"));
        assertNull(Entities.getCharacterByName(null));
        assertNull(Entities.getCharacterByName(""));
    }

    @Test
    public void testEscapeModesAndSyntax() {
        Document.OutputSettings out = new Document.OutputSettings();
        out.escapeMode(Entities.EscapeMode.extended);
        out.syntax(Document.OutputSettings.Syntax.html);

        String text = "© & ®";
        String result = Entities.escape(text, out);
        assertNotNull(result);
    }

    @Test
    public void testEscapeEdgeCases() {
        // Test null, empty, and ascii-only strings
        assertEquals("", Entities.escape("", Document.OutputSettings.Syntax.html, Entities.EscapeMode.base));
        assertEquals("abc123", Entities.escape("abc123", Document.OutputSettings.Syntax.html, Entities.EscapeMode.base));
    }

    @Test
    public void testUnescapeEdgeCases() {
        assertEquals("", Entities.unescape(""));
        assertNull(Entities.unescape(null));
        assertEquals("abc", Entities.unescape("abc"));
        // Ampersand trailing or standalone
        assertEquals("&", Entities.unescape("&"));
        assertEquals("&abc", Entities.unescape("&abc"));
    }

    @Test
    public void testEntitiesMapConsistency() {
        // Access internal mappings via public APIs
        Character amp = Entities.getCharacterByName("amp");
        assertEquals('&', (char) amp);
        assertTrue(Entities.isNamedEntity("amp"));
    }
}