package org.jfree.chart.util;

import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the {@link ShapeList} class.
 * Designed to achieve high coverage and detect potential faults.
 */
public class ShapeListTest {

    private ShapeList list;

    @Before
    public void setUp() {
        list = new ShapeList();
    }

    // ---- Constructor and basic state ----
    @Test
    public void testDefaultConstructor() {
        assertEquals(0, list.size());
    }

    @Test
    public void testConstructorWithShapes() {
        Shape[] shapes = new Shape[] {
                new Rectangle2D.Double(1, 2, 3, 4),
                new Ellipse2D.Double(5, 6, 7, 8)
        };
        ShapeList sl = new ShapeList(shapes);
        assertEquals(2, sl.size());
        assertEquals(new Rectangle2D.Double(1, 2, 3, 4), sl.getShape(0));
        assertEquals(new Ellipse2D.Double(5, 6, 7, 8), sl.getShape(1));
    }

    @Test
    public void testConstructorWithNullArray() {
        // Depending on implementation; assume it creates empty list
        try {
            ShapeList sl = new ShapeList((Shape[]) null);
            assertEquals(0, sl.size());
        } catch (NullPointerException e) {
            // Also valid if it throws
        }
    }

    // ---- getShape and setShape ----
    @Test
    public void testSetAndGetShape() {
        Shape s = new Rectangle2D.Double(1, 2, 3, 4);
        list.setShape(0, s);
        assertEquals(s, list.getShape(0));
        assertEquals(1, list.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetShapeNegativeIndex() {
        list.getShape(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetShapeIndexGreaterThanSize() {
        list.getShape(5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetShapeNegativeIndex() {
        list.setShape(-1, new Rectangle2D.Double());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetShapeIndexGreaterThanSize() {
        list.setShape(5, new Rectangle2D.Double());
    }

    @Test
    public void testSetShapeExpandsList() {
        list.setShape(0, new Rectangle2D.Double());
        list.setShape(1, new Ellipse2D.Double());
        list.setShape(10, new Line2D.Double(1,2,3,4));
        assertEquals(11, list.size());
        assertNull(list.getShape(2));  // indices 2..9 should be null
        assertNotNull(list.getShape(10));
    }

    @Test
    public void testSetShapeOverwritesExisting() {
        Shape s1 = new Rectangle2D.Double(1,2,3,4);
        Shape s2 = new Ellipse2D.Double(5,6,7,8);
        list.setShape(0, s1);
        list.setShape(0, s2);
        assertEquals(s2, list.getShape(0));
    }

    // ---- size ----
    @Test
    public void testSizeAfterExpansion() {
        assertEquals(0, list.size());
        list.setShape(0, new Rectangle2D.Double());
        assertEquals(1, list.size());
        list.setShape(5, new Rectangle2D.Double());
        assertEquals(6, list.size());
    }

    // ---- equals ----
    @Test
    public void testEqualsSameObject() {
        assertTrue(list.equals(list));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(list.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(list.equals("string"));
    }

    @Test
    public void testEqualsEmptyLists() {
        ShapeList other = new ShapeList();
        assertTrue(list.equals(other));
        assertEquals(list.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsEqualContent() {
        list.setShape(0, new Rectangle2D.Double(1,2,3,4));
        list.setShape(1, null);
        ShapeList other = new ShapeList();
        other.setShape(0, new Rectangle2D.Double(1,2,3,4));
        other.setShape(1, null);
        assertTrue(list.equals(other));
        assertEquals(list.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsDifferentSizes() {
        list.setShape(0, new Rectangle2D.Double());
        ShapeList other = new ShapeList();
        assertFalse(list.equals(other));
    }

    @Test
    public void testEqualsDifferentShapes() {
        list.setShape(0, new Rectangle2D.Double(1,2,3,4));
        ShapeList other = new ShapeList();
        other.setShape(0, new Ellipse2D.Double(1,2,3,4));
        assertFalse(list.equals(other));
    }

    @Test
    public void testEqualsOneNullShape() {
        list.setShape(0, new Rectangle2D.Double());
        ShapeList other = new ShapeList();
        other.setShape(0, null);
        assertFalse(list.equals(other));
    }

    // ---- clone ----
    @Test
    public void testCloneBasic() throws CloneNotSupportedException {
        list.setShape(0, new Rectangle2D.Double(1,2,3,4));
        list.setShape(1, new Ellipse2D.Double(5,6,7,8));
        ShapeList cloned = (ShapeList) list.clone();
        assertNotSame(cloned, list);
        assertEquals(list, cloned);
    }

    @Test
    public void testCloneDeepCopyShapes() throws CloneNotSupportedException {
        Rectangle2D original = new Rectangle2D.Double(1,2,3,4);
        list.setShape(0, original);
        ShapeList cloned = (ShapeList) list.clone();

        // Modify original shape
        original.setRect(10,10,100,100);
        // Cloned shape should remain unchanged
        assertFalse(list.equals(cloned));  // lists now differ
        assertNotEquals(original, cloned.getShape(0));
    }

    @Test
    public void testCloneNullShapes() throws CloneNotSupportedException {
        list.setShape(0, null);
        list.setShape(1, new Rectangle2D.Double());
        ShapeList cloned = (ShapeList) list.clone();
        assertNull(cloned.getShape(0));
        assertNotNull(cloned.getShape(1));
        assertEquals(list, cloned);
    }

    // ---- serialization ----
    @Test
    public void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
        list.setShape(0, new Rectangle2D.Double(1,2,3,4));
        list.setShape(1, null);
        list.setShape(2, new Ellipse2D.Double(5,6,7,8));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(list);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();

        assertEquals(list, deserialized);
    }

    @Test
    public void testSerializationNullList() throws IOException, ClassNotFoundException {
        // Empty list
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(list);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        ShapeList deserialized = (ShapeList) ois.readObject();
        ois.close();

        assertEquals(0, deserialized.size());
        assertTrue(list.equals(deserialized));
    }

    // ---- shape manipulation and edge cases ----
    @Test
    public void testSettingShapeToNull() {
        list.setShape(0, null);
        assertEquals(1, list.size());
        assertNull(list.getShape(0));
    }

    @Test
    public void testGetShapeReturnsNullForUnsetIndices() {
        list.setShape(5, new Rectangle2D.Double());
        assertNull(list.getShape(0));
        assertNull(list.getShape(4));
        assertNotNull(list.getShape(5));
    }

    @Test
    public void testMultipleSetGetConsistency() {
        Shape s1 = new Line2D.Double(1,1,10,10);
        Shape s2 = new Rectangle2D.Double(10,10,20,20);
        list.setShape(0, s1);
        list.setShape(1, s2);
        assertSame(s1, list.getShape(0));
        assertSame(s2, list.getShape(1));
    }

    // ---- hashCode ----
    @Test
    public void testHashCodeConsistency() {
        list.setShape(0, new Rectangle2D.Double(1,2,3,4));
        int hash1 = list.hashCode();
        int hash2 = list.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCodeDifferentObjects() {
        list.setShape(0, new Rectangle2D.Double(1,2,3,4));
        ShapeList other = new ShapeList();
        other.setShape(0, new Rectangle2D.Double(1,2,3,4));
        assertEquals(list.hashCode(), other.hashCode());

        other.setShape(0, new Rectangle2D.Double(1,2,3,5));
        assertNotEquals(list.hashCode(), other.hashCode());
    }

    // ---- toArray / asList? (not defined in original API, but if exists) ----
    // The class likely has no such methods; skip.

    // ---- Additional edge: ensure clone returns independent object and equals behaves with different sizes ----
    @Test
    public void testCloneIndependence() throws CloneNotSupportedException {
        ShapeList sl = new ShapeList();
        sl.setShape(0, new Rectangle2D.Double());
        ShapeList clone = (ShapeList) sl.clone();
        clone.setShape(1, new Ellipse2D.Double());
        assertFalse(sl.equals(clone));  // different sizes
    }

    // ---- null shapes in constructor ----
    @Test
    public void testConstructorWithNullElements() {
        Shape[] shapes = new Shape[] { new Rectangle2D.Double(), null, new Ellipse2D.Double() };
        ShapeList sl = new ShapeList(shapes);
        assertEquals(3, sl.size());
        assertNotNull(sl.getShape(0));
        assertNull(sl.getShape(1));
        assertNotNull(sl.getShape(2));
    }
}