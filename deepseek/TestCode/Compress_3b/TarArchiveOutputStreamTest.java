package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Comprehensive JUnit 4 test suite for TarArchiveOutputStream.
 * Covers normal operations, edge cases, boundary values, and potential fault triggers.
 */
public class TarArchiveOutputStreamTest {

    private ByteArrayOutputStream baos;
    private TarArchiveOutputStream tarOut;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
        tarOut = new TarArchiveOutputStream(baos);
    }

    @After
    public void tearDown() throws IOException {
        if (tarOut != null) {
            tarOut.close();
        }
    }

    // -----------------------------------------------------------------------
    //  Basic entry creation and writing
    // -----------------------------------------------------------------------

    @Test
    public void testPutAndCloseSimpleEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(5);
        tarOut.putArchiveEntry(entry);
        tarOut.write("hello".getBytes());
        tarOut.closeArchiveEntry();
        // The stream should be in a valid state to finish
        tarOut.finish();
        byte[] result = baos.toByteArray();
        assertTrue("Resulting tar should contain data", result.length > 0);
    }

    @Test
    public void testWriteAfterCloseEntryThrows() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(3);
        tarOut.putArchiveEntry(entry);
        tarOut.write("abc".getBytes());
        tarOut.closeArchiveEntry();
        // Trying to write without an open entry should throw IOException
        try {
            tarOut.write(1);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testPutMultipleEntries() throws IOException {
        for (int i = 0; i < 10; i++) {
            TarArchiveEntry entry = new TarArchiveEntry("file" + i + ".txt");
            entry.setSize(1);
            tarOut.putArchiveEntry(entry);
            tarOut.write((byte) ('0' + i));
            tarOut.closeArchiveEntry();
        }
        tarOut.finish();
        byte[] data = baos.toByteArray();
        assertTrue("Size should be > 0", data.length > 0);
    }

    // -----------------------------------------------------------------------
    //  Edge cases: empty entries, zero size, null names
    // -----------------------------------------------------------------------

    @Test(expected = IOException.class)
    public void testPutNullEntryThrows() throws IOException {
        tarOut.putArchiveEntry(null);
    }

    @Test
    public void testEntryWithEmptyName() throws IOException {
        // Some implementations disallow empty name; expect exception
        try {
            TarArchiveEntry entry = new TarArchiveEntry("");
            entry.setSize(0);
            tarOut.putArchiveEntry(entry);
            // Might or might not throw depending on implementation
            tarOut.closeArchiveEntry();
        } catch (IllegalArgumentException | IOException e) {
            // Acceptable
        }
    }

    @Test
    public void testZeroSizedEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        // No write needed
        tarOut.closeArchiveEntry();
        tarOut.finish();
        byte[] data = baos.toByteArray();
        assertTrue("Result should contain at least the header", data.length > 0);
    }

    @Test
    public void testEntryWithNegativeSize() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("negative.txt");
        // Setting size to a negative value - may be ignored or cause issues
        try {
            entry.setSize(-1);
        } catch (IllegalArgumentException e) {
            // Implementation may reject negative size
            return;
        }
        // If size is set to -1, header may encode it incorrectly
        // This test may expose buggy handling
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[0]);
        tarOut.closeArchiveEntry();
    }

    // -----------------------------------------------------------------------
    //  Long name handling (GNU/PAX extensions) 
    // -----------------------------------------------------------------------

    @Test
    public void testEntryWithLongNameOver100Chars() throws IOException {
        // Create a name exceeding 100 characters
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            sb.append('a');
        }
        String longName = sb.toString();
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(5);
        tarOut.putArchiveEntry(entry);
        tarOut.write("hello".getBytes());
        tarOut.closeArchiveEntry();
        // This should not throw, and the name should be stored in a PAX header or long link entry
        tarOut.finish();
        byte[] data = baos.toByteArray();
        // Verify the entry exists by recreating
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(data));
        TarArchiveEntry readEntry = tarIn.getNextTarEntry();
        assertNotNull("Entry should exist", readEntry);
        assertEquals("Long name should be preserved", longName, readEntry.getName());
        tarIn.close();
    }

    @Test
    public void testEntryWithNameOver155Chars() throws IOException {
        // Name > 155 requires both GNU long name and PAX header
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('b');
        }
        String longName = sb.toString();
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(1);
        tarOut.putArchiveEntry(entry);
        tarOut.write((byte) 'x');
        tarOut.closeArchiveEntry();
        tarOut.finish();
        // Read back and verify name
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tarIn.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(longName, readEntry.getName());
        tarIn.close();
    }

    // -----------------------------------------------------------------------
    //  Symbolic link and other entry types
    // -----------------------------------------------------------------------

    @Test
    public void testSymbolicLinkEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("link", TarArchiveEntry.LF_SYMLINK);
        entry.setLinkName("target");
        // For symlinks, size is usually 0
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        // Verify by reading
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(TarArchiveEntry.LF_SYMLINK, readEntry.getLinkFlag());
        assertEquals("target", readEntry.getLinkName());
        tin.close();
    }

    @Test
    public void testDirectoryEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("mydir/");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
    }

    // -----------------------------------------------------------------------
    //  Handling of file modification times
    // -----------------------------------------------------------------------

    @Test
    public void testSetAndGetModTime() throws IOException {
        Date modTime = new Date(123456789000L); // Some fixed date
        TarArchiveEntry entry = new TarArchiveEntry("time.txt");
        entry.setModTime(modTime);
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(modTime.getTime(), readEntry.getLastModifiedDate().getTime());
        tin.close();
    }

    // -----------------------------------------------------------------------
    //  Edge cases with writing more data than declared size
    // -----------------------------------------------------------------------

    @Test(expected = IOException.class)
    public void testWriteExceedsSizeThrows() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("limited.txt");
        entry.setSize(5);
        tarOut.putArchiveEntry(entry);
        // Write 10 bytes, which is >5 => should cause IOException
        tarOut.write("more than five bytes".getBytes());
        // flush?}
    }

    // -----------------------------------------------------------------------
    //  Finish without close or with pending entry
    // -----------------------------------------------------------------------

    @Test(expected = IOException.class)
    public void testFinishWithUpenEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("open.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        // Not closed
        tarOut.finish();
    }

    @Test
    public void testDoubleFinish() throws IOException {
        tarOut.finish();
        // Second finish should be no-op and not throw
        tarOut.finish();
    }

    @Test(expected = IOException.class)
    public void testWriteAfterFinish() throws IOException {
        tarOut.finish();
        tarOut.write(1);
    }

    @Test(expected = IOException.class)
    public void testPutEntryAfterFinish() throws IOException {
        tarOut.finish();
        TarArchiveEntry entry = new TarArchiveEntry("after.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
    }

    // -----------------------------------------------------------------------
    //  Large data (using ByteArrayOutputStream with appropriate size)
    // -----------------------------------------------------------------------

    @Test
    public void testLargeFileEntry() throws IOException {
        int size = 10 * 1024; // 10KB
        byte[] data = new byte[size];
        new Random().nextBytes(data);
        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        entry.setSize(size);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        // Verify data integrity
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tin = new TarArchiveInputStream(bais);
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertEquals(size, readEntry.getSize());
        byte[] readData = new byte[size];
        tin.read(readData);
        assertArrayEquals(data, readData);
        tin.close();
    }

    // -----------------------------------------------------------------------
    //  Close before finish
    // -----------------------------------------------------------------------

    @Test
    public void testCloseWithoutFinish() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();
        // Verify that the underlying stream is closed and data written
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    // -----------------------------------------------------------------------
    //  EOF marker consistency
    // -----------------------------------------------------------------------

    @Test
    public void testTwoEmptyRecordsAtEnd() throws IOException {
        // Finished tar should have two 512-byte zero blocks at end
        tarOut.finish();
        byte[] data = baos.toByteArray();
        assertTrue("Should end with at least 1024 zero bytes", data.length >= 1024);
        int len = data.length;
        // Last 1024 bytes zero
        for (int i = len - 1024; i < len; i++) {
            assertEquals(0, data[i]);
        }
    }

    // -----------------------------------------------------------------------
    //  Unusual character in file names (unicode, spaces)
    // -----------------------------------------------------------------------

    @Test
    public void testEntryWithSpecialCharacters() throws IOException {
        String name = "file with spaces and üñicøde.txt";
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(3);
        tarOut.putArchiveEntry(entry);
        tarOut.write("abc".getBytes());
        tarOut.closeArchiveEntry();
        tarOut.finish();
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(name, readEntry.getName());
        tin.close();
    }

    // -----------------------------------------------------------------------
    //  Multiple closeArchiveEntry calls (should throw)
    // -----------------------------------------------------------------------

    @Test(expected = IOException.class)
    public void testDoubleCloseArchiveEntry() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("double.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.closeArchiveEntry(); // Second close should throw
    }

    // -----------------------------------------------------------------------
    //  Put entry after stream closed
    // -----------------------------------------------------------------------

    @Test(expected = IOException.class)
    public void testPutEntryAfterClose() throws IOException {
        tarOut.close();
        TarArchiveEntry entry = new TarArchiveEntry("closed.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
    }

    // -----------------------------------------------------------------------
    //  Write zero bytes (empty content but set size)
    // -----------------------------------------------------------------------

    @Test
    public void testWriteZeroBytes() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("empty.bin");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[0]);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        // Should succeed; no content but header
    }

    // -----------------------------------------------------------------------
    //  Negative size from entry (setSize may reject)
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSetSizeNegative() {
        TarArchiveEntry entry = new TarArchiveEntry("neg.txt");
        entry.setSize(-10);
    }

    // -----------------------------------------------------------------------
    //  Using setBigNumberMode - if supported
    // -----------------------------------------------------------------------

    @Test
    public void testLargeSizeAndBigNumberMode() throws IOException {
        // Size > 8589934591 (0x1FFFFFFFF) requires STAR/GNU extensions
        // For testing we use a moderate large size ( > 8GB ) to trigger big number handling
        // but writing that much is impractical. We'll just set a large size and write nothing.
        // Many implementations will write a header with large number using base-256.
        tarOut.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        TarArchiveEntry entry = new TarArchiveEntry("largeSize.txt");
        entry.setSize(10L * 1024 * 1024 * 1024); // 10GB
        tarOut.putArchiveEntry(entry);
        // Writing zero bytes is acceptable if implementation allows
        tarOut.write(new byte[0]);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        // At least header should be written
        assertTrue(baos.toByteArray().length > 0);
    }

    // -----------------------------------------------------------------------
    //  Handling of long link names
    // -----------------------------------------------------------------------

    @Test
    public void testLongLinkName() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append('x');
        }
        String longLink = sb.toString();
        TarArchiveEntry entry = new TarArchiveEntry("linktest", TarArchiveEntry.LF_SYMLINK);
        entry.setLinkName(longLink);
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        // Verify
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(longLink, readEntry.getLinkName());
        tin.close();
    }

    // -----------------------------------------------------------------------
    //  User/group name settings
    // -----------------------------------------------------------------------

    @Test
    public void testUserNameAndGroupName() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("user.txt");
        entry.setUserName("testuser");
        entry.setGroupName("testgroup");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tin.getNextTarEntry();
        assertEquals("testuser", readEntry.getUserName());
        assertEquals("testgroup", readEntry.getGroupName());
        tin.close();
    }

    // -----------------------------------------------------------------------
    //  Writing partial buffer
    // -----------------------------------------------------------------------

    @Test
    public void testWritePartialBuffer() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("partial.txt");
        entry.setSize(10);
        tarOut.putArchiveEntry(entry);
        // Write only 3 bytes
        tarOut.write("hel".getBytes());
        tarOut.closeArchiveEntry();
        tarOut.finish();
        // Should succeed - implementation may pad remaining with zeros
    }
}