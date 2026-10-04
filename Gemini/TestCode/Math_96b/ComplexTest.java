package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

public class ComplexTest {

    @Test
    public void testEqualsNull() {
        Complex c = new Complex(1.0, 2.0);
        assertFalse(c.equals(null));
    }

    @Test
    public void testEqualsSelf() {
        Complex c = new Complex(1.0, 2.0);
        assertTrue(c.equals(c));
    }

    @Test
    public void testEqualsDifferentClass() {
        Complex c = new Complex(1.0, 2.0);
        assertFalse(c.equals("Not a Complex"));
    }

    @Test
    public void testEqualsNaN() {
        Complex c1 = new Complex(Double.NaN, Double.NaN);
        Complex c2 = new Complex(Double.NaN, Double.NaN);
        Complex c3 = new Complex(1.0, Double.NaN);
        Complex c4 = new Complex(Double.NaN, 1.0);
        Complex c5 = new Complex(1.0, 2.0);

        assertTrue(c1.equals(c2));
        assertTrue(c1.equals(c3));
        assertTrue(c1.equals(c4));
        assertTrue(c3.equals(c1));
        assertFalse(c5.equals(c1));
        assertFalse(c1.equals(c5));
    }

    @Test
    public void testEqualsNormal() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = new Complex(1.0, 3.0);
        Complex c4 = new Complex(2.0, 2.0);

        assertTrue(c1.equals(c2));
        assertFalse(c1.equals(c3));
        assertFalse(c1.equals(c4));
    }

    @Test
    public void testHashCode() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex cNaN = new Complex(Double.NaN, Double.NaN);

        assertEquals(c1.hashCode(), c2.hashCode());
        assertEquals(cNaN.hashCode(), new Complex(Double.NaN, 0.0).hashCode());
    }

    @Test
    public void testAbs() {
        Complex c = new Complex(3.0, 4.0);
        assertEquals(5.0, c.abs(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 0.0);
        assertTrue(Double.isNaN(cNaN.abs()));

        Complex cInf = new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        assertEquals(Double.POSITIVE_INFINITY, cInf.abs(), 1e-12);
    }

    @Test
    public void testAdd() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex sum = c1.add(c2);

        assertEquals(4.0, sum.getReal(), 1e-12);
        assertEquals(6.0, sum.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(c1.add(cNaN).isNaN());
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
        Complex div = c1.divide(c2);

        // (1+2i)/(3+4i) = (1*3 + 2*4)/(3^2+4^2) + i(2*3 - 1*4)/(3^2+4^2) = 11/25 + 2/25 i = 0.44 + 0.08i
        assertEquals(0.44, div.getReal(), 1e-12);
        assertEquals(0.08, div.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(c1.divide(cNaN).isNaN());
        assertTrue(cNaN.divide(c1).isNaN());
        
        Complex zero = new Complex(0.0, 0.0);
        assertTrue(c1.divide(zero).isNaN());
    }

    @Test
    public void testGetArgument() {
        Complex c = new Complex(0.0, 1.0);
        assertEquals(Math.PI / 2, c.getArgument(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 1.0);
        assertTrue(Double.isNaN(cNaN.getArgument()));
    }

    @Test
    public void testMultiply() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex prod = c1.multiply(c2);

        // (1+2i)(3+4i) = 3 + 4i + 6i - 8 = -5 + 10i
        assertEquals(-5.0, prod.getReal(), 1e-12);
        assertEquals(10.0, prod.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(c1.multiply(cNaN).isNaN());
        
        Complex inf = new Complex(Double.POSITIVE_INFINITY, 1.0);
        assertTrue(c1.multiply(inf).isInfinite());
    }

    @Test
    public void testNegate() {
        Complex c = new Complex(1.0, -2.0);
        Complex neg = c.negate();

        assertEquals(-1.0, neg.getReal(), 1e-12);
        assertEquals(2.0, neg.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        assertTrue(cNaN.negate().isNaN());
    }

    @Test
    public void testSubtract() {
        Complex c1 = new Complex(3.0, 4.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex sub = c1.subtract(c2);

        assertEquals(2.0, sub.getReal(), 1e-12);
        assertEquals(2.0, sub.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(c1.subtract(cNaN).isNaN());
    }

    @Test
    public void testAcos() {
        Complex c = new Complex(1.0, 0.0);
        Complex acos = c.acos();
        assertEquals(0.0, acos.getReal(), 1e-12);
        assertEquals(0.0, acos.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.acos().isNaN());
    }

    @Test
    public void testAsin() {
        Complex c = new Complex(0.0, 0.0);
        Complex asin = c.asin();
        assertEquals(0.0, asin.getReal(), 1e-12);
        assertEquals(0.0, asin.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.asin().isNaN());
    }

    @Test
    public void testAtan() {
        Complex c = new Complex(0.0, 0.0);
        Complex atan = c.atan();
        assertEquals(0.0, atan.getReal(), 1e-12);
        assertEquals(0.0, atan.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.atan().isNaN());
    }

    @Test
    public void testCos() {
        Complex c = new Complex(0.0, 0.0);
        Complex cos = c.cos();
        assertEquals(1.0, cos.getReal(), 1e-12);
        assertEquals(0.0, cos.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.cos().isNaN());
    }

    @Test
    public void testCosh() {
        Complex c = new Complex(0.0, 0.0);
        Complex cosh = c.cosh();
        assertEquals(1.0, cosh.getReal(), 1e-12);
        assertEquals(0.0, cosh.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.cosh().isNaN());
    }

    @Test
    public void testExp() {
        Complex c = new Complex(0.0, 0.0);
        Complex exp = c.exp();
        assertEquals(1.0, exp.getReal(), 1e-12);
        assertEquals(0.0, exp.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.exp().isNaN());
    }

    @Test
    public void testLog() {
        Complex c = new Complex(1.0, 0.0);
        Complex log = c.log();
        assertEquals(0.0, log.getReal(), 1e-12);
        assertEquals(0.0, log.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.log().isNaN());
    }

    @Test
    public void testPow() {
        Complex c1 = new Complex(2.0, 0.0);
        Complex c2 = new Complex(3.0, 0.0);
        Complex pow = c1.pow(c2);
        assertEquals(8.0, pow.getReal(), 1e-12);
        assertEquals(0.0, pow.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(c1.pow(cNaN).isNaN());
        assertTrue(cNaN.pow(c1).isNaN());
    }

    @Test
    public void testSin() {
        Complex c = new Complex(0.0, 0.0);
        Complex sin = c.sin();
        assertEquals(0.0, sin.getReal(), 1e-12);
        assertEquals(0.0, sin.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.sin().isNaN());
    }

    @Test
    public void testSinh() {
        Complex c = new Complex(0.0, 0.0);
        Complex sinh = c.sinh();
        assertEquals(0.0, sinh.getReal(), 1e-12);
        assertEquals(0.0, sinh.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.sinh().isNaN());
    }

    @Test
    public void testSqrt() {
        Complex c = new Complex(4.0, 0.0);
        Complex sqrt = c.sqrt();
        assertEquals(2.0, sqrt.getReal(), 1e-12);
        assertEquals(0.0, sqrt.getImaginary(), 1e-12);

        Complex cNegative = new Complex(-4.0, 0.0);
        Complex sqrtNeg = cNegative.sqrt();
        assertEquals(0.0, sqrtNeg.getReal(), 1e-12);
        assertEquals(2.0, sqrtNeg.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.sqrt().isNaN());
    }

    @Test
    public void testSqrt1z() {
        Complex c = new Complex(0.0, 0.0);
        Complex sqrt1z = c.sqrt1z();
        assertEquals(1.0, sqrt1z.getReal(), 1e-12);
        assertEquals(0.0, sqrt1z.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.sqrt1z().isNaN());
    }

    @Test
    public void testTan() {
        Complex c = new Complex(0.0, 0.0);
        Complex tan = c.tan();
        assertEquals(0.0, tan.getReal(), 1e-12);
        assertEquals(0.0, tan.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.tan().isNaN());
    }

    @Test
    public void testTanh() {
        Complex c = new Complex(0.0, 0.0);
        Complex tanh = c.tanh();
        assertEquals(0.0, tanh.getReal(), 1e-12);
        assertEquals(0.0, tanh.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.tanh().isNaN());
    }

    @Test
    public void testIsNaNAndInfinite() {
        Complex cNormal = new Complex(1.0, 2.0);
        assertFalse(cNormal.isNaN());
        assertFalse(cNormal.isInfinite());

        Complex cNaN = new Complex(Double.NaN, 2.0);
        assertTrue(cNaN.isNaN());
        assertFalse(cNaN.isInfinite());

        Complex cInf = new Complex(Double.POSITIVE_INFINITY, 2.0);
        assertFalse(cInf.isNaN());
        assertTrue(cInf.isInfinite());
    }

    @Test
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex instance = c.createComplex(3.0, 4.0);
        assertEquals(3.0, instance.getReal(), 1e-12);
        assertEquals(4.0, instance.getImaginary(), 1e-12);
    }

    @Test
    public void testConstants() {
        assertNotNull(Complex.I);
        assertNotNull(Complex.NaN);
        assertNotNull(Complex.ONE);
        assertNotNull(Complex.ZERO);

        assertEquals(0.0, Complex.I.getReal(), 1e-12);
        assertEquals(1.0, Complex.I.getImaginary(), 1e-12);

        assertTrue(Complex.NaN.isNaN());

        assertEquals(1.0, Complex.ONE.getReal(), 1e-12);
        assertEquals(0.0, Complex.ONE.getImaginary(), 1e-12);

        assertEquals(0.0, Complex.ZERO.getReal(), 1e-12);
        assertEquals(0.0, Complex.ZERO.getImaginary(), 1e-12);
    }
}