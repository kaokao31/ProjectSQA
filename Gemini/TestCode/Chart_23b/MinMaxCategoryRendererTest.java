package org.jfree.chart.renderer.category;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.util.BooleanList;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

public class MinMaxCategoryRendererTest {

    private MinMaxCategoryRenderer renderer;

    @Before
    public void setUp() {
        renderer = new MinMaxCategoryRenderer();
    }

    @Test
    public void testDefaultConstructor() {
        assertFalse(renderer.isDrawLines());
        assertNotNull(renderer.getGroupPaint());
        assertNotNull(renderer.getGroupStroke());
        assertNull(renderer.getMinIcon());
        assertNull(renderer.getMaxIcon());
        assertNull(renderer.getObjectIcon());
    }

    @Test
    public void testSetDrawLines() {
        renderer.setDrawLines(true);
        assertTrue(renderer.isDrawLines());
        renderer.setDrawLines(false);
        assertFalse(renderer.isDrawLines());
    }

    @Test
    public void testSetGroupPaint() {
        Paint paint = Color.RED;
        renderer.setGroupPaint(paint);
        assertEquals(paint, renderer.getGroupPaint());

        try {
            renderer.setGroupPaint(null);
            fail("Expected IllegalArgumentException for null group paint");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetGroupStroke() {
        Stroke stroke = renderer.getGroupStroke();
        renderer.setGroupStroke(stroke);
        assertEquals(stroke, renderer.getGroupStroke());

        try {
            renderer.setGroupStroke(null);
            fail("Expected IllegalArgumentException for null group stroke");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIcons() {
        assertNull(renderer.getMinIcon());
        assertNull(renderer.getMaxIcon());
        assertNull(renderer.getObjectIcon());

        // Since Icons are usually Icon implementations, let's test setting them to null or mock if possible.
        // MinMaxCategoryRenderer accepts Icon for minIcon, maxIcon, objectIcon.
        renderer.setMinIcon(null);
        renderer.setMaxIcon(null);
        renderer.setObjectIcon(null);

        assertNull(renderer.getMinIcon());
        assertNull(renderer.getMaxIcon());
        assertNull(renderer.getObjectIcon());
    }

    @Test
    public void testEqualsAndHashCode() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();

        assertTrue(r1.equals(r2));
        assertEquals(r1.hashCode(), r2.hashCode());

        r1.setDrawLines(true);
        assertFalse(r1.equals(r2));

        r2.setDrawLines(true);
        assertTrue(r1.equals(r2));

        r1.setGroupPaint(Color.BLUE);
        assertFalse(r1.equals(r2));

        r2.setGroupPaint(Color.BLUE);
        assertTrue(r1.equals(r2));

        assertFalse(r1.equals(null));
        assertFalse(r1.equals("Some String"));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        renderer.setDrawLines(true);
        renderer.setGroupPaint(Color.GREEN);
        
        MinMaxCategoryRenderer clone = (MinMaxCategoryRenderer) renderer.clone();
        assertEquals(renderer, clone);
        assertNotSame(renderer, clone);
    }

    @Test
    public void testDrawItem() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row1", "Col1");
        dataset.addValue(2.0, "Row1", "Col2");
        dataset.addValue(3.0, "Row2", "Col1");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), new NumberAxis("Value"), renderer);
        JFreeChart chart = new JFreeChart(plot);
        
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 400, 300);
        
        // This will test the rendering logic without throwing exceptions
        chart.draw(g2, dataArea);
        
        renderer.setDrawLines(true);
        chart.draw(g2, dataArea);
    }
}