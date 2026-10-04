package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class TarArchiveInputStreamTest {

    private static final String LONG_NAME = 
        "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
        "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz" +
        "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"; // > 100 chars

    private static final String LONG_LINK_NAME = 
        "linklinklinklinklinklinklinklinklinklinklinklinklinklink" +
        "linklinklinklinklinklinklinklinklinklinklinklinklinklink" +
        "linklinklinklinklinklinklinklinklink"; // > 100 chars

    private byte[] createTarFromEntry(TarArchiveEntry entry, byte[] content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tos.setLongLinkMode(TarArchiveOutputStream.LONGFILE_GNU);
        tos.putArchiveEntry(entry);
        if (content != null) {
            tos.write(content);
        }
        tos.closeArchiveEntry();
        tos.close();
        return bos.toByteArray();
    }

    @Test
    public void testReadRegularFile() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        byte[] content = "1234567890".getBytes();
        byte[] tarBytes = createTarFromEntry(entry, content);

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            TarArchiveEntry readEntry = tin.getNextEntry();
            assertNotNull("getNextEntry should not be null", readEntry);
            assertEquals("Entry name", "test.txt", readEntry.getName());
            assertFalse("Entry should be a file", readEntry.isDirectory());
            assertEquals("Entry size", 10, readEntry.getSize());

            byte[] readBuf = new byte[10];
            int bytesRead = tin.read(readBuf);
            assertEquals("Read bytes count", 10, bytesRead);
            assertArrayEquals("Content mismatch", content, readBuf);

            int finalRead = tin.read(readBuf);
            assertEquals("Read past end should be -1", -1, finalRead);

            assertNull("No more entries", tin.getNextEntry());
        }
    }

    @Test
    public void testReadDirectory() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("mydir/");
        entry.setSize(0);
        byte[] tarBytes = createTarFromEntry(entry, null);

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            TarArchiveEntry dirEntry = tin.getNextEntry();
            assertNotNull(dirEntry);
            assertTrue(dirEntry.isDirectory());
            assertEquals(0, dirEntry.getSize());

            byte[] buf = new byte[10];
            assertEquals("Read from directory should be -1", -1, tin.read(buf));
        }
    }

    @Test
    public void testReadLongFileName() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(LONG_NAME);
        byte[] content = "data".getBytes();
        entry.setSize(content.length);
        byte[] tarBytes = createTarFromEntry(entry, content);

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            TarArchiveEntry longNameEntry = tin.getNextEntry();
            assertNotNull(longNameEntry);
            assertEquals("Long file name preserved", LONG_NAME, longNameEntry.getName());

            byte[] readBuf = new byte[content.length];
            assertEquals(content.length, tin.read(readBuf));
            assertArrayEquals(content, readBuf);
        }
    }

    @Test
    public void testReadLongLinkName() throws IOException {
        // Create a symbolic link entry with a very long link name
        TarArchiveEntry entry = new TarArchiveEntry("link", TarArchiveEntry.SYMBOLIC_LINK);
        entry.setLinkName(LONG_LINK_NAME);
        // The actual entry has zero size (symlinks have no data)
        entry.setSize(0);
        byte[] tarBytes = createTarFromEntry(entry, null);

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            TarArchiveEntry linkEntry = tin.getNextEntry();
            assertNotNull("Link entry should not be null", linkEntry);
            assertEquals("Link name should be preserved", LONG_LINK_NAME, linkEntry.getLinkName());
            assertTrue("Entry should be a symbolic link", linkEntry.isSymbolicLink());
            // Reading from a symlink should return -1
            byte[] buf = new byte[10];
            assertEquals(-1, tin.read(buf));
        }
    }

    @Test
    public void testSkipEntry() throws IOException {
        byte[] content = "abcdefghij".getBytes();
        TarArchiveEntry entry = new TarArchiveEntry("skip.txt");
        entry.setSize(content.length);
        byte[] tarBytes = createTarFromEntry(entry, content);

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            TarArchiveEntry skippedEntry = tin.getNextEntry();
            assertNotNull(skippedEntry);

            long skipped = tin.skip(5);
            assertEquals("Skip count", 5, skipped);

            byte[] readBuf = new byte[5];
            int read = tin.read(readBuf);
            assertEquals("Read remaining bytes", 5, read);
            assertArrayEquals("Remaining content", "fghij".getBytes(), readBuf);

            assertEquals("Next read should be -1", -1, tin.read(readBuf));
        }
    }

    @Test
    public void testReadPartial() throws IOException {
        byte[] content = "0123456789".getBytes();
        TarArchiveEntry entry = new TarArchiveEntry("partial.txt");
        entry.setSize(content.length);
        byte[] tarBytes = createTarFromEntry(entry, content);

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            tin.getNextEntry();
            byte[] buf = new byte[3];
            assertEquals(3, tin.read(buf));
            assertArrayEquals("012".getBytes(), buf);

            assertEquals(3, tin.read(buf));
            assertArrayEquals("345".getBytes(), buf);

            assertEquals(3, tin.read(buf));
            assertArrayEquals("678".getBytes(), buf);

            assertEquals(1, tin.read(buf));
            assertArrayEquals("9".getBytes(), new byte[]{buf[0]});

            assertEquals(-1, tin.read(buf));
        }
    }

    @Test
    public void testMultipleEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tos.setLongLinkMode(TarArchiveOutputStream.LONGFILE_GNU);

        TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        entry1.setSize(3);
        tos.putArchiveEntry(entry1);
        tos.write("AAA".getBytes());
        tos.closeArchiveEntry();

        TarArchiveEntry entry2 = new TarArchiveEntry("dir/");
        entry2.setSize(0);
        tos.putArchiveEntry(entry2);
        tos.closeArchiveEntry();

        TarArchiveEntry entry3 = new TarArchiveEntry("file2.txt");
        entry3.setSize(3);
        tos.putArchiveEntry(entry3);
        tos.write("BBB".getBytes());
        tos.closeArchiveEntry();

        tos.close();
        byte[] tarBytes = bos.toByteArray();

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            // First entry
            TarArchiveEntry e1 = tin.getNextEntry();
            assertNotNull(e1);
            assertEquals("file1.txt", e1.getName());
            assertFalse(e1.isDirectory());
            byte[] buf = new byte[3];
            assertEquals(3, tin.read(buf));
            assertArrayEquals("AAA".getBytes(), buf);

            // Second entry
            TarArchiveEntry e2 = tin.getNextEntry();
            assertNotNull(e2);
            assertEquals("dir/", e2.getName());
            assertTrue(e2.isDirectory());
            assertEquals(-1, tin.read(buf));

            // Third entry
            TarArchiveEntry e3 = tin.getNextEntry();
            assertNotNull(e3);
            assertEquals("file2.txt", e3.getName());
            assertEquals(3, tin.read(buf));
            assertArrayEquals("BBB".getBytes(), buf);

            // No more entries
            assertNull(tin.getNextEntry());
        }
    }

    @Test
    public void testEOFAfterLastEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("last.txt");
        entry.setSize(1);
        byte[] tarBytes = createTarFromEntry(entry, "X".getBytes());

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            assertNotNull(tin.getNextEntry());
            byte[] buf = new byte[1];
            assertEquals(1, tin.read(buf));
            assertEquals(-1, tin.read(buf));
            assertNull(tin.getNextEntry());
        }
    }

    @Test
    public void testMarkSupported() {
        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertFalse("mark no supported", tin.markSupported());
        } catch (IOException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullInputStream() throws IOException {
        new TarArchiveInputStream(null);
    }

    @Test
    public void testBlockingFactor() throws IOException {
        // Create a simple tar and read with custom blocking factor
        TarArchiveEntry entry = new TarArchiveEntry("small.txt");
        entry.setSize(2);
        byte[] content = "AB".getBytes();
        byte[] tarBytes = createTarFromEntry(entry, content);

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes), 20)) {
            TarArchiveEntry e = tin.getNextEntry();
            assertNotNull(e);
            byte[] buf = new byte[2];
            assertEquals(2, tin.read(buf));
            assertArrayEquals(content, buf);
        }
    }

    @Test
    public void testReadZeroLengthEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        entry.setSize(0);
        byte[] tarBytes = createTarFromEntry(entry, null);

        try (TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(tarBytes))) {
            TarArchiveEntry e = tin.getNextEntry();
            assertNotNull(e);
            assertEquals(0, e.getSize());
            byte[] buf = new byte[1];
            assertEquals("Read from zero-size entry should return -1", -1, tin.read(buf));
        }
    }
}