package org.apache.commons.math.fraction;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

public class BigFractionTest {

    @Test
    public void testConstructorsAndGetters() {
        BigFraction bf1 = new BigFraction(10);
        Assert.assertEquals(BigInteger.valueOf(10), bf1.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(1), bf1.getDenominator());

        BigFraction bf2 = new BigFraction(2, 4);
        Assert.assertEquals(BigInteger.valueOf(1), bf2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), bf2.getDenominator());

        BigFraction bf3 = new BigFraction(2L);
        Assert.assertEquals(BigInteger.valueOf(2), bf3.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(1), bf3.getDenominator());

        BigFraction bf4 = new BigFraction(2L, 4L);
        Assert.assertEquals(BigInteger.valueOf(1), bf4.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), bf4.getDenominator());

        BigFraction bf5 = new BigFraction(BigInteger.valueOf(5));
        Assert.assertEquals(BigInteger.valueOf(5), bf5.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(1), bf5.getDenominator());

        BigFraction bf6 = new BigFraction(BigInteger.valueOf(6), BigInteger.valueOf(12));
        Assert.assertEquals(BigInteger.valueOf(1), bf6.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), bf6.getDenominator());

        BigFraction bf7 = new BigFraction(0.5);
        Assert.assertEquals(BigInteger.valueOf(1), bf7.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), bf7.getDenominator());

        BigFraction bf8 = new BigFraction(0.1, 1.0e-5, 100);
        Assert.assertNotNull(bf8);

        BigFraction bf9 = new BigFraction(0.1, 100);
        Assert.assertNotNull(bf9);
    }

    @Test(expected = ArithmeticException.class)
    public void testZeroDenominatorInt() {
        new BigFraction(1, 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testZeroDenominatorLong() {
        new BigFraction(1L, 0L);
    }

    @Test(expected = ArithmeticException.class)
    public void testZeroDenominatorBigInteger() {
        new BigFraction(BigInteger.ONE, BigInteger.ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullNumeratorBigInteger() {
        new BigFraction(null, BigInteger.ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullDenominatorBigInteger() {
        new BigFraction(BigInteger.ONE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleConstructorMaxIterations() {
        new BigFraction(Double.NaN, 1.0e-5, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDoubleConstructorInfinite() {
        new BigFraction(Double.POSITIVE_INFINITY, 100);
    }

    @Test
    public void testDoubleConstructorEpsilon() {
        BigFraction bf = new BigFraction(0.0000001, 1e-12, 10);
        Assert.assertNotNull(bf);
    }

    @Test
    public void testAbs() {
        BigFraction bf = new BigFraction(-1, 2);
        BigFraction abs = bf.abs();
        Assert.assertEquals(BigInteger.ONE, abs.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), abs.getDenominator());
    }

    @Test
    public void testAdd() {
        BigFraction bf1 = new BigFraction(1, 2);
        BigFraction bf2 = new BigFraction(1, 3);
        BigFraction sum = bf1.add(bf2);
        Assert.assertEquals(BigInteger.valueOf(5), sum.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(6), sum.getDenominator());

        BigFraction sumInt = bf1.add(1);
        Assert.assertEquals(BigInteger.valueOf(3), sumInt.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), sumInt.getDenominator());

        BigFraction sumLong = bf1.add(1L);
        Assert.assertEquals(BigInteger.valueOf(3), sumLong.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), sumLong.getDenominator());

        BigFraction sumBi = bf1.add(BigInteger.ONE);
        Assert.assertEquals(BigInteger.valueOf(3), sumBi.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), sumBi.getDenominator());
    }

    @Test
    public void testSubtract() {
        BigFraction bf1 = new BigFraction(1, 2);
        BigFraction bf2 = new BigFraction(1, 3);
        BigFraction diff = bf1.subtract(bf2);
        Assert.assertEquals(BigInteger.ONE, diff.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(6), diff.getDenominator());

        BigFraction diffInt = bf1.subtract(1);
        Assert.assertEquals(BigInteger.valueOf(-1), diffInt.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), diffInt.getDenominator());

        BigFraction diffLong = bf1.subtract(1L);
        Assert.assertEquals(BigInteger.valueOf(-1), diffLong.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), diffLong.getDenominator());

        BigFraction diffBi = bf1.subtract(BigInteger.ONE);
        Assert.assertEquals(BigInteger.valueOf(-1), diffBi.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), diffBi.getDenominator());
    }

    @Test
    public void testMultiply() {
        BigFraction bf1 = new BigFraction(1, 2);
        BigFraction bf2 = new BigFraction(2, 3);
        BigFraction prod = bf1.multiply(bf2);
        Assert.assertEquals(BigInteger.ONE, prod.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(3), prod.getDenominator());

        BigFraction prodInt = bf1.multiply(4);
        Assert.assertEquals(BigInteger.valueOf(2), prodInt.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(1), prodInt.getDenominator());

        BigFraction prodLong = bf1.multiply(4L);
        Assert.assertEquals(BigInteger.valueOf(2), prodLong.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(1), prodLong.getDenominator());

        BigFraction prodBi = bf1.multiply(BigInteger.valueOf(4));
        Assert.assertEquals(BigInteger.valueOf(2), prodBi.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(1), prodBi.getDenominator());
    }

    @Test
    public void testDivide() {
        BigFraction bf1 = new BigFraction(1, 2);
        BigFraction bf2 = new BigFraction(1, 4);
        BigFraction quot = bf1.divide(bf2);
        Assert.assertEquals(BigInteger.valueOf(2), quot.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(1), quot.getDenominator());

        BigFraction quotInt = bf1.divide(2);
        Assert.assertEquals(BigInteger.ONE, quotInt.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), quotInt.getDenominator());

        BigFraction quotLong = bf1.divide(2L);
        Assert.assertEquals(BigInteger.ONE, quotLong.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), quotLong.getDenominator());

        BigFraction quotBi = bf1.divide(BigInteger.valueOf(2));
        Assert.assertEquals(BigInteger.ONE, quotBi.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), quotBi.getDenominator());
    }

    @Test(expected = ArithmeticException.class)
    public void testDivideByZero() {
        BigFraction bf1 = new BigFraction(1, 2);
        bf1.divide(BigFraction.ZERO);
    }

    @Test
    public void testPow() {
        BigFraction bf = new BigFraction(2, 3);
        BigFraction pow2 = bf.pow(2);
        Assert.assertEquals(BigInteger.valueOf(4), pow2.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(9), pow2.getDenominator());

        BigFraction powNeg = bf.pow(-2);
        Assert.assertEquals(BigInteger.valueOf(9), powNeg.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(4), powNeg.getDenominator());

        BigFraction powLong = bf.pow(2L);
        Assert.assertEquals(BigInteger.valueOf(4), powLong.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(9), powLong.getDenominator());

        BigFraction powDouble = bf.pow(2.0);
        Assert.assertNotNull(powDouble);
    }

    @Test
    public void testReciprocal() {
        BigFraction bf = new BigFraction(2, 3);
        BigFraction rec = bf.reciprocal();
        Assert.assertEquals(BigInteger.valueOf(3), rec.getNumerator());
        Assert.assertEquals(BigInteger.valueOf(2), rec.getDenominator());
    }

    @Test
    public void testConversions() {
        BigFraction bf = new BigFraction(5, 2);
        Assert.assertEquals(2.5, bf.doubleValue(), 1e-15);
        Assert.assertEquals(2.5f, bf.floatValue(), 1e-15f);
        Assert.assertEquals(2L, bf.longValue());
        Assert.assertEquals(2, bf.intValue());
        Assert.assertEquals(BigDecimal.valueOf(2.5), bf.bigDecimalValue());
    }

    @Test
    public void testCompareTo() {
        BigFraction bf1 = new BigFraction(1, 2);
        BigFraction bf2 = new BigFraction(1, 3);
        BigFraction bf3 = new BigFraction(2, 4);

        Assert.assertTrue(bf1.compareTo(bf2) > 0);
        Assert.assertTrue(bf2.compareTo(bf1) < 0);
        Assert.assertEquals(0, bf1.compareTo(bf3));
    }

    @Test
    public void testEqualsAndHashCode() {
        BigFraction bf1 = new BigFraction(1, 2);
        BigFraction bf2 = new BigFraction(1, 2);
        BigFraction bf3 = new BigFraction(1, 3);
        Object notABigFraction = "Not a BigFraction";

        Assert.assertTrue(bf1.equals(bf1));
        Assert.assertTrue(bf1.equals(bf2));
        Assert.assertFalse(bf1.equals(bf3));
        Assert.assertFalse(bf1.equals(notABigFraction));
        Assert.assertFalse(bf1.equals(null));

        Assert.assertEquals(bf1.hashCode(), bf2.hashCode());
    }

    @Test
    public void testToString() {
        BigFraction bf = new BigFraction(1, 2);
        String str = bf.toString();
        Assert.assertNotNull(str);
        
        BigFraction bfInt = new BigFraction(3);
        Assert.assertEquals("3", bfInt.toString());
        
        BigFraction bfFrac = new BigFraction(3, 4);
        Assert.assertEquals("3 / 4", bfFrac.toString());
    }

    @Test
    public void testGetPercentage() {
        BigFraction bf = new BigFraction(1, 2);
        Assert.assertEquals(50.0, bf.percentageValue(), 1e-15);
    }
}