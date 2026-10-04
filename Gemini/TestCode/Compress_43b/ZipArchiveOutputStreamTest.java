package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

public class ZipArchiveOutputStreamTest {

    @Test
    public void testDefaultEncoding() {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(new ByteArrayOutputStream());
        assertEquals("UTF8", zos.getEncoding());
        zos.setEncoding("UTF-8");
        assertEquals("UTF-8", zos.getEncoding());
    }

    @Test
    public void testCreateArchiveEntryWithZipFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        File dummyFile = File.createTempFile("commons-compress-test", ".tmp");
        dummyFile.deleteOnExit();

        ZipArchiveEntry entry = zos.createArchiveEntry(dummyFile, "test.txt");
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(dummyFile.length(), entry.getSize());
    }

    @Test
    public void testSetLevelAndMethod() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setLevel(5);
        zos.setMethod(ZipArchiveOutputStream.DEFLATED);

        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        zos.putArchiveEntry(entry);
        zos.write(new byte[]{1, 2, 3});
        zos.closeArchiveEntry();
        zos.finish();
        zos.close();

        assertTrue(baos.size() > 0);
    }

    @Test
    public void testAddCompressedSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("compressed.txt");
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zos.putArchiveEntry(entry);
        zos.write("Hello World".getBytes());
        zos.closeArchiveEntry();
        zos.finish();

        assertTrue(entry.getCompressedSize() >= 0);
    }

    @Test
    public void testUnicodeExtraFieldsPolicy() {
        ZipArchiveOutputStream.UnicodeExtraFieldPolicy policy = ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS;
        assertNotNull(policy.toString());
        
        ZipArchiveOutputStream.UnicodeExtraFieldPolicy never = ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER;
        assertNotNull(never.toString());

        ZipArchiveOutputStream.UnicodeExtraFieldPolicy notEncode = ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE;
        assertNotNull(notEncode.toString());
    }

    @Test
    public void testSetUseZip64() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setUseZip64(Zip64Mode.Always);

        ZipArchiveEntry entry = new ZipArchiveEntry("zip64.txt");
        zos.putArchiveEntry(entry);
        zos.write(new byte[]{1, 2, 3});
        zos.closeArchiveEntry();
        zos.finish();

        assertTrue(baos.size() > 0);
    }

    @Test(expected = ZipException.class)
    public void testExplicitZip64RequiredCheck() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setUseZip64(Zip64Mode.Never);

        ZipArchiveEntry entry = new ZipArchiveEntry("toobig.txt");
        // Force size beyond standard 4GB limit in estimation or behavior
        entry.setSize(0xFFFFFFFFL + 1L);
        zos.putArchiveEntry(entry);
    }

    @Test
    public void testSetComment() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setComment("Test Archive Comment");
        zos.finish();
        assertTrue(baos.size() > 0);
    }

    @Test
    public void testWriteOfNullByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("empty.txt");
        zos.putArchiveEntry(entry);
        try {
            zos.write(null, 0, 0);
        } catch (NullPointerException npe) {
            // expected in some implementations, or handled gracefully
        }
        zos.closeArchiveEntry();
        zos.finish();
    }
}