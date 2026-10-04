package org.jfree.chart.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.junit.Test;

public class ShapeUtilitiesTest {

    @Test
    public void testCloneNull() {
        assertNull(ShapeUtilities.clone(null));
    }

    @Test
    public void testCloneShape() {
        Rectangle2D r = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape cloned = ShapeUtilities.clone(r);
        assertNotNull(cloned);
        assertTrue(cloned instanceof Rectangle2D);
        assertEquals(r, cloned);
    }

    @Test
    public void testEqualNulls() {
        assertTrue(ShapeUtilities.equal(null, null));
    }

    @Test
    public void testEqualFirstNull() {
        assertFalse(ShapeUtilities.equal(null, new Rectangle2D.Double()));
    }

    @Test
    public void testEqualSecondNull() {
        assertFalse(ShapeUtilities.equal(new Rectangle2D.Double(), null));
    }

    @Test
    public void testEqualLines() {
        Line2D l1 = new Line2D.Double(0, 0, 10, 10);
        Line2D l2 = new Line2D.Double(0, 0, 10, 10);
        Line2D l3 = new Line2D.Double(0, 0, 5, 5);

        assertTrue(ShapeUtilities.equal(l1, l2));
        assertFalse(ShapeUtilities.equal(l1, l3));
        assertFalse(ShapeUtilities.equal(l1, null));
    }

    @Test
    public void testEqualRectangles() {
        Rectangle2D r1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D r2 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D r3 = new Rectangle2D.Double(0, 0, 5, 5);

        assertTrue(ShapeUtilities.equal(r1, r2));
        assertFalse(ShapeUtilities.equal(r1, r3));
    }

    @Test
    public void testEqualEllipses() {
        Ellipse2D e1 = new Ellipse2D.Double(0, 0, 10, 10);
        Ellipse2D e2 = new Ellipse2D.Double(0, 0, 10, 10);
        Ellipse2D e3 = new Ellipse2D.Double(0, 0, 5, 5);

        assertTrue(ShapeUtilities.equal(e1, e2));
        assertFalse(ShapeUtilities.equal(e1, e3));
    }

    @Test
    public void testEqualArcs() {
        Arc2D a1 = new Arc2D.Double(0, 0, 10, 10, 0, 90, Arc2D.OPEN);
        Arc2D a2 = new Arc2D.Double(0, 0, 10, 10, 0, 90, Arc2D.OPEN);
        Arc2D a3 = new Arc2D.Double(0, 0, 10, 10, 0, 45, Arc2D.OPEN);
        Arc2D a4 = new Arc2D.Double(0, 0, 10, 10, 0, 90, Arc2D.CHORD);

        assertTrue(ShapeUtilities.equal(a1, a2));
        assertFalse(ShapeUtilities.equal(a1, a3));
        assertFalse(ShapeUtilities.equal(a1, a4));
    }

    @Test
    public void testEqualPolygons() {
        Polygon p1 = new Polygon(new int[] {0, 1, 2}, new int[] {0, 1, 2}, 3);
        Polygon p2 = new Polygon(new int[] {0, 1, 2}, new int[] {0, 1, 2}, 3);
        Polygon p3 = new Polygon(new int[] {0, 1, 3}, new int[] {0, 1, 2}, 3);
        Polygon p4 = new Polygon(new int[] {0, 1}, new int[] {0, 1}, 2);

        assertTrue(ShapeUtilities.equal(p1, p2));
        assertFalse(ShapeUtilities.equal(p1, p3));
        assertFalse(ShapeUtilities.equal(p1, p4));
        assertFalse(ShapeUtilities.equal(p1, new Rectangle2D.Double()));
    }

    @Test
    public void testEqualGeneralPaths() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        GeneralPath gp3 = new GeneralPath();
        gp3.moveTo(0, 0);
        gp3.lineTo(2, 2);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
        assertFalse(ShapeUtilities.equal(gp1, gp3));
    }

    @Test
    public void testEqualGenericShapes() {
        Shape s1 = new Rectangle(0, 0, 10, 10);
        Shape s2 = new Rectangle(0, 0, 10, 10);
        Shape s3 = new Rectangle(0, 0, 5, 5);

        assertTrue(ShapeUtilities.equal(s1, s2));
        assertFalse(ShapeUtilities.equal(s1, s3));
    }

    @Test
    public void testCreatePoint() {
        Rectangle2D r = new Rectangle2D.Double(0, 0, 10, 20);
        Point2D p = ShapeUtilities.calculateAlignedPoint(r, 10.0, 10.0, RectangleAnchor.CENTER);
        assertNotNull(p);
    }

    @Test
    public void testCreateDiagonalCross() {
        Shape s = ShapeUtilities.createDiagonalCross(5.0f, 1.0f);
        assertNotNull(s);
    }

    @Test
    public void testCreateRegularCross() {
        Shape s = ShapeUtilities.createRegularCross(5.0f, 1.0f);
        assertNotNull(s);
    }

    @Test
    public void testCreateLine() {
        Line2D l = ShapeUtilities.createLine(new Rectangle2D.Double(0, 0, 10, 10), RectangleAnchor.TOP_LEFT, RectangleAnchor.BOTTOM_RIGHT);
        assertNotNull(l);
    }

    @Test
    public void testRotateShape() {
        Shape r = new Rectangle2D.Double(0, 0, 10, 10);
        Shape rotated = ShapeUtilities.rotateShape(r, Math.PI, 5.0f, 5.0f);
        assertNotNull(rotated);
        
        assertNull(ShapeUtilities.rotateShape(null, Math.PI, 5.0f, 5.0f));
    }

    @Test
    public void testDrawRotatedShape() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Shape r = new Rectangle2D.Double(0, 0, 10, 10);
        ShapeUtilities.drawRotatedShape(g2, r, Math.PI, 5.0f, 5.0f);
        g2.dispose();
    }

    @Test
    public void testTranslateShape() {
        Shape r = new Rectangle2D.Double(0, 0, 10, 10);
        Shape translated = ShapeUtilities.createTranslatedShape(r, 5.0, 5.0);
        assertNotNull(translated);

        assertNull(ShapeUtilities.createTranslatedShape(null, 5.0, 5.0));

        Shape translatedAnchor = ShapeUtilities.createTranslatedShape(r, RectangleAnchor.CENTER, 10.0, 10.0);
        assertNotNull(translatedAnchor);
    }

    @Test
    public void testGetPointInRectangle() {
        Rectangle2D r = new Rectangle2D.Double(0, 0, 10, 10);
        Point2D p = ShapeUtilities.getPointInRectangle(5.0, 5.0, r);
        assertEquals(5.0, p.getX(), 0.001);
        assertEquals(5.0, p.getY(), 0.001);

        Point2D pLeft = ShapeUtilities.getPointInRectangle(-5.0, 5.0, r);
        assertEquals(0.0, pLeft.getX(), 0.001);

        Point2D pRight = ShapeUtilities.getPointInRectangle(15.0, 5.0, r);
        assertEquals(10.0, pRight.getX(), 0.001);

        Point2D pTop = ShapeUtilities.getPointInRectangle(5.0, -5.0, r);
        assertEquals(0.0, pTop.getY(), 0.001);

        Point2D pBottom = ShapeUtilities.getPointInRectangle(5.0, 15.0, r);
        assertEquals(10.0, pBottom.getY(), 0.001);
    }
}