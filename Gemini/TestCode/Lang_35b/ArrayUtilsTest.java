package org.apache.commons.lang3;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ArrayUtils}.
 */
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

    // ------------------------------------------------------------------------
    // Tests for Defects4J Bug 35 (LANG-571): add(T[], T) and add(T[], int, T)
    // ------------------------------------------------------------------------

    @Test
    public void testLANG571() {
        final String[] stringArray = null;
        final String aString = "a";
        final String[] sa = ArrayUtils.add(stringArray, aString);
        assertNotNull(sa);
        assertTrue(Arrays.equals(new String[]{"a"}, sa));
        assertEquals(String.class, sa.getClass().getComponentType());

        final String[] saIndex = ArrayUtils.add(stringArray, 0, aString);
        assertNotNull(saIndex);
        assertTrue(Arrays.equals(new String[]{"a"}, saIndex));
        assertEquals(String.class, saIndex.getClass().getComponentType());

        final Integer[] intArray = null;
        final Integer aInt = 1;
        final Integer[] ia = ArrayUtils.add(intArray, aInt);
        assertNotNull(ia);
        assertTrue(Arrays.equals(new Integer[]{1}, ia));
        assertEquals(Integer.class, ia.getClass().getComponentType());

        final Integer[] iaIndex = ArrayUtils.add(intArray, 0, aInt);
        assertNotNull(iaIndex);
        assertTrue(Arrays.equals(new Integer[]{1}, iaIndex));
        assertEquals(Integer.class, iaIndex.getClass().getComponentType());

        final Object[] nullArray = null;
        final Object[] oa = ArrayUtils.add(nullArray, null);
        assertNotNull(oa);
        assertTrue(Arrays.equals(new Object[]{null}, oa));
        assertEquals(Object.class, oa.getClass().getComponentType());

        final Object[] oaIndex = ArrayUtils.add(nullArray, 0, null);
        assertNotNull(oaIndex);
        assertTrue(Arrays.equals(new Object[]{null}, oaIndex));
        assertEquals(Object.class, oaIndex.getClass().getComponentType());
    }

    @Test
    public void testAddObjectArray() {
        String[] array = new String[]{"a", "b"};
        String[] result = ArrayUtils.add(array, "c");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
        assertEquals(String.class, result.getClass().getComponentType());

        result = ArrayUtils.add(array, 1, "inserted");
        assertArrayEquals(new String[]{"a", "inserted", "b"}, result);

        result = ArrayUtils.add(array, 0, "first");
        assertArrayEquals(new String[]{"first", "a", "b"}, result);

        result = ArrayUtils.add(array, 2, "last");
        assertArrayEquals(new String[]{"a", "b", "last"}, result);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddObjectArrayOutOfBoundsNegative() {
        ArrayUtils.add(new String[]{"a"}, -1, "b");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddObjectArrayOutOfBoundsPositive() {
        ArrayUtils.add(new String[]{"a"}, 2, "b");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddObjectArrayIncompatibleType() {
        Object[] array = new String[]{"a"};
        ArrayUtils.add(array, 0, Integer.valueOf(1));
    }

    // ------------------------------------------------------------------------
    // Tests for primitive add and add at index
    // ------------------------------------------------------------------------

    @Test
    public void testAddPrimitives() {
        // boolean
        assertArrayEquals(new boolean[]{true}, ArrayUtils.add((boolean[]) null, true));
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.add(new boolean[]{true}, false));
        assertArrayEquals(new boolean[]{false, true}, ArrayUtils.add(new boolean[]{true}, 0, false));

        // byte
        assertArrayEquals(new byte[]{1}, ArrayUtils.add((byte[]) null, (byte) 1));
        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.add(new byte[]{1}, (byte) 2));
        assertArrayEquals(new byte[]{2, 1}, ArrayUtils.add(new byte[]{1}, 0, (byte) 2));

        // char
        assertArrayEquals(new char[]{'a'}, ArrayUtils.add((char[]) null, 'a'));
        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.add(new char[]{'a'}, 'b'));
        assertArrayEquals(new char[]{'b', 'a'}, ArrayUtils.add(new char[]{'a'}, 0, 'b'));

        // double
        assertArrayEquals(new double[]{1.0}, ArrayUtils.add((double[]) null, 1.0), 0.0);
        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.add(new double[]{1.0}, 2.0), 0.0);
        assertArrayEquals(new double[]{2.0, 1.0}, ArrayUtils.add(new double[]{1.0}, 0, 2.0), 0.0);

        // float
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.add((float[]) null, 1.0f), 0.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.add(new float[]{1.0f}, 2.0f), 0.0f);
        assertArrayEquals(new float[]{2.0f, 1.0f}, ArrayUtils.add(new float[]{1.0f}, 0, 2.0f), 0.0f);

        // int
        assertArrayEquals(new int[]{1}, ArrayUtils.add((int[]) null, 1));
        assertArrayEquals(new int[]{1, 2}, ArrayUtils.add(new int[]{1}, 2));
        assertArrayEquals(new int[]{2, 1}, ArrayUtils.add(new int[]{1}, 0, 2));

        // long
        assertArrayEquals(new long[]{1L}, ArrayUtils.add((long[]) null, 1L));
        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.add(new long[]{1L}, 2L));
        assertArrayEquals(new long[]{2L, 1L}, ArrayUtils.add(new long[]{1L}, 0, 2L));

        // short
        assertArrayEquals(new short[]{1}, ArrayUtils.add((short[]) null, (short) 1));
        assertArrayEquals(new short[]{1, 2}, ArrayUtils.add(new short[]{1}, (short) 2));
        assertArrayEquals(new short[]{2, 1}, ArrayUtils.add(new short[]{1}, 0, (short) 2));
    }

    // ------------------------------------------------------------------------
    // Tests for addAll
    // ------------------------------------------------------------------------

    @Test
    public void testAddAllObjectArray() {
        assertNull(ArrayUtils.addAll((Object[]) null, (Object[]) null));
        assertArrayEquals(new String[]{"a"}, ArrayUtils.addAll(new String[]{"a"}, (String[]) null));
        assertArrayEquals(new String[]{"b"}, ArrayUtils.addAll((String[]) null, new String[]{"b"}));
        assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.addAll(new String[]{"a"}, new String[]{"b", "c"}));

        // Type promotion to superclass
        Number[] numbers = ArrayUtils.addAll(new Number[]{1}, new Number[]{2.0});
        assertArrayEquals(new Number[]{1, 2.0}, numbers);
    }

    @Test
    public void testAddAllPrimitives() {
        // boolean
        assertNull(ArrayUtils.addAll((boolean[]) null, (boolean[]) null));
        assertArrayEquals(new boolean[]{true}, ArrayUtils.addAll(new boolean[]{true}, (boolean[]) null));
        assertArrayEquals(new boolean[]{false}, ArrayUtils.addAll((boolean[]) null, new boolean[]{false}));
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.addAll(new boolean[]{true}, new boolean[]{false}));

        // byte
        assertNull(ArrayUtils.addAll((byte[]) null, (byte[]) null));
        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.addAll(new byte[]{1}, new byte[]{2}));

        // char
        assertNull(ArrayUtils.addAll((char[]) null, (char[]) null));
        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.addAll(new char[]{'a'}, new char[]{'b'}));

        // double
        assertNull(ArrayUtils.addAll((double[]) null, (double[]) null));
        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.addAll(new double[]{1.0}, new double[]{2.0}), 0.0);

        // float
        assertNull(ArrayUtils.addAll((float[]) null, (float[]) null));
        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.addAll(new float[]{1.0f}, new float[]{2.0f}), 0.0f);

        // int
        assertNull(ArrayUtils.addAll((int[]) null, (int[]) null));
        assertArrayEquals(new int[]{1, 2}, ArrayUtils.addAll(new int[]{1}, new int[]{2}));

        // long
        assertNull(ArrayUtils.addAll((long[]) null, (long[]) null));
        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.addAll(new long[]{1L}, new long[]{2L}));

        // short
        assertNull(ArrayUtils.addAll((short[]) null, (short[]) null));
        assertArrayEquals(new short[]{1, 2}, ArrayUtils.addAll(new short[]{1}, new short[]{2}));
    }

    // ------------------------------------------------------------------------
    // Tests for remove and removeElement
    // ------------------------------------------------------------------------

    @Test
    public void testRemoveObjectArray() {
        String[] array = new String[]{"a", "b", "c"};
        assertArrayEquals(new String[]{"a", "c"}, ArrayUtils.remove(array, 1));
        assertArrayEquals(new String[]{"b", "c"}, ArrayUtils.remove(array, 0));
        assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.remove(array, 2));

        assertArrayEquals(new String[]{"a", "c"}, ArrayUtils.removeElement(array, "b"));
        assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.removeElement(array, "d"));
        assertNull(ArrayUtils.remove((String[]) null, 0));
        assertNull(ArrayUtils.removeElement((String[]) null, "a"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveObjectArrayOutOfBoundsLow() {
        ArrayUtils.remove(new String[]{"a"}, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveObjectArrayOutOfBoundsHigh() {
        ArrayUtils.remove(new String[]{"a"}, 1);
    }

    @Test
    public void testRemovePrimitives() {
        assertArrayEquals(new int[]{1, 3}, ArrayUtils.remove(new int[]{1, 2, 3}, 1));
        assertArrayEquals(new int[]{1, 3}, ArrayUtils.removeElement(new int[]{1, 2, 3}, 2));
        assertArrayEquals(new int[]{1, 2, 3}, ArrayUtils.removeElement(new int[]{1, 2, 3}, 4));
        assertNull(ArrayUtils.remove((int[]) null, 0));
        assertNull(ArrayUtils.removeElement((int[]) null, 1));

        assertArrayEquals(new boolean[]{true}, ArrayUtils.remove(new boolean[]{true, false}, 1));
        assertArrayEquals(new boolean[]{false}, ArrayUtils.removeElement(new boolean[]{true, false}, true));

        assertArrayEquals(new byte[]{1}, ArrayUtils.remove(new byte[]{1, 2}, 1));
        assertArrayEquals(new byte[]{2}, ArrayUtils.removeElement(new byte[]{1, 2}, (byte) 1));

        assertArrayEquals(new char[]{'a'}, ArrayUtils.remove(new char[]{'a', 'b'}, 1));
        assertArrayEquals(new char[]{'b'}, ArrayUtils.removeElement(new char[]{'a', 'b'}, 'a'));

        assertArrayEquals(new double[]{1.0}, ArrayUtils.remove(new double[]{1.0, 2.0}, 1), 0.0);
        assertArrayEquals(new double[]{2.0}, ArrayUtils.removeElement(new double[]{1.0, 2.0}, 1.0), 0.0);

        assertArrayEquals(new float[]{1.0f}, ArrayUtils.remove(new float[]{1.0f, 2.0f}, 1), 0.0f);
        assertArrayEquals(new float[]{2.0f}, ArrayUtils.removeElement(new float[]{1.0f, 2.0f}, 1.0f), 0.0f);

        assertArrayEquals(new long[]{1L}, ArrayUtils.remove(new long[]{1L, 2L}, 1));
        assertArrayEquals(new long[]{2L}, ArrayUtils.removeElement(new long[]{1L, 2L}, 1L));

        assertArrayEquals(new short[]{1}, ArrayUtils.remove(new short[]{1, 2}, 1));
        assertArrayEquals(new short[]{2}, ArrayUtils.removeElement(new short[]{1, 2}, (short) 1));
    }

    // ------------------------------------------------------------------------
    // Tests for indexOf, lastIndexOf, contains
    // ------------------------------------------------------------------------

    @Test
    public void testIndexOfObject() {
        String[] array = new String[]{"a", "b", "c", "b", null};
        assertEquals(1, ArrayUtils.indexOf(array, "b"));
        assertEquals(3, ArrayUtils.indexOf(array, "b", 2));
        assertEquals(-1, ArrayUtils.indexOf(array, "d"));
        assertEquals(4, ArrayUtils.indexOf(array, null));
        assertEquals(-1, ArrayUtils.indexOf((Object[]) null, "a"));
        assertEquals(-1, ArrayUtils.indexOf(array, "b", 10));
        assertEquals(1, ArrayUtils.indexOf(array, "b", -1));

        assertEquals(3, ArrayUtils.lastIndexOf(array, "b"));
        assertEquals(1, ArrayUtils.lastIndexOf(array, "b", 2));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, "d"));
        assertEquals(4, ArrayUtils.lastIndexOf(array, null));
        assertEquals(-1, ArrayUtils.lastIndexOf((Object[]) null, "a"));
        assertEquals(-1, ArrayUtils.lastIndexOf(array, "b", -1));

        assertTrue(ArrayUtils.contains(array, "a"));
        assertTrue(ArrayUtils.contains(array, null));
        assertFalse(ArrayUtils.contains(array, "z"));
        assertFalse(ArrayUtils.contains((Object[]) null, "a"));
    }

    @Test
    public void testIndexOfPrimitives() {
        int[] intArray = new int[]{1, 2, 3, 2};
        assertEquals(1, ArrayUtils.indexOf(intArray, 2));
        assertEquals(3, ArrayUtils.indexOf(intArray, 2, 2));
        assertEquals(3, ArrayUtils.lastIndexOf(intArray, 2));
        assertEquals(1, ArrayUtils.lastIndexOf(intArray, 2, 2));
        assertTrue(ArrayUtils.contains(intArray, 2));
        assertFalse(ArrayUtils.contains(intArray, 5));

        long[] longArray = new long[]{1L, 2L, 3L};
        assertEquals(1, ArrayUtils.indexOf(longArray, 2L));
        assertEquals(1, ArrayUtils.lastIndexOf(longArray, 2L));
        assertTrue(ArrayUtils.contains(longArray, 2L));

        short[] shortArray = new short[]{1, 2, 3};
        assertEquals(1, ArrayUtils.indexOf(shortArray, (short) 2));
        assertEquals(1, ArrayUtils.lastIndexOf(shortArray, (short) 2));
        assertTrue(ArrayUtils.contains(shortArray, (short) 2));

        char[] charArray = new char[]{'a', 'b', 'c'};
        assertEquals(1, ArrayUtils.indexOf(charArray, 'b'));
        assertEquals(1, ArrayUtils.lastIndexOf(charArray, 'b'));
        assertTrue(ArrayUtils.contains(charArray, 'b'));

        byte[] byteArray = new byte[]{1, 2, 3};
        assertEquals(1, ArrayUtils.indexOf(byteArray, (byte) 2));
        assertEquals(1, ArrayUtils.lastIndexOf(byteArray, (byte) 2));
        assertTrue(ArrayUtils.contains(byteArray, (byte) 2));

        double[] doubleArray = new double[]{1.0, 2.0, 3.0};
        assertEquals(1, ArrayUtils.indexOf(doubleArray, 2.0));
        assertEquals(1, ArrayUtils.lastIndexOf(doubleArray, 2.0));
        assertTrue(ArrayUtils.contains(doubleArray, 2.0));
        assertEquals(1, ArrayUtils.indexOf(doubleArray, 2.05, 0, 0.1));
        assertEquals(1, ArrayUtils.lastIndexOf(doubleArray, 2.05, 2, 0.1));
        assertTrue(ArrayUtils.contains(doubleArray, 2.05, 0.1));

        float[] floatArray = new float[]{1.0f, 2.0f, 3.0f};
        assertEquals(1, ArrayUtils.indexOf(floatArray, 2.0f));
        assertEquals(1, ArrayUtils.lastIndexOf(floatArray, 2.0f));
        assertTrue(ArrayUtils.contains(floatArray, 2.0f));

        boolean[] boolArray = new boolean[]{true, false, true};
        assertEquals(1, ArrayUtils.indexOf(boolArray, false));
        assertEquals(1, ArrayUtils.lastIndexOf(boolArray, false));
        assertTrue(ArrayUtils.contains(boolArray, false));
    }

    // ------------------------------------------------------------------------
    // Tests for subarray and clone
    // ------------------------------------------------------------------------

    @Test
    public void testSubarrayObject() {
        String[] array = new String[]{"a", "b", "c", "d"};
        assertArrayEquals(new String[]{"b", "c"}, ArrayUtils.subarray(array, 1, 3));
        assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.subarray(array, -1, 2));
        assertArrayEquals(new String[]{"c", "d"}, ArrayUtils.subarray(array, 2, 10));
        assertArrayEquals(new String[]{}, ArrayUtils.subarray(array, 3, 2));
        assertNull(ArrayUtils.subarray((String[]) null, 0, 1));
    }

    @Test
    public void testSubarrayPrimitives() {
        int[] intArray = new int[]{1, 2, 3, 4};
        assertArrayEquals(new int[]{2, 3}, ArrayUtils.subarray(intArray, 1, 3));
        assertNull(ArrayUtils.subarray((int[]) null, 0, 1));

        long[] longArray = new long[]{1L, 2L, 3L, 4L};
        assertArrayEquals(new long[]{2L, 3L}, ArrayUtils.subarray(longArray, 1, 3));
        assertNull(ArrayUtils.subarray((long[]) null, 0, 1));

        short[] shortArray = new short[]{1, 2, 3, 4};
        assertArrayEquals(new short[]{2, 3}, ArrayUtils.subarray(shortArray, 1, 3));
        assertNull(ArrayUtils.subarray((short[]) null, 0, 1));

        char[] charArray = new char[]{'a', 'b', 'c', 'd'};
        assertArrayEquals(new char[]{'b', 'c'}, ArrayUtils.subarray(charArray, 1, 3));
        assertNull(ArrayUtils.subarray((char[]) null, 0, 1));

        byte[] byteArray = new byte[]{1, 2, 3, 4};
        assertArrayEquals(new byte[]{2, 3}, ArrayUtils.subarray(byteArray, 1, 3));
        assertNull(ArrayUtils.subarray((byte[]) null, 0, 1));

        double[] doubleArray = new double[]{1.0, 2.0, 3.0, 4.0};
        assertArrayEquals(new double[]{2.0, 3.0}, ArrayUtils.subarray(doubleArray, 1, 3), 0.0);
        assertNull(ArrayUtils.subarray((double[]) null, 0, 1));

        float[] floatArray = new float[]{1.0f, 2.0f, 3.0f, 4.0f};
        assertArrayEquals(new float[]{2.0f, 3.0f}, ArrayUtils.subarray(floatArray, 1, 3), 0.0f);
        assertNull(ArrayUtils.subarray((float[]) null, 0, 1));

        boolean[] boolArray = new boolean[]{true, false, true, false};
        assertArrayEquals(new boolean[]{false, true}, ArrayUtils.subarray(boolArray, 1, 3));
        assertNull(ArrayUtils.subarray((boolean[]) null, 0, 1));
    }

    @Test
    public void testClone() {
        String[] strArray = new String[]{"a", "b"};
        assertArrayEquals(strArray, ArrayUtils.clone(strArray));
        assertNull(ArrayUtils.clone((String[]) null));

        int[] intArray = new int[]{1, 2};
        assertArrayEquals(intArray, ArrayUtils.clone(intArray));
        assertNull(ArrayUtils.clone((int[]) null));

        long[] longArray = new long[]{1L, 2L};
        assertArrayEquals(longArray, ArrayUtils.clone(longArray));
        assertNull(ArrayUtils.clone((long[]) null));

        short[] shortArray = new short[]{1, 2};
        assertArrayEquals(shortArray, ArrayUtils.clone(shortArray));
        assertNull(ArrayUtils.clone((short[]) null));

        char[] charArray = new char[]{'a', 'b'};
        assertArrayEquals(charArray, ArrayUtils.clone(charArray));
        assertNull(ArrayUtils.clone((char[]) null));

        byte[] byteArray = new byte[]{1, 2};
        assertArrayEquals(byteArray, ArrayUtils.clone(byteArray));
        assertNull(ArrayUtils.clone((byte[]) null));

        double[] doubleArray = new double[]{1.0, 2.0};
        assertArrayEquals(doubleArray, ArrayUtils.clone(doubleArray), 0.0);
        assertNull(ArrayUtils.clone((double[]) null));

        float[] floatArray = new float[]{1.0f, 2.0f};
        assertArrayEquals(floatArray, ArrayUtils.clone(floatArray), 0.0f);
        assertNull(ArrayUtils.clone((float[]) null));

        boolean[] boolArray = new boolean[]{true, false};
        assertArrayEquals(boolArray, ArrayUtils.clone(boolArray));
        assertNull(ArrayUtils.clone((boolean[]) null));
    }

    // ------------------------------------------------------------------------
    // Tests for isEmpty, isNotEmpty, getLength, isSameLength, isSameType
    // ------------------------------------------------------------------------

    @Test
    public void testLengthAndEmpty() {
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertTrue(ArrayUtils.isEmpty(new Object[0]));
        assertFalse(ArrayUtils.isEmpty(new Object[]{1}));

        assertTrue(ArrayUtils.isNotEmpty(new Object[]{1}));
        assertFalse(ArrayUtils.isNotEmpty((Object[]) null));

        assertEquals(0, ArrayUtils.getLength(null));
        assertEquals(3, ArrayUtils.getLength(new int[]{1, 2, 3}));
        assertEquals(0, ArrayUtils.getLength(new Object[0]));

        assertTrue(ArrayUtils.isSameLength((int[]) null, (int[]) null));
        assertTrue(ArrayUtils.isSameLength(new int[]{1}, new int[]{2}));
        assertFalse(ArrayUtils.isSameLength(new int[]{1}, new int[]{1, 2}));
        assertFalse(ArrayUtils.isSameLength(new int[]{1}, (int[]) null));

        assertTrue(ArrayUtils.isSameType(new String[0], new String[1]));
        assertFalse(ArrayUtils.isSameType(new String[0], new Integer[0]));
    }

    // ------------------------------------------------------------------------
    // Tests for reverse and toMap
    // ------------------------------------------------------------------------

    @Test
    public void testReverse() {
        String[] strArray = new String[]{"a", "b", "c"};
        ArrayUtils.reverse(strArray);
        assertArrayEquals(new String[]{"c", "b", "a"}, strArray);
        ArrayUtils.reverse((Object[]) null);

        int[] intArray = new int[]{1, 2, 3, 4};
        ArrayUtils.reverse(intArray);
        assertArrayEquals(new int[]{4, 3, 2, 1}, intArray);
        ArrayUtils.reverse((int[]) null);

        long[] longArray = new long[]{1L, 2L, 3L};
        ArrayUtils.reverse(longArray);
        assertArrayEquals(new long[]{3L, 2L, 1L}, longArray);

        short[] shortArray = new short[]{1, 2, 3};
        ArrayUtils.reverse(shortArray);
        assertArrayEquals(new short[]{3, 2, 1}, shortArray);

        char[] charArray = new char[]{'a', 'b', 'c'};
        ArrayUtils.reverse(charArray);
        assertArrayEquals(new char[]{'c', 'b', 'a'}, charArray);

        byte[] byteArray = new byte[]{1, 2, 3};
        ArrayUtils.reverse(byteArray);
        assertArrayEquals(new byte[]{3, 2, 1}, byteArray);

        double[] doubleArray = new double[]{1.0, 2.0, 3.0};
        ArrayUtils.reverse(doubleArray);
        assertArrayEquals(new double[]{3.0, 2.0, 1.0}, doubleArray, 0.0);

        float[] floatArray = new float[]{1.0f, 2.0f, 3.0f};
        ArrayUtils.reverse(floatArray);
        assertArrayEquals(new float[]{3.0f, 2.0f, 1.0f}, floatArray, 0.0f);

        boolean[] boolArray = new boolean[]{true, false, true};
        ArrayUtils.reverse(boolArray);
        assertArrayEquals(new boolean[]{true, false, true}, boolArray);
    }

    @Test
    public void testToMap() {
        Map<Object, Object> map = ArrayUtils.toMap(new String[][]{
                {"key1", "value1"},
                {"key2", "value2"}
        });
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
        assertNull(ArrayUtils.toMap(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToMapInvalid() {
        ArrayUtils.toMap(new String[]{"invalid"});
    }

    // ------------------------------------------------------------------------
    // Tests for toPrimitive and toObject
    // ------------------------------------------------------------------------

    @Test
    public void testToPrimitiveAndToObject() {
        Integer[] objInts = new Integer[]{1, 2, null};
        int[] primInts = ArrayUtils.toPrimitive(objInts, 0);
        assertArrayEquals(new int[]{1, 2, 0}, primInts);
        assertArrayEquals(new Integer[]{1, 2, 0}, ArrayUtils.toObject(primInts));
        assertNull(ArrayUtils.toPrimitive((Integer[]) null));
        assertNull(ArrayUtils.toObject((int[]) null));

        Boolean[] objBools = new Boolean[]{Boolean.TRUE, Boolean.FALSE, null};
        boolean[] primBools = ArrayUtils.toPrimitive(objBools, true);
        assertArrayEquals(new boolean[]{true, false, true}, primBools);
        assertArrayEquals(new Boolean[]{true, false, true}, ArrayUtils.toObject(primBools));
        assertNull(ArrayUtils.toPrimitive((Boolean[]) null));
        assertNull(ArrayUtils.toObject((boolean[]) null));

        Long[] objLongs = new Long[]{1L, 2L, null};
        long[] primLongs = ArrayUtils.toPrimitive(objLongs, 0L);
        assertArrayEquals(new long[]{1L, 2L, 0L}, primLongs);
        assertArrayEquals(new Long[]{1L, 2L, 0L}, ArrayUtils.toObject(primLongs));
        assertNull(ArrayUtils.toPrimitive((Long[]) null));
        assertNull(ArrayUtils.toObject((long[]) null));

        Double[] objDoubles = new Double[]{1.0, 2.0, null};
        double[] primDoubles = ArrayUtils.toPrimitive(objDoubles, 0.0);
        assertArrayEquals(new double[]{1.0, 2.0, 0.0}, primDoubles, 0.0);
        assertArrayEquals(new Double[]{1.0, 2.0, 0.0}, ArrayUtils.toObject(primDoubles));
        assertNull(ArrayUtils.toPrimitive((Double[]) null));
        assertNull(ArrayUtils.toObject((double[]) null));

        Float[] objFloats = new Float[]{1.0f, 2.0f, null};
        float[] primFloats = ArrayUtils.toPrimitive(objFloats, 0.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f, 0.0f}, primFloats, 0.0f);
        assertArrayEquals(new Float[]{1.0f, 2.0f, 0.0f}, ArrayUtils.toObject(primFloats));
        assertNull(ArrayUtils.toPrimitive((Float[]) null));
        assertNull(ArrayUtils.toObject((float[]) null));

        Character[] objChars = new Character[]{'a', 'b', null};
        char[] primChars = ArrayUtils.toPrimitive(objChars, ' ');
        assertArrayEquals(new char[]{'a', 'b', ' '}, primChars);
        assertArrayEquals(new Character[]{'a', 'b', ' '}, ArrayUtils.toObject(primChars));
        assertNull(ArrayUtils.toPrimitive((Character[]) null));
        assertNull(ArrayUtils.toObject((char[]) null));

        Byte[] objBytes = new Byte[]{1, 2, null};
        byte[] primBytes = ArrayUtils.toPrimitive(objBytes, (byte) 0);
        assertArrayEquals(new byte[]{1, 2, 0}, primBytes);
        assertArrayEquals(new Byte[]{1, 2, 0}, ArrayUtils.toObject(primBytes));
        assertNull(ArrayUtils.toPrimitive((Byte[]) null));
        assertNull(ArrayUtils.toObject((byte[]) null));

        Short[] objShorts = new Short[]{1, 2, null};
        short[] primShorts = ArrayUtils.toPrimitive(objShorts, (short) 0);
        assertArrayEquals(new short[]{1, 2, 0}, primShorts);
        assertArrayEquals(new Short[]{1, 2, 0}, ArrayUtils.toObject(primShorts));
        assertNull(ArrayUtils.toPrimitive((Short[]) null));
        assertNull(ArrayUtils.toObject((short[]) null));
    }

    // ------------------------------------------------------------------------
    // Tests for nullToEmpty and toArray
    // ------------------------------------------------------------------------

    @Test
    public void testNullToEmpty() {
        assertNotNull(ArrayUtils.nullToEmpty((Object[]) null));
        assertEquals(0, ArrayUtils.nullToEmpty((Object[]) null).length);
        assertNotNull(ArrayUtils.nullToEmpty((String[]) null));
        assertEquals(0, ArrayUtils.nullToEmpty((String[]) null).length);

        assertNotNull(ArrayUtils.nullToEmpty((boolean[]) null));
        assertNotNull(ArrayUtils.nullToEmpty((byte[]) null));
        assertNotNull(ArrayUtils.nullToEmpty((char[]) null));
        assertNotNull(ArrayUtils.nullToEmpty((double[]) null));
        assertNotNull(ArrayUtils.nullToEmpty((float[]) null));
        assertNotNull(ArrayUtils.nullToEmpty((int[]) null));
        assertNotNull(ArrayUtils.nullToEmpty((long[]) null));
        assertNotNull(ArrayUtils.nullToEmpty((short[]) null));

        String[] original = new String[]{"a"};
        assertSame(original, ArrayUtils.nullToEmpty(original));
    }

    @Test
    public void testToArray() {
        String[] result = ArrayUtils.toArray("a", "b", "c");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }
}