package org.apache.commons.math3.geometry.euclidean.twod;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;

/**
 * Comprehensive JUnit 4 test suite for PolygonsSet.
 * Targets maximum line/branch coverage and fault detection,
 * specifically the bug where getVertices() returns null elements.
 */
public class PolygonsSetTest {

    private Vector2D v1, v2, v3, v4;
    private Polygon triangle, square;
    private PolygonsSet emptySet, singleSet, multiSet;

    @Before
    public void setUp() {
        // Basic vertices
        v1 = new Vector2D(0, 0);
        v2 = new Vector2D(1, 0);
        v3 = new Vector2D(0, 1);
        v4 = new Vector2D(1, 1);

        // Polygons
        triangle = new Polygon(v1, v2, v3);
        square  = new Polygon(v1, v2, v4, v3);

        // PolygonsSets
        emptySet  = new PolygonsSet();
        singleSet = new PolygonsSet(triangle);
        multiSet  = new PolygonsSet(triangle, square);
    }

    // ========== getVertices() tests ==========

    @Test
    public void testEmptySetVertices() {
        Vector2D[] vertices = emptySet.getVertices();
        assertNotNull("Vertices array must not be null", vertices);
        assertEquals("Empty set should have 0 vertices", 0, vertices.length);
    }

    @Test
    public void testSinglePolygonVertices() {
        Vector2D[] vertices = singleSet.getVertices();
        assertNotNull("Vertices array must not be null", vertices);
        assertEquals("Triangle should have 3 vertices", 3, vertices.length);
        for (Vector2D v : vertices) {
            assertNotNull("Vertex must not be null", v);
        }
    }

    @Test
    public void testMultiplePolygonsVertices() {
        Vector2D[] vertices = multiSet.getVertices();
        assertNotNull("Vertices array must not be null", vertices);
        // triangle (3) + square (4) = 7
        assertEquals("Two polygons should have 7 vertices", 7, vertices.length);
        for (Vector2D v : vertices) {
            assertNotNull("Vertex must not be null", v);
        }
    }

    @Test
    public void testGetVerticesNoNulls() {
        // Primary bug regression: getVertices() must never contain null elements
        Vector2D[] vertices;

        vertices = singleSet.getVertices();
        for (Vector2D v : vertices) {
            assertNotNull("Vertex should not be null", v);
        }

        vertices = multiSet.getVertices();
        for (Vector2D v : vertices) {
            assertNotNull("Vertex should not be null", v);
        }
    }

    // ========== getBarycenter() tests ==========

    @Test
    public void testGetBarycenter() {
        Vector2D barycenter = singleSet.getBarycenter();
        assertNotNull("Barycenter must not be null", barycenter);
        // For triangle (0,0), (1,0), (0,1) barycenter = (1/3, 1/3)
        assertEquals("Barycenter x", 1.0 / 3.0, barycenter.getX(), 1e-10);
        assertEquals("Barycenter y", 1.0 / 3.0, barycenter.getY(), 1e-10);
    }

    // ========== getSize() tests ==========

    @Test
    public void testGetSize() {
        assertEquals("Empty set size", 0, emptySet.getSize());
        assertEquals("Single polygon size", 1, singleSet.getSize());
        assertEquals("Multiple polygons size", 2, multiSet.getSize());
    }

    // ========== getSegments() tests ==========

    @Test
    public void testGetSegments() {
        Segment[] segments = singleSet.getSegments();
        assertNotNull("Segments array must not be null", segments);
        // Triangle has 3 edges
        assertEquals("Triangle should have 3 segments", 3, segments.length);
        for (Segment s : segments) {
            assertNotNull("Segment must not be null", s);
        }
    }

    // ========== Edge cases ==========

    @Test(expected = IllegalArgumentException.class)
    public void testDegeneratePolygon() {
        // Collinear points should be rejected
        new Polygon(v1, v2, new Vector2D(2, 0));
    }

    @Test
    public void testLargeCoordinates() {
        // Stress test with large values to detect overflow issues
        Vector2D big1 = new Vector2D(1e10, 1e10);
        Vector2D big2 = new Vector2D(1e10 + 1, 1e10);
        Vector2D big3 = new Vector2D(1e10, 1e10 + 1);
        Polygon bigTriangle = new Polygon(big1, big2, big3);
        PolygonsSet bigSet = new PolygonsSet(bigTriangle);
        Vector2D[] vertices = bigSet.getVertices();
        assertEquals(3, vertices.length);
        for (Vector2D v : vertices) {
            assertNotNull(v);
        }
    }

    @Test
    public void testNegativeCoordinates() {
        // Ensure negative coordinates are handled correctly
        Vector2D n1 = new Vector2D(-1, -1);
        Vector2D n2 = new Vector2D(-1, 0);
        Vector2D n3 = new Vector2D(0, -1);
        Polygon negTriangle = new Polygon(n1, n2, n3);
        PolygonsSet negSet = new PolygonsSet(negTriangle);
        Vector2D[] vertices = negSet.getVertices();
        assertEquals(3, vertices.length);
        for (Vector2D v : vertices) {
            assertNotNull(v);
        }
    }

    @Test
    public void testPolygonWithHole() {
        // If PolygonsSet supports holes, this test validates that case.
        // Placeholder – actual implementation depends on API.
        // For now, we simply verify that a set with a hole does not produce null vertices.
        // Example: outer square, inner triangle (hole)
        Polygon outer = new Polygon(v1, v2, v4, v3);
        Polygon inner = new Polygon(
            new Vector2D(0.25, 0.25),
            new Vector2D(0.75, 0.25),
            new Vector2D(0.5, 0.75)
        );
        PolygonsSet withHole = new PolygonsSet(outer, inner);
        Vector2D[] vertices = withHole.getVertices();
        assertNotNull(vertices);
        // Outer (4) + inner (3) = 7
        assertEquals(7, vertices.length);
        for (Vector2D v : vertices) {
            assertNotNull(v);
        }
    }
}