package org.jfree.chart.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ShapeUtilities (Defects4J Chart-11).
 */
public class ShapeUtilitiesTest {

    private Shape line;
    private Shape rect;
    private Shape ellipse;
    private Shape generalPath;
    private Shape arc;
    private Shape roundRect;
    private Shape quadCurve;
    private Shape cubicCurve;
    private Shape area;
    private Shape point;
    private Shape emptyPath;

    @Before
    public void setUp() {
        line = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        rect = new Rectangle2D.Double(0.0, 0.0, 10.0, 20.0);
        ellipse = new Ellipse2D.Double(5.0, 5.0, 15.0, 25.0);
        generalPath = new GeneralPath();
        generalPath.moveTo(0, 0);
        generalPath.lineTo(10, 0);
        generalPath.lineTo(10, 10);
        generalPath.closePath();
        arc = new Arc2D.Double(0.0, 0.0, 100.0, 100.0, 45.0, 90.0, Arc2D.PIE);
        roundRect = new RoundRectangle2D.Double(1.0, 2.0, 30.0, 40.0, 5.0, 5.0);
        quadCurve = new QuadCurve2D.Double(0.0, 0.0, 10.0, 20.0, 30.0, 0.0);
        cubicCurve = new CubicCurve2D.Double(0.0, 0.0, 10.0, 20.0, 20.0, 20.0, 30.0, 0.0);
        area = new Area(new Ellipse2D.Double(0, 0, 10, 10));
        point = new Point2D.Double(5.0, 5.0);
        emptyPath = new GeneralPath();
    }

    // ---------- cloneShape ----------

    @Test
    public void testCloneShapeNull() {
        assertNull("Cloning null should return null", ShapeUtilities.cloneShape(null));
    }

    @Test
    public void testCloneShapeLine() {
        Shape cloned = ShapeUtilities.cloneShape(line);
        assertNotNull("Line clone should not be null", cloned);
        assertTrue("Cloned line should be equal", ShapeUtilities.equal(line, cloned));
        assertFalse("Cloned line should not be same instance", line == cloned);
    }

    @Test
    public void testCloneShapeRectangle() {
        Shape cloned = ShapeUtilities.cloneShape(rect);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(rect, cloned));
        assertFalse(rect == cloned);
    }

    @Test
    public void testCloneShapeEllipse() {
        Shape cloned = ShapeUtilities.cloneShape(ellipse);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(ellipse, cloned));
        assertFalse(ellipse == cloned);
    }

    @Test
    public void testCloneShapeGeneralPath() {
        Shape cloned = ShapeUtilities.cloneShape(generalPath);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(generalPath, cloned));
        assertFalse(generalPath == cloned);
    }

    @Test
    public void testCloneShapeArc() {
        Shape cloned = ShapeUtilities.cloneShape(arc);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(arc, cloned));
        assertFalse(arc == cloned);
    }

    @Test
    public void testCloneShapeRoundRect() {
        Shape cloned = ShapeUtilities.cloneShape(roundRect);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(roundRect, cloned));
        assertFalse(roundRect == cloned);
    }

    @Test
    public void testCloneShapeQuadCurve() {
        Shape cloned = ShapeUtilities.cloneShape(quadCurve);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(quadCurve, cloned));
        assertFalse(quadCurve == cloned);
    }

    @Test
    public void testCloneShapeCubicCurve() {
        Shape cloned = ShapeUtilities.cloneShape(cubicCurve);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(cubicCurve, cloned));
        assertFalse(cubicCurve == cloned);
    }

    @Test
    public void testCloneShapeArea() {
        Shape cloned = ShapeUtilities.cloneShape(area);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(area, cloned));
        assertFalse(area == cloned);
    }

    @Test
    public void testCloneShapePoint() {
        Shape cloned = ShapeUtilities.cloneShape(point);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(point, cloned));
        assertFalse(point == cloned);
    }

    @Test
    public void testCloneShapeEmptyPath() {
        Shape cloned = ShapeUtilities.cloneShape(emptyPath);
        assertNotNull(cloned);
        assertTrue(ShapeUtilities.equal(emptyPath, cloned));
        assertFalse(emptyPath == cloned);
    }

    // ---------- equal ----------

    @Test
    public void testEqualBothNull() {
        assertTrue("Both null should be equal", ShapeUtilities.equal(null, null));
    }

    @Test
    public void testEqualFirstNull() {
        assertFalse("First null, second not null should be false", ShapeUtilities.equal(null, line));
    }

    @Test
    public void testEqualSecondNull() {
        assertFalse("First not null, second null should be false", ShapeUtilities.equal(line, null));
    }

    @Test
    public void testEqualSameObject() {
        assertTrue("Same object should be equal", ShapeUtilities.equal(line, line));
    }

    @Test
    public void testEqualDifferentTypes() {
        assertFalse("Different types should not be equal", ShapeUtilities.equal(line, rect));
    }

    @Test
    public void testEqualIdenticalLines() {
        Shape line2 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        assertTrue("Identical lines should be equal", ShapeUtilities.equal(line, line2));
    }

    @Test
    public void testEqualDifferentLines() {
        Shape line2 = new Line2D.Double(1.0, 2.0, 3.0, 5.0);
        assertFalse("Different lines should not be equal", ShapeUtilities.equal(line, line2));
    }

    @Test
    public void testEqualIdenticalRectangles() {
        Shape rect2 = new Rectangle2D.Double(0.0, 0.0, 10.0, 20.0);
        assertTrue(ShapeUtilities.equal(rect, rect2));
    }

    @Test
    public void testEqualDifferentRectangles() {
        Shape rect2 = new Rectangle2D.Double(0.0, 0.0, 10.0, 30.0);
        assertFalse(ShapeUtilities.equal(rect, rect2));
    }

    @Test
    public void testEqualIdenticalEllipses() {
        Shape ellipse2 = new Ellipse2D.Double(5.0, 5.0, 15.0, 25.0);
        assertTrue(ShapeUtilities.equal(ellipse, ellipse2));
    }

    @Test
    public void testEqualIdenticalGeneralPaths() {
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(10, 0);
        gp2.lineTo(10, 10);
        gp2.closePath();
        assertTrue(ShapeUtilities.equal(generalPath, gp2));
    }

    @Test
    public void testEqualDifferentGeneralPaths() {
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(10, 0);
        gp2.lineTo(10, 20);
        gp2.closePath();
        assertFalse(ShapeUtilities.equal(generalPath, gp2));
    }

    @Test
    public void testEqualIdenticalArcs() {
        Shape arc2 = new Arc2D.Double(0.0, 0.0, 100.0, 100.0, 45.0, 90.0, Arc2D.PIE);
        assertTrue(ShapeUtilities.equal(arc, arc2));
    }

    @Test
    public void testEqualIdenticalRoundRects() {
        Shape rr2 = new RoundRectangle2D.Double(1.0, 2.0, 30.0, 40.0, 5.0, 5.0);
        assertTrue(ShapeUtilities.equal(roundRect, rr2));
    }

    @Test
    public void testEqualIdenticalQuadCurves() {
        Shape qc2 = new QuadCurve2D.Double(0.0, 0.0, 10.0, 20.0, 30.0, 0.0);
        assertTrue(ShapeUtilities.equal(quadCurve, qc2));
    }

    @Test
    public void testEqualIdenticalCubicCurves() {
        Shape cc2 = new CubicCurve2D.Double(0.0, 0.0, 10.0, 20.0, 20.0, 20.0, 30.0, 0.0);
        assertTrue(ShapeUtilities.equal(cubicCurve, cc2));
    }

    @Test
    public void testEqualIdenticalAreas() {
        Shape area2 = new Area(new Ellipse2D.Double(0, 0, 10, 10));
        assertTrue(ShapeUtilities.equal(area, area2));
    }

    @Test
    public void testEqualIdenticalPoints() {
        Shape point2 = new Point2D.Double(5.0, 5.0);
        assertTrue(ShapeUtilities.equal(point, point2));
    }

    @Test
    public void testEqualEmptyPaths() {
        GeneralPath empty2 = new GeneralPath();
        assertTrue(ShapeUtilities.equal(emptyPath, empty2));
    }

    // ---------- serialize / deserialize ----------

    @Test
    public void testSerializationLine() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(line);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(line, deserialized));
    }

    @Test
    public void testSerializationRectangle() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(rect);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(rect, deserialized));
    }

    @Test
    public void testSerializationEllipse() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(ellipse);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(ellipse, deserialized));
    }

    @Test
    public void testSerializationGeneralPath() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(generalPath);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(generalPath, deserialized));
    }

    @Test
    public void testSerializationArc() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(arc);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(arc, deserialized));
    }

    @Test
    public void testSerializationRoundRect() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(roundRect);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(roundRect, deserialized));
    }

    @Test
    public void testSerializationQuadCurve() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(quadCurve);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(quadCurve, deserialized));
    }

    @Test
    public void testSerializationCubicCurve() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(cubicCurve);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(cubicCurve, deserialized));
    }

    @Test
    public void testSerializationArea() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(area);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(area, deserialized));
    }

    @Test
    public void testSerializationPoint() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(point);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(point, deserialized));
    }

    @Test
    public void testSerializationEmptyPath() throws IOException, ClassNotFoundException {
        byte[] serialized = serialize(emptyPath);
        Shape deserialized = (Shape) deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(ShapeUtilities.equal(emptyPath, deserialized));
    }

    // ---------- rotateShape ----------

    @Test
    public void testRotateShapeNull() {
        assertNull("Rotating null should return null", ShapeUtilities.rotateShape(null, 0.5, 0.0, 0.0));
    }

    @Test
    public void testRotateShapeLine() {
        Shape rotated = ShapeUtilities.rotateShape(line, Math.PI / 4, 0.0, 0.0);
        assertNotNull(rotated);
        // Check that the rotated shape is not null and is a different instance
        assertFalse(line == rotated);
    }

    @Test
    public void testRotateShapeRectangle() {
        Shape rotated = ShapeUtilities.rotateShape(rect, Math.PI / 2, 5.0, 10.0);
        assertNotNull(rotated);
    }

    @Test
    public void testRotateShapeZeroAngle() {
        Shape rotated = ShapeUtilities.rotateShape(line, 0.0, 0.0, 0.0);
        assertTrue("Rotating by zero should produce equal shape", ShapeUtilities.equal(line, rotated));
    }

    // ---------- translateShape ----------

    @Test
    public void testTranslateShapeNull() {
        assertNull("Translating null should return null", ShapeUtilities.translateShape(null, 10.0, 20.0));
    }

    @Test
    public void testTranslateShapeLine() {
        Shape translated = ShapeUtilities.translateShape(line, 5.0, 5.0);
        assertNotNull(translated);
        assertFalse(line == translated);
    }

    @Test
    public void testTranslateShapeZeroDelta() {
        Shape translated = ShapeUtilities.translateShape(line, 0.0, 0.0);
        assertTrue("Translating by zero should produce equal shape", ShapeUtilities.equal(line, translated));
    }

    // ---------- scaleShape ----------

    @Test
    public void testScaleShapeNull() {
        assertNull("Scaling null should return null", ShapeUtilities.scaleShape(null, 2.0, 3.0));
    }

    @Test
    public void testScaleShapeLine() {
        Shape scaled = ShapeUtilities.scaleShape(line, 2.0, 2.0);
        assertNotNull(scaled);
        assertFalse(line == scaled);
    }

    @Test
    public void testScaleShapeUnitScale() {
        Shape scaled = ShapeUtilities.scaleShape(line, 1.0, 1.0);
        assertTrue("Scaling by 1 should produce equal shape", ShapeUtilities.equal(line, scaled));
    }

    @Test
    public void testScaleShapeNegativeScale() {
        Shape scaled = ShapeUtilities.scaleShape(line, -1.0, 1.0);
        assertNotNull(scaled);
    }

    // ---------- createTransformedShape ----------

    @Test
    public void testCreateTransformedShapeNullShape() {
        AffineTransform transform = AffineTransform.getTranslateInstance(10, 20);
        assertNull("Null shape should return null", ShapeUtilities.createTransformedShape(null, transform));
    }

    @Test
    public void testCreateTransformedShapeNullTransform() {
        Shape transformed = ShapeUtilities.createTransformedShape(line, null);
        assertNull("Null transform should return null", transformed);
    }

    @Test
    public void testCreateTransformedShapeIdentity() {
        AffineTransform identity = new AffineTransform();
        Shape transformed = ShapeUtilities.createTransformedShape(line, identity);
        assertTrue("Identity transform should produce equal shape", ShapeUtilities.equal(line, transformed));
    }

    @Test
    public void testCreateTransformedShapeTranslate() {
        AffineTransform translate = AffineTransform.getTranslateInstance(10, 20);
        Shape transformed = ShapeUtilities.createTransformedShape(line, translate);
        assertNotNull(transformed);
        assertFalse(line == transformed);
    }

    @Test
    public void testCreateTransformedShapeRotate() {
        AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 4);
        Shape transformed = ShapeUtilities.createTransformedShape(rect, rotate);
        assertNotNull(transformed);
    }

    // ---------- Helper methods for serialization ----------

    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        return bos.toByteArray();
    }

    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }
}