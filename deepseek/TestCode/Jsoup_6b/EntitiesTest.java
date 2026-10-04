package org.jsoup.nodes;

import org.junit.Test;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntitiesTest {

    private CharsetEncoder asciiEncoder() {
        return StandardCharsets.US_ASCII.newEncoder();
    }

    @Test
    public void escapeEscapesCoreEntitiesInBaseMode() {
        assertEquals("&amp;&lt;&gt;&quot;",
                Entities.escape("&<>\"", asciiEncoder()));
    }

    @Test
    public void escapeLeavesOrdinaryCharactersAlone() {
        assertEquals("plain text 123", Entities.escape("plain text 123", asciiEncoder()));
    }

    @Test
    public void escapeUsesNumericReferenceForCharsOutsideEncoder() {
        assertEquals("&#4660;", Entities.escape("\u1234", asciiEncoder()));
    }

    @Test
    public void escapeUsesNumericReferenceForNotEncodableInExtendedMode() {
        assertEquals("&#4660;", Entities.escape("\u1234", asciiEncoder(), Entities.EscapeMode.extended));
    }

    @Test
    public void escapeExtendedModeCanUseNamedEntity() {
        assertEquals("&copy;", Entities.escape("\u00a9", asciiEncoder(), Entities.EscapeMode.extended));
    }

    @Test
    public void unescapeReplacesBasicNamedEntities() {
        assertEquals("<>&\"\u00a0", Entities.unescape("&lt;&gt;&amp;&quot;&nbsp;"));
    }

    @Test
    public void unescapeReplacesDecimalNumericReferences() {
        assertEquals("\u00e9", Entities.unescape("&#233;"));
    }

    @Test
    public void unescapeReplacesHexNumericReferences() {
        assertEquals("\u00e9", Entities.unescape("&#xE9;"));
        assertEquals("\u00e9", Entities.unescape("&#XE9;"));
    }

    @Test
    public void unescapeReplacesNamedEntitiesThatContainDigits() {
        assertEquals("\u00bd", Entities.unescape("&frac12;"));
        assertEquals("\u00bd", Entities.unescape("&frac12"));
        assertEquals("\u00bc", Entities.unescape("&frac14;"));
        assertEquals("\u00be", Entities.unescape("&frac34;"));
        assertEquals("\u00b2", Entities.unescape("&sup2;"));
        assertEquals("\u00b3", Entities.unescape("&sup3;"));
    }

    @Test
    public void unescapeStrictRequiresSemicolon() {
        assertEquals("\u00bd", Entities.unescape("&frac12;", true));
        assertEquals("&amp", Entities.unescape("&amp", true));
    }

    @Test
    public void unescapeLooseAllowsMissingSemicolon() {
        assertEquals("&", Entities.unescape("&amp"));
    }

    @Test
    public void unescapeLeavesUnknownEntitiesUnchanged() {
        assertEquals("&foo;", Entities.unescape("&foo;"));
        assertEquals("&noentity", Entities.unescape("&noentity"));
    }

    @Test
    public void unescapeHandlesMultipleEntitiesInOneString() {
        assertEquals("<x> & y", Entities.unescape("&lt;x&gt; &amp; y"));
    }

    @Test
    public void isNamedEntityRecognizesKnownNames() {
        assertTrue(Entities.isNamedEntity("amp"));
        assertTrue(Entities.isNamedEntity("frac12"));
        assertFalse(Entities.isNamedEntity("not-a-real-entity"));
    }

    @Test
    public void getCharacterByNameReturnsMappedCharacter() {
        assertEquals('\u00bd', Entities.getCharacterByName("frac12"));
    }
}