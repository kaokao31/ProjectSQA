package org.apache.commons.math3.linear;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Test suite for RectangularCholeskyDecomposition.
 * Covers normal cases, edge cases, and fault detection.
 */
public class RectangularCholeskyDecompositionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final double DEFAULT_TOLERANCE = 1e-10;

    // Helper to check decomposition: B * B^T should equal original matrix
    private void checkDecomposition(RealMatrix a, RectangularCholeskyDecomposition dec) {
        RealMatrix b = dec.getRootMatrix();
        RealMatrix reconstructed = b.multiply(b.transpose());
        double maxDiff = 0.0;
        for (int i = 0; i < a.getRowDimension(); i++) {
            for (int j = 0; j < a.getColumnDimension(); j++) {
                double diff = Math.abs(a.getEntry(i, j) - reconstructed.getEntry(i, j));
                if (diff > maxDiff) maxDiff = diff;
            }
        }
        Assert.assertTrue("Reconstruction error too large: " + maxDiff, maxDiff < 1e-8);
    }

    @Test
    public void testSquarePositiveDefinite() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 4, 2, 1 },
            { 2, 5, 3 },
            { 1, 3, 6 }
        });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        // Factor should be lower triangular? Actually rectangular Cholesky returns a rectangular matrix
        // For square positive definite, it should be square lower triangular
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(a.getRowDimension(), b.getRowDimension());
        Assert.assertEquals(a.getColumnDimension(), b.getColumnDimension());
        // Check lower triangular property
        for (int i = 0; i < b.getRowDimension(); i++) {
            for (int j = i+1; j < b.getColumnDimension(); j++) {
                Assert.assertEquals(0.0, b.getEntry(i, j), 1e-12);
            }
        }
    }

    @Test
    public void testRectangularFactor() {
        // 3x3 positive semidefinite with rank 2
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 1, 0 },
            { 1, 1, 0 },
            { 0, 0, 0 }
        });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        // Factor should be 3x2 (since rank 2)
        Assert.assertEquals(3, b.getRowDimension());
        Assert.assertEquals(2, b.getColumnDimension());
    }

    @Test
    public void testRankDeficient() {
        // 2x2 matrix with rank 1
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 1 },
            { 1, 1 }
        });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(2, b.getRowDimension());
        Assert.assertEquals(1, b.getColumnDimension());
    }

    @Test
    public void testZeroMatrix() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 0, 0 },
            { 0, 0 }
        });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(2, b.getRowDimension());
        Assert.assertEquals(0, b.getColumnDimension()); // rank 0 -> empty factor
    }

    @Test
    public void test1x1Positive() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] { { 5 } });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(1, b.getRowDimension());
        Assert.assertEquals(1, b.getColumnDimension());
        Assert.assertEquals(Math.sqrt(5), b.getEntry(0, 0), 1e-12);
    }

    @Test
    public void test1x1Zero() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] { { 0 } });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(1, b.getRowDimension());
        Assert.assertEquals(0, b.getColumnDimension());
    }

    @Test(expected = NonSquareMatrixException.class)
    public void testNonSquareMatrix() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 2 },
            { 3, 4 },
            { 5, 6 }
        });
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test(expected = NonSymmetricMatrixException.class)
    public void testNonSymmetricMatrix() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 2 },
            { 3, 4 }
        });
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testNotPositiveDefinite() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { -1, 0 },
            { 0, 1 }
        });
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testIndefiniteMatrix() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 2 },
            { 2, 1 }
        });
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test(expected = NullArgumentException.class)
    public void testNullMatrix() {
        new RectangularCholeskyDecomposition(null, DEFAULT_TOLERANCE);
    }

    @Test
    public void testLargeMatrix() {
        int n = 10;
        double[][] data = new double[n][n];
        // Create a positive definite matrix: A = M * M^T + I
        RealMatrix m = MatrixUtils.createRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m.setEntry(i, j, Math.random() - 0.5);
            }
        }
        RealMatrix a = m.multiply(m.transpose()).add(MatrixUtils.createRealIdentityMatrix(n));
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(n, b.getRowDimension());
        Assert.assertEquals(n, b.getColumnDimension());
    }

    @Test
    public void testToleranceEffect() {
        // Matrix with small negative eigenvalue due to rounding
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 1 },
            { 1, 1 + 1e-12 }
        });
        // With default tolerance, should be positive semidefinite
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, 1e-10);
        checkDecomposition(a, dec);
        // With stricter tolerance, might fail
        thrown.expect(NotPositiveDefiniteMatrixException.class);
        new RectangularCholeskyDecomposition(a, 1e-15);
    }

    @Test
    public void testMultipleCalls() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 9, 3, 0 },
            { 3, 5, 1 },
            { 0, 1, 2 }
        });
        RectangularCholeskyDecomposition dec1 = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        RectangularCholeskyDecomposition dec2 = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        // Ensure both produce same factor
        RealMatrix b1 = dec1.getRootMatrix();
        RealMatrix b2 = dec2.getRootMatrix();
        for (int i = 0; i < b1.getRowDimension(); i++) {
            for (int j = 0; j < b1.getColumnDimension(); j++) {
                Assert.assertEquals(b1.getEntry(i, j), b2.getEntry(i, j), 1e-12);
            }
        }
    }

    @Test
    public void testGetRootMatrixDimensions() {
        // 4x4 rank 2
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0, 0, 0 },
            { 0, 1, 0, 0 },
            { 0, 0, 0, 0 },
            { 0, 0, 0, 0 }
        });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(4, b.getRowDimension());
        Assert.assertEquals(2, b.getColumnDimension());
    }

    @Test
    public void testNegativeTolerance() {
        // Tolerance should be positive; if negative, behavior? Probably treated as absolute value or throws.
        // We'll test that it still works with negative tolerance (might be bug)
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0 },
            { 0, 1 }
        });
        // Using negative tolerance - should it work? The code might use Math.abs or throw.
        // We'll just ensure no exception for this case.
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, -1e-10);
        checkDecomposition(a, dec);
    }

    @Test
    public void testVerySmallTolerance() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0 },
            { 0, 1e-20 }
        });
        // With very small tolerance, the second eigenvalue might be considered zero
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, 1e-15);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        // Should have rank 1 because second eigenvalue is below tolerance
        Assert.assertEquals(2, b.getRowDimension());
        Assert.assertEquals(1, b.getColumnDimension());
    }

    @Test
    public void testDiagonalMatrix() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 4, 0, 0 },
            { 0, 9, 0 },
            { 0, 0, 16 }
        });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(3, b.getRowDimension());
        Assert.assertEquals(3, b.getColumnDimension());
        // Diagonal entries should be sqrt of original
        Assert.assertEquals(2.0, b.getEntry(0, 0), 1e-12);
        Assert.assertEquals(3.0, b.getEntry(1, 1), 1e-12);
        Assert.assertEquals(4.0, b.getEntry(2, 2), 1e-12);
    }

    @Test
    public void testIdentityMatrix() {
        RealMatrix a = MatrixUtils.createRealIdentityMatrix(5);
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(5, b.getRowDimension());
        Assert.assertEquals(5, b.getColumnDimension());
        // Should be identity
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == j) {
                    Assert.assertEquals(1.0, b.getEntry(i, j), 1e-12);
                } else {
                    Assert.assertEquals(0.0, b.getEntry(i, j), 1e-12);
                }
            }
        }
    }

    @Test
    public void testRandomPositiveDefinite() {
        // Generate random positive definite matrix
        int n = 6;
        RealMatrix m = MatrixUtils.createRealMatrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m.setEntry(i, j, Math.random() - 0.5);
            }
        }
        RealMatrix a = m.multiply(m.transpose()).add(MatrixUtils.createRealIdentityMatrix(n).scalarMultiply(0.1));
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        checkDecomposition(a, dec);
    }

    @Test
    public void testSingularWithNegativeEigenvalue() {
        // Matrix with one negative eigenvalue (should fail)
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 2, 0 },
            { 2, 4, 0 },
            { 0, 0, -1 }
        });
        thrown.expect(NotPositiveDefiniteMatrixException.class);
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test
    public void testEmptyMatrix() {
        // 0x0 matrix
        RealMatrix a = MatrixUtils.createRealMatrix(new double[0][0]);
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(0, b.getRowDimension());
        Assert.assertEquals(0, b.getColumnDimension());
    }

    @Test
    public void test1x2NonSquare() {
        // Non-square should throw
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] { { 1, 2 } });
        thrown.expect(NonSquareMatrixException.class);
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test
    public void testAsymmetricWithSmallDifference() {
        // Slightly asymmetric matrix
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 1.0000000001 },
            { 1.0000000002, 2 }
        });
        thrown.expect(NonSymmetricMatrixException.class);
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test
    public void testSymmetricButNotPositiveSemidefinite() {
        // Symmetric with negative eigenvalue
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 2 },
            { 2, 1 }
        });
        thrown.expect(NotPositiveDefiniteMatrixException.class);
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test
    public void testLargeTolerance() {
        // With large tolerance, a nearly singular matrix may be considered rank deficient
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 1 },
            { 1, 1 + 1e-6 }
        });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, 1e-4);
        RealMatrix b = dec.getRootMatrix();
        // Should have rank 1 because second eigenvalue is below tolerance
        Assert.assertEquals(2, b.getRowDimension());
        Assert.assertEquals(1, b.getColumnDimension());
    }

    @Test
    public void testZeroTolerance() {
        // Zero tolerance should treat all positive eigenvalues as non-zero
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { 1, 0 },
            { 0, 1e-12 }
        });
        RectangularCholeskyDecomposition dec = new RectangularCholeskyDecomposition(a, 0.0);
        // The small eigenvalue is positive, so it should be included
        RealMatrix b = dec.getRootMatrix();
        Assert.assertEquals(2, b.getRowDimension());
        Assert.assertEquals(2, b.getColumnDimension());
        checkDecomposition(a, dec);
    }

    @Test
    public void testNaNInMatrix() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { Double.NaN, 0 },
            { 0, 1 }
        });
        // Should throw some exception (IllegalArgumentException or MathRuntimeException)
        thrown.expect(IllegalArgumentException.class);
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }

    @Test
    public void testInfinityInMatrix() {
        RealMatrix a = MatrixUtils.createRealMatrix(new double[][] {
            { Double.POSITIVE_INFINITY, 0 },
            { 0, 1 }
        });
        thrown.expect(IllegalArgumentException.class);
        new RectangularCholeskyDecomposition(a, DEFAULT_TOLERANCE);
    }
}