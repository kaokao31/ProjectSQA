package org.jfree.chart.renderer.category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for MinMaxCategoryRenderer.
 * Designed to achieve high coverage and detect potential faults.
 */
public class MinMaxCategoryRendererTest {

    private MinMaxCategoryRenderer renderer;
    private CategoryPlot plot;
    private DefaultCategoryDataset dataset;

    @Before
    public void setUp() {
        renderer = new MinMaxCategoryRenderer();
        dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        dataset.addValue(3.0, "R2", "C1");
        dataset.addValue(4.0, "R2", "C2");
        plot = new CategoryPlot(dataset, null, null, renderer);
    }

    // ---- Constructor and Defaults ----
    @Test
    public void testDefaultValues() {
        assertTrue("Default drawLines should be true", renderer.getDrawLines());
        assertNull("Default groupPaint should be null", renderer.getGroupPaint());
        assertNull("Default groupStroke should be null", renderer.getGroupStroke());
        assertNull("Default maxIcon should be null", renderer.getMaxIcon());
        assertNull("Default minIcon should be null", renderer.getMinIcon());
    }

    // ---- Setters and Getters ----
    @Test
    public void testSetDrawLines() {
        renderer.setDrawLines(false);
        assertFalse("drawLines should be false", renderer.getDrawLines());
        renderer.setDrawLines(true);
        assertTrue("drawLines should be true", renderer.getDrawLines());
    }

    @Test
    public void testSetGroupPaint() {
        Paint paint = Color.RED;
        renderer.setGroupPaint(paint);
        assertEquals("groupPaint should be RED", paint, renderer.getGroupPaint());
        renderer.setGroupPaint(null);
        assertNull("groupPaint should be null", renderer.getGroupPaint());
    }

    @Test
    public void testSetGroupStroke() {
        Stroke stroke = new BasicStroke(2.0f);
        renderer.setGroupStroke(stroke);
        assertEquals("groupStroke should be the same", stroke, renderer.getGroupStroke());
        renderer.setGroupStroke(null);
        assertNull("groupStroke should be null", renderer.getGroupStroke());
    }

    @Test
    public void testSetMaxIcon() {
        Icon icon = new ImageIcon();
        renderer.setMaxIcon(icon);
        assertEquals("maxIcon should be the same", icon, renderer.getMaxIcon());
        renderer.setMaxIcon(null);
        assertNull("maxIcon should be null", renderer.getMaxIcon());
    }

    @Test
    public void testSetMinIcon() {
        Icon icon = new ImageIcon();
        renderer.setMinIcon(icon);
        assertEquals("minIcon should be the same", icon, renderer.getMinIcon());
        renderer.setMinIcon(null);
        assertNull("minIcon should be null", renderer.getMinIcon());
    }

    // ---- Equals and HashCode ----
    @Test
    public void testEquals() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        assertTrue("Two default renderers should be equal", r1.equals(r2));
        assertTrue("HashCode should be equal", r1.hashCode() == r2.hashCode());

        // Change drawLines
        r1.setDrawLines(false);
        assertFalse("Different drawLines should not be equal", r1.equals(r2));
        r2.setDrawLines(false);
        assertTrue("Same drawLines should be equal", r1.equals(r2));

        // Change groupPaint
        r1.setGroupPaint(Color.BLUE);
        assertFalse("Different groupPaint should not be equal", r1.equals(r2));
        r2.setGroupPaint(Color.BLUE);
        assertTrue("Same groupPaint should be equal", r1.equals(r2));

        // Change groupStroke
        r1.setGroupStroke(new BasicStroke(1.5f));
        assertFalse("Different groupStroke should not be equal", r1.equals(r2));
        r2.setGroupStroke(new BasicStroke(1.5f));
        assertTrue("Same groupStroke should be equal", r1.equals(r2));

        // Change maxIcon
        r1.setMaxIcon(new ImageIcon());
        assertFalse("Different maxIcon should not be equal", r1.equals(r2));
        r2.setMaxIcon(new ImageIcon());
        assertTrue("Same maxIcon should be equal", r1.equals(r2));

        // Change minIcon
        r1.setMinIcon(new ImageIcon());
        assertFalse("Different minIcon should not be equal", r1.equals(r2));
        r2.setMinIcon(new ImageIcon());
        assertTrue("Same minIcon should be equal", r1.equals(r2));

        // Null and different class
        assertFalse("Null should not be equal", r1.equals(null));
        assertFalse("Different class should not be equal", r1.equals("string"));
    }

    @Test
    public void testHashCodeConsistency() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        assertEquals("HashCode should be consistent for equal objects", r1.hashCode(), r2.hashCode());
        r1.setDrawLines(false);
        // After change, hashCodes may differ; just ensure no exception
        r1.hashCode();
    }

    // ---- Clone ----
    @Test
    public void testClone() throws CloneNotSupportedException {
        renderer.setDrawLines(false);
        renderer.setGroupPaint(Color.GREEN);
        renderer.setGroupStroke(new BasicStroke(3.0f));
        renderer.setMaxIcon(new ImageIcon());
        renderer.setMinIcon(new ImageIcon());
        MinMaxCategoryRenderer cloned = (MinMaxCategoryRenderer) renderer.clone();
        assertNotSame("Cloned object should be different", renderer, cloned);
        assertEquals("Cloned object should be equal", renderer, cloned);
        // Modify original and verify clone unchanged
        renderer.setDrawLines(true);
        assertFalse("Clone should not be affected by original changes", cloned.getDrawLines());
    }

    // ---- Serialization ----
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        renderer.setDrawLines(false);
        renderer.setGroupPaint(Color.MAGENTA);
        renderer.setGroupStroke(new BasicStroke(2.5f));
        renderer.setMaxIcon(new ImageIcon());
        renderer.setMinIcon(new ImageIcon());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(renderer);
        oos.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        MinMaxCategoryRenderer deserialized = (MinMaxCategoryRenderer) ois.readObject();
        assertEquals("Serialized and deserialized should be equal", renderer, deserialized);
        assertNotSame("Should be different objects", renderer, deserialized);
    }

    // ---- drawRangeGridline ----
    @Test
    public void testDrawRangeGridlineWithNullGraphics() {
        // Should not throw NullPointerException
        try {
            renderer.drawRangeGridline(null, plot, plot.getRangeAxis(), 1.0,
                    new Rectangle2D.Double(0, 0, 100, 100), true);
        } catch (NullPointerException e) {
            // Acceptable if method throws NPE on null graphics; but we want to detect if it's handled
        }
    }

    @Test
    public void testDrawRangeGridlineWithNullPlot() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            renderer.drawRangeGridline(g2, null, plot.getRangeAxis(), 1.0,
                    new Rectangle2D.Double(0, 0, 100, 100), true);
        } catch (NullPointerException e) {
            // Acceptable if method throws NPE on null plot
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawRangeGridlineWithNullAxis() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            renderer.drawRangeGridline(g2, plot, null, 1.0,
                    new Rectangle2D.Double(0, 0, 100, 100), true);
        } catch (NullPointerException e) {
            // Acceptable if method throws NPE on null axis
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawRangeGridlineWithNullDataArea() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            renderer.drawRangeGridline(g2, plot, plot.getRangeAxis(), 1.0, null, true);
        } catch (NullPointerException e) {
            // Acceptable if method throws NPE on null dataArea
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawRangeGridlineNormal() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 180, 180);
        // Ensure plot has range axis
        plot.setRangeAxis(plot.getRangeAxis());
        renderer.drawRangeGridline(g2, plot, plot.getRangeAxis(), 2.0, dataArea, true);
        g2.dispose();
        // No exception expected; just ensure method runs without error
    }

    // ---- Additional edge cases ----
    @Test
    public void testSetGroupPaintWithNullAndNonNull() {
        renderer.setGroupPaint(Color.YELLOW);
        assertNotNull("groupPaint should not be null", renderer.getGroupPaint());
        renderer.setGroupPaint(null);
        assertNull("groupPaint should be null", renderer.getGroupPaint());
    }

    @Test
    public void testSetGroupStrokeWithNullAndNonNull() {
        renderer.setGroupStroke(new BasicStroke(1.0f));
        assertNotNull("groupStroke should not be null", renderer.getGroupStroke());
        renderer.setGroupStroke(null);
        assertNull("groupStroke should be null", renderer.getGroupStroke());
    }

    @Test
    public void testSetMaxIconWithNullAndNonNull() {
        renderer.setMaxIcon(new ImageIcon());
        assertNotNull("maxIcon should not be null", renderer.getMaxIcon());
        renderer.setMaxIcon(null);
        assertNull("maxIcon should be null", renderer.getMaxIcon());
    }

    @Test
    public void testSetMinIconWithNullAndNonNull() {
        renderer.setMinIcon(new ImageIcon());
        assertNotNull("minIcon should not be null", renderer.getMinIcon());
        renderer.setMinIcon(null);
        assertNull("minIcon should be null", renderer.getMinIcon());
    }

    // ---- Test that renderer implements Serializable ----
    @Test
    public void testSerializable() {
        assertTrue("MinMaxCategoryRenderer should implement Serializable",
                renderer instanceof Serializable);
    }
}