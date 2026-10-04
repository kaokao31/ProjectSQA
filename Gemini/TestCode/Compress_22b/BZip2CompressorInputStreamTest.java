package org.apache.commons.compress.compressors.bzip2;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BZip2CompressorInputStreamTest {

    @Test(expected = NullPointerException.class)
    public void testConstructorNullStream() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    @Test(expected = IOException.class)
    public void testConstructorInvalidHeader1() throws IOException {
        byte[] data = new byte[] { 'A', 'B' };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        new BZip2CompressorInputStream(in);
    }

    @Test(expected = IOException.class)
    public void testConstructorInvalidHeader2() throws IOException {
        byte[] data = new byte[] { 'B', 'Z', 'a', '1' }; // expects 'h'
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        new BZip2CompressorInputStream(in);
    }

    @Test
    public void testReadClosedStream() {
        try {
            byte[] data = new byte[] { 'B', 'Z', 'h', '1' };
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            // This might fail at construction or read, but let's test closing first or reading after EOF/close
            BZip2CompressorInputStream bzip = new BZip2CompressorInputStream(in, true);
            bzip.close();
            int res = bzip.read();
            assertEquals(-1, res);
        } catch (IOException e) {
            // Expected if constructor fails on invalid stream
        }
    }

    @Test
    public void testReadWithNullBuffer() {
        try {
            byte[] data = new byte[] { 'B', 'Z', 'h', '1' };
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            BZip2CompressorInputStream bzip = new BZip2CompressorInputStream(in, true);
            bzip.read(null, 0, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        } catch (IOException e) {
            // Also acceptable if header validation fails first
        }
    }

    @Test
    public void testReadWithInvalidBounds() {
        try {
            byte[] data = new byte[] { 'B', 'Z', 'h', '1' };
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            BZip2CompressorInputStream bzip = new BZip2CompressorInputStream(in, true);
            byte[] buf = new byte[10];
            bzip.read(buf, -1, 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        } catch (IOException e) {
            // Also acceptable if header validation fails first
        }
    }

    @Test
    public void testMatches() {
        byte[] signature = new byte[] { 'B', 'Z', 'h', '5' };
        boolean matches = BZip2CompressorInputStream.matches(signature, 4);
        assertTrue(matches);

        byte[] badSignature = new byte[] { 'B', 'Z', 'x', '5' };
        assertFalse(BZip2CompressorInputStream.matches(badSignature, 4));

        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'B', 'Z' }, 2));
        assertFalse(BZip2CompressorInputStream.matches(null, 4));
    }
}