package org.apache.commons.math.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FastMathTest {

    private static void assertClose(double expected, double actual, double relTol) {
        if (Double.compare(expected, actual) == 0) {
            return;
        }
        double tol = relTol * Math.max(1.0, Math.abs(expected));
        assertEquals("expected " + expected + " actual " + actual, expected, actual, tol);
    }

    @Test
    public void testConstants() {
        assertEquals(Math.E, FastMath.E, 1e-15);
        assertEquals(Math.PI, FastMath.PI, 1e-15);
    }

    @Test
    public void testAbsDouble() {
        assertEquals(1.0, FastMath.abs(-1.0), 0.0);
        assertEquals(1.0, FastMath.abs(1.0), 0.0);
        assertEquals(0.0, FastMath.abs(0.0), 0.0);
        assertEquals(0.0, FastMath.abs(-0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.abs(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.abs(Double.NaN)));
        assertEquals(Double.MAX_VALUE, FastMath.abs(-Double.MAX_VALUE), 0.0);
    }

    @Test
    public void testAbsFloat() {
        assertEquals(1.0f, FastMath.abs(-1.0f), 0.0f);
        assertEquals(1.0f, FastMath.abs(1.0f), 0.0f);
        assertEquals(0.0f, FastMath.abs(-0.0f), 0.0f);
        assertEquals(Float.POSITIVE_INFINITY, FastMath.abs(Float.NEGATIVE_INFINITY), 0.0f);
        assertTrue(Float.isNaN(FastMath.abs(Float.NaN)));
        assertEquals(Float.MAX_VALUE, FastMath.abs(-Float.MAX_VALUE), 0.0f);
    }

    @Test
    public void testAbsInt() {
        assertEquals(1, FastMath.abs(-1));
        assertEquals(1, FastMath.abs(1));
        assertEquals(Integer.MAX_VALUE, FastMath.abs(Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, FastMath.abs(Integer.MIN_VALUE));
    }

    @Test
    public void testAbsLong() {
        assertEquals(1L, FastMath.abs(-1L));
        assertEquals(1L, FastMath.abs(1L));
        assertEquals(Long.MAX_VALUE, FastMath.abs(Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, FastMath.abs(Long.MIN_VALUE));
    }

    @Test
    public void testCeil() {
        assertTrue(Double.isNaN(FastMath.ceil(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.ceil(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.ceil(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.ceil(0.0), 0.0);
        assertEquals(0.0, FastMath.ceil(-0.0), 0.0);
        assertEquals(2.0, FastMath.ceil(1.2), 0.0);
        assertEquals(-1.0, FastMath.ceil(-1.2), 0.0);
        assertEquals(2.0, FastMath.ceil(1.5), 0.0);
        assertEquals(-1.0, FastMath.ceil(-1.5), 0.0);
    }

    @Test
    public void testFloor() {
        assertTrue(Double.isNaN(FastMath.floor(Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.floor(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.floor(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.floor(0.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.floor(-0.0)));
        assertEquals(1.0, FastMath.floor(1.2), 0.0);
        assertEquals(-2.0, FastMath.floor(-1.2), 0.0);
        assertEquals(1.0, FastMath.floor(1.5), 0.0);
        assertEquals(-2.0, FastMath.floor(-1.5), 0.0);
    }

    @Test
    public void testRint() {
        double[] values = {0.0, -0.0, 1.5, -1.5, 2.5, -2.5, 3.5, -3.5, 0.5, -0.5};
        for (double x : values) {
            assertEquals("rint(" + x + ")", Math.rint(x), FastMath.rint(x), 0.0);
        }
        assertEquals(Double.POSITIVE_INFINITY, FastMath.rint(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.rint(Double.NaN)));
    }

    @Test
    public void testRoundDouble() {
        assertEquals(-2L, FastMath.round(-1.6));
        assertEquals(-1L, FastMath.round(-1.5));
        assertEquals(0L, FastMath.round(-0.5));
        assertEquals(1L, FastMath.round(0.5));
        assertEquals(2L, FastMath.round(1.5));
        assertEquals(1L, FastMath.round(1.4));
        assertEquals(Long.MAX_VALUE, FastMath.round(Double.POSITIVE_INFINITY));
        assertEquals(Long.MIN_VALUE, FastMath.round(Double.NEGATIVE_INFINITY));
        assertEquals(0L, FastMath.round(Double.NaN));
    }

    @Test
    public void testRoundFloat() {
        assertEquals(-2, FastMath.round(-1.6f));
        assertEquals(-1, FastMath.round(-1.5f));
        assertEquals(0, FastMath.round(-0.5f));
        assertEquals(1, FastMath.round(0.5f));
        assertEquals(2, FastMath.round(1.5f));
        assertEquals(Integer.MAX_VALUE, FastMath.round(Float.POSITIVE_INFINITY));
        assertEquals(Integer.MIN_VALUE, FastMath.round(Float.NEGATIVE_INFINITY));
        assertEquals(0, FastMath.round(Float.NaN));
    }

    @Test
    public void testMaxDouble() {
        assertEquals(2.0, FastMath.max(1.0, 2.0), 0.0);
        assertEquals(2.0, FastMath.max(2.0, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.max(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.max(1.0, Double.NaN)));
        assertTrue(Double.isNaN(FastMath.max(Double.NaN, Double.NaN)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.max(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(1.0, FastMath.max(Double.NEGATIVE_INFINITY, 1.0), 0.0);
        assertEquals(0L, Double.doubleToRawLongBits(FastMath.max(0.0, -0.0)));
        assertEquals(0L, Double.doubleToRawLongBits(FastMath.max(-0.0, 0.0)));
    }

    @Test
    public void testMinDouble() {
        assertEquals(1.0, FastMath.min(1.0, 2.0), 0.0);
        assertEquals(1.0, FastMath.min(2.0, 1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.min(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.min(1.0, Double.NaN)));
        assertTrue(Double.isNaN(FastMath.min(Double.NaN, Double.NaN)));
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.min(Double.NEGATIVE_INFINITY, 1.0), 0.0);
        assertEquals(1.0, FastMath.min(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.min(0.0, -0.0)));
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.min(-0.0, 0.0)));
    }

    @Test
    public void testMaxFloat() {
        assertEquals(2.0f, FastMath.max(1.0f, 2.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.max(Float.NaN, 1.0f)));
        assertTrue(Float.isNaN(FastMath.max(1.0f, Float.NaN)));
        assertTrue(Float.isNaN(FastMath.max(Float.NaN, Float.NaN)));
        assertEquals(0, Float.floatToRawIntBits(FastMath.max(0.0f, -0.0f)));
        assertEquals(0, Float.floatToRawIntBits(FastMath.max(-0.0f, 0.0f)));
    }

    @Test
    public void testMinFloat() {
        assertEquals(1.0f, FastMath.min(1.0f, 2.0f), 0.0f);
        assertTrue(Float.isNaN(FastMath.min(Float.NaN, 1.0f)));
        assertTrue(Float.isNaN(FastMath.min(1.0f, Float.NaN)));
        assertTrue(Float.isNaN(FastMath.min(Float.NaN, Float.NaN)));
        assertEquals(Float.floatToRawIntBits(-0.0f), Float.floatToRawIntBits(FastMath.min(0.0f, -0.0f)));
        assertEquals(Float.floatToRawIntBits(-0.0f), Float.floatToRawIntBits(FastMath.min(-0.0f, 0.0f)));
    }

    @Test
    public void testMaxMinIntLong() {
        assertEquals(2, FastMath.max(1, 2));
        assertEquals(-1, FastMath.max(-1, -2));
        assertEquals(Integer.MIN_VALUE, FastMath.min(0, Integer.MIN_VALUE));
        assertEquals(2L, FastMath.max(1L, 2L));
        assertEquals(-1L, FastMath.max(-1L, -2L));
        assertEquals(Long.MIN_VALUE, FastMath.min(0L, Long.MIN_VALUE));
    }

    @Test
    public void testSqrt() {
        assertEquals(2.0, FastMath.sqrt(4.0), 0.0);
        assertEquals(0.0, FastMath.sqrt(0.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.sqrt(-0.0)));
        assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sqrt(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.sqrt(Double.NEGATIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.sqrt(Double.NaN)));
    }

    @Test
    public void testCbrt() {
        assertEquals(3.0, FastMath.cbrt(27.0), 0.0);
        assertEquals(-3.0, FastMath.cbrt(-27.0), 0.0);
        assertEquals(0.0, FastMath.cbrt(0.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.cbrt(-0.0)));
        assertEquals(Math.cbrt(2.0), FastMath.cbrt(2.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.cbrt(Double.NaN)));
    }

    @Test
    public void testExp() {
        assertEquals(1.0, FastMath.exp(0.0), 0.0);
        assertEquals(Math.E, FastMath.exp(1.0), 1e-15);
        assertClose(Math.exp(2.0), FastMath.exp(2.0), 1e-12);
        assertClose(Math.exp(-2.0), FastMath.exp(-2.0), 1e-12);
        assertClose(Math.exp(10.0), FastMath.exp(10.0), 1e-12);
        assertClose(Math.exp(-10.0), FastMath.exp(-10.0), 1e-12);
        assertEquals(0.0, FastMath.exp(-1000.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(1000.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(0.0, FastMath.exp(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.exp(Double.NaN)));
        double maxLog = Math.log(Double.MAX_VALUE);
        assertClose(Math.exp(maxLog), FastMath.exp(maxLog), 1e-9);
    }

    @Test
    public void testLog() {
        assertEquals(0.0, FastMath.log(1.0), 0.0);
        assertEquals(1.0, FastMath.log(Math.E), 1e-15);
        assertClose(Math.log(10.0), FastMath.log(10.0), 1e-12);
        assertClose(Math.log(0.001), FastMath.log(0.001), 1e-12);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(-0.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
    }

    @Test
    public void testExpm1() {
        assertEquals(0.0, FastMath.expm1(0.0), 0.0);
        assertClose(Math.expm1(1.0), FastMath.expm1(1.0), 1e-12);
        assertClose(Math.expm1(-1.0), FastMath.expm1(-1.0), 1e-12);
        assertClose(Math.expm1(1e-12), FastMath.expm1(1e-12), 1e-12);
        assertEquals(-1.0, FastMath.expm1(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.expm1(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.expm1(Double.NaN)));
    }

    @Test
    public void testLog1p() {
        assertEquals(0.0, FastMath.log1p(0.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.log1p(-0.0)));
        assertClose(Math.log1p(1.0), FastMath.log1p(1.0), 1e-12);
        assertClose(Math.log1p(-0.5), FastMath.log1p(-0.5), 1e-12);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log1p(-1.0), 0.0);
        assertTrue(Double.isNaN(FastMath.log1p(-2.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log1p(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.log1p(Double.NaN)));
    }

    @Test
    public void testPowDoubleDouble() {
        assertEquals(8.0, FastMath.pow(2.0, 3.0), 1e-15);
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 0.0);
        assertEquals(1.0, FastMath.pow(-0.0, 0.0), 0.0);
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), 1e-15);
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 0.5)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1.0), 0.0);
        assertEquals(0.0, FastMath.pow(0.0, 1.0), 0.0);
        assertEquals(1.0, FastMath.pow(1.0, Double.NaN), 0.0);
        assertEquals(1.0, FastMath.pow(Double.NaN, 0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(Double.POSITIVE_INFINITY, 2.0), 0.0);
        assertEquals(0.0, FastMath.pow(Double.POSITIVE_INFINITY, -1.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.pow(-0.0, 3.0)));

        double[] bases = {0.5, 2.0, 3.0, -2.0, 10.0, 0.1};
        double[] exponents = {-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 0.5};
        for (double b : bases) {
            for (double e : exponents) {
                double expected = Math.pow(b, e);
                double actual = FastMath.pow(b, e);
                if (Double.isNaN(expected)) {
                    assertTrue("pow(" + b + "," + e + ")", Double.isNaN(actual));
                } else {
                    assertClose("pow(" + b + "," + e + ")", expected, actual, 1e-12);
                }
            }
        }
    }

    @Test
    public void testPowDoubleInt() {
        assertEquals(8.0, FastMath.pow(2.0, 3), 1e-15);
        assertEquals(1.0 / 8.0, FastMath.pow(2.0, -3), 1e-15);
        assertEquals(1.0, FastMath.pow(0.0, 0), 0.0);
        assertEquals(-8.0, FastMath.pow(-2.0, 3), 1e-15);
        assertEquals(4.0, FastMath.pow(-2.0, 2), 1e-15);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.pow(-0.0, 3)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1), 0.0);
        assertEquals(0.0, FastMath.pow(0.0, 1), 0.0);
    }

    @Test
    public void testSinCosTan() {
        double[] small = {0.0, -0.0, Math.PI / 6, Math.PI / 4, Math.PI / 3, Math.PI / 2,
                          Math.PI, -Math.PI / 2, -Math.PI, 1.0, -1.0, 2.0, -2.0};
        for (double x : small) {
            assertEquals("sin(" + x + ")", Math.sin(x), FastMath.sin(x), 1e-14);
            assertEquals("cos(" + x + ")", Math.cos(x), FastMath.cos(x), 1e-14);
            assertEquals("tan(" + x + ")", Math.tan(x), FastMath.tan(x), 1e-12);
        }

        double[] large = {1e8, 1e10, -1e10, 1e15};
        for (double x : large) {
            double sin = FastMath.sin(x);
            double cos = FastMath.cos(x);
            double tan = FastMath.tan(x);
            assertTrue("sin finite", Double.isFinite(sin));
            assertTrue("cos finite", Double.isFinite(cos));
            assertTrue("tan finite", Double.isFinite(tan));
            assertEquals("sin^2+cos^2", 1.0, sin * sin + cos * cos, 1e-9);
        }

        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.sin(-0.0)));
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.tan(-0.0)));
        assertEquals(1.0, FastMath.cos(0.0), 0.0);
    }

    @Test
    public void testAsin() {
        assertEquals(0.0, FastMath.asin(0.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.asin(-0.0)));
        assertEquals(Math.PI / 2, FastMath.asin(1.0), 1e-15);
        assertEquals(-Math.PI / 2, FastMath.asin(-1.0), 1e-15);
        assertClose(Math.asin(0.5), FastMath.asin(0.5), 1e-12);
        assertClose(Math.asin(-0.5), FastMath.asin(-0.5), 1e-12);
        assertTrue(Double.isNaN(FastMath.asin(1.5)));
        assertTrue(Double.isNaN(FastMath.asin(-1.5)));
        assertTrue(Double.isNaN(FastMath.asin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.asin(Double.NaN)));
    }

    @Test
    public void testAcos() {
        assertEquals(0.0, FastMath.acos(1.0), 0.0);
        assertEquals(Math.PI, FastMath.acos(-1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.acos(0.0), 1e-15);
        assertClose(Math.acos(0.5), FastMath.acos(0.5), 1e-12);
        assertClose(Math.acos(-0.5), FastMath.acos(-0.5), 1e-12);
        assertTrue(Double.isNaN(FastMath.acos(1.5)));
        assertTrue(Double.isNaN(FastMath.acos(-1.5)));
        assertTrue(Double.isNaN(FastMath.acos(Double.NEGATIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.acos(Double.NaN)));
    }

    @Test
    public void testAtan() {
        assertEquals(0.0, FastMath.atan(0.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.atan(-0.0)));
        assertEquals(Math.PI / 4, FastMath.atan(1.0), 1e-15);
        assertEquals(-Math.PI / 4, FastMath.atan(-1.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.atan(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(-Math.PI / 2, FastMath.atan(Double.NEGATIVE_INFINITY), 0.0);
        assertClose(Math.atan(3.0), FastMath.atan(3.0), 1e-12);
        assertClose(Math.atan(-3.0), FastMath.atan(-3.0), 1e-12);
        assertTrue(Double.isNaN(FastMath.atan(Double.NaN)));
    }

    @Test
    public void testAtan2() {
        double[] y = {0.0, -0.0, 1.0, -1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double[] x = {0.0, -0.0, 1.0, -1.0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double yy : y) {
            for (double xx : x) {
                double expected = Math.atan2(yy, xx);
                double actual = FastMath.atan2(yy, xx);
                if (Double.isNaN(expected)) {
                    assertTrue("atan2(" + yy + "," + xx + ")", Double.isNaN(actual));
                } else {
                    assertClose("atan2(" + yy + "," + xx + ")", expected, actual, 1e-12);
                }
            }
        }

        assertClose(Math.atan2(3.0, 4.0), FastMath.atan2(3.0, 4.0), 1e-12);
        assertClose(Math.atan2(-3.0, 4.0), FastMath.atan2(-3.0, 4.0), 1e-12);
        assertClose(Math.atan2(3.0, -4.0), FastMath.atan2(3.0, -4.0), 1e-12);
        assertClose(Math.atan2(-3.0, -4.0), FastMath.atan2(-3.0, -4.0), 1e-12);
    }

    @Test
    public void testSinhCoshTanh() {
        double[] values = {0.0, -0.0, 1.0, -1.0, 2.0, -2.0};
        for (double x : values) {
            assertEquals("sinh(" + x + ")", Math.sinh(x), FastMath.sinh(x), 1e-14);
            assertEquals("cosh(" + x + ")", Math.cosh(x), FastMath.cosh(x), 1e-14);
            assertEquals("tanh(" + x + ")", Math.tanh(x), FastMath.tanh(x), 1e-14);
        }
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.sinh(-0.0)));
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.tanh(-0.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sinh(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.sinh(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(Double.NEGATIVE_INFINITY), 0.0);
        assertEquals(1.0, FastMath.tanh(Double.POSITIVE_INFINITY), 0.0);
        assertEquals(-1.0, FastMath.tanh(Double.NEGATIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));
    }

    @Test
    public void testHypot() {
        assertEquals(5.0, FastMath.hypot(3.0, 4.0), 1e-15);
        assertEquals(5.0, FastMath.hypot(-3.0, 4.0), 1e-15);
        assertEquals(0.0, FastMath.hypot(0.0, 0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.POSITIVE_INFINITY, 1.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.hypot(Double.NEGATIVE_INFINITY, Double.NaN), 0.0);
        assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.hypot(Double.NaN, Double.NaN)));
        assertEquals(Double.MAX_VALUE, FastMath.hypot(Double.MAX_VALUE, 0.0), 0.0);
        assertClose(Math.hypot(1e154, 1e154), FastMath.hypot(1e154, 1e154), 1e-12);
    }

    @Test
    public void testSignum() {
        assertEquals(1.0, FastMath.signum(2.0), 0.0);
        assertEquals(-1.0, FastMath.signum(-2.0), 0.0);
        assertEquals(0.0, FastMath.signum(0.0), 0.0);
        assertEquals(0x8000000000000000L, Double.doubleToRawLongBits(FastMath.signum(-0.0)));
        assertTrue(Double.isNaN(FastMath.signum(Double.NaN)));

        assertEquals(1.0f, FastMath.signum(2.0f), 0.0f);
        assertEquals(-1.0f, FastMath.signum(-2.0f), 0.0f);
        assertEquals(0.0f, FastMath.signum(0.0f), 0.0f);
        assertEquals(Float.floatToRawIntBits(-0.0f), Float.floatToRawIntBits(FastMath.signum(-0.0f)));
        assertTrue(Float.isNaN(FastMath.signum(Float.NaN)));
    }

    @Test
    public void testToRadiansToDegrees() {
        double[] values = {0.0, -0.0, 1.0, -1.0, Math.PI, -Math.PI, 180.0, -180.0,
                           Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN};
        for (double x : values) {
            double eRad = Math.toRadians(x);
            double aRad = FastMath.toRadians(x);
            if (Double.isNaN(eRad)) {
                assertTrue(Double.isNaN(aRad));
            } else {
                assertEquals(eRad, aRad, 1e-12 * Math.max(1.0, Math.abs(eRad)));
            }

            double eDeg = Math.toDegrees(x);
            double aDeg = FastMath.toDegrees(x);
            if (Double.isNaN(eDeg)) {
                assertTrue(Double.isNaN(aDeg));
            } else {
                assertEquals(eDeg, aDeg, 1e-12 * Math.max(1.0, Math.abs(eDeg)));
            }
        }
    }

    @Test
    public void testUlp() {
        assertEquals(Math.ulp(1.0), FastMath.ulp(1.0), 0.0);
        assertEquals(Math.ulp(0.0), FastMath.ulp(0.0), 0.0);
        assertEquals(Math.ulp(-0.0), FastMath.ulp(-0.0), 0.0);
        assertEquals(Math.ulp(Double.MAX_VALUE), FastMath.ulp(Double.MAX_VALUE), 0.0);
        assertEquals(Math.ulp(Double.POSITIVE_INFINITY), FastMath.ulp(Double.POSITIVE_INFINITY), 0.0);
        assertTrue(Double.isNaN(FastMath.ulp(Double.NaN)));

        assertEquals(Math.ulp(1.0f), FastMath.ulp(1.0f), 0.0f);
        assertEquals(Math.ulp(0.0f), FastMath.ulp(0.0f), 0.0f);
        assertEquals(Math.ulp(Float.MAX_VALUE), FastMath.ulp(Float.MAX_VALUE), 0.0f);
        assertEquals(Math.ulp(Float.POSITIVE_INFINITY), FastMath.ulp(Float.POSITIVE_INFINITY), 0.0f);
        assertTrue(Float.isNaN(FastMath.ulp(Float.NaN)));
    }

    @Test
    public void testScalb() {
        assertEquals(Math.scalb(1.0, 3), FastMath.scalb(1.0, 3), 0.0);
        assertEquals(Math.scalb(1.0, -3), FastMath.scalb(1.0, -3), 0.0);
        assertEquals(Math.scalb(0.0, 100), FastMath.scalb(0.0, 100), 0.0);
        assertEquals(Math.scalb(-0.0, 100), FastMath.scalb(-0.0, 100), 0.0);
        assertEquals(Math.scalb(Double.MAX_VALUE, 2), FastMath.scalb(Double.MAX_VALUE, 2), 0.0);
        assertEquals(Math.scalb(1.0f, 3), FastMath.scalb(1.0f, 3), 0.0f);
        assertEquals(Math.scalb(1.0f, -3), FastMath.scalb(1.0f, -3), 0.0f);
    }

    @Test
    public void testRandom() {
        double r = FastMath.random();
        assertTrue("random in range", r >= 0.0 && r < 1.0);
    }
}