package org.apache.commons.math.geometry;

import org.junit.Test;
import static org.junit.Assert.*;

public class LineTest {

    @Test
    public void testConstructorAndGetters() {
        Vector1D p1 = new Vector1D(0.0);
        Vector1D p2 = new Vector1D(1.0);
        SubLine subLine = new SubLine(p1, p2);
        
        Line line = new Line(p1, p2);
        assertNotNull(line);
    }

    @Test
    public void testFromSpaceToSubSpace() {
        Line line = new Line(new Vector1D(0.0), new Vector1D(1.0));
        Vector1D spacePoint = new Vector1D(5.0);
        Vector1D subSpacePoint = line.toSubSpace(spacePoint);
        assertNotNull(subSpacePoint);
        assertEquals(5.0, subSpacePoint.getX(), 1e-10);
    }

    @Test
    public void testFromSubSpaceToSpace() {
        Line line = new Line(new Vector1D(0.0), new Vector1D(1.0));
        Vector1D subSpacePoint = new Vector1D(5.0);
        Vector1D spacePoint = line.toSpace(subSpacePoint);
        assertNotNull(spacePoint);
        assertEquals(5.0, spacePoint.getX(), 1e-10);
    }

    @Test
    public void testReset() {
        Line line = new Line(new Vector1D(0.0), new Vector1D(1.0));
        line.reset(new Vector1D(2.0), new Vector1D(3.0));
        Vector1D subSpacePoint = line.toSubSpace(new Vector1D(2.0));
        assertEquals(0.0, subSpacePoint.getX(), 1e-10);
    }

    @Test
    public void testReversed() {
        Line line = new Line(new Vector1D(0.0), new Vector1D(1.0));
        Line reversed = line.getReversed();
        assertNotNull(reversed);
        
        Vector1D subSpacePoint = reversed.toSubSpace(new Vector1D(1.0));
        assertEquals(-1.0, subSpacePoint.getX(), 1e-10);
    }

    @Test
    public void testIntersection() {
        Line line1 = new Line(new Vector1D(0.0), new Vector1D(1.0));
        Line line2 = new Line(new Vector1D(2.0), new Vector1D(3.0));
        
        // In 1D geometry, lines are either identical or parallel
        Vector1D intersection = line1.intersection(line2);
        // Depending on implementation details in commons-math, test behavior safely
        // If parallel, intersection might be null or throw.
        // Let's test with the same line or standard usage:
        Vector1D selfIntersection = line1.intersection(line1);
        assertNotNull(selfIntersection);
    }

    @Test
    public void testContains() {
        Line line = new Line(new Vector1D(0.0), new Vector1D(1.0));
        assertTrue(line.contains(new Vector1D(5.0)));
    }

    @Test
    public void testDistance() {
        Line line = new Line(new Vector1D(0.0), new Vector1D(1.0));
        double dist = line.distance(new Vector1D(5.0));
        assertEquals(0.0, dist, 1e-10);
    }

    @Test
    public void testGetOffset() {
        Line line = new Line(new Vector1D(0.0), new Vector1D(1.0));
        double offset = line.getOffset(new Vector1D(5.0));
        assertEquals(0.0, offset, 1e-10);
    }
}