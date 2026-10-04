package org.apache.commons.math.complex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComplexTest {

    @Test
    public void testConstructorAndGetters() {
        Complex c = new Complex(3.0, -4.0);
        assertEquals(3.0, c.getReal(), 0.0);
        assertEquals(-4.0, c.getImaginary(), 0.0);
        assertFalse(c.isNaN());
        assertFalse(c.isInfinite());

        Complex realOnly = new Complex(2.5);
        assertEquals(2.5, realOnly.getReal(), 0.0);
        assertEquals(0.0, realOnly.getImaginary(), 0.0);
    }

    @Test
    public void testValueOf() {
        Complex c = Complex.valueOf(1.0, -2.0);
        assertEquals(1.0, c.getReal(), 0.0);
        assertEquals(-2.0, c.getImaginary(), 0.0);

        Complex realOnly = Complex.valueOf(3.0);
        assertEquals(3.0, realOnly.getReal(), 0.0);
        assertEquals(0.0, realOnly.getImaginary(), 0.0);
    }

    @Test
    public void testAdd() {
        Complex sum = new Complex(1.0, 2.0).add(new Complex(3.0, -5.0));
        assertEquals(4.0, sum.getReal(), 0.0);
        assertEquals(-3.0, sum.getImaginary(), 0.0);

        assertTrue(new Complex(1.0, 2.0).add(Complex.NaN).isNaN());
    }

    @Test
    public void testSubtract() {
        Complex diff = new Complex(5.0, 6.0).subtract(new Complex(2.0, 4.0));
        assertEquals(3.0, diff.getReal(), 0.0);
        assertEquals(2.0, diff.getImaginary(), 0.0);

        assertTrue(new Complex(1.0, 2.0).subtract(Complex.NaN).isNaN());
    }

    @Test
    public void testMultiply() {
        Complex product = new Complex(2.0, 3.0).multiply(new Complex(4.0, 5.0));
        assertEquals(-7.0, product.getReal(), 1e-12);
        assertEquals(22.0, product.getImaginary(), 1e-12);

        assertTrue(new Complex(1.0, 2.0).multiply(Complex.NaN).isNaN());
    }

    @Test
    public void testMultiplyScalar() {
        Complex scaled = new Complex(2.0, -3.0).multiply(4.0);
        assertEquals(8.0, scaled.getReal(), 0.0);
        assertEquals(-12.0, scaled.getImaginary(), 0.0);
    }

    @Test
    public void testMultiplyInfinite() {
        Complex result = new Complex(2.0, 3.0).multiply(Complex.INF);
        assertTrue(result.isInfinite());

        result = Complex.INF.multiply(new Complex(2.0, 3.0));
        assertTrue(result.isInfinite());
    }

    @Test
    public void testDivide() {
        Complex quotient = new Complex(1.0, 2.0).divide(new Complex(3.0, 4.0));
        assertEquals(0.44, quotient.getReal(), 1e-12);
        assertEquals(0.08, quotient.getImaginary(), 1e-12);

        quotient = new Complex(1.0, 2.0).divide(new Complex(4.0, 5.0));
        assertEquals(14.0 / 41.0, quotient.getReal(), 1e-12);
        assertEquals(3.0 / 41.0, quotient.getImaginary(), 1e-12);

        assertTrue(new Complex(1.0, 2.0).divide(Complex.NaN).isNaN());
    }

    @Test
    public void testDivideScalar() {
        Complex quotient = new Complex(3.0, 6.0).divide(3.0);
        assertEquals(1.0, quotient.getReal(), 0.0);
        assertEquals(2.0, quotient.getImaginary(), 0.0);
    }

    @Test
    public void testDivideByZero() {
        Complex result = new Complex(1.0, 2.0).divide(Complex.ZERO);
        assertEquals(Complex.INF, result);

        assertTrue(Complex.ZERO.divide(Complex.ZERO).isNaN());
    }

    @Test
    public void testDivideFiniteByInfinite() {
        Complex result = new Complex(2.0, 3.0).divide(Complex.INF);
        assertEquals(Complex.ZERO, result);
    }

    @Test
    public void testDivideInfiniteByFinite() {
        Complex result = Complex.INF.divide(new Complex(1.0, 2.0));
        assertTrue(result.isInfinite());
    }

    @Test
    public void testDivideInfiniteByInfinite() {
        Complex result = Complex.INF.divide(Complex.INF);
        assertTrue(result.isNaN());
    }

    @Test
    public void testReciprocal() {
        Complex reciprocal = new Complex(1.0, 2.0).reciprocal();
        assertEquals(0.2, reciprocal.getReal(), 1e-12);
        assertEquals(-0.4, reciprocal.getImaginary(), 1e-12);

        reciprocal = new Complex(2.0, 1.0).reciprocal();
        assertEquals(0.4, reciprocal.getReal(), 1e-12);
        assertEquals(-0.2, reciprocal.getImaginary(), 1e-12);

        assertEquals(Complex.INF, Complex.ZERO.reciprocal());
    }

    @Test
    public void testConjugate() {
        Complex result = new Complex(1.0, 2.0).conjugate();
        assertEquals(1.0, result.getReal(), 0.0);
        assertEquals(-2.0, result.getImaginary(), 0.0);
    }

    @Test
    public void testNegate() {
        Complex result = new Complex(1.0, -2.0).negate();
        assertEquals(-1.0, result.getReal(), 0.0);
        assertEquals(2.0, result.getImaginary(), 0.0);
    }

    @Test
    public void testAbs() {
        assertEquals(Math.sqrt(5.0), new Complex(1.0, 2.0).abs(), 1e-12);
        assertEquals(1.0, new Complex(1.0, 0.0).abs(), 0.0);
        assertEquals(2.0, new Complex(0.0, 2.0).abs(), 0.0);
        assertEquals(0.0, Complex.ZERO.abs(), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), 0.0);
        assertTrue(Double.isNaN(Complex.NaN.abs()));
    }

    @Test
    public void testGetArgument() {
        assertEquals(Math.atan2(2.0, 1.0), new Complex(1.0, 2.0).getArgument(), 1e-12);
        assertEquals(0.0, new Complex(1.0, 0.0).getArgument(), 0.0);
        assertEquals(Math.PI / 2.0, new Complex(0.0, 1.0).getArgument(), 1e-12);
        assertEquals(Math.PI, new Complex(-1.0, 0.0).getArgument(), 1e-12);
        assertEquals(-Math.PI / 2.0, new Complex(0.0, -1.0).getArgument(), 1e-12);
    }

    @Test
    public void testIsNaN() {
        assertTrue(new Complex(Double.NaN, 1.0).isNaN());
        assertTrue(new Complex(1.0, Double.NaN).isNaN());
        assertTrue(Complex.NaN.isNaN());
        assertFalse(new Complex(1.0, 2.0).isNaN());
    }

    @Test
    public void testIsInfinite() {
        assertTrue(new Complex(Double.POSITIVE_INFINITY, 1.0).isInfinite());
        assertTrue(new Complex(1.0, Double.NEGATIVE_INFINITY).isInfinite());
        assertTrue(Complex.INF.isInfinite());
        assertFalse(new Complex(Double.NaN, 1.0).isInfinite());
        assertFalse(new Complex(1.0, 2.0).isInfinite());
    }

    @Test
    public void testEquals() {
        Complex c = new Complex(1.0, 2.0);
        assertTrue(c.equals(new Complex(1.0, 2.0)));
        assertFalse(c.equals(new Complex(1.0, 3.0)));
        assertFalse(c.equals(null));
        assertFalse(c.equals("not a complex"));
    }

    @Test
    public void testEqualsNaN() {
        assertTrue(Complex.NaN.equals(Complex.NaN));
        assertTrue(Complex.NaN.equals(new Complex(Double.NaN, Double.NaN)));
        assertFalse(Complex.NaN.equals(new Complex(1.0, 2.0)));
        assertFalse(new Complex(1.0, 2.0).equals(Complex.NaN));
    }

    @Test
    public void testHashCode() {
        assertEquals(new Complex(1.0, 2.0).hashCode(), new Complex(1.0, 2.0).hashCode());
        assertEquals(Complex.NaN.hashCode(), new Complex(Double.NaN, Double.NaN).hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("(1.0, 2.0)", new Complex(1.0, 2.0).toString());
        assertEquals("(1.0, -2.0)", new Complex(1.0, -2.0).toString());
    }

    @Test
    public void testExp() {
        Complex expOne = Complex.ONE.exp();
        assertEquals(Math.E, expOne.getReal(), 1e-12);
        assertEquals(0.0, expOne.getImaginary(), 1e-12);

        Complex expI = Complex.I.exp();
        assertEquals(Math.cos(1.0), expI.getReal(), 1e-12);
        assertEquals(Math.sin(1.0), expI.getImaginary(), 1e-12);
    }

    @Test
    public void testLog() {
        Complex logOne = Complex.ONE.log();
        assertEquals(0.0, logOne.getReal(), 1e-12);
        assertEquals(0.0, logOne.getImaginary(), 1e-12);

        Complex logI = Complex.I.log();
        assertEquals(0.0, logI.getReal(), 1e-12);
        assertEquals(Math.PI / 2.0, logI.getImaginary(), 1e-12);
    }

    @Test
    public void testSqrt() {
        Complex sqrtOne = Complex.ONE.sqrt();
        assertEquals(1.0, sqrtOne.getReal(), 1e-12);
        assertEquals(0.0, sqrtOne.getImaginary(), 1e-12);

        Complex sqrtNegOne = new Complex(-1.0, 0.0).sqrt();
        assertEquals(0.0, sqrtNegOne.getReal(), 1e-12);
        assertEquals(1.0, sqrtNegOne.getImaginary(), 1e-12);

        Complex sqrtI = Complex.I.sqrt();
        double root = Math.sqrt(0.5);
        assertEquals(root, sqrtI.getReal(), 1e-12);
        assertEquals(root, sqrtI.getImaginary(), 1e-12);
    }

    @Test
    public void testTrigonometricIdentitiesAtZero() {
        Complex sin0 = Complex.ZERO.sin();
        assertEquals(0.0, sin0.getReal(), 0.0);
        assertEquals(0.0, sin0.getImaginary(), 0.0);

        Complex cos0 = Complex.ZERO.cos();
        assertEquals(1.0, cos0.getReal(), 0.0);
        assertEquals(0.0, cos0.getImaginary(), 0.0);

        Complex tan0 = Complex.ZERO.tan();
        assertEquals(0.0, tan0.getReal(), 0.0);
        assertEquals(0.0, tan0.getImaginary(), 0.0);
    }
}