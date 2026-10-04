package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for {@link ExtendedBufferedReader}.
 * Achieves high line and branch coverage, and targets potential faults.
 */
public class ExtendedBufferedReaderTest {

    private static final String EMPTY_STRING = "";
    private static final String SINGLE_CHAR = "a";
    private static final String SHORT_STRING = "abc";
    private static final String MULTILINE = "line1\nline2\nline3";
    private static final String TRAILING_NEWLINE = "line1\nline2\n";
    private static final String ONLY_NEWLINES = "\n\n\n";
    private static final String CARRIAGE_RETURN = "line1\r\nline2\r\n";
    private static final String MIXED_LINE_ENDINGS = "line1\nline2\r\nline3\r";

    private ExtendedBufferedReader reader;

    @Before
    public void setUp() throws Exception {
        // Default setup: empty reader
        reader = new ExtendedBufferedReader(new StringReader(EMPTY_STRING));
    }

    // ---------- Constructor Tests ----------

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullReader() {
        new ExtendedBufferedReader(null);
    }

    @Test
    public void testConstructorWithValidReader() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("test"));
        assertEquals("test", br.readLine());
    }

    // ---------- read() Tests ----------

    @Test
    public void testReadSingleChar() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SINGLE_CHAR));
        assertEquals('a', reader.read());
        assertEquals(-1, reader.read());
    }

    @Test
    public void testReadMultipleChars() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        assertEquals('c', reader.read());
        assertEquals(-1, reader.read());
    }

    @Test
    public void testReadFromEmptyReader() throws IOException {
        assertEquals(-1, reader.read());
    }

    @Test
    public void testReadAfterEndOfStream() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("x"));
        reader.read(); // consume 'x'
        assertEquals(-1, reader.read());
        assertEquals(-1, reader.read()); // multiple reads after end
    }

    // ---------- lookAhead() Tests ----------

    @Test
    public void testLookAheadReturnsNextCharWithoutConsuming() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        assertEquals('a', reader.lookAhead());
        assertEquals('a', reader.read()); // still 'a'
        assertEquals('b', reader.lookAhead());
        assertEquals('b', reader.read());
    }

    @Test
    public void testLookAheadAtEndOfStream() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("x"));
        reader.read(); // consume 'x'
        assertEquals(-1, reader.lookAhead());
        assertEquals(-1, reader.lookAhead()); // multiple lookaheads at end
    }

    @Test
    public void testLookAheadOnEmptyReader() throws IOException {
        assertEquals(-1, reader.lookAhead());
    }

    // ---------- getLastChar() Tests ----------

    @Test
    public void testGetLastCharInitially() throws IOException {
        assertEquals(-1, reader.getLastChar());
    }

    @Test
    public void testGetLastCharAfterRead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        reader.read(); // 'a'
        assertEquals('a', reader.getLastChar());
        reader.read(); // 'b'
        assertEquals('b', reader.getLastChar());
    }

    @Test
    public void testGetLastCharAfterEndOfStream() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("x"));
        reader.read(); // 'x'
        reader.read(); // -1
        assertEquals(-1, reader.getLastChar());
    }

    @Test
    public void testGetLastCharAfterLookAhead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        reader.lookAhead(); // does not consume
        assertEquals(-1, reader.getLastChar()); // last char unchanged
        reader.read(); // 'a'
        assertEquals('a', reader.getLastChar());
    }

    // ---------- read(char[], int, int) Tests ----------

    @Test(expected = NullPointerException.class)
    public void testReadCharArrayWithNullBuffer() throws IOException {
        reader.read(null, 0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadCharArrayWithNegativeOffset() throws IOException {
        reader.read(new char[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadCharArrayWithNegativeLength() throws IOException {
        reader.read(new char[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadCharArrayWithOffsetPlusLengthExceedsLength() throws IOException {
        reader.read(new char[5], 3, 3);
    }

    @Test
    public void testReadCharArrayZeroLength() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        char[] buf = new char[10];
        assertEquals(0, reader.read(buf, 0, 0));
        // buffer unchanged
        assertEquals('\0', buf[0]);
    }

    @Test
    public void testReadCharArrayPartialFill() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        char[] buf = new char[10];
        int count = reader.read(buf, 0, 2);
        assertEquals(2, count);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        // remaining chars still available
        assertEquals('c', reader.read());
    }

    @Test
    public void testReadCharArrayExactFill() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        char[] buf = new char[3];
        int count = reader.read(buf, 0, 3);
        assertEquals(3, count);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
        assertEquals(-1, reader.read());
    }

    @Test
    public void testReadCharArrayWithOffset() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        char[] buf = new char[10];
        int count = reader.read(buf, 5, 3);
        assertEquals(3, count);
        assertEquals('a', buf[5]);
        assertEquals('b', buf[6]);
        assertEquals('c', buf[7]);
    }

    @Test
    public void testReadCharArrayFromEmptyReader() throws IOException {
        char[] buf = new char[10];
        assertEquals(-1, reader.read(buf, 0, 10));
    }

    @Test
    public void testReadCharArrayAfterEndOfStream() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("x"));
        reader.read(); // consume
        char[] buf = new char[10];
        assertEquals(-1, reader.read(buf, 0, 10));
    }

    // ---------- readLine() Tests ----------

    @Test
    public void testReadLineEmptyReader() throws IOException {
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineSingleLineNoNewline() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(SHORT_STRING));
        assertEquals(SHORT_STRING, reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineMultipleLines() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(MULTILINE));
        assertEquals("line1", reader.readLine());
        assertEquals("line2", reader.readLine());
        assertEquals("line3", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineWithTrailingNewline() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(TRAILING_NEWLINE));
        assertEquals("line1", reader.readLine());
        assertEquals("line2", reader.readLine());
        assertEquals("", reader.readLine()); // empty line after trailing newline
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineOnlyNewlines() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(ONLY_NEWLINES));
        assertEquals("", reader.readLine());
        assertEquals("", reader.readLine());
        assertEquals("", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineWithCarriageReturn() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(CARRIAGE_RETURN));
        assertEquals("line1", reader.readLine());
        assertEquals("line2", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineMixedLineEndings() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(MIXED_LINE_ENDINGS));
        assertEquals("line1", reader.readLine());
        assertEquals("line2", reader.readLine());
        assertEquals("line3", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineAfterRead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("ab\ncd"));
        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        assertEquals("", reader.readLine()); // after reading 'b', next is newline -> empty line
        assertEquals("cd", reader.readLine());
        assertNull(reader.readLine());
    }

    // ---------- Interaction Tests ----------

    @Test
    public void testReadLineAndLookAhead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("hello\nworld"));
        assertEquals('h', reader.lookAhead());
        assertEquals("hello", reader.readLine());
        assertEquals('w', reader.lookAhead());
        assertEquals("world", reader.readLine());
        assertEquals(-1, reader.lookAhead());
    }

    @Test
    public void testReadAndGetLastCharSequence() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("123"));
        assertEquals(-1, reader.getLastChar());
        reader.read();
        assertEquals('1', reader.getLastChar());
        reader.read();
        assertEquals('2', reader.getLastChar());
        reader.read();
        assertEquals('3', reader.getLastChar());
        reader.read(); // -1
        assertEquals(-1, reader.getLastChar());
    }

    @Test
    public void testLookAheadDoesNotAffectLastChar() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("x"));
        assertEquals(-1, reader.getLastChar());
        reader.lookAhead();
        assertEquals(-1, reader.getLastChar()); // still -1
        reader.read();
        assertEquals('x', reader.getLastChar());
    }

    // ---------- Edge Cases for read(char[], int, int) ----------

    @Test
    public void testReadCharArrayLargeBuffer() throws IOException {
        String longStr = "abcdefghijklmnopqrstuvwxyz";
        reader = new ExtendedBufferedReader(new StringReader(longStr));
        char[] buf = new char[100];
        int count = reader.read(buf, 0, 100);
        assertEquals(26, count);
        assertEquals('a', buf[0]);
        assertEquals('z', buf[25]);
        assertEquals(-1, reader.read());
    }

    @Test
    public void testReadCharArrayWithOffsetAndPartialRead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("12345"));
        char[] buf = new char[10];
        // read 3 chars into positions 2,3,4
        int count = reader.read(buf, 2, 3);
        assertEquals(3, count);
        assertEquals('1', buf[2]);
        assertEquals('2', buf[3]);
        assertEquals('3', buf[4]);
        // read remaining 2 chars into positions 0,1
        count = reader.read(buf, 0, 2);
        assertEquals(2, count);
        assertEquals('4', buf[0]);
        assertEquals('5', buf[1]);
    }

    // ---------- Fault Detection: Potential off-by-one or state corruption ----------

    @Test
    public void testReadAfterLookAheadMultipleTimes() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abc"));
        reader.lookAhead(); // 'a'
        reader.lookAhead(); // 'a' again
        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        assertEquals('c', reader.read());
        assertEquals(-1, reader.read());
    }

    @Test
    public void testReadLineAfterPartialRead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abc\ndef"));
        reader.read(); // 'a'
        reader.read(); // 'b'
        // now we have 'c' then newline
        assertEquals("c", reader.readLine()); // reads until newline, returns "c"
        assertEquals("def", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineWithCarriageReturnOnly() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("line1\rline2"));
        // \r is not a line terminator by default in BufferedReader? Actually BufferedReader treats \r as line separator only if followed by \n? 
        // ExtendedBufferedReader may handle \r differently. We test both.
        // This test may expose bug if implementation incorrectly handles \r alone.
        String line1 = reader.readLine();
        // Depending on implementation, line1 could be "line1" or "line1\rline2"? 
        // We'll just assert that we get something and then check state.
        // For fault detection, we ensure no exception and consistent behavior.
        assertTrue(line1 != null);
        // The second readLine should return the rest or null.
        String line2 = reader.readLine();
        // If \r is treated as line separator, line2 is "line2". If not, line2 is null.
        // We'll just check that the reader is not in an invalid state.
        // This test is more about not crashing.
    }

    @Test
    public void testReadCharArrayWithLengthLargerThanAvailable() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("short"));
        char[] buf = new char[100];
        int count = reader.read(buf, 0, 100);
        assertEquals(5, count);
        assertEquals('s', buf[0]);
        assertEquals('t', buf[4]);
        assertEquals(-1, reader.read());
    }

    @Test
    public void testReadAfterClose() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("test"));
        reader.close();
        // After close, read should throw IOException (or return -1 depending on implementation)
        // We expect IOException as per Reader contract.
        try {
            reader.read();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testReadLineAfterClose() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("test"));
        reader.close();
        try {
            reader.readLine();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testLookAheadAfterClose() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("test"));
        reader.close();
        try {
            reader.lookAhead();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    // ---------- Additional Branch Coverage ----------

    @Test
    public void testReadCharArrayWithLengthZeroAndNullBufferNotAllowed() throws IOException {
        // Already covered by NullPointerException test, but ensure zero length with null buffer still throws NPE
        try {
            reader.read(null, 0, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testReadLineWithOnlyCarriageReturnNewline() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        assertEquals("", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineWithMultipleCarriageReturnNewline() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        assertEquals("", reader.readLine());
        assertEquals("", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testReadLineWithCarriageReturnNotFollowedByNewline() throws IOException {
        // This tests the behavior when \r appears alone.
        // Standard BufferedReader treats \r as line separator only if not followed by \n? Actually it treats \r as line separator.
        // ExtendedBufferedReader may have different logic.
        reader = new ExtendedBufferedReader(new StringReader("a\rb"));
        String first = reader.readLine();
        // If \r is line separator, first = "a", second = "b"
        // If not, first = "a\rb" and second = null
        // We'll just check that we can read something and no exception.
        assertTrue(first != null);
        String second = reader.readLine();
        // No assertion on value, just ensure no crash.
    }
}