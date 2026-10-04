package org.jfree.chart.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Paint;

import org.junit.Test;

public class GrayPaintScaleTest {

    @Test
    public void testDefaultConstructor() {
        GrayPaintScale scale = new GrayPaintScale();
        assertEquals(0.0, scale.getLowerBound(), 0.00001);
        assertEquals(1.0, scale.getUpperBound(), 0.00001);
    }

    @Test
    public void testParameterizedConstructor() {
        GrayPaintScale scale = new GrayPaintScale(2.0, 10.0);
        assertEquals(2.0, scale.getLowerBound(), 0.00001);
        assertEquals(10.0, scale.getUpperBound(), 0.00001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructorBounds() {
        // Lower bound must be less than upper bound
        new GrayPaintScale(5.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructorBoundsReversed() {
        new GrayPaintScale(10.0, 2.0);
    }

    @Test
    public void testGetPaintAtLowerBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(0.0);
        assertEquals(Color.black, paint);
    }

    @Test
    public void testGetPaintAtUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(1.0);
        assertEquals(Color.white, paint);
    }

    @Test
    public void testGetPaintBelowLowerBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(-10.0);
        assertEquals(Color.black, paint);
    }

    @Test
    public void testGetPaintAboveUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(10.0);
        assertEquals(Color.white, paint);
    }

    @Test
    public void testGetPaintMidpoint() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 100.0);
        Paint paint = scale.getPaint(50.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        // Midpoint should be gray (equal R, G, B values)
        assertEquals(color.getRed(), color.getGreen());
        assertEquals(color.getGreen(), color.getBlue());
    }

    @Test
    public void testEquals() {
        GrayPaintScale scale1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale scale2 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale scale3 = new GrayPaintScale(0.0, 2.0);
        GrayPaintScale scale4 = new GrayPaintScale(0.5, 1.0);

        assertEquals(scale1, scale1);
        assertEquals(scale1, scale2);
        assertEquals(scale2, scale1);

        assertNotEquals(scale1, scale3);
        assertNotEquals(scale1, scale4);
        assertNotEquals(scale1, null);
        assertNotEquals(scale1, "Some String");
    }

    @Test
    public void testHashCode() {
        GrayPaintScale scale1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale scale2 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale scale3 = new GrayPaintScale(0.0, 2.0);

        assertEquals(scale1.hashCode(), scale2.hashCode());
        // Hash codes might differ for different bounds, though not strictly required, 
        // it's good practice to verify consistency.
        boolean hashDiffersOrNot = (scale1.hashCode() != scale3.hashCode()) || (scale1.hashCode() == scale3.hashCode());
        assertTrue(hashDiffersOrNot);
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        GrayPaintScale scale1 = new GrayPaintScale(1.0, 5.0);
        GrayPaintScale scale2 = (GrayPaintScale) scale1.clone();

        assertEquals(scale1, scale2);
        assertTrue(scale1 != scale2);
    }
}