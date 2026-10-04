package org.apache.commons.math.complex;

import org.junit.Assert;
import org.junit.Test;

public class ComplexTest {

    @Test
    public void testDivideZeroByZero() {
        Complex c1 = Complex.ZERO;
        Complex c2 = Complex.ZERO;
        Complex result = c1.divide(c2);
        Assert.assertTrue(result.isNaN());
    }

    @Test
    public void testDivideByZero() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = Complex.ZERO;
        Complex result = c1.divide(c2);
        Assert.assertEquals(Complex.INF, result);
    }

    @Test
    public void testDivideZeroByNonZero() {
        Complex c1 = Complex.ZERO;
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.divide(c2);
        Assert.assertEquals(Complex.ZERO, result);
    }

    @Test
    public void testDivideNormal() {
        Complex c1 = new Complex(4.0, 3.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex result = c1.divide(c2);
        // (4+3i)/(1+2i) = ((4*1 + 3*2) + (3*1 - 4*2)i) / (1^2 + 2^2) = (10 - 5i) / 5 = 2 - i
        Assert.assertEquals(2.0, result.getReal(), 1e-12);
        Assert.assertEquals(-1.0, result.getImaginary(), 1e-12);
    }

    @Test
    public void testDivideInfiniteAndNaN() {
        Complex c1 = Complex.INF;
        Complex c2 = Complex.NaN;
        Complex result = c1.divide(c2);
        Assert.assertTrue(result.isNaN());

        Complex c3 = new Complex(Double.NaN, Double.NaN);
        Assert.assertTrue(c3.isNaN());
        Assert.assertFalse(c3.isInfinite());
    }

    @Test
    public void testConstructorAndGetters() {
        Complex c = new Complex(3.0, 4.0);
        Assert.assertEquals(3.0, c.getReal(), 1e-12);
        Assert.assertEquals(4.0, c.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN);
        Assert.assertTrue(cNaN.isNaN());

        Complex cReal = new Complex(5.0);
        Assert.assertEquals(5.0, cReal.getReal(), 1e-12);
        Assert.assertEquals(0.0, cReal.getImaginary(), 1e-12);
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = new Complex(1.0, 3.0);
        Complex cNaN = Complex.NaN;
        Complex cNaN2 = new Complex(Double.NaN, Double.NaN);

        Assert.assertTrue(c1.equals(c1));
        Assert.assertTrue(c1.equals(c2));
        Assert.assertFalse(c1.equals(c3));
        Assert.assertFalse(c1.equals(null));
        Assert.assertFalse(c1.equals("Some string"));
        Assert.assertTrue(cNaN.equals(cNaN2));

        Complex cNaNReal = new Complex(Double.NaN, 1.0);
        Complex cNaNReal2 = new Complex(1.0, Double.NaN);
        Assert.assertTrue(cNaNReal.equals(cNaNReal2));

        Assert.assertEquals(c1.hashCode(), c2.hashCode());
        Assert.assertEquals(cNaN.hashCode(), cNaN2.hashCode());
    }

    @Test
    public void testAdd() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.add(c2);
        Assert.assertEquals(4.0, result.getReal(), 1e-12);
        Assert.assertEquals(6.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(c1.add(cNaN).isNaN());
    }

    @Test
    public void testSubtract() {
        Complex c1 = new Complex(5.0, 7.0);
        Complex c2 = new Complex(2.0, 3.0);
        Complex result = c1.subtract(c2);
        Assert.assertEquals(3.0, result.getReal(), 1e-12);
        Assert.assertEquals(4.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(c1.subtract(cNaN).isNaN());
    }

    @Test
    public void testMultiply() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        // (1+2i)(3+4i) = 3 + 4i + 6i - 8 = -5 + 10i
        Complex result = c1.multiply(c2);
        Assert.assertEquals(-5.0, result.getReal(), 1e-12);
        Assert.assertEquals(10.0, result.getImaginary(), 1e-12);

        Complex cNaN = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(c1.multiply(cNaN).isNaN());
    }

    @Test
    public void testNegate() {
        Complex c = new Complex(1.0, -2.0);
        Complex result = c.negate();
        Assert.assertEquals(-1.0, result.getReal(), 1e-12);
        Assert.assertEquals(2.0, result.getImaginary(), 1e-12);

        Assert.assertTrue(Complex.NaN.negate().isNaN());
    }

    @Test
    public void testAbs() {
        Complex c = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, c.abs(), 1e-12);

        Assert.assertTrue(Double.isNaN(Complex.NaN.abs()));
        Assert.assertEquals(Double.POSITIVE_INFINITY, Complex.INF.abs(), 1e-12);
    }

    @Test
    public void testConjugate() {
        Complex c = new Complex(3.0, 4.0);
        Complex result = c.conjugate();
        Assert.assertEquals(3.0, result.getReal(), 1e-12);
        Assert.assertEquals(-4.0, result.getImaginary(), 1e-12);

        Assert.assertTrue(Complex.NaN.conjugate().isNaN());
    }

    @Test
    public void testLog() {
        Complex c = new Complex(Math.E, 0.0);
        Complex result = c.log();
        Assert.assertEquals(1.0, result.getReal(), 1e-12);
        Assert.assertEquals(0.0, result.getImaginary(), 1e-12);

        Assert.assertTrue(Complex.NaN.log().isNaN());
    }

    @Test
    public void testSqrt() {
        Complex c = new Complex(-4.0, 0.0);
        Complex result = c.sqrt();
        Assert.assertEquals(0.0, result.getReal(), 1e-12);
        Assert.assertEquals(2.0, result.getImaginary(), 1e-12);

        Assert.assertTrue(Complex.NaN.sqrt().isNaN());
    }

    @Test
    public void testSqrt1z() {
        Complex c = new Complex(0.0, 0.0);
        Complex result = c.sqrt1z();
        Assert.assertEquals(1.0, result.getReal(), 1e-12);
        Assert.assertEquals(0.0, result.getImaginary(), 1e-12);

        Assert.assertTrue(Complex.NaN.sqrt1z().isNaN());
    }

    @Test
    public void testPow() {
        Complex c = new Complex(2.0, 0.0);
        Complex x = new Complex(3.0, 0.0);
        Complex result = c.pow(x);
        Assert.assertEquals(8.0, result.getReal(), 1e-12);
        Assert.assertEquals(0.0, result.getImaginary(), 1e-12);

        Assert.assertTrue(Complex.NaN.pow(c).isNaN());
        Assert.assertTrue(c.pow(Complex.NaN).isNaN());
    }

    @Test
    public void testExp() {
        Complex c = new Complex(0.0, Math.PI);
        Complex result = c.exp();
        Assert.assertEquals(-1.0, result.getReal(), 1e-12);
        Assert.assertEquals(0.0, result.getImaginary(), 1e-12);

        Assert.assertTrue(Complex.NaN.exp().isNaN());
    }

    @Test
    public void testCosAndSinAndTan() {
        Complex c = new Complex(0.0, 0.0);
        Assert.assertEquals(1.0, c.cos().getReal(), 1e-12);
        Assert.assertEquals(0.0, c.sin().getReal(), 1e-12);
        Assert.assertEquals(0.0, c.tan().getReal(), 1e-12);

        Assert.assertTrue(Complex.NaN.cos().isNaN());
        Assert.assertTrue(Complex.NaN.sin().isNaN());
        Assert.assertTrue(Complex.NaN.tan().isNaN());
    }

    @Test
    public void testCoshAndSinhAndTanh() {
        Complex c = new Complex(0.0, 0.0);
        Assert.assertEquals(1.0, c.cosh().getReal(), 1e-12);
        Assert.assertEquals(0.0, c.sinh().getReal(), 1e-12);
        Assert.assertEquals(0.0, c.tanh().getReal(), 1e-12);

        Assert.assertTrue(Complex.NaN.cosh().isNaN());
        Assert.assertTrue(Complex.NaN.sinh().isNaN());
        Assert.assertTrue(Complex.NaN.tanh().isNaN());
    }

    @Test
    public void testAcosAndAsinAndAtan() {
        Complex c = new Complex(1.0, 0.0);
        Assert.assertNotNull(c.acos());
        Assert.assertNotNull(c.asin());
        Assert.assertNotNull(c.atan());

        Assert.assertTrue(Complex.NaN.acos().isNaN());
        Assert.assertTrue(Complex.NaN.asin().isNaN());
        Assert.assertTrue(Complex.NaN.atan().isNaN());
    }

    @Test
    public void testCreateComplex() {
        Complex c = new Complex(1.0, 2.0);
        Complex instance = c.createComplex(3.0, 4.0);
        Assert.assertEquals(3.0, instance.getReal(), 1e-12);
        Assert.assertEquals(4.0, instance.getImaginary(), 1e-12);
    }

    @Test
    public void testToString() {
        Complex c = new Complex(1.0, 2.0);
        Assert.assertEquals("(1.0, 2.0)", c.toString());
        Assert.assertEquals("(NaN, NaN)", Complex.NaN.toString());
    }

    @Test
    public void testScalarOperations() {
        Complex c = new Complex(2.0, 3.0);
        
        Complex scaled = c.multiply(2.0);
        Assert.assertEquals(4.0, scaled.getReal(), 1e-12);
        Assert.assertEquals(6.0, scaled.getImaginary(), 1e-12);

        Complex multipliedInt = c.multiply(2);
        Assert.assertEquals(4.0, multipliedInt.getReal(), 1e-12);
        Assert.assertEquals(6.0, multipliedInt.getImaginary(), 1e-12);

        Complex dividedScalar = c.divide(2.0);
        Assert.assertEquals(1.0, dividedScalar.getReal(), 1e-12);
        Assert.assertEquals(1.5, dividedScalar.getImaginary(), 1e-12);
    }
}