package org.apache.commons.math.util;

import org.junit.Test;
import org.junit.Assert;
import org.apache.commons.math.MathIllegalArgumentException;
import org.apache.commons.math.exception.MathArithmeticException;

/**
 * Test suite for MathUtils, targeting the binomial coefficient bug (Math-92).
 */
public class MathUtilsTest {

    // ---------- binomialCoefficient ----------

    @Test
    public void testBinomialCoefficientSmallValues() {
        // n choose k for small values
        Assert.assertEquals(1, MathUtils.binomialCoefficient(0, 0));
        Assert.assertEquals(1, MathUtils.binomialCoefficient(5, 0));
        Assert.assertEquals(5, MathUtils.binomialCoefficient(5, 1));
        Assert.assertEquals(10, MathUtils.binomialCoefficient(5, 2));
        Assert.assertEquals(10, MathUtils.binomialCoefficient(5, 3));
        Assert.assertEquals(5, MathUtils.binomialCoefficient(5, 4));
        Assert.assertEquals(1, MathUtils.binomialCoefficient(5, 5));
        Assert.assertEquals(6, MathUtils.binomialCoefficient(4, 2));
        Assert.assertEquals(35, MathUtils.binomialCoefficient(7, 3));
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientNegativeN() {
        MathUtils.binomialCoefficient(-1, 0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientNegativeK() {
        MathUtils.binomialCoefficient(5, -1);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientKGreaterThanN() {
        MathUtils.binomialCoefficient(5, 6);
    }

    @Test
    public void testBinomialCoefficientOverflow() {
        // This combination overflows int, should throw MathArithmeticException
        try {
            MathUtils.binomialCoefficient(67, 30);
            Assert.fail("Expected MathArithmeticException for overflow");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testBinomialCoefficientLargeButNotOverflow() {
        // n=30, k=15 is within int range (155117520)
        Assert.assertEquals(155117520, MathUtils.binomialCoefficient(30, 15));
    }

    @Test
    public void testBinomialCoefficientEdgeCases() {
        // n=1, k=0 and k=1
        Assert.assertEquals(1, MathUtils.binomialCoefficient(1, 0));
        Assert.assertEquals(1, MathUtils.binomialCoefficient(1, 1));
        // n=2, k=1
        Assert.assertEquals(2, MathUtils.binomialCoefficient(2, 1));
    }

    // ---------- binomialCoefficientDouble ----------

    @Test
    public void testBinomialCoefficientDoubleSmall() {
        Assert.assertEquals(1.0, MathUtils.binomialCoefficientDouble(0, 0), 1e-15);
        Assert.assertEquals(5.0, MathUtils.binomialCoefficientDouble(5, 1), 1e-15);
        Assert.assertEquals(10.0, MathUtils.binomialCoefficientDouble(5, 2), 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNegativeN() {
        MathUtils.binomialCoefficientDouble(-1, 0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientDoubleNegativeK() {
        MathUtils.binomialCoefficientDouble(5, -1);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientDoubleKGreaterThanN() {
        MathUtils.binomialCoefficientDouble(5, 6);
    }

    @Test
    public void testBinomialCoefficientDoubleLarge() {
        // Large values that would overflow int but double can represent approximately
        double result = MathUtils.binomialCoefficientDouble(67, 30);
        // Expected value approximately 1.419857e19
        Assert.assertEquals(1.419857e19, result, 1e14);
    }

    // ---------- binomialCoefficientLog ----------

    @Test
    public void testBinomialCoefficientLogSmall() {
        Assert.assertEquals(0.0, MathUtils.binomialCoefficientLog(0, 0), 1e-15);
        double log5 = Math.log(5);
        Assert.assertEquals(log5, MathUtils.binomialCoefficientLog(5, 1), 1e-15);
        double log10 = Math.log(10);
        Assert.assertEquals(log10, MathUtils.binomialCoefficientLog(5, 2), 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeN() {
        MathUtils.binomialCoefficientLog(-1, 0);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientLogNegativeK() {
        MathUtils.binomialCoefficientLog(5, -1);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testBinomialCoefficientLogKGreaterThanN() {
        MathUtils.binomialCoefficientLog(5, 6);
    }

    @Test
    public void testBinomialCoefficientLogLarge() {
        double logResult = MathUtils.binomialCoefficientLog(67, 30);
        double expectedLog = Math.log(1.419857e19);
        Assert.assertEquals(expectedLog, logResult, 1e-12);
    }

    // ---------- factorial ----------

    @Test
    public void testFactorialSmall() {
        Assert.assertEquals(1, MathUtils.factorial(0));
        Assert.assertEquals(1, MathUtils.factorial(1));
        Assert.assertEquals(2, MathUtils.factorial(2));
        Assert.assertEquals(6, MathUtils.factorial(3));
        Assert.assertEquals(24, MathUtils.factorial(4));
        Assert.assertEquals(120, MathUtils.factorial(5));
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testFactorialNegative() {
        MathUtils.factorial(-1);
    }

    @Test
    public void testFactorialOverflow() {
        // 13! = 6227020800 > Integer.MAX_VALUE, should throw MathArithmeticException
        try {
            MathUtils.factorial(13);
            Assert.fail("Expected MathArithmeticException for overflow");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testFactorialMaxInt() {
        // 12! = 479001600, within int range
        Assert.assertEquals(479001600, MathUtils.factorial(12));
    }

    // ---------- factorialDouble ----------

    @Test
    public void testFactorialDoubleSmall() {
        Assert.assertEquals(1.0, MathUtils.factorialDouble(0), 1e-15);
        Assert.assertEquals(1.0, MathUtils.factorialDouble(1), 1e-15);
        Assert.assertEquals(2.0, MathUtils.factorialDouble(2), 1e-15);
        Assert.assertEquals(6.0, MathUtils.factorialDouble(3), 1e-15);
        Assert.assertEquals(24.0, MathUtils.factorialDouble(4), 1e-15);
        Assert.assertEquals(120.0, MathUtils.factorialDouble(5), 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testFactorialDoubleNegative() {
        MathUtils.factorialDouble(-1);
    }

    @Test
    public void testFactorialDoubleLarge() {
        // 20! = 2.43290200817664e18
        Assert.assertEquals(2.43290200817664e18, MathUtils.factorialDouble(20), 1e11);
    }

    // ---------- factorialLog ----------

    @Test
    public void testFactorialLogSmall() {
        Assert.assertEquals(0.0, MathUtils.factorialLog(0), 1e-15);
        Assert.assertEquals(0.0, MathUtils.factorialLog(1), 1e-15);
        Assert.assertEquals(Math.log(2), MathUtils.factorialLog(2), 1e-15);
        Assert.assertEquals(Math.log(6), MathUtils.factorialLog(3), 1e-15);
    }

    @Test(expected = MathIllegalArgumentException.class)
    public void testFactorialLogNegative() {
        MathUtils.factorialLog(-1);
    }

    @Test
    public void testFactorialLogLarge() {
        double log20 = Math.log(2.43290200817664e18);
        Assert.assertEquals(log20, MathUtils.factorialLog(20), 1e-12);
    }

    // ---------- gcd ----------

    @Test
    public void testGcd() {
        Assert.assertEquals(0, MathUtils.gcd(0, 0));
        Assert.assertEquals(5, MathUtils.gcd(5, 0));
        Assert.assertEquals(5, MathUtils.gcd(0, 5));
        Assert.assertEquals(1, MathUtils.gcd(1, 1));
        Assert.assertEquals(6, MathUtils.gcd(12, 18));
        Assert.assertEquals(1, MathUtils.gcd(17, 13));
        Assert.assertEquals(12, MathUtils.gcd(24, 36));
        // Negative values
        Assert.assertEquals(6, MathUtils.gcd(-12, 18));
        Assert.assertEquals(6, MathUtils.gcd(12, -18));
        Assert.assertEquals(6, MathUtils.gcd(-12, -18));
    }

    @Test
    public void testGcdLarge() {
        // Large values that might cause overflow in subtraction-based algorithm
        Assert.assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
        Assert.assertEquals(1, MathUtils.gcd(Integer.MAX_VALUE, 1));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.gcd(Integer.MAX_VALUE, 0));
    }

    // ---------- lcm ----------

    @Test
    public void testLcm() {
        Assert.assertEquals(0, MathUtils.lcm(0, 5));
        Assert.assertEquals(0, MathUtils.lcm(5, 0));
        Assert.assertEquals(36, MathUtils.lcm(12, 18));
        Assert.assertEquals(221, MathUtils.lcm(17, 13));
        Assert.assertEquals(72, MathUtils.lcm(24, 36));
    }

    @Test(expected = MathArithmeticException.class)
    public void testLcmOverflow() {
        // lcm of large numbers may overflow
        MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE - 1);
    }

    // ---------- addAndCheck ----------

    @Test
    public void testAddAndCheck() {
        Assert.assertEquals(5, MathUtils.addAndCheck(2, 3));
        Assert.assertEquals(-5, MathUtils.addAndCheck(-2, -3));
        Assert.assertEquals(0, MathUtils.addAndCheck(Integer.MAX_VALUE, -Integer.MAX_VALUE));
    }

    @Test(expected = MathArithmeticException.class)
    public void testAddAndCheckOverflowPositive() {
        MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testAddAndCheckOverflowNegative() {
        MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
    }

    // ---------- subAndCheck ----------

    @Test
    public void testSubAndCheck() {
        Assert.assertEquals(2, MathUtils.subAndCheck(5, 3));
        Assert.assertEquals(-2, MathUtils.subAndCheck(3, 5));
        Assert.assertEquals(0, MathUtils.subAndCheck(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test(expected = MathArithmeticException.class)
    public void testSubAndCheckOverflowPositive() {
        MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
    }

    @Test(expected = MathArithmeticException.class)
    public void testSubAndCheckOverflowNegative() {
        MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
    }

    // ---------- mulAndCheck ----------

    @Test
    public void testMulAndCheck() {
        Assert.assertEquals(6, MathUtils.mulAndCheck(2, 3));
        Assert.assertEquals(-6, MathUtils.mulAndCheck(-2, 3));
        Assert.assertEquals(0, MathUtils.mulAndCheck(0, 100));
    }

    @Test(expected = MathArithmeticException.class)
    public void testMulAndCheckOverflowPositive() {
        MathUtils.mulAndCheck(Integer.MAX_VALUE, 2);
    }

    @Test(expected = MathArithmeticException.class)
    public void testMulAndCheckOverflowNegative() {
        MathUtils.mulAndCheck(Integer.MIN_VALUE, 2);
    }

    // ---------- sign ----------

    @Test
    public void testSign() {
        Assert.assertEquals(1, MathUtils.sign(10));
        Assert.assertEquals(-1, MathUtils.sign(-10));
        Assert.assertEquals(0, MathUtils.sign(0));
    }

    // ---------- round ----------

    @Test
    public void testRoundDouble() {
        Assert.assertEquals(3.0, MathUtils.round(3.14, 0), 1e-15);
        Assert.assertEquals(3.1, MathUtils.round(3.14, 1), 1e-15);
        Assert.assertEquals(3.14, MathUtils.round(3.14159, 2), 1e-15);
        Assert.assertEquals(3.142, MathUtils.round(3.14159, 3), 1e-15);
    }

    @Test
    public void testRoundFloat() {
        Assert.assertEquals(3.0f, MathUtils.round(3.14f, 0), 1e-15f);
        Assert.assertEquals(3.1f, MathUtils.round(3.14f, 1), 1e-15f);
    }

    // ---------- normalizeAngle ----------

    @Test
    public void testNormalizeAngle() {
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(0.0, Math.PI), 1e-15);
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(2 * Math.PI, Math.PI), 1e-15);
        Assert.assertEquals(-Math.PI, MathUtils.normalizeAngle(Math.PI, 0.0), 1e-15);
        Assert.assertEquals(0.0, MathUtils.normalizeAngle(-2 * Math.PI, Math.PI), 1e-15);
    }

    // ---------- distance ----------

    @Test
    public void testDistance1() {
        Assert.assertEquals(5.0, MathUtils.distance1(new double[]{0, 0}, new double[]{3, 4}), 1e-15);
        Assert.assertEquals(0.0, MathUtils.distance1(new double[]{1, 2}, new double[]{1, 2}), 1e-15);
    }

    @Test
    public void testDistanceInf() {
        Assert.assertEquals(4.0, MathUtils.distanceInf(new double[]{0, 0}, new double[]{3, 4}), 1e-15);
        Assert.assertEquals(0.0, MathUtils.distanceInf(new double[]{1, 2}, new double[]{1, 2}), 1e-15);
    }

    @Test
    public void testDistance() {
        Assert.assertEquals(5.0, MathUtils.distance(new double[]{0, 0}, new double[]{3, 4}), 1e-15);
        Assert.assertEquals(0.0, MathUtils.distance(new double[]{1, 2}, new double[]{1, 2}), 1e-15);
    }

    // ---------- equals with epsilon ----------

    @Test
    public void testEqualsWithEpsilon() {
        Assert.assertTrue(MathUtils.equals(1.0, 1.0, 1e-15));
        Assert.assertTrue(MathUtils.equals(1.0, 1.0000000001, 1e-9));
        Assert.assertFalse(MathUtils.equals(1.0, 1.1, 1e-2));
    }

    // ---------- hash ----------

    @Test
    public void testHash() {
        // Just ensure no exception and some basic property
        int h1 = MathUtils.hash(1.0);
        int h2 = MathUtils.hash(1.0);
        Assert.assertEquals(h1, h2);
        Assert.assertNotEquals(MathUtils.hash(1.0), MathUtils.hash(2.0));
    }

    @Test
    public void testHashDoubleArray() {
        double[] arr = {1.0, 2.0};
        int h1 = MathUtils.hash(arr);
        int h2 = MathUtils.hash(arr);
        Assert.assertEquals(h1, h2);
    }

    // ---------- indicator ----------

    @Test
    public void testIndicatorByte() {
        Assert.assertEquals((byte)1, MathUtils.indicator((byte)5));
        Assert.assertEquals((byte)-1, MathUtils.indicator((byte)-5));
        Assert.assertEquals((byte)0, MathUtils.indicator((byte)0));
    }

    @Test
    public void testIndicatorDouble() {
        Assert.assertEquals(1.0, MathUtils.indicator(5.0), 1e-15);
        Assert.assertEquals(-1.0, MathUtils.indicator(-5.0), 1e-15);
        Assert.assertEquals(0.0, MathUtils.indicator(0.0), 1e-15);
    }

    // ---------- clamp ----------

    @Test
    public void testClamp() {
        Assert.assertEquals(5, MathUtils.clamp(10, 0, 5));
        Assert.assertEquals(0, MathUtils.clamp(-1, 0, 5));
        Assert.assertEquals(3, MathUtils.clamp(3, 0, 5));
    }

    // ---------- checkBinomial (internal) ----------

    // Additional tests for binomialCoefficient to ensure bug detection
    @Test
    public void testBinomialCoefficientBugTrigger() {
        // Known bug: binomialCoefficient(67, 30) should throw exception but might return wrong value
        // This test ensures the exception is thrown
        try {
            MathUtils.binomialCoefficient(67, 30);
            Assert.fail("Overflow should be detected");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testBinomialCoefficientLargeValid() {
        // n=66, k=33 is within int range? 66 choose 33 = 7219428434016265740? That's > Integer.MAX_VALUE.
        // Actually 66 choose 33 is about 7.2e18, overflows int. So should throw.
        try {
            MathUtils.binomialCoefficient(66, 33);
            Assert.fail("Overflow should be detected");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testBinomialCoefficientMaxIntResult() {
        // Find n,k such that result is exactly Integer.MAX_VALUE? Not possible, but test near boundary.
        // 34 choose 17 = 2333606220 > Integer.MAX_VALUE, so overflow.
        try {
            MathUtils.binomialCoefficient(34, 17);
            Assert.fail("Overflow should be detected");
        } catch (MathArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testBinomialCoefficientSymmetry() {
        // n choose k == n choose n-k
        Assert.assertEquals(MathUtils.binomialCoefficient(10, 3), MathUtils.binomialCoefficient(10, 7));
        Assert.assertEquals(MathUtils.binomialCoefficient(20, 8), MathUtils.binomialCoefficient(20, 12));
    }
}