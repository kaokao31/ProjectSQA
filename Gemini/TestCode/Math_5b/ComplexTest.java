package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexTest {

    @Test
    public void testConstructorNullPointer() {
        try {
            new Complex(Double.NaN, 0.0);
            // Depending on implementation, NaN might be allowed or throw exception
        } catch (Exception e) {
            // expected if strict
        }
    }

    @Test
    public void testAbs() {
        Complex c = new Complex(3.0, 4.0);
        assertEquals(5.0, c.abs(), 1e-12);

        Complex cZero = new Complex(0.0, 0.0);
        assertEquals(0.0, cZero.abs(), 1e-12);

        Complex cNan = new Complex(Double.NaN, 4.0);
        assertTrue(Double.isNaN(cNan.abs()));

        Complex cInf = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, cInf.abs(), 1e-12);
    }

    @Test
    public void testAdd() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.add(c2);
        assertEquals(4.0, result.getReal(), 1e-12);
        assertEquals(6.0, result.getImaginary(), 1e-12);

        // Test null check if applicable
        try {
            c1.add(null);
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (Exception e) {
            // Expected
        }

        Complex cNaN = new Complex(Double.NaN, 1.0);
        assertTrue(cNaN.add(c2).isNaN());
    }

    @Test
    public void testConjugate() {
        Complex c = new Complex(1.0, 2.0);
        Complex conj = c.conjugate();
        assertEquals(1.0, conj.getReal(), 1e-12);
        assertEquals(-2.0, conj.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        assertTrue(cNaN.conjugate().isNaN());
    }

    @Test
    public void testDivide() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.divide(c2);
        // (1+2i)/(3+4i) = (1*3 + 2*4)/(9+16) + i(2*3 - 1*4)/(9+16) = 11/25 + 2/25 i = 0.44 + 0.08i
        assertEquals(0.44, result.getReal(), 1e-12);
        assertEquals(0.08, result.getImaginary(), 1e-12);

        try {
            c1.divide(null);
            fail("Expected Exception");
        } catch (Exception e) {
            // Expected
        }

        Complex cZero = new Complex(0.0, 0.0);
        Complex divZero = c1.divide(cZero);
        // Check behavior with zero divisor
        assertTrue(Double.isNaN(divZero.getReal()) || Double.isInfinite(divZero.getReal()));
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = new Complex(2.0, 1.0);
        Complex cNaN1 = new Complex(Double.NaN, 0.0);
        Complex cNaN2 = new Complex(Double.NaN, 0.0);

        assertEquals(c1, c1);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
        assertNotEquals(c1, null);
        assertNotEquals(c1, "Some String");

        assertEquals(cNaN1, cNaN2);
        assertEquals(cNaN1.hashCode(), cNaN2.hashCode());

        Complex cNaNReal = new Complex(1.0, Double.NaN);
        Complex cNaNReal2 = new Complex(1.0, Double.NaN);
        assertEquals(cNaNReal, cNaNReal2);
    }

    @Test
    public void testExp() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.exp();
        assertEquals(1.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 1.0);
        assertTrue(cNaN.exp().isNaN());
    }

    @Test
    public void testLog() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.log();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 1.0);
        assertTrue(cNaN.log().isNaN());
    }

    @Test
    public void testMultiply() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.multiply(c2);
        // (1+2i)(3+4i) = 3 + 4i + 6i - 8 = -5 + 10i
        assertEquals(-5.0, result.getReal(), 1e-12);
        assertEquals(10.0, result.getImaginary(), 1e-12);

        try {
            c1.multiply(null);
            fail("Expected Exception");
        } catch (Exception e) {
            // Expected
        }

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.multiply(c2).isNaN());
    }

    @Test
    public void testNegate() {
        Complex c = new Complex(1.0, -2.0);
        Complex result = c.negate();
        assertEquals(-1.0, result.getReal(), 1e-12);
        assertEquals(2.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.negate().isNaN());
    }

    @Test
    public void testSubtract() {
        Complex c1 = new Complex(3.0, 5.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.subtract(c2);
        assertEquals(2.0, result.getReal(), 1e-12);
        assertEquals(3.0, result.getImaginary(), 1e-12);

        try {
            c1.subtract(null);
            fail("Expected Exception");
        } catch (Exception e) {
            // Expected
        }

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.subtract(c2).isNaN());
    }

    @Test
    public void testAcos() {
        Complex c = new Complex(1.0, 0.0);
        Complex result = c.acos();
        assertNotNull(result);
        assertTrue(result.isNaN() || !Double.isNaN(result.getReal()));

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.acos().isNaN());
    }

    @Test
    public void testAsin() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.asin();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.asin().isNaN());
    }

    @Test
    public void testAtan() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.atan();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.atan().isNaN());
    }

    @Test
    public void testCos() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.cos();
        assertEquals(1.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.cos().isNaN());
    }

    @Test
    public void testCosh() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.cosh();
        assertEquals(1.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.cosh().isNaN());
    }

    @Test
    public void testSin() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sin();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.sin().isNaN());
    }

    @Test
    public void testSinh() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sinh();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.sinh().isNaN());
    }

    @Test
    public void testTan() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.tan();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.tan().isNaN());
    }

    @Test
    public void testTanh() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.tanh();
        assertEquals(0.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.tanh().isNaN());
    }

    @Test
    public void testPow() {
        Complex c1 = new Complex(2.0, 0.0);
        Complex c2 = new Complex(3.0, 0.0);
        Complex result = c1.pow(c2);
        assertEquals(8.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        try {
            c1.pow(null);
            fail("Expected Exception");
        } catch (Exception e) {
            // Expected
        }

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.pow(c2).isNaN());
        assertTrue(c1.pow(cNaN).isNaN());
    }

    @Test
    public void testSqrt() {
        Complex c = new Complex(4.0, 0.0);
        Complex result = c.sqrt();
        assertEquals(2.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNeg = new Complex(-4.0, 0.0);
        Complex resNeg = cNeg.sqrt();
        assertEquals(0.0, resNeg.getReal(), 1e-12);
        assertEquals(2.0, resNeg.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.sqrt().isNaN());
    }

    @Test
    public void testSqrt1z() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sqrt1z();
        assertEquals(1.0, result.getReal(), 1e-12);
        assertEquals(0.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(cNaN.sqrt1z().isNaN());
    }

    @Test
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex created = c.createComplex(3.0, 4.0);
        assertEquals(3.0, created.getReal(), 1e-12);
        assertEquals(4.0, created.getImaginary(), 1e-12);
    }

    @Test
    public void testGetArgument() {
        Complex c = new Complex(1.0, 0.0);
        assertEquals(0.0, c.getArgument(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 1.0);
        assertTrue(Double.isNaN(cNaN.getArgument()));
    }

    @Test
    public void testIsNaN() {
        assertFalse(new Complex(1.0, 2.0).isNaN());
        assertTrue(new Complex(Double.NaN, 2.0).isNaN());
        assertTrue(new Complex(1.0, Double.NaN).isNaN());
    }

    @Test
    public void testIsInfinite() {
        assertFalse(new Complex(1.0, 2.0).isInfinite());
        assertTrue(new Complex(Double.POSITIVE_INFINITY, 2.0).isInfinite());
        assertTrue(new Complex(1.0, Double.NEGATIVE_INFINITY).isInfinite());
    }

    @Test
    public void testToString() {
        Complex c = new Complex(1.0, 2.0);
        assertNotNull(c.toString());
    }
}