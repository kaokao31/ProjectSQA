package org.apache.commons.csv;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CSVPrinterTest {

    @Test
    public void testPrintNullObject() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.print(null);
            printer.println();
        }
        assertEquals("\"\"", sw.toString().trim());
    }

    @Test
    public void testPrintCustomNullString() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString("NULL");
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.print(null);
        }
        assertEquals("NULL", sw.toString());
    }

    @Test
    public void testPrintStringWithoutQuotes() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.print("value");
        }
        assertEquals("value", sw.toString());
    }

    @Test
    public void testPrintRecordIterable() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.printRecord(Arrays.asList("a", "b", "c"));
        }
        assertEquals("a,b,c\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordObjectArray() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.printRecord("x", "y", "z");
        }
        assertEquals("x,y,z\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordsIterable() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.printRecords(Arrays.asList(
                    Arrays.asList("1", "2"),
                    Arrays.asList("3", "4")
            ));
        }
        assertEquals("1,2\r\n3,4\r\n", sw.toString());
    }

    @Test
    public void testPrintRecordsObjectArray() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.printRecords(new Object[]{"a", "b"}, new Object[]{"c", "d"});
        }
        assertEquals("a,b\r\nc,d\r\n", sw.toString());
    }

    @Test
    public void testPrintComment() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.printComment("This is a comment");
        }
        assertEquals("# This is a comment\r\n", sw.toString());
    }

    @Test
    public void testPrintCommentWithMultipleLines() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.printComment("Line 1\nLine 2\rLine 3\r\nLine 4");
        }
        assertEquals("# Line 1\r\n# Line 2\r\n# Line 3\r\n# Line 4\r\n", sw.toString());
    }

    @Test
    public void testGetOut() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            assertEquals(sw, printer.getOut());
        }
    }

    @Test
    public void testPrintWithNullStringDefault() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withNullString(null);
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.print(null);
        }
        assertEquals("", sw.toString());
    }

    @Test
    public void testPrintln() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.println();
        }
        assertEquals("\r\n", sw.toString());
    }

    @Test
    public void testPrintRange() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.print(new char[]{'a', 'b', 'c'}, 0, 3);
        }
        assertEquals("abc", sw.toString());
    }

    @Test
    public void testPrintReader() throws IOException {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT)) {
            printer.print(new java.io.StringReader("reader data"));
        }
        assertEquals("reader data", sw.toString());
    }

    @Test
    public void testPrintWithSurroundingSpacesIgnored() throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withTrim(true);
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            printer.print("  foo  ");
        }
        assertEquals("foo", sw.toString());
    }
}