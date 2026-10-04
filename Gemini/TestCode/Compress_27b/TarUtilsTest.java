package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TarUtilsTest {

    @Test
    public void testComputeCheckSum() {
        byte[] buf = new byte[512];
        // Fill some data
        for (int i = 0; i < 100; i++) {
            buf[i] = (byte) i;
        }
        long sum = TarUtils.computeCheckSum(buf);
        assertTrue(sum >= 0);
    }

    @Test
    public void testParseName() {
        byte[] buf = "testname\0\0".getBytes();
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("testname", name);
    }

    @Test
    public void testParseNameWithOffsetAndLength() {
        byte[] buf = "---testname---".getBytes();
        String name = TarUtils.parseName(buf, 3, 8);
        assertEquals("testname", name);
    }

    @Test
    public void testFormatNameBytes() {
        byte[] buf = new byte[10];
        int len = TarUtils.formatNameBytes("test", buf, 0, 10);
        assertEquals(10, len);
        assertEquals('t', buf[0]);
        assertEquals(0, buf[4]);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(12345L, buf, 0, 12);
        assertEquals(12, len);
    }

    @Test
    public void testFormatCheckOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatCheckOctalBytes(123L, buf, 0, 8);
        assertEquals(8, len);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatOctalBytes(123L, buf, 0, 8);
        assertEquals(8, len);
    }

    @Test
    public void testParseOctal() {
        byte[] buf = " 123 \0".getBytes();
        long val = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(123L, val);
    }

    @Test
    public void testParseOctalInvalid() {
        byte[] buf = "abc".getBytes();
        try {
            TarUtils.parseOctal(buf, 0, buf.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testParseOctalOrBinary() {
        // Test standard octal parsing via parseOctalOrBinary
        byte[] buf = " 123 \0".getBytes();
        long val = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(123L, val);
    }

    @Test
    public void testParseOctalOrBinaryLong() {
        // Test binary format starting with 0x80
        byte[] buf = new byte[8];
        buf[0] = (byte) 0x80;
        buf[7] = 0x01; // value 1
        long val = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(1L, val);
    }

    @Test
    public void testParseOctalOrBinaryNegativeLong() {
        // Test negative binary format starting with 0xFF
        byte[] buf = new byte[8];
        for (int i = 0; i < 8; i++) {
            buf[i] = (byte) 0xFF;
        }
        long val = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(-1L, val);
    }

    @Test
    public void testFormatBoolean() {
        byte[] buf = new byte[1];
        TarUtils.formatBooleanFA(true, buf, 0, 1);
        assertEquals((byte) 'Y', buf[0]);

        TarUtils.formatBooleanFA(false, buf, 0, 1);
        assertEquals((byte) 'N', buf[0]);
    }

    @Test
    public void testParseBoolean() {
        byte[] buf = {(byte) 'Y'};
        assertTrue(TarUtils.parseBoolean(buf, 0));

        buf[0] = (byte) 'N';
        assertFalse(TarUtils.parseBoolean(buf, 0));
    }
}