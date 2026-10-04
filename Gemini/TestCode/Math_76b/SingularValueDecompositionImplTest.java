package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class SingularValueDecompositionImplTest {

    @Test
    public void testSvdBasicRealMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        });

        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);

        RealMatrix u = svd.getU();
        RealMatrix s = svd.getS();
        RealMatrix v = svd.getV();

        assertNotNull(u);
        assertNotNull(s);
        assertNotNull(v);

        assertEquals(3, u.getRowDimension());
        assertEquals(3, u.getColumnDimension());

        assertEquals(3, s.getRowDimension());
        assertEquals(2, s.getColumnDimension());

        assertEquals(2, v.getRowDimension());
        assertEquals(2, v.getColumnDimension());

        assertTrue(svd.getNorm() > 0.0);
        assertTrue(svd.getConditionNumber() > 0.0);
        assertEquals(2, svd.getRank());

        double[] singularValues = svd.getSingularValues();
        assertEquals(2, singularValues.length);
        assertTrue(singularValues[0] >= singularValues[1]);
        assertTrue(singularValues[1] >= 0.0);

        RealMatrix cov = svd.getCovariance(0.1);
        assertNotNull(cov);
    }

    @Test
    public void testSvdSquareMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {2.0, 1.0},
            {1.0, 3.0}
        });

        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        RealMatrix cachedV = svd.getVT();
        assertNotNull(cachedV);

        DecompositionSolver solver = svd.getSolver();
        assertNotNull(solver);
        assertTrue(solver.isNonSingular());

        RealVector b = new ArrayRealVector(new double[] {1.0, 1.0});
        RealVector x = solver.solve(b);
        assertNotNull(x);
    }

    @Test
    public void testSvdMoreColumnsThanRows() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0}
        });

        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        assertNotNull(svd.getU());
        assertNotNull(svd.getS());
        assertNotNull(svd.getV());
        assertEquals(2, svd.getSingularValues().length);
    }

    @Test
    public void testZeroMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {0.0, 0.0},
            {0.0, 0.0}
        });

        SingularValueDecomposition svd = new SingularValueDecompositionImpl(matrix);
        assertEquals(0.0, svd.getNorm(), 1e-14);
        assertEquals(0, svd.getRank());
    }

    @Test
    public void testGetInverseVector() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1.0, 0.0},
            {0.0, 2.0}
        });

        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(matrix);
        DecompositionSolver solver = svd.getSolver();
        RealVector b = new ArrayRealVector(new double[] {2.0, 4.0});
        RealVector x = solver.solve(b);
        assertEquals(2.0, x.getEntry(0), 1e-12);
        assertEquals(2.0, x.getEntry(1), 1e-12);
    }
}