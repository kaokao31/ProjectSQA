package org.jsoup.nodes;

import org.junit.Test;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for {@link Entities} class.
 * Designed to achieve high code coverage and detect the Defects4J bug #28
 * (malformed entity handling in unescape).
 */
public class EntitiesTest {

    private static final CharsetEncoder asciiEncoder = StandardCharsets.US_ASCII.newEncoder();

    // ==================== unescape tests ====================

    @Test
    public void testUnescapeNull() {
        assertNull(Entities.unescape(null));
    }

    @Test
    public void testUnescapeEmpty() {
        assertEquals("", Entities.unescape(""));
    }

    @Test
    public void testUnescapeNoEntities() {
        assertEquals("Hello World", Entities.unescape("Hello World"));
    }

    @Test
    public void testUnescapeBasicNamedEntities() {
        assertEquals("&", Entities.unescape("&amp;"));
        assertEquals("<", Entities.unescape("&lt;"));
        assertEquals(">", Entities.unescape("&gt;"));
        assertEquals("\"", Entities.unescape("&quot;"));
        assertEquals("'", Entities.unescape("&apos;"));
        assertEquals("\u00A0", Entities.unescape("&nbsp;"));
    }

    @Test
    public void testUnescapeNumericDecimal() {
        assertEquals("A", Entities.unescape("&#65;"));
        assertEquals("Z", Entities.unescape("&#90;"));
        assertEquals("\u00E9", Entities.unescape("&#233;"));
    }

    @Test
    public void testUnescapeNumericHex() {
        assertEquals("A", Entities.unescape("&#x41;"));
        assertEquals("Z", Entities.unescape("&#x5A;"));
        assertEquals("\u00E9", Entities.unescape("&#xE9;"));
        assertEquals("\u00E9", Entities.unescape("&#xe9;"));
    }

    @Test
    public void testUnescapeMultipleEntities() {
        assertEquals("& < > \" ' \u00A0",
                Entities.unescape("&amp; &lt; &gt; &quot; &apos; &nbsp;"));
    }

    @Test
    public void testUnescapeMixedWithText() {
        assertEquals("Hello & World <test>",
                Entities.unescape("Hello &amp; World &lt;test&gt;"));
    }

    @Test
    public void testUnescapeMissingSemicolon() {
        // Bug #28: malformed entity without semicolon should not throw
        assertEquals("&amp", Entities.unescape("&amp"));
        assertEquals("&#65", Entities.unescape("&#65"));
        assertEquals("&#x41", Entities.unescape("&#x41"));
    }

    @Test
    public void testUnescapeLoneAmpersand() {
        assertEquals("&", Entities.unescape("&"));
        assertEquals("& ", Entities.unescape("& "));
    }

    @Test
    public void testUnescapeIncompleteNumeric() {
        assertEquals("&#", Entities.unescape("&#"));
        assertEquals("&#x", Entities.unescape("&#x"));
        assertEquals("&#X", Entities.unescape("&#X"));
    }

    @Test
    public void testUnescapeUnknownEntity() {
        assertEquals("&unknown;", Entities.unescape("&unknown;"));
        assertEquals("&notreal;", Entities.unescape("&notreal;"));
    }

    @Test
    public void testUnescapeCaseSensitivity() {
        // Named entities are case-sensitive in HTML5; Jsoup should treat them as such
        assertEquals("&AMP;", Entities.unescape("&AMP;"));
        assertEquals("&Lt;", Entities.unescape("&Lt;"));
    }

    @Test
    public void testUnescapeWithTrailingText() {
        assertEquals("&ampx", Entities.unescape("&ampx"));
        assertEquals("&#65extra", Entities.unescape("&#65extra"));
    }

    @Test
    public void testUnescapeEmptyEntity() {
        assertEquals("&;", Entities.unescape("&;"));
    }

    // ==================== escape tests ====================

    @Test
    public void testEscapeNull() {
        assertNull(Entities.escape(null, asciiEncoder, Entities.EscapeMode.base));
        assertNull(Entities.escape(null, asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeEmpty() {
        assertEquals("", Entities.escape("", asciiEncoder, Entities.EscapeMode.base));
        assertEquals("", Entities.escape("", asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeNoSpecialChars() {
        String plain = "Hello World";
        assertEquals(plain, Entities.escape(plain, asciiEncoder, Entities.EscapeMode.base));
        assertEquals(plain, Entities.escape(plain, asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeAmpersand() {
        assertEquals("&amp;", Entities.escape("&", asciiEncoder, Entities.EscapeMode.base));
        assertEquals("&amp;", Entities.escape("&", asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeLessThan() {
        assertEquals("&lt;", Entities.escape("<", asciiEncoder, Entities.EscapeMode.base));
        assertEquals("&lt;", Entities.escape("<", asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeGreaterThan() {
        assertEquals("&gt;", Entities.escape(">", asciiEncoder, Entities.EscapeMode.base));
        assertEquals("&gt;", Entities.escape(">", asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeQuot() {
        assertEquals("&quot;", Entities.escape("\"", asciiEncoder, Entities.EscapeMode.base));
        assertEquals("&quot;", Entities.escape("\"", asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeExtendedNonAscii() {
        // Extended mode should escape non-ASCII characters
        String input = "\u00E9";
        String expectedBase = "\u00E9"; // base mode may leave it as is
        String expectedExtended = "&eacute;";
        assertEquals(expectedBase, Entities.escape(input, asciiEncoder, Entities.EscapeMode.base));
        assertEquals(expectedExtended, Entities.escape(input, asciiEncoder, Entities.EscapeMode.extended));
    }

    @Test
    public void testEscapeMixed() {
        String input = "Hello & <World>";
        String expectedBase = "Hello &amp; &lt;World&gt;";
        assertEquals(expectedBase, Entities.escape(input, asciiEncoder, Entities.EscapeMode.base));
    }

    // ==================== isNamedEntity tests ====================

    @Test
    public void testIsNamedEntityValid() {
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("lt"));
        assertTrue(Entities.isNamedEntity("gt"));
        assertTrue(Entities.isNamedEntity("quot"));
        assertTrue(Entities.isNamedEntity("apos"));
        assertTrue(Entities.isNamedEntity("nbsp"));
        assertTrue(Entities.isNamedEntity("eacute"));
    }

    @Test
    public void testIsNamedEntityInvalid() {
        assertFalse(Entities.isNamedEntity("unknown"));
        assertFalse(Entities.isNamedEntity("AMP")); // case-sensitive
        assertFalse(Entities.isNamedEntity(""));
        assertFalse(Entities.isNamedEntity(null));
    }

    // ==================== isBaseNamedEntity tests ====================

    @Test
    public void testIsBaseNamedEntityValid() {
        assertTrue(Entities.isBaseNamedEntity("amp"));
        assertTrue(Entities.isBaseNamedEntity("lt"));
        assertTrue(Entities.isBaseNamedEntity("gt"));
        assertTrue(Entities.isBaseNamedEntity("quot"));
        assertTrue(Entities.isBaseNamedEntity("apos"));
        assertTrue(Entities.isBaseNamedEntity("nbsp"));
    }

    @Test
    public void testIsBaseNamedEntityInvalid() {
        assertFalse(Entities.isBaseNamedEntity("eacute")); // extended entity
        assertFalse(Entities.isBaseNamedEntity("unknown"));
        assertFalse(Entities.isBaseNamedEntity(""));
        assertFalse(Entities.isBaseNamedEntity(null));
    }

    // ==================== getCharacterByName tests ====================

    @Test
    public void testGetCharacterByNameValid() {
        assertEquals(38, Entities.getCharacterByName("amp"));   // &
        assertEquals(60, Entities.getCharacterByName("lt"));    // <
        assertEquals(62, Entities.getCharacterByName("gt"));    // >
        assertEquals(34, Entities.getCharacterByName("quot"));  // "
        assertEquals(39, Entities.getCharacterByName("apos"));  // '
        assertEquals(160, Entities.getCharacterByName("nbsp")); // non-breaking space
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCharacterByNameInvalid() {
        Entities.getCharacterByName("unknown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCharacterByNameNull() {
        Entities.getCharacterByName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCharacterByNameEmpty() {
        Entities.getCharacterByName("");
    }

    // ==================== escape with OutputSettings (convenience) ====================

    @Test
    public void testEscapeWithOutputSettingsNull() {
        assertNull(Entities.escape(null, new Document("").outputSettings()));
    }

    @Test
    public void testEscapeWithOutputSettingsEmpty() {
        assertEquals("", Entities.escape("", new Document("").outputSettings()));
    }

    @Test
    public void testEscapeWithOutputSettingsBasic() {
        Document doc = new Document("");
        assertEquals("&amp;", Entities.escape("&", doc.outputSettings()));
    }
}