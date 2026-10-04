package org.apache.commons.codec.language;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class SoundexTest {
    private Soundex soundex;

    @Before
    public void setUp() {
        soundex = new Soundex();
    }

    @Test
    public void testNullInput() {
        assertNull(soundex.soundex(null));
    }

    @Test
    public void testEmptyInput() {
        assertEquals("", soundex.soundex(""));
    }

    @Test
    public void testSingleCharacter() {
        assertEquals("A000", soundex.soundex("A"));
    }

    @Test
    public void testAllVowels() {
        assertEquals("A000", soundex.soundex("AEIOU"));
    }

    @Test
    public void testConsecutiveSameCode() {
        assertEquals("B000", soundex.soundex("Bb"));
    }

    @Test
    public void testVowelSeparator() {
        assertEquals("B100", soundex.soundex("Baab"));
    }

    @Test
    public void testHWSeparator() {
        // H and W should act as separators
        assertEquals("B100", soundex.soundex("Bhb"));
        assertEquals("B100", soundex.soundex("Bwb"));
    }

    @Test
    public void testTymczak() {
        assertEquals("T522", soundex.soundex("Tymczak"));
    }

    @Test
    public void testAshcraft() {
        // With H as separator, Ashcraft should be A226
        assertEquals("A226", soundex.soundex("Ashcraft"));
    }

    @Test
    public void testLongString() {
        // Test a longer string to ensure no buffer overflow
        assertEquals("H464", soundex.soundex("HelloWorld"));
    }

    @Test
    public void testNonAlphabetic() {
        // Non-alphabetic characters should be ignored
        assertEquals("H400", soundex.soundex("Hello123"));
    }

    @Test
    public void testLowerCase() {
        assertEquals("H400", soundex.soundex("hello"));
    }

    @Test
    public void testAllConsonantsSameCode() {
        // B, F, P, V all have code 1, so they should be merged
        assertEquals("B000", soundex.soundex("BFPV"));
    }

    @Test
    public void testMixedCase() {
        assertEquals("H400", soundex.soundex("Hello"));
    }

    @Test
    public void testDifference() {
        // Assuming difference method exists
        assertEquals(4, soundex.difference("Robert", "Rupert"));
        assertEquals(3, soundex.difference("Robert", "Rubin"));
    }

    @Test
    public void testEncode() {
        // Assuming encode method is an alias for soundex
        assertEquals("H400", soundex.encode("Hello"));
    }
}