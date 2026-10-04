package org.apache.commons.lang3.text.translate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Test;

public class NumericEntityUnescaperTest {

    @Test
    public void testDefaultConstructor() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("A", neu.translate("&#65;"));
        assertEquals("A", neu.translate("&#x41;"));
        assertEquals("A", neu.translate("&#X41;"));
    }

    @Test
    public void testSupplementaryCharacters() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // 0x10000 -> 65536 in decimal -> supplementary character (surrogate pair)
        assertEquals("\uD800\uDC00", neu.translate("&#65536;"));
        assertEquals("\uD800\uDC00", neu.translate("&#x10000;"));
        assertEquals("\uD800\uDC00", neu.translate("&#X10000;"));
    }

    @Test
    public void testSemiColonRequiredOption() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonRequired);
        assertEquals("A", neu.translate("&#65;"));
        assertEquals("&#65", neu.translate("&#65"));
        assertEquals("&#x41", neu.translate("&#x41"));
        assertEquals("&#X41", neu.translate("&#X41"));
    }

    @Test
    public void testSemiColonOptionalOption() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonOptional);
        assertEquals("A", neu.translate("&#65;"));
        assertEquals("A", neu.translate("&#65"));
        assertEquals("A", neu.translate("&#x41"));
        assertEquals("A", neu.translate("&#X41"));
        assertEquals("AB", neu.translate("&#65;B"));
        assertEquals("AB", neu.translate("&#65B"));
    }

    @Test
    public void testErrorIfNoSemiColonOption() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.errorIfNoSemiColon);
        assertEquals("A", neu.translate("&#65;"));
        assertEquals("A", neu.translate("&#x41;"));

        try {
            neu.translate("&#65");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            neu.translate("&#x41");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testUnfinishedEntity() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("&#", neu.translate("&#"));
        assertEquals("&#x", neu.translate("&#x"));
        assertEquals("&#X", neu.translate("&#X"));
        assertEquals("&#;", neu.translate("&#;"));
        assertEquals("&#x;", neu.translate("&#x;"));
        assertEquals("&#X;", neu.translate("&#X;"));
        assertEquals("test &# and &#x and &#X test", neu.translate("test &# and &#x and &#X test"));
    }

    @Test
    public void testOutOfBounds() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("Funny &# chars at the end &#", neu.translate("Funny &# chars at the end &#"));
        assertEquals("Funny &#x chars at the end &#x", neu.translate("Funny &#x chars at the end &#x"));
        assertEquals("Funny &#X chars at the end &#X", neu.translate("Funny &#X chars at the end &#X"));
    }

    @Test
    public void testNonEntityAmpersand() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("&", neu.translate("&"));
        assertEquals("&abc", neu.translate("&abc"));
        assertEquals("Hello & World", neu.translate("Hello & World"));
        assertEquals("&#xyz;", neu.translate("&#xyz;"));
        assertEquals("&#xzz", neu.translate("&#xzz"));
    }

    @Test
    public void testTranslateWriterDirectly() throws IOException {
        NumericEntityUnescaper neu = new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonOptional);
        StringWriter sw = new StringWriter();
        int consumed = neu.translate("&#65;", 0, sw);
        assertEquals(5, consumed);
        assertEquals("A", sw.toString());

        sw = new StringWriter();
        consumed = neu.translate("&#65", 0, sw);
        assertEquals(4, consumed);
        assertEquals("A", sw.toString());

        sw = new StringWriter();
        consumed = neu.translate("test", 0, sw);
        assertEquals(0, consumed);
        assertEquals("", sw.toString());
    }

    @Test
    public void testEmptyAndNullStrings() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("", neu.translate(""));
        assertEquals(null, neu.translate(null));
    }
}