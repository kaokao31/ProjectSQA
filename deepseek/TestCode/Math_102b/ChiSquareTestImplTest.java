package org.apache.commons.math.stat.inference;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test class for ChiSquareTestImpl.
 * Designed to achieve high coverage and detect potential faults.
 */
public class ChiSquareTestImplTest {

    private ChiSquareTestImpl testImpl;

    @Before
    public void setUp() {
        testImpl = new ChiSquareTestImpl();
    }

    // ---------- chiSquare(double[] expected, long[] observed) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareNullExpected() {
        testImpl.chiSquare(null, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareNullObserved() {
        testImpl.chiSquare(new double[]{1.0, 2.0}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareLengthMismatch() {
        testImpl.chiSquare(new double[]{1.0, 2.0}, new long[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareObservedNegative() {
        testImpl.chiSquare(new double[]{1.0, 2.0}, new long[]{-1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareExpectedZero() {
        testImpl.chiSquare(new double[]{0.0, 2.0}, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareExpectedNegative() {
        testImpl.chiSquare(new double[]{-1.0, 2.0}, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareEmptyArrays() {
        testImpl.chiSquare(new double[]{}, new long[]{});
    }

    @Test
    public void testChiSquareBasic() {
        double[] expected = {10.0, 10.0, 10.0};
        long[] observed = {5, 15, 10};
        double result = testImpl.chiSquare(expected, observed);
        // Expected chi-square = (5-10)^2/10 + (15-10)^2/10 + (10-10)^2/10 = 2.5 + 2.5 + 0 = 5.0
        assertEquals(5.0, result, 1e-10);
    }

    @Test
    public void testChiSquareAllObservedZero() {
        double[] expected = {1.0, 2.0, 3.0};
        long[] observed = {0, 0, 0};
        double result = testImpl.chiSquare(expected, observed);
        // (0-1)^2/1 + (0-2)^2/2 + (0-3)^2/3 = 1 + 2 + 3 = 6.0
        assertEquals(6.0, result, 1e-10);
    }

    @Test
    public void testChiSquarePerfectMatch() {
        double[] expected = {5.0, 5.0};
        long[] observed = {5, 5};
        double result = testImpl.chiSquare(expected, observed);
        assertEquals(0.0, result, 1e-10);
    }

    @Test
    public void testChiSquareSingleElement() {
        double[] expected = {10.0};
        long[] observed = {7};
        double result = testImpl.chiSquare(expected, observed);
        assertEquals(0.9, result, 1e-10);
    }

    // ---------- chiSquareTest(double[] expected, long[] observed) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestNullExpected() {
        testImpl.chiSquareTest(null, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestNullObserved() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestLengthMismatch() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, new long[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestObservedNegative() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, new long[]{-1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestExpectedZero() {
        testImpl.chiSquareTest(new double[]{0.0, 2.0}, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestExpectedNegative() {
        testImpl.chiSquareTest(new double[]{-1.0, 2.0}, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestEmptyArrays() {
        testImpl.chiSquareTest(new double[]{}, new long[]{});
    }

    @Test
    public void testChiSquareTestBasic() {
        double[] expected = {10.0, 10.0, 10.0};
        long[] observed = {5, 15, 10};
        double pValue = testImpl.chiSquareTest(expected, observed);
        // chi-square = 5.0, df=2, p-value ~ 0.082085
        assertTrue(pValue > 0.08 && pValue < 0.09);
    }

    @Test
    public void testChiSquareTestPerfectMatch() {
        double[] expected = {5.0, 5.0};
        long[] observed = {5, 5};
        double pValue = testImpl.chiSquareTest(expected, observed);
        assertEquals(1.0, pValue, 1e-10);
    }

    @Test
    public void testChiSquareTestLargeDifference() {
        double[] expected = {100.0, 100.0};
        long[] observed = {0, 200};
        double pValue = testImpl.chiSquareTest(expected, observed);
        // chi-square = 200, df=1, p-value extremely small
        assertTrue(pValue < 1e-10);
    }

    // ---------- chiSquareTest(double[] expected, long[] observed, double alpha) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaNullExpected() {
        testImpl.chiSquareTest(null, new long[]{1, 2}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaNullObserved() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, null, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaLengthMismatch() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, new long[]{1}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaObservedNegative() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, new long[]{-1, 2}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaExpectedZero() {
        testImpl.chiSquareTest(new double[]{0.0, 2.0}, new long[]{1, 2}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaExpectedNegative() {
        testImpl.chiSquareTest(new double[]{-1.0, 2.0}, new long[]{1, 2}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaEmptyArrays() {
        testImpl.chiSquareTest(new double[]{}, new long[]{}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaNegativeAlpha() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, new long[]{1, 2}, -0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaZeroAlpha() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, new long[]{1, 2}, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestAlphaAlphaGreaterThanOne() {
        testImpl.chiSquareTest(new double[]{1.0, 2.0}, new long[]{1, 2}, 1.5);
    }

    @Test
    public void testChiSquareTestAlphaRejectNull() {
        double[] expected = {10.0, 10.0, 10.0};
        long[] observed = {5, 15, 10};
        // p-value ~ 0.082 > 0.05, so should not reject null
        assertFalse(testImpl.chiSquareTest(expected, observed, 0.05));
    }

    @Test
    public void testChiSquareTestAlphaRejectNullTrue() {
        double[] expected = {100.0, 100.0};
        long[] observed = {0, 200};
        // p-value extremely small, reject null
        assertTrue(testImpl.chiSquareTest(expected, observed, 0.05));
    }

    @Test
    public void testChiSquareTestAlphaBoundary() {
        double[] expected = {10.0, 10.0};
        long[] observed = {10, 10};
        // p-value = 1.0, alpha = 0.05, should not reject
        assertFalse(testImpl.chiSquareTest(expected, observed, 0.05));
    }

    // ---------- chiSquareDataSetsComparison(long[] observed1, long[] observed2) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparisonNullFirst() {
        testImpl.chiSquareDataSetsComparison(null, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparisonNullSecond() {
        testImpl.chiSquareDataSetsComparison(new long[]{1, 2}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparisonLengthMismatch() {
        testImpl.chiSquareDataSetsComparison(new long[]{1, 2}, new long[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparisonNegativeObservedFirst() {
        testImpl.chiSquareDataSetsComparison(new long[]{-1, 2}, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparisonNegativeObservedSecond() {
        testImpl.chiSquareDataSetsComparison(new long[]{1, 2}, new long[]{-1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparisonAllZeroFirst() {
        testImpl.chiSquareDataSetsComparison(new long[]{0, 0}, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparisonAllZeroSecond() {
        testImpl.chiSquareDataSetsComparison(new long[]{1, 2}, new long[]{0, 0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareDataSetsComparisonEmptyArrays() {
        testImpl.chiSquareDataSetsComparison(new long[]{}, new long[]{});
    }

    @Test
    public void testChiSquareDataSetsComparisonBasic() {
        long[] observed1 = {10, 20, 30};
        long[] observed2 = {15, 25, 35};
        double result = testImpl.chiSquareDataSetsComparison(observed1, observed2);
        // Expected chi-square statistic for independence
        assertTrue(result > 0);
    }

    @Test
    public void testChiSquareDataSetsComparisonIdentical() {
        long[] observed1 = {5, 10, 15};
        long[] observed2 = {5, 10, 15};
        double result = testImpl.chiSquareDataSetsComparison(observed1, observed2);
        // When identical, chi-square should be 0
        assertEquals(0.0, result, 1e-10);
    }

    @Test
    public void testChiSquareDataSetsComparisonSingleElement() {
        long[] observed1 = {10};
        long[] observed2 = {20};
        double result = testImpl.chiSquareDataSetsComparison(observed1, observed2);
        // With one category, chi-square should be 0 (no degrees of freedom?)
        // Actually for 2x1 table, chi-square = (10-15)^2/15 + (20-15)^2/15 = 25/15+25/15=50/15≈3.333
        assertEquals(3.3333333333333335, result, 1e-10);
    }

    @Test
    public void testChiSquareDataSetsComparisonLargeDifference() {
        long[] observed1 = {100, 0};
        long[] observed2 = {0, 100};
        double result = testImpl.chiSquareDataSetsComparison(observed1, observed2);
        // Expected chi-square = 200
        assertEquals(200.0, result, 1e-10);
    }

    // ---------- chiSquareTestDataSetsComparison(long[] observed1, long[] observed2) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonNullFirst() {
        testImpl.chiSquareTestDataSetsComparison(null, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonNullSecond() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonLengthMismatch() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonNegativeObservedFirst() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{-1, 2}, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonNegativeObservedSecond() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{-1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAllZeroFirst() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{0, 0}, new long[]{1, 2});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAllZeroSecond() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{0, 0});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonEmptyArrays() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{}, new long[]{});
    }

    @Test
    public void testChiSquareTestDataSetsComparisonBasic() {
        long[] observed1 = {10, 20, 30};
        long[] observed2 = {15, 25, 35};
        double pValue = testImpl.chiSquareTestDataSetsComparison(observed1, observed2);
        assertTrue(pValue > 0 && pValue < 1);
    }

    @Test
    public void testChiSquareTestDataSetsComparisonIdentical() {
        long[] observed1 = {5, 10, 15};
        long[] observed2 = {5, 10, 15};
        double pValue = testImpl.chiSquareTestDataSetsComparison(observed1, observed2);
        assertEquals(1.0, pValue, 1e-10);
    }

    @Test
    public void testChiSquareTestDataSetsComparisonLargeDifference() {
        long[] observed1 = {100, 0};
        long[] observed2 = {0, 100};
        double pValue = testImpl.chiSquareTestDataSetsComparison(observed1, observed2);
        assertTrue(pValue < 1e-10);
    }

    // ---------- chiSquareTestDataSetsComparison(long[] observed1, long[] observed2, double alpha) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaNullFirst() {
        testImpl.chiSquareTestDataSetsComparison(null, new long[]{1, 2}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaNullSecond() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, null, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaLengthMismatch() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{1}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaNegativeObservedFirst() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{-1, 2}, new long[]{1, 2}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaNegativeObservedSecond() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{-1, 2}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaAllZeroFirst() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{0, 0}, new long[]{1, 2}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaAllZeroSecond() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{0, 0}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaEmptyArrays() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{}, new long[]{}, 0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaNegativeAlpha() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{1, 2}, -0.05);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaZeroAlpha() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{1, 2}, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChiSquareTestDataSetsComparisonAlphaAlphaGreaterThanOne() {
        testImpl.chiSquareTestDataSetsComparison(new long[]{1, 2}, new long[]{1, 2}, 1.5);
    }

    @Test
    public void testChiSquareTestDataSetsComparisonAlphaRejectNullFalse() {
        long[] observed1 = {10, 20, 30};
        long[] observed2 = {15, 25, 35};
        // p-value likely > 0.05, so should not reject
        assertFalse(testImpl.chiSquareTestDataSetsComparison(observed1, observed2, 0.05));
    }

    @Test
    public void testChiSquareTestDataSetsComparisonAlphaRejectNullTrue() {
        long[] observed1 = {100, 0};
        long[] observed2 = {0, 100};
        assertTrue(testImpl.chiSquareTestDataSetsComparison(observed1, observed2, 0.05));
    }

    @Test
    public void testChiSquareTestDataSetsComparisonAlphaBoundary() {
        long[] observed1 = {5, 10};
        long[] observed2 = {5, 10};
        assertFalse(testImpl.chiSquareTestDataSetsComparison(observed1, observed2, 0.05));
    }

    // ---------- Edge cases for internal distribution methods ----------

    @Test
    public void testChiSquareTestWithVeryLargeValues() {
        double[] expected = {1e10, 1e10};
        long[] observed = {1, 1};
        double pValue = testImpl.chiSquareTest(expected, observed);
        // chi-square = (1-1e10)^2/1e10 + (1-1e10)^2/1e10 ≈ 2e10, df=1, p-value ~0
        assertTrue(pValue < 1e-10);
    }

    @Test
    public void testChiSquareTestWithVerySmallExpected() {
        double[] expected = {1e-10, 1e-10};
        long[] observed = {1, 1};
        double pValue = testImpl.chiSquareTest(expected, observed);
        // chi-square = (1-1e-10)^2/1e-10 + (1-1e-10)^2/1e-10 ≈ 2e10, df=1, p-value ~0
        assertTrue(pValue < 1e-10);
    }

    @Test
    public void testChiSquareTestWithOneCategory() {
        double[] expected = {5.0};
        long[] observed = {3};
        double pValue = testImpl.chiSquareTest(expected, observed);
        // chi-square = 0.8, df=0? Actually df = n-1 = 0, but implementation may handle differently.
        // Expect p-value = 1.0 (since no degrees of freedom)
        assertEquals(1.0, pValue, 1e-10);
    }

    @Test
    public void testChiSquareDataSetsComparisonWithOneCategory() {
        long[] observed1 = {5};
        long[] observed2 = {3};
        double result = testImpl.chiSquareDataSetsComparison(observed1, observed2);
        // For 2x1 table, chi-square = (5-4)^2/4 + (3-4)^2/4 = 0.25+0.25=0.5
        assertEquals(0.5, result, 1e-10);
    }

    @Test
    public void testChiSquareTestDataSetsComparisonWithOneCategory() {
        long[] observed1 = {5};
        long[] observed2 = {3};
        double pValue = testImpl.chiSquareTestDataSetsComparison(observed1, observed2);
        // df=0? Actually for 2x1 table, df = (2-1)*(1-1)=0, p-value should be 1.0
        assertEquals(1.0, pValue, 1e-10);
    }

    // ---------- Additional fault detection: overflow/underflow ----------

    @Test
    public void testChiSquareWithLargeNumbersNoOverflow() {
        double[] expected = {1e10, 1e10};
        long[] observed = {10000000000L, 10000000000L};
        double result = testImpl.chiSquare(expected, observed);
        // Perfect match, should be 0
        assertEquals(0.0, result, 1e-10);
    }

    @Test
    public void testChiSquareTestWithLargeNumbersNoOverflow() {
        double[] expected = {1e10, 1e10};
        long[] observed = {10000000000L, 10000000000L};
        double pValue = testImpl.chiSquareTest(expected, observed);
        assertEquals(1.0, pValue, 1e-10);
    }

    // ---------- Test for bug ID 102 specific scenario (if known) ----------
    // Bug 102: ChiSquareTestImpl.chiSquareTestDataSetsComparison throws exception when observed arrays have zero counts in same category?
    // We'll test zero counts in both arrays for a category.

    @Test
    public void testChiSquareDataSetsComparisonWithZeroCountsInBoth() {
        long[] observed1 = {0, 10, 0};
        long[] observed2 = {0, 20, 0};
        double result = testImpl.chiSquareDataSetsComparison(observed1, observed2);
        // Categories with zero in both should be skipped? Or cause division by zero?
        // Expected behavior: should handle gracefully, maybe treat as 0 contribution.
        assertTrue(result >= 0);
    }

    @Test
    public void testChiSquareTestDataSetsComparisonWithZeroCountsInBoth() {
        long[] observed1 = {0, 10, 0};
        long[] observed2 = {0, 20, 0};
        double pValue = testImpl.chiSquareTestDataSetsComparison(observed1, observed2);
        assertTrue(pValue > 0 && pValue < 1);
    }

    @Test
    public void testChiSquareTestDataSetsComparisonAlphaWithZeroCountsInBoth() {
        long[] observed1 = {0, 10, 0};
        long[] observed2 = {0, 20, 0};
        boolean result = testImpl.chiSquareTestDataSetsComparison(observed1, observed2, 0.05);
        // Should not throw exception
        assertFalse(result); // likely not significant
    }
}