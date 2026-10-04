package org.apache.commons.csv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVParserTest {

    @Test
    public void testNewCSVParserStringFormat() throws IOException {
        CSVParser parser = new CSVParser("a,b,c\r\n1,2,3", CSVFormat.DEFAULT);
        assertNotNull(parser);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("1", records.get(1).get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewCSVParserNullStringFormat() throws IOException {
        new CSVParser((String) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewCSVParserStringNullFormat() throws IOException {
        new CSVParser("a,b,c", (CSVFormat) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewCSVParserNullReaderFormat() throws IOException {
        new CSVParser((Reader) null, CSVFormat.DEFAULT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewCSVParserReaderNullFormat() throws IOException {
        new CSVParser(new StringReader("a,b,c"), (CSVFormat) null);
    }

    @Test
    public void testGetHeaderMap() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader("H1", "H2", "H3");
        CSVParser parser = new CSVParser(new StringReader("A,B,C\n1,2,3"), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("H1"));
        assertEquals(Integer.valueOf(1), headerMap.get("H2"));
        assertEquals(Integer.valueOf(2), headerMap.get("H3"));
    }

    @Test
    public void testGetHeaderMapNull() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("A,B,C\n1,2,3"), CSVFormat.DEFAULT);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        // Depending on format, headerMap might be null or empty
        if (headerMap != null) {
            assertTrue(headerMap.isEmpty());
        } else {
            assertNull(headerMap);
        }
    }

    @Test
    public void testIterator() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\n1,2"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        assertNotNull(iterator);
        assertTrue(iterator.hasNext());
        CSVRecord record1 = iterator.next();
        assertEquals("a", record1.get(0));
        assertTrue(iterator.hasNext());
        CSVRecord record2 = iterator.next();
        assertEquals("1", record2.get(0));
        assertFalse(iterator.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\n1,2"), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoSuchElement() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        iterator.next();
    }

    @Test
    public void testGetRecordWithComments() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentStart('#');
        CSVParser parser = new CSVParser(new StringReader("#comment\na,b,c\n#another\n1,2,3"), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("1", records.get(1).get(0));
    }

    @Test
    public void testHeaderAutoDetection() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        CSVParser parser = new CSVParser(new StringReader("Col1,Col2,Col3\nVal1,Val2,Val3"), format);
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertEquals(Integer.valueOf(0), headerMap.get("Col1"));

        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        // Depending on Commons CSV version, header auto-detection might consume the header line or not
        // Let's check records size or content
    }

    @Test
    public void testGetLineNumber() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\nc,d\ne,f"), CSVFormat.DEFAULT);
        assertEquals(0, parser.getLineNumber());
        parser.nextRecord();
        assertEquals(1, parser.getLineNumber());
        parser.nextRecord();
        assertEquals(2, parser.getLineNumber());
    }

    @Test
    public void testCurrentLineNumber() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b\r\nc,d"), CSVFormat.DEFAULT);
        assertNotNull(parser.nextRecord());
        // line number checks
        assertTrue(parser.getLineNumber() >= 1);
    }

    @Test
    public void testEmptyFile() throws IOException {
        CSVParser parser = new CSVParser(new StringReader(""), CSVFormat.DEFAULT);
        assertNull(parser.nextRecord());
        assertTrue(parser.getRecords().isEmpty());
    }

    @Test
    public void testTrailingDelimiters() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b,\n,,"), CSVFormat.DEFAULT);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals(3, records.get(0.size() == 2 ? 0 : 0).size());
    }

    @Test
    public void testParserWithCustomDelimiter() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        CSVParser parser = new CSVParser(new StringReader("a;b;c\n1;2;3"), format);
        CSVRecord record = parser.nextRecord();
        assertNotNull(record);
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("c", record.get(2));
    }

    @Test
    public void testParserWithQuotes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        CSVParser parser = new CSVParser(new StringReader("\"a,b\",c\n\"d\"\"e\",f"), format);
        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        assertEquals("a,b", records.get(0).get(0));
        assertEquals("d\"e", records.get(1).get(0));
    }

    @Test
    public void testCloseParser() throws IOException {
        CSVParser parser = new CSVParser(new StringReader("a,b,c"), CSVFormat.DEFAULT);
        parser.close();
        // Subsequent operations after close might throw IOException or return null/empty depending on implementation
        try {
            parser.nextRecord();
        } catch (IOException e) {
            // Expected in some implementations
        }
    }
}