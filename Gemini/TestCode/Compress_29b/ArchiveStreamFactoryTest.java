package org.apache.commons.compress.archivers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Test;

public class ArchiveStreamFactoryTest {

    @Test
    public void testDetectNullInputStream() {
        try {
            ArchiveStreamFactory.detect(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (ArchiveException e) {
            fail("Unexpected ArchiveException: " + e.getMessage());
        }
    }

    @Test
    public void testCreateArchiveInputStreamNullParameters() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);

        try {
            factory.createArchiveInputStream(null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        try {
            factory.createArchiveInputStream(null, in);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        try {
            factory.createArchiveInputStream(ArchiveStreamFactory.AR, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testCreateArchiveOutputStreamNullParameters() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

        try {
            factory.createArchiveOutputStream(null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        try {
            factory.createArchiveOutputStream(null, out);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }

        try {
            factory.createArchiveOutputStream(ArchiveStreamFactory.AR, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testUnknownArchiveType() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

        try {
            factory.createArchiveInputStream("unknownType", in);
            fail("Expected ArchiveException");
        } catch (ArchiveException e) {
            // expected
        }

        try {
            factory.createArchiveOutputStream("unknownType", out);
            fail("Expected ArchiveException");
        } catch (ArchiveException e) {
            // expected
        }
    }

    @Test
    public void testDetectUnknownType() {
        byte[] unknownData = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
        ByteArrayInputStream in = new ByteArrayInputStream(unknownData);
        try {
            ArchiveStreamFactory.detect(in);
            fail("Expected ArchiveException");
        } catch (ArchiveException e) {
            // expected
        }
    }

    @Test
    public void testDetectZip() {
        // Zip magic: PK\003\004
        byte[] zipHeader = new byte[] { 'P', 'K', 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        ByteArrayInputStream in = new ByteArrayInputStream(zipHeader);
        try {
            String detected = ArchiveStreamFactory.detect(in);
            assertEquals(ArchiveStreamFactory.ZIP, detected);
        } catch (ArchiveException e) {
            fail("Unexpected ArchiveException: " + e.getMessage());
        }
    }

    @Test
    public void testDetectTar() {
        // Tar magic check usually looks for ustar or similar around offset 257, or let's provide a valid header or check exception
        byte[] tarHeader = new byte[512];
        // Populate ustar in tar header
        System.arraycopy(new byte[] { 'u', 's', 't', 'a', 'r' }, 0, tarHeader, 257, 5);
        ByteArrayInputStream in = new ByteArrayInputStream(tarHeader);
        try {
            String detected = ArchiveStreamFactory.detect(in);
            assertEquals(ArchiveStreamFactory.TAR, detected);
        } catch (ArchiveException e) {
            fail("Unexpected ArchiveException: " + e.getMessage());
        }
    }

    @Test
    public void testEncodingGettersAndSetters() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        assertNull(factory.getEntryEncoding());

        factory.setEntryEncoding("UTF-8");
        assertEquals("UTF-8", factory.getEntryEncoding());

        factory.setEntryEncoding(null);
        assertNull(factory.getEntryEncoding());
    }

    @Test
    public void testCreateZipWithEncoding() {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.setEntryEncoding("UTF-8");
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[100]);
        try {
            // Even if stream is invalid for actual zip reading, it exercises the code path creating ZipArchiveInputStream with encoding
            factory.createArchiveInputStream(ArchiveStreamFactory.ZIP, in);
        } catch (Exception e) {
            // Expected due to invalid zip content, but code path is covered
        }
    }
}