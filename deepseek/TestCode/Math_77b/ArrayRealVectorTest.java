package org.apache.commons.math.linear;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for ArrayRealVector, targeting bug Math-77.
 * Focuses on getL1Distance and getLInfDistance with NaN/Infinity.
 */
public class ArrayRealVectorTest {

    private static final double EPS = 1e-14;

    private double[] data1;
    private double[] data2;
    private double[] dataNaN;
    private double[] dataInf;
    private double[] dataMixed;

    @Before
    public void setUp() {
        data1 = new double[] {1.0, 2.0, 3.0, 4.0};
        data2 = new double[] {5.0, 6.0, 7.0, 8.0};
        dataNaN = new double[] {Double.NaN, 1.0, 2.0};
        dataInf = new double[] {Double.POSITIVE_INFINITY, 1.0, 2.0};
        dataMixed = new double[] {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.0};
    }

    // ========== Basic construction and dimension ==========

    @Test
    public void testConstructorFromArray() {
        ArrayRealVector v = new ArrayRealVector(data1);
        assertEquals(4, v.getDimension());
        assertEquals(1.0, v.getEntry(0), EPS);
        assertEquals(4.0, v.getEntry(3), EPS);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArray() {
        new ArrayRealVector((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyArray() {
        new ArrayRealVector(new double[0]);
    }

    // ========== getEntry / setEntry ==========

    @Test
    public void testGetEntry() {
        ArrayRealVector v = new ArrayRealVector(data1);
        assertEquals(2.0, v.getEntry(1), EPS);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetEntryNegativeIndex() {
        ArrayRealVector v = new ArrayRealVector(data1);
        v.getEntry(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetEntryOutOfBounds() {
        ArrayRealVector v = new ArrayRealVector(data1);
        v.getEntry(4);
    }

    @Test
    public void testSetEntry() {
        ArrayRealVector v = new ArrayRealVector(data1);
        v.setEntry(2, 99.0);
        assertEquals(99.0, v.getEntry(2), EPS);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetEntryNegativeIndex() {
        ArrayRealVector v = new ArrayRealVector(data1);
        v.setEntry(-1, 0.0);
    }

    // ========== getL1Distance ==========

    @Test
    public void testGetL1DistanceNormal() {
        ArrayRealVector v1 = new ArrayRealVector(data1);
        ArrayRealVector v2 = new ArrayRealVector(data2);
        // |1-5| + |2-6| + |3-7| + |4-8| = 4+4+4+4 = 16
        assertEquals(16.0, v1.getL1Distance(v2), EPS);
    }

    @Test
    public void testGetL1DistanceWithNaN() {
        ArrayRealVector vNaN = new ArrayRealVector(dataNaN);
        ArrayRealVector v = new ArrayRealVector(new double[] {0.0, 0.0, 0.0});
        double result = vNaN.getL1Distance(v);
        // Expected: NaN because first component difference is NaN
        assertTrue(Double.isNaN(result));
    }

    @Test
    public void testGetL1DistanceWithInfinity() {
        ArrayRealVector vInf = new ArrayRealVector(dataInf);
        ArrayRealVector v = new ArrayRealVector(new double[] {0.0, 0.0, 0.0});
        double result = vInf.getL1Distance(v);
        // |Inf - 0| = Inf, plus |1-0| + |2-0| = 1+2 = 3 => Inf
        assertTrue(Double.isInfinite(result));
        assertTrue(result > 0);
    }

    @Test
    public void testGetL1DistanceMixedNaNInf() {
        ArrayRealVector vMixed = new ArrayRealVector(dataMixed);
        ArrayRealVector v = new ArrayRealVector(new double[] {0.0, 0.0, 0.0, 0.0});
        double result = vMixed.getL1Distance(v);
        // Contains NaN => overall NaN
        assertTrue(Double.isNaN(result));
    }

    @Test(expected = NullPointerException.class)
    public void testGetL1DistanceNull() {
        ArrayRealVector v = new ArrayRealVector(data1);
        v.getL1Distance(null);
    }

    @Test
    public void testGetL1DistanceDifferentDimension() {
        ArrayRealVector v1 = new ArrayRealVector(data1);
        ArrayRealVector v2 = new ArrayRealVector(new double[] {1.0, 2.0});
        try {
            v1.getL1Distance(v2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ========== getLInfDistance ==========

    @Test
    public void testGetLInfDistanceNormal() {
        ArrayRealVector v1 = new ArrayRealVector(data1);
        ArrayRealVector v2 = new ArrayRealVector(data2);
        // max(|1-5|, |2-6|, |3-7|, |4-8|) = 4
        assertEquals(4.0, v1.getLInfDistance(v2), EPS);
    }

    @Test
    public void testGetLInfDistanceWithNaN() {
        ArrayRealVector vNaN = new ArrayRealVector(dataNaN);
        ArrayRealVector v = new ArrayRealVector(new double[] {0.0, 0.0, 0.0});
        double result = vNaN.getLInfDistance(v);
        assertTrue(Double.isNaN(result));
    }

    @Test
    public void testGetLInfDistanceWithInfinity() {
        ArrayRealVector vInf = new ArrayRealVector(dataInf);
        ArrayRealVector v = new ArrayRealVector(new double[] {0.0, 0.0, 0.0});
        double result = vInf.getLInfDistance(v);
        // max(|Inf-0|, |1-0|, |2-0|) = Inf
        assertTrue(Double.isInfinite(result));
        assertTrue(result > 0);
    }

    @Test
    public void testGetLInfDistanceMixedNaNInf() {
        ArrayRealVector vMixed = new ArrayRealVector(dataMixed);
        ArrayRealVector v = new ArrayRealVector(new double[] {0.0, 0.0, 0.0, 0.0});
        double result = vMixed.getLInfDistance(v);
        assertTrue(Double.isNaN(result));
    }

    @Test(expected = NullPointerException.class)
    public void testGetLInfDistanceNull() {
        ArrayRealVector v = new ArrayRealVector(data1);
        v.getLInfDistance(null);
    }

    @Test
    public void testGetLInfDistanceDifferentDimension() {
        ArrayRealVector v1 = new ArrayRealVector(data1);
        ArrayRealVector v2 = new ArrayRealVector(new double[] {1.0, 2.0});
        try {
            v1.getLInfDistance(v2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ========== getNorm, getL1Norm, getLInfNorm ==========

    @Test
    public void testGetNorm() {
        ArrayRealVector v = new ArrayRealVector(new double[] {3.0, 4.0});
        assertEquals(5.0, v.getNorm(), EPS);
    }

    @Test
    public void testGetL1Norm() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, -2.0, 3.0});
        assertEquals(6.0, v.getL1Norm(), EPS);
    }

    @Test
    public void testGetLInfNorm() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, -2.0, 3.0});
        assertEquals(3.0, v.getLInfNorm(), EPS);
    }

    @Test
    public void testGetNormWithNaN() {
        ArrayRealVector v = new ArrayRealVector(new double[] {Double.NaN, 1.0});
        assertTrue(Double.isNaN(v.getNorm()));
    }

    @Test
    public void testGetL1NormWithNaN() {
        ArrayRealVector v = new ArrayRealVector(new double[] {Double.NaN, 1.0});
        assertTrue(Double.isNaN(v.getL1Norm()));
    }

    @Test
    public void testGetLInfNormWithNaN() {
        ArrayRealVector v = new ArrayRealVector(new double[] {Double.NaN, 1.0});
        assertTrue(Double.isNaN(v.getLInfNorm()));
    }

    // ========== getDistance ==========

    @Test
    public void testGetDistanceNormal() {
        ArrayRealVector v1 = new ArrayRealVector(data1);
        ArrayRealVector v2 = new ArrayRealVector(data2);
        // sqrt((1-5)^2 + (2-6)^2 + (3-7)^2 + (4-8)^2) = sqrt(16+16+16+16) = 8
        assertEquals(8.0, v1.getDistance(v2), EPS);
    }

    @Test
    public void testGetDistanceWithNaN() {
        ArrayRealVector vNaN = new ArrayRealVector(dataNaN);
        ArrayRealVector v = new ArrayRealVector(new double[] {0.0, 0.0, 0.0});
        assertTrue(Double.isNaN(vNaN.getDistance(v)));
    }

    // ========== Edge cases: zero vector ==========

    @Test
    public void testZeroVector() {
        ArrayRealVector zero = new ArrayRealVector(new double[] {0.0, 0.0, 0.0});
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, 2.0, 3.0});
        assertEquals(6.0, zero.getL1Distance(v), EPS);
        assertEquals(3.0, zero.getLInfDistance(v), EPS);
        assertEquals(0.0, zero.getNorm(), EPS);
        assertEquals(0.0, zero.getL1Norm(), EPS);
        assertEquals(0.0, zero.getLInfNorm(), EPS);
    }

    // ========== Negative values ==========

    @Test
    public void testNegativeValues() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {-1.0, -2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {3.0, 4.0});
        // | -1-3 | + | -2-4 | = 4+6 = 10
        assertEquals(10.0, v1.getL1Distance(v2), EPS);
        // max(|-1-3|, |-2-4|) = max(4,6) = 6
        assertEquals(6.0, v1.getLInfDistance(v2), EPS);
    }

    // ========== Large values to test overflow? ==========

    @Test
    public void testLargeValues() {
        double big = 1e200;
        double small = 1e-200;
        ArrayRealVector v1 = new ArrayRealVector(new double[] {big, small});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {0.0, 0.0});
        assertEquals(big + small, v1.getL1Distance(v2), EPS);
        assertEquals(big, v1.getLInfDistance(v2), EPS);
    }

    // ========== Test that distance methods are symmetric ==========

    @Test
    public void testDistanceSymmetry() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {4.0, 5.0, 6.0});
        assertEquals(v1.getL1Distance(v2), v2.getL1Distance(v1), EPS);
        assertEquals(v1.getLInfDistance(v2), v2.getLInfDistance(v1), EPS);
        assertEquals(v1.getDistance(v2), v2.getDistance(v1), EPS);
    }

    // ========== Test with RealVector interface ==========

    @Test
    public void testDistanceWithRealVector() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0});
        RealVector v2 = new ArrayRealVector(new double[] {3.0, 4.0});
        assertEquals(4.0, v1.getL1Distance(v2), EPS);
        assertEquals(2.0, v1.getLInfDistance(v2), EPS);
    }
}