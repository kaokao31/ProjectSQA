package org.jfree.chart.renderer.category;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

/**
 * Test suite for StatisticalBarRenderer, targeting fault detection and high coverage.
 */
public class StatisticalBarRendererTest {

    private DefaultStatisticalCategoryDataset dataset;
    private StatisticalBarRenderer renderer;
    private JFreeChart chart;

    @Before
    public void setUp() {
        renderer = new StatisticalBarRenderer();
        dataset = new DefaultStatisticalCategoryDataset();
        // Build a basic dataset with two series and three categories
        dataset.add(1.0, 0.5, "Series 1", "Category 1");
        dataset.add(2.0, 0.3, "Series 1", "Category 2");
        dataset.add(3.0, 0.4, "Series 1", "Category 3");
        dataset.add(2.5, 0.6, "Series 2", "Category 1");
        dataset.add(3.5, 0.7, "Series 2", "Category 2");
        dataset.add(4.0, 0.8, "Series 2", "Category 3");
    }

    /**
     * Helper to create a chart with the given dataset and renderer.
     */
    private JFreeChart createChart(DefaultStatisticalCategoryDataset data) {
        JFreeChart chart = ChartFactory.createBarChart(
            "Test", "Category", "Value", data, PlotOrientation.VERTICAL,
            true, true, false
        );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setRenderer(renderer);
        // Ensure range axis is a NumberAxis (default)
        return chart;
    }

    /**
     * Helper to render the chart onto a buffered image (accepts null dataset for negative testing).
     */
    private void renderChart(DefaultStatisticalCategoryDataset data) {
        if (data == null) {
            // create an empty chart with null dataset? Not allowed; pass empty dataset
            data = new DefaultStatisticalCategoryDataset();
        }
        JFreeChart c = createChart(data);
        BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            c.draw(g2, new Rectangle2D.Double(0, 0, 600, 400),
                   new ChartRenderingInfo(new StandardEntityCollection()));
        } finally {
            g2.dispose();
        }
    }

    /**
     * Test rendering with a standard dataset (baseline).
     */
    @Test
    public void testRenderWithValidData() {
        // Should not throw any exception
        renderChart(dataset);
        assertTrue(true); // if we reach here, no exception
    }

    /**
     * Test rendering with null mean values in the dataset.
     * The buggy version might throw NullPointerException.
     */
    @Test
    public void testRenderWithNullMean() {
        dataset.add(null, 0.5, "Series 1", "Category 4");
        renderChart(dataset);
        assertTrue("Rendering with null mean should not throw", true);
    }

    /**
     * Test rendering with null standard deviation values.
     */
    @Test
    public void testRenderWithNullStdDev() {
        dataset.add(1.0, null, "Series 2", "Category 4");
        renderChart(dataset);
        assertTrue("Rendering with null std dev should not throw", true);
    }

    /**
     * Test rendering with both mean and std dev null.
     */
    @Test
    public void testRenderWithBothNull() {
        dataset.add(null, null, "Series 1", "Category 5");
        renderChart(dataset);
        assertTrue("Rendering with both null should not throw", true);
    }

    /**
     * Test rendering with an empty dataset.
     */
    @Test
    public void testRenderWithEmptyDataset() {
        DefaultStatisticalCategoryDataset empty = new DefaultStatisticalCategoryDataset();
        renderChart(empty);
        assertTrue("Rendering empty dataset should not throw", true);
    }

    /**
     * Test rendering with a single item (edge case).
     */
    @Test
    public void testRenderWithSingleItem() {
        DefaultStatisticalCategoryDataset single = new DefaultStatisticalCategoryDataset();
        single.add(5.0, 1.0, "S1", "C1");
        renderChart(single);
        assertTrue("Rendering single item should not throw", true);
    }

    /**
     * Test rendering with negative values (mean and std dev).
     */
    @Test
    public void testRenderWithNegativeValues() {
        dataset.add(-2.0, 0.5, "Series 1", "NegCat");
        dataset.add(-3.0, 0.3, "Series 2", "NegCat");
        renderChart(dataset);
        assertTrue("Rendering negative values should not throw", true);
    }

    /**
     * Test rendering with large positive values near axis boundary.
     */
    @Test
    public void testRenderWithLargeValues() {
        dataset.add(1e10, 1e8, "Series 1", "LargeCat");
        renderChart(dataset);
        assertTrue("Rendering large values should not throw", true);
    }

    /**
     * Test rendering with very small (subnormal) values.
     */
    @Test
    public void testRenderWithVerySmallValues() {
        dataset.add(1e-300, 1e-310, "Series 1", "SmallCat");
        renderChart(dataset);
        assertTrue("Rendering very small values should not throw", true);
    }

    /**
     * Test that the renderer's equals and hashcode methods work.
     */
    @Test
    public void testEquals() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    /**
     * Test clone method (if present) – basic structural equality.
     */
    @Test
    public void testClone() throws CloneNotSupportedException {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = (StatisticalBarRenderer) r1.clone();
        assertNotSame(r1, r2);
        assertTrue(r1.equals(r2));
    }

    /**
     * Test serialization round-trip.
     */
    @Test
    public void testSerialization() throws Exception {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        // Serialize and deserialize using basic Java serialization
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(r1);
        oos.flush();
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        StatisticalBarRenderer r2 = (StatisticalBarRenderer) ois.readObject();
        assertTrue(r1.equals(r2));
    }

    /**
     * Test drawing with an axis that has a fixed range (edge case).
     */
    @Test
    public void testRenderWithFixedAxisRange() {
        JFreeChart c = createChart(dataset);
        CategoryPlot plot = (CategoryPlot) c.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(-10.0, 10.0);
        BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            c.draw(g2, new Rectangle2D.Double(0, 0, 600, 400),
                   new ChartRenderingInfo(new StandardEntityCollection()));
        } finally {
            g2.dispose();
        }
        assertTrue("Rendering with fixed axis should not throw", true);
    }

    /**
     * Test drawing when the dataset returns null for a column key.
     * (Some renderers may index incorrectly.)
     */
    @Test
    public void testRenderWithMissingColumnKey() {
        // Add a row that has no data for a column – dataset may return null
        dataset.add(10.0, 1.0, "Series 3", "NewCat");
        // Accessing an item with null mean in a different way – covered by testRenderWithNullMean
        renderChart(dataset);
        assertTrue(true);
    }

    /**
     * Stress test with many series and categories.
     */
    @Test
    public void testRenderWithManyItems() {
        DefaultStatisticalCategoryDataset big = new DefaultStatisticalCategoryDataset();
        for (int s = 0; s < 20; s++) {
            for (int c = 0; c < 20; c++) {
                big.add(Math.random() * 100, Math.random() * 10,
                        "S" + s, "C" + c);
            }
        }
        renderChart(big);
        assertTrue("Rendering many items should not throw", true);
    }
}