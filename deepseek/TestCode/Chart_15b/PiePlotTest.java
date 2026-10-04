package org.jfree.chart.plot;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

/**
 * Comprehensive JUnit 4 test suite for PiePlot, targeting maximum code coverage
 * and fault detection for Defects4J Chart-15.
 */
public class PiePlotTest {

    private PiePlot plot;
    private DefaultPieDataset emptyDataset;
    private DefaultPieDataset singleDataset;
    private DefaultPieDataset multiDataset;

    @Before
    public void setUp() {
        plot = new PiePlot();
        emptyDataset = new DefaultPieDataset();
        singleDataset = new DefaultPieDataset();
        singleDataset.setValue("A", 1.0);
        multiDataset = new DefaultPieDataset();
        multiDataset.setValue("A", 10.0);
        multiDataset.setValue("B", 20.0);
        multiDataset.setValue("C", 30.0);
        // Start with null dataset
        plot.setDataset(null);
    }

    // -------------------- Constructor Tests --------------------
    @Test
    public void testConstructorWithNullDataset() {
        PiePlot p = new PiePlot(null);
        assertNull("Dataset should be null", p.getDataset());
    }

    @Test
    public void testConstructorWithValidDataset() {
        PiePlot p = new PiePlot(singleDataset);
        assertSame("Dataset should be the one passed", singleDataset, p.getDataset());
    }

    // -------------------- getDataset / setDataset --------------------
    @Test
    public void testSetNullDataset() {
        plot.setDataset(null);
        assertNull("Dataset should be null", plot.getDataset());
    }

    @Test
    public void testSetEmptyDataset() {
        plot.setDataset(emptyDataset);
        assertSame("Dataset should be empty", emptyDataset, plot.getDataset());
    }

    @Test
    public void testSetValidDataset() {
        plot.setDataset(multiDataset);
        assertSame("Dataset should be valid", multiDataset, plot.getDataset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNegativePercentagesDataset() {
        DefaultPieDataset bad = new DefaultPieDataset();
        bad.setValue("X", -5.0);
        plot.setDataset(bad); // may throw exception if validation exists
    }

    // -------------------- getSectionCount --------------------
    @Test
    public void testSectionCountNullDataset() {
        plot.setDataset(null);
        assertEquals("Section count for null dataset should be 0", 0, plot.getSectionCount());
    }

    @Test
    public void testSectionCountEmptyDataset() {
        plot.setDataset(emptyDataset);
        assertEquals("Section count for empty dataset should be 0", 0, plot.getSectionCount());
    }

    @Test
    public void testSectionCountSingleDataset() {
        plot.setDataset(singleDataset);
        assertEquals("Section count for single item should be 1", 1, plot.getSectionCount());
    }

    @Test
    public void testSectionCountMultiDataset() {
        plot.setDataset(multiDataset);
        assertEquals("Section count for three items should be 3", 3, plot.getSectionCount());
    }

    // -------------------- getKey --------------------
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyNullDataset() {
        plot.setDataset(null);
        plot.getKey(0); // should throw
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndex() {
        plot.setDataset(multiDataset);
        plot.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyIndexEqualToSize() {
        plot.setDataset(multiDataset);
        plot.getKey(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyIndexGreaterThanSize() {
        plot.setDataset(multiDataset);
        plot.getKey(100);
    }

    @Test
    public void testGetKeyValidFirst() {
        plot.setDataset(multiDataset);
        assertEquals("First key should be 'A'", "A", plot.getKey(0));
    }

    @Test
    public void testGetKeyValidLast() {
        plot.setDataset(multiDataset);
        assertEquals("Last key should be 'C'", "C", plot.getKey(2));
    }

    // -------------------- getValue --------------------
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNullDataset() {
        plot.setDataset(null);
        plot.getValue(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeIndex() {
        plot.setDataset(multiDataset);
        plot.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueIndexEqualToSize() {
        plot.setDataset(multiDataset);
        plot.getValue(3);
    }

    @Test
    public void testGetValueValid() {
        plot.setDataset(multiDataset);
        assertEquals("Value for index 0 should be 10.0", 10.0, plot.getValue(0), 0.0001);
    }

    // -------------------- getValue by Comparable key --------------------
    @Test
    public void testGetValueByKeyNullDataset() {
        plot.setDataset(null);
        assertNull("Value for null dataset should be null", plot.getValue("A"));
    }

    @Test
    public void testGetValueByKeyValidKey() {
        plot.setDataset(multiDataset);
        assertEquals("Value for key 'B' should be 20.0", 20.0, plot.getValue("B"), 0.0001);
    }

    @Test
    public void testGetValueByKeyNonExistentKey() {
        plot.setDataset(multiDataset);
        assertNull("Value for non-existing key should be null", plot.getValue("Z"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueByKeyNullKey() {
        plot.setDataset(multiDataset);
        plot.getValue(null); // must throw according to some implementations
    }

    // -------------------- Legend Label generation (if applicable) --------------------
    @Test
    public void testGetLegendItemsNullDataset() {
        plot.setDataset(null);
        assertNotNull("Legend items should not be null", plot.getLegendItems());
        assertEquals("Legend items count should be 0", 0, plot.getLegendItems().getItemCount());
    }

    @Test
    public void testGetLegendItemsEmptyDataset() {
        plot.setDataset(emptyDataset);
        assertNotNull("Legend items should not be null", plot.getLegendItems());
        assertEquals("Legend items count should be 0", 0, plot.getLegendItems().getItemCount());
    }

    @Test
    public void testGetLegendItemsSingleDataset() {
        plot.setDataset(singleDataset);
        assertEquals("Legend items count should be 1", 1, plot.getLegendItems().getItemCount());
    }

    @Test
    public void testGetLegendItemsMultiDataset() {
        plot.setDataset(multiDataset);
        assertEquals("Legend items count should be 3", 3, plot.getLegendItems().getItemCount());
    }

    // -------------------- Edge cases with empty dataset keys --------------------
    @Test
    public void testGetSectionCountAfterSettingNullThenValid() {
        plot.setDataset(null);
        assertEquals("Should be 0", 0, plot.getSectionCount());
        plot.setDataset(singleDataset);
        assertEquals("Should now be 1", 1, plot.getSectionCount());
    }

    @Test
    public void testGetSectionCountAfterSettingMultipleDatasets() {
        plot.setDataset(multiDataset);
        assertEquals("Should be 3", 3, plot.getSectionCount());
        plot.setDataset(singleDataset);
        assertEquals("Should be 1", 1, plot.getSectionCount());
    }

    // -------------------- Handling of dataset with zero values --------------------
    @Test
    public void testDatasetWithZeroValue() {
        DefaultPieDataset ds = new DefaultPieDataset();
        ds.setValue("X", 0.0);
        plot.setDataset(ds);
        assertEquals("Key should be retrievable", "X", plot.getKey(0));
        assertEquals("Value should be 0.0", 0.0, plot.getValue(0), 0.0001);
    }

    // -------------------- Dataset with null key value (if allowed) --------------------
    @Test(expected = IllegalArgumentException.class)
    public void testDatasetWithNullKey() {
        DefaultPieDataset ds = new DefaultPieDataset();
        ds.setValue(null, 10.0); // might be invalid
        plot.setDataset(ds);
    }

    // -------------------- Test for potential null pointer in getValue(index) when dataset has null key --------------------
    // (depending on implementation)

    // -------------------- test for key ordering (assumed unchanged) --------------------
    @Test
    public void testKeyOrderingPreserved() {
        plot.setDataset(multiDataset);
        assertEquals("First key should be A", "A", plot.getKey(0));
        assertEquals("Second key should be B", "B", plot.getKey(1));
        assertEquals("Third key should be C", "C", plot.getKey(2));
    }

    // -------------------- Test for modification after set --------------------
    @Test
    public void testDatasetModificationAfterSet() {
        plot.setDataset(multiDataset);
        assertEquals("Initial count 3", 3, plot.getSectionCount());
        multiDataset.setValue("D", 40.0);
        assertEquals("Count should increase to 4", 4, plot.getSectionCount());
    }

    // -------------------- Additional getValue for edge cases (large values, negative keys?) --------------------
    @Test
    public void testGetValueLargeNumbers() {
        DefaultPieDataset ds = new DefaultPieDataset();
        ds.setValue("L", Double.MAX_VALUE);
        plot.setDataset(ds);
        assertEquals("Large value should be retrievable", Double.MAX_VALUE, plot.getValue("L"), 0.0);
    }

    // -------------------- Test for multiple calls --------------------
    @Test
    public void testRepeatedGetters() {
        plot.setDataset(multiDataset);
        for (int i = 0; i < 3; i++) {
            assertEquals("Key for index " + i, multiDataset.getKey(i), plot.getKey(i));
            assertEquals("Value for index " + i, multiDataset.getValue(i), plot.getValue(i));
        }
    }
}