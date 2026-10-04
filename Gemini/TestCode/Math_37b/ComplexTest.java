package org.apache.commons.math.complex;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ComplexTest {

    private static final double EPSILON = 1e-12;

    @Test
    public void testConstructorAndGetters() {
        Complex c = new Complex(3.0, 4.0);
        Assert.assertEquals(3.0, c.getReal(), EPSILON);
        Assert.assertEquals(4.0, c.getImaginary(), EPSILON);
        Assert.assertFalse(c.isNaN());
        Assert.assertFalse(c.isInfinite());
    }

    @Test
    public void testConstructorsWithNaNAndInf() {
        Complex cNaN = new Complex(Double.NaN, 1.0);
        Assert.assertTrue(cNaN.isNaN());

        Complex cNaNImag = new Complex(1.0, Double.NaN);
        Assert.assertTrue(cNaNImag.isNaN());

        Complex cInf = new Complex(Double.POSITIVE_INFINITY, 1.0);
        Assert.assertTrue(cInf.isInfinite());

        Complex cInfImag = new Complex(1.0, Double.NEGATIVE_INFINITY);
        Assert.assertTrue(cInfImag.isInfinite());
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = new Complex(1.0, 3.0);
        Complex c4 = new Complex(2.0, 2.0);
        Complex cNaN = Complex.NaN;
        Complex cNaN2 = new Complex(Double.NaN, Double.NaN);

        Assert.assertTrue(c1.equals(c1));
        Assert.assertTrue(c1.equals(c2));
        Assert.assertEquals(c1.hashCode(), c2.hashCode());

        Assert.assertFalse(c1.equals(c3));
        Assert.assertFalse(c1.equals(c4));
        Assert.assertFalse(c1.equals(null));
        Assert.assertFalse(c1.equals("Some String"));

        Assert.assertTrue(cNaN.equals(cNaN2));
        Assert.assertEquals(cNaN.hashCode(), cNaN2.hashCode());

        Complex cRealNaN1 = new Complex(Double.NaN, 2.0);
        Complex cRealNaN2 = new Complex(Double.NaN, 3.0);
        Assert.assertTrue(cRealNaN1.equals(cRealNaN2));
    }

    @Test
    public void testAbs() {
        Complex c = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, c.abs(), EPSILON);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(Double.isNaN(cNaN.abs()));

        Complex cInf = new Complex(Double.POSITIVE_INFINITY, Double.NaN);
        Assert.assertTrue(Double.isNaN(cInf.abs()));

        Complex cInf2 = new Complex(Double.POSITIVE_INFINITY, 3.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, cInf2.abs(), EPSILON);
    }

    @Test
    public void testAdd() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex sum = c1.add(c2);

        Assert.assertEquals(4.0, sum.getReal(), EPSILON);
        Assert.assertEquals(6.0, sum.getImaginary(), EPSILON);

        Complex sumNaN = c1.add(Complex.NaN);
        Assert.assertTrue(sumNaN.isNaN());

        Complex sumNull = c1.add(null);
        // Depending on implementation, or throws NPE. Let's check safely or expect NPE if designed.
        // Actually Commons Math usually throws IllegalArgumentException or NullPointerException.
        try {
            c1.add(null);
            Assert.fail("Expected NullPointerException or IllegalArgumentException");
        } catch (NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConjugate() {
        Complex c = new Complex(1.0, 2.0);
        Complex conj = c.conjugate();
        Assert.assertEquals(1.0, conj.getReal(), EPSILON);
        Assert.assertEquals(-2.0, conj.getImaginary(), EPSILON);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.conjugate().isNaN());
    }

    @Test
    public void testDivide() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex div = c1.divide(c2);

        // (1+2i)/(3+4i) = ((1*3 + 2*4) + (2*3 - 1*4)i) / (9 + 16) = (11 + 2i)/25 = 0.44 + 0.08i
        Assert.assertEquals(0.44, div.getReal(), EPSILON);
        Assert.assertEquals(0.08, div.getImaginary(), EPSILON);

        Complex divNaN = c1.divide(Complex.NaN);
        Assert.assertTrue(divNaN.isNaN());

        Complex divZero = c1.divide(Complex.ZERO);
        Assert.assertTrue(divZero.isInfinite());
    }

    @Test
    public void testMultiply() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex mult = c1.multiply(c2);

        // (1+2i)(3+4i) = 3 + 4i + 6i - 8 = -5 + 10i
        Assert.assertEquals(-5.0, mult.getReal(), EPSILON);
        Assert.assertEquals(10.0, mult.getImaginary(), EPSILON);

        Complex multDouble = c1.multiply(2.5);
        Assert.assertEquals(2.5, multDouble.getReal(), EPSILON);
        Assert.assertEquals(5.0, multDouble.getImaginary(), EPSILON);

        Complex multNaN = c1.multiply(Complex.NaN);
        Assert.assertTrue(multNaN.isNaN());
        
        Complex multInf = c1.multiply(Complex.INF);
        Assert.assertTrue(multInf.isInfinite());
    }

    @Test
    public void testNegate() {
        Complex c = new Complex(1.0, -2.0);
        Complex neg = c.negate();
        Assert.assertEquals(-1.0, neg.getReal(), EPSILON);
        Assert.assertEquals(2.0, neg.getImaginary(), EPSILON);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.negate().isNaN());
    }

    @Test
    public void testSubtract() {
        Complex c1 = new Complex(3.0, 5.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex sub = c1.subtract(c2);

        Assert.assertEquals(2.0, sub.getReal(), EPSILON);
        Assert.assertEquals(3.0, sub.getImaginary(), EPSILON);

        Complex subNaN = c1.subtract(Complex.NaN);
        Assert.assertTrue(subNaN.isNaN());
    }

    @Test
    public void testAcos() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.acos();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.acos().isNaN());
    }

    @Test
    public void testAsin() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.asin();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.asin().isNaN());
    }

    @Test
    public void testAtan() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.atan();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.atan().isNaN());
    }

    @Test
    public void testCos() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.cos();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.cos().isNaN());
    }

    @Test
    public void testCosh() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.cosh();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.cosh().isNaN());
    }

    @Test
    public void testExp() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.exp();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.exp().isNaN());
    }

    @Test
    public void testLog() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.log();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.log().isNaN());
    }

    @Test
    public void testPow() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 1.0);
        Complex res = c1.pow(c2);
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.pow(c1).isNaN());
        Assert.assertTrue(c1.pow(cNaN).isNaN());
    }

    @Test
    public void testSin() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.sin();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.sin().isNaN());
    }

    @Test
    public void testSinh() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.sinh();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.sinh().isNaN());
    }

    @Test
    public void testSqrt() {
        Complex c = new Complex(3.0, 4.0);
        Complex res = c.sqrt();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.sqrt().isNaN());
        
        Complex cPolar = new Complex(-3.0, 0.0);
        Assert.assertNotNull(cPolar.sqrt());
    }

    @Test
    public void testSqrt1z() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.sqrt1z();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.sqrt1z().isNaN());
    }

    @Test
    public void testTan() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.tan();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.tan().isNaN());
    }

    @Test
    public void testTanh() {
        Complex c = new Complex(1.0, 2.0);
        Complex res = c.tanh();
        Assert.assertNotNull(res);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(cNaN.tanh().isNaN());
    }

    @Test
    public void testGetArgument() {
        Complex c = new Complex(1.0, 1.0);
        Assert.assertEquals(Math.PI / 4, c.getArgument(), EPSILON);

        Complex cNaN = Complex.NaN;
        Assert.assertTrue(Double.isNaN(cNaN.getArgument()));
    }

    @Test
    public void testNthRoot() {
        Complex c = new Complex(1.0, 0.0);
        List<Complex> roots = c.nthRoot(2);
        Assert.assertEquals(2, roots.size());

        Complex cNaN = Complex.NaN;
        List<Complex> nanRoots = cNaN.nthRoot(2);
        Assert.assertEquals(1, nanRoots.size());
        Assert.assertTrue(nanRoots.get(0).isNaN());

        try {
            c.nthRoot(0);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToString() {
        Complex c = new Complex(1.0, 2.0);
        String str = c.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("1.0"));
        Assert.assertTrue(str.contains("2.0"));
    }
}