package org.jfree.chart.axis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link Axis}.  This test covers the public API of the
 * abstract Axis class using a concrete subclass (NumberAxis).
 */
public class AxisTest {

    private NumberAxis axis;

    @Before
    public void setUp() {
        axis = new NumberAxis("Test Axis");
    }

    // ---- Constructor and basic properties ----

    @Test
    public void testConstructor() {
        assertEquals("Test Axis", axis.getLabel());
        assertTrue(axis.isVisible());
        assertTrue(axis.isTickLabelsVisible());
        assertTrue(axis.isTickMarksVisible());
        assertTrue(axis.isAxisLineVisible());
        assertFalse(axis.isInverted());
        assertTrue(axis.isAutoRange());
        assertFalse(axis.isMinorTickMarksVisible());
        assertEquals(0.0, axis.getLabelAngle(), 0.0001);
    }

    @Test
    public void testConstructorNullLabel() {
        NumberAxis a = new NumberAxis(null);
        assertNull(a.getLabel());
    }

    // ---- Label ----

    @Test
    public void testSetLabel() {
        axis.setLabel("New Label");
        assertEquals("New Label", axis.getLabel());
    }

    @Test
    public void testSetLabelNull() {
        axis.setLabel(null);
        assertNull(axis.getLabel());
    }

    @Test
    public void testSetLabelAngle() {
        axis.setLabelAngle(Math.PI / 4);
        assertEquals(Math.PI / 4, axis.getLabelAngle(), 0.0001);
    }

    @Test
    public void testSetLabelAngleNegative() {
        axis.setLabelAngle(-Math.PI / 2);
        assertEquals(-Math.PI / 2, axis.getLabelAngle(), 0.0001);
    }

    @Test
    public void testSetLabelAngleZero() {
        axis.setLabelAngle(0.0);
        assertEquals(0.0, axis.getLabelAngle(), 0.0001);
    }

    @Test
    public void testSetLabelAngleLarge() {
        axis.setLabelAngle(2 * Math.PI);
        assertEquals(2 * Math.PI, axis.getLabelAngle(), 0.0001);
    }

    // ---- Label font ----

    @Test
    public void testSetLabelFont() {
        java.awt.Font font = new java.awt.Font("Dialog", java.awt.Font.BOLD, 14);
        axis.setLabelFont(font);
        assertEquals(font, axis.getLabelFont());
    }

    @Test
    public void testSetLabelFontNull() {
        axis.setLabelFont(null);
        assertNull(axis.getLabelFont());
    }

    // ---- Label paint ----

    @Test
    public void testSetLabelPaint() {
        axis.setLabelPaint(Color.RED);
        assertEquals(Color.RED, axis.getLabelPaint());
    }

    @Test
    public void testSetLabelPaintNull() {
        axis.setLabelPaint(null);
        assertNull(axis.getLabelPaint());
    }

    // ---- Label insets ----

    @Test
    public void testSetLabelInsets() {
        RectangleInsets insets = new RectangleInsets(1, 2, 3, 4);
        axis.setLabelInsets(insets);
        assertEquals(insets, axis.getLabelInsets());
    }

    @Test
    public void testSetLabelInsetsNull() {
        axis.setLabelInsets(null);
        assertNull(axis.getLabelInsets());
    }

    // ---- Tick labels visible ----

    @Test
    public void testSetTickLabelsVisible() {
        axis.setTickLabelsVisible(false);
        assertFalse(axis.isTickLabelsVisible());
    }

    @Test
    public void testSetTickLabelsVisibleTrue() {
        axis.setTickLabelsVisible(true);
        assertTrue(axis.isTickLabelsVisible());
    }

    // ---- Tick label font ----

    @Test
    public void testSetTickLabelFont() {
        java.awt.Font font = new java.awt.Font("Dialog", java.awt.Font.ITALIC, 10);
        axis.setTickLabelFont(font);
        assertEquals(font, axis.getTickLabelFont());
    }

    @Test
    public void testSetTickLabelFontNull() {
        axis.setTickLabelFont(null);
        assertNull(axis.getTickLabelFont());
    }

    // ---- Tick label paint ----

    @Test
    public void testSetTickLabelPaint() {
        axis.setTickLabelPaint(Color.BLUE);
        assertEquals(Color.BLUE, axis.getTickLabelPaint());
    }

    @Test
    public void testSetTickLabelPaintNull() {
        axis.setTickLabelPaint(null);
        assertNull(axis.getTickLabelPaint());
    }

    // ---- Tick label insets ----

    @Test
    public void testSetTickLabelInsets() {
        RectangleInsets insets = new RectangleInsets(1, 2, 3, 4);
        axis.setTickLabelInsets(insets);
        assertEquals(insets, axis.getTickLabelInsets());
    }

    @Test
    public void testSetTickLabelInsetsNull() {
        axis.setTickLabelInsets(null);
        assertNull(axis.getTickLabelInsets());
    }

    // ---- Tick marks visible ----

    @Test
    public void testSetTickMarksVisible() {
        axis.setTickMarksVisible(false);
        assertFalse(axis.isTickMarksVisible());
    }

    @Test
    public void testSetTickMarksVisibleTrue() {
        axis.setTickMarksVisible(true);
        assertTrue(axis.isTickMarksVisible());
    }

    // ---- Tick mark stroke ----

    @Test
    public void testSetTickMarkStroke() {
        Stroke stroke = new BasicStroke(2.0f);
        axis.setTickMarkStroke(stroke);
        assertEquals(stroke, axis.getTickMarkStroke());
    }

    @Test
    public void testSetTickMarkStrokeNull() {
        axis.setTickMarkStroke(null);
        assertNull(axis.getTickMarkStroke());
    }

    // ---- Tick mark paint ----

    @Test
    public void testSetTickMarkPaint() {
        axis.setTickMarkPaint(Color.GREEN);
        assertEquals(Color.GREEN, axis.getTickMarkPaint());
    }

    @Test
    public void testSetTickMarkPaintNull() {
        axis.setTickMarkPaint(null);
        assertNull(axis.getTickMarkPaint());
    }

    // ---- Tick mark outside length ----

    @Test
    public void testSetTickMarkOutsideLength() {
        axis.setTickMarkOutsideLength(5.0);
        assertEquals(5.0, axis.getTickMarkOutsideLength(), 0.0001);
    }

    @Test
    public void testSetTickMarkOutsideLengthNegative() {
        axis.setTickMarkOutsideLength(-1.0);
        assertEquals(-1.0, axis.getTickMarkOutsideLength(), 0.0001);
    }

    // ---- Tick mark inside length ----

    @Test
    public void testSetTickMarkInsideLength() {
        axis.setTickMarkInsideLength(3.0);
        assertEquals(3.0, axis.getTickMarkInsideLength(), 0.0001);
    }

    @Test
    public void testSetTickMarkInsideLengthNegative() {
        axis.setTickMarkInsideLength(-2.0);
        assertEquals(-2.0, axis.getTickMarkInsideLength(), 0.0001);
    }

    // ---- Minor tick marks ----

    @Test
    public void testSetMinorTickMarksVisible() {
        axis.setMinorTickMarksVisible(true);
        assertTrue(axis.isMinorTickMarksVisible());
    }

    @Test
    public void testSetMinorTickMarksVisibleFalse() {
        axis.setMinorTickMarksVisible(false);
        assertFalse(axis.isMinorTickMarksVisible());
    }

    @Test
    public void testSetMinorTickMarkOutsideLength() {
        axis.setMinorTickMarkOutsideLength(2.0);
        assertEquals(2.0, axis.getMinorTickMarkOutsideLength(), 0.0001);
    }

    @Test
    public void testSetMinorTickMarkInsideLength() {
        axis.setMinorTickMarkInsideLength(1.0);
        assertEquals(1.0, axis.getMinorTickMarkInsideLength(), 0.0001);
    }

    // ---- Axis line visible ----

    @Test
    public void testSetAxisLineVisible() {
        axis.setAxisLineVisible(false);
        assertFalse(axis.isAxisLineVisible());
    }

    @Test
    public void testSetAxisLineVisibleTrue() {
        axis.setAxisLineVisible(true);
        assertTrue(axis.isAxisLineVisible());
    }

    // ---- Axis line paint ----

    @Test
    public void testSetAxisLinePaint() {
        axis.setAxisLinePaint(Color.YELLOW);
        assertEquals(Color.YELLOW, axis.getAxisLinePaint());
    }

    @Test
    public void testSetAxisLinePaintNull() {
        axis.setAxisLinePaint(null);
        assertNull(axis.getAxisLinePaint());
    }

    // ---- Axis line stroke ----

    @Test
    public void testSetAxisLineStroke() {
        Stroke stroke = new BasicStroke(1.5f);
        axis.setAxisLineStroke(stroke);
        assertEquals(stroke, axis.getAxisLineStroke());
    }

    @Test
    public void testSetAxisLineStrokeNull() {
        axis.setAxisLineStroke(null);
        assertNull(axis.getAxisLineStroke());
    }

    // ---- Fixed dimension ----

    @Test
    public void testSetFixedDimension() {
        axis.setFixedDimension(100.0);
        assertEquals(100.0, axis.getFixedDimension(), 0.0001);
    }

    @Test
    public void testSetFixedDimensionNegative() {
        axis.setFixedDimension(-50.0);
        assertEquals(-50.0, axis.getFixedDimension(), 0.0001);
    }

    // ---- Visible ----

    @Test
    public void testSetVisible() {
        axis.setVisible(false);
        assertFalse(axis.isVisible());
    }

    @Test
    public void testSetVisibleTrue() {
        axis.setVisible(true);
        assertTrue(axis.isVisible());
    }

    // ---- Range ----

    @Test
    public void testSetRange() {
        Range range = new Range(0.0, 10.0);
        axis.setRange(range);
        assertEquals(range, axis.getRange());
    }

    @Test
    public void testSetRangeNull() {
        axis.setRange(null);
        assertNull(axis.getRange());
    }

    @Test
    public void testSetRangeWithLowerUpper() {
        axis.setRange(5.0, 15.0);
        assertEquals(new Range(5.0, 15.0), axis.getRange());
    }

    // ---- Range type ----

    @Test
    public void testSetRangeType() {
        axis.setRangeType(RangeType.POSITIVE);
        assertEquals(RangeType.POSITIVE, axis.getRangeType());
    }

    @Test
    public void testSetRangeTypeNull() {
        axis.setRangeType(null);
        assertNull(axis.getRangeType());
    }

    // ---- Fixed auto range ----

    @Test
    public void testSetFixedAutoRange() {
        axis.setFixedAutoRange(20.0);
        assertEquals(20.0, axis.getFixedAutoRange(), 0.0001);
    }

    @Test
    public void testSetFixedAutoRangeNegative() {
        axis.setFixedAutoRange(-10.0);
        assertEquals(-10.0, axis.getFixedAutoRange(), 0.0001);
    }

    // ---- Margins ----

    @Test
    public void testSetLowerMargin() {
        axis.setLowerMargin(0.1);
        assertEquals(0.1, axis.getLowerMargin(), 0.0001);
    }

    @Test
    public void testSetLowerMarginNegative() {
        axis.setLowerMargin(-0.5);
        assertEquals(-0.5, axis.getLowerMargin(), 0.0001);
    }

    @Test
    public void testSetUpperMargin() {
        axis.setUpperMargin(0.2);
        assertEquals(0.2, axis.getUpperMargin(), 0.0001);
    }

    @Test
    public void testSetUpperMarginNegative() {
        axis.setUpperMargin(-0.3);
        assertEquals(-0.3, axis.getUpperMargin(), 0.0001);
    }

    // ---- Auto range ----

    @Test
    public void testSetAutoRange() {
        axis.setAutoRange(false);
        assertFalse(axis.isAutoRange());
    }

    @Test
    public void testSetAutoRangeTrue() {
        axis.setAutoRange(true);
        assertTrue(axis.isAutoRange());
    }

    @Test
    public void testSetAutoRangeMinimumSize() {
        axis.setAutoRangeMinimumSize(5.0);
        assertEquals(5.0, axis.getAutoRangeMinimumSize(), 0.0001);
    }

    @Test
    public void testSetAutoRangeMinimumSizeNegative() {
        axis.setAutoRangeMinimumSize(-1.0);
        assertEquals(-1.0, axis.getAutoRangeMinimumSize(), 0.0001);
    }

    // ---- Auto range sticky zero ----

    @Test
    public void testSetAutoRangeStickyZero() {
        axis.setAutoRangeStickyZero(false);
        assertFalse(axis.isAutoRangeStickyZero());
    }

    @Test
    public void testSetAutoRangeStickyZeroTrue() {
        axis.setAutoRangeStickyZero(true);
        assertTrue(axis.isAutoRangeStickyZero());
    }

    // ---- Auto range includes zero ----

    @Test
    public void testSetAutoRangeIncludesZero() {
        axis.setAutoRangeIncludesZero(false);
        assertFalse(axis.isAutoRangeIncludesZero());
    }

    @Test
    public void testSetAutoRangeIncludesZeroTrue() {
        axis.setAutoRangeIncludesZero(true);
        assertTrue(axis.isAutoRangeIncludesZero());
    }

    // ---- Inverted ----

    @Test
    public void testSetInverted() {
        axis.setInverted(true);
        assertTrue(axis.isInverted());
    }

    @Test
    public void testSetInvertedFalse() {
        axis.setInverted(false);
        assertFalse(axis.isInverted());
    }

    // ---- Center at ----

    @Test
    public void testSetCenterAt() {
        axis.setCenterAt(50.0);
        assertEquals(50.0, axis.getCenterAt(), 0.0001);
    }

    @Test
    public void testSetCenterAtNull() {
        axis.setCenterAt(null);
        assertNull(axis.getCenterAt());
    }

    // ---- Draw method (basic smoke test) ----

    @Test
    public void testDraw() {
        // Create a simple chart to get a valid Graphics2D and plot area
        XYSeries series = new XYSeries("Test");
        series.add(1.0, 2.0);
        series.add(3.0, 4.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Test Chart", "X", "Y", dataset);
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setLabel("Domain");
        domainAxis.setLabelAngle(Math.PI / 4); // Set a non-zero angle

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);
        try {
            // Draw the axis (this exercises the draw method)
            AxisState state = domainAxis.draw(g2, 0.0, area, area, RectangleEdge.BOTTOM, null);
            assertNotNull(state);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawWithNullInfo() {
        // Test draw with null plotRenderingInfo
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 200, 200);
        try {
            AxisState state = axis.draw(g2, 0.0, area, area, RectangleEdge.LEFT, null);
            assertNotNull(state);
        } finally {
            g2.dispose();
        }
    }

    // ---- Clone ----

    @Test
    public void testClone() throws CloneNotSupportedException {
        axis.setLabel("Original");
        axis.setLabelAngle(1.5);
        NumberAxis cloned = (NumberAxis) axis.clone();
        assertEquals(axis.getLabel(), cloned.getLabel());
        assertEquals(axis.getLabelAngle(), cloned.getLabelAngle(), 0.0001);
        assertTrue(axis != cloned);
    }

    // ---- Equals ----

    @Test
    public void testEquals() {
        NumberAxis a1 = new NumberAxis("Axis");
        NumberAxis a2 = new NumberAxis("Axis");
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        a1.setLabel("Different");
        assertFalse(a1.equals(a2));
        a2.setLabel("Different");
        assertTrue(a1.equals(a2));

        a1.setLabelAngle(0.5);
        assertFalse(a1.equals(a2));
        a2.setLabelAngle(0.5);
        assertTrue(a1.equals(a2));
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse(axis.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        assertFalse(axis.equals("Not an axis"));
    }

    // ---- HashCode ----

    @Test
    public void testHashCode() {
        NumberAxis a1 = new NumberAxis("Axis");
        NumberAxis a2 = new NumberAxis("Axis");
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    // ---- Serialization ----

    @Test
    public void testSerialization() {
        // Basic serialization round-trip
        NumberAxis a1 = new NumberAxis("Serial");
        a1.setLabelAngle(2.0);
        NumberAxis a2 = (NumberAxis) TestUtilities.serialized(a1);
        assertEquals(a1.getLabel(), a2.getLabel());
        assertEquals(a1.getLabelAngle(), a2.getLabelAngle(), 0.0001);
    }

    // ---- Additional edge cases ----

    @Test
    public void testSetLabelInsetsWithNull() {
        axis.setLabelInsets(null);
        assertNull(axis.getLabelInsets());
    }

    @Test
    public void testSetTickLabelInsetsWithNull() {
        axis.setTickLabelInsets(null);
        assertNull(axis.getTickLabelInsets());
    }

    @Test
    public void testSetTickMarkStrokeWithNull() {
        axis.setTickMarkStroke(null);
        assertNull(axis.getTickMarkStroke());
    }

    @Test
    public void testSetTickMarkPaintWithNull() {
        axis.setTickMarkPaint(null);
        assertNull(axis.getTickMarkPaint());
    }

    @Test
    public void testSetAxisLinePaintWithNull() {
        axis.setAxisLinePaint(null);
        assertNull(axis.getAxisLinePaint());
    }

    @Test
    public void testSetAxisLineStrokeWithNull() {
        axis.setAxisLineStroke(null);
        assertNull(axis.getAxisLineStroke());
    }

    @Test
    public void testSetRangeWithNull() {
        axis.setRange(null);
        assertNull(axis.getRange());
    }

    @Test
    public void testSetRangeTypeWithNull() {
        axis.setRangeType(null);
        assertNull(axis.getRangeType());
    }

    @Test
    public void testSetCenterAtWithNull() {
        axis.setCenterAt(null);
        assertNull(axis.getCenterAt());
    }

    @Test
    public void testSetLabelFontWithNull() {
        axis.setLabelFont(null);
        assertNull(axis.getLabelFont());
    }

    @Test
    public void testSetTickLabelFontWithNull() {
        axis.setTickLabelFont(null);
        assertNull(axis.getTickLabelFont());
    }

    @Test
    public void testSetLabelPaintWithNull() {
        axis.setLabelPaint(null);
        assertNull(axis.getLabelPaint());
    }

    @Test
    public void testSetTickLabelPaintWithNull() {
        axis.setTickLabelPaint(null);
        assertNull(axis.getTickLabelPaint());
    }
}