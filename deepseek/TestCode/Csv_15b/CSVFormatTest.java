package org.apache.commons.csv;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CSVFormatTest {

    private CSVFormat format;

    @Before
    public void setUp() {
        format = CSVFormat.DEFAULT;
    }

    @Test
    public void testDefaultFormat() {
        assertEquals(',', format.getDelimiter());
        assertEquals('"', format.getQuoteChar());
        assertEquals('\\', format.getEscape());
        assertEquals("\n", format.getRecordSeparator());
        assertEquals("", format.getNullString());
        assertEquals("", format.getNullRecord());
        assertFalse(format.getIgnoreEmptyLines());
        assertFalse(format.getAllowMissingColumnNames());
        assertFalse(format.getTrailingDelimiter());
        assertFalse(format.getTrim());
        assertTrue(format.getHeader().isEmpty());
    }

    @Test
    public void testWithDelimiter() {
        CSVFormat fmt = format.withDelimiter(';');
        assertEquals(';', fmt.getDelimiter());
        assertEquals(',', format.getDelimiter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithDelimiterInvalid() {
        format.withDelimiter('"');
    }

    @Test
    public void testWithQuoteChar() {
        CSVFormat fmt = format.withQuoteChar('\'');
        assertEquals('\'', fmt.getQuoteChar());
    }

    @Test
    public void testWithEscape() {
        CSVFormat fmt = format.withEscape('/');
        assertEquals('/', fmt.getEscape());
    }

    @Test
    public void testWithRecordSeparator() {
        CSVFormat fmt = format.withRecordSeparator("\r\n");
        assertEquals("\r\n", fmt.getRecordSeparator());
    }

    @Test
    public void testWithNullString() {
        CSVFormat fmt = format.withNullString("NULL");
        assertEquals("NULL", fmt.getNullString());
    }

    @Test
    public void testWithNullRecord() {
        CSVFormat fmt = format.withNullRecord("N/A");
        assertEquals("N/A", fmt.getNullRecord());
    }

    @Test
    public void testNullStringAndNullRecord() {
        CSVFormat fmt = format.withNullString("\\N").withNullRecord("\\R");
        assertEquals("\\N", fmt.getNullString());
        assertEquals("\\R", fmt.getNullRecord());
    }

    @Test
    public void testWithIgnoreEmptyLines() {
        CSVFormat fmt = format.withIgnoreEmptyLines(true);
        assertTrue(fmt.getIgnoreEmptyLines());
        fmt = format.withIgnoreEmptyLines(false);
        assertFalse(fmt.getIgnoreEmptyLines());
    }

    @Test
    public void testWithAllowMissingColumnNames() {
        CSVFormat fmt = format.withAllowMissingColumnNames(true);
        assertTrue(fmt.getAllowMissingColumnNames());
    }

    @Test
    public void testWithTrailingDelimiter() {
        CSVFormat fmt = format.withTrailingDelimiter(true);
        assertTrue(fmt.getTrailingDelimiter());
    }

    @Test
    public void testWithTrim() {
        CSVFormat fmt = format.withTrim(true);
        assertTrue(fmt.getTrim());
    }

    @Test
    public void testWithHeader() {
        CSVFormat fmt = format.withHeader("a", "b", "c");
        assertArrayEquals(new String[]{"a", "b", "c"}, fmt.getHeader().toArray());
    }

    @Test(expected = NullPointerException.class)
    public void testWithHeaderNull() {
        format.withHeader((String[]) null);
    }

    @Test
    public void testWithHeaderEmpty() {
        CSVFormat fmt = format.withHeader();
        assertTrue(fmt.getHeader().isEmpty());
    }

    @Test
    public void testEquals() {
        CSVFormat fmt1 = format.withDelimiter(';');
        CSVFormat fmt2 = format.withDelimiter(';');
        assertEquals(fmt1, fmt2);
        assertEquals(fmt1.hashCode(), fmt2.hashCode());
    }

    @Test
    public void testNotEquals() {
        CSVFormat fmt1 = format.withDelimiter(';');
        CSVFormat fmt2 = format.withDelimiter(',');
        assertNotEquals(fmt1, fmt2);
    }

    @Test
    public void testToString() {
        assertNotNull(format.toString());
    }

    @Test
    public void testImmutability() {
        CSVFormat original = format;
        CSVFormat modified = format.withDelimiter(';');
        assertNotSame(original, modified);
        assertEquals(',', original.getDelimiter());
        assertEquals(';', modified.getDelimiter());
    }

    @Test
    public void testDefaultNullStringNotNull() {
        assertNotNull(format.getNullString());
        assertNotNull(format.getNullRecord());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithNullStringNull() {
        format.withNullString(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithNullRecordNull() {
        format.withNullRecord(null);
    }

    @Test
    public void testWithDelimiterTab() {
        CSVFormat fmt = format.withDelimiter('\t');
        assertEquals('\t', fmt.getDelimiter());
    }

    @Test
    public void testWithQuoteCharNone() {
        CSVFormat fmt = format.withQuoteChar(CSVFormat.NON_QUOTE_CHAR);
        assertEquals(CSVFormat.NON_QUOTE_CHAR, fmt.getQuoteChar());
    }

    @Test
    public void testChaining() {
        CSVFormat fmt = format
                .withDelimiter('|')
                .withQuoteChar('~')
                .withEscape('^')
                .withRecordSeparator("\n")
                .withNullString("NULL")
                .withNullRecord("NR");
        assertEquals('|', fmt.getDelimiter());
        assertEquals('~', fmt.getQuoteChar());
        assertEquals('^', fmt.getEscape());
        assertEquals("\n", fmt.getRecordSeparator());
        assertEquals("NULL", fmt.getNullString());
        assertEquals("NR", fmt.getNullRecord());
    }

    @Test
    public void testClone() {
        CSVFormat clone = format.clone();
        assertEquals(format, clone);
        assertNotSame(format, clone);
    }
}