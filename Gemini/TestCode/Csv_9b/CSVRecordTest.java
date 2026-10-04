/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * work for additional information regarding copyright ownership.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class CSVRecordTest {

    private CSVRecord createRecord(String[] values, Map<String, Integer> mapping) {
        return new CSVRecord(values, mapping, null, 0L);
    }

    private CSVRecord createRecord(String[] values, Map<String, Integer> mapping, String comment, long recordNumber) {
        return new CSVRecord(values, mapping, comment, recordNumber);
    }

    @Test
    public void testGetWithValidName() {
        final Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        final CSVRecord record = createRecord(new String[]{"valA", "valB"}, mapping);

        assertEquals("valA", record.get("A"));
        assertEquals("valB", record.get("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithNullMapping() {
        final CSVRecord record = createRecord(new String[]{"valA"}, null);
        record.get("A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWithInvalidName() {
        final Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        final CSVRecord record = createRecord(new String[]{"valA"}, mapping);
        record.get("NON_EXISTENT");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetWithUnmappedName() {
        final Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", null);
        final CSVRecord record = createRecord(new String[]{"valA"}, mapping);
        record.get("A");
    }

    @Test
    public void testGetWithValidIndex() {
        final CSVRecord record = createRecord(new String[]{"val0", "val1"}, null);

        assertEquals("val0", record.get(0));
        assertEquals("val1", record.get(1));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetWithNegativeIndex() {
        final CSVRecord record = createRecord(new String[]{"val0"}, null);
        record.get(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetWithOutOfBoundsIndex() {
        final CSVRecord record = createRecord(new String[]{"val0"}, null);
        record.get(1);
    }

    @Test
    public void testPutInMap() {
        final Map<String, Integer> mapping = new HashMap<>();
        mapping.put("First", 0);
        mapping.put("Second", 1);
        final CSVRecord record = createRecord(new String[]{"A", "B"}, mapping);

        final Map<String, String> targetMap = new HashMap<>();
        final Map<String, String> result = record.putIn(targetMap);

        assertSame(targetMap, result);
        assertEquals(2, targetMap.size());
        assertEquals("A", targetMap.get("First"));
        assertEquals("B", targetMap.get("Second"));
    }

    @Test
    public void testPutInMapWithNullTarget() {
        final Map<String, Integer> mapping = new HashMap<>();
        mapping.put("First", 0);
        final CSVRecord record = createRecord(new String[]{"A"}, mapping);

        final Map<String, String> result = record.putIn(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A", result.get("First"));
    }

    @Test
    public void testComment() {
        final CSVRecord recordWithComment = createRecord(new String[]{"A"}, null, "This is a comment", 1L);
        assertEquals("This is a comment", recordWithComment.getComment());

        final CSVRecord recordWithoutComment = createRecord(new String[]{"A"}, null, null, 1L);
        assertNull(recordWithoutComment.getComment());
    }

    @Test
    public void testRecordNumber() {
        final CSVRecord record = createRecord(new String[]{"A"}, null, null, 42L);
        assertEquals(42L, record.getRecordNumber());
    }

    @Test
    public void testSize() {
        final CSVRecord recordEmpty = createRecord(new String[]{}, null);
        assertEquals(0, recordEmpty.size());
        assertTrue(recordEmpty.isConsistent());

        final CSVRecord recordNonEmpty = createRecord(new String[]{"A", "B", "C"}, null);
        assertEquals(3, recordNonEmpty.size());
        assertTrue(recordNonEmpty.isConsistent());
    }

    @Test
    public void testIterator() {
        final CSVRecord record = createRecord(new String[]{"A", "B", "C"}, null);
        final Iterator<String> iterator = record.iterator();

        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertEquals("A", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("B", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("C", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testToList() {
        final CSVRecord record = createRecord(new String[]{"A", "B"}, null);
        final List<String> list = record.toList();

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test
    public void testToString() {
        final CSVRecord record = createRecord(new String[]{"A", "B"}, null, "comment", 10L);
        final String str = record.toString();

        assertNotNull(str);
        assertTrue(str.contains("A"));
        assertTrue(str.contains("B"));
    }

    @Test
    public void testValues() {
        final String[] values = new String[]{"val1", "val2"};
        final CSVRecord record = createRecord(values, null);

        assertArrayEquals(values, record.values());
    }
}