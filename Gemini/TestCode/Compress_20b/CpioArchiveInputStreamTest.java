package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class CpioArchiveInputStreamTest {

    @Test
    public void testMatches() {
        byte[] sigBytes = new byte[6];
        sigBytes[0] = (byte) 0x71;
        sigBytes[1] = (byte) 0xc7;
        assertEquals(true, CpioArchiveInputStream.matches(sigBytes, 6));

        sigBytes[0] = (byte) 0xc7;
        sigBytes[1] = (byte) 0x71;
        assertEquals(true, CpioArchiveInputStream.matches(sigBytes, 6));

        sigBytes[0] = (byte) 0x30;
        sigBytes[1] = (byte) 0x37;
        sigBytes[2] = (byte) 0x30;
        sigBytes[3] = (byte) 0x37;
        sigBytes[4] = (byte) 0x30;
        sigBytes[5] = (byte) 0x31;
        assertEquals(true, CpioArchiveInputStream.matches(sigBytes, 6));

        sigBytes[5] = (byte) 0x32;
        assertEquals(true, CpioArchiveInputStream.matches(sigBytes, 6));

        sigBytes[5] = (byte) 0x37;
        assertEquals(true, CpioArchiveInputStream.matches(sigBytes, 6));

        sigBytes[5] = (byte) 0x38;
        assertEquals(false, CpioArchiveInputStream.matches(sigBytes, 6));

        assertEquals(false, CpioArchiveInputStream.matches(new byte[2], 2));
    }

    @Test
    public void testCpioArchiveInputStreamConstructorWithDefaultEncoding() {
        byte[] dummy = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(dummy);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in)) {
            assertNotNull(cpio);
        } catch (IOException e) {
            fail("IOException not expected here");
        }
    }

    @Test
    public void testCpioArchiveInputStreamConstructorWithBlockSize() {
        byte[] dummy = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(dummy);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in, 512)) {
            assertNotNull(cpio);
        } catch (IOException e) {
            fail("IOException not expected here");
        }
    }

    @Test
    public void testCpioArchiveInputStreamConstructorWithEncoding() {
        byte[] dummy = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(dummy);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in, "US-ASCII")) {
            assertNotNull(cpio);
        } catch (IOException e) {
            fail("IOException not expected here");
        }
    }

    @Test
    public void testAvailableWithNoEntry() {
        byte[] dummy = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(dummy);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in)) {
            assertEquals(0, cpio.available());
        } catch (IOException e) {
            fail("IOException not expected");
        }
    }

    @Test
    public void testReadWithNullBuffer() {
        byte[] dummy = new byte[10];
        ByteArrayInputStream in = new ByteArrayInputStream(dummy);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in)) {
            cpio.read(null, 0, 10);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testReadWithInvalidOffsetAndLength() {
        byte[] dummy = new byte[10];
        ByteArrayInputStream in = new ByteArrayInputStream(dummy);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in)) {
            cpio.read(new byte[10], -1, 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testReadZeroLength() {
        byte[] dummy = new byte[10];
        ByteArrayInputStream in = new ByteArrayInputStream(dummy);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in)) {
            int read = cpio.read(new byte[10], 0, 0);
            assertEquals(0, read);
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test
    public void testGetNextCpioEntryEOF() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in)) {
            CpioArchiveEntry entry = cpio.getNextCpioEntry();
            assertNull(entry);
        }
    }

    @Test
    public void testSkipNegative() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        try (CpioArchiveInputStream cpio = new CpioArchiveInputStream(in)) {
            long skipped = cpio.skip(-1);
            assertEquals(0, skipped);
        }
    }

    @Test
    public void testCloseTwice() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[10]);
        CpioArchiveInputStream cpio = new CpioArchiveInputStream(in);
        cpio.close();
        cpio.close(); // Should not throw
    }
}