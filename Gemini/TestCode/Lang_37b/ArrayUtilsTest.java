package org.apache.commons.lang3;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ArrayUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new ArrayUtils());
        Constructor<?>[] constructors = ArrayUtils.class.getDeclaredConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPublic(constructors[0].getModifiers()));
        assertTrue(Modifier.isPublic(ArrayUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(ArrayUtils.class.getModifiers()));
    }

    @Test
    public void testJira567() {
        try {
            Number[] n = ArrayUtils.addAll(new Integer[]{Integer.valueOf(1)}, new Long[]{Long.valueOf(2)});
            fail("Should have generated IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Expected exception
        }
    }

    @Test
    public void testAddAllObject() {
        final String[] array1 = new String[]{"a", "b"};
        final String[] array2 = new String[]{"c", "d"};
        final String[] expected = new String[]{"a", "b", "c", "d"};

        assertArrayEquals(expected, ArrayUtils.addAll(array1, array2));
        assertArrayEquals(array1, ArrayUtils.addAll(array1, (String[]) null));
        assertArrayEquals(array2, ArrayUtils.addAll((String[]) null, array2));
        assertNull(ArrayUtils.addAll((String[]) null, (String[]) null));

        final Number[] num1 = new Number[]{1, 2};
        final Number[] num2 = new Number[]{3.0, 4.0};
        final Number[] result = ArrayUtils.addAll(num1, num2);
        assertEquals(4, result.length);
        assertEquals(1, result[0]);
        assertEquals(4.0, result[3]);
    }

    @Test
    public void testAddAllPrimitives() {
        assertArrayEquals(new boolean[]{true, false, true}, ArrayUtils.addAll(new boolean[]{true}, new boolean[]{false, true}));
        assertArrayEquals(new boolean[]{true}, ArrayUtils.addAll(new boolean[]{true}, (boolean[]) null));
        assertArrayEquals(new boolean[]{false}, ArrayUtils.addAll((boolean[]) null, new boolean[]{false}));
        assertNull(ArrayUtils.addAll((boolean[]) null, (boolean[]) null));

        assertArrayEquals(new byte[]{1, 2, 3, 4}, ArrayUtils.addAll(new byte[]{1, 2}, new byte[]{3, 4}));
        assertArrayEquals(new byte[]{1}, ArrayUtils.addAll(new byte[]{1}, (byte[]) null));
        assertArrayEquals(new byte[]{2}, ArrayUtils.addAll((byte[]) null, new byte[]{2}));
        assertNull(ArrayUtils.addAll((byte[]) null, (byte[]) null));

        assertArrayEquals(new char[]{'a', 'b', 'c'}, ArrayUtils.addAll(new char[]{'a', 'b'}, new char[]{'c'}));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.addAll(new char[]{'a'}, (char[]) null));
        assertArrayEquals(new char[]{'b'}, ArrayUtils.addAll((char[]) null, new char[]{'b'}));
        assertNull(ArrayUtils.addAll((char[]) null, (char[]) null));

        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, ArrayUtils.addAll(new double[]{1.0}, new double[]{2.0, 3.0}), 0.001);
        assertArrayEquals(new double[]{1.0}, ArrayUtils.addAll(new double[]{1.0}, (double[]) null), 0.001);
        assertArrayEquals(new double[]{2.0}, ArrayUtils.addAll((double[]) null, new double[]{2.0}), 0.001);
        assertNull(ArrayUtils.addAll((double[]) null, (double[]) null));

        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.addAll(new float[]{1.0f}, new float[]{2.0f}), 0.001f);
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.addAll(new float[]{1.0f}, (float[]) null), 0.001f);
        assertArrayEquals(new float[]{2.0f}, ArrayUtils.addAll((float[]) null, new float[]{2.0f}), 0.001f);
        assertNull(ArrayUtils.addAll((float[]) null, (float[]) null));

        assertArrayEquals(new int[]{1, 2, 3}, ArrayUtils.addAll(new int[]{1, 2}, new int[]{3}));
        assertArrayEquals(new int[]{1}, ArrayUtils.addAll(new int[]{1}, (int[]) null));
        assertArrayEquals(new int[]{2}, ArrayUtils.addAll((int[]) null, new int[]{2}));
        assertNull(ArrayUtils.addAll((int[]) null, (int[]) null));

        assertArrayEquals(new long[]{1L, 2L, 3L}, ArrayUtils.addAll(new long[]{1L}, new long[]{2L, 3L}));
        assertArrayEquals(new long[]{1L}, ArrayUtils.addAll(new long[]{1L}, (long[]) null));
        assertArrayEquals(new long[]{2L}, ArrayUtils.addAll((long[]) null, new long[]{2L}));
        assertNull(ArrayUtils.addAll((long[]) null, (long[]) null));

        assertArrayEquals(new short[]{1, 2, 3}, ArrayUtils.addAll(new short[]{1, 2}, new short[]{3}));
        assertArrayEquals(new short[]{1}, ArrayUtils.addAll(new short[]{1}, (short[]) null));
        assertArrayEquals(new short[]{2}, ArrayUtils.addAll((short[]) null, new short[]{2}));
        assertNull(ArrayUtils.addAll((short[]) null, (short[]) null));
    }

    @Test
    public void testAdd() {
        assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.add(new String[]{"a", "b"}, "c"));
        assertArrayEquals(new String[]{"a"}, ArrayUtils.add(null, "a"));

        assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.add(new String[]{"b", "c"}, 0, "a"));
        assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.add(new String[]{"a", "c"}, 1, "b"));
        assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.add(new String[]{"a", "b"}, 2, "c"));
        assertArrayEquals(new String[]{"a"}, ArrayUtils.add(null, 0, "a"));

        assertArrayEquals(new int[]{1, 2, 3}, ArrayUtils.add(new int[]{1, 2}, 3));
        assertArrayEquals(new int[]{1}, ArrayUtils.add((int[]) null, 1));
        assertArrayEquals(new int[]{1, 2, 3}, ArrayUtils.add(new int[]{2, 3}, 0, 1));

        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.add(new boolean[]{true}, false));
        assertArrayEquals(new boolean[]{true}, ArrayUtils.add((boolean[]) null, true));
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.add(new boolean[]{true}, 1, false));

        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.add(new byte[]{1}, (byte) 2));
        assertArrayEquals(new byte[]{1}, ArrayUtils.add((byte[]) null, (byte) 1));
        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.add(new byte[]{2}, 0, (byte) 1));

        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.add(new char[]{'a'}, 'b'));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.add((char[]) null, 'a'));
        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.add(new char[]{'b'}, 0, 'a'));

        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.add(new double[]{1.0}, 2.0), 0.001);
        assertArrayEquals(new double[]{1.0}, ArrayUtils.add((double[]) null, 1.0), 0.001);
        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.add(new double[]{2.0}, 0, 1.0), 0.001);

        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.add(new float[]{1.0f}, 2.0f), 0.001f);
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.add((float[]) null, 1.0f), 0.001f);
        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.add(new float[]{2.0f}, 0, 1.0f), 0.001f);

        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.add(new long[]{1L}, 2L));
        assertArrayEquals(new long[]{1L}, ArrayUtils.add((long[]) null, 1L));
        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.add(new long[]{2L}, 0, 1L));

        assertArrayEquals(new short[]{1, 2}, ArrayUtils.add(new short[]{1}, (short) 2));
        assertArrayEquals(new short[]{1}, ArrayUtils.add((short[]) null, (short) 1));
        assertArrayEquals(new short[]{1, 2}, ArrayUtils.add(new short[]{2}, 0, (short) 1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddIndexOutOfBoundsNegative() {
        ArrayUtils.add(new String[]{"a"}, -1, "b");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddIndexOutOfBoundsPositive() {
        ArrayUtils.add(new String[]{"a"}, 2, "b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullArrayNullElement() {
        ArrayUtils.add((Object[]) null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddIndexedNullArrayNullElement() {
        ArrayUtils.add((Object[]) null, 0, null);
    }

    @Test
    public void testRemoveElement() {
        assertArrayEquals(new String[]{"a", "c"}, ArrayUtils.removeElement(new String[]{"a", "b", "c"}, "b"));
        assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.removeElement(new String[]{"a", "b", "c"}, "d"));
        assertNull(ArrayUtils.removeElement((String[]) null, "a"));

        assertArrayEquals(new int[]{1, 3}, ArrayUtils.removeElement(new int[]{1, 2, 3}, 2));
        assertArrayEquals(new int[]{1, 2, 3}, ArrayUtils.removeElement(new int[]{1, 2, 3}, 4));
        assertNull(ArrayUtils.removeElement((int[]) null, 1));

        assertArrayEquals(new boolean[]{false}, ArrayUtils.removeElement(new boolean[]{true, false}, true));
        assertArrayEquals(new byte[]{2}, ArrayUtils.removeElement(new byte[]{1, 2}, (byte) 1));
        assertArrayEquals(new char[]{'b'}, ArrayUtils.removeElement(new char[]{'a', 'b'}, 'a'));
        assertArrayEquals(new double[]{2.0}, ArrayUtils.removeElement(new double[]{1.0, 2.0}, 1.0), 0.001);
        assertArrayEquals(new float[]{2.0f}, ArrayUtils.removeElement(new float[]{1.0f, 2.0f}, 1.0f), 0.001f);
        assertArrayEquals(new long[]{2L}, ArrayUtils.removeElement(new long[]{1L, 2L}, 1L));
        assertArrayEquals(new short[]{2}, ArrayUtils.removeElement(new short[]{1, 2}, (short) 1));
    }

    @Test
    public void testRemove() {
        assertArrayEquals(new String[]{"a", "c"}, ArrayUtils.remove(new String[]{"a", "b", "c"}, 1));
        assertArrayEquals(new int[]{1, 3}, ArrayUtils.remove(new int[]{1, 2, 3}, 1));
        assertArrayEquals(new boolean[]{false}, ArrayUtils.remove(new boolean[]{true, false}, 0));
        assertArrayEquals(new byte[]{2}, ArrayUtils.remove(new byte[]{1, 2}, 0));
        assertArrayEquals(new char[]{'b'}, ArrayUtils.remove(new char[]{'a', 'b'}, 0));
        assertArrayEquals(new double[]{2.0}, ArrayUtils.remove(new double[]{1.0, 2.0}, 0), 0.001);
        assertArrayEquals(new float[]{2.0f}, ArrayUtils.remove(new float[]{1.0f, 2.0f}, 0), 0.001f);
        assertArrayEquals(new long[]{2L}, ArrayUtils.remove(new long[]{1L, 2L}, 0));
        assertArrayEquals(new short[]{2}, ArrayUtils.remove(new short[]{1, 2}, 0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBounds() {
        ArrayUtils.remove(new String[]{"a"}, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBoundsNegative() {
        ArrayUtils.remove(new String[]{"a"}, -1);
    }

    @Test
    public void testClone() {
        final String[] strArr = new String[]{"a", "b"};
        final String[] clonedStr = ArrayUtils.clone(strArr);
        assertArrayEquals(strArr, clonedStr);
        assertNotSame(strArr, clonedStr);
        assertNull(ArrayUtils.clone((String[]) null));

        final int[] intArr = new int[]{1, 2};
        final int[] clonedInt = ArrayUtils.clone(intArr);
        assertArrayEquals(intArr, clonedInt);
        assertNotSame(intArr, clonedInt);
        assertNull(ArrayUtils.clone((int[]) null));

        assertArrayEquals(new boolean[]{true}, ArrayUtils.clone(new boolean[]{true}));
        assertNull(ArrayUtils.clone((boolean[]) null));
        assertArrayEquals(new byte[]{1}, ArrayUtils.clone(new byte[]{1}));
        assertNull(ArrayUtils.clone((byte[]) null));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.clone(new char[]{'a'}));
        assertNull(ArrayUtils.clone((char[]) null));
        assertArrayEquals(new double[]{1.0}, ArrayUtils.clone(new double[]{1.0}), 0.001);
        assertNull(ArrayUtils.clone((double[]) null));
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.clone(new float[]{1.0f}), 0.001f);
        assertNull(ArrayUtils.clone((float[]) null));
        assertArrayEquals(new long[]{1L}, ArrayUtils.clone(new long[]{1L}));
        assertNull(ArrayUtils.clone((long[]) null));
        assertArrayEquals(new short[]{1}, ArrayUtils.clone(new short[]{1}));
        assertNull(ArrayUtils.clone((short[]) null));
    }

    @Test
    public void testSubarray() {
        assertArrayEquals(new String[]{"b", "c"}, ArrayUtils.subarray(new String[]{"a", "b", "c", "d"}, 1, 3));
        assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.subarray(new String[]{"a", "b"}, -1, 5));
        assertArrayEquals(new String[]{}, ArrayUtils.subarray(new String[]{"a", "b"}, 2, 1));
        assertNull(ArrayUtils.subarray((String[]) null, 0, 1));

        assertArrayEquals(new int[]{2, 3}, ArrayUtils.subarray(new int[]{1, 2, 3, 4}, 1, 3));
        assertArrayEquals(new int[]{1, 2}, ArrayUtils.subarray(new int[]{1, 2}, -1, 5));
        assertArrayEquals(new int[]{}, ArrayUtils.subarray(new int[]{1, 2}, 2, 1));
        assertNull(ArrayUtils.subarray((int[]) null, 0, 1));

        assertArrayEquals(new boolean[]{true}, ArrayUtils.subarray(new boolean[]{true, false}, 0, 1));
        assertNull(ArrayUtils.subarray((boolean[]) null, 0, 1));
        assertArrayEquals(new byte[]{2}, ArrayUtils.subarray(new byte[]{1, 2, 3}, 1, 2));
        assertNull(ArrayUtils.subarray((byte[]) null, 0, 1));
        assertArrayEquals(new char[]{'b'}, ArrayUtils.subarray(new char[]{'a', 'b'}, 1, 2));
        assertNull(ArrayUtils.subarray((char[]) null, 0, 1));
        assertArrayEquals(new double[]{2.0}, ArrayUtils.subarray(new double[]{1.0, 2.0}, 1, 2), 0.001);
        assertNull(ArrayUtils.subarray((double[]) null, 0, 1));
        assertArrayEquals(new float[]{2.0f}, ArrayUtils.subarray(new float[]{1.0f, 2.0f}, 1, 2), 0.001f);
        assertNull(ArrayUtils.subarray((float[]) null, 0, 1));
        assertArrayEquals(new long[]{2L}, ArrayUtils.subarray(new long[]{1L, 2L}, 1, 2));
        assertNull(ArrayUtils.subarray((long[]) null, 0, 1));
        assertArrayEquals(new short[]{2}, ArrayUtils.subarray(new short[]{1, 2}, 1, 2));
        assertNull(ArrayUtils.subarray((short[]) null, 0, 1));
    }

    @Test
    public void testReverse() {
        final String[] strArr = new String[]{"a", "b", "c"};
        ArrayUtils.reverse(strArr);
        assertArrayEquals(new String[]{"c", "b", "a"}, strArr);
        ArrayUtils.reverse((String[]) null);

        final int[] intArr = new int[]{1, 2, 3, 4};
        ArrayUtils.reverse(intArr);
        assertArrayEquals(new int[]{4, 3, 2, 1}, intArr);
        ArrayUtils.reverse((int[]) null);

        final boolean[] boolArr = new boolean[]{true, false};
        ArrayUtils.reverse(boolArr);
        assertArrayEquals(new boolean[]{false, true}, boolArr);
        ArrayUtils.reverse((boolean[]) null);

        final byte[] byteArr = new byte[]{1, 2};
        ArrayUtils.reverse(byteArr);
        assertArrayEquals(new byte[]{2, 1}, byteArr);
        ArrayUtils.reverse((byte[]) null);

        final char[] charArr = new char[]{'a', 'b'};
        ArrayUtils.reverse(charArr);
        assertArrayEquals(new char[]{'b', 'a'}, charArr);
        ArrayUtils.reverse((char[]) null);

        final double[] doubleArr = new double[]{1.0, 2.0};
        ArrayUtils.reverse(doubleArr);
        assertArrayEquals(new double[]{2.0, 1.0}, doubleArr, 0.001);
        ArrayUtils.reverse((double[]) null);

        final float[] floatArr = new float[]{1.0f, 2.0f};
        ArrayUtils.reverse(floatArr);
        assertArrayEquals(new float[]{2.0f, 1.0f}, floatArr, 0.001f);
        ArrayUtils.reverse((float[]) null);

        final long[] longArr = new long[]{1L, 2L};
        ArrayUtils.reverse(longArr);
        assertArrayEquals(new long[]{2L, 1L}, longArr);
        ArrayUtils.reverse((long[]) null);

        final short[] shortArr = new short[]{1, 2};
        ArrayUtils.reverse(shortArr);
        assertArrayEquals(new short[]{2, 1}, shortArr);
        ArrayUtils.reverse((short[]) null);
    }

    @Test
    public void testIndexOfAndContains() {
        final String[] strArr = new String[]{"a", "b", "c", "b", null};
        assertEquals(1, ArrayUtils.indexOf(strArr, "b"));
        assertEquals(3, ArrayUtils.indexOf(strArr, "b", 2));
        assertEquals(4, ArrayUtils.indexOf(strArr, null));
        assertEquals(-1, ArrayUtils.indexOf(strArr, "z"));
        assertEquals(-1, ArrayUtils.indexOf(null, "a"));
        assertEquals(3, ArrayUtils.lastIndexOf(strArr, "b"));
        assertEquals(1, ArrayUtils.lastIndexOf(strArr, "b", 2));
        assertEquals(4, ArrayUtils.lastIndexOf(strArr, null));
        assertEquals(-1, ArrayUtils.lastIndexOf(strArr, "z"));
        assertEquals(-1, ArrayUtils.lastIndexOf(null, "a"));

        assertTrue(ArrayUtils.contains(strArr, "b"));
        assertTrue(ArrayUtils.contains(strArr, null));
        assertFalse(ArrayUtils.contains(strArr, "z"));
        assertFalse(ArrayUtils.contains(null, "a"));

        final int[] intArr = new int[]{1, 2, 3, 2};
        assertEquals(1, ArrayUtils.indexOf(intArr, 2));
        assertEquals(3, ArrayUtils.indexOf(intArr, 2, 2));
        assertEquals(-1, ArrayUtils.indexOf(intArr, 5));
        assertEquals(-1, ArrayUtils.indexOf(null, 1));
        assertEquals(3, ArrayUtils.lastIndexOf(intArr, 2));
        assertEquals(1, ArrayUtils.lastIndexOf(intArr, 2, 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(intArr, 5));
        assertEquals(-1, ArrayUtils.lastIndexOf(null, 1));
        assertTrue(ArrayUtils.contains(intArr, 2));
        assertFalse(ArrayUtils.contains(intArr, 5));
        assertFalse(ArrayUtils.contains(null, 1));

        final boolean[] boolArr = new boolean[]{true, false, true};
        assertEquals(1, ArrayUtils.indexOf(boolArr, false));
        assertEquals(0, ArrayUtils.lastIndexOf(boolArr, true, 0));
        assertTrue(ArrayUtils.contains(boolArr, false));
        assertFalse(ArrayUtils.contains(null, false));

        final byte[] byteArr = new byte[]{1, 2, 1};
        assertEquals(0, ArrayUtils.indexOf(byteArr, (byte) 1));
        assertEquals(2, ArrayUtils.lastIndexOf(byteArr, (byte) 1));
        assertTrue(ArrayUtils.contains(byteArr, (byte) 2));
        assertFalse(ArrayUtils.contains(null, (byte) 1));

        final char[] charArr = new char[]{'a', 'b', 'a'};
        assertEquals(0, ArrayUtils.indexOf(charArr, 'a'));
        assertEquals(2, ArrayUtils.lastIndexOf(charArr, 'a'));
        assertTrue(ArrayUtils.contains(charArr, 'b'));
        assertFalse(ArrayUtils.contains(null, 'a'));

        final double[] doubleArr = new double[]{1.0, 2.0, 1.0};
        assertEquals(0, ArrayUtils.indexOf(doubleArr, 1.0));
        assertEquals(0, ArrayUtils.indexOf(doubleArr, 1.05, 0.1));
        assertEquals(2, ArrayUtils.lastIndexOf(doubleArr, 1.0));
        assertEquals(2, ArrayUtils.lastIndexOf(doubleArr, 1.05, 0.1));
        assertTrue(ArrayUtils.contains(doubleArr, 2.0));
        assertTrue(ArrayUtils.contains(doubleArr, 2.05, 0.1));
        assertFalse(ArrayUtils.contains(null, 1.0));

        final float[] floatArr = new float[]{1.0f, 2.0f, 1.0f};
        assertEquals(0, ArrayUtils.indexOf(floatArr, 1.0f));
        assertEquals(2, ArrayUtils.lastIndexOf(floatArr, 1.0f));
        assertTrue(ArrayUtils.contains(floatArr, 2.0f));
        assertFalse(ArrayUtils.contains(null, 1.0f));

        final long[] longArr = new long[]{1L, 2L, 1L};
        assertEquals(0, ArrayUtils.indexOf(longArr, 1L));
        assertEquals(2, ArrayUtils.lastIndexOf(longArr, 1L));
        assertTrue(ArrayUtils.contains(longArr, 2L));
        assertFalse(ArrayUtils.contains(null, 1L));

        final short[] shortArr = new short[]{1, 2, 1};
        assertEquals(0, ArrayUtils.indexOf(shortArr, (short) 1));
        assertEquals(2, ArrayUtils.lastIndexOf(shortArr, (short) 1));
        assertTrue(ArrayUtils.contains(shortArr, (short) 2));
        assertFalse(ArrayUtils.contains(null, (short) 1));
    }

    @Test
    public void testIsEmptyAndIsNotEmpty() {
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertTrue(ArrayUtils.isEmpty(new String[0]));
        assertFalse(ArrayUtils.isEmpty(new String[]{"a"}));

        assertFalse(ArrayUtils.isNotEmpty((Object[]) null));
        assertFalse(ArrayUtils.isNotEmpty(new String[0]));
        assertTrue(ArrayUtils.isNotEmpty(new String[]{"a"}));

        assertTrue(ArrayUtils.isEmpty((int[]) null));
        assertTrue(ArrayUtils.isEmpty(new int[0]));
        assertFalse(ArrayUtils.isEmpty(new int[]{1}));

        assertTrue(ArrayUtils.isEmpty((boolean[]) null));
        assertTrue(ArrayUtils.isEmpty(new boolean[0]));
        assertFalse(ArrayUtils.isEmpty(new boolean[]{true}));

        assertTrue(ArrayUtils.isEmpty((byte[]) null));
        assertTrue(ArrayUtils.isEmpty(new byte[0]));
        assertFalse(ArrayUtils.isEmpty(new byte[]{1}));

        assertTrue(ArrayUtils.isEmpty((char[]) null));
        assertTrue(ArrayUtils.isEmpty(new char[0]));
        assertFalse(ArrayUtils.isEmpty(new char[]{'a'}));

        assertTrue(ArrayUtils.isEmpty((double[]) null));
        assertTrue(ArrayUtils.isEmpty(new double[0]));
        assertFalse(ArrayUtils.isEmpty(new double[]{1.0}));

        assertTrue(ArrayUtils.isEmpty((float[]) null));
        assertTrue(ArrayUtils.isEmpty(new float[0]));
        assertFalse(ArrayUtils.isEmpty(new float[]{1.0f}));

        assertTrue(ArrayUtils.isEmpty((long[]) null));
        assertTrue(ArrayUtils.isEmpty(new long[0]));
        assertFalse(ArrayUtils.isEmpty(new long[]{1L}));

        assertTrue(ArrayUtils.isEmpty((short[]) null));
        assertTrue(ArrayUtils.isEmpty(new short[0]));
        assertFalse(ArrayUtils.isEmpty(new short[]{1}));
    }

    @Test
    public void testToPrimitiveAndToObject() {
        assertArrayEquals(new int[]{1, 2}, ArrayUtils.toPrimitive(new Integer[]{1, 2}));
        assertArrayEquals(new int[]{1, -1}, ArrayUtils.toPrimitive(new Integer[]{1, null}, -1));
        assertNull(ArrayUtils.toPrimitive((Integer[]) null));
        assertArrayEquals(new Integer[]{1, 2}, ArrayUtils.toObject(new int[]{1, 2}));
        assertNull(ArrayUtils.toObject((int[]) null));

        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.toPrimitive(new Boolean[]{true, false}));
        assertArrayEquals(new boolean[]{true, true}, ArrayUtils.toPrimitive(new Boolean[]{true, null}, true));
        assertNull(ArrayUtils.toPrimitive((Boolean[]) null));
        assertArrayEquals(new Boolean[]{true, false}, ArrayUtils.toObject(new boolean[]{true, false}));
        assertNull(ArrayUtils.toObject((boolean[]) null));

        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.toPrimitive(new Byte[]{1, 2}));
        assertArrayEquals(new byte[]{1, 0}, ArrayUtils.toPrimitive(new Byte[]{1, null}, (byte) 0));
        assertNull(ArrayUtils.toPrimitive((Byte[]) null));
        assertArrayEquals(new Byte[]{1, 2}, ArrayUtils.toObject(new byte[]{1, 2}));
        assertNull(ArrayUtils.toObject((byte[]) null));

        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.toPrimitive(new Character[]{'a', 'b'}));
        assertArrayEquals(new char[]{'a', 'z'}, ArrayUtils.toPrimitive(new Character[]{'a', null}, 'z'));
        assertNull(ArrayUtils.toPrimitive((Character[]) null));
        assertArrayEquals(new Character[]{'a', 'b'}, ArrayUtils.toObject(new char[]{'a', 'b'}));
        assertNull(ArrayUtils.toObject((char[]) null));

        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.toPrimitive(new Double[]{1.0, 2.0}), 0.001);
        assertArrayEquals(new double[]{1.0, 0.0}, ArrayUtils.toPrimitive(new Double[]{1.0, null}, 0.0), 0.001);
        assertNull(ArrayUtils.toPrimitive((Double[]) null));
        assertArrayEquals(new Double[]{1.0, 2.0}, ArrayUtils.toObject(new double[]{1.0, 2.0}));
        assertNull(ArrayUtils.toObject((double[]) null));

        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.toPrimitive(new Float[]{1.0f, 2.0f}), 0.001f);
        assertArrayEquals(new float[]{1.0f, 0.0f}, ArrayUtils.toPrimitive(new Float[]{1.0f, null}, 0.0f), 0.001f);
        assertNull(ArrayUtils.toPrimitive((Float[]) null));
        assertArrayEquals(new Float[]{1.0f, 2.0f}, ArrayUtils.toObject(new float[]{1.0f, 2.0f}));
        assertNull(ArrayUtils.toObject((float[]) null));

        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.toPrimitive(new Long[]{1L, 2L}));
        assertArrayEquals(new long[]{1L, 0L}, ArrayUtils.toPrimitive(new Long[]{1L, null}, 0L));
        assertNull(ArrayUtils.toPrimitive((Long[]) null));
        assertArrayEquals(new Long[]{1L, 2L}, ArrayUtils.toObject(new long[]{1L, 2L}));
        assertNull(ArrayUtils.toObject((long[]) null));

        assertArrayEquals(new short[]{1, 2}, ArrayUtils.toPrimitive(new Short[]{1, 2}));
        assertArrayEquals(new short[]{1, 0}, ArrayUtils.toPrimitive(new Short[]{1, null}, (short) 0));
        assertNull(ArrayUtils.toPrimitive((Short[]) null));
        assertArrayEquals(new Short[]{1, 2}, ArrayUtils.toObject(new short[]{1, 2}));
        assertNull(ArrayUtils.toObject((short[]) null));
    }

    @Test
    public void testToMap() {
        Map<Object, Object> map = ArrayUtils.toMap(new String[][]{{"key1", "val1"}, {"key2", "val2"}});
        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));
        assertNull(ArrayUtils.toMap(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToMapInvalidElement() {
        ArrayUtils.toMap(new Object[]{"not a map entry or array"});
    }

    @Test
    public void testGetLength() {
        assertEquals(0, ArrayUtils.getLength(null));
        assertEquals(2, ArrayUtils.getLength(new String[]{"a", "b"}));
        assertEquals(3, ArrayUtils.getLength(new int[]{1, 2, 3}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLengthNonArray() {
        ArrayUtils.getLength("not an array");
    }

    @Test
    public void testIsSameLength() {
        assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
        assertTrue(ArrayUtils.isSameLength(new String[]{"a"}, new String[]{"b"}));
        assertFalse(ArrayUtils.isSameLength(new String[]{"a"}, (Object[]) null));
        assertFalse(ArrayUtils.isSameLength((Object[]) null, new String[]{"a"}));
        assertFalse(ArrayUtils.isSameLength(new String[]{"a"}, new String[]{"a", "b"}));

        assertTrue(ArrayUtils.isSameLength((int[]) null, (int[]) null));
        assertTrue(ArrayUtils.isSameLength(new int[]{1}, new int[]{2}));
        assertFalse(ArrayUtils.isSameLength(new int[]{1}, (int[]) null));
        assertFalse(ArrayUtils.isSameLength((int[]) null, new int[]{1}));
        assertFalse(ArrayUtils.isSameLength(new int[]{1}, new int[]{1, 2}));
    }

    @Test
    public void testIsSameType() {
        assertTrue(ArrayUtils.isSameType(new String[]{"a"}, new String[]{"b"}));
        assertFalse(ArrayUtils.isSameType(new String[]{"a"}, new Integer[]{1}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameTypeNullFirst() {
        ArrayUtils.isSameType(null, new String[]{"a"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsSameTypeNullSecond() {
        ArrayUtils.isSameType(new String[]{"a"}, null);
    }

    @Test
    public void testToString() {
        assertEquals("{}", ArrayUtils.toString(null));
        assertEquals("null", ArrayUtils.toString(null, "null"));
        assertEquals("{a,b}", ArrayUtils.toString(new String[]{"a", "b"}));
    }

    @Test
    public void testHashCode() {
        final String[] array = new String[]{"a", "b"};
        assertEquals(Arrays.hashCode(array), ArrayUtils.hashCode(array));
    }
}