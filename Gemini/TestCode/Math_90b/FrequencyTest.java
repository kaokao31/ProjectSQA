package org.apache.commons.math.stat;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
        assertEquals(0L, f.getCount("test"));

        assertEquals(0.0, f.getPct('a'), 0.0001);
        assertEquals(0.0, f.getPct(1), 0.0001);
        assertEquals(0.0, f.getPct(1L), 0.0001);
        assertEquals(0.0, f.getPct("test"), 0.0001);

        assertEquals(0.0, f.getCumPct('a'), 0.0001);
        assertEquals(0.0, f.getCumPct(1), 0.0001);
        assertEquals(0.0, f.getCumPct(1L), 0.0001);
        assertEquals(0.0, f.getCumPct("test"), 0.0001);

        assertFalse(f.valuesIterator().hasNext());
        assertFalse(f.entrySetIterator().hasNext());
    }

    @Test
    public void testIntegerFrequencies() {
        f.addValue(1);
        f.addValue(2);
        f.addValue(1);
        f.addValue(Integer.valueOf(3));
        f.addValue(2);
        f.addValue(1);

        assertEquals(3L, f.getCount(1));
        assertEquals(2L, f.getCount(2));
        assertEquals(1L, f.getCount(3));
        assertEquals(0L, f.getCount(4));

        assertEquals(3L, f.getCount(Integer.valueOf(1)));

        assertEquals(3.0 / 6.0, f.getPct(1), 0.0001);
        assertEquals(2.0 / 6.0, f.getPct(2), 0.0001);
        assertEquals(1.0 / 6.0, f.getPct(3), 0.0001);
        assertEquals(0.0, f.getPct(4), 0.0001);

        assertEquals(3.0 / 6.0, f.getCumPct(1), 0.0001);
        assertEquals(5.0 / 6.0, f.getCumPct(2), 0.0001);
        assertEquals(6.0 / 6.0, f.getCumPct(3), 0.0001);
        assertEquals(1.0, f.getCumPct(10), 0.0001);
        assertEquals(0.0, f.getCumPct(0), 0.0001);

        // Test with long overloads for int values
        assertEquals(3L, f.getCount(1L));
        assertEquals(3.0 / 6.0, f.getPct(1L), 0.0001);
        assertEquals(3.0 / 6.0, f.getCumPct(1L), 0.0001);
    }

    @Test
    public void testLongFrequencies() {
        f.addValue(10L);
        f.addValue(20L);
        f.addValue(10L);
        f.addValue(Long.valueOf(30L));

        assertEquals(2L, f.getCount(10L));
        assertEquals(1L, f.getCount(20L));
        assertEquals(1L, f.getCount(30L));
        assertEquals(0L, f.getCount(40L));

        assertEquals(2L, f.getCount(10)); // int overload for long stored values
        assertEquals(2.0 / 4.0, f.getPct(10L), 0.0001);
        assertEquals(2.0 / 4.0, f.getPct(10), 0.0001);

        assertEquals(2.0 / 4.0, f.getCumPct(10L), 0.0001);
        assertEquals(3.0 / 4.0, f.getCumPct(20L), 0.0001);
        assertEquals(4.0 / 4.0, f.getCumPct(30L), 0.0001);
        assertEquals(0.0, f.getCumPct(5L), 0.0001);
        assertEquals(1.0, f.getCumPct(40L), 0.0001);
    }

    @Test
    public void testCharFrequencies() {
        f.addValue('a');
        f.addValue('b');
        f.addValue('a');

        assertEquals(2L, f.getCount('a'));
        assertEquals(1L, f.getCount('b'));
        assertEquals(0L, f.getCount('c'));

        assertEquals(2.0 / 3.0, f.getPct('a'), 0.0001);
        assertEquals(1.0 / 3.0, f.getPct('b'), 0.0001);
        assertEquals(0.0, f.getPct('c'), 0.0001);

        assertEquals(2.0 / 3.0, f.getCumPct('a'), 0.0001);
        assertEquals(1.0, f.getCumPct('b'), 0.0001);
        assertEquals(1.0, f.getCumPct('z'), 0.0001);
        assertEquals(0.0, f.getCumPct('0'), 0.0001);
    }

    @Test
    public void testStringFrequencies() {
        f.addValue("one");
        f.addValue("two");
        f.addValue("one");

        assertEquals(2L, f.getCount("one"));
        assertEquals(1L, f.getCount("two"));
        assertEquals(0L, f.getCount("three"));

        assertEquals(2.0 / 3.0, f.getPct("one"), 0.0001);
        assertEquals(1.0 / 3.0, f.getPct("two"), 0.0001);
        assertEquals(0.0, f.getPct("three"), 0.0001);

        // CumPct on Strings uses natural ordering
        assertEquals(2.0 / 3.0, f.getCumPct("one"), 0.0001);
        assertEquals(1.0, f.getCumPct("two"), 0.0001);
        assertEquals(0.0, f.getCumPct("a"), 0.0001);
        assertEquals(1.0, f.getCumPct("z"), 0.0001);
    }

    @Test
    public void testAddCollection() {
        List<Comparable<?>> list = new ArrayList<Comparable<?>>();
        list.add(1);
        list.add(2);
        list.add(2);
        list.add("test");

        f.addValue(list); // Wait, addValue takes Object, but Frequency has addValue(Collection) or does it? 
        // Let's check standard Commons Math Frequency API: addValue(Object) handles collections or addCollection(Collection)?
        // Actually Frequency has addValue(Object) which unpacks Collections if passed, or addCollection(Collection<?> coll).
        // Let's test addCollection if it exists, or test addValue with a collection.
        
        Frequency f2 = new Frequency();
        f2.addValue(list); // Frequency.addValue(Object) iterates if Object is a Collection
        
        assertEquals(1L, f2.getCount(1));
        assertEquals(2L, f2.getCount(2));
        assertEquals(1L, f2.getCount("test"));
    }

    @Test
    public void testClear() {
        f.addValue(1);
        f.addValue(2);
        assertEquals(2L, f.getSumFreq());
        
        f.clear();
        assertEquals(0L, f.getSumFreq());
        assertEquals(0L, f.getCount(1));
    }

    @Test
    public void testIterators() {
        f.addValue(2);
        f.addValue(1);
        f.addValue(3);

        Iterator<Comparable<?>> valIt = f.valuesIterator();
        assertNotNull(valIt);
        assertTrue(valIt.hasNext());
        
        Iterator<?> entryIt = f.entrySetIterator();
        assertNotNull(entryIt);
        assertTrue(entryIt.hasNext());
    }

    @Test
    public void testEqualsAndHashCode() {
        Frequency f1 = new Frequency();
        Frequency f2 = new Frequency();

        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());

        f1.addValue(1);
        assertFalse(f1.equals(f2));

        f2.addValue(1);
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());

        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Some String"));
    }

    @Test
    public void testSerialization() throws Exception {
        f.addValue(1);
        f.addValue("test");
        f.addValue('x');

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(f);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Frequency deserialized = (Frequency) ois.readObject();

        assertEquals(f, deserialized);
        assertEquals(1L, deserialized.getCount(1));
        assertEquals(1L, deserialized.getCount("test"));
        assertEquals(1L, deserialized.getCount('x'));
    }

    @Test
    public void testGetCumFreq() {
        f.addValue(1);
        f.addValue(2);
        f.addValue(2);
        f.addValue(3);

        assertEquals(0L, f.getCumFreq(0));
        assertEquals(1L, f.getCumFreq(1));
        assertEquals(3L, f.getCumFreq(2));
        assertEquals(4L, f.getCumFreq(3));
        assertEquals(4L, f.getCumFreq(10));

        // Long overloads
        assertEquals(3L, f.getCumFreq(2L));
        // Char overloads
        Frequency charFreq = new Frequency();
        charFreq.addValue('a');
        charFreq.addValue('c');
        assertEquals(1L, charFreq.getCumFreq('b')); // 'b' falls between 'a' and 'c'
        assertEquals(1L, charFreq.getCumFreq('c'));
        assertEquals(2L, charFreq.getCumFreq('d'));
        assertEquals(0L, charFreq.getCumFreq('`'));

        // String overloads
        Frequency strFreq = new Frequency();
        strFreq.addValue("apple");
        strFreq.addValue("cat");
        assertEquals(1L, strFreq.getCumFreq("banana"));
        assertEquals(1L, strFreq.getCumFreq("cat"));
        assertEquals(2L, strFreq.getCumFreq("dog"));
        assertEquals(0L, strFreq.getCumFreq("aardvark"));
    }
}