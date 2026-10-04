package org.apache.commons.codec.language;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DoubleMetaphoneTest {

    private DoubleMetaphone metaphone;

    @Before
    public void setUp() {
        metaphone = new DoubleMetaphone();
    }

    @Test
    public void testConstructorAndGettersSetters() {
        assertNotNull(metaphone);
        assertEquals(4, metaphone.getMaxCodeLen());
        metaphone.setMaxCodeLen(6);
        assertEquals(6, metaphone.getMaxCodeLen());
        
        assertEquals("A", metaphone.doubleMetaphone(null));
        assertEquals("", metaphone.doubleMetaphone(""));
        assertEquals("", metaphone.doubleMetaphone("   "));
    }

    @Test
    public void testIsDoubleMetaphoneEqual() {
        assertTrue(DoubleMetaphone.isDoubleMetaphoneEqual(null, null));
        assertFalse(DoubleMetaphone.isDoubleMetaphoneEqual("A", null));
        assertFalse(DoubleMetaphone.isDoubleMetaphoneEqual(null, "A"));
        assertTrue(DoubleMetaphone.isDoubleMetaphoneEqual("Smith", "Smyth"));
        assertFalse(DoubleMetaphone.isDoubleMetaphoneEqual("Smith", "Jones"));
        
        assertTrue(metaphone.isEqual("Smith", "Smyth"));
        assertFalse(metaphone.isEqual("Smith", "Jones"));
    }

    @Test
    public void testVowelsAtStart() {
        assertEquals("A", metaphone.doubleMetaphone("Area"));
        assertEquals("E", metaphone.doubleMetaphone("Earth"));
        assertEquals("I", metaphone.doubleMetaphone("Ice"));
        assertEquals("O", metaphone.doubleMetaphone("Open"));
        assertEquals("U", metaphone.doubleMetaphone("User"));
        assertEquals("Y", metaphone.doubleMetaphone("Yankee"));
    }

    @Test
    public void testInitialLettersAndCombinations() {
        // 'GN', 'KN', 'PN', 'WR', 'WH'
        assertEquals("N", metaphone.doubleMetaphone("Gnat"));
        assertEquals("N", metaphone.doubleMetaphone("Knight"));
        assertEquals("N", metaphone.doubleMetaphone("Pneumatic"));
        assertEquals("R", metaphone.doubleMetaphone("Write"));
        assertEquals("AK", metaphone.doubleMetaphone("White")); // 'WH' at start
        assertEquals("WT", metaphone.doubleMetaphone("Who"));
    }

    @Test
    public void testXAtStart() {
        assertEquals("S", metaphone.doubleMetaphone("Xavier"));
    }

    @Test
    public void testLetterC() {
        // 'CH'
        assertEquals("X", metaphone.doubleMetaphone("Child"));
        assertEquals("X", metaphone.doubleMetaphone("Cheese"));
        assertEquals("K", metaphone.doubleMetaphone("Chorus"));
        // 'CIA'
        assertEquals("X", metaphone.doubleMetaphone("Facial"));
        // 'CC'
        assertEquals("KS", metaphone.doubleMetaphone("Soccer"));
        // 'CK', 'CG', 'CQ'
        assertEquals("K", metaphone.doubleMetaphone("Back"));
        // 'C', 'CI', 'CE', 'CY'
        assertEquals("S", metaphone.doubleMetaphone("Center"));
        assertEquals("S", metaphone.doubleMetaphone("City"));
        assertEquals("S", metaphone.doubleMetaphone("Cyan"));
        assertEquals("K", metaphone.doubleMetaphone("Cat"));
    }

    @Test
    public void testLetterD() {
        // 'DG'
        assertEquals("J", metaphone.doubleMetaphone("Badge"));
        // 'DT', 'DD'
        assertEquals("T", metaphone.doubleMetaphone("Credit"));
        assertEquals("T", metaphone.doubleMetaphone("Add"));
    }

    @Test
    public void testLetterG() {
        // 'GH'
        assertEquals("K", metaphone.doubleMetaphone("Laugh"));
        // 'GN'
        assertEquals("N", metaphone.doubleMetaphone("Sign"));
        // 'G' hard/soft
        assertEquals("J", metaphone.doubleMetaphone("General"));
        assertEquals("K", metaphone.doubleMetaphone("Good"));
    }

    @Test
    public void testLetterH() {
        assertEquals("H", metaphone.doubleMetaphone("House"));
        // silent h after vowels handled in code
        assertEquals("K", metaphone.doubleMetaphone("Ahd")); 
    }

    @Test
    public void testLetterJ() {
        assertEquals("J", metaphone.doubleMetaphone("Juice"));
        assertEquals("K", metaphone.doubleMetaphone("Jose"));
    }

    @Test
    public void testLetterP() {
        // 'PH'
        assertEquals("F", metaphone.doubleMetaphone("Phone"));
    }

    @Test
    public void testLetterQ() {
        assertEquals("K", metaphone.doubleMetaphone("Queen"));
    }

    @Test
    public void testLetterS() {
        // 'SH'
        assertEquals("X", metaphone.doubleMetaphone("Ship"));
        // 'SIO', 'SIA'
        assertEquals("SK", metaphone.doubleMetaphone("Action")); // wait, 'TION' or 'SION'
        assertEquals("X", metaphone.doubleMetaphone("Mansion"));
    }

    @Test
    public void testLetterT() {
        // 'TIA', 'TIO'
        assertEquals("X", metaphone.doubleMetaphone("Nation"));
        // 'TH'
        assertEquals("0", metaphone.doubleMetaphone("Thin")); // '0' represents theta/dental
        assertEquals("T", metaphone.doubleMetaphone("Thomas"));
    }

    @Test
    public void testLetterV() {
        assertEquals("F", metaphone.doubleMetaphone("Vine"));
    }

    @Test
    public void testLetterW() {
        assertEquals("F", metaphone.doubleMetaphone("OffensiveW")); // testing w rules
        assertEquals("", metaphone.doubleMetaphone("Law"));
    }

    @Test
    public void testLetterX() {
        assertEquals("KS", metaphone.doubleMetaphone("Box"));
    }

    @Test
    public void testLetterZ() {
        assertEquals("S", metaphone.doubleMetaphone("Zero"));
    }

    @Test
    public void testSlavoGermanicAndAlternativeHandling() {
        // Test names containing 'W', 'CZ', 'WITZ' to hit Slavo-Germanic branches
        assertEquals("FS", metaphone.doubleMetaphone("witz"));
        assertEquals("SK", metaphone.doubleMetaphone("czar"));
    }

    @Test
    public void testEdgeCasesAndSlugs() {
        // Short strings, special characters, various lengths
        assertEquals("P", metaphone.doubleMetaphone("P"));
        assertEquals("PRT", metaphone.doubleMetaphone("Part"));
        assertEquals("SK", metaphone.doubleMetaphone("Schwartz"));
        assertEquals("SK", metaphone.doubleMetaphone("Scuba"));
    }
}