package org.mockito.internal.matchers;

import org.junit.Test;
import static org.junit.Assert.*;

public class EqualityTest {

    @Test
    public void testAreEqualNulls() {
        assertTrue(Equality.areEqual(null, null));
    }

    @Test
    public void testAreEqualFirstNull() {
        assertFalse(Equality.areEqual(null, "test"));
    }

    @Test
    public void testAreEqualSecondNull() {
        assertFalse(Equality.areEqual("test", null));
    }

    @Test
    public void testAreEqualSameInstance() {
        String s = "hello";
        assertTrue(Equality.areEqual(s, s));
    }

    @Test
    public void testAreEqualEqualObjects() {
        assertTrue(Equality.areEqual(new String("hello"), new String("hello")));
    }

    @Test
    public void testAreEqualDifferentObjects() {
        assertFalse(Equality.areEqual("hello", "world"));
    }

    @Test
    public void testAreEqualPrimitiveArraysSame() {
        int[] arr1 = {1, 2, 3};
        int[] arr2 = {1, 2, 3};
        assertTrue(Equality.areEqual(arr1, arr2));
    }

    @Test
    public void testAreEqualPrimitiveArraysDifferent() {
        int[] arr1 = {1, 2, 3};
        int[] arr2 = {1, 2, 4};
        assertFalse(Equality.areEqual(arr1, arr2));
    }

    @Test
    public void testAreEqualDifferentArrayTypes() {
        int[] arr1 = {1, 2, 3};
        long[] arr2 = {1L, 2L, 3L};
        assertFalse(Equality.areEqual(arr1, arr2));
    }

    @Test
    public void testAreEqualObjectArraysSame() {
        String[] arr1 = {"a", "b"};
        String[] arr2 = {"a", "b"};
        assertTrue(Equality.areEqual(arr1, arr2));
    }

    @Test
    public void testAreEqualObjectArraysDifferent() {
        String[] arr1 = {"a", "b"};
        String[] arr2 = {"a", "c"};
        assertFalse(Equality.areEqual(arr1, arr2));
    }

    @Test
    public void testAreEqualNestedArrays() {
        int[][] arr1 = {{1, 2}, {3, 4}};
        int[][] arr2 = {{1, 2}, {3, 4}};
        assertTrue(Equality.areEqual(arr1, arr2));
    }

    @Test
    public void testAreEqualNestedArraysDifferent() {
        int[][] arr1 = {{1, 2}, {3, 4}};
        int[][] arr2 = {{1, 2}, {3, 5}};
        assertFalse(Equality.areEqual(arr1, arr2));
    }

    @Test
    public void testAreEqualBooleanArrays() {
        boolean[] arr1 = {true, false};
        boolean[] arr2 = {true, false};
        boolean[] arr3 = {true, true};
        assertTrue(Equality.areEqual(arr1, arr2));
        assertFalse(Equality.areEqual(arr1, arr3));
    }

    @Test
    public void testAreEqualByteArrays() {
        byte[] arr1 = {1, 2};
        byte[] arr2 = {1, 2};
        byte[] arr3 = {1, 3};
        assertTrue(Equality.areEqual(arr1, arr2));
        assertFalse(Equality.areEqual(arr1, arr3));
    }

    @Test
    public void testAreEqualCharArrays() {
        char[] arr1 = {'a', 'b'};
        char[] arr2 = {'a', 'b'};
        char[] arr3 = {'a', 'c'};
        assertTrue(Equality.areEqual(arr1, arr2));
        assertFalse(Equality.areEqual(arr1, arr3));
    }

    @Test
    public void testAreEqualDoubleArrays() {
        double[] arr1 = {1.0, 2.0};
        double[] arr2 = {1.0, 2.0};
        double[] arr3 = {1.0, 3.0};
        assertTrue(Equality.areEqual(arr1, arr2));
        assertFalse(Equality.areEqual(arr1, arr3));
    }

    @Test
    public void testAreEqualFloatArrays() {
        float[] arr1 = {1.0f, 2.0f};
        float[] arr2 = {1.0f, 2.0f};
        float[] arr3 = {1.0f, 3.0f};
        assertTrue(Equality.areEqual(arr1, arr2));
        assertFalse(Equality.areEqual(arr1, arr3));
    }

    @Test
    public void testAreEqualLongArrays() {
        long[] arr1 = {1L, 2L};
        long[] arr2 = {1L, 2L};
        long[] arr3 = {1L, 3L};
        assertTrue(Equality.areEqual(arr1, arr2));
        assertFalse(Equality.areEqual(arr1, arr3));
    }

    @Test
    public void testAreEqualShortArrays() {
        short[] arr1 = {1, 2};
        short[] arr2 = {1, 2};
        short[] arr3 = {1, 3};
        assertTrue(Equality.areEqual(arr1, arr2));
        assertFalse(Equality.areEqual(arr1, arr3));
    }

    @Test
    public void testAreEqualDifferentLengths() {
        int[] arr1 = {1, 2};
        int[] arr2 = {1, 2, 3};
        assertFalse(Equality.areEqual(arr1, arr2));
    }
}