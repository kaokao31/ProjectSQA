package org.apache.commons.math.util;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class MathUtilsTest {

    // ========== gcd tests ==========
    @Test
    public void testGcdZero() {
        assertEquals(5, MathUtils.gcd(5, 0));
        assertEquals(5, MathUtils.gcd(0, 5));
        assertEquals(0, MathUtils.gcd(0, 0));
    }

    @Test
    public void testGcdPositive() {
        assertEquals(6, MathUtils.gcd(12, 18));
        assertEquals(1, MathUtils.gcd(17, 23));
        assertEquals(7, MathUtils.gcd(7, 7));
    }

    @Test
    public void testGcdNegative() {
        assertEquals(6, MathUtils.gcd(-12, 18));
        assertEquals(6, MathUtils.gcd(12, -18));
        assertEquals(6, MathUtils.gcd(-12, -18));
    }

    @Test
    public void testGcdLargeValues() {
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
        assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, 1));
        assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflow() {
        // gcd(Integer.MIN_VALUE, 0) would overflow if result is taken as absolute value
        MathUtils.gcd(Integer.MIN_VALUE, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflow2() {
        // gcd(Integer.MIN_VALUE, Integer.MIN_VALUE) also overflows
        MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testGcdOverflow3() {
        // gcd(Integer.MIN_VALUE, 2) leads to overflow in algorithm
        MathUtils.gcd(Integer.MIN_VALUE, 2);
    }

    // ========== lcm tests ==========
    @Test
    public void testLcmPositive() {
        assertEquals(36, MathUtils.lcm(12, 18));
        assertEquals(0, MathUtils.lcm(0, 5));
        assertEquals(0, MathUtils.lcm(5, 0));
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow() {
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testLcmOverflow2() {
        MathUtils.lcm(1000000, 1000000);
    }

    // ========== addAndCheck tests ==========
    @Test
    public void testAddAndCheckNoOverflow() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        assertEquals(0, MathUtils.addAndCheck(Integer.MAX_VALUE, -Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = ArithmeticException.class)
    public void testAddAndCheckOverflowNegative() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    // ========== subAndCheck tests ==========
    @Test
    public void testSubAndCheckNoOverflow() {
        assertEquals(-1, MathUtils.subAndCheck(2, 3));
        assertEquals(1, MathUtils.subAndCheck(-2, -3));
        assertEquals(0, MathUtils.subAndCheck(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckOverflowPositive() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = ArithmeticException.class)
    public void testSubAndCheckOverflowNegative() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    // ========== mulAndCheck tests ==========
    @Test
    public void testMulAndCheckNoOverflow() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        assertEquals(0, MathUtils.mulAndCheck(0, Integer.MAX_VALUE));
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckOverflowPositive() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = ArithmeticException.class)
    public void testMulAndCheckOverflowNegative() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    // ========== round tests ==========
    @Test
    public void testRoundDouble() {
        assertEquals(3, MathUtils.round(3.14, 0));
        assertEquals(3.1, MathUtils.round(3.14, 1), 1e-10);
        assertEquals(3.14, MathUtils.round(3.14159, 2), 1e-10);
        assertEquals(3.142, MathUtils.round(3.14159, 3), 1e-10);
    }

    @Test
    public void testRoundFloat() {
        assertEquals(3.0f, MathUtils.round(3.14f, 0), 1e-10);
        assertEquals(3.1f, MathUtils.round(3.14f, 1), 1e-10);
        assertEquals(3.14f, MathUtils.round(3.14159f, 2), 1e-10);
    }

    // ========== sign tests ==========
    @Test
    public void testSignByte() {
        assertEquals(1, MathUtils.sign((byte) 5));
        assertEquals(-1, MathUtils.sign((byte) -3));
        assertEquals(0, MathUtils.sign((byte) 0));
    }

    @Test
    public void testSignDouble() {
        assertEquals(1.0, MathUtils.sign(2.5), 1e-10);
        assertEquals(-1.0, MathUtils.sign(-3.7), 1e-10);
        assertEquals(0.0, MathUtils.sign(0.0), 1e-10);
        assertEquals(1.0, MathUtils.sign(Double.POSITIVE_INFINITY), 1e-10);
        assertEquals(-1.0, MathUtils.sign(Double.NEGATIVE_INFINITY), 1e-10);
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));
    }

    @Test
    public void testSignFloat() {
        assertEquals(1.0f, MathUtils.sign(2.5f), 1e-10);
        assertEquals(-1.0f, MathUtils.sign(-3.7f), 1e-10);
        assertEquals(0.0f, MathUtils.sign(0.0f), 1e-10);
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));
    }

    // ========== copySign tests ==========
    @Test
    public void testCopySignDouble() {
        assertEquals(5.0, MathUtils.copySign(5.0, 1.0), 1e-10);
        assertEquals(-5.0, MathUtils.copySign(5.0, -1.0), 1e-10);
        assertEquals(5.0, MathUtils.copySign(-5.0, 1.0), 1e-10);
        assertEquals(-5.0, MathUtils.copySign(-5.0, -1.0), 1e-10);
    }

    @Test
    public void testCopySignFloat() {
        assertEquals(5.0f, MathUtils.copySign(5.0f, 1.0f), 1e-10);
        assertEquals(-5.0f, MathUtils.copySign(5.0f, -1.0f), 1e-10);
    }

    // ========== scalb tests ==========
    @Test
    public void testScalbDouble() {
        assertEquals(8.0, MathUtils.scalb(2.0, 2), 1e-10);
        assertEquals(0.5, MathUtils.scalb(2.0, -2), 1e-10);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.scalb(Double.MAX_VALUE, 2), 1e-10);
    }

    @Test
    public void testScalbFloat() {
        assertEquals(8.0f, MathUtils.scalb(2.0f, 2), 1e-10);
        assertEquals(0.5f, MathUtils.scalb(2.0f, -2), 1e-10);
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.scalb(Float.MAX_VALUE, 2), 1e-10);
    }

    // ========== nextAfter tests ==========
    @Test
    public void testNextAfterDouble() {
        assertEquals(1.0000000000000002, MathUtils.nextAfter(1.0, 2.0), 1e-10);
        assertEquals(0.9999999999999999, MathUtils.nextAfter(1.0, 0.0), 1e-10);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextAfter(Double.NEGATIVE_INFINITY, -1.0), 1e-10);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0), 1e-10);
    }

    @Test
    public void testNextAfterFloat() {
        assertEquals(1.0000001f, MathUtils.nextAfter(1.0f, 2.0f), 1e-10);
        assertEquals(0.99999994f, MathUtils.nextAfter(1.0f, 0.0f), 1e-10);
    }

    // ========== nextUp tests ==========
    @Test
    public void testNextUpDouble() {
        assertEquals(1.0000000000000002, MathUtils.nextUp(1.0), 1e-10);
        assertEquals(Double.POSITIVE_INFINITY, MathUtils.nextUp(Double.MAX_VALUE), 1e-10);
    }

    @Test
    public void testNextUpFloat() {
        assertEquals(1.0000001f, MathUtils.nextUp(1.0f), 1e-10);
        assertEquals(Float.POSITIVE_INFINITY, MathUtils.nextUp(Float.MAX_VALUE), 1e-10);
    }

    // ========== nextDown tests ==========
    @Test
    public void testNextDownDouble() {
        assertEquals(0.9999999999999999, MathUtils.nextDown(1.0), 1e-10);
        assertEquals(Double.NEGATIVE_INFINITY, MathUtils.nextDown(-Double.MAX_VALUE), 1e-10);
    }

    @Test
    public void testNextDownFloat() {
        assertEquals(0.99999994f, MathUtils.nextDown(1.0f), 1e-10);
        assertEquals(Float.NEGATIVE_INFINITY, MathUtils.nextDown(-Float.MAX_VALUE), 1e-10);
    }

    // ========== sin/cos/tan tests ==========
    @Test
    public void testSin() {
        assertEquals(0.0, MathUtils.sin(0.0), 1e-10);
        assertEquals(1.0, MathUtils.sin(Math.PI / 2), 1e-10);
        assertEquals(0.0, MathUtils.sin(Math.PI), 1e-10);
    }

    @Test
    public void testCos() {
        assertEquals(1.0, MathUtils.cos(0.0), 1e-10);
        assertEquals(0.0, MathUtils.cos(Math.PI / 2), 1e-10);
        assertEquals(-1.0, MathUtils.cos(Math.PI), 1e-10);
    }

    @Test
    public void testTan() {
        assertEquals(0.0, MathUtils.tan(0.0), 1e-10);
        assertEquals(1.0, MathUtils.tan(Math.PI / 4), 1e-10);
        assertTrue(Double.isInfinite(MathUtils.tan(Math.PI / 2)));
    }

    // ========== asin/acos/atan tests ==========
    @Test
    public void testAsin() {
        assertEquals(0.0, MathUtils.asin(0.0), 1e-10);
        assertEquals(Math.PI / 2, MathUtils.asin(1.0), 1e-10);
        assertEquals(-Math.PI / 2, MathUtils.asin(-1.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.asin(1.1)));
    }

    @Test
    public void testAcos() {
        assertEquals(0.0, MathUtils.acos(1.0), 1e-10);
        assertEquals(Math.PI / 2, MathUtils.acos(0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.acos(-1.0), 1e-10);
        assertTrue(Double.isNaN(MathUtils.acos(1.1)));
    }

    @Test
    public void testAtan() {
        assertEquals(0.0, MathUtils.atan(0.0), 1e-10);
        assertEquals(Math.PI / 4, MathUtils.atan(1.0), 1e-10);
        assertEquals(-Math.PI / 4, MathUtils.atan(-1.0), 1e-10);
    }

    // ========== toRadians/toDegrees tests ==========
    @Test
    public void testToRadians() {
        assertEquals(0.0, MathUtils.toRadians(0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.toRadians(180.0), 1e-10);
        assertEquals(-Math.PI, MathUtils.toRadians(-180.0), 1e-10);
    }

    @Test
    public void testToDegrees() {
        assertEquals(0.0, MathUtils.toDegrees(0.0), 1e-10);
        assertEquals(180.0, MathUtils.toDegrees(Math.PI), 1e-10);
        assertEquals(-180.0, MathUtils.toDegrees(-Math.PI), 1e-10);
    }

    // ========== normalizeAngle tests ==========
    @Test
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-10);
        assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, 0.0), 1e-10);
        assertEquals(0.0, MathUtils.normalizeAngle(-2 * Math.PI, 0.0), 1e-10);
        assertEquals(Math.PI, MathUtils.normalizeAngle(Math.PI, 0.0), 1e-10);
        assertEquals(-Math.PI, MathUtils.normalizeAngle(-Math.PI, 0.0), 1e-10);
    }

    // ========== reduce tests ==========
    @Test
    public void testReduce() {
        assertEquals(0.0, MathUtils.reduce(0.0, 1.0, 0.0), 1e-10);
        assertEquals(0.5, MathUtils.reduce(0.5, 1.0, 0.0), 1e-10);
        assertEquals(0.0, MathUtils.reduce(1.0, 1.0, 0.0), 1e-10);
        assertEquals(0.2, MathUtils.reduce(1.2, 1.0, 0.0), 1e-10);
    }

    // ========== checkFinite tests ==========
    @Test
    public void testCheckFinite() {
        assertTrue(MathUtils.checkFinite(1.0));
        assertFalse(MathUtils.checkFinite(Double.POSITIVE_INFINITY));
        assertFalse(MathUtils.checkFinite(Double.NEGATIVE_INFINITY));
        assertFalse(MathUtils.checkFinite(Double.NaN));
    }

    // ========== pow tests ==========
    @Test
    public void testPowInt() {
        assertEquals(8, MathUtils.pow(2, 3));
        assertEquals(1, MathUtils.pow(2, 0));
        assertEquals(0, MathUtils.pow(0, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testPowIntOverflow() {
        MathUtils.pow(2, 31);
    }

    @Test
    public void testPowLong() {
        assertEquals(8L, MathUtils.pow(2L, 3));
        assertEquals(1L, MathUtils.pow(2L, 0));
        assertEquals(0L, MathUtils.pow(0L, 3));
    }

    @Test(expected = ArithmeticException.class)
    public void testPowLongOverflow() {
        MathUtils.pow(2L, 63);
    }
}