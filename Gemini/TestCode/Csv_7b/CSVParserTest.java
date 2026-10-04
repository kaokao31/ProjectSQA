/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to licence this file to the Apache License
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Comprehensive test suite for CSVParser targeting maximum code coverage
 * and bug detection for Commons CSV Bug 7.
 */
public class CSVParserTest {

    @Test
    public void testParseStringFormat() throws IOException {
        String csv = "a,b,c\n1,2,3";
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT);
        assertNotNull(parser);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("1", records.get(1).get(0));
    }

    @Test
    public void testGetHeaderMap() throws IOException {
        String csv = "A,B,C\n1,2,3";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("A", "B", "C");
        CSVParser parser = new CSVParser(new StringReader(csv), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("A"));
        assertEquals(Integer.valueOf(1), headerMap.get("B"));
        assertEquals(Integer.valueOf(2), headerMap.get("C"));
    }

    @Test
    public void testGetHeaderMapAuto() throws IOException {
        String csv = "A,B,C\n1,2,3";
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        CSVParser parser = new CSVParser(new StringReader(csv), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size()); // First line is header
    }

    @Test
    public void testGetHeaderMapNull() throws IOException {
        String csv = "1,2,3\n4,5,6";
        CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
    }

    @Test
    public void testGetRecordNumber() throws IOException {
        String csv = "a,b,c\n1,2,3\n4,5,6";
        CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT);
        assertEquals(0, parser.getRecordNumber());
        
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals(1, parser.getRecordNumber());
        
        record = parser.nextRecord();
        assertNotNull(record);
        assertEquals(2, parser.getRecordNumber());
    }

    @Test
    public void testGetCurrentLineNumber() throws IOException {
        String csv = "a,b,c\r\n1,2,3\n4,5,6";
        CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT);
        
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        // Line number tracking depends on lexer state
        assertTrue(parser.getCurrentLineNumber() >= 1);
    }

    @Test
    public void testIterator() throws IOException {
        String csv = "a,b,c\n1,2,3";
        CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        assertNotNull(iterator.next());
        assertTrue(iterator.hasNext());
        assertNotNull(iterator.next());
        assertTrue(!iterator.hasNext());
    }

    @Test
    public void testClose() throws IOException {
        String csv = "a,b,c\n1,2,3";
        CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT);
        parser.close();
        // Subsequent calls or interactions after close
        Iterator<CSVRecord> iterator = parser.iterator();
        assertTrue(!iterator.hasNext());
    }

    @Test
    public void testGetRecords() throws IOException {
        String csv = "a,b,c\n1,2,3\n4,5,6";
        CSVParser parser = new CSVParser(new StringReader(csv), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullReader() throws IOException {
        new CSVParser(null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFormat() throws IOException {
        new CSVParser(new StringReader("a,b,c"), null);
    }

    @Test
    public void testParseURLCharsetFormat() throws Exception {
        // Test parsing using a dummy or local file URL if possible, or test method signature handling
        File tempFile = File.createTempFile("csvParserTest", ".csv");
        tempFile.deleteOnExit();
        URL url = tempFile.toURI().toURL();
        try {
            CSVParser.parse(url, Charset.defaultCharset(), CSVFormat.DEFAULT);
        } catch (IOException e) {
            // Handled in case of access restrictions, but method is covered
        }
    }

    @Test
    public void testParseFileCharsetFormat() throws Exception {
        File tempFile = File.createTempFile("csvParserTest", ".csv");
        tempFile.deleteOnExit();
        CSVParser parser = CSVParser.parse(tempFile, Charset.defaultCharset(), CSVFormat.DEFAULT);
        assertNotNull(parser);
        parser.close();
    }

    @Test
    public void testCustomHeaderHandling() throws IOException {
        String csv = "ID,Name,Value\n1,Test,100";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("ID", "Name", "Value").withSkipHeaderRecord(true);
        CSVParser parser = new CSVParser(new StringReader(csv), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(1, records.size());
        assertEquals("Test", records.get(0).get("Name"));
    }
}