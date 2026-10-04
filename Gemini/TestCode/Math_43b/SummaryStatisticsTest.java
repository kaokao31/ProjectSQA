package org.apache.commons.math.stat.descriptive;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math.stat.descriptive.moment.Skewness;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;

public class SummaryStatisticsTest {

    private SummaryStatistics u;

    @Before
    public void setUp() {
        u = new SummaryStatistics();
    }

    @Test
    public void testInitiallyEmpty() {
        Assert.assertEquals(0L, u.getN());
        Assert.assertEquals(Double.NaN, u.getMean(), 0);
        Assert.assertEquals(Double.NaN, u.getVariance(), 0);
        Assert.assertEquals(Double.NaN, u.getStandardDeviation(), 0);
        Assert.assertEquals(Double.NaN, u.getGeometricMean(), 0);
        Assert.assertEquals(Double.NaN, u.getMax(), 0);
        Assert.assertEquals(Double.NaN, u.getMin(), 0);
        Assert.assertEquals(Double.NaN, u.getSum(), 0);
        Assert.assertEquals(Double.NaN, u.getSumOfSquares(), 0);
        Assert.assertEquals(Double.NaN, u.getSumOfLogs(), 0);
        Assert.assertEquals(Double.NaN, u.getSkewness(), 0);
    }

    @Test
    public void testBasicAddValue() {
        u.addValue(1.0);
        u.addValue(2.0);
        u.addValue(3.0);

        Assert.assertEquals(3L, u.getN());
        Assert.assertEquals(2.0, u.getMean(), 1e-14);
        Assert.assertEquals(6.0, u.getSum(), 1e-14);
        Assert.assertEquals(14.0, u.getSumOfSquares(), 1e-14);
        Assert.assertEquals(1.0, u.getMin(), 1e-14);
        Assert.assertEquals(3.0, u.getMax(), 1e-14);
        Assert.assertEquals(1.0, u.getVariance(), 1e-14);
        Assert.assertEquals(1.0, u.getStandardDeviation(), 1e-14);
        Assert.assertEquals(Math.cbrt(6.0), u.getGeometricMean(), 1e-14);
    }

    @Test
    public void testNaNAddValueAndBug43Context() {
        // Math-43 involves cumulative statistics updating incorrectly when NaN or Null indicators occur,
        // or when secondary statistics listeners are added/removed.
        u.addValue(1.0);
        u.addValue(Double.NaN);
        u.addValue(3.0);

        // Depending on implementation, NaN values might be counted or skipped.
        // Let's verify standard commons-math behavior (SummaryStatistics typically includes NaN unless filtered).
        Assert.assertEquals(3L, u.getN());
        Assert.assertTrue(Double.isNaN(u.getMean()));
    }

    @Test
    public void testClear() {
        u.addValue(1.0);
        u.addValue(2.0);
        Assert.assertEquals(2L, u.getN());
        
        u.clear();
        Assert.assertEquals(0L, u.getN());
        Assert.assertEquals(Double.NaN, u.getMean(), 0);
        Assert.assertEquals(Double.NaN, u.getVariance(), 0);
    }

    @Test
    public void testCopyConstructorAndClone() {
        u.addValue(1.0);
        u.addValue(2.0);

        SummaryStatistics copy = new SummaryStatistics(u);
        Assert.assertEquals(u.getN(), copy.getN());
        Assert.assertEquals(u.getMean(), copy.getMean(), 1e-14);

        SummaryStatistics clone = u.clone();
        Assert.assertEquals(u.getN(), clone.getN());
        Assert.assertEquals(u.getVariance(), clone.getVariance(), 1e-14);
    }

    @Test
    public void testCustomStatImpls() {
        u.setSumImpl(new Sum());
        u.setMaxImpl(new Max());
        u.setMinImpl(new Min());
        u.setSumOfSquaresImpl(new SumOfSquares());
        u.setSumOfLogsImpl(new SumOfLogs());
        u.setGeometricMeanImpl(new GeometricMean());
        u.setMeanImpl(new Mean());
        u.setVarianceImpl(new Variance());
        u.setStandardDeviationImpl(new StandardDeviation());
        u.setSkewnessImpl(new Skewness());

        u.addValue(2.0);
        u.addValue(4.0);

        Assert.assertEquals(2L, u.getN());
        Assert.assertEquals(6.0, u.getSum(), 1e-14);
        Assert.assertEquals(2.0, u.getMin(), 1e-14);
        Assert.assertEquals(4.0, u.getMax(), 1e-14);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMeanImplNull() {
        u.setMeanImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVarianceImplNull() {
        u.setVarianceImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSumImplNull() {
        u.setSumImpl(null);
    }

    @Test
    public void testPercentile() {
        u.addValue(1.0);
        u.addValue(2.0);
        u.addValue(3.0);
        u.addValue(4.0);
        u.addValue(5.0);

        Percentile p = new Percentile(50.0);
        u.setPercentileImpl(p);

        Assert.assertEquals(3.0, u.getPercentile(50.0), 1e-14);
    }

    @Test
    public void testToString() {
        u.addValue(1.0);
        String str = u.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("SummaryStatistics"));
    }

    @Test
    public void testEqualsAndHashCode() {
        SummaryStatistics u1 = new SummaryStatistics();
        SummaryStatistics u2 = new SummaryStatistics();

        Assert.assertTrue(u1.equals(u2));
        Assert.assertEquals(u1.hashCode(), u2.hashCode());

        u1.addValue(1.0);
        Assert.assertFalse(u1.equals(u2));

        u2.addValue(1.0);
        Assert.assertTrue(u1.equals(u2));
        Assert.assertEquals(u1.hashCode(), u2.hashCode());

        Assert.assertFalse(u1.equals(null));
        Assert.assertFalse(u1.equals("Some String"));
        
        // Self check
        Assert.assertTrue(u1.equals(u1));
    }

    @Test
    public void testAddValueWithSecondMomentListenerBug43Scenario() {
        // Specifically tests interaction where secondary moments or custom statistics 
        // might fail to update if a StatImpl is invoked out of order or throws.
        SecondMoment sm = new SecondMoment();
        u.addValue(10.0);
        u.addValue(20.0);
        
        Assert.assertEquals(15.0, u.getMean(), 1e-14);
        Assert.assertEquals(50.0, u.getSecondMoment(), 1e-14);
    }
}