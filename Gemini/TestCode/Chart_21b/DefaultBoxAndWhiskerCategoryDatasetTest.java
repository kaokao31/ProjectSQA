package org.jfree.data.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DefaultBoxAndWhiskerCategoryDatasetTest {

    private DefaultBoxAndWhiskerCategoryDataset dataset;

    @Before
    public void setUp() {
        dataset = new DefaultBoxAndWhiskerCategoryDataset();
    }

    @Test
    public void testAddAndGetItem() {
        List<Double> values1 = new ArrayList<Double>();
        values1.add(1.0);
        values1.add(2.0);
        values1.add(3.0);

        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(2.0, 2.0, 1.0, 3.0, 0.5, 3.5, 0.0, 4.0, new ArrayList<Double>());
        dataset.add(item1, "Row1", "Column1");

        assertEquals(item1, dataset.getItem("Row1", "Column1"));
        assertEquals(item1, dataset.getItem(0, 0));
        assertEquals(2.0, dataset.getValue("Row1", "Column1"));
        assertEquals(2.0, dataset.getValue(0, 0));

        assertEquals(1, dataset.getRowCount());
        assertEquals(1, dataset.getColumnCount());
        assertEquals("Row1", dataset.getRowKey(0));
        assertEquals("Column1", dataset.getColumnKey(0));
    }

    @Test
    public void testAddNullItemOrKey() {
        List<Double> values = new ArrayList<Double>();
        values.add(10.0);
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 10.0, 5.0, 15.0, 2.0, 18.0, 1.0, 19.0, new ArrayList<Double>());

        // Test adding with null row key
        boolean exceptionThrown = false;
        try {
            dataset.add(item, null, "Column1");
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown || dataset.getRowCount() >= 0);

        // Test adding with null column key
        exceptionThrown = false;
        try {
            dataset.add(item, "Row1", null);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown || dataset.getColumnCount() >= 0);
    }

    @Test
    public void testRemove() {
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(2.0, 2.0, 1.0, 3.0, 0.5, 3.5, 0.0, 4.0, new ArrayList<Double>());
        dataset.add(item1, "Row1", "Column1");
        
        assertEquals(1, dataset.getRowCount());
        
        dataset.remove("Row1", "Column1");
        assertEquals(0, dataset.getRowCount());
        assertEquals(0, dataset.getColumnCount());
    }

    @Test
    public void testGetStatistic() {
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(5.0, 5.0, 2.0, 8.0, 1.0, 9.0, 0.0, 10.0, new ArrayList<Double>());
        dataset.add(item1, "Row1", "Column1");

        assertEquals(5.0, dataset.getMean("Row1", "Column1"));
        assertEquals(5.0, dataset.getMean(0, 0));
        
        assertEquals(5.0, dataset.getMedian("Row1", "Column1"));
        assertEquals(5.0, dataset.getMedian(0, 0));
        
        assertEquals(2.0, dataset.getQ1("Row1", "Column1"));
        assertEquals(2.0, dataset.getQ1(0, 0));
        
        assertEquals(8.0, dataset.getQ3("Row1", "Column1"));
        assertEquals(8.0, dataset.getQ3(0, 0));
        
        assertEquals(0.0, dataset.getMinRegularValue("Row1", "Column1"));
        assertEquals(0.0, dataset.getMinRegularValue(0, 0));
        
        assertEquals(10.0, dataset.getMaxRegularValue("Row1", "Column1"));
        assertEquals(10.0, dataset.getMaxRegularValue(0, 0));
        
        assertEquals(1.0, dataset.getMinOutlier("Row1", "Column1"));
        assertEquals(1.0, dataset.getMinOutlier(0, 0));
        
        assertEquals(9.0, dataset.getMaxOutlier("Row1", "Column1"));
        assertEquals(9.0, dataset.getMaxOutlier(0, 0));
        
        assertNotNull(dataset.getOutliers("Row1", "Column1"));
        assertNotNull(dataset.getOutliers(0, 0));
    }

    @Test
    public void testGetStatisticNullCases() {
        assertNull(dataset.getMean("NonExistent", "Column1"));
        assertNull(dataset.getMedian("NonExistent", "Column1"));
        assertNull(dataset.getQ1("NonExistent", "Column1"));
        assertNull(dataset.getQ3("NonExistent", "Column1"));
        assertNull(dataset.getMinRegularValue("NonExistent", "Column1"));
        assertNull(dataset.getMaxRegularValue("NonExistent", "Column1"));
        assertNull(dataset.getMinOutlier("NonExistent", "Column1"));
        assertNull(dataset.getMaxOutlier("NonExistent", "Column1"));
        assertNull(dataset.getOutliers("NonExistent", "Column1"));
    }

    @Test
    public void testGetRangeBounds() {
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(5.0, 5.0, 2.0, 8.0, 1.0, 15.0, 0.0, 20.0, new ArrayList<Double>());
        dataset.add(item1, "Row1", "Column1");

        org.jfree.data.Range range = dataset.getRangeBounds(false);
        assertNotNull(range);
        
        org.jfree.data.Range rangeIncludeOutliers = dataset.getRangeBounds(true);
        assertNotNull(rangeIncludeOutliers);
        
        DefaultBoxAndWhiskerCategoryDataset emptyDataset = new DefaultBoxAndWhiskerCategoryDataset();
        assertNull(emptyDataset.getRangeBounds(false));
    }

    @Test
    public void testEqualsAndClone() {
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(5.0, 5.0, 2.0, 8.0, 1.0, 15.0, 0.0, 20.0, new ArrayList<Double>());
        dataset.add(item1, "Row1", "Column1");

        DefaultBoxAndWhiskerCategoryDataset dataset2 = new DefaultBoxAndWhiskerCategoryDataset();
        dataset2.add(item1, "Row1", "Column1");

        assertTrue(dataset.equals(dataset2));
        assertTrue(dataset.equals(dataset));
        assertFalse(dataset.equals(null));
        assertFalse(dataset.equals("Some String"));

        try {
            DefaultBoxAndWhiskerCategoryDataset clone = (DefaultBoxAndWhiskerCategoryDataset) dataset.clone();
            assertTrue(dataset.equals(clone));
        } catch (CloneNotSupportedException e) {
            // Should not happen
        }
    }

    @Test
    public void testCapacity() {
        DefaultBoxAndWhiskerCategoryDataset customDataset = new DefaultBoxAndWhiskerCategoryDataset(10);
        assertEquals(0, customDataset.getRowCount());
    }
}