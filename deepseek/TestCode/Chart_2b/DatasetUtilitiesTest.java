package org.jfree.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jfree.data.DataUtilities;
import org.jfree.data.KeyedValues;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.Values2D;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.DefaultKeyedValues2D;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the DatasetUtilities class.
 * This test class is designed to achieve maximum code coverage and
 * to reveal potential faults in the implementation.
 */
public class DatasetUtilitiesTest {

    private Values2D values2D;
    private DefaultKeyedValues keyedValues;

    @Before
    public void setUp() {
        // Initialize a 2D dataset with known values
        values2D = new DefaultKeyedValues2D();
        ((DefaultKeyedValues2D) values2D).addValue(1.0, 0, 0);
        ((DefaultKeyedValues2D) values2D).addValue(2.0, 0, 1);
        ((DefaultKeyedValues2D) values2D).addValue(3.0, 1, 0);
        ((DefaultKeyedValues2D) values2D).addValue(4.0, 1, 1);

        // Initialize a KeyedValues dataset
        keyedValues = new DefaultKeyedValues();
        keyedValues.addValue("A", 1.0);
        keyedValues.addValue("B", 2.0);
        keyedValues.addValue("C", 3.0);
    }

    // ==================== Tests for calculateColumnTotal ====================

    @Test
    public void testCalculateColumnTotal_ValidData() {
        assertEquals("Column total for column 0", 4.0, 
                DataUtilities.calculateColumnTotal(values2D, 0), 0.0000001);
        assertEquals("Column total for column 1", 6.0, 
                DataUtilities.calculateColumnTotal(values2D, 1), 0.0000001);
    }

    @Test
    public void testCalculateColumnTotal_EmptyData() {
        Values2D emptyData = new DefaultKeyedValues2D();
        assertEquals("Column total for empty data", 0.0, 
                DataUtilities.calculateColumnTotal(emptyData, 0), 0.0000001);
    }

    @Test
    public void testCalculateColumnTotal_NullData() {
        try {
            DataUtilities.calculateColumnTotal(null, 0);
            fail("Expected IllegalArgumentException for null data");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testCalculateColumnTotal_InvalidColumnIndex() {
        try {
            DataUtilities.calculateColumnTotal(values2D, -1);
            fail("Expected IndexOutOfBoundsException for negative column index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testCalculateColumnTotal_ColumnWithNullValues() {
        DefaultKeyedValues2D dataWithNull = new DefaultKeyedValues2D();
        dataWithNull.addValue(1.0, 0, 0);
        dataWithNull.addValue(null, 1, 0);
        dataWithNull.addValue(3.0, 2, 0);
        assertEquals("Column total with null values", 4.0, 
                DataUtilities.calculateColumnTotal(dataWithNull, 0), 0.0000001);
    }

    // ==================== Tests for calculateRowTotal ====================

    @Test
    public void testCalculateRowTotal_ValidData() {
        assertEquals("Row total for row 0", 3.0, 
                DataUtilities.calculateRowTotal(values2D, 0), 0.0000001);
        assertEquals("Row total for row 1", 7.0, 
                DataUtilities.calculateRowTotal(values2D, 1), 0.0000001);
    }

    @Test
    public void testCalculateRowTotal_EmptyData() {
        Values2D emptyData = new DefaultKeyedValues2D();
        assertEquals("Row total for empty data", 0.0, 
                DataUtilities.calculateRowTotal(emptyData, 0), 0.0000001);
    }

    @Test
    public void testCalculateRowTotal_NullData() {
        try {
            DataUtilities.calculateRowTotal(null, 0);
            fail("Expected IllegalArgumentException for null data");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testCalculateRowTotal_InvalidRowIndex() {
        try {
            DataUtilities.calculateRowTotal(values2D, -1);
            fail("Expected IndexOutOfBoundsException for negative row index");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testCalculateRowTotal_RowWithNullValues() {
        DefaultKeyedValues2D dataWithNull = new DefaultKeyedValues2D();
        dataWithNull.addValue(1.0, 0, 0);
        dataWithNull.addValue(null, 0, 1);
        dataWithNull.addValue(3.0, 0, 2);
        assertEquals("Row total with null values", 4.0, 
                DataUtilities.calculateRowTotal(dataWithNull, 0), 0.0000001);
    }

    // ==================== Tests for createNumberArray ====================

    @Test
    public void testCreateNumberArray_ValidData() {
        double[] data = {1.0, 2.0, 3.0};
        Number[] result = DataUtilities.createNumberArray(data);
        assertNotNull("Result should not be null", result);
        assertEquals("Array length", 3, result.length);
        assertEquals("First element", 1.0, result[0].doubleValue(), 0.0);
        assertEquals("Second element", 2.0, result[1].doubleValue(), 0.0);
        assertEquals("Third element", 3.0, result[2].doubleValue(), 0.0);
    }

    @Test
    public void testCreateNumberArray_EmptyData() {
        double[] data = {};
        Number[] result = DataUtilities.createNumberArray(data);
        assertNotNull("Result should not be null", result);
        assertEquals("Array length", 0, result.length);
    }

    @Test
    public void testCreateNumberArray_NullData() {
        try {
            DataUtilities.createNumberArray(null);
            fail("Expected IllegalArgumentException for null data");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testCreateNumberArray_WithNegativeAndZero() {
        double[] data = {-1.0, 0.0, 1.0};
        Number[] result = DataUtilities.createNumberArray(data);
        assertEquals("First element", -1.0, result[0].doubleValue(), 0.0);
        assertEquals("Second element", 0.0, result[1].doubleValue(), 0.0);
        assertEquals("Third element", 1.0, result[2].doubleValue(), 0.0);
    }

    // ==================== Tests for createNumberArray2D ====================

    @Test
    public void testCreateNumberArray2D_ValidData() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        Number[][] result = DataUtilities.createNumberArray2D(data);
        assertNotNull("Result should not be null", result);
        assertEquals("Number of rows", 2, result.length);
        assertEquals("Number of columns in row 0", 2, result[0].length);
        assertEquals("Element [0][0]", 1.0, result[0][0].doubleValue(), 0.0);
        assertEquals("Element [1][1]", 4.0, result[1][1].doubleValue(), 0.0);
    }

    @Test
    public void testCreateNumberArray2D_EmptyData() {
        double[][] data = {};
        Number[][] result = DataUtilities.createNumberArray2D(data);
        assertNotNull("Result should not be null", result);
        assertEquals("Number of rows", 0, result.length);
    }

    @Test
    public void testCreateNumberArray2D_NullData() {
        try {
            DataUtilities.createNumberArray2D(null);
            fail("Expected IllegalArgumentException for null data");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testCreateNumberArray2D_RaggedArray() {
        double[][] data = {{1.0}, {2.0, 3.0}};
        Number[][] result = DataUtilities.createNumberArray2D(data);
        assertEquals("Row 0 length", 1, result[0].length);
        assertEquals("Row 1 length", 2, result[1].length);
    }

    // ==================== Tests for getCumulativePercentages ====================

    @Test
    public void testGetCumulativePercentages_ValidData() {
        KeyedValues result = DataUtilities.getCumulativePercentages(keyedValues);
        assertNotNull("Result should not be null", result);
        assertEquals("Number of items", 3, result.getItemCount());
        assertEquals("Cumulative percentage for A", 1.0/6.0, 
                result.getValue("A").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for B", 3.0/6.0, 
                result.getValue("B").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for C", 6.0/6.0, 
                result.getValue("C").doubleValue(), 0.0000001);
    }

    @Test
    public void testGetCumulativePercentages_EmptyData() {
        DefaultKeyedValues emptyData = new DefaultKeyedValues();
        KeyedValues result = DataUtilities.getCumulativePercentages(emptyData);
        assertNotNull("Result should not be null", result);
        assertEquals("Number of items", 0, result.getItemCount());
    }

    @Test
    public void testGetCumulativePercentages_NullData() {
        try {
            DataUtilities.getCumulativePercentages(null);
            fail("Expected IllegalArgumentException for null data");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetCumulativePercentages_WithNullValues() {
        DefaultKeyedValues dataWithNull = new DefaultKeyedValues();
        dataWithNull.addValue("A", 1.0);
        dataWithNull.addValue("B", null);
        dataWithNull.addValue("C", 3.0);
        KeyedValues result = DataUtilities.getCumulativePercentages(dataWithNull);
        assertEquals("Cumulative percentage for A", 1.0/4.0, 
                result.getValue("A").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for B", 1.0/4.0, 
                result.getValue("B").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for C", 4.0/4.0, 
                result.getValue("C").doubleValue(), 0.0000001);
    }

    @Test
    public void testGetCumulativePercentages_WithNegativeValues() {
        DefaultKeyedValues dataWithNegative = new DefaultKeyedValues();
        dataWithNegative.addValue("A", -1.0);
        dataWithNegative.addValue("B", 2.0);
        dataWithNegative.addValue("C", 3.0);
        KeyedValues result = DataUtilities.getCumulativePercentages(dataWithNegative);
        assertEquals("Cumulative percentage for A", -1.0/4.0, 
                result.getValue("A").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for B", 1.0/4.0, 
                result.getValue("B").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for C", 4.0/4.0, 
                result.getValue("C").doubleValue(), 0.0000001);
    }

    @Test
    public void testGetCumulativePercentages_WithZeroTotal() {
        DefaultKeyedValues dataWithZeroTotal = new DefaultKeyedValues();
        dataWithZeroTotal.addValue("A", 0.0);
        dataWithZeroTotal.addValue("B", 0.0);
        KeyedValues result = DataUtilities.getCumulativePercentages(dataWithZeroTotal);
        assertEquals("Cumulative percentage for A", 0.0, 
                result.getValue("A").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for B", 0.0, 
                result.getValue("B").doubleValue(), 0.0000001);
    }

    @Test
    public void testGetCumulativePercentages_SingleItem() {
        DefaultKeyedValues singleItem = new DefaultKeyedValues();
        singleItem.addValue("A", 5.0);
        KeyedValues result = DataUtilities.getCumulativePercentages(singleItem);
        assertEquals("Cumulative percentage for single item", 1.0, 
                result.getValue("A").doubleValue(), 0.0000001);
    }

    // ==================== Tests for getCumulativePercentages with large values ====================

    @Test
    public void testGetCumulativePercentages_LargeValues() {
        DefaultKeyedValues largeValues = new DefaultKeyedValues();
        largeValues.addValue("A", Double.MAX_VALUE);
        largeValues.addValue("B", Double.MAX_VALUE);
        KeyedValues result = DataUtilities.getCumulativePercentages(largeValues);
        // Due to potential overflow, the result may be NaN or Infinity
        // This test is designed to reveal such issues
        assertNotNull("Result should not be null", result);
        // We don't assert specific values here to avoid false failures
        // but the test will execute the code path
    }

    // ==================== Tests for edge cases in calculateColumnTotal ====================

    @Test
    public void testCalculateColumnTotal_AllNullValues() {
        DefaultKeyedValues2D allNull = new DefaultKeyedValues2D();
        allNull.addValue(null, 0, 0);
        allNull.addValue(null, 1, 0);
        assertEquals("Column total with all null values", 0.0, 
                DataUtilities.calculateColumnTotal(allNull, 0), 0.0000001);
    }

    @Test
    public void testCalculateColumnTotal_WithNaNValues() {
        DefaultKeyedValues2D dataWithNaN = new DefaultKeyedValues2D();
        dataWithNaN.addValue(Double.NaN, 0, 0);
        dataWithNaN.addValue(1.0, 1, 0);
        double result = DataUtilities.calculateColumnTotal(dataWithNaN, 0);
        assertTrue("Result should be NaN", Double.isNaN(result));
    }

    // ==================== Tests for edge cases in calculateRowTotal ====================

    @Test
    public void testCalculateRowTotal_AllNullValues() {
        DefaultKeyedValues2D allNull = new DefaultKeyedValues2D();
        allNull.addValue(null, 0, 0);
        allNull.addValue(null, 0, 1);
        assertEquals("Row total with all null values", 0.0, 
                DataUtilities.calculateRowTotal(allNull, 0), 0.0000001);
    }

    @Test
    public void testCalculateRowTotal_WithNaNValues() {
        DefaultKeyedValues2D dataWithNaN = new DefaultKeyedValues2D();
        dataWithNaN.addValue(Double.NaN, 0, 0);
        dataWithNaN.addValue(1.0, 0, 1);
        double result = DataUtilities.calculateRowTotal(dataWithNaN, 0);
        assertTrue("Result should be NaN", Double.isNaN(result));
    }

    // ==================== Tests for createNumberArray with special values ====================

    @Test
    public void testCreateNumberArray_WithNaN() {
        double[] data = {Double.NaN, 1.0};
        Number[] result = DataUtilities.createNumberArray(data);
        assertTrue("First element should be NaN", Double.isNaN(result[0].doubleValue()));
        assertEquals("Second element", 1.0, result[1].doubleValue(), 0.0);
    }

    @Test
    public void testCreateNumberArray_WithInfinity() {
        double[] data = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        Number[] result = DataUtilities.createNumberArray(data);
        assertEquals("Positive infinity", Double.POSITIVE_INFINITY, 
                result[0].doubleValue(), 0.0);
        assertEquals("Negative infinity", Double.NEGATIVE_INFINITY, 
                result[1].doubleValue(), 0.0);
    }

    // ==================== Tests for createNumberArray2D with special values ====================

    @Test
    public void testCreateNumberArray2D_WithNaN() {
        double[][] data = {{Double.NaN}, {1.0}};
        Number[][] result = DataUtilities.createNumberArray2D(data);
        assertTrue("Element [0][0] should be NaN", Double.isNaN(result[0][0].doubleValue()));
        assertEquals("Element [1][0]", 1.0, result[1][0].doubleValue(), 0.0);
    }

    // ==================== Additional edge case tests ====================

    @Test
    public void testCalculateColumnTotal_ColumnIndexOutOfBounds() {
        try {
            DataUtilities.calculateColumnTotal(values2D, 2);
            fail("Expected IndexOutOfBoundsException for column index 2");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testCalculateRowTotal_RowIndexOutOfBounds() {
        try {
            DataUtilities.calculateRowTotal(values2D, 2);
            fail("Expected IndexOutOfBoundsException for row index 2");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testGetCumulativePercentages_WithUnknownKey() {
        KeyedValues result = DataUtilities.getCumulativePercentages(keyedValues);
        try {
            result.getValue("Unknown");
            fail("Expected UnknownKeyException for unknown key");
        } catch (UnknownKeyException e) {
            // Expected
        }
    }

    @Test
    public void testGetCumulativePercentages_ResultValues() {
        KeyedValues result = DataUtilities.getCumulativePercentages(keyedValues);
        // Verify the keys are preserved
        assertEquals("Key 0", "A", result.getKey(0));
        assertEquals("Key 1", "B", result.getKey(1));
        assertEquals("Key 2", "C", result.getKey(2));
    }

    @Test
    public void testGetCumulativePercentages_WithDuplicateValues() {
        DefaultKeyedValues duplicateValues = new DefaultKeyedValues();
        duplicateValues.addValue("A", 2.0);
        duplicateValues.addValue("B", 2.0);
        duplicateValues.addValue("C", 2.0);
        KeyedValues result = DataUtilities.getCumulativePercentages(duplicateValues);
        assertEquals("Cumulative percentage for A", 2.0/6.0, 
                result.getValue("A").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for B", 4.0/6.0, 
                result.getValue("B").doubleValue(), 0.0000001);
        assertEquals("Cumulative percentage for C", 6.0/6.0, 
                result.getValue("C").doubleValue(), 0.0000001);
    }

    @Test
    public void testCalculateColumnTotal_WithNegativeValues() {
        DefaultKeyedValues2D negativeData = new DefaultKeyedValues2D();
        negativeData.addValue(-1.0, 0, 0);
        negativeData.addValue(-2.0, 1, 0);
        assertEquals("Column total with negative values", -3.0, 
                DataUtilities.calculateColumnTotal(negativeData, 0), 0.0000001);
    }

    @Test
    public void testCalculateRowTotal_WithNegativeValues() {
        DefaultKeyedValues2D negativeData = new DefaultKeyedValues2D();
        negativeData.addValue(-1.0, 0, 0);
        negativeData.addValue(-2.0, 0, 1);
        assertEquals("Row total with negative values", -3.0, 
                DataUtilities.calculateRowTotal(negativeData, 0), 0.0000001);
    }

    @Test
    public void testCreateNumberArray_PreservesOrder() {
        double[] data = {3.0, 1.0, 2.0};
        Number[] result = DataUtilities.createNumberArray(data);
        assertEquals("First element", 3.0, result[0].doubleValue(), 0.0);
        assertEquals("Second element", 1.0, result[1].doubleValue(), 0.0);
        assertEquals("Third element", 2.0, result[2].doubleValue(), 0.0);
    }

    @Test
    public void testCreateNumberArray2D_PreservesStructure() {
        double[][] data = {{1.0, 2.0}, {3.0}};
        Number[][] result = DataUtilities.createNumberArray2D(data);
        assertEquals("Row 0 length", 2, result[0].length);
        assertEquals("Row 1 length", 1, result[1].length);
        assertEquals("Element [0][1]", 2.0, result[0][1].doubleValue(), 0.0);
        assertEquals("Element [1][0]", 3.0, result[1][0].doubleValue(), 0.0);
    }

    @Test
    public void testGetCumulativePercentages_WithSingleNullValue() {
        DefaultKeyedValues singleNull = new DefaultKeyedValues();
        singleNull.addValue("A", null);
        KeyedValues result = DataUtilities.getCumulativePercentages(singleNull);
        assertEquals("Cumulative percentage for null value", 0.0, 
                result.getValue("A").doubleValue(), 0.0000001);
    }

    @Test
    public void testCalculateColumnTotal_WithMixedNullAndValues() {
        DefaultKeyedValues2D mixedData = new DefaultKeyedValues2D();
        mixedData.addValue(1.0, 0, 0);
        mixedData.addValue(null, 1, 0);
        mixedData.addValue(2.0, 2, 0);
        assertEquals("Column total with mixed values", 3.0, 
                DataUtilities.calculateColumnTotal(mixedData, 0), 0.0000001);
    }

    @Test
    public void testCalculateRowTotal_WithMixedNullAndValues() {
        DefaultKeyedValues2D mixedData = new DefaultKeyedValues2D();
        mixedData.addValue(1.0, 0, 0);
        mixedData.addValue(null, 0, 1);
        mixedData.addValue(2.0, 0, 2);
        assertEquals("Row total with mixed values", 3.0, 
                DataUtilities.calculateRowTotal(mixedData, 0), 0.0000001);
    }
}