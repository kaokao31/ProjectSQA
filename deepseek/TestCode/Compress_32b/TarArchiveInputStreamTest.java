package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class TarArchiveInputStreamTest {

    private static final String LONG_NAME = "this/is/a/very/long/path/that/exceeds/the/typical/100/character/limit/for/tar/entries/and/should/be/handled/by/gnu/long/name/extension/1234567890";
    private static final String LONG_LINK_NAME = "this/is/a/very/long/symlink/target/that/also/exceeds/the/100/character/limit/for/tar/link/names/and/should/be/handled/by/gnu/long/link/extension/abcdefghij";

    private ByteArrayOutputStream baos;
    private TarArchiveOutputStream tos;

    @Before
    public void setUp() throws Exception {
        baos = new ByteArrayOutputStream();
        tos = new TarArchiveOutputStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
    }

    @Test
    public void testNormalEntry() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("normal.txt");
        entry.setSize(5);
        tos.putArchiveEntry(entry);
        tos.write("hello".getBytes());
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("normal.txt", readEntry.getName());
        byte[] content = new byte[5];
        IOUtils.readFully(tis, content);
        assertEquals("hello", new String(content));
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test
    public void testLongFileName() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry(LONG_NAME);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(LONG_NAME, readEntry.getName());
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test
    public void testLongLinkName() throws Exception {
        // Create a symbolic link entry with a long link name
        TarArchiveEntry entry = new TarArchiveEntry(LONG_NAME, TarArchiveConstants.LF_SYMLINK);
        entry.setLinkName(LONG_LINK_NAME);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(LONG_NAME, readEntry.getName());
        assertEquals(LONG_LINK_NAME, readEntry.getLinkName());
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test
    public void testDirectoryEntry() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("somedir/");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);
        assertTrue(readEntry.isDirectory());
        assertEquals("somedir/", readEntry.getName());
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test
    public void testReadAndSkip() throws Exception {
        // Create an entry with some data
        TarArchiveEntry entry = new TarArchiveEntry("data.bin");
        byte[] data = new byte[1024];
        new Random(0).nextBytes(data);
        entry.setSize(data.length);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("data.bin", readEntry.getName());
        assertEquals(data.length, readEntry.getSize());

        // Read first 512 bytes
        byte[] buf = new byte[512];
        int bytesRead = tis.read(buf);
        assertEquals(512, bytesRead);
        byte[] expectedFirst512 = new byte[512];
        System.arraycopy(data, 0, expectedFirst512, 0, 512);
        assertArrayEquals(expectedFirst512, buf);

        // Skip remaining 512 bytes
        long skipped = tis.skip(512);
        assertEquals(512, skipped);

        // Should be at end of entry
        assertEquals(-1, tis.read());
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test
    public void testEmptyArchive() throws Exception {
        tos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test
    public void testMultipleEntries() throws Exception {
        TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        entry1.setSize(4);
        tos.putArchiveEntry(entry1);
        tos.write("one ".getBytes());
        tos.closeArchiveEntry();

        TarArchiveEntry entry2 = new TarArchiveEntry("file2.txt");
        entry2.setSize(4);
        tos.putArchiveEntry(entry2);
        tos.write("two ".getBytes());
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry e1 = tis.getNextTarEntry();
        assertNotNull(e1);
        assertEquals("file1.txt", e1.getName());
        byte[] content1 = new byte[4];
        IOUtils.readFully(tis, content1);
        assertEquals("one ", new String(content1));

        TarArchiveEntry e2 = tis.getNextTarEntry();
        assertNotNull(e2);
        assertEquals("file2.txt", e2.getName());
        byte[] content2 = new byte[4];
        IOUtils.readFully(tis, content2);
        assertEquals("two ", new String(content2));

        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test(expected = IOException.class)
    public void testCorruptArchive() throws Exception {
        byte[] corruptData = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        ByteArrayInputStream bais = new ByteArrayInputStream(corruptData);
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        tis.getNextTarEntry(); // Should throw IOException
    }

    @Test
    public void testGnuLongNameAndLinkNameCombined() throws Exception {
        // Create a file with long name and a symlink with long link name
        TarArchiveEntry fileEntry = new TarArchiveEntry(LONG_NAME);
        fileEntry.setSize(0);
        tos.putArchiveEntry(fileEntry);
        tos.closeArchiveEntry();

        TarArchiveEntry linkEntry = new TarArchiveEntry("shortlink", TarArchiveConstants.LF_SYMLINK);
        linkEntry.setLinkName(LONG_LINK_NAME);
        tos.putArchiveEntry(linkEntry);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bais);
        TarArchiveEntry e1 = tis.getNextTarEntry();
        assertNotNull(e1);
        assertEquals(LONG_NAME, e1.getName());

        TarArchiveEntry e2 = tis.getNextTarEntry();
        assertNotNull(e2);
        assertEquals("shortlink", e2.getName());
        assertEquals(LONG_LINK_NAME, e2.getLinkName());

        assertNull(tis.getNextTarEntry());
        tis.close();
    }
}