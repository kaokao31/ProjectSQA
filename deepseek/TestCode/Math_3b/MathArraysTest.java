package org.apache.commons.math3.util;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for MathArrays utility class.
 * Targets maximum line/branch coverage and fault detection.
 */
public class MathArraysTest {

    private double[] testArray1;
    private double[] testArray2;
    private double[] testArray3;
    private double[] testArray4;
    private double[] testArray5;
    private double[] testArray6;

    @Before
    public void setUp() {
        testArray1 = new double[] {1.0, 2.0, 3.0, 4.0, 5.0};
        testArray2 = new double[] {5.0, 4.0, 3.0, 2.0, 1.0};
        testArray3 = new double[] {Double.NaN, 2.0, 3.0};
        testArray4 = new double[] {Double.POSITIVE_INFINITY, 2.0, 3.0};
        testArray5 = new double[] {1.0, 2.0, Double.NEGATIVE_INFINITY};
        testArray6 = new double[] {};
    }

    // ========== checkFinite ==========
    @Test(expected = IllegalArgumentException.class)
    public void testCheckFiniteWithNaN() {
        MathArrays.checkFinite(testArray3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckFiniteWithPositiveInfinity() {
        MathArrays.checkFinite(testArray4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckFiniteWithNegativeInfinity() {
        MathArrays.checkFinite(testArray5);
    }

    @Test
    public void testCheckFiniteValid() {
        MathArrays.checkFinite(testArray1);
        // no exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckFiniteNull() {
        MathArrays.checkFinite(null);
    }

    // ========== checkNotNaN ==========
    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotNaNWithNaN() {
        MathArrays.checkNotNaN(testArray3);
    }

    @Test
    public void testCheckNotNaNValid() {
        MathArrays.checkNotNaN(testArray1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotNaNNull() {
        MathArrays.checkNotNaN(null);
    }

    // ========== ebeAdd ==========
    @Test
    public void testEbeAdd() {
        double[] result = MathArrays.ebeAdd(testArray1, testArray2);
        assertArrayEquals(new double[] {6.0, 6.0, 6.0, 6.0, 6.0}, result, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEbeAddDifferentLength() {
        MathArrays.ebeAdd(testArray1, new double[] {1.0, 2.0});
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEbeAddNullFirst() {
        MathArrays.ebeAdd(null, testArray1);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEbeAddNullSecond() {
        MathArrays.ebeAdd(testArray1, null);
    }

    // ========== ebeSubtract ==========
    @Test
    public void testEbeSubtract() {
        double[] result = MathArrays.ebeSubtract(testArray1, testArray2);
        assertArrayEquals(new double[] {-4.0, -2.0, 0.0, 2.0, 4.0}, result, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEbeSubtractDifferentLength() {
        MathArrays.ebeSubtract(testArray1, new double[] {1.0});
    }

    // ========== ebeMultiply ==========
    @Test
    public void testEbeMultiply() {
        double[] result = MathArrays.ebeMultiply(testArray1, testArray2);
        assertArrayEquals(new double[] {5.0, 8.0, 9.0, 8.0, 5.0}, result, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEbeMultiplyDifferentLength() {
        MathArrays.ebeMultiply(testArray1, new double[] {1.0, 2.0, 3.0});
    }

    // ========== ebeDivide ==========
    @Test
    public void testEbeDivide() {
        double[] result = MathArrays.ebeDivide(testArray1, testArray2);
        assertArrayEquals(new double[] {0.2, 0.5, 1.0, 2.0, 5.0}, result, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEbeDivideDifferentLength() {
        MathArrays.ebeDivide(testArray1, new double[] {1.0, 2.0});
    }

    // ========== distance1 ==========
    @Test
    public void testDistance1() {
        double dist = MathArrays.distance1(testArray1, testArray2);
        assertEquals(12.0, dist, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testDistance1DifferentLength() {
        MathArrays.distance1(testArray1, new double[] {1.0});
    }

    @Test
    public void testDistance1Empty() {
        double dist = MathArrays.distance1(testArray6, testArray6);
        assertEquals(0.0, dist, 1e-15);
    }

    // ========== distanceInf ==========
    @Test
    public void testDistanceInf() {
        double dist = MathArrays.distanceInf(testArray1, testArray2);
        assertEquals(4.0, dist, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testDistanceInfDifferentLength() {
        MathArrays.distanceInf(testArray1, new double[] {1.0, 2.0, 3.0, 4.0});
    }

    // ========== distance ==========
    @Test
    public void testDistance() {
        double dist = MathArrays.distance(testArray1, testArray2);
        assertEquals(Math.sqrt(40.0), dist, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testDistanceDifferentLength() {
        MathArrays.distance(testArray1, new double[] {1.0});
    }

    // ========== normalizeArray ==========
    @Test
    public void testNormalizeArray() {
        double[] normalized = MathArrays.normalizeArray(testArray1, 10.0);
        double sum = 0.0;
        for (double v : normalized) sum += v;
        assertEquals(10.0, sum, 1e-15);
        // check proportional
        assertArrayEquals(new double[] {10.0/15, 20.0/15, 30.0/15, 40.0/15, 50.0/15}, normalized, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayWithNaN() {
        MathArrays.normalizeArray(testArray3, 1.0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayWithInfinity() {
        MathArrays.normalizeArray(testArray4, 1.0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayZeroSum() {
        MathArrays.normalizeArray(new double[] {1.0, -1.0}, 1.0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayAllZero() {
        MathArrays.normalizeArray(new double[] {0.0, 0.0}, 1.0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayNull() {
        MathArrays.normalizeArray(null, 1.0);
    }

    // ========== scale ==========
    @Test
    public void testScale() {
        double[] scaled = MathArrays.scale(2.0, testArray1);
        assertArrayEquals(new double[] {2.0, 4.0, 6.0, 8.0, 10.0}, scaled, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testScaleNull() {
        MathArrays.scale(2.0, null);
    }

    // ========== copyOf ==========
    @Test
    public void testCopyOf() {
        double[] copy = MathArrays.copyOf(testArray1);
        assertArrayEquals(testArray1, copy, 1e-15);
        assertNotSame(testArray1, copy);
    }

    @Test
    public void testCopyOfWithLength() {
        double[] copy = MathArrays.copyOf(testArray1, 3);
        assertArrayEquals(new double[] {1.0, 2.0, 3.0}, copy, 1e-15);
    }

    @Test
    public void testCopyOfWithLongerLength() {
        double[] copy = MathArrays.copyOf(testArray1, 7);
        assertEquals(7, copy.length);
        assertArrayEquals(new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 0.0, 0.0}, copy, 1e-15);
    }

    // ========== sortInPlace ==========
    @Test
    public void testSortInPlace() {
        double[] x = {3.0, 1.0, 2.0};
        double[] y = {30.0, 10.0, 20.0};
        MathArrays.sortInPlace(x, y);
        assertArrayEquals(new double[] {1.0, 2.0, 3.0}, x, 1e-15);
        assertArrayEquals(new double[] {10.0, 20.0, 30.0}, y, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testSortInPlaceDifferentLength() {
        MathArrays.sortInPlace(new double[] {1.0, 2.0}, new double[] {1.0});
    }

    // ========== isMonotonic ==========
    @Test
    public void testIsMonotonicIncreasing() {
        assertTrue(MathArrays.isMonotonic(testArray1, MathArrays.OrderDirection.INCREASING, false));
    }

    @Test
    public void testIsMonotonicDecreasing() {
        assertTrue(MathArrays.isMonotonic(testArray2, MathArrays.OrderDirection.DECREASING, false));
    }

    @Test
    public void testIsMonotonicNotStrict() {
        double[] nonStrict = {1.0, 2.0, 2.0, 3.0};
        assertTrue(MathArrays.isMonotonic(nonStrict, MathArrays.OrderDirection.INCREASING, false));
        assertFalse(MathArrays.isMonotonic(nonStrict, MathArrays.OrderDirection.INCREASING, true));
    }

    @Test
    public void testIsMonotonicEmpty() {
        assertTrue(MathArrays.isMonotonic(testArray6, MathArrays.OrderDirection.INCREASING, false));
        assertTrue(MathArrays.isMonotonic(testArray6, MathArrays.OrderDirection.INCREASING, true));
    }

    @Test
    public void testIsMonotonicSingle() {
        assertTrue(MathArrays.isMonotonic(new double[] {1.0}, MathArrays.OrderDirection.INCREASING, false));
        assertTrue(MathArrays.isMonotonic(new double[] {1.0}, MathArrays.OrderDirection.INCREASING, true));
    }

    // ========== equals (double[], double[]) ==========
    @Test
    public void testEquals() {
        assertTrue(MathArrays.equals(testArray1, new double[] {1.0, 2.0, 3.0, 4.0, 5.0}));
        assertFalse(MathArrays.equals(testArray1, testArray2));
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse(MathArrays.equals(null, testArray1));
        assertFalse(MathArrays.equals(testArray1, null));
        assertTrue(MathArrays.equals(null, null));
    }

    @Test
    public void testEqualsWithTolerance() {
        assertTrue(MathArrays.equals(testArray1, new double[] {1.0, 2.0, 3.0, 4.0, 5.0}, 1e-10));
        assertFalse(MathArrays.equals(testArray1, new double[] {1.0, 2.0, 3.0, 4.0, 5.1}, 1e-10));
    }

    // ========== checkOrder ==========
    @Test(expected = MathIllegalArgumentException.class)
    public void testCheckOrderIncreasingFail() {
        MathArrays.checkOrder(new double[] {1.0, 3.0, 2.0});
    }

    @Test
    public void testCheckOrderIncreasingPass() {
        MathArrays.checkOrder(testArray1);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testCheckOrderDecreasingFail() {
        MathArrays.checkOrder(new double[] {3.0, 2.0, 4.0}, MathArrays.OrderDirection.DECREASING, false, false);
    }

    @Test
    public void testCheckOrderDecreasingPass() {
        MathArrays.checkOrder(testArray2, MathArrays.OrderDirection.DECREASING, false, false);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testCheckOrderStrictIncreasingFail() {
        MathArrays.checkOrder(new double[] {1.0, 2.0, 2.0}, MathArrays.OrderDirection.INCREASING, true, false);
    }

    @Test
    public void testCheckOrderNonStrictIncreasingPass() {
        MathArrays.checkOrder(new double[] {1.0, 2.0, 2.0}, MathArrays.OrderDirection.INCREASING, false, false);
    }

    // ========== buildArray ==========
    @Test
    public void testBuildArray() {
        double[] arr = MathArrays.buildArray(5, 3.14);
        assertEquals(5, arr.length);
        for (double v : arr) assertEquals(3.14, v, 1e-15);
    }

    @Test(expected = NegativeArraySizeException.class)
    public void testBuildArrayNegativeSize() {
        MathArrays.buildArray(-1, 0.0);
    }

    // ========== convolve ==========
    @Test
    public void testConvolve() {
        double[] x = {1.0, 2.0, 3.0};
        double[] h = {0.0, 1.0, 0.5};
        double[] result = MathArrays.convolve(x, h);
        double[] expected = {0.0, 1.0, 2.5, 4.0, 1.5};
        assertArrayEquals(expected, result, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConvolveNullFirst() {
        MathArrays.convolve(null, new double[] {1.0});
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConvolveNullSecond() {
        MathArrays.convolve(new double[] {1.0}, null);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConvolveEmptyFirst() {
        MathArrays.convolve(new double[] {}, new double[] {1.0});
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testConvolveEmptySecond() {
        MathArrays.convolve(new double[] {1.0}, new double[] {});
    }

    // ========== linearCombination ==========
    @Test
    public void testLinearCombination() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        double result = MathArrays.linearCombination(a, b);
        assertEquals(1.0*4.0 + 2.0*5.0 + 3.0*6.0, result, 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testLinearCombinationDifferentLength() {
        MathArrays.linearCombination(new double[] {1.0, 2.0}, new double[] {1.0});
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testLinearCombinationNullFirst() {
        MathArrays.linearCombination(null, new double[] {1.0});
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testLinearCombinationNullSecond() {
        MathArrays.linearCombination(new double[] {1.0}, null);
    }

    // ========== Additional edge cases ==========
    @Test
    public void testDistance1WithNaN() {
        double[] a = {1.0, Double.NaN};
        double[] b = {2.0, 3.0};
        double dist = MathArrays.distance1(a, b);
        assertTrue(Double.isNaN(dist));
    }

    @Test
    public void testDistanceInfWithInfinity() {
        double[] a = {Double.POSITIVE_INFINITY, 1.0};
        double[] b = {2.0, 3.0};
        double dist = MathArrays.distanceInf(a, b);
        assertTrue(Double.isInfinite(dist));
    }

    @Test
    public void testEbeMultiplyWithNaN() {
        double[] a = {1.0, Double.NaN};
        double[] b = {2.0, 3.0};
        double[] result = MathArrays.ebeMultiply(a, b);
        assertTrue(Double.isNaN(result[1]));
    }

    @Test
    public void testEbeDivideByZero() {
        double[] a = {1.0, 2.0};
        double[] b = {0.0, 1.0};
        double[] result = MathArrays.ebeDivide(a, b);
        assertTrue(Double.isInfinite(result[0]));
    }

    @Test
    public void testScaleWithNaN() {
        double[] scaled = MathArrays.scale(Double.NaN, testArray1);
        assertTrue(Double.isNaN(scaled[0]));
    }

    @Test
    public void testNormalizeArrayWithNegativeSum() {
        double[] arr = {-1.0, -2.0, -3.0};
        double[] normalized = MathArrays.normalizeArray(arr, 10.0);
        double sum = 0.0;
        for (double v : normalized) sum += v;
        assertEquals(10.0, sum, 1e-15);
        assertTrue(normalized[0] < 0);
    }
}