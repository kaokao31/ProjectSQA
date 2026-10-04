package org.apache.commons.math3.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class RectangularCholeskyDecompositionTest {

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testNotPositiveDefinite() {
        // A matrix that is not positive definite (negative eigenvalue / root)
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 2.0 },
            { 2.0, 1.0 }
        });
        new RectangularCholeskyDecomposition(matrix, 0.0);
    }

    @Test
    public void testIdentityMatrix() {
        RealMatrix matrix = MatrixUtils.createRealIdentityMatrix(3);
        RectangularCholeskyDecomposition decomposition = new RectangularCholeskyDecomposition(matrix, 0.0);
        RealMatrix root = decomposition.getRootMatrix();
        int rank = decomposition.getRank();

        assertEquals(3, rank);
        assertNotNull(root);
        
        // Check B * B^T == A
        RealMatrix rebuilt = root.multiply(root.transpose());
        for (int i = 0; i < 3; i++) {
            for  (int j = 0; j < 3; j++) {
                assertEquals(matrix.getEntry(i, j), rebuilt.getEntry(i, j), 1e-10);
            }
        }
    }

    @Test
    public void testRankDeficientMatrix() {
        // Rank 1 matrix, size 2x2
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1.0, 1.0 },
            { 1.0, 1.0 }
        });
        RectangularCholeskyDecomposition decomposition = new RectangularCholeskyDecomposition(matrix, 1e-10);
        
        assertEquals(1, decomposition.getRank());
        RealMatrix root = decomposition.getRootMatrix();
        
        RealMatrix rebuilt = root.multiply(root.transpose());
        assertEquals(matrix.getEntry(0, 0), rebuilt.getEntry(0, 0), 1e-10);
        assertEquals(matrix.getEntry(0, 1), rebuilt.getEntry(0, 1), 1e-10);
        assertEquals(matrix.getEntry(1, 0), rebuilt.getEntry(1, 0), 1e-10);
        assertEquals(matrix.getEntry(1, 1), rebuilt.getEntry(1, 1), 1e-10);
    }

    @Test
    public void testZeroMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 0.0 },
            { 0.0, 0.0 }
        });
        RectangularCholeskyDecomposition decomposition = new RectangularCholeskyDecomposition(matrix, 1e-10);
        assertEquals(0, decomposition.getRank());
        RealMatrix root = decomposition.getRootMatrix();
        assertEquals(2, root.getRowDimension());
        assertEquals(0, root.getColumnDimension());
    }

    @Test(expected = NonPositiveDefiniteMatrixException.class)
    public void testSmallNegativePivotWithTolerance() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 1e-15, 0.0 },
            { 0.0, -1.0 }
        });
        new RectangularCholeskyDecomposition(matrix, 0.0);
    }

    @Test
    public void testPermutationVector() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            { 0.0, 0.0, 0.0 },
            { 0.0, 2.0, 2.0 },
            { 0.0, 2.0, 2.0 }
        });
        RectangularCholeskyDecomposition decomposition = new RectangularCholeskyDecomposition(matrix, 1e-10);
        int[] perm = decomposition.getPermutation();
        assertNotNull(perm);
        assertEquals(3, perm.length);
    }
}