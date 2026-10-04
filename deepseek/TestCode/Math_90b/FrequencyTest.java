package org.apache.commons.math.stat;

import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FrequencyTest {

    private Frequency freq;

    @Before
    public void setUp() {
        freq = new Frequency();
    }

    @Test
    public void testAddValueInt() {
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(1);
        assertEquals(2, freq.getCount(1));
        assertEquals(1, freq.getCount(2));
        assertEquals(3, freq.getSumFreq());
    }

    @Test
    public void testAddValueLong() {
        freq.addValue(1L);
        freq.addValue(2L);
        freq.addValue(1L);
        assertEquals(2, freq.getCount(1L));
        assertEquals(1, freq.getCount(2L));
        assertEquals(3, freq.getSumFreq());
    }

    @Test
    public void testAddValueChar() {
        freq.addValue('a');
        freq.addValue('b');
        freq.addValue('a');
        assertEquals(2, freq.getCount('a'));
        assertEquals(1, freq.getCount('b'));
        assertEquals(3, freq.getSumFreq());
    }

    @Test
    public void testAddValueComparable() {
        freq.addValue("test");
        freq.addValue("test");
        freq.addValue("other");
        assertEquals(2, freq.getCount("test"));
        assertEquals(1, freq.getCount("other"));
        assertEquals(3, freq.getSumFreq());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddValueNull() {
        freq.addValue(null);
    }

    @Test
    public void testAddValueIntAndLongSameValue() {
        // This test targets the bug in Math-90: adding a Long after an Integer should not throw ClassCastException
        // and should treat them as the same value if the comparator is fixed.
        freq.addValue(1);
        freq.addValue(1L);
        // After fix, both should be considered the same key, so count should be 2.
        assertEquals(2, freq.getCount(1));
        assertEquals(2, freq.getCount(1L));
        assertEquals(2, freq.getSumFreq());
    }

    @Test
    public void testAddValueIntAndLongDifferentValues() {
        freq.addValue(1);
        freq.addValue(2L);
        assertEquals(1, freq.getCount(1));
        assertEquals(1, freq.getCount(2L));
        assertEquals(2, freq.getSumFreq());
    }

    @Test
    public void testGetPctInt() {
        freq.addValue(1);
        freq.addValue(1);
        freq.addValue(2);
        assertEquals(2.0 / 3.0, freq.getPct(1), 1e-10);
        assertEquals(1.0 / 3.0, freq.getPct(2), 1e-10);
    }

    @Test
    public void testGetPctLong() {
        freq.addValue(1L);
        freq.addValue(1L);
        freq.addValue(2L);
        assertEquals(2.0 / 3.0, freq.getPct(1L), 1e-10);
        assertEquals(1.0 / 3.0, freq.getPct(2L), 1e-10);
    }

    @Test
    public void testGetPctChar() {
        freq.addValue('a');
        freq.addValue('a');
        freq.addValue('b');
        assertEquals(2.0 / 3.0, freq.getPct('a'), 1e-10);
        assertEquals(1.0 / 3.0, freq.getPct('b'), 1e-10);
    }

    @Test
    public void testGetCumFreqInt() {
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(3);
        assertEquals(1, freq.getCumFreq(1));
        assertEquals(2, freq.getCumFreq(2));
        assertEquals(3, freq.getCumFreq(3));
        assertEquals(0, freq.getCumFreq(0));
        assertEquals(3, freq.getCumFreq(4));
    }

    @Test
    public void testGetCumFreqLong() {
        freq.addValue(1L);
        freq.addValue(2L);
        freq.addValue(3L);
        assertEquals(1, freq.getCumFreq(1L));
        assertEquals(2, freq.getCumFreq(2L));
        assertEquals(3, freq.getCumFreq(3L));
        assertEquals(0, freq.getCumFreq(0L));
        assertEquals(3, freq.getCumFreq(4L));
    }

    @Test
    public void testGetCumPctInt() {
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(3);
        assertEquals(1.0 / 3.0, freq.getCumPct(1), 1e-10);
        assertEquals(2.0 / 3.0, freq.getCumPct(2), 1e-10);
        assertEquals(1.0, freq.getCumPct(3), 1e-10);
        assertEquals(0.0, freq.getCumPct(0), 1e-10);
        assertEquals(1.0, freq.getCumPct(4), 1e-10);
    }

    @Test
    public void testGetUniqueCount() {
        assertEquals(0, freq.getUniqueCount());
        freq.addValue(1);
        assertEquals(1, freq.getUniqueCount());
        freq.addValue(1);
        assertEquals(1, freq.getUniqueCount());
        freq.addValue(2);
        assertEquals(2, freq.getUniqueCount());
    }

    @Test
    public void testGetSumFreq() {
        assertEquals(0, freq.getSumFreq());
        freq.addValue(1);
        assertEquals(1, freq.getSumFreq());
        freq.addValue(2);
        assertEquals(2, freq.getSumFreq());
        freq.addValue(1);
        assertEquals(3, freq.getSumFreq());
    }

    @Test
    public void testValuesIterator() {
        freq.addValue(2);
        freq.addValue(1);
        freq.addValue(2);
        Iterator<?> iter = freq.valuesIterator();
        assertTrue(iter.hasNext());
        assertEquals(1, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(2, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void testEmptyFrequency() {
        assertEquals(0, freq.getCount(1));
        assertEquals(0, freq.getCount(1L));
        assertEquals(0, freq.getCount('a'));
        assertEquals(0, freq.getCount("test"));
        assertEquals(0.0, freq.getPct(1), 0.0);
        assertEquals(0, freq.getCumFreq(1));
        assertEquals(0.0, freq.getCumPct(1), 0.0);
        assertEquals(0, freq.getUniqueCount());
        assertEquals(0, freq.getSumFreq());
        assertFalse(freq.valuesIterator().hasNext());
    }

    @Test
    public void testAddValueMultipleTypes() {
        // Add values of different types and ensure no exception
        freq.addValue(1);
        freq.addValue(2L);
        freq.addValue('c');
        freq.addValue("string");
        // Each should be separate keys
        assertEquals(1, freq.getCount(1));
        assertEquals(1, freq.getCount(2L));
        assertEquals(1, freq.getCount('c'));
        assertEquals(1, freq.getCount("string"));
        assertEquals(4, freq.getSumFreq());
        assertEquals(4, freq.getUniqueCount());
    }

    @Test
    public void testCumFreqWithMixedTypes() {
        // This test may expose bugs in cumulative frequency when types are mixed
        freq.addValue(1);
        freq.addValue(2L);
        freq.addValue(3);
        // In the fixed version, values should be comparable across types.
        assertEquals(2, freq.getCumFreq(2)); // includes 1 and 2L (if 2L <= 2)
        assertEquals(3, freq.getCumFreq(3)); // includes 1, 2L, 3
    }
}