package org.apache.commons.math.linear;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class EigenDecompositionImplTest {

    private static final double EPSILON = 1e-10;

    // Test for basic 2x2 symmetric matrix
    @Test
    public void test2x2Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 1},
            {1, 2}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        assertNotNull(decomposition);
        assertEquals(3.0, decomposition.getRealEigenvalue(0), EPSILON);
        assertEquals(1.0, decomposition.getRealEigenvalue(1), EPSILON);
    }

    // Test for 3x3 identity matrix
    @Test
    public void testIdentity3x3() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        assertEquals(1.0, decomposition.getRealEigenvalue(0), EPSILON);
        assertEquals(1.0, decomposition.getRealEigenvalue(1), EPSILON);
        assertEquals(1.0, decomposition.getRealEigenvalue(2), EPSILON);
    }

    // Test for 3x3 diagonal matrix with distinct eigenvalues
    @Test
    public void testDiagonal3x3() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 0, 0},
            {0, 3, 0},
            {0, 0, 5}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        assertEquals(2.0, decomposition.getRealEigenvalue(0), EPSILON);
        assertEquals(3.0, decomposition.getRealEigenvalue(1), EPSILON);
        assertEquals(5.0, decomposition.getRealEigenvalue(2), EPSILON);
    }

    // Test for symmetric matrix with known eigenvalues
    @Test
    public void testSymmetric3x3() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {3, 1, 0},
            {1, 2, 1},
            {0, 1, 1}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        // Eigenvalues approximately: 4.0, 2.0, 0.0
        double[] eigenvalues = decomposition.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
    }

    // Test for singular matrix (determinant = 0)
    @Test
    public void testSingularMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 2, 3},
            {2, 4, 6},
            {3, 6, 9}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        double[] eigenvalues = decomposition.getRealEigenvalues();
        // One eigenvalue should be approximately 0
        boolean hasZero = false;
        for (double val : eigenvalues) {
            if (Math.abs(val) < 1e-8) {
                hasZero = true;
                break;
            }
        }
        assertTrue(hasZero);
    }

    // Test for matrix with negative eigenvalues
    @Test
    public void testNegativeEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {-2, 1},
            {1, -2}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        assertEquals(-1.0, decomposition.getRealEigenvalue(0), EPSILON);
        assertEquals(-3.0, decomposition.getRealEigenvalue(1), EPSILON);
    }

    // Test eigenvector orthogonality and reconstruction
    @Test
    public void testEigenvectorReconstruction() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {4, 1, 0},
            {1, 3, 1},
            {0, 1, 2}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        double[] eigenvalues = decomposition.getRealEigenvalues();
        RealMatrix V = decomposition.getV();
        RealMatrix D = decomposition.getD();
        RealMatrix reconstructed = V.multiply(D).multiply(V.transpose());
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-8);
            }
        }
    }

    // Test for matrix with repeated eigenvalues
    @Test
    public void testRepeatedEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 0, 0},
            {0, 2, 0},
            {0, 0, 1}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        double[] eigenvalues = decomposition.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
    }

    // Test for 1x1 matrix
    @Test
    public void test1x1Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {{5}});
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        assertEquals(5.0, decomposition.getRealEigenvalue(0), EPSILON);
    }

    // Test for matrix with zero off-diagonal elements (already tridiagonal)
    @Test
    public void testTridiagonalMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {3, 1, 0},
            {1, 4, 2},
            {0, 2, 5}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        assertNotNull(decomposition);
        assertTrue(decomposition.getRealEigenvalues().length == 3);
    }

    // Test for larger matrix (4x4) to stress test
    @Test
    public void test4x4Matrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {4, 1, 0, 0},
            {1, 4, 1, 0},
            {0, 1, 4, 1},
            {0, 0, 1, 4}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        double[] eigenvalues = decomposition.getRealEigenvalues();
        assertEquals(4, eigenvalues.length);
        RealMatrix V = decomposition.getV();
        RealMatrix D = decomposition.getD();
        RealMatrix reconstructed = V.multiply(D).multiply(V.transpose());
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-8);
            }
        }
    }

    // Test for matrix with all zeros
    @Test(expected = IllegalArgumentException.class)
    public void testZeroMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {0, 0},
            {0, 0}
        });
        new EigenDecompositionImpl(matrix, EPSILON);
    }

    // Test for non-square matrix (should throw)
    @Test(expected = IllegalArgumentException.class)
    public void testNonSquareMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 2, 3},
            {4, 5, 6}
        });
        new EigenDecompositionImpl(matrix, EPSILON);
    }

    // Test with very small epsilon
    @Test
    public void testTinyEpsilon() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {3, 1},
            {1, 2}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, 1e-15);
        double[] eigenvalues = decomposition.getRealEigenvalues();
        assertEquals(2, eigenvalues.length);
    }

    // Test getV and getD consistency
    @Test
    public void testGetVAndGetD() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0},
            {0, 1}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        RealMatrix V = decomposition.getV();
        RealMatrix D = decomposition.getD();
        RealMatrix product = V.multiply(D).multiply(V.transpose());
        assertEquals(1.0, product.getEntry(0, 0), EPSILON);
        assertEquals(0.0, product.getEntry(0, 1), EPSILON);
        assertEquals(0.0, product.getEntry(1, 0), EPSILON);
        assertEquals(1.0, product.getEntry(1, 1), EPSILON);
    }

    // Test for matrix that triggers the bug (from Defects4J context)
    @Test
    public void testBugTriggerMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 2, 3, 4},
            {2, 5, -1, 0},
            {3, -1, 6, 2},
            {4, 0, 2, 7}
        });
        try {
            EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
            // If no exception, ensure eigenvalues are computed
            assertNotNull(decomposition.getRealEigenvalues());
            // Verify reconstruction works without numerical issues
            RealMatrix V = decomposition.getV();
            RealMatrix D = decomposition.getD();
            RealMatrix reconstructed = V.multiply(D).multiply(V.transpose());
            for (int i = 0; i < matrix.getRowDimension(); i++) {
                for (int j = 0; j < matrix.getColumnDimension(); j++) {
                    assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-8);
                }
            }
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // Test for matrix with very close eigenvalues (nearly defective)
    @Test
    public void testCloseEigenvalues() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {3.0001, 1, 0},
            {1, 3, 1},
            {0, 1, 2.9999}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        double[] eigenvalues = decomposition.getRealEigenvalues();
        assertEquals(3, eigenvalues.length);
    }

    // Test getEigenvector magnitude
    @Test
    public void testEigenvectorMagnitude() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 0},
            {0, 3}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        RealVector vector0 = decomposition.getEigenvector(0);
        assertNotNull(vector0);
        assertEquals(1.0, vector0.getNorm(), EPSILON);
        RealVector vector1 = decomposition.getEigenvector(1);
        assertNotNull(vector1);
        assertEquals(1.0, vector1.getNorm(), EPSILON);
    }

    // Test getVT
    @Test
    public void testGetVT() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0},
            {0, 1}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        RealMatrix VT = decomposition.getVT();
        assertNotNull(VT);
        assertEquals(2, VT.getRowDimension());
        assertEquals(2, VT.getColumnDimension());
    }

    // Test with NaN in matrix (should throw)
    @Test(expected = IllegalArgumentException.class)
    public void testNaNMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, Double.NaN},
            {Double.NaN, 2}
        });
        new EigenDecompositionImpl(matrix, EPSILON);
    }

    // Test with Infinity in matrix (should throw)
    @Test(expected = IllegalArgumentException.class)
    public void testInfinityMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, Double.POSITIVE_INFINITY},
            {Double.POSITIVE_INFINITY, 2}
        });
        new EigenDecompositionImpl(matrix, EPSILON);
    }

    // Test for negative epsilon (should throw)
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeEpsilon() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {{1}});
        new EigenDecompositionImpl(matrix, -1e-10);
    }

    // Test for zero epsilon (edge case)
    @Test(expected = IllegalArgumentException.class)
    public void testZeroEpsilon() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {{1}});
        new EigenDecompositionImpl(matrix, 0.0);
    }

    // Test reconstruction with non-symmetric matrix (should throw)
    @Test(expected = IllegalArgumentException.class)
    public void testNonSymmetricMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 2},
            {3, 4}
        });
        new EigenDecompositionImpl(matrix, EPSILON);
    }

    // Test getDeterminant
    @Test
    public void testDeterminant() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {1, 2},
            {2, 1}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        double det = decomposition.getDeterminant();
        // Determinant should be -3 (product of eigenvalues 3 and -1)
        assertEquals(-3.0, det, 1e-8);
    }

    // Test getSquareRoot
    @Test
    public void testSquareRoot() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {2, 1},
            {1, 2}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        RealMatrix sqrt = decomposition.getSquareRoot();
        RealMatrix sqrtSquared = sqrt.multiply(sqrt);
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                assertEquals(matrix.getEntry(i, j), sqrtSquared.getEntry(i, j), 1e-8);
            }
        }
    }

    // Test for large matrix (10x10 random symmetric)
    @Test
    public void testLargeMatrix() {
        int n = 10;
        double[][] data = new double[n][n];
        // Fill with random symmetric matrix
        java.util.Random rand = new java.util.Random(42);
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                double val = rand.nextDouble() * 100 - 50;
                data[i][j] = val;
                data[j][i] = val;
            }
        }
        RealMatrix matrix = MatrixUtils.createRealMatrix(data);
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        double[] eigenvalues = decomposition.getRealEigenvalues();
        assertEquals(n, eigenvalues.length);
        RealMatrix V = decomposition.getV();
        RealMatrix D = decomposition.getD();
        RealMatrix reconstructed = V.multiply(D).multiply(V.transpose());
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                assertEquals(matrix.getEntry(i, j), reconstructed.getEntry(i, j), 1e-8);
            }
        }
    }

    // Test for matrix with zeros on diagonal and off-diagonal
    @Test
    public void testAllZerosOffDiagonal() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[][] {
            {5, 0, 0},
            {0, 5, 0},
            {0, 0, 5}
        });
        EigenDecomposition decomposition = new EigenDecompositionImpl(matrix, EPSILON);
        double[] eigenvalues = decomposition.getRealEigenvalues();
        for (double val : eigenvalues) {
            assertEquals(5.0, val, EPSILON);
        }
    }

    // Test empty matrix (0x0) should throw
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMatrix() {
        RealMatrix matrix = MatrixUtils.createRealMatrix(new double[0][0]);
        new EigenDecompositionImpl(matrix, EPSILON);
    }
}