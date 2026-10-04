package org.apache.commons.csv;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import static org.junit.Assert.*;

public class CSVParserTest {

    private CSVFormat format;
    private CSVParser parser;

    @Before
    public void setUp() {
        format = CSVFormat.DEFAULT.withHeader();
    }

    @After
    public void tearDown() throws IOException {
        if (parser != null) {
            parser.close();
        }
    }

    // ---------- Basic parsing tests ----------

    @Test(expected = NullPointerException.class)
    public void testNullReader() throws IOException {
        CSVParser.parse(null, format);
    }

    @Test(expected = NullPointerException.class)
    public void testNullFormat() throws IOException {
        CSVParser.parse(new StringReader("a,b,c"), null);
    }

    @Test
    public void testEmptyInput() throws IOException {
        parser = CSVParser.parse(new StringReader(""), format);
        assertFalse("No records expected for empty input", parser.iterator().hasNext());
    }

    @Test
    public void testSingleRecord() throws IOException {
        parser = CSVParser.parse(new StringReader("a,b,c"), format);
        Iterator<CSVRecord> it = parser.iterator();
        assertTrue("Should have one record", it.hasNext());
        CSVRecord record = it.next();
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("c", record.get(2));
        assertFalse("Should not have more records", it.hasNext());
    }

    @Test
    public void testMultipleRecords() throws IOException {
        String csv = "1,2,3\n4,5,6\n7,8,9";
        parser = CSVParser.parse(new StringReader(csv), format);
        Iterator<CSVRecord> it = parser.iterator();
        int count = 0;
        while (it.hasNext()) {
            CSVRecord record = it.next();
            count++;
            assertEquals(String.valueOf(count), 3, record.size());
        }
        assertEquals("Expected 3 rows", 3, count);
    }

    @Test
    public void testEmptyFields() throws IOException {
        String csv = "a,,c";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals("a", record.get(0));
        assertEquals("", record.get(1));
        assertEquals("c", record.get(2));
    }

    @Test
    public void testTrailingEmptyField() throws IOException {
        String csv = "a,b,";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals(3, record.size());
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("", record.get(2));
    }

    @Test
    public void testLeadingEmptyField() throws IOException {
        String csv = ",b,c";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals(3, record.size());
        assertEquals("", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("c", record.get(2));
    }

    // ---------- Quoted fields ----------

    @Test
    public void testQuotedField() throws IOException {
        String csv = "\"hello, world\",a";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals("hello, world", record.get(0));
        assertEquals("a", record.get(1));
    }

    @Test
    public void testQuotedFieldWithNewline() throws IOException {
        String csv = "\"hello\nworld\",a";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals("hello\nworld", record.get(0));
        assertEquals("a", record.get(1));
    }

    @Test
    public void testEscapedQuotesInsideQuoted() throws IOException {
        String csv = "\"\"\"hello\"\"\",world";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals("\"hello\"", record.get(0));
        assertEquals("world", record.get(1));
    }

    // ---------- Edge cases for Defects4J bug 16 ----------
    // Bug: Input ending with an empty field (e.g., "a,") causes issues in some versions
    @Test
    public void testLastFieldEmptyUnquoted() throws IOException {
        String csv = "a,";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals(2, record.size());
        assertEquals("a", record.get(0));
        assertEquals("", record.get(1));
    }

    @Test
    public void testMultipleEmptyLastFields() throws IOException {
        String csv = "a,,";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals(3, record.size());
        assertEquals("a", record.get(0));
        assertEquals("", record.get(1));
        assertEquals("", record.get(2));
    }

    @Test
    public void testOnlyEmptyFields() throws IOException {
        String csv = ",";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals(2, record.size());
        assertEquals("", record.get(0));
        assertEquals("", record.get(1));
    }

    @Test
    public void testEndingWithQuotedEmptyField() throws IOException {
        String csv = "a,\"\"";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord record = parser.iterator().next();
        assertEquals(2, record.size());
        assertEquals("a", record.get(0));
        assertEquals("", record.get(1));
    }

    // ---------- Header handling ----------

    @Test
    public void testWithHeader() throws IOException {
        String csv = "col1,col2\nval1,val2";
        parser = CSVParser.parse(new StringReader(csv), CSVFormat.DEFAULT.withHeader());
        CSVRecord record = parser.iterator().next();
        assertEquals("val1", record.get("col1"));
        assertEquals("val2", record.get("col2"));
    }

    @Test
    public void testHeaderMapping() throws IOException {
        String csv = "a,b,c\n1,2,3";
        parser = CSVParser.parse(new StringReader(csv), CSVFormat.DEFAULT.withHeader());
        CSVRecord record = parser.iterator().next();
        assertEquals("1", record.get("a"));
        assertEquals("2", record.get("b"));
        assertEquals("3", record.get("c"));
    }

    // ---------- Custom delimiter ----------

    @Test
    public void testTabDelimited() throws IOException {
        String csv = "a\tb\tc";
        parser = CSVParser.parse(new StringReader(csv), CSVFormat.DEFAULT.withDelimiter('\t'));
        CSVRecord record = parser.iterator().next();
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("c", record.get(2));
    }

    // ---------- getRecords() method ----------

    @Test
    public void testGetRecords() throws IOException {
        String csv = "a,b\nc,d\ne,f";
        parser = CSVParser.parse(new StringReader(csv), format);
        java.util.List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        assertEquals("a", records.get(0).get(0));
        assertEquals("f", records.get(2).get(1));
    }

    @Test
    public void testGetRecordsEmpty() throws IOException {
        parser = CSVParser.parse(new StringReader(""), format);
        java.util.List<CSVRecord> records = parser.getRecords();
        assertTrue(records.isEmpty());
    }

    // ---------- Exception handling ----------

    @Test(expected = IOException.class)
    public void testIOExceptionOnClosedStream() throws IOException {
        Reader reader = new StringReader("a,b,c");
        reader.close();
        parser = CSVParser.parse(reader, format);
        parser.iterator().next(); // should throw IOException
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormat() throws IOException {
        CSVFormat badFormat = CSVFormat.DEFAULT.withQuoteChar(null).withEscapeChar(null);
        parser = CSVParser.parse(new StringReader("a,b"), badFormat);
    }

    // ---------- Iterator remove (unsupported) ----------

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws IOException {
        parser = CSVParser.parse(new StringReader("a,b"), format);
        Iterator<CSVRecord> it = parser.iterator();
        it.next();
        it.remove();
    }

    // ---------- Large input stress test ----------

    @Test
    public void testManyRecords() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append(i).append(",").append(i * 2).append("\n");
        }
        parser = CSVParser.parse(new StringReader(sb.toString()), format);
        int count = 0;
        for (CSVRecord record : parser) {
            assertEquals(String.valueOf(count), record.get(0));
            assertEquals(String.valueOf(count * 2), record.get(1));
            count++;
        }
        assertEquals(1000, count);
    }

    // ---------- Record number tracking ----------

    @Test
    public void testRecordNumbers() throws IOException {
        String csv = "a\nb\nc";
        parser = CSVParser.parse(new StringReader(csv), format);
        CSVRecord r1 = parser.iterator().next();
        assertEquals(1, r1.getRecordNumber());
        CSVRecord r2 = parser.iterator().next();
        assertEquals(2, r2.getRecordNumber());
        CSVRecord r3 = parser.iterator().next();
        assertEquals(3, r3.getRecordNumber());
    }
}