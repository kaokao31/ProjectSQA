package org.apache.commons.math.stat.descriptive;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for SummaryStatistics, targeting high coverage and bug detection (Defects4J Math-43).
 */
public class SummaryStatisticsTest {

    private SummaryStatistics stats;

    @Before
    public void setUp() {
        stats = new SummaryStatistics();
    }

    // ---------- Empty statistics ----------
    @Test
    public void testEmptyStats() {
        assertEquals(0, stats.getN());
        assertTrue(Double.isNaN(stats.getMean()));
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isNaN(stats.getStandardDeviation()));
        assertTrue(Double.isNaN(stats.getSum()));
        assertTrue(Double.isNaN(stats.getSumsq()));
        assertTrue(Double.isNaN(stats.getMin()));
        assertTrue(Double.isNaN(stats.getMax()));
        assertTrue(Double.isNaN(stats.getPopulationVariance()));
        assertTrue(Double.isNaN(stats.getGeometricMean()));
    }

    // ---------- Single value ----------
    @Test
    public void testSingleValue() {
        stats.addValue(5.0);
        assertEquals(1, stats.getN());
        assertEquals(5.0, stats.getMean(), 1e-12);
        // Bug: variance should be 0 for a single value (sample variance undefined, but expected 0)
        assertEquals(0.0, stats.getVariance(), 1e-12);
        assertEquals(0.0, stats.getStandardDeviation(), 1e-12);
        assertEquals(5.0, stats.getSum(), 1e-12);
        assertEquals(25.0, stats.getSumsq(), 1e-12);
        assertEquals(5.0, stats.getMin(), 1e-12);
        assertEquals(5.0, stats.getMax(), 1e-12);
        assertEquals(0.0, stats.getPopulationVariance(), 1e-12);
        assertEquals(5.0, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Two identical values ----------
    @Test
    public void testTwoIdenticalValues() {
        stats.addValue(3.0);
        stats.addValue(3.0);
        assertEquals(2, stats.getN());
        assertEquals(3.0, stats.getMean(), 1e-12);
        assertEquals(0.0, stats.getVariance(), 1e-12);
        assertEquals(0.0, stats.getStandardDeviation(), 1e-12);
        assertEquals(6.0, stats.getSum(), 1e-12);
        assertEquals(18.0, stats.getSumsq(), 1e-12);
        assertEquals(3.0, stats.getMin(), 1e-12);
        assertEquals(3.0, stats.getMax(), 1e-12);
        assertEquals(0.0, stats.getPopulationVariance(), 1e-12);
        assertEquals(3.0, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Two different values ----------
    @Test
    public void testTwoDifferentValues() {
        stats.addValue(1.0);
        stats.addValue(3.0);
        assertEquals(2, stats.getN());
        assertEquals(2.0, stats.getMean(), 1e-12);
        assertEquals(2.0, stats.getVariance(), 1e-12); // sample variance: ((1-2)^2+(3-2)^2)/(2-1)=2
        assertEquals(Math.sqrt(2.0), stats.getStandardDeviation(), 1e-12);
        assertEquals(4.0, stats.getSum(), 1e-12);
        assertEquals(10.0, stats.getSumsq(), 1e-12);
        assertEquals(1.0, stats.getMin(), 1e-12);
        assertEquals(3.0, stats.getMax(), 1e-12);
        assertEquals(1.0, stats.getPopulationVariance(), 1e-12); // population variance: ((1-2)^2+(3-2)^2)/2=1
        assertEquals(Math.sqrt(3.0), stats.getGeometricMean(), 1e-12);
    }

    // ---------- Multiple values ----------
    @Test
    public void testMultipleValues() {
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        for (double v : values) {
            stats.addValue(v);
        }
        assertEquals(5, stats.getN());
        assertEquals(3.0, stats.getMean(), 1e-12);
        assertEquals(2.5, stats.getVariance(), 1e-12); // sample variance = 2.5
        assertEquals(Math.sqrt(2.5), stats.getStandardDeviation(), 1e-12);
        assertEquals(15.0, stats.getSum(), 1e-12);
        assertEquals(55.0, stats.getSumsq(), 1e-12);
        assertEquals(1.0, stats.getMin(), 1e-12);
        assertEquals(5.0, stats.getMax(), 1e-12);
        assertEquals(2.0, stats.getPopulationVariance(), 1e-12);
        double expectedGeoMean = Math.pow(120, 1.0/5.0);
        assertEquals(expectedGeoMean, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Negative values ----------
    @Test
    public void testNegativeValues() {
        stats.addValue(-2.0);
        stats.addValue(-4.0);
        stats.addValue(-6.0);
        assertEquals(3, stats.getN());
        assertEquals(-4.0, stats.getMean(), 1e-12);
        assertEquals(4.0, stats.getVariance(), 1e-12); // sample variance
        assertEquals(2.0, stats.getStandardDeviation(), 1e-12);
        assertEquals(-12.0, stats.getSum(), 1e-12);
        assertEquals(56.0, stats.getSumsq(), 1e-12);
        assertEquals(-6.0, stats.getMin(), 1e-12);
        assertEquals(-2.0, stats.getMax(), 1e-12);
        assertEquals(8.0/3.0, stats.getPopulationVariance(), 1e-12);
        double expectedGeoMean = Math.pow(48, 1.0/3.0);
        assertEquals(expectedGeoMean, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Zero values ----------
    @Test
    public void testZeroValues() {
        stats.addValue(0.0);
        stats.addValue(0.0);
        assertEquals(2, stats.getN());
        assertEquals(0.0, stats.getMean(), 1e-12);
        assertEquals(0.0, stats.getVariance(), 1e-12);
        assertEquals(0.0, stats.getStandardDeviation(), 1e-12);
        assertEquals(0.0, stats.getSum(), 1e-12);
        assertEquals(0.0, stats.getSumsq(), 1e-12);
        assertEquals(0.0, stats.getMin(), 1e-12);
        assertEquals(0.0, stats.getMax(), 1e-12);
        assertEquals(0.0, stats.getPopulationVariance(), 1e-12);
        assertEquals(0.0, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Large values ----------
    @Test
    public void testLargeValues() {
        stats.addValue(Double.MAX_VALUE);
        stats.addValue(Double.MAX_VALUE);
        assertEquals(2, stats.getN());
        assertEquals(Double.MAX_VALUE, stats.getMean(), 1e-12);
        assertEquals(0.0, stats.getVariance(), 1e-12);
        assertEquals(0.0, stats.getStandardDeviation(), 1e-12);
        assertEquals(Double.MAX_VALUE * 2, stats.getSum(), 1e-12);
        assertEquals(Double.MAX_VALUE * Double.MAX_VALUE * 2, stats.getSumsq(), 1e-12);
        assertEquals(Double.MAX_VALUE, stats.getMin(), 1e-12);
        assertEquals(Double.MAX_VALUE, stats.getMax(), 1e-12);
        assertEquals(0.0, stats.getPopulationVariance(), 1e-12);
        assertEquals(Double.MAX_VALUE, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Very small values ----------
    @Test
    public void testVerySmallValues() {
        stats.addValue(Double.MIN_VALUE);
        stats.addValue(Double.MIN_VALUE);
        assertEquals(2, stats.getN());
        assertEquals(Double.MIN_VALUE, stats.getMean(), 1e-12);
        assertEquals(0.0, stats.getVariance(), 1e-12);
        assertEquals(0.0, stats.getStandardDeviation(), 1e-12);
        assertEquals(Double.MIN_VALUE * 2, stats.getSum(), 1e-12);
        assertEquals(Double.MIN_VALUE * Double.MIN_VALUE * 2, stats.getSumsq(), 1e-12);
        assertEquals(Double.MIN_VALUE, stats.getMin(), 1e-12);
        assertEquals(Double.MIN_VALUE, stats.getMax(), 1e-12);
        assertEquals(0.0, stats.getPopulationVariance(), 1e-12);
        assertEquals(Double.MIN_VALUE, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Clear and re-add ----------
    @Test
    public void testClear() {
        stats.addValue(1.0);
        stats.addValue(2.0);
        stats.clear();
        assertEquals(0, stats.getN());
        assertTrue(Double.isNaN(stats.getMean()));
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isNaN(stats.getSum()));
        assertTrue(Double.isNaN(stats.getSumsq()));
        assertTrue(Double.isNaN(stats.getMin()));
        assertTrue(Double.isNaN(stats.getMax()));

        stats.addValue(10.0);
        assertEquals(1, stats.getN());
        assertEquals(10.0, stats.getMean(), 1e-12);
        assertEquals(0.0, stats.getVariance(), 1e-12);
        assertEquals(10.0, stats.getSum(), 1e-12);
        assertEquals(100.0, stats.getSumsq(), 1e-12);
        assertEquals(10.0, stats.getMin(), 1e-12);
        assertEquals(10.0, stats.getMax(), 1e-12);
    }

    // ---------- Add after clear ----------
    @Test
    public void testAddAfterClear() {
        stats.addValue(5.0);
        stats.clear();
        stats.addValue(7.0);
        stats.addValue(9.0);
        assertEquals(2, stats.getN());
        assertEquals(8.0, stats.getMean(), 1e-12);
        assertEquals(2.0, stats.getVariance(), 1e-12);
        assertEquals(16.0, stats.getSum(), 1e-12);
        assertEquals(130.0, stats.getSumsq(), 1e-12);
        assertEquals(7.0, stats.getMin(), 1e-12);
        assertEquals(9.0, stats.getMax(), 1e-12);
    }

    // ---------- Test toString (optional) ----------
    @Test
    public void testToString() {
        stats.addValue(1.0);
        stats.addValue(2.0);
        String str = stats.toString();
        assertNotNull(str);
        assertTrue(str.contains("n: 2"));
        assertTrue(str.contains("mean: 1.5"));
    }

    // ---------- Test addValue with NaN ----------
    @Test(expected = IllegalArgumentException.class)
    public void testAddNaN() {
        stats.addValue(Double.NaN);
    }

    // ---------- Test addValue with Infinity ----------
    @Test
    public void testAddInfinity() {
        stats.addValue(Double.POSITIVE_INFINITY);
        stats.addValue(1.0);
        assertEquals(2, stats.getN());
        assertTrue(Double.isInfinite(stats.getMean()));
        assertTrue(Double.isNaN(stats.getVariance())); // variance becomes NaN due to infinity
        assertTrue(Double.isInfinite(stats.getSum()));
        assertTrue(Double.isInfinite(stats.getSumsq()));
        assertEquals(Double.POSITIVE_INFINITY, stats.getMax(), 1e-12);
        assertEquals(1.0, stats.getMin(), 1e-12);
    }

    // ---------- Test multiple instances ----------
    @Test
    public void testMultipleInstances() {
        SummaryStatistics stats1 = new SummaryStatistics();
        SummaryStatistics stats2 = new SummaryStatistics();
        stats1.addValue(1.0);
        stats2.addValue(2.0);
        assertEquals(1, stats1.getN());
        assertEquals(1.0, stats1.getMean(), 1e-12);
        assertEquals(1, stats2.getN());
        assertEquals(2.0, stats2.getMean(), 1e-12);
    }

    // ---------- Test getters after addValue with same value ----------
    @Test
    public void testSameValueMultipleTimes() {
        for (int i = 0; i < 10; i++) {
            stats.addValue(5.0);
        }
        assertEquals(10, stats.getN());
        assertEquals(5.0, stats.getMean(), 1e-12);
        assertEquals(0.0, stats.getVariance(), 1e-12);
        assertEquals(0.0, stats.getStandardDeviation(), 1e-12);
        assertEquals(50.0, stats.getSum(), 1e-12);
        assertEquals(250.0, stats.getSumsq(), 1e-12);
        assertEquals(5.0, stats.getMin(), 1e-12);
        assertEquals(5.0, stats.getMax(), 1e-12);
        assertEquals(0.0, stats.getPopulationVariance(), 1e-12);
        assertEquals(5.0, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Test that variance is correct for sample size 2 with large difference ----------
    @Test
    public void testLargeDifference() {
        stats.addValue(-1e10);
        stats.addValue(1e10);
        assertEquals(2, stats.getN());
        assertEquals(0.0, stats.getMean(), 1e-12);
        double expectedVariance = (1e10 * 1e10 + 1e10 * 1e10) / 1.0; // ( ( -1e10 - 0)^2 + (1e10-0)^2 ) / 1 = 2e20
        assertEquals(2e20, stats.getVariance(), 1e-12);
        assertEquals(Math.sqrt(2e20), stats.getStandardDeviation(), 1e-12);
        assertEquals(0.0, stats.getSum(), 1e-12);
        assertEquals(2e20, stats.getSumsq(), 1e-12);
        assertEquals(-1e10, stats.getMin(), 1e-12);
        assertEquals(1e10, stats.getMax(), 1e-12);
        assertEquals(1e20, stats.getPopulationVariance(), 1e-12);
        // Geometric mean of -1e10 and 1e10 is undefined (negative) -> NaN
        assertTrue(Double.isNaN(stats.getGeometricMean()));
    }

    // ---------- Test that geometric mean handles negative values ----------
    @Test
    public void testGeometricMeanWithNegative() {
        stats.addValue(-1.0);
        stats.addValue(2.0);
        assertTrue(Double.isNaN(stats.getGeometricMean()));
    }

    // ---------- Test that geometric mean handles zero ----------
    @Test
    public void testGeometricMeanWithZero() {
        stats.addValue(0.0);
        stats.addValue(2.0);
        assertEquals(0.0, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Test that variance is NaN for n=1 (bug detection) ----------
    // This test is designed to fail if the bug is present (variance returns NaN)
    // The expected correct behavior is 0.0.
    @Test
    public void testVarianceForSingleValue() {
        stats.addValue(42.0);
        assertEquals("Variance for single value should be 0.0", 0.0, stats.getVariance(), 1e-12);
    }

    // ---------- Test that population variance is NaN for n=1 (bug detection) ----------
    @Test
    public void testPopulationVarianceForSingleValue() {
        stats.addValue(42.0);
        assertEquals("Population variance for single value should be 0.0", 0.0, stats.getPopulationVariance(), 1e-12);
    }

    // ---------- Test that standard deviation is 0 for n=1 ----------
    @Test
    public void testStdDevForSingleValue() {
        stats.addValue(7.0);
        assertEquals(0.0, stats.getStandardDeviation(), 1e-12);
    }

    // ---------- Test that addValue updates correctly after many values ----------
    @Test
    public void testManyValues() {
        double sum = 0;
        double sumSq = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 1; i <= 1000; i++) {
            double val = i;
            stats.addValue(val);
            sum += val;
            sumSq += val * val;
            if (val < min) min = val;
            if (val > max) max = val;
        }
        assertEquals(1000, stats.getN());
        assertEquals(sum / 1000.0, stats.getMean(), 1e-12);
        double mean = sum / 1000.0;
        double variance = (sumSq - mean * mean * 1000) / 999.0;
        assertEquals(variance, stats.getVariance(), 1e-9);
        assertEquals(Math.sqrt(variance), stats.getStandardDeviation(), 1e-9);
        assertEquals(sum, stats.getSum(), 1e-9);
        assertEquals(sumSq, stats.getSumsq(), 1e-9);
        assertEquals(min, stats.getMin(), 1e-12);
        assertEquals(max, stats.getMax(), 1e-12);
        double popVariance = (sumSq - mean * mean * 1000) / 1000.0;
        assertEquals(popVariance, stats.getPopulationVariance(), 1e-9);
        double geoMean = Math.pow(1000, 1.0/1000.0); // product of 1..1000 = 1000! but we approximate
        // Actually geometric mean of 1..1000 is exp( (1/1000)*sum(log(i)) )
        // We'll skip exact check for geometric mean due to large product, just check it's finite
        assertTrue(Double.isFinite(stats.getGeometricMean()));
    }

    // ---------- Test that getSumOfLogs works (indirectly) ----------
    @Test
    public void testSumOfLogs() {
        stats.addValue(1.0);
        stats.addValue(2.0);
        stats.addValue(3.0);
        double expectedSumLogs = Math.log(1) + Math.log(2) + Math.log(3);
        // There is no direct getter for sumOfLogs, but geometric mean uses it
        double expectedGeoMean = Math.exp(expectedSumLogs / 3.0);
        assertEquals(expectedGeoMean, stats.getGeometricMean(), 1e-12);
    }

    // ---------- Test that getN returns correct count after multiple adds ----------
    @Test
    public void testGetN() {
        assertEquals(0, stats.getN());
        stats.addValue(1.0);
        assertEquals(1, stats.getN());
        stats.addValue(2.0);
        assertEquals(2, stats.getN());
        stats.clear();
        assertEquals(0, stats.getN());
    }

    // ---------- Test that getters return NaN after clear ----------
    @Test
    public void testGettersAfterClear() {
        stats.addValue(1.0);
        stats.clear();
        assertTrue(Double.isNaN(stats.getMean()));
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isNaN(stats.getStandardDeviation()));
        assertTrue(Double.isNaN(stats.getSum()));
        assertTrue(Double.isNaN(stats.getSumsq()));
        assertTrue(Double.isNaN(stats.getMin()));
        assertTrue(Double.isNaN(stats.getMax()));
        assertTrue(Double.isNaN(stats.getPopulationVariance()));
        assertTrue(Double.isNaN(stats.getGeometricMean()));
    }

    // ---------- Test that addValue with same value does not cause overflow in variance ----------
    @Test
    public void testVarianceStability() {
        double val = 1e10;
        stats.addValue(val);
        stats.addValue(val);
        stats.addValue(val);
        assertEquals(0.0, stats.getVariance(), 1e-12);
        assertEquals(0.0, stats.getPopulationVariance(), 1e-12);
    }

    // ---------- Test that addValue with alternating signs works ----------
    @Test
    public void testAlternatingSigns() {
        stats.addValue(1.0);
        stats.addValue(-1.0);
        stats.addValue(1.0);
        stats.addValue(-1.0);
        assertEquals(4, stats.getN());
        assertEquals(0.0, stats.getMean(), 1e-12);
        double expectedVariance = (1.0 + 1.0 + 1.0 + 1.0) / 3.0; // sum of squares = 4, n-1=3
        assertEquals(4.0/3.0, stats.getVariance(), 1e-12);
        assertEquals(0.0, stats.getSum(), 1e-12);
        assertEquals(4.0, stats.getSumsq(), 1e-12);
        assertEquals(-1.0, stats.getMin(), 1e-12);
        assertEquals(1.0, stats.getMax(), 1e-12);
        assertEquals(1.0, stats.getPopulationVariance(), 1e-12);
        // Geometric mean: product = 1 * -1 * 1 * -1 = 1, but negative values cause NaN
        assertTrue(Double.isNaN(stats.getGeometricMean()));
    }

    // ---------- Test that addValue with very large numbers does not cause catastrophic cancellation ----------
    @Test
    public void testLargeNumbersVariance() {
        double base = 1e8;
        stats.addValue(base);
        stats.addValue(base + 1);
        stats.addValue(base + 2);
        double mean = base + 1;
        double expectedVariance = (1 + 0 + 1) / 2.0; // deviations: -1, 0, 1 -> squares: 1,0,1 -> sum=2, n-1=2
        assertEquals(1.0, stats.getVariance(), 1e-12);
        assertEquals(mean, stats.getMean(), 1e-12);
    }

    // ---------- Test that getMin and getMax are correct after multiple adds ----------
    @Test
    public void testMinMax() {
        stats.addValue(5.0);
        stats.addValue(3.0);
        stats.addValue(7.0);
        assertEquals(3.0, stats.getMin(), 1e-12);
        assertEquals(7.0, stats.getMax(), 1e-12);
        stats.addValue(2.0);
        assertEquals(2.0, stats.getMin(), 1e-12);
        stats.addValue(9.0);
        assertEquals(9.0, stats.getMax(), 1e-12);
    }

    // ---------- Test that getSum and getSumsq are correct ----------
    @Test
    public void testSumAndSumsq() {
        stats.addValue(2.0);
        stats.addValue(3.0);
        stats.addValue(4.0);
        assertEquals(9.0, stats.getSum(), 1e-12);
        assertEquals(4.0 + 9.0 + 16.0, stats.getSumsq(), 1e-12);
    }

    // ---------- Test that addValue with Double.NEGATIVE_INFINITY ----------
    @Test
    public void testNegativeInfinity() {
        stats.addValue(Double.NEGATIVE_INFINITY);
        stats.addValue(1.0);
        assertEquals(2, stats.getN());
        assertTrue(Double.isInfinite(stats.getMean()));
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isInfinite(stats.getSum()));
        assertTrue(Double.isInfinite(stats.getSumsq()));
        assertEquals(Double.NEGATIVE_INFINITY, stats.getMin(), 1e-12);
        assertEquals(1.0, stats.getMax(), 1e-12);
    }

    // ---------- Test that addValue with both infinities ----------
    @Test
    public void testBothInfinities() {
        stats.addValue(Double.POSITIVE_INFINITY);
        stats.addValue(Double.NEGATIVE_INFINITY);
        assertEquals(2, stats.getN());
        assertTrue(Double.isNaN(stats.getMean())); // mean of +inf and -inf is NaN
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isNaN(stats.getSum()));
        assertTrue(Double.isInfinite(stats.getSumsq())); // sumsq = inf^2 + (-inf)^2 = inf
        assertEquals(Double.NEGATIVE_INFINITY, stats.getMin(), 1e-12);
        assertEquals(Double.POSITIVE_INFINITY, stats.getMax(), 1e-12);
    }

    // ---------- Test that addValue with NaN throws exception ----------
    @Test(expected = IllegalArgumentException.class)
    public void testAddNaNThrows() {
        stats.addValue(Double.NaN);
    }

    // ---------- Test that addValue with null? Not applicable ----------
}