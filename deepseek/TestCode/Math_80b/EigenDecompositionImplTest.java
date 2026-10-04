package org.apache.commons.math.linear;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for EigenDecompositionImpl targeting high coverage and fault detection.
 */
public class EigenDecompositionImplTest {

    private static final double EPSILON = 1e-10;

    // Helper to check that A * v = lambda * v
    private void checkEigenvector(RealMatrix matrix, double eigenvalue, RealVector eigenvector) {
        RealVector left = matrix.operate(eigenvector);
        RealVector right = eigenvector.mapMultiply(eigenvalue);
        double[] diff = left.subtract(right).getData();
        for (double d : diff) {
            assertEquals(0.0, d, EPSILON);
        }
    }

    // Helper to check that eigenvectors are orthogonal (for symmetric matrix)
    private void checkOrthogonality(RealMatrix v) {
        int n = v.getColumnDimension();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dot = 0.0;
                for (int k = 0; k < n; k++) {
                    dot += v.getEntry(k, i) * v.getEntry(k, j);
                }
                assertEquals(0.0, dot, EPSILON);
            }
        }
    }

    @Test
    public void testIdentityMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        assertArrayEquals(new double[] {1, 1, 1}, eigenvalues, EPSILON);
        RealMatrix v = ed.getV();
        // Check A*V = V*D
        RealMatrix d = ed.getD();
        RealMatrix product = matrix.multiply(v);
        RealMatrix expected = v.multiply(d);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(product.getEntry(i, j), expected.getEntry(i, j), EPSILON);
            }
        }
        checkOrthogonality(v);
    }

    @Test
    public void testDiagonalMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {2, 0, 0},
            {0, -1, 0},
            {0, 0, 5}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        // Order may vary; sort to compare
        java.util.Arrays.sort(eigenvalues);
        assertArrayEquals(new double[] {-1, 2, 5}, eigenvalues, EPSILON);
        // Check eigenvectors are standard basis
        RealMatrix v = ed.getV();
        for (int i = 0; i < 3; i++) {
            double[] col = v.getColumn(i);
            // Each column should be a unit vector with one entry ±1
            double sum = 0;
            for (double d : col) sum += Math.abs(d);
            assertEquals(1.0, sum, EPSILON);
        }
    }

    @Test
    public void testSymmetricMatrixWithKnownEigenvalues() {
        // Matrix: [[2,1],[1,2]] eigenvalues 3 and 1
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {2, 1},
            {1, 2}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        java.util.Arrays.sort(eigenvalues);
        assertArrayEquals(new double[] {1, 3}, eigenvalues, EPSILON);
        // Check eigenvectors
        RealVector v0 = ed.getEigenvector(0);
        RealVector v1 = ed.getEigenvector(1);
        checkEigenvector(matrix, eigenvalues[0], v0);
        checkEigenvector(matrix, eigenvalues[1], v1);
        checkOrthogonality(ed.getV());
    }

    @Test
    public void testSingularMatrix() {
        // Matrix with zero eigenvalue: [[1,1],[1,1]]
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1, 1},
            {1, 1}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        java.util.Arrays.sort(eigenvalues);
        assertArrayEquals(new double[] {0, 2}, eigenvalues, EPSILON);
        // Check eigenvector for zero eigenvalue
        RealVector v0 = ed.getEigenvector(0);
        checkEigenvector(matrix, 0, v0);
    }

    @Test
    public void testRepeatedEigenvalues() {
        // Matrix: [[2,0,0],[0,2,0],[0,0,1]] eigenvalues 2 (double) and 1
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {2, 0, 0},
            {0, 2, 0},
            {0, 0, 1}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        java.util.Arrays.sort(eigenvalues);
        assertArrayEquals(new double[] {1, 2, 2}, eigenvalues, EPSILON);
        // Check that eigenvectors for eigenvalue 2 span the correct subspace
        RealVector v1 = ed.getEigenvector(1); // eigenvalue 2
        RealVector v2 = ed.getEigenvector(2); // eigenvalue 2
        checkEigenvector(matrix, 2, v1);
        checkEigenvector(matrix, 2, v2);
        // They should be orthogonal
        assertEquals(0.0, v1.dotProduct(v2), EPSILON);
    }

    @Test
    public void testDimension1() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {{5}});
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        assertArrayEquals(new double[] {5}, eigenvalues, EPSILON);
        RealVector v = ed.getEigenvector(0);
        assertEquals(1.0, v.getEntry(0), EPSILON);
        checkEigenvector(matrix, 5, v);
    }

    @Test
    public void testDimension2WithNegativeEigenvalue() {
        // Matrix: [[0,1],[1,0]] eigenvalues 1 and -1
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {0, 1},
            {1, 0}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        java.util.Arrays.sort(eigenvalues);
        assertArrayEquals(new double[] {-1, 1}, eigenvalues, EPSILON);
        RealVector v0 = ed.getEigenvector(0);
        RealVector v1 = ed.getEigenvector(1);
        checkEigenvector(matrix, -1, v0);
        checkEigenvector(matrix, 1, v1);
        checkOrthogonality(ed.getV());
    }

    @Test
    public void testRandomSymmetricMatrix() {
        // Create a random symmetric 4x4 matrix
        double[][] data = new double[4][4];
        java.util.Random rand = new java.util.Random(42);
        for (int i = 0; i < 4; i++) {
            for (int j = i; j < 4; j++) {
                double val = rand.nextDouble() * 10 - 5;
                data[i][j] = val;
                data[j][i] = val;
            }
        }
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        // Check A*V = V*D
        RealMatrix left = matrix.multiply(v);
        RealMatrix right = v.multiply(d);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(left.getEntry(i, j), right.getEntry(i, j), 1e-8);
            }
        }
        checkOrthogonality(v);
        // Check that eigenvalues are sorted (ascending)
        for (int i = 0; i < 3; i++) {
            assertTrue(eigenvalues[i] <= eigenvalues[i+1] + EPSILON);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonSquareMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1, 2, 3},
            {4, 5, 6}
        });
        new EigenDecompositionImpl(matrix);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonSymmetricMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1, 2},
            {3, 4}
        });
        new EigenDecompositionImpl(matrix);
    }

    @Test
    public void testGetD() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {3, 0},
            {0, 7}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        RealMatrix d = ed.getD();
        // D should be diagonal with eigenvalues
        double[] eigenvalues = ed.getEigenvalues();
        for (int i = 0; i < 2; i++) {
            assertEquals(eigenvalues[i], d.getEntry(i, i), EPSILON);
            assertEquals(0.0, d.getEntry(i, (i+1)%2), EPSILON);
        }
    }

    @Test
    public void testGetVT() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1, 0},
            {0, 1}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        RealMatrix vt = ed.getVT();
        RealMatrix v = ed.getV();
        // VT should be transpose of V
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(v.getEntry(j, i), vt.getEntry(i, j), EPSILON);
            }
        }
    }

    @Test
    public void testGetEigenvectorIndexOutOfBounds() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {{1}});
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        try {
            ed.getEigenvector(1);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testZeroMatrix() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {0, 0},
            {0, 0}
        });
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        assertArrayEquals(new double[] {0, 0}, eigenvalues, EPSILON);
        // Eigenvectors should be orthogonal
        RealVector v0 = ed.getEigenvector(0);
        RealVector v1 = ed.getEigenvector(1);
        assertEquals(0.0, v0.dotProduct(v1), EPSILON);
        checkEigenvector(matrix, 0, v0);
        checkEigenvector(matrix, 0, v1);
    }

    @Test
    public void testLargeMatrix() {
        // 10x10 symmetric matrix with known structure
        int n = 10;
        double[][] data = new double[n][n];
        for (int i = 0; i < n; i++) {
            data[i][i] = i + 1;
            for (int j = i + 1; j < n; j++) {
                data[i][j] = 0.1;
                data[j][i] = 0.1;
            }
        }
        RealMatrix matrix = new Array2DRowRealMatrix(data);
        EigenDecompositionImpl ed = new EigenDecompositionImpl(matrix);
        double[] eigenvalues = ed.getEigenvalues();
        // Check that eigenvalues are sorted
        for (int i = 0; i < n - 1; i++) {
            assertTrue(eigenvalues[i] <= eigenvalues[i+1] + EPSILON);
        }
        // Check A*V = V*D
        RealMatrix v = ed.getV();
        RealMatrix d = ed.getD();
        RealMatrix left = matrix.multiply(v);
        RealMatrix right = v.multiply(d);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                assertEquals(left.getEntry(i, j), right.getEntry(i, j), 1e-6);
            }
        }
        checkOrthogonality(v);
    }
}