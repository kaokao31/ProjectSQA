package org.apache.commons.compress.archivers.sevenz;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class SevenZOutputFileTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private File archiveFile;
    private SevenZOutputFile sevenZOut;

    @Before
    public void setUp() throws Exception {
        archiveFile = tempFolder.newFile("test.7z");
        sevenZOut = new SevenZOutputFile(archiveFile);
    }

    @After
    public void tearDown() throws Exception {
        if (sevenZOut != null) {
            sevenZOut.close();
        }
    }

    // -----------------------------------------------------------------------
    // Test 1: Empty archive (no entries)
    // -----------------------------------------------------------------------
    @Test
    public void testEmptyArchive() throws Exception {
        sevenZOut.close();
        sevenZOut = null;
        // Verify archive exists and can be read (should have 0 entries)
        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            assertEquals(0, sevenZFile.getEntries().size());
        }
    }

    // -----------------------------------------------------------------------
    // Test 2: Write a single entry with content
    // -----------------------------------------------------------------------
    @Test
    public void testSingleEntryWithContent() throws Exception {
        final String content = "Hello, SevenZ!";
        final SevenZArchiveEntry entry = createEntry("entry1.txt", content.length());
        sevenZOut.putArchiveEntry(entry);
        sevenZOut.write(content.getBytes(StandardCharsets.UTF_8));
        sevenZOut.closeArchiveEntry();
        sevenZOut.close();
        sevenZOut = null;

        // Verify
        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry readEntry = sevenZFile.getNextEntry();
            assertNotNull(readEntry);
            assertEquals("entry1.txt", readEntry.getName());
            byte[] readContent = new byte[(int) readEntry.getSize()];
            assertEquals(readEntry.getSize(), sevenZFile.read(readContent));
            assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), readContent);
        }
    }

    // -----------------------------------------------------------------------
    // Test 3: Multiple entries
    // -----------------------------------------------------------------------
    @Test
    public void testMultipleEntries() throws Exception {
        final String[] names = {"a.txt", "b.txt", "c.txt"};
        final byte[][] contents = {
            "Content A".getBytes(StandardCharsets.UTF_8),
            "Content B".getBytes(StandardCharsets.UTF_8),
            "Content C".getBytes(StandardCharsets.UTF_8)
        };

        for (int i = 0; i < names.length; i++) {
            SevenZArchiveEntry entry = createEntry(names[i], contents[i].length);
            sevenZOut.putArchiveEntry(entry);
            sevenZOut.write(contents[i]);
            sevenZOut.closeArchiveEntry();
        }
        sevenZOut.close();
        sevenZOut = null;

        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            for (int i = 0; i < names.length; i++) {
                SevenZArchiveEntry readEntry = sevenZFile.getNextEntry();
                assertNotNull("Missing entry " + i, readEntry);
                assertEquals(names[i], readEntry.getName());
                byte[] readContent = new byte[(int) readEntry.getSize()];
                int bytesRead = sevenZFile.read(readContent);
                assertEquals(contents[i].length, bytesRead);
                assertArrayEquals(contents[i], readContent);
            }
            assertNull(sevenZFile.getNextEntry());
        }
    }

    // -----------------------------------------------------------------------
    // Test 4: Write zero-length entry
    // -----------------------------------------------------------------------
    @Test
    public void testZeroLengthEntry() throws Exception {
        SevenZArchiveEntry entry = createEntry("empty.txt", 0);
        sevenZOut.putArchiveEntry(entry);
        sevenZOut.closeArchiveEntry();
        sevenZOut.close();
        sevenZOut = null;

        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry readEntry = sevenZFile.getNextEntry();
            assertNotNull(readEntry);
            assertEquals(0, readEntry.getSize());
            byte[] buffer = new byte[1];
            assertEquals(-1, sevenZFile.read(buffer)); // EOF
        }
    }

    // -----------------------------------------------------------------------
    // Test 5: Write after close throws IOException
    // -----------------------------------------------------------------------
    @Test(expected = IOException.class)
    public void testWriteAfterClose() throws Exception {
        sevenZOut.close();
        sevenZOut.write(new byte[1]);
    }

    // -----------------------------------------------------------------------
    // Test 6: Put archive entry after close throws IOException
    // -----------------------------------------------------------------------
    @Test(expected = IOException.class)
    public void testPutEntryAfterClose() throws Exception {
        sevenZOut.close();
        sevenZOut.putArchiveEntry(createEntry("late.txt", 0));
    }

    // -----------------------------------------------------------------------
    // Test 7: Close with no entries is allowed
    // -----------------------------------------------------------------------
    @Test
    public void testCloseNoEntries() throws Exception {
        sevenZOut.close();
        sevenZOut = null;
        // Should succeed without exception
    }

    // -----------------------------------------------------------------------
    // Test 8: Write null entry should throw NullPointerException
    // -----------------------------------------------------------------------
    @Test(expected = NullPointerException.class)
    public void testNullEntry() throws Exception {
        sevenZOut.putArchiveEntry(null);
    }

    // -----------------------------------------------------------------------
    // Test 9: Write without opening entry should throw IllegalStateException
    // -----------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void testWriteWithoutEntry() throws Exception {
        sevenZOut.write(new byte[1]);
    }

    // -----------------------------------------------------------------------
    // Test 10: Double close of archive entry (closeArchiveEntry when no entry open)
    // -----------------------------------------------------------------------
    @Test(expected = IOException.class)
    public void testDoubleCloseArchiveEntry() throws Exception {
        SevenZArchiveEntry entry = createEntry("test.txt", 5);
        sevenZOut.putArchiveEntry(entry);
        sevenZOut.closeArchiveEntry();
        sevenZOut.closeArchiveEntry(); // second close should fail
    }

    // -----------------------------------------------------------------------
    // Test 11: Large content to trigger potential buffer issues
    // -----------------------------------------------------------------------
    @Test
    public void testLargeContent() throws Exception {
        final int size = 1024 * 1024; // 1 MB
        byte[] largeContent = new byte[size];
        new Random().nextBytes(largeContent);
        SevenZArchiveEntry entry = createEntry("large.bin", size);
        sevenZOut.putArchiveEntry(entry);
        sevenZOut.write(largeContent);
        sevenZOut.closeArchiveEntry();
        sevenZOut.close();
        sevenZOut = null;

        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry readEntry = sevenZFile.getNextEntry();
            assertNotNull(readEntry);
            assertEquals(size, readEntry.getSize());
            byte[] readContent = new byte[size];
            int totalRead = 0;
            while (totalRead < size) {
                int read = sevenZFile.read(readContent, totalRead, size - totalRead);
                if (read < 0) break;
                totalRead += read;
            }
            assertEquals(size, totalRead);
            assertArrayEquals(largeContent, readContent);
        }
    }

    // -----------------------------------------------------------------------
    // Test 12: Different compression method (if applicable)
    // -----------------------------------------------------------------------
    @Test
    public void testCompressionMethodLZMA2() throws Exception {
        // SevenZOutputFile default is LZMA2; just ensure it works
        String content = "Compression test with LZMA2";
        SevenZArchiveEntry entry = createEntry("lzma2.txt", content.length());
        sevenZOut.putArchiveEntry(entry);
        sevenZOut.write(content.getBytes(StandardCharsets.UTF_8));
        sevenZOut.closeArchiveEntry();
        sevenZOut.close();
        sevenZOut = null;

        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry readEntry = sevenZFile.getNextEntry();
            assertNotNull(readEntry);
            assertEquals(content.length(), readEntry.getSize());
        }
    }

    // -----------------------------------------------------------------------
    // Test 13: Copy from input stream (utility style)
    // -----------------------------------------------------------------------
    @Test
    public void testWriteFromInputStream() throws Exception {
        final String data = "Data from stream";
        SevenZArchiveEntry entry = createEntry("stream.txt", data.length());
        sevenZOut.putArchiveEntry(entry);
        try (InputStream in = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
            IOUtils.copy(in, (OutputStream) sevenZOut);
        }
        sevenZOut.closeArchiveEntry();
        sevenZOut.close();
        sevenZOut = null;

        try (SevenZFile sevenZFile = new SevenZFile(archiveFile)) {
            SevenZArchiveEntry readEntry = sevenZFile.getNextEntry();
            assertNotNull(readEntry);
            byte[] readContent = new byte[(int) readEntry.getSize()];
            sevenZFile.read(readContent);
            assertArrayEquals(data.getBytes(StandardCharsets.UTF_8), readContent);
        }
    }

    // -----------------------------------------------------------------------
    // Test 14: Test that finish() is allowed only once (close calls finish)
    // -----------------------------------------------------------------------
    @Test(expected = IOException.class)
    public void testFinishCalledTwice() throws Exception {
        sevenZOut.close(); // calls finish
        sevenZOut.finish(); // second finish should throw
    }

    // -----------------------------------------------------------------------
    // Helper method to create SevenZArchiveEntry with given name and size
    // -----------------------------------------------------------------------
    private SevenZArchiveEntry createEntry(String name, long size) {
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName(name);
        entry.setSize(size);
        return entry;
    }

    // Additional assertion helper (assertNull) used in testMultipleEntries
    private void assertNull(Object obj) {
        if (obj != null) {
            throw new AssertionError("Expected null but was: " + obj);
        }
    }
}