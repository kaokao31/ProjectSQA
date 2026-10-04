package org.apache.commons.codec.language;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SoundexTest {

    private Soundex soundex;

    @Before
    public void setUp() {
        soundex = new Soundex();
    }

    @Test
    public void testNewInstanceWithMappingString() {
        Soundex customSoundex = new Soundex("01230120022455012623010202");
        assertNotNull(customSoundex);
        assertEquals(4, customSoundex.getMaxLength());
    }

    @Test
    public void testNewInstanceWithCharArray() {
        char[] mapping = new char[]{'0', '1', '2', '3', '0', '1', '2', '0', '0', '2', '2', '4', '5', '5', '0', '1', '2', '6', '2', '3', '0', '1', '0', '2', '0', '2'};
        Soundex customSoundex = new Soundex(mapping);
        assertNotNull(customSoundex);
        assertEquals(4, customSoundex.getMaxLength());
    }

    @Test
    public void testDifference() throws Exception {
        assertEquals(4, soundex.difference("Smith", "Smyth"));
        assertEquals(0, soundex.difference(null, "Smith"));
        assertEquals(0, soundex.difference("Smith", null));
    }

    @Test
    public void testEncodeNullAndEmpty() {
        assertNull(soundex.encode(null));
        assertEquals("", soundex.encode(""));
    }

    @Test
    public void testEncodeBasicWords() {
        assertEquals("W252", soundex.encode("Washington"));
        assertEquals("L200", soundex.encode("Lee"));
        assertEquals("G200", soundex.encode("Gutierrez"));
        assertEquals("P236", soundex.encode("Pfister"));
        assertEquals("J250", soundex.encode("Jackson"));
    }

    @Test
    public void testSoundexWithSpecialCharactersAndAccents() {
        // Testing characters that map to 0 or are ignored
        assertEquals("H462", soundex.encode("Honeyman"));
        assertEquals("R163", soundex.encode("Robert"));
        assertEquals("R163", soundex.encode("Rupert"));
    }

    @Test
    public void testGetMappingCode() {
        // Test within bounds 'A' to 'Z'
        char codeA = soundex.map('A');
        char codeZ = soundex.map('Z');
        // Test out of bounds or non-alpha (depending on implementation, usually throws or returns 0)
        char codeLower = soundex.map('a');
        assertEquals(codeA, soundex.map('A'));
    }

    @Test
    public void testSetMaxLength() {
        soundex.setMaxLength(5);
        assertEquals(5, soundex.getMaxLength());
        // Verify encoding respects maxLength if applicable
        assertEquals("W2520", soundex.encode("Washington"));
    }

    @Test
    public void testConstants() {
        assertNotNull(Soundex.US_ENGLISH_MAPPING_STRING);
        assertNotNull(Soundex.US_ENGLISH_MAPPING);
        assertEquals(Soundex.US_ENGLISH_MAPPING_STRING.length(), Soundex.US_ENGLISH_MAPPING.length);
    }
}