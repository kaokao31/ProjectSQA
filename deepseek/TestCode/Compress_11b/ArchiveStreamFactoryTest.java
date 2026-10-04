package org.apache.commons.compress.archivers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.ar.ArArchiveOutputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.Before;
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    private ArchiveStreamFactory factory;

    @Before
    public void setUp() {
        factory = new ArchiveStreamFactory();
    }

    // ---------- createArchiveInputStream tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullArg() throws ArchiveException {
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullStream() throws ArchiveException {
        factory.createArchiveInputStream("zip", null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamEmptyStream() throws ArchiveException, IOException {
        ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream(empty);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamUnknownType() throws ArchiveException, IOException {
        ByteArrayInputStream unknown = new ByteArrayInputStream(new byte[]{0, 1, 2, 3});
        factory.createArchiveInputStream(unknown);
    }

    @Test
    public void testCreateArchiveInputStreamZip() throws ArchiveException, IOException {
        // Minimal ZIP local file header signature: PK\003\004
        byte[] zipBytes = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteArrayInputStream in = new ByteArrayInputStream(zipBytes);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue(ais instanceof ZipArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamJar() throws ArchiveException, IOException {
        // JAR is same as ZIP but with META-INF/MANIFEST.MF; detection uses ZIP signature
        byte[] jarBytes = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteArrayInputStream in = new ByteArrayInputStream(jarBytes);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue(ais instanceof JarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamTar() throws ArchiveException, IOException {
        // TAR header starts with filename (100 bytes) then fields; first byte can be anything but typically not null
        // Use a minimal valid tar header: 512 zero bytes except magic "ustar" at offset 257
        byte[] tarBytes = new byte[512];
        // Set magic "ustar\0" at offset 257
        System.arraycopy("ustar\0".getBytes(), 0, tarBytes, 257, 6);
        ByteArrayInputStream in = new ByteArrayInputStream(tarBytes);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue(ais instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamCpio() throws ArchiveException, IOException {
        // CPIO binary format magic: 0xC771 (little endian) or 0x71C7 (big endian)
        byte[] cpioBytes = new byte[]{0x71, (byte)0xC7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteArrayInputStream in = new ByteArrayInputStream(cpioBytes);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue(ais instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamAr() throws ArchiveException, IOException {
        // AR archive starts with "!<arch>\n"
        byte[] arBytes = "!<arch>\n".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(arBytes);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue(ais instanceof ArArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamDump() throws ArchiveException, IOException {
        // Dump archive starts with magic 0x01 0x15 (old) or 0x01 0x16 (new)
        byte[] dumpBytes = new byte[]{0x01, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteArrayInputStream in = new ByteArrayInputStream(dumpBytes);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        assertTrue(ais instanceof DumpArchiveInputStream);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamWithNameNull() throws ArchiveException {
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamWithNameEmpty() throws ArchiveException {
        factory.createArchiveInputStream("", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamWithNameUnknown() throws ArchiveException {
        factory.createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]));
    }

    @Test
    public void testCreateArchiveInputStreamWithNameZip() throws ArchiveException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream("zip", in);
        assertNotNull(ais);
        assertTrue(ais instanceof ZipArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameJar() throws ArchiveException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream("jar", in);
        assertNotNull(ais);
        assertTrue(ais instanceof JarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameTar() throws ArchiveException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream("tar", in);
        assertNotNull(ais);
        assertTrue(ais instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameCpio() throws ArchiveException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream("cpio", in);
        assertNotNull(ais);
        assertTrue(ais instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameAr() throws ArchiveException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream("ar", in);
        assertNotNull(ais);
        assertTrue(ais instanceof ArArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameDump() throws ArchiveException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ArchiveInputStream ais = factory.createArchiveInputStream("dump", in);
        assertNotNull(ais);
        assertTrue(ais instanceof DumpArchiveInputStream);
    }

    // ---------- createArchiveOutputStream tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullArg() throws ArchiveException {
        factory.createArchiveOutputStream((OutputStream) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullStream() throws ArchiveException {
        factory.createArchiveOutputStream("zip", null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamWithNameNull() throws ArchiveException {
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamWithNameEmpty() throws ArchiveException {
        factory.createArchiveOutputStream("", new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamWithNameUnknown() throws ArchiveException {
        factory.createArchiveOutputStream("unknown", new ByteArrayOutputStream());
    }

    @Test
    public void testCreateArchiveOutputStreamZip() throws ArchiveException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream("zip", out);
        assertNotNull(aos);
        assertTrue(aos instanceof ZipArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamJar() throws ArchiveException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream("jar", out);
        assertNotNull(aos);
        assertTrue(aos instanceof JarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamTar() throws ArchiveException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream("tar", out);
        assertNotNull(aos);
        assertTrue(aos instanceof TarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamCpio() throws ArchiveException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream("cpio", out);
        assertNotNull(aos);
        assertTrue(aos instanceof CpioArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamAr() throws ArchiveException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream("ar", out);
        assertNotNull(aos);
        assertTrue(aos instanceof ArArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamDump() throws ArchiveException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream("dump", out);
        assertNotNull(aos);
        assertTrue(aos instanceof DumpArchiveOutputStream);
    }

    // ---------- Edge cases and bug detection ----------

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamWithNameCaseSensitivity() throws ArchiveException {
        // Should be case-insensitive? Typically not; "ZIP" might fail
        factory.createArchiveInputStream("ZIP", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamWithNameCaseSensitivity() throws ArchiveException {
        factory.createArchiveOutputStream("TAR", new ByteArrayOutputStream());
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamWithNameExtraSpaces() throws ArchiveException {
        factory.createArchiveInputStream(" zip ", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamWithNameExtraSpaces() throws ArchiveException {
        factory.createArchiveOutputStream(" ar ", new ByteArrayOutputStream());
    }

    @Test
    public void testCreateArchiveInputStreamWithNameNullStream() throws ArchiveException {
        // Should throw IllegalArgumentException
        try {
            factory.createArchiveInputStream("zip", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCreateArchiveOutputStreamWithNameNullStream() throws ArchiveException {
        try {
            factory.createArchiveOutputStream("zip", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Test detection with stream that has enough bytes but wrong magic
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamDetectionFail() throws ArchiveException, IOException {
        byte[] garbage = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
        ByteArrayInputStream in = new ByteArrayInputStream(garbage);
        factory.createArchiveInputStream(in);
    }

    // Test that detection does not consume more bytes than necessary
    @Test
    public void testCreateArchiveInputStreamDetectionPreservesStream() throws ArchiveException, IOException {
        // Create a valid ZIP stream with extra data after signature
        byte[] zipBytes = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteArrayInputStream in = new ByteArrayInputStream(zipBytes);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull(ais);
        // After detection, the stream should still have the remaining bytes (if any)
        // We can't easily check, but at least no exception
    }

    // Test that createArchiveInputStream with name "7z" is not supported (if not in factory)
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamWithName7z() throws ArchiveException {
        factory.createArchiveInputStream("7z", new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamWithName7z() throws ArchiveException {
        factory.createArchiveOutputStream("7z", new ByteArrayOutputStream());
    }
}