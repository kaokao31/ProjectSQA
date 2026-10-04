package org.apache.commons.lang3.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class PartialTest {

    @Test
    public void testCreateNumberValid() {
        assertEquals(Integer.valueOf(123), NumberUtils.createNumber("123"));
        assertEquals(Long.valueOf(123L), NumberUtils.createNumber("123L"));
        assertEquals(Long.valueOf(123l), NumberUtils.createNumber("123l"));
        assertEquals(Float.valueOf(123.4f), NumberUtils.createNumber("123.4f"));
        assertEquals(Float.valueOf(123.4F), NumberUtils.createNumber("123.4F"));
        assertEquals(Double.valueOf(123.4d), NumberUtils.createNumber("123.4d"));
        assertEquals(Double.valueOf(123.4D), NumberUtils.createNumber("123.4D"));
        assertEquals(Double.valueOf(123.4), NumberUtils.createNumber("123.4"));
        assertEquals(Integer.valueOf(0), NumberUtils.createNumber("0"));
    }

    @Test
    public void testCreateNumberHex() {
        assertEquals(Integer.valueOf(10), NumberUtils.createNumber("0xA"));
        assertEquals(Integer.valueOf(10), NumberUtils.createNumber("0xa"));
        assertEquals(Long.valueOf(10L), NumberUtils.createNumber("0xAL"));
        assertEquals(Long.valueOf(10L), NumberUtils.createNumber("0xal"));
    }

    @Test
    public void testCreateNumberNull() {
        assertNull(NumberUtils.createNumber(null));
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberEmpty() {
        NumberUtils.createNumber("");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberBlank() {
        NumberUtils.createNumber("   ");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidSign() {
        NumberUtils.createNumber("--123");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberMultipleDecimals() {
        NumberUtils.createNumber("12.34.5");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberInvalidExponent() {
        NumberUtils.createNumber("123e4.5");
    }

    @Test(expected = NumberFormatException.class)
    public void testCreateNumberTrailingInvalid() {
        NumberUtils.createNumber("123xyz");
    }

    @Test
    public void testMinMaxInt() {
        int[] arr = { 1, 5, 2, -3, 0 };
        assertEquals(-3, NumberUtils.min(arr));
        assertEquals(5, NumberUtils.max(arr));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntNull() {
        NumberUtils.min((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinIntEmpty() {
        NumberUtils.min(new int[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntNull() {
        NumberUtils.max((int[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaxIntEmpty() {
        NumberUtils.max(new int[0]);
    }

    @Test
    public void testMinMaxLong() {
        long[] arr = { 1L, 5L, 2L, -3L, 0L };
        assertEquals(-3L, NumberUtils.min(arr));
        assertEquals(5L, NumberUtils.max(arr));
    }

    @Test
    public void testMinMaxFloat() {
        float[] arr = { 1.1f, 5.5f, 2.2f, -3.3f, 0.0f };
        assertEquals(-3.3f, NumberUtils.min(arr), 0.0001f);
        assertEquals(5.5f, NumberUtils.max(arr), 0.0001f);
    }

    @Test
    public void testMinMaxDouble() {
        double[] arr = { 1.1, 5.5, 2.2, -3.3, 0.0 };
        assertEquals(-3.3, NumberUtils.min(arr), 0.0001);
        assertEquals(5.5, NumberUtils.max(arr), 0.0001);
    }

    @Test
    public void testIsDigits() {
        assertTrue(NumberUtils.isDigits("12345"));
        assertFalse(NumberUtils.isDigits("123.45"));
        assertFalse(NumberUtils.isDigits("-12345"));
        assertFalse(NumberUtils.isDigits(null));
        assertFalse(NumberUtils.isDigits(""));
    }

    @Test
    public void testIsNumber() {
        assertTrue(NumberUtils.isNumber("12345"));
        assertTrue(NumberUtils.isNumber("123.45"));
        assertTrue(NumberUtils.isNumber("-12345"));
        assertTrue(NumberUtils.isNumber("0xA"));
        assertFalse(NumberUtils.isNumber(null));
        assertFalse(NumberUtils.isNumber(""));
        assertFalse(NumberUtils.isNumber("123.45.67"));
    }

    @Test
    public void testCompare() {
        assertTrue(NumberUtils.compare(1, 2) < 0);
        assertTrue(NumberUtils.compare(2, 1) > 0);
        assertEquals(0, NumberUtils.compare(1, 1));

        assertTrue(NumberUtils.compare(1L, 2L) < 0);
        assertTrue(NumberUtils.compare(2L, 1L) > 0);
        assertEquals(0, NumberUtils.compare(1L, 1L));
    }
}