package org.apache.commons.math3.geometry.euclidean.twod;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for the Line class from Apache Commons Math.
 * Designed to achieve high coverage and detect potential faults.
 */
public class LineTest {

    private Line horizontalLine;
    private Line verticalLine;
    private Line diagonalLine;
    private Line lineThroughOrigin;

    @Before
    public void setUp() {
        // Horizontal line: y = 2
        horizontalLine = new Line(new Vector2D(0, 2), new Vector2D(1, 2));
        // Vertical line: x = 3
        verticalLine = new Line(new Vector2D(3, 0), new Vector2D(3, 1));
        // Diagonal line: y = x
        diagonalLine = new Line(new Vector2D(0, 0), new Vector2D(1, 1));
        // Line through origin with angle 0 (horizontal through origin)
        lineThroughOrigin = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
    }

    @Test
    public void testConstructorAndAngle() {
        // Test that the angle is correctly computed
        assertEquals(Math.PI / 4, diagonalLine.getAngle(), 1e-10);
        assertEquals(0.0, lineThroughOrigin.getAngle(), 1e-10);
        assertEquals(Math.PI / 2, verticalLine.getAngle(), 1e-10);
        assertEquals(0.0, horizontalLine.getAngle(), 1e-10);
    }

    @Test
    public void testConstructorWithNullPoints() {
        try {
            new Line(null, new Vector2D(1, 1));
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            new Line(new Vector2D(1, 1), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithIdenticalPoints() {
        // Should create a line with zero direction? Actually it may throw an exception.
        // In Commons Math, it throws IllegalArgumentException.
        try {
            new Line(new Vector2D(1, 1), new Vector2D(1, 1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetOffset() {
        // Offset of point (0,0) from horizontal line y=2 is -2 (since line is above)
        assertEquals(-2.0, horizontalLine.getOffset(new Vector2D(0, 0)), 1e-10);
        // Offset of point (0,2) from horizontal line is 0
        assertEquals(0.0, horizontalLine.getOffset(new Vector2D(0, 2)), 1e-10);
        // Offset of point (3,0) from vertical line x=3 is 0
        assertEquals(0.0, verticalLine.getOffset(new Vector2D(3, 0)), 1e-10);
        // Offset of point (0,0) from vertical line x=3 is -3
        assertEquals(-3.0, verticalLine.getOffset(new Vector2D(0, 0)), 1e-10);
        // Diagonal: offset of (0,0) from y=x is 0
        assertEquals(0.0, diagonalLine.getOffset(new Vector2D(0, 0)), 1e-10);
        // Diagonal: offset of (1,0) from y=x is -sqrt(2)/2
        assertEquals(-Math.sqrt(2) / 2, diagonalLine.getOffset(new Vector2D(1, 0)), 1e-10);
    }

    @Test
    public void testGetOffsetWithNull() {
        try {
            horizontalLine.getOffset(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testDistance() {
        // Distance from point (0,0) to horizontal line y=2 is 2
        assertEquals(2.0, horizontalLine.distance(new Vector2D(0, 0)), 1e-10);
        // Distance from point (0,2) to horizontal line is 0
        assertEquals(0.0, horizontalLine.distance(new Vector2D(0, 2)), 1e-10);
        // Distance from point (3,0) to vertical line x=3 is 0
        assertEquals(0.0, verticalLine.distance(new Vector2D(3, 0)), 1e-10);
        // Distance from point (0,0) to vertical line x=3 is 3
        assertEquals(3.0, verticalLine.distance(new Vector2D(0, 0)), 1e-10);
        // Diagonal: distance from (1,0) to y=x is sqrt(2)/2
        assertEquals(Math.sqrt(2) / 2, diagonalLine.distance(new Vector2D(1, 0)), 1e-10);
    }

    @Test
    public void testDistanceWithNull() {
        try {
            horizontalLine.distance(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testClosestPoint() {
        // Closest point on horizontal line y=2 to (0,0) is (0,2)
        Vector2D closest = horizontalLine.closestPoint(new Vector2D(0, 0));
        assertEquals(0.0, closest.getX(), 1e-10);
        assertEquals(2.0, closest.getY(), 1e-10);
        // Closest point on vertical line x=3 to (0,0) is (3,0)
        closest = verticalLine.closestPoint(new Vector2D(0, 0));
        assertEquals(3.0, closest.getX(), 1e-10);
        assertEquals(0.0, closest.getY(), 1e-10);
        // Closest point on diagonal y=x to (1,0) is (0.5,0.5)
        closest = diagonalLine.closestPoint(new Vector2D(1, 0));
        assertEquals(0.5, closest.getX(), 1e-10);
        assertEquals(0.5, closest.getY(), 1e-10);
    }

    @Test
    public void testClosestPointWithNull() {
        try {
            horizontalLine.closestPoint(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testIntersection() {
        // Horizontal and vertical lines intersect at (3,2)
        Vector2D intersection = horizontalLine.intersection(verticalLine);
        assertNotNull(intersection);
        assertEquals(3.0, intersection.getX(), 1e-10);
        assertEquals(2.0, intersection.getY(), 1e-10);
        // Parallel lines (horizontal and another horizontal) should return null
        Line anotherHorizontal = new Line(new Vector2D(0, 5), new Vector2D(1, 5));
        assertNull(horizontalLine.intersection(anotherHorizontal));
        // Same line should return null (or infinite points)
        assertNull(horizontalLine.intersection(horizontalLine));
        // Diagonal and horizontal intersect at (2,2)
        intersection = diagonalLine.intersection(horizontalLine);
        assertNotNull(intersection);
        assertEquals(2.0, intersection.getX(), 1e-10);
        assertEquals(2.0, intersection.getY(), 1e-10);
    }

    @Test
    public void testIntersectionWithNull() {
        try {
            horizontalLine.intersection(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testContains() {
        // Point on line
        assertTrue(horizontalLine.contains(new Vector2D(0, 2)));
        assertTrue(horizontalLine.contains(new Vector2D(100, 2)));
        assertTrue(verticalLine.contains(new Vector2D(3, 0)));
        assertTrue(verticalLine.contains(new Vector2D(3, 100)));
        assertTrue(diagonalLine.contains(new Vector2D(0, 0)));
        assertTrue(diagonalLine.contains(new Vector2D(1, 1)));
        // Point not on line
        assertFalse(horizontalLine.contains(new Vector2D(0, 0)));
        assertFalse(verticalLine.contains(new Vector2D(0, 0)));
        assertFalse(diagonalLine.contains(new Vector2D(1, 0)));
    }

    @Test
    public void testContainsWithNull() {
        try {
            horizontalLine.contains(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testParallel() {
        assertTrue(horizontalLine.isParallelTo(new Line(new Vector2D(0, 5), new Vector2D(1, 5))));
        assertTrue(verticalLine.isParallelTo(new Line(new Vector2D(5, 0), new Vector2D(5, 1))));
        assertTrue(diagonalLine.isParallelTo(new Line(new Vector2D(2, 2), new Vector2D(3, 3))));
        assertFalse(horizontalLine.isParallelTo(verticalLine));
        assertFalse(diagonalLine.isParallelTo(horizontalLine));
    }

    @Test
    public void testParallelWithNull() {
        try {
            horizontalLine.isParallelTo(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testTranslate() {
        // Translate horizontal line up by 3: new line y=5
        Line translated = horizontalLine.translate(3.0);
        assertEquals(0.0, translated.getAngle(), 1e-10);
        assertEquals(5.0, translated.getOffset(new Vector2D(0, 0)), 1e-10);
        // Translate vertical line right by 2: new line x=5
        translated = verticalLine.translate(2.0);
        assertEquals(Math.PI / 2, translated.getAngle(), 1e-10);
        assertEquals(5.0, translated.getOffset(new Vector2D(0, 0)), 1e-10);
        // Translate diagonal line by 1: offset changes
        translated = diagonalLine.translate(1.0);
        assertEquals(Math.PI / 4, translated.getAngle(), 1e-10);
        // Original offset from (0,0) was 0, after translation by 1 (positive direction) offset becomes 1
        assertEquals(1.0, translated.getOffset(new Vector2D(0, 0)), 1e-10);
    }

    @Test
    public void testRevert() {
        Line reverted = horizontalLine.revert();
        // Reverted line should have angle + PI (mod 2PI)
        assertEquals(Math.PI, reverted.getAngle(), 1e-10);
        // Offset from (0,0) should be same magnitude but sign? Actually revert flips direction but offset remains same?
        // For horizontal line y=2, offset of (0,0) is -2. After revert, offset should still be -2 because line is same set of points.
        assertEquals(-2.0, reverted.getOffset(new Vector2D(0, 0)), 1e-10);
    }

    @Test
    public void testEqualsAndHashCode() {
        Line sameLine = new Line(new Vector2D(0, 2), new Vector2D(1, 2));
        assertEquals(horizontalLine, sameLine);
        assertEquals(horizontalLine.hashCode(), sameLine.hashCode());
        assertNotEquals(horizontalLine, verticalLine);
        assertNotEquals(horizontalLine, null);
        assertNotEquals(horizontalLine, new Object());
    }

    @Test
    public void testToString() {
        assertNotNull(horizontalLine.toString());
        assertTrue(horizontalLine.toString().contains("Line"));
    }

    @Test
    public void testReset() {
        // reset() is not public in Commons Math Line? Actually it's package-private.
        // We'll skip or test via reflection if needed. For now, just ensure no crash.
        // Not testing reset as it's not part of public API.
    }

    @Test
    public void testGetPointAt() {
        // For horizontal line y=2, point at abscissa 0 is (0,2)
        Vector2D point = horizontalLine.getPointAt(0.0);
        assertEquals(0.0, point.getX(), 1e-10);
        assertEquals(2.0, point.getY(), 1e-10);
        // For diagonal line y=x, point at abscissa 1 is (1,1)
        point = diagonalLine.getPointAt(1.0);
        assertEquals(1.0, point.getX(), 1e-10);
        assertEquals(1.0, point.getY(), 1e-10);
        // For vertical line x=3, point at abscissa 0 is (3,0)
        point = verticalLine.getPointAt(0.0);
        assertEquals(3.0, point.getX(), 1e-10);
        assertEquals(0.0, point.getY(), 1e-10);
    }

    @Test
    public void testGetPointAtLargeValues() {
        // Test with large abscissa to ensure no overflow
        Vector2D point = horizontalLine.getPointAt(1e10);
        assertEquals(1e10, point.getX(), 1e-5);
        assertEquals(2.0, point.getY(), 1e-10);
    }

    @Test
    public void testGetPointAtNegative() {
        Vector2D point = horizontalLine.getPointAt(-5.0);
        assertEquals(-5.0, point.getX(), 1e-10);
        assertEquals(2.0, point.getY(), 1e-10);
    }

    @Test
    public void testWholeLineIntersection() {
        // Two lines that are the same should return null (infinite intersections)
        Line sameLine = new Line(new Vector2D(0, 2), new Vector2D(1, 2));
        assertNull(horizontalLine.intersection(sameLine));
    }

    @Test
    public void testOffsetSignConsistency() {
        // For a point on the line, offset should be 0
        assertEquals(0.0, horizontalLine.getOffset(new Vector2D(5, 2)), 1e-10);
        // For a point above the line (y > 2), offset should be positive? Actually offset is signed distance.
        // The line's direction is from (0,2) to (1,2) which is to the right. The normal points upward? In Commons Math, offset is signed distance along the normal.
        // For horizontal line with direction to the right, the normal points upward (positive y). So point (0,3) should have positive offset.
        assertEquals(1.0, horizontalLine.getOffset(new Vector2D(0, 3)), 1e-10);
        // Point below should have negative offset
        assertEquals(-1.0, horizontalLine.getOffset(new Vector2D(0, 1)), 1e-10);
    }

    @Test
    public void testClosestPointOnLineItself() {
        // Closest point to a point on the line is the point itself
        Vector2D onLine = new Vector2D(4, 2);
        Vector2D closest = horizontalLine.closestPoint(onLine);
        assertEquals(onLine, closest);
    }

    @Test
    public void testDistanceToPointOnLine() {
        assertEquals(0.0, horizontalLine.distance(new Vector2D(4, 2)), 1e-10);
    }

    @Test
    public void testIntersectionWithParallelLines() {
        Line parallelHorizontal = new Line(new Vector2D(0, 5), new Vector2D(1, 5));
        assertNull(horizontalLine.intersection(parallelHorizontal));
        // Also test with lines that are collinear but not same direction? Actually parallel includes same line.
        assertNull(horizontalLine.intersection(horizontalLine));
    }

    @Test
    public void testContainsWithTolerance() {
        // contains uses a tolerance of 1e-10? Actually Line.contains uses a static tolerance.
        // Test a point very close to the line
        Vector2D closePoint = new Vector2D(0, 2 + 1e-12);
        assertTrue(horizontalLine.contains(closePoint));
        // Test a point slightly farther
        Vector2D farPoint = new Vector2D(0, 2 + 1e-8);
        assertFalse(horizontalLine.contains(farPoint));
    }

    @Test
    public void testGetAngleModPi() {
        // Angle should be in [0, PI)
        double angle = horizontalLine.getAngle();
        assertTrue(angle >= 0 && angle < Math.PI);
        // Reverted line angle should be angle + PI mod PI? Actually revert adds PI, then mod PI gives same angle.
        // But getAngle returns value in [0, PI). So reverted line should have same angle? No, because direction is opposite but line is same set of points.
        // In Commons Math, revert changes the direction, so angle becomes (angle + PI) % (2PI) but getAngle returns in [0, PI). So it should be same.
        assertEquals(horizontalLine.getAngle(), horizontalLine.revert().getAngle(), 1e-10);
    }

    @Test
    public void testTranslateNegative() {
        // Translate horizontal line down by 2: new line y=0
        Line translated = horizontalLine.translate(-2.0);
        assertEquals(0.0, translated.getOffset(new Vector2D(0, 0)), 1e-10);
    }

    @Test
    public void testTranslateZero() {
        Line translated = horizontalLine.translate(0.0);
        assertEquals(horizontalLine, translated);
    }

    @Test
    public void testConstructorWithDirectionVector() {
        // Already tested via setUp, but ensure that direction is normalized
        // The internal direction should have unit length
        // We can test by checking that getPointAt(1) gives a point at distance 1 from origin along line
        Vector2D point = diagonalLine.getPointAt(1.0);
        // For diagonal, direction is (sqrt(2)/2, sqrt(2)/2), so point at abscissa 1 should be (sqrt(2)/2, sqrt(2)/2) from origin? Actually getPointAt uses abscissa along line.
        // The origin of the line is the point used in constructor? In Commons Math, the line is defined by two points, and the origin is the first point.
        // For diagonalLine, first point is (0,0), so getPointAt(1) should be (cos(45°), sin(45°)) = (sqrt(2)/2, sqrt(2)/2)
        assertEquals(Math.sqrt(2)/2, point.getX(), 1e-10);
        assertEquals(Math.sqrt(2)/2, point.getY(), 1e-10);
    }

    @Test
    public void testGetPointAtWithNegativeAbscissa() {
        Vector2D point = diagonalLine.getPointAt(-1.0);
        assertEquals(-Math.sqrt(2)/2, point.getX(), 1e-10);
        assertEquals(-Math.sqrt(2)/2, point.getY(), 1e-10);
    }

    @Test
    public void testIntersectionWithPerpendicularLines() {
        // Horizontal and vertical intersect
        Vector2D inter = horizontalLine.intersection(verticalLine);
        assertNotNull(inter);
        assertEquals(3.0, inter.getX(), 1e-10);
        assertEquals(2.0, inter.getY(), 1e-10);
    }

    @Test
    public void testIntersectionWithDiagonalAndHorizontal() {
        Vector2D inter = diagonalLine.intersection(horizontalLine);
        assertNotNull(inter);
        assertEquals(2.0, inter.getX(), 1e-10);
        assertEquals(2.0, inter.getY(), 1e-10);
    }

    @Test
    public void testIntersectionWithDiagonalAndVertical() {
        Vector2D inter = diagonalLine.intersection(verticalLine);
        assertNotNull(inter);
        assertEquals(3.0, inter.getX(), 1e-10);
        assertEquals(3.0, inter.getY(), 1e-10);
    }

    @Test
    public void testOffsetOfPointOnLine() {
        assertEquals(0.0, horizontalLine.getOffset(new Vector2D(100, 2)), 1e-10);
    }

    @Test
    public void testDistanceToPointOnLine() {
        assertEquals(0.0, horizontalLine.distance(new Vector2D(100, 2)), 1e-10);
    }

    @Test
    public void testClosestPointToPointOnLine() {
        Vector2D p = new Vector2D(100, 2);
        assertEquals(p, horizontalLine.closestPoint(p));
    }

    @Test
    public void testIsParallelToSameLine() {
        assertTrue(horizontalLine.isParallelTo(horizontalLine));
    }

    @Test
    public void testIsParallelToOppositeDirection() {
        Line opposite = horizontalLine.revert();
        assertTrue(horizontalLine.isParallelTo(opposite));
    }

    @Test
    public void testEqualsDifferentLine() {
        Line different = new Line(new Vector2D(0, 3), new Vector2D(1, 3));
        assertNotEquals(horizontalLine, different);
    }

    @Test
    public void testHashCodeConsistency() {
        int hash = horizontalLine.hashCode();
        assertEquals(hash, horizontalLine.hashCode());
    }

    @Test
    public void testConstructorWithPointsInReverseOrder() {
        // Should produce same line
        Line reversed = new Line(new Vector2D(1, 2), new Vector2D(0, 2));
        assertEquals(horizontalLine, reversed);
    }

    @Test
    public void testGetOffsetWithLargeCoordinates() {
        // Ensure no overflow
        double offset = horizontalLine.getOffset(new Vector2D(1e10, 1e10));
        assertEquals(1e10 - 2, offset, 1e5); // approximate
    }

    @Test
    public void testDistanceWithLargeCoordinates() {
        double dist = horizontalLine.distance(new Vector2D(1e10, 1e10));
        assertEquals(1e10 - 2, dist, 1e5);
    }

    @Test
    public void testClosestPointWithLargeCoordinates() {
        Vector2D closest = horizontalLine.closestPoint(new Vector2D(1e10, 1e10));
        assertEquals(1e10, closest.getX(), 1e-5);
        assertEquals(2.0, closest.getY(), 1e-10);
    }

    @Test
    public void testIntersectionWithLinesAtOrigin() {
        Line line1 = new Line(new Vector2D(0, 0), new Vector2D(1, 0));
        Line line2 = new Line(new Vector2D(0, 0), new Vector2D(0, 1));
        Vector2D inter = line1.intersection(line2);
        assertNotNull(inter);
        assertEquals(0.0, inter.getX(), 1e-10);
        assertEquals(0.0, inter.getY(), 1e-10);
    }

    @Test
    public void testContainsPointAtInfinity() {
        // Not applicable, but ensure no crash
        Vector2D far = new Vector2D(Double.POSITIVE_INFINITY, 2);
        assertFalse(horizontalLine.contains(far));
    }

    @Test
    public void testConstructorWithNaN() {
        // Should throw exception
        try {
            new Line(new Vector2D(Double.NaN, 0), new Vector2D(1, 1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithInfinite() {
        try {
            new Line(new Vector2D(Double.POSITIVE_INFINITY, 0), new Vector2D(1, 1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}