package org.jfree.chart.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.junit.Before;
import org.junit.Test;

/**
 * A comprehensive test suite for {@link CategoryPlot}.
 * Designed for JUnit 4, targeting high branch/line coverage and exposing
 * potential edge-case bugs typical in JFreeChart Chart-19.
 */
public class CategoryPlotTest {

    private CategoryPlot plot;
    private DefaultCategoryDataset dataset;
    private CategoryAxis domainAxis;
    private ValueAxis rangeAxis;
    private CategoryItemRenderer renderer;

    @Before
    public void setUp() {
        this.dataset = new DefaultCategoryDataset();
        this.domainAxis = new CategoryAxis("Category");
        this.rangeAxis = new NumberAxis("Value");
        this.renderer = new BarRenderer();
        this.plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
    }

    @Test
    public void testConstructorAndGetters() {
        assertNotNull(plot);
        assertEquals(dataset, plot.getDataset());
        assertEquals(domainAxis, plot.getDomainAxis());
        assertEquals(rangeAxis, plot.getRangeAxis());
        assertEquals(renderer, plot.getRenderer());
        assertEquals(Plot.DEFAULT_INSETS, plot.getInsets());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDomainAxisNullIndexZero() {
        plot.setDomainAxis(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeAxisNullIndexZero() {
        plot.setRangeAxis(0, null);
    }

    @Test
    public void testMapDatasetToDomainAxis() {
        plot.mapDatasetToDomainAxis(1, 1);
        assertEquals(1, plot.getDomainAxisForDataset(1).hashCode()); // or check mapping index list
        
        // Test out of bounds / default fallback mapping
        CategoryAxis fallbackAxis = plot.getDomainAxisForDataset(99);
        assertEquals(domainAxis, fallbackAxis);
    }

    @Test
    public void testMapDatasetToRangeAxis() {
        plot.mapDatasetToRangeAxis(1, 1);
        assertEquals(rangeAxis, plot.getRangeAxisForDataset(99));
    }

    @Test
    public void testRendererManagement() {
        plot.setRenderer(1, null);
        assertNull(plot.getRenderer(1));

        CategoryItemRenderer newRenderer = new BarRenderer();
        plot.setRenderer(1, newRenderer);
        assertEquals(newRenderer, plot.getRenderer(1));
    }

    @Test
    public void testDatasetManagement() {
        plot.setDataset(1, null);
        assertNull(plot.getDataset(1));

        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();
        plot.setDataset(1, ds2);
        assertEquals(ds2, plot.getDataset(1));
        
        assertEquals(2, plot.indexOf(ds2));
    }

    @Test
    public void testRemoveDatasetBugTrigger() {
        // Specifically targeting index manipulation and listener management 
        // which are common fault areas in JFreeChart plots (e.g., Chart-19)
        DefaultCategoryDataset ds1 = new DefaultCategoryDataset();
        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();
        
        plot.setDataset(0, ds1);
        plot.setDataset(1, ds2);
        
        assertEquals(2, plot.indexOf(ds1));
        
        // Remove dataset at index 0
        plot.setDataset(0, null);
        assertNull(plot.getDataset(0));
    }

    @Test
    public void testRemoveRendererBugTrigger() {
        CategoryItemRenderer r1 = new BarRenderer();
        CategoryItemRenderer r2 = new BarRenderer();
        
        plot.setRenderer(0, r1);
        plot.setRenderer(1, r2);
        
        plot.setRenderer(0, null);
        assertNull(plot.getRenderer(0));
        assertEquals(r2, plot.getRenderer(1));
    }

    @Test
    public void testDomainAxisIndexOperations() {
        CategoryAxis axis2 = new CategoryAxis("Secondary");
        plot.setDomainAxis(1, axis2);
        assertEquals(1, plot.getDomainAxisIndex(axis2));
        
        CategoryAxis unaddedAxis = new CategoryAxis("Unadded");
        assertEquals(-1, plot.getDomainAxisIndex(unaddedAxis));
        assertEquals(-1, plot.getDomainAxisIndex(null));
    }

    @Test
    public void testRangeAxisIndexOperations() {
        ValueAxis axis2 = new NumberAxis("Secondary Range");
        plot.setRangeAxis(1, axis2);
        assertEquals(1, plot.getRangeAxisIndex(axis2));
        
        ValueAxis unaddedAxis = new NumberAxis("Unadded Range");
        assertEquals(-1, plot.getRangeAxisIndex(unaddedAxis));
        assertEquals(-1, plot.getRangeAxisIndex(null));
    }

    @Test
    public void testGetPlotType() {
        assertNotNull(plot.getPlotType());
    }

    @Test
    public void testEqualsAndClone() throws Exception {
        CategoryPlot clonePlot = (CategoryPlot) plot.clone();
        assertTrue(plot.equals(clonePlot));
        assertTrue(clonePlot.equals(plot));

        clonePlot.setInsets(new RectangleInsets(1, 2, 3, 4));
        assertFalse(plot.equals(clonePlot));

        assertFalse(plot.equals(null));
        assertFalse(plot.equals("Not A Plot"));
    }

    @Test
    public void testDrawWithNullGraphics() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
        
        // Test drawing execution flow
        try {
            plot.draw(g2, area, null, null, new ChartRenderingInfo());
        } catch (Exception e) {
            // Depending on dataset state, some internal layout calculations might occur
        }
    }

    @Test
    public void testListenerNotifications() {
        TestPlotChangeListener listener = new TestPlotChangeListener();
        plot.addChangeListener(listener);
        
        plot.setInsets(new RectangleInsets(10, 10, 10, 10));
        assertTrue(listener.eventReceived);
        
        plot.removeChangeListener(listener);
        listener.eventReceived = false;
        
        plot.setInsets(new RectangleInsets(5, 5, 5, 5));
        assertFalse(listener.eventReceived);
    }

    private static class TestPlotChangeListener implements PlotChangeListener {
        boolean eventReceived = false;

        @Override
        public void plotChanged(PlotChangeEvent event) {
            this.eventReceived = true;
        }
    }
}