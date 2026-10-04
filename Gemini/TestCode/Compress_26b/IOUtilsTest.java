package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtilsTest {

    @Test
    public void testCopyRangeZeroOrNegative() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Copy 0 bytes
        long copied = IOUtils.copy(in, out, 0);
        assertEquals(0L, copied);
        assertEquals(0, out.size());

        // Copy negative bytes (should be treated as 0 or EOF depending on implementation, but let's test safely)
        copied = IOUtils.copy(in, out, -1);
        assertEquals(0L, copied);
    }

    @Test
    public void testCopyRangeValid() throws IOException {
        byte[] data = new byte[] { 10, 20, 30, 40, 50 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long copied = IOUtils.copy(in, out, 3);
        assertEquals(3L, copied);
        assertArrayEquals(new byte[] { 10, 20, 30 }, out.toByteArray());
    }

    @Test
    public void testCopyRangeExceedingStream() throws IOException {
        byte[] data = new byte[] { 1, 2 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Request to copy more than available
        long copied = IOUtils.copy(in, out, 5);
        assertEquals(2L, copied);
        assertArrayEquals(new byte[] { 1, 2 }, out.toByteArray());
    }

    @Test
    public void testCopyStandard() throws IOException {
        byte[] data = new byte[] { 5, 4, 3, 2, 1 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        long copied = IOUtils.copy(in, out);
        assertEquals(5L, copied);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    public void testSkipValid() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        long skipped = IOUtils.skip(in, 3);
        assertEquals(3L, skipped);
        assertEquals(4, in.read());
    }

    @Test
    public void testSkipBeyondEOF() throws IOException {
        byte[] data = new byte[] { 1, 2 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        long skipped = IOUtils.skip(in, 5);
        assertEquals(2L, skipped);
    }

    @Test
    public void testSkipZeroOrNegative() throws IOException {
        byte[] data = new byte[] { 1, 2, 3 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        long skipped = IOUtils.skip(in, 0);
        assertEquals(0L, skipped);

        skipped = IOUtils.skip(in, -5);
        assertEquals(0L, skipped);
    }

    @Test
    public void testReadFullyByteArray() throws IOException {
        byte[] data = new byte[] { 10, 20, 30, 40, 50 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte[] b = new byte[3];

        int read = IOUtils.readFully(in, b);
        assertEquals(3, read);
        assertArrayEquals(new byte[] { 10, 20, 30 }, b);
    }

    @Test
    public void testReadFullyByteArrayWithOffsetAndLength() throws IOException {
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte[] b = new byte[5];

        int read = IOUtils.readFully(in, b, 1, 3);
        assertEquals(3, read);
        assertArrayEquals(new byte[] { 0, 1, 2, 3, 0 }, b);
    }

    @Test(expected = IOException.class)
    public void testReadFullyShortReadThrowsException() throws IOException {
        byte[] data = new byte[] { 1, 2 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte[] b = new byte[5];

        IOUtils.readFully(in, b);
    }

    @Test
    public void testToByteArray() throws IOException {
        byte[] data = new byte[] { 9, 8, 7, 6, 5 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        byte[] result = IOUtils.toByteArray(in);
        assertArrayEquals(data, result);
    }
}