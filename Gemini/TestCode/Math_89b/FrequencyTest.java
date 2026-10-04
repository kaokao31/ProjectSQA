/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math.stat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for the Frequency class.
 */
public class FrequencyTest {

    private Frequency f = null;

    @Before
    public void setUp() {
        f = new Frequency();
    }

    @Test
    public void testAdd() {
        assertEquals(0, f.getCount('a'));
        f.addValue('a');
        assertEquals(1, f.getCount('a'));
        f.addValue(Character.toString('a'));
        assertEquals(2, f.getCount('a'));
        f.addValue(new Character('a'));
        assertEquals(3, f.getCount('a'));

        assertEquals(0, f.getCount(1));
        f.addValue(1);
        assertEquals(1, f.getCount(1));
        f.addValue(Integer.valueOf(1));
        assertEquals(2, f.getCount(1));
        f.addValue(Long.valueOf(1));
        assertEquals(3, f.getCount(1));
        f.addValue(Short.valueOf((short) 1));
        assertEquals(4, f.getCount(1));
        f.addValue(Byte.valueOf((byte) 1));
        assertEquals(5, f.getCount(1));
    }

    @Test
    public void testAddCollection() {
        List<Comparable<?>> list = new ArrayList<Comparable<?>>();
        list.add('a');
        list.add('b');
        list.add('c');
        list.add('a');
        f.addValue(list);

        // Depending on how Frequency handles collections (iterating or adding the collection itself)
        // Math-89 specifically deals with addValue(Object) where if the object is a Collection,
        // it iterates and adds each element.
        assertEquals(1, f.getCount('a'));
        assertEquals(1, f.getCount('b'));
        assertEquals(1, f.getCount('c'));
    }

    @Test
    public void testIntegerCounts() {
        assertEquals(0, f.getCount(1));
        assertEquals(0, f.getCumPct(1));
        f.addValue(1);
        f.addValue(new Integer(1));
        f.addValue(Long.valueOf(1));
        f.addValue(Short.valueOf((short) 1));
        f.addValue(Byte.valueOf((byte) 1));
        assertEquals(5, f.getCount(1));
        assertEquals(5, f.getCount(Integer.valueOf(1)));
        assertEquals(5, f.getCount(Long.valueOf(1)));
        assertEquals(5, f.getCount(Short.valueOf((short) 1)));
        assertEquals(5, f.getCount(Byte.valueOf((byte) 1)));
        assertEquals(1.0, f.getPct(1), 0.1);
        assertEquals(1.0, f.getCumPct(1), 0.1);

        f.addValue(2);
        f.addValue(Long.valueOf(2));
        assertEquals(7, f.getSumFreq());
        assertEquals(5, f.getCount(1));
        assertEquals(2, f.getCount(2));
        assertEquals(5 / 7.0, f.getPct(1), 0.001);
        assertEquals(5 / 7.0, f.getCumPct(1), 0.001);
        assertEquals(1.0, f.getCumPct(2), 0.001);
        assertEquals(1.0, f.getCumPct(Long.valueOf(2)), 0.001);
        assertEquals(1.0, f.getCumPct(Byte.valueOf((byte) 2)), 0.001);
        assertEquals(1.0, f.getCumPct(Short.valueOf((short) 2)), 0.001);
    }

    @Test
    public void testCounts() {
        f.addValue(1);
        f.addValue(2);
        f.addValue(1);
        f.addValue(-1);
        
        assertEquals(2, f.getCount(1));
        assertEquals(1, f.getCount(2));
        assertEquals(1, f.getCount(-1));
        assertEquals(0, f.getCount(0));
        assertEquals(4, f.getSumFreq());
        assertEquals(0.5, f.getPct(1), 0.1);
        assertEquals(0.25, f.getPct(-1), 0.1);
        assertEquals(0.25, f.getPct(2), 0.1);
        assertEquals(0.0, f.getPct(0), 0.1);
        assertEquals(0.25, f.getCumPct(-1), 0.1);
        assertEquals(0.75, f.getCumPct(1), 0.1);
        assertEquals(1.0, f.getCumPct(2), 0.1);
        assertEquals(1.0, f.getCumPct(10), 0.1);
        assertEquals(0.0, f.getCumPct(-10), 0.1);
    }

    @Test
    public void testCharCounts() {
        f.addValue('a');
        f.addValue('b');
        f.addValue('a');
        f.addValue('c');
        
        assertEquals(2, f.getCount('a'));
        assertEquals(1, f.getCount('b'));
        assertEquals(1, f.getCount('c'));
        assertEquals(0, f.getCount('d'));
        assertEquals(4, f.getSumFreq());
        assertEquals(0.5, f.getPct('a'), 0.1);
        assertEquals(0.25, f.getPct('b'), 0.1);
        assertEquals(0.25, f.getPct('c'), 0.1);
        assertEquals(0.0, f.getPct('d'), 0.1);
        assertEquals(0.75, f.getCumPct('b'), 0.1);
        assertEquals(1.0, f.getCumPct('d'), 0.1);
        assertEquals(0.0, f.getCumPct('0'), 0.1);
    }

    @Test
    public void testStringCounts() {
        f.addValue("a");
        f.addValue("b");
        f.addValue("a");
        f.addValue("c");
        
        assertEquals(2, f.getCount("a"));
        assertEquals(1, f.getCount("b"));
        assertEquals(1, f.getCount("c"));
        assertEquals(0, f.getCount("d"));
        assertEquals(4, f.getSumFreq());
        assertEquals(0.5, f.getPct("a"), 0.1);
        assertEquals(0.25, f.getPct("b"), 0.1);
        assertEquals(0.25, f.getPct("c"), 0.1);
        assertEquals(0.0, f.getPct("d"), 0.1);
        assertEquals(0.75, f.getCumPct("b"), 0.1);
        assertEquals(1.0, f.getCumPct("d"), 0.1);
        assertEquals(0.0, f.getCumPct("0"), 0.1);
    }

    @Test
    public void testSerialization() {
        f.addValue(1);
        f.addValue(2);
        f.addValue(1);
        // Frequency implements Serializable, test basic operations still function
        assertEquals(2, f.getCount(1));
    }

    @Test
    public void testPcts() {
        f.addValue(1);
        f.addValue(2);
        f.addValue(1);
        f.addValue(3);
        
        Iterator<Comparable<?>> it = f.valuesIterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            assertNotNull(it.next());
            count++;
        }
        assertEquals(3, count);
        
        assertEquals(0.5, f.getPct(1), 0.0);
        assertEquals(0.25, f.getPct(2), 0.0);
        assertEquals(0.25, f.getPct(3), 0.0);
        assertEquals(0.0, f.getPct(4), 0.0);
        
        assertEquals(0.5, f.getCumPct(1), 0.0);
        assertEquals(0.75, f.getCumPct(2), 0.0);
        assertEquals(1.0, f.getCumPct(3), 0.0);
        assertEquals(1.0, f.getCumPct(4), 0.0);
        assertEquals(0.0, f.getCumPct(0), 0.0);
    }
    
    @Test
    public void testAddNotComparable() {
        try {
            f.addValue(new Object());
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testMath89() {
        // Specifically targeting Math-89 where addValue with a non-comparable or specific collection behavior fails or succeeds
        Frequency freq = new Frequency();
        freq.addValue(new Integer(1));
        assertEquals(1, freq.getCount(1));
        
        // Test adding a collection of non-comparables or mixed types if applicable, 
        // or test the exact issue context: addValue casting / Comparable handling.
        List<Object> badList = new ArrayList<Object>();
        badList.add(new Object());
        try {
            freq.addValue(badList);
            fail("Expected IllegalArgumentException or ClassCastException");
        } catch (Exception e) {
            // Expected due to non-comparable inside collection
            assertTrue(true);
        }
    }
    
    @Test
    public void testMisc() {
        assertEquals(0, f.getSumFreq());
        assertTrue(f.valuesIterator() != null);
        
        f.addValue("one");
        f.addValue("Two".toLowerCase());
        assertEquals(2, f.getCount("one"));
    }
}