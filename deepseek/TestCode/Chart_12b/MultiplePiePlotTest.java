package org.jfree.chart.plot;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link MultiplePiePlot}.  Designed to exercise all branches
 * and uncover any latent bugs (including the Defects4J Chart-12 issue).
 */
public class MultiplePiePlotTest {

    private MultiplePiePlot plot;
    private DefaultCategoryDataset dataset;

    /**
     * Creates a plot with a standard dataset before each test.
     */
    @Before
    public void setUp() {
        dataset = createDataset();
        plot = new MultiplePiePlot(dataset);
    }

    /**
     * Creates a sample category dataset with two rows and two columns.
     *
     * @return the dataset.
     */
    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset data = new DefaultCategoryDataset();
        data.addValue(1.0, "R1", "C1");
        data.addValue(2.0, "R1", "C2");
        data.addValue(3.0, "R2", "C1");
        data.addValue(4.0, "R2", "C2");
        return data;
    }

    /**
     * Test the no-argument constructor.
     */
    @Test
    public void testDefaultConstructor() {
        MultiplePiePlot p = new MultiplePiePlot();
        assertNull(p.getDataset());
        assertNotNull(p.getPieChart());
        assertEquals("Multiple Pie Plot", p.getPlotType());
        assertEquals(TableOrder.BY_COLUMN, p.getDataExtractOrder());
        assertEquals(0.0, p.getLimit(), 0.0000001);
    }

    /**
     * Test the constructor with a dataset.
     */
    @Test
    public void testConstructorWithDataset() {
        assertNotNull(plot.getPieChart());
        assertSame(dataset, plot.getDataset());
        assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    /**
     * Test getDataset and setDataset.
     */
    @Test
    public void testSetDataset() {
        DefaultCategoryDataset newData = new DefaultCategoryDataset();
        newData.addValue(1.0, "X", "Y");
        plot.setDataset(newData);
        assertSame(newData, plot.getDataset());

        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    /**
     * Test getDataExtractOrder and setDataExtractOrder.
     */
    @Test
    public void testDataExtractOrder() {
        assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertEquals(TableOrder.BY_ROW, plot.getDataExtractOrder());
    }

    /**
     * Test getLimit and setLimit.
     */
    @Test
    public void testLimit() {
        assertEquals(0.0, plot.getLimit(), 0.000001);
        plot.setLimit(0.5);
        assertEquals(0.5, plot.getLimit(), 0.000001);
        plot.setLimit(-1.0); // negative values should still be stored? 
        assertEquals(-1.0, plot.getLimit(), 0.000001);
    }

    /**
     * Test getPieChart and setPieChart.
     */
    @Test
    public void testSetPieChart() {
        JFreeChart original = plot.getPieChart();
        assertNotNull(original);
        JFreeChart newChart = new JFreeChart("New", JFreeChart.DEFAULT_TITLE_FONT,
                new PiePlot(), false);
        plot.setPieChart(newChart);
        assertSame(newChart, plot.getPieChart());
        plot.setPieChart(null);
        assertNull(plot.getPieChart());
    }

    /**
     * Test getPlotType.
     */
    @Test
    public void testGetPlotType() {
        assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    /**
     * Test the label generator getter and setter.
     */
    @Test
    public void testLabelGenerator() {
        assertNull(plot.getLabelGenerator());
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator();
        plot.setLabelGenerator(gen);
        assertSame(gen, plot.getLabelGenerator());
        plot.setLabelGenerator(null);
        assertNull(plot.getLabelGenerator());
    }

    /**
     * Test the tool tip generator getter and setter.
     */
    @Test
    public void testToolTipGenerator() {
        assertNull(plot.getToolTipGenerator());
        PieToolTipGenerator gen = new StandardPieToolTipGenerator();
        plot.setToolTipGenerator(gen);
        assertSame(gen, plot.getToolTipGenerator());
        plot.setToolTipGenerator(null);
        assertNull(plot.getToolTipGenerator());
    }

    /**
     * Test the URL generator getter and setter.
     */
    @Test
    public void testUrlGenerator() {
        assertNull(plot.getUrlGenerator());
        PieURLGenerator gen = new StandardPieURLGenerator();
        plot.setUrlGenerator(gen);
        assertSame(gen, plot.getUrlGenerator());
        plot.setUrlGenerator(null);
        assertNull(plot.getUrlGenerator());
    }

    /**
     * Test the legend label generator getter and setter.
     */
    @Test
    public void testLegendLabelGenerator() {
        assertNotNull(plot.getLegendLabelGenerator());
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator();
        plot.setLegendLabelGenerator(gen);
        assertSame(gen, plot.getLegendLabelGenerator());
        plot.setLegendLabelGenerator(null);
        assertNull(plot.getLegendLabelGenerator());
    }

    /**
     * Test the legend label tool tip generator getter and setter.
     */
    @Test
    public void testLegendLabelToolTipGenerator() {
        assertNull(plot.getLegendLabelToolTipGenerator());
        PieToolTipGenerator gen = new StandardPieToolTipGenerator();
        plot.setLegendLabelToolTipGenerator(gen);
        assertSame(gen, plot.getLegendLabelToolTipGenerator());
        plot.setLegendLabelToolTipGenerator(null);
        assertNull(plot.getLegendLabelToolTipGenerator());
    }

    /**
     * Test the legend label URL generator getter and setter.
     */
    @Test
    public void testLegendLabelURLGenerator() {
        assertNull(plot.getLegendLabelURLGenerator());
        PieURLGenerator gen = new StandardPieURLGenerator();
        plot.setLegendLabelURLGenerator(gen);
        assertSame(gen, plot.getLegendLabelURLGenerator());
        plot.setLegendLabelURLGenerator(null);
        assertNull(plot.getLegendLabelURLGenerator());
    }

    /**
     * Test the legend item shape getter and setter.
     */
    @Test
    public void testLegendItemShape() {
        assertNotNull(plot.getLegendItemShape());
        Shape shape = new Rectangle(2, 2);
        plot.setLegendItemShape(shape);
        assertEquals(shape, plot.getLegendItemShape());
    }

    /**
     * Test that getLegendItems returns a non-null list with the correct size
     * for a non-null dataset.
     */
    @Test
    public void testGetLegendItems() {
        List<?> items = plot.getLegendItems();
        assertNotNull(items);
        if (plot.getDataExtractOrder() == TableOrder.BY_COLUMN) {
            assertEquals(2, items.size()); // two columns
        } else {
            assertEquals(2, items.size()); // two rows
        }
    }

    /**
     * Test getLegendItems with a null dataset.  The contract is that it should
     * return an empty list (or at least not throw an exception).
     */
    @Test
    public void testGetLegendItemsNullDataset() {
        plot.setDataset(null);
        List<?> items = plot.getLegendItems();
        assertNotNull(items);
        assertEquals(0, items.size());
    }

    /**
     * Test the clone() method.  The cloned plot must be independent of the
     * original, especially the pie chart should not be shared.
     */
    @Test
    public void testClone() {
        MultiplePiePlot p2 = null;
        try {
            p2 = (MultiplePiePlot) plot.clone();
        } catch (CloneNotSupportedException e) {
            fail("Clone should be supported");
        }
        assertNotSame(plot, p2);
        assertEquals(plot, p2);
        assertNotSame(plot.getPieChart(), p2.getPieChart());
        assertNotSame(plot.getPieChart().getPlot(), p2.getPieChart().getPlot());
        assertSame(plot.getDataset(), p2.getDataset()); // dataset may be shared
    }

    /**
     * Test equals() for symmetry and content equality.
     */
    @Test
    public void testEquals() {
        MultiplePiePlot p1 = new MultiplePiePlot(dataset);
        MultiplePiePlot p2 = new MultiplePiePlot(dataset);
        assertEquals(p1, p2);
        assertNotSame(p1, p2);

        p2.setLimit(10.0);
        assertNotEquals(p1, p2);

        p2 = new MultiplePiePlot(dataset);
        p2.setDataExtractOrder(TableOrder.BY_ROW);
        assertNotEquals(p1, p2);

        p2 = new MultiplePiePlot(dataset);
        p2.setLabelGenerator(new StandardPieSectionLabelGenerator());
        assertNotEquals(p1, p2);
    }

    /**
     * Test hashCode consistency.
     */
    @Test
    public void testHashCode() {
        MultiplePiePlot p1 = new MultiplePiePlot(dataset);
        MultiplePiePlot p2 = new MultiplePiePlot(dataset);
        assertEquals(p1.hashCode(), p2.hashCode());

        p2.setLimit(1.0);
        assertNotEquals(p1.hashCode(), p2.hashCode());
    }

    /**
     * Test that a brief draw call completes without error.
     */
    @Test
    public void testDraw() {
        BufferedImage image = new BufferedImage(300, 200,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 300, 200);
        try {
            plot.draw(g2, area, null, null, null);
        } catch (Exception e) {
            fail("Unexpected exception during draw: " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    /**
     * Test drawing with a null dataset (should not throw an exception).
     */
    @Test
    public void testDrawNullDataset() {
        plot.setDataset(null);
        BufferedImage image = new BufferedImage(300, 200,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 300, 200);
        try {
            plot.draw(g2, area, null, null, null);
        } catch (Exception e) {
            fail("Unexpected exception during draw with null dataset: " + e.getMessage());
        } finally {
            g2.dispose();
        }
    }

    /**
     * Test the datasetChanged() method by attaching a listener and firing a change.
     */
    @Test
    public void testDatasetChange() {
        final boolean[] called = new boolean[1];
        DatasetChangeListener listener = new DatasetChangeListener() {
            @Override
            public void datasetChanged(DatasetChangeEvent event) {
                called[0] = true;
            }
        };
        plot.addChangeListener(listener);
        dataset.fireDatasetChanged();
        assertTrue("Dataset change should be propagated", called[0]);
    }

    /**
     * Test setDataExtractOrder with null (should not throw?).
     */
    @Test
    public void testSetDataExtractOrderNull() {
        try {
            plot.setDataExtractOrder(null);
            assertNull(plot.getDataExtractOrder());
        } catch (IllegalArgumentException e) {
            // acceptable if documented behaviour
        }
    }

    /**
     * Test setLimit with Double.MAX_VALUE.
     */
    @Test
    public void testSetLimitExtreme() {
        plot.setLimit(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, plot.getLimit(), 0.0);
        plot.setLimit(-Double.MAX_VALUE);
        assertEquals(-Double.MAX_VALUE, plot.getLimit(), 0.0);
    }

    /**
     * Test that the pie chart is created with a PiePlot when the plot is constructed.
     */
    @Test
    public void testPieChartHasPiePlot() {
        JFreeChart chart = plot.getPieChart();
        assertTrue(chart.getPlot() instanceof PiePlot);
    }
}