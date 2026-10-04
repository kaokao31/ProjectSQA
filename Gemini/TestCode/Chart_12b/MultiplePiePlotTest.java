package org.jfree.chart.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Test;

public class MultiplePiePlotTest {

    @Test
    public void testConstructorAndDefaults() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertNull(plot.getDataset());
        assertNotNull(plot.getPieChart());
        assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
        assertEquals(0.0, plot.getLimit(), 0.0001);
        assertNull(plot.getPieURLTemplate());
        assertNull(plot.getPieToolTipGenerator());
        assertNull(plot.getLegendItemChart());
        assertEquals("MultiplePiePlot", plot.getPlotType());
    }

    @Test
    public void testDatasetHandling() {
        MultiplePiePlot plot = new MultiplePiePlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Row1", "Col1");

        plot.setDataset(dataset);
        assertEquals(dataset, plot.getDataset());

        // Test dataset with null to check listener updates / clearing
        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    @Test
    public void testSetDataExtractOrder() {
        MultiplePiePlot plot = new MultiplePiePlot();
        try {
            plot.setDataExtractOrder(null);
            fail("Expected IllegalArgumentException for null dataExtractOrder");
        } catch (IllegalArgumentException e) {
            // expected
        }

        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertEquals(TableOrder.BY_ROW, plot.getDataExtractOrder());
    }

    @Test
    public void testSetPieChart() {
        MultiplePiePlot plot = new MultiplePiePlot();
        try {
            plot.setPieChart(null);
            fail("Expected IllegalArgumentException for null pieChart");
        } catch (IllegalArgumentException e) {
            // expected
        }

        JFreeChart newChart = new JFreeChart(new PiePlot());
        plot.setPieChart(newChart);
        assertEquals(newChart, plot.getPieChart());
    }

    @Test
    public void testLimit() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setLimit(0.15);
        assertEquals(0.15, plot.getLimit(), 0.0001);
    }

    @Test
    public void testPieURLTemplateAndToolTipGenerator() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setPieURLTemplate("template");
        assertEquals("template", plot.getPieURLTemplate());

        // We can set a mock or standard tool tip generator if available, or null
        plot.setPieToolTipGenerator(null);
        assertNull(plot.getPieToolTipGenerator());
    }

    @Test
    public void testEqualsAndClone() throws CloneNotSupportedException {
        MultiplePiePlot plot1 = new MultiplePiePlot();
        MultiplePiePlot plot2 = new MultiplePiePlot();

        assertTrue(plot1.equals(plot2));
        assertEquals(plot1, plot2);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        plot1.setDataset(dataset);
        assertFalse(plot1.equals(plot2));

        plot2.setDataset(dataset);
        assertTrue(plot1.equals(plot2));

        plot1.setLimit(0.05);
        assertFalse(plot1.equals(plot2));
        plot2.setLimit(0.05);
        assertTrue(plot1.equals(plot2));

        plot1.setDataExtractOrder(TableOrder.BY_ROW);
        assertFalse(plot1.equals(plot2));
        plot2.setDataExtractOrder(TableOrder.BY_ROW);
        assertTrue(plot1.equals(plot2));

        plot1.setPieURLTemplate("url");
        assertFalse(plot1.equals(plot2));
        plot2.setPieURLTemplate("url");
        assertTrue(plot1.equals(plot2));

        // Test cloning
        MultiplePiePlot clone = (MultiplePiePlot) plot1.clone();
        assertTrue(plot1.equals(clone));
        // Ensure dataset is cloned or handled properly (depends on implementation, 
        // usually dataset might be referenced or cloned, let's verify equality)
        assertTrue(plot1.getDataSet().equals(clone.getDataSet()));
    }

    @Test
    public void testDrawWithNullDataset() {
        MultiplePiePlot plot = new MultiplePiePlot();
        // Dataset is null
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        // Should handle null dataset gracefully without throwing NullPointerException
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawWithDatasetByColumn() {
        MultiplePiePlot plot = new MultiplePiePlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "Row1", "Col1");
        dataset.addValue(3.0, "Row2", "Col1");
        dataset.addValue(2.0, "Row1", "Col2");
        dataset.addValue(8.0, "Row2", "Col2");
        plot.setDataset(dataset);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);

        BufferedImage image = new BufferedImage(500, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 500, 400);

        plot.draw(g2, area, null, null, null);
        assertNotNull(plot.getLegendItems());
    }

    @Test
    public void testDrawWithDatasetByRow() {
        MultiplePiePlot plot = new MultiplePiePlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "Row1", "Col1");
        dataset.addValue(3.0, "Row2", "Col1");
        dataset.addValue(2.0, "Row1", "Col2");
        dataset.addValue(8.0, "Row2", "Col2");
        plot.setDataset(dataset);
        plot.setDataExtractOrder(TableOrder.BY_ROW);

        BufferedImage image = new BufferedImage(500, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 500, 400);

        plot.draw(g2, area, null, null, null);
        assertNotNull(plot.getLegendItems());
    }

    @Test
    public void testEqualsEdgeCases() {
        MultiplePiePlot plot = new MultiplePiePlot();
        assertFalse(plot.equals(null));
        assertFalse(plot.equals("Some String"));
        assertTrue(plot.equals(plot));
    }
}