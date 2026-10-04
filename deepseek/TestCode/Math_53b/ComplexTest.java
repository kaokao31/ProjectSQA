package org.apache.commons.math.complex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComplexTest {

    @Test
    public void testConstructorAndGetters() {
        Complex z = new Complex(3.0, -4.0);
        assertEquals(3.0, z.getReal(), 0.0);
        assertEquals(-4.0, z.getImaginary(), 0.0);
        assertFalse(z.isNaN());
        assertFalse(z.isInfinite());
    }

    @Test
    public void testConstructorNaN() {
        Complex z1 = new Complex(Double.NaN, 1.0);
        assertTrue(z1.isNaN());
        assertFalse(z1.isInfinite());

        Complex z2 = new Complex(1.0, Double.NaN);
        assertTrue(z2.isNaN());
        assertFalse(z2.isInfinite());
    }

    @Test
    public void testConstructorInfinite() {
        Complex z1 = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertFalse(z1.isNaN());
        assertTrue(z1.isInfinite());

        Complex z2 = new Complex(0.0, Double.NEGATIVE_INFINITY);
        assertFalse(z2.isNaN());
        assertTrue(z2.isInfinite());

        Complex z3 = new Complex(Double.NaN, Double.POSITIVE_INFINITY);
        assertTrue(z3.isNaN());
        assertFalse(z3.isInfinite());
    }

    @Test
    public void testAbs() {
        assertEquals(5.0, new Complex(3.0, 4.0).abs(), 0.0);
        assertEquals(5.0, new Complex(-3.0, 4.0).abs(), 0.0);
        assertEquals(3.0, new Complex(-3.0, 0.0).abs(), 0.0);
        assertEquals(4.0, new Complex(0.0, -4.0).abs(), 0.0);
        assertEquals(0.0, new Complex(0.0, 0.0).abs(), 0.0);
    }

    @Test
    public void testAbsLargeFiniteValues() {
        double max = Double.MAX_VALUE;

        double a1 = new Complex(max, 0.0).abs();
        assertFalse(Double.isNaN(a1));
        assertFalse(Double.isInfinite(a1));
        assertEquals(max, a1, max * 1e-15);

        double a2 = new Complex(0.0, max).abs();
        assertFalse(Double.isNaN(a2));
        assertFalse(Double.isInfinite(a2));
        assertEquals(max, a2, max * 1e-15);

        double half = max / 2.0;
        double expected = Math.sqrt(2.0) * half;
        double a3 = new Complex(half, half).abs();
        assertFalse(Double.isNaN(a3));
        assertFalse(Double.isInfinite(a3));
        assertEquals(expected, a3, expected * 1e-12);
    }

    @Test
    public void testAbsNaN() {
        assertTrue(Double.isNaN(new Complex(Double.NaN, 1.0).abs()));
        assertTrue(Double.isNaN(new Complex(1.0, Double.NaN).abs()));
    }

    @Test
    public void testAbsInfinite() {
        assertEquals(Double.POSITIVE_INFINITY,
                new Complex(Double.POSITIVE_INFINITY, 0.0).abs(), 0.0);
        assertEquals(Double.POSITIVE_INFINITY,
                new Complex(0.0, Double.NEGATIVE_INFINITY).abs(), 0.0);
    }

    @Test
    public void testAdd() {
        Complex z = new Complex(1.0, 2.0).add(new Complex(3.0, 4.0));
        assertEquals(4.0, z.getReal(), 0.0);
        assertEquals(6.0, z.getImaginary(), 0.0);
    }

    @Test
    public void testAddNaN() {
        Complex z = new Complex(1.0, 2.0).add(new Complex(Double.NaN, 0.0));
        assertTrue(z.isNaN());
    }

    @Test
    public void testAddInfinite() {
        Complex z = new Complex(Double.POSITIVE_INFINITY, 1.0)
                .add(new Complex(2.0, 3.0));
        assertTrue(z.isInfinite());
        assertEquals(Double.POSITIVE_INFINITY, z.getReal(), 0.0);
        assertEquals(4.0, z.getImaginary(), 0.0);
    }

    @Test
    public void testSubtract() {
        Complex z = new Complex(5.0, 7.0).subtract(new Complex(3.0, 2.0));
        assertEquals(2.0, z.getReal(), 0.0);
        assertEquals(5.0, z.getImaginary(), 0.0);
    }

    @Test
    public void testMultiply() {
        Complex z = new Complex(1.0, 2.0).multiply(new Complex(3.0, 4.0));
        assertEquals(-5.0, z.getReal(), 0.0);
        assertEquals(10.0, z.getImaginary(), 0.0);
    }

    @Test
    public void testMultiplyByZero() {
        Complex z = new Complex(1.0, 2.0).multiply(new Complex(0.0, 0.0));
        assertEquals(0.0, z.getReal(), 0.0);
        assertEquals(0.0, z.getImaginary(), 0.0);
        assertFalse(z.isNaN());
    }

    @Test
    public void testMultiplyNaN() {
        Complex z = new Complex(1.0, 2.0).multiply(new Complex(Double.NaN, 0.0));
        assertTrue(z.isNaN());
    }

    @Test
    public void testDivide() {
        Complex z = new Complex(1.0, 2.0).divide(new Complex(3.0, 4.0));
        assertEquals(11.0 / 25.0, z.getReal(), 1e-12);
        assertEquals(2.0 / 25.0, z.getImaginary(), 1e-12);
    }

    @Test
    public void testDivideByZero() {
        Complex z = new Complex(1.0, 2.0).divide(new Complex(0.0, 0.0));
        assertTrue(z.isNaN());
    }

    @Test
    public void testDivideNaN() {
        Complex z = new Complex(1.0, 2.0).divide(new Complex(Double.NaN, 0.0));
        assertTrue(z.isNaN());
    }

    @Test
    public void testDivideInfinite() {
        Complex z1 = new Complex(1.0, 2.0)
                .divide(new Complex(Double.POSITIVE_INFINITY, 0.0));
        assertEquals(0.0, z1.getReal(), 0.0);
        assertEquals(0.0, z1.getImaginary(), 0.0);

        Complex z2 = new Complex(Double.POSITIVE_INFINITY, 0.0)
                .divide(new Complex(1.0, 2.0));
        assertTrue(z2.isInfinite());
    }

    @Test
    public void testConjugate() {
        Complex z = new Complex(3.0, -4.0).conjugate();
        assertEquals(3.0, z.getReal(), 0.0);
        assertEquals(4.0, z.getImaginary(), 0.0);
    }

    @Test
    public void testNegate() {
        Complex z = new Complex(3.0, -4.0).negate();
        assertEquals(-3.0, z.getReal(), 0.0);
        assertEquals(4.0, z.getImaginary(), 0.0);
    }

    @Test
    public void testArg() {
        assertEquals(Math.PI / 2.0, new Complex(0.0, 1.0).arg(), 1e-12);
        assertEquals(0.0, new Complex(1.0, 0.0).arg(), 1e-12);
        assertEquals(-Math.PI / 4.0, new Complex(1.0, -1.0).arg(), 1e-12);
        assertTrue(Double.isNaN(new Complex(Double.NaN, 0.0).arg()));
    }

    @Test
    public void testReciprocal() {
        Complex z = new Complex(1.0, 2.0).reciprocal();
        assertEquals(0.2, z.getReal(), 1e-12);
        assertEquals(-0.4, z.getImaginary(), 1e-12);

        assertTrue(new Complex(0.0, 0.0).reciprocal().isNaN());
    }

    @Test
    public void testExp() {
        Complex z1 = new Complex(1.0, 0.0).exp();
        assertEquals(Math.E, z1.getReal(), 1e-12);
        assertEquals(0.0, z1.getImaginary(), 1e-12);

        Complex z2 = new Complex(0.0, Math.PI).exp();
        assertEquals(-1.0, z2.getReal(), 1e-12);
        assertEquals(0.0, z2.getImaginary(), 1e-12);
    }

    @Test
    public void testLog() {
        Complex z1 = new Complex(1.0, 0.0).log();
        assertEquals(0.0, z1.getReal(), 1e-12);
        assertEquals(0.0, z1.getImaginary(), 1e-12);

        Complex z2 = new Complex(0.0, 1.0).log();
        assertEquals(0.0, z2.getReal(), 1e-12);
        assertEquals(Math.PI / 2.0, z2.getImaginary(), 1e-12);
    }

    @Test
    public void testSinCosTan() {
        Complex sin = new Complex(1.0, 0.0).sin();
        assertEquals(Math.sin(1.0), sin.getReal(), 1e-12);
        assertEquals(0.0, sin.getImaginary(), 1e-12);

        Complex cos = new Complex(1.0, 0.0).cos();
        assertEquals(Math.cos(1.0), cos.getReal(), 1e-12);
        assertEquals(0.0, cos.getImaginary(), 1e-12);

        Complex tan = new Complex(1.0, 0.0).tan();
        assertEquals(Math.tan(1.0), tan.getReal(), 1e-12);
        assertEquals(0.0, tan.getImaginary(), 1e-12);
    }

    @Test
    public void testSqrt() {
        Complex z1 = new Complex(1.0, 0.0).sqrt();
        assertEquals(1.0, z1.getReal(), 1e-12);
        assertEquals(0.0, z1.getImaginary(), 1e-12);

        Complex z2 = new Complex(-1.0, 0.0).sqrt();
        assertEquals(0.0, z2.getReal(), 1e-12);
        assertEquals(1.0, z2.getImaginary(), 1e-12);
    }

    @Test
    public void testEquals() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        assertTrue(a.equals(b));
        assertFalse(a.equals(new Complex(1.0, 3.0)));
        assertFalse(a.equals(null));
        assertFalse(a.equals("not a complex"));
        assertTrue(new Complex(Double.NaN, 0.0).equals(new Complex(Double.NaN, 0.0)));
    }

    @Test
    public void testHashCode() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        assertEquals(a.hashCode(), b.hashCode());
    }
}