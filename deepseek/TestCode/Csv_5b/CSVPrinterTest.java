package org.apache.commons.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CSVPrinterTest {

    private StringWriter writer;
    private CSVPrinter printer;
    private CSVFormat format;

    @Before
    public void setUp() throws Exception {
        writer = new StringWriter();
        format = CSVFormat.DEFAULT;
        printer = new CSVPrinter(writer, format);
    }

    @After
    public void tearDown() throws Exception {
        if (printer != null) {
            printer.close();
        }
    }

    // ==================== Constructor Tests ====================

    @Test(expected = NullPointerException.class)
    public void testConstructorNullOut() {
        new CSVPrinter(null, format);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFormat() {
        new CSVPrinter(writer, null);
    }

    @Test
    public void testConstructorValid() {
        assertNotNull(printer);
    }

    // ==================== printRecord Tests ====================

    @Test
    public void testPrintRecordEmpty() throws IOException {
        printer.printRecord();
        assertEquals("", writer.toString());
    }

    @Test
    public void testPrintRecordSingleValue() throws IOException {
        printer.printRecord("hello");
        assertEquals("hello\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordMultipleValues() throws IOException {
        printer.printRecord("a", "b", "c");
        assertEquals("a,b,c\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithNull() throws IOException {
        printer.printRecord("a", null, "c");
        assertEquals("a,,c\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithEmptyString() throws IOException {
        printer.printRecord("", "b");
        assertEquals(",b\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithSpecialCharacters() throws IOException {
        printer.printRecord("a,b", "c\"d", "e\nf");
        assertEquals("\"a,b\",\"c\"\"d\",\"e\nf\"\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithNumbers() throws IOException {
        printer.printRecord(1, 2.5, 100L);
        assertEquals("1,2.5,100\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithBoolean() throws IOException {
        printer.printRecord(true, false);
        assertEquals("true,false\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithIterable() throws IOException {
        printer.printRecord((Iterable<?>) Arrays.asList("x", "y"));
        assertEquals("x,y\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithArray() throws IOException {
        printer.printRecord(new Object[] {"p", "q", "r"});
        assertEquals("p,q,r\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordMultipleCalls() throws IOException {
        printer.printRecord("first", "second");
        printer.printRecord("third");
        assertEquals("first,second\r\nthird\r\n", writer.toString());
    }

    // ==================== print Tests ====================

    @Test
    public void testPrintSingleValue() throws IOException {
        printer.print("test");
        assertEquals("test", writer.toString());
    }

    @Test
    public void testPrintNull() throws IOException {
        printer.print(null);
        assertEquals("", writer.toString());
    }

    @Test
    public void testPrintWithQuoting() throws IOException {
        printer.print("a,b");
        assertEquals("\"a,b\"", writer.toString());
    }

    @Test
    public void testPrintThenPrintln() throws IOException {
        printer.print("value");
        printer.println();
        assertEquals("value\r\n", writer.toString());
    }

    // ==================== println Tests ====================

    @Test
    public void testPrintln() throws IOException {
        printer.println();
        assertEquals("\r\n", writer.toString());
    }

    @Test
    public void testPrintlnAfterPrint() throws IOException {
        printer.print("data");
        printer.println();
        assertEquals("data\r\n", writer.toString());
    }

    // ==================== flush Tests ====================

    @Test
    public void testFlush() throws IOException {
        printer.print("test");
        printer.flush();
        assertTrue(writer.toString().contains("test"));
    }

    // ==================== close Tests ====================

    @Test
    public void testClose() throws IOException {
        printer.close();
        assertTrue(printer.isClosed());
    }

    @Test(expected = IOException.class)
    public void testPrintAfterClose() throws IOException {
        printer.close();
        printer.print("should fail");
    }

    @Test(expected = IOException.class)
    public void testPrintRecordAfterClose() throws IOException {
        printer.close();
        printer.printRecord("fail");
    }

    // ==================== Format Variations ====================

    @Test
    public void testCustomDelimiter() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withDelimiter(';');
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a", "b", "c");
        assertEquals("a;b;c\r\n", writer.toString());
        customPrinter.close();
    }

    @Test
    public void testCustomQuoteChar() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withQuoteChar('\'');
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a,b", "c'd");
        assertEquals("'a,b','c''d'\r\n", writer.toString());
        customPrinter.close();
    }

    @Test
    public void testNoQuoting() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withQuoteChar(null);
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a,b", "c");
        assertEquals("a,b,c\r\n", writer.toString());
        customPrinter.close();
    }

    @Test
    public void testEscapeChar() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withEscapeChar('\\');
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a,b", "c\"d");
        assertEquals("\"a,b\",\"c\\\"d\"\r\n", writer.toString());
        customPrinter.close();
    }

    @Test
    public void testRecordSeparator() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withRecordSeparator('\n');
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a", "b");
        assertEquals("a,b\n", writer.toString());
        customPrinter.close();
    }

    @Test
    public void testTrim() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withTrim(true);
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("  a  ", " b ");
        assertEquals("a,b\r\n", writer.toString());
        customPrinter.close();
    }

    @Test
    public void testIgnoreEmptyLines() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a", "");
        assertEquals("a,\r\n", writer.toString());
        customPrinter.close();
    }

    // ==================== Edge Cases ====================

    @Test
    public void testPrintRecordWithVeryLongString() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("x");
        }
        printer.printRecord(sb.toString());
        assertEquals(sb.toString() + "\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithUnicode() throws IOException {
        printer.printRecord("üñîçødé", "日本語");
        assertEquals("üñîçødé,日本語\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithNullInIterable() throws IOException {
        printer.printRecord(Arrays.asList("a", null, "b"));
        assertEquals("a,,b\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithEmptyIterable() throws IOException {
        printer.printRecord(Collections.emptyList());
        assertEquals("\r\n", writer.toString());
    }

    @Test
    public void testPrintRecordWithNestedIterable() throws IOException {
        printer.printRecord((Object) Arrays.asList("a", "b"));
        assertEquals("a,b\r\n", writer.toString());
    }

    // ==================== Exception Handling ====================

    @Test(expected = IOException.class)
    public void testIOExceptionOnWrite() throws IOException {
        Appendable mockAppendable = mock(Appendable.class);
        when(mockAppendable.append(any(CharSequence.class))).thenThrow(new IOException("Mock IO Error"));
        CSVPrinter failingPrinter = new CSVPrinter(mockAppendable, format);
        failingPrinter.print("test");
    }

    @Test(expected = IOException.class)
    public void testIOExceptionOnPrintRecord() throws IOException {
        Appendable mockAppendable = mock(Appendable.class);
        when(mockAppendable.append(any(CharSequence.class))).thenThrow(new IOException("Mock IO Error"));
        CSVPrinter failingPrinter = new CSVPrinter(mockAppendable, format);
        failingPrinter.printRecord("a", "b");
    }

    // ==================== State Tests ====================

    @Test
    public void testIsClosedInitiallyFalse() {
        assertFalse(printer.isClosed());
    }

    @Test
    public void testIsClosedAfterClose() throws IOException {
        printer.close();
        assertTrue(printer.isClosed());
    }

    @Test
    public void testMultipleCloseCalls() throws IOException {
        printer.close();
        printer.close(); // should not throw
        assertTrue(printer.isClosed());
    }

    // ==================== Interaction with CSVFormat ====================

    @Test
    public void testFormatHeader() throws IOException {
        CSVFormat headerFormat = CSVFormat.DEFAULT.withHeader("col1", "col2");
        CSVPrinter headerPrinter = new CSVPrinter(writer, headerFormat);
        headerPrinter.printRecord("val1", "val2");
        assertEquals("col1,col2\r\nval1,val2\r\n", writer.toString());
        headerPrinter.close();
    }

    @Test
    public void testFormatSkipHeader() throws IOException {
        CSVFormat skipFormat = CSVFormat.DEFAULT.withSkipHeaderRecord(true);
        CSVPrinter skipPrinter = new CSVPrinter(writer, skipFormat);
        skipPrinter.printRecord("a", "b");
        assertEquals("a,b\r\n", writer.toString());
        skipPrinter.close();
    }

    // ==================== Bug Regression Tests ====================

    // Defects4J bug: printRecord with null values might cause issues
    @Test
    public void testPrintRecordWithAllNulls() throws IOException {
        printer.printRecord(null, null, null);
        assertEquals(",,\r\n", writer.toString());
    }

    // Defects4J bug: handling of custom delimiter with special characters
    @Test
    public void testCustomDelimiterWithQuoting() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withDelimiter(',');
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a,b", "c");
        assertEquals("\"a,b\",c\r\n", writer.toString());
        customPrinter.close();
    }

    // Defects4J bug: escape char with null quote char
    @Test
    public void testEscapeCharWithNullQuote() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withQuoteChar(null).withEscapeChar('\\');
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a,b", "c\\d");
        assertEquals("a\\,b,c\\\\d\r\n", writer.toString());
        customPrinter.close();
    }

    // Defects4J bug: record separator with carriage return
    @Test
    public void testRecordSeparatorCarriageReturn() throws IOException {
        CSVFormat customFormat = CSVFormat.DEFAULT.withRecordSeparator('\r');
        CSVPrinter customPrinter = new CSVPrinter(writer, customFormat);
        customPrinter.printRecord("a", "b");
        assertEquals("a,b\r", writer.toString());
        customPrinter.close();
    }

    // Defects4J bug: printing after flush with closed writer
    @Test(expected = IOException.class)
    public void testPrintAfterFlushWithClosedWriter() throws IOException {
        writer.close();
        printer.print("test");
    }

    // ==================== Performance / Stress ====================

    @Test(timeout = 1000)
    public void testLargeNumberOfRecords() throws IOException {
        for (int i = 0; i < 1000; i++) {
            printer.printRecord("value" + i);
        }
        String result = writer.toString();
        assertTrue(result.contains("value999\r\n"));
    }

    // ==================== Helper Methods ====================

    // Not needed as we use Mockito for mocking
}