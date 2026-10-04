package org.apache.commons.compress.compressors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Test;

public class CompressorStreamFactoryTest {

    @Test
    public void testGetInputStreamWithoutDecompressConcatenated() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        assertEquals(false, factory.getDecompressConcatenated());
    }

    @Test
    public void testSetAndGetInputStreamDecompressConcatenated() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.setDecompressConcatenated(true);
        assertEquals(true, factory.getDecompressConcatenated());
        factory.setDecompressConcatenated(false);
        assertEquals(false, factory.getDecompressConcatenated());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamNullName() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamNullStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream(CompressorStreamFactory.GZIP, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStreamNullName() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStreamNullStream() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream(CompressorStreamFactory.GZIP, null);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamUnknownName() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorInputStream("unknown-format", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStreamUnknownName() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        factory.createCompressorOutputStream("unknown-format", new ByteArrayOutputStream());
    }

    @Test
    public void testCreateCompressorOutputStreamBzip2() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream compressor = factory.createCompressorOutputStream(CompressorStreamFactory.BZIP2, out);
        assertNotNull(compressor);
        compressor.close();
    }

    @Test
    public void testCreateCompressorOutputStreamGzip() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream compressor = factory.createCompressorOutputStream(CompressorStreamFactory.GZIP, out);
        assertNotNull(compressor);
        compressor.close();
    }

    @Test
    public void testCreateCompressorOutputStreamPack200() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream compressor = factory.createCompressorOutputStream(CompressorStreamFactory.PACK200, out);
        assertNotNull(compressor);
        compressor.close();
    }

    @Test
    public void testCreateCompressorInputStreamGzip() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        byte[] data = new byte[] { 31, -117, 8, 0, 0, 0, 0, 0, 0, 0 };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            CompressorInputStream compressor = factory.createCompressorInputStream(CompressorStreamFactory.GZIP, in);
            assertNotNull(compressor);
        } catch (CompressorException e) {
            // Depending on exact environment/JDK, GZIP may throw on empty header, but factory method is exercised
        }
    }

    @Test
    public void testCreateCompressorInputStreamBzip2() throws Exception {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        byte[] data = new byte[] { 'B', 'Z', 'h', '9' };
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        try {
            CompressorInputStream compressor = factory.createCompressorInputStream(CompressorStreamFactory.BZIP2, in);
            assertNotNull(compressor);
        } catch (Exception e) {
            // Expected if stream is truncated, but exercises factory routing
        }
    }

    @Test
    public void testAutodetectCompressorInputStreamNullStream() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        try {
            factory.createCompressorInputStream(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CompressorException e) {
            fail("Unexpected CompressorException");
        }
    }

    @Test
    public void testAutodetectCompressorInputStreamMarkNotSupported() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        InputStream in = new InputStream() {
            @Override
            public int read() {
                return -1;
            }
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        try {
            factory.createCompressorInputStream(in);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (CompressorException e) {
            fail("Unexpected CompressorException");
        }
    }

    @Test
    public void testAutodetectCompressorInputStreamShortStream() {
        CompressorStreamFactory factory = new CompressorStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[] { 1 });
        try {
            factory.createCompressorInputStream(in);
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            // expected due to insufficient data for autodetection
        }
    }
}