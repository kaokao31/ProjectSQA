package org.apache.commons.math.stat;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;

public class FrequencyTest {

    private Frequency freq;

    @Before
    public void setUp() {
        freq = new Frequency();
    }

    // ---------- addValue tests ----------

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
        freq.addValue(10L);
        freq.addValue(-5L);
        freq.addValue(10L);
        assertEquals(2, freq.getCount(10L));
        assertEquals(1, freq.getCount(-5L));
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
        freq.addValue("apple");
        freq.addValue("banana");
        freq.addValue("apple");
        assertEquals(2, freq.getCount("apple"));
        assertEquals(1, freq.getCount("banana"));
        assertEquals(3, freq.getSumFreq());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddValueNull() {
        freq.addValue(null);
    }

    // ---------- getCount tests ----------

    @Test
    public void testGetCountNonExistent() {
        assertEquals(0, freq.getCount(999));
        assertEquals(0, freq.getCount(999L));
        assertEquals(0, freq.getCount('z'));
        assertEquals(0, freq.getCount("nonexistent"));
    }

    @Test
    public void testGetCountAfterClear() {
        freq.addValue(1);
        freq.addValue(2);
        freq.clear();
        assertEquals(0, freq.getCount(1));
        assertEquals(0, freq.getSumFreq());
    }

    // ---------- getCumFreq tests ----------

    @Test
    public void testGetCumFreqInt() {
        freq.addValue(1);
        freq.addValue(3);
        freq.addValue(2);
        freq.addValue(1);
        // sorted: 1(2), 2(1), 3(1)
        assertEquals(0, freq.getCumFreq(0));
        assertEquals(2, freq.getCumFreq(1));
        assertEquals(3, freq.getCumFreq(2));
        assertEquals(4, freq.getCumFreq(3));
        assertEquals(4, freq.getCumFreq(10));
    }

    @Test
    public void testGetCumFreqLong() {
        freq.addValue(-5L);
        freq.addValue(0L);
        freq.addValue(5L);
        // sorted: -5(1), 0(1), 5(1)
        assertEquals(0, freq.getCumFreq(-10L));
        assertEquals(1, freq.getCumFreq(-5L));
        assertEquals(2, freq.getCumFreq(0L));
        assertEquals(3, freq.getCumFreq(5L));
        assertEquals(3, freq.getCumFreq(10L));
    }

    @Test
    public void testGetCumFreqComparable() {
        freq.addValue("a");
        freq.addValue("c");
        freq.addValue("b");
        // sorted: "a"(1), "b"(1), "c"(1)
        assertEquals(0, freq.getCumFreq("0"));
        assertEquals(1, freq.getCumFreq("a"));
        assertEquals(2, freq.getCumFreq("b"));
        assertEquals(3, freq.getCumFreq("c"));
        assertEquals(3, freq.getCumFreq("z"));
    }

    // ---------- getPct tests ----------

    @Test
    public void testGetPctInt() {
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(1);
        assertEquals(2.0/3.0, freq.getPct(1), 1e-10);
        assertEquals(1.0/3.0, freq.getPct(2), 1e-10);
        assertEquals(0.0, freq.getPct(3), 1e-10);
    }

    @Test
    public void testGetPctLong() {
        freq.addValue(-5L);
        freq.addValue(0L);
        freq.addValue(5L);
        // This test targets potential bug with negative long values
        assertEquals(1.0/3.0, freq.getPct(-5L), 1e-10);
        assertEquals(1.0/3.0, freq.getPct(0L), 1e-10);
        assertEquals(1.0/3.0, freq.getPct(5L), 1e-10);
    }

    @Test
    public void testGetPctChar() {
        freq.addValue('x');
        freq.addValue('y');
        freq.addValue('x');
        assertEquals(2.0/3.0, freq.getPct('x'), 1e-10);
        assertEquals(1.0/3.0, freq.getPct('y'), 1e-10);
    }

    @Test
    public void testGetPctComparable() {
        freq.addValue("cat");
        freq.addValue("dog");
        freq.addValue("cat");
        assertEquals(2.0/3.0, freq.getPct("cat"), 1e-10);
        assertEquals(1.0/3.0, freq.getPct("dog"), 1e-10);
        assertEquals(0.0, freq.getPct("bird"), 1e-10);
    }

    // ---------- getCumPct tests ----------

    @Test
    public void testGetCumPctInt() {
        freq.addValue(1);
        freq.addValue(3);
        freq.addValue(2);
        freq.addValue(1);
        // sorted: 1(2), 2(1), 3(1)
        assertEquals(0.0, freq.getCumPct(0), 1e-10);
        assertEquals(2.0/4.0, freq.getCumPct(1), 1e-10);
        assertEquals(3.0/4.0, freq.getCumPct(2), 1e-10);
        assertEquals(4.0/4.0, freq.getCumPct(3), 1e-10);
        assertEquals(1.0, freq.getCumPct(10), 1e-10);
    }

    @Test
    public void testGetCumPctLong() {
        freq.addValue(-5L);
        freq.addValue(0L);
        freq.addValue(5L);
        // sorted: -5(1), 0(1), 5(1)
        assertEquals(0.0, freq.getCumPct(-10L), 1e-10);
        assertEquals(1.0/3.0, freq.getCumPct(-5L), 1e-10);
        assertEquals(2.0/3.0, freq.getCumPct(0L), 1e-10);
        assertEquals(3.0/3.0, freq.getCumPct(5L), 1e-10);
        assertEquals(1.0, freq.getCumPct(10L), 1e-10);
    }

    @Test
    public void testGetCumPctComparable() {
        freq.addValue("a");
        freq.addValue("c");
        freq.addValue("b");
        // sorted: "a"(1), "b"(1), "c"(1)
        assertEquals(0.0, freq.getCumPct("0"), 1e-10);
        assertEquals(1.0/3.0, freq.getCumPct("a"), 1e-10);
        assertEquals(2.0/3.0, freq.getCumPct("b"), 1e-10);
        assertEquals(3.0/3.0, freq.getCumPct("c"), 1e-10);
        assertEquals(1.0, freq.getCumPct("z"), 1e-10);
    }

    // ---------- getSumFreq tests ----------

    @Test
    public void testGetSumFreqEmpty() {
        assertEquals(0, freq.getSumFreq());
    }

    @Test
    public void testGetSumFreqAfterAdd() {
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(1);
        assertEquals(3, freq.getSumFreq());
    }

    // ---------- valuesIterator tests ----------

    @Test
    public void testValuesIteratorEmpty() {
        assertFalse(freq.valuesIterator().hasNext());
    }

    @Test
    public void testValuesIteratorOrder() {
        freq.addValue(3);
        freq.addValue(1);
        freq.addValue(2);
        Iterator<?> it = freq.valuesIterator();
        assertEquals(1, it.next());
        assertEquals(2, it.next());
        assertEquals(3, it.next());
        assertFalse(it.hasNext());
    }

    // ---------- toString tests ----------

    @Test
    public void testToStringNotEmpty() {
        freq.addValue(1);
        freq.addValue(2);
        String str = freq.toString();
        assertNotNull(str);
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2"));
    }

    // ---------- clear tests ----------

    @Test
    public void testClear() {
        freq.addValue(1);
        freq.addValue(2);
        freq.clear();
        assertEquals(0, freq.getSumFreq());
        assertEquals(0, freq.getCount(1));
        assertFalse(freq.valuesIterator().hasNext());
    }

    // ---------- Edge cases: large numbers and overflow ----------

    @Test
    public void testLargeValues() {
        freq.addValue(Long.MAX_VALUE);
        freq.addValue(Long.MIN_VALUE);
        freq.addValue(0L);
        assertEquals(1, freq.getCount(Long.MAX_VALUE));
        assertEquals(1, freq.getCount(Long.MIN_VALUE));
        assertEquals(1, freq.getCount(0L));
        assertEquals(3, freq.getSumFreq());
        assertEquals(1.0/3.0, freq.getPct(Long.MAX_VALUE), 1e-10);
        assertEquals(1.0/3.0, freq.getPct(Long.MIN_VALUE), 1e-10);
    }

    @Test
    public void testDuplicateValues() {
        freq.addValue(5);
        freq.addValue(5);
        freq.addValue(5);
        assertEquals(3, freq.getCount(5));
        assertEquals(1.0, freq.getPct(5), 1e-10);
        assertEquals(1.0, freq.getCumPct(5), 1e-10);
    }

    // ---------- Test for bug: getPct with negative long (Math-75) ----------

    @Test
    public void testBugMath75NegativeLongPct() {
        // This test specifically targets the bug in Math-75
        freq.addValue(-1L);
        freq.addValue(0L);
        freq.addValue(1L);
        // Expected: each value appears once, so pct = 1/3 ≈ 0.3333
        assertEquals(1.0/3.0, freq.getPct(-1L), 1e-10);
        assertEquals(1.0/3.0, freq.getPct(0L), 1e-10);
        assertEquals(1.0/3.0, freq.getPct(1L), 1e-10);
        // Also test cumulative percentages
        assertEquals(1.0/3.0, freq.getCumPct(-1L), 1e-10);
        assertEquals(2.0/3.0, freq.getCumPct(0L), 1e-10);
        assertEquals(1.0, freq.getCumPct(1L), 1e-10);
    }

    @Test
    public void testBugMath75NegativeOnly() {
        // Only negative values
        freq.addValue(-10L);
        freq.addValue(-5L);
        freq.addValue(-1L);
        assertEquals(1.0/3.0, freq.getPct(-10L), 1e-10);
        assertEquals(1.0/3.0, freq.getPct(-5L), 1e-10);
        assertEquals(1.0/3.0, freq.getPct(-1L), 1e-10);
        assertEquals(1.0/3.0, freq.getCumPct(-10L), 1e-10);
        assertEquals(2.0/3.0, freq.getCumPct(-5L), 1e-10);
        assertEquals(1.0, freq.getCumPct(-1L), 1e-10);
    }
}