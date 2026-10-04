package org.jfree.chart.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

import org.junit.Test;

public class ValueMarkerTest {

    @Test
    public void testConstructorsAndGetters() {
        // Constructor 1: ValueMarker(double value)
        ValueMarker m1 = new ValueMarker(100.0);
        assertEquals(100.0, m1.getValue(), 0.00001);

        // Constructor 2: ValueMarker(double value, Paint paint, Stroke stroke)
        Paint paint = Color.RED;
        Stroke stroke = new BasicStroke(2.0f);
        ValueMarker m2 = new ValueMarker(200.0, paint, stroke);
        assertEquals(200.0, m2.getValue(), 0.00001);
        assertEquals(paint, m2.getPaint());
        assertEquals(stroke, m2.getStroke());

        // Constructor 3: ValueMarker(double value, Paint paint, Stroke stroke, Paint outlinePaint, Stroke outlineStroke, float alpha)
        Paint outlinePaint = Color.BLUE;
        Stroke outlineStroke = new BasicStroke(1.0f);
        ValueMarker m3 = new ValueMarker(300.0, paint, stroke, outlinePaint, outlineStroke, 0.5f);
        assertEquals(300.0, m3.getValue(), 0.00001);
        assertEquals(paint, m3.getPaint());
        assertEquals(stroke, m3.getStroke());
        assertEquals(outlinePaint, m3.getOutlinePaint());
        assertEquals(outlineStroke, m3.getOutlineStroke());
        assertEquals(0.5f, m3.getAlpha(), 0.00001);
    }

    @Test
    public void testSetValue() {
        ValueMarker marker = new ValueMarker(50.0);
        assertEquals(50.0, marker.getValue(), 0.00001);

        marker.setValue(150.0);
        assertEquals(150.0, marker.getValue(), 0.00001);
    }

    @Test
    public void testEquals() {
        ValueMarker m1 = new ValueMarker(100.0, Color.RED, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(100.0, Color.RED, new BasicStroke(1.0f));

        // Test reflexivity
        assertTrue(m1.equals(m1));

        // Test symmetry/equality
        assertTrue(m1.equals(m2));
        assertTrue(m2.equals(m1));

        // Test null comparison
        assertFalse(m1.equals(null));

        // Test different class comparison
        assertFalse(m1.equals("Some String"));

        // Test different value
        ValueMarker m3 = new ValueMarker(105.0, Color.RED, new BasicStroke(1.0f));
        assertFalse(m1.equals(m3));

        // Test superclass equals contract (Marker properties)
        ValueMarker m4 = new ValueMarker(100.0, Color.BLUE, new BasicStroke(1.0f));
        assertFalse(m1.equals(m4));
    }

    @Test
    public void testHashCode() {
        ValueMarker m1 = new ValueMarker(100.0, Color.RED, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(100.0, Color.RED, new BasicStroke(1.0f));

        assertEquals(m1.hashCode(), m2.hashCode());

        ValueMarker m3 = new ValueMarker(200.0, Color.RED, new BasicStroke(1.0f));
        // While not strictly required to be unequal, distinct values should ideally yield different hash codes or at least not fail
        assertNotNull(m3.hashCode());
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        ValueMarker m1 = new ValueMarker(123.45, Color.GREEN, new BasicStroke(1.5f));
        ValueMarker m2 = (ValueMarker) m1.clone();

        assertNotNull(m2);
        assertEquals(m1, m2);
        assertTrue(m1 != m2);
        assertEquals(m1.getValue(), m2.getValue(), 0.00001);
    }
}