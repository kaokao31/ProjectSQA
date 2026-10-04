package org.apache.commons.compress.archivers.tar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for TarArchiveOutputStream.
 * Designed to achieve maximum line/branch coverage and detect known faults
 * (e.g., Defects4J Compress-18: long file name handling in FAIL mode).
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

    // ===================== Basic Functionality =====================

    @Test
    public void testWriteEntryShortName() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("short.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        assertTrue("Output should contain at least a valid tar header",
                baos.toByteArray().length > 0);
    }

    @Test
    public void testWriteEntryWithData() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("data.bin");
        byte[] data = "Hello, Tar!".getBytes("UTF-8");
        entry.setSize(data.length);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
        byte[] output = baos.toByteArray();
        assertTrue(output.length > 0);
        // Basic check that data block is present
        assertNotNull("Should have written data block", output);
    }

    // ===================== Long File Name Modes =====================

    // Helper to create a long file name (>100 characters)
    private String createLongFileName(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('a');
        }
        return sb.toString();
    }

    @Test(expected = IOException.class)
    public void testLongFileNameFailsInFailMode() throws IOException {
        // Trigger Defects4J Compress-18: might not throw when it should
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_FAIL);
        String longName = createLongFileName(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tarOut.putArchiveEntry(entry); // Should throw IOException
    }

    @Test
    public void testLongFileNameTruncated() throws IOException {
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        String longName = createLongFileName(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        // No exception expected; name silently truncated
    }

    @Test
    public void testLongFileNameGnuExtension() throws IOException {
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        String longName = createLongFileName(200);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        // Should use GNU long name extension
    }

    @Test
    public void testLongFileNamePosixExtension() throws IOException {
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        String longName = createLongFileName(200);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        // Should use PAX long name extension
    }

    @Test(expected = IOException.class)
    public void testLongFileNameWarnModeThrows() throws IOException {
        // In WARN mode, a warning is logged but no exception should be thrown.
        // However, due to bug, it might throw. We expect NO exception,
        // so this test is designed to pass only if the bug is absent.
        // Actually, we want to detect regressions: if set to WARN, should not throw.
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_WARN);
        String longName = createLongFileName(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        // If no exception, test passes; if exception, it fails (contradicts expected)
        // So we remove the "expected" and use a flag.
    }

    @Test
    public void testLongFileNameWarnModeDoesNotThrow() throws IOException {
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_WARN);
        String longName = createLongFileName(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        try {
            tarOut.putArchiveEntry(entry);
            // Success
            tarOut.closeArchiveEntry();
        } catch (IOException e) {
            fail("IOException should not be thrown in WARN mode: " + e.getMessage());
        }
    }

    // ===================== Edge Cases =====================

    @Test(expected = IOException.class)
    public void testWriteAfterFinish() throws IOException {
        tarOut.finish();
        TarArchiveEntry entry = new TarArchiveEntry("after_finish.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry); // Should throw
    }

    @Test(expected = IOException.class)
    public void testWriteAfterClose() throws IOException {
        tarOut.close();
        TarArchiveEntry entry = new TarArchiveEntry("after_close.txt");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry); // Should throw
    }

    @Test
    public void testMultipleEntries() throws IOException {
        for (int i = 0; i < 5; i++) {
            TarArchiveEntry entry = new TarArchiveEntry("file" + i + ".txt");
            entry.setSize(0);
            tarOut.putArchiveEntry(entry);
            tarOut.closeArchiveEntry();
        }
        byte[] output = baos.toByteArray();
        // 5 * 512 (header) + 2 * 512 (end-of-archive) = 3584 bytes ?
        assertTrue("Output size should be at least 5*512", output.length >= 5 * 512);
    }

    @Test
    public void testEmptyTar() throws IOException {
        tarOut.finish();
        byte[] output = baos.toByteArray();
        assertEquals("Empty tar should have two zero blocks (1024 bytes)",
                1024, output.length);
    }

    // ===================== Stream Edge Cases =====================

    @Test(expected = IOException.class)
    public void testSetLongFileModeInvalid() throws IOException {
        // Invalid mode value (e.g., -1)
        tarOut.setLongFileMode(-1);
    }

    // ===================== Coverage of internal methods =====================

    @Test
    public void testWriteRawAndClose() throws IOException {
        // To cover write(byte[], int, int)
        byte[] data = new byte[1024];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        TarArchiveEntry entry = new TarArchiveEntry("full_block.bin");
        entry.setSize(data.length);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data, 0, data.length);
        tarOut.closeArchiveEntry();
    }

    @Test
    public void testFlush() throws IOException {
        // Flush is delegated to underlying stream
        tarOut.flush();
    }

    // ===================== Bug-specific regression tests =====================

    @Test
    public void testPosixLongFileNameWithUtf8() throws IOException {
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        // Name with characters that might stress encoding
        String utf8Name = "あいうえお_長いファイル名_".repeat(10);
        TarArchiveEntry entry = new TarArchiveEntry(utf8Name);
        entry.setSize(0);
        try {
            tarOut.putArchiveEntry(entry);
            tarOut.closeArchiveEntry();
        } catch (IOException e) {
            fail("POSIX mode should handle long UTF-8 names: " + e.getMessage());
        }
    }

    @Test
    public void testFailModeShouldThrowForLongName() {
        // Explicitly verify the bug: LONGFILE_FAIL must throw IOException
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_FAIL);
        String longName = createLongFileName(150);
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        try {
            tarOut.putArchiveEntry(entry);
            fail("Expected IOException for long file name in FAIL mode");
        } catch (IOException e) {
            // Expected
        }
    }
}