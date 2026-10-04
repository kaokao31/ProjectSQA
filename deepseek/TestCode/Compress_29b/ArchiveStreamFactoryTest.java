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

    // ========== createArchiveInputStream tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullArg() throws ArchiveException {
        factory.createArchiveInputStream((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamNullStream() throws ArchiveException {
        factory.createArchiveInputStream((InputStream) null);
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamEmptyStream() throws ArchiveException {
        factory.createArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamUnsupportedSignature() throws ArchiveException {
        byte[] data = new byte[] { 0x00, 0x01, 0x02, 0x03 };
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test
    public void testCreateArchiveInputStreamAr() throws ArchiveException, IOException {
        byte[] arHeader = "!<arch>\n".getBytes("ASCII");
        InputStream in = new ByteArrayInputStream(arHeader);
        assertTrue(factory.createArchiveInputStream(in) instanceof ArArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamCpio() throws ArchiveException, IOException {
        byte[] cpioHeader = new byte[] { 0x71, 0xC7, 0x00, 0x00, 0x00, 0x00 };
        InputStream in = new ByteArrayInputStream(cpioHeader);
        assertTrue(factory.createArchiveInputStream(in) instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamDump() throws ArchiveException, IOException {
        byte[] dumpHeader = new byte[] { 0x01, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        InputStream in = new ByteArrayInputStream(dumpHeader);
        assertTrue(factory.createArchiveInputStream(in) instanceof DumpArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamJar() throws ArchiveException, IOException {
        byte[] jarHeader = new byte[] { 0x50, 0x4B, 0x03, 0x04 };
        InputStream in = new ByteArrayInputStream(jarHeader);
        assertTrue(factory.createArchiveInputStream(in) instanceof JarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamTar() throws ArchiveException, IOException {
        byte[] tarHeader = new byte[512];
        // TAR magic: "ustar" at offset 257
        tarHeader[257] = 'u';
        tarHeader[258] = 's';
        tarHeader[259] = 't';
        tarHeader[260] = 'a';
        tarHeader[261] = 'r';
        InputStream in = new ByteArrayInputStream(tarHeader);
        assertTrue(factory.createArchiveInputStream(in) instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamZip() throws ArchiveException, IOException {
        byte[] zipHeader = new byte[] { 0x50, 0x4B, 0x03, 0x04 };
        InputStream in = new ByteArrayInputStream(zipHeader);
        assertTrue(factory.createArchiveInputStream(in) instanceof ZipArchiveInputStream);
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
    public void testCreateArchiveInputStreamWithNameAr() throws ArchiveException {
        assertTrue(factory.createArchiveInputStream("ar", new ByteArrayInputStream(new byte[0])) instanceof ArArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameCpio() throws ArchiveException {
        assertTrue(factory.createArchiveInputStream("cpio", new ByteArrayInputStream(new byte[0])) instanceof CpioArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameDump() throws ArchiveException {
        assertTrue(factory.createArchiveInputStream("dump", new ByteArrayInputStream(new byte[0])) instanceof DumpArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameJar() throws ArchiveException {
        assertTrue(factory.createArchiveInputStream("jar", new ByteArrayInputStream(new byte[0])) instanceof JarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameTar() throws ArchiveException {
        assertTrue(factory.createArchiveInputStream("tar", new ByteArrayInputStream(new byte[0])) instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveInputStreamWithNameZip() throws ArchiveException {
        assertTrue(factory.createArchiveInputStream("zip", new ByteArrayInputStream(new byte[0])) instanceof ZipArchiveInputStream);
    }

    // ========== createArchiveOutputStream tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullArg() throws ArchiveException {
        factory.createArchiveOutputStream((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamNullStream() throws ArchiveException {
        factory.createArchiveOutputStream((OutputStream) null);
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
    public void testCreateArchiveOutputStreamAr() throws ArchiveException {
        assertTrue(factory.createArchiveOutputStream("ar", new ByteArrayOutputStream()) instanceof ArArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamCpio() throws ArchiveException {
        assertTrue(factory.createArchiveOutputStream("cpio", new ByteArrayOutputStream()) instanceof CpioArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamDump() throws ArchiveException {
        assertTrue(factory.createArchiveOutputStream("dump", new ByteArrayOutputStream()) instanceof DumpArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamJar() throws ArchiveException {
        assertTrue(factory.createArchiveOutputStream("jar", new ByteArrayOutputStream()) instanceof JarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamTar() throws ArchiveException {
        assertTrue(factory.createArchiveOutputStream("tar", new ByteArrayOutputStream()) instanceof TarArchiveOutputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamZip() throws ArchiveException {
        assertTrue(factory.createArchiveOutputStream("zip", new ByteArrayOutputStream()) instanceof ZipArchiveOutputStream);
    }

    // ========== Edge cases for signature detection ==========

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamShortSignatureAr() throws ArchiveException {
        // Only 7 bytes instead of 8
        byte[] data = "!<arch>".getBytes();
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamShortSignatureCpio() throws ArchiveException {
        byte[] data = new byte[] { 0x71, 0xC7 };
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamShortSignatureDump() throws ArchiveException {
        byte[] data = new byte[] { 0x01, 0x15 };
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamShortSignatureZip() throws ArchiveException {
        byte[] data = new byte[] { 0x50, 0x4B };
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamShortSignatureTar() throws ArchiveException {
        byte[] data = new byte[300];
        data[257] = 'u';
        data[258] = 's';
        data[259] = 't';
        data[260] = 'a';
        data[261] = 'r';
        // Only 300 bytes, not full 512
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamInvalidTarSignature() throws ArchiveException {
        byte[] data = new byte[512];
        data[257] = 'u';
        data[258] = 's';
        data[259] = 't';
        data[260] = 'a';
        data[261] = 'r';
        data[262] = ' '; // invalid version
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamInvalidArSignature() throws ArchiveException {
        byte[] data = "!<arch>\n".getBytes();
        data[0] = 'X'; // corrupt
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamInvalidCpioSignature() throws ArchiveException {
        byte[] data = new byte[] { 0x71, 0xC7, 0x00, 0x00, 0x00, 0x01 };
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamInvalidDumpSignature() throws ArchiveException {
        byte[] data = new byte[32];
        data[0] = 0x01;
        data[1] = 0x16; // wrong second byte
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamInvalidZipSignature() throws ArchiveException {
        byte[] data = new byte[] { 0x50, 0x4B, 0x03, 0x05 };
        factory.createArchiveInputStream(new ByteArrayInputStream(data));
    }

    // ========== Test that stream is not closed after detection ==========

    @Test
    public void testCreateArchiveInputStreamStreamNotClosed() throws ArchiveException, IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[] { 0x50, 0x4B, 0x03, 0x04 });
        assertTrue(in.available() > 0);
        factory.createArchiveInputStream(in);
        assertTrue(in.available() > 0); // stream should still be open
    }

    // ========== Test with encoding parameter ==========

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamWithEncodingNullStream() throws ArchiveException {
        factory.createArchiveInputStream("zip", null, "UTF-8");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveInputStreamWithEncodingNullName() throws ArchiveException {
        factory.createArchiveInputStream(null, new ByteArrayInputStream(new byte[0]), "UTF-8");
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveInputStreamWithEncodingUnknownName() throws ArchiveException {
        factory.createArchiveInputStream("unknown", new ByteArrayInputStream(new byte[0]), "UTF-8");
    }

    @Test
    public void testCreateArchiveInputStreamWithEncodingZip() throws ArchiveException {
        assertTrue(factory.createArchiveInputStream("zip", new ByteArrayInputStream(new byte[0]), "UTF-8") instanceof ZipArchiveInputStream);
    }

    // ========== Test createArchiveOutputStream with encoding ==========

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamWithEncodingNullStream() throws ArchiveException {
        factory.createArchiveOutputStream("zip", null, "UTF-8");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArchiveOutputStreamWithEncodingNullName() throws ArchiveException {
        factory.createArchiveOutputStream(null, new ByteArrayOutputStream(), "UTF-8");
    }

    @Test(expected = ArchiveException.class)
    public void testCreateArchiveOutputStreamWithEncodingUnknownName() throws ArchiveException {
        factory.createArchiveOutputStream("unknown", new ByteArrayOutputStream(), "UTF-8");
    }

    @Test
    public void testCreateArchiveOutputStreamWithEncodingZip() throws ArchiveException {
        assertTrue(factory.createArchiveOutputStream("zip", new ByteArrayOutputStream(), "UTF-8") instanceof ZipArchiveOutputStream);
    }

    // ========== Test case sensitivity ==========

    @Test
    public void testCreateArchiveInputStreamCaseInsensitive() throws ArchiveException {
        assertTrue(factory.createArchiveInputStream("ZIP", new ByteArrayInputStream(new byte[0])) instanceof ZipArchiveInputStream);
        assertTrue(factory.createArchiveInputStream("Tar", new ByteArrayInputStream(new byte[0])) instanceof TarArchiveInputStream);
    }

    @Test
    public void testCreateArchiveOutputStreamCaseInsensitive() throws ArchiveException {
        assertTrue(factory.createArchiveOutputStream("AR", new ByteArrayOutputStream()) instanceof ArArchiveOutputStream);
        assertTrue(factory.createArchiveOutputStream("Cpio", new ByteArrayOutputStream()) instanceof CpioArchiveOutputStream);
    }
}