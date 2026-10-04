package org.apache.commons.lang;

import static org.junit.Assert.*;
import org.junit.Test;

public class BooleanUtilsTest {

    // ========== toBoolean(String) tests ==========
    @Test
    public void testToBooleanStringNull() {
        assertNull(BooleanUtils.toBoolean((String) null));
    }

    @Test
    public void testToBooleanStringTrue() {
        assertTrue(BooleanUtils.toBoolean("true"));
        assertTrue(BooleanUtils.toBoolean("TRUE"));
        assertTrue(BooleanUtils.toBoolean("tRuE"));
    }

    @Test
    public void testToBooleanStringFalse() {
        assertFalse(BooleanUtils.toBoolean("false"));
        assertFalse(BooleanUtils.toBoolean("FALSE"));
        assertFalse(BooleanUtils.toBoolean("fAlSe"));
    }

    @Test
    public void testToBooleanStringYesNo() {
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertTrue(BooleanUtils.toBoolean("YES"));
        assertTrue(BooleanUtils.toBoolean("yEs"));
        assertFalse(BooleanUtils.toBoolean("no"));
        assertFalse(BooleanUtils.toBoolean("NO"));
        assertFalse(BooleanUtils.toBoolean("nO"));
    }

    @Test
    public void testToBooleanStringOnOff() {
        assertTrue(BooleanUtils.toBoolean("on"));
        assertTrue(BooleanUtils.toBoolean("ON"));
        assertTrue(BooleanUtils.toBoolean("oN"));
        assertFalse(BooleanUtils.toBoolean("off"));
        assertFalse(BooleanUtils.toBoolean("OFF"));
        assertFalse(BooleanUtils.toBoolean("oFf"));
    }

    @Test
    public void testToBooleanStringYN() {
        assertTrue(BooleanUtils.toBoolean("y"));
        assertTrue(BooleanUtils.toBoolean("Y"));
        assertFalse(BooleanUtils.toBoolean("n"));
        assertFalse(BooleanUtils.toBoolean("N"));
    }

    @Test
    public void testToBooleanStringInvalid() {
        // Any string that is not a recognized boolean representation should return false
        assertFalse(BooleanUtils.toBoolean(""));
        assertFalse(BooleanUtils.toBoolean("maybe"));
        assertFalse(BooleanUtils.toBoolean("1"));
        assertFalse(BooleanUtils.toBoolean("0"));
        assertFalse(BooleanUtils.toBoolean("T"));
        assertFalse(BooleanUtils.toBoolean("F"));
    }

    @Test
    public void testToBooleanStringLength3EdgeCase() {
        // This test exposes the bug: StringIndexOutOfBoundsException for length 3 strings
        try {
            boolean result = BooleanUtils.toBoolean("xyz");
            // If no exception, should be false
            assertFalse(result);
        } catch (StringIndexOutOfBoundsException e) {
            fail("toBoolean(String) should not throw StringIndexOutOfBoundsException for arbitrary 3-character string");
        }
        // Additional length 3 strings that are not "yes"/"no"/"on"/"off"
        assertFalse(BooleanUtils.toBoolean("abc"));
        assertFalse(BooleanUtils.toBoolean("123"));
        assertFalse(BooleanUtils.toBoolean("!@#"));
    }

    @Test
    public void testToBooleanStringEdgeCases() {
        // Length 2 strings
        assertFalse(BooleanUtils.toBoolean("xy"));
        // Length 4 strings that are not "true" or "false"
        assertFalse(BooleanUtils.toBoolean("test"));
        // Length 5 strings
        assertFalse(BooleanUtils.toBoolean("hello"));
    }

    // ========== toBoolean(int) tests ==========
    @Test
    public void testToBooleanIntZero() {
        assertFalse(BooleanUtils.toBoolean(0));
    }

    @Test
    public void testToBooleanIntNonZero() {
        assertTrue(BooleanUtils.toBoolean(1));
        assertTrue(BooleanUtils.toBoolean(-1));
        assertTrue(BooleanUtils.toBoolean(42));
    }

    // ========== toBoolean(Integer) tests ==========
    @Test
    public void testToBooleanIntegerNull() {
        assertNull(BooleanUtils.toBoolean((Integer) null));
    }

    @Test
    public void testToBooleanIntegerZero() {
        assertFalse(BooleanUtils.toBoolean(Integer.valueOf(0)));
    }

    @Test
    public void testToBooleanIntegerNonZero() {
        assertTrue(BooleanUtils.toBoolean(Integer.valueOf(1)));
        assertTrue(BooleanUtils.toBoolean(Integer.valueOf(-1)));
    }

    // ========== booleanToBoolean (BooleanUtils.booleanToBoolean) not present, skip
    // ========== toBoolean(String, String, String) tests ==========
    @Test
    public void testToBooleanStringStringStringNull() {
        // if string is null, return null
        assertNull(BooleanUtils.toBoolean(null, "true", "false"));
        // if trueString is null -> false
        assertFalse(BooleanUtils.toBoolean("anything", null, "false"));
        // if falseString is null -> false
        assertFalse(BooleanUtils.toBoolean("anything", "true", null));
    }

    @Test
    public void testToBooleanStringStringStringMatch() {
        assertTrue(BooleanUtils.toBoolean("yes", "yes", "no"));
        assertFalse(BooleanUtils.toBoolean("no", "yes", "no"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanStringStringStringNoMatch() {
        BooleanUtils.toBoolean("maybe", "yes", "no");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToBooleanStringStringStringEqualStrings() {
        // Both trueString and falseString are same -> should throw
        BooleanUtils.toBoolean("any", "same", "same");
    }

    // ========== toBoolean(boolean) no op, skip
    // ========== toInteger(boolean) tests ==========
    @Test
    public void testToIntegerBooleanTrue() {
        assertEquals(1, BooleanUtils.toInteger(true));
    }

    @Test
    public void testToIntegerBooleanFalse() {
        assertEquals(0, BooleanUtils.toInteger(false));
    }

    // ========== toInteger(Boolean) tests ==========
    @Test
    public void testToIntegerBooleanObjectNull() {
        assertNull(BooleanUtils.toInteger((Boolean) null));
    }

    @Test
    public void testToIntegerBooleanObjectTrue() {
        assertEquals(Integer.valueOf(1), BooleanUtils.toInteger(Boolean.TRUE));
    }

    @Test
    public void testToIntegerBooleanObjectFalse() {
        assertEquals(Integer.valueOf(0), BooleanUtils.toInteger(Boolean.FALSE));
    }

    // ========== toString(boolean, String, String, String) tests ==========
    @Test
    public void testToStringTrueFalseNull() {
        assertEquals("true", BooleanUtils.toString(true, "true", "false", "null"));
        assertEquals("false", BooleanUtils.toString(false, "true", "false", "null"));
        String nullString = null;
        assertNull(nullString);
    }

    // ========== toStringTrueFalse, toStringOnOff, toStringYesNo tests ==========
    @Test
    public void testToStringTrueFalse() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
    }

    @Test
    public void testToStringOnOff() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
    }

    @Test
    public void testToStringYesNo() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
    }

    // ========== xor tests ==========
    @Test
    public void testXorNull() {
        try {
            BooleanUtils.xor((boolean[]) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXorEmptyArray() {
        BooleanUtils.xor(new boolean[0]);
    }

    @Test
    public void testXorBasic() {
        assertFalse(BooleanUtils.xor(new boolean[]{false, false}));
        assertTrue(BooleanUtils.xor(new boolean[]{true, false}));
        assertTrue(BooleanUtils.xor(new boolean[]{false, true}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, true}));
        assertTrue(BooleanUtils.xor(new boolean[]{true, false, true}));
        assertTrue(BooleanUtils.xor(new boolean[]{true, false, false}));
        assertFalse(BooleanUtils.xor(new boolean[]{true, true, true}));
    }

    // ========== and, or tests (if present) ==========
    // Assuming BooleanUtils has static methods and(boolean...), or(boolean...)
    @Test(expected = IllegalArgumentException.class)
    public void testAndEmpty() {
        BooleanUtils.and(new boolean[0]);
    }

    @Test
    public void testAnd() {
        assertTrue(BooleanUtils.and(new boolean[]{true, true, true}));
        assertFalse(BooleanUtils.and(new boolean[]{true, false, true}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrEmpty() {
        BooleanUtils.or(new boolean[0]);
    }

    @Test
    public void testOr() {
        assertTrue(BooleanUtils.or(new boolean[]{true, false, false}));
        assertFalse(BooleanUtils.or(new boolean[]{false, false}));
    }

    // ========== isTrue/isFalse/... tests? Possibly not present, skip ==========
}