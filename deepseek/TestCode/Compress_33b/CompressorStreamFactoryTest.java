package org.apache.commons.compress.compressors;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.xz.XzCompressorInputStream;

public class CompressorStreamFactoryTest {

    private CompressorStreamFactory factory;

    @Before
    public void setUp() {
        factory = new CompressorStreamFactory();
    }

    // -----------------------------------------------------------------------
    // createCompressorInputStream – auto detection
    // -----------------------------------------------------------------------

    @Test
    public void testCreateCompressorInputStreamWithGzip() throws Exception {
        byte[] gzipMagic = new byte[] {0x1F, (byte)0x8B, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        InputStream in = new ByteArrayInputStream(gzipMagic);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull("Created stream should not be null", cis);
        assertTrue("Should be GzipCompressorInputStream", cis instanceof GzipCompressorInputStream);
    }

    @Test
    public void testCreateCompressorInputStreamWithBzip2() throws Exception {
        byte[] bzip2Magic = new byte[] {0x42, 0x5A, 0x68, 0x31, 0x41, 0x59, 0x26, 0x53, 0x59};
        InputStream in = new ByteArrayInputStream(bzip2Magic);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull(cis);
        assertTrue("Should be BZip2CompressorInputStream", cis instanceof BZip2CompressorInputStream);
    }

    @Test
    public void testCreateCompressorInputStreamWithDeflate() throws Exception {
        byte[] deflateMagic = new byte[] {0x78, (byte)0x9C, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        InputStream in = new ByteArrayInputStream(deflateMagic);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull(cis);
        assertTrue("Should be DeflateCompressorInputStream", cis instanceof DeflateCompressorInputStream);
    }

    @Test
    public void testCreateCompressorInputStreamWithXz() throws Exception {
        byte[] xzMagic = new byte[] {(byte)0xFD, 0x37, 0x7A, 0x58, 0x5A, 0x00, 0x00, 0x00, 0x00, 0x00};
        InputStream in = new ByteArrayInputStream(xzMagic);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull(cis);
        assertTrue("Should be XzCompressorInputStream", cis instanceof XzCompressorInputStream);
    }

    @Test
    public void testCreateCompressorInputStreamWithLzma() throws Exception {
        byte[] lzmaMagic = new byte[] {0x5D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        InputStream in = new ByteArrayInputStream(lzmaMagic);
        CompressorInputStream cis = factory.createCompressorInputStream(in);
        assertNotNull(cis);
        assertTrue("Should be LZMACompressorInputStream", cis instanceof LZMACompressorInputStream);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamWithUnknown() throws Exception {
        byte[] unknownMagic = new byte[] {0x00, 0x01, 0x02, 0x03};
        InputStream in = new ByteArrayInputStream(unknownMagic);
        factory.createCompressorInputStream(in);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamWithEmptyInput() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createCompressorInputStream(in);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamWithNullInput() throws Exception {
        factory.createCompressorInputStream((InputStream) null);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamWithShortInput() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[] {0x1F});
        factory.createCompressorInputStream(in);
    }

    // -----------------------------------------------------------------------
    // createCompressorInputStream – explicit format
    // -----------------------------------------------------------------------

    @Test
    public void testCreateCompressorInputStreamExplicitGzip() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CompressorInputStream cis = factory.createCompressorInputStream("gz", in);
        assertNotNull(cis);
        assertTrue(cis instanceof GzipCompressorInputStream);
    }

    @Test
    public void testCreateCompressorInputStreamExplicitBzip2() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CompressorInputStream cis = factory.createCompressorInputStream("bzip2", in);
        assertNotNull(cis);
        assertTrue(cis instanceof BZip2CompressorInputStream);
    }

    @Test
    public void testCreateCompressorInputStreamExplicitDeflate() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CompressorInputStream cis = factory.createCompressorInputStream("deflate", in);
        assertNotNull(cis);
        assertTrue(cis instanceof DeflateCompressorInputStream);
    }

    @Test
    public void testCreateCompressorInputStreamExplicitXz() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CompressorInputStream cis = factory.createCompressorInputStream("xz", in);
        assertNotNull(cis);
        assertTrue(cis instanceof XzCompressorInputStream);
    }

    @Test
    public void testCreateCompressorInputStreamExplicitLzma() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        CompressorInputStream cis = factory.createCompressorInputStream("lzma", in);
        assertNotNull(cis);
        assertTrue(cis instanceof LZMACompressorInputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamExplicitNullFormat() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createCompressorInputStream((String) null, in);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorInputStreamExplicitNullStream() throws Exception {
        factory.createCompressorInputStream("gz", (InputStream) null);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorInputStreamExplicitUnknownFormat() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createCompressorInputStream("unknown", in);
    }

    // -----------------------------------------------------------------------
    // createCompressorOutputStream
    // -----------------------------------------------------------------------

    @Test
    public void testCreateCompressorOutputStreamGzip() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream cos = factory.createCompressorOutputStream(out, "gz");
        assertNotNull(cos);
    }

    @Test
    public void testCreateCompressorOutputStreamBzip2() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream cos = factory.createCompressorOutputStream(out, "bzip2");
        assertNotNull(cos);
    }

    @Test
    public void testCreateCompressorOutputStreamDeflate() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream cos = factory.createCompressorOutputStream(out, "deflate");
        assertNotNull(cos);
    }

    @Test
    public void testCreateCompressorOutputStreamXz() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream cos = factory.createCompressorOutputStream(out, "xz");
        assertNotNull(cos);
    }

    @Test
    public void testCreateCompressorOutputStreamLzma() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        CompressorOutputStream cos = factory.createCompressorOutputStream(out, "lzma");
        assertNotNull(cos);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStreamNullOutputStream() throws Exception {
        factory.createCompressorOutputStream(null, "gz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCompressorOutputStreamNullFormat() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        factory.createCompressorOutputStream(out, null);
    }

    @Test(expected = CompressorException.class)
    public void testCreateCompressorOutputStreamUnknownFormat() throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        factory.createCompressorOutputStream(out, "unknown");
    }

    // -----------------------------------------------------------------------
    // getCompressorNames
    // -----------------------------------------------------------------------

    @Test
    public void testGetCompressorNames() {
        Set<String> names = factory.getCompressorNames();
        assertNotNull(names);
        assertTrue("Should contain gz", names.contains("gz"));
        assertTrue("Should contain bzip2", names.contains("bzip2"));
        assertTrue("Should contain deflate", names.contains("deflate"));
        assertTrue("Should contain xz", names.contains("xz"));
        assertTrue("Should contain lzma", names.contains("lzma"));
    }

    // -----------------------------------------------------------------------
    // Reuse of factory instance
    // -----------------------------------------------------------------------

    @Test
    public void testFactoryReuse() throws Exception {
        byte[] gzipMagic = new byte[] {0x1F, (byte)0x8B, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        InputStream in1 = new ByteArrayInputStream(gzipMagic);
        CompressorInputStream cis1 = factory.createCompressorInputStream(in1);
        assertNotNull(cis1);

        byte[] bzip2Magic = new byte[] {0x42, 0x5A, 0x68, 0x31, 0x41, 0x59, 0x26, 0x53, 0x59};
        InputStream in2 = new ByteArrayInputStream(bzip2Magic);
        CompressorInputStream cis2 = factory.createCompressorInputStream(in2);
        assertNotNull(cis2);
        assertTrue("Second stream should be BZip2CompressorInputStream", cis2 instanceof BZip2CompressorInputStream);
    }

    // -----------------------------------------------------------------------
    // Edge case: empty input exception message
    // -----------------------------------------------------------------------

    @Test
    public void testCreateCompressorInputStreamEmptyInputMessage() {
        try {
            factory.createCompressorInputStream(new ByteArrayInputStream(new byte[0]));
            fail("Expected CompressorException");
        } catch (CompressorException e) {
            assertNotNull("Exception message should not be null", e.getMessage());
            // The message should indicate that the format could not be detected
            assertTrue("Message should indicate detection failure",
                       e.getMessage().toLowerCase().contains("could not detect") ||
                       e.getMessage().toLowerCase().contains("unknown") ||
                       e.getMessage().toLowerCase().contains("no format"));
        }
    }
}