package org.apache.commons.math3.linear;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for OpenMapRealVector.
 * Targets maximal line/branch coverage and fault detection (Math-29 related).
 */
public class OpenMapRealVectorTest {

    private static final double EPSILON = 1e-12;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        OpenMapRealVector v = new OpenMapRealVector();
        assertEquals(0, v.getDimension());
    }

    @Test(expected = NegativeArraySizeException.class)
    public void testConstructorNegativeDimension() {
        new OpenMapRealVector(-1);
    }

    @Test
    public void testConstructorDimension() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        assertEquals(5, v.getDimension());
        for (int i = 0; i < 5; i++) {
            assertEquals(0.0, v.getEntry(i), EPSILON);
        }
    }

    @Test
    public void testConstructorDimensionAndInitialValue() {
        OpenMapRealVector v = new OpenMapRealVector(3, 2.0);
        assertEquals(3, v.getDimension());
        for (int i = 0; i < 3; i++) {
            assertEquals(2.0, v.getEntry(i), EPSILON);
        }
    }

    @Test
    public void testConstructorFromDoubleArray() {
        double[] data = {1.0, 0.0, -3.0, 5.0};
        OpenMapRealVector v = new OpenMapRealVector(data);
        assertEquals(4, v.getDimension());
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], v.getEntry(i), EPSILON);
        }
    }

    @Test
    public void testConstructorFromOpenMapRealVector() {
        OpenMapRealVector original = new OpenMapRealVector(new double[]{2.0, 0.0, 4.0});
        OpenMapRealVector copy = new OpenMapRealVector(original);
        assertEquals(original.getDimension(), copy.getDimension());
        for (int i = 0; i < original.getDimension(); i++) {
            assertEquals(original.getEntry(i), copy.getEntry(i), EPSILON);
        }
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorNullArray() {
        new OpenMapRealVector((double[]) null);
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorNullVector() {
        new OpenMapRealVector((OpenMapRealVector) null);
    }

    // -----------------------------------------------------------------------
    // Basic getters/setters
    // -----------------------------------------------------------------------

    @Test
    public void testSetEntry() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(2, -3.5);
        assertEquals(-3.5, v.getEntry(2), EPSILON);
        assertEquals(0.0, v.getEntry(0), EPSILON); // unchanged
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetEntryOutOfRangeNegative() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(-1, 1.0);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetEntryOutOfRangeTooLarge() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(3, 1.0);
    }

    @Test
    public void testSetEntryZero() {
        OpenMapRealVector v = new OpenMapRealVector(4);
        v.setEntry(1, 5.0);
        v.setEntry(1, 0.0); // should remove entry
        assertEquals(0.0, v.getEntry(1), EPSILON);
        // ensure the internal map does not store zero
        // we cannot access private field, but getEntry behaves correctly
    }

    @Test
    public void testSet() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.set(7.0);
        for (int i = 0; i < 3; i++) {
            assertEquals(7.0, v.getEntry(i), EPSILON);
        }
    }

    @Test
    public void testSetZeroDimension() {
        OpenMapRealVector v = new OpenMapRealVector(0);
        v.set(1.0); // no-op, should not throw
        assertEquals(0, v.getDimension());
    }

    // -----------------------------------------------------------------------
    // Arithmetic operations
    // -----------------------------------------------------------------------

    @Test
    public void testAdd() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{4.0, 5.0, 0.0});
        OpenMapRealVector sum = v1.add(v2);
        assertEquals(3, sum.getDimension());
        assertEquals(5.0, sum.getEntry(0), EPSILON);
        assertEquals(5.0, sum.getEntry(1), EPSILON);
        assertEquals(3.0, sum.getEntry(2), EPSILON);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v1.add(v2);
    }

    @Test
    public void testAddWithZeroEntries() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{0.0, 2.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 0.0, 0.0});
        OpenMapRealVector sum = v1.add(v2);
        assertEquals(0.0, sum.getEntry(0), EPSILON);
        assertEquals(2.0, sum.getEntry(1), EPSILON);
        assertEquals(0.0, sum.getEntry(2), EPSILON);
    }

    @Test
    public void testAddToSelf() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, -2.0, 3.0});
        v.addToEntry(1, 5.0);
        assertEquals(1.0, v.getEntry(0), EPSILON);
        assertEquals(3.0, v.getEntry(1), EPSILON);
        assertEquals(3.0, v.getEntry(2), EPSILON);
    }

    @Test
    public void testSubtract() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{5.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 2.0, 0.0});
        OpenMapRealVector diff = v1.subtract(v2);
        assertEquals(4.0, diff.getEntry(0), EPSILON);
        assertEquals(-2.0, diff.getEntry(1), EPSILON);
        assertEquals(2.0, diff.getEntry(2), EPSILON);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testSubtractDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.subtract(v2);
    }

    @Test
    public void testEbeMultiply() {
        // Basic case
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{2.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 5.0, 1.0});
        OpenMapRealVector product = (OpenMapRealVector) v1.ebeMultiply(v2);
        assertEquals(3, product.getDimension());
        assertEquals(0.0, product.getEntry(0), EPSILON); // 2*0
        assertEquals(0.0, product.getEntry(1), EPSILON); // 0*5
        assertEquals(3.0, product.getEntry(2), EPSILON); // 3*1
    }

    @Test
    public void testEbeMultiplyWithDenseVector() {
        // Multiply sparse with dense RealVector (ArrayRealVector)
        OpenMapRealVector sparse = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        ArrayRealVector dense = new ArrayRealVector(new double[]{3.0, 4.0, 0.0});
        RealVector product = sparse.ebeMultiply(dense);
        assertEquals(3, product.getDimension());
        assertEquals(3.0, product.getEntry(0), EPSILON);
        assertEquals(0.0, product.getEntry(1), EPSILON);
        assertEquals(0.0, product.getEntry(2), EPSILON);
    }

    @Test
    public void testEbeMultiplyWithNaN() {
        // Potential fault: If argument has a zero entry, product should be zero, not NaN.
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 1.0});
        OpenMapRealVector product = (OpenMapRealVector) v1.ebeMultiply(v2);
        assertEquals(0.0, product.getEntry(0), EPSILON); // 1*0
        assertEquals(0.0, product.getEntry(1), EPSILON); // 0*1
    }

    @Test
    public void testEbeMultiplyWithInfinity() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{Double.POSITIVE_INFINITY, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0});
        OpenMapRealVector product = (OpenMapRealVector) v1.ebeMultiply(v2);
        assertEquals(0.0, product.getEntry(0), EPSILON);
        assertEquals(6.0, product.getEntry(1), EPSILON);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEbeMultiplyDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.ebeMultiply(v2);
    }

    @Test
    public void testEbeDivide() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{6.0, 0.0, 4.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{2.0, 3.0, 1.0});
        OpenMapRealVector quotient = (OpenMapRealVector) v1.ebeDivide(v2);
        assertEquals(3, quotient.getDimension());
        assertEquals(3.0, quotient.getEntry(0), EPSILON);
        assertEquals(0.0, quotient.getEntry(1), EPSILON); // 0/3
        assertEquals(4.0, quotient.getEntry(2), EPSILON);
    }

    @Test
    public void testEbeDivideByZero() {
        // Division by zero should yield Infinity or NaN depending on numerator
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{2.0, 0.0, -1.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 0.0, 0.0});
        OpenMapRealVector quotient = (OpenMapRealVector) v1.ebeDivide(v2);
        assertEquals(2.0/0.0, quotient.getEntry(0), EPSILON); // Infinity
        assertEquals(0.0/0.0, quotient.getEntry(1), EPSILON); // NaN
        assertEquals(-1.0/0.0, quotient.getEntry(2), EPSILON); // -Infinity
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testEbeDivideDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.ebeDivide(v2);
    }

    @Test
    public void testDotProduct() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{4.0, 5.0, 6.0});
        double dot = v1.dotProduct(v2);
        assertEquals(1*4 + 2*5 + 3*6, dot, EPSILON);
    }

    @Test
    public void testDotProductWithSparseAndZero() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{0.0, 2.0, 0.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3.0, 0.0, 4.0});
        double dot = v1.dotProduct(v2);
        assertEquals(0.0, dot, EPSILON);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testDotProductDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.dotProduct(v2);
    }

    // -----------------------------------------------------------------------
    // Norm and distance
    // -----------------------------------------------------------------------

    @Test
    public void testGetNorm() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{3.0, 0.0, 4.0});
        assertEquals(5.0, v.getNorm(), EPSILON);
    }

    @Test
    public void testGetNormZero() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        assertEquals(0.0, v.getNorm(), EPSILON);
    }

    @Test
    public void testGetSparseNorm() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{-2.0, 0.0, 3.0});
        assertEquals(5.0, v.getL1Norm(), EPSILON);
    }

    @Test
    public void testGetLInfNorm() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{-7.0, 0.0, 3.0});
        assertEquals(7.0, v.getLInfNorm(), EPSILON);
    }

    @Test
    public void testDistance() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{4.0, 0.0, 6.0});
        double dist = v1.distance(v2);
        assertEquals(5.0, dist, EPSILON); // sqrt(3^2+0+4^2)
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testDistanceMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.distance(v2);
    }

    @Test
    public void testGetSparseIterator() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 5.0, 0.0, -1.0});
        RealVector.Entry e;
        int count = 0;
        double[] expectedValues = {5.0, -1.0};
        int[] expectedIndices = {1, 3};
        RealVector.SparseEntryIterator it = v.sparseIterator();
        while (it.hasNext()) {
            e = it.next();
            assertEquals(expectedIndices[count], e.getIndex());
            assertEquals(expectedValues[count], e.getValue(), EPSILON);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testSparseIteratorEmpty() {
        OpenMapRealVector v = new OpenMapRealVector(10);
        RealVector.SparseEntryIterator it = v.sparseIterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testSparseIteratorModification() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 2.0, 0.0});
        RealVector.SparseEntryIterator it = v.sparseIterator();
        assertTrue(it.hasNext());
        RealVector.Entry e = it.next();
        assertEquals(1, e.getIndex());
        assertEquals(2.0, e.getValue(), EPSILON);
        // modify via setValue
        e.setValue(7.0);
        assertEquals(7.0, v.getEntry(1), EPSILON);
        // try to remove? Not supported, but we can test next again
        e = it.next();
        assertNull(e); // no more elements
    }

    // -----------------------------------------------------------------------
    // Map functions
    // -----------------------------------------------------------------------

    @Test
    public void testMapAdd() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, -2.0});
        v.mapAdd(3.0);
        assertEquals(4.0, v.getEntry(0), EPSILON);
        assertEquals(3.0, v.getEntry(1), EPSILON);
        assertEquals(1.0, v.getEntry(2), EPSILON);
    }

    @Test
    public void testMapAddToSelf() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, -2.0});
        v.mapAddToSelf(3.0);
        assertEquals(4.0, v.getEntry(0), EPSILON);
        assertEquals(3.0, v.getEntry(1), EPSILON);
        assertEquals(1.0, v.getEntry(2), EPSILON);
    }

    @Test
    public void testMapMultiply() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, -2.0});
        v.mapMultiply(2.0);
        assertEquals(2.0, v.getEntry(0), EPSILON);
        assertEquals(0.0, v.getEntry(1), EPSILON);
        assertEquals(-4.0, v.getEntry(2), EPSILON);
    }

    @Test
    public void testMapMultiplyToSelf() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, -2.0});
        v.mapMultiplyToSelf(2.0);
        assertEquals(2.0, v.getEntry(0), EPSILON);
        assertEquals(0.0, v.getEntry(1), EPSILON);
        assertEquals(-4.0, v.getEntry(2), EPSILON);
    }

    @Test
    public void testMapSubtract() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, -2.0});
        v.mapSubtract(2.0);
        assertEquals(-1.0, v.getEntry(0), EPSILON);
        assertEquals(-2.0, v.getEntry(1), EPSILON);
        assertEquals(-4.0, v.getEntry(2), EPSILON);
    }

    @Test
    public void testMapDivide() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{4.0, 0.0, -2.0});
        v.mapDivide(2.0);
        assertEquals(2.0, v.getEntry(0), EPSILON);
        assertEquals(0.0, v.getEntry(1), EPSILON);
        assertEquals(-1.0, v.getEntry(2), EPSILON);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testMapDivideByZero() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        v.mapDivide(0.0);
    }

    @Test
    public void testMapPow() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{2.0, 0.0, -3.0});
        v.mapPow(2);
        assertEquals(4.0, v.getEntry(0), EPSILON);
        assertEquals(0.0, v.getEntry(1), EPSILON);
        assertEquals(9.0, v.getEntry(2), EPSILON);
    }

    // -----------------------------------------------------------------------
    // Copy and conversion
    // -----------------------------------------------------------------------

    @Test
    public void testCopy() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector c = v.copy();
        assertNotNull(c);
        assertEquals(v.getDimension(), c.getDimension());
        for (int i = 0; i < v.getDimension(); i++) {
            assertEquals(v.getEntry(i), c.getEntry(i), EPSILON);
        }
        c.setEntry(0, 99.0);
        assertEquals(1.0, v.getEntry(0), EPSILON); // original unchanged
    }

    @Test
    public void testToArray() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        double[] arr = v.toArray();
        assertArrayEquals(new double[]{1.0, 0.0, 2.0}, arr, EPSILON);
    }

    @Test
    public void testToArrayEmpty() {
        OpenMapRealVector v = new OpenMapRealVector(0);
        double[] arr = v.toArray();
        assertEquals(0, arr.length);
    }

    // -----------------------------------------------------------------------
    // Unit tests targeting Math-29 (ebeMultiply/ebeDivide bugs)
    // -----------------------------------------------------------------------

    @Test
    public void testEbeMultiplyWithOtherSparseContainingZero() {
        // Specific test for bug: other vector has a zero entry that is stored (set to zero),
        // but the product should be zero, not NaN.
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        OpenMapRealVector other = new OpenMapRealVector(new double[]{0.0, 1.0, 0.0});
        OpenMapRealVector result = (OpenMapRealVector) v.ebeMultiply(other);
        assertEquals(0.0, result.getEntry(0), EPSILON);
        assertEquals(2.0, result.getEntry(1), EPSILON);
        assertEquals(0.0, result.getEntry(2), EPSILON);
    }

    @Test
    public void testEbeMultiplyWhereThisIsSparseAndOtherIsDense() {
        OpenMapRealVector sparse = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        ArrayRealVector dense = new ArrayRealVector(new double[]{0.0, 2.0, 0.0});
        RealVector product = sparse.ebeMultiply(dense);
        assertEquals(0.0, product.getEntry(0), EPSILON);
        assertEquals(0.0, product.getEntry(1), EPSILON);
        assertEquals(0.0, product.getEntry(2), EPSILON);
    }

    @Test
    public void testEbeMultiplyDenseWhereOtherEntryIsNaN() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0});
        ArrayRealVector other = new ArrayRealVector(new double[]{Double.NaN, 1.0});
        RealVector result = v.ebeMultiply(other);
        assertTrue(Double.isNaN(result.getEntry(0)));
        assertEquals(0.0, result.getEntry(1), EPSILON);
    }

    @Test
    public void testEbeDivideWithOtherSparseContainingZero() {
        // Division by zero yields Infinity/NaN
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        OpenMapRealVector other = new OpenMapRealVector(new double[]{0.0, 1.0, 0.0});
        OpenMapRealVector result = (OpenMapRealVector) v.ebeDivide(other);
        assertTrue(Double.isInfinite(result.getEntry(0)) && result.getEntry(0) > 0);
        assertEquals(2.0, result.getEntry(1), EPSILON);
        assertTrue(Double.isInfinite(result.getEntry(2)) && result.getEntry(2) > 0);
    }

    @Test
    public void testEbeDivideWhereOtherHasZeroAndThisHasZero() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 1.0});
        OpenMapRealVector other = new OpenMapRealVector(new double[]{0.0, 2.0});
        OpenMapRealVector result = (OpenMapRealVector) v.ebeDivide(other);
        assertTrue(Double.isNaN(result.getEntry(0)));
        assertEquals(0.5, result.getEntry(1), EPSILON);
    }

    // -----------------------------------------------------------------------
    // Edge cases with dimension zero
    // -----------------------------------------------------------------------

    @Test
    public void testZeroDimensionOperations() {
        OpenMapRealVector v = new OpenMapRealVector(0);
        assertEquals(0.0, v.getNorm(), EPSILON);
        assertEquals(0.0, v.getL1Norm(), EPSILON);
        assertEquals(0.0, v.getLInfNorm(), EPSILON);
        assertArrayEquals(new double[0], v.toArray(), EPSILON);
        OpenMapRealVector sum = v.add(v);
        assertEquals(0, sum.getDimension());
        OpenMapRealVector product = (OpenMapRealVector) v.ebeMultiply(v);
        assertEquals(0, product.getDimension());
        assertFalse(v.sparseIterator().hasNext());
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testAddZeroDimensionWithNonZero() {
        OpenMapRealVector v1 = new OpenMapRealVector(0);
        OpenMapRealVector v2 = new OpenMapRealVector(1);
        v1.add(v2);
    }

    // -----------------------------------------------------------------------
    // Miscellaneous
    // -----------------------------------------------------------------------

    @Test
    public void testIsZero() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        assertTrue(v.isZero());
        v.setEntry(2, 0.5);
        assertFalse(v.isZero());
    }

    @Test
    public void testHashCodeAndEquals() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        assertEquals(v1.hashCode(), v2.hashCode());
        assertTrue(v1.equals(v2));
        v2.setEntry(2, 3.0);
        assertFalse(v1.equals(v2));
        assertFalse(v1.equals(null));
        assertFalse(v1.equals("string"));
    }

    @Test
    public void testSerialization() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        // Simple serialization test by converting to byte array and back (if available)
        // but we can simply check that serializable interface is implemented.
        assertTrue(v instanceof java.io.Serializable);
    }
}