package org.apache.commons.math.stat;

import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;

import static org.junit.Assert.*;

public class FrequencyTest {

    private Frequency f;

    @Before
    public void setUp() {
        f = new Frequency();
    }

    @Test
    public void testEmptyFrequency() {
        assertEquals(0L, f.getCount('a'));
        assertEquals(0L, f.getCount(1));
        assertEquals(0L, f.getCount(1L));
        assertEquals(0L, f.getCount(1.0));
        assertEquals(0.0, f.getPct('a'), 1.0e-15);
        assertEquals(0.0, f.getPct(1), 1.0e-15);
        assertEquals(0.0, f.getPct(1L), 1.0e-15);
        assertEquals(0.0, f.getPct(1.0), 1.0e-15);
        assertEquals(0.0, f.getCumPct('a'), 1.0e-15);
        assertEquals(0.0, f.getCumPct(1), 1.0e-15);
        assertEquals(0.0, f.getCumPct(1L), 1.0e-15);
        assertEquals(0.0, f.getCumPct(1.0), 1.0e-15);
        assertNotNull(f.toString());
    }

    @Test
    public void testLongValues() {
        f.addValue(1L);
        f.addValue(2L);
        f.addValue(1L);
        f.addValue(Long.valueOf(3L));

        assertEquals(2L, f.getCount(1L));
        assertEquals(1L, f.getCount(2L));
        assertEquals(1L, f.getCount(3L));
        assertEquals(0L, f.getCount(4L));

        // Int overloads delegate to long
        assertEquals(2L, f.getCount(1));
        assertEquals(1L, f.getCount(2));

        assertEquals(4L, f.getSum());

        assertEquals(0.5, f.getPct(1L), 1.0e-15);
        assertEquals(0.25, f.getPct(2L), 1.0e-15);
        assertEquals(0.5, f.getPct(1), 1.0e-15);

        assertEquals(0.5, f.getCumPct(1L), 1.0e-15);
        assertEquals(0.75, f.getCumPct(2L), 1.0e-15);
        assertEquals(1.0, f.getCumPct(3L), 1.0e-15);
        assertEquals(1.0, f.getCumPct(10L), 1.0e-15);
        assertEquals(0.0, f.getCumPct(-1L), 1.0e-15);

        // Int overloads for cumPct
        assertEquals(0.5, f.getCumPct(1), 1.0e-15);

        Iterator<Entry<Object, Long>> it = f.entrySetIterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
    }

    @Test
    public void testCharValues() {
        f.addValue('a');
        f.addValue('b');
        f.addValue('a');

        assertEquals(2L, f.getCount('a'));
        assertEquals(1L, f.getCount('b'));
        assertEquals(0L, f.getCount('c'));

        assertEquals(2.0 / 3.0, f.getPct('a'), 1.0e-15);
        assertEquals(1.0 / 3.0, f.getPct('b'), 1.0e-15);
        assertEquals(0.0, f.getPct('c'), 1.0e-15);

        // Cast char to int/long or use direct char methods
        assertEquals(2.0 / 3.0, f.getCumPct('a'), 1.0e-15);
        assertEquals(1.0, f.getCumPct('b'), 1.0e-15);
        // Before 'a'
        assertEquals(0.0, f.getCumPct('`'), 1.0e-15);
        // After 'b'
        assertEquals(1.0, f.getCumPct('z'), 1.0e-15);
        
        // Testing int/long getCount with char values (characters are stored as Character objects or cast)
        // Frequency maps chars as Character objects
        assertEquals(2L, f.getCount(Character.valueOf('a')));
    }

    @Test
    public void testIntegerValues() {
        f.addValue(10);
        f.addValue(20);
        f.addValue(10);

        assertEquals(2L, f.getCount(10));
        assertEquals(1L, f.getCount(20));
        assertEquals(2L, f.getCount(10L));

        assertEquals(2.0 / 3.0, f.getPct(10), 1.0e-15);
        assertEquals(2.0 / 3.0, f.getPct(10L), 1.0e-15);

        assertEquals(2.0 / 3.0, f.getCumPct(10), 1.0e-15);
        assertEquals(2.0 / 3.0, f.getCumPct(10L), 1.0e-15);
        assertEquals(1.0, f.getCumPct(20), 1.0e-15);
        assertEquals(1.0, f.getCumPct(20L), 1.0e-15);
    }

    @Test
    public void testAddValueComparable() {
        f.addValue("one");
        f.addValue("two");
        f.addValue("one");

        assertEquals(2L, f.getCount("one"));
        assertEquals(1L, f.getCount("two"));
        assertEquals(0L, f.getCount("three"));

        assertEquals(2.0 / 3.0, f.getPct("one"), 1.0e-15);
        assertEquals(2.0 / 3.0, f.getCumPct("one"), 1.0e-15);
        assertEquals(1.0, f.getCumPct("two"), 1.0e-15);
        // Below range or uncomparable in natural order
        assertEquals(0.0, f.getCumPct("a"), 1.0e-15);
        assertEquals(1.0, f.getCumPct("z"), 1.0e-15);
    }

    @Test
    public void testCustomComparator() {
        // Case-insensitive string frequency
        Comparator<String> comp = String.CASE_INSENSITIVE_ORDER;
        Frequency freq = new Frequency(comp);
        
        freq.addValue("A");
        freq.addValue("a");
        freq.addValue("B");

        assertEquals(2L, freq.getCount("a"));
        assertEquals(2L, freq.getCount("A"));
        assertEquals(1L, freq.getCount("b"));
        assertEquals(3L, freq.getSum());
        
        assertEquals(2.0 / 3.0, freq.getPct("a"), 1.0e-15);
        assertEquals(1.0, freq.getCumPct("b"), 1.0e-15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddValueIncompatibleTypes() {
        f.addValue(1L);
        // Mixing Long and String with default natural comparator should fail class cast internally or throw
        f.addValue("string");
    }

    @Test
    public void testEqualsAndHashCode() {
        Frequency f1 = new Frequency();
        Frequency f2 = new Frequency();

        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());

        f1.addValue(1);
        assertFalse(f1.equals(f2));
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Some String"));

        f2.addValue(1);
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testGetUniqueCount() {
        assertEquals(0, f.getUniqueCount());
        f.addValue(1);
        f.addValue(1);
        f.addValue(2);
        assertEquals(2, f.getUniqueCount());
    }

    @Test
    public void testClear() {
        f.addValue(1);
        f.addValue(2);
        assertEquals(2L, f.getSum());
        f.clear();
        assertEquals(0L, f.getSum());
        assertEquals(0, f.getUniqueCount());
    }

    @Test
    public void testDoubleBugContextRatioFix() {
        // Specifically targeting potential issues with Double/Float casting or equals in getPct/getCumPct 
        // as seen in Math-75 where Double.valueOf() vs primitive or compareTo is used incorrectly.
        f.addValue(1.0);
        f.addValue(1.0);
        f.addValue(2.0);

        assertEquals(2L, f.getCount(1.0));
        assertEquals(1L, f.getCount(2.0));
        assertEquals(2.0 / 3.0, f.getPct(1.0), 1.0e-15);
        assertEquals(1.0, f.getCumPct(2.0), 1.0e-15);
        
        // Test with boxed Double objects explicitly
        assertEquals(2L, f.getCount(Double.valueOf(1.0)));
        assertEquals(2.0 / 3.0, f.getPct(Double.valueOf(1.0)), 1.0e-15);
        assertEquals(1.0, f.getCumPct(Double.valueOf(2.0)), 1.0e-15);
    }
}