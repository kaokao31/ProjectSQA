package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import java.io.File;
import java.util.Map;

import static org.junit.Assert.*;

public class TarArchiveEntryTest {

    @Test
    public void testConstructorsAndBasicGetters() {
        TarArchiveEntry entry1 = new TarArchiveEntry("testName");
        assertEquals("testName", entry1.getName());
        assertEquals(0100755, entry1.getMode());
        assertEquals(0, entry1.getSize());

        TarArchiveEntry entry2 = new TarArchiveEntry("testName2", (byte) 'L');
        assertEquals("testName2", entry2.getName());
        assertEquals((byte) 'L', entry2.getLinkFlag());

        File dummyFile = new File("dummyFile.txt");
        TarArchiveEntry entry3 = new TarArchiveEntry(dummyFile);
        assertNotNull(entry3.getName());
        assertEquals(dummyFile.length(), entry3.getSize());

        byte[] headerBuf = new byte[512];
        TarArchiveEntry entry4 = new TarArchiveEntry(headerBuf);
        assertNotNull(entry4.getName());
    }

    @Test
    public void testFileConstructorWithRootAndRelativePaths() {
        // Test various file paths for file constructor
        File rootFile = new File("/");
        TarArchiveEntry entryRoot = new TarArchiveEntry(rootFile);
        assertNotNull(entryRoot.getName());

        File normalFile = new File("some/path/file.txt");
        TarArchiveEntry entryNormal = new TarArchiveEntry(normalFile);
        assertNotNull(entryNormal.getName());
    }

    @Test
    public void testSettersAndGetters() {
        TarArchiveEntry entry = new TarArchiveEntry("foo");
        
        entry.setName("bar");
        assertEquals("bar", entry.getName());

        entry.setMode(0777);
        assertEquals(0777, entry.getMode());

        entry.setUserId(123);
        assertEquals(123, entry.getUserId());

        entry.setGroupId(456);
        assertEquals(456, entry.getGroupId());

        entry.setUserName("user");
        assertEquals("user", entry.getUserName());

        entry.setGroupName("group");
        assertEquals("group", entry.getGroupName());

        entry.setSize(1024L);
        assertEquals(1024L, entry.getSize());

        entry.setModTime(10000L);
        assertEquals(10000L, entry.getModTime().getTime());

        entry.setModTime(new java.util.Date(20000L));
        assertEquals(20000L, entry.getModTime().getTime());

        byte[] devNums = {1, 2};
        // Just exercising dev constructors/setters if available
        entry.setDevMajor(10);
        assertEquals(10, entry.getDevMajor());

        entry.setDevMinor(20);
        assertEquals(20, entry.getDevMinor());
    }

    @Test
    public void testCheckPaxHeaders() {
        TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.fillPaxHeaders("path=newpath\nsize=500\n");
        Map<String, String> paxHeaders = entry.getExtraPaxHeaders();
        assertNotNull(paxHeaders);
        assertEquals("newpath", paxHeaders.get("path"));
        assertEquals("500", paxHeaders.get("size"));
    }

    @Test
    public void testDirectoryCheck() {
        TarArchiveEntry entry = new TarArchiveEntry("dir/", (byte) '5');
        assertTrue(entry.isDirectory());

        TarArchiveEntry entry2 = new TarArchiveEntry("file.txt", (byte) '0');
        assertFalse(entry2.isDirectory());

        entry2.setMode(040000); // S_IFDIR equivalent
        assertTrue(entry2.isDirectory());
    }

    @Test
    public void testEqualsAndHashCode() {
        TarArchiveEntry entry1 = new TarArchiveEntry("sameName");
        TarArchiveEntry entry2 = new TarArchiveEntry("sameName");
        TarArchiveEntry entry3 = new TarArchiveEntry("differentName");

        assertTrue(entry1.equals(entry2));
        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals("some string"));

        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    public void testGetDirectoryEntries() {
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        TarArchiveEntry[] subEntries = entry.getDirectoryEntries(tempDir);
        assertNotNull(subEntries);
    }

    @Test
    public void testWriteHeader() {
        byte[] outbuf = new byte[512];
        TarArchiveEntry entry = new TarArchiveEntry("testfile");
        entry.writeEntryHeader(outbuf);
        
        TarArchiveEntry entryRead = new TarArchiveEntry(outbuf);
        assertEquals("testfile", entryRead.getName());
    }
}