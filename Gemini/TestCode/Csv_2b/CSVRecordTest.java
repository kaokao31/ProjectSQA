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
package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

/**
 * Test cases for {@link CSVRecord}.
 */
public class CSVRecordTest {

    private CSVRecord createRecord(String[] values, String mappingName) {
        Map<String, Integer> mapping = null;
        if (mappingName != null) {
            mapping = new HashMap<String, Integer>();
            for (int i = 0; i < values.length; i++) {
                mapping.put(mappingName + i, Integer.valueOf(i));
            }
        }
        return new CSVRecord(values, mapping, null, 0L);
    }

    @Test
    public void testGetWithValidName() {
        CSVRecord record = createRecord(new String[]{"A", "B", "C"}, "col");
        assertEquals("A", record.get("col0"));
        assertEquals("B", record.get("col1"));
        assertEquals("C", record.get("col2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithInvalidName() {
        CSVRecord record = createRecord(new String[]{"A"}, "col");
        record.get("nonexistent");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetWithNullMapping() {
        CSVRecord record = createRecord(new String[]{"A"}, null);
        record.get("col0");
    }

    @Test
    public void testGetWithValidIndex() {
        CSVRecord record = createRecord(new String[]{"A", "B", "C"}, null);
        assertEquals("A", record.get(0));
        assertEquals("B", record.get(1));
        assertEquals("C", record.get(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetWithNegativeIndex() {
        CSVRecord record = createRecord(new String[]{"A"}, null);
        record.get(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetWithOutOfBoundsIndex() {
        CSVRecord record = createRecord(new String[]{"A"}, null);
        record.get(1);
    }

    @Test
    public void testIsConsistent() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("A", 0);
        
        CSVRecord recordWithMapping = new CSVRecord(new String[]{"val"}, mapping, null, 1L);
        assertTrue(recordWithMapping.isConsistent());

        CSVRecord recordWithoutMapping = new CSVRecord(new String[]{"val"}, null, null, 1L);
        assertTrue(recordWithoutMapping.isConsistent());
    }

    @Test
    public void testIsSetByName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        
        CSVRecord record = new CSVRecord(new String[]{"A", null}, mapping, null, 1L);
        assertTrue(record.isSet("col0"));
        assertFalse(record.isSet("col1"));
        assertFalse(record.isSet("nonexistent"));
    }

    @Test(expected = IllegalStateException.class)
    public void testIsSetByNameNullMapping() {
        CSVRecord record = createRecord(new String[]{"A"}, null);
        record.isSet("col0");
    }

    @Test
    public void testIsSetByIndex() {
        CSVRecord record = new CSVRecord(new String[]{"A", null}, null, null, 1L);
        assertTrue(record.isSet(0));
        assertFalse(record.isSet(1));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testIsSetByIndexOutOfBounds() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        record.isSet(5);
    }

    @Test
    public void testGetComment() {
        CSVRecord recordWithComment = new CSVRecord(new String[]{"A"}, null, "This is a comment", 1L);
        assertEquals("This is a comment", recordWithComment.getComment());

        CSVRecord recordWithoutComment = new CSVRecord(new String[]{"A"}, null, null, 1L);
        assertNull(recordWithoutComment.getComment());
    }

    @Test
    public void testGetRecordNumber() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 42L);
        assertEquals(42L, record.getRecordNumber());
    }

    @Test
    public void testSize() {
        CSVRecord record1 = new CSVRecord(new String[]{"A", "B", "C"}, null, null, 1L);
        assertEquals(3, record1.size());

        CSVRecord record2 = new CSVRecord(new String[0], null, null, 1L);
        assertEquals(0, record2.size());
    }

    @Test
    public void testIterator() {
        CSVRecord record = new CSVRecord(new String[]{"A", "B"}, null, null, 1L);
        Iterator<String> iterator = record.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("B", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testPutIn() {
        Map<String, String> map = new HashMap<String, String>();
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("first", 0);
        mapping.put("second", 1);

        CSVRecord record = new CSVRecord(new String[]{"A", "B"}, mapping, null, 1L);
        Map<String, String> result = record.putIn(map);
        assertSame(map, result);
        assertEquals("A", result.get("first"));
        assertEquals("B", result.get("second"));
    }

    @Test
    public void testToString() {
        CSVRecord record = new CSVRecord(new String[]{"A", "B"}, null, "comment", 1L);
        String toString = record.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("A"));
        assertTrue(toString.contains("B"));
    }

    @Test
    public void testToMap() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        mapping.put("col2", 1);

        CSVRecord record = new CSVRecord(new String[]{"Val1", "Val2"}, mapping, null, 1L);
        Map<String, String> map = record.toMap();
        assertNotNull(map);
        assertEquals("Val1", map.get("col1"));
        assertEquals("Val2", map.get("col2"));
    }
}