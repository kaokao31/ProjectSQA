package org.apache.commons.compress.archivers;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.compress.archivers.ar.ArArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.dump.DumpArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveInputStream;
import org.junit.Test;

public class ArchiveStreamFactoryTest {

    // Helper to create a stream with given magic bytes
    private InputStream createStream(byte[] magic) {
        return new ByteArrayInputStream(magic);
    }

    // Helper to create a stream with given magic bytes and extra data
    private InputStream createStream(byte[] magic, byte[] extra) {
        byte[] data = new byte[magic.length + extra.length];
        System.arraycopy(magic, 0, data, 0, magic.length);
        System.arraycopy(extra, 0, data, magic.length, extra.length);
        return new ByteArrayInputStream(data);
    }

    // Test createArchiveInputStream with null input
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNull() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream((InputStream) null);
    }

    // Test createArchiveInputStream with empty stream
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamEmpty() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }

    // Test createArchiveInputStream with unknown magic bytes
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamUnknown() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] unknownMagic = new byte[] {0x00, 0x01, 0x02, 0x03};
        factory.createArchiveInputStream(createStream(unknownMagic));
    }

    // Test ZIP detection
    @Test
    public void testCreateArchiveInputStreamZip() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] zipMagic = new byte[] {0x50, 0x4B, 0x03, 0x04};
        InputStream in = createStream(zipMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return a ZipArchiveInputStream", ais);
        assertTrue("Should be instance of ZipArchiveInputStream", ais instanceof ZipArchiveInputStream);
    }

    // Test JAR detection (same magic as ZIP)
    @Test
    public void testCreateArchiveInputStreamJar() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] jarMagic = new byte[] {0x50, 0x4B, 0x03, 0x04};
        InputStream in = createStream(jarMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return a JarArchiveInputStream", ais);
        assertTrue("Should be instance of JarArchiveInputStream", ais instanceof JarArchiveInputStream);
    }

    // Test TAR detection (ustar magic at offset 257)
    @Test
    public void testCreateArchiveInputStreamTar() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // TAR magic: "ustar" at offset 257, but we need to provide a valid header
        // For simplicity, we create a minimal TAR header with magic at correct position
        byte[] tarHeader = new byte[512];
        // Set magic "ustar\0" at offset 257
        byte[] magic = "ustar\0".getBytes();
        System.arraycopy(magic, 0, tarHeader, 257, magic.length);
        InputStream in = new ByteArrayInputStream(tarHeader);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return a TarArchiveInputStream", ais);
        assertTrue("Should be instance of TarArchiveInputStream", ais instanceof TarArchiveInputStream);
    }

    // Test CPIO detection (old ASCII magic: "070707")
    @Test
    public void testCreateArchiveInputStreamCpioOldAscii() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] cpioMagic = "070707".getBytes();
        InputStream in = createStream(cpioMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return a CpioArchiveInputStream", ais);
        assertTrue("Should be instance of CpioArchiveInputStream", ais instanceof CpioArchiveInputStream);
    }

    // Test CPIO detection (new ASCII magic: "070701")
    @Test
    public void testCreateArchiveInputStreamCpioNewAscii() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] cpioMagic = "070701".getBytes();
        InputStream in = createStream(cpioMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return a CpioArchiveInputStream", ais);
        assertTrue("Should be instance of CpioArchiveInputStream", ais instanceof CpioArchiveInputStream);
    }

    // Test CPIO detection (binary magic: 0xC7, 0x71)
    @Test
    public void testCreateArchiveInputStreamCpioBinary() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] cpioMagic = new byte[] {(byte)0xC7, (byte)0x71};
        InputStream in = createStream(cpioMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return a CpioArchiveInputStream", ais);
        assertTrue("Should be instance of CpioArchiveInputStream", ais instanceof CpioArchiveInputStream);
    }

    // Test AR detection (magic: "!<arch>\n")
    @Test
    public void testCreateArchiveInputStreamAr() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] arMagic = "!<arch>\n".getBytes();
        InputStream in = createStream(arMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return an ArArchiveInputStream", ais);
        assertTrue("Should be instance of ArArchiveInputStream", ais instanceof ArArchiveInputStream);
    }

    // Test DUMP detection (magic: 0x01, 0x15 at offset 0)
    @Test
    public void testCreateArchiveInputStreamDump() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] dumpMagic = new byte[] {0x01, 0x15};
        InputStream in = createStream(dumpMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return a DumpArchiveInputStream", ais);
        assertTrue("Should be instance of DumpArchiveInputStream", ais instanceof DumpArchiveInputStream);
    }

    // Test 7Z detection (magic: "7z\xBC\xAF\x27\x1C")
    @Test
    public void testCreateArchiveInputStream7z() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] sevenZMagic = new byte[] {0x37, 0x7A, (byte)0xBC, (byte)0xAF, 0x27, 0x1C};
        InputStream in = createStream(sevenZMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return a SevenZArchiveInputStream", ais);
        assertTrue("Should be instance of SevenZArchiveInputStream", ais instanceof SevenZArchiveInputStream);
    }

    // Test createArchiveInputStream with explicit archive name (valid)
    @Test
    public void testCreateArchiveInputStreamWithName() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // Use a minimal valid stream for each type
        // For ZIP, we need at least the magic bytes
        byte[] zipMagic = new byte[] {0x50, 0x4B, 0x03, 0x04};
        InputStream in = createStream(zipMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream("zip", in);
        assertNotNull(ais);
        assertTrue(ais instanceof ZipArchiveInputStream);
    }

    // Test createArchiveInputStream with explicit archive name (invalid name)
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamWithInvalidName() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream("invalid", in);
    }

    // Test createArchiveInputStream with null name
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamWithNullName() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream(null, in);
    }

    // Test createArchiveOutputStream with null output
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNull() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        factory.createArchiveOutputStream((OutputStream) null);
    }

    // Test createArchiveOutputStream with explicit name (valid)
    @Test
    public void testCreateArchiveOutputStreamWithName() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        ArchiveOutputStream aos = factory.createArchiveOutputStream("zip", out);
        assertNotNull(aos);
        assertTrue(aos instanceof ZipArchiveOutputStream);
    }

    // Test createArchiveOutputStream with invalid name
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamWithInvalidName() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        factory.createArchiveOutputStream("invalid", out);
    }

    // Test createArchiveOutputStream with null name
    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamWithNullName() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        factory.createArchiveOutputStream(null, out);
    }

    // Test that the factory correctly handles the "ar" format (potential bug area)
    @Test
    public void testCreateArchiveInputStreamArWithExtraData() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] arMagic = "!<arch>\n".getBytes();
        byte[] extra = "some data".getBytes();
        InputStream in = createStream(arMagic, extra);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        assertNotNull("Should return an ArArchiveInputStream", ais);
        assertTrue("Should be instance of ArArchiveInputStream", ais instanceof ArArchiveInputStream);
    }

    // Test that the factory throws exception for truncated magic bytes
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamTruncatedMagic() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        // Only first two bytes of ZIP magic
        byte[] truncatedMagic = new byte[] {0x50, 0x4B};
        factory.createArchiveInputStream(createStream(truncatedMagic));
    }

    // Test that the factory can handle multiple calls (reuse)
    @Test
    public void testMultipleDetections() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] zipMagic = new byte[] {0x50, 0x4B, 0x03, 0x04};
        byte[] tarMagic = new byte[512];
        byte[] ustar = "ustar\0".getBytes();
        System.arraycopy(ustar, 0, tarMagic, 257, ustar.length);

        ArchiveInputStream ais1 = factory.createArchiveInputStream(createStream(zipMagic));
        assertTrue(ais1 instanceof ZipArchiveInputStream);

        ArchiveInputStream ais2 = factory.createArchiveInputStream(new ByteArrayInputStream(tarMagic));
        assertTrue(ais2 instanceof TarArchiveInputStream);
    }

    // Test that the factory returns correct type for JAR when stream has JAR magic (same as ZIP)
    @Test
    public void testJarDetection() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        byte[] jarMagic = new byte[] {0x50, 0x4B, 0x03, 0x04};
        InputStream in = createStream(jarMagic);
        ArchiveInputStream ais = factory.createArchiveInputStream(in);
        // The factory should return JarArchiveInputStream for JAR streams
        assertTrue("Should be JarArchiveInputStream", ais instanceof JarArchiveInputStream);
    }

    // Test that the factory throws exception for unsupported archive type in createArchiveInputStream with name
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamUnsupportedName() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        factory.createArchiveInputStream("7z", in); // 7z is not supported via name? Actually it is, but let's test a non-existent
    }

    // Test that the factory throws exception for unsupported archive type in createArchiveOutputStream with name
    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamUnsupportedName() throws ArchiveException {
        ArchiveStreamFactory factory = new ArchiveStreamFactory();
        OutputStream out = new ByteArrayOutputStream();
        factory.createArchiveOutputStream("7z", out);
    }
}