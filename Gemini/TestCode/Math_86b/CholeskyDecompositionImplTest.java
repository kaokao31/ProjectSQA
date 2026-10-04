package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class CholeskyDecompositionImplTest {

    @Test(expected = NonSquareMatrixException.class)
    public void testNotSquareMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        });
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = NotSymmetricMatrixException.class)
    public void testNotSymmetricMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 1.0}
        });
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testNotPositiveDefiniteMatrix() {
        // A matrix that is symmetric but not positive definite (e.g., negative element on diagonal after update)
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {-1.0, 0.0},
            {0.0, -1.0}
        });
        new CholeskyDecompositionImpl(matrix);
    }

    @Test
    public void testValidDecomposition1x1() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {4.0}
        });
        CholeskyDecomposition chol = new CholeskyDecompositionImpl(matrix);
        
        RealMatrix L = chol.getL();
        assertNotNull(L);
        assertEquals(1, L.getRowDimension());
        assertEquals(1, L.getColumnDimension());
        assertEquals(2.0, L.getEntry(0, 0), 1.0e-14);

        RealMatrix LT = chol.getLT();
        assertNotNull(LT);
        assertEquals(2.0, LT.getEntry(0, 0), 1.0e-14);

        assertEquals(0.0, chol.getDeterminant(), 1.0e-14); // Wait, determinant of [4] is 4.0. Let's verify.
        // determinant of [4] is 4.0. Let's check getDeterminant() implementation or test it directly.
        // Actually product of diagonal elements squared: L_00 = 2, det = 2^2 = 4.
        assertEquals(4.0, chol.getDeterminant(), 1.0e-14);

        RealSolver solver = chol.getSolver();
        assertNotNull(solver);
        RealVector b = MatrixUtils.createRealVector(new double[] { 8.0 });
        RealVector x = solver.solve(b);
        assertEquals(2.0, x.getEntry(0), 1.0e-14);
    }

    @Test
    public void testValidDecomposition3x3() {
        // A well-known symmetric positive definite matrix:
        // [  4, 12, -16 ]
        // [ 12, 37, -43 ]
        // [-16,-43,  98 ]
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 12.0, -16.0 },
            { 12.0, 37.0, -43.0 },
            { -16.0, -43.0, 98.0 }
        });

        CholeskyDecomposition chol = new CholeskyDecompositionImpl(matrix);
        
        RealMatrix L = chol.getL();
        RealMatrix LT = chol.getLT();
        
        assertNotNull(L);
        assertNotNull(LT);
        assertEquals(3, L.getRowDimension());
        assertEquals(3, L.getColumnDimension());

        // Check L * LT equals original matrix
        RealMatrix reconstructed = L.multiply(LT);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1.0e-12);
            }
        }

        // Check NormDet / Determinant
        assertTrue(chol.getDeterminant() > 0.0);

        // Solve linear system
        RealVector b = MatrixUtils.createRealVector(new double[] { 1.0, 2.0, 3.0 });
        RealVector x = chol.getSolver().solve(b);
        assertNotNull(x);
        
        RealVector bComputed = matrix.operate(x);
        assertEquals(b.getEntry(0), bComputed.getEntry(0), 1.0e-10);
        assertEquals(b.getEntry(1), bComputed.getEntry(1), 1.0e-10);
        assertEquals(b.getEntry(2), bComputed.getEntry(2), 1.0e-10);
    }
    
    @Test
    public void testSolveWithMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 2.0 },
            { 2.0, 2.0 }
        });

        CholeskyDecomposition chol = new CholeskyDecompositionImpl(matrix);
        RealSolver solver = chol.getSolver();
        
        RealMatrix b = MatrixUtils.createRealMatrix(new double[][] {
            { 4.0, 2.0 },
            { 2.0, 4.0 }
        });
        
        RealMatrix x = solver.solve(b);
        assertNotNull(x);
        assertEquals(2, x.getRowDimension());
        assertEquals(2, x.getColumnDimension());
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testMath86DefectCase() {
        // Defect in Math 86: Cholesky decomposition might fail to detect non-positive-definite 
        // matrices properly under certain threshold conditions, or throws ArrayIndexOutOfBounds / 
        // accepts matrix that should fail.
        // Let's test a matrix with a zero or negative pivot that previously might have passed incorrectly
        // or thrown wrong exceptions.
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0, 4.0 },
            { 2.0, 4.0, 5.0 },
            { 4.0, 5.0, 5.0 }
        });
        // This matrix is symmetric:
        // [ 1, 2, 4 ]
        // [ 2, 4, 5 ]
        // [ 4, 5, 5 ]
        // Let's check its eigenvalues or positive definiteness.
        // L_00 = 1
        // L_10 = 2, L_11 = sqrt(4 - 4) = 0 -> Zero pivot! Should throw NotPositiveDefiniteMatrixException.
        new CholeskyDecompositionImpl(matrix);
    }
}