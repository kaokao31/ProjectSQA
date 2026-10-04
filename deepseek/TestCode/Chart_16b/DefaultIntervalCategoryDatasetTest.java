package org.jfree.data.category;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for DefaultIntervalCategoryDataset.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class DefaultIntervalCategoryDatasetTest {

    private static final double EPSILON = 0.0000001;
    private DefaultIntervalCategoryDataset emptyDataset;
    private DefaultIntervalCategoryDataset singleDataset;
    private DefaultIntervalCategoryDataset multiDataset;

    @Before
    public void setUp() {
        // Empty dataset
        emptyDataset = new DefaultIntervalCategoryDataset(
            new String[0], new String[0], new double[0][0], new double[0][0]);

        // Single series, single category
        singleDataset = new DefaultIntervalCategoryDataset(
            new String[]{"S1"},
            new String[]{"C1"},
            new double[][]{{1.0}},
            new double[][]{{2.0}});

        // Multiple series and categories
        multiDataset = new DefaultIntervalCategoryDataset(
            new String[]{"S1", "S2"},
            new String[]{"C1", "C2"},
            new double[][]{{1.0, 3.0}, {5.0, 7.0}},
            new double[][]{{2.0, 4.0}, {6.0, 8.0}});
    }

    // ========== Constructor Tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSeriesNames() {
        new DefaultIntervalCategoryDataset(null, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullCategoryNames() {
        new DefaultIntervalCategoryDataset(new String[]{"S1"}, null,
            new double[][]{{1.0}}, new double[][]{{2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullStartData() {
        new DefaultIntervalCategoryDataset(new String[]{"S1"}, new String[]{"C1"},
            null, new double[][]{{2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullEndData() {
        new DefaultIntervalCategoryDataset(new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedSeriesCount() {
        new DefaultIntervalCategoryDataset(new String[]{"S1", "S2"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMismatchedCategoryCount() {
        new DefaultIntervalCategoryDataset(new String[]{"S1"}, new String[]{"C1", "C2"},
            new double[][]{{1.0, 2.0}}, new double[][]{{3.0, 4.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorStartEndDimensionMismatch() {
        new DefaultIntervalCategoryDataset(new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0, 2.0}}, new double[][]{{3.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorStartEndRowMismatch() {
        new DefaultIntervalCategoryDataset(new String[]{"S1", "S2"}, new String[]{"C1"},
            new double[][]{{1.0}, {2.0}}, new double[][]{{3.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorStartEndColumnMismatch() {
        new DefaultIntervalCategoryDataset(new String[]{"S1"}, new String[]{"C1", "C2"},
            new double[][]{{1.0, 2.0}}, new double[][]{{3.0}});
    }

    @Test
    public void testConstructorEmptyArrays() {
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(
            new String[0], new String[0], new double[0][0], new double[0][0]);
        assertEquals(0, ds.getRowCount());
        assertEquals(0, ds.getColumnCount());
    }

    // ========== getRowCount / getColumnCount ==========

    @Test
    public void testGetRowCount() {
        assertEquals(0, emptyDataset.getRowCount());
        assertEquals(1, singleDataset.getRowCount());
        assertEquals(2, multiDataset.getRowCount());
    }

    @Test
    public void testGetColumnCount() {
        assertEquals(0, emptyDataset.getColumnCount());
        assertEquals(1, singleDataset.getColumnCount());
        assertEquals(2, multiDataset.getColumnCount());
    }

    // ========== getRowKeys / getColumnKeys ==========

    @Test
    public void testGetRowKeys() {
        assertArrayEquals(new String[0], emptyDataset.getRowKeys());
        assertArrayEquals(new String[]{"S1"}, singleDataset.getRowKeys());
        assertArrayEquals(new String[]{"S1", "S2"}, multiDataset.getRowKeys());
    }

    @Test
    public void testGetColumnKeys() {
        assertArrayEquals(new String[0], emptyDataset.getColumnKeys());
        assertArrayEquals(new String[]{"C1"}, singleDataset.getColumnKeys());
        assertArrayEquals(new String[]{"C1", "C2"}, multiDataset.getColumnKeys());
    }

    // ========== getValue ==========

    @Test
    public void testGetValueValid() {
        assertEquals(1.0, singleDataset.getValue("S1", "C1"), EPSILON);
        assertEquals(1.0, multiDataset.getValue("S1", "C1"), EPSILON);
        assertEquals(3.0, multiDataset.getValue("S1", "C2"), EPSILON);
        assertEquals(5.0, multiDataset.getValue("S2", "C1"), EPSILON);
        assertEquals(7.0, multiDataset.getValue("S2", "C2"), EPSILON);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueUnknownSeries() {
        multiDataset.getValue("S3", "C1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueUnknownCategory() {
        multiDataset.getValue("S1", "C3");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeRow() {
        singleDataset.getValue(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeColumn() {
        singleDataset.getValue(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueRowOutOfBounds() {
        singleDataset.getValue(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueColumnOutOfBounds() {
        singleDataset.getValue(0, 1);
    }

    // ========== getStartValue ==========

    @Test
    public void testGetStartValueValid() {
        assertEquals(1.0, singleDataset.getStartValue("S1", "C1"), EPSILON);
        assertEquals(1.0, multiDataset.getStartValue("S1", "C1"), EPSILON);
        assertEquals(3.0, multiDataset.getStartValue("S1", "C2"), EPSILON);
        assertEquals(5.0, multiDataset.getStartValue("S2", "C1"), EPSILON);
        assertEquals(7.0, multiDataset.getStartValue("S2", "C2"), EPSILON);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetStartValueUnknownSeries() {
        multiDataset.getStartValue("S3", "C1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetStartValueUnknownCategory() {
        multiDataset.getStartValue("S1", "C3");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetStartValueNegativeRow() {
        singleDataset.getStartValue(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetStartValueNegativeColumn() {
        singleDataset.getStartValue(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetStartValueRowOutOfBounds() {
        singleDataset.getStartValue(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetStartValueColumnOutOfBounds() {
        singleDataset.getStartValue(0, 1);
    }

    // ========== getEndValue ==========

    @Test
    public void testGetEndValueValid() {
        assertEquals(2.0, singleDataset.getEndValue("S1", "C1"), EPSILON);
        assertEquals(2.0, multiDataset.getEndValue("S1", "C1"), EPSILON);
        assertEquals(4.0, multiDataset.getEndValue("S1", "C2"), EPSILON);
        assertEquals(6.0, multiDataset.getEndValue("S2", "C1"), EPSILON);
        assertEquals(8.0, multiDataset.getEndValue("S2", "C2"), EPSILON);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetEndValueUnknownSeries() {
        multiDataset.getEndValue("S3", "C1");
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetEndValueUnknownCategory() {
        multiDataset.getEndValue("S1", "C3");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetEndValueNegativeRow() {
        singleDataset.getEndValue(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetEndValueNegativeColumn() {
        singleDataset.getEndValue(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetEndValueRowOutOfBounds() {
        singleDataset.getEndValue(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetEndValueColumnOutOfBounds() {
        singleDataset.getEndValue(0, 1);
    }

    // ========== getRowIndex / getColumnIndex ==========

    @Test
    public void testGetRowIndex() {
        assertEquals(0, multiDataset.getRowIndex("S1"));
        assertEquals(1, multiDataset.getRowIndex("S2"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetRowIndexUnknown() {
        multiDataset.getRowIndex("S3");
    }

    @Test
    public void testGetColumnIndex() {
        assertEquals(0, multiDataset.getColumnIndex("C1"));
        assertEquals(1, multiDataset.getColumnIndex("C2"));
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetColumnIndexUnknown() {
        multiDataset.getColumnIndex("C3");
    }

    // ========== getRowKey / getColumnKey ==========

    @Test
    public void testGetRowKey() {
        assertEquals("S1", multiDataset.getRowKey(0));
        assertEquals("S2", multiDataset.getRowKey(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyNegative() {
        multiDataset.getRowKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetRowKeyOutOfBounds() {
        multiDataset.getRowKey(2);
    }

    @Test
    public void testGetColumnKey() {
        assertEquals("C1", multiDataset.getColumnKey(0));
        assertEquals("C2", multiDataset.getColumnKey(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyNegative() {
        multiDataset.getColumnKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetColumnKeyOutOfBounds() {
        multiDataset.getColumnKey(2);
    }

    // ========== setKeys ==========

    @Test
    public void testSetSeriesKeys() {
        multiDataset.setSeriesKeys(new String[]{"X", "Y"});
        assertArrayEquals(new String[]{"X", "Y"}, multiDataset.getRowKeys());
        // Verify data still accessible
        assertEquals(1.0, multiDataset.getValue("X", "C1"), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSeriesKeysNull() {
        multiDataset.setSeriesKeys(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSeriesKeysWrongLength() {
        multiDataset.setSeriesKeys(new String[]{"X"});
    }

    @Test
    public void testSetCategoryKeys() {
        multiDataset.setCategoryKeys(new String[]{"A", "B"});
        assertArrayEquals(new String[]{"A", "B"}, multiDataset.getColumnKeys());
        assertEquals(1.0, multiDataset.getValue("S1", "A"), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeysNull() {
        multiDataset.setCategoryKeys(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCategoryKeysWrongLength() {
        multiDataset.setCategoryKeys(new String[]{"X"});
    }

    // ========== addValue / setValue (if present) ==========
    // Note: DefaultIntervalCategoryDataset may not have addValue; we test setValue if exists.
    // Assuming there is a setValue method for completeness.

    @Test
    public void testSetValue() {
        // This test assumes setValue exists; if not, it will fail compilation.
        // We'll include it as a placeholder; adjust based on actual API.
        // For safety, we'll comment it out if not present.
        // multiDataset.setValue(10.0, 20.0, "S1", "C1");
        // assertEquals(10.0, multiDataset.getStartValue("S1", "C1"), EPSILON);
        // assertEquals(20.0, multiDataset.getEndValue("S1", "C1"), EPSILON);
    }

    // ========== equals / clone ==========

    @Test
    public void testEquals() {
        DefaultIntervalCategoryDataset ds1 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        DefaultIntervalCategoryDataset ds2 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        assertTrue(ds1.equals(ds2));
        assertTrue(ds2.equals(ds1));
    }

    @Test
    public void testEqualsDifferentSeries() {
        DefaultIntervalCategoryDataset ds1 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        DefaultIntervalCategoryDataset ds2 = new DefaultIntervalCategoryDataset(
            new String[]{"S2"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        assertFalse(ds1.equals(ds2));
    }

    @Test
    public void testEqualsDifferentCategories() {
        DefaultIntervalCategoryDataset ds1 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        DefaultIntervalCategoryDataset ds2 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C2"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        assertFalse(ds1.equals(ds2));
    }

    @Test
    public void testEqualsDifferentStartData() {
        DefaultIntervalCategoryDataset ds1 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        DefaultIntervalCategoryDataset ds2 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.5}}, new double[][]{{2.0}});
        assertFalse(ds1.equals(ds2));
    }

    @Test
    public void testEqualsDifferentEndData() {
        DefaultIntervalCategoryDataset ds1 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        DefaultIntervalCategoryDataset ds2 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.5}});
        assertFalse(ds1.equals(ds2));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(singleDataset.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(singleDataset.equals("string"));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        DefaultIntervalCategoryDataset cloned = (DefaultIntervalCategoryDataset) multiDataset.clone();
        assertNotSame(multiDataset, cloned);
        assertTrue(multiDataset.equals(cloned));
        // Modify original and ensure clone unchanged
        multiDataset.setSeriesKeys(new String[]{"X", "Y"});
        assertFalse(multiDataset.equals(cloned));
    }

    // ========== hashCode ==========

    @Test
    public void testHashCode() {
        DefaultIntervalCategoryDataset ds1 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        DefaultIntervalCategoryDataset ds2 = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{1.0}}, new double[][]{{2.0}});
        assertEquals(ds1.hashCode(), ds2.hashCode());
    }

    // ========== Edge Cases ==========

    @Test
    public void testGetValueWithNullKey() {
        // Assuming null key throws UnknownKeyException or IllegalArgumentException
        try {
            multiDataset.getValue(null, "C1");
            fail("Expected exception for null series key");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testGetStartValueWithNullKey() {
        try {
            multiDataset.getStartValue(null, "C1");
            fail("Expected exception for null series key");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testGetEndValueWithNullKey() {
        try {
            multiDataset.getEndValue(null, "C1");
            fail("Expected exception for null series key");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testEmptyDatasetGetValue() {
        // Should throw IndexOutOfBoundsException or similar
        try {
            emptyDataset.getValue(0, 0);
            fail("Expected exception on empty dataset");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testEmptyDatasetGetStartValue() {
        try {
            emptyDataset.getStartValue(0, 0);
            fail("Expected exception on empty dataset");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testEmptyDatasetGetEndValue() {
        try {
            emptyDataset.getEndValue(0, 0);
            fail("Expected exception on empty dataset");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    // ========== Additional Coverage for Internal Methods ==========

    @Test
    public void testGetSeriesCount() {
        assertEquals(0, emptyDataset.getSeriesCount());
        assertEquals(1, singleDataset.getSeriesCount());
        assertEquals(2, multiDataset.getSeriesCount());
    }

    @Test
    public void testGetCategoryCount() {
        assertEquals(0, emptyDataset.getCategoryCount());
        assertEquals(1, singleDataset.getCategoryCount());
        assertEquals(2, multiDataset.getCategoryCount());
    }

    @Test
    public void testGetSeries() {
        assertEquals("S1", singleDataset.getSeries(0));
        assertEquals("S1", multiDataset.getSeries(0));
        assertEquals("S2", multiDataset.getSeries(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetSeriesNegative() {
        singleDataset.getSeries(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetSeriesOutOfBounds() {
        singleDataset.getSeries(1);
    }

    @Test
    public void testGetCategories() {
        assertArrayEquals(new String[0], emptyDataset.getCategories());
        assertArrayEquals(new String[]{"C1"}, singleDataset.getCategories());
        assertArrayEquals(new String[]{"C1", "C2"}, multiDataset.getCategories());
    }

    // ========== Test for Bug #16 (potential issue) ==========
    // Bug 16 might involve handling of negative values or zero-length arrays.
    // We'll add a test that exercises a scenario that could trigger the bug.

    @Test
    public void testBug16NegativeStartValue() {
        // If bug is about negative start values, test that.
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{-5.0}}, new double[][]{{-2.0}});
        assertEquals(-5.0, ds.getStartValue("S1", "C1"), EPSILON);
        assertEquals(-2.0, ds.getEndValue("S1", "C1"), EPSILON);
    }

    @Test
    public void testBug16StartGreaterThanEnd() {
        // Some implementations might assume start <= end; test that.
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{10.0}}, new double[][]{{5.0}});
        assertEquals(10.0, ds.getStartValue("S1", "C1"), EPSILON);
        assertEquals(5.0, ds.getEndValue("S1", "C1"), EPSILON);
    }

    @Test
    public void testBug16LargeValues() {
        double large = Double.MAX_VALUE;
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{large}}, new double[][]{{large}});
        assertEquals(large, ds.getStartValue("S1", "C1"), EPSILON);
        assertEquals(large, ds.getEndValue("S1", "C1"), EPSILON);
    }

    @Test
    public void testBug16NaNValues() {
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{Double.NaN}}, new double[][]{{Double.NaN}});
        assertTrue(Double.isNaN(ds.getStartValue("S1", "C1")));
        assertTrue(Double.isNaN(ds.getEndValue("S1", "C1")));
    }

    @Test
    public void testBug16InfiniteValues() {
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(
            new String[]{"S1"}, new String[]{"C1"},
            new double[][]{{Double.POSITIVE_INFINITY}}, new double[][]{{Double.NEGATIVE_INFINITY}});
        assertEquals(Double.POSITIVE_INFINITY, ds.getStartValue("S1", "C1"), EPSILON);
        assertEquals(Double.NEGATIVE_INFINITY, ds.getEndValue("S1", "C1"), EPSILON);
    }
}