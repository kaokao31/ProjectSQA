package org.jfree.data.general;

import org.junit.Test;
import static org.junit.Assert.*;

import org.jfree.data.KeyedValues;
import org.jfree.data.PieDataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.geom.Rectangle2D;

public class DatasetUtilitiesTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePieDatasetTotal_Null() {
        DatasetUtilities.calculatePieDatasetTotal(null);
    }

    @Test
    public void testCalculatePieDatasetTotal_Valid() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 20.0);
        dataset.setValue("C", null);
        
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        assertEquals(30.0, total, 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePieDataset_NullCategoryDataset() {
        DatasetUtilities.createPieDataset(null, int.valueOf(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePieDataset_InvalidRowIndex() {
        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        categoryDataset.addValue(1.0, "Row1", "Col1");
        DatasetUtilities.createPieDataset(categoryDataset, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePieDataset_InvalidColIndex() {
        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        categoryDataset.addValue(1.0, "Row1", "Col1");
        DatasetUtilities.createPieDataset(categoryDataset, TableOrder.BY_ROW, 5);
    }

    @Test
    public void testCreatePieDataset_ByRow() {
        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        categoryDataset.addValue(1.0, "Row1", "Col1");
        categoryDataset.addValue(2.0, "Row1", "Col2");
        
        PieDataset dataset = DatasetUtilities.createPieDataset(categoryDataset, 0);
        assertNotNull(dataset);
        assertEquals(2, dataset.getItemCount());
        assertEquals(1.0, dataset.getValue("Col1").doubleValue(), 0.0001);
    }

    @Test
    public void testCreatePieDataset_ByColumn() {
        DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
        categoryDataset.addValue(1.0, "Row1", "Col1");
        categoryDataset.addValue(2.0, "Row2", "Col1");
        
        PieDataset dataset = DatasetUtilities.createPieDataset(categoryDataset, TableOrder.BY_COLUMN, 0);
        assertNotNull(dataset);
        assertEquals(2, dataset.getItemCount());
        assertEquals(1.0, dataset.getValue("Row1").doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDataset_NullRowKeys() {
        DatasetUtilities.createCategoryDataset(null, new String[]{"C1"}, new double[][]{{1.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDataset_NullColumnKeys() {
        DatasetUtilities.createCategoryDataset(new String[]{"R1"}, null, new double[][]{{1.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDataset_NullData() {
        DatasetUtilities.createCategoryDataset(new String[]{"R1"}, new String[]{"C1"}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDataset_DimensionMismatchRow() {
        DatasetUtilities.createCategoryDataset(
                new String[]{"R1", "R2"}, 
                new String[]{"C1"}, 
                new double[][]{{1.0}}
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDataset_DimensionMismatchCol() {
        DatasetUtilities.createCategoryDataset(
                new String[]{"R1"}, 
                new String[]{"C1", "C2"}, 
                new double[][]{{1.0}}
        );
    }

    @Test
    public void testCreateCategoryDataset_ValidDoubles() {
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
                new String[]{"R1"}, 
                new String[]{"C1"}, 
                new double[][]{{10.5}}
        );
        assertNotNull(dataset);
        assertEquals(10.5, dataset.getValue("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testCreateCategoryDataset_ValidNumbers() {
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
                new String[]{"R1"}, 
                new String[]{"C1"}, 
                new Number[][]{{Double.valueOf(10.5)}}
        );
        assertNotNull(dataset);
        assertEquals(10.5, dataset.getValue("R1", "C1").doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2D_Null() {
        DatasetUtilities.sampleFunction2D(null, 0.0, 1.0, 2, "Test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2D_InvalidSamples() {
        org.jfree.data.function.Function2D f = x -> x;
        DatasetUtilities.sampleFunction2D(f, 0.0, 1.0, -1, "Test");
    }

    @Test
    public void testSampleFunction2D_Valid() {
        org.jfree.data.function.Function2D f = x -> x * 2;
        XYSeriesCollection dataset = (XYSeriesCollection) DatasetUtilities.sampleFunction2D(f, 0.0, 2.0, 3, "TestSeries");
        assertNotNull(dataset);
        XYSeries series = dataset.getSeries(0);
        assertEquals(3, series.getItemCount());
        assertEquals(0.0, series.getY(0).doubleValue(), 0.0001);
        assertEquals(4.0, series.getY(2).doubleValue(), 0.0001);
    }

    @Test
    public void testGetMinMaxColumnValue_Null() {
        assertNull(DatasetUtilities.getMinMaxColumnValue(null, 0));
    }

    @Test
    public void testGetMinMaxRowValue_Null() {
        assertNull(DatasetUtilities.getMinMaxRowValue(null, 0));
    }

    @Test
    public void testFindMinMaxValues_XY() {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 5.0);
        series.add(2.0, 15.0);
        XYSeriesCollection collection = new XYSeriesCollection(series);
        
        double[] minMax = DatasetUtilities.findStackedXYRange(collection);
        assertNotNull(minMax);
    }

    @Test
    public void testIsEmptyOrNull_Category() {
        assertTrue(DatasetUtilities.isEmptyOrNull((CategoryDataset) null));
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
        
        dataset.addValue(1.0, "R1", "C1");
        assertFalse(DatasetUtilities.isEmptyOrNull(dataset));
    }

    @Test
    public void testIsEmptyOrNull_Pie() {
        assertTrue(DatasetUtilities.isEmptyOrNull((PieDataset) null));
        DefaultPieDataset dataset = new DefaultPieDataset();
        assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
        
        dataset.setValue("A", 1.0);
        assertFalse(DatasetUtilities.isEmptyOrNull(dataset));
    }

    @Test
    public void testCumulativePercent() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 10.0);
        
        PieDataset cumulative = DatasetUtilities.createCumulativePercentDataset(dataset);
        assertNotNull(cumulative);
        assertEquals(0.5, cumulative.getValue("A").doubleValue(), 0.0001);
        assertEquals(1.0, cumulative.getValue("B").doubleValue(), 0.0001);
    }
}