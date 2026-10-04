package org.apache.commons.math.linear;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class CholeskyDecompositionImplTest {

    private RealMatrix testMatrix;
    private RealMatrix nonSquareMatrix;
    private RealMatrix nonSymmetricMatrix;
    private RealMatrix notPositiveDefiniteMatrix;
    private RealMatrix singularMatrix;
    private RealMatrix identityMatrix;
    private RealMatrix zeroMatrix;
    private RealMatrix negativeDiagonalMatrix;
    private RealMatrix smallPositiveDefiniteMatrix;
    private RealMatrix largePositiveDefiniteMatrix;

    @Before
    public void setUp() {
        // 2x2 positive definite matrix
        testMatrix = new Array2DRowRealMatrix(new double[][] {
            {4.0, 2.0},
            {2.0, 3.0}
        }, false);

        // Non-square matrix (3x2)
        nonSquareMatrix = new Array2DRowRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        }, false);

        // Non-symmetric matrix
        nonSymmetricMatrix = new Array2DRowRealMatrix(new double[][] {
            {4.0, 1.0},
            {2.0, 3.0}
        }, false);

        // Not positive definite matrix (determinant <= 0)
        notPositiveDefiniteMatrix = new Array2DRowRealMatrix(new double[][] {
            {1.0, 2.0},
            {2.0, 1.0}
        }, false);

        // Singular matrix (zero determinant)
        singularMatrix = new Array2DRowRealMatrix(new double[][] {
            {1.0, 2.0},
            {2.0, 4.0}
        }, false);

        // Identity matrix (positive definite)
        identityMatrix = new Array2DRowRealMatrix(new double[][] {
            {1.0, 0.0},
            {0.0, 1.0}
        }, false);

        // Zero matrix (not positive definite)
        zeroMatrix = new Array2DRowRealMatrix(new double[][] {
            {0.0, 0.0},
            {0.0, 0.0}
        }, false);

        // Matrix with negative diagonal element
        negativeDiagonalMatrix = new Array2DRowRealMatrix(new double[][] {
            {-1.0, 0.0},
            {0.0, 1.0}
        }, false);

        // Small positive definite matrix (1x1)
        smallPositiveDefiniteMatrix = new Array2DRowRealMatrix(new double[][] {
            {5.0}
        }, false);

        // Large positive definite matrix (3x3)
        largePositiveDefiniteMatrix = new Array2DRowRealMatrix(new double[][] {
            {25.0, 15.0, -5.0},
            {15.0, 18.0, 0.0},
            {-5.0, 0.0, 11.0}
        }, false);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testNotPositiveDefiniteMatrix() {
        new CholeskyDecompositionImpl(notPositiveDefiniteMatrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testSingularMatrix() {
        new CholeskyDecompositionImpl(singularMatrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testZeroMatrix() {
        new CholeskyDecompositionImpl(zeroMatrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testNegativeDiagonalMatrix() {
        new CholeskyDecompositionImpl(negativeDiagonalMatrix);
    }

    @Test(expected = NonSquareMatrixException.class)
    public void testNonSquareMatrix() {
        new CholeskyDecompositionImpl(nonSquareMatrix);
    }

    @Test(expected = NonSquareMatrixException.class)
    public void testNonSymmetricMatrix() {
        new CholeskyDecompositionImpl(nonSymmetricMatrix);
    }

    @Test
    public void testIdentityMatrix() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(identityMatrix);
        RealMatrix l = decomposition.getL();
        RealMatrix lt = decomposition.getLT();

        // L should be identity matrix
        assertEquals(1.0, l.getEntry(0, 0), 1e-12);
        assertEquals(0.0, l.getEntry(0, 1), 1e-12);
        assertEquals(0.0, l.getEntry(1, 0), 1e-12);
        assertEquals(1.0, l.getEntry(1, 1), 1e-12);

        // LT should be identity matrix
        assertEquals(1.0, lt.getEntry(0, 0), 1e-12);
        assertEquals(0.0, lt.getEntry(0, 1), 1e-12);
        assertEquals(0.0, lt.getEntry(1, 0), 1e-12);
        assertEquals(1.0, lt.getEntry(1, 1), 1e-12);
    }

    @Test
    public void testSmallPositiveDefiniteMatrix() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(smallPositiveDefiniteMatrix);
        RealMatrix l = decomposition.getL();
        RealMatrix lt = decomposition.getLT();

        // L should be sqrt(5)
        assertEquals(Math.sqrt(5.0), l.getEntry(0, 0), 1e-12);

        // LT should be same as L for 1x1
        assertEquals(l.getEntry(0, 0), lt.getEntry(0, 0), 1e-12);
    }

    @Test
    public void testLargePositiveDefiniteMatrix() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(largePositiveDefiniteMatrix);
        RealMatrix l = decomposition.getL();
        RealMatrix lt = decomposition.getLT();

        // Verify L * LT = original matrix
        RealMatrix product = l.multiply(lt);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(largePositiveDefiniteMatrix.getEntry(i, j), product.getEntry(i, j), 1e-12);
            }
        }
    }

    @Test
    public void testStandardPositiveDefiniteMatrix() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(testMatrix);
        RealMatrix l = decomposition.getL();
        RealMatrix lt = decomposition.getLT();

        // Verify L * LT = original matrix
        RealMatrix product = l.multiply(lt);
        assertEquals(4.0, product.getEntry(0, 0), 1e-12);
        assertEquals(2.0, product.getEntry(0, 1), 1e-12);
        assertEquals(2.0, product.getEntry(1, 0), 1e-12);
        assertEquals(3.0, product.getEntry(1, 1), 1e-12);

        // Verify L is lower triangular
        assertEquals(0.0, l.getEntry(0, 1), 1e-12);

        // Verify LT is upper triangular
        assertEquals(0.0, lt.getEntry(1, 0), 1e-12);
    }

    @Test
    public void testGetLAndGetLT() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(testMatrix);
        RealMatrix l = decomposition.getL();
        RealMatrix lt = decomposition.getLT();

        // L should be lower triangular
        assertTrue(l.getEntry(0, 1) == 0.0);

        // LT should be transpose of L
        assertEquals(l.getEntry(0, 0), lt.getEntry(0, 0), 1e-12);
        assertEquals(l.getEntry(1, 0), lt.getEntry(0, 1), 1e-12);
        assertEquals(l.getEntry(0, 1), lt.getEntry(1, 0), 1e-12);
        assertEquals(l.getEntry(1, 1), lt.getEntry(1, 1), 1e-12);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testMatrixWithZeroDiagonalElement() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {0.0, 1.0},
            {1.0, 2.0}
        }, false);
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testMatrixWithVerySmallDiagonalElement() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1e-20, 1.0},
            {1.0, 2.0}
        }, false);
        new CholeskyDecompositionImpl(matrix);
    }

    @Test
    public void testMatrixWithOneElement() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {9.0}
        }, false);
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(matrix);
        RealMatrix l = decomposition.getL();
        assertEquals(3.0, l.getEntry(0, 0), 1e-12);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testMatrixWithNegativeOffDiagonal() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1.0, -2.0},
            {-2.0, 5.0}
        }, false);
        new CholeskyDecompositionImpl(matrix);
    }

    @Test
    public void testMatrixWithLargeValues() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {1e10, 1e5},
            {1e5, 1e10}
        }, false);
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(matrix);
        RealMatrix l = decomposition.getL();
        RealMatrix lt = decomposition.getLT();
        RealMatrix product = l.multiply(lt);
        assertEquals(1e10, product.getEntry(0, 0), 1e-3);
        assertEquals(1e5, product.getEntry(0, 1), 1e-3);
        assertEquals(1e5, product.getEntry(1, 0), 1e-3);
        assertEquals(1e10, product.getEntry(1, 1), 1e-3);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testMatrixWithNaN() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {Double.NaN, 1.0},
            {1.0, 2.0}
        }, false);
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testMatrixWithInfinity() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {Double.POSITIVE_INFINITY, 1.0},
            {1.0, 2.0}
        }, false);
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = NotPositiveDefiniteMatrixException.class)
    public void testMatrixWithNegativeInfinity() {
        RealMatrix matrix = new Array2DRowRealMatrix(new double[][] {
            {Double.NEGATIVE_INFINITY, 1.0},
            {1.0, 2.0}
        }, false);
        new CholeskyDecompositionImpl(matrix);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMatrix() {
        new CholeskyDecompositionImpl((RealMatrix) null);
    }

    @Test
    public void testGetDeterminant() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(testMatrix);
        double determinant = decomposition.getDeterminant();
        assertEquals(8.0, determinant, 1e-12);
    }

    @Test
    public void testGetDeterminantForIdentity() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(identityMatrix);
        double determinant = decomposition.getDeterminant();
        assertEquals(1.0, determinant, 1e-12);
    }

    @Test
    public void testGetDeterminantForLargeMatrix() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(largePositiveDefiniteMatrix);
        double determinant = decomposition.getDeterminant();
        // Determinant of original matrix = 25*(18*11 - 0) - 15*(15*11 - (-5)*0) + (-5)*(15*0 - (-5)*18)
        // = 25*198 - 15*165 + (-5)*90 = 4950 - 2475 - 450 = 2025
        assertEquals(2025.0, determinant, 1e-9);
    }

    @Test
    public void testSolver() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(testMatrix);
        DecompositionSolver solver = decomposition.getSolver();
        assertNotNull(solver);
        assertTrue(solver.isNonSingular());

        RealVector b = new ArrayRealVector(new double[] {1.0, 2.0});
        RealVector x = solver.solve(b);
        // Solve: [4 2; 2 3] * x = [1; 2]
        // Expected: x = [-0.125; 0.75]
        assertEquals(-0.125, x.getEntry(0), 1e-12);
        assertEquals(0.75, x.getEntry(1), 1e-12);
    }

    @Test
    public void testSolverWithIdentity() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(identityMatrix);
        DecompositionSolver solver = decomposition.getSolver();
        assertTrue(solver.isNonSingular());

        RealVector b = new ArrayRealVector(new double[] {3.0, 4.0});
        RealVector x = solver.solve(b);
        assertEquals(3.0, x.getEntry(0), 1e-12);
        assertEquals(4.0, x.getEntry(1), 1e-12);
    }

    @Test(expected = SingularMatrixException.class)
    public void testSolverWithSingularMatrix() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(singularMatrix);
        DecompositionSolver solver = decomposition.getSolver();
        assertFalse(solver.isNonSingular());
        RealVector b = new ArrayRealVector(new double[] {1.0, 2.0});
        solver.solve(b);
    }

    @Test
    public void testSolverGetInverse() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(testMatrix);
        DecompositionSolver solver = decomposition.getSolver();
        RealMatrix inverse = solver.getInverse();
        // Inverse of [4 2; 2 3] = (1/8) * [3 -2; -2 4] = [0.375 -0.25; -0.25 0.5]
        assertEquals(0.375, inverse.getEntry(0, 0), 1e-12);
        assertEquals(-0.25, inverse.getEntry(0, 1), 1e-12);
        assertEquals(-0.25, inverse.getEntry(1, 0), 1e-12);
        assertEquals(0.5, inverse.getEntry(1, 1), 1e-12);
    }

    @Test
    public void testSolverSolveMatrix() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(testMatrix);
        DecompositionSolver solver = decomposition.getSolver();
        RealMatrix b = new Array2DRowRealMatrix(new double[][] {
            {1.0, 3.0},
            {2.0, 4.0}
        }, false);
        RealMatrix x = solver.solve(b);
        // Solve: [4 2; 2 3] * X = [1 3; 2 4]
        // Expected: X = [-0.125 0.125; 0.75 1.25]
        assertEquals(-0.125, x.getEntry(0, 0), 1e-12);
        assertEquals(0.125, x.getEntry(0, 1), 1e-12);
        assertEquals(0.75, x.getEntry(1, 0), 1e-12);
        assertEquals(1.25, x.getEntry(1, 1), 1e-12);
    }

    @Test(expected = MatrixDimensionMismatchException.class)
    public void testSolverWithWrongDimension() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(testMatrix);
        DecompositionSolver solver = decomposition.getSolver();
        RealVector b = new ArrayRealVector(new double[] {1.0, 2.0, 3.0});
        solver.solve(b);
    }

    @Test(expected = MatrixDimensionMismatchException.class)
    public void testSolverSolveMatrixWithWrongDimensions() {
        CholeskyDecompositionImpl decomposition = new CholeskyDecompositionImpl(testMatrix);
        DecompositionSolver solver = decomposition.getSolver();
        RealMatrix b = new Array2DRowRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        }, false);
        solver.solve(b);
    }
}