package org.apache.commons.math.linear;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for SingularValueDecompositionImpl, targeting high coverage and bug #76.
 */
public class SingularValueDecompositionImplTest {

    private static final double EPS = 1e-10;

    @Test(expected = NullPointerException.class)
    public void testNullMatrix() {
        new SingularValueDecompositionImpl((RealMatrix) null);
    }

    // --- Square matrix cases ---

    @Test
    public void testSquareFullRank() {
        double[][] data = { {1, 2}, {3, 4} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
    }

    @Test
    public void testSquareSingular() {
        double[][] data = { {1, 2}, {2, 4} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("Rank should be 1", 1, svd.getRank());
        assertEquals("Condition number should be infinite", Double.POSITIVE_INFINITY, svd.getConditionNumber(), 0.0);
    }

    @Test
    public void testIdentity() {
        double[][] data = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        double[] singularValues = svd.getSingularValues();
        assertEquals("Number of singular values", 3, singularValues.length);
        for (double v : singularValues) {
            assertEquals("Singular value = 1", 1.0, v, EPS);
        }
        assertEquals("Rank", 3, svd.getRank());
        assertEquals("Norm", 1.0, svd.getNorm(), EPS);
    }

    @Test
    public void testZeroMatrix() {
        double[][] data = { {0, 0}, {0, 0} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("Rank should be 0", 0, svd.getRank());
        assertTrue("Condition number should be infinite", Double.isInfinite(svd.getConditionNumber()));
        assertEquals("Norm", 0.0, svd.getNorm(), EPS);
    }

    // --- Tall matrix (rows > cols) ---

    @Test
    public void testTallFullRank() {
        double[][] data = { {1, 2}, {3, 4}, {5, 6} }; // 3x2
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("U rows", 3, svd.getU().getRowDimension());
        assertEquals("U cols", 3, svd.getU().getColumnDimension());
        assertEquals("V rows", 2, svd.getV().getRowDimension());
        assertEquals("V cols", 2, svd.getV().getColumnDimension());
        assertEquals("S rows", 3, svd.getS().getRowDimension());
        assertEquals("S cols", 2, svd.getS().getColumnDimension());
    }

    @Test
    public void testTallRankDeficient() {
        double[][] data = { {1, 2}, {2, 4}, {3, 6} }; // 3x2, rank 1
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("Rank should be 1", 1, svd.getRank());
    }

    // --- Wide matrix (cols > rows) - This is the core of bug #76 ---

    @Test
    public void testWideFullRank() {
        double[][] data = { {1, 2, 3}, {4, 5, 6} }; // 2x3
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        // Bug #76: U should be m x m (2x2), not m x n (2x3)
        assertEquals("U rows", 2, svd.getU().getRowDimension());
        assertEquals("U cols", 2, svd.getU().getColumnDimension());
        assertEquals("V rows", 3, svd.getV().getRowDimension());
        assertEquals("V cols", 3, svd.getV().getColumnDimension());
        assertEquals("S rows", 2, svd.getS().getRowDimension());
        assertEquals("S cols", 3, svd.getS().getColumnDimension());
        // Check that U^T * U is identity
        RealMatrix utu = svd.getU().transpose().multiply(svd.getU());
        RealMatrix identity2 = MatrixUtils.createRealIdentityMatrix(2);
        assertMatrixEquals("U^T * U should be I", identity2, utu, EPS);
    }

    @Test
    public void testWideRankDeficient() {
        double[][] data = { {1, 2, 3}, {2, 4, 6} }; // 2x3, rank 1
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("Rank should be 1", 1, svd.getRank());
        assertEquals("Wide matrix U dimensions", 2, svd.getU().getColumnDimension());
    }

    // --- 1x1 case ---

    @Test
    public void test1x1() {
        double[][] data = { {5.0} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("Singular values", 5.0, svd.getSingularValues()[0], EPS);
        assertEquals("Rank", 1, svd.getRank());
    }

    @Test
    public void test1x1Zero() {
        double[][] data = { {0.0} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("Rank", 0, svd.getRank());
    }

    // --- Edge cases: rows > 1, cols = 1 and vice versa ---

    @Test
    public void testColumnVector() {
        double[][] data = { {1}, {2}, {3} }; // 3x1
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("U cols", 3, svd.getU().getColumnDimension());
        assertEquals("V cols", 1, svd.getV().getColumnDimension());
    }

    @Test
    public void testRowVector() {
        double[][] data = { {1, 2, 3, 4} }; // 1x4
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        checkSVDProperties(svd, m);
        assertEquals("U cols", 1, svd.getU().getColumnDimension());
        assertEquals("V cols", 4, svd.getV().getColumnDimension());
    }

    // --- Additional property checks ---

    @Test
    public void testSingularValuesSorted() {
        double[][] data = { {9, 8, 7}, {6, 5, 4}, {3, 2, 1} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        double[] sv = svd.getSingularValues();
        for (int i = 0; i < sv.length - 1; i++) {
            assertTrue("Singular values should be non-increasing", sv[i] >= sv[i+1] - EPS);
        }
    }

    @Test
    public void testConditionNumber() {
        double[][] data = { {1, 0}, {0, 1e-12} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        double condition = svd.getConditionNumber();
        assertTrue("Condition number should be very large", condition > 1e10);
        assertFalse("Condition number should not be infinite", Double.isInfinite(condition));
    }

    @Test
    public void testNorm() {
        double[][] data = { {3, 4}, {0, 0} }; // singular values: 5, 0
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        assertEquals("Norm should equal max singular value", 5.0, svd.getNorm(), EPS);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetUWithInvalidColumn() {
        double[][] data = { {1, 2}, {3, 4} };
        RealMatrix m = new Array2DRowRealMatrix(data, false);
        SingularValueDecompositionImpl svd = new SingularValueDecompositionImpl(m);
        svd.getU().getColumn(-1); // OutOfRangeException expected
    }

    // --- Helper methods ---

    private void checkSVDProperties(SingularValueDecompositionImpl svd, RealMatrix original) {
        // Reconstruct: U * S * V^T should equal original matrix
        RealMatrix reconstructed = svd.getU().multiply(svd.getS()).multiply(svd.getV().transpose());
        assertMatrixEquals("SVD reconstruction failed", original, reconstructed, EPS);

        // Check that U is orthogonal (U^T * U = I) m x m
        RealMatrix utu = svd.getU().transpose().multiply(svd.getU());
        int m = original.getRowDimension();
        RealMatrix identityM = MatrixUtils.createRealIdentityMatrix(m);
        assertMatrixEquals("U^T * U should be identity", identityM, utu, EPS);

        // Check that V is orthogonal (V^T * V = I) n x n
        RealMatrix vtv = svd.getV().transpose().multiply(svd.getV());
        int n = original.getColumnDimension();
        RealMatrix identityN = MatrixUtils.createRealIdentityMatrix(n);
        assertMatrixEquals("V^T * V should be identity", identityN, vtv, EPS);
    }

    private void assertMatrixEquals(String message, RealMatrix expected, RealMatrix actual, double tol) {
        assertEquals(message + " rows", expected.getRowDimension(), actual.getRowDimension());
        assertEquals(message + " cols", expected.getColumnDimension(), actual.getColumnDimension());
        for (int i = 0; i < expected.getRowDimension(); i++) {
            for (int j = 0; j < expected.getColumnDimension(); j++) {
                assertEquals(message + "(" + i + "," + j + ")", 
                             expected.getEntry(i, j), actual.getEntry(i, j), tol);
            }
        }
    }
}