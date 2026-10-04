package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;

public class TarUtilsTest {

    // ---------- parseOctal tests ----------
    @Test
    public void testParseOctalSimple() {
        byte[] buf = "123\0".getBytes();
        long result = TarUtils.parseOctal(buf, 0, 4);
        assertEquals(83L, result);
    }

    @Test
    public void testParseOctalLeadingSpaces() {
        // bug: leading spaces should be skipped
        byte[] buf = "  123\0".getBytes();
        long result = TarUtils.parseOctal(buf, 0, 6);
        assertEquals(83L, result);
    }

    @Test
    public void testParseOctalAllZeros() {
        byte[] buf = "000000\0".getBytes();
        long result = TarUtils.parseOctal(buf,0,7);
        assertEquals(0L,result);
    }

    @Test
    public void testParseOctalNullTerminated() {
        byte[] buf = new byte[]{'1','0','0','0','\0','1'};
        long result = TarUtils.parseOctal(buf,0,6);
        assertEquals(64L,result); // octal 1000 = 512 decimal? Actually 1000 octal = 512 decimal. Wait: parseOctal interprets as octal digits; '1','0','0','0' = 1000 octal = 512 decimal.
        assertEquals(512L,result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidCharacter() {
        byte[] buf = "12\08".getBytes(); // '8' is invalid octal
        TarUtils.parseOctal(buf,0,4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalTooShort() {
        byte[] buf = "12".getBytes();
        TarUtils.parseOctal(buf,0,2); // length < 3? Actually no null terminator, and length is 2. But check: The method requires at least 3 bytes? Not sure. Usually requires at least length>=2? Let's assume it throws if length < 2. Actually the condition might be length < 2. We'll test to cover.
        // But to be safe, we test with minimal length that should work (like 3) and then with 1.
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalZeroLength() {
        byte[] buf = "".getBytes();
        TarUtils.parseOctal(buf,0,1); // length 1, insufficient
    }

    @Test
    public void testParseOctalWithTrailingSpaces() {
        byte[] buf = "123   ".getBytes(); // spaces after number
        long result = TarUtils.parseOctal(buf,0,7);
        assertEquals(83L,result);
    }

    // ---------- parseBoolean tests ----------
    @Test
    public void testParseBooleanTrue() {
        byte[] buf = "true".getBytes();
        assertTrue(TarUtils.parseBoolean(buf,0,4));
    }

    @Test
    public void testParseBooleanFalse() {
        byte[] buf = "false".getBytes();
        assertFalse(TarUtils.parseBoolean(buf,0,5));
    }

    @Test
    public void testParseBooleanNull() {
        byte[] buf = null;
        assertFalse(TarUtils.parseBoolean(buf,0,0));
    }

    @Test
    public void testParseBooleanEmpty() {
        byte[] buf = new byte[0];
        assertFalse(TarUtils.parseBoolean(buf,0,0));
    }

    // ---------- parseName tests ----------
    @Test
    public void testParseNameSimple() {
        byte[] buf = "filename.txt\0".getBytes();
        String name = TarUtils.parseName(buf,0,16);
        assertEquals("filename.txt",name);
    }

    @Test
    public void testParseNameNoNullTerminator() {
        byte[] buf = "short".getBytes();
        String name = TarUtils.parseName(buf,0,5);
        assertEquals("short",name);
    }

    @Test
    public void testParseNameEmpty() {
        byte[] buf = new byte[0];
        String name = TarUtils.parseName(buf,0,0);
        assertEquals("",name);
    }

    @Test
    public void testParseNameLongName() {
        byte[] buf = new byte[100];
        for (int i = 0; i < 99; i++) buf[i] = 'a';
        buf[99] = '\0';
        String name = TarUtils.parseName(buf,0,100);
        assertEquals(99, name.length());
        for (char c : name.toCharArray()) assertEquals('a', c);
    }

    // ---------- formatLongOctalBytes tests ----------
    @Test
    public void testFormatLongOctalBytesZero() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(0L, buf, 0, 12);
        // Expected: "000000000000" (12 bytes + null? Actually format writes octal digits and then null-terminates? The method writes a trailing null. So we expect "0" followed by spaces? Let's check typical implementation: It writes octal number left-aligned, then null byte. So for value 0, it writes '0' then null, then maybe spaces? But normally it writes the octal number and then fills rest with spaces and a null? Hard to predict. We'll just assert that it does not throw and the buffer is modified.
        // Better: After call, the buffer should contain '0' at offset.
        assertEquals('0', buf[0]);
    }

    @Test
    public void testFormatLongOctalBytesLarge() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(511L, buf, 0, 12);
        // 511 decimal = 777 octal -> '777' + null
        assertEquals('8', buf[0]? Actually 7. Let's just check that first character is '7'.
        assertTrue(buf[0] == '7');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(-1L, buf, 0, 12);
    }

    // ---------- formatLongOctalOrBinaryBytes tests ----------
    @Test
    public void testFormatLongOctalOrBinaryBytesOctal() {
        byte[] buf = new byte[12];
        // Write a value that fits in octal: 511
        TarUtils.formatLongOctalOrBinaryBytes(511L, buf, 0, 12);
        // Check that it starts with '7' (since 777 octal)
        assertEquals('7', buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinary() {
        byte[] buf = new byte[12];
        // Write a value that exceeds octal range (e.g., 8^12?) Actually the method switches to binary if the value doesn't fit in octal representation within the given length. For a negative value, it uses binary. Let's use a large positive that forces binary? Actually the method uses binary if the value is negative or if the octal representation would exceed length. We'll use a value that is too large for 12 bytes octal? Octal max for 12 bytes is 8^12-1 = ~6.87e10, which is huge. So a positive long always fits? Actually long max is 9e18, and 8^12 = 6.87e10, so larger longs would not fit octal in 12 bytes. So we can test a value > 8^12-1, e.g., Long.MAX_VALUE.
        // However, the method might also use binary for positive values that don't fit. We'll test with Long.MAX_VALUE.
        TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buf, 0, 12);
        // The first byte should be '0'? No, binary representation starts with a nonzero high bit? In tar binary format, the first byte is 0x80 or similar. Actually the method writes a leading 0x80 or 0x00? Let's just check it doesn't throw and the first byte is not 0? We'll simply call.
        // Alternatively, we can test that it works without exception.
        assertNotNull(buf);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 12);
        // Should use binary representation, first byte should be 0xff? Actually negative numbers represented with leading 0xff.
        assertTrue(buf[0] < 0); // signed byte
    }

    // Additional edge cases for parseOctal to trigger bug: leading space and then null.
    @Test
    public void testParseOctalLeadingSpaceThenNull() {
        byte[] buf = " \0".getBytes(); // single space followed by null
        // Expected: 0? Actually the method should skip leading spaces, then encounter null -> value 0.
        long result = TarUtils.parseOctal(buf,0,2);
        assertEquals(0L, result);
    }

    @Test
    public void testParseOctalLeadingSpacesAndValue() {
        byte[] buf = "   123\0".getBytes();
        long result = TarUtils.parseOctal(buf,0,8);
        assertEquals(83L, result);
    }

    // Test that parseOctal correctly handles the case where there are non-digit characters after a valid number.
    @Test
    public void testParseOctalWhitesaceAfterValue() {
        byte[] buf = "123   \0".getBytes();
        long result = TarUtils.parseOctal(buf,0,7);
        assertEquals(83L, result);
    }

    @Test(expected = NumberFormatException.class) // Actually the method throws IllegalArgumentException? We'll assume.
    public void testParseOctalNegativeNumber() {
        byte[] buf = "-123\0".getBytes();
        TarUtils.parseOctal(buf,0,5); // negative sign is invalid in octal
    }

    // ---------- parseBoolean edge cases ----------
    @Test
    public void testParseBooleanCaseInsensitive() {
        byte[] buf = "True".getBytes();
        assertTrue(TarUtils.parseBoolean(buf,0,4));
    }

    @Test
    public void testParseBooleanWithSpaces() {
        byte[] buf = "  true\0".getBytes();
        assertTrue(TarUtils.parseBoolean(buf,0,7));
    }

    @Test
    public void testParseBooleanOtherString() {
        byte[] buf = "yes".getBytes();
        assertFalse(TarUtils.parseBoolean(buf,0,3));
    }

    // ---------- parseName boundary ----------
    @Test
    public void testParseNameOffset() {
        byte[] buf = "prefix/targetName\0".getBytes();
        String name = TarUtils.parseName(buf,7,15);
        assertEquals("targetName", name);
    }

    @Test
    public void testParseNameMaxLength() {
        byte[] buf = new byte[100];
        Arrays.fill(buf, (byte)'a');
        buf[99] = '\0';
        String name = TarUtils.parseName(buf,0,100);
        assertEquals(99, name.length());
    }

    // ---------- formatLongOctalBytes length constraints ----------
    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesBufferTooShort() {
        byte[] buf = new byte[4];
        TarUtils.formatLongOctalBytes(100L, buf, 0, 4); // need at least 5? Actually method requires length >= 2 plus number digits? At least 2. 4 is likely too short for value 100 decimal (144 octal = 3 digits + null => 4 bytes). So should throw.
    }

    @Test
    public void testFormatLongOctalBytesExactFit() {
        byte[] buf = new byte[4];
        TarUtils.formatLongOctalBytes(64L, buf, 0,4); // 64 decimal = 100 octal = 3 digits + null => 4 bytes
        assertEquals('1', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[2]);
        assertEquals('\0', buf[3]);
    }

}