package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DiscreteDistributionTest {

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("zero", 0.0));
        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("negative", -1.0));
        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = NotANumberException.class)
    public void testConstructorNanProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("nan", Double.NaN));
        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = org.apache.commons.math3.exception.NotPositiveException.class)
    public void testConstructorInfiniteProbability() {
        // Depending on math3 version, infinity might throw NotPositive or NotStrictlyPositive
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("infinite", Double.POSITIVE_INFINITY));
        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = org.apache.commons.math3.exception.NullArgumentException.class)
    public void testConstructorNullSamples() {
        new DiscreteDistribution<String>(null);
    }

    @Test(expected = org.apache.commons.math3.exception.NoDataException.class)
    public void testConstructorEmptySamples() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        new DiscreteDistribution<String>(samples);
    }

    @Test
    public void testBasicDistribution() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 0.3));
        samples.add(new Pair<String, Double>("b", 0.7));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);

        List<Pair<String, Double>> pmf = dist.getSamples();
        Assert.assertEquals(2, pmf.size());
        Assert.assertEquals(0.3, dist.probability("a"), 1e-12);
        Assert.assertEquals(0.7, dist.probability("b"), 1e-12);
        Assert.assertEquals(0.0, dist.probability("c"), 1e-12);
        Assert.assertEquals(0.0, dist.probability(null), 1e-12);

        // Test sampling
        String sample = dist.sample();
        Assert.assertTrue(sample.equals("a") || sample.equals("b"));

        String[] sampledArray = dist.sample(10);
        Assert.assertEquals(10, sampledArray.length);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testSampleInvalidSize() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 1.0));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.sample(0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testSampleNegativeSize() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 1.0));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.sample(-5);
    }

    @Test
    public void testProbabilityWithNullSamples() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>(null, 0.5));
        samples.add(new Pair<String, Double>("a", 0.5));

        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        Assert.assertEquals(0.5, dist.probability(null), 1e-12);
        Assert.assertEquals(0.5, dist.probability("a"), 1e-12);
    }

    @Test
    public void testNumericalMoments() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(new Pair<String, Double>("a", 1.0));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);

        Assert.assertTrue(Double.isNaN(dist.getNumericalMean()));
        Assert.assertTrue(Double.isNaN(dist.getNumericalVariance()));
        Assert.assertFalse(dist.isSupportConnected());
        Assert.assertFalse(dist.isSupportLowerBoundInclusive());
        Assert.assertFalse(dist.isSupportUpperBoundInclusive());
        Assert.assertEquals(0, dist.getSupportLowerBound());
        Assert.assertEquals(0, dist.getSupportUpperBound());
    }
}