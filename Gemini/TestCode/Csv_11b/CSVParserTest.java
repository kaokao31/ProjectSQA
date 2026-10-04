package org.apache.commons.csv;

import org.junit.Test;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CSVParserTest {

    @Test
    public void testNewParserWithNullFormat() {
        try {
            new CSVParser(new StringReader("a,b,c"), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test
    public void testParseReaderWithNullFormat() {
        try {
            CSVParser.parse(new StringReader("a,b,c"), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test
    public void testParseStringWithNullFormat() {
        try {
            CSVParser.parse("a,b,c", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Expected
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test
    public void testParseURLWithNullCharset() {
        try {
            URL url = new URL("http://localhost/");
            CSVParser.parse(url, (Charset) null, CSVFormat.DEFAULT);
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            // Expected (MalformedURLException or IllegalArgumentException)
        }
    }

    @Test
    public void testParseURLWithNullStringCharset() {
        try {
            URL url = new URL("http://localhost/");
            CSVParser.parse(url, (String) null, CSVFormat.DEFAULT);
            fail("Expected IllegalArgumentException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testParseFileWithNullCharset() {
        try {
            CSVParser.parse(new java.io.File("nonexistent"), (Charset) null, CSVFormat.DEFAULT);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Expected
        } catch (IOException e) {
            // Also acceptable if file doesn't exist before charset check
        }
    }

    @Test
    public void testParseFileWithNullStringCharset() {
        try {
            CSVParser.parse(new java.io.File("nonexistent"), (String) null, CSVFormat.DEFAULT);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Expected
        } catch (IOException e) {
            // Acceptable
        }
    }

    @Test
    public void testBasicParsing() throws IOException {
        String code = "A,B,C\r\n1,2,3\r\n#comment\r\na,b,c";
        CSVFormat format = CSVFormat.DEFAULT.withHeader("Col1", "Col2", "Col3").withCommentMarker('#');
        CSVParser parser = CSVParser.parse(code, format);

        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(3, headerMap.size());
        assertTrue(headerMap.containsKey("Col1"));

        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());

        CSVRecord record1 = records.get(0);
        assertEquals("1", record1.get(0));
        assertEquals("2", record1.get("Col2"));
        assertEquals(2L, record1.getRecordNumber());
        assertEquals(3, record1.size());
        assertFalse(record1.isConsistent());

        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testIteratorAndStreamMethods() throws IOException {
        String code = "a,b\n1,2";
        CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
        Iterator<CSVRecord> iterator = parser.iterator();
        assertTrue(iterator.hasNext());
        CSVRecord record = iterator.next();
        assertEquals("a", record.get(0));
        assertTrue(iterator.hasNext());
        assertEquals("1", iterator.next().get(0));
        assertFalse(iterator.hasNext());

        try {
            iterator.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        parser.close();
    }

    @Test
    public void testHeaderAutoDetection() throws IOException {
        String code = "Header1,Header2\nValue1,Value2";
        CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT.withHeader());
        Map<String, Integer> headerMap = parser.getHeaderMap();
        assertNotNull(headerMap);
        assertEquals(2, headerMap.size());
        assertEquals(0, (int) headerMap.get("Header1"));
        assertEquals(1, (int) headerMap.get("Header2"));
        
        List<CSVRecord> list = parser.getRecords();
        assertEquals(1, list.size());
        assertEquals("Value1", list.get(0).get("Header1"));
        parser.close();
    }

    @Test
    public void testHeaderWithEmptyNamesAndDuplicates() throws IOException {
        // Specifically targeting header handling logic which is frequently a source of bugs in D4J Csv 11
        String code = ",,a,a\n1,2,3,4";
        CSVFormat format = CSVFormat.DEFAULT.withHeader();
        CSVParser parser = CSVParser.parse(code, format);
        Map<String, Integer> map = parser.getHeaderMap();
        assertNotNull(map);
        parser.close();
    }
    
    @Test
    public void testHeaderExplicitArray() throws IOException {
        String code = "1,2,3";
        String[] header = {"H1", "H2", "H3"};
        CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT.withHeader(header));
        Map<String, Integer> map = parser.getHeaderMap();
        assertEquals(3, map.size());
        assertEquals(0, (int) map.get("H1"));
        parser.close();
    }

    @Test
    public void testGetCurrentLineNumber() throws IOException {
        String code = "a,b\r\nc,d\n";
        CSVParser parser = CSVParser.parse(code, CSVFormat.DEFAULT);
        assertEquals(0L, parser.getCurrentLineNumber());
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue(it.hasNext());
        it.next();
        assertEquals(1L, parser.getCurrentLineNumber());
        parser.close();
    }

    @Test
    public void testGetHeaderMapNull() throws IOException {
        CSVParser parser = CSVParser.parse("a,b,c", CSVFormat.DEFAULT);
        assertNull(parser.getHeaderMap());
        parser.close();
    }

    @Test
    public void testGetRecordNumber() throws IOException {
        CSVParser parser = CSVParser.parse("a,b\nc,d", CSVFormat.DEFAULT);
        assertEquals(0L, parser.getRecordNumber());
        parser.nextRecord();
        assertEquals(1L, parser.getRecordNumber());
        parser.close();
    }

    @Test
    public void testEmptyFileParsing() throws IOException {
        CSVParser parser = CSVParser.parse("", CSVFormat.DEFAULT);
        assertNull(parser.nextRecord());
        assertTrue(parser.getRecords().isEmpty());
        parser.close();
    }
}