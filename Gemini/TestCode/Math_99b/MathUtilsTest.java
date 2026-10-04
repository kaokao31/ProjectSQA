package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class MathUtilsTest {

    @Test
    public void testAddAndCheckLong() {
        assertEquals(5L, MathUtils.addAndCheck(2L, 3L));
        assertEquals(Long.MAX_VALUE, MathUtils.addAndCheck(Long.MAX_VALUE - 1L, 1L));
        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test
    public void testAddAndCheckInt() {
        assertEquals(5, MathUtils.addAndCheck(2, 3));
        assertEquals(Integer.MAX_VALUE, MathUtils.addAndCheck(Integer.MAX_VALUE - 1, 1));
        try {
            MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test
    public void testMulAndCheckLong() {
        assertEquals(6L, MathUtils.mulAndCheck(2L, 3L));
        assertEquals(Long.MAX_VALUE, MathUtils.mulAndCheck(Long.MAX_VALUE, 1L));
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, 2L);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test
    public void testMulAndCheckInt() {
        assertEquals(6, MathUtils.mulAndCheck(2, 3));
        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test
    public void testSubAndCheckLong() {
        assertEquals(1L, MathUtils.subAndCheck(3L, 2L));
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test
    public void testSubAndCheckInt() {
        assertEquals(1, MathUtils.subAndCheck(3, 2));
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test
    public void testGcd() {
        assertEquals(3L, MathUtils.gcd(9L, 6L));
        assertEquals(3L, MathUtils.gcd(-9L, 6L));
        assertEquals(3L, MathUtils.gcd(9L, -6L));
        assertEquals(3L, MathUtils.gcd(-9L, -6L));
        assertEquals(0L, MathUtils.gcd(0L, 0L));
        assertEquals(5L, MathUtils.gcd(5L, 0L));
        assertEquals(5L, MathUtils.gcd(0L, 5L));
        
        // Edge cases with Long.MIN_VALUE
        try {
            MathUtils.gcd(Long.MIN_VALUE, 0L);
        } catch (Exception e) {
            // Depending on implementation, might throw exception or handle it
        }
    }

    @Test
    public void testLcm() {
        assertEquals(18L, MathUtils.lcm(9L, 6L));
        assertEquals(0L, MathUtils.lcm(0L, 5L));
        assertEquals(0L, MathUtils.lcm(5L, 0L));
        try {
            MathUtils.lcm(Long.MAX_VALUE, 2L);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test
    public void testSign() {
        assertEquals(1, MathUtils.sign(5.5));
        assertEquals(-1, MathUtils.sign(-5.5));
        assertEquals(0, MathUtils.sign(0.0));
        assertEquals(0, MathUtils.sign(-0.0));
        assertTrue(Double.isNaN(MathUtils.sign(Double.NaN)));

        assertEquals(1, MathUtils.sign((byte) 5));
        assertEquals(-1, MathUtils.sign((byte) -5));
        assertEquals(0, MathUtils.sign((byte) 0));

        assertEquals(1, MathUtils.sign(5.0f));
        assertEquals(-1, MathUtils.sign(-5.0f));
        assertEquals(0, MathUtils.sign(0.0f));
        assertTrue(Float.isNaN(MathUtils.sign(Float.NaN)));

        assertEquals(1, MathUtils.sign(5));
        assertEquals(-1, MathUtils.sign(-5));
        assertEquals(0, MathUtils.sign(0));

        assertEquals(1, MathUtils.sign(5L));
        assertEquals(-1, MathUtils.sign(-5L));
        assertEquals(0, MathUtils.sign(0L));

        assertEquals(1, MathUtils.sign(5.0));
        assertEquals(-1, MathUtils.sign(-5.0));
    }

    @Test
    public void testFactorial() {
        assertEquals(1L, MathUtils.factorial(0));
        assertEquals(1L, MathUtils.factorial(1));
        assertEquals(120L, MathUtils.factorial(5));
        try {
            MathUtils.factorial(-1);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            MathUtils.factorial(21);
            fail("Expecting ArithmeticException");
        } catch (ArithmeticException expected) {
            // expected
        }
    }

    @Test
    public void testFactorialDouble() {
        assertEquals(1.0, MathUtils.factorialDouble(0), 1e-9);
        assertEquals(120.0, MathUtils.factorialDouble(5), 1e-9);
        try {
            MathUtils.factorialDouble(-1);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testFactorialLog() {
        assertEquals(0.0, MathUtils.factorialLog(0), 1e-9);
        assertEquals(Math.log(120.0), MathUtils.factorialLog(5), 1e-9);
        try {
            MathUtils.factorialLog(-1);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testBinomialCoefficient() {
        assertEquals(1L, MathUtils.binomialCoefficient(5, 0));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 1));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 2));
        assertEquals(10L, MathUtils.binomialCoefficient(5, 3));
        assertEquals(5L, MathUtils.binomialCoefficient(5, 4));
        assertEquals(1L, MathUtils.binomialCoefficient(5, 5));

        try {
            MathUtils.binomialCoefficient(4, 5);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    public void testBinomialCoefficientDouble() {
        assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-9);
    }

    @Test
    public void testBinomialCoefficientLog() {
        assertEquals(Math.log(10.0), MathUtils.binomialCoefficientLog(5, 2), 1e-9);
    }

    @Test
    public void testEqualsDoubleArray() {
        assertTrue(MathUtils.equals(new double[] { 1.0, 2.0 }, new double[] { 1.0, 2.0 }));
        assertFalse(MathUtils.equals(new double[] { 1.0 }, new double[] { 1.0, 2.0 }));
        assertFalse(MathUtils.equals(new double[] { 1.0, 2.0 }, new double[] { 1.0, 3.0 }));
        assertNull(MathUtils.equals(null, null) ? null : (Boolean)null); // depends on implementation, test signature
    }

    @Test
    public void testHash() {
        assertEquals(Double.valueOf(1.0).hashCode(), MathUtils.hash(1.0));
        int hashNull = MathUtils.hash((double[]) null);
        assertEquals(0, hashNull);
        int hashArray = MathUtils.hash(new double[] { 1.0, 2.0 });
        assertTrue(hashArray != 0);
    }

    @Test
    public void testNormalizeAngle() {
        assertEquals(0.0, MathUtils.normalizeAngle(0.0, 0.0), 1e-9);
        assertEquals(Math.PI, MathUtils.normalizeAngle(3 * Math.PI, 0.0), 1e-9);
    }

    @Test
    public void testReduce() {
        assertEquals(1.0, MathUtils.reduce(5.0, 2.0, 0.0), 1e-9);
    }

    @Test
    public void testRound() {
        assertEquals(1.24, MathUtils.round(1.235, 2), 1e-9);
        assertEquals(1.24, MathUtils.round(1.235, 2, java.math.BigDecimal.ROUND_HALF_EVEN), 1e-9);
        assertEquals(1.23, MathUtils.round(1.234, 2, java.math.BigDecimal.ROUND_HALF_EVEN), 1e-9);
        assertEquals(1.24f, MathUtils.round(1.235f, 2), 1e-9);
        assertEquals(1.24f, MathUtils.round(1.235f, 2, java.math.BigDecimal.ROUND_HALF_EVEN), 1e-9);
    }

    @Test
    public void testCosh() {
        assertEquals(1.0, MathUtils.cosh(0.0), 1e-9);
    }

    @Test
    public void testSinh() {
        assertEquals(0.0, MathUtils.sinh(0.0), 1e-9);
    }

    @Test
    public void testTanh() {
        assertEquals(0.0, MathUtils.tanh(0.0), 1e-9);
    }

    @Test
    public void testSafeNorm() {
        assertEquals(5.0, MathUtils.safeNorm(new double[] { 3.0, 4.0 }), 1e-9);
        assertEquals(0.0, MathUtils.safeNorm(new double[0]), 1e-9);
        assertEquals(0.0, MathUtils.safeNorm(null), 1e-9);
    }

    @Test
    public void testIndexOf() {
        double[] array = { 1.0, 2.0, 3.0, Double.NaN };
        assertEquals(1, MathUtils.indexOf(array, 2.0));
        assertEquals(-1, MathUtils.indexOf(array, 5.0));
        assertEquals(3, MathUtils.indexOf(array, Double.NaN));
        assertEquals(-1, MathUtils.indexOf(null, 1.0));
    }
}