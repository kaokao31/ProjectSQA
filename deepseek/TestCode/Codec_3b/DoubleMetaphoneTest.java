package org.apache.commons.codec.language;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for DoubleMetaphone.
 * Designed to achieve maximum coverage and detect faults.
 */
public class DoubleMetaphoneTest {

    private DoubleMetaphone doubleMetaphone;

    @Before
    public void setUp() {
        doubleMetaphone = new DoubleMetaphone();
    }

    // ========== Null and Empty Input ==========

    @Test(expected = NullPointerException.class)
    public void testNullInput() {
        doubleMetaphone.doubleMetaphone(null, false);
    }

    @Test
    public void testEmptyString() {
        assertNull("Empty string should return null", doubleMetaphone.doubleMetaphone("", false));
        assertNull("Empty string with alternate flag", doubleMetaphone.doubleMetaphone("", true));
    }

    // ========== Single Character Input ==========

    @Test
    public void testSingleCharacter() {
        assertEquals("A", "A", doubleMetaphone.doubleMetaphone("A", false));
        assertEquals("A", "A", doubleMetaphone.doubleMetaphone("A", true));
        assertEquals("B", "P", doubleMetaphone.doubleMetaphone("B", false));
        assertEquals("C", "K", doubleMetaphone.doubleMetaphone("C", false));
        assertEquals("D", "T", doubleMetaphone.doubleMetaphone("D", false));
        assertEquals("E", "A", doubleMetaphone.doubleMetaphone("E", false));
        assertEquals("F", "F", doubleMetaphone.doubleMetaphone("F", false));
        assertEquals("G", "K", doubleMetaphone.doubleMetaphone("G", false));
        assertEquals("H", "H", doubleMetaphone.doubleMetaphone("H", false));
        assertEquals("I", "A", doubleMetaphone.doubleMetaphone("I", false));
        assertEquals("J", "J", doubleMetaphone.doubleMetaphone("J", false));
        assertEquals("K", "K", doubleMetaphone.doubleMetaphone("K", false));
        assertEquals("L", "L", doubleMetaphone.doubleMetaphone("L", false));
        assertEquals("M", "M", doubleMetaphone.doubleMetaphone("M", false));
        assertEquals("N", "N", doubleMetaphone.doubleMetaphone("N", false));
        assertEquals("O", "A", doubleMetaphone.doubleMetaphone("O", false));
        assertEquals("P", "P", doubleMetaphone.doubleMetaphone("P", false));
        assertEquals("Q", "K", doubleMetaphone.doubleMetaphone("Q", false));
        assertEquals("R", "R", doubleMetaphone.doubleMetaphone("R", false));
        assertEquals("S", "S", doubleMetaphone.doubleMetaphone("S", false));
        assertEquals("T", "T", doubleMetaphone.doubleMetaphone("T", false));
        assertEquals("U", "A", doubleMetaphone.doubleMetaphone("U", false));
        assertEquals("V", "F", doubleMetaphone.doubleMetaphone("V", false));
        assertEquals("W", "W", doubleMetaphone.doubleMetaphone("W", false));
        assertEquals("X", "S", doubleMetaphone.doubleMetaphone("X", false));
        assertEquals("Y", "A", doubleMetaphone.doubleMetaphone("Y", false));
        assertEquals("Z", "S", doubleMetaphone.doubleMetaphone("Z", false));
    }

    // ========== Common Names and Words ==========

    @Test
    public void testCommonNames() {
        // Smith -> SM0 / XMT
        assertEquals("Smith primary", "SM0", doubleMetaphone.doubleMetaphone("Smith", false));
        assertEquals("Smith alternate", "XMT", doubleMetaphone.doubleMetaphone("Smith", true));

        // Jones -> JNS / JNS (no alternate)
        assertEquals("Jones primary", "JNS", doubleMetaphone.doubleMetaphone("Jones", false));
        assertEquals("Jones alternate", "JNS", doubleMetaphone.doubleMetaphone("Jones", true));

        // Miller -> MLR / MLR
        assertEquals("Miller primary", "MLR", doubleMetaphone.doubleMetaphone("Miller", false));
        assertEquals("Miller alternate", "MLR", doubleMetaphone.doubleMetaphone("Miller", true));

        // Johnson -> JNSN / JNSN
        assertEquals("Johnson primary", "JNSN", doubleMetaphone.doubleMetaphone("Johnson", false));
        assertEquals("Johnson alternate", "JNSN", doubleMetaphone.doubleMetaphone("Johnson", true));

        // Williams -> WLM / WLM (alternate? Actually WLM for both)
        assertEquals("Williams primary", "WLM", doubleMetaphone.doubleMetaphone("Williams", false));
        assertEquals("Williams alternate", "WLM", doubleMetaphone.doubleMetaphone("Williams", true));
    }

    @Test
    public void testSlavoGermanicInput() {
        // "Wicz" -> TS / FX (Slavic)
        assertEquals("Wicz primary", "TS", doubleMetaphone.doubleMetaphone("Wicz", false));
        assertEquals("Wicz alternate", "FX", doubleMetaphone.doubleMetaphone("Wicz", true));

        // "Schmidt" -> SMT / XMT
        assertEquals("Schmidt primary", "SMT", doubleMetaphone.doubleMetaphone("Schmidt", false));
        assertEquals("Schmidt alternate", "XMT", doubleMetaphone.doubleMetaphone("Schmidt", true));
    }

    // ========== Silent Letters and Special Prefixes ==========

    @Test
    public void testSilentPrefixes() {
        // "Kn" -> N
        assertEquals("Knight primary", "NT", doubleMetaphone.doubleMetaphone("Knight", false));
        assertEquals("Knight alternate", "NT", doubleMetaphone.doubleMetaphone("Knight", true));

        // "Pn" -> N
        assertEquals("Pneumonia primary", "NMN", doubleMetaphone.doubleMetaphone("Pneumonia", false));
        assertEquals("Pneumonia alternate", "NMN", doubleMetaphone.doubleMetaphone("Pneumonia", true));

        // "Wr" -> R
        assertEquals("Wright primary", "RT", doubleMetaphone.doubleMetaphone("Wright", false));
        assertEquals("Wright alternate", "RT", doubleMetaphone.doubleMetaphone("Wright", true));

        // "Gn" -> N
        assertEquals("Gnome primary", "NM", doubleMetaphone.doubleMetaphone("Gnome", false));
        assertEquals("Gnome alternate", "NM", doubleMetaphone.doubleMetaphone("Gnome", true));
    }

    @Test
    public void testSilentH() {
        // "H" is silent in some contexts
        assertEquals("Hannah primary", "HN", doubleMetaphone.doubleMetaphone("Hannah", false));
        assertEquals("Hannah alternate", "HN", doubleMetaphone.doubleMetaphone("Hannah", true));

        // "H" after vowel? e.g., "Oh" -> A? Actually "Oh" -> "A" (since O->A, H silent)
        assertEquals("Oh primary", "A", doubleMetaphone.doubleMetaphone("Oh", false));
        assertEquals("Oh alternate", "A", doubleMetaphone.doubleMetaphone("Oh", true));
    }

    // ========== Edge Cases with 'C' and 'G' ==========

    @Test
    public void testCAndGSoftHard() {
        // "C" before "E", "I", "Y" -> S (soft)
        assertEquals("Celia primary", "SL", doubleMetaphone.doubleMetaphone("Celia", false));
        assertEquals("Celia alternate", "SL", doubleMetaphone.doubleMetaphone("Celia", true));

        // "C" before "A", "O", "U" -> K (hard)
        assertEquals("Carter primary", "KRT", doubleMetaphone.doubleMetaphone("Carter", false));
        assertEquals("Carter alternate", "KRT", doubleMetaphone.doubleMetaphone("Carter", true));

        // "G" before "E", "I", "Y" -> J (soft) but not always
        assertEquals("George primary", "JRJ", doubleMetaphone.doubleMetaphone("George", false));
        assertEquals("George alternate", "JRJ", doubleMetaphone.doubleMetaphone("George", true));

        // "G" before "A", "O", "U" -> K (hard)
        assertEquals("Garden primary", "KRTN", doubleMetaphone.doubleMetaphone("Garden", false));
        assertEquals("Garden alternate", "KRTN", doubleMetaphone.doubleMetaphone("Garden", true));
    }

    // ========== Double Consonants and 'X' ==========

    @Test
    public void testDoubleConsonants() {
        // "Ll" -> L
        assertEquals("Lloyd primary", "LT", doubleMetaphone.doubleMetaphone("Lloyd", false));
        assertEquals("Lloyd alternate", "LT", doubleMetaphone.doubleMetaphone("Lloyd", true));

        // "Rr" -> R
        assertEquals("Carroll primary", "KRL", doubleMetaphone.doubleMetaphone("Carroll", false));
        assertEquals("Carroll alternate", "KRL", doubleMetaphone.doubleMetaphone("Carroll", true));
    }

    @Test
    public void testXHandling() {
        // "X" at start -> S
        assertEquals("Xavier primary", "SF", doubleMetaphone.doubleMetaphone("Xavier", false));
        assertEquals("Xavier alternate", "SF", doubleMetaphone.doubleMetaphone("Xavier", true));

        // "X" in middle -> KS
        assertEquals("Alex primary", "ALKS", doubleMetaphone.doubleMetaphone("Alex", false));
        assertEquals("Alex alternate", "ALKS", doubleMetaphone.doubleMetaphone("Alex", true));
    }

    // ========== Vowel Handling ==========

    @Test
    public void testVowelsAtStart() {
        // Vowels at start produce 'A' encoding
        assertEquals("Apple primary", "APL", doubleMetaphone.doubleMetaphone("Apple", false));
        assertEquals("Apple alternate", "APL", doubleMetaphone.doubleMetaphone("Apple", true));

        assertEquals("Orange primary", "ARNJ", doubleMetaphone.doubleMetaphone("Orange", false));
        assertEquals("Orange alternate", "ARNJ", doubleMetaphone.doubleMetaphone("Orange", true));
    }

    // ========== Alternate Encoding Flag ==========

    @Test
    public void testAlternateFlag() {
        // Some words have different alternate encodings
        // "Schmidt" primary SMT, alternate XMT
        assertEquals("Schmidt primary", "SMT", doubleMetaphone.doubleMetaphone("Schmidt", false));
        assertEquals("Schmidt alternate", "XMT", doubleMetaphone.doubleMetaphone("Schmidt", true));

        // "Wicz" primary TS, alternate FX
        assertEquals("Wicz primary", "TS", doubleMetaphone.doubleMetaphone("Wicz", false));
        assertEquals("Wicz alternate", "FX", doubleMetaphone.doubleMetaphone("Wicz", true));
    }

    // ========== Long Strings and Performance ==========

    @Test
    public void testLongString() {
        String longInput = "Supercalifragilisticexpialidocious";
        String primary = doubleMetaphone.doubleMetaphone(longInput, false);
        String alternate = doubleMetaphone.doubleMetaphone(longInput, true);
        assertNotNull("Primary should not be null", primary);
        assertNotNull("Alternate should not be null", alternate);
        // Just ensure it runs without exception and returns something
        assertTrue("Primary length should be > 0", primary.length() > 0);
        assertTrue("Alternate length should be > 0", alternate.length() > 0);
    }

    // ========== Known Defects4J Bug Triggers ==========

    @Test
    public void testBuggyPathSilentLetters() {
        // Known bug: "gh" silent in some contexts
        // "Hugh" -> H? Actually "Hugh" -> "H" (since H is not silent at start, but 'gh' silent)
        assertEquals("Hugh primary", "H", doubleMetaphone.doubleMetaphone("Hugh", false));
        assertEquals("Hugh alternate", "H", doubleMetaphone.doubleMetaphone("Hugh", true));

        // "Borough" -> BRK? Actually "Borough" -> "BRK" (silent 'gh')
        assertEquals("Borough primary", "BRK", doubleMetaphone.doubleMetaphone("Borough", false));
        assertEquals("Borough alternate", "BRK", doubleMetaphone.doubleMetaphone("Borough", true));
    }

    @Test
    public void testBuggyPathCWithK() {
        // "Ck" -> K (single K)
        assertEquals("Ck primary", "K", doubleMetaphone.doubleMetaphone("Ck", false));
        assertEquals("Ck alternate", "K", doubleMetaphone.doubleMetaphone("Ck", true));

        // "Mc" -> MK (but sometimes alternate)
        assertEquals("McDonald primary", "MKNT", doubleMetaphone.doubleMetaphone("McDonald", false));
        assertEquals("McDonald alternate", "MKNT", doubleMetaphone.doubleMetaphone("McDonald", true));
    }

    @Test
    public void testBuggyPathGermanic() {
        // "Sch" -> X (alternate) or S (primary)
        assertEquals("Sch primary", "S", doubleMetaphone.doubleMetaphone("Sch", false));
        assertEquals("Sch alternate", "X", doubleMetaphone.doubleMetaphone("Sch", true));

        // "Tsch" -> TS (primary) and FX (alternate)
        assertEquals("Tsch primary", "TS", doubleMetaphone.doubleMetaphone("Tsch", false));
        assertEquals("Tsch alternate", "FX", doubleMetaphone.doubleMetaphone("Tsch", true));
    }

    // ========== Boundary Cases ==========

    @Test
    public void testSingleLetterWithAlternate() {
        // Single letters should have same primary and alternate
        assertEquals("A primary", "A", doubleMetaphone.doubleMetaphone("A", false));
        assertEquals("A alternate", "A", doubleMetaphone.doubleMetaphone("A", true));
    }

    @Test
    public void testNonAlphabeticCharacters() {
        // Non-alphabetic characters should be ignored or cause specific behavior
        // Typically they are skipped
        assertEquals("O'Brien primary", "APRN", doubleMetaphone.doubleMetaphone("O'Brien", false));
        assertEquals("O'Brien alternate", "APRN", doubleMetaphone.doubleMetaphone("O'Brien", true));

        // Hyphenated names
        assertEquals("Jean-Pierre primary", "JNPR", doubleMetaphone.doubleMetaphone("Jean-Pierre", false));
        assertEquals("Jean-Pierre alternate", "JNPR", doubleMetaphone.doubleMetaphone("Jean-Pierre", true));
    }

    @Test
    public void testUpperCaseLowerCase() {
        // Should be case-insensitive
        assertEquals("smith lowercase", "SM0", doubleMetaphone.doubleMetaphone("smith", false));
        assertEquals("SMITH uppercase", "SM0", doubleMetaphone.doubleMetaphone("SMITH", false));
        assertEquals("Smith mixed", "SM0", doubleMetaphone.doubleMetaphone("Smith", false));
    }

    // ========== Additional Edge Cases ==========

    @Test
    public void testEndingWithS() {
        // "S" at end often produces "S" or "Z"? Actually "S" -> "S"
        assertEquals("Jones primary", "JNS", doubleMetaphone.doubleMetaphone("Jones", false));
        assertEquals("Jones alternate", "JNS", doubleMetaphone.doubleMetaphone("Jones", true));
    }

    @Test
    public void testEndingWithZ() {
        // "Z" -> "S"
        assertEquals("Waltz primary", "WLT", doubleMetaphone.doubleMetaphone("Waltz", false));
        assertEquals("Waltz alternate", "WLT", doubleMetaphone.doubleMetaphone("Waltz", true));
    }

    @Test
    public void testDoubleMetaphoneWithAlternateTrue() {
        // Ensure alternate flag is correctly passed
        String primary = doubleMetaphone.doubleMetaphone("Smith", false);
        String alternate = doubleMetaphone.doubleMetaphone("Smith", true);
        assertNotEquals("Primary and alternate should differ for Smith", primary, alternate);
    }

    @Test
    public void testMaxCodeLength() {
        // The default max code length is 4, but we can test with longer strings
        // The method should truncate to maxCodeLength
        // We can set maxCodeLength via setMaxCodeLen? Not exposed? Usually it's a field.
        // We'll just test that the result is not longer than 4 by default.
        String result = doubleMetaphone.doubleMetaphone("Supercalifragilisticexpialidocious", false);
        assertTrue("Result length should be <= 4", result.length() <= 4);
    }

    // ========== Test for isDoubleMetaphoneEqual ==========

    @Test
    public void testIsDoubleMetaphoneEqual() {
        assertTrue("Smith and Smyth should be equal", doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Smyth"));
        assertFalse("Smith and Jones should not be equal", doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Jones"));
    }

    // ========== Test for encode method ==========

    @Test
    public void testEncode() {
        assertEquals("Encode Smith", "SM0", doubleMetaphone.encode("Smith"));
        assertEquals("Encode Jones", "JNS", doubleMetaphone.encode("Jones"));
    }

    // ========== Test for getMaxCodeLen / setMaxCodeLen ==========

    @Test
    public void testMaxCodeLenGetterSetter() {
        int defaultMax = doubleMetaphone.getMaxCodeLen();
        assertEquals("Default max code length should be 4", 4, defaultMax);
        doubleMetaphone.setMaxCodeLen(8);
        assertEquals("Max code length should be 8", 8, doubleMetaphone.getMaxCodeLen());
        // Reset for other tests
        doubleMetaphone.setMaxCodeLen(4);
    }
}