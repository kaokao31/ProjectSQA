package org.jfree.data.time;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

/**
 * Comprehensive JUnit 4 test suite for TimePeriodValues.
 * Designed to achieve high code coverage and detect defects,
 * particularly the known bug in Defects4J Chart-7 (null handling).
 */
public class TimePeriodValuesTest {

    private TimePeriodValues emptySeries;
    private TimePeriodValues oneItemSeries;
    private TimePeriodValues multiItemSeries;

    private SimpleTimePeriod period1;
    private SimpleTimePeriod period2;
    private SimpleTimePeriod period3;

    @Before
    public void setUp() {
        emptySeries = new TimePeriodValues("Empty");
        oneItemSeries = new TimePeriodValues("One");
        multiItemSeries = new TimePeriodValues("Multi");

        period1 = new SimpleTimePeriod(100, 200);
        period2 = new SimpleTimePeriod(300, 400);
        period3 = new SimpleTimePeriod(500, 600);

        oneItemSeries.add(period1, 1.0);

        multiItemSeries.add(period1, 10.0);
        multiItemSeries.add(period2, 20.0);
        multiItemSeries.add(period3, 30.0);
    }

    // ===================== Constructor Tests =====================

    @Test
    public void testDefaultConstructor() {
        TimePeriodValues s = new TimePeriodValues("Test");
        assertEquals("Test", s.getKey());
        assertEquals(0, s.getItemCount());
    }

    @Test
    public void testConstructorWithDescription() {
        TimePeriodValues s = new TimePeriodValues("Name", "Desc");
        assertEquals("Name", s.getKey());
        assertEquals("Desc", s.getDescription());
        assertEquals(0, s.getItemCount());
    }

    @Test
    public void testConstructorNullName() {
        try {
            new TimePeriodValues(null);
            fail("Expected IllegalArgumentException for null name");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ===================== add() Method Tests =====================

    @Test
    public void testAddValidPeriod() {
        TimePeriodValues s = new TimePeriodValues("s");
        s.add(period1, 5.0);
        assertEquals(1, s.getItemCount());
        assertEquals(5.0, s.getValue(0), 0.0001);
        assertEquals(period1, s.getPeriod(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPeriod() {
        emptySeries.add(null, 1.0);
    }

    @Test
    public void testAddNegativeValue() {
        TimePeriodValues s = new TimePeriodValues("s");
        s.add(period1, -100.0);
        assertEquals(-100.0, s.getValue(0), 0.0001);
    }

    @Test
    public void testAddLargeValue() {
        TimePeriodValues s = new TimePeriodValues("s");
        s.add(period1, Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, s.getValue(0), 0.0);
    }

    @Test
    public void testAddMultipleItemsInOrder() {
        assertEquals(3, multiItemSeries.getItemCount());
        assertEquals(10.0, multiItemSeries.getValue(0), 0.0);
        assertEquals(20.0, multiItemSeries.getValue(1), 0.0);
        assertEquals(30.0, multiItemSeries.getValue(2), 0.0);
    }

    @Test
    public void testAddItemsOutOfOrder() {
        TimePeriodValues s = new TimePeriodValues("s");
        s.add(new SimpleTimePeriod(300, 400), 2.0);
        // Appending before the last (should be allowed in this implementation)
        s.add(new SimpleTimePeriod(100, 200), 1.0);
        assertEquals(2, s.getItemCount());
        assertEquals(2.0, s.getValue(0), 0.0);
        assertEquals(1.0, s.getValue(1), 0.0);
    }

    @Test
    public void testAddUpdatesDomainBounds() {
        // Check that after adding, the min/max indices are updated
        TimePeriodValues s = new TimePeriodValues("s");
        SimpleTimePeriod early = new SimpleTimePeriod(10, 20);
        SimpleTimePeriod late = new SimpleTimePeriod(100, 200);
        s.add(early, 1.0);
        assertEquals(0, s.getMinStartIndex());
        assertEquals(0, s.getMaxStartIndex());
        assertEquals(0, s.getMinMiddleIndex());
        assertEquals(0, s.getMaxMiddleIndex());
        assertEquals(0, s.getMinEndIndex());
        assertEquals(0, s.getMaxEndIndex());

        s.add(late, 2.0);
        assertEquals(0, s.getMinStartIndex()); // early is still min
        assertEquals(1, s.getMaxStartIndex());
        assertEquals(0, s.getMinMiddleIndex());
        assertEquals(1, s.getMaxMiddleIndex());
        assertEquals(0, s.getMinEndIndex());
        assertEquals(1, s.getMaxEndIndex());
    }

    // ===================== getValue() & getPeriod() =====================

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeIndex() {
        emptySeries.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueOutOfRange() {
        emptySeries.getValue(0);
    }

    @Test
    public void testGetValueValid() {
        assertEquals(10.0, multiItemSeries.getValue(0), 0.0);
        assertEquals(20.0, multiItemSeries.getValue(1), 0.0);
        assertEquals(30.0, multiItemSeries.getValue(2), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetPeriodNegativeIndex() {
        emptySeries.getPeriod(-1);
    }

    @Test
    public void testGetPeriodValid() {
        assertEquals(period1, multiItemSeries.getPeriod(0));
        assertEquals(period2, multiItemSeries.getPeriod(1));
        assertEquals(period3, multiItemSeries.getPeriod(2));
    }

    // ===================== getDataItem() =====================

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemNegativeIndex() {
        emptySeries.getDataItem(-1);
    }

    @Test
    public void testGetDataItemValid() {
        assertNotNull(multiItemSeries.getDataItem(0));
        assertEquals(period1, multiItemSeries.getDataItem(0).getPeriod());
        assertEquals(10.0, multiItemSeries.getDataItem(0).getValue(), 0.0);
    }

    // ===================== delete() =====================

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteNegativeIndex() {
        emptySeries.delete(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteOutOfRange() {
        emptySeries.delete(0);
    }

    @Test
    public void testDeleteFirstItem() {
        multiItemSeries.delete(0);
        assertEquals(2, multiItemSeries.getItemCount());
        assertEquals(period2, multiItemSeries.getPeriod(0));
        assertEquals(20.0, multiItemSeries.getValue(0), 0.0);
    }

    @Test
    public void testDeleteLastItem() {
        multiItemSeries.delete(2);
        assertEquals(2, multiItemSeries.getItemCount());
        assertEquals(period1, multiItemSeries.getPeriod(0));
        assertEquals(period2, multiItemSeries.getPeriod(1));
    }

    @Test
    public void testDeleteMiddleItem() {
        multiItemSeries.delete(1);
        assertEquals(2, multiItemSeries.getItemCount());
        assertEquals(period1, multiItemSeries.getPeriod(0));
        assertEquals(period3, multiItemSeries.getPeriod(1));
        assertEquals(10.0, multiItemSeries.getValue(0), 0.0);
        assertEquals(30.0, multiItemSeries.getValue(1), 0.0);
    }

    @Test
    public void testDeleteUpdatesDomainBounds() {
        TimePeriodValues s = new TimePeriodValues("s");
        SimpleTimePeriod early = new SimpleTimePeriod(10, 20);
        SimpleTimePeriod late = new SimpleTimePeriod(100, 200);
        s.add(early, 1.0);
        s.add(late, 2.0);
        // Delete the early one, now late becomes min and max
        s.delete(0);
        assertEquals(0, s.getMinStartIndex());
        assertEquals(0, s.getMaxStartIndex());
        assertEquals(0, s.getMinMiddleIndex());
        assertEquals(0, s.getMaxMiddleIndex());
        assertEquals(0, s.getMinEndIndex());
        assertEquals(0, s.getMaxEndIndex());
    }

    @Test
    public void testDeleteAllItems() {
        TimePeriodValues s = new TimePeriodValues("s");
        s.add(period1, 1.0);
        s.delete(0);
        assertEquals(0, s.getItemCount());
    }

    // ===================== setValue() =====================

    @Test
    public void testSetValueFirstItem() {
        multiItemSeries.setValue(0, 100.0);
        assertEquals(100.0, multiItemSeries.getValue(0), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetValueOutOfRange() {
        oneItemSeries.setValue(1, 5.0);
    }

    // ===================== Domain Bounds Methods =====================

    @Test
    public void testDomainBoundsEmptySeries() {
        assertEquals(-1, emptySeries.getMinStartIndex());
        assertEquals(-1, emptySeries.getMaxStartIndex());
        assertEquals(-1, emptySeries.getMinMiddleIndex());
        assertEquals(-1, emptySeries.getMaxMiddleIndex());
        assertEquals(-1, emptySeries.getMinEndIndex());
        assertEquals(-1, emptySeries.getMaxEndIndex());
    }

    @Test
    public void testDomainBoundsOneItem() {
        assertEquals(0, oneItemSeries.getMinStartIndex());
        assertEquals(0, oneItemSeries.getMaxStartIndex());
        assertEquals(0, oneItemSeries.getMinMiddleIndex());
        assertEquals(0, oneItemSeries.getMaxMiddleIndex());
        assertEquals(0, oneItemSeries.getMinEndIndex());
        assertEquals(0, oneItemSeries.getMaxEndIndex());
    }

    @Test
    public void testDomainBoundsMultiItem() {
        // period1: [100,200], period2: [300,400], period3: [500,600]
        // Start: min=0, max=2
        // Middle: min=0, max=2
        // End: min=0, max=2
        assertEquals(0, multiItemSeries.getMinStartIndex());
        assertEquals(2, multiItemSeries.getMaxStartIndex());
        assertEquals(0, multiItemSeries.getMinMiddleIndex());
        assertEquals(2, multiItemSeries.getMaxMiddleIndex());
        assertEquals(0, multiItemSeries.getMinEndIndex());
        assertEquals(2, multiItemSeries.getMaxEndIndex());
    }

    // ===================== equals() =====================

    @Test
    public void testEqualsNull() {
        // According to equals contract, equals(null) should return false.
        // Bug in Defects4J Chart-7: may throw NullPointerException.
        try {
            boolean result = emptySeries.equals(null);
            assertFalse("equals(null) should return false", result);
        } catch (Exception e) {
            fail("equals(null) threw an exception: " + e.getClass().getName());
        }
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(emptySeries.equals(emptySeries));
    }

    @Test
    public void testEqualsIdenticalSeries() {
        TimePeriodValues s1 = new TimePeriodValues("Name", "Desc");
        TimePeriodValues s2 = new TimePeriodValues("Name", "Desc");
        assertEquals(s1, s2);
    }

    @Test
    public void testEqualsDifferentName() {
        TimePeriodValues s1 = new TimePeriodValues("Name", "Desc");
        TimePeriodValues s2 = new TimePeriodValues("Other", "Desc");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentDescription() {
        TimePeriodValues s1 = new TimePeriodValues("Name", "Desc");
        TimePeriodValues s2 = new TimePeriodValues("Name", "Other");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentData() {
        TimePeriodValues s1 = new TimePeriodValues("Name");
        TimePeriodValues s2 = new TimePeriodValues("Name");
        s1.add(period1, 1.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(emptySeries.equals("SomeString"));
    }

    // ===================== hashCode() =====================

    @Test
    public void testHashCodeConsistency() {
        int hash1 = emptySeries.hashCode();
        int hash2 = emptySeries.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCodeEqualsContract() {
        TimePeriodValues s1 = new TimePeriodValues("Name", "Desc");
        TimePeriodValues s2 = new TimePeriodValues("Name", "Desc");
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    // ===================== clone() =====================

    @Test
    public void testClone() {
        TimePeriodValues cloned = null;
        try {
            cloned = (TimePeriodValues) multiItemSeries.clone();
        } catch (CloneNotSupportedException e) {
            fail("Clone not supported");
        }
        assertNotNull(cloned);
        assertEquals(multiItemSeries, cloned);
        assertNotSame(multiItemSeries, cloned);
        // Ensure deep copy of data
        cloned.setValue(0, 999.0);
        assertFalse(multiItemSeries.getValue(0) == 999.0);
    }

    @Test
    public void testClonedSeriesIndependent() {
        TimePeriodValues cloned = null;
        try {
            cloned = (TimePeriodValues) multiItemSeries.clone();
        } catch (CloneNotSupportedException e) {
            fail("Clone not supported");
        }
        cloned.add(new SimpleTimePeriod(700, 800), 40.0);
        assertEquals(3, multiItemSeries.getItemCount());
        assertEquals(4, cloned.getItemCount());
    }

    // ===================== Serialization =====================

    @Test
    public void testSerialization() {
        TimePeriodValues s = new TimePeriodValues("Test");
        s.add(period1, 1.0);
        s.add(period2, 2.0);
        TimePeriodValues deserialized = (TimePeriodValues) TestUtilities.serialised(s);
        assertEquals(s, deserialized);
    }

    // ===================== Other =====================

    @Test
    public void testGetItemCountAfterAdd() {
        assertEquals(0, emptySeries.getItemCount());
        assertEquals(1, oneItemSeries.getItemCount());
        assertEquals(3, multiItemSeries.getItemCount());
    }

    @Test
    public void testGetKey() {
        assertEquals("Empty", emptySeries.getKey());
        assertEquals("One", oneItemSeries.getKey());
        assertEquals("Multi", multiItemSeries.getKey());
    }

    @Test
    public void testGetDescriptionDefault() {
        assertNull(emptySeries.getDescription());
    }

    @Test
    public void testSetDescription() {
        TimePeriodValues s = new TimePeriodValues("Name");
        s.setDescription("New Desc");
        assertEquals("New Desc", s.getDescription());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDescriptionNull() {
        TimePeriodValues s = new TimePeriodValues("Name");
        s.setDescription(null);
    }

    // ===================== Edge Cases for add() =====================

    @Test
    public void testAddWithSamePeriod() {
        TimePeriodValues s = new TimePeriodValues("s");
        s.add(period1, 1.0);
        s.add(period1, 2.0);
        assertEquals(2, s.getItemCount());
        // Both items should be stored separately (JFreeChart allows duplicates)
        assertEquals(1.0, s.getValue(0), 0.0);
        assertEquals(2.0, s.getValue(1), 0.0);
    }

    @Test
    public void testAddWithMinimalTimePeriod() {
        TimePeriodValues s = new TimePeriodValues("s");
        SimpleTimePeriod tiny = new SimpleTimePeriod(0, 0);
        s.add(tiny, 0.0);
        assertEquals(1, s.getItemCount());
    }

    @Test
    public void testAddWithOverlappingPeriods() {
        TimePeriodValues s = new TimePeriodValues("s");
        SimpleTimePeriod p1 = new SimpleTimePeriod(100, 300);
        SimpleTimePeriod p2 = new SimpleTimePeriod(200, 400);
        s.add(p1, 1.0);
        s.add(p2, 2.0);
        // Just ensure they are added without exception
        assertEquals(2, s.getItemCount());
    }

    // ===================== Performance / Stress =====================

    @Test(timeout = 1000)
    public void testAddManyItems() {
        TimePeriodValues s = new TimePeriodValues("s");
        for (int i = 0; i < 10000; i++) {
            s.add(new SimpleTimePeriod(i * 100, i * 100 + 50), i * 1.0);
        }
        assertEquals(10000, s.getItemCount());
        assertEquals(0.0, s.getValue(0), 0.0);
        assertEquals(9999.0, s.getValue(9999), 0.0);
    }
}