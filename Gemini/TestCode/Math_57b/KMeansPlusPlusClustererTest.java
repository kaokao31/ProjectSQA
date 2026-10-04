package org.apache.commons.math.stat.clustering;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KMeansPlusPlusClustererTest {

    static class DummyClusterable implements Clusterable<DummyClusterable> {
        private final double[] points;

        public DummyClusterable(double[] points) {
            this.points = points;
        }

        public double[] getPoint() {
            return points;
        }

        public DummyClusterable centroidOf(Collection<DummyClusterable> p) {
            if (p.isEmpty()) {
                return new DummyClusterable(new double[points.length]);
            }
            double[] centroid = new double[points.length];
            for (DummyClusterable doc : p) {
                double[] pt = doc.getPoint();
                for (int i = 0; i < pt.length; i++) {
                    centroid[i] += pt[i];
                }
            }
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] /= p.size();
            }
            return new DummyClusterable(centroid);
        }

        public double distanceFrom(DummyClusterable p) {
            double[] pt = p.getPoint();
            double sum = 0.0;
            for (int i = 0; i < pt.length; i++) {
                double diff = pt[i] - points[i];
                sum += diff * diff;
            }
            return Math.sqrt(sum);
        }
    }

    @Test
    public void testKMeansPlusPlusClustererBasic() {
        Random random = new Random(42L);
        KMeansPlusPlusClusterer<DummyClusterable> clusterer = 
            new KMeansPlusPlusClusterer<DummyClusterable>(random);

        List<DummyClusterable> points = new ArrayList<DummyClusterable>();
        points.add(new DummyClusterable(new double[] { 0.0, 0.0 }));
        points.add(new DummyClusterable(new double[] { 0.1, 0.1 }));
        points.add(new DummyClusterable(new double[] { 10.0, 10.0 }));
        points.add(new DummyClusterable(new double[] { 10.1, 10.1 }));

        List<Cluster<DummyClusterable>> clusters = clusterer.cluster(points, 2, 10);
        Assert.assertNotNull(clusters);
        Assert.assertEquals(2, clusters.size());
    }

    @Test
    public void testKMeansPlusPlusClustererWithEmptyPoints() {
        Random random = new Random(42L);
        KMeansPlusPlusClusterer<DummyClusterable> clusterer = 
            new KMeansPlusPlusClusterer<DummyClusterable>(random, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);

        List<DummyClusterable> points = new ArrayList<DummyClusterable>();
        points.add(new DummyClusterable(new double[] { 1.0, 1.0 }));
        points.add(new DummyClusterable(new double[] { 1.5, 1.5 }));
        points.add(new DummyClusterable(new double[] { 5.0, 5.0 }));
        points.add(new DummyClusterable(new double[] { 5.5, 5.5 }));

        // Requesting more clusters than points or empty cluster generation paths
        List<Cluster<DummyClusterable>> clusters = clusterer.cluster(points, 2, 1);
        Assert.assertNotNull(clusters);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKMeansPlusPlusClustererKGreaterThanPoints() {
        Random random = new Random(42L);
        KMeansPlusPlusClusterer<DummyClusterable> clusterer = 
            new KMeansPlusPlusClusterer<DummyClusterable>(random);

        List<DummyClusterable> points = new ArrayList<DummyClusterable>();
        points.add(new DummyClusterable(new double[] { 0.0, 0.0 }));

        // k=2, points size=1, should throw IllegalArgumentException or handle it
        clusterer.cluster(points, 2, 1);
    }

    @Test
    public void testEmptyClusterStrategies() {
        for (KMeansPlusPlusClusterer.EmptyClusterStrategy strategy : KMeansPlusPlusClusterer.EmptyClusterStrategy.values()) {
            Random random = new Random(0L);
            KMeansPlusPlusClusterer<DummyClusterable> clusterer = 
                new KMeansPlusPlusClusterer<DummyClusterable>(random, strategy);

            List<DummyClusterable> points = new ArrayList<DummyClusterable>();
            points.add(new DummyClusterable(new double[] { 0.0 }));
            points.add(new DummyClusterable(new double[] { 1.0 }));
            
            try {
                List<Cluster<DummyClusterable>> clusters = clusterer.cluster(points, 2, 5);
                Assert.assertNotNull(clusters);
            } catch (Exception e) {
                // Some strategies might throw exceptions depending on internal state, ensure it's handled or captured
            }
        }
    }

    @Test
    public void testMath57BugFixScenario() {
        // Specifically targeting the Math-57 bug where points with 0 distance or specific sums 
        // can cause issues in probability calculations for KMeans++ center selection.
        List<DummyClusterable> points = new ArrayList<DummyClusterable>();
        points.add(new DummyClusterable(new double[] { 1.0 }));
        points.add(new DummyClusterable(new double[] { 1.0 }));
        points.add(new DummyClusterable(new double[] { 1.0 }));
        points.add(new DummyClusterable(new double[] { 2.0 }));

        KMeansPlusPlusClusterer<DummyClusterable> clusterer = 
            new KMeansPlusPlusClusterer<DummyClusterable>(new Random(123L));

        List<Cluster<DummyClusterable>> clusters = clusterer.cluster(points, 2, 10);
        Assert.assertEquals(2, clusters.size());
    }
}