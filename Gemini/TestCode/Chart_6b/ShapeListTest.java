package org.jfree.chart.util;

import static org.junit.Assert.*;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;

import org.junit.Test;

public class ShapeListTest {

    @Test
    public void testConstructorAndDefaults() {
        ShapeList list = new ShapeList();
        assertEquals(0, list.size());
        assertNull(list.getShape(0));
    }

    @Test
    public void testSetAndGetShape() {
        ShapeList list = new ShapeList();
        Shape rect = new Rectangle(0, 0, 10, 10);
        Shape line = new Line2D.Double(0, 0, 5, 5);

        list.setShape(0, rect);
        list.setShape(2, line);

        assertEquals(3, list.size());
        assertSame(rect, list.getShape(0));
        assertNull(list.getShape(1));
        assertSame(line, list.getShape(2));
    }

    @Test
    public void testSetShapeNull() {
        ShapeList list = new ShapeList();
        Shape rect = new Rectangle(1, 1, 5, 5);
        
        list.setShape(0, rect);
        assertEquals(1, list.size());
        
        list.setShape(0, null);
        assertNull(list.getShape(0));
    }

    @Test
    public void testEqualsAndHashCode() {
        ShapeList list1 = new ShapeList();
        ShapeList list2 = new ShapeList();

        assertTrue(list1.equals(list2));
        assertEquals(list1.hashCode(), list2.hashCode());

        Shape rect1 = new Rectangle(0, 0, 10, 10);
        Shape rect2 = new Rectangle(0, 0, 10, 10);
        Shape poly = new Polygon(new int[] {1, 2}, new int[] {1, 2}, 2);

        list1.setShape(0, rect1);
        assertFalse(list1.equals(list2));

        // Note: AbstractObjectList / ShapeList uses equals which might compare Shape objects 
        // using standard equals or reference equality depending on implementation. 
        // Let's test with exact same reference and different shapes.
        list2.setShape(0, rect1);
        assertTrue(list1.equals(list2));
        assertEquals(list1.hashCode(), list2.hashCode());

        list2.setShape(0, poly);
        assertFalse(list1.equals(list2));
    }

    @Test
    public void testEqualsWithDifferentTypesAndNull() {
        ShapeList list = new ShapeList();
        assertFalse(list.equals(null));
        assertFalse(list.equals("Some String"));
    }

    @Test
    public void testClone() {
        ShapeList list1 = new ShapeList();
        Shape rect = new Rectangle(0, 0, 10, 10);
        list1.setShape(0, rect);
        list1.setShape(5, new Line2D.Double(1, 2, 3, 4));

        try {
            ShapeList list2 = (ShapeList) list1.clone();
            assertNotSame(list1, list2);
            assertEquals(list1, list2);
            assertEquals(list1.size(), list2.size());
            assertSame(list1.getShape(0), list2.getShape(0));
            assertSame(list1.getShape(5), list2.getShape(5));
        } catch (CloneNotSupportedException e) {
            fail("CloneNotSupportedException should not have been thrown");
        }
    }
}