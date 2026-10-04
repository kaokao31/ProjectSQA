package org.apache.commons.compress.archivers.cpio;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CpioArchiveOutputStreamTest {

    @Test
    public void testDefaultConstructor() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3, 4});
        out.closeArchiveEntry();
        out.close();

        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(result));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("test.txt", readEntry.getName());
        assertEquals(4, readEntry.getSize());
        byte[] content = new byte[4];
        int read = in.read(content, 0, 4);
        assertEquals(4, read);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, content);
        in.close();
    }

    @Test
    public void testFormatNewCrcRoundtrip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        byte[] data = "Hello CPIO CRC World!".getBytes("UTF-8");
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_entry.txt", data.length);
        out.putArchiveEntry(entry);
        out.write(data);
        out.closeArchiveEntry();
        out.close();

        byte[] result = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(result));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("crc_entry.txt", readEntry.getName());
        assertEquals(data.length, readEntry.getSize());
        byte[] content = new byte[data.length];
        int read = in.read(content, 0, data.length);
        assertEquals(data.length, read);
        assertArrayEquals(data, content);
        in.close();
    }

    @Test
    public void testFormatOldAsciiRoundtrip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        byte[] data = "Old ASCII Format Data".getBytes("UTF-8");
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "old_ascii.txt", data.length);
        out.putArchiveEntry(entry);
        out.write(data);
        out.closeArchiveEntry();
        out.close();

        byte[] result = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(result));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("old_ascii.txt", readEntry.getName());
        assertEquals(data.length, readEntry.getSize());
        byte[] content = new byte[data.length];
        int read = in.read(content, 0, data.length);
        assertEquals(data.length, read);
        assertArrayEquals(data, content);
        in.close();
    }

    @Test
    public void testFormatOldBinaryRoundtrip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        byte[] data = new byte[]{10, 20, 30, 40, 50};
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "binary.bin", data.length);
        out.putArchiveEntry(entry);
        out.write(data);
        out.closeArchiveEntry();
        out.close();

        byte[] result = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(result));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("binary.bin", readEntry.getName());
        assertEquals(data.length, readEntry.getSize());
        byte[] content = new byte[data.length];
        int read = in.read(content, 0, data.length);
        assertEquals(data.length, read);
        assertArrayEquals(data, content);
        in.close();
    }

    @Test
    public void testMultipleEntriesAndPadding() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        for (int i = 1; i <= 5; i++) {
            byte[] data = new byte[i];
            for (int j = 0; j < i; j++) {
                data[j] = (byte) (i * 10 + j);
            }
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file_" + i + ".dat", i);
            out.putArchiveEntry(entry);
            out.write(data);
            out.closeArchiveEntry();
        }
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        for (int i = 1; i <= 5; i++) {
            CpioArchiveEntry entry = (CpioArchiveEntry) in.getNextEntry();
            assertNotNull(entry);
            assertEquals("file_" + i + ".dat", entry.getName());
            assertEquals(i, entry.getSize());
            byte[] buf = new byte[i];
            int read = in.read(buf, 0, i);
            assertEquals(i, read);
            for (int j = 0; j < i; j++) {
                assertEquals((byte) (i * 10 + j), buf[j]);
            }
        }
        in.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormatInConstructor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(baos, (short) 999);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBlockSizeInConstructor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBlockSizeInConstructor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, -512);
    }

    @Test(expected = IOException.class)
    public void testWriteTooMuchDataThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "toosmall.txt", 2);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3});
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithInsufficientDataThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "toolarge.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2});
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryWhilePreviousStillOpenThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "entry1.txt", 0);
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "entry2.txt", 0);
        out.putArchiveEntry(entry1);
        out.putArchiveEntry(entry2);
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryAfterFinishedThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "after_finish.txt", 0);
        out.putArchiveEntry(entry);
    }

    @Test(expected = IOException.class)
    public void testFinishWithOpenEntryThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "open.txt", 10);
        out.putArchiveEntry(entry);
        out.finish();
    }

    @Test(expected = IOException.class)
    public void testWriteAfterCloseThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.close();
        out.write(new byte[]{1, 2, 3});
    }

    @Test(expected = IOException.class)
    public void testWriteSingleByteAfterCloseThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.close();
        out.write(42);
    }

    @Test(expected = IOException.class)
    public void testMismatchedEntryFormatThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "mismatch.txt", 0);
        out.putArchiveEntry(entry);
    }

    @Test(expected = ClassCastException.class)
    public void testNonCpioEntryThrowsException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        ArchiveEntry foreignEntry = new ArchiveEntry() {
            @Override
            public String getName() {
                return "foreign";
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public java.util.Date getLastModifiedDate() {
                return new java.util.Date();
            }
        };
        out.putArchiveEntry(foreignEntry);
    }

    @Test
    public void testWriteSingleByte() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "byte.txt", 1);
        out.putArchiveEntry(entry);
        out.write(65);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals(65, in.read());
        in.close();
    }

    @Test
    public void testWriteOffsetAndLength() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "offset.txt", 3);
        out.putArchiveEntry(entry);
        byte[] buffer = new byte[]{0, 10, 20, 30, 0};
        out.write(buffer, 1, 3);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        byte[] readBuf = new byte[3];
        in.read(readBuf, 0, 3);
        assertArrayEquals(new byte[]{10, 20, 30}, readBuf);
        in.close();
    }

    @Test
    public void testCustomBlockSizePadding() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int blockSize = 1024;
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, blockSize);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();

        byte[] result = baos.toByteArray();
        assertEquals(0, result.length % blockSize);
    }

    @Test
    public void testCreateArchiveEntryFromFile() throws Exception {
        File tempFile = File.createTempFile("cpio_test", ".tmp");
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        fos.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        ArchiveEntry entry = out.createArchiveEntry(tempFile, "archive_entry_name.bin");
        assertNotNull(entry);
        assertEquals("archive_entry_name.bin", entry.getName());
        assertEquals(8, entry.getSize());

        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        out.closeArchiveEntry();
        out.close();

        tempFile.delete();
    }

    @Test
    public void testCustomEncodingConstructor() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW, 512, "UTF-8");
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "utf8_entry_\u00E4\u00F6\u00FC.txt", 0);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "UTF-8");
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("utf8_entry_\u00E4\u00F6\u00FC.txt", readEntry.getName());
        in.close();
    }

    @Test
    public void testMultipleCloseCallsAreIdempotent() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.close();
        out.close();
    }

    @Test
    public void testMultipleFinishCallsAreIdempotent() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.finish();
        out.close();
    }

    @Test
    public void testFormatOldBinaryOddNamePadding() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "a", 3);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3});
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("a", readEntry.getName());
        assertEquals(3, readEntry.getSize());
        byte[] buf = new byte[3];
        in.read(buf);
        assertArrayEquals(new byte[]{1, 2, 3}, buf);
        in.close();
    }

    @Test(expected = IOException.class)
    public void testInvalidWriteBoundsNegativeOffset() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "bounds.txt", 10);
        out.putArchiveEntry(entry);
        try {
            out.write(new byte[10], -1, 5);
        } finally {
            out.close();
        }
    }

    @Test(expected = IOException.class)
    public void testInvalidWriteBoundsNegativeLength() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "bounds.txt", 10);
        out.putArchiveEntry(entry);
        try {
            out.write(new byte[10], 0, -1);
        } finally {
            out.close();
        }
    }

    @Test(expected = IOException.class)
    public void testInvalidWriteBoundsExceedArray() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "bounds.txt", 10);
        out.putArchiveEntry(entry);
        try {
            out.write(new byte[10], 5, 6);
        } finally {
            out.close();
        }
    }
}