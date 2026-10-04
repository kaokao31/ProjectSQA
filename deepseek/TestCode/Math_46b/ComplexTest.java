package org.apache.commons.math.complex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComplexTest {

    private static final double EPS = 1e-12;

    @Test
    public void testStaticConstants() {
        assertEquals(0.0, Complex.ZERO.getReal(), 0.0);
        assertEquals(0.0, Complex.ZERO.getImaginary(), 0.0);
        assertEquals(1.0, Complex.ONE.getReal(), 0.0);
        assertEquals(0.0, Complex.ONE.getImaginary(), 0.0);
        assertEquals(0.0, Complex.I.getReal(), 0.0);
        assertEquals(1.0, Complex.I.getImaginary(), 0.0);
        assertTrue(Complex.NaN.isNaN());
    }

    @Test
    public void testConstructorAndGetters() {
        Complex c = new Complex(2.5, -3.5);
        assertEquals(2.5, c.getReal(), 0.0);
        assertEquals(-3.5, c.getImaginary(), 0.0);
        assertFalse(c.isNaN());
        assertFalse(c.isInfinite());
    }

    @Test
    public void testIsNaN() {
        assertTrue(new Complex(Double.NaN, 1.0).isNaN());
        assertTrue(new Complex(1.0, Double.NaN).isNaN());
        assertTrue(new Complex(Double.NaN, Double.NaN).isNaN());
        assertFalse(new Complex(1.0, 2.0).isNaN());
    }

    @Test
    public void testIsInfinite() {
        assertTrue(new Complex(Double.POSITIVE_INFINITY, 1.0).isInfinite());
        assertTrue(new Complex(1.0, Double.NEGATIVE_INFINITY).isInfinite());
        assertTrue(new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isInfinite());
        assertFalse(new Complex(Double.NaN, Double.POSITIVE_INFINITY).isInfinite());
        assertFalse(new Complex(1.0, 2.0).isInfinite());
    }

    @Test
    public void testEqualsSameObject() {
        Complex c = new Complex(3.0, -1.0);
        assertTrue(c.equals(c));
    }

    @Test
    public void testEqualsEqualValues() {
        Complex a = new Complex(3.0, -1.0);
        Complex b = new Complex(3.0, -1.0);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    @Test
    public void testEqualsNaN() {
        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(1.0, Double.NaN);
        assertTrue(nan1.equals(nan1));
        assertTrue(nan1.equals(nan2));
        assertTrue(nan2.equals(nan1));
        assertTrue(Complex.NaN.equals(nan1));
        assertFalse(nan1.equals(new Complex(1.0, 2.0)));
        assertFalse(new Complex(1.0, 2.0).equals(nan1));
    }

    @Test
    public void testEqualsNegativeZero() {
        Complex posZero = new Complex(0.0, 0.0);
        Complex negRealZero = new Complex(-0.0, 0.0);
        Complex negImagZero = new Complex(0.0, -0.0);
        assertFalse(posZero.equals(negRealZero));
        assertFalse(posZero.equals(negImagZero));
        assertFalse(negRealZero.equals(posZero));
        assertFalse(negImagZero.equals(posZero));
        assertFalse(negRealZero.equals(negImagZero));
    }

    @Test
    public void testEqualsDifferentTypes() {
        Complex c = new Complex(1.0, 2.0);
        assertFalse(c.equals(null));
        assertFalse(c.equals("not a complex"));
        assertFalse(c.equals(new Object()));
        assertFalse(c.equals(new Complex(1.0, 2.1)));
        assertFalse(c.equals(new Complex(1.1, 2.0)));
    }

    @Test
    public void testHashCodeEqualObjects() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        assertEquals(a.hashCode(), b.hashCode());

        Complex nan1 = new Complex(Double.NaN, 0.0);
        Complex nan2 = new Complex(0.0, Double.NaN);
        assertEquals(nan1.hashCode(), nan2.hashCode());
        assertEquals(Complex.NaN.hashCode(), nan1.hashCode());
    }

    @Test
    public void testHashCodeNegativeZeroDiffers() {
        Complex posZero = new Complex(0.0, 0.0);
        Complex negRealZero = new Complex(-0.0, 0.0);
        Complex negImagZero = new Complex(0.0, -0.0);
        assertTrue(posZero.hashCode() != negRealZero.hashCode());
        assertTrue(posZero.hashCode() != negImagZero.hashCode());
    }

    @Test
    public void testAdd() {
        Complex c = new Complex(1.0, 2.0).add(new Complex(3.0, -5.0));
        assertEquals(4.0, c.getReal(), 0.0);
        assertEquals(-3.0, c.getImaginary(), 0.0);
    }

    @Test
    public void testAddNaN() {
        assertTrue(new Complex(1.0, 2.0).add(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.add(new Complex(1.0, 2.0)).isNaN());
        assertTrue(Complex.NaN.add(Complex.NaN).isNaN());
    }

    @Test(expected = RuntimeException.class)
    public void testAddNull() {
        new Complex(1.0, 2.0).add(null);
    }

    @Test
    public void testSubtract() {
        Complex c = new Complex(5.0, 5.0).subtract(new Complex(3.0, 4.0));
        assertEquals(2.0, c.getReal(), 0.0);
        assertEquals(1.0, c.getImaginary(), 0.0);
        assertTrue(new Complex(1.0, 2.0).subtract(Complex.NaN).isNaN());
    }

    @Test(expected = RuntimeException.class)
    public void testSubtractNull() {
        new Complex(1.0, 2.0).subtract(null);
    }

    @Test
    public void testMultiply() {
        Complex c = new Complex(1.0, 2.0).multiply(new Complex(3.0, 4.0));
        assertEquals(-5.0, c.getReal(), EPS);
        assertEquals(10.0, c.getImaginary(), EPS);
        assertTrue(new Complex(1.0, 2.0).multiply(Complex.NaN).isNaN());
    }

    @Test(expected = RuntimeException.class)
    public void testMultiplyNull() {
        new Complex(1.0, 2.0).multiply(null);
    }

    @Test
    public void testDivideBasic() {
        Complex c = new Complex(6.0, 8.0).divide(new Complex(4.0, 2.0));
        assertEquals(2.0, c.getReal(), EPS);
        assertEquals(1.0, c.getImaginary(), EPS);

        c = new Complex(6.0, 8.0).divide(new Complex(2.0, 4.0));
        assertEquals(2.2, c.getReal(), EPS);
        assertEquals(-0.4, c.getImaginary(), EPS);
    }

    @Test
    public void testDivideSpecialCases() {
        assertTrue(new Complex(1.0, 2.0).divide(Complex.ZERO).isNaN());
        assertTrue(Complex.ZERO.divide(Complex.ZERO).isNaN());
        assertTrue(new Complex(1.0, 2.0).divide(Complex.NaN).isNaN());
        assertTrue(Complex.NaN.divide(new Complex(1.0, 2.0)).isNaN());

        Complex result = new Complex(1.0, 2.0).divide(new Complex(Double.POSITIVE_INFINITY, 0.0));
        assertTrue(result.equals(Complex.ZERO));

        assertTrue(new Complex(Double.POSITIVE_INFINITY, 0.0)
                   .divide(new Complex(Double.POSITIVE_INFINITY, 0.0)).isNaN());
    }

    @Test(expected = RuntimeException.class)
    public void testDivideNull() {
        new Complex(1.0, 2.0).divide(null);
    }

    @Test
    public void testConjugate() {
        Complex c = new Complex(1.0, 2.0).conjugate();
        assertEquals(1.0, c.getReal(), 0.0);
        assertEquals(-2.0, c.getImaginary(), 0.0);
        assertTrue(Complex.NaN.conjugate().isNaN());
    }

    @Test
    public void testNegate() {
        Complex c = new Complex(1.0, -2.0).negate();
        assertEquals(-1.0, c.getReal(), 0.0);
        assertEquals(2.0, c.getImaginary(), 0.0);
        assertTrue(Complex.NaN.negate().isNaN());
    }

    @Test
    public void testAbs() {
        assertEquals(0.0, Complex.ZERO.abs(), 0.0);
        assertEquals(5.0, new Complex(3.0, 4.0).abs(), EPS);
        assertEquals(Double.POSITIVE_INFINITY, new Complex(Double.POSITIVE_INFINITY, 0.0).abs(), 0.0);
        assertTrue(Double.isNaN(Complex.NaN.abs()));
    }

    @Test
    public void testSqrt() {
        Complex c = new Complex(3.0, 4.0).sqrt();
        assertEquals(2.0, c.getReal(), EPS);
        assertEquals(1.0, c.getImaginary(), EPS);

        c = new Complex(-4.0, 0.0).sqrt();
        assertEquals(0.0, c.getReal(), EPS);
        assertEquals(2.0, c.getImaginary(), EPS);

        c = Complex.ZERO.sqrt();
        assertEquals(0.0, c.getReal(), EPS);
        assertEquals(0.0, c.getImaginary(), EPS);

        assertTrue(Complex.NaN.sqrt().isNaN());
    }

    @Test
    public void testExp() {
        Complex c = new Complex(1.0, Math.PI / 2.0).exp();
        assertEquals(0.0, c.getReal(), 1e-12);
        assertEquals(Math.E, c.getImaginary(), EPS);
    }

    @Test
    public void testLog() {
        Complex c = new Complex(0.0, 1.0).log();
        assertEquals(0.0, c.getReal(), EPS);
        assertEquals(Math.PI / 2.0, c.getImaginary(), EPS);
    }

    @Test
    public void testSinCos() {
        double a = 1.0;
        double b = 2.0;

        Complex sin = new Complex(a, b).sin();
        assertEquals(Math.sin(a) * Math.cosh(b), sin.getReal(), EPS);
        assertEquals(Math.cos(a) * Math.sinh(b), sin.getImaginary(), EPS);

        Complex cos = new Complex(a, b).cos();
        assertEquals(Math.cos(a) * Math.cosh(b), cos.getReal(), EPS);
        assertEquals(-Math.sin(a) * Math.sinh(b), cos.getImaginary(), EPS);
    }

    @Test
    public void testTan() {
        Complex c = new Complex(Math.PI / 4.0, 0.0).tan();
        assertEquals(1.0, c.getReal(), EPS);
        assertEquals(0.0, c.getImaginary(), EPS);
    }

    @Test
    public void testSinhCosh() {
        double a = 1.0;
        double b = 2.0;

        Complex sinh = new Complex(a, b).sinh();
        assertEquals(Math.sinh(a) * Math.cos(b), sinh.getReal(), EPS);
        assertEquals(Math.cosh(a) * Math.sin(b), sinh.getImaginary(), EPS);

        Complex cosh = new Complex(a, b).cosh();
        assertEquals(Math.cosh(a) * Math.cos(b), cosh.getReal(), EPS);
        assertEquals(Math.sinh(a) * Math.sin(b), cosh.getImaginary(), EPS);
    }

    @Test
    public void testTanh() {
        Complex c = new Complex(1.0, 0.0).tanh();
        assertEquals(Math.tanh(1.0), c.getReal(), EPS);
        assertEquals(0.0, c.getImaginary(), EPS);
    }

    @Test
    public void testPow() {
        Complex c = new Complex(2.0, 0.0).pow(new Complex(3.0, 0.0));
        assertEquals(8.0, c.getReal(), EPS);
        assertEquals(0.0, c.getImaginary(), EPS);
        assertTrue(new Complex(2.0, 0.0).pow(Complex.NaN).isNaN());
    }

    @Test
    public void testValueOf() {
        Complex c = Complex.valueOf(2.0, -3.0);
        assertEquals(2.0, c.getReal(), 0.0);
        assertEquals(-3.0, c.getImaginary(), 0.0);
        assertTrue(Complex.valueOf(Double.NaN, 0.0).isNaN());
        assertTrue(Complex.valueOf(0.0, Double.NaN).isNaN());
    }

    @Test
    public void testToString() {
        assertNotNull(new Complex(1.0, 2.0).toString());
        assertTrue(new Complex(1.0, 2.0).toString().length() > 0);
    }
}