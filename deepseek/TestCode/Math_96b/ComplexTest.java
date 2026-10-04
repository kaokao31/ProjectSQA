package org.apache.commons.math.complex;

import static org.junit.Assert.*;
import org.junit.Test;

public class ComplexTest {

    // ---------- equals() and hashCode() ----------
    @Test
    public void testEqualsNaN() {
        Complex nan = new Complex(Double.NaN, Double.NaN);
        Complex nan2 = new Complex(Double.NaN, Double.NaN);
        assertTrue("NaN Complex should be equal to itself", nan.equals(nan));
        assertTrue("Two NaN Complex objects should be equal", nan.equals(nan2));
        assertEquals("Hash codes for NaN Complex should be equal", nan.hashCode(), nan2.hashCode());
    }

    @Test
    public void testEqualsWithZeroAndNegativeZero() {
        Complex zero = new Complex(0.0, 0.0);
        Complex negZero = new Complex(-0.0, -0.0);
        // Double.compare(0.0, -0.0) returns -1, so they are not equal
        assertFalse("0.0 and -0.0 should not be equal", zero.equals(negZero));
        assertFalse("-0.0 and 0.0 should not be equal", negZero.equals(zero));
    }

    @Test
    public void testEqualsWithInfinity() {
        Complex inf = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Complex inf2 = new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertTrue("Infinity Complex should be equal to itself", inf.equals(inf));
        assertTrue("Two Infinity Complex objects should be equal", inf.equals(inf2));
        assertEquals("Hash codes for Infinity Complex should be equal", inf.hashCode(), inf2.hashCode());
    }

    @Test
    public void testEqualsNull() {
        Complex c = new Complex(1.0, 2.0);
        assertFalse("Complex should not be equal to null", c.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Complex c = new Complex(1.0, 2.0);
        assertFalse("Complex should not be equal to a different class", c.equals("string"));
    }

    @Test
    public void testEqualsSymmetry() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        assertTrue("a.equals(b) should be true", a.equals(b));
        assertTrue("b.equals(a) should be true", b.equals(a));
    }

    @Test
    public void testEqualsTransitivity() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(1.0, 2.0);
        Complex c = new Complex(1.0, 2.0);
        assertTrue(a.equals(b));
        assertTrue(b.equals(c));
        assertTrue(a.equals(c));
    }

    @Test
    public void testHashCodeConsistency() {
        Complex c = new Complex(3.0, 4.0);
        int hash1 = c.hashCode();
        int hash2 = c.hashCode();
        assertEquals("Hash code must be consistent", hash1, hash2);
    }

    @Test
    public void testHashCodeForEqualObjects() {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(5.0, 6.0);
        assertEquals("Equal objects must have equal hash codes", a.hashCode(), b.hashCode());
    }

    // ---------- isNaN() and isInfinite() ----------
    @Test
    public void testIsNaN() {
        Complex nan = new Complex(Double.NaN, 0.0);
        assertTrue("Complex with NaN real part should be NaN", nan.isNaN());
        nan = new Complex(0.0, Double.NaN);
        assertTrue("Complex with NaN imaginary part should be NaN", nan.isNaN());
        nan = new Complex(Double.NaN, Double.NaN);
        assertTrue("Complex with both NaN parts should be NaN", nan.isNaN());
        Complex notNaN = new Complex(1.0, 2.0);
        assertFalse("Normal Complex should not be NaN", notNaN.isNaN());
    }

    @Test
    public void testIsInfinite() {
        Complex inf = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertTrue("Complex with infinite real part should be infinite", inf.isInfinite());
        inf = new Complex(0.0, Double.NEGATIVE_INFINITY);
        assertTrue("Complex with infinite imaginary part should be infinite", inf.isInfinite());
        inf = new Complex(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        assertTrue("Complex with both infinite parts should be infinite", inf.isInfinite());
        Complex notInf = new Complex(1.0, 2.0);
        assertFalse("Normal Complex should not be infinite", notInf.isInfinite());
    }

    // ---------- add() ----------
    @Test
    public void testAdd() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.add(b);
        assertEquals("Real part of sum", 4.0, result.getReal(), 0.0);
        assertEquals("Imaginary part of sum", 6.0, result.getImaginary(), 0.0);
    }

    @Test
    public void testAddWithNaN() {
        Complex a = new Complex(1.0, 2.0);
        Complex nan = new Complex(Double.NaN, 0.0);
        Complex result = a.add(nan);
        assertTrue("Sum with NaN should be NaN", result.isNaN());
    }

    @Test
    public void testAddWithInfinity() {
        Complex a = new Complex(1.0, 2.0);
        Complex inf = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex result = a.add(inf);
        assertTrue("Sum with infinity should be infinite", result.isInfinite());
    }

    // ---------- subtract() ----------
    @Test
    public void testSubtract() {
        Complex a = new Complex(5.0, 6.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.subtract(b);
        assertEquals("Real part of difference", 2.0, result.getReal(), 0.0);
        assertEquals("Imaginary part of difference", 2.0, result.getImaginary(), 0.0);
    }

    @Test
    public void testSubtractWithNaN() {
        Complex a = new Complex(1.0, 2.0);
        Complex nan = new Complex(Double.NaN, 0.0);
        Complex result = a.subtract(nan);
        assertTrue("Difference with NaN should be NaN", result.isNaN());
    }

    // ---------- multiply() ----------
    @Test
    public void testMultiply() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.multiply(b);
        // (1+2i)*(3+4i) = 3 + 4i + 6i + 8i^2 = 3 + 10i - 8 = -5 + 10i
        assertEquals("Real part of product", -5.0, result.getReal(), 0.0);
        assertEquals("Imaginary part of product", 10.0, result.getImaginary(), 0.0);
    }

    @Test
    public void testMultiplyByZero() {
        Complex a = new Complex(1.0, 2.0);
        Complex zero = new Complex(0.0, 0.0);
        Complex result = a.multiply(zero);
        assertEquals("Product with zero should be zero", 0.0, result.getReal(), 0.0);
        assertEquals("Product with zero should be zero", 0.0, result.getImaginary(), 0.0);
    }

    @Test
    public void testMultiplyByNaN() {
        Complex a = new Complex(1.0, 2.0);
        Complex nan = new Complex(Double.NaN, 0.0);
        Complex result = a.multiply(nan);
        assertTrue("Product with NaN should be NaN", result.isNaN());
    }

    // ---------- divide() ----------
    @Test
    public void testDivide() {
        Complex a = new Complex(1.0, 2.0);
        Complex b = new Complex(3.0, 4.0);
        Complex result = a.divide(b);
        // (1+2i)/(3+4i) = (1+2i)*(3-4i)/(9+16) = (3 -4i +6i -8i^2)/25 = (3+2i+8)/25 = (11+2i)/25 = 0.44 + 0.08i
        assertEquals("Real part of quotient", 0.44, result.getReal(), 1e-10);
        assertEquals("Imaginary part of quotient", 0.08, result.getImaginary(), 1e-10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDivideByZero() {
        Complex a = new Complex(1.0, 2.0);
        Complex zero = new Complex(0.0, 0.0);
        a.divide(zero);
    }

    @Test
    public void testDivideByNaN() {
        Complex a = new Complex(1.0, 2.0);
        Complex nan = new Complex(Double.NaN, 0.0);
        Complex result = a.divide(nan);
        assertTrue("Quotient with NaN should be NaN", result.isNaN());
    }

    // ---------- abs() ----------
    @Test
    public void testAbs() {
        Complex c = new Complex(3.0, 4.0);
        assertEquals("Absolute value of 3+4i should be 5", 5.0, c.abs(), 0.0);
    }

    @Test
    public void testAbsNaN() {
        Complex c = new Complex(Double.NaN, 0.0);
        assertTrue("Absolute value of NaN should be NaN", Double.isNaN(c.abs()));
    }

    @Test
    public void testAbsInfinity() {
        Complex c = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertEquals("Absolute value of infinity should be infinity", Double.POSITIVE_INFINITY, c.abs(), 0.0);
    }

    // ---------- angle() ----------
    @Test
    public void testAngle() {
        Complex c = new Complex(1.0, 0.0);
        assertEquals("Angle of 1+0i should be 0", 0.0, c.angle(), 0.0);
        c = new Complex(0.0, 1.0);
        assertEquals("Angle of 0+1i should be pi/2", Math.PI / 2, c.angle(), 0.0);
        c = new Complex(-1.0, 0.0);
        assertEquals("Angle of -1+0i should be pi", Math.PI, c.angle(), 0.0);
        c = new Complex(0.0, -1.0);
        assertEquals("Angle of 0-1i should be -pi/2", -Math.PI / 2, c.angle(), 0.0);
    }

    @Test
    public void testAngleNaN() {
        Complex c = new Complex(Double.NaN, 0.0);
        assertTrue("Angle of NaN should be NaN", Double.isNaN(c.angle()));
    }

    // ---------- conjugate() ----------
    @Test
    public void testConjugate() {
        Complex c = new Complex(1.0, 2.0);
        Complex conj = c.conjugate();
        assertEquals("Real part of conjugate", 1.0, conj.getReal(), 0.0);
        assertEquals("Imaginary part of conjugate", -2.0, conj.getImaginary(), 0.0);
    }

    @Test
    public void testConjugateNaN() {
        Complex c = new Complex(Double.NaN, 0.0);
        Complex conj = c.conjugate();
        assertTrue("Conjugate of NaN should be NaN", conj.isNaN());
    }

    // ---------- reciprocal() ----------
    @Test
    public void testReciprocal() {
        Complex c = new Complex(2.0, 0.0);
        Complex rec = c.reciprocal();
        assertEquals("Reciprocal of 2 should be 0.5", 0.5, rec.getReal(), 0.0);
        assertEquals("Imaginary part should be 0", 0.0, rec.getImaginary(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReciprocalZero() {
        Complex zero = new Complex(0.0, 0.0);
        zero.reciprocal();
    }

    @Test
    public void testReciprocalNaN() {
        Complex c = new Complex(Double.NaN, 0.0);
        Complex rec = c.reciprocal();
        assertTrue("Reciprocal of NaN should be NaN", rec.isNaN());
    }

    // ---------- toString() ----------
    @Test
    public void testToString() {
        Complex c = new Complex(1.0, 2.0);
        String str = c.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain real part", str.contains("1.0"));
        assertTrue("toString should contain imaginary part", str.contains("2.0"));
    }

    // ---------- getReal() and getImaginary() ----------
    @Test
    public void testGetters() {
        Complex c = new Complex(3.0, -4.0);
        assertEquals("getReal", 3.0, c.getReal(), 0.0);
        assertEquals("getImaginary", -4.0, c.getImaginary(), 0.0);
    }

    // ---------- Edge cases for equals with mixed NaN and infinity ----------
    @Test
    public void testEqualsMixedNaN() {
        Complex nanReal = new Complex(Double.NaN, 0.0);
        Complex nanImag = new Complex(0.0, Double.NaN);
        assertFalse("NaN real vs NaN imag should not be equal", nanReal.equals(nanImag));
    }

    @Test
    public void testEqualsInfinityAndNaN() {
        Complex inf = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex nan = new Complex(Double.NaN, 0.0);
        assertFalse("Infinity and NaN should not be equal", inf.equals(nan));
    }

    // ---------- Additional hashCode tests ----------
    @Test
    public void testHashCodeForNaN() {
        Complex nan1 = new Complex(Double.NaN, 1.0);
        Complex nan2 = new Complex(Double.NaN, 1.0);
        assertEquals("Hash codes for NaN with same imaginary part", nan1.hashCode(), nan2.hashCode());
    }

    @Test
    public void testHashCodeForInfinity() {
        Complex inf1 = new Complex(Double.POSITIVE_INFINITY, 0.0);
        Complex inf2 = new Complex(Double.POSITIVE_INFINITY, 0.0);
        assertEquals("Hash codes for infinity", inf1.hashCode(), inf2.hashCode());
    }
}