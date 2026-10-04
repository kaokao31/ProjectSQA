package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

public class ArchiveUtilsTest {

    @Test
    public void testMatchAsciiBytesNullArgs() {
        assertFalse(ArchiveUtils.matchAsciiBuffer(null, new byte[0]));
        assertFalse(ArchiveUtils.matchAsciiBuffer("test", null));
        assertFalse(ArchiveUtils.matchAsciiBuffer(null, null, 0, 0));
        assertFalse(ArchiveUtils.matchAsciiBuffer("test", null, 0, 4));
        assertFalse(ArchiveUtils.matchAsciiBuffer(null, new byte[10], 0, 4));
    }

    @Test
    public void testMatchAsciiBufferBasic() {
        byte[] buffer = "hello world".getBytes(Charset.forName("US-ASCII"));
        assertTrue(ArchiveUtils.matchAsciiBuffer("hello", buffer));
        assertFalse(ArchiveUtils.matchAsciiBuffer("world", buffer));
    }

    @Test
    public void testMatchAsciiBufferWithOffsets() {
        byte[] buffer = "hello world".getBytes(Charset.forName("US-ASCII"));
        // "world" starts at index 6
        assertTrue(ArchiveUtils.matchAsciiBuffer("world", buffer, 6, 5));
        assertFalse(ArchiveUtils.matchAsciiBuffer("world", buffer, 0, 5));
        
        // Length mismatch cases
        assertFalse(ArchiveUtils.matchAsciiBuffer("hello", buffer, 0, 3));
    }

    @Test
    public void testToAsciiBytes() {
        String s = "abc";
        byte[] bytes = ArchiveUtils.toAsciiBytes(s);
        assertNotNull(bytes);
        assertEquals(3, bytes.length);
        assertEquals((byte) 'a', bytes[0]);
        assertEquals((byte) 'b', bytes[1]);
        assertEquals((byte) 'c', bytes[2]);
    }

    @Test
    public void testToAsciiString() {
        byte[] buffer = new byte[] { (byte) 'a', (byte) 'b', (byte) 'c' };
        assertEquals("abc", ArchiveUtils.toAsciiString(buffer));
        assertEquals("b", ArchiveUtils.toAsciiString(buffer, 1, 1));
    }

    @Test
    public void testIsEqualNullChecks() {
        byte[] b = new byte[10];
        assertFalse(ArchiveUtils.isArrayZero(null, 0));
        assertTrue(ArchiveUtils.isEqual(null, null));
        assertFalse(ArchiveUtils.isEqual(b, null));
        assertFalse(ArchiveUtils.isEqual(null, b));
        
        assertTrue(ArchiveUtils.isEqual(b, 0, 5, null, 0, 5, true));
        assertFalse(ArchiveUtils.isEqual(b, 0, 5, null, 0, 5, false));
    }

    @Test
    public void testIsEqualBuffers() {
        byte[] b1 = "test".getBytes();
        byte[] b2 = "test".getBytes();
        byte[] b3 = "tesT".getBytes();
        byte[] b4 = "testing".getBytes();

        assertTrue(ArchiveUtils.isEqual(b1, b2));
        assertFalse(ArchiveUtils.isEqual(b1, b3));
        assertFalse(ArchiveUtils.isEqual(b1, b4));

        // With offset and length
        assertTrue(ArchiveUtils.isEqual(b1, 0, 4, b2, 0, 4));
        assertFalse(ArchiveUtils.isEqual(b1, 0, 4, b4, 0, 5));
    }

    @Test
    public void testIsEqualWithTrailingZeroTolerance() {
        // Tar headers often have trailing zeros or spaces
        byte[] b1 = new byte[] { 'a', 'b', 'c', 0, 0 };
        byte[] b2 = new byte[] { 'a', 'b', 'c' };

        assertTrue(ArchiveUtils.isEqual(b1, 0, 5, b2, 0, 3, true));
        assertFalse(ArchiveUtils.isEqual(b1, 0, 5, b2, 0, 3, false));

        // Test ignoring trailing spaces/zeros logic specifically in ArchiveUtils (often related to D4J Compress 39)
        byte[] b3 = new byte[] { 'a', 'b', ' ', ' ' };
        byte[] b4 = new byte[] { 'a', 'b' };
        assertTrue(ArchiveUtils.isEqual(b3, 0, 4, b4, 0, 2, true));
    }

    @Test
    public void testIsArrayZero() {
        byte[] zeroBuffer = new byte[10];
        assertTrue(ArchiveUtils.isArrayZero(zeroBuffer, 10));

        byte[] nonZeroBuffer = new byte[10];
        nonZeroBuffer[5] = 1;
        assertFalse(ArchiveUtils.isArrayZero(nonZeroBuffer, 10));
    }

    @Test
    public void testSanitize() {
        // Printable string
        assertEquals("abc", ArchiveUtils.sanitize("abc"));
        
        // Non-printable and control characters
        assertEquals("a?b", ArchiveUtils.sanitize("a\u0001b"));
        assertEquals("a?b", ArchiveUtils.sanitize("a\u007Fb"));
        assertEquals("a?b", ArchiveUtils.sanitize("a\u0080b"));
    }

    @Test
    public void testToStringWithHeader() {
        byte[] buffer = "hello".getBytes();
        String result = ArchiveUtils.toAsciiString(buffer);
        assertNotNull(result);
    }
}