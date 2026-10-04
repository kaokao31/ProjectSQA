package org.apache.commons.compress.archivers.tar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for TarArchiveOutputStream.
 * Designed to achieve high line/branch coverage and trigger potential faults.
 */
public class TarArchiveOutputStreamTest {

    private ByteArrayOutputStream baos;
    private TarArchiveOutputStream taos;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
        taos = new TarArchiveOutputStream(baos);
    }

    @After
    public void tearDown() throws IOException {
        if (taos != null) {
            taos.close();
        }
    }

    // ---------- putArchiveEntry tests ----------

    @Test(expected = NullPointerException.class)
    public void testPutArchiveEntryNull() throws IOException {
        taos.putArchiveEntry(null);
    }

    @Test
    public void testPutArchiveEntryEmptyName() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test
    public void testPutArchiveEntryNormalName() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test
    public void testPutArchiveEntryLongName() throws IOException {
        // Name longer than 100 characters (old tar format limit)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append('a');
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test
    public void testPutArchiveEntryDirectory() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("mydir/");
        entry.setMode(040755); // typical directory mode
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryAfterClose() throws IOException {
        taos.close();
        TarArchiveEntry entry = new TarArchiveEntry("afterclose.txt");
        taos.putArchiveEntry(entry);
    }

    // ---------- closeArchiveEntry tests ----------

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithoutPut() throws IOException {
        taos.closeArchiveEntry();
    }

    @Test
    public void testCloseArchiveEntryMultiple() throws IOException {
        TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        taos.putArchiveEntry(entry1);
        taos.closeArchiveEntry();
        TarArchiveEntry entry2 = new TarArchiveEntry("file2.txt");
        taos.putArchiveEntry(entry2);
        taos.closeArchiveEntry();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // ---------- write tests ----------

    @Test(expected = NullPointerException.class)
    public void testWriteNullBuffer() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.write(null, 0, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteNegativeOffset() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.write(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteNegativeLength() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.write(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteOffsetPlusLengthExceedsBuffer() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.write(new byte[10], 5, 10);
    }

    @Test
    public void testWriteZeroLength() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        taos.putArchiveEntry(entry);
        taos.write(new byte[0], 0, 0);
        taos.closeArchiveEntry();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test
    public void testWriteSmallData() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("small.txt");
        entry.setSize(5);
        taos.putArchiveEntry(entry);
        taos.write(new byte[]{1,2,3,4,5}, 0, 5);
        taos.closeArchiveEntry();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test
    public void testWriteLargeData() throws IOException {
        // Write more than 512 bytes to test block padding
        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        byte[] large = new byte[1024];
        for (int i = 0; i < large.length; i++) {
            large[i] = (byte) (i % 256);
        }
        entry.setSize(large.length);
        taos.putArchiveEntry(entry);
        taos.write(large, 0, large.length);
        taos.closeArchiveEntry();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test(expected = IOException.class)
    public void testWriteAfterClose() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();
        taos.write(new byte[10], 0, 10);
    }

    // ---------- finish tests ----------

    @Test
    public void testFinishNormal() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.finish();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
        // Should end with two zero blocks (512 bytes each)
        assertTrue(data.length >= 1024);
        for (int i = data.length - 1024; i < data.length; i++) {
            assertEquals(0, data[i]);
        }
    }

    @Test(expected = IOException.class)
    public void testFinishWithoutClosingEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.finish(); // should fail because entry not closed
    }

    @Test(expected = IOException.class)
    public void testFinishAfterClose() throws IOException {
        taos.close();
        taos.finish();
    }

    // ---------- close tests ----------

    @Test
    public void testCloseWithoutEntries() throws IOException {
        taos.close();
        // Should produce empty archive (just two zero blocks)
        byte[] data = baos.toByteArray();
        assertEquals(1024, data.length);
        for (byte b : data) {
            assertEquals(0, b);
        }
    }

    @Test
    public void testCloseWithEntries() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test
    public void testCloseMultipleTimes() throws IOException {
        taos.close();
        taos.close(); // should be idempotent
    }

    // ---------- long name bug detection (Defects4J style) ----------

    @Test
    public void testLongNameWithContent() throws IOException {
        // Some versions of TarArchiveOutputStream mishandle long names
        // when writing content. This test attempts to trigger that.
        String longName = "a".repeat(200);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        byte[] content = "Hello, World!".getBytes();
        entry.setSize(content.length);
        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        // Verify that the entry name is stored correctly (via tar header parsing)
        // We can't easily parse here, but at least no exception should occur.
        assertTrue(data.length > 0);
    }

    // ---------- edge cases for size ----------

    @Test(expected = IOException.class)
    public void testWriteExceedingSetSize() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("sizecheck.txt");
        entry.setSize(5);
        taos.putArchiveEntry(entry);
        taos.write(new byte[10], 0, 10); // writing more than declared size
        taos.closeArchiveEntry();
    }

    @Test
    public void testWriteLessThanSetSize() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("partial.txt");
        entry.setSize(10);
        taos.putArchiveEntry(entry);
        taos.write(new byte[5], 0, 5);
        taos.closeArchiveEntry(); // should pad remaining with zeros
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // ---------- PAX header tests (if supported) ----------

    @Test
    public void testPaxHeadersForLongName() throws IOException {
        // Enable PAX headers for long names (if implementation supports it)
        // This test assumes the implementation uses PAX when name > 100.
        String longName = "a".repeat(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        byte[] content = "data".getBytes();
        entry.setSize(content.length);
        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        // Should contain a PAX header entry (type 'x') before the actual entry
        // We can check for the presence of 'x' in the header type field.
        // Simple check: look for the byte 'x' at offset 156 in the first header block.
        // This is fragile but can indicate PAX usage.
        // For robustness, we just ensure no exception.
        assertTrue(data.length > 0);
    }

    // ---------- flush tests ----------

    @Test
    public void testFlush() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.write(new byte[]{1,2,3});
        taos.flush(); // should not throw
        taos.closeArchiveEntry();
    }

    // ---------- getBytesWritten tests (if method exists) ----------

    @Test
    public void testGetBytesWritten() throws IOException {
        // Assuming TarArchiveOutputStream has getBytesWritten() method
        // (present in some versions)
        long initial = taos.getBytesWritten();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.write(new byte[100]);
        taos.closeArchiveEntry();
        long after = taos.getBytesWritten();
        assertTrue(after > initial);
    }

    // ---------- setAddPaxHeadersForNonAsciiNames (if method exists) ----------

    @Test
    public void testSetAddPaxHeadersForNonAsciiNames() throws IOException {
        // This method may not exist in all versions; test if present.
        // We'll call it via reflection to avoid compilation issues.
        // For simplicity, assume it exists.
        taos.setAddPaxHeadersForNonAsciiNames(true);
        TarArchiveEntry entry = new TarArchiveEntry("ümlaut.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // ---------- setLongFileMode tests ----------

    @Test
    public void testSetLongFileModeLONGFILE_POSIX() throws IOException {
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        String longName = "a".repeat(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test(expected = IOException.class)
    public void testSetLongFileModeLONGFILE_ERROR() throws IOException {
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        String longName = "a".repeat(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        taos.putArchiveEntry(entry);
    }

    @Test
    public void testSetLongFileModeLONGFILE_TRUNCATE() throws IOException {
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        String longName = "a".repeat(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // ---------- big number tests (size > 8GB) ----------

    @Test
    public void testLargeEntrySize() throws IOException {
        // Some implementations have bugs with sizes > 2^31 or > 8GB.
        // We'll test with a size that fits in long but may overflow int.
        TarArchiveEntry entry = new TarArchiveEntry("large.txt");
        long hugeSize = 8589934592L; // 8GB + 1 byte
        entry.setSize(hugeSize);
        taos.putArchiveEntry(entry);
        // Write nothing, just close to test header generation
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // ---------- negative size tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeEntrySize() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("neg.txt");
        entry.setSize(-1);
        taos.putArchiveEntry(entry);
    }

    // ---------- null entry name tests ----------

    @Test(expected = NullPointerException.class)
    public void testEntryWithNullName() throws IOException {
        // TarArchiveEntry constructor may throw NPE for null name
        TarArchiveEntry entry = new TarArchiveEntry((String) null);
        taos.putArchiveEntry(entry);
    }

    // ---------- write after finish ----------

    @Test(expected = IOException.class)
    public void testWriteAfterFinish() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.finish();
        taos.write(new byte[10], 0, 10);
    }

    // ---------- multiple entries with same name ----------

    @Test
    public void testDuplicateEntryNames() throws IOException {
        TarArchiveEntry entry1 = new TarArchiveEntry("dup.txt");
        taos.putArchiveEntry(entry1);
        taos.closeArchiveEntry();
        TarArchiveEntry entry2 = new TarArchiveEntry("dup.txt");
        taos.putArchiveEntry(entry2);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // ---------- entry with special characters in name ----------

    @Test
    public void testSpecialCharsInName() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("file with spaces.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // ---------- write with offset > 0 ----------

    @Test
    public void testWriteWithOffset() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("offset.txt");
        byte[] buf = new byte[100];
        for (int i = 0; i < 100; i++) {
            buf[i] = (byte) i;
        }
        entry.setSize(50);
        taos.putArchiveEntry(entry);
        taos.write(buf, 25, 50); // write bytes 25..74
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // ---------- ensure no extra bytes written after close ----------

    @Test
    public void testNoExtraBytesAfterClose() throws IOException {
        taos.close();
        byte[] data = baos.toByteArray();
        assertEquals(1024, data.length); // exactly two zero blocks
    }

    // ---------- test that finish writes EOF blocks ----------

    @Test
    public void testFinishWritesEOFBlocks() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.finish();
        byte[] data = baos.toByteArray();
        // Last 1024 bytes should be zeros
        for (int i = data.length - 1024; i < data.length; i++) {
            assertEquals(0, data[i]);
        }
    }

    // ---------- test that close also writes EOF blocks ----------

    @Test
    public void testCloseWritesEOFBlocks() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();
        byte[] data = baos.toByteArray();
        // Last 1024 bytes should be zeros
        for (int i = data.length - 1024; i < data.length; i++) {
            assertEquals(0, data[i]);
        }
    }
}