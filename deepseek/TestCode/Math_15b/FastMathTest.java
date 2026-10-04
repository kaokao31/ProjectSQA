package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for FastMath.
 * Designed to achieve high code coverage and detect potential faults,
 * including the known bug #15 (pow with negative base and non-integer exponent).
 */
public class FastMathTest {

    // --- pow(double, double) ---
    @Test
    public void testPowBasic() {
        assertEquals("pow(2.0, 3.0)", 8.0, FastMath.pow(2.0, 3.0), 1e-15);
        assertEquals("pow(4.0, 0.5)", 2.0, FastMath.pow(4.0, 0.5), 1e-15);
        assertEquals("pow(0.0, 0.0)", 1.0, FastMath.pow(0.0, 0.0), 0.0);
        assertEquals("pow(0.0, 5.0)", 0.0, FastMath.pow(0.0, 5.0), 0.0);
        assertEquals("pow(0.0, -1.0)", Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1.0), 0.0);
        assertEquals("pow(0.0, -2.0)", Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), 0.0);
        assertEquals("pow(-2.0, 2.0)", 4.0, FastMath.pow(-2.0, 2.0), 1e-15);
        assertEquals("pow(-2.0, 3.0)", -8.0, FastMath.pow(-2.0, 3.0), 1e-15);
    }

    @Test
    public void testPowNegativeBaseNonIntegerExponent() {
        // Known bug #15: should return NaN
        assertTrue("pow(-2.0, 0.5) should be NaN",
                   Double.isNaN(FastMath.pow(-2.0, 0.5)));
        assertTrue("pow(-1.0, 0.5) should be NaN",
                   Double.isNaN(FastMath.pow(-1.0, 0.5)));
        assertTrue("pow(-0.5, 0.5) should be NaN",
                   Double.isNaN(FastMath.pow(-0.5, 0.5)));
    }

    @Test
    public void testPowSpecialCases() {
        // NaN base
        assertTrue("pow(NaN, 0.0) should be 1.0",
                   FastMath.pow(Double.NaN, 0.0) == 1.0);
        assertTrue("pow(NaN, 1.0) should be NaN",
                   Double.isNaN(FastMath.pow(Double.NaN, 1.0)));
        // Infinity base
        assertEquals("pow(Inf, 0.0)", 1.0, FastMath.pow(Double.POSITIVE_INFINITY, 0.0), 0.0);
        assertEquals("pow(Inf, 1.0)", Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals("pow(Inf, -1.0)", 0.0, FastMath.pow(Double.POSITIVE_INFINITY, -1.0), 0.0);
        assertEquals("pow(-Inf, 2.0)", Double.POSITIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 2.0), 0.0);
        assertEquals("pow(-Inf, 3.0)", Double.NEGATIVE_INFINITY, FastMath.pow(Double.NEGATIVE_INFINITY, 3.0), 0.0);
        // Exponent NaN
        assertTrue("pow(2.0, NaN) should be NaN",
                   Double.isNaN(FastMath.pow(2.0, Double.NaN)));
        // Exponent Infinity
        assertEquals("pow(2.0, Inf)", Double.POSITIVE_INFINITY, FastMath.pow(2.0, Double.POSITIVE_INFINITY), 0.0);
        assertEquals("pow(0.5, Inf)", 0.0, FastMath.pow(0.5, Double.POSITIVE_INFINITY), 0.0);
        assertEquals("pow(2.0, -Inf)", 0.0, FastMath.pow(2.0, Double.NEGATIVE_INFINITY), 0.0);
        assertEquals("pow(0.5, -Inf)", Double.POSITIVE_INFINITY, FastMath.pow(0.5, Double.NEGATIVE_INFINITY), 0.0);
        // 1.0 base
        assertEquals("pow(1.0, any)", 1.0, FastMath.pow(1.0, 100.0), 0.0);
        assertEquals("pow(1.0, NaN)", 1.0, FastMath.pow(1.0, Double.NaN), 0.0);
        // -1.0 base with infinity exponent
        assertTrue("pow(-1.0, Inf) should be NaN",
                   Double.isNaN(FastMath.pow(-1.0, Double.POSITIVE_INFINITY)));
        assertTrue("pow(-1.0, -Inf) should be NaN",
                   Double.isNaN(FastMath.pow(-1.0, Double.NEGATIVE_INFINITY)));
    }

    // --- exp(double) ---
    @Test
    public void testExp() {
        assertEquals("exp(0.0)", 1.0, FastMath.exp(0.0), 1e-15);
        assertEquals("exp(1.0)", Math.E, FastMath.exp(1.0), 1e-15);
        assertEquals("exp(-1.0)", 1.0 / Math.E, FastMath.exp(-1.0), 1e-15);
        assertEquals("exp(2.0)", Math.exp(2.0), FastMath.exp(2.0), 1e-15);
        assertEquals("exp(-2.0)", Math.exp(-2.0), FastMath.exp(-2.0), 1e-15);
        // Large positive
        assertEquals("exp(709.0)", Math.exp(709.0), FastMath.exp(709.0), 1e-15);
        // Large negative
        assertEquals("exp(-745.0)", Math.exp(-745.0), FastMath.exp(-745.0), 1e-15);
        // Overflow to infinity
        assertEquals("exp(710.0)", Double.POSITIVE_INFINITY, FastMath.exp(710.0), 0.0);
        // Underflow to zero
        assertEquals("exp(-746.0)", 0.0, FastMath.exp(-746.0), 0.0);
        // NaN
        assertTrue("exp(NaN) should be NaN",
                   Double.isNaN(FastMath.exp(Double.NaN)));
        // Infinity
        assertEquals("exp(Inf)", Double.POSITIVE_INFINITY, FastMath.exp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("exp(-Inf)", 0.0, FastMath.exp(Double.NEGATIVE_INFINITY), 0.0);
    }

    // --- log(double) ---
    @Test
    public void testLog() {
        assertEquals("log(1.0)", 0.0, FastMath.log(1.0), 1e-15);
        assertEquals("log(Math.E)", 1.0, FastMath.log(Math.E), 1e-15);
        assertEquals("log(10.0)", Math.log(10.0), FastMath.log(10.0), 1e-15);
        assertEquals("log(0.5)", Math.log(0.5), FastMath.log(0.5), 1e-15);
        // Zero and negative
        assertEquals("log(0.0)", Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertTrue("log(-1.0) should be NaN",
                   Double.isNaN(FastMath.log(-1.0)));
        // NaN and Infinity
        assertTrue("log(NaN) should be NaN",
                   Double.isNaN(FastMath.log(Double.NaN)));
        assertEquals("log(Inf)", Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        // Very small positive
        assertEquals("log(Double.MIN_VALUE)", Math.log(Double.MIN_VALUE), FastMath.log(Double.MIN_VALUE), 1e-15);
    }

    // --- log10(double) ---
    @Test
    public void testLog10() {
        assertEquals("log10(1.0)", 0.0, FastMath.log10(1.0), 1e-15);
        assertEquals("log10(10.0)", 1.0, FastMath.log10(10.0), 1e-15);
        assertEquals("log10(100.0)", 2.0, FastMath.log10(100.0), 1e-15);
        assertEquals("log10(0.1)", -1.0, FastMath.log10(0.1), 1e-15);
        // Special cases
        assertEquals("log10(0.0)", Double.NEGATIVE_INFINITY, FastMath.log10(0.0), 0.0);
        assertTrue("log10(-1.0) should be NaN",
                   Double.isNaN(FastMath.log10(-1.0)));
        assertTrue("log10(NaN) should be NaN",
                   Double.isNaN(FastMath.log10(Double.NaN)));
        assertEquals("log10(Inf)", Double.POSITIVE_INFINITY, FastMath.log10(Double.POSITIVE_INFINITY), 0.0);
    }

    // --- sin, cos, tan ---
    @Test
    public void testSin() {
        assertEquals("sin(0.0)", 0.0, FastMath.sin(0.0), 1e-15);
        assertEquals("sin(PI/2)", 1.0, FastMath.sin(Math.PI / 2), 1e-15);
        assertEquals("sin(PI)", 0.0, FastMath.sin(Math.PI), 1e-15);
        assertEquals("sin(3*PI/2)", -1.0, FastMath.sin(3 * Math.PI / 2), 1e-15);
        // Large values
        assertEquals("sin(1e10)", Math.sin(1e10), FastMath.sin(1e10), 1e-15);
        // NaN and Infinity
        assertTrue("sin(NaN) should be NaN",
                   Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue("sin(Inf) should be NaN",
                   Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue("sin(-Inf) should be NaN",
                   Double.isNaN(FastMath.sin(Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void testCos() {
        assertEquals("cos(0.0)", 1.0, FastMath.cos(0.0), 1e-15);
        assertEquals("cos(PI/2)", 0.0, FastMath.cos(Math.PI / 2), 1e-15);
        assertEquals("cos(PI)", -1.0, FastMath.cos(Math.PI), 1e-15);
        assertEquals("cos(3*PI/2)", 0.0, FastMath.cos(3 * Math.PI / 2), 1e-15);
        // Large values
        assertEquals("cos(1e10)", Math.cos(1e10), FastMath.cos(1e10), 1e-15);
        // NaN and Infinity
        assertTrue("cos(NaN) should be NaN",
                   Double.isNaN(FastMath.cos(Double.NaN)));
        assertTrue("cos(Inf) should be NaN",
                   Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        assertTrue("cos(-Inf) should be NaN",
                   Double.isNaN(FastMath.cos(Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void testTan() {
        assertEquals("tan(0.0)", 0.0, FastMath.tan(0.0), 1e-15);
        assertEquals("tan(PI/4)", 1.0, FastMath.tan(Math.PI / 4), 1e-15);
        assertEquals("tan(-PI/4)", -1.0, FastMath.tan(-Math.PI / 4), 1e-15);
        // Large values
        assertEquals("tan(1e10)", Math.tan(1e10), FastMath.tan(1e10), 1e-15);
        // NaN and Infinity
        assertTrue("tan(NaN) should be NaN",
                   Double.isNaN(FastMath.tan(Double.NaN)));
        assertTrue("tan(Inf) should be NaN",
                   Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
        assertTrue("tan(-Inf) should be NaN",
                   Double.isNaN(FastMath.tan(Double.NEGATIVE_INFINITY)));
    }

    // --- sqrt, cbrt ---
    @Test
    public void testSqrt() {
        assertEquals("sqrt(0.0)", 0.0, FastMath.sqrt(0.0), 0.0);
        assertEquals("sqrt(1.0)", 1.0, FastMath.sqrt(1.0), 1e-15);
        assertEquals("sqrt(4.0)", 2.0, FastMath.sqrt(4.0), 1e-15);
        assertEquals("sqrt(0.25)", 0.5, FastMath.sqrt(0.25), 1e-15);
        // Negative
        assertTrue("sqrt(-1.0) should be NaN",
                   Double.isNaN(FastMath.sqrt(-1.0)));
        // NaN and Infinity
        assertTrue("sqrt(NaN) should be NaN",
                   Double.isNaN(FastMath.sqrt(Double.NaN)));
        assertEquals("sqrt(Inf)", Double.POSITIVE_INFINITY, FastMath.sqrt(Double.POSITIVE_INFINITY), 0.0);
        // Large value
        assertEquals("sqrt(Double.MAX_VALUE)", Math.sqrt(Double.MAX_VALUE), FastMath.sqrt(Double.MAX_VALUE), 1e-15);
    }

    @Test
    public void testCbrt() {
        assertEquals("cbrt(0.0)", 0.0, FastMath.cbrt(0.0), 0.0);
        assertEquals("cbrt(1.0)", 1.0, FastMath.cbrt(1.0), 1e-15);
        assertEquals("cbrt(8.0)", 2.0, FastMath.cbrt(8.0), 1e-15);
        assertEquals("cbrt(-8.0)", -2.0, FastMath.cbrt(-8.0), 1e-15);
        // NaN and Infinity
        assertTrue("cbrt(NaN) should be NaN",
                   Double.isNaN(FastMath.cbrt(Double.NaN)));
        assertEquals("cbrt(Inf)", Double.POSITIVE_INFINITY, FastMath.cbrt(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("cbrt(-Inf)", Double.NEGATIVE_INFINITY, FastMath.cbrt(Double.NEGATIVE_INFINITY), 0.0);
    }

    // --- hypot ---
    @Test
    public void testHypot() {
        assertEquals("hypot(3,4)", 5.0, FastMath.hypot(3.0, 4.0), 1e-15);
        assertEquals("hypot(0,0)", 0.0, FastMath.hypot(0.0, 0.0), 0.0);
        // Overflow/underflow safe
        double huge = Double.MAX_VALUE / 2;
        assertEquals("hypot(huge, huge)", Math.hypot(huge, huge), FastMath.hypot(huge, huge), 1e-15);
        double tiny = Double.MIN_VALUE * 2;
        assertEquals("hypot(tiny, tiny)", Math.hypot(tiny, tiny), FastMath.hypot(tiny, tiny), 1e-15);
        // NaN and Infinity
        assertTrue("hypot(NaN, 1) should be NaN",
                   Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
        assertEquals("hypot(Inf, 1)", Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals("hypot(-Inf, 1)", Double.POSITIVE_INFINITY, FastMath.hypot(Double.NEGATIVE_INFINITY, 1.0), 0.0);
    }

    // --- ceil, floor, rint, round ---
    @Test
    public void testCeil() {
        assertEquals("ceil(1.5)", 2.0, FastMath.ceil(1.5), 0.0);
        assertEquals("ceil(-1.5)", -1.0, FastMath.ceil(-1.5), 0.0);
        assertEquals("ceil(0.0)", 0.0, FastMath.ceil(0.0), 0.0);
        assertEquals("ceil(Double.NaN)", Double.NaN, FastMath.ceil(Double.NaN), 0.0);
        assertEquals("ceil(Inf)", Double.POSITIVE_INFINITY, FastMath.ceil(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("ceil(-Inf)", Double.NEGATIVE_INFINITY, FastMath.ceil(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test
    public void testFloor() {
        assertEquals("floor(1.5)", 1.0, FastMath.floor(1.5), 0.0);
        assertEquals("floor(-1.5)", -2.0, FastMath.floor(-1.5), 0.0);
        assertEquals("floor(0.0)", 0.0, FastMath.floor(0.0), 0.0);
        assertEquals("floor(Double.NaN)", Double.NaN, FastMath.floor(Double.NaN), 0.0);
        assertEquals("floor(Inf)", Double.POSITIVE_INFINITY, FastMath.floor(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("floor(-Inf)", Double.NEGATIVE_INFINITY, FastMath.floor(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test
    public void testRint() {
        assertEquals("rint(1.5)", 2.0, FastMath.rint(1.5), 0.0);
        assertEquals("rint(2.5)", 2.0, FastMath.rint(2.5), 0.0);
        assertEquals("rint(-1.5)", -2.0, FastMath.rint(-1.5), 0.0);
        assertEquals("rint(0.0)", 0.0, FastMath.rint(0.0), 0.0);
        assertEquals("rint(Double.NaN)", Double.NaN, FastMath.rint(Double.NaN), 0.0);
        assertEquals("rint(Inf)", Double.POSITIVE_INFINITY, FastMath.rint(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("rint(-Inf)", Double.NEGATIVE_INFINITY, FastMath.rint(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test
    public void testRound() {
        assertEquals("round(1.5)", 2L, FastMath.round(1.5));
        assertEquals("round(1.4)", 1L, FastMath.round(1.4));
        assertEquals("round(-1.5)", -1L, FastMath.round(-1.5)); // note: round(-1.5) = -1 (ties to even? Actually Math.round(-1.5) = -1)
        assertEquals("round(-1.6)", -2L, FastMath.round(-1.6));
        assertEquals("round(0.0)", 0L, FastMath.round(0.0));
        // Special: large values
        assertEquals("round(Double.MAX_VALUE)", Long.MAX_VALUE, FastMath.round(Double.MAX_VALUE));
        assertEquals("round(-Double.MAX_VALUE)", Long.MIN_VALUE, FastMath.round(-Double.MAX_VALUE));
        // NaN and Infinity: should throw? Actually Math.round(NaN) = 0L, Math.round(Inf) = Long.MAX_VALUE, etc.
        assertEquals("round(NaN)", 0L, FastMath.round(Double.NaN));
        assertEquals("round(Inf)", Long.MAX_VALUE, FastMath.round(Double.POSITIVE_INFINITY));
        assertEquals("round(-Inf)", Long.MIN_VALUE, FastMath.round(Double.NEGATIVE_INFINITY));
    }

    // --- abs, signum ---
    @Test
    public void testAbs() {
        assertEquals("abs(0.0)", 0.0, FastMath.abs(0.0), 0.0);
        assertEquals("abs(1.0)", 1.0, FastMath.abs(1.0), 0.0);
        assertEquals("abs(-1.0)", 1.0, FastMath.abs(-1.0), 0.0);
        assertEquals("abs(Double.NaN)", Double.NaN, FastMath.abs(Double.NaN), 0.0);
        assertEquals("abs(-Inf)", Double.POSITIVE_INFINITY, FastMath.abs(Double.NEGATIVE_INFINITY), 0.0);
        // Integer version
        assertEquals("abs(int -1)", 1, FastMath.abs(-1));
        assertEquals("abs(int 0)", 0, FastMath.abs(0));
        assertEquals("abs(int Integer.MIN_VALUE)", Integer.MIN_VALUE, FastMath.abs(Integer.MIN_VALUE)); // edge case
        // Long version
        assertEquals("abs(long -1L)", 1L, FastMath.abs(-1L));
        assertEquals("abs(long Long.MIN_VALUE)", Long.MIN_VALUE, FastMath.abs(Long.MIN_VALUE));
    }

    @Test
    public void testSignum() {
        assertEquals("signum(0.0)", 0.0, FastMath.signum(0.0), 0.0);
        assertEquals("signum(5.0)", 1.0, FastMath.signum(5.0), 0.0);
        assertEquals("signum(-5.0)", -1.0, FastMath.signum(-5.0), 0.0);
        assertEquals("signum(Double.NaN)", Double.NaN, FastMath.signum(Double.NaN), 0.0);
        assertEquals("signum(Inf)", 1.0, FastMath.signum(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("signum(-Inf)", -1.0, FastMath.signum(Double.NEGATIVE_INFINITY), 0.0);
    }

    // --- max, min ---
    @Test
    public void testMaxMin() {
        assertEquals("max(1,2)", 2.0, FastMath.max(1.0, 2.0), 0.0);
        assertEquals("max(2,1)", 2.0, FastMath.max(2.0, 1.0), 0.0);
        assertEquals("max(NaN,1)", Double.NaN, FastMath.max(Double.NaN, 1.0), 0.0);
        assertEquals("max(1,NaN)", Double.NaN, FastMath.max(1.0, Double.NaN), 0.0);
        assertEquals("min(1,2)", 1.0, FastMath.min(1.0, 2.0), 0.0);
        assertEquals("min(2,1)", 1.0, FastMath.min(2.0, 1.0), 0.0);
        assertEquals("min(NaN,1)", Double.NaN, FastMath.min(Double.NaN, 1.0), 0.0);
        assertEquals("min(1,NaN)", Double.NaN, FastMath.min(1.0, Double.NaN), 0.0);
        // Integer versions
        assertEquals("max(int 1,2)", 2, FastMath.max(1, 2));
        assertEquals("min(int 1,2)", 1, FastMath.min(1, 2));
        assertEquals("max(long 1L,2L)", 2L, FastMath.max(1L, 2L));
        assertEquals("min(long 1L,2L)", 1L, FastMath.min(1L, 2L));
    }

    // --- scalb, nextAfter, nextUp, nextDown ---
    @Test
    public void testScalb() {
        assertEquals("scalb(1.0, 2)", 4.0, FastMath.scalb(1.0, 2), 0.0);
        assertEquals("scalb(1.0, -2)", 0.25, FastMath.scalb(1.0, -2), 1e-15);
        assertEquals("scalb(0.0, 100)", 0.0, FastMath.scalb(0.0, 100), 0.0);
        assertEquals("scalb(Inf, 1)", Double.POSITIVE_INFINITY, FastMath.scalb(Double.POSITIVE_INFINITY, 1), 0.0);
        assertEquals("scalb(NaN, 1)", Double.NaN, FastMath.scalb(Double.NaN, 1), 0.0);
    }

    @Test
    public void testNextAfter() {
        assertEquals("nextAfter(0.0, 1.0)", Double.MIN_VALUE, FastMath.nextAfter(0.0, 1.0), 0.0);
        assertEquals("nextAfter(0.0, -1.0)", -Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        assertEquals("nextAfter(1.0, 2.0)", Math.nextAfter(1.0, 2.0), FastMath.nextAfter(1.0, 2.0), 0.0);
        assertEquals("nextAfter(1.0, 0.0)", Math.nextAfter(1.0, 0.0), FastMath.nextAfter(1.0, 0.0), 0.0);
        // NaN and Infinity
        assertTrue("nextAfter(NaN, 1) should be NaN",
                   Double.isNaN(FastMath.nextAfter(Double.NaN, 1.0)));
        assertEquals("nextAfter(Inf, 1)", Double.POSITIVE_INFINITY, FastMath.nextAfter(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals("nextAfter(Inf, -1)", Double.MAX_VALUE, FastMath.nextAfter(Double.POSITIVE_INFINITY, -1.0), 0.0);
    }

    @Test
    public void testNextUp() {
        assertEquals("nextUp(0.0)", Double.MIN_VALUE, FastMath.nextUp(0.0), 0.0);
        assertEquals("nextUp(1.0)", Math.nextUp(1.0), FastMath.nextUp(1.0), 0.0);
        assertEquals("nextUp(-1.0)", Math.nextUp(-1.0), FastMath.nextUp(-1.0), 0.0);
        assertEquals("nextUp(Inf)", Double.POSITIVE_INFINITY, FastMath.nextUp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("nextUp(-Inf)", -Double.MAX_VALUE, FastMath.nextUp(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue("nextUp(NaN) should be NaN",
                   Double.isNaN(FastMath.nextUp(Double.NaN)));
    }

    @Test
    public void testNextDown() {
        assertEquals("nextDown(0.0)", -Double.MIN_VALUE, FastMath.nextDown(0.0), 0.0);
        assertEquals("nextDown(1.0)", Math.nextDown(1.0), FastMath.nextDown(1.0), 0.0);
        assertEquals("nextDown(-1.0)", Math.nextDown(-1.0), FastMath.nextDown(-1.0), 0.0);
        assertEquals("nextDown(Inf)", Double.MAX_VALUE, FastMath.nextDown(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("nextDown(-Inf)", Double.NEGATIVE_INFINITY, FastMath.nextDown(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue("nextDown(NaN) should be NaN",
                   Double.isNaN(FastMath.nextDown(Double.NaN)));
    }

    // --- ulp, copySign, getExponent ---
    @Test
    public void testUlp() {
        assertEquals("ulp(0.0)", Double.MIN_VALUE, FastMath.ulp(0.0), 0.0);
        assertEquals("ulp(1.0)", Math.ulp(1.0), FastMath.ulp(1.0), 0.0);
        assertEquals("ulp(-1.0)", Math.ulp(-1.0), FastMath.ulp(-1.0), 0.0);
        assertEquals("ulp(Inf)", Double.POSITIVE_INFINITY, FastMath.ulp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("ulp(NaN)", Double.NaN, FastMath.ulp(Double.NaN), 0.0);
    }

    @Test
    public void testCopySign() {
        assertEquals("copySign(1.0, -1.0)", -1.0, FastMath.copySign(1.0, -1.0), 0.0);
        assertEquals("copySign(-1.0, 1.0)", 1.0, FastMath.copySign(-1.0, 1.0), 0.0);
        assertEquals("copySign(0.0, -1.0)", -0.0, FastMath.copySign(0.0, -1.0), 0.0);
        assertEquals("copySign(0.0, 1.0)", 0.0, FastMath.copySign(0.0, 1.0), 0.0);
        assertEquals("copySign(NaN, 1.0)", Double.NaN, FastMath.copySign(Double.NaN, 1.0), 0.0);
        assertEquals("copySign(1.0, NaN)", 1.0, FastMath.copySign(1.0, Double.NaN), 0.0); // sign of NaN is positive? Actually Math.copySign(1, NaN) returns 1
    }

    @Test
    public void testGetExponent() {
        assertEquals("getExponent(1.0)", 0, FastMath.getExponent(1.0));
        assertEquals("getExponent(2.0)", 1, FastMath.getExponent(2.0));
        assertEquals("getExponent(0.5)", -1, FastMath.getExponent(0.5));
        assertEquals("getExponent(0.0)", -1023, FastMath.getExponent(0.0)); // Double.MIN_EXPONENT - 1? Actually Math.getExponent(0) = -1023
        assertEquals("getExponent(Inf)", Integer.MAX_VALUE, FastMath.getExponent(Double.POSITIVE_INFINITY));
        assertEquals("getExponent(NaN)", Integer.MAX_VALUE, FastMath.getExponent(Double.NaN));
    }

    // --- toDegrees, toRadians ---
    @Test
    public void testToDegrees() {
        assertEquals("toDegrees(PI)", 180.0, FastMath.toDegrees(Math.PI), 1e-15);
        assertEquals("toDegrees(0)", 0.0, FastMath.toDegrees(0.0), 0.0);
        assertEquals("toDegrees(2*PI)", 360.0, FastMath.toDegrees(2 * Math.PI), 1e-15);
    }

    @Test
    public void testToRadians() {
        assertEquals("toRadians(180)", Math.PI, FastMath.toRadians(180.0), 1e-15);
        assertEquals("toRadians(0)", 0.0, FastMath.toRadians(0.0), 0.0);
        assertEquals("toRadians(360)", 2 * Math.PI, FastMath.toRadians(360.0), 1e-15);
    }

    // --- IEEEremainder ---
    @Test
    public void testIEEEremainder() {
        assertEquals("IEEEremainder(5.0, 2.0)", 1.0, FastMath.IEEEremainder(5.0, 2.0), 1e-15);
        assertEquals("IEEEremainder(5.0, 2.5)", 0.0, FastMath.IEEEremainder(5.0, 2.5), 1e-15);
        assertEquals("IEEEremainder(5.0, 0.0)", Double.NaN, FastMath.IEEEremainder(5.0, 0.0), 0.0);
        assertEquals("IEEEremainder(Inf, 1.0)", Double.NaN, FastMath.IEEEremainder(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals("IEEEremainder(5.0, Inf)", 5.0, FastMath.IEEEremainder(5.0, Double.POSITIVE_INFINITY), 1e-15);
    }

    // --- asin, acos, atan, atan2 ---
    @Test
    public void testAsin() {
        assertEquals("asin(0.0)", 0.0, FastMath.asin(0.0), 1e-15);
        assertEquals("asin(1.0)", Math.PI / 2, FastMath.asin(1.0), 1e-15);
        assertEquals("asin(-1.0)", -Math.PI / 2, FastMath.asin(-1.0), 1e-15);
        assertTrue("asin(1.1) should be NaN",
                   Double.isNaN(FastMath.asin(1.1)));
        assertTrue("asin(NaN) should be NaN",
                   Double.isNaN(FastMath.asin(Double.NaN)));
    }

    @Test
    public void testAcos() {
        assertEquals("acos(1.0)", 0.0, FastMath.acos(1.0), 1e-15);
        assertEquals("acos(0.0)", Math.PI / 2, FastMath.acos(0.0), 1e-15);
        assertEquals("acos(-1.0)", Math.PI, FastMath.acos(-1.0), 1e-15);
        assertTrue("acos(1.1) should be NaN",
                   Double.isNaN(FastMath.acos(1.1)));
        assertTrue("acos(NaN) should be NaN",
                   Double.isNaN(FastMath.acos(Double.NaN)));
    }

    @Test
    public void testAtan() {
        assertEquals("atan(0.0)", 0.0, FastMath.atan(0.0), 1e-15);
        assertEquals("atan(1.0)", Math.PI / 4, FastMath.atan(1.0), 1e-15);
        assertEquals("atan(-1.0)", -Math.PI / 4, FastMath.atan(-1.0), 1e-15);
        assertEquals("atan(Inf)", Math.PI / 2, FastMath.atan(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals("atan(-Inf)", -Math.PI / 2, FastMath.atan(Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue("atan(NaN) should be NaN",
                   Double.isNaN(FastMath.atan(Double.NaN)));
    }

    @Test
    public void testAtan2() {
        assertEquals("atan2(0, 1)", 0.0, FastMath.atan2(0.0, 1.0), 1e-15);
        assertEquals("atan2(1, 0)", Math.PI / 2, FastMath.atan2(1.0, 0.0), 1e-15);
        assertEquals("atan2(0, -1)", Math.PI, FastMath.atan2(0.0, -1.0), 1e-15);
        assertEquals("atan2(-1, 0)", -Math.PI / 2, FastMath.atan2(-1.0, 0.0), 1e-15);
        assertEquals("atan2(1, 1)", Math.PI / 4, FastMath.atan2(1.0, 1.0), 1e-15);
        // Special cases
        assertEquals("atan2(0, 0)", 0.0, FastMath.atan2(0.0, 0.0), 0.0);
        assertEquals("atan2(Inf, 1)", Math.PI / 2, FastMath.atan2(Double.POSITIVE_INFINITY, 1.0), 1e-15);
        assertEquals("atan2(-Inf, 1)", -Math.PI / 2, FastMath.atan2(Double.NEGATIVE_INFINITY, 1.0), 1e-15);
        assertEquals("atan2(1, Inf)", 0.0, FastMath.atan2(1.0, Double.POSITIVE_INFINITY), 1e-15);
        assertEquals("atan2(1, -Inf)", Math.PI, FastMath.atan2(1.0, Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue("atan2(NaN, 1) should be NaN",
                   Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        assertTrue("atan2(1, NaN) should be NaN",
                   Double.isNaN(FastMath.atan2(1.0, Double.NaN)));
    }

    // --- sinh, cosh, tanh ---
    @Test
    public void testSinh() {
        assertEquals("sinh(0.0)", 0.0, FastMath.sinh(0.0), 1e-15);
        assertEquals("sinh(1.0)", Math.sinh(1.0), FastMath.sinh(1.0), 1e-15);
        assertEquals("sinh(-1.0)", Math.sinh(-1.0), FastMath.sinh(-1.0), 1e-15);
        assertEquals("sinh(Inf)", Double.POSITIVE_INFINITY, FastMath.sinh(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("sinh(-Inf)", Double.NEGATIVE_INFINITY, FastMath.sinh(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue("sinh(NaN) should be NaN",
                   Double.isNaN(FastMath.sinh(Double.NaN)));
    }

    @Test
    public void testCosh() {
        assertEquals("cosh(0.0)", 1.0, FastMath.cosh(0.0), 1e-15);
        assertEquals("cosh(1.0)", Math.cosh(1.0), FastMath.cosh(1.0), 1e-15);
        assertEquals("cosh(-1.0)", Math.cosh(-1.0), FastMath.cosh(-1.0), 1e-15);
        assertEquals("cosh(Inf)", Double.POSITIVE_INFINITY, FastMath.cosh(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("cosh(-Inf)", Double.POSITIVE_INFINITY, FastMath.cosh(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue("cosh(NaN) should be NaN",
                   Double.isNaN(FastMath.cosh(Double.NaN)));
    }

    @Test
    public void testTanh() {
        assertEquals("tanh(0.0)", 0.0, FastMath.tanh(0.0), 1e-15);
        assertEquals("tanh(1.0)", Math.tanh(1.0), FastMath.tanh(1.0), 1e-15);
        assertEquals("tanh(-1.0)", Math.tanh(-1.0), FastMath.tanh(-1.0), 1e-15);
        assertEquals("tanh(Inf)", 1.0, FastMath.tanh(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals("tanh(-Inf)", -1.0, FastMath.tanh(Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue("tanh(NaN) should be NaN",
                   Double.isNaN(FastMath.tanh(Double.NaN)));
    }

    // --- log1p, expm1 ---
    @Test
    public void testLog1p() {
        assertEquals("log1p(0.0)", 0.0, FastMath.log1p(0.0), 1e-15);
        assertEquals("log1p(1.0)", Math.log1p(1.0), FastMath.log1p(1.0), 1e-15);
        assertEquals("log1p(-0.5)", Math.log1p(-0.5), FastMath.log1p(-0.5), 1e-15);
        assertEquals("log1p(-1.0)", Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertTrue("log1p(-2.0) should be NaN",
                   Double.isNaN(FastMath.log1p(-2.0)));
        assertTrue("log1p(NaN) should be NaN",
                   Double.isNaN(FastMath.log1p(Double.NaN)));
    }

    @Test
    public void testExpm1() {
        assertEquals("expm1(0.0)", 0.0, FastMath.expm1(0.0), 1e-15);
        assertEquals("expm1(1.0)", Math.expm1(1.0), FastMath.expm1(1.0), 1e-15);
        assertEquals("expm1(-1.0)", Math.expm1(-1.0), FastMath.expm1(-1.0), 1e-15);
        assertEquals("expm1(Inf)", Double.POSITIVE_INFINITY, FastMath.expm1(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("expm1(-Inf)", -1.0, FastMath.expm1(Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue("expm1(NaN) should be NaN",
                   Double.isNaN(FastMath.expm1(Double.NaN)));
    }

    // --- toIntExact (if exists) ---
    // FastMath may not have toIntExact; skip.

    // --- floorDiv, floorMod (if exists) ---
    // Skip.

    // --- Additional edge cases for coverage ---
    @Test
    public void testPowEdgeCases() {
        // pow with large exponent causing overflow
        assertEquals("pow(2.0, 1024.0)", Double.POSITIVE_INFINITY, FastMath.pow(2.0, 1024.0), 0.0);
        assertEquals("pow(2.0, -1074.0)", 0.0, FastMath.pow(2.0, -1074.0), 0.0);
        // pow with negative zero
        assertEquals("pow(-0.0, 2.0)", 0.0, FastMath.pow(-0.0, 2.0), 0.0);
        assertEquals("pow(-0.0, 3.0)", -0.0, FastMath.pow(-0.0, 3.0), 0.0);
        // pow with integer exponent as double
        assertEquals("pow(2.0, 0.0)", 1.0, FastMath.pow(2.0, 0.0), 0.0);
        assertEquals("pow(2.0, -0.0)", 1.0, FastMath.pow(2.0, -0.0), 0.0);
    }

    @Test
    public void testExpEdgeCases() {
        // exp near overflow boundary
        assertEquals("exp(709.7827)", Math.exp(709.7827), FastMath.exp(709.7827), 1e-15);
        // exp near underflow boundary
        assertEquals("exp(-745.1332)", Math.exp(-745.1332), FastMath.exp(-745.1332), 1e-15);
    }

    @Test
    public void testLogEdgeCases() {
        // log near zero
        assertEquals("log(Double.MIN_NORMAL)", Math.log(Double.MIN_NORMAL), FastMath.log(Double.MIN_NORMAL), 1e-15);
        // log of very large number
        assertEquals("log(Double.MAX_VALUE)", Math.log(Double.MAX_VALUE), FastMath.log(Double.MAX_VALUE), 1e-15);
    }

    @Test
    public void testTrigEdgeCases() {
        // sin/cos/tan of very large numbers to test argument reduction
        double large = 1e15;
        assertEquals("sin(large)", Math.sin(large), FastMath.sin(large), 1e-15);
        assertEquals("cos(large)", Math.cos(large), FastMath.cos(large), 1e-15);
        assertEquals("tan(large)", Math.tan(large), FastMath.tan(large), 1e-15);
        // sin/cos/tan of PI multiples
        assertEquals("sin(PI)", 0.0, FastMath.sin(Math.PI), 1e-15);
        assertEquals("cos(PI)", -1.0, FastMath.cos(Math.PI), 1e-15);
        assertEquals("tan(PI)", 0.0, FastMath.tan(Math.PI), 1e-15);
    }

    @Test
    public void testHypotEdgeCases() {
        // hypot with one zero
        assertEquals("hypot(0, 5)", 5.0, FastMath.hypot(0.0, 5.0), 1e-15);
        assertEquals("hypot(5, 0)", 5.0, FastMath.hypot(5.0, 0.0), 1e-15);
        // hypot with both negative
        assertEquals("hypot(-3, -4)", 5.0, FastMath.hypot(-3.0, -4.0), 1e-15);
    }

    @Test
    public void testRoundEdgeCases() {
        // round of large double
        assertEquals("round(1e20)", 100000000000000000000L, FastMath.round(1e20));
        assertEquals("round(-1e20)", -100000000000000000000L, FastMath.round(-1e20));
    }

    @Test
    public void testScalbEdgeCases() {
        // scalb with large scale factor
        assertEquals("scalb(1.0, 1024)", Double.POSITIVE_INFINITY, FastMath.scalb(1.0, 1024), 0.0);
        assertEquals("scalb(1.0, -1074)", 0.0, FastMath.scalb(1.0, -1074), 0.0);
    }

    @Test
    public void testNextAfterEdgeCases() {
        // nextAfter from negative to positive
        assertEquals("nextAfter(-0.0, 1.0)", Double.MIN_VALUE, FastMath.nextAfter(-0.0, 1.0), 0.0);
        assertEquals("nextAfter(0.0, -1.0)", -Double.MIN_VALUE, FastMath.nextAfter(0.0, -1.0), 0.0);
        // nextAfter from MAX_VALUE to Infinity
        assertEquals("nextAfter(Double.MAX_VALUE, Inf)", Double.POSITIVE_INFINITY, FastMath.nextAfter(Double.MAX_VALUE, Double.POSITIVE_INFINITY), 0.0);
        assertEquals("nextAfter(-Double.MAX_VALUE, -Inf)", Double.NEGATIVE_INFINITY, FastMath.nextAfter(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test
    public void testUlpEdgeCases() {
        // ulp of MAX_VALUE
        assertEquals("ulp(Double.MAX_VALUE)", Math.ulp(Double.MAX_VALUE), FastMath.ulp(Double.MAX_VALUE), 0.0);
        // ulp of MIN_VALUE
        assertEquals("ulp(Double.MIN_VALUE)", Double.MIN_VALUE, FastMath.ulp(Double.MIN_VALUE), 0.0);
    }

    @Test
    public void testCopySignEdgeCases() {
        // copySign with negative zero
        assertTrue("copySign(1.0, -0.0) should be -1.0",
                   FastMath.copySign(1.0, -0.0) == -1.0);
        assertTrue("copySign(-1.0, 0.0) should be 1.0",
                   FastMath.copySign(-1.0, 0.0) == 1.0);
    }

    @Test
    public void testGetExponentEdgeCases() {
        assertEquals("getExponent(Double.MIN_NORMAL)", -1022, FastMath.getExponent(Double.MIN_NORMAL));
        assertEquals("getExponent(Double.MIN_VALUE)", -1023, FastMath.getExponent(Double.MIN_VALUE));
    }

    @Test
    public void testAtan2EdgeCases() {
        // atan2 with both negative
        assertEquals("atan2(-1, -1)", -3 * Math.PI / 4, FastMath.atan2(-1.0, -1.0), 1e-15);
        // atan2 with zero and negative zero
        assertEquals("atan2(0.0, -1.0)", Math.PI, FastMath.atan2(0.0, -1.0), 1e-15);
        assertEquals("atan2(-0.0, -1.0)", -Math.PI, FastMath.atan2(-0.0, -1.0), 1e-15);
    }

    @Test
    public void testSinhCoshTanhEdgeCases() {
        // large values to test overflow
        assertEquals("sinh(710.0)", Double.POSITIVE_INFINITY, FastMath.sinh(710.0), 0.0);
        assertEquals("sinh(-710.0)", Double.NEGATIVE_INFINITY, FastMath.sinh(-710.0), 0.0);
        assertEquals("cosh(710.0)", Double.POSITIVE_INFINITY, FastMath.cosh(710.0), 0.0);
        assertEquals("cosh(-710.0)", Double.POSITIVE_INFINITY, FastMath.cosh(-710.0), 0.0);
        assertEquals("tanh(710.0)", 1.0, FastMath.tanh(710.0), 1e-15);
        assertEquals("tanh(-710.0)", -1.0, FastMath.tanh(-710.0), 1e-15);
    }

    @Test
    public void testLog1pExpm1EdgeCases() {
        // log1p of very small value
        double tiny = 1e-200;
        assertEquals("log1p(tiny)", Math.log1p(tiny), FastMath.log1p(tiny), 1e-15);
        // expm1 of very small value
        assertEquals("expm1(tiny)", Math.expm1(tiny), FastMath.expm1(tiny), 1e-15);
        // expm1 of large positive
        assertEquals("expm1(710.0)", Double.POSITIVE_INFINITY, FastMath.expm1(710.0), 0.0);
        // expm1 of large negative
        assertEquals("expm1(-710.0)", -1.0, FastMath.expm1(-710.0), 1e-15);
    }

    // --- Test for known bug #15 specifically (pow negative base non-integer exponent) ---
    @Test
    public void testBug15() {
        // This test directly targets the bug: pow(-2.0, 0.5) should be NaN
        assertTrue("Bug #15: pow(-2.0, 0.5) should be NaN",
                   Double.isNaN(FastMath.pow(-2.0, 0.5)));
        assertTrue("Bug #15: pow(-3.0, 0.5) should be NaN",
                   Double.isNaN(FastMath.pow(-3.0, 0.5)));
        assertTrue("Bug #15: pow(-0.5, 0.5) should be NaN",
                   Double.isNaN(FastMath.pow(-0.5, 0.5)));
        // Also test that integer exponent still works
        assertEquals("pow(-2.0, 2.0)", 4.0, FastMath.pow(-2.0, 2.0), 1e-15);
        assertEquals("pow(-2.0, 3.0)", -8.0, FastMath.pow(-2.0, 3.0), 1e-15);
    }
}