package org.apache.commons.math3.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class FastMathTest {

    // ========== cosh tests (including bug Math-16) ==========
    @Test
    public void testCoshBugLargeNegative() {
        // Bug: for large negative x, cosh should return +Infinity, not 0.0
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(-1000.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(-Double.MAX_VALUE), 0.0);
    }

    @Test
    public void testCoshLargePositive() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(1000.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(Double.MAX_VALUE), 0.0);
    }

    @Test
    public void testCoshZero() {
        assertEquals(1.0, FastMath.cosh(0.0), 0.0);
        assertEquals(1.0, FastMath.cosh(-0.0), 0.0);
    }

    @Test
    public void testCoshOne() {
        double expected = (FastMath.exp(1.0) + FastMath.exp(-1.0)) / 2.0;
        assertEquals(expected, FastMath.cosh(1.0), 1e-15);
    }

    @Test
    public void testCoshNaN() {
        assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
    }

    @Test
    public void testCoshInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test
    public void testCoshNearThreshold() {
        // Test around the threshold (20) used in the implementation
        double x = 20.0;
        double expected = FastMath.exp(x) / 2.0;
        assertEquals(expected, FastMath.cosh(x), 1e-12);
        x = -20.0;
        expected = FastMath.exp(-x) / 2.0;
        assertEquals(expected, FastMath.cosh(x), 1e-12);
        x = 19.0;
        expected = (FastMath.exp(x) + FastMath.exp(-x)) / 2.0;
        assertEquals(expected, FastMath.cosh(x), 1e-12);
        x = -19.0;
        expected = (FastMath.exp(x) + FastMath.exp(-x)) / 2.0;
        assertEquals(expected, FastMath.cosh(x), 1e-12);
    }

    // ========== sinh tests ==========
    @Test
    public void testSinhZero() {
        assertEquals(0.0, FastMath.sinh(0.0), 0.0);
        assertEquals(0.0, FastMath.sinh(-0.0), 0.0);
    }

    @Test
    public void testSinhOne() {
        double expected = (FastMath.exp(1.0) - FastMath.exp(-1.0)) / 2.0;
        assertEquals(expected, FastMath.sinh(1.0), 1e-15);
    }

    @Test
    public void testSinhLargePositive() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sinh(1000.0), 0.0);
    }

    @Test
    public void testSinhLargeNegative() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.sinh(-1000.0), 0.0);
    }

    @Test
    public void testSinhNaN() {
        assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
    }

    @Test
    public void testSinhInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sinh(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.sinh(Double.NEGATIVE_INFINITY), 0.0);
    }

    // ========== tanh tests ==========
    @Test
    public void testTanhZero() {
        assertEquals(0.0, FastMath.tanh(0.0), 0.0);
        assertEquals(0.0, FastMath.tanh(-0.0), 0.0);
    }

    @Test
    public void testTanhOne() {
        double expected = FastMath.sinh(1.0) / FastMath.cosh(1.0);
        assertEquals(expected, FastMath.tanh(1.0), 1e-15);
    }

    @Test
    public void testTanhLargePositive() {
        assertEquals(1.0, FastMath.tanh(1000.0), 1e-15);
    }

    @Test
    public void testTanhLargeNegative() {
        assertEquals(-1.0, FastMath.tanh(-1000.0), 1e-15);
    }

    @Test
    public void testTanhNaN() {
        assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));
    }

    @Test
    public void testTanhInfinity() {
        assertEquals(1.0, FastMath.tanh(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(-1.0, FastMath.tanh(Double.NEGATIVE_INFINITY), 1e-15);
    }

    // ========== exp tests ==========
    @Test
    public void testExpZero() {
        assertEquals(1.0, FastMath.exp(0.0), 0.0);
        assertEquals(1.0, FastMath.exp(-0.0), 0.0);
    }

    @Test
    public void testExpOne() {
        assertEquals(Math.E, FastMath.exp(1.0), 1e-15);
    }

    @Test
    public void testExpLargePositive() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(1000.0), 0.0);
    }

    @Test
    public void testExpLargeNegative() {
        assertEquals(0.0, FastMath.exp(-1000.0), 1e-300);
    }

    @Test
    public void testExpNaN() {
        assertTrue(Double.isNaN(FastMath.exp(Double.NaN)));
    }

    @Test
    public void testExpInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.exp(Double.NEGATIVE_INFINITY), 0.0);
    }

    // ========== log tests ==========
    @Test
    public void testLogOne() {
        assertEquals(0.0, FastMath.log(1.0), 0.0);
    }

    @Test
    public void testLogE() {
        assertEquals(1.0, FastMath.log(Math.E), 1e-15);
    }

    @Test
    public void testLogZero() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(-0.0), 0.0);
    }

    @Test
    public void testLogNegative() {
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
    }

    @Test
    public void testLogInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void testLogNaN() {
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
    }

    // ========== log10 tests ==========
    @Test
    public void testLog10One() {
        assertEquals(0.0, FastMath.log10(1.0), 0.0);
    }

    @Test
    public void testLog10Ten() {
        assertEquals(1.0, FastMath.log10(10.0), 1e-15);
    }

    @Test
    public void testLog10Zero() {
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);
    }

    @Test
    public void testLog10Negative() {
        assertTrue(Double.isNaN(FastMath.log10(-1.0)));
    }

    // ========== pow tests ==========
    @Test
    public void testPowZeroExponent() {
        assertEquals(1.0, FastMath.pow(2.0, 0.0), 0.0);
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0); // 0^0 = 1 by convention
    }

    @Test
    public void testPowZeroBasePositiveExponent() {
        assertEquals(0.0, FastMath.pow(0.0, 2.0), 0.0);
    }

    @Test
    public void testPowZeroBaseNegativeExponent() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), 0.0);
    }

    @Test
    public void testPowNegativeBaseFractionalExponent() {
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 0.5)));
    }

    @Test
    public void testPowLargeOverflow() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(2.0, 1024.0), 0.0);
    }

    @Test
    public void testPowNaN() {
        assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.pow(1.0, Double.NaN)));
    }

    // ========== sqrt tests ==========
    @Test
    public void testSqrtZero() {
        assertEquals(0.0, FastMath.sqrt(0.0), 0.0);
        assertEquals(0.0, FastMath.sqrt(-0.0), 0.0);
    }

    @Test
    public void testSqrtPositive() {
        assertEquals(2.0, FastMath.sqrt(4.0), 1e-15);
    }

    @Test
    public void testSqrtNegative() {
        assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
    }

    @Test
    public void testSqrtInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sqrt(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void testSqrtNaN() {
        assertTrue(Double.isNaN(FastMath.sqrt(Double.NaN)));
    }

    // ========== cbrt tests ==========
    @Test
    public void testCbrtZero() {
        assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
    }

    @Test
    public void testCbrtPositive() {
        assertEquals(3.0, FastMath.cbrt(27.0), 1e-15);
    }

    @Test
    public void testCbrtNegative() {
        assertEquals(-3.0, FastMath.cbrt(-27.0), 1e-15);
    }

    @Test
    public void testCbrtNaN() {
        assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
    }

    // ========== hypot tests ==========
    @Test
    public void testHypotZero() {
        assertEquals(0.0, FastMath.hypot(0.0, 0.0), 0.0);
    }

    @Test
    public void testHypotNormal() {
        assertEquals(5.0, FastMath.hypot(3.0, 4.0), 1e-15);
    }

    @Test
    public void testHypotOverflow() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.MAX_VALUE, Double.MAX_VALUE), 0.0);
    }

    @Test
    public void testHypotUnderflow() {
        double tiny = Double.MIN_VALUE / 2.0;
        assertEquals(tiny, FastMath.hypot(tiny, 0.0), 1e-300);
    }

    @Test
    public void testHypotNaN() {
        assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.hypot(1.0, Double.NaN)));
    }

    // ========== sin tests ==========
    @Test
    public void testSinZero() {
        assertEquals(0.0, FastMath.sin(0.0), 0.0);
        assertEquals(0.0, FastMath.sin(-0.0), 0.0);
    }

    @Test
    public void testSinPiOver2() {
        assertEquals(1.0, FastMath.sin(Math.PI / 2.0), 1e-15);
    }

    @Test
    public void testSinPi() {
        assertEquals(0.0, FastMath.sin(Math.PI), 1e-15);
    }

    @Test
    public void testSinNaN() {
        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
    }

    @Test
    public void testSinInfinity() {
        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.sin(Double.NEGATIVE_INFINITY)));
    }

    // ========== cos tests ==========
    @Test
    public void testCosZero() {
        assertEquals(1.0, FastMath.cos(0.0), 0.0);
        assertEquals(1.0, FastMath.cos(-0.0), 0.0);
    }

    @Test
    public void testCosPi() {
        assertEquals(-1.0, FastMath.cos(Math.PI), 1e-15);
    }

    @Test
    public void testCosNaN() {
        assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
    }

    @Test
    public void testCosInfinity() {
        assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.cos(Double.NEGATIVE_INFINITY)));
    }

    // ========== tan tests ==========
    @Test
    public void testTanZero() {
        assertEquals(0.0, FastMath.tan(0.0), 0.0);
        assertEquals(0.0, FastMath.tan(-0.0), 0.0);
    }

    @Test
    public void testTanPiOver4() {
        assertEquals(1.0, FastMath.tan(Math.PI / 4.0), 1e-15);
    }

    @Test
    public void testTanNaN() {
        assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));
    }

    @Test
    public void testTanInfinity() {
        assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.tan(Double.NEGATIVE_INFINITY)));
    }

    // ========== asin tests ==========
    @Test
    public void testAsinZero() {
        assertEquals(0.0, FastMath.asin(0.0), 0.0);
    }

    @Test
    public void testAsinOne() {
        assertEquals(Math.PI / 2.0, FastMath.asin(1.0), 1e-15);
    }

    @Test
    public void testAsinNegativeOne() {
        assertEquals(-Math.PI / 2.0, FastMath.asin(-1.0), 1e-15);
    }

    @Test
    public void testAsinOutOfDomain() {
        assertTrue(Double.isNaN(FastMath.asin(1.1)));
        assertTrue(Double.isNaN(FastMath.asin(-1.1)));
    }

    @Test
    public void testAsinNaN() {
        assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));
    }

    // ========== acos tests ==========
    @Test
    public void testAcosZero() {
        assertEquals(Math.PI / 2.0, FastMath.acos(0.0), 1e-15);
    }

    @Test
    public void testAcosOne() {
        assertEquals(0.0, FastMath.acos(1.0), 1e-15);
    }

    @Test
    public void testAcosNegativeOne() {
        assertEquals(Math.PI, FastMath.acos(-1.0), 1e-15);
    }

    @Test
    public void testAcosOutOfDomain() {
        assertTrue(Double.isNaN(FastMath.acos(1.1)));
        assertTrue(Double.isNaN(FastMath.acos(-1.1)));
    }

    @Test
    public void testAcosNaN() {
        assertTrue(Double.isNaN(FastMath.acos(Double.NaN)));
    }

    // ========== atan tests ==========
    @Test
    public void testAtanZero() {
        assertEquals(0.0, FastMath.atan(0.0), 0.0);
    }

    @Test
    public void testAtanOne() {
        assertEquals(Math.PI / 4.0, FastMath.atan(1.0), 1e-15);
    }

    @Test
    public void testAtanInfinity() {
        assertEquals(Math.PI / 2.0, FastMath.atan(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(-Math.PI / 2.0, FastMath.atan(Double.NEGATIVE_INFINITY), 1e-15);
    }

    @Test
    public void testAtanNaN() {
        assertTrue(Double.isNaN(FastMath.atan(Double.NaN)));
    }

    // ========== atan2 tests ==========
    @Test
    public void testAtan2ZeroZero() {
        assertEquals(0.0, FastMath.atan2(0.0, 0.0), 0.0);
    }

    @Test
    public void testAtan2PositiveYZeroX() {
        assertEquals(Math.PI / 2.0, FastMath.atan2(1.0, 0.0), 1e-15);
    }

    @Test
    public void testAtan2NegativeYZeroX() {
        assertEquals(-Math.PI / 2.0, FastMath.atan2(-1.0, 0.0), 1e-15);
    }

    @Test
    public void testAtan2Infinity() {
        assertEquals(Math.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(3.0 * Math.PI / 4.0, FastMath.atan2(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), 1e-15);
    }

    @Test
    public void testAtan2NaN() {
        assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));
    }

    // ========== abs tests ==========
    @Test
    public void testAbsPositive() {
        assertEquals(2.0, FastMath.abs(2.0), 0.0);
    }

    @Test
    public void testAbsNegative() {
        assertEquals(2.0, FastMath.abs(-2.0), 0.0);
    }

    @Test
    public void testAbsZero() {
        assertEquals(0.0, FastMath.abs(0.0), 0.0);
        assertEquals(0.0, FastMath.abs(-0.0), 0.0);
    }

    @Test
    public void testAbsInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, FastMath.abs(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.abs(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test
    public void testAbsNaN() {
        assertTrue(Double.isNaN(FastMath.abs(Double.NaN)));
    }

    // ========== ceil, floor, rint, round tests ==========
    @Test
    public void testCeil() {
        assertEquals(2.0, FastMath.ceil(1.5), 0.0);
        assertEquals(-1.0, FastMath.ceil(-1.5), 0.0);
        assertEquals(1.0, FastMath.ceil(1.0), 0.0);
    }

    @Test
    public void testFloor() {
        assertEquals(1.0, FastMath.floor(1.5), 0.0);
        assertEquals(-2.0, FastMath.floor(-1.5), 0.0);
        assertEquals(1.0, FastMath.floor(1.0), 0.0);
    }

    @Test
    public void testRint() {
        assertEquals(2.0, FastMath.rint(1.5), 0.0);
        assertEquals(-2.0, FastMath.rint(-1.5), 0.0);
        assertEquals(1.0, FastMath.rint(1.0), 0.0);
    }

    @Test
    public void testRound() {
        assertEquals(2, FastMath.round(1.5f));
        assertEquals(-1, FastMath.round(-1.5f));
        assertEquals(1L, FastMath.round(1.5));
        assertEquals(-1L, FastMath.round(-1.5));
    }

    // ========== min, max tests ==========
    @Test
    public void testMin() {
        assertEquals(1.0, FastMath.min(1.0, 2.0), 0.0);
        assertEquals(1.0, FastMath.min(2.0, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.min(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.min(1.0, Double.NaN)));
    }

    @Test
    public void testMax() {
        assertEquals(2.0, FastMath.max(1.0, 2.0), 0.0);
        assertEquals(2.0, FastMath.max(2.0, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.max(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.max(1.0, Double.NaN)));
    }

    // ========== signum tests ==========
    @Test
    public void testSignum() {
        assertEquals(1.0, FastMath.signum(2.0), 0.0);
        assertEquals(-1.0, FastMath.signum(-2.0), 0.0);
        assertEquals(0.0, FastMath.signum(0.0), 0.0);
        assertEquals(0.0, FastMath.signum(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));
    }

    // ========== copySign tests ==========
    @Test
    public void testCopySign() {
        assertEquals(2.0, FastMath.copySign(2.0, 1.0), 0.0);
        assertEquals(-2.0, FastMath.copySign(2.0, -1.0), 0.0);
        assertEquals(2.0, FastMath.copySign(-2.0, 1.0), 0.0);
        assertEquals(-2.0, FastMath.copySign(-2.0, -1.0), 0.0);
    }

    // ========== nextAfter, nextUp, nextDown tests ==========
    @Test
    public void testNextAfter() {
        assertEquals(1.0 + Double.MIN_VALUE, FastMath.nextAfter(1.0, 2.0), 0.0);
        assertEquals(1.0 - Double.MIN_VALUE, FastMath.nextAfter(1.0, 0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.nextAfter(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE), 0.0);
    }

    @Test
    public void testNextUp() {
        assertEquals(1.0 + Double.MIN_VALUE, FastMath.nextUp(1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.nextUp(Double.MAX_VALUE), 0.0);
    }

    @Test
    public void testNextDown() {
        assertEquals(1.0 - Double.MIN_VALUE, FastMath.nextDown(1.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.nextDown(-Double.MAX_VALUE), 0.0);
    }

    // ========== scalb tests ==========
    @Test
    public void testScalb() {
        assertEquals(4.0, FastMath.scalb(1.0, 2), 0.0);
        assertEquals(0.5, FastMath.scalb(1.0, -1), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 1024), 0.0);
    }

    // ========== getExponent tests ==========
    @Test
    public void testGetExponent() {
        assertEquals(0, FastMath.getExponent(1.0));
        assertEquals(2, FastMath.getExponent(4.0));
        assertEquals(-2, FastMath.getExponent(0.25));
        assertEquals(Double.MAX_EXPONENT, FastMath.getExponent(Double.MAX_VALUE));
        assertEquals(Double.MIN_EXPONENT, FastMath.getExponent(Double.MIN_NORMAL));
    }

    // ========== log1p tests ==========
    @Test
    public void testLog1p() {
        assertEquals(0.0, FastMath.log1p(0.0), 0.0);
        assertEquals(Math.log(2.0), FastMath.log1p(1.0), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log1p(-2.0)));
    }

    // ========== expm1 tests ==========
    @Test
    public void testExpm1() {
        assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        assertEquals(Math.E - 1.0, FastMath.expm1(1.0), 1e-15);
        assertEquals(-1.0, FastMath.expm1(-1000.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.expm1(1000.0), 0.0);
    }

    // ========== toRadians / toDegrees tests ==========
    @Test
    public void testToRadians() {
        assertEquals(0.0, FastMath.toRadians(0.0), 0.0);
        assertEquals(Math.PI, FastMath.toRadians(180.0), 1e-15);
    }

    @Test
    public void testToDegrees() {
        assertEquals(0.0, FastMath.toDegrees(0.0), 0.0);
        assertEquals(180.0, FastMath.toDegrees(Math.PI), 1e-12);
    }

    // ========== Additional edge cases for coverage ==========
    @Test
    public void testCoshVerySmall() {
        double x = 1e-10;
        double expected = 1.0 + x * x / 2.0; // Taylor series
        assertEquals(expected, FastMath.cosh(x), 1e-20);
    }

    @Test
    public void testSinhVerySmall() {
        double x = 1e-10;
        double expected = x;
        assertEquals(expected, FastMath.sinh(x), 1e-20);
    }

    @Test
    public void testTanhVerySmall() {
        double x = 1e-10;
        double expected = x;
        assertEquals(expected, FastMath.tanh(x), 1e-20);
    }

    @Test
    public void testExpVerySmall() {
        double x = 1e-10;
        double expected = 1.0 + x;
        assertEquals(expected, FastMath.exp(x), 1e-20);
    }

    @Test
    public void testLogVeryCloseToOne() {
        double x = 1.0 + 1e-10;
        double expected = 1e-10;
        assertEquals(expected, FastMath.log(x), 1e-20);
    }

    @Test
    public void testPowSpecialCases() {
        assertEquals(0.0, FastMath.pow(0.0, 1.0), 0.0);
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1.0), 0.0);
        assertEquals(1.0, FastMath.pow(1.0, Double.NaN), 0.0); // 1^anything = 1
        assertEquals(1.0, FastMath.pow(Double.NaN, 0.0), 0.0); // anything^0 = 1
    }

    @Test
    public void testHypotSpecialCases() {
        assertEquals(0.0, FastMath.hypot(0.0, 0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(1.0, Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
    }

    @Test
    public void testAbsInt() {
        assertEquals(2, FastMath.abs(2));
        assertEquals(2, FastMath.abs(-2));
        assertEquals(Integer.MIN_VALUE, FastMath.abs(Integer.MIN_VALUE)); // special case
    }

    @Test
    public void testAbsLong() {
        assertEquals(2L, FastMath.abs(2L));
        assertEquals(2L, FastMath.abs(-2L));
        assertEquals(Long.MIN_VALUE, FastMath.abs(Long.MIN_VALUE)); // special case
    }

    @Test
    public void testMinMaxInt() {
        assertEquals(1, FastMath.min(1, 2));
        assertEquals(2, FastMath.max(1, 2));
    }

    @Test
    public void testMinMaxLong() {
        assertEquals(1L, FastMath.min(1L, 2L));
        assertEquals(2L, FastMath.max(1L, 2L));
    }

    @Test
    public void testMinMaxFloat() {
        assertEquals(1.0f, FastMath.min(1.0f, 2.0f), 0.0f);
        assertEquals(2.0f, FastMath.max(1.0f, 2.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.min(Float.NaN, 1.0f)));
        assertTrue(Float.isNaN(FastMath.max(Float.NaN, 1.0f)));
    }

    @Test
    public void testSignumFloat() {
        assertEquals(1.0f, FastMath.signum(2.0f), 0.0f);
        assertEquals(-1.0f, FastMath.signum(-2.0f), 0.0f);
        assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.signum(Float.NaN)));
    }

    @Test
    public void testCopySignFloat() {
        assertEquals(2.0f, FastMath.copySign(2.0f, 1.0f), 0.0f);
        assertEquals(-2.0f, FastMath.copySign(2.0f, -1.0f), 0.0f);
    }

    @Test
    public void testNextAfterFloat() {
        assertEquals(1.0f + Float.MIN_VALUE, FastMath.nextAfter(1.0f, 2.0f), 0.0f);
        assertEquals(1.0f - Float.MIN_VALUE, FastMath.nextAfter(1.0f, 0.0f), 0.0f);
    }

    @Test
    public void testNextUpFloat() {
        assertEquals(1.0f + Float.MIN_VALUE, FastMath.nextUp(1.0f), 0.0f);
    }

    @Test
    public void testNextDownFloat() {
        assertEquals(1.0f - Float.MIN_VALUE, FastMath.nextDown(1.0f), 0.0f);
    }

    @Test
    public void testScalbFloat() {
        assertEquals(4.0f, FastMath.scalb(1.0f, 2), 0.0f);
        assertEquals(0.5f, FastMath.scalb(1.0f, -1), 0.0f);
    }

    @Test
    public void testGetExponentFloat() {
        assertEquals(0, FastMath.getExponent(1.0f));
        assertEquals(2, FastMath.getExponent(4.0f));
    }

    @Test
    public void testRoundFloat() {
        assertEquals(2, FastMath.round(1.5f));
        assertEquals(-1, FastMath.round(-1.5f));
    }

    @Test
    public void testRoundDouble() {
        assertEquals(1L, FastMath.round(1.5));
        assertEquals(-1L, FastMath.round(-1.5));
    }

    @Test
    public void testIeeeRemainder() {
        assertEquals(1.0, FastMath.IEEEremainder(10.0, 3.0), 1e-15);
        assertEquals(-1.0, FastMath.IEEEremainder(11.0, 3.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.IEEEremainder(1.0, 0.0)));
    }

    @Test
    public void testUlp() {
        assertEquals(Double.MIN_VALUE, FastMath.ulp(1.0), 0.0);
        assertEquals(Double.MIN_VALUE, FastMath.ulp(-1.0), 0.0);
        assertEquals(Double.MIN_VALUE * 2, FastMath.ulp(2.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ulp(Double.POSITIVE_INFINITY), 0.0);
    }

    @Test
    public void testUlpFloat() {
        assertEquals(Float.MIN_VALUE, FastMath.ulp(1.0f), 0.0f);
        assertEquals(Float.MIN_VALUE, FastMath.ulp(-1.0f), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.ulp(Float.POSITIVE_INFINITY), 0.0f);
    }

    @Test
    public void testScalbDouble() {
        assertEquals(4.0, FastMath.scalb(1.0, 2), 0.0);
        assertEquals(0.5, FastMath.scalb(1.0, -1), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 1024), 0.0);
        assertEquals(0.0, FastMath.scalb(1.0, -1075), 0.0);
    }

    @Test
    public void testScalbFloat() {
        assertEquals(4.0f, FastMath.scalb(1.0f, 2), 0.0f);
        assertEquals(0.5f, FastMath.scalb(1.0f, -1), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.scalb(1.0f, 128), 0.0f);
        assertEquals(0.0f, FastMath.scalb(1.0f, -150), 0.0f);
    }

    @Test
    public void testHypotFloat() {
        assertEquals(5.0f, FastMath.hypot(3.0f, 4.0f), 1e-15f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.hypot(Float.MAX_VALUE, Float.MAX_VALUE), 0.0f);
    }

    @Test
    public void testPowFloat() {
        assertEquals(8.0f, FastMath.pow(2.0f, 3.0f), 1e-15f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.pow(2.0f, 128.0f), 0.0f);
    }

    @Test
    public void testLogFloat() {
        assertEquals(0.0f, FastMath.log(1.0f), 0.0f);
        assertEquals(1.0f, FastMath.log((float) Math.E), 1e-15f);
    }

    @Test
    public void testExpFloat() {
        assertEquals(1.0f, FastMath.exp(0.0f), 0.0f);
        assertEquals((float) Math.E, FastMath.exp(1.0f), 1e-15f);
    }

    @Test
    public void testSinCosFloat() {
        assertEquals(0.0f, FastMath.sin(0.0f), 0.0f);
        assertEquals(1.0f, FastMath.cos(0.0f), 0.0f);
    }

    @Test
    public void testTanFloat() {
        assertEquals(0.0f, FastMath.tan(0.0f), 0.0f);
        assertEquals(1.0f, FastMath.tan((float) (Math.PI / 4.0)), 1e-15f);
    }

    @Test
    public void testAsinAcosFloat() {
        assertEquals(0.0f, FastMath.asin(0.0f), 0.0f);
        assertEquals(0.0f, FastMath.acos(1.0f), 0.0f);
    }

    @Test
    public void testAtanFloat() {
        assertEquals(0.0f, FastMath.atan(0.0f), 0.0f);
        assertEquals((float) (Math.PI / 4.0), FastMath.atan(1.0f), 1e-15f);
    }

    @Test
    public void testAtan2Float() {
        assertEquals(0.0f, FastMath.atan2(0.0f, 1.0f), 0.0f);
        assertEquals((float) (Math.PI / 2.0), FastMath.atan2(1.0f, 0.0f), 1e-15f);
    }

    @Test
    public void testCoshFloat() {
        assertEquals(1.0f, FastMath.cosh(0.0f), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.cosh(100.0f), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.cosh(-100.0f), 0.0f);
    }

    @Test
    public void testSinhFloat() {
        assertEquals(0.0f, FastMath.sinh(0.0f), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.sinh(100.0f), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, FastMath.sinh(-100.0f), 0.0f);
    }

    @Test
    public void testTanhFloat() {
        assertEquals(0.0f, FastMath.tanh(0.0f), 0.0f);
        assertEquals(1.0f, FastMath.tanh(100.0f), 1e-15f);
        assertEquals(-1.0f, FastMath.tanh(-100.0f), 1e-15f);
    }

    @Test
    public void testSqrtFloat() {
        assertEquals(2.0f, FastMath.sqrt(4.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.sqrt(-1.0f)));
    }

    @Test
    public void testCbrtFloat() {
        assertEquals(3.0f, FastMath.cbrt(27.0f), 1e-15f);
        assertEquals(-3.0f, FastMath.cbrt(-27.0f), 1e-15f);
    }

    @Test
    public void testToRadiansFloat() {
        assertEquals(0.0f, FastMath.toRadians(0.0f), 0.0f);
        assertEquals((float) Math.PI, FastMath.toRadians(180.0f), 1e-15f);
    }

    @Test
    public void testToDegreesFloat() {
        assertEquals(0.0f, FastMath.toDegrees(0.0f), 0.0f);
        assertEquals(180.0f, FastMath.toDegrees((float) Math.PI), 1e-12f);
    }
}