package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CpioArchiveOutputStreamTest {

    private ByteArrayOutputStream baos;
    private CpioArchiveOutputStream out;

    @Before
    public void setUp() {
        baos = new ByteArrayOutputStream();
        out = new CpioArchiveOutputStream(baos);
    }

    @After
    public void tearDown() throws IOException {
        if (out != null) {
            out.close();
        }
    }

    // ---------------------------------------------------------------
    // Constructor and simple write tests
    // ---------------------------------------------------------------

    @Test
    public void testWriteSingleEntry() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("testfile.txt");
        entry.setSize(5);
        out.putArchiveEntry(entry);
        out.write("hello".getBytes());
        out.closeArchiveEntry();
        out.finish();
        byte[] written = baos.toByteArray();
        assertTrue(written.length > 0);
    }

    @Test
    public void testWriteEmptyEntry() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("empty");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        byte[] written = baos.toByteArray();
        assertTrue(written.length > 0);
    }

    @Test
    public void testWriteMultipleEntries() throws IOException {
        CpioArchiveEntry entry1 = new CpioArchiveEntry("file1.txt");
        entry1.setSize(2);
        out.putArchiveEntry(entry1);
        out.write("ab".getBytes());
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry("file2.txt");
        entry2.setSize(3);
        out.putArchiveEntry(entry2);
        out.write("cde".getBytes());
        out.closeArchiveEntry();
        out.finish();
        byte[] written = baos.toByteArray();
        assertTrue(written.length > 0);
    }

    @Test
    public void testWriteWithExactSize() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("exact");
        // Size matches exactly what we write
        entry.setSize(10);
        out.putArchiveEntry(entry);
        out.write("1234567890".getBytes());
        out.closeArchiveEntry();
        out.finish();
    }

    @Test
    public void testWriteLessThanDeclaredSize() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("smallwrite");
        entry.setSize(100);
        out.putArchiveEntry(entry);
        out.write("short".getBytes());
        // Expect exception when closing, or before closing? Usually closing will fail.
        assertThrows(IOException.class, () -> out.closeArchiveEntry());
    }

    @Test
    public void testWriteMoreThanDeclaredSize() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("oversize");
        entry.setSize(3);
        out.putArchiveEntry(entry);
        out.write("1234".getBytes());
        assertThrows(IOException.class, () -> out.closeArchiveEntry());
    }

    // ---------------------------------------------------------------
    // Format variants
    // ---------------------------------------------------------------

    @Test
    public void testNewFormat() throws IOException {
        out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry("newfmt.txt");
        entry.setSize(1);
        out.putArchiveEntry(entry);
        out.write("X".getBytes());
        out.closeArchiveEntry();
        out.finish();
        byte[] written = baos.toByteArray();
        assertTrue(written.length > 0);
    }

    @Test
    public void testOldASCIIFormat() throws IOException {
        out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry("oldascii.txt");
        entry.setSize(2);
        out.putArchiveEntry(entry);
        out.write("YZ".getBytes());
        out.closeArchiveEntry();
        out.finish();
    }

    @Test
    public void testOldBinaryFormat() throws IOException {
        out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry("oldbinary.file");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
    }

    @Test
    public void testNewCRCFormat() throws IOException {
        out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry("crcfile.doc");
        entry.setSize(4);
        out.putArchiveEntry(entry);
        out.write("data".getBytes());
        out.closeArchiveEntry();
        out.finish();
    }

    // ---------------------------------------------------------------
    // Entry properties (mode, uid, gid, etc.)
    // ---------------------------------------------------------------

    @Test
    public void testEntryWithModeAndIds() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("propfile.txt");
        entry.setSize(0);
        entry.setMode(0100644); // Regular file with permissions
        entry.setUID(1000);
        entry.setGID(500);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
    }

    @Test
    public void testEntryWithSymbolicLink() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("link", "target");
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
    }

    // ---------------------------------------------------------------
    // Exception handling and edge cases
    // ---------------------------------------------------------------

    @Test
    public void testPutEntryAfterClose() throws IOException {
        out.close();
        CpioArchiveEntry entry = new CpioArchiveEntry("late");
        assertThrows(IOException.class, () -> out.putArchiveEntry(entry));
    }

    @Test
    public void testWriteAfterClose() throws IOException {
        out.close();
        assertThrows(IOException.class, () -> out.write(new byte[1]));
    }

    @Test
    public void testCloseArchiveEntryWithoutPut() {
        assertThrows(IOException.class, () -> out.closeArchiveEntry());
    }

    @Test
    public void testFinishWithoutClosingEntry() throws IOException {
        out.putArchiveEntry(new CpioArchiveEntry("open"));
        out.write("some".getBytes());
        // Finish should throw an exception because entry is not closed
        assertThrows(IOException.class, () -> out.finish());
        // Clean up by closing entry then closing stream
        out.closeArchiveEntry();
    }

    @Test
    public void testFinishAfterFinish() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("done");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        // Second finish is allowed? Usually it should be idempotent or throw.
        // We assume it's allowed (some implementations do nothing).
        out.finish();
    }

    @Test
    public void testPutEntryWithNullName() {
        assertThrows(NullPointerException.class, () -> new CpioArchiveEntry(null));
    }

    @Test
    public void testWriteNullArray() throws IOException {
        out.putArchiveEntry(new CpioArchiveEntry("nulltest"));
        assertThrows(NullPointerException.class, () -> out.write(null, 0, 1));
        out.closeArchiveEntry();
    }

    @Test
    public void testWriteNegativeOffset() throws IOException {
        out.putArchiveEntry(new CpioArchiveEntry("negaoff"));
        assertThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[2], -1, 1));
        out.closeArchiveEntry();
    }

    @Test
    public void testWriteNegativeLength() throws IOException {
        out.putArchiveEntry(new CpioArchiveEntry("neglen"));
        assertThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 0, -1));
        out.closeArchiveEntry();
    }

    @Test
    public void testWriteOversizedLength() throws IOException {
        out.putArchiveEntry(new CpioArchiveEntry("overlen"));
        assertThrows(IndexOutOfBoundsException.class, () -> out.write(new byte[2], 0, 5));
        out.closeArchiveEntry();
    }

    // ---------------------------------------------------------------
    // Block size variants
    // ---------------------------------------------------------------

    @Test
    public void testConstructWithBlockSize() throws IOException {
        out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, 4096);
        CpioArchiveEntry entry = new CpioArchiveEntry("blocktest");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
    }

    @Test
    public void testConstructWithInvalidBlockSize() {
        // Negative block size is invalid (will be ignored or cause exception?)
        assertThrows(IllegalArgumentException.class,
                () -> new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, -1));
    }

    // ---------------------------------------------------------------
    // Read back and verify (simple round-trip)
    // ---------------------------------------------------------------

    @Test
    public void testRoundTripSimple() throws IOException {
        String expectedContent = "Hello CPIO!";
        String entryName = "greeting.txt";
        CpioArchiveEntry entry = new CpioArchiveEntry(entryName);
        entry.setSize(expectedContent.length());
        out.putArchiveEntry(entry);
        out.write(expectedContent.getBytes());
        out.closeArchiveEntry();
        out.finish();
        byte[] archiveBytes = baos.toByteArray();

        // Now read it back
        try (ByteArrayInputStream bais = new ByteArrayInputStream(archiveBytes);
             CpioArchiveInputStream in = new CpioArchiveInputStream(bais)) {
            CpioArchiveEntry readEntry = in.getNextEntry();
            assertNotNull(readEntry);
            assertEquals(entryName, readEntry.getName());
            assertEquals(expectedContent.length(), readEntry.getSize());
            byte[] readData = new byte[(int) readEntry.getSize()];
            int off = 0;
            while (off < readData.length) {
                int count = in.read(readData, off, readData.length - off);
                if (count == -1) break;
                off += count;
            }
            assertEquals(expectedContent, new String(readData));
        }
    }

    // ---------------------------------------------------------------
    // Test close without finishing (should work but check no exception)
    // ---------------------------------------------------------------

    @Test
    public void testCloseWithoutFinish() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("nofinish.txt");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        // close() should call finish() internally
        out.close();
        out = null; // prevent double close in @After
    }

    // ---------------------------------------------------------------
    // Bad format constants
    // ---------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormat() throws IOException {
        new CpioArchiveOutputStream(baos, 666);
    }

    // ---------------------------------------------------------------
    // Entry with very long name (boundary)
    // ---------------------------------------------------------------

    @Test
    public void testLongEntryName() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("abcdefghij");
        }
        String longName = sb.toString();
        CpioArchiveEntry entry = new CpioArchiveEntry(longName);
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        // Should not throw; implementation may handle long names via name length field.
    }

    // ---------------------------------------------------------------
    // Format-specific edge case: old binary max filesize?
    // Not feasible to test with huge data, but we can test header size limits?
    // ---------------------------------------------------------------

    @Test
    public void testOldBinaryLargeFilesize() throws IOException {
        out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        // According to standard, max size is 2^31-1? Actually old binary uses 32-bit unsigned.
        CpioArchiveEntry entry = new CpioArchiveEntry("huge");
        entry.setSize(0xFFFFFFFFL); // Max 32-bit unsigned
        // Just checking that the entry can be written (no actual data)
        out.putArchiveEntry(entry);
        long size = entry.getSize();
        assertEquals(0xFFFFFFFFL, size);
        out.closeArchiveEntry();
        out.finish();
    }

    @Test
    public void testNewFormatLargeFilesize() throws IOException {
        out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        // New format uses 32-bit size as well, but also 64-bit in some variants?
        // We'll use a large positive number.
        CpioArchiveEntry entry = new CpioArchiveEntry("largenew");
        entry.setSize(0x7FFFFFFFL);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
    }

    // ---------------------------------------------------------------
    // Test that flush and close are safe
    // ---------------------------------------------------------------

    @Test
    public void testFlush() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("flushtest");
        entry.setSize(3);
        out.putArchiveEntry(entry);
        out.write("abc".getBytes());
        out.flush(); // Some streams support flush during entry
        out.closeArchiveEntry();
        out.finish();
    }

    // ---------------------------------------------------------------
    // Test multiple closeArchiveEntry in a row (should fail)
    // ---------------------------------------------------------------

    @Test
    public void testDoubleCloseArchiveEntry() throws IOException {
        CpioArchiveEntry entry = new CpioArchiveEntry("doubleclose");
        entry.setSize(0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        assertThrows(IOException.class, () -> out.closeArchiveEntry());
    }
}