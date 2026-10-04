package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class EigenDecompositionImplTest {

    @Test
    public void testMath81BugScenario() {
        // This test specifically targets the EigenDecompositionImpl bug in Commons Math 81 (Defects4J Math 81).
        // In Math 81, when dealing with certain tridiagonal matrices (specifically when 
        // work[iStart + 4 * mainStep - 1] or similar shift/split conditions occur), 
        // an ArrayIndexOutOfBoundsException or incorrect eigenvalue computation happens 
        // due to off-by-one errors in secondary/main diagonal loops and work array indexing.
        
        // Construct a matrix known to expose issues in the decomposition/eigenvalues for Math 81.
        double[] lower = new double[] { 2.0, 3.0, 4.0, 5.0, 6.0 };
        double[] diag  = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0 };
        
        TriDiagonalTransformer transformer = new TriDiagonalTransformer(
            new Array2DRowRealMatrix(new double[][] {
                {1, 2, 0, 0, 0, 0},
                {2, 2, 3, 0, 0, 0},
                {0, 3, 3, 4, 0, 0},
                {0, 0, 4, 4, 5, 0},
                {0, 0, 0, 5, 5, 6},
                {0, 0, 0, 0, 6, 6}
            })
        );
        
        try {
            EigenDecompositionImpl ed = new EigenDecompositionImpl(
                transformer.getMainDiagonal(),
                transformer.getSecondaryDiagonal(),
                0.0
            );
            double[] realEigenvalues = ed.getRealEigenvalues();
            assertNotNull(realEigenvalues);
            assertEquals(6, realEigenvalues.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("Math 81 Bug triggered: ArrayIndexOutOfBoundsException in EigenDecompositionImpl: " + e.getMessage());
        }
    }

    @Test
    public void testIdentityMatrixEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(3);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 0.0);
        
        double[] eigenvalues = ed.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
        for (double ev : eigenvalues) {
            assertEquals(1.0, ev, 1e-12);
        }
        
        RealMatrix v = ed.getV();
        assertNotNull(v);
        RealMatrix d = ed.getD();
        assertNotNull(d);
        RealMatrix vt = ed.getVT();
        assertNotNull(vt);
        
        assertNotNull(ed.getDeterminant());
        assertEquals(1.0, ed.getDeterminant(), 1e-12);
    }

    @Test
    public void testSymmetricMatrixDecomposition() {
        double[][] data = {
            {4.0, 1.0, -2.0},
            {1.0, 2.0,  0.0},
            {-2.0, 0.0, 3.0}
        };
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 0.0);
        
        RealMatrix V = ed.getV();
        RealMatrix D = ed.getD();
        
        // V * D * V^T should equal the original matrix
        RealMatrix reconstructed = V.multiply(D).multiply(ed.getVT());
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-10);
            }
        }
    }

    @Test(expected = InvalidMatrixException.class)
    public void testNonSymmetricMatrixThrowsException() {
        double[][] data = {
            {1.0, 2.0},
            {3.0, 4.0}
        };
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        // EigenDecompositionImpl requires symmetric matrix by default (or check)
        new EigenDecompositionImpl(matrix, 1e-14);
    }

    @Test
    public void testSolversAndInverse() {
        double[][] data = {
            {2.0, 0.0},
            {0.0, 3.0}
        };
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 0.0);
        
        DecompositionSolver solver = ed.getSolver();
        assertTrue(solver.isNonSingular());
        
        RealVector b = new ArrayRealVector(new double[] {2.0, 9.0});
        RealVector x = solver.solve(b);
        assertEquals(1.0, x.getEntry(0), 1e-12);
        assertEquals(3.0, x.getEntry(1), 1e-12);
    }

    @Test
    public void testImaginaryEigenvalues() {
        // Construct a case where eigenvalues might have imaginary parts or specific off-diagonals
        double[] main = {2.0, 2.0, 2.0};
        double[] secondary = {1.0, 1.0};
        
        EigenDecompositionImpl ed = new EigenDecompositionImpl(main, secondary, 0.0);
        assertNotNull(ed.getRealEigenvalues());
        assertNotNull(ed.getImagEigenvalues());
        assertEquals(3, ed.getImagEigenvalues().length);
    }

    @Test
    public void testVectorSolving() {
        double[][] data = {
            {2.0, 1.0},
            {1.0, 2.0}
        };
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix, 0.0);
        
        DecompositionSolver solver = ed.getSolver();
        RealMatrix identity = MatrixUtils.createRealIdentityMatrix(2);
        RealMatrix inverse = solver.getInverse();
        
        RealMatrix product = matrix.multiply(inverse);
        assertEquals(1.0, product.getEntry(0, 0), 1e-10);
        assertEquals(0.0, product.getEntry(0, 1), 1e-10);
        assertEquals(0.0, product.getEntry(1, 0), 1e-10);
        assertEquals(1.0, product.getEntry(1, 1), 1e-10);
    }
}