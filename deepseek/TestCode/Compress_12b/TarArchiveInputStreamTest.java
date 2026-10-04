package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

/**
 * Test class for TarArchiveInputStream.
 * Covers typical usage, edge cases, and the specific fault of bug 12
 * (handling of zero-size file entries).
 */
public class TarArchiveInputStreamTest {

    private ByteArrayOutputStream baos;
    private TarArchiveOutputStream taos;
    private byte[] tarData;

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
        if (baos != null) {
            baos.close();
        }
    }

    // Helper to build tar data from a list of entries
    private void buildTar(List<TarArchiveEntry> entries, Map<String, byte[]> data) throws IOException {
        for (TarArchiveEntry entry : entries) {
            taos.putArchiveEntry(entry);
            if (entry.isFile()) {
                byte[] content = data.getOrDefault(entry.getName(), new byte[0]);
                taos.write(content);
            }
            taos.closeArchiveEntry();
        }
        taos.close();
        tarData = baos.toByteArray();
    }

    // Helper to create a TarArchiveInputStream from the built tar data
    private TarArchiveInputStream createTarInputStream() {
        return new TarArchiveInputStream(new ByteArrayInputStream(tarData));
    }

    // ---------------------------------------------------------------
    // Tests for standard file entries
    // ---------------------------------------------------------------

    @Test
    public void testReadRegularFile() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(5);
        byte[] content = "Hello".getBytes("UTF-8");
        buildTar(Arrays.asList(entry), Collections.singletonMap("file.txt", content));

        TarArchiveInputStream tin = createTarInputStream();
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull("First entry should not be null", readEntry);
        assertEquals("file.txt", readEntry.getName());
        assertTrue("Should be a file", readEntry.isFile());
        byte[] buffer = new byte[512];
        int len = tin.read(buffer);
        assertEquals("Should read 5 bytes", 5, len);
        assertArrayEquals("Hello".getBytes("UTF-8"), Arrays.copyOf(buffer, 5));
        int next = tin.read(buffer);
        assertEquals("Read past end should return -1", -1, next);
        assertNull("No more entries", tin.getNextTarEntry());
        tin.close();
    }

    @Test
    public void testReadEmptyFile() throws Exception {
        // Bug 12: zero-size file entry must be handled without error
        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        entry.setSize(0);
        buildTar(Arrays.asList(entry), new HashMap<String, byte[]>());

        TarArchiveInputStream tin = createTarInputStream();
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull("Entry should not be null", readEntry);
        assertEquals("empty.txt", readEntry.getName());
        assertTrue("Should be a file", readEntry.isFile());
        assertEquals("File size should be 0", 0, readEntry.getSize());
        byte[] buffer = new byte[1];
        int len = tin.read(buffer);
        assertEquals("Read on empty file should return -1", -1, len);
        assertNull("No more entries", tin.getNextTarEntry());
        tin.close();
    }

    @Test
    public void testReadDirectory() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("mydir/");
        entry.setSize(0);
        buildTar(Arrays.asList(entry), new HashMap<String, byte[]>());

        TarArchiveInputStream tin = createTarInputStream();
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("mydir/", readEntry.getName());
        assertTrue("Should be a directory", readEntry.isDirectory());
        assertEquals(0, readEntry.getSize());
        // Reading a directory entry should yield -1 immediately
        int len = tin.read(new byte[1]);
        assertEquals("Read on directory should return -1", -1, len);
        assertNull("No more entries", tin.getNextTarEntry());
        tin.close();
    }

    // ---------------------------------------------------------------
    // Tests for skip and available
    // ---------------------------------------------------------------

    @Test
    public void testSkip() throws Exception {
        byte[] content = "1234567890".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("skip.txt");
        entry.setSize(10);
        buildTar(Arrays.asList(entry), Collections.singletonMap("skip.txt", content));

        TarArchiveInputStream tin = createTarInputStream();
        tin.getNextTarEntry();
        long skipped = tin.skip(5);
        assertEquals("Should skip 5 bytes", 5, skipped);
        byte[] buffer = new byte[5];
        int len = tin.read(buffer);
        assertEquals("Should read remaining 5 bytes", 5, len);
        assertArrayEquals("67890".getBytes("UTF-8"), buffer);
        tin.close();
    }

    @Test
    public void testSkipPastEnd() throws Exception {
        byte[] content = "abc".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("small.txt");
        entry.setSize(3);
        buildTar(Arrays.asList(entry), Collections.singletonMap("small.txt", content));

        TarArchiveInputStream tin = createTarInputStream();
        tin.getNextTarEntry();
        long skipped = tin.skip(10);
        assertEquals("Should skip only 3 bytes", 3, skipped);
        int next = tin.read(new byte[1]);
        assertEquals("Read after skipping all data should return -1", -1, next);
        tin.close();
    }

    @Test
    public void testAvailableBeforeAndAfterRead() throws Exception {
        byte[] content = "Hello".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("avail.txt");
        entry.setSize(5);
        buildTar(Arrays.asList(entry), Collections.singletonMap("avail.txt", content));

        TarArchiveInputStream tin = createTarInputStream();
        tin.getNextTarEntry();
        // available returns an estimate, at least 0
        assertTrue("Available should be >= 0", tin.available() >= 0);
        tin.read(new byte[5]);
        // After reading all, available should still be >= 0
        assertTrue("Available after read should be >= 0", tin.available() >= 0);
        tin.close();
    }

    // ---------------------------------------------------------------
    // Tests for multiple entries
    // ---------------------------------------------------------------

    @Test
    public void testMultipleEntries() throws Exception {
        TarArchiveEntry entry1 = new TarArchiveEntry("a.txt");
        entry1.setSize(2);
        TarArchiveEntry entry2 = new TarArchiveEntry("b.txt");
        entry2.setSize(3);
        Map<String, byte[]> data = new HashMap<>();
        data.put("a.txt", "aa".getBytes("UTF-8"));
        data.put("b.txt", "bbb".getBytes("UTF-8"));
        buildTar(Arrays.asList(entry1, entry2), data);

        TarArchiveInputStream tin = createTarInputStream();
        TarArchiveEntry e1 = tin.getNextTarEntry();
        assertNotNull(e1);
        assertEquals("a.txt", e1.getName());
        assertEquals(2, e1.getSize());
        byte[] buf = new byte[2];
        assertEquals(2, tin.read(buf));
        assertArrayEquals("aa".getBytes("UTF-8"), buf);

        TarArchiveEntry e2 = tin.getNextTarEntry();
        assertNotNull(e2);
        assertEquals("b.txt", e2.getName());
        assertEquals(3, e2.getSize());
        buf = new byte[3];
        assertEquals(3, tin.read(buf));
        assertArrayEquals("bbb".getBytes("UTF-8"), buf);

        assertNull("No more entries", tin.getNextTarEntry());
        tin.close();
    }

    // ---------------------------------------------------------------
    // Test for long file names (GNU extension)
    // ---------------------------------------------------------------

    @Test
    public void testLongFileName() throws Exception {
        // Create a name longer than 100 chars to force use of GNU long name extension
        String longName = "this/is/a/very/long/file/name/that/exceeds/the/standard/100/character/limit/and/should/be/handled/by/gnu/extension/1234567890.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        buildTar(Arrays.asList(entry), new HashMap<String, byte[]>());

        TarArchiveInputStream tin = createTarInputStream();
        // Need to set the long file mode to support GNU? The default should work.
        // Actually, with TarArchiveOutputStream, long file names are written as GNU style by default.
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull("Long name entry should be read", readEntry);
        assertEquals("Long name should be preserved", longName, readEntry.getName());
        assertTrue("Should be a file", readEntry.isFile());
        assertEquals(0, readEntry.getSize());
        assertNull("No more entries", tin.getNextTarEntry());
        tin.close();
    }

    // ---------------------------------------------------------------
    // Test for reading in small chunks (branch coverage)
    // ---------------------------------------------------------------

    @Test
    public void testReadInChunks() throws Exception {
        byte[] content = "HelloWorld".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("chunks.txt");
        entry.setSize(10);
        buildTar(Arrays.asList(entry), Collections.singletonMap("chunks.txt", content));

        TarArchiveInputStream tin = createTarInputStream();
        tin.getNextTarEntry();
        byte[] buffer = new byte[3];
        int total = 0;
        int len;
        while ((len = tin.read(buffer)) != -1) {
            total += len;
        }
        assertEquals("Total bytes read should be 10", 10, total);
        tin.close();
    }

    // ---------------------------------------------------------------
    // Test for reading past end returns -1
    // ---------------------------------------------------------------

    @Test
    public void testReadPastEndOfStream() throws Exception {
        byte[] content = "123".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("three.txt");
        entry.setSize(3);
        buildTar(Arrays.asList(entry), Collections.singletonMap("three.txt", content));

        TarArchiveInputStream tin = createTarInputStream();
        tin.getNextTarEntry();
        byte[] buf = new byte[10];
        int first = tin.read(buf);
        assertEquals("First read should get all 3 bytes", 3, first);
        int second = tin.read(buf);
        assertEquals("Second read should return -1", -1, second);
        tin.close();
    }

    // ---------------------------------------------------------------
    // Tests for constructor exceptions and null handling
    // ---------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void testConstructorNullInputStream() {
        new TarArchiveInputStream(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullInputStreamAndEncoding() {
        new TarArchiveInputStream(null, "UTF-8");
    }

    @Test
    public void testConstructorWithEncoding() throws Exception {
        // Just verify that we can construct with encoding
        TarArchiveInputStream tin = new TarArchiveInputStream(
                new ByteArrayInputStream(new byte[0]), "UTF-8");
        assertNotNull(tin);
        tin.close();
    }

    // ---------------------------------------------------------------
    // Test for getRecordSize
    // ---------------------------------------------------------------

    @Test
    public void testGetRecordSize() {
        // Default record size is 512 bytes
        TarArchiveInputStream tin = new TarArchiveInputStream(
                new ByteArrayInputStream(new byte[0]));
        assertEquals("Record size should be 512", 512, tin.getRecordSize());
        tin.close();
    }

    // ---------------------------------------------------------------
    // Test for reading after close (should throw IOException)
    // ---------------------------------------------------------------

    @Test(expected = IOException.class)
    public void testReadAfterClose() throws Exception {
        TarArchiveInputStream tin = new TarArchiveInputStream(
                new ByteArrayInputStream(new byte[0]));
        tin.close();
        tin.read(new byte[1]);
    }

    @Test(expected = IOException.class)
    public void testSkipAfterClose() throws Exception {
        TarArchiveInputStream tin = new TarArchiveInputStream(
                new ByteArrayInputStream(new byte[0]));
        tin.close();
        tin.skip(1);
    }

    // ---------------------------------------------------------------
    // Test for reading a tar file with only end-of-archive blocks (two zero blocks)
    // ---------------------------------------------------------------

    @Test
    public void testEmptyTar() throws Exception {
        // A tar file with no entries just has two 512-byte zero blocks.
        byte[] emptyTar = new byte[1024];
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(emptyTar));
        assertNull("getNextTarEntry should return null for empty tar", tin.getNextTarEntry());
        // read should return -1
        byte[] buf = new byte[1];
        assertEquals("read on empty tar should return -1", -1, tin.read(buf));
        tin.close();
    }

    // ---------------------------------------------------------------
    // Test for handling of entries with size larger than actual data (branch on read)
    // This is important for edge cases.
    // ---------------------------------------------------------------

    @Test
    public void testReadEntryWithSizeLargerThanData() throws Exception {
        // Write an entry claiming size 10 but only provide 5 bytes (will be padded)
        // TarArchiveOutputStream will pad with zeros to the block boundary.
        byte[] content = "12345".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("partial.txt");
        entry.setSize(10);
        buildTar(Arrays.asList(entry), Collections.singletonMap("partial.txt", content));

        TarArchiveInputStream tin = createTarInputStream();
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(10, readEntry.getSize());
        byte[] buffer = new byte[10];
        int len = tin.read(buffer);
        assertEquals("Should read exactly 10 bytes", 10, len);
        // First 5 bytes are our content, last 5 are zeros (padding)
        byte[] expected = Arrays.copyOf(content, 10);
        assertArrayEquals("Content should be data + zeros", expected, buffer);
        int next = tin.read(buffer);
        assertEquals("No more data", -1, next);
        tin.close();
    }
}