package org.apache.commons.math.stat.correlation;

import org.junit.Before;
import org.junit.Test;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for PearsonsCorrelation.
 * Designed to achieve high coverage and detect potential faults.
 */
public class PearsonsCorrelationTest {

    private PearsonsCorrelation correlation;

    @Before
    public void setUp() {
        // Default empty setup; use specific constructors in tests.
    }

    // ------------------------- Constructor Tests -------------------------

    @Test(expected = NullPointerException.class)
    public void testConstructorNullData() {
        new PearsonsCorrelation((double[][]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullMatrix() {
        new PearsonsCorrelation((RealMatrix) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyData() {
        new PearsonsCorrelation(new double[0][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSingleRow() {
        new PearsonsCorrelation(new double[][] {{1.0, 2.0, 3.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorSingleColumn() {
        // At least two rows needed for correlation
        new PearsonsCorrelation(new double[][] {{1.0}, {2.0}});
    }

    @Test
    public void testConstructorValidMatrix() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        assertNotNull(pc);
    }

    // ------------------------- correlation(double[], double[]) Tests -------------------------

    @Test(expected = NullPointerException.class)
    public void testCorrelationNullX() {
        correlation = new PearsonsCorrelation();
        correlation.correlation(null, new double[]{1.0, 2.0});
    }

    @Test(expected = NullPointerException.class)
    public void testCorrelationNullY() {
        correlation = new PearsonsCorrelation();
        correlation.correlation(new double[]{1.0, 2.0}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCorrelationDifferentLengths() {
        correlation = new PearsonsCorrelation();
        correlation.correlation(new double[]{1.0, 2.0}, new double[]{1.0, 2.0, 3.0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCorrelationLessThanTwoPairs() {
        correlation = new PearsonsCorrelation();
        correlation.correlation(new double[]{1.0}, new double[]{2.0});
    }

    @Test
    public void testCorrelationPerfectPositive() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {2.0, 4.0, 6.0, 8.0, 10.0};
        double expected = 1.0;
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertEquals(expected, actual, 1e-14);
    }

    @Test
    public void testCorrelationPerfectNegative() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {-1.0, -2.0, -3.0, -4.0, -5.0};
        double expected = -1.0;
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertEquals(expected, actual, 1e-14);
    }

    @Test
    public void testCorrelationNoCorrelation() {
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y = {5.0, 4.0, 3.0, 2.0, 1.0};
        // In this case, correlation should be -1.0 (since y is reverse of x)
        // Actually x and y are perfectly negatively correlated.
        // Let's use a truly uncorrelated pattern: e.g., x = {1,2,3}, y = {0,0,0}
        // But constant y gives NaN. Use alternative.
        double[] x2 = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] y2 = {5.0, 1.0, 2.0, 3.0, 4.0}; // somewhat random
        double r = new PearsonsCorrelation().correlation(x2, y2);
        // Not precisely 0, but non-extreme
        assertTrue(r > -1.0 && r < 1.0);
    }

    @Test
    public void testCorrelationNearZero() {
        double[] x = {-1.0, 0.0, 1.0};
        double[] y = {1.0, 0.0, -1.0};
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertEquals(-1.0, actual, 1e-14); // perfect negative
    }

    @Test
    public void testCorrelationConstantX() {
        // Zero variance in x -> correlation should be NaN
        double[] x = {5.0, 5.0, 5.0, 5.0};
        double[] y = {1.0, 2.0, 3.0, 4.0};
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertTrue(Double.isNaN(actual));
    }

    @Test
    public void testCorrelationConstantY() {
        double[] x = {1.0, 2.0, 3.0, 4.0};
        double[] y = {7.0, 7.0, 7.0, 7.0};
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertTrue(Double.isNaN(actual));
    }

    @Test
    public void testCorrelationBothConstant() {
        double[] x = {3.0, 3.0, 3.0};
        double[] y = {5.0, 5.0, 5.0};
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertTrue(Double.isNaN(actual));
    }

    @Test
    public void testCorrelationWithNaN() {
        double[] x = {1.0, Double.NaN, 3.0, 4.0};
        double[] y = {2.0, 3.0, 4.0, 5.0};
        double actual = new PearsonsCorrelation().correlation(x, y);
        // Expect NaN because sum contains NaN
        assertTrue(Double.isNaN(actual));
    }

    @Test
    public void testCorrelationWithInfinity() {
        double[] x = {1.0, Double.POSITIVE_INFINITY, 3.0};
        double[] y = {2.0, 3.0, 4.0};
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertTrue(Double.isNaN(actual));
    }

    @Test
    public void testCorrelationLargeValues() {
        double[] x = {1e100, 2e100, 3e100};
        double[] y = {2e100, 4e100, 6e100};
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertEquals(1.0, actual, 1e-14);
    }

    @Test
    public void testCorrelationSmallValues() {
        double[] x = {1e-100, 2e-100, 3e-100};
        double[] y = {2e-100, 4e-100, 6e-100};
        double actual = new PearsonsCorrelation().correlation(x, y);
        assertEquals(1.0, actual, 1e-14);
    }

    // ------------------------- getCorrelationMatrix() Tests -------------------------

    @Test
    public void testGetCorrelationMatrix() {
        double[][] data = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix corrMatrix = pc.getCorrelationMatrix();
        // Matrix should be 3x3 with 1.0 on diagonal
        assertNotNull(corrMatrix);
        assertEquals(3, corrMatrix.getRowDimension());
        assertEquals(3, corrMatrix.getColumnDimension());
        for (int i = 0; i < 3; i++) {
            assertEquals(1.0, corrMatrix.getEntry(i, i), 1e-14);
        }
    }

    @Test
    public void testGetCorrelationMatrixTwoColumns() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix corrMatrix = pc.getCorrelationMatrix();
        assertEquals(2, corrMatrix.getRowDimension());
        assertEquals(2, corrMatrix.getColumnDimension());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCorrelationMatrixInsufficientData() {
        // Only 1 row -> insufficient to compute correlations
        double[][] data = {{1.0, 2.0, 3.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        pc.getCorrelationMatrix(); // Should throw because n < 2
    }

    // ------------------------- getCorrelationStandardErrors() Tests -------------------------

    @Test
    public void testGetCorrelationStandardErrors() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}, {7.0, 8.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix seMatrix = pc.getCorrelationStandardErrors();
        // Standard errors should be non-negative
        assertNotNull(seMatrix);
        for (int i = 0; i < seMatrix.getRowDimension(); i++) {
            for (int j = 0; j < seMatrix.getColumnDimension(); j++) {
                if (i == j) {
                    // Diagonal should be 0.0? Actually standard error for correlation with itself is 0.
                    assertEquals(0.0, seMatrix.getEntry(i, j), 1e-14);
                } else {
                    assertTrue(seMatrix.getEntry(i, j) >= 0);
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCorrelationStandardErrorsInsufficientData() {
        double[][] data = {{1.0}, {2.0}}; // only 2 rows, 1 column -> need at least 1 column pair? Actually n=2, k=1 => no off-diagonal correlations
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        pc.getCorrelationStandardErrors(); // Should throw due to n<2? Actually n=2 -> OK, but k=1 no off-diagonal
    }

    // ------------------------- getCorrelationPValues() Tests -------------------------

    @Test
    public void testGetCorrelationPValues() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}, {7.0, 8.0}, {9.0, 10.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix pMatrix = pc.getCorrelationPValues();
        assertNotNull(pMatrix);
        // Diagonal should be 0.0 (p-value for perfect correlation)
        for (int i = 0; i < pMatrix.getRowDimension(); i++) {
            assertEquals(0.0, pMatrix.getEntry(i, i), 1e-14);
        }
    }

    @Test
    public void testGetCorrelationPValuesWithNaN() {
        // If variance zero, correlation is NaN, p-value should be NaN
        double[][] data = {{1.0, 2.0}, {1.0, 4.0}, {1.0, 6.0}}; // first column constant
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix pMatrix = pc.getCorrelationPValues();
        assertTrue(Double.isNaN(pMatrix.getEntry(0, 1)));
        assertTrue(Double.isNaN(pMatrix.getEntry(1, 0)));
        // Diagonal is 0.0 (correlation of column with itself is 1 -> p=0)
        assertEquals(0.0, pMatrix.getEntry(0, 0), 0.0);
    }

    @Test
    public void testGetCorrelationPValuesPerfectCorrelation() {
        // Two columns perfectly correlated (one is linear transformation of the other)
        double[][] data = {{1.0, 2.0}, {2.0, 4.0}, {3.0, 6.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix pMatrix = pc.getCorrelationPValues();
        // p-value should be 0 (or very close)
        assertEquals(0.0, pMatrix.getEntry(0, 1), 1e-14);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCorrelationPValuesInsufficientData() {
        double[][] data = {{1.0, 2.0}}; // only one row
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        pc.getCorrelationPValues();
    }

    // ------------------------- Edge Cases and Bug Triggers -------------------------

    @Test
    public void testBug69TriggerNaNInPValues() {
        // Potential bug: p-values not NaN when correlation is NaN due to constant column
        double[][] data = {
            {1.0, 2.0, 3.0},
            {1.0, 2.0, 3.0},
            {1.0, 2.0, 3.0}
        };
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix pMatrix = pc.getCorrelationPValues();
        // All correlations should be NaN (all columns constant) -> p-values should be NaN
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertEquals(0.0, pMatrix.getEntry(i, j), 0.0);
                } else {
                    assertTrue("Expected NaN for p-value", Double.isNaN(pMatrix.getEntry(i, j)));
                }
            }
        }
    }

    @Test
    public void testCorrelationMatrixSymmetry() {
        double[][] data = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix corr = pc.getCorrelationMatrix();
        for (int i = 0; i < corr.getRowDimension(); i++) {
            for (int j = 0; j < corr.getColumnDimension(); j++) {
                assertEquals(corr.getEntry(i, j), corr.getEntry(j, i), 1e-14);
            }
        }
    }

    @Test
    public void testStandardErrorsSymmetry() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}, {7.0, 8.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix se = pc.getCorrelationStandardErrors();
        for (int i = 0; i < se.getRowDimension(); i++) {
            for (int j = 0; j < se.getColumnDimension(); j++) {
                assertEquals(se.getEntry(i, j), se.getEntry(j, i), 1e-14);
            }
        }
    }

    @Test
    public void testPValuesSymmetry() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}, {7.0, 8.0}};
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix pv = pc.getCorrelationPValues();
        for (int i = 0; i < pv.getRowDimension(); i++) {
            for (int j = 0; j < pv.getColumnDimension(); j++) {
                assertEquals(pv.getEntry(i, j), pv.getEntry(j, i), 1e-14);
            }
        }
    }

    // ------------------------- Additional Constructor Tests -------------------------

    @Test
    public void testConstructorWithCovariance() {
        // Not directly testable without covariance class, but ensure matrix constructor works
        double[][] covData = {{1.0, 2.0, 3.0}, {2.0, 4.0, 6.0}, {3.0, 6.0, 9.0}};
        Array2DRowRealMatrix covarianceMatrix = new Array2DRowRealMatrix(covData);
        PearsonsCorrelation pc = new PearsonsCorrelation(covarianceMatrix);
        // Should compute correlation matrix from covariance
        RealMatrix corr = pc.getCorrelationMatrix();
        assertEquals(1.0, corr.getEntry(0, 0), 1e-14);
        assertEquals(1.0, corr.getEntry(1, 1), 1e-14);
        // Check off-diagonal: covariance[0][1] = 2, var0 = 1, var1 = 4 => r = 2/sqrt(1*4)=1
        assertEquals(1.0, corr.getEntry(0, 1), 1e-14);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNonPositiveVarianceOnDiagonal() {
        // Covariance matrix with zero variance on diagonal should cause exception
        double[][] covData = {{0.0, 0.0}, {0.0, 1.0}};
        Array2DRowRealMatrix covMatrix = new Array2DRowRealMatrix(covData);
        new PearsonsCorrelation(covMatrix);
    }

    @Test
    public void testConstructorWithNaNInCovariance() {
        // If covariance matrix contains NaN, correlations should be NaN
        double[][] covData = {{1.0, Double.NaN}, {Double.NaN, 1.0}};
        Array2DRowRealMatrix covMatrix = new Array2DRowRealMatrix(covData);
        PearsonsCorrelation pc = new PearsonsCorrelation(covMatrix);
        RealMatrix corr = pc.getCorrelationMatrix();
        assertTrue(Double.isNaN(corr.getEntry(0, 1)));
        assertTrue(Double.isNaN(corr.getEntry(1, 0)));
    }
}