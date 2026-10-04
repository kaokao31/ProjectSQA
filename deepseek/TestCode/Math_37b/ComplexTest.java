package org.apache.commons.math.complex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComplexTest {

    private static final double EPS = 1e-9;

    private void assertComplexEquals(Complex expected, Complex actual) {
        assertNotNull(actual);
        assertEquals(expected.getReal(), actual.getReal(), EPS);
        assertEquals(expected.getImaginary(), actual.getImaginary(), EPS);
    }

    @Test
    public void testConstructorAndAccessors() {
        Complex z = new Complex(3.0, -4.0);
        assertEquals(3.0, z.getReal(), 0.0);
        assertEquals(-4.0, z.getImaginary(), 0.0);
        assertFalse(z.isNaN());
        assertFalse(z.isInfinite());
    }

    @Test
    public void testIsNaNIsInfinite() {
        assertTrue(new Complex(Double.NaN, 1.0).isNaN());
        assertTrue(new Complex(1.0, Double.NaN).isNaN());
        assertFalse(new Complex(Double.NaN, 1.0).isInfinite());

        assertTrue(new Complex(Double.POSITIVE_INFINITY, 1.0).isInfinite());
        assertTrue(new Complex(1.0, Double.NEGATIVE_INFINITY).isInfinite());
        assertFalse(new Complex(Double.POSITIVE_INFINITY, 1.0).isNaN());

        assertFalse(new Complex(1.0, 2.0).isNaN());
        assertFalse(new Complex(1.0, 2.0).isInfinite());
    }

    @Test
    public void testAddSubtract() {
        Complex z = new Complex(1.0, 2.0);

        assertComplexEquals(new Complex(4.0, 6.0), z.add(new Complex(3.0, 4.0)));
        assertComplexEquals(new Complex(4.0, 2.0), z.add(3.0));

        assertComplexEquals(new Complex(-2.0, -2.0), z.subtract(new Complex(3.0, 4.0)));
        assertComplexEquals(new Complex(-2.0, 2.0), z.subtract(3.0));
    }

    @Test
    public void testMultiply() {
        Complex z = new Complex(1.0, 2.0);

        assertComplexEquals(new Complex(-5.0, 10.0), z.multiply(new Complex(3.0, 4.0)));
        assertComplexEquals(new Complex(3.0, 6.0), z.multiply(3.0));
    }

    @Test
    public void testDivide() {
        Complex z = new Complex(1.0, 2.0);

        assertComplexEquals(new Complex(1.5, 0.5), z.divide(new Complex(1.0, 1.0)));
        assertComplexEquals(new Complex(0.5, 1.0), z.divide(2.0));
    }

    @Test
    public void testConjugate() {
        assertComplexEquals(new Complex(1.0, -2.0), new Complex(1.0, 2.0).conjugate());
        assertComplexEquals(new Complex(-1.0, 3.0), new Complex(-1.0, -3.0).conjugate());
    }

    @Test
    public void testReciprocal() {
        assertComplexEquals(new Complex(1.0, 0.0), new Complex(1.0, 0.0).reciprocal());
        assertComplexEquals(new Complex(0.0, -1.0), new Complex(0.0, 1.0).reciprocal());
        assertComplexEquals(new Complex(0.2, -0.4), new Complex(1.0, 2.0).reciprocal());
    }

    @Test
    public void testAbs() {
        assertEquals(5.0, new Complex(3.0, 4.0).abs(), 0.0);
        assertEquals(1.0, new Complex(0.0, 1.0).abs(), 0.0);
        assertEquals(0.0, new Complex(0.0, 0.0).abs(), 0.0);
        assertTrue(Double.isNaN(new Complex(Double.NaN, 0.0).abs()));
    }

    @Test
    public void testArg() {
        assertEquals(0.0, new Complex(1.0, 0.0).arg(), EPS);
        assertEquals(Math.PI / 2.0, new Complex(0.0, 1.0).arg(), EPS);
        assertEquals(Math.PI, new Complex(-1.0, 0.0).arg(), EPS);
        assertEquals(-Math.PI / 2.0, new Complex(0.0, -1.0).arg(), EPS);
    }

    @Test
    public void testExpLog() {
        assertComplexEquals(new Complex(Math.E, 0.0), new Complex(1.0, 0.0).exp());
        assertComplexEquals(new Complex(-1.0, 0.0), new Complex(0.0, Math.PI).exp());
        assertComplexEquals(new Complex(0.0, 1.0), new Complex(0.0, Math.PI / 2.0).exp());

        assertComplexEquals(new Complex(0.0, 0.0), new Complex(1.0, 0.0).log());
        assertComplexEquals(new Complex(0.0, Math.PI / 2.0), new Complex(0.0, 1.0).log());
        assertComplexEquals(new Complex(0.0, Math.PI), new Complex(-1.0, 0.0).log());
    }

    @Test
    public void testSqrtPositiveAndImaginaryValues() {
        assertComplexEquals(new Complex(2.0, 0.0), new Complex(4.0, 0.0).sqrt());
        assertComplexEquals(new Complex(3.0, 0.0), new Complex(9.0, 0.0).sqrt());
        assertComplexEquals(new Complex(1.0, 1.0), new Complex(0.0, 2.0).sqrt());
        assertComplexEquals(new Complex(1.0, -1.0), new Complex(0.0, -2.0).sqrt());
        assertComplexEquals(new Complex(2.0, 1.0), new Complex(3.0, 4.0).sqrt());
        assertComplexEquals(new Complex(1.0, 2.0), new Complex(-3.0, 4.0).sqrt());
        assertComplexEquals(new Complex(0.0, 0.0), new Complex(0.0, 0.0).sqrt());
    }

    @Test
    public void testSqrtNegativeReal() {
        Complex sqrtNeg1 = new Complex(-1.0, 0.0).sqrt();
        assertFalse("sqrt(-1) must not be NaN", sqrtNeg1.isNaN());
        assertComplexEquals(new Complex(0.0, 1.0), sqrtNeg1);

        Complex sqrtNeg4 = new Complex(-4.0, 0.0).sqrt();
        assertFalse("sqrt(-4) must not be NaN", sqrtNeg4.isNaN());
        assertComplexEquals(new Complex(0.0, 2.0), sqrtNeg4);

        Complex sqrtNeg9 = new Complex(-9.0, 0.0).sqrt();
        assertFalse("sqrt(-9) must not be NaN", sqrtNeg9.isNaN());
        assertComplexEquals(new Complex(0.0, 3.0), sqrtNeg9);
    }

    @Test
    public void testSqrtNaN() {
        Complex result = new Complex(Double.NaN, 0.0).sqrt();
        assertTrue(result.isNaN());
    }

    @Test
    public void testSinCos() {
        Complex z = new Complex(1.0, 2.0);

        Complex sinExpected = new Complex(
                Math.sin(1.0) * Math.cosh(2.0),
                Math.cos(1.0) * Math.sinh(2.0));
        assertComplexEquals(sinExpected, z.sin());

        Complex cosExpected = new Complex(
                Math.cos(1.0) * Math.cosh(2.0),
                -Math.sin(1.0) * Math.sinh(2.0));
        assertComplexEquals(cosExpected, z.cos());
    }

    @Test
    public void testTan() {
        Complex z = new Complex(1.0, 2.0);

        double denominator = Math.cos(2.0) + Math.cosh(2.0);
        Complex tanExpected = new Complex(
                Math.sin(2.0) / denominator,
                Math.sinh(2.0) / denominator);

        assertComplexEquals(tanExpected, z.tan());
        assertComplexEquals(new Complex(0.0, 0.0), new Complex(0.0, 0.0).tan());
    }

    @Test
    public void testSinhCoshTanh() {
        Complex z = new Complex(2.0, 3.0);

        Complex sinhExpected = new Complex(
                Math.sinh(2.0) * Math.cos(3.0),
                Math.cosh(2.0) * Math.sin(3.0));
        assertComplexEquals(sinhExpected, z.sinh());

        Complex coshExpected = new Complex(
                Math.cosh(2.0) * Math.cos(3.0),
                Math.sinh(2.0) * Math.sin(3.0));
        assertComplexEquals(coshExpected, z.cosh());

        double denom = Math.cosh(4.0) + Math.cos(6.0);
        Complex tanhExpected = new Complex(
                Math.sinh(4.0) / denom,
                Math.sin(6.0) / denom);
        assertComplexEquals(tanhExpected, z.tanh());
    }

    @Test
    public void testNaNPropagation() {
        Complex nan = new Complex(Double.NaN, 1.0);

        assertTrue(nan.add(new Complex(1.0, 2.0)).isNaN());
        assertTrue(nan.subtract(new Complex(1.0, 2.0)).isNaN());
        assertTrue(nan.multiply(new Complex(1.0, 2.0)).isNaN());
        assertTrue(nan.divide(new Complex(1.0, 2.0)).isNaN());
        assertTrue(nan.conjugate().isNaN());
        assertTrue(Double.isNaN(nan.abs()));
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);

        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertFalse(a.equals(null));
        assertFalse(a.equals("not a complex"));
        assertFalse(a.equals(new Complex(1.0, 3.0)));

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testToStringNotNull() {
        assertNotNull(new Complex(1.0, 2.0).toString());
    }
}