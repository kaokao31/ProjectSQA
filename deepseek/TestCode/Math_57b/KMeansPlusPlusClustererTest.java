package org.apache.commons.math.stat.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for KMeansPlusPlusClusterer.
 * Targets maximum code coverage and fault detection (Defects4J Math-57).
 */
public class KMeansPlusPlusClustererTest {

    private KMeansPlusPlusClusterer<EuclideanIntegerPoint> clusterer;
    private Random random;

    @Before
    public void setUp() {
        // Use a fixed seed for reproducibility
        random = new Random(42);
        clusterer = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(random);
    }

    // Helper to create a list of EuclideanIntegerPoint from int arrays
    private List<EuclideanIntegerPoint> createPoints(int[][] coordinates) {
        List<EuclideanIntegerPoint> points = new ArrayList<EuclideanIntegerPoint>();
        for (int[] coord : coordinates) {
            points.add(new EuclideanIntegerPoint(coord));
        }
        return points;
    }

    // Helper to create a list of EuclideanIntegerPoint with a single point
    private List<EuclideanIntegerPoint> singlePoint() {
        return createPoints(new int[][]{{1, 2}});
    }

    // Helper to create a list of EuclideanIntegerPoint with two distinct points
    private List<EuclideanIntegerPoint> twoDistinctPoints() {
        return createPoints(new int[][]{{0, 0}, {10, 10}});
    }

    // Helper to create a list of EuclideanIntegerPoint with three points forming a triangle
    private List<EuclideanIntegerPoint> threePoints() {
        return createPoints(new int[][]{{0, 0}, {5, 5}, {10, 0}});
    }

    // Helper to create a list of EuclideanIntegerPoint with many points
    private List<EuclideanIntegerPoint> manyPoints() {
        return createPoints(new int[][]{
            {1, 1}, {2, 2}, {3, 3}, {10, 10}, {11, 11}, {12, 12},
            {20, 20}, {21, 21}, {22, 22}
        });
    }

    // ========================
    // Normal operation tests
    // ========================

    @Test
    public void testClusterWithTwoPointsAndOneCluster() {
        List<EuclideanIntegerPoint> points = twoDistinctPoints();
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 1, 10);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 1 cluster", 1, clusters.size());
        assertEquals("Cluster should contain both points", 2, clusters.get(0).getPoints().size());
    }

    @Test
    public void testClusterWithTwoPointsAndTwoClusters() {
        List<EuclideanIntegerPoint> points = twoDistinctPoints();
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 2 clusters", 2, clusters.size());
        // Each cluster should contain exactly one point
        for (Cluster<EuclideanIntegerPoint> c : clusters) {
            assertEquals("Each cluster should have one point", 1, c.getPoints().size());
        }
    }

    @Test
    public void testClusterWithThreePointsAndTwoClusters() {
        List<EuclideanIntegerPoint> points = threePoints();
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 2 clusters", 2, clusters.size());
        int totalPoints = 0;
        for (Cluster<EuclideanIntegerPoint> c : clusters) {
            totalPoints += c.getPoints().size();
        }
        assertEquals("Total points should be 3", 3, totalPoints);
    }

    @Test
    public void testClusterWithManyPointsAndThreeClusters() {
        List<EuclideanIntegerPoint> points = manyPoints();
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 3, 20);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 3 clusters", 3, clusters.size());
        int totalPoints = 0;
        for (Cluster<EuclideanIntegerPoint> c : clusters) {
            totalPoints += c.getPoints().size();
        }
        assertEquals("Total points should be 9", 9, totalPoints);
    }

    // ========================
    // Edge cases: empty and null
    // ========================

    @Test(expected = IllegalArgumentException.class)
    public void testClusterWithNullPoints() {
        clusterer.cluster(null, 2, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClusterWithEmptyPoints() {
        List<EuclideanIntegerPoint> empty = new ArrayList<EuclideanIntegerPoint>();
        clusterer.cluster(empty, 2, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClusterWithNegativeK() {
        List<EuclideanIntegerPoint> points = twoDistinctPoints();
        clusterer.cluster(points, -1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClusterWithZeroK() {
        List<EuclideanIntegerPoint> points = twoDistinctPoints();
        clusterer.cluster(points, 0, 10);
    }

    // ========================
    // Bug-triggering edge cases (Defects4J Math-57)
    // ========================

    // When k > number of points, the algorithm should handle gracefully.
    // Defects4J Math-57: likely bug where k > points throws exception or returns incorrect clusters.
    @Test
    public void testClusterWithKGreaterThanNumberOfPoints() {
        List<EuclideanIntegerPoint> points = singlePoint(); // 1 point
        // Request 2 clusters
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertNotNull("Clusters should not be null", clusters);
        // Expect either 1 cluster (since only one point) or 2 clusters with empty ones?
        // The fix should return at most as many clusters as points.
        // We'll assert that the number of clusters is <= number of points.
        assertTrue("Number of clusters should not exceed number of points",
                   clusters.size() <= points.size());
        // Also ensure no cluster is null
        for (Cluster<EuclideanIntegerPoint> c : clusters) {
            assertNotNull("Cluster should not be null", c);
        }
    }

    @Test
    public void testClusterWithKEqualToNumberOfPoints() {
        List<EuclideanIntegerPoint> points = twoDistinctPoints(); // 2 points
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 2 clusters", 2, clusters.size());
        // Each cluster should have exactly one point
        for (Cluster<EuclideanIntegerPoint> c : clusters) {
            assertEquals("Each cluster should have one point", 1, c.getPoints().size());
        }
    }

    @Test
    public void testClusterWithSinglePointAndKOne() {
        List<EuclideanIntegerPoint> points = singlePoint();
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 1, 10);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 1 cluster", 1, clusters.size());
        assertEquals("Cluster should contain the single point", 1, clusters.get(0).getPoints().size());
    }

    @Test
    public void testClusterWithDuplicatePoints() {
        // All points identical
        List<EuclideanIntegerPoint> points = createPoints(new int[][]{{1,1}, {1,1}, {1,1}});
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertNotNull("Clusters should not be null", clusters);
        // With identical points, algorithm may produce 1 or 2 clusters depending on initialization.
        // But it should not throw an exception.
        assertTrue("Number of clusters should be at least 1", clusters.size() >= 1);
        int totalPoints = 0;
        for (Cluster<EuclideanIntegerPoint> c : clusters) {
            totalPoints += c.getPoints().size();
        }
        assertEquals("Total points should be 3", 3, totalPoints);
    }

    // ========================
    // Additional edge cases
    // ========================

    @Test(expected = IllegalArgumentException.class)
    public void testClusterWithNegativeMaxIterations() {
        List<EuclideanIntegerPoint> points = twoDistinctPoints();
        clusterer.cluster(points, 2, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClusterWithZeroMaxIterations() {
        List<EuclideanIntegerPoint> points = twoDistinctPoints();
        clusterer.cluster(points, 2, 0);
    }

    @Test
    public void testClusterWithLargeMaxIterations() {
        List<EuclideanIntegerPoint> points = manyPoints();
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 3, Integer.MAX_VALUE);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 3 clusters", 3, clusters.size());
    }

    // ========================
    // Stability and randomness
    // ========================

    @Test
    public void testClusterDeterministicWithSameSeed() {
        List<EuclideanIntegerPoint> points = threePoints();
        // First run
        List<Cluster<EuclideanIntegerPoint>> clusters1 = clusterer.cluster(points, 2, 10);
        // Second run with same random seed
        clusterer = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(42));
        List<Cluster<EuclideanIntegerPoint>> clusters2 = clusterer.cluster(points, 2, 10);
        // The clusters should be identical in terms of point assignments
        assertEquals("Number of clusters should match", clusters1.size(), clusters2.size());
        for (int i = 0; i < clusters1.size(); i++) {
            assertEquals("Cluster " + i + " size should match",
                         clusters1.get(i).getPoints().size(),
                         clusters2.get(i).getPoints().size());
        }
    }

    // ========================
    // Test with initial clusters (if method exists)
    // ========================

    // Note: The KMeansPlusPlusClusterer may have a method that accepts initial clusters.
    // We'll test that variant if the constructor or method signature allows it.
    // For completeness, we assume there is a method: cluster(Collection<T> points, List<Cluster<T>> initialClusters, int maxIterations)
    // But since we don't have the exact signature, we'll skip this test to avoid compilation errors.
    // Instead, we focus on the standard cluster(points, k, maxIterations) method.

    // ========================
    // Test with different distance measures (if configurable)
    // ========================

    // The default distance measure is Euclidean. We'll assume it's fixed.

    // ========================
    // Additional coverage: ensure no NullPointerException on internal operations
    // ========================

    @Test
    public void testClusterWithPointsContainingNull() {
        List<EuclideanIntegerPoint> points = new ArrayList<EuclideanIntegerPoint>();
        points.add(new EuclideanIntegerPoint(new int[]{0,0}));
        points.add(null); // null point
        try {
            clusterer.cluster(points, 2, 10);
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (NullPointerException e) {
            // also acceptable depending on implementation
        }
    }

    // ========================
    // Test with one-dimensional points
    // ========================

    @Test
    public void testClusterWithOneDimensionalPoints() {
        List<EuclideanIntegerPoint> points = createPoints(new int[][]{{1}, {2}, {3}, {10}, {11}});
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 2 clusters", 2, clusters.size());
        int totalPoints = 0;
        for (Cluster<EuclideanIntegerPoint> c : clusters) {
            totalPoints += c.getPoints().size();
        }
        assertEquals("Total points should be 5", 5, totalPoints);
    }

    // ========================
    // Test with high-dimensional points
    // ========================

    @Test
    public void testClusterWithHighDimensionalPoints() {
        List<EuclideanIntegerPoint> points = createPoints(new int[][]{
            {1,2,3,4}, {5,6,7,8}, {9,10,11,12}
        });
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 2, 10);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 2 clusters", 2, clusters.size());
        int totalPoints = 0;
        for (Cluster<EuclideanIntegerPoint> c : clusters) {
            totalPoints += c.getPoints().size();
        }
        assertEquals("Total points should be 3", 3, totalPoints);
    }

    // ========================
    // Test with maxIterations = 1 (single iteration)
    // ========================

    @Test
    public void testClusterWithSingleIteration() {
        List<EuclideanIntegerPoint> points = manyPoints();
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 3, 1);
        assertNotNull("Clusters should not be null", clusters);
        assertEquals("Should have exactly 3 clusters", 3, clusters.size());
        // Points may not be optimally assigned, but no exception should occur
    }
}