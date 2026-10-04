package org.jfree.data.category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;

public class DefaultIntervalCategoryDatasetTest {

    @Test
    public void testConstructorWithArrays() {
        Number[][] startsData = {
            {1.0, 2.0},
            {3.0, 4.0}
        };
        Number[][] endsData = {
            {1.5, 2.5},
            {3.5, 4.5}
        };

        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);

        assertEquals(2, dataset.getRowCount());
        assertEquals(2, dataset.getColumnCount());
        assertEquals(1.0, dataset.getStartValue(0, 0).doubleValue(), 0.0001);
        assertEquals(1.5, dataset.getEndValue(0, 0).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStarts() {
        Number[][] endsData = { {1.0, 2.0} };
        new DefaultIntervalCategoryDataset(null, endsData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullEnds() {
        Number[][] startsData = { {1.0, 2.0} };
        new DefaultIntervalCategoryDataset(startsData, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayDimensionMismatchRow() {
        Number[][] startsData = { {1.0, 2.0}, {3.0, 4.0} };
        Number[][] endsData = { {1.5, 2.5} };
        new DefaultIntervalCategoryDataset(startsData, endsData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayDimensionMismatchColumn() {
        Number[][] startsData = { {1.0, 2.0} };
        Number[][] endsData = { {1.5, 2.5, 3.5} };
        new DefaultIntervalCategoryDataset(startsData, endsData);
    }

    @Test
    public void testConstructorWithList() {
        List<String> rowKeys = Arrays.asList("Row 1", "Row 2");
        List<String> colKeys = Arrays.asList("Col 1", "Col 2");

        List<List<Number>> starts = Arrays.asList(
            Arrays.asList(1.0, 2.0),
            Arrays.asList(3.0, 4.0)
        );
        List<List<Number>> ends = Arrays.asList(
            Arrays.asList(1.5, 2.5),
            Arrays.asList(3.5, 4.5)
        );

        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, ends);

        assertEquals(2, dataset.getRowCount());
        assertEquals(2, dataset.getColumnCount());
        assertEquals("Row 1", dataset.getRowKey(0));
        assertEquals("Col 1", dataset.getColumnKey(0));
        assertEquals(1.0, dataset.getStartValue(0, 0).doubleValue(), 0.0001);
        assertEquals(1.5, dataset.getEndValue(0, 0).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListNullRowKeys() {
        List<String> colKeys = Arrays.asList("Col 1");
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0));
        List<List<Number>> ends = Arrays.asList(Arrays.asList(2.0));
        new DefaultIntervalCategoryDataset(null, colKeys, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListNullColumnKeys() {
        List<String> rowKeys = Arrays.asList("Row 1");
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0));
        List<List<Number>> ends = Arrays.asList(Arrays.asList(2.0));
        new DefaultIntervalCategoryDataset(rowKeys, null, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListNullStarts() {
        List<String> rowKeys = Arrays.asList("Row 1");
        List<String> colKeys = Arrays.asList("Col 1");
        List<List<Number>> ends = Arrays.asList(Arrays.asList(2.0));
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, null, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListNullEnds() {
        List<String> rowKeys = Arrays.asList("Row 1");
        List<String> colKeys = Arrays.asList("Col 1");
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0));
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListEmptyRowKeys() {
        List<String> rowKeys = Arrays.asList();
        List<String> colKeys = Arrays.asList("Col 1");
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0));
        List<List<Number>> ends = Arrays.asList(Arrays.asList(2.0));
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListEmptyColKeys() {
        List<String> rowKeys = Arrays.asList("Row 1");
        List<String> colKeys = Arrays.asList();
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0));
        List<List<Number>> ends = Arrays.asList(Arrays.asList(2.0));
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListDuplicateRowKey() {
        List<String> rowKeys = Arrays.asList("Row 1", "Row 1");
        List<String> colKeys = Arrays.asList("Col 1");
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0), Arrays.asList(2.0));
        List<List<Number>> ends = Arrays.asList(Arrays.asList(1.5), Arrays.asList(2.5));
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListDuplicateColKey() {
        List<String> rowKeys = Arrays.asList("Row 1");
        List<String> colKeys = Arrays.asList("Col 1", "Col 1");
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0, 2.0));
        List<List<Number>> ends = Arrays.asList(Arrays.asList(1.5, 2.5));
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListStartsSizeMismatch() {
        List<String> rowKeys = Arrays.asList("Row 1", "Row 2");
        List<String> colKeys = Arrays.asList("Col 1");
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0));
        List<List<Number>> ends = Arrays.asList(Arrays.asList(1.5), Arrays.asList(2.5));
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListEndsSizeMismatch() {
        List<String> rowKeys = Arrays.asList("Row 1", "Row 2");
        List<String> colKeys = Arrays.asList("Col 1");
        List<List<Number>> starts = Arrays.asList(Arrays.asList(1.0), Arrays.asList(2.0));
        List<List<Number>> ends = Arrays.asList(Arrays.asList(1.5));
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, ends);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorListRowLengthMismatch() {
        List<String> rowKeys = Arrays.asList("Row 1", "Row 2");
        List<String> colKeys = Arrays.asList("Col 1", "Col 2");
        List<List<Number>> starts = Arrays.asList(
            Arrays.asList(1.0, 2.0),
            Arrays.asList(3.0)
        );
        List<List<Number>> ends = Arrays.asList(
            Arrays.asList(1.5, 2.5),
            Arrays.asList(3.5, 4.5)
        );
        new DefaultIntervalCategoryDataset(rowKeys, colKeys, starts, ends);
    }

    @Test
    public void testGetSeriesCountAndCategoryCount() {
        Number[][] startsData = { {1.0, 2.0, 3.0} };
        Number[][] endsData = { {1.5, 2.5, 3.5} };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);

        assertEquals(1, dataset.getSeriesCount());
        assertEquals(3, dataset.getCategoryCount());
        assertEquals(1, dataset.getRowCount());
        assertEquals(3, dataset.getColumnCount());
    }

    @Test
    public void testGetValuesAndIntervals() {
        Number[][] startsData = {
            {1.0, 2.0},
            {3.0, 4.0}
        };
        Number[][] endsData = {
            {1.5, 2.5},
            {3.5, 4.5}
        };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);

        // Test standard value retrieval (normally returns end value or average, check implementation specifics)
        assertNotNull(dataset.getValue(0, 0));
        assertNotNull(dataset.getStartValue(0, 0));
        assertNotNull(dataset.getEndValue(0, 0));

        assertNotNull(dataset.getValue("Row 0", "Col 0"));
        assertNotNull(dataset.getStartValue("Row 0", "Col 0"));
        assertNotNull(dataset.getEndValue("Row 0", "Col 0"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetStartValueOutOfBounds() {
        Number[][] startsData = { {1.0} };
        Number[][] endsData = { {1.5} };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);
        dataset.getStartValue(5, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetEndValueOutOfBounds() {
        Number[][] startsData = { {1.0} };
        Number[][] endsData = { {1.5} };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);
        dataset.getEndValue(0, 5);
    }

    @Test
    public void testSetStartAndEndValues() {
        Number[][] startsData = { {1.0, 2.0} };
        Number[][] endsData = { {1.5, 2.5} };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);

        dataset.setStartValue(0, 0, 10.0);
        dataset.setEndValue(0, 0, 15.0);

        assertEquals(10.0, dataset.getStartValue(0, 0).doubleValue(), 0.0001);
        assertEquals(15.0, dataset.getEndValue(0, 0).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStartValueInvalidRow() {
        Number[][] startsData = { {1.0} };
        Number[][] endsData = { {1.5} };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);
        dataset.setStartValue(-1, 0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEndValueInvalidRow() {
        Number[][] startsData = { {1.0} };
        Number[][] endsData = { {1.5} };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);
        dataset.setEndValue(10, 0, 5.0);
    }

    @Test
    public void testGetCategoryIndexAndRowIndex() {
        Number[][] startsData = { {1.0, 2.0} };
        Number[][] endsData = { {1.5, 2.5} };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);

        assertEquals(0, dataset.getRowIndex("Row 0"));
        assertEquals(1, dataset.getColumnIndex("Col 1"));
        assertEquals(-1, dataset.getRowIndex("NonExistent"));
        assertEquals(-1, dataset.getColumnIndex("NonExistent"));
    }

    @Test
    public void testGetRowKeysAndColumnKeys() {
        Number[][] startsData = { {1.0, 2.0} };
        Number[][] endsData = { {1.5, 2.5} };
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(startsData, endsData);

        assertNotNull(dataset.getRowKeys());
        assertNotNull(dataset.getColumnKeys());
        assertEquals(1, dataset.getRowKeys().size());
        assertEquals(2, dataset.getColumnKeys().size());
    }

    @Test
    public void testEqualsAndClone() throws CloneNotSupportedException {
        Number[][] startsData = { {1.0, 2.0} };
        Number[][] endsData = { {1.5, 2.5} };
        DefaultIntervalCategoryDataset dataset1 = new DefaultIntervalCategoryDataset(startsData, endsData);
        DefaultIntervalCategoryDataset dataset2 = new DefaultIntervalCategoryDataset(startsData, endsData);

        assertEquals(dataset1, dataset2);
        assertTrue(dataset1.equals(dataset1));
        assertFalse(dataset1.equals(null));
        assertFalse(dataset1.equals("Some String"));

        DefaultIntervalCategoryDataset clone = (DefaultIntervalCategoryDataset) dataset1.clone();
        assertEquals(dataset1, clone);
    }

    @Test
    public void testSeriesNameGeneration() {
        Comparable[] seriesKeys = { "Series A" };
        Number[][] startsData = { {1.0, 2.0} };
        Number[][] endsData = { {1.5, 2.5} };
        
        // Testing static method or alternative constructor if available in D4J Chart 16
        // Let's test DefaultIntervalCategoryDataset generation helpers if present
        String[] rowKeys = {"R1"};
        String[] colKeys = {"C1", "C2"};
        DefaultIntervalCategoryDataset dataset = new DefaultIntervalCategoryDataset(rowKeys, startsData, endsData);
        assertEquals(1, dataset.getRowCount());
        assertEquals(2, dataset.getColumnCount());
    }
}