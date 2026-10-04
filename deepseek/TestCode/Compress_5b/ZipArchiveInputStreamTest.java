package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for ZipArchiveInputStream.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class ZipArchiveInputStreamTest {

    private File tempZipFile;

    @Before
    public void setUp() throws Exception {
        tempZipFile = File.createTempFile("testZip", ".zip");
        tempZipFile.deleteOnExit();
    }

    @After
    public void tearDown() throws Exception {
        if (tempZipFile != null && tempZipFile.exists()) {
            tempZipFile.delete();
        }
    }

    // Helper method to create a simple zip file with one entry containing data.
    private void createSimpleZip(String entryName, byte[] data) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(data);
            zos.closeEntry();
        }
    }

    // ---------- Constructor Tests ----------
    @Test
    public void testConstructorWithInputStream() {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertNotNull(zis);
        } catch (Exception e) {
            fail("Construction should not fail with empty stream");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullInputStream() {
        new ZipArchiveInputStream(null);
    }

    // ---------- getNextZipEntry Tests ----------
    @Test
    public void testGetNextZipEntrySingleEntry() throws IOException {
        byte[] content = "Hello World".getBytes("UTF-8");
        createSimpleZip("test.txt", content);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull("First entry should not be null", entry);
            assertEquals("Entry name mismatch", "test.txt", entry.getName());
            // Read entry content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = zis.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            assertArrayEquals("Content mismatch", content, baos.toByteArray());
            // No more entries
            assertNull("Next entry should be null", zis.getNextZipEntry());
        }
    }

    @Test
    public void testGetNextZipEntryMultipleEntries() throws IOException {
        createSimpleZip("entry1", "data1".getBytes());
        // Add second entry by recreating zip with both entries
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
            zos.putNextEntry(new ZipEntry("entry1"));
            zos.write("data1".getBytes());
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("entry2"));
            zos.write("data2".getBytes());
            zos.closeEntry();
        }
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("entry1", e1.getName());
            // Skip reading content
            zis.getNextZipEntry(); // skip to next
            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull("Second entry should exist", e2);
            assertEquals("entry2", e2.getName());
            assertNull("No more entries", zis.getNextZipEntry());
        }
    }

    @Test
    public void testGetNextZipEntryEmptyZip() throws IOException {
        // Create empty zip (just header)
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
            // no entries added
        }
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            assertNull("Empty zip should have no entries", zis.getNextZipEntry());
        }
    }

    // ---------- read() Tests ----------
    @Test
    public void testReadFromSingleEntry() throws IOException {
        byte[] content = "Test data for reading".getBytes("UTF-8");
        createSimpleZip("file.txt", content);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            byte[] readBuf = new byte[1024];
            int totalRead = 0;
            int len;
            while ((len = zis.read(readBuf, 0, readBuf.length)) != -1) {
                totalRead += len;
            }
            assertEquals("Read should return exact content length", content.length, totalRead);
        }
    }

    @Test
    public void testReadPartialBuffer() throws IOException {
        byte[] content = "Partial read test".getBytes("UTF-8");
        createSimpleZip("file.txt", content);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            zis.getNextZipEntry();
            byte[] smallBuf = new byte[5];
            int len = zis.read(smallBuf);
            assertEquals("First partial read should get 5 bytes", 5, len);
            // Continue reading remaining
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(zis, baos);
            byte[] remaining = baos.toByteArray();
            assertEquals("Remaining bytes length", content.length - 5, remaining.length);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testReadNullBuffer() throws IOException {
        createSimpleZip("entry", "data".getBytes());
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            zis.getNextZipEntry();
            zis.read(null, 0, 10);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        createSimpleZip("entry", "data".getBytes());
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            zis.getNextZipEntry();
            byte[] buf = new byte[10];
            zis.read(buf, -1, 5);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        createSimpleZip("entry", "data".getBytes());
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            zis.getNextZipEntry();
            byte[] buf = new byte[10];
            zis.read(buf, 0, -5);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLengthExceedsArray() throws IOException {
        createSimpleZip("entry", "data".getBytes());
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            zis.getNextZipEntry();
            byte[] buf = new byte[10];
            zis.read(buf, 8, 5); // 8+5 > 10
        }
    }

    @Test
    public void testReadZeroLength() throws IOException {
        createSimpleZip("entry", "data".getBytes());
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            zis.getNextZipEntry();
            byte[] buf = new byte[10];
            int len = zis.read(buf, 0, 0);
            assertEquals("Reading zero bytes should return 0", 0, len);
        }
    }

    // ---------- close() Tests ----------
    @Test
    public void testCloseAfterReading() throws IOException {
        createSimpleZip("entry", "data".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile));
        zis.getNextZipEntry();
        IOUtils.copy(zis, new ByteArrayOutputStream());
        zis.close(); // should not throw
    }

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws IOException {
        createSimpleZip("entry", "data".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile));
        zis.getNextZipEntry();
        zis.close();
        zis.read(new byte[10], 0, 10); // Should throw IOException
    }

    // ---------- Edge Cases ----------
    @Test
    public void testCorruptedZipExpectException() throws IOException {
        byte[] corrupt = { 0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        // Incomplete local file header
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(corrupt))) {
            assertNull("Corrupted entry should cause null or throw", zis.getNextZipEntry());
        } catch (ZipArchiveException e) {
            // expected
        }
    }

    @Test
    public void testLargeEntryName() throws IOException {
        String name = "";
        for (int i = 0; i < 65535; i++) name += "a";
        // This may exceed limits; test should handle gracefully.
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
            ZipEntry entry = new ZipEntry(name);
            zos.putNextEntry(entry);
            zos.write("data".getBytes());
            zos.closeEntry();
        } catch (IllegalArgumentException e) {
            // Name too long for Java's ZipEntry, skip test
            return;
        }
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull("Should accept long entry name", entry);
        }
    }

    // ---------- Coverage for loops and branches ----------
    @Test
    public void testReadWithMultipleCallsSmallBuffer() throws IOException {
        byte[] content = "abcdefghijklmnopqrstuvwxyz".getBytes("UTF-8");
        createSimpleZip("letters.txt", content);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            zis.getNextZipEntry();
            byte[] buf = new byte[3];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            while ((len = zis.read(buf, 0, 3)) != -1) {
                baos.write(buf, 0, len);
            }
            assertArrayEquals("Content should match after multiple reads", content, baos.toByteArray());
        }
    }

    @Test
    public void testEntryWithExtraField() throws IOException {
        // Create zip with extra field (simple)
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
            ZipEntry entry = new ZipEntry("extra.txt");
            entry.setExtra(new byte[] { 0x00, 0x01, 0x02, 0x03 });
            zos.putNextEntry(entry);
            zos.write("data".getBytes());
            zos.closeEntry();
        }
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertNotNull("Extra fields should be present", entry.getExtra());
            assertEquals("Extra field length", 4, entry.getExtra().length);
        }
    }

    // ---------- Tests for correct handling of STORED vs DEFLATED methods ----------
    @Test
    public void testStoredEntry() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
            ZipEntry entry = new ZipEntry("stored.txt");
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(4);
            entry.setCompressedSize(4);
            entry.setCrc(0x4b5b6c7aL); // CRC32 for "data"
            zos.putNextEntry(entry);
            zos.write("data".getBytes());
            zos.closeEntry();
        }
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(zis, baos);
            assertEquals("Stored content should match", "data", baos.toString("UTF-8"));
        }
    }

    // ---------- Multiple entries with skip reading ----------
    @Test
    public void testSkipReadingEntries() throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempZipFile))) {
            zos.putNextEntry(new ZipEntry("a"));
            zos.write("aaa".getBytes());
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("b"));
            zos.write("bbb".getBytes());
            zos.closeEntry();
        }
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(tempZipFile))) {
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertEquals("a", e1.getName());
            // do not read content of entry a; go to next
            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull("Should skip to next entry", e2);
            assertEquals("b", e2.getName());
            // read content of b
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(zis, baos);
            assertEquals("bbb", baos.toString("UTF-8"));
            assertNull("No more entries", zis.getNextZipEntry());
        }
    }

    // ---------- Test null name in ZipArchiveEntry (should not happen normally) ----------
    @Test
    public void testNullNamedEntry() throws Exception {
        // Create a zip with a null-named entry (using ZipArchiveEntry directly)
        File nullNameZip = File.createTempFile("nullName", ".zip");
        nullNameZip.deleteOnExit();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new FileOutputStream(nullNameZip))) {
            ZipEntry entry = new ZipEntry("");
            zos.putNextEntry(entry);
            zos.write("dummy".getBytes());
            zos.closeEntry();
        }
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(nullNameZip))) {
            ZipArchiveEntry e = zis.getNextZipEntry();
            assertNotNull(e);
            // Name might be empty string, not null
            assertTrue("Name should not be null", e.getName() != null);
        } finally {
            nullNameZip.delete();
        }
    }
}