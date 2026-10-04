package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChiSquareTestImplTest {

    private ChiSquareTestImpl testImpl;

    @Before
    public void setUp() {
        testImpl = new ChiSquareTestImpl();
    }

    @Test
    public void testChiSquareIndependenceValid() throws Exception {
        long[][] counts = {
            {40, 10, 20},
            {10, 50, 30}
        };
        double chiSquare = testImpl.chiSquare(counts);
        assertTrue(chiSquare > 0.0);

        double pValue = testImpl.chiSquareTest(counts);
        assertTrue(pValue >= 0.0 && pValue <= 1.0);

        boolean reject = testImpl.chiSquareTest(counts, 0.05);
        // Just verify it runs and returns a boolean
        assertNotNull(reject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareIndependenceNull() throws Exception {
        testImpl.chiSquare(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareIndependenceNotEnoughRows() throws Exception {
        long[][] counts = {
            {40, 10, 20}
        };
        testImpl.chiSquare(counts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareIndependenceNotEnoughColumns() throws Exception {
        long[][] counts = {
            {40},
            {10}
        };
        testImpl.chiSquare(counts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareIndependenceNegativeValue() throws Exception {
        long[][] counts = {
            {-40, 10},
            {10, 50}
        };
        testImpl.chiSquare(counts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareIndependenceZeroRowTotal() throws Exception {
        long[][] counts = {
            {0, 0},
            {10, 50}
        };
        testImpl.chiSquare(counts);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareIndependenceZeroColumnTotal() throws Exception {
        long[][] counts = {
            {0, 10},
            {0, 50}
        };
        testImpl.chiSquare(counts);
    }

    @Test
    public void testChiSquareDataSetsCompareValid() throws Exception {
        long[] sample1 = {10, 15, 20};
        long[] sample2 = {18, 22, 25};

        double chiSquare = testImpl.chiSquareDataSetsCompared(sample1, sample2);
        assertTrue(chiSquare >= 0.0);

        double pValue = testImpl.chiSquareTestDataSetsCompared(sample1, sample2);
        assertTrue(pValue >= 0.0 && pValue <= 1.0);

        boolean reject = testImpl.chiSquareTestDataSetsCompared(sample1, sample2, 0.05);
        assertNotNull(reject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsCompareNull1() throws Exception {
        long[] sample1 = null;
        long[] sample2 = {18, 22, 25};
        testImpl.chiSquareDataSetsCompared(sample1, sample2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsCompareNull2() throws Exception {
        long[] sample1 = {10, 15, 20};
        long[] sample2 = null;
        testImpl.chiSquareDataSetsCompared(sample1, sample2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsCompareShort1() throws Exception {
        long[] sample1 = {10};
        long[] sample2 = {18, 22, 25};
        testImpl.chiSquareDataSetsCompared(sample1, sample2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsCompareShort2() throws Exception {
        long[] sample1 = {10, 15, 20};
        long[] sample2 = {18};
        testImpl.chiSquareDataSetsCompared(sample1, sample2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsCompareLengthMismatch() throws Exception {
        long[] sample1 = {10, 15, 20};
        long[] sample2 = {18, 22};
        testImpl.chiSquareDataSetsCompared(sample1, sample2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsCompareNegative() throws Exception {
        long[] sample1 = {-10, 15, 20};
        long[] sample2 = {18, 22, 25};
        testImpl.chiSquareDataSetsCompared(sample1, sample2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsCompareZeroSum1() throws Exception {
        long[] sample1 = {0, 0, 0};
        long[] sample2 = {18, 22, 25};
        testImpl.chiSquareDataSetsCompared(sample1, sample2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsCompareZeroSum2() throws Exception {
        long[] sample1 = {10, 15, 20};
        long[] sample2 = {0, 0, 0};
        testImpl.chiSquareDataSetsCompared(sample1, sample2);
    }

    @Test
    public void testChiSquareUniformValid() throws Exception {
        double[] expected = {0.5, 0.5};
        long[] observed = {50, 50};

        double chiSquare = testImpl.chiSquare(expected, observed);
        assertTrue(chiSquare >= 0.0);

        double pValue = testImpl.chiSquareTest(expected, observed);
        assertTrue(pValue >= 0.0 && pValue <= 1.0);

        boolean reject = testImpl.chiSquareTest(expected, observed, 0.05);
        assertNotNull(reject);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformNullExpected() throws Exception {
        double[] expected = null;
        long[] observed = {50, 50};
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformNullObserved() throws Exception {
        double[] expected = {0.5, 0.5};
        long[] observed = null;
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformShortExpected() throws Exception {
        double[] expected = {0.5};
        long[] observed = {50, 50};
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformShortObserved() throws Exception {
        double[] expected = {0.5, 0.5};
        long[] observed = {50};
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformLengthMismatch() throws Exception {
        double[] expected = {0.5, 0.5};
        long[] observed = {50, 50, 20};
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformNegativeExpected() throws Exception {
        double[] expected = {-0.5, 1.5};
        long[] observed = {50, 50};
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformNegativeObserved() throws Exception {
        double[] expected = {0.5, 0.5};
        long[] observed = {-50, 50};
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformZeroSumExpected() throws Exception {
        double[] expected = {0.0, 0.0};
        long[] observed = {50, 50};
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareUniformZeroSumObserved() throws Exception {
        double[] expected = {0.5, 0.5};
        long[] observed = {0, 0};
        testImpl.chiSquare(expected, observed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestInvalidAlphaLow() throws Exception {
        double[] expected = {0.5, 0.5};
        long[] observed = {50, 50};
        testImpl.chiSquareTest(expected, observed, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestInvalidAlphaHigh() throws Exception {
        double[] expected = {0.5, 0.5};
        long[] observed = {50, 50};
        testImpl.chiSquareTest(expected, observed, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestInvalidAlphaLowMatrix() throws Exception {
        long[][] counts = {{40, 10}, {10, 50}};
        testImpl.chiSquareTest(counts, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestInvalidAlphaHighMatrix() throws Exception {
        long[][] counts = {{40, 10}, {10, 50}};
        testImpl.chiSquareTest(counts, 0.6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestInvalidAlphaLowDatasets() throws Exception {
        long[] sample1 = {10, 15};
        long[] sample2 = {18, 22};
        testImpl.chiSquareTestDataSetsCompared(sample1, sample2, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestInvalidAlphaHighDatasets() throws Exception {
        long[] sample1 = {10, 15};
        long[] sample2 = {18, 22};
        testImpl.chiSquareTestDataSetsCompared(sample1, sample2, 1.0);
    }
}