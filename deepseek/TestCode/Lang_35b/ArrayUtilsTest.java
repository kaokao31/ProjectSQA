package org.apache.commons.lang3;

import static org.junit.Assert.*;

import org.junit.Test;
import java.lang.reflect.Array;
import java.util.Arrays;

public class ArrayUtilsTest {

    // -----------------------------------------------------------------------
    // Tests for LANG571 bug (add method should preserve array type)
    // -----------------------------------------------------------------------

    @Test
    public void testAddToStringArrayReturnsStringArray() {
        String[] original = new String[]{"a", "b"};
        // Direct assignment - will throw ClassCastException if bug present
        String[] result = ArrayUtils.add(original, "c");
        assertEquals(3, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
        assertEquals("c", result[2]);
    }

    @Test
    public void testAddToStringArrayWithNullElementReturnsStringArray() {
        String[] original = new String[]{"a"};
        String[] result = ArrayUtils.add(original, (String) null);
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertNull(result[1]);
        assertTrue("Result must be String[]", result instanceof String[]);
    }

    @Test
    public void testAddToIntegerArrayReturnsIntegerArray() {
        Integer[] original = new Integer[]{1, 2};
        Integer[] result = ArrayUtils.add(original, 3);
        assertEquals(3, result.length);
        assertEquals(1, result[0].intValue());
        assertEquals(2, result[1].intValue());
        assertEquals(3, result[2].intValue());
        assertTrue("Result must be Integer[]", result instanceof Integer[]);
    }

    @Test
    public void testAddToIntArrayReturnsIntArray() {
        int[] original = new int[]{1, 2};
        int[] result = ArrayUtils.add(original, 3);
        assertEquals(3, result.length);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test
    public void testAddToLongArray() {
        long[] original = new long[]{1L, 2L};
        long[] result = ArrayUtils.add(original, 3L);
        assertArrayEquals(new long[]{1L, 2L, 3L}, result);
    }

    @Test
    public void testAddToBooleanArray() {
        boolean[] original = new boolean[]{true};
        boolean[] result = ArrayUtils.add(original, false);
        assertArrayEquals(new boolean[]{true, false}, result);
    }

    @Test
    public void testAddToByteArray() {
        byte[] original = new byte[]{1, 2};
        byte[] result = ArrayUtils.add(original, (byte) 3);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    public void testAddToShortArray() {
        short[] original = new short[]{1, 2};
        short[] result = ArrayUtils.add(original, (short) 3);
        assertArrayEquals(new short[]{1, 2, 3}, result);
    }

    @Test
    public void testAddToCharArray() {
        char[] original = new char[]{'a', 'b'};
        char[] result = ArrayUtils.add(original, 'c');
        assertArrayEquals(new char[]{'a', 'b', 'c'}, result);
    }

    @Test
    public void testAddToFloatArray() {
        float[] original = new float[]{1.0f, 2.0f};
        float[] result = ArrayUtils.add(original, 3.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, result, 0.0001f);
    }

    @Test
    public void testAddToDoubleArray() {
        double[] original = new double[]{1.0, 2.0};
        double[] result = ArrayUtils.add(original, 3.0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, result, 0.0001);
    }

    // -----------------------------------------------------------------------
    // Tests for add with null input
    // -----------------------------------------------------------------------

    @Test
    public void testAddNullArray() {
        String[] result = ArrayUtils.add((String[]) null, "a");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("a", result[0]);
        assertTrue("Result from null must be String[]", result instanceof String[]);
    }

    @Test
    public void testAddNullArrayWithNullElement() {
        String[] result = ArrayUtils.add((String[]) null, (String) null);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullArrayToPrimitive() {
        ArrayUtils.add((int[]) null, 1);
    }

    // -----------------------------------------------------------------------
    // Tests for addAll (both generic and primitive)
    // -----------------------------------------------------------------------

    @Test
    public void testAddAllToStringArrays() {
        String[] array1 = new String[]{"a", "b"};
        String[] array2 = new String[]{"c", "d"};
        String[] result = ArrayUtils.addAll(array1, array2);
        assertTrue("Result must be String[]", result instanceof String[]);
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, result);
    }

    @Test
    public void testAddAllWithNullFirstArray() {
        String[] result = ArrayUtils.addAll((String[]) null, new String[]{"a"});
        assertTrue("Result must be String[]", result instanceof String[]);
        assertArrayEquals(new String[]{"a"}, result);
    }

    @Test
    public void testAddAllWithNullSecondArray() {
        String[] result = ArrayUtils.addAll(new String[]{"a"}, (String[]) null);
        assertTrue("Result must be String[]", result instanceof String[]);
        assertArrayEquals(new String[]{"a"}, result);
    }

    @Test
    public void testAddAllIntegerArrays() {
        Integer[] array1 = new Integer[]{1, 2};
        Integer[] array2 = new Integer[]{3, 4};
        Integer[] result = ArrayUtils.addAll(array1, array2);
        assertTrue("Result must be Integer[]", result instanceof Integer[]);
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, result);
    }

    @Test
    public void testAddAllPrimitiveInt() {
        int[] result = ArrayUtils.addAll(new int[]{1, 2}, new int[]{3, 4});
        assertArrayEquals(new int[]{1, 2, 3, 4}, result);
    }

    // -----------------------------------------------------------------------
    // Tests for clone
    // -----------------------------------------------------------------------

    @Test
    public void testCloneStringArray() {
        String[] original = new String[]{"a", "b"};
        String[] cloned = ArrayUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
        assertTrue("Cloned array must be String[]", cloned instanceof String[]);
        // Modify clone to ensure independence
        cloned[0] = "c";
        assertEquals("a", original[0]);
    }

    @Test
    public void testCloneNull() {
        assertNull(ArrayUtils.clone((String[]) null));
    }

    @Test
    public void testClonePrimitiveInt() {
        int[] original = new int[]{1, 2, 3};
        int[] cloned = ArrayUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    // -----------------------------------------------------------------------
    // Tests for subarray
    // -----------------------------------------------------------------------

    @Test
    public void testSubarrayNormal() {
        int[] array = new int[]{1, 2, 3, 4, 5};
        int[] sub = ArrayUtils.subarray(array, 1, 4);
        assertArrayEquals(new int[]{2, 3, 4}, sub);
    }

    @Test
    public void testSubarrayStartNegative() {
        int[] array = new int[]{1, 2, 3};
        int[] sub = ArrayUtils.subarray(array, -1, 2);
        assertArrayEquals(new int[]{1, 2}, sub);
    }

    @Test
    public void testSubarrayEndGreaterThanLength() {
        int[] array = new int[]{1, 2, 3};
        int[] sub = ArrayUtils.subarray(array, 1, 5);
        assertArrayEquals(new int[]{2, 3}, sub);
    }

    @Test
    public void testSubarrayNull() {
        assertNull(ArrayUtils.subarray((int[]) null, 0, 1));
    }

    @Test
    public void testSubarrayEmptyResult() {
        int[] array = new int[]{1, 2, 3};
        int[] sub = ArrayUtils.subarray(array, 2, 1);
        assertNotNull(sub);
        assertEquals(0, sub.length);
    }

    // -----------------------------------------------------------------------
    // Tests for indexOf / lastIndexOf / contains
    // -----------------------------------------------------------------------

    @Test
    public void testIndexOfInt() {
        assertEquals(2, ArrayUtils.indexOf(new int[]{1, 2, 3, 2}, 2));
        assertEquals(-1, ArrayUtils.indexOf(new int[]{1, 2, 3}, 4));
    }

    @Test
    public void testIndexOfIntWithStartIndex() {
        assertEquals(3, ArrayUtils.indexOf(new int[]{1, 2, 3, 2}, 2, 2));
        assertEquals(-1, ArrayUtils.indexOf(new int[]{1, 2, 3, 2}, 2, 4));
    }

    @Test
    public void testLastIndexOfInt() {
        assertEquals(3, ArrayUtils.lastIndexOf(new int[]{1, 2, 3, 2}, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(new int[]{1, 2, 3}, 4));
    }

    @Test
    public void testContainsInt() {
        assertTrue(ArrayUtils.contains(new int[]{1, 2, 3}, 2));
        assertFalse(ArrayUtils.contains(new int[]{1, 2, 3}, 4));
    }

    @Test
    public void testIndexOfObject() {
        assertEquals(1, ArrayUtils.indexOf(new String[]{"a", "b", "c"}, "b"));
        assertEquals(-1, ArrayUtils.indexOf(new String[]{"a", "b", "c"}, "d"));
    }

    @Test
    public void testIndexOfObjectWithNull() {
        assertEquals(2, ArrayUtils.indexOf(new String[]{"a", null, "c"}, null));
        assertEquals(-1, ArrayUtils.indexOf(new String[]{"a", "b", "c"}, null));
    }

    // -----------------------------------------------------------------------
    // Tests for isEmpty / isNotEmpty
    // -----------------------------------------------------------------------

    @Test
    public void testIsEmptyNull() {
        assertTrue(ArrayUtils.isEmpty((int[]) null));
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
    }

    @Test
    public void testIsEmptyEmpty() {
        assertTrue(ArrayUtils.isEmpty(new int[0]));
        assertTrue(ArrayUtils.isEmpty(new String[0]));
    }

    @Test
    public void testIsEmptyNonEmpty() {
        assertFalse(ArrayUtils.isEmpty(new int[]{1}));
        assertFalse(ArrayUtils.isEmpty(new String[]{"a"}));
    }

    @Test
    public void testIsNotEmptyNull() {
        assertFalse(ArrayUtils.isNotEmpty((int[]) null));
        assertFalse(ArrayUtils.isNotEmpty((Object[]) null));
    }

    @Test
    public void testIsNotEmptyEmpty() {
        assertFalse(ArrayUtils.isNotEmpty(new int[0]));
        assertFalse(ArrayUtils.isNotEmpty(new String[0]));
    }

    @Test
    public void testIsNotEmptyNonEmpty() {
        assertTrue(ArrayUtils.isNotEmpty(new int[]{1}));
        assertTrue(ArrayUtils.isNotEmpty(new String[]{"a"}));
    }

    // -----------------------------------------------------------------------
    // Tests for toObject / toPrimitive
    // -----------------------------------------------------------------------

    @Test
    public void testToObjectInt() {
        Integer[] result = ArrayUtils.toObject(new int[]{1, 2, 3});
        assertArrayEquals(new Integer[]{1, 2, 3}, result);
    }

    @Test
    public void testToObjectNull() {
        assertNull(ArrayUtils.toObject((int[]) null));
    }

    @Test
    public void testToPrimitiveInteger() {
        int[] result = ArrayUtils.toPrimitive(new Integer[]{1, 2, 3});
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test(expected = NullPointerException.class)
    public void testToPrimitiveWithNullElement() {
        ArrayUtils.toPrimitive(new Integer[]{1, null, 3});
    }

    @Test
    public void testToPrimitiveIntegerNull() {
        assertNull(ArrayUtils.toPrimitive((Integer[]) null));
    }

    // -----------------------------------------------------------------------
    // Tests for nullToEmpty
    // -----------------------------------------------------------------------

    @Test
    public void testNullToEmptyStringArray() {
        String[] result = ArrayUtils.nullToEmpty((String[]) null);
        assertNotNull(result);
        assertEquals(0, result.length);
        assertTrue(result instanceof String[]);
    }

    @Test
    public void testNullToEmptyIntArray() {
        int[] result = ArrayUtils.nullToEmpty((int[]) null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testNullToEmptyNonNull() {
        String[] original = new String[]{"a"};
        assertSame(original, ArrayUtils.nullToEmpty(original));
    }

    // -----------------------------------------------------------------------
    // Tests for reverse
    // -----------------------------------------------------------------------

    @Test
    public void testReverseIntArray() {
        int[] array = new int[]{1, 2, 3, 4};
        ArrayUtils.reverse(array);
        assertArrayEquals(new int[]{4, 3, 2, 1}, array);
    }

    @Test
    public void testReverseObjectArray() {
        String[] array = new String[]{"a", "b", "c"};
        ArrayUtils.reverse(array);
        assertArrayEquals(new String[]{"c", "b", "a"}, array);
    }

    @Test
    public void testReverseNull() {
        // No exception expected
        ArrayUtils.reverse((int[]) null);
    }

    // -----------------------------------------------------------------------
    // Tests for shift
    // -----------------------------------------------------------------------

    @Test
    public void testShiftRight() {
        int[] array = new int[]{1, 2, 3, 4, 5};
        ArrayUtils.shift(array, 2);
        assertArrayEquals(new int[]{4, 5, 1, 2, 3}, array);
    }

    @Test
    public void testShiftLeft() {
        int[] array = new int[]{1, 2, 3, 4, 5};
        ArrayUtils.shift(array, -2);
        assertArrayEquals(new int[]{3, 4, 5, 1, 2}, array);
    }

    @Test
    public void testShiftFullCycle() {
        int[] array = new int[]{1, 2, 3};
        ArrayUtils.shift(array, 3);
        assertArrayEquals(new int[]{1, 2, 3}, array);
    }

    @Test
    public void testShiftNull() {
        ArrayUtils.shift((int[]) null, 1);
    }

    // -----------------------------------------------------------------------
    // Tests for swap
    // -----------------------------------------------------------------------

    @Test
    public void testSwapInt() {
        int[] array = new int[]{1, 2, 3};
        ArrayUtils.swap(array, 0, 2);
        assertArrayEquals(new int[]{3, 2, 1}, array);
    }

    @Test
    public void testSwapSameIndex() {
        int[] array = new int[]{1, 2, 3};
        ArrayUtils.swap(array, 1, 1);
        assertArrayEquals(new int[]{1, 2, 3}, array);
    }

    @Test
    public void testSwapNull() {
        ArrayUtils.swap((int[]) null, 0, 1);
    }

    @Test
    public void testSwapOutOfBoundsDoesNothing() {
        int[] array = new int[]{1, 2};
        ArrayUtils.swap(array, 0, 5);
        assertArrayEquals(new int[]{1, 2}, array);
    }

    // -----------------------------------------------------------------------
    // Tests for remove / removeElement / removeAll
    // -----------------------------------------------------------------------

    @Test
    public void testRemoveInt() {
        int[] array = new int[]{1, 2, 3, 4};
        int[] result = ArrayUtils.remove(array, 2);
        assertArrayEquals(new int[]{1, 2, 4}, result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveNegativeIndex() {
        ArrayUtils.remove(new int[]{1, 2}, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBounds() {
        ArrayUtils.remove(new int[]{1, 2}, 2);
    }

    @Test
    public void testRemoveElementInt() {
        int[] array = new int[]{1, 2, 3, 2, 4};
        int[] result = ArrayUtils.removeElement(array, 2);
        assertArrayEquals(new int[]{1, 3, 2, 4}, result);
    }

    @Test
    public void testRemoveElementNonexistent() {
        int[] array = new int[]{1, 2, 3};
        int[] result = ArrayUtils.removeElement(array, 4);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test
    public void testRemoveElementAll() {
        int[] array = new int[]{1, 2, 2, 3};
        int[] result = ArrayUtils.removeAll(array, 1, 2); // indices 1 and 2
        assertArrayEquals(new int[]{1, 3}, result);
    }

    // -----------------------------------------------------------------------
    // Tests for addFirst
    // -----------------------------------------------------------------------

    @Test
    public void testAddFirstToStringArray() {
        String[] array = new String[]{"b", "c"};
        String[] result = ArrayUtils.addFirst(array, "a");
        assertTrue("Result must be String[]", result instanceof String[]);
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    public void testAddFirstToIntArray() {
        int[] array = new int[]{2, 3};
        int[] result = ArrayUtils.addFirst(array, 1);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    // -----------------------------------------------------------------------
    // Additional edge case tests
    // -----------------------------------------------------------------------

    @Test
    public void testAddEmptyStringArray() {
        String[] empty = new String[0];
        String[] result = ArrayUtils.add(empty, "a");
        assertEquals(1, result.length);
        assertEquals("a", result[0]);
        assertTrue("Result must be String[]", result instanceof String[]);
    }

    @Test
    public void testAddAllEmptyAndNonEmpty() {
        String[] result = ArrayUtils.addAll(new String[0], new String[]{"a"});
        assertTrue("Result must be String[]", result instanceof String[]);
        assertArrayEquals(new String[]{"a"}, result);
    }

    @Test
    public void testCloneBooleanArray() {
        boolean[] original = new boolean[]{true, false, true};
        boolean[] cloned = ArrayUtils.clone(original);
        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertArrayEquals(original, cloned);
    }

    @Test
    public void testToObjectDouble() {
        Double[] result = ArrayUtils.toObject(new double[]{1.1, 2.2});
        assertArrayEquals(new Double[]{1.1, 2.2}, result);
    }

    @Test
    public void testToPrimitiveDouble() {
        double[] result = ArrayUtils.toPrimitive(new Double[]{1.1, 2.2});
        assertArrayEquals(new double[]{1.1, 2.2}, result, 0.0001);
    }
}