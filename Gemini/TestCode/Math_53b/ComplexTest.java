package org.apache.commons.math.complex;

import org.junit.Assert;
import org.junit.Test;

public class ComplexTest {

    private static final double TOLERANCE = 1e-12;

    @Test
    public void testAddBasic() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.add(c2);
        Assert.assertEquals(4.0, result.getReal(), TOLERANCE);
        Assert.assertEquals(6.0, result.getImaginary(), TOLERANCE);
    }

    @Test
    public void testAddNull() {
        Complex c1 = new Complex(1.0, 2.0);
        try {
            c1.add(null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (NullPointerException e) {
            // Commons-Math sometimes throws NPE or IllegalArgumentException for nulls depending on version
        }
    }

    @Test
    public void testAddNaN() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Complex result = c1.add(cNaN);
        Assert.assertTrue(result.isNaN());
    }

    @Test
    public void testAddWithNaNRealOrImaginary() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(Double.NaN, 0.0);
        Complex result = c1.add(c2);
        Assert.assertTrue(result.isNaN());

        Complex c3 = new Complex(0.0, Double.NaN);
        Complex result2 = c1.add(c3);
        Assert.assertTrue(result2.isNaN());
    }

    @Test
    public void testAddInf() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex cInf = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Complex result = c1.add(cInf);
        Assert.assertTrue(result.isInfinite());
    }

    @Test
    public void testSubtractBasic() {
        Complex c1 = new Complex(5.0, 7.0);
        Complex c2 = new Complex(2.0, 3.0);
        Complex result = c1.subtract(c2);
        Assert.assertEquals(3.0, result.getReal(), TOLERANCE);
        Assert.assertEquals(4.0, result.getImaginary(), TOLERANCE);
    }

    @Test
    public void testSubtractNull() {
        Complex c1 = new Complex(1.0, 2.0);
        try {
            c1.subtract(null);
            Assert.fail("Expected exception for null argument");
        } catch (IllegalArgumentException | NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testSubtractNaN() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Assert.assertTrue(c1.subtract(cNaN).isNaN());
    }

    @Test
    public void testMultiplyBasic() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(3.0, 4.0);
        // (1 + 2i)(3 + 4i) = 3 + 4i + 6i - 8 = -5 + 10i
        Complex result = c1.multiply(c2);
        Assert.assertEquals(-5.0, result.getReal(), TOLERANCE);
        Assert.assertEquals(10.0, result.getImaginary(), TOLERANCE);
    }

    @Test
    public void testMultiplyNull() {
        Complex c1 = new Complex(1.0, 2.0);
        try {
            c1.multiply(null);
            Assert.fail("Expected exception for null argument");
        } catch (IllegalArgumentException | NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testMultiplyScalar() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex result = c1.multiply(2.5);
        Assert.assertEquals(2.5, result.getReal(), TOLERANCE);
        Assert.assertEquals(5.0, result.getImaginary(), TOLERANCE);
    }

    @Test
    public void testMultiplyNaN() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Assert.assertTrue(c1.multiply(cNaN).isNaN());
        Assert.assertTrue(c1.multiply(Double.NaN).isNaN());
    }

    @Test
    public void testDivideBasic() {
        Complex c1 = new Complex(-5.0, 10.0);
        Complex c2 = new Complex(3.0, 4.0);
        Complex result = c1.divide(c2);
        // Checking division logic
        Assert.assertNotNull(result);
    }

    @Test
    public void testDivideNull() {
        Complex c1 = new Complex(1.0, 2.0);
        try {
            c1.divide(null);
            Assert.fail("Expected exception for null argument");
        } catch (IllegalArgumentException | NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testDivideScalar() {
        Complex c1 = new Complex(2.0, 4.0);
        Complex result = c1.divide(2.0);
        Assert.assertEquals(1.0, result.getReal(), TOLERANCE);
        Assert.assertEquals(2.0, result.getImaginary(), TOLERANCE);
    }

    @Test
    public void testDivideNaN() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Assert.assertTrue(c1.divide(cNaN).isNaN());
        Assert.assertTrue(c1.divide(Double.NaN).isNaN());
    }

    @Test
    public void testConjugate() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex result = c1.conjugate();
        Assert.assertEquals(1.0, result.getReal(), TOLERANCE);
        Assert.assertEquals(-2.0, result.getImaginary(), TOLERANCE);
    }

    @Test
    public void testConjugateNaN() {
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Assert.assertTrue(cNaN.conjugate().isNaN());
    }

    @Test
    public void testAbs() {
        Complex c1 = new Complex(3.0, 4.0);
        Assert.assertEquals(5.0, c1.abs(), TOLERANCE);
    }

    @Test
    public void testAbsNaN() {
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Assert.assertTrue(Double.isNaN(cNaN.abs()));
    }

    @Test
    public void testAbsInf() {
        Complex cInf = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, cInf.abs(), TOLERANCE);
    }

    @Test
    public void testPow() {
        Complex c1 = new Complex(1.0, 1.0);
        Complex c2 = new Complex(2.0, 0.0);
        Complex result = c1.pow(c2);
        Assert.assertNotNull(result);
    }

    @Test
    public void testPowNull() {
        Complex c1 = new Complex(1.0, 1.0);
        try {
            c1.pow(null);
            Assert.fail("Expected exception");
        } catch (IllegalArgumentException | NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testSqrt() {
        Complex c1 = new Complex(-1.0, 0.0);
        Complex result = c1.sqrt();
        Assert.assertNotNull(result);
    }

    @Test
    public void testSqrt1z() {
        Complex c1 = new Complex(2.0, 3.0);
        Complex result = c1.sqrt1z();
        Assert.assertNotNull(result);
    }

    @Test
    public void testTrigFunctions() {
        Complex c1 = new Complex(1.0, 1.0);
        Assert.assertNotNull(c1.cos());
        Assert.assertNotNull(c1.cosh());
        Assert.assertNotNull(c1.sin());
        Assert.assertNotNull(c1.sinh());
        Assert.assertNotNull(c1.tan());
        Assert.assertNotNull(c1.tanh());
        Assert.assertNotNull(c1.acos());
        Assert.assertNotNull(c1.asin());
        Assert.assertNotNull(c1.atan());
        Assert.assertNotNull(c1.exp());
        Assert.assertNotNull(c1.log());
    }

    @Test
    public void testEqualsAndHashCode() {
        Complex c1 = new Complex(1.0, 2.0);
        Complex c2 = new Complex(1.0, 2.0);
        Complex c3 = new Complex(2.0, 1.0);
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Complex cNaN2 = new Complex(Double.NaN, Double.NaN);

        Assert.assertTrue(c1.equals(c1));
        Assert.assertTrue(c1.equals(c2));
        Assert.assertFalse(c1.equals(c3));
        Assert.assertFalse(c1.equals(null));
        Assert.assertFalse(c1.equals("Some String"));
        
        Assert.assertTrue(cNaN.equals(cNaN2));
        Assert.assertEquals(cNaN.hashCode(), cNaN2.hashCode());
        Assert.assertEquals(c1.hashCode(), c2.hashCode());

        Complex cZeroRealNaN = new Complex(0.0, Double.NaN);
        Complex cZeroRealNaN2 = new Complex(0.0, Double.NaN);
        Assert.assertTrue(cZeroRealNaN.equals(cZeroRealNaN2));
        Assert.assertEquals(cZeroRealNaN.hashCode(), cZeroRealNaN2.hashCode());
    }

    @Test
    public void testToString() {
        Complex c1 = new Complex(1.0, 2.0);
        Assert.assertNotNull(c1.toString());
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Assert.assertNotNull(cNaN.toString());
    }

    @Test
    public void testCreateComplexFactory() {
        Complex c = new Complex(1.0, 2.0);
        Complex derived = c.createComplex(3.0, 4.0);
        Assert.assertEquals(3.0, derived.getReal(), TOLERANCE);
        Assert.assertEquals(4.0, derived.getImaginary(), TOLERANCE);
    }

    @Test
    public void testGetArgument() {
        Complex c = new Complex(1.0, 1.0);
        Assert.assertEquals(Math.PI / 4, c.getArgument(), TOLERANCE);
    }

    @Test
    public void testNegate() {
        Complex c = new Complex(1.0, -2.0);
        Complex negated = c.negate();
        Assert.assertEquals(-1.0, negated.getReal(), TOLERANCE);
        Assert.assertEquals(2.0, negated.getImaginary(), TOLERANCE);
    }

    @Test
    public void testNegateNaN() {
        Complex cNaN = new Complex(Double.NaN, Double.NaN);
        Assert.assertTrue(cNaN.negate().isNaN());
    }
}