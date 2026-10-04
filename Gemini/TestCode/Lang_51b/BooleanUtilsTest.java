package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for org.apache.commons.lang.BooleanUtils.
 * Specifically targets Bug ID 51 (toBoolean(String) StringIndexOutOfBoundsException).
 */
public class BooleanUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new BooleanUtils());
    }

    // --- Tests for toBoolean(String) triggering Bug 51 ---

    @Test
    public void testToBoolean_String_Null() {
        assertFalse(BooleanUtils.toBoolean((String) null));
    }

    @Test
    public void testToBoolean_String_True() {
        assertTrue(BooleanUtils.toBoolean("true"));
        assertTrue(BooleanUtils.toBoolean("TRUE"));
        assertTrue(BooleanUtils.toBoolean("TrUe"));
    }

    @Test
    public void testToBoolean_String_Yes() {
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertTrue(BooleanUtils.toBoolean("YES"));
        assertTrue(BooleanUtils.toBoolean("Yes"));
    }

    @Test
    public void testToBoolean_String_On() {
        assertTrue(BooleanUtils.toBoolean("on"));
        assertTrue(BooleanUtils.toBoolean("ON"));
        assertTrue(BooleanUtils.toBoolean("On"));
    }

    @Test
    public void testToBoolean_String_ShortStrings() {
        // Triggering potential length checks (< 2, < 3, < 4) that caused StringIndexOutOfBoundsException in Bug 51
        assertFalse(BooleanUtils.toBoolean(""));
        assertFalse(BooleanUtils.toBoolean("f"));
        assertFalse(BooleanUtils.toBoolean("o"));
        assertFalse(BooleanUtils.toBoolean("y"));
        
        assertFalse(BooleanUtils.toBoolean("no"));
        assertFalse(BooleanUtils.toBoolean("off"));
        assertFalse(BooleanUtils.toBoolean("false"));
    }

    @Test
    public void testToBoolean_String_ExactLengthMatchesAndMismatches() {
        // Length 2
        assertTrue(BooleanUtils.toBoolean("on"));
        assertFalse(BooleanUtils.toBoolean("ox"));

        // Length 3
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertFalse(BooleanUtils.toBoolean("foo"));

        // Length 4
        assertTrue(BooleanUtils.toBoolean("true"));
        assertFalse(BooleanUtils.toBoolean("test"));
    }

    @Test
    public void testToBoolean_String_VariousLengths() {
        assertFalse(BooleanUtils.toBoolean("t"));
        assertFalse(BooleanUtils.toBoolean("tr"));
        assertFalse(BooleanUtils.toBoolean("tru"));
        assertTrue(BooleanUtils.toBoolean("true"));
        assertFalse(BooleanUtils.toBoolean("true!"));

        assertFalse(BooleanUtils.toBoolean("y"));
        assertFalse(BooleanUtils.toBoolean("ye"));
        assertTrue(BooleanUtils.toBoolean("yes"));
        assertFalse(BooleanUtils.toBoolean("yes!"));

        assertFalse(BooleanUtils.toBoolean("o"));
        assertTrue(BooleanUtils.toBoolean("on"));
        assertFalse(BooleanUtils.toBoolean("one"));
    }

    // --- Tests for toBooleanObject(String) ---

    @Test
    public void testToBooleanObject_String() {
        assertNull(BooleanUtils.toBooleanObject((String) null));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("true"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("yes"));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("on"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("false"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("no"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("off"));
        assertNull(BooleanUtils.toBooleanObject("random"));
        assertNull(BooleanUtils.toBooleanObject(""));
        assertNull(BooleanUtils.toBooleanObject("t"));
        assertNull(BooleanUtils.toBooleanObject("ye"));
    }

    // --- Tests for toBoolean(boolean, boolean, boolean) ---

    @Test
    public void testToBoolean_prim_prim_prim() {
        assertTrue(BooleanUtils.toBoolean(true, true, false));
        assertFalse(BooleanUtils.toBoolean(false, true, false));
        assertTrue(BooleanUtils.toBoolean(false, false, true));
        assertFalse(BooleanUtils.toBoolean(true, false, true));
    }

    // --- Tests for toBooleanObject(Boolean, Boolean, Boolean, Boolean) ---

    @Test
    public void testToBooleanObject_Boolean_Obj_Obj_Obj() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.NULL));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.NULL));
        assertNull(BooleanUtils.toBooleanObject(null, Boolean.TRUE, Boolean.FALSE, null));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE));
    }

    // --- Tests for toBoolean(int) and variants ---

    @Test
    public void testToBoolean_int() {
        assertFalse(BooleanUtils.toBoolean(0));
        assertTrue(BooleanUtils.toBoolean(1));
        assertTrue(BooleanUtils.toBoolean(-1));
        assertTrue(BooleanUtils.toBoolean(99));
    }

    @Test
    public void testToBooleanObject_int() {
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1));
        assertNull(BooleanUtils.toBooleanObject(2));
        assertNull(BooleanUtils.toBooleanObject(-1));
    }

    @Test
    public void testToBooleanObject_Integer() {
        assertNull(BooleanUtils.toBooleanObject((Integer) null));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0)));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(1)));
        assertNull(BooleanUtils.toBooleanObject(Integer.valueOf(2)));
    }

    @Test
    public void testToBoolean_int_int_int() {
        assertTrue(BooleanUtils.toBoolean(1, 1, 0));
        assertFalse(BooleanUtils.toBoolean(0, 1, 0));
    }

    @Test
    public void testToBoolean_Integer_Integer_Integer() {
        assertTrue(BooleanUtils.toBoolean(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0)));
        assertFalse(BooleanUtils.toBoolean(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0)));
        try {
            BooleanUtils.toBoolean(null, Integer.valueOf(1), Integer.valueOf(0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToBooleanObject_int_int_int() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(1, 1, 0, 9));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(0, 1, 0, 9));
        assertNull(BooleanUtils.toBooleanObject(5, 1, 0, 9));
    }

    @Test
    public void testToBooleanObject_Integer_Integer_Integer_Integer() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(9)));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(9)));
        assertNull(BooleanUtils.toBooleanObject(Integer.valueOf(5), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(9)));
        try {
            BooleanUtils.toBooleanObject(null, Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(9));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- Tests for toInteger ---

    @Test
    public void testToInteger_boolean() {
        assertEquals(1, BooleanUtils.toInteger(true));
        assertEquals(0, BooleanUtils.toInteger(false));
    }

    @Test
    public void testToInteger_boolean_int_int() {
        assertEquals(5, BooleanUtils.toInteger(true, 5, 3));
        assertEquals(3, BooleanUtils.toInteger(false, 5, 3));
    }

    @Test
    public void testToInteger_Boolean_int_int_int() {
        assertEquals(5, BooleanUtils.toInteger(Boolean.TRUE, 5, 3, 1));
        assertEquals(3, BooleanUtils.toInteger(Boolean.FALSE, 5, 3, 1));
        assertEquals(1, BooleanUtils.toInteger(null, 5, 3, 1));
    }

    @Test
    public void testToInteger_Boolean() {
        assertEquals(1, BooleanUtils.toInteger(Boolean.TRUE));
        assertEquals(0, BooleanUtils.toInteger(Boolean.FALSE));
        try {
            BooleanUtils.toInteger(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- Tests for toIntegerObject ---

    @Test
    public void testToIntegerObject_boolean() {
        assertEquals(Integer.valueOf(1), BooleanUtils.toIntegerObject(true));
        assertEquals(Integer.valueOf(0), BooleanUtils.toIntegerObject(false));
    }

    @Test
    public void testToIntegerObject_Boolean() {
        assertNull(BooleanUtils.toIntegerObject(null));
        assertEquals(Integer.valueOf(1), BooleanUtils.toIntegerObject(Boolean.TRUE));
        assertEquals(Integer.valueOf(0), BooleanUtils.toIntegerObject(Boolean.FALSE));
    }

    @Test
    public void testToIntegerObject_boolean_Integer_Integer() {
        assertEquals(Integer.valueOf(5), BooleanUtils.toIntegerObject(true, Integer.valueOf(5), Integer.valueOf(3)));
        assertEquals(Integer.valueOf(3), BooleanUtils.toIntegerObject(false, Integer.valueOf(5), Integer.valueOf(3)));
    }

    @Test
    public void testToIntegerObject_Boolean_Integer_Integer_Integer() {
        assertEquals(Integer.valueOf(5), BooleanUtils.toIntegerObject(Boolean.TRUE, Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(1)));
        assertEquals(Integer.valueOf(3), BooleanUtils.toIntegerObject(Boolean.FALSE, Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(1)));
        assertEquals(Integer.valueOf(1), BooleanUtils.toIntegerObject(null, Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(1)));
    }

    // --- Tests for toBooleanObject(String, String, String, String) ---

    @Test
    public void testToBooleanObject_String_String_String_String() {
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject("a", "a", "b", "c"));
        assertEquals(Boolean.FALSE, BooleanUtils.toBooleanObject("b", "a", "b", "c"));
        assertNull(BooleanUtils.toBooleanObject("c", "a", "b", "c"));
        assertNull(BooleanUtils.toBooleanObject("d", "a", "b", "c"));
        assertNull(BooleanUtils.toBooleanObject(null, "a", "b", null));
        assertEquals(Boolean.TRUE, BooleanUtils.toBooleanObject(null, "a", "b", "true"));
    }

    // --- Tests for toBoolean(String, String, String) ---

    @Test
    public void testToBoolean_String_String_String() {
        assertTrue(BooleanUtils.toBoolean("a", "a", "b"));
        assertFalse(BooleanUtils.toBoolean("b", "a", "b"));
        try {
            BooleanUtils.toBoolean("c", "a", "b");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            BooleanUtils.toBoolean(null, "a", "b");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- Tests for toString ---

    @Test
    public void testToString_boolean_String_String() {
        assertEquals("true", BooleanUtils.toString(true, "true", "false"));
        assertEquals("false", BooleanUtils.toString(false, "true", "false"));
    }

    @Test
    public void testToString_Boolean_String_String_String() {
        assertEquals("true", BooleanUtils.toString(Boolean.TRUE, "true", "false", "null"));
        assertEquals("false", BooleanUtils.toString(Boolean.FALSE, "true", "false", "null"));
        assertEquals("null", BooleanUtils.toString(null, "true", "false", "null"));
    }

    @Test
    public void testToStringTrueFalse() {
        assertEquals("true", BooleanUtils.toStringTrueFalse(true));
        assertEquals("false", BooleanUtils.toStringTrueFalse(false));
        assertNull(BooleanUtils.toStringTrueFalse(null));
    }

    @Test
    public void testToStringOnOff() {
        assertEquals("on", BooleanUtils.toStringOnOff(true));
        assertEquals("off", BooleanUtils.toStringOnOff(false));
        assertNull(BooleanUtils.toStringOnOff(null));
    }

    @Test
    public void testToStringYesNo() {
        assertEquals("yes", BooleanUtils.toStringYesNo(true));
        assertEquals("no", BooleanUtils.toStringYesNo(false));
        assertNull(BooleanUtils.toStringYesNo(null));
    }

    // --- Tests for xor ---

    @Test
    public void testXor_primitive() {
        assertTrue(BooleanUtils.xor(new boolean[] { true }));
        assertTrue(BooleanUtils.xor(new boolean[] { true, false }));
        assertFalse(BooleanUtils.xor(new boolean[] { true, true }));
        assertFalse(BooleanUtils.xor(new boolean[] { false, false }));
        assertTrue(BooleanUtils.xor(new boolean[] { true, false, false }));
        assertFalse(BooleanUtils.xor(new boolean[] { true, true, false }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_primitive_empty() {
        BooleanUtils.xor(new boolean[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_primitive_null() {
        BooleanUtils.xor((boolean[]) null);
    }

    @Test
    public void testXor_object() {
        assertTrue(BooleanUtils.xor(new Boolean[] { Boolean.TRUE }));
        assertTrue(BooleanUtils.xor(new Boolean[] { Boolean.TRUE, Boolean.FALSE }));
        assertFalse(BooleanUtils.xor(new Boolean[] { Boolean.TRUE, Boolean.TRUE }));
        assertFalse(BooleanUtils.xor(new Boolean[] { Boolean.FALSE, Boolean.FALSE }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_object_empty() {
        BooleanUtils.xor(new Boolean[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_object_null() {
        BooleanUtils.xor((Boolean[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testXor_object_containsNull() {
        BooleanUtils.xor(new Boolean[] { Boolean.TRUE, null });
    }
}