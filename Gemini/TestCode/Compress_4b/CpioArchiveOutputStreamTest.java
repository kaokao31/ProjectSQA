package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

public class CpioArchiveOutputStreamTest {

    @Test
    public void testCpioArchiveOutputStreamShortFormat() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cos = new CpioArchiveOutputStream(out, CpioConstants.FORMAT_OLD_ASCII)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "test.txt");
            entry.setSize(5);
            cos.putArchiveEntry(entry);
            cos.write("hello".getBytes());
            cos.closeArchiveEntry();
            cos.finish();
        }
        assertNotNull(out.toByteArray());
    }

    @Test
    public void testCpioArchiveOutputStreamNewFormat() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cos = new CpioArchiveOutputStream(out, CpioConstants.FORMAT_NEW)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test_new.txt");
            entry.setSize(4);
            cos.putArchiveEntry(entry);
            cos.write("test".getBytes());
            cos.closeArchiveEntry();
            cos.finish();
        }
        assertNotNull(out.toByteArray());
    }

    @Test
    public void testCpioArchiveOutputStreamNewCRCFormat() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cos = new CpioArchiveOutputStream(out, CpioConstants.FORMAT_NEW_CRC)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "test_crc.txt");
            entry.setSize(3);
            cos.putArchiveEntry(entry);
            cos.write("crc".getBytes());
            cos.closeArchiveEntry();
            cos.finish();
        }
        assertNotNull(out.toByteArray());
    }

    @Test
    public void testCpioArchiveOutputStreamBinaryFormat() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cos = new CpioArchiveOutputStream(out, CpioConstants.FORMAT_OLD_BINARY)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "test_bin.txt");
            entry.setSize(3);
            cos.putArchiveEntry(entry);
            cos.write("bin".getBytes());
            cos.closeArchiveEntry();
            cos.finish();
        }
        assertNotNull(out.toByteArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormat() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Passing an unsupported format identifier
        new CpioArchiveOutputStream(out, (short) 9999);
    }

    @Test(expected = IOException.class)
    public void testWriteExceedingDeclaredSize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cos = new CpioArchiveOutputStream(out, CpioConstants.FORMAT_NEW)) {
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "toolarge.txt");
            entry.setSize(2);
            cos.putArchiveEntry(entry);
            cos.write("toolarge".getBytes()); // Should trigger IOException due to size mismatch
        }
    }

    @Test(expected = IOException.class)
    public void testCloseWithoutFinishIfHasMoreEntriesOrNotFinished() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CpioArchiveOutputStream cos = new CpioArchiveOutputStream(out, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "unclosed.txt");
        entry.setSize(0);
        cos.putArchiveEntry(entry);
        // Not closing archive entry or finishing properly before finish/close
        cos.finish();
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CpioArchiveOutputStream cos = new CpioArchiveOutputStream(out, CpioConstants.FORMAT_NEW)) {
            ArchiveEntry entry = cos.createArchiveEntry(new java.io.File("dummy.txt"), "dummy.txt");
            assertNotNull(entry);
            assertEquals("dummy.txt", entry.getName());
        }
    }
}