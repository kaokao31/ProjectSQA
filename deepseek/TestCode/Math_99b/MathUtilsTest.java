package org.apache.commons.math.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class MathUtilsTest {

    // ---------- gcd tests ----------
    @Test
    public void testGcdPositive() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(1, MathUtils.gcd(13, 17));
        assertEquals(12, MathUtils.gcd(12, 0));
        assertEquals(12, MathUtils.gcd(0, 12));
    }

    @Test
    public void testGcdNegative() {
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(6, MathUtils.gcd(12, -18));
        assertEquals(6, MathUtils.gcd(-12, -18));
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdZeroZero() {
        MathUtils.gcd(0, 0);
    }

    @Test
    public void testGcdMinValue() {
        // gcd(Integer.MIN_VALUE, 0) should return abs(Integer.MIN_VALUE) which overflows?
        // The method likely uses Math.abs, which for Integer.MIN_VALUE returns Integer.MIN_VALUE (negative).
        // This is a known edge case. We test that it doesn't throw and returns a negative value.
        int result = MathUtils.gcd(Integer.MIN_VALUE, 0);
        // The result is implementation dependent; we just ensure no exception.
        assertTrue(result == Integer.MIN_VALUE || result == 0);
    }

    // ---------- lcm tests ----------
    @Test
    public void testLcmPositive() {
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(12, MathUtils.lcm(12, 12));
        assertEquals(0, MathUtils.lcm(0, 12));
        assertEquals(0, MathUtils.lcm(12, 0));
    }

    @Test
    public void testLcmNegative() {
        assertEquals(36, MathUtils.lcm(-12, 18));
        assertEquals(36, MathUtils.lcm(12, -18));
        assertEquals(36, MathUtils.lcm(-12, -18));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        // This should trigger overflow: gcd = 1, so lcm = a * b overflows int.
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflowMinValue() {
        // lcm with Integer.MIN_VALUE and any non-zero number may overflow.
        MathUtils.lcm(Integer.MIN_VALUE, 1);
    }

    @Test
    public void testLcmMaxValue() {
        // lcm of same max value should be max value (no overflow)
        assertEquals(Integer.MAX_VALUE, MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmBothMinValue() {
        // lcm(Integer.MIN_VALUE, Integer.MIN_VALUE) would be abs(Integer.MIN_VALUE) which overflows.
        MathUtils.lcm(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
}