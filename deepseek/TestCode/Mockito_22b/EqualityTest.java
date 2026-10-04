package org.mockito.internal.matchers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * JUnit 4 test suite for the Equality class from Mockito.
 * Designed to achieve high coverage and detect potential faults,
 * especially those related to array equality (Bug ID: 22).
 */
public class EqualityTest {

    // ----- Basic equality cases -----

    @Test
    public void testSameObjectReference() {
        Object obj = new Object();
        assertTrue("Same reference should be equal", Equality.areEqual(obj, obj));
    }

    @Test
    public void testBothNull() {
        assertTrue("Both null should be equal", Equality.areEqual(null, null));
    }

    @Test
    public void testFirstNullSecondNonNull() {
        assertFalse("First null, second non-null should not be equal", Equality.areEqual(null, "x"));
    }

    @Test
    public void testFirstNonNullSecondNull() {
        assertFalse("First non-null, second null should not be equal", Equality.areEqual("x", null));
    }

    @Test
    public void testEqualStrings() {
        assertTrue("Equal strings should be equal", Equality.areEqual("hello", "hello"));
    }

    @Test
    public void testDifferentStrings() {
        assertFalse("Different strings should not be equal", Equality.areEqual("hello", "world"));
    }

    @Test
    public void testEqualIntegers() {
        assertTrue("Equal integers should be equal", Equality.areEqual(42, 42));
    }

    @Test
    public void testDifferentIntegers() {
        assertFalse("Different integers should not be equal", Equality.areEqual(42, 43));
    }

    @Test
    public void testIntegerAndLong() {
        // Same numeric value, different types
        assertFalse("Integer and Long should not be equal", Equality.areEqual(42, 42L));
    }

    @Test
    public void testDoubleEqual() {
        assertTrue("Equal doubles should be equal", Equality.areEqual(3.14, 3.14));
    }

    @Test
    public void testDoubleNaN() {
        assertTrue("NaN should be equal to NaN", Equality.areEqual(Double.NaN, Double.NaN));
    }

    @Test
    public void testDoublePositiveZeroNegativeZero() {
        // According to equals contract, 0.0 != -0.0
        assertFalse("Positive zero and negative zero should not be equal", Equality.areEqual(0.0, -0.0));
    }

    // ----- Array equality cases (critical for Bug 22) -----

    @Test
    public void testBothNullArrays() {
        assertTrue("Null arrays should be equal", Equality.areEqual((int[]) null, (int[]) null));
    }

    @Test
    public void testNullArrayWithNonNullArray() {
        assertFalse("Null vs non-null array should not be equal", Equality.areEqual((int[]) null, new int[]{1}));
    }

    @Test
    public void testObjectArrayWithNull() {
        assertFalse("Array vs null should not be equal", Equality.areEqual(new Object[]{"a"}, null));
    }

    @Test
    public void testSameIntArrayReference() {
        int[] arr = {1, 2, 3};
        assertTrue("Same int array reference should be equal", Equality.areEqual(arr, arr));
    }

    @Test
    public void testEqualIntArrays() {
        assertTrue("Equal int arrays should be equal", Equality.areEqual(new int[]{1, 2, 3}, new int[]{1, 2, 3}));
    }

    @Test
    public void testDifferentIntArrays() {
        assertFalse("Different int arrays should not be equal", Equality.areEqual(new int[]{1, 2, 3}, new int[]{1, 2, 4}));
    }

    @Test
    public void testIntArraysDifferentLength() {
        assertFalse("Int arrays with different lengths should not be equal", Equality.areEqual(new int[]{1, 2}, new int[]{1, 2, 3}));
    }

    @Test
    public void testEqualStringArrays() {
        assertTrue("Equal String arrays should be equal", Equality.areEqual(new String[]{"a", "b"}, new String[]{"a", "b"}));
    }

    @Test
    public void testDifferentStringArrays() {
        assertFalse("Different String arrays should not be equal", Equality.areEqual(new String[]{"a", "b"}, new String[]{"a", "c"}));
    }

    @Test
    public void testPrimitiveBooleanArrays() {
        assertTrue("Equal boolean arrays should be equal", Equality.areEqual(new boolean[]{true, false}, new boolean[]{true, false}));
        assertFalse("Different boolean arrays should not be equal", Equality.areEqual(new boolean[]{true}, new boolean[]{false}));
    }

    @Test
    public void testPrimitiveByteArrays() {
        assertTrue("Equal byte arrays should be equal", Equality.areEqual(new byte[]{1, 2}, new byte[]{1, 2}));
        assertFalse("Different byte arrays should not be equal", Equality.areEqual(new byte[]{1}, new byte[]{2}));
    }

    @Test
    public void testPrimitiveCharArray() {
        assertTrue("Equal char arrays should be equal", Equality.areEqual(new char[]{'a', 'b'}, new char[]{'a', 'b'}));
        assertFalse("Different char arrays should not be equal", Equality.areEqual(new char[]{'a'}, new char[]{'b'}));
    }

    @Test
    public void testPrimitiveDoubleArray() {
        assertTrue("Equal double arrays should be equal", Equality.areEqual(new double[]{1.0, 2.0}, new double[]{1.0, 2.0}));
        assertFalse("Different double arrays should not be equal", Equality.areEqual(new double[]{1.0}, new double[]{2.0}));
    }

    @Test
    public void testPrimitiveFloatArray() {
        assertTrue("Equal float arrays should be equal", Equality.areEqual(new float[]{1.0f, 2.0f}, new float[]{1.0f, 2.0f}));
        assertFalse("Different float arrays should not be equal", Equality.areEqual(new float[]{1.0f}, new float[]{2.0f}));
    }

    @Test
    public void testPrimitiveLongArray() {
        assertTrue("Equal long arrays should be equal", Equality.areEqual(new long[]{1L, 2L}, new long[]{1L, 2L}));
        assertFalse("Different long arrays should not be equal", Equality.areEqual(new long[]{1L}, new long[]{2L}));
    }

    @Test
    public void testPrimitiveShortArray() {
        assertTrue("Equal short arrays should be equal", Equality.areEqual(new short[]{1, 2}, new short[]{1, 2}));
        assertFalse("Different short arrays should not be equal", Equality.areEqual(new short[]{1}, new short[]{2}));
    }

    @Test
    public void testMixedArrayTypes() {
        // One is int[], other is Integer[] (object array)
        assertFalse("int[] and Integer[] should not be equal", Equality.areEqual(new int[]{1}, new Integer[]{1}));
    }

    @Test
    public void testMultidimensionalObjectArray() {
        // Two-level object array
        Object[] a = new Object[]{1, new Object[]{"inner"}};
        Object[] b = new Object[]{1, new Object[]{"inner"}};
        assertTrue("Equal nested object arrays should be equal", Equality.areEqual(a, b));
    }

    @Test
    public void testMultidimensionalObjectArrayMismatch() {
        Object[] a = new Object[]{1, new Object[]{"inner"}};
        Object[] b = new Object[]{1, new Object[]{"other"}};
        assertFalse("Different nested object arrays should not be equal", Equality.areEqual(a, b));
    }

    @Test
    public void testMultidimensionalPrimitiveArray() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 4}};
        assertTrue("Equal 2D int arrays should be equal", Equality.areEqual(a, b));
    }

    @Test
    public void testMultidimensionalPrimitiveArrayMismatch() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{1, 2}, {3, 5}};
        assertFalse("Different 2D int arrays should not be equal", Equality.areEqual(a, b));
    }

    @Test
    public void testEmptyArray() {
        assertTrue("Empty int arrays should be equal", Equality.areEqual(new int[]{}, new int[]{}));
        assertTrue("Empty String arrays should be equal", Equality.areEqual(new String[]{}, new String[]{}));
        assertFalse("Empty vs non-empty should not be equal", Equality.areEqual(new int[]{}, new int[]{1}));
    }

    // ----- Non-array edge cases -----

    @Test
    public void testClassEquality() {
        assertTrue("Same class should be equal", Equality.areEqual(String.class, String.class));
        assertFalse("Different classes should not be equal", Equality.areEqual(String.class, Integer.class));
    }

    @Test
    public void testCustomObject() {
        Object a = new Object();
        Object b = new Object();
        assertFalse("Different objects should not be equal unless they implement equals", Equality.areEqual(a, b));
    }

    @Test
    public void testCustomObjectSameEquals() {
        // Using a simple wrapper that compares value
        Value v1 = new Value(5);
        Value v2 = new Value(5);
        assertTrue("Objects with equal values should be equal", Equality.areEqual(v1, v2));
    }

    @Test
    public void testCustomObjectDifferentEquals() {
        Value v1 = new Value(5);
        Value v2 = new Value(10);
        assertFalse("Objects with different values should not be equal", Equality.areEqual(v1, v2));
    }

    // Helper class for custom equality
    static class Value {
        private final int x;
        Value(int x) { this.x = x; }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Value value = (Value) obj;
            return x == value.x;
        }
        @Override
        public int hashCode() { return x; }
    }
}