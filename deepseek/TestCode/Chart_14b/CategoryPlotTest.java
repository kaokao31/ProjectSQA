package org.jfree.chart.plot;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CategoryPlot} designed to achieve high code coverage
 * and expose the bug from Defects4J Chart-14 (removeRangeMarker exception).
 */
public class CategoryPlotTest {

    private DefaultCategoryDataset dataset;
    private CategoryAxis domainAxis;
    private NumberAxis rangeAxis;
    private CategoryItemRenderer renderer;
    private CategoryPlot plot;

    @Before
    public void setUp() {
        dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row1", "Col1");
        dataset.addValue(2.0, "Row1", "Col2");
        dataset.addValue(3.0, "Row2", "Col1");
        dataset.addValue(4.0, "Row2", "Col2");

        domainAxis = new CategoryAxis("Domain");
        rangeAxis = new NumberAxis("Range");
        renderer = new BarRenderer();

        plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
    }

    @Test
    public void testConstructor() {
        assertNotNull(plot);
        assertSame(dataset, plot.getDataset());
        assertSame(domainAxis, plot.getDomainAxis());
        assertSame(rangeAxis, plot.getRangeAxis());
        assertSame(renderer, plot.getRenderer());
    }

    @Test
    public void testSetDataset() {
        DefaultCategoryDataset newData = new DefaultCategoryDataset();
        newData.addValue(10.0, "A", "B");
        plot.setDataset(newData);
        assertSame(newData, plot.getDataset());
    }

    @Test
    public void testSetNullDataset() {
        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    @Test
    public void testSetRenderer() {
        CategoryItemRenderer newRenderer = new BarRenderer();
        plot.setRenderer(newRenderer);
        assertSame(newRenderer, plot.getRenderer());
    }

    @Test
    public void testSetNullRenderer() {
        plot.setRenderer(null);
        assertNull(plot.getRenderer());
    }

    @Test
    public void testSetDomainAxis() {
        CategoryAxis newAxis = new CategoryAxis("New Domain");
        plot.setDomainAxis(newAxis);
        assertSame(newAxis, plot.getDomainAxis());
    }

    @Test
    public void testSetRangeAxis() {
        NumberAxis newAxis = new NumberAxis("New Range");
        plot.setRangeAxis(newAxis);
        assertSame(newAxis, plot.getRangeAxis());
    }

    @Test
    public void testGetSeriesCount() {
        assertEquals(2, plot.getSeriesCount());
        dataset.addValue(5.0, "Row3", "Col1");
        assertEquals(3, plot.getSeriesCount());
    }

    @Test
    public void testGetRowCount() {
        assertEquals(2, plot.getRowCount());
    }

    @Test
    public void testGetColumnCount() {
        assertEquals(2, plot.getColumnCount());
    }

    @Test
    public void testIndexOf() {
        assertTrue(plot.indexOf(renderer) >= 0);
        assertTrue(plot.indexOf(domainAxis) >= 0);
        assertTrue(plot.indexOf(rangeAxis) >= 0);
    }

    @Test
    public void testAddRemoveDomainMarker() {
        Marker marker = new ValueMarker(1.0);
        plot.addDomainMarker(marker);
        List<Marker> markers = plot.getDomainMarkers();
        assertNotNull(markers);
        assertTrue(markers.contains(marker));

        boolean removed = plot.removeDomainMarker(marker);
        assertTrue(removed);
        markers = plot.getDomainMarkers();
        assertNull(markers);
    }

    @Test
    public void testAddRemoveRangeMarker() {
        Marker marker = new ValueMarker(2.0);
        plot.addRangeMarker(marker);
        List<Marker> markers = plot.getRangeMarkers();
        assertNotNull(markers);
        assertTrue(markers.contains(marker));

        boolean removed = plot.removeRangeMarker(marker);
        assertTrue(removed);
        markers = plot.getRangeMarkers();
        assertNull(markers);
    }

    // Bug triggering test: remove a range marker that was never added
    // Defects4J Chart-14: removeRangeMarker throws ArrayIndexOutOfBoundsException
    @Test
    public void testRemoveRangeMarkerNotFound() {
        Marker marker = new ValueMarker(5.0);
        // Marker not added, should return false without throwing
        boolean removed = false;
        try {
            removed = plot.removeRangeMarker(marker);
        } catch (Exception e) {
            fail("Should not throw exception when removing non-existing range marker: " + e.getMessage());
        }
        assertFalse(removed);
    }

    // Similarly for domain marker
    @Test
    public void testRemoveDomainMarkerNotFound() {
        Marker marker = new ValueMarker(5.0);
        boolean removed = false;
        try {
            removed = plot.removeDomainMarker(marker);
        } catch (Exception e) {
            fail("Should not throw exception when removing non-existing domain marker: " + e.getMessage());
        }
        assertFalse(removed);
    }

    @Test
    public void testClearRangeMarkers() {
        Marker m1 = new ValueMarker(1.0);
        Marker m2 = new ValueMarker(2.0);
        plot.addRangeMarker(m1);
        plot.addRangeMarker(m2);
        assertEquals(2, plot.getRangeMarkers().size());

        plot.clearRangeMarkers();
        assertNull(plot.getRangeMarkers());
    }

    @Test
    public void testClearDomainMarkers() {
        Marker m1 = new ValueMarker(1.0);
        Marker m2 = new ValueMarker(2.0);
        plot.addDomainMarker(m1);
        plot.addDomainMarker(m2);
        assertEquals(2, plot.getDomainMarkers().size());

        plot.clearDomainMarkers();
        assertNull(plot.getDomainMarkers());
    }

    @Test
    public void testAddRangeMarkerWithLayer() {
        Marker marker = new ValueMarker(3.0);
        plot.addRangeMarker(marker, org.jfree.chart.util.Layer.FOREGROUND);
        List<Marker> markers = plot.getRangeMarkers(org.jfree.chart.util.Layer.FOREGROUND);
        assertNotNull(markers);
        assertTrue(markers.contains(marker));
    }

    @Test
    public void testAddDomainMarkerWithLayer() {
        Marker marker = new ValueMarker(3.0);
        plot.addDomainMarker(marker, org.jfree.chart.util.Layer.BACKGROUND);
        List<Marker> markers = plot.getDomainMarkers(org.jfree.chart.util.Layer.BACKGROUND);
        assertNotNull(markers);
        assertTrue(markers.contains(marker));
    }

    @Test
    public void testGetRangeAxisForDataset() {
        assertSame(rangeAxis, plot.getRangeAxisForDataset(0));
    }

    @Test
    public void testGetDomainAxisForDataset() {
        assertSame(domainAxis, plot.getDomainAxisForDataset(0));
    }

    @Test
    public void testGetLegendItems() {
        assertNotNull(plot.getLegendItems());
    }

    @Test
    public void testSetRangeCrosshairValue() {
        plot.setRangeCrosshairValue(2.5);
        assertEquals(2.5, plot.getRangeCrosshairValue(), 0.0001);
    }

    @Test
    public void testSetDomainCrosshairValue() {
        plot.setDomainCrosshairValue(1.5);
        assertEquals(1.5, plot.getDomainCrosshairValue(), 0.0001);
    }

    @Test
    public void testSetRangeCrosshairVisible() {
        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(false);
        assertFalse(plot.isRangeCrosshairVisible());
    }

    @Test
    public void testSetDomainCrosshairVisible() {
        plot.setDomainCrosshairVisible(true);
        assertTrue(plot.isDomainCrosshairVisible());
        plot.setDomainCrosshairVisible(false);
        assertFalse(plot.isDomainCrosshairVisible());
    }

    @Test
    public void testGetDataRange() {
        assertNotNull(plot.getDataRange(rangeAxis));
    }

    @Test
    public void testGetRangeAxisIndex() {
        // Index 0 by default
        assertTrue(plot.getRangeAxisIndex(rangeAxis) >= 0);
    }

    @Test
    public void testSetSeriesToForeground() {
        // simple call, no exception
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToDomainAxis(0, 0);
    }

    @Test
    public void testDatasetOrder() {
        assertEquals(DatasetSelectionOrder.FORWARD, plot.getDatasetOrder());
    }

    @Test
    public void testSetDatasetOrder() {
        plot.setDatasetOrder(DatasetSelectionOrder.REVERSE);
        assertEquals(DatasetSelectionOrder.REVERSE, plot.getDatasetOrder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullRangeMarker() {
        plot.addRangeMarker(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullDomainMarker() {
        plot.addDomainMarker(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullRangeMarker() {
        plot.removeRangeMarker(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullDomainMarker() {
        plot.removeDomainMarker(null);
    }

    @Test
    public void testMultipleDatasets() {
        DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
        dataset2.addValue(100.0, "RowX", "ColY");
        plot.setDataset(1, dataset2);
        assertSame(dataset2, plot.getDataset(1));
    }

    @Test
    public void testGetRendererCount() {
        assertEquals(1, plot.getRendererCount());
    }

    @Test
    public void testGetRendererForDataset() {
        assertSame(renderer, plot.getRendererForDataset(dataset));
        assertNull(plot.getRendererForDataset(null));
    }

    @Test
    public void testSetRendererWithIndex() {
        CategoryItemRenderer r2 = new BarRenderer();
        plot.setRenderer(1, r2);
        assertSame(r2, plot.getRenderer(1));
    }
}