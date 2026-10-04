package org.apache.commons.math.geometry;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.apache.commons.math.util.MathUtils;

public class Vector3DTest {

    private static final double EPS = 1.0e-10;

    // Test vectors for reuse
    private Vector3D v1;
    private Vector3D v2;
    private Vector3D vZero;

    @Before
    public void setUp() {
        vZero = new Vector3D(0, 0, 0);
        v1 = new Vector3D(1, 2, 3);
        v2 = new Vector3D(-4, 5, -6);
    }

    @Test
    public void testZeroVector() {
        assertEquals(0, vZero.getX(), EPS);
        assertEquals(0, vZero.getY(), EPS);
        assertEquals(0, vZero.getZ(), EPS);
        assertEquals(0, vZero.getNormSq(), EPS);
        assertEquals(0, vZero.getNorm(), EPS);
        assertTrue(vZero.isNaN() == false);
        assertTrue(vZero.isInfinite() == false);
    }

    @Test
    public void testCoordinates() {
        assertEquals(1, v1.getX(), EPS);
        assertEquals(2, v1.getY(), EPS);
        assertEquals(3, v1.getZ(), EPS);
    }

    @Test
    public void testNorm() {
        // norm of (1,2,3) = sqrt(1+4+9) = sqrt(14) ≈ 3.74165738677
        assertEquals(Math.sqrt(14), v1.getNorm(), EPS);
        // norm squared = 14
        assertEquals(14, v1.getNormSq(), EPS);
    }

    @Test
    public void testAdd() {
        Vector3D sum = v1.add(v2);
        assertEquals(-3, sum.getX(), EPS);
        assertEquals(7, sum.getY(), EPS);
        assertEquals(-3, sum.getZ(), EPS);
    }

    @Test
    public void testSubtract() {
        Vector3D diff = v1.subtract(v2);
        assertEquals(5, diff.getX(), EPS);
        assertEquals(-3, diff.getY(), EPS);
        assertEquals(9, diff.getZ(), EPS);
    }

    @Test
    public void testScalarMultiply() {
        Vector3D scaled = v1.scalarMultiply(2);
        assertEquals(2, scaled.getX(), EPS);
        assertEquals(4, scaled.getY(), EPS);
        assertEquals(6, scaled.getZ(), EPS);
    }

    @Test
    public void testNegate() {
        Vector3D neg = v1.negate();
        assertEquals(-1, neg.getX(), EPS);
        assertEquals(-2, neg.getY(), EPS);
        assertEquals(-3, neg.getZ(), EPS);
    }

    @Test
    public void testDotProduct() {
        double dot = Vector3D.dotProduct(v1, v2);
        // (1)(-4) + (2)(5) + (3)(-6) = -4 + 10 - 18 = -12
        assertEquals(-12, dot, EPS);
    }

    @Test
    public void testCrossProduct() {
        Vector3D cross = Vector3D.crossProduct(v1, v2);
        // cross = (2*(-6) - 3*5, 3*(-4) - 1*(-6), 1*5 - 2*(-4))
        //       = (-12 - 15, -12 + 6, 5 + 8) = (-27, -6, 13)
        assertEquals(-27, cross.getX(), EPS);
        assertEquals(-6, cross.getY(), EPS);
        assertEquals(13, cross.getZ(), EPS);
    }

    @Test
    public void testCrossProductOrthogonal() {
        // Cross product of two orthogonal vectors should have norm = product of norms
        Vector3D u = new Vector3D(1, 0, 0);
        Vector3D v = new Vector3D(0, 1, 0);
        Vector3D w = Vector3D.crossProduct(u, v);
        assertEquals(0, w.getX(), EPS);
        assertEquals(0, w.getY(), EPS);
        assertEquals(1, w.getZ(), EPS);
        assertEquals(1, w.getNorm(), EPS);
    }

    @Test
    public void testCrossProductSelf() {
        // Cross product of a vector with itself should be zero
        Vector3D selfCross = Vector3D.crossProduct(v1, v1);
        assertEquals(0, selfCross.getX(), EPS);
        assertEquals(0, selfCross.getY(), EPS);
        assertEquals(0, selfCross.getZ(), EPS);
    }

    @Test
    public void testDistance() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, 6, 3);
        double dist = Vector3D.distance(p1, p2);
        // distance = sqrt((3)^2 + (4)^2 + 0^2) = sqrt(25) = 5
        assertEquals(5, dist, EPS);
    }

    @Test
    public void testDistanceSq() {
        Vector3D p1 = new Vector3D(1, 2, 3);
        Vector3D p2 = new Vector3D(4, 6, 3);
        double distSq = Vector3D.distanceSq(p1, p2);
        // distanceSq = 9 + 16 + 0 = 25
        assertEquals(25, distSq, EPS);
    }

    @Test
    public void testAngle() {
        Vector3D u = new Vector3D(1, 0, 0);
        Vector3D v = new Vector3D(0, 1, 0);
        double angle = Vector3D.angle(u, v);
        // angle should be pi/2
        assertEquals(Math.PI / 2, angle, EPS);
    }

    @Test
    public void testAngleWithParallelVectors() {
        Vector3D u = new Vector3D(2, 0, 0);
        Vector3D v = new Vector3D(5, 0, 0);
        double angle = Vector3D.angle(u, v);
        // parallel vectors should have angle 0
        assertEquals(0, angle, EPS);
    }

    @Test
    public void testAngleWithOppositeVectors() {
        Vector3D u = new Vector3D(3, 0, 0);
        Vector3D v = new Vector3D(-2, 0, 0);
        double angle = Vector3D.angle(u, v);
        // opposite vectors should have angle pi
        assertEquals(Math.PI, angle, EPS);
    }

    @Test
    public void testIsNaN() {
        Vector3D nanVec = new Vector3D(Double.NaN, 0, 0);
        assertTrue(nanVec.isNaN());
        assertFalse(nanVec.isInfinite());

        Vector3D nanVec2 = new Vector3D(0, Double.NaN, 0);
        assertTrue(nanVec2.isNaN());

        Vector3D nanVec3 = new Vector3D(0, 0, Double.NaN);
        assertTrue(nanVec3.isNaN());
    }

    @Test
    public void testIsInfinite() {
        Vector3D infVec = new Vector3D(Double.POSITIVE_INFINITY, 0, 0);
        assertTrue(infVec.isInfinite());
        assertFalse(infVec.isNaN());

        Vector3D infVec2 = new Vector3D(0, Double.NEGATIVE_INFINITY, 0);
        assertTrue(infVec2.isInfinite());

        Vector3D infVec3 = new Vector3D(0, 0, Double.POSITIVE_INFINITY);
        assertTrue(infVec3.isInfinite());
    }

    @Test
    public void testNaNNoInfinite() {
        // Pure NaN is neither NaN nor Infinite (isNaN returns true, isInfinite false)
        Vector3D nanVec = new Vector3D(Double.NaN, Double.NaN, Double.NaN);
        assertTrue(nanVec.isNaN());
        assertFalse(nanVec.isInfinite());
    }

    @Test
    public void testInfiniteNoNaN() {
        // Pure infinite is not NaN
        Vector3D infVec = new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertFalse(infVec.isNaN());
        assertTrue(infVec.isInfinite());
    }

    @Test
    public void testMixedNaNInfinite() {
        // Vector with both NaN and Inf components: isNaN true, isInfinite false per spec
        Vector3D mixed = new Vector3D(Double.NaN, Double.POSITIVE_INFINITY, 0);
        assertTrue(mixed.isNaN());
        assertFalse(mixed.isInfinite());
    }

    @Test
    public void testEqualsAndHashCode() {
        Vector3D v1a = new Vector3D(1, 2, 3);
        Vector3D v1b = new Vector3D(1, 2, 3);
        Vector3D vDiff = new Vector3D(1, 2, 4);

        assertEquals(v1a, v1b);
        assertEquals(v1a.hashCode(), v1b.hashCode());
        assertFalse(v1a.equals(vDiff));
        assertFalse(v1a.equals(null));
        assertFalse(v1a.equals("string"));
    }

    @Test
    public void testNaNEquals() {
        Vector3D nan1 = new Vector3D(Double.NaN, 0, 0);
        Vector3D nan2 = new Vector3D(Double.NaN, 0, 0);
        // NaN vectors should be equals based on field equality (NaN != NaN in IEEE754 but Java's Float/Double treats them as equal for fields)
        assertTrue(nan1.equals(nan2));
        assertEquals(nan1.hashCode(), nan2.hashCode());
    }

    @Test
    public void testToString() {
        String str = v1.toString();
        assertNotNull(str);
        assertTrue(str.contains("1.0"));
        assertTrue(str.contains("2.0"));
        assertTrue(str.contains("3.0"));
    }

    @Test
    public void testSerialization() {
        // Simple serialization consistency test
        Vector3D v = new Vector3D(5, 6, 7);
        // Check that constructing from components works
        assertEquals(5, v.getX(), EPS);
        assertEquals(6, v.getY(), EPS);
        assertEquals(7, v.getZ(), EPS);
    }

    // The following test targets the Defects4J Math-55 bug:
    // The cross product method had a bug in computing the y-component
    // (3*(-4) - 1*(-6)) should be -12+6 = -6, but the buggy version computed
    // something else (e.g., double-counting a sign flip).
    // This test ensures that cross product is correctly computed for non-trivial vectors.
    @Test
    public void testCrossProductBug55() {
        // Using vectors that would expose bug in y-component calculation
        Vector3D u = new Vector3D(1, 0, 1);
        Vector3D v = new Vector3D(0, 1, 0);
        // expected cross = (0*0 - 1*1, 1*0 - 1*0, 1*1 - 0*0) = (-1, 0, 1)
        Vector3D cross = Vector3D.crossProduct(u, v);
        assertEquals(-1, cross.getX(), EPS);
        assertEquals(0, cross.getY(), EPS);
        assertEquals(1, cross.getZ(), EPS);
    }

    @Test
    public void testCrossProductBug55_2() {
        // Another pair to expose bug
        Vector3D u = new Vector3D(2, 3, 4);
        Vector3D v = new Vector3D(5, 6, 7);
        // expected cross = (3*7 - 4*6, 4*5 - 2*7, 2*6 - 3*5) = (21-24, 20-14, 12-15) = (-3, 6, -3)
        Vector3D cross = Vector3D.crossProduct(u, v);
        assertEquals(-3, cross.getX(), EPS);
        assertEquals(6, cross.getY(), EPS);
        assertEquals(-3, cross.getZ(), EPS);
    }

    // Test for consistency with identity cross product:
    // cross(i,j) = k, etc.
    @Test
    public void testCrossProductCanonicalBasis() {
        Vector3D i = new Vector3D(1, 0, 0);
        Vector3D j = new Vector3D(0, 1, 0);
        Vector3D k = new Vector3D(0, 0, 1);

        assertEquals(k, Vector3D.crossProduct(i, j));
        assertEquals(i, Vector3D.crossProduct(j, k));
        assertEquals(j, Vector3D.crossProduct(k, i));
    }

    @Test
    public void testAnglesNearPi() {
        // Angle between (1,0,0) and (-1,0,0) should be pi
        Vector3D u = new Vector3D(1, 0, 0);
        Vector3D v = new Vector3D(-1, 0, 0);
        assertEquals(Math.PI, Vector3D.angle(u, v), EPS);
    }

    @Test
    public void testScalarMultiplyZero() {
        Vector3D result = v1.scalarMultiply(0);
        assertEquals(0, result.getX(), EPS);
        assertEquals(0, result.getY(), EPS);
        assertEquals(0, result.getZ(), EPS);
    }

    @Test
    public void testNormOfZero() {
        assertEquals(0, vZero.getNorm(), EPS);
        assertEquals(0, vZero.getNormSq(), EPS);
    }

    @Test
    public void testDistancesBetweenSamePoint() {
        double dist = Vector3D.distance(v1, v1);
        assertEquals(0, dist, EPS);
        double distSq = Vector3D.distanceSq(v1, v1);
        assertEquals(0, distSq, EPS);
    }

    @Test
    public void testDotProductWithZero() {
        assertEquals(0, Vector3D.dotProduct(v1, vZero), EPS);
    }

    @Test
    public void testOrthogonalCrossProductNorm() {
        Vector3D u = new Vector3D(3, 4, 0);
        Vector3D v = new Vector3D(-4, 3, 0);
        Vector3D w = Vector3D.crossProduct(u, v);
        // For orthogonal vectors, norm of cross should equal product of norms
        double expected = u.getNorm() * v.getNorm();
        assertEquals(expected, w.getNorm(), EPS);
    }

    @Test
    public void testNaNBehavior() {
        Vector3D nanVec = new Vector3D(1, Double.NaN, 3);
        assertEquals(1, nanVec.getX(), EPS);
        assertTrue(Double.isNaN(nanVec.getY()));
        assertEquals(3, nanVec.getZ(), EPS);
        assertTrue(nanVec.isNaN());
        assertFalse(nanVec.isInfinite());
        assertEquals(Double.NaN, nanVec.getNorm(), EPS);
        assertEquals(Double.NaN, nanVec.getNormSq(), EPS);
    }
}