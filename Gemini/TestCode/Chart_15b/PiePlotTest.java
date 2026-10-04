package org.jfree.chart.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.junit.Before;
import org.junit.Test;

public class PiePlotTest {

    private PiePlot plot;
    private DefaultPieDataset dataset;

    @Before
    public void setUp() {
        dataset = new DefaultPieDataset();
        dataset.setValue("Key 1", 10.0);
        dataset.setValue("Key 2", 20.0);
        dataset.setValue("Key 3", 30.0);
        
        plot = new PiePlot(dataset);
    }

    @Test
    public void testConstructorsAndDefaults() {
        PiePlot defaultPlot = new PiePlot();
        assertNull(defaultPlot.getDataset());
        
        PiePlot datasetPlot = new PiePlot(dataset);
        assertEquals(dataset, datasetPlot.getDataset());
        
        assertEquals(0.20, plot.getStartAngle(), 0.001);
        assertEquals(true, plot.getCircular());
        assertEquals(0.0, plot.getInteriorGap(), 0.001);
        assertEquals(0.8, plot.getMaximumLabelWidth(), 0.001);
        assertEquals(0.1, plot.getLabelGap(), 0.001);
    }

    @Test
    public void testSettersAndGetters() {
        plot.setStartAngle(45.0);
        assertEquals(45.0, plot.getStartAngle(), 0.001);

        plot.setDirection(Rotation.ANTICLOCKWISE);
        assertEquals(Rotation.ANTICLOCKWISE, plot.getDirection());

        plot.setInteriorGap(0.15);
        assertEquals(0.15, plot.getInteriorGap(), 0.001);

        plot.setCircular(false);
        assertFalse(plot.getCircular());
        plot.setCircular(true, true);
        assertTrue(plot.getCircular());

        plot.setIgnoreNullValues(true);
        assertTrue(plot.getIgnoreNullValues());

        plot.setIgnoreZeroValues(true);
        assertTrue(plot.getIgnoreZeroValues());

        plot.setSectionPaint("Key 1", Color.RED);
        assertEquals(Color.RED, plot.getSectionPaint("Key 1"));

        plot.setSectionOutlinePaint("Key 1", Color.BLUE);
        assertEquals(Color.BLUE, plot.getSectionOutlinePaint("Key 1"));

        plot.setSectionOutlineStroke("Key 1", plot.getSectionOutlineStroke());
        assertNotNull(plot.getSectionOutlineStroke("Key 1"));

        plot.setShadowPaint(Color.GRAY);
        assertEquals(Color.GRAY, plot.getShadowPaint());

        plot.setShadowXOffset(5.0);
        assertEquals(5.0, plot.getShadowXOffset(), 0.001);

        plot.setShadowYOffset(6.0);
        assertEquals(6.0, plot.getShadowYOffset(), 0.001);

        plot.setExplodePercent("Key 1", 0.1);
        assertEquals(0.1, plot.getExplodePercent("Key 1"), 0.001);

        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        assertEquals(new Font("SansSerif", Font.PLAIN, 12), plot.getLabelFont());

        plot.setLabelPaint(Color.BLACK);
        assertEquals(Color.BLACK, plot.getLabelPaint());

        plot.setLabelBackgroundPaint(Color.WHITE);
        assertEquals(Color.WHITE, plot.getLabelBackgroundPaint());

        plot.setLabelOutlinePaint(Color.BLACK);
        assertEquals(Color.BLACK, plot.getLabelOutlinePaint());

        plot.setLabelOutlineStroke(plot.getLabelOutlineStroke());
        assertNotNull(plot.getLabelOutlineStroke());

        plot.setLabelShadowPaint(Color.GRAY);
        assertEquals(Color.GRAY, plot.getLabelShadowPaint());

        plot.setLabelLinksVisible(false);
        assertFalse(plot.getLabelLinksVisible());

        plot.setLabelLinkStyle(PieSectionLabelLinkStyle.STANDARD);
        assertEquals(PieSectionLabelLinkStyle.STANDARD, plot.getLabelLinkStyle());

        plot.setLabelLinkPaint(Color.RED);
        assertEquals(Color.RED, plot.getLabelLinkPaint());

        plot.setLabelLinkStroke(plot.getLabelLinkStroke());
        assertNotNull(plot.getLabelLinkStroke());

        plot.setMaximumLabelWidth(0.5);
        assertEquals(0.5, plot.getMaximumLabelWidth(), 0.001);

        plot.setLabelGap(0.05);
        assertEquals(0.05, plot.getLabelGap(), 0.05);

        plot.setLabelGenerator(null);
        assertNull(plot.getLabelGenerator());

        plot.setToolTipGenerator(null);
        assertNull(plot.getToolTipGenerator());

        plot.setURLGenerator(null);
        assertNull(plot.getURLGenerator());

        plot.setMinimumPieAngle(Math.PI / 6);
        assertEquals(Math.PI / 6, plot.getMinimumPieAngle(), 0.001);

        plot.setSimpleLabels(true);
        assertTrue(plot.getSimpleLabels());

        plot.setSimpleLabelOffset(plot.getSimpleLabelOffset());
        assertNotNull(plot.getSimpleLabelOffset());

        plot.setAutoPopulateSectionPaint(false);
        assertFalse(plot.autoPopulateSectionPaint());

        plot.setAutoPopulateSectionOutlinePaint(true);
        assertTrue(plot.autoPopulateSectionOutlinePaint());

        plot.setAutoPopulateSectionOutlineStroke(false);
        assertFalse(plot.autoPopulateSectionOutlineStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInteriorGapInvalid() {
        plot.setInteriorGap(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInteriorGapInvalidMax() {
        plot.setInteriorGap(0.55);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStartAngleNull() {
        plot.setDirection(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetExplodePercentNullKey() {
        plot.setExplodePercent(null, 0.1);
    }

    @Test
    public void testGetLegendItems() {
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull(items);
        assertEquals(3, items.getItemCount());

        // Test with ignore null/zero
        dataset.setValue("Key 4", null);
        dataset.setValue("Key 5", 0.0);
        plot.setIgnoreNullValues(true);
        plot.setIgnoreZeroValues(true);
        
        LegendItemCollection itemsFiltered = plot.getLegendItems();
        assertNotNull(itemsFiltered);
        assertEquals(3, itemsFiltered.getItemCount());
    }

    @Test
    public void testDrawAndRendering() {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);
        ChartRenderingInfo info = new ChartRenderingInfo();

        plot.draw(g2, area, null, null, info);
        assertNotNull(info.getPlotInfo());
    }

    @Test
    public void testDrawWithNullDataset() {
        PiePlot emptyPlot = new PiePlot(null);
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);
        
        emptyPlot.draw(g2, area, null, null, null);
        assertTrue(true); // Should handle gracefully without exception
    }

    @Test
    public void testEqualityAndCloning() throws CloneNotSupportedException {
        PiePlot plot1 = new PiePlot(dataset);
        PiePlot plot2 = new PiePlot(dataset);

        assertTrue(plot1.equals(plot2));
        
        plot2.setStartAngle(90.0);
        assertFalse(plot1.equals(plot2));

        PiePlot cloned = (PiePlot) plot1.clone();
        assertTrue(plot1.equals(cloned));
    }

    @Test
    public void testGetPlotType() {
        assertNotNull(plot.getPlotType());
    }

    @Test
    public void testLookupSectionPaint() {
        assertNotNull(plot.lookupSectionPaint("Key 1"));
        assertNotNull(plot.lookupSectionPaint("Key 1", true));
        assertNotNull(plot.lookupSectionPaint("Key 1", false));
    }

    @Test
    public void testLookupSectionOutlinePaint() {
        assertNotNull(plot.lookupSectionOutlinePaint("Key 1"));
        assertNotNull(plot.lookupSectionOutlinePaint("Key 1", true));
        assertNotNull(plot.lookupSectionOutlinePaint("Key 1", false));
    }

    @Test
    public void testLookupSectionOutlineStroke() {
        assertNotNull(plot.lookupSectionOutlineStroke("Key 1"));
        assertNotNull(plot.lookupSectionOutlineStroke("Key 1", true));
        assertNotNull(plot.lookupSectionOutlineStroke("Key 1", false));
    }

    @Test
    public void testGetMaximumExplodePercent() {
        plot.setExplodePercent("Key 1", 0.2);
        plot.setExplodePercent("Key 2", 0.4);
        assertEquals(0.4, plot.getMaximumExplodePercent(), 0.001);
    }
}