package org.apache.commons.lang3;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class ArrayUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testAddFirstNullArray() {
        ArrayUtils.add(null, new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFirstNullObject() {
        ArrayUtils.add(new Object[]{"a"}, null);
    }

    @Test
    public void testAddFirstBoolean() {
        boolean[] array = {true, false};
        boolean[] result = ArrayUtils.add(array, 0, true);
        assertArrayEquals(new boolean[]{true, true, false}, result);
    }

    @Test
    public void testAddFirstByte() {
        byte[] array = {1, 2};
        byte[] result = ArrayUtils.add(array, 0, (byte) 3);
        assertArrayEquals(new byte[]{3, 1, 2}, result);
    }

    @Test
    public void testAddFirstChar() {
        char[] array = {'a', 'b'};
        char[] result = ArrayUtils.add(array, 0, 'c');
        assertArrayEquals(new char[]{'c', 'a', 'b'}, result);
    }

    @Test
    public void testAddFirstDouble() {
        double[] array = {1.0, 2.0};
        double[] result = ArrayUtils.add(array, 0, 3.0);
        assertArrayEquals(new double[]{3.0, 1.0, 2.0}, result, 1e-15);
    }

    @Test
    public void testAddFirstFloat() {
        float[] array = {1.0f, 2.0f};
        float[] result = ArrayUtils.add(array, 0, 3.0f);
        assertArrayEquals(new float[]{3.0f, 1.0f, 2.0f}, result, 0.0f);
    }

    @Test
    public void testAddFirstInt() {
        int[] array = {1, 2};
        int[] result = ArrayUtils.add(array, 0, 3);
        assertArrayEquals(new int[]{3, 1, 2}, result);
    }

    @Test
    public void testAddFirstLong() {
        long[] array = {1L, 2L};
        long[] result = ArrayUtils.add(array, 0, 3L);
        assertArrayEquals(new long[]{3L, 1L, 2L}, result);
    }

    @Test
    public void testAddFirstShort() {
        short[] array = {(short) 1, (short) 2};
        short[] result = ArrayUtils.add(array, 0, (short) 3);
        assertArrayEquals(new short[]{(short) 3, (short) 1, (short) 2}, result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexNegativeIndex() {
        ArrayUtils.add(new int[]{1, 2}, -1, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexOutOfBounds() {
        ArrayUtils.add(new int[]{1, 2}, 3, 3);
    }

    @Test
    public void testAddAtEnd() {
        int[] array = {1, 2};
        int[] result = ArrayUtils.add(array, 2, 3);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test
    public void testAddAtStart() {
        int[] array = {1, 2};
        int[] result = ArrayUtils.add(array, 0, 3);
        assertArrayEquals(new int[]{3, 1, 2}, result);
    }

    @Test
    public void testAddAtMiddle() {
        int[] array = {1, 2, 3};
        int[] result = ArrayUtils.add(array, 1, 4);
        assertArrayEquals(new int[]{1, 4, 2, 3}, result);
    }

    @Test
    public void testAddPrimitiveArrayWithObject() {
        Object[] result = ArrayUtils.add(new int[]{1, 2}, 0, "string");
        assertArrayEquals(new Object[]{1, 2, "string"}, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddObjectArrayNull() {
        ArrayUtils.add((Object[]) null, 0, "test");
    }

    @Test
    public void testAddObjectArrayNullElement() {
        Object[] result = ArrayUtils.add(new Object[]{"a", "b"}, 1, null);
        assertArrayEquals(new Object[]{"a", null, "b"}, result);
    }

    @Test
    public void testAddObjectArrayNewType() {
        Number[] array = {1, 2};
        Number[] result = ArrayUtils.add(array, 0, 3.0);
        assertEquals(Double.class, result[0].getClass());
    }

    @Test
    public void testAddObjectArrayMaintainType() {
        String[] array = {"a", "b"};
        String[] result = ArrayUtils.add(array, 1, "c");
        assertArrayEquals(new String[]{"a", "c", "b"}, result);
    }

    @Test(expected = ArrayStoreException.class)
    public void testAddIncompatibleType() {
        String[] array = {"a", "b"};
        ArrayUtils.add(array, 1, 123);
    }

    @Test
    public void testAddLongArray() {
        long[] array = {1L, 2L};
        long[] result = ArrayUtils.add(array, 1, 3L);
        assertArrayEquals(new long[]{1L, 3L, 2L}, result);
    }

    @Test
    public void testAddEmptyObjectArray() {
        Object[] array = {};
        Object[] result = ArrayUtils.add(array, 0, "test");
        assertArrayEquals(new Object[]{"test"}, result);
    }

    @Test
    public void testAddEmptyStringArrayWithString() {
        String[] array = {};
        String[] result = ArrayUtils.add(array, 0, "test");
        assertArrayEquals(new String[]{"test"}, result);
    }

    @Test
    public void testAddSameType() {
        Object[] array = {"a", "b"};
        Object[] result = ArrayUtils.add(array, 0, "c");
        assertArrayEquals(new Object[]{"c", "a", "b"}, result);
    }

    @Test
    public void testAddSubtype() {
        Number[] array = {1, 2};
        Number[] result = ArrayUtils.add(array, 0, 3.0);
        assertArrayEquals(new Number[]{3.0, 1, 2}, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToBooleanArray() {
        ArrayUtils.add((boolean[]) null, 0, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToByteArray() {
        ArrayUtils.add((byte[]) null, 0, (byte) 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToCharArray() {
        ArrayUtils.add((char[]) null, 0, 'a');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToDoubleArray() {
        ArrayUtils.add((double[]) null, 0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToFloatArray() {
        ArrayUtils.add((float[]) null, 0, 1.0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToIntArray() {
        ArrayUtils.add((int[]) null, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToLongArray() {
        ArrayUtils.add((long[]) null, 0, 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToShortArray() {
        ArrayUtils.add((short[]) null, 0, (short) 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddBooleanArrayNegativeIndex() {
        ArrayUtils.add(new boolean[]{true}, -1, false);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddByteArrayNegativeIndex() {
        ArrayUtils.add(new byte[]{1}, -1, (byte) 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddCharArrayNegativeIndex() {
        ArrayUtils.add(new char[]{'a'}, -1, 'b');
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddDoubleArrayNegativeIndex() {
        ArrayUtils.add(new double[]{1.0}, -1, 2.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddFloatArrayNegativeIndex() {
        ArrayUtils.add(new float[]{1.0f}, -1, 2.0f);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddLongArrayNegativeIndex() {
        ArrayUtils.add(new long[]{1L}, -1, 2L);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddShortArrayNegativeIndex() {
        ArrayUtils.add(new short[]{(short) 1}, -1, (short) 2);
    }

    @Test
    public void testAddBooleanArrayAtEnd() {
        boolean[] array = {true, false};
        boolean[] result = ArrayUtils.add(array, 2, true);
        assertArrayEquals(new boolean[]{true, false, true}, result);
    }

    @Test
    public void testAddByteArrayAtEnd() {
        byte[] array = {1, 2};
        byte[] result = ArrayUtils.add(array, 2, (byte) 3);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    public void testAddCharArrayAtEnd() {
        char[] array = {'a', 'b'};
        char[] result = ArrayUtils.add(array, 2, 'c');
        assertArrayEquals(new char[]{'a', 'b', 'c'}, result);
    }

    @Test
    public void testAddDoubleArrayAtEnd() {
        double[] array = {1.0, 2.0};
        double[] result = ArrayUtils.add(array, 2, 3.0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, result, 1e-15);
    }

    @Test
    public void testAddFloatArrayAtEnd() {
        float[] array = {1.0f, 2.0f};
        float[] result = ArrayUtils.add(array, 2, 3.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, result, 0.0f);
    }

    @Test
    public void testAddLongArrayAtEnd() {
        long[] array = {1L, 2L};
        long[] result = ArrayUtils.add(array, 2, 3L);
        assertArrayEquals(new long[]{1L, 2L, 3L}, result);
    }

    @Test
    public void testAddShortArrayAtEnd() {
        short[] array = {(short) 1, (short) 2};
        short[] result = ArrayUtils.add(array, 2, (short) 3);
        assertArrayEquals(new short[]{(short) 1, (short) 2, (short) 3}, result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddBooleanArrayOutOfBounds() {
        ArrayUtils.add(new boolean[]{true}, 2, false);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddByteArrayOutOfBounds() {
        ArrayUtils.add(new byte[]{1}, 2, (byte) 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddCharArrayOutOfBounds() {
        ArrayUtils.add(new char[]{'a'}, 2, 'b');
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddDoubleArrayOutOfBounds() {
        ArrayUtils.add(new double[]{1.0}, 2, 2.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddFloatArrayOutOfBounds() {
        ArrayUtils.add(new float[]{1.0f}, 2, 2.0f);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddLongArrayOutOfBounds() {
        ArrayUtils.add(new long[]{1L}, 2, 2L);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddShortArrayOutOfBounds() {
        ArrayUtils.add(new short[]{(short) 1}, 2, (short) 2);
    }

    @Test
    public void testAddBooleanArrayMultiple() {
        boolean[] array = {};
        boolean[] result = ArrayUtils.add(array, 0, true);
        assertArrayEquals(new boolean[]{true}, result);
    }

    @Test
    public void testAddByteArrayMultiple() {
        byte[] array = {};
        byte[] result = ArrayUtils.add(array, 0, (byte) 1);
        assertArrayEquals(new byte[]{1}, result);
    }

    @Test
    public void testAddCharArrayMultiple() {
        char[] array = {};
        char[] result = ArrayUtils.add(array, 0, 'a');
        assertArrayEquals(new char[]{'a'}, result);
    }

    @Test
    public void testAddDoubleArrayMultiple() {
        double[] array = {};
        double[] result = ArrayUtils.add(array, 0, 1.0);
        assertArrayEquals(new double[]{1.0}, result, 1e-15);
    }

    @Test
    public void testAddFloatArrayMultiple() {
        float[] array = {};
        float[] result = ArrayUtils.add(array, 0, 1.0f);
        assertArrayEquals(new float[]{1.0f}, result, 0.0f);
    }

    @Test
    public void testAddIntArrayMultiple() {
        int[] array = {};
        int[] result = ArrayUtils.add(array, 0, 1);
        assertArrayEquals(new int[]{1}, result);
    }

    @Test
    public void testAddLongArrayMultiple() {
        long[] array = {};
        long[] result = ArrayUtils.add(array, 0, 1L);
        assertArrayEquals(new long[]{1L}, result);
    }

    @Test
    public void testAddShortArrayMultiple() {
        short[] array = {};
        short[] result = ArrayUtils.add(array, 0, (short) 1);
        assertArrayEquals(new short[]{(short) 1}, result);
    }
}