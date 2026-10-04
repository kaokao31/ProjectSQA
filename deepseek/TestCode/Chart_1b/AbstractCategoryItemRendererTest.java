package org.jfree.chart.renderer.category;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Test class for AbstractCategoryItemRenderer.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class AbstractCategoryItemRendererTest {

    private DefaultCategoryDataset dataset;
    private CategoryPlot plot;
    private AbstractCategoryItemRenderer renderer;

    @Before
    public void setUp() {
        dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row1", "Col1");
        dataset.addValue(2.0, "Row1", "Col2");
        dataset.addValue(3.0, "Row2", "Col1");
        dataset.addValue(4.0, "Row2", "Col2");
        plot = new CategoryPlot(dataset, null, null, null);
        renderer = new LineAndShapeRenderer(); // concrete subclass for testing
        plot.setRenderer(renderer);
    }

    @Test
    public void testGetItemVisible() {
        assertTrue(renderer.getItemVisible(0, 0));
        assertTrue(renderer.getItemVisible(1, 1));
        // test with null dataset
        plot.setDataset(null);
        assertFalse(renderer.getItemVisible(0, 0));
    }

    @Test
    public void testGetRowCount() {
        assertEquals(2, renderer.getRowCount());
        // test with null dataset
        plot.setDataset(null);
        assertEquals(0, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCount() {
        assertEquals(2, renderer.getColumnCount());
        plot.setDataset(null);
        assertEquals(0, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKey() {
        assertEquals("Row1", renderer.getRowKey(0));
        assertEquals("Row2", renderer.getRowKey(1));
        // test with null dataset
        plot.setDataset(null);
        assertNull(renderer.getRowKey(0));
    }

    @Test
    public void testGetColumnKey() {
        assertEquals("Col1", renderer.getColumnKey(0));
        assertEquals("Col2", renderer.getColumnKey(1));
        plot.setDataset(null);
        assertNull(renderer.getColumnKey(0));
    }

    @Test
    public void testGetRowIndex() {
        assertEquals(0, renderer.getRowIndex("Row1"));
        assertEquals(1, renderer.getRowIndex("Row2"));
        // test with unknown key
        assertEquals(-1, renderer.getRowIndex("Unknown"));
    }

    @Test
    public void testGetColumnIndex() {
        assertEquals(0, renderer.getColumnIndex("Col1"));
        assertEquals(1, renderer.getColumnIndex("Col2"));
        assertEquals(-1, renderer.getColumnIndex("Unknown"));
    }

    @Test
    public void testGetItem() {
        assertEquals(1.0, renderer.getItem(0, 0).doubleValue(), 0.0001);
        assertEquals(4.0, renderer.getItem(1, 1).doubleValue(), 0.0001);
        // test with null dataset
        plot.setDataset(null);
        assertNull(renderer.getItem(0, 0));
    }

    @Test
    public void testGetLegendItem() {
        LegendItem item = renderer.getLegendItem(0, 0);
        assertNotNull(item);
        assertEquals("Row1", item.getLabel());
        // test with null dataset
        plot.setDataset(null);
        assertNull(renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetLegendItems() {
        List<LegendItem> items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals("Row1", items.get(0).getLabel());
        assertEquals("Row2", items.get(1).getLabel());
    }

    @Test
    public void testDrawItem() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);
        // test with valid indices
        renderer.drawItem(g2, null, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, null, 0);
        // test with null dataset
        plot.setDataset(null);
        renderer.drawItem(g2, null, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), null, 0, 0, null, 0);
        g2.dispose();
    }

    @Test
    public void testSetSeriesVisible() {
        renderer.setSeriesVisible(0, Boolean.FALSE);
        assertFalse(renderer.getSeriesVisible(0));
        assertTrue(renderer.getSeriesVisible(1));
        // test with null flag
        renderer.setSeriesVisible(0, null);
        assertTrue(renderer.getSeriesVisible(0));
    }

    @Test
    public void testGetSeriesVisible() {
        assertTrue(renderer.getSeriesVisible(0));
        assertTrue(renderer.getSeriesVisible(1));
        // test with out-of-bounds index
        assertTrue(renderer.getSeriesVisible(2));
    }

    @Test
    public void testSetSeriesVisibleInLegend() {
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        assertFalse(renderer.getSeriesVisibleInLegend(0));
        assertTrue(renderer.getSeriesVisibleInLegend(1));
        renderer.setSeriesVisibleInLegend(0, null);
        assertTrue(renderer.getSeriesVisibleInLegend(0));
    }

    @Test
    public void testGetSeriesVisibleInLegend() {
        assertTrue(renderer.getSeriesVisibleInLegend(0));
        assertTrue(renderer.getSeriesVisibleInLegend(1));
        assertTrue(renderer.getSeriesVisibleInLegend(2));
    }

    @Test
    public void testSetBaseSeriesVisible() {
        renderer.setBaseSeriesVisible(false);
        assertFalse(renderer.getBaseSeriesVisible());
        renderer.setBaseSeriesVisible(true);
        assertTrue(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testSetBaseSeriesVisibleInLegend() {
        renderer.setBaseSeriesVisibleInLegend(false);
        assertFalse(renderer.getBaseSeriesVisibleInLegend());
        renderer.setBaseSeriesVisibleInLegend(true);
        assertTrue(renderer.getBaseSeriesVisibleInLegend());
    }

    @Test
    public void testGetItemCount() {
        assertEquals(2, renderer.getItemCount(0));
        assertEquals(2, renderer.getItemCount(1));
        // test with null dataset
        plot.setDataset(null);
        assertEquals(0, renderer.getItemCount(0));
    }

    @Test
    public void testGetItemCountWithNullDataset() {
        plot.setDataset(null);
        assertEquals(0, renderer.getItemCount(0));
    }

    @Test
    public void testGetRowCountWithNullDataset() {
        plot.setDataset(null);
        assertEquals(0, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCountWithNullDataset() {
        plot.setDataset(null);
        assertEquals(0, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKeyWithNullDataset() {
        plot.setDataset(null);
        assertNull(renderer.getRowKey(0));
    }

    @Test
    public void testGetColumnKeyWithNullDataset() {
        plot.setDataset(null);
        assertNull(renderer.getColumnKey(0));
    }

    @Test
    public void testGetItemWithNullDataset() {
        plot.setDataset(null);
        assertNull(renderer.getItem(0, 0));
    }

    @Test
    public void testGetLegendItemWithNullDataset() {
        plot.setDataset(null);
        assertNull(renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetLegendItemsWithNullDataset() {
        plot.setDataset(null);
        List<LegendItem> items = renderer.getLegendItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetItemVisibleWithNullDataset() {
        plot.setDataset(null);
        assertFalse(renderer.getItemVisible(0, 0));
    }

    @Test
    public void testGetItemVisibleWithNegativeIndex() {
        assertFalse(renderer.getItemVisible(-1, 0));
        assertFalse(renderer.getItemVisible(0, -1));
    }

    @Test
    public void testGetItemVisibleWithOutOfBoundsIndex() {
        assertFalse(renderer.getItemVisible(10, 0));
        assertFalse(renderer.getItemVisible(0, 10));
    }

    @Test
    public void testGetRowCountWithNegativeIndex() {
        // rowCount should not be affected by index
        assertEquals(2, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCountWithNegativeIndex() {
        assertEquals(2, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKeyWithNegativeIndex() {
        assertNull(renderer.getRowKey(-1));
    }

    @Test
    public void testGetColumnKeyWithNegativeIndex() {
        assertNull(renderer.getColumnKey(-1));
    }

    @Test
    public void testGetRowIndexWithNullKey() {
        assertEquals(-1, renderer.getRowIndex(null));
    }

    @Test
    public void testGetColumnIndexWithNullKey() {
        assertEquals(-1, renderer.getColumnIndex(null));
    }

    @Test
    public void testGetItemWithNegativeIndex() {
        assertNull(renderer.getItem(-1, 0));
        assertNull(renderer.getItem(0, -1));
    }

    @Test
    public void testGetItemWithOutOfBoundsIndex() {
        assertNull(renderer.getItem(10, 0));
        assertNull(renderer.getItem(0, 10));
    }

    @Test
    public void testGetLegendItemWithNegativeIndex() {
        assertNull(renderer.getLegendItem(-1, 0));
    }

    @Test
    public void testGetLegendItemWithOutOfBoundsIndex() {
        assertNull(renderer.getLegendItem(10, 0));
    }

    @Test
    public void testSetSeriesVisibleWithNegativeIndex() {
        renderer.setSeriesVisible(-1, Boolean.FALSE);
        // should not throw exception
    }

    @Test
    public void testGetSeriesVisibleWithNegativeIndex() {
        assertTrue(renderer.getSeriesVisible(-1));
    }

    @Test
    public void testSetSeriesVisibleInLegendWithNegativeIndex() {
        renderer.setSeriesVisibleInLegend(-1, Boolean.FALSE);
    }

    @Test
    public void testGetSeriesVisibleInLegendWithNegativeIndex() {
        assertTrue(renderer.getSeriesVisibleInLegend(-1));
    }

    @Test
    public void testGetItemCountWithNegativeIndex() {
        assertEquals(0, renderer.getItemCount(-1));
    }

    @Test
    public void testGetItemCountWithOutOfBoundsIndex() {
        assertEquals(0, renderer.getItemCount(10));
    }

    @Test
    public void testDrawItemWithNullGraphics() {
        try {
            renderer.drawItem(null, null, new Rectangle2D.Double(), plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, null, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testDrawItemWithNullDataArea() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, null, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, null, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        g2.dispose();
    }

    @Test
    public void testDrawItemWithNullPlot() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(), null, null, null, dataset, 0, 0, null, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        g2.dispose();
    }

    @Test
    public void testDrawItemWithNullDataset() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(), plot, plot.getDomainAxis(), plot.getRangeAxis(), null, 0, 0, null, 0);
            // should not throw exception, just return
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        g2.dispose();
    }

    @Test
    public void testDrawItemWithNegativeIndex() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(), plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, -1, 0, null, 0);
            // should not throw exception
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        g2.dispose();
    }

    @Test
    public void testDrawItemWithOutOfBoundsIndex() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(), plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 10, 0, null, 0);
            // should not throw exception
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        g2.dispose();
    }

    @Test
    public void testGetLegendItemWithNullPlot() {
        plot.setRenderer(null);
        assertNull(renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetLegendItemsWithNullPlot() {
        plot.setRenderer(null);
        List<LegendItem> items = renderer.getLegendItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetItemVisibleWithNullPlot() {
        plot.setRenderer(null);
        assertFalse(renderer.getItemVisible(0, 0));
    }

    @Test
    public void testGetRowCountWithNullPlot() {
        plot.setRenderer(null);
        assertEquals(0, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCountWithNullPlot() {
        plot.setRenderer(null);
        assertEquals(0, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKeyWithNullPlot() {
        plot.setRenderer(null);
        assertNull(renderer.getRowKey(0));
    }

    @Test
    public void testGetColumnKeyWithNullPlot() {
        plot.setRenderer(null);
        assertNull(renderer.getColumnKey(0));
    }

    @Test
    public void testGetItemWithNullPlot() {
        plot.setRenderer(null);
        assertNull(renderer.getItem(0, 0));
    }

    @Test
    public void testGetItemCountWithNullPlot() {
        plot.setRenderer(null);
        assertEquals(0, renderer.getItemCount(0));
    }

    @Test
    public void testGetRowIndexWithNullPlot() {
        plot.setRenderer(null);
        assertEquals(-1, renderer.getRowIndex("Row1"));
    }

    @Test
    public void testGetColumnIndexWithNullPlot() {
        plot.setRenderer(null);
        assertEquals(-1, renderer.getColumnIndex("Col1"));
    }

    @Test
    public void testSetSeriesVisibleWithNullPlot() {
        plot.setRenderer(null);
        renderer.setSeriesVisible(0, Boolean.FALSE);
        // should not throw exception
    }

    @Test
    public void testGetSeriesVisibleWithNullPlot() {
        plot.setRenderer(null);
        assertTrue(renderer.getSeriesVisible(0));
    }

    @Test
    public void testSetSeriesVisibleInLegendWithNullPlot() {
        plot.setRenderer(null);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
    }

    @Test
    public void testGetSeriesVisibleInLegendWithNullPlot() {
        plot.setRenderer(null);
        assertTrue(renderer.getSeriesVisibleInLegend(0));
    }

    @Test
    public void testSetBaseSeriesVisibleWithNullPlot() {
        plot.setRenderer(null);
        renderer.setBaseSeriesVisible(false);
        assertFalse(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testGetBaseSeriesVisibleWithNullPlot() {
        plot.setRenderer(null);
        assertTrue(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testSetBaseSeriesVisibleInLegendWithNullPlot() {
        plot.setRenderer(null);
        renderer.setBaseSeriesVisibleInLegend(false);
        assertFalse(renderer.getBaseSeriesVisibleInLegend());
    }

    @Test
    public void testGetBaseSeriesVisibleInLegendWithNullPlot() {
        plot.setRenderer(null);
        assertTrue(renderer.getBaseSeriesVisibleInLegend());
    }

    @Test
    public void testGetLegendItemWithNullSeries() {
        // create a dataset with no rows
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertNull(renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetLegendItemsWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        List<LegendItem> items = renderer.getLegendItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetItemVisibleWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertFalse(renderer.getItemVisible(0, 0));
    }

    @Test
    public void testGetRowCountWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertEquals(0, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCountWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertEquals(0, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKeyWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertNull(renderer.getRowKey(0));
    }

    @Test
    public void testGetColumnKeyWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertNull(renderer.getColumnKey(0));
    }

    @Test
    public void testGetItemWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertNull(renderer.getItem(0, 0));
    }

    @Test
    public void testGetItemCountWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertEquals(0, renderer.getItemCount(0));
    }

    @Test
    public void testGetRowIndexWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertEquals(-1, renderer.getRowIndex("Row1"));
    }

    @Test
    public void testGetColumnIndexWithEmptyDataset() {
        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        plot.setDataset(emptyDataset);
        assertEquals(-1, renderer.getColumnIndex("Col1"));
    }

    @Test
    public void testGetLegendItemWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetLegendItemsWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        List<LegendItem> items = renderer.getLegendItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetItemVisibleWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertFalse(renderer.getItemVisible(0, 0));
    }

    @Test
    public void testGetRowCountWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCountWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKeyWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getRowKey(0));
    }

    @Test
    public void testGetColumnKeyWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getColumnKey(0));
    }

    @Test
    public void testGetItemWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getItem(0, 0));
    }

    @Test
    public void testGetItemCountWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getItemCount(0));
    }

    @Test
    public void testGetRowIndexWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(-1, renderer.getRowIndex("Row1"));
    }

    @Test
    public void testGetColumnIndexWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(-1, renderer.getColumnIndex("Col1"));
    }

    @Test
    public void testSetSeriesVisibleWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setSeriesVisible(0, Boolean.FALSE);
    }

    @Test
    public void testGetSeriesVisibleWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getSeriesVisible(0));
    }

    @Test
    public void testSetSeriesVisibleInLegendWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
    }

    @Test
    public void testGetSeriesVisibleInLegendWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getSeriesVisibleInLegend(0));
    }

    @Test
    public void testSetBaseSeriesVisibleWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setBaseSeriesVisible(false);
        assertFalse(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testGetBaseSeriesVisibleWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testSetBaseSeriesVisibleInLegendWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setBaseSeriesVisibleInLegend(false);
        assertFalse(renderer.getBaseSeriesVisibleInLegend());
    }

    @Test
    public void testGetBaseSeriesVisibleInLegendWithNullPlotAndNullDataset() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getBaseSeriesVisibleInLegend());
    }

    @Test
    public void testDrawItemWithNullDomainAxis() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(), plot, null, plot.getRangeAxis(), dataset, 0, 0, null, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        g2.dispose();
    }

    @Test
    public void testDrawItemWithNullRangeAxis() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(), plot, plot.getDomainAxis(), null, dataset, 0, 0, null, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        g2.dispose();
    }

    @Test
    public void testDrawItemWithNullState() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(), plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, null, 0);
            // should not throw exception
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        g2.dispose();
    }

    @Test
    public void testDrawItemWithNullPass() {
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        try {
            renderer.drawItem(g2, null, new Rectangle2D.Double(), plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, null, 0);
            // should not throw exception
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        g2.dispose();
    }

    @Test
    public void testGetLegendItemWithNullSeriesKey() {
        // dataset with null row key
        DefaultCategoryDataset datasetWithNullKey = new DefaultCategoryDataset();
        datasetWithNullKey.addValue(1.0, null, "Col1");
        plot.setDataset(datasetWithNullKey);
        LegendItem item = renderer.getLegendItem(0, 0);
        assertNull(item);
    }

    @Test
    public void testGetLegendItemsWithNullSeriesKey() {
        DefaultCategoryDataset datasetWithNullKey = new DefaultCategoryDataset();
        datasetWithNullKey.addValue(1.0, null, "Col1");
        plot.setDataset(datasetWithNullKey);
        List<LegendItem> items = renderer.getLegendItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetItemWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertNull(renderer.getItem(0, 0));
    }

    @Test
    public void testGetItemVisibleWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertTrue(renderer.getItemVisible(0, 0));
    }

    @Test
    public void testGetRowCountWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertEquals(1, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCountWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertEquals(1, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKeyWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertEquals("Row1", renderer.getRowKey(0));
    }

    @Test
    public void testGetColumnKeyWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertEquals("Col1", renderer.getColumnKey(0));
    }

    @Test
    public void testGetItemCountWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertEquals(1, renderer.getItemCount(0));
    }

    @Test
    public void testGetRowIndexWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertEquals(0, renderer.getRowIndex("Row1"));
    }

    @Test
    public void testGetColumnIndexWithNullValue() {
        DefaultCategoryDataset datasetWithNull = new DefaultCategoryDataset();
        datasetWithNull.addValue(null, "Row1", "Col1");
        plot.setDataset(datasetWithNull);
        assertEquals(0, renderer.getColumnIndex("Col1"));
    }

    @Test
    public void testGetLegendItemWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetLegendItemsWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        List<LegendItem> items = renderer.getLegendItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetItemVisibleWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertFalse(renderer.getItemVisible(0, 0));
    }

    @Test
    public void testGetRowCountWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCountWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKeyWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getRowKey(0));
    }

    @Test
    public void testGetColumnKeyWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getColumnKey(0));
    }

    @Test
    public void testGetItemWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getItem(0, 0));
    }

    @Test
    public void testGetItemCountWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getItemCount(0));
    }

    @Test
    public void testGetRowIndexWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(-1, renderer.getRowIndex("Row1"));
    }

    @Test
    public void testGetColumnIndexWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(-1, renderer.getColumnIndex("Col1"));
    }

    @Test
    public void testSetSeriesVisibleWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setSeriesVisible(0, Boolean.FALSE);
    }

    @Test
    public void testGetSeriesVisibleWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getSeriesVisible(0));
    }

    @Test
    public void testSetSeriesVisibleInLegendWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
    }

    @Test
    public void testGetSeriesVisibleInLegendWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getSeriesVisibleInLegend(0));
    }

    @Test
    public void testSetBaseSeriesVisibleWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setBaseSeriesVisible(false);
        assertFalse(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testGetBaseSeriesVisibleWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testSetBaseSeriesVisibleInLegendWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setBaseSeriesVisibleInLegend(false);
        assertFalse(renderer.getBaseSeriesVisibleInLegend());
    }

    @Test
    public void testGetBaseSeriesVisibleInLegendWithNullPlotAndNullDatasetAndNullSeries() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getBaseSeriesVisibleInLegend());
    }

    @Test
    public void testGetLegendItemWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetLegendItemsWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        List<LegendItem> items = renderer.getLegendItems();
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    public void testGetItemVisibleWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertFalse(renderer.getItemVisible(0, 0));
    }

    @Test
    public void testGetRowCountWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getRowCount());
    }

    @Test
    public void testGetColumnCountWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getColumnCount());
    }

    @Test
    public void testGetRowKeyWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getRowKey(0));
    }

    @Test
    public void testGetColumnKeyWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getColumnKey(0));
    }

    @Test
    public void testGetItemWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertNull(renderer.getItem(0, 0));
    }

    @Test
    public void testGetItemCountWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(0, renderer.getItemCount(0));
    }

    @Test
    public void testGetRowIndexWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(-1, renderer.getRowIndex("Row1"));
    }

    @Test
    public void testGetColumnIndexWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertEquals(-1, renderer.getColumnIndex("Col1"));
    }

    @Test
    public void testSetSeriesVisibleWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setSeriesVisible(0, Boolean.FALSE);
    }

    @Test
    public void testGetSeriesVisibleWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getSeriesVisible(0));
    }

    @Test
    public void testSetSeriesVisibleInLegendWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
    }

    @Test
    public void testGetSeriesVisibleInLegendWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getSeriesVisibleInLegend(0));
    }

    @Test
    public void testSetBaseSeriesVisibleWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setBaseSeriesVisible(false);
        assertFalse(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testGetBaseSeriesVisibleWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getBaseSeriesVisible());
    }

    @Test
    public void testSetBaseSeriesVisibleInLegendWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        renderer.setBaseSeriesVisibleInLegend(false);
        assertFalse(renderer.getBaseSeriesVisibleInLegend());
    }

    @Test
    public void testGetBaseSeriesVisibleInLegendWithNullPlotAndNullDatasetAndNullSeriesAndNullKey() {
        plot.setRenderer(null);
        plot.setDataset(null);
        assertTrue(renderer.getBaseSeriesVisibleInLegend());
    }
}