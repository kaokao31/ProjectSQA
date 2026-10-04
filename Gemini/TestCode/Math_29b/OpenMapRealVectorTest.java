package org.apache.commons.math3.linear;

import org.junit.Assert;
import org.junit.Test;

public class OpenMapRealVectorTest {

    @Test
    public void testConstructorsAndBasics() {
        OpenMapRealVector v1 = new OpenMapRealVector();
        Assert.assertEquals(0, v1.getDimension());
        Assert.assertTrue(v1.isSparse());

        OpenMapRealVector v2 = new OpenMapRealVector(10);
        Assert.assertEquals(10, v2.getDimension());

        OpenMapRealVector v3 = new OpenMapRealVector(10, 0.5);
        Assert.assertEquals(10, v3.getDimension());

        double[] data = {1.0, 0.0, 2.0, 0.0, 3.0};
        OpenMapRealVector v4 = new OpenMapRealVector(data);
        Assert.assertEquals(5, v4.getDimension());
        Assert.assertEquals(1.0, v4.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, v4.getEntry(2), 1e-12);

        OpenMapRealVector v5 = new OpenMapRealVector(v4);
        Assert.assertEquals(5, v5.getDimension());
        Assert.assertEquals(1.0, v5.getEntry(0), 1e-12);

        RealVector standardVector = new ArrayRealVector(new double[]{0.0, 5.0, 0.0});
        OpenMapRealVector v6 = new OpenMapRealVector(standardVector);
        Assert.assertEquals(3, v6.getDimension());
        Assert.assertEquals(5.0, v6.getEntry(1), 1e-12);

        OpenMapRealVector v7 = new OpenMapRealVector(v4, 1e-6);
        Assert.assertEquals(5, v7.getDimension());

        OpenMapRealVector v8 = new OpenMapRealVector(v2, 1e-6);
        Assert.assertEquals(10, v8.getDimension());
    }

    @Test
    public void testSetAndGetEntry() {
        OpenMapRealVector v = new OpenMapRealVector(10);
        v.setEntry(2, 5.5);
        Assert.assertEquals(5.5, v.getEntry(2), 1e-12);
        Assert.assertEquals(0.0, v.getEntry(0), 1e-12);

        v.setEntry(2, 0.0); // Test setting to tolerance or zero
        Assert.assertEquals(0.0, v.getEntry(2), 1e-12);

        v.setEntry(5, 7.7);
        v.addToEntry(5, 2.3);
        Assert.assertEquals(10.0, v.getEntry(5), 1e-12);

        v.addToEntry(0, 1.1);
        Assert.assertEquals(1.1, v.getEntry(0), 1e-12);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetEntryOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getEntry(10);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetEntryOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(-1, 1.0);
    }

    @Test
    public void testCopy() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        RealVector copy = v.copy();
        Assert.assertTrue(copy instanceof OpenMapRealVector);
        Assert.assertEquals(v.getDimension(), copy.getDimension());
        Assert.assertEquals(1.0, copy.getEntry(0), 1e-12);
    }

    @Test
    public void testAdd() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 2.0, 3.0});
        RealVector result = v1.add(v2);
        Assert.assertEquals(1.0, result.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, result.getEntry(1), 1e-12);
        Assert.assertEquals(6.0, result.getEntry(2), 1e-12);

        ArrayRealVector v3 = new ArrayRealVector(new double[]{1.0, 1.0, 1.0});
        RealVector result2 = v1.add(v3);
        Assert.assertEquals(2.0, result2.getEntry(0), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        v1.add(v2);
    }

    @Test
    public void testAppend() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3.0, 4.0});

        OpenMapRealVector res1 = v1.append(v2);
        Assert.assertEquals(4, res1.getDimension());
        Assert.assertEquals(3.0, res1.getEntry(2), 1e-12);

        RealVector res2 = v1.append(new ArrayRealVector(new double[]{5.0}));
        Assert.assertEquals(3, res2.getDimension());
        Assert.assertEquals(5.0, res2.getEntry(2), 1e-12);

        RealVector res3 = v1.append(99.0);
        Assert.assertEquals(3, res3.getDimension());
        Assert.assertEquals(99.0, res3.getEntry(2), 1e-12);
    }

    @Test
    public void testDotProduct() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0, 4.0});

        double dot = v1.dotProduct(v2);
        Assert.assertEquals(8.0, dot, 1e-12);

        ArrayRealVector v3 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        double dot2 = v1.dotProduct(v3);
        Assert.assertEquals(7.0, dot2, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDotProductDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0});
        v1.dotProduct(v2);
    }

    @Test
    public void testSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        RealVector sub = v.getSubVector(1, 3);
        Assert.assertEquals(3, sub.getDimension());
        Assert.assertEquals(2.0, sub.getEntry(0), 1e-12);
        Assert.assertEquals(4.0, sub.getEntry(2), 1e-12);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSubVectorOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        v.getSubVector(1, 5);
    }

    @Test
    public void testSetSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0, 4.0});
        v.setSubVector(1, new ArrayRealVector(new double[]{9.0, 8.0}));
        Assert.assertEquals(1.0, v.getEntry(0), 1e-12);
        Assert.assertEquals(9.0, v.getEntry(1), 1e-12);
        Assert.assertEquals(8.0, v.getEntry(2), 1e-12);
        Assert.assertEquals(4.0, v.getEntry(3), 1e-12);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetSubVectorOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        v.setSubVector(3, new ArrayRealVector(new double[]{9.0}));
    }

    @Test
    public void testUnitVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{3.0, 0.0, 4.0});
        RealVector unit = v.unitVector();
        Assert.assertEquals(0.6, unit.getEntry(0), 1e-12);
        Assert.assertEquals(0.8, unit.getEntry(2), 1e-12);
    }

    @Test(expected = ArithmeticException.class)
    public void testUnitVectorZeroNorm() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 0.0});
        v.unitVector();
    }

    @Test
    public void testMapOperations() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        RealVector mapped = v.mapAdd(2.0);
        Assert.assertEquals(3.0, mapped.getEntry(0), 1e-12);
        Assert.assertEquals(2.0, mapped.getEntry(1), 1e-12);

        RealVector mappedAddToSelf = v.mapAdd(1.0);
        Assert.assertNotNull(mappedAddToSelf);

        RealVector subMapped = v.mapSubtract(1.0);
        Assert.assertNotNull(subMapped);

        RealVector divMapped = v.mapDivide(2.0);
        Assert.assertNotNull(divMapped);

        RealVector mulMapped = v.mapMultiply(2.0);
        Assert.assertNotNull(mulMapped);
    }

    @Test
    public void testIterator() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        RealVector.EntryIterator it = v.iterator();
        Assert.assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            RealVector.Entry entry = it.next();
            Assert.assertNotNull(entry);
            count++;
        }
        Assert.assertTrue(count > 0);
    }

    @Test
    public void testSparseIterator() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        java.util.Iterator<RealVector.Entry> it = v.sparseIterator();
        Assert.assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            RealVector.Entry entry = it.next();
            Assert.assertNotNull(entry);
            count++;
        }
        Assert.assertEquals(2, count);
    }

    @Test
    public void testEqualsAndHashCode() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1.0, 1.0, 3.0});

        Assert.assertTrue(v1.equals(v1));
        Assert.assertTrue(v1.equals(v2));
        Assert.assertFalse(v1.equals(v3));
        Assert.assertFalse(v1.equals(null));
        Assert.assertFalse(v1.equals(new Object()));

        Assert.assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void testMiscMethods() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, -2.0, 3.0});
        Assert.assertEquals(3.0, v.getL1Norm(), 1e-12);
        Assert.assertEquals(Math.sqrt(14.0), v.getL2Norm(), 1e-12);
        Assert.assertEquals(3.0, v.getInfNorm(), 1e-12);

        RealVector vAbs = v.mapAbs();
        Assert.assertEquals(2.0, vAbs.getEntry(1), 1e-12);

        RealVector vSqrt = v.mapSubtract(1.0).mapSqrt();
        Assert.assertNotNull(vSqrt);
    }
}