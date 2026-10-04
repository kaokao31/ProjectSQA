package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

public class CaverphoneTest {

    private Caverphone caverphone;

    @org.junit.Before
    public void setUp() {
        caverphone = new Caverphone();
    }

    @Test
    public void testEncodeNull() {
        assertNull("Should return null for null input", caverphone.encode(null));
    }

    @Test
    public void testEncodeEmpty() {
        String result = caverphone.encode("");
        assertEquals("Empty string should produce default code", "1111111111", result);
    }

    @Test
    public void testEncodeSingleCharacter() {
        assertEquals("Single 'a' should produce code", "A111111111", caverphone.encode("a"));
        assertEquals("Single 'z' should produce code", "S111111111", caverphone.encode("z"));
        assertEquals("Single 'e' should produce code", "A111111111", caverphone.encode("e"));
        assertEquals("Single 'c' should produce code", "K111111111", caverphone.encode("c"));
    }

    @Test
    public void testEncodeTypicalNames() {
        assertEquals("David", "TFT1111111", caverphone.encode("David"));
        assertEquals("david", "TFT1111111", caverphone.encode("david"));
        assertEquals("Caverphone", "KFRFN11111", caverphone.encode("Caverphone"));
        assertEquals("Smith", "SNT1111111", caverphone.encode("Smith"));
        assertEquals("Wright", "RFT1111111", caverphone.encode("Wright"));
    }

    @Test
    public void testEncodeWithNumbers() {
        assertEquals("Input with numbers should treat them as letters? or ignore?",
                     "A111111111", caverphone.encode("a1b2c3"));
    }

    @Test
    public void testEncodeWithSpecialCharacters() {
        assertEquals("Special chars should be removed or produce default?",
                     "A111111111", caverphone.encode("a.b!c@d#"));
    }

    @Test
    public void testEncodeMixedCase() {
        assertEquals("Mixed case should be lowercase normalized",
                     "TFT1111111", caverphone.encode("DAviD"));
    }

    @Test
    public void testEncodeLongString() {
        String longString = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        String result = caverphone.encode(longString);
        assertEquals("Long input should still produce 10-character code", 10, result.length());
    }

    @Test
    public void testCaverphoneMethod() {
        // The old caverphone method should behave the same as encode
        assertEquals("caverphone and encode should match for null",
                     caverphone.encode(null), caverphone.caverphone(null));
        assertEquals("caverphone and encode should match for empty",
                     caverphone.encode(""), caverphone.caverphone(""));
        assertEquals("caverphone and encode should match for typical",
                     caverphone.encode("David"), caverphone.caverphone("David"));
    }

    @Test
    public void testEncodeWithLowerCaseOnly() {
        assertEquals("bob", "P111111111", caverphone.encode("bob"));
        assertEquals("ann", "AN11111111", caverphone.encode("ann"));
        assertEquals("test", "TST1111111", caverphone.encode("test"));
    }

    @Test(expected = NullPointerException.class)
    public void testCaverphoneNull() {
        // The caverphone method may throw NullPointerException for null input
        caverphone.caverphone(null);
    }

    @Test
    public void testEncodeWithLeadingTrailingSpaces() {
        assertEquals("Spaces should be trimmed",
                     "TFT1111111", caverphone.encode("   David   "));
    }
}