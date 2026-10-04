package org.jfree.chart.renderer.category;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryLineAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.IntervalCategoryItemLabelGenerator;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryCrosshairState;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.general.DefaultKeyedValues2DDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.junit.Test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the {@link AbstractCategoryItemRenderer} class.
 */
public class AbstractCategoryItemRendererTest {

    private static class CustomRenderer extends AbstractCategoryItemRenderer {
        private static final long serialVersionUID = 1L;

        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                             Rectangle2D dataArea, CategoryPlot plot,
                             CategoryAxis domainAxis, ValueAxis rangeAxis,
                             CategoryDataset dataset, int row, int column,
                             int pass) {
            // Empty implementation for testing
        }
    }

    /**
     * Test equals() and hashCode() consistency.
     */
    @Test
    public void testEquals() {
        CustomRenderer r1 = new CustomRenderer();
        CustomRenderer r2 = new CustomRenderer();
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
        assertEquals(r1.hashCode(), r2.hashCode());

        // Tooltip generator
        r1.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
        assertTrue(r1.equals(r2));

        r1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        assertTrue(r1.equals(r2));

        // Item label generator
        r1.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        assertTrue(r1.equals(r2));

        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        assertTrue(r1.equals(r2));

        // URL generator
        r1.setSeriesURLGenerator(0, new StandardCategoryURLGenerator());
        assertFalse(r1.equals(r2));
        r2.setSeriesURLGenerator(0, new StandardCategoryURLGenerator());
        assertTrue(r1.equals(r2));

        r1.setBaseURLGenerator(new StandardCategoryURLGenerator());
        assertFalse(r1.equals(r2));
        r2.setBaseURLGenerator(new StandardCategoryURLGenerator());
        assertTrue(r1.equals(r2));

        // Legend item label generator
        r1.setLegendItemLabelGenerator(new StandardCategorySeriesLabelGenerator("{0}"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemLabelGenerator(new StandardCategorySeriesLabelGenerator("{0}"));
        assertTrue(r1.equals(r2));

        // Legend item tool tip generator
        r1.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("{1}"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("{1}"));
        assertTrue(r1.equals(r2));

        // Legend item url generator
        r1.setLegendItemURLGenerator(new StandardCategorySeriesLabelGenerator("{2}"));
        assertFalse(r1.equals(r2));
        r2.setLegendItemURLGenerator(new StandardCategorySeriesLabelGenerator("{2}"));
        assertTrue(r1.equals(r2));

        // Background / Foreground annotations
        CategoryAnnotation a1 = new CategoryTextAnnotation("Test", "Category", 10.0);
        r1.addAnnotation(a1, Layer.BACKGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(a1, Layer.BACKGROUND);
        assertTrue(r1.equals(r2));

        CategoryAnnotation a2 = new CategoryTextAnnotation("Test2", "Category2", 20.0);
        r1.addAnnotation(a2, Layer.FOREGROUND);
        assertFalse(r1.equals(r2));
        r2.addAnnotation(a2, Layer.FOREGROUND);
        assertTrue(r1.equals(r2));

        assertFalse(r1.equals(null));
        assertFalse(r1.equals("Not a renderer"));
    }

    /**
     * Test cloning and independence of the clone.
     */
    @Test
    public void testCloning() throws CloneNotSupportedException {
        CustomRenderer r1 = new CustomRenderer();
        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        r1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        r1.setBaseURLGenerator(new StandardCategoryURLGenerator());
        r1.addAnnotation(new CategoryTextAnnotation("A", "C1", 1.0));

        CustomRenderer r2 = (CustomRenderer) r1.clone();
        assertTrue(r1 != r2);
        assertTrue(r1.getClass() == r2.getClass());
        assertTrue(r1.equals(r2));

        // Modify r1 and verify r2 is unchanged
        r1.addAnnotation(new CategoryTextAnnotation("B", "C2", 2.0));
        assertFalse(r1.equals(r2));
    }

    /**
     * Test serialization.
     */
    @Test
    public void testSerialization() throws Exception {
        CustomRenderer r1 = new CustomRenderer();
        r1.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        r1.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
        r1.setSeriesURLGenerator(0, new StandardCategoryURLGenerator());
        r1.addAnnotation(new CategoryTextAnnotation("A", "C1", 1.0));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(r1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        CustomRenderer r2 = (CustomRenderer) in.readObject();
        in.close();

        assertEquals(r1, r2);
    }

    /**
     * Test range bounds calculation with various dataset types and configurations.
     */
    @Test
    public void testFindRangeBounds() {
        CustomRenderer r = new CustomRenderer();
        assertNull(r.findRangeBounds(null));

        DefaultCategoryDataset emptyDataset = new DefaultCategoryDataset();
        assertNull(r.findRangeBounds(emptyDataset));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(5.0, "R1", "C2");
        dataset.addValue(-2.0, "R2", "C1");

        Range range = r.findRangeBounds(dataset);
        assertEquals(new Range(-2.0, 5.0), range);

        // When series is not visible
        r.setSeriesVisible(0, Boolean.FALSE);
        range = r.findRangeBounds(dataset);
        assertEquals(new Range(-2.0, -2.0), range);

        r.setSeriesVisible(1, Boolean.FALSE);
        assertNull(r.findRangeBounds(dataset));

        // Range bounds with null entries
        DefaultCategoryDataset datasetWithNulls = new DefaultCategoryDataset();
        datasetWithNulls.addValue(null, "R1", "C1");
        assertNull(r.findRangeBounds(datasetWithNulls));
    }

    /**
     * Test findRangeBounds with IntervalCategoryDataset.
     */
    @Test
    public void testFindRangeBoundsWithIntervalDataset() {
        LineAndShapeRenderer r = new LineAndShapeRenderer();
        Double[][] starts = new Double[][]{{0.5, 1.5}};
        Double[][] ends = new Double[][]{{2.5, 4.5}};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(
                new String[]{"S1"}, new String[]{"C1", "C2"}, starts, ends
        );

        Range bounds = r.findRangeBounds(dataset);
        assertEquals(new Range(0.5, 4.5), bounds);
    }

    /**
     * Test annotations management and null checks.
     */
    @Test
    public void testAddAndRemoveAnnotation() {
        CustomRenderer r = new CustomRenderer();
        CategoryAnnotation a1 = new CategoryTextAnnotation("A1", "C1", 10.0);
        CategoryAnnotation a2 = new CategoryTextAnnotation("A2", "C2", 20.0);

        r.addAnnotation(a1);
        r.addAnnotation(a2, Layer.BACKGROUND);
        assertEquals(2, r.getAnnotations().size());

        // Test removal
        assertTrue(r.removeAnnotation(a1));
        assertFalse(r.removeAnnotation(a1)); // already removed
        assertEquals(1, r.getAnnotations().size());

        assertTrue(r.removeAnnotation(a2));
        assertEquals(0, r.getAnnotations().size());

        // Test removeAnnotations()
        r.addAnnotation(a1);
        r.addAnnotation(a2, Layer.BACKGROUND);
        r.removeAnnotations();
        assertEquals(0, r.getAnnotations().size());

        // Null argument checks
        try {
            r.addAnnotation(null);
            fail("Expected IllegalArgumentException on null annotation");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            r.removeAnnotation(null);
            fail("Expected IllegalArgumentException on null annotation");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Test generators resolution: per-series vs base.
     */
    @Test
    public void testItemLabelAndToolTipGenerators() {
        CustomRenderer r = new CustomRenderer();
        CategoryItemLabelGenerator g1 = new StandardCategoryItemLabelGenerator();
        CategoryItemLabelGenerator g2 = new StandardCategoryItemLabelGenerator();

        r.setBaseItemLabelGenerator(g1);
        assertEquals(g1, r.getItemLabelGenerator(0, 0));
        assertEquals(g1, r.getBaseItemLabelGenerator());

        r.setSeriesItemLabelGenerator(0, g2);
        assertEquals(g2, r.getItemLabelGenerator(0, 0));
        assertEquals(g1, r.getItemLabelGenerator(1, 0));
        assertEquals(g2, r.getSeriesItemLabelGenerator(0));

        CategoryToolTipGenerator t1 = new StandardCategoryToolTipGenerator();
        CategoryToolTipGenerator t2 = new StandardCategoryToolTipGenerator();

        r.setBaseToolTipGenerator(t1);
        assertEquals(t1, r.getToolTipGenerator(0, 0));
        assertEquals(t1, r.getBaseToolTipGenerator());

        r.setSeriesToolTipGenerator(0, t2);
        assertEquals(t2, r.getToolTipGenerator(0, 0));
        assertEquals(t1, r.getToolTipGenerator(1, 0));
        assertEquals(t2, r.getSeriesToolTipGenerator(0));

        CategoryURLGenerator u1 = new StandardCategoryURLGenerator();
        CategoryURLGenerator u2 = new StandardCategoryURLGenerator();

        r.setBaseURLGenerator(u1);
        assertEquals(u1, r.getBaseURLGenerator());
        assertEquals(u1, r.getURLGenerator(0, 0));

        r.setSeriesURLGenerator(0, u2);
        assertEquals(u2, r.getSeriesURLGenerator(0));
        assertEquals(u2, r.getURLGenerator(0, 0));
        assertEquals(u1, r.getURLGenerator(1, 0));
    }

    /**
     * Test legend item label, tool tip, and URL generators.
     */
    @Test
    public void testLegendGenerators() {
        CustomRenderer r = new CustomRenderer();
        try {
            r.setLegendItemLabelGenerator(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        CategorySeriesLabelGenerator g = new StandardCategorySeriesLabelGenerator("{0}");
        r.setLegendItemLabelGenerator(g);
        assertEquals(g, r.getLegendItemLabelGenerator());

        r.setLegendItemToolTipGenerator(g);
        assertEquals(g, r.getLegendItemToolTipGenerator());

        r.setLegendItemURLGenerator(g);
        assertEquals(g, r.getLegendItemURLGenerator());
    }

    /**
     * Test legend items creation with various plot/dataset combinations.
     */
    @Test
    public void testGetLegendItems() {
        BarRenderer renderer = new BarRenderer();
        CategoryPlot plot = new CategoryPlot();
        renderer.setPlot(plot);

        // Null dataset
        LegendItemCollection lic = renderer.getLegendItems();
        assertEquals(0, lic.getItemCount());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series 1", "Type 1");
        dataset.addValue(2.0, "Series 2", "Type 1");
        plot.setDataset(dataset);
        plot.setRenderer(renderer);

        lic = renderer.getLegendItems();
        assertEquals(2, lic.getItemCount());

        // Make series 0 invisible
        renderer.setSeriesVisible(0, Boolean.FALSE);
        lic = renderer.getLegendItems();
        assertEquals(1, lic.getItemCount());

        // Make series not visible in legend
        renderer.setSeriesVisible(0, Boolean.TRUE);
        renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        lic = renderer.getLegendItems();
        assertEquals(1, lic.getItemCount());

        // Null plot returns empty collection
        renderer.setPlot(null);
        lic = renderer.getLegendItems();
        assertEquals(0, lic.getItemCount());
    }

    /**
     * Test getLegendItem() method with custom attributes.
     */
    @Test
    public void testGetLegendItem() {
        BarRenderer renderer = new BarRenderer();
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series 1", "Type 1");
        plot.setDataset(dataset);
        plot.setRenderer(renderer);

        renderer.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("Tooltip: {0}"));
        renderer.setLegendItemURLGenerator(new StandardCategorySeriesLabelGenerator("http://url/{0}"));

        LegendItem item = renderer.getLegendItem(0, 0);
        assertNotNull(item);
        assertEquals("Series 1", item.getLabel());
        assertEquals("Tooltip: Series 1", item.getToolTipText());
        assertEquals("http://url/Series 1", item.getURLText());

        // Unassigned or out-of-range dataset returns null
        assertNull(renderer.getLegendItem(1, 0));
        assertNull(renderer.getLegendItem(0, -1));

        renderer.setSeriesVisible(0, Boolean.FALSE);
        assertNull(renderer.getLegendItem(0, 0));
    }

    /**
     * Test calculateBarWidth and series offsets.
     */
    @Test
    public void testCalculateBarWidthAndItemMiddle() {
        BarRenderer renderer = new BarRenderer();
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis domainAxis = new CategoryAxis("Category");
        ValueAxis rangeAxis = new NumberAxis("Value");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "S1", "C1");
        dataset.addValue(20.0, "S2", "C1");
        dataset.addValue(30.0, "S1", "C2");
        dataset.addValue(40.0, "S2", "C2");

        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setDataset(dataset);
        plot.setRenderer(renderer);

        Rectangle2D dataArea = new Rectangle2D.Double(0.0, 0.0, 400.0, 300.0);

        // Test calculateBarWidth
        renderer.setItemMargin(0.05);
        renderer.calculateBarWidth(plot, dataArea, dataset, new CategoryItemRendererState(new PlotRenderingInfo(null)));
        assertTrue(renderer.getItemMargin() == 0.05);

        // Test getItemMiddle for horizontal & vertical orientations
        plot.setOrientation(PlotOrientation.VERTICAL);
        double middleV = renderer.getItemMiddle("S1", "C1", dataset, domainAxis, dataArea, RectangleEdge.BOTTOM);
        assertTrue(middleV >= 0.0);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        double middleH = renderer.getItemMiddle("S1", "C1", dataset, domainAxis, dataArea, RectangleEdge.LEFT);
        assertTrue(middleH >= 0.0);
    }

    /**
     * Test drawing markers and gridlines.
     */
    @Test
    public void testDrawDomainAndRangeMarkers() {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        CustomRenderer renderer = new CustomRenderer();
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis domainAxis = new CategoryAxis("Category");
        ValueAxis rangeAxis = new NumberAxis("Value");
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        renderer.setPlot(plot);

        Rectangle2D dataArea = new Rectangle2D.Double(20.0, 20.0, 360.0, 260.0);

        // Domain Marker
        CategoryMarker categoryMarker = new CategoryMarker("C1", new Color(255, 0, 0, 100), new BasicStroke(1.0f));
        categoryMarker.setDrawAsLine(false);
        renderer.drawDomainMarker(g2, plot, domainAxis, categoryMarker, dataArea);

        categoryMarker.setDrawAsLine(true);
        renderer.drawDomainMarker(g2, plot, domainAxis, categoryMarker, dataArea);

        // Value / Interval Range Marker
        ValueMarker valueMarker = new ValueMarker(15.0, Color.BLUE, new BasicStroke(1.2f));
        renderer.drawRangeMarker(g2, plot, rangeAxis, valueMarker, dataArea);

        IntervalMarker intervalMarker = new IntervalMarker(10.0, 20.0, Color.GREEN);
        renderer.drawRangeMarker(g2, plot, rangeAxis, intervalMarker, dataArea);

        // Range line
        renderer.drawRangeLine(g2, plot, rangeAxis, dataArea, 15.0, Color.BLACK, new BasicStroke(1.0f));

        // Domain gridline
        renderer.drawDomainGridLine(g2, plot, dataArea, 50.0);

        // Range gridline
        renderer.drawRangeGridLine(g2, plot, rangeAxis, dataArea, 10.0);

        g2.dispose();
    }

    /**
     * Test drawing annotations with both foreground and background layers.
     */
    @Test
    public void testDrawAnnotations() {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        CustomRenderer renderer = new CustomRenderer();
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis domainAxis = new CategoryAxis("Category");
        ValueAxis rangeAxis = new NumberAxis("Value");
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        renderer.setPlot(plot);

        Rectangle2D dataArea = new Rectangle2D.Double(20.0, 20.0, 360.0, 260.0);
        PlotRenderingInfo info = new PlotRenderingInfo(null);

        CategoryTextAnnotation annotationForeground = new CategoryTextAnnotation("Foreground", "C1", 10.0);
        CategoryLineAnnotation annotationBackground = new CategoryLineAnnotation("C1", 5.0, "C2", 15.0, Color.RED, new BasicStroke(1.0f));

        renderer.addAnnotation(annotationForeground, Layer.FOREGROUND);
        renderer.addAnnotation(annotationBackground, Layer.BACKGROUND);

        renderer.drawAnnotations(g2, dataArea, domainAxis, rangeAxis, Layer.FOREGROUND, info);
        renderer.drawAnnotations(g2, dataArea, domainAxis, rangeAxis, Layer.BACKGROUND, info);

        g2.dispose();
    }

    /**
     * Test entity collection additions.
     */
    @Test
    public void testAddEntity() {
        CustomRenderer renderer = new CustomRenderer();
        EntityCollection entities = new StandardEntityCollection();
        Shape hotspot = new Rectangle2D.Double(10.0, 10.0, 20.0, 20.0);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");

        // Tooltip & URL generator present
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        renderer.setBaseURLGenerator(new StandardCategoryURLGenerator());

        renderer.addEntity(entities, hotspot, dataset, 0, 0, true);
        assertEquals(1, entities.getEntityCount());

        // Zero width / height hotspot should not be added
        Shape emptyHotspot = new Rectangle2D.Double(10.0, 10.0, 0.0, 0.0);
        renderer.addEntity(entities, emptyHotspot, dataset, 0, 0, true);
        assertEquals(1, entities.getEntityCount());

        // Null hotspot should not throw exception and should not add entity
        renderer.addEntity(entities, null, dataset, 0, 0, true);
        assertEquals(1, entities.getEntityCount());
    }

    /**
     * Test crosshair updating logic.
     */
    @Test
    public void testUpdateCrosshairValues() {
        CustomRenderer renderer = new CustomRenderer();
        CategoryCrosshairState crosshairState = new CategoryCrosshairState();
        crosshairState.setCrosshairDistance(Double.POSITIVE_INFINITY);

        renderer.updateCrosshairValues(crosshairState, "R1", "C1", 100.0, 0, 50.0, 75.0, PlotOrientation.VERTICAL);
        assertEquals("R1", crosshairState.getRowKey());
        assertEquals("C1", crosshairState.getColumnKey());
        assertEquals(100.0, crosshairState.getCrosshairY(), 0.001);

        // Null crosshair state should do nothing safely
        renderer.updateCrosshairValues(null, "R1", "C1", 100.0, 0, 50.0, 75.0, PlotOrientation.VERTICAL);
    }

    /**
     * Test createState method and column count tracking.
     */
    @Test
    public void testCreateState() {
        CustomRenderer renderer = new CustomRenderer();
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        plot.setDataset(dataset);

        PlotRenderingInfo info = new PlotRenderingInfo(null);
        CategoryItemRendererState state = renderer.createState(info);
        assertNotNull(state);
        assertEquals(0, state.getVisibleSeriesCount());

        CategoryItemRendererState stateWithData = renderer.initialise(null, new Rectangle2D.Double(), plot, dataset, info);
        assertEquals(1, stateWithData.getVisibleSeriesCount());
    }

    /**
     * Test series visibility / paint / stroke fallbacks and settings.
     */
    @Test
    public void testSeriesOverrides() {
        CustomRenderer renderer = new CustomRenderer();
        assertEquals(1, renderer.getPassCount());

        renderer.setSeriesItemLabelGenerator(0, null);
        assertNull(renderer.getSeriesItemLabelGenerator(0));

        renderer.setSeriesToolTipGenerator(0, null);
        assertNull(renderer.getSeriesToolTipGenerator(0));

        renderer.setSeriesURLGenerator(0, null);
        assertNull(renderer.getSeriesURLGenerator(0));
    }
}