package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharSequenceUtilsTest {

    @Test
    public void testConstructor() {
        // Test instantiation of the utility class for code coverage
        CharSequenceUtils utils = new CharSequenceUtils();
        assertNotNull(utils);
    }

    @Test
    public void testRegionMatchesString() {
        CharSequence cs = "Hello World";
        
        // Valid match, ignoreCase = false
        assertTrue(CharSequenceUtils.regionMatches(cs, false, 0, "Hello", 0, 5));
        
        // Valid match, ignoreCase = true
        assertTrue(CharSequenceUtils.regionMatches(cs, true, 0, "hElLo", 0, 5));
        
        // Mismatch due to case sensitivity
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 0, "hElLo", 0, 5));
        
        // Mismatch in content
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 0, "World", 0, 5));
    }

    @Test
    public void testRegionMatchesStringBuilder() {
        CharSequence cs = new StringBuilder("Apache Commons");
        
        // Valid match
        assertTrue(CharSequenceUtils.regionMatches(cs, false, 7, "Commons", 0, 7));
        
        // Valid match with ignoreCase
        assertTrue(CharSequenceUtils.regionMatches(cs, true, 7, "cOmMoNs", 0, 7));
        
        // Mismatch
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 7, "Apache", 0, 6));
    }

    @Test
    public void testRegionMatchesStringBuffer() {
        CharSequence cs = new StringBuffer("Java Programming");
        
        // Valid match
        assertTrue(CharSequenceUtils.regionMatches(cs, false, 5, "Programming", 0, 11));
        
        // Valid match with ignoreCase
        assertTrue(CharSequenceUtils.regionMatches(cs, true, 5, "pRoGrAmMiNg", 0, 11));
        
        // Mismatch
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 0, "Programming", 0, 11));
    }

    @Test
    public void testRegionMatchesEdgeCasesAndInvalidInputs() {
        CharSequence cs = "Test";

        // null checks and out of bounds according to typical CharSequenceUtils implementation
        // If cs or substring is null, it typically throws NullPointerException or returns false depending on implementation.
        try {
            CharSequenceUtils.regionMatches(null, false, 0, "Test", 0, 4);
            fail("Expected NullPointerException or similar for null charSequence");
        } catch (NullPointerException | StringIndexOutOfBoundsException e) {
            // Expected
        }

        // Negative offsets or length exceeding bounds
        assertFalse(CharSequenceUtils.regionMatches(cs, false, -1, "Test", 0, 4));
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 0, "Test", -1, 4));
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 0, "Test", 0, 10));
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 10, "Test", 0, 1));
        
        // toOffset + length exceeds this CharSequence length
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 2, "Test", 0, 3));
        
        // substring toOffset + length exceeds substring length
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 0, "Te", 0, 3));
    }
}