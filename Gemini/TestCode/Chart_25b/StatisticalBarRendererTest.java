package org.jfree.chart.renderer.category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.junit.Before;
import org.junit.Test;

/**
 * A JUnit 4 test suite for StatisticalBarRenderer.
 * Targets maximum branch/line coverage and aims to exercise potential bugs in JFreeChart Chart-25.
 */
public class StatisticalBarRendererTest {

    private StatisticalBarRenderer renderer;

    @Before
    public void setUp() {
        renderer = new StatisticalBarRenderer();
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(renderer.getErrorIndicatorPaint());
        assertEquals(Color.gray, renderer.getErrorIndicatorPaint());
        assertNotNull(renderer.getErrorIndicatorStroke());
    }

    @Test
    public void testSetGetErrorIndicatorPaint() {
        assertNotNull(renderer.getErrorIndicatorPaint());
        renderer.setErrorIndicatorPaint(Color.RED);
        assertEquals(Color.RED, renderer.getErrorIndicatorPaint());

        renderer.setErrorIndicatorPaint(null);
        assertNull(renderer.getErrorIndicatorPaint());
    }

    @Test
    public void testSetGetErrorIndicatorStroke() {
        assertNotNull(renderer.getErrorIndicatorStroke());
        renderer.setErrorIndicatorStroke(null);
        assertNull(renderer.getErrorIndicatorStroke());
    }

    @Test
    public void testEqualsAndHashCode() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();

        assertTrue(r1.equals(r2));
        assertEquals(r1.hashCode(), r2.hashCode());

        r1.setErrorIndicatorPaint(Color.BLUE);
        assertTrue(!r1.equals(r2));

        r2.setErrorIndicatorPaint(Color.BLUE);
        assertTrue(r1.equals(r2));

        assertTrue(!r1.equals(null));
        assertTrue(!r1.equals("Some String"));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        renderer.setErrorIndicatorPaint(Color.GREEN);
        StatisticalBarRenderer clone = (StatisticalBarRenderer) renderer.clone();
        assertNotNull(clone);
        assertEquals(renderer, clone);
        assertTrue(clone != renderer);
    }

    @Test
    public void testDrawItemWithStatisticalDatasetVertical() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Row 1", "Col 1");
        dataset.add(15.0, 3.0, "Row 1", "Col 2");

        JFreeChart chart = ChartFactory.createBarChart(
                "Test Chart",
                "Category",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setRenderer(renderer);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        try {
            chart.draw(g2, area);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawItemWithStatisticalDatasetHorizontal() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Row 1", "Col 1");
        dataset.add(15.0, 3.0, "Row 1", "Col 2");

        JFreeChart chart = ChartFactory.createBarChart(
                "Test Chart",
                "Category",
                "Value",
                dataset,
                PlotOrientation.HORIZONTAL,
                true,
                true,
                false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setRenderer(renderer);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        try {
            chart.draw(g2, area);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawItemWithStandardCategoryDatasetFallback() {
        // StatisticalBarRenderer expects a StatisticalCategoryDataset, but what happens with a standard DefaultCategoryDataset?
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "Row 1", "Col 1");

        JFreeChart chart = ChartFactory.createBarChart(
                "Test Chart",
                "Category",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setRenderer(renderer);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        try {
            chart.draw(g2, area);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawItemNullMeanOrStdDev() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        // Adding nulls or items with null mean/stddev to check robustness against NPEs
        dataset.add(null, null, "Row 1", "Col 1");
        dataset.add(null, 1.0, "Row 1", "Col 2");
        dataset.add(5.0, null, "Row 1", "Col 3");

        JFreeChart chart = ChartFactory.createBarChart(
                "Test Chart",
                "Category",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setRenderer(renderer);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        try {
            chart.draw(g2, area);
        } finally {
            g2.dispose();
        }
    }
}