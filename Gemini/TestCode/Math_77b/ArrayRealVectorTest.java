package org.apache.commons.math.linear;

import org.junit.Assert;
import org.junit.Test;

public class ArrayRealVectorTest {

    @Test
    public void testGetNorm() {
        double[] data = {3.0, 4.0};
        ArrayRealVector v = new ArrayRealVector(data);
        Assert.assertEquals(5.0, v.getNorm(), 1e-12);
    }

    @Test
    public void testGetL1Norm() {
        double[] data = {-2.0, 3.0, -5.0};
        ArrayRealVector v = new ArrayRealVector(data);
        Assert.assertEquals(10.0, v.getL1Norm(), 1e-12);
    }

    @Test
    public void testGetLinfNorm() {
        double[] data = {-2.0, 7.0, -5.0};
        ArrayRealVector v = new ArrayRealVector(data);
        Assert.assertEquals(7.0, v.getLinfNorm(), 1e-12);
    }

    @Test
    public void testDistance() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {4.0, 6.0, 15.0});
        Assert.assertEquals(13.0, v1.getDistance(v2), 1e-12);
        
        RealVector rv2 = new ArrayRealVector(new double[] {4.0, 6.0, 15.0});
        Assert.assertEquals(13.0, v1.getDistance(rv2), 1e-12);
    }

    @Test
    public void testL1Distance() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {4.0, 5.0, 6.0});
        Assert.assertEquals(9.0, v1.getL1Distance(v2), 1e-12);
        
        RealVector rv2 = new ArrayRealVector(new double[] {4.0, 5.0, 6.0});
        Assert.assertEquals(9.0, v1.getL1Distance(rv2), 1e-12);
    }

    @Test
    public void testLinfDistance() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {4.0, 5.0, 6.0});
        Assert.assertEquals(3.0, v1.getLinfDistance(v2), 1e-12);
        
        RealVector rv2 = new ArrayRealVector(new double[] {4.0, 5.0, 6.0});
        Assert.assertEquals(3.0, v1.getLinfDistance(rv2), 1e-12);
    }

    @Test
    public void testUnitVector() {
        ArrayRealVector v = new ArrayRealVector(new double[] {3.0, 4.0});
        RealVector unit = v.unitVector();
        Assert.assertEquals(0.6, unit.getEntry(0), 1e-12);
        Assert.assertEquals(0.8, unit.getEntry(1), 1e-12);
    }

    @Test
    public void testUnitize() {
        ArrayRealVector v = new ArrayRealVector(new double[] {3.0, 4.0});
        v.unitize();
        Assert.assertEquals(0.6, v.getEntry(0), 1e-12);
        Assert.assertEquals(0.8, v.getEntry(1), 1e-12);
    }

    @Test
    public void testProjection() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 0.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {2.0, 0.0});
        RealVector proj = v1.projection(v2);
        Assert.assertEquals(1.0, proj.getEntry(0), 1e-12);
        
        RealVector rv2 = new ArrayRealVector(new double[] {2.0, 0.0});
        RealVector proj2 = v1.projection(rv2);
        Assert.assertEquals(1.0, proj2.getEntry(0), 1e-12);
    }

    @Test
    public void testOuterProduct() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {3.0, 4.0, 5.0});
        RealMatrix op = v1.outerProduct(v2);
        Assert.assertEquals(2, op.getRowDimension());
        Assert.assertEquals(3, op.getColumnDimension());
        Assert.assertEquals(3.0, op.getEntry(0, 0), 1e-12);
        Assert.assertEquals(8.0, op.getEntry(1, 1), 1e-12);
        
        RealVector rv2 = new ArrayRealVector(new double[] {3.0, 4.0, 5.0});
        RealMatrix op2 = v1.outerProduct(rv2);
        Assert.assertEquals(2, op2.getRowDimension());
        Assert.assertEquals(3, op2.getColumnDimension());
    }

    @Test
    public void testAdd() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {3.0, 4.0});
        RealVector res = v1.add(v2);
        Assert.assertEquals(4.0, res.getEntry(0), 1e-12);
        Assert.assertEquals(6.0, res.getEntry(1), 1e-12);
        
        RealVector rv2 = new ArrayRealVector(new double[] {3.0, 4.0});
        RealVector res2 = v1.add(rv2);
        Assert.assertEquals(4.0, res2.getEntry(0), 1e-12);
    }

    @Test
    public void testSubtract() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {3.0, 5.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {1.0, 2.0});
        RealVector res = v1.subtract(v2);
        Assert.assertEquals(2.0, res.getEntry(0), 1e-12);
        Assert.assertEquals(3.0, res.getEntry(1), 1e-12);
        
        RealVector rv2 = new ArrayRealVector(new double[] {1.0, 2.0});
        RealVector res2 = v1.subtract(rv2);
        Assert.assertEquals(2.0, res2.getEntry(0), 1e-12);
    }

    @Test
    public void testMapOperations() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, 2.0});
        
        RealVector res1 = v.mapAdd(1.0);
        Assert.assertEquals(2.0, res1.getEntry(0), 1e-12);
        
        RealVector res2 = v.mapAdd(1.0);
        Assert.assertNotNull(res2);

        RealVector res3 = v.mapSubtract(1.0);
        Assert.assertEquals(0.0, res3.getEntry(0), 1e-12);

        RealVector res4 = v.mapMultiply(2.0);
        Assert.assertEquals(4.0, res4.getEntry(1), 1e-12);

        RealVector res5 = v.mapDivide(2.0);
        Assert.assertEquals(0.5, res5.getEntry(0), 1e-12);

        RealVector res6 = v.mapPow(2.0);
        Assert.assertEquals(4.0, res6.getEntry(1), 1e-12);

        RealVector res7 = v.mapExp();
        Assert.assertNotNull(res7);

        RealVector res8 = v.mapExpm1();
        Assert.assertNotNull(res8);

        RealVector res9 = v.mapLog();
        Assert.assertNotNull(res9);

        RealVector res10 = v.mapLog10();
        Assert.assertNotNull(res10);

        RealVector res11 = v.mapLog1p();
        Assert.assertNotNull(res11);

        RealVector res12 = v.mapCosh();
        Assert.assertNotNull(res12);

        RealVector res13 = v.mapSinh();
        Assert.assertNotNull(res13);

        RealVector res14 = v.mapTanh();
        Assert.assertNotNull(res14);

        RealVector res15 = v.mapCos();
        Assert.assertNotNull(res15);

        RealVector res16 = v.mapSin();
        Assert.assertNotNull(res16);

        RealVector res17 = v.mapTan();
        Assert.assertNotNull(res17);

        RealVector res18 = v.mapAcos();
        Assert.assertNotNull(res18);

        RealVector res19 = v.mapAsin();
        Assert.assertNotNull(res19);

        RealVector res20 = v.mapAtan();
        Assert.assertNotNull(res20);

        RealVector res21 = v.mapInv();
        Assert.assertEquals(0.5, res21.getEntry(1), 1e-12);

        RealVector res22 = v.mapAbs();
        Assert.assertNotNull(res22);

        RealVector res23 = v.mapSqrt();
        Assert.assertNotNull(res23);

        RealVector res24 = v.mapCbrt();
        Assert.assertNotNull(res24);

        RealVector res25 = v.mapCeil();
        Assert.assertNotNull(res25);

        RealVector res26 = v.mapFloor();
        Assert.assertNotNull(res26);

        RealVector res27 = v.mapRint();
        Assert.assertNotNull(res27);

        RealVector res28 = v.mapSignum();
        Assert.assertNotNull(res28);

        RealVector res29 = v.mapUlp();
        Assert.assertNotNull(res29);
    }

    @Test
    public void testMapToSelfOperations() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, 2.0});
        
        v.mapAdd(1.0);
        v.mapAdd(1.0);
        v.mapSubtract(1.0);
        v.mapMultiply(2.0);
        v.mapDivide(2.0);
        v.mapPow(2.0);
        v.mapExp();
        v.mapExpm1();
        v.mapLog();
        v.mapLog10();
        v.mapLog1p();
        v.mapCosh();
        v.mapSinh();
        v.mapTanh();
        v.mapCos();
        v.mapSin();
        v.mapTan();
        v.mapAcos();
        v.mapAsin();
        v.mapAtan();
        v.mapInv();
        v.mapAbs();
        v.mapSqrt();
        v.mapCbrt();
        v.mapCeil();
        v.mapFloor();
        v.mapRint();
        v.mapSignum();
        v.mapUlp();
        
        Assert.assertNotNull(v);
    }

    @Test
    public void testBasicGettersAndSetters() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, 2.0, 3.0});
        Assert.assertEquals(3, v.getDimension());
        
        double[] data = v.getData();
        Assert.assertEquals(3, data.length);
        
        double[] ref = v.getDataRef();
        Assert.assertNotNull(ref);
        
        Assert.assertEquals(2.0, v.getEntry(1), 1e-12);
        
        v.setEntry(1, 5.0);
        Assert.assertEquals(5.0, v.getEntry(1), 1e-12);
        
        v.set(10.0);
        Assert.assertEquals(10.0, v.getEntry(0), 1e-12);
        Assert.assertEquals(10.0, v.getEntry(1), 1e-12);
        Assert.assertEquals(10.0, v.getEntry(2), 1e-12);
    }

    @Test
    public void testSubVector() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, 2.0, 3.0, 4.0});
        RealVector sub = v.getSubVector(1, 2);
        Assert.assertEquals(2, sub.getDimension());
        Assert.assertEquals(2.0, sub.getEntry(0), 1e-12);
        Assert.assertEquals(3.0, sub.getEntry(1), 1e-12);
        
        v.setSubVector(1, new double[] {9.0, 8.0});
        Assert.assertEquals(9.0, v.getEntry(1), 1e-12);
        Assert.assertEquals(8.0, v.getEntry(2), 1e-12);
        
        ArrayRealVector otherSub = new ArrayRealVector(new double[] {7.0, 6.0});
        v.setSubVector(0, otherSub);
        Assert.assertEquals(7.0, v.getEntry(0), 1e-12);
        Assert.assertEquals(6.0, v.getEntry(1), 1e-12);
    }

    @Test
    public void testIsNaNAndIsInfinite() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, Double.NaN});
        Assert.assertTrue(v.isNaN());

        ArrayRealVector v2 = new ArrayRealVector(new double[] {1.0, Double.POSITIVE_INFINITY});
        Assert.assertTrue(v2.isInfinite());
    }

    @Test
    public void testEqualsAndHashCode() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {1.0, 2.0});
        ArrayRealVector v3 = new ArrayRealVector(new double[] {1.0, 3.0});
        
        Assert.assertTrue(v1.equals(v1));
        Assert.assertTrue(v1.equals(v2));
        Assert.assertFalse(v1.equals(v3));
        Assert.assertFalse(v1.equals(null));
        Assert.assertFalse(v1.equals(new Object()));
        
        Assert.assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    public void testAppend() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {3.0, 4.0});
        
        RealVector r1 = v1.append(v2);
        Assert.assertEquals(4, r1.getDimension());
        Assert.assertEquals(3.0, r1.getEntry(2), 1e-12);
        
        RealVector r2 = v1.append(5.0);
        Assert.assertEquals(3, r2.getDimension());
        Assert.assertEquals(5.0, r2.getEntry(2), 1e-12);
        
        RealVector r3 = v1.append(new double[] {6.0, 7.0});
        Assert.assertEquals(4, r3.getDimension());
        Assert.assertEquals(6.0, r3.getEntry(2), 1e-12);
    }

    @Test
    public void testDotProduct() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[] {4.0, 5.0, 6.0});
        
        double dp = v1.dotProduct(v2);
        Assert.assertEquals(32.0, dp, 1e-12);
        
        RealVector rv2 = new ArrayRealVector(new double[] {4.0, 5.0, 6.0});
        double dp2 = v1.dotProduct(rv2);
        Assert.assertEquals(32.0, dp2, 1e-12);
    }

    @Test
    public void testIterativeAndVisits() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, 2.0});
        
        RealVectorPreservingVisitor preservingVisitor = new RealVectorPreservingVisitor() {
            private double sum = 0;
            public void visit(int index, double value) { sum += value; }
            public double getResult() { return sum; }
            public void start(int dimension, int start, int end) {}
        };
        double resPres = v.walkInOptimizedOrder(preservingVisitor);
        Assert.assertEquals(3.0, resPres, 1e-12);

        RealVectorChangingVisitor changingVisitor = new RealVectorChangingVisitor() {
            public double visit(int index, double value) { return value * 2.0; }
            public double getResult() { return 0; }
            public void start(int dimension, int start, int end) {}
        };
        double resChange = v.walkInOptimizedOrder(changingVisitor);
        Assert.assertEquals(0.0, resChange, 1e-12);
        Assert.assertEquals(4.0, v.getEntry(1), 1e-12);
    }

    @Test
    public void testCopyAndExtract() {
        ArrayRealVector v = new ArrayRealVector(new double[] {1.0, 2.0});
        RealVector copy = v.copy();
        Assert.assertEquals(v.getDimension(), copy.getDimension());
        Assert.assertEquals(v.getEntry(0), copy.getEntry(0), 1e-12);
        
        double[] dest = new double[2];
        v.copyInto(dest, 0);
        Assert.assertEquals(1.0, dest[0], 1e-12);
        Assert.assertEquals(2.0, dest[1], 1e-12);
    }

    @Test
    public void testConstructorsAndEdgeCases() {
        ArrayRealVector v0 = new ArrayRealVector();
        Assert.assertEquals(0, v0.getDimension());

        ArrayRealVector v1 = new ArrayRealVector(5);
        Assert.assertEquals(5, v1.getDimension());
        Assert.assertEquals(0.0, v1.getEntry(0), 1e-12);

        ArrayRealVector v2 = new ArrayRealVector(3, 2.5);
        Assert.assertEquals(3, v2.getDimension());
        Assert.assertEquals(2.5, v2.getEntry(0), 1e-12);

        Double[] dObj = {1.0, 2.0, 3.0};
        ArrayRealVector v3 = new ArrayRealVector(dObj);
        Assert.assertEquals(3, v3.getDimension());
        Assert.assertEquals(2.0, v3.getEntry(1), 1e-12);

        ArrayRealVector v4 = new ArrayRealVector(dObj, 1, 2);
        Assert.assertEquals(2, v4.getDimension());
        Assert.assertEquals(2.0, v4.getEntry(0), 1e-12);

        double[] dPrim = {4.0, 5.0, 6.0};
        ArrayRealVector v5 = new ArrayRealVector(dPrim, false);
        Assert.assertEquals(3, v5.getDimension());

        ArrayRealVector v6 = new ArrayRealVector(dPrim, 1, 2, false);
        Assert.assertEquals(2, v6.getDimension());
        Assert.assertEquals(5.0, v6.getEntry(0), 1e-12);

        ArrayRealVector v7 = new ArrayRealVector(v5);
        Assert.assertEquals(3, v7.getDimension());

        SparseRealVector srv = new OpenMapRealVector(new double[] {1.0, 0.0, 3.0});
        ArrayRealVector v8 = new ArrayRealVector(srv);
        Assert.assertEquals(3, v8.getDimension());
        Assert.assertEquals(3.0, v8.getEntry(2), 1e-12);
    }

    @Test
    public void testOperationsWithOtherVectorTypes() {
        ArrayRealVector v1 = new ArrayRealVector(new double[] {1.0, 2.0});
        SparseRealVector srv = new OpenMapRealVector(new double[] {3.0, 4.0});
        
        RealVector addRes = v1.add(srv);
        Assert.assertEquals(4.0, addRes.getEntry(0), 1e-12);

        RealVector subRes = v1.subtract(srv);
        Assert.assertEquals(-2.0, subRes.getEntry(0), 1e-12);

        double dotRes = v1.dotProduct(srv);
        Assert.assertEquals(11.0, dotRes, 1e-12);

        double distRes = v1.getDistance(srv);
        Assert.assertNotNull(distRes);

        double l1Dist = v1.getL1Distance(srv);
        Assert.assertNotNull(l1Dist);

        double linfDist = v1.getLinfDistance(srv);
        Assert.assertNotNull(linfDist);

        RealVector proj = v1.projection(srv);
        Assert.assertNotNull(proj);
    }
}