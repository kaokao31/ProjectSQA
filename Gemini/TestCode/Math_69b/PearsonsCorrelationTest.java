package org.apache.commons.math.stat.correlation;

import org.junit.Test;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.BlockRealMatrix;

import static org.junit.Assert.*;

public class PearsonsCorrelationTest {

    @Test
    public void testDefaultConstructor() {
        PearsonsCorrelation corr = new PearsonsCorrelation();
        assertNotNull(corr);
        assertNotNull(corr.getCorrelationMatrix());
        assertNotNull(corr.getcovarianceMatrix());
        assertEquals(0, corr.getSampleSize());
    }

    @Test
    public void testDoubleArray2DConstructor() {
        double[][] data = {
            {1.0, 2.0},
            {2.0, 4.0},
            {3.0, 6.0}
        };
        PearsonsCorrelation corr = new PearsonsCorrelation(data);
        assertNotNull(corr.getCorrelationMatrix());
        assertEquals(3, corr.getSampleSize());
        assertEquals(1.0, corr.getCorrelationMatrix().getEntry(0, 0), 1e-12);
        assertEquals(1.0, corr.getCorrelationMatrix().getEntry(0, 1), 1e-12);
    }

    @Test
    public void testRealMatrixConstructor() {
        RealMatrix matrix = new BlockRealMatrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        });
        PearsonsCorrelation corr = new PearsonsCorrelation(matrix);
        assertNotNull(corr.getCorrelationMatrix());
        assertEquals(3, corr.getSampleSize());
    }

    @Test
    public void testCovarianceConstructor() {
        Covariance cov = new Covariance(new double[][] {
            {1.0, 2.0},
            {2.0, 4.0},
            {3.0, 6.0}
        });
        PearsonsCorrelation corr = new PearsonsCorrelation(cov);
        assertNotNull(corr.getCorrelationMatrix());
        assertEquals(3, corr.getSampleSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCovarianceConstructorNull() {
        new PearsonsCorrelation((Covariance) null);
    }

    @Test
    public void testComputeCorrelationMatrix_RealMatrix() {
        RealMatrix matrix = new BlockRealMatrix(new double[][] {
            {1.0, 10.0},
            {2.0, 20.0},
            {3.0, 30.0}
        });
        PearsonsCorrelation corr = new PearsonsCorrelation();
        RealMatrix result = corr.computeCorrelationMatrix(matrix);
        assertEquals(2, result.getRowDimension());
        assertEquals(2, result.getColumnDimension());
        assertEquals(1.0, result.getEntry(0, 0), 1e-12);
        assertEquals(1.0, result.getEntry(0, 1), 1e-12);
    }

    @Test
    public void testComputeCorrelationMatrix_DoubleArray2D() {
        double[][] data = {
            {1.0, 10.0},
            {2.0, 20.0},
            {3.0, 30.0}
        };
        PearsonsCorrelation corr = new PearsonsCorrelation();
        RealMatrix result = corr.computeCorrelationMatrix(data);
        assertEquals(2, result.getRowDimension());
        assertEquals(2, result.getColumnDimension());
        assertEquals(1.0, result.getEntry(0, 0), 1e-12);
        assertEquals(1.0, result.getEntry(0, 1), 1e-12);
    }

    @Test
    public void testCorrelation_DoubleArrays() {
        double[] xArray = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] yArray = {2.0, 4.0, 6.0, 8.0, 10.0};
        PearsonsCorrelation corr = new PearsonsCorrelation();
        double correlation = corr.correlation(xArray, yArray);
        assertEquals(1.0, correlation, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCorrelation_MismatchedLengths() {
        double[] xArray = {1.0, 2.0};
        double[] yArray = {1.0, 2.0, 3.0};
        PearsonsCorrelation corr = new PearsonsCorrelation();
        corr.correlation(xArray, yArray);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCorrelation_TooShort() {
        double[] xArray = {1.0};
        double[] yArray = {2.0};
        PearsonsCorrelation corr = new PearsonsCorrelation();
        corr.correlation(xArray, yArray);
    }

    @Test
    public void testGetPValues() {
        RealMatrix matrix = new BlockRealMatrix(new double[][] {
            {1.0, 2.0},
            {2.0, 4.0},
            {3.0, 6.0},
            {4.0, 8.0}
        });
        PearsonsCorrelation corr = new PearsonsCorrelation(matrix);
        RealMatrix pValues = corr.getPValues();
        assertNotNull(pValues);
        assertEquals(2, pValues.getRowDimension());
        assertEquals(2, pValues.getColumnDimension());
    }

    @Test
    public void testGetCorrelationStandardErrors() {
        RealMatrix matrix = new BlockRealMatrix(new double[][] {
            {1.0, 2.0},
            {2.0, 4.0},
            {3.0, 6.0},
            {4.0, 8.0}
        });
        PearsonsCorrelation corr = new PearsonsCorrelation(matrix);
        RealMatrix se = corr.getCorrelationStandardErrors();
        assertNotNull(se);
        assertEquals(2, se.getRowDimension());
        assertEquals(2, se.getColumnDimension());
    }

    @Test
    public void testComputeCorrelation_ConstantColumnBug69() {
        // Defects4J Math 69 involves p-value / correlation matrix calculation issues when columns are constant or p-values are computed
        RealMatrix matrix = new BlockRealMatrix(new double[][] {
            {1.0, 2.0},
            {1.0, 4.0},
            {1.0, 6.0}
        });
        PearsonsCorrelation corr = new PearsonsCorrelation(matrix);
        RealMatrix pValues = corr.getPValues();
        assertNotNull(pValues);
        RealMatrix se = corr.getCorrelationStandardErrors();
        assertNotNull(se);
    }
}