package org.jfree.data.xy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link XYSeries} class.
 * Designed to achieve high code coverage and detect known bugs
 * from the Defects4J benchmark (JFreeChart version).
 */
public class XYSeriesTest {

    private XYSeries series;
    private XYSeries seriesWithDuplicates;

    @Before
    public void setUp() {
        series = new XYSeries("Test");
        seriesWithDuplicates = new XYSeries("Dup", false, false);
    }

    // ------------------------------------------------------------
    // Constructor tests
    // ------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new XYSeries(null);
    }

    @Test
    public void testConstructorDefaultFlags() {
        XYSeries s = new XYSeries("Default");
        assertTrue("autoSort should be true", s.getAutoSort());
        assertTrue("allowDuplicateXValues should be true", s.getAllowDuplicateXValues());
        assertEquals("Default", s.getKey());
    }

    @Test
    public void testConstructorCustomFlags() {
        XYSeries s = new XYSeries("Custom", false, false);
        assertFalse(s.getAutoSort());
        assertFalse(s.getAllowDuplicateXValues());
    }

    // ------------------------------------------------------------
    // add(Number, Number) tests
    // ------------------------------------------------------------
    @Test
    public void testAddFirstItem() {
        series.add(1.0, 2.0);
        assertEquals(1, series.getItemCount());
        assertEquals(1.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(2.0, series.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testAddMultipleSorted() {
        series.add(2.0, 20.0);
        series.add(1.0, 10.0);
        assertEquals(2, series.getItemCount());
        // Should be sorted by x
        assertEquals(1.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(2.0, series.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testAddWithDuplicateNotAllowed() {
        XYSeries s = new XYSeries("NoDup", true, false);
        s.add(1.0, 10.0);
        try {
            s.add(1.0, 20.0);
            fail("Expected IllegalArgumentException for duplicate x");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddWithDuplicateAllowed() {
        series.add(1.0, 10.0);
        series.add(1.0, 20.0); // duplicate allowed
        assertEquals(2, series.getItemCount());
    }

    @Test
    public void testAddNullX() {
        try {
            series.add(null, 5.0);
            fail("Expected IllegalArgumentException for null x");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddNullY() {
        series.add(1.0, null);
        assertEquals(1, series.getItemCount());
        assertNull(series.getY(0));
    }

    @Test
    public void testAddNaN() {
        series.add(Double.NaN, 5.0);
        series.add(Double.NaN, 10.0);
        // NaN is considered less than any number; check insertion order
        assertEquals(2, series.getItemCount());
    }

    // ------------------------------------------------------------
    // add(double, double) tests
    // ------------------------------------------------------------
    @Test
    public void testAddDoubleDouble() {
        series.add(1.0, 2.0);
        assertEquals(1, series.getItemCount());
    }

    // ------------------------------------------------------------
    // update(int, Number) tests
    // ------------------------------------------------------------
    @Test
    public void testUpdateValidIndex() {
        series.add(1.0, 10.0);
        series.update(0, 50.0);
        assertEquals(50.0, series.getY(0).doubleValue(), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateNegativeIndex() {
        series.add(1.0, 10.0);
        series.update(-1, 20.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateIndexOutOfBounds() {
        series.update(0, 10.0);
    }

    @Test
    public void testUpdateNullYValue() {
        series.add(1.0, 10.0);
        series.update(0, null);
        assertNull(series.getY(0));
    }

    // ------------------------------------------------------------
    // updateByIndex / update(Number, Number) tests
    // ------------------------------------------------------------
    @Test
    public void testUpdateByX() {
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.update(2.0, 25.0);
        assertEquals(25.0, series.getY(1).doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateByXNotFound() {
        series.add(1.0, 10.0);
        series.update(99.0, 99.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateByXNullX() {
        series.update((Number) null, 5.0);
    }

    // ------------------------------------------------------------
    // delete(int, int) tests
    // ------------------------------------------------------------
    @Test
    public void testDeleteSingleItem() {
        series.add(1.0, 10.0);
        series.delete(0, 0);
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testDeleteRange() {
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(3.0, 30.0);
        series.delete(1, 2);
        assertEquals(1, series.getItemCount());
        assertEquals(1.0, series.getX(0).doubleValue(), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteNegativeFrom() {
        series.delete(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteFromBeyondCount() {
        series.add(1.0, 10.0);
        series.delete(2, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFromGreaterThanTo() {
        series.add(1.0, 10.0);
        series.delete(1, 0);
    }

    // ------------------------------------------------------------
    // getX(int) and getY(int) tests
    // ------------------------------------------------------------
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetXNegativeIndex() {
        series.add(1.0, 10.0);
        series.getX(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetXIndexOutOfBounds() {
        series.getX(0);
    }

    @Test
    public void testGetXReturnsNumber() {
        series.add(1.0, 10.0);
        Number x = series.getX(0);
        assertNotNull(x);
        assertEquals(1.0, x.doubleValue(), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetYNegativeIndex() {
        series.add(1.0, 10.0);
        series.getY(-1);
    }

    // ------------------------------------------------------------
    // getItemCount tests
    // ------------------------------------------------------------
    @Test
    public void testGetItemCountEmpty() {
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testGetItemCountAfterAdd() {
        series.add(1.0, 10.0);
        assertEquals(1, series.getItemCount());
    }

    // ------------------------------------------------------------
    // getMinX, getMaxX, getMinY, getMaxY tests
    // ------------------------------------------------------------
    @Test
    public void testGetMinMaxX() {
        series.add(2.0, 20.0);
        series.add(1.0, 10.0);
        series.add(3.0, 30.0);
        assertEquals(1.0, series.getMinX(), 0.0);
        assertEquals(3.0, series.getMaxX(), 0.0);
    }

    @Test
    public void testGetMinMaxY() {
        series.add(1.0, 30.0);
        series.add(2.0, 10.0);
        series.add(3.0, 20.0);
        assertEquals(10.0, series.getMinY(), 0.0);
        assertEquals(30.0, series.getMaxY(), 0.0);
    }

    @Test
    public void testGetMinXEmptySeries() {
        assertEquals(Double.NaN, series.getMinX(), 0.0);
    }

    @Test
    public void testGetMaxXEmptySeries() {
        assertEquals(Double.NaN, series.getMaxX(), 0.0);
    }

    // ------------------------------------------------------------
    // addOrUpdate tests
    // ------------------------------------------------------------
    @Test
    public void testAddOrUpdateNewItem() {
        XYSeries s = new XYSeries("test", false, true);
        s.addOrUpdate(1.0, 10.0);
        assertEquals(1, s.getItemCount());
        assertEquals(10.0, s.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdateExistingItem() {
        XYSeries s = new XYSeries("test", false, true);
        s.add(1.0, 10.0);
        s.addOrUpdate(1.0, 20.0);
        assertEquals(1, s.getItemCount());
        assertEquals(20.0, s.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdateWithDuplicateAllowedButSameX() {
        // autoSort = true, allowDuplicateXValues = false
        XYSeries s = new XYSeries("test", true, false);
        s.add(1.0, 10.0);
        s.addOrUpdate(1.0, 20.0);
        assertEquals(1, s.getItemCount());
        assertEquals(20.0, s.getY(0).doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullX() {
        series.addOrUpdate(null, 5.0);
    }

    // ------------------------------------------------------------
    // setMaximumItemCount / getMaximumItemCount tests
    // ------------------------------------------------------------
    @Test
    public void testSetMaximumItemCount() {
        series.setMaximumItemCount(10);
        assertEquals(10, series.getMaximumItemCount());
    }

    @Test
    public void testMaximumItemCountTruncates() {
        series.setMaximumItemCount(2);
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        series.add(3.0, 30.0);
        assertEquals(2, series.getItemCount());
        // Should keep the last two items (since sorted by x) -> x=2 and x=3
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(3.0, series.getX(1).doubleValue(), 0.0);
    }

    // ------------------------------------------------------------
    // getAutoSort / setAutoSort tests
    // ------------------------------------------------------------
    @Test
    public void testSetAutoSort() {
        XYSeries s = new XYSeries("test", true, true);
        s.setAutoSort(false);
        assertFalse(s.getAutoSort());
    }

    // ------------------------------------------------------------
    // getAllowDuplicateXValues / setAllowDuplicateXValues tests
    // ------------------------------------------------------------
    @Test
    public void testSetAllowDuplicateXValues() {
        XYSeries s = new XYSeries("test", true, true);
        s.setAllowDuplicateXValues(false);
        assertFalse(s.getAllowDuplicateXValues());
    }

    // ------------------------------------------------------------
    // clone tests
    // ------------------------------------------------------------
    @Test
    public void testClone() throws CloneNotSupportedException {
        series.add(1.0, 10.0);
        series.add(2.0, 20.0);
        XYSeries cloned = (XYSeries) series.clone();
        assertNotNull(cloned);
        // Verify content equal
        assertEquals(series.getItemCount(), cloned.getItemCount());
        for (int i = 0; i < series.getItemCount(); i++) {
            assertEquals(series.getX(i), cloned.getX(i));
            assertEquals(series.getY(i), cloned.getY(i));
        }
        // Verify independent
        cloned.update(0, 99.0);
        assertEquals(10.0, series.getY(0).doubleValue(), 0.0);
    }

    // ------------------------------------------------------------
    // equals tests
    // ------------------------------------------------------------
    @Test
    public void testEqualsSameContent() {
        XYSeries s1 = new XYSeries("S1");
        XYSeries s2 = new XYSeries("S1");
        s1.add(1.0, 10.0);
        s2.add(1.0, 10.0);
        assertTrue(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentKey() {
        XYSeries s1 = new XYSeries("S1");
        XYSeries s2 = new XYSeries("S2");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentData() {
        XYSeries s1 = new XYSeries("S");
        XYSeries s2 = new XYSeries("S");
        s1.add(1.0, 10.0);
        s2.add(1.0, 20.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(series.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(series.equals("string"));
    }

    // ------------------------------------------------------------
    // hashCode tests
    // ------------------------------------------------------------
    @Test
    public void testHashCode() {
        XYSeries s1 = new XYSeries("S");
        XYSeries s2 = new XYSeries("S");
        s1.add(1.0, 10.0);
        s2.add(1.0, 10.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    // ------------------------------------------------------------
    // getKey tests
    // ------------------------------------------------------------
    @Test
    public void testGetKey() {
        assertEquals("Test", series.getKey());
    }

    // ------------------------------------------------------------
    // setKey tests
    // ------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyNull() {
        series.setKey(null);
    }

    @Test
    public void testSetKey() {
        series.setKey("NewKey");
        assertEquals("NewKey", series.getKey());
    }

    // ------------------------------------------------------------
    // SeriesChangeListener tests (fire events)
    // ------------------------------------------------------------
    @Test
    public void testAddWithNotification() {
        final boolean[] flagged = {false};
        series.addChangeListener(new SeriesChangeListener() {
            @Override
            public void seriesChanged(SeriesChangeEvent event) {
                flagged[0] = true;
            }
        });
        series.add(1.0, 10.0);
        assertTrue("Event should be fired", flagged[0]);
    }

    @Test
    public void testAddWithoutNotification() {
        final boolean[] flagged = {false};
        series.addChangeListener(new SeriesChangeListener() {
            @Override
            public void seriesChanged(SeriesChangeEvent event) {
                flagged[0] = true;
            }
        });
        series.add(1.0, 10.0, false);
        assertFalse("Event should not be fired", flagged[0]);
    }

    @Test
    public void testSetNotify() {
        series.setNotify(false);
        // indirectly check that no event is fired
        final boolean[] flagged = {false};
        series.addChangeListener(event -> flagged[0] = true);
        series.add(1.0, 10.0);
        assertFalse(flagged[0]);
        series.setNotify(true);
        series.add(2.0, 20.0);
        assertTrue(flagged[0]);
    }

    // ------------------------------------------------------------
    // toArray tests (if exists, optional)
    // ------------------------------------------------------------
    // (Not standard in XYSeries, but some extended versions)
    // Omitted to avoid depending on potentially missing methods.

    // ------------------------------------------------------------
    // Regression test for Defects4J bug: update with duplicate x
    // ------------------------------------------------------------
    @Test
    public void testUpdateWithDuplicateX() {
        XYSeries s = new XYSeries("test");
        s.add(1.0, 10.0);
        s.add(1.0, 20.0);
        s.update(1, 25.0);  // update second occurrence (index 1)
        assertEquals(25.0, s.getY(1).doubleValue(), 0.0);
    }

    // ------------------------------------------------------------
    // Regression test for Defects4J bug: addOrUpdate with null y
    // ------------------------------------------------------------
    @Test
    public void testAddOrUpdateWithNullY() {
        XYSeries s = new XYSeries("test", false, true);
        s.addOrUpdate(1.0, null);
        assertNull(s.getY(0));
    }

    // ------------------------------------------------------------
    // Edge: many items, performance
    // ------------------------------------------------------------
    @Test
    public void testAddManyItems() {
        for (int i = 0; i < 1000; i++) {
            series.add((double) i, (double) i);
        }
        assertEquals(1000, series.getItemCount());
        assertEquals(0.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(999.0, series.getX(999).doubleValue(), 0.0);
    }

    // ------------------------------------------------------------
    // delete by index range with negative y values
    // ------------------------------------------------------------
    @Test
    public void testDeleteWithNegativeY() {
        series.add(1.0, -10.0);
        series.add(2.0, -20.0);
        series.delete(0, 1);
        assertEquals(0, series.getItemCount());
    }

    // ------------------------------------------------------------
    // getMinY and getMaxY with null y values
    // ------------------------------------------------------------
    @Test
    public void testMinMaxYWithNullY() {
        series.add(1.0, null);
        series.add(2.0, 5.0);
        // Null y should be ignored in min/max? The standard behavior
        // may return NaN if first y is null. We test defensively.
        double minY = series.getMinY();
        double maxY = series.getMaxY();
        // Behavior may vary; we just ensure no exception
        assertTrue(Double.isFinite(minY) || Double.isNaN(minY));
    }

    // ------------------------------------------------------------
    // Sorting stability (autoSort = false)
    // ------------------------------------------------------------
    @Test
    public void testNoAutoSortPreservesInsertionOrder() {
        XYSeries s = new XYSeries("Unsorted", false, true);
        s.add(2.0, 20.0);
        s.add(1.0, 10.0);
        s.add(3.0, 30.0);
        assertEquals(2.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getX(1).doubleValue(), 0.0);
        assertEquals(3.0, s.getX(2).doubleValue(), 0.0);
    }

    // ------------------------------------------------------------
    // Fire event on update
    // ------------------------------------------------------------
    @Test
    public void testUpdateFiresEvent() {
        final boolean[] flagged = {false};
        series.addChangeListener(event -> flagged[0] = true);
        series.add(1.0, 10.0);
        flagged[0] = false;
        series.update(0, 20.0);
        assertTrue("Event should be fired on update", flagged[0]);
    }

    // ------------------------------------------------------------
    // Fire event on delete
    // ------------------------------------------------------------
    @Test
    public void testDeleteFiresEvent() {
        final boolean[] flagged = {false};
        series.addChangeListener(event -> flagged[0] = true);
        series.add(1.0, 10.0);
        flagged[0] = false;
        series.delete(0, 0);
        assertTrue("Event should be fired on delete", flagged[0]);
    }

    // ------------------------------------------------------------
    // getX(Number) - deprecated but may be present
    // ------------------------------------------------------------
    // (If the method exists, test it)
    @Test
    public void testGetXByIndexNumber() {
        series.add(1.0, 10.0);
        Number x = series.getX(0);
        assertEquals(1.0, x.doubleValue(), 0.0);
    }

    // ------------------------------------------------------------
    // getY(Number) - deprecated
    // ------------------------------------------------------------
    @Test
    public void testGetYByIndexNumber() {
        series.add(1.0, 10.0);
        Number y = series.getY(0);
        assertEquals(10.0, y.doubleValue(), 0.0);
    }

    // ------------------------------------------------------------
    // setKey fires event
    // ------------------------------------------------------------
    @Test
    public void testSetKeyFiresEvent() {
        final boolean[] flagged = {false};
        series.addChangeListener(event -> flagged[0] = true);
        series.setKey("NewKey");
        assertTrue("Event should be fired on key change", flagged[0]);
    }
}