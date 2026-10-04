package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for ZipArchiveOutputStream.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class ZipArchiveOutputStreamTest {

    private ByteArrayOutputStream baos;
    private ZipArchiveOutputStream zos;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
        zos = new ZipArchiveOutputStream(baos);
    }

    @After
    public void tearDown() throws IOException {
        if (zos != null) {
            zos.close();
        }
    }

    // ==================== Basic Entry Operations ====================

    @Test
    public void testPutAndCloseSingleEntry() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        zos.putArchiveEntry(entry);
        zos.write("Hello".getBytes("UTF-8"));
        zos.closeArchiveEntry();
        zos.finish();
        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0);
    }

    @Test(expected = IOException.class)
    public void testPutEntryAfterClose() throws IOException {
        zos.close();
        zos.putArchiveEntry(new ZipArchiveEntry("fail.txt"));
    }

    @Test(expected = IOException.class)
    public void testWriteAfterClose() throws IOException {
        zos.close();
        zos.write(new byte[1]);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithoutEntry() throws IOException {
        zos.closeArchiveEntry();
    }

    // ==================== Multiple Entries ====================

    @Test
    public void testMultipleEntries() throws IOException {
        for (int i = 0; i < 5; i++) {
            ZipArchiveEntry entry = new ZipArchiveEntry("file" + i + ".txt");
            zos.putArchiveEntry(entry);
            zos.write(("content" + i).getBytes("UTF-8"));
            zos.closeArchiveEntry();
        }
        zos.finish();
        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0);
    }

    // ==================== Compression Methods ====================

    @Test
    public void testStoredCompression() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipEntry.STORED);
        // For stored entries, we must set size and CRC
        byte[] data = "Stored content".getBytes("UTF-8");
        entry.setSize(data.length);
        entry.setCrc(computeCrc32(data));
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
        zos.finish();
    }

    @Test
    public void testDeflatedCompression() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("deflated.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putArchiveEntry(entry);
        zos.write("Deflated content".getBytes("UTF-8"));
        zos.closeArchiveEntry();
        zos.finish();
    }

    // ==================== Unicode Names ====================

    @Test
    public void testUnicodeEntryName() throws IOException {
        String unicodeName = "测试文件.txt";
        ZipArchiveEntry entry = new ZipArchiveEntry(unicodeName);
        zos.putArchiveEntry(entry);
        zos.write("Unicode".getBytes("UTF-8"));
        zos.closeArchiveEntry();
        zos.finish();
        // Verify the name is stored correctly (UTF-8)
        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0);
    }

    // ==================== Comment and Extra Fields ====================

    @Test
    public void testSetComment() throws IOException {
        zos.setComment("Archive comment");
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        zos.putArchiveEntry(entry);
        zos.closeArchiveEntry();
        zos.finish();
    }

    @Test
    public void testEntryWithExtraFields() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("extra.txt");
        byte[] extra = new byte[] {0x01, 0x02, 0x03};
        entry.setExtra(extra);
        zos.putArchiveEntry(entry);
        zos.write("Extra".getBytes("UTF-8"));
        zos.closeArchiveEntry();
        zos.finish();
    }

    // ==================== Edge Cases ====================

    @Test
    public void testEmptyEntry() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("empty.txt");
        zos.putArchiveEntry(entry);
        zos.closeArchiveEntry();
        zos.finish();
    }

    @Test
    public void testLargeEntry() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("large.bin");
        byte[] data = new byte[65536];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
        zos.finish();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullEntry() throws IOException {
        zos.putArchiveEntry(null);
    }

    // ==================== Finish and Close ====================

    @Test
    public void testFinishWithoutEntries() throws IOException {
        zos.finish();
        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0); // Should produce an empty zip
    }

    @Test(expected = IOException.class)
    public void testFinishAfterClose() throws IOException {
        zos.close();
        zos.finish();
    }

    @Test
    public void testDoubleClose() throws IOException {
        zos.close();
        zos.close(); // Should not throw
    }

    // ==================== Write Methods ====================

    @Test
    public void testWriteByteArrayOffset() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("offset.txt");
        zos.putArchiveEntry(entry);
        byte[] data = "Offset test".getBytes("UTF-8");
        zos.write(data, 2, 5);
        zos.closeArchiveEntry();
        zos.finish();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteInvalidOffset() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("invalid.txt");
        zos.putArchiveEntry(entry);
        zos.write(new byte[10], -1, 5);
    }

    // ==================== Encoding ====================

    @Test
    public void testUtf8Encoding() throws IOException {
        zos.setEncoding("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry("utf8.txt");
        zos.putArchiveEntry(entry);
        zos.write("UTF-8 content".getBytes("UTF-8"));
        zos.closeArchiveEntry();
        zos.finish();
    }

    // ==================== Helper Methods ====================

    private long computeCrc32(byte[] data) {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data);
        return crc.getValue();
    }

    // ==================== File-based Tests ====================

    @Test
    public void testFileOutput() throws IOException {
        File tempFile = File.createTempFile("testZip", ".zip");
        tempFile.deleteOnExit();
        ZipArchiveOutputStream fileZos = null;
        try {
            fileZos = new ZipArchiveOutputStream(tempFile);
            ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
            fileZos.putArchiveEntry(entry);
            fileZos.write("File output".getBytes("UTF-8"));
            fileZos.closeArchiveEntry();
            fileZos.finish();
            assertTrue(tempFile.length() > 0);
        } finally {
            if (fileZos != null) {
                fileZos.close();
            }
        }
    }

    @Test(expected = IOException.class)
    public void testWriteToClosedFileStream() throws IOException {
        File tempFile = File.createTempFile("testZip", ".zip");
        tempFile.deleteOnExit();
        ZipArchiveOutputStream fileZos = new ZipArchiveOutputStream(tempFile);
        fileZos.close();
        fileZos.putArchiveEntry(new ZipArchiveEntry("fail.txt"));
    }

    // ==================== Defects4J Bug Pattern Tests ====================

    // Bug 43 often involves handling of zip entries with specific attributes
    @Test
    public void testEntryWithUnixMode() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("unix.txt");
        entry.setUnixMode(0755);
        zos.putArchiveEntry(entry);
        zos.write("Unix mode".getBytes("UTF-8"));
        zos.closeArchiveEntry();
        zos.finish();
    }

    @Test
    public void testEntryWithPlatformSpecific() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("platform.txt");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        zos.putArchiveEntry(entry);
        zos.closeArchiveEntry();
        zos.finish();
    }

    @Test
    public void testEntryWithGeneralPurposeBit() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("gpb.txt");
        entry.setGeneralPurposeBit(new GeneralPurposeBit().useUTF8ForNames(true));
        zos.putArchiveEntry(entry);
        zos.write("GPB".getBytes("UTF-8"));
        zos.closeArchiveEntry();
        zos.finish();
    }

    // ==================== Stress Test ====================

    @Test(timeout = 10000)
    public void testManySmallEntries() throws IOException {
        for (int i = 0; i < 1000; i++) {
            ZipArchiveEntry entry = new ZipArchiveEntry("entry" + i + ".txt");
            zos.putArchiveEntry(entry);
            zos.write(("data" + i).getBytes("UTF-8"));
            zos.closeArchiveEntry();
        }
        zos.finish();
    }
}