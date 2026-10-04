package org.apache.commons.compress.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class ArchiveUtilsTest {

    // Test isArchiveSignature with null
    @Test(expected = NullPointerException.class)
    public void testIsArchiveSignatureNull() {
        ArchiveUtils.isArchiveSignature(null);
    }

    // Test isArchiveSignature with empty array
    @Test
    public void testIsArchiveSignatureEmpty() {
        assertFalse(ArchiveUtils.isArchiveSignature(new byte[0]));
    }

    // Test isArchiveSignature with known signatures (e.g., ZIP, TAR, etc.)
    @Test
    public void testIsArchiveSignatureZip() {
        byte[] zipSig = {0x50, 0x4B, 0x03, 0x04};
        assertTrue(ArchiveUtils.isArchiveSignature(zipSig));
    }

    @Test
    public void testIsArchiveSignatureTar() {
        byte[] tarSig = {0x75, 0x73, 0x74, 0x61, 0x72}; // "ustar"
        assertTrue(ArchiveUtils.isArchiveSignature(tarSig));
    }

    @Test
    public void testIsArchiveSignatureJar() {
        byte[] jarSig = {0x50, 0x4B, 0x03, 0x04}; // same as ZIP
        assertTrue(ArchiveUtils.isArchiveSignature(jarSig));
    }

    @Test
    public void testIsArchiveSignatureAr() {
        byte[] arSig = {0x21, 0x3C, 0x61, 0x72, 0x63, 0x68, 0x3E}; // "!<arch>"
        assertTrue(ArchiveUtils.isArchiveSignature(arSig));
    }

    @Test
    public void testIsArchiveSignatureCpio() {
        byte[] cpioSig = {0x30, 0x37, 0x30, 0x37, 0x30, 0x31}; // "070701"
        assertTrue(ArchiveUtils.isArchiveSignature(cpioSig));
    }

    @Test
    public void testIsArchiveSignatureUnknown() {
        byte[] unknown = {0x00, 0x01, 0x02};
        assertFalse(ArchiveUtils.isArchiveSignature(unknown));
    }

    // Test toAsciiString with null
    @Test(expected = NullPointerException.class)
    public void testToAsciiStringNull() {
        ArchiveUtils.toAsciiString(null);
    }

    // Test toAsciiString with empty array
    @Test
    public void testToAsciiStringEmpty() {
        assertEquals("", ArchiveUtils.toAsciiString(new byte[0]));
    }

    // Test toAsciiString with normal ASCII
    @Test
    public void testToAsciiStringNormal() {
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
        assertEquals("Hello", ArchiveUtils.toAsciiString(data));
    }

    // Test toAsciiString with non-printable characters (should still convert)
    @Test
    public void testToAsciiStringNonPrintable() {
        byte[] data = {0x00, 0x01, 0x02};
        String result = ArchiveUtils.toAsciiString(data);
        assertEquals(3, result.length());
        assertEquals('\0', result.charAt(0));
        assertEquals('\1', result.charAt(1));
        assertEquals('\2', result.charAt(2));
    }

    // Test toAsciiString with offset and length
    @Test
    public void testToAsciiStringOffsetLength() {
        byte[] data = {0x41, 0x42, 0x43, 0x44, 0x45}; // "ABCDE"
        assertEquals("BCD", ArchiveUtils.toAsciiString(data, 1, 3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testToAsciiStringOffsetNegative() {
        byte[] data = {0x41};
        ArchiveUtils.toAsciiString(data, -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testToAsciiStringLengthNegative() {
        byte[] data = {0x41};
        ArchiveUtils.toAsciiString(data, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testToAsciiStringOffsetPlusLengthExceeds() {
        byte[] data = {0x41, 0x42};
        ArchiveUtils.toAsciiString(data, 1, 2);
    }

    // Test toAsciiString with null and offset/length
    @Test(expected = NullPointerException.class)
    public void testToAsciiStringNullWithOffset() {
        ArchiveUtils.toAsciiString(null, 0, 1);
    }

    // Test isEqual with null arrays
    @Test
    public void testIsEqualBothNull() {
        assertTrue(ArchiveUtils.isEqual(null, null));
    }

    @Test
    public void testIsEqualFirstNull() {
        assertFalse(ArchiveUtils.isEqual(null, new byte[0]));
    }

    @Test
    public void testIsEqualSecondNull() {
        assertFalse(ArchiveUtils.isEqual(new byte[0], null));
    }

    // Test isEqual with equal arrays
    @Test
    public void testIsEqualEqual() {
        byte[] a = {0x01, 0x02, 0x03};
        byte[] b = {0x01, 0x02, 0x03};
        assertTrue(ArchiveUtils.isEqual(a, b));
    }

    // Test isEqual with different lengths
    @Test
    public void testIsEqualDifferentLengths() {
        byte[] a = {0x01, 0x02};
        byte[] b = {0x01, 0x02, 0x03};
        assertFalse(ArchiveUtils.isEqual(a, b));
    }

    // Test isEqual with different content
    @Test
    public void testIsEqualDifferentContent() {
        byte[] a = {0x01, 0x02, 0x03};
        byte[] b = {0x01, 0x02, 0x04};
        assertFalse(ArchiveUtils.isEqual(a, b));
    }

    // Test isEqual with empty arrays
    @Test
    public void testIsEqualBothEmpty() {
        assertTrue(ArchiveUtils.isEqual(new byte[0], new byte[0]));
    }

    // Test matchAsciiBuffer with null prefix
    @Test(expected = NullPointerException.class)
    public void testMatchAsciiBufferNullPrefix() {
        ArchiveUtils.matchAsciiBuffer(null, new byte[0]);
    }

    // Test matchAsciiBuffer with null buffer
    @Test(expected = NullPointerException.class)
    public void testMatchAsciiBufferNullBuffer() {
        ArchiveUtils.matchAsciiBuffer("test", null);
    }

    // Test matchAsciiBuffer with matching prefix
    @Test
    public void testMatchAsciiBufferMatch() {
        byte[] buffer = {0x74, 0x65, 0x73, 0x74, 0x00}; // "test\0"
        assertTrue(ArchiveUtils.matchAsciiBuffer("test", buffer));
    }

    // Test matchAsciiBuffer with non-matching prefix
    @Test
    public void testMatchAsciiBufferNoMatch() {
        byte[] buffer = {0x74, 0x65, 0x73, 0x74, 0x00}; // "test\0"
        assertFalse(ArchiveUtils.matchAsciiBuffer("tes", buffer));
    }

    // Test matchAsciiBuffer with offset and length
    @Test
    public void testMatchAsciiBufferWithOffset() {
        byte[] buffer = {0x00, 0x74, 0x65, 0x73, 0x74}; // "\0test"
        assertTrue(ArchiveUtils.matchAsciiBuffer("test", buffer, 1, 4));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testMatchAsciiBufferOffsetNegative() {
        byte[] buffer = {0x74, 0x65, 0x73, 0x74};
        ArchiveUtils.matchAsciiBuffer("test", buffer, -1, 4);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testMatchAsciiBufferLengthNegative() {
        byte[] buffer = {0x74, 0x65, 0x73, 0x74};
        ArchiveUtils.matchAsciiBuffer("test", buffer, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testMatchAsciiBufferOffsetPlusLengthExceeds() {
        byte[] buffer = {0x74, 0x65, 0x73, 0x74};
        ArchiveUtils.matchAsciiBuffer("test", buffer, 2, 3);
    }

    // Test matchAsciiBuffer with empty prefix
    @Test
    public void testMatchAsciiBufferEmptyPrefix() {
        byte[] buffer = {0x74, 0x65, 0x73, 0x74};
        assertTrue(ArchiveUtils.matchAsciiBuffer("", buffer));
    }

    // Test matchAsciiBuffer with empty buffer
    @Test
    public void testMatchAsciiBufferEmptyBuffer() {
        assertFalse(ArchiveUtils.matchAsciiBuffer("test", new byte[0]));
    }

    // Additional edge cases for isArchiveSignature with partial signatures
    @Test
    public void testIsArchiveSignaturePartialZip() {
        byte[] partial = {0x50, 0x4B};
        assertFalse(ArchiveUtils.isArchiveSignature(partial));
    }

    @Test
    public void testIsArchiveSignatureLongerThanKnown() {
        byte[] longSig = new byte[10];
        // fill with something that starts with a known signature
        longSig[0] = 0x50; longSig[1] = 0x4B; longSig[2] = 0x03; longSig[3] = 0x04;
        longSig[4] = 0x00; // extra byte
        assertTrue(ArchiveUtils.isArchiveSignature(longSig));
    }

    // Test toAsciiString with large array (performance not tested, just correctness)
    @Test
    public void testToAsciiStringLarge() {
        byte[] large = new byte[1000];
        for (int i = 0; i < large.length; i++) {
            large[i] = (byte) (i % 128);
        }
        String result = ArchiveUtils.toAsciiString(large);
        assertEquals(1000, result.length());
    }

    // Test isEqual with same reference
    @Test
    public void testIsEqualSameReference() {
        byte[] a = {0x01, 0x02};
        assertTrue(ArchiveUtils.isEqual(a, a));
    }

    // Test matchAsciiBuffer with prefix longer than buffer
    @Test
    public void testMatchAsciiBufferPrefixLonger() {
        byte[] buffer = {0x74, 0x65}; // "te"
        assertFalse(ArchiveUtils.matchAsciiBuffer("test", buffer));
    }

    // Test matchAsciiBuffer with case sensitivity (ASCII is case-sensitive)
    @Test
    public void testMatchAsciiBufferCaseSensitive() {
        byte[] buffer = {0x54, 0x65, 0x73, 0x74}; // "Test"
        assertFalse(ArchiveUtils.matchAsciiBuffer("test", buffer));
    }
}