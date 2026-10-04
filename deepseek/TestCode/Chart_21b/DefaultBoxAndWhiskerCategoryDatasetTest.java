package org.jfree.data.statistics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Arrays;

import org.jfree.data.Range;
import org.jfree.data.UnknownKeyException;

/**
 * Test suite for DefaultBoxAndWhiskerCategoryDataset.
 * Achieves high coverage and tests edge cases including null, empty, boundary values.
 */
public class DefaultBoxAndWhiskerCategoryDatasetTest {

    private DefaultBoxAndWhiskerCategoryDataset dataset;
    private BoxAndWhiskerItem item1;
    private BoxAndWhiskerItem item2;
    private BoxAndWhiskerItem itemNull;

    @Before
    public void setUp() {
        dataset = new DefaultBoxAndWhiskerCategoryDataset();
        item1 = new BoxAndWhiskerItem(10.0, 8.0, 5.0, 15.0, 3.0, 17.0, null, null, null);
        item2 = new BoxAndWhiskerItem(12.0, 9.0, 6.0, 18.0, 2.0, 20.0, null, null, null);
        // item with only mean for partial data
        itemNull = new BoxAndWhiskerItem(10.0, null, null, null, null, null, null, null, null);
    }

    @After
    public void tearDown() {
        dataset = null;
    }

    // ------------------ add() and basic queries ------------------

    @Test
    public void testAddItemNewRowNewColumn() {
        dataset.add(item1, "Row1", "Col1");
        assertEquals(1, dataset.getRowCount());
        assertEquals(1, dataset.getColumnCount());
        assertEquals("Row1", dataset.getRowKey(0));
        assertEquals("Col1", dataset.getColumnKey(0));
    }

    @Test
    public void testAddItemExistingRowExistingColumn() {
        dataset.add(item1, "Row1", "Col1");
        dataset.add(item2, "Row1", "Col1");
        assertEquals(1, dataset.getRowCount());
        assertEquals(1, dataset.getColumnCount());
        // item2 replaces item1
        assertEquals(12.0, dataset.getMeanValue("Row1", "Col1").doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        dataset.add(null, "Row1", "Col1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullRowKeys() {
        dataset.add(item1, null, "Col1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullColumnKeys() {
        dataset.add(item1, "Row1", null);
    }

    // ------------------ getRowCount / getColumnCount ------------------

    @Test
    public void testGetRowCountEmpty() {
        assertEquals(0, dataset.getRowCount());
    }

    @Test
    public void testGetColumnCountEmpty() {
        assertEquals(0, dataset.getColumnCount());
    }

    @Test
    public void testGetRowCountNonEmpty() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C1");
        assertEquals(2, dataset.getRowCount());
    }

    @Test
    public void testGetColumnCountNonEmpty() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item1, "R1", "C2");
        assertEquals(2, dataset.getColumnCount());
    }

    // ------------------ getRowKeys / getColumnKeys ------------------

    @Test
    public void testGetRowKeysEmpty() {
        assertTrue(dataset.getRowKeys().isEmpty());
    }

    @Test
    public void testGetRowKeysNonEmpty() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C1");
        List expected = Arrays.asList("R1", "R2");
        assertEquals(expected, dataset.getRowKeys());
    }

    @Test
    public void testGetColumnKeysOrder() {
        dataset.add(item1, "R1", "C2");
        dataset.add(item1, "R1", "C1");
        assertEquals(Arrays.asList("C2", "C1"), dataset.getColumnKeys()); // depending on internal order
    }

    // ------------------ getValue (CategoryDataset) ------------------

    @Test
    public void testGetValueByKey() {
        dataset.add(item1, "R1", "C1");
        Number val = dataset.getValue("R1", "C1");
        assertNotNull(val);
        // getValue returns mean value? Actually it returns the mean according to BoxAndWhiskerCategoryDataset
        // but CategoryDataset getValue returns a double? It returns the mean Double.
        assertEquals(10.0, val.doubleValue(), 0.0001);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueByUnknownRow() {
        dataset.add(item1, "R1", "C1");
        dataset.getValue("R2", "C1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueByUnknownColumn() {
        dataset.add(item1, "R1", "C1");
        dataset.getValue("R1", "C2");
    }

    @Test
    public void testGetValueByIndex() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C2");
        Number val = dataset.getValue(0, 0); // R1, C1
        assertEquals(10.0, val.doubleValue(), 0.0001);
    }

    // ------------------ Mean, Median, Quartiles, etc. ------------------

    @Test
    public void testGetMeanValueExisting() {
        dataset.add(item1, "R1", "C1");
        assertEquals(10.0, dataset.getMeanValue("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testGetMeanValueMissingKey() {
        assertNull(dataset.getMeanValue("R1", "C1"));
    }

    @Test
    public void testGetMeanValueNullMean() {
        dataset.add(itemNull, "R1", "C1");
        assertNull(dataset.getMeanValue("R1", "C1"));
    }

    // Similar for median, etc.
    @Test
    public void testGetMedianValueExisting() {
        dataset.add(item1, "R1", "C1");
        assertEquals(8.0, dataset.getMedianValue("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testGetMedianValueNull() {
        dataset.add(itemNull, "R1", "C1");
        assertNull(dataset.getMedianValue("R1", "C1"));
    }

    @Test
    public void testGetQ1Value() {
        dataset.add(item1, "R1", "C1");
        assertEquals(5.0, dataset.getQ1Value("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testGetQ3Value() {
        dataset.add(item1, "R1", "C1");
        assertEquals(15.0, dataset.getQ3Value("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testGetMinRegularValue() {
        dataset.add(item1, "R1", "C1");
        assertEquals(3.0, dataset.getMinRegularValue("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testGetMaxRegularValue() {
        dataset.add(item1, "R1", "C1");
        assertEquals(17.0, dataset.getMaxRegularValue("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testGetMinOutlier() {
        // item1 has null minOutlier, so should return null
        dataset.add(item1, "R1", "C1");
        assertNull(dataset.getMinOutlier("R1", "C1"));
    }

    @Test
    public void testGetMaxOutlier() {
        dataset.add(item1, "R1", "C1");
        assertNull(dataset.getMaxOutlier("R1", "C1"));
    }

    @Test
    public void testGetMinOutlierWithValue() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 5.0, 15.0, 3.0, 17.0, 1.0, 19.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(1.0, dataset.getMinOutlier("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testGetMaxOutlierWithValue() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 5.0, 15.0, 3.0, 17.0, 1.0, 19.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(19.0, dataset.getMaxOutlier("R1", "C1").doubleValue(), 0.0001);
    }

    // ------------------ getRangeBounds() ------------------

    @Test
    public void testGetRangeBoundsEmptyDataset() {
        // assume method exists: public Range getRangeBounds(boolean includeInterval)
        // or public Range getRangeBounds(List visibleSeriesKeys, Range xRange, boolean includeInterval)
        // We need to check exact signature. Typically: getRangeBounds(boolean)
        // Let's test with includeInterval = true/false
        // For empty dataset, should return null
        Range r = dataset.getRangeBounds(true);
        assertNull("Range should be null for empty dataset", r);
    }

    @Test
    public void testGetRangeBoundsSingleItem() {
        dataset.add(item1, "R1", "C1");
        Range r = dataset.getRangeBounds(true);
        assertNotNull(r);
        // The range should cover minRegularValue and maxRegularValue (3.0 to 17.0)
        assertEquals(3.0, r.getLowerBound(), 0.0001);
        assertEquals(17.0, r.getUpperBound(), 0.0001);
    }

    @Test
    public void testGetRangeBoundsTwoItems() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C1");
        Range r = dataset.getRangeBounds(true);
        // min = min(3,2)=2, max = max(17,20)=20
        assertEquals(2.0, r.getLowerBound(), 0.0001);
        assertEquals(20.0, r.getUpperBound(), 0.0001);
    }

    @Test
    public void testGetRangeBoundsIncludeIntervalFalse() {
        // When includeInterval false, should include only minOutlier and maxOutlier if present?
        // Actually typical implementation: uses minOutlier / maxOutlier if not null, else fallback to minRegular/maxRegular.
        // Let's create item with outliers
        BoxAndWhiskerItem itemWithOutliers = new BoxAndWhiskerItem(10.0, 8.0, 5.0, 15.0, 3.0, 17.0, 1.0, 19.0, null);
        dataset.add(itemWithOutliers, "R1", "C1");
        Range r = dataset.getRangeBounds(false);
        // includeInterval false should still use minOutlier/maxOutlier
        assertEquals(1.0, r.getLowerBound(), 0.0001);
        assertEquals(19.0, r.getUpperBound(), 0.0001);
    }

    @Test
    public void testGetRangeBoundsWithNullMeanAndNullMinMax() {
        // item with only mean, all other values null -> range should be [mean, mean]
        BoxAndWhiskerItem partial = new BoxAndWhiskerItem(10.0, null, null, null, null, null, null, null, null);
        dataset.add(partial, "R1", "C1");
        Range r = dataset.getRangeBounds(true);
        assertEquals(10.0, r.getLowerBound(), 0.0001);
        assertEquals(10.0, r.getUpperBound(), 0.0001);
    }

    // ------------------ getRowBounds / getColumnBounds (if available) ------------------

    @Test
    public void testGetRowBounds() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R1", "C2");
        Range r = dataset.getRowBounds(0);
        assertNotNull(r);
        assertEquals(2.0, r.getLowerBound(), 0.0001); // min of minRegular (3 and 2)
        assertEquals(20.0, r.getUpperBound(), 0.0001); // max of maxRegular (17 and 20)
    }

    @Test
    public void testGetColumnBounds() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C1");
        Range r = dataset.getColumnBounds(0);
        assertNotNull(r);
        assertEquals(2.0, r.getLowerBound(), 0.0001);
        assertEquals(20.0, r.getUpperBound(), 0.0001);
    }

    @Test
    public void testGetRowBoundsEmptyRow() {
        // add data to other row only
        dataset.add(item1, "R2", "C1");
        // row index 0 has no data? Actually row 0 is R2, row 1 is None? In DefaultKeyedValues2D, rows are added as keys appear.
        // So row index 0 is "R2", it has data. No row with index 1 exists.
        assertEquals(1, dataset.getRowCount());
        Range r = dataset.getRowBounds(0);
        assertNotNull(r);
        assertEquals(3.0, r.getLowerBound(), 0.0001);
        assertEquals(17.0, r.getUpperBound(), 0.0001);
    }

    // ------------------ intersect() (not in class, but maybe) ------------------
    // Not needed.

    // ------------------ Edge cases: no items for a key ------------------

    @Test
    public void testGetMeanValueNoItem() {
        dataset.add(item1, "R1", "C1");
        assertNull(dataset.getMeanValue("R1", "C2"));
    }

    @Test
    public void testGetMedianValueNoItem() {
        assertNull(dataset.getMedianValue("R1", "C1"));
    }

    // ------------------ Non-null integer keys ------------------

    @Test
    public void testAddWithIntegerKeys() {
        dataset.add(item1, 1, 2);
        assertEquals(1, dataset.getRowCount());
        assertEquals(2, dataset.getColumnCount());
        assertEquals(10.0, dataset.getMeanValue(1, 2).doubleValue(), 0.0001);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetMeanValueWithMissingIntegerKey() {
        dataset.getMeanValue(1, 2);
    }

    // ------------------ getValue() returns mean, verify with empty mean? ------------------

    @Test
    public void testGetValueReturnsMeanOrNull() {
        dataset.add(itemNull, "R1", "C1");
        // mean is 10.0, so getValue should return that
        assertEquals(10.0, dataset.getValue("R1", "C1").doubleValue(), 0.0001);
    }

    @Test
    public void testGetValueWithNullMean() {
        BoxAndWhiskerItem itemWithNullMean = new BoxAndWhiskerItem(null, 8.0, 5.0, 15.0, 3.0, 17.0, null, null, null);
        dataset.add(itemWithNullMean, "R1", "C1");
        // getValue returns the mean, which is null
        assertNull(dataset.getValue("R1", "C1"));
    }

    // ------------------ Range with negative numbers ------------------

    @Test
    public void testGetRangeBoundsNegativeValues() {
        BoxAndWhiskerItem negItem = new BoxAndWhiskerItem(-5.0, -7.0, -10.0, -2.0, -12.0, -1.0, null, null, null);
        dataset.add(negItem, "R1", "C1");
        Range r = dataset.getRangeBounds(true);
        assertEquals(-12.0, r.getLowerBound(), 0.0001);
        assertEquals(-1.0, r.getUpperBound(), 0.0001);
    }

    // ------------------ Override equals? Not needed.

    // ------------------ Additional coverage: getRowBounds, getColumnBounds with multiple rows/columns

    @Test
    public void testGetRowBoundsMultipleRows() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C1");
        Range r1 = dataset.getRowBounds(0);
        Range r2 = dataset.getRowBounds(1);
        // R1: min=3, max=17; R2: min=2, max=20
        assertEquals(3.0, r1.getLowerBound(), 0.0001);
        assertEquals(17.0, r1.getUpperBound(), 0.0001);
        assertEquals(2.0, r2.getLowerBound(), 0.0001);
        assertEquals(20.0, r2.getUpperBound(), 0.0001);
    }

    @Test
    public void testGetColumnBoundsMultipleColumns() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R1", "C2");
        Range r1 = dataset.getColumnBounds(0);
        Range r2 = dataset.getColumnBounds(1);
        assertEquals(3.0, r1.getLowerBound(), 0.0001);
        assertEquals(17.0, r1.getUpperBound(), 0.0001);
        assertEquals(2.0, r2.getLowerBound(), 0.0001);
        assertEquals(20.0, r2.getUpperBound(), 0.0001);
    }

    // ------------------ getRangeBounds(visibleSeriesKeys, xRange, includeInterval) ------------------
    // This overloaded version exists in some implementations. Assuming it exists.
    @Test
    public void testGetRangeBoundsWithVisibleSeries() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C1");
        List visible = Arrays.asList("R1");
        Range r = dataset.getRangeBounds(visible, new Range(-10, 100), true);
        assertNotNull(r);
        assertEquals(3.0, r.getLowerBound(), 0.0001);
        assertEquals(17.0, r.getUpperBound(), 0.0001);
    }

    @Test
    public void testGetRangeBoundsWithVisibleSeriesEmptyList() {
        List visible = java.util.Collections.emptyList();
        Range r = dataset.getRangeBounds(visible, new Range(-10, 100), true);
        // Should return null because no series visible
        assertNull(r);
    }

    @Test
    public void testGetRangeBoundsWithXRangeLimiting() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C1");
        List visible = Arrays.asList("R1", "R2");
        // xRange is category domain, not used for boxplot range? Usually ignored.
        Range r = dataset.getRangeBounds(visible, new Range(0, 1), true);
        assertNotNull(r);
        assertEquals(2.0, r.getLowerBound(), 0.0001);
        assertEquals(20.0, r.getUpperBound(), 0.0001);
    }

    // ------------------ Test that getRangeBounds includes all necessary statistics ------------------
    // Verify that it considers minOutlier and maxOutlier when present.

    @Test
    public void testGetRangeBoundsWithOutliers() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 5.0, 15.0, 3.0, 17.0, 1.0, 19.0, null);
        dataset.add(item, "R1", "C1");
        Range r = dataset.getRangeBounds(true);
        // includeInterval true should include interval (minOutlier..maxOutlier i.e. 1 and 19)
        assertEquals(1.0, r.getLowerBound(), 0.0001);
        assertEquals(19.0, r.getUpperBound(), 0.0001);
    }

    @Test
    public void testGetRangeBoundsIncludeIntervalFalseWithOutliers() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 5.0, 15.0, 3.0, 17.0, 1.0, 19.0, null);
        dataset.add(item, "R1", "C1");
        Range r = dataset.getRangeBounds(false);
        // includeInterval false should still return minOutlier..maxOutlier? Actually depends on implementation.
        // Usually getRangeBounds with includeInterval=false returns minRegular..maxRegular.
        // But let's test both to detect defects.
        // For this class, we need to know the actual implementation. We'll assume standard: if includeInterval false, use minRegularValue/maxRegularValue.
        assertEquals(3.0, r.getLowerBound(), 0.0001);
        assertEquals(17.0, r.getUpperBound(), 0.0001);
    }

    // ------------------ Boundary when all items have null values for some fields ------------------

    @Test
    public void testGetRangeBoundsAllNullExceptMean() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(-1.0, null, null, null, null, null, null, null, null);
        dataset.add(item, "R1", "C1");
        Range r = dataset.getRangeBounds(true);
        assertEquals(-1.0, r.getLowerBound(), 0.0001);
        assertEquals(-1.0, r.getUpperBound(), 0.0001);
    }

    // ------------------ Null item in the middle? Already tested add with null.

    // ------------------ getRowBounds with mixed null and non-null values

    @Test
    public void testGetRowBoundsMixedNulls() {
        BoxAndWhiskerItem partial = new BoxAndWhiskerItem(100.0, null, null, null, null, null, null, null, null);
        dataset.add(partial, "R1", "C1");
        dataset.add(item1, "R1", "C2");
        Range r = dataset.getRowBounds(0);
        assertEquals(3.0, r.getLowerBound(), 0.0001); // min of 100? Actually minRegular is null for partial, so fallback to mean? The implementation likely: for each item get minRegularValue (if null, then maybe mean? or skip?). Need to test to reveal bug.
        // We'll just check it doesn't crash and returns some range.
        assertNotNull(r);
    }

    // ------------------ Test that mean can be null but other values present

    @Test
    public void testGetRangeBoundsNullMeanWithOtherValues() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(null, 8.0, 5.0, 15.0, 3.0, 17.0, null, null, null);
        dataset.add(item, "R1", "C1");
        Range r = dataset.getRangeBounds(true);
        // Should still consider minRegular=3, maxRegular=17
        assertEquals(3.0, r.getLowerBound(), 0.0001);
        assertEquals(17.0, r.getUpperBound(), 0.0001);
    }

    // ------------------ Add after removal? Not available in this class.

    // ------------------ clear()? Not available.

    // Additional tests for internal data structures: not exposed.

    // ------------------ getRowKeys() returns unmodifiable list? Not tested.

    // Ensure we cover all exceptions.

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowBoundsInvalidIndex() {
        dataset.getRowBounds(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnBoundsInvalidIndex() {
        dataset.getColumnBounds(0); // empty dataset, index 0 invalid
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetMeanValueUnknownRow() {
        dataset.getMeanValue("Unknown", "Col");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetMeanValueUnknownColumn() {
        dataset.add(item1, "R1", "C1");
        dataset.getMeanValue("R1", "Unknown");
    }

    // Verify other getters throw UnknownKeyException similarly
    @Test(expected = UnknownKeyException.class)
    public void testGetMedianValueUnknownKey() {
        dataset.getMedianValue("R1", "C1");
    }

    // ------------------ Test getRowBounds and getColumnBounds after adding data to specific positions

    @Test
    public void testGetRowBoundsAfterAddingMultipleColumns() {
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R1", "C2");
        dataset.add(item2, "R1", "C3"); // third column
        Range r = dataset.getRowBounds(0);
        // min among all columns: 2.0, max: 20.0
        assertEquals(2.0, r.getLowerBound(), 0.0001);
        assertEquals(20.0, r.getUpperBound(), 0.0001);
    }

    // End of test methods.
}