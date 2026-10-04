package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class FastMathTest {

    @Test
    public void testPowSpecialCases() {
        // Testing pow with special values, particularly focusing on edge cases like base 0 or 1, exponents near zero or infinity
        assertEquals(1.0, FastMath.pow(0.0, 0.0), 1e-15);
        assertEquals(1.0, FastMath.pow(1.0, 10.0), 1e-15);
        assertEquals(0.0, FastMath.pow(0.0, 5.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.pow(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.pow(2.0, Double.NaN)));
        
        // Additional edge cases for Math-15 (often related to pow(double, double) when base is negative or zero)
        assertEquals(0.0, FastMath.pow(0.0, 3), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -1.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.pow(0.0, -2.0), 1e-15);
    }

    @Test
    public void testPowNegativeBase() {
        // Exponent is an integer
        assertEquals(-8.0, FastMath.pow(-2.0, 3.0), 1e-15);
        assertEquals(4.0, FastMath.pow(-2.0, 2.0), 1e-15);
        
        // Exponent is not an integer
        assertTrue(Double.isNaN(FastMath.pow(-2.0, 2.5)));
    }

    @Test
    public void testCosh() {
        double val = FastMath.cosh(0.0);
        assertEquals(1.0, val, 1e-15);

        double valNeg = FastMath.cosh(-1.0);
        double valPos = FastMath.cosh(1.0);
        assertEquals(valPos, valNeg, 1e-15);

        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.cosh(Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue(Double.isNaN(FastMath.cosh(Double.NaN)));
    }

    @Test
    public void testSinh() {
        assertEquals(0.0, FastMath.sinh(0.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sinh(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.sinh(Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue(Double.isNaN(FastMath.sinh(Double.NaN)));
    }

    @Test
    public void testTanh() {
        assertEquals(0.0, FastMath.tanh(0.0), 1e-15);
        assertEquals(1.0, FastMath.tanh(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(-1.0, FastMath.tanh(Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue(Double.isNaN(FastMath.tanh(Double.NaN)));
    }

    @Test
    public void testAbs() {
        assertEquals(5.0, FastMath.abs(-5.0), 1e-15);
        assertEquals(5.0, FastMath.abs(5.0), 1e-15);
        assertEquals(0.0, FastMath.abs(0.0), 1e-15);
        assertEquals(0.0, FastMath.abs(-0.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.abs(Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue(Double.isNaN(FastMath.abs(Double.NaN)));

        assertEquals(5, FastMath.abs(-5));
        assertEquals(5, FastMath.abs(5));
        assertEquals(Integer.MAX_VALUE, FastMath.abs(Integer.MIN_VALUE + 1));

        assertEquals(5L, FastMath.abs(-5L));
        assertEquals(5L, FastMath.abs(5L));
        assertEquals(Long.MAX_VALUE, FastMath.abs(Long.MIN_VALUE + 1L));

        assertEquals(5.0f, FastMath.abs(-5.0f), 1e-7f);
        assertEquals(5.0f, FastMath.abs(5.0f), 1e-7f);
    }

    @Test
    public void testSqrt() {
        assertEquals(2.0, FastMath.sqrt(4.0), 1e-15);
        assertEquals(0.0, FastMath.sqrt(0.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.sqrt(Double.POSITIVE_INFINITY), 1e-15);
        assertTrue(Double.isNaN(FastMath.sqrt(-1.0)));
        assertTrue(Double.isNaN(FastMath.sqrt(Double.NaN)));
    }

    @Test
    public void testLog() {
        assertEquals(0.0, FastMath.log(1.0), 1e-15);
        assertEquals(Double.NEGATIVE_INFINITY, FastMath.log(0.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.log(-1.0)));
        assertEquals(Double.POSITIVE_INFINITY, FastMath.log(Double.POSITIVE_INFINITY), 1e-15);
        assertTrue(Double.isNaN(FastMath.log(Double.NaN)));
    }

    @Test
    public void testExp() {
        assertEquals(1.0, FastMath.exp(0.0), 1e-15);
        assertEquals(Double.POSITIVE_INFINITY, FastMath.exp(Double.POSITIVE_INFINITY), 1e-15);
        assertEquals(0.0, FastMath.exp(Double.NEGATIVE_INFINITY), 1e-15);
        assertTrue(Double.isNaN(FastMath.exp(Double.NaN)));
    }

    @Test
    public void testTrig() {
        assertEquals(0.0, FastMath.sin(0.0), 1e-15);
        assertEquals(1.0, FastMath.cos(0.0), 1e-15);
        assertEquals(0.0, FastMath.tan(0.0), 1e-15);

        assertTrue(Double.isNaN(FastMath.sin(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.cos(Double.NaN)));
        assertTrue(Double.isNaN(FastMath.tan(Double.NaN)));

        assertTrue(Double.isNaN(FastMath.sin(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.cos(Double.POSITIVE_INFINITY)));
        assertTrue(Double.isNaN(FastMath.tan(Double.POSITIVE_INFINITY)));
    }

    @Test
    public void testAtan2() {
        assertEquals(0.0, FastMath.atan2(0.0, 0.0), 1e-15);
        assertEquals(Math.PI / 2, FastMath.atan2(1.0, 0.0), 1e-15);
        assertEquals(0.0, FastMath.atan2(0.0, 1.0), 1e-15);
        assertTrue(Double.isNaN(FastMath.atan2(Double.NaN, 1.0)));
        assertTrue(Double.isNaN(FastMath.atan2(1.0, Double.NaN)));
    }
}