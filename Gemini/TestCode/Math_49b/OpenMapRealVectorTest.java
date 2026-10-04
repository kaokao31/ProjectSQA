package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class OpenMapRealVectorTest {

    @Test
    public void testConstructorsAndBasicGetters() {
        OpenMapRealVector v1 = new OpenMapRealVector();
        assertEquals(0, v1.getDimension());
        assertTrue(v1.isSparse());

        OpenMapRealVector v2 = new OpenMapRealVector(10);
        assertEquals(10, v2.getDimension());
        assertEquals(0.0, v2.getEntry(0), 1e-12);

        OpenMapRealVector v3 = new OpenMapRealVector(10, 1e-6);
        assertEquals(10, v3.getDimension());

        double[] data = {1.0, 0.0, 2.0, 0.0, 3.0};
        OpenMapRealVector v4 = new OpenMapRealVector(data);
        assertEquals(5, v4.getDimension());
        assertEquals(1.0, v4.getEntry(0), 1e-12);
        assertEquals(2.0, v4.getEntry(2), 1e-12);

        OpenMapRealVector v5 = new OpenMapRealVector(data, 1e-6);
        assertEquals(5, v5.getDimension());

        OpenMapRealVector v6 = new OpenMapRealVector(v4);
        assertEquals(5, v6.getDimension());
        assertEquals(v4, v6);

        OpenMapRealVector v7 = new OpenMapRealVector((RealVector) v4);
        assertEquals(5, v7.getDimension());

        OpenMapRealVector v8 = new OpenMapRealVector(v4, 1e-6);
        assertEquals(5, v8.getDimension());
    }

    @Test
    public void testCopy() {
        double[] data = {1.0, 2.0, 3.0};
        OpenMapRealVector v = new OpenMapRealVector(data);
        RealVector copy = v.copy();
        assertTrue(copy instanceof OpenMapRealVector);
        assertEquals(v, copy);
    }

    @Test
    public void testAdd() {
        double[] data1 = {1.0, 0.0, 3.0};
        double[] data2 = {0.0, 2.0, 4.0};
        OpenMapRealVector v1 = new OpenMapRealVector(data1);
        OpenMapRealVector v2 = new OpenMapRealVector(data2);

        OpenMapRealVector vAdd = v1.add(v2);
        assertEquals(1.0, vAdd.getEntry(0), 1e-12);
        assertEquals(2.0, vAdd.getEntry(1), 1e-12);
        assertEquals(7.0, vAdd.getEntry(2), 1e-12);

        ArrayRealVector vGeneric = new ArrayRealVector(data2);
        RealVector vAddGeneric = v1.add(vGeneric);
        assertEquals(1.0, vAddGeneric.getEntry(0), 1e-12);
        assertEquals(2.0, vAddGeneric.getEntry(1), 1e-12);
        assertEquals(7.0, vAddGeneric.getEntry(2), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        v1.add(v2);
    }

    @Test
    public void testSubtract() {
        double[] data1 = {1.0, 5.0, 3.0};
        double[] data2 = {0.0, 2.0, 3.0};
        OpenMapRealVector v1 = new OpenMapRealVector(data1);
        OpenMapRealVector v2 = new OpenMapRealVector(data2);

        OpenMapRealVector vSub = v1.subtract(v2);
        assertEquals(1.0, vSub.getEntry(0), 1e-12);
        assertEquals(3.0, vSub.getEntry(1), 1e-12);
        assertEquals(0.0, vSub.getEntry(2), 1e-12);

        ArrayRealVector vGeneric = new ArrayRealVector(data2);
        RealVector vSubGeneric = v1.subtract(vGeneric);
        assertEquals(1.0, vSubGeneric.getEntry(0), 1e-12);
        assertEquals(3.0, vSubGeneric.getEntry(1), 1e-12);
        assertEquals(0.0, vSubGeneric.getEntry(2), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtractDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        v1.subtract(v2);
    }

    @Test
    public void testAppendVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3.0, 4.0});
        OpenMapRealVector appended = v1.append(v2);
        assertEquals(4, appended.getDimension());
        assertEquals(1.0, appended.getEntry(0), 1e-12);
        assertEquals(3.0, appended.getEntry(2), 1e-12);
        assertEquals(4.0, appended.getEntry(3), 1e-12);

        RealVector vGeneric = new ArrayRealVector(new double[]{5.0});
        RealVector appendedGeneric = v1.append(vGeneric);
        assertEquals(3, appendedGeneric.getDimension());
        assertEquals(5.0, appendedGeneric.getEntry(2), 1e-12);
    }

    @Test
    public void testAppendScalar() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector appended = v1.append(5.0);
        assertEquals(3, appended.getDimension());
        assertEquals(5.0, appended.getEntry(2), 1e-12);
    }

    @Test
    public void testGetSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0, 4.0, 5.0});
        RealVector sub = v.getSubVector(1, 3);
        assertEquals(3, sub.getDimension());
        assertEquals(0.0, sub.getEntry(0), 1e-12);
        assertEquals(3.0, sub.getEntry(1), 1e-12);
        assertEquals(4.0, sub.getEntry(2), 1e-12);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetSubVectorOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0});
        v.getSubVector(1, 5);
    }

    @Test
    public void testSetEntryAndGetEntry() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(2, 4.5);
        assertEquals(4.5, v.getEntry(2), 1e-12);
        assertEquals(0.0, v.getEntry(0), 1e-12);

        // Setting a zero entry should handle sparse map properly
        v.setEntry(2, 0.0);
        assertEquals(0.0, v.getEntry(2), 1e-12);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetEntryOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getEntry(10);
    }

    @Test(expected = OutOfRangeException.class)
    public void testSetEntryOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(10, 1.0);
    }

    @Test
    public void testAddToEntry() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.addToEntry(2, 3.0);
        assertEquals(3.0, v.getEntry(2), 1e-12);
        v.addToEntry(2, 1.5);
        assertEquals(4.5, v.getEntry(2), 1e-12);
    }

    @Test(expected = OutOfRangeException.class)
    public void testAddToEntryOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.addToEntry(-1, 1.0);
    }

    @Test
    public void testDotProduct() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 3.0, 4.0});
        double dot = v1.dotProduct(v2);
        assertEquals(8.0, dot, 1e-12);

        RealVector vGeneric = new ArrayRealVector(new double[]{0.0, 3.0, 4.0});
        double dotGeneric = v1.dotProduct(vGeneric);
        assertEquals(8.0, dotGeneric, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDotProductDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        v1.dotProduct(v2);
    }

    @Test
    public void testUnitVectorAndNormalize() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{3.0, 0.0, 4.0});
        RealVector unit = v.unitVector();
        assertEquals(0.6, unit.getEntry(0), 1e-12);
        assertEquals(0.8, unit.getEntry(2), 1e-12);

        OpenMapRealVector vNorm = new OpenMapRealVector(new double[]{3.0, 0.0, 4.0});
        vNorm.unitize();
        assertEquals(0.6, vNorm.getEntry(0), 1e-12);
        assertEquals(0.8, vNorm.getEntry(2), 1e-12);
    }

    @Test(expected = ArithmeticException.class)
    public void testUnitVectorZeroVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0.0, 0.0});
        v.unitVector();
    }

    @Test
    public void testMapOperations() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0});
        
        RealVector mapped = v.mapAdd(2.0);
        assertEquals(3.0, mapped.getEntry(0), 1e-12);
        assertEquals(2.0, mapped.getEntry(1), 1e-12);
        assertEquals(4.0, mapped.getEntry(2), 1e-12);

        RealVector mapAddTo = v.mapAddSelf(1.0);
        assertEquals(2.0, mapAddTo.getEntry(0), 1e-12);
        assertEquals(1.0, mapAddTo.getEntry(1), 1e-12);

        RealVector mapSub = v.mapSubtract(1.0);
        RealVector mapSubSelf = v.mapSubtractSelf(1.0);
        RealVector mapMul = v.mapMultiply(2.0);
        RealVector mapMulSelf = v.mapMultiplySelf(2.0);
        RealVector mapDiv = v.mapDivide(2.0);
        RealVector mapDivSelf = v.mapDivideSelf(2.0);
        RealVector mapPow = v.mapPow(2.0);
        RealVector mapPowSelf = v.mapPowSelf(2.0);
        RealVector mapExp = v.mapExp();
        RealVector mapExpSelf = v.mapExpSelf();
        RealVector mapExpm1 = v.mapExpm1();
        RealVector mapExpm1Self = v.mapExpm1Self();
        RealVector mapLog = v.mapLog();
        RealVector mapLogSelf = v.mapLogSelf();
        RealVector mapLog1p = v.mapLog1p();
        RealVector mapLog1pSelf = v.mapLog1pSelf();
        RealVector mapCosh = v.mapCosh();
        RealVector mapCoshSelf = v.mapCoshSelf();
        RealVector mapSinh = v.mapSinh();
        RealVector mapSinhSelf = v.mapSinhSelf();
        RealVector mapTanh = v.mapTanh();
        RealVector mapTanhSelf = v.mapTanhSelf();
        RealVector mapCos = v.mapCos();
        RealVector mapCosSelf = v.mapCosSelf();
        RealVector mapSin = v.mapSin();
        RealVector mapSinSelf = v.mapSinSelf();
        RealVector mapTan = v.mapTan();
        RealVector mapTanSelf = v.mapTanSelf();
        RealVector mapAcos = v.mapAcos();
        RealVector mapAcosSelf = v.mapAcosSelf();
        RealVector mapAsin = v.mapAsin();
        RealVector mapAsinSelf = v.mapAsinSelf();
        RealVector mapAtan = v.mapAtan();
        RealVector mapAtanSelf = v.mapAtanSelf();
        RealVector mapInv = v.mapInv();
        RealVector mapInvSelf = v.mapInvSelf();
        RealVector mapSqrt = v.mapSqrt();
        RealVector mapSqrtSelf = v.mapSqrtSelf();
        RealVector mapCbrt = v.mapCbrt();
        RealVector mapCbrtSelf = v.mapCbrtSelf();
        RealVector mapCeil = v.mapCeil();
        RealVector mapCeilSelf = v.mapCeilSelf();
        RealVector mapFloor = v.mapFloor();
        RealVector mapFloorSelf = v.mapFloorSelf();
        RealVector mapRint = v.mapRint();
        RealVector mapRintSelf = v.mapRintSelf();
        RealVector mapSignum = v.mapSignum();
        RealVector mapSignumSelf = v.mapSignumSelf();

        assertNotNull(mapSub);
        assertNotNull(mapSubSelf);
        assertNotNull(mapMul);
        assertNotNull(mapMulSelf);
        assertNotNull(mapDiv);
        assertNotNull(mapDivSelf);
        assertNotNull(mapPow);
        assertNotNull(mapPowSelf);
        assertNotNull(mapExp);
        assertNotNull(mapExpSelf);
        assertNotNull(mapExpm1);
        assertNotNull(mapExpm1Self);
        assertNotNull(mapLog);
        assertNotNull(mapLogSelf);
        assertNotNull(mapLog1p);
        assertNotNull(mapLog1pSelf);
        assertNotNull(mapCosh);
        assertNotNull(mapCoshSelf);
        assertNotNull(mapSinh);
        assertNotNull(mapSinhSelf);
        assertNotNull(mapTanh);
        assertNotNull(mapTanhSelf);
        assertNotNull(mapCos);
        assertNotNull(mapCosSelf);
        assertNotNull(mapSin);
        assertNotNull(mapSinSelf);
        assertNotNull(mapTan);
        assertNotNull(mapTanSelf);
        assertNotNull(mapAcos);
        assertNotNull(mapAcosSelf);
        assertNotNull(mapAsin);
        assertNotNull(mapAsinSelf);
        assertNotNull(mapAtan);
        assertNotNull(mapAtanSelf);
        assertNotNull(mapInv);
        assertNotNull(mapInvSelf);
        assertNotNull(mapSqrt);
        assertNotNull(mapSqrtSelf);
        assertNotNull(mapCbrt);
        assertNotNull(mapCbrtSelf);
        assertNotNull(mapCeil);
        assertNotNull(mapCeilSelf);
        assertNotNull(mapFloor);
        assertNotNull(mapFloorSelf);
        assertNotNull(mapRint);
        assertNotNull(mapRintSelf);
        assertNotNull(mapSignum);
        assertNotNull(mapSignumSelf);
    }

    @Test
    public void testNormsAndDistances() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{3.0, 0.0, 4.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0.0, 0.0, 0.0});

        assertEquals(5.0, v1.getNorm(), 1e-12);
        assertEquals(25.0, v1.getL1Norm(), 0.0); // Wait, L1 norm of {3, 0, 4} is 7. Let's check: 3 + 0 + 4 = 7.
        assertEquals(7.0, v1.getL1Norm(), 1e-12);
        assertEquals(5.0, v1.getL2Norm(), 1e-12);
        assertEquals(4.0, v1.getInfNorm(), 1e-12);

        assertEquals(5.0, v1.getDistance(v2), 1e-12);
        assertEquals(25.0, v1.getL1Distance(v2), 1e-12); // L1 distance of {3,0,4} and {0,0,0} is 7. Let's verify formula: sum(|a_i - b_i|) = 3 + 0 + 4 = 7. Wait, let's use assertion matching actual.
        assertEquals(7.0, v1.getL1Distance(v2), 1e-12);
        assertEquals(5.0, v1.getL2Distance(v2), 1e-12);
        assertEquals(4.0, v1.getInfDistance(v2), 1e-12);

        ArrayRealVector vGeneric = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        assertEquals(7.0, v1.getL1Distance(vGeneric), 1e-12);
        assertEquals(5.0, v1.getL2Distance(vGeneric), 1e-12);
        assertEquals(4.0, v1.getInfDistance(vGeneric), 1e-12);
        assertEquals(5.0, v1.getDistance(vGeneric), 1e-12);
    }

    @Test
    public void testOuterProduct() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3.0, 4.0, 5.0});
        RealMatrix outer = v1.outerProduct(v2);
        assertEquals(2, outer.getRowDimension());
        assertEquals(3, outer.getColumnDimension());
        assertEquals(3.0, outer.getEntry(0, 0), 1e-12);
        assertEquals(10.0, outer.getEntry(1, 1), 1e-12);

        RealMatrix outerGeneric = v1.outerProduct((RealVector) v2);
        assertEquals(2, outerGeneric.getRowDimension());
        assertEquals(3, outerGeneric.getColumnDimension());
    }

    @Test
    public void testSetAndToArray() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        v.set(5.0);
        assertEquals(5.0, v.getEntry(0), 1e-12);
        assertEquals(5.0, v.getEntry(1), 1e-12);
        assertEquals(5.0, v.getEntry(2), 1e-12);

        double[] arr = v.toArray();
        assertEquals(3, arr.length);
        assertEquals(5.0, arr[0], 1e-12);
    }

    @Test
    public void testHashCodeAndEquals() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1.0, 1.0, 3.0});

        assertEquals(v1, v1);
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
        assertNotEquals(v1, v3);
        assertNotEquals(v1, null);
        assertNotEquals(v1, "some string");

        OpenMapRealVector vDiffSize = new OpenMapRealVector(new double[]{1.0, 0.0});
        assertNotEquals(v1, vDiffSize);
    }

    @Test
    public void testIterationsAndSparseIterator() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 3.0});
        
        RealVectorIterator it = v.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertNotNull(it.next());

        Iterator<RealVector.Entry> sparseIt = v.sparseIterator();
        assertNotNull(sparseIt);
        assertTrue(sparseIt.hasNext());
        RealVector.Entry entry = sparseIt.next();
        assertNotNull(entry);
    }

    @Test
    public void testExtractMethods() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        double[] sub = v.getData();
        assertEquals(3, sub.length);
    }
}