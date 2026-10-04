package org.apache.commons.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class ExtendedBufferedReaderTest {

    private ExtendedBufferedReader createBufferedReader(final String input) {
        return new ExtendedBufferedReader(new StringReader(input));
    }

    @Test
    public void testEmptyStream() throws IOException {
        try (final ExtendedBufferedReader br = createBufferedReader("")) {
            assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
            assertNull(br.readLine());
            assertEquals(0, br.getCurrentLineNumber());
            assertEquals(0, br.getPosition());
        }
    }

    @Test
    public void testReadCharByChar() throws IOException {
        final String data = "1\r\n2\r3\n4";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
            assertEquals(0, br.getCurrentLineNumber());
            assertEquals(0, br.getPosition());

            // '1'
            assertEquals('1', br.lookAhead());
            assertEquals('1', br.read());
            assertEquals('1', br.readAgain());
            assertEquals(0, br.getCurrentLineNumber());
            assertEquals(1, br.getPosition());

            // '\r'
            assertEquals('\r', br.lookAhead());
            assertEquals('\r', br.read());
            assertEquals('\r', br.readAgain());
            assertEquals(1, br.getCurrentLineNumber());
            assertEquals(2, br.getPosition());

            // '\n' (part of \r\n, line number should not increment again)
            assertEquals('\n', br.lookAhead());
            assertEquals('\n', br.read());
            assertEquals('\n', br.readAgain());
            assertEquals(1, br.getCurrentLineNumber());
            assertEquals(3, br.getPosition());

            // '2'
            assertEquals('2', br.read());
            assertEquals(1, br.getCurrentLineNumber());
            assertEquals(4, br.getPosition());

            // '\r'
            assertEquals('\r', br.read());
            assertEquals(2, br.getCurrentLineNumber());
            assertEquals(5, br.getPosition());

            // '3'
            assertEquals('3', br.read());
            assertEquals(2, br.getCurrentLineNumber());
            assertEquals(6, br.getPosition());

            // '\n'
            assertEquals('\n', br.read());
            assertEquals(3, br.getCurrentLineNumber());
            assertEquals(7, br.getPosition());

            // '4'
            assertEquals('4', br.read());
            assertEquals(3, br.getCurrentLineNumber());
            assertEquals(8, br.getPosition());

            // EOF
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
            assertEquals(3, br.getCurrentLineNumber());
            assertEquals(8, br.getPosition());
        }
    }

    @Test
    public void testReadCharArray() throws IOException {
        final String data = "1234\r\n5678\r\n";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            final char[] buf = new char[10];

            assertEquals(0, br.read(buf, 0, 0));
            assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
            assertEquals(0, br.getPosition());

            final int read1 = br.read(buf, 0, 6);
            assertEquals(6, read1);
            assertEquals("1234\r\n", new String(buf, 0, 6));
            assertEquals('\n', br.readAgain());
            assertEquals(1, br.getCurrentLineNumber());
            assertEquals(6, br.getPosition());

            final int read2 = br.read(buf, 0, 6);
            assertEquals(6, read2);
            assertEquals("5678\r\n", new String(buf, 0, 6));
            assertEquals('\n', br.readAgain());
            assertEquals(2, br.getCurrentLineNumber());
            assertEquals(12, br.getPosition());

            final int read3 = br.read(buf, 0, 6);
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, read3);
            assertEquals(12, br.getPosition());
        }
    }

    @Test
    public void testReadCharArrayCRLFBoundary() throws IOException {
        final String data = "1234\r\n5678";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            final char[] buf = new char[5];

            // Read up to and including '\r'
            final int read1 = br.read(buf, 0, 5);
            assertEquals(5, read1);
            assertEquals("1234\r", new String(buf, 0, 5));
            assertEquals('\r', br.readAgain());
            assertEquals(1, br.getCurrentLineNumber());
            assertEquals(5, br.getPosition());

            // Read next chunk starting with '\n'
            final int read2 = br.read(buf, 0, 5);
            assertEquals(5, read2);
            assertEquals("\n5678", new String(buf, 0, 5));
            assertEquals('8', br.readAgain());
            // Line number should still be 1 (since \r\n is a single newline sequence)
            assertEquals(1, br.getCurrentLineNumber());
            assertEquals(10, br.getPosition());
        }
    }

    @Test
    public void testReadCharArrayWithMultipleCRLF() throws IOException {
        final String data = "a\rb\nc\r\nd";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            final char[] buf = new char[10];
            final int read = br.read(buf, 0, 10);
            assertEquals(8, read);
            assertEquals("a\rb\nc\r\nd", new String(buf, 0, 8));
            assertEquals('d', br.readAgain());
            assertEquals(3, br.getCurrentLineNumber());
            assertEquals(8, br.getPosition());
        }
    }

    @Test
    public void testReadLine() throws IOException {
        final String data = "line1\nline2\r\nline3\rline4";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            assertEquals("line1", br.readLine());
            assertEquals(1, br.getCurrentLineNumber());
            assertEquals('\n', br.readAgain());

            assertEquals("line2", br.readLine());
            assertEquals(2, br.getCurrentLineNumber());
            assertEquals('\n', br.readAgain());

            assertEquals("line3", br.readLine());
            assertEquals(3, br.getCurrentLineNumber());
            assertEquals('\r', br.readAgain());

            assertEquals("line4", br.readLine());
            assertEquals(4, br.getCurrentLineNumber());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());

            assertNull(br.readLine());
            assertEquals(4, br.getCurrentLineNumber());
        }
    }

    @Test
    public void testReadLineTrailingNewline() throws IOException {
        final String data = "line1\n";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            assertEquals("line1", br.readLine());
            assertEquals(1, br.getCurrentLineNumber());
            assertEquals('\n', br.readAgain());
            assertNull(br.readLine());
            assertEquals(1, br.getCurrentLineNumber());
        }
    }

    @Test
    public void testLookAhead() throws IOException {
        final String data = "abcdef";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            assertEquals('a', br.lookAhead());
            assertEquals('a', br.lookAhead());
            assertEquals(0, br.getPosition());
            assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());

            assertEquals('a', br.read());
            assertEquals('b', br.lookAhead());
            assertEquals(1, br.getPosition());
            assertEquals('a', br.readAgain());

            assertEquals('b', br.read());
            assertEquals('c', br.lookAhead());
            assertEquals(2, br.getPosition());
            assertEquals('b', br.readAgain());
        }
    }

    @Test
    public void testLookAheadAtEOF() throws IOException {
        try (final ExtendedBufferedReader br = createBufferedReader("a")) {
            assertEquals('a', br.read());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.lookAhead());
        }
    }

    @Test
    public void testIsClosed() throws IOException {
        final ExtendedBufferedReader br = createBufferedReader("test");
        assertFalse(br.isClosed());
        assertEquals('t', br.read());
        assertFalse(br.isClosed());
        br.close();
        assertTrue(br.isClosed());
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        final ExtendedBufferedReader br = createBufferedReader("test");
        br.close();
        br.read();
    }

    @Test(expected = IOException.class)
    public void testLookAheadAfterClose() throws IOException {
        final ExtendedBufferedReader br = createBufferedReader("test");
        br.close();
        br.lookAhead();
    }

    @Test(expected = IOException.class)
    public void testReadLineAfterClose() throws IOException {
        final ExtendedBufferedReader br = createBufferedReader("test");
        br.close();
        br.readLine();
    }

    @Test
    public void testConsecutiveNewlines() throws IOException {
        final String data = "\n\r\n\r\r\n";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            // \n -> line 1
            assertEquals('\n', br.read());
            assertEquals(1, br.getCurrentLineNumber());

            // \r\n -> line 2
            assertEquals('\r', br.read());
            assertEquals(2, br.getCurrentLineNumber());
            assertEquals('\n', br.read());
            assertEquals(2, br.getCurrentLineNumber());

            // \r -> line 3
            assertEquals('\r', br.read());
            assertEquals(3, br.getCurrentLineNumber());

            // \r\n -> line 4
            assertEquals('\r', br.read());
            assertEquals(4, br.getCurrentLineNumber());
            assertEquals('\n', br.read());
            assertEquals(4, br.getCurrentLineNumber());

            assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.read());
            assertEquals(4, br.getCurrentLineNumber());
        }
    }

    @Test
    public void testMixedReadsAndLineNumberTracking() throws IOException {
        final String data = "foo\nbar\r\nbaz\rqux";
        try (final ExtendedBufferedReader br = createBufferedReader(data)) {
            assertEquals("foo", br.readLine());
            assertEquals(1, br.getCurrentLineNumber());

            final char[] buf = new char[4];
            int read = br.read(buf, 0, 4);
            assertEquals(4, read);
            assertEquals("bar\r", new String(buf, 0, 4));
            assertEquals(2, br.getCurrentLineNumber());

            assertEquals('\n', br.read());
            assertEquals(2, br.getCurrentLineNumber());

            assertEquals('b', br.read());
            assertEquals('a', br.read());
            assertEquals('z', br.read());
            assertEquals('\r', br.read());
            assertEquals(3, br.getCurrentLineNumber());

            assertEquals("qux", br.readLine());
            assertEquals(4, br.getCurrentLineNumber());
        }
    }
}