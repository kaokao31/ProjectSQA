package org.apache.commons.math3.dfp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for Dfp class targeting maximum coverage
 * and potential fault detection (Defects4J Math-17).
 */
public class DfpTest {

    private DfpField field;
    private Dfp zero;
    private Dfp one;
    private Dfp two;
    private Dfp ten;
    private Dfp nan;
    private Dfp inf;
    private Dfp minusOne;

    @Before
    public void setUp() {
        // Use a standard field with 20 digits of precision
        field = new DfpField(20);
        zero = field.newDfp(0.0);
        one = field.newDfp(1.0);
        two = field.newDfp(2.0);
        ten = field.newDfp(10.0);
        nan = field.newDfp("NaN");
        inf = field.newDfp("Infinity");
        minusOne = field.newDfp(-1.0);
    }

    // ==================== Constructor Tests ====================
    @Test
    public void testConstructors() {
        Dfp fromDouble = field.newDfp(3.141592653589793);
        Assert.assertNotNull("Constructor from double should not be null", fromDouble);
        Assert.assertEquals("3.141592653589793", fromDouble.toString().substring(0, 17));

        Dfp fromString = field.newDfp("123.456e-2");
        Assert.assertEquals("1.23456", fromString.toString());

        Dfp fromLong = field.newDfp(123456789L);
        Assert.assertEquals("123456789", fromLong.toString());

        Dfp fromBytes = field.newDfp((byte) 42);
        Assert.assertEquals("42", fromBytes.toString());

        Dfp fromInt = field.newDfp(42);
        Assert.assertEquals("42", fromInt.toString());

        // Copy constructor
        Dfp copy = new Dfp(field, one);
        Assert.assertEquals("Copy constructor should equal original", one, copy);
        Assert.assertNotSame("Copy should be a new object", one, copy);

        // Constructor with sign and bytes
        byte[] bytes = {1, 2, 3, 4};
        Dfp fromSignBytes = new Dfp(field, true, bytes);
        Assert.assertNotNull(fromSignBytes);
    }

    // ==================== Arithmetic Tests ====================
    @Test
    public void testAdd() {
        Dfp result = one.add(two);
        Assert.assertEquals("1 + 2 = 3", field.newDfp(3), result);

        // Addition with NaN
        Assert.assertTrue("NaN + anything = NaN", one.add(nan).isNaN());
        Assert.assertTrue("NaN + NaN = NaN", nan.add(nan).isNaN());

        // Addition with infinity
        Assert.assertTrue("inf + inf = inf", inf.add(inf).isInfinite());
        Assert.assertTrue("inf + 1 = inf", inf.add(one).isInfinite());
        Assert.assertTrue("(-inf) + (-inf) = -inf", inf.negate().add(inf.negate()).isInfinite());

        // Addition of zero
        Assert.assertEquals("zero + one = one", one, zero.add(one));
        Assert.assertEquals("one + zero = one", one, one.add(zero));

        // Addition results in overflow to infinity
        Dfp large = field.newDfp("1e100");
        Dfp larger = field.newDfp("1e100");
        // This depends on precision; but we can test massive values
        // Note: field with 20 digits, max exponent is around 1e100? Actually Dfp can handle.
        // For safety, use exponent near limit.
        Dfp huge = field.newDfp("1e1000000"); // large exponent
        Dfp bigResult = huge.add(huge);
        Assert.assertTrue("Overflow should produce infinity", bigResult.isInfinite());

        // Addition underflow to zero? Not applicable here.
    }

    @Test
    public void testSubtract() {
        Dfp result = two.subtract(one);
        Assert.assertEquals("2 - 1 = 1", one, result);

        // Subtraction with negative
        Dfp neg = zero.subtract(one);
        Assert.assertEquals("0 - 1 = -1", minusOne, neg);

        // Subtraction of equal numbers
        Assert.assertEquals("1 - 1 = 0", zero, one.subtract(one));

        // Subtraction with NaN
        Assert.assertTrue("x - NaN = NaN", one.subtract(nan).isNaN());
        Assert.assertTrue("NaN - x = NaN", nan.subtract(one).isNaN());

        // Subtraction with infinity
        Assert.assertTrue("inf - inf = NaN", inf.subtract(inf).isNaN());
        Assert.assertTrue("inf - 1 = inf", inf.subtract(one).isInfinite());
    }

    @Test
    public void testMultiply() {
        Dfp result = two.multiply(ten);
        Assert.assertEquals("2 * 10 = 20", field.newDfp(20), result);

        // Multiplication by zero
        Assert.assertEquals("zero * anything = zero", zero, zero.multiply(one));
        Assert.assertEquals("anything * zero = zero", zero, one.multiply(zero));

        // Multiplication by one
        Assert.assertEquals("x * 1 = x", one, one.multiply(one));
        Assert.assertEquals("x * 1 = x", ten, ten.multiply(one));

        // Multiplication by NaN
        Assert.assertTrue("x * NaN = NaN", one.multiply(nan).isNaN());
        Assert.assertTrue("NaN * x = NaN", nan.multiply(one).isNaN());

        // Multiplication with infinity
        Assert.assertTrue("inf * inf = inf", inf.multiply(inf).isInfinite());
        Assert.assertTrue("inf * 0 = NaN", inf.multiply(zero).isNaN());
        Assert.assertTrue("inf * 2 = inf", inf.multiply(two).isInfinite());

        // Overflow to infinity
        Dfp huge = field.newDfp("1e1000000");
        Assert.assertTrue("Overflow multiplication", huge.multiply(huge).isInfinite());
    }

    @Test
    public void testDivide() {
        Dfp result = ten.divide(two);
        Assert.assertEquals("10 / 2 = 5", field.newDfp(5), result);

        // Division by one
        Assert.assertEquals("x / 1 = x", ten, ten.divide(one));

        // Division by zero
        Assert.assertTrue("x / 0 = +/-inf or NaN", ten.divide(zero).isInfinite());
        Assert.assertTrue("0/0 = NaN", zero.divide(zero).isNaN());

        // Division by NaN
        Assert.assertTrue("x / NaN = NaN", one.divide(nan).isNaN());
        Assert.assertTrue("NaN / x = NaN", nan.divide(one).isNaN());

        // Division of infinity
        Assert.assertTrue("inf / inf = NaN", inf.divide(inf).isNaN());
        Assert.assertTrue("inf / 1 = inf", inf.divide(one).isInfinite());
        Assert.assertTrue("1 / inf = 0", one.divide(inf).equals(zero));

        // Underflow to zero
        Dfp small = field.newDfp("1e-1000");
        Dfp large = field.newDfp("1e1000");
        Dfp underflowResult = small.divide(large);
        Assert.assertTrue("Underflow should yield zero (or subnormal)", underflowResult.equals(zero) || underflowResult.isZero());
    }

    // ==================== Rounding and Special Methods ====================
    @Test
    public void testFloor() {
        Dfp value = field.newDfp("3.7");
        Dfp floor = value.floor();
        Assert.assertEquals("floor(3.7) = 3", field.newDfp(3), floor);

        value = field.newDfp("-3.7");
        floor = value.floor();
        Assert.assertEquals("floor(-3.7) = -4", field.newDfp(-4), floor);

        // Exact integer
        value = field.newDfp("5.0");
        floor = value.floor();
        Assert.assertEquals("floor(5.0) = 5", field.newDfp(5), floor);
    }

    @Test
    public void testCeil() {
        Dfp value = field.newDfp("3.2");
        Dfp ceil = value.ceil();
        Assert.assertEquals("ceil(3.2) = 4", field.newDfp(4), ceil);

        value = field.newDfp("-3.2");
        ceil = value.ceil();
        Assert.assertEquals("ceil(-3.2) = -3", field.newDfp(-3), ceil);
    }

    @Test
    public void testRint() {
        Dfp value = field.newDfp("3.5");
        Dfp rint = value.rint();
        // Round to nearest even: 3.5 -> 4 (nearest even)
        Assert.assertEquals("rint(3.5) = 4", field.newDfp(4), rint);

        value = field.newDfp("2.5");
        rint = value.rint();
        Assert.assertEquals("rint(2.5) = 2", field.newDfp(2), rint);

        value = field.newDfp("3.2");
        rint = value.rint();
        Assert.assertEquals("rint(3.2) = 3", field.newDfp(3), rint);
    }

    @Test
    public void testAbs() {
        Dfp neg = field.newDfp("-123.456");
        Dfp abs = neg.abs();
        Assert.assertEquals("abs(-123.456) = 123.456", field.newDfp("123.456"), abs);

        Assert.assertEquals("abs(0) = 0", zero, zero.abs());
        Assert.assertEquals("abs(inf) = inf", inf, inf.abs());
        Assert.assertTrue("abs(NaN) = NaN", nan.abs().isNaN());
    }

    @Test
    public void testNegate() {
        Dfp value = field.newDfp("42.0");
        Dfp neg = value.negate();
        Assert.assertEquals("negate(42) = -42", field.newDfp(-42), neg);

        Assert.assertEquals("negate(-42) = 42", neg.negate(), value);
        Assert.assertTrue("negate(NaN) = NaN", nan.negate().isNaN());
        Assert.assertTrue("negate(inf) = -inf", inf.negate().isInfinite() && inf.negate().lessThan(zero));
    }

    // ==================== Comparison Methods ====================
    @Test
    public void testCompareTo() {
        Assert.assertTrue("1 < 2", one.compareTo(two) < 0);
        Assert.assertTrue("2 > 1", two.compareTo(one) > 0);
        Assert.assertTrue("1 == 1", one.compareTo(one) == 0);
        Assert.assertTrue("-1 < 0", minusOne.compareTo(zero) < 0);
        Assert.assertTrue("0.0 compared to -0.0 should be >0", zero.compareTo(zero.negate()) > 0);
        // NaN comparisons
        Assert.assertTrue("NaN compareTo anything should indicate ordering", nan.compareTo(one) > 0);
        Assert.assertTrue("anything compareTo NaN should be < 0", one.compareTo(nan) < 0);
        Assert.assertTrue("NaN equals NaN", nan.compareTo(nan) == 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        Dfp a = field.newDfp("123.456");
        Dfp b = field.newDfp("123.456");
        Assert.assertEquals("Equal Dfp objects should be equal", a, b);
        Assert.assertEquals("Equal Dfp objects should have same hashCode", a.hashCode(), b.hashCode());

        // Different values
        Dfp c = field.newDfp("123.457");
        Assert.assertNotEquals("Different values not equal", a, c);

        // NaN should be equal to itself (per IEEE 754? but we'll follow Dfp contract)
        Assert.assertEquals("NaN equals NaN", nan, nan);

        // +0 and -0 are considered equal in Java Double but might be different?
        // In Dfp, +0 and -0 might be equal? We'll test.
        // Actually Dfp equals compares as numeric equality, so +0 == -0.
        Dfp positiveZero = field.newDfp(0.0);
        Dfp negativeZero = field.newDfp("-0.0");
        Assert.assertEquals("+0 == -0", positiveZero, negativeZero);
        // But compareTo distinguishes them. So equals and compareTo are consistent.
    }

    // ==================== Conversion Methods ====================
    @Test
    public void testToDouble() {
        Assert.assertEquals("1.0 as double", 1.0, one.toDouble(), 1e-15);
        Assert.assertEquals("0.0 as double", 0.0, zero.toDouble(), 1e-15);
        Assert.assertEquals("NaN as double", Double.NaN, nan.toDouble(), 0.0);
        Assert.assertEquals("Infinity as double", Double.POSITIVE_INFINITY, inf.toDouble(), 0.0);
        Assert.assertEquals("negative infinity", Double.NEGATIVE_INFINITY, inf.negate().toDouble(), 0.0);
    }

    @Test
    public void testToInt() {
        Assert.assertEquals("1 to int", 1, one.toInt());
        Assert.assertEquals("0 to int", 0, zero.toInt());
        Assert.assertEquals("10 to int", 10, ten.toInt());
    }

    @Test
    public void testToString() {
        Dfp value = field.newDfp("123.456e2");
        Assert.assertEquals("12345.6", value.toString());

        value = field.newDfp("NaN");
        Assert.assertEquals("NaN", value.toString());

        value = field.newDfp("Infinity");
        Assert.assertEquals("Infinity", value.toString());

        value = field.newDfp("-Infinity");
        Assert.assertEquals("-Infinity", value.toString());
    }

    // ==================== Special Cases: NaN, Infinity, Zero ====================
    @Test
    public void testIsNaN() {
        Assert.assertTrue("nan is NaN", nan.isNaN());
        Assert.assertFalse("one is not NaN", one.isNaN());
        Assert.assertFalse("inf is not NaN", inf.isNaN());
    }

    @Test
    public void testIsInfinite() {
        Assert.assertTrue("inf is infinite", inf.isInfinite());
        Assert.assertFalse("one is not infinite", one.isInfinite());
        Assert.assertFalse("nan is not infinite", nan.isInfinite());
        Assert.assertTrue("neg inf is infinite", inf.negate().isInfinite());
    }

    @Test
    public void testIsZero() {
        Assert.assertTrue("zero is zero", zero.isZero());
        Assert.assertFalse("one is not zero", one.isZero());
        Assert.assertFalse("nan is not zero", nan.isZero());
        // Negative zero is zero
        Dfp negZero = field.newDfp("-0.0");
        Assert.assertTrue("negative zero is zero", negZero.isZero());
    }

    // ==================== Square Root Test (if available) ====================
    @Test
    public void testSqrt() {
        Dfp four = field.newDfp(4);
        Dfp sqrt = DfpMath.sqrt(four);
        Assert.assertEquals("sqrt(4) = 2", two, sqrt);

        sqrt = DfpMath.sqrt(one);
        Assert.assertEquals("sqrt(1) = 1", one, sqrt);

        // sqrt of zero
        sqrt = DfpMath.sqrt(zero);
        Assert.assertEquals("sqrt(0) = 0", zero, sqrt);

        // sqrt of negative
        sqrt = DfpMath.sqrt(minusOne);
        Assert.assertTrue("sqrt(-1) = NaN", sqrt.isNaN());

        // sqrt of NaN
        sqrt = DfpMath.sqrt(nan);
        Assert.assertTrue("sqrt(NaN) = NaN", sqrt.isNaN());

        // sqrt of infinity
        sqrt = DfpMath.sqrt(inf);
        Assert.assertTrue("sqrt(inf) = inf", sqrt.isInfinite());
    }

    // ==================== Power Test ====================
    @Test
    public void testPow() {
        Dfp base = two;
        Dfp exponent = two;
        Dfp result = DfpMath.pow(base, exponent);
        Assert.assertEquals("2^2 = 4", field.newDfp(4), result);

        // pow with integer exponent using method
        result = DfpMath.pow(ten, 3);
        Assert.assertEquals("10^3 = 1000", field.newDfp(1000), result);

        // pow with negative integer
        result = DfpMath.pow(ten, -2);
        Assert.assertEquals("10^-2 = 0.01", field.newDfp("0.01"), result);

        // pow with zero exponent
        result = DfpMath.pow(ten, 0);
        Assert.assertEquals("10^0 = 1", one, result);

        // pow with NaN
        result = DfpMath.pow(nan, one);
        Assert.assertTrue("pow(NaN, x) = NaN", result.isNaN());

        // pow base zero positive exponent
        result = DfpMath.pow(zero, two);
        Assert.assertEquals("0^2 = 0", zero, result);

        // pow base zero negative exponent -> divide by zero -> inf
        result = DfpMath.pow(zero, minusOne);
        Assert.assertTrue("0^-1 = inf", result.isInfinite());
    }

    // ==================== Copy and Clone ====================
    @Test
    public void testClone() {
        Dfp original = field.newDfp("987.654");
        Dfp cloned = original.clone();
        Assert.assertEquals("Cloned should equal original", original, cloned);
        Assert.assertNotSame("Cloned should be different object from original", original, cloned);
    }

    // ==================== Remainder Test ====================
    @Test
    public void testRemainder() {
        Dfp dividend = ten;
        Dfp divisor = two;
        Dfp rem = dividend.remainder(divisor);
        Assert.assertEquals("10 % 2 = 0", zero, rem);

        dividend = field.newDfp(7);
        divisor = field.newDfp(3);
        rem = dividend.remainder(divisor);
        Assert.assertEquals("7 % 3 = 1", field.newDfp(1), rem);

        // With negative
        dividend = field.newDfp(-7);
        rem = dividend.remainder(divisor);
        Assert.assertEquals("-7 % 3 = -1", field.newDfp(-1), rem);

        // Special cases
        Assert.assertTrue("anything % 0 = NaN", one.remainder(zero).isNaN());
        Assert.assertTrue("NaN % x = NaN", nan.remainder(one).isNaN());
        Assert.assertTrue("x % NaN = NaN", one.remainder(nan).isNaN());
        Assert.assertTrue("inf % x = NaN", inf.remainder(one).isNaN());
        Assert.assertTrue("x % inf = x", one.remainder(inf).equals(one));
    }

    // ==================== Multiply and Divide by Integer ====================
    @Test
    public void testMultiplyDivideByInt() {
        Dfp val = field.newDfp("3.5");
        Dfp result = val.multiply(2);
        Assert.assertEquals("3.5 * 2 = 7", field.newDfp(7), result);

        result = val.divide(2);
        Assert.assertEquals("3.5 / 2 = 1.75", field.newDfp("1.75"), result);

        // Edge case: multiply by zero
        result = val.multiply(0);
        Assert.assertEquals("any * 0 = 0", zero, result);

        // Divide by zero should throw exception or return inf? Dfp.divide(int) calls divide(Dfp)
        // It will result in infinite or NaN.
        try {
            val.divide(0);
            Assert.fail("Expected an exception or infinite result");
        } catch (ArithmeticException | org.apache.commons.math3.exception.MathIllegalArgumentException e) {
            // Expected exception
        }
    }

    // ==================== Rounding Mode Test ====================
    @Test
    public void testRoundingModes() {
        // Test that rounding works correctly with different modes.
        // Since field uses default rounding (ROUND_HALF_UP), test a case that demonstrates.
        Dfp a = field.newDfp("2.5");
        Dfp b = field.newDfp("1");
        Dfp result = a.divide(b); // should be 2.5 exactly
        Assert.assertEquals("2.5/1 = 2.5", field.newDfp("2.5"), result);

        // A division that produces rounding: 1/3 = 0.33333...
        Dfp oneThird = one.divide(field.newDfp(3));
        // With 20 digits, should be 0.33333333333333333333 (20 digits)
        String expected = "0.33333333333333333333";
        Assert.assertEquals("1/3 rounded to 20 digits", expected, oneThird.toString());
    }

    // ==================== Check for potential fault: Add with overflow detection ====================
    @Test
    public void testAddOverflow() {
        // Use near-max exponent values
        Dfp max = field.newDfp("1e1000000"); // exponent near limit
        Dfp overflowSum = max.add(max);
        Assert.assertTrue("Adding two large numbers should overflow to infinity", overflowSum.isInfinite());
    }

    @Test
    public void testSubtractOverflow() {
        Dfp maxPos = field.newDfp("1e1000000");
        Dfp maxNeg = field.newDfp("-1e1000000");
        // maxPos - maxNeg = 2e1000000 -> overflow
        Dfp overflowDiff = maxPos.subtract(maxNeg);
        Assert.assertTrue("Subtracting large negative from large positive should overflow", overflowDiff.isInfinite());
    }

    @Test
    public void testMultiplyOverflow() {
        Dfp large = field.newDfp("1e1000000");
        Dfp overflow = large.multiply(large);
        Assert.assertTrue("Multiplication overflow to infinity", overflow.isInfinite());
    }

    @Test
    public void testDivideUnderflow() {
        Dfp small = field.newDfp("1e-1000");
        Dfp large = field.newDfp("1e1000");
        Dfp underflow = small.divide(large);
        Assert.assertTrue("Underflow should produce zero", underflow.isZero());
    }

    // ==================== Test for Exception on null inputs ====================
    @Test(expected = NullPointerException.class)
    public void testConstructorNullField() {
        new Dfp(null, one); // field is null
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullValue() {
        new Dfp(field, (Dfp) null); // value is null
    }

    // ==================== Additional edge tests for completeness ====================
    @Test
    public void testAlignmentAndScaling() {
        Dfp a = field.newDfp("1.23456e20");
        Dfp b = field.newDfp("2.34567e-20");
        Dfp result = a.add(b); // This requires alignment; should not lose too much precision.
        // For this test, just ensure no crash.
        Assert.assertNotNull(result);
        // The sum should be close to a because b is much smaller.
        Assert.assertEquals("1.23456e20 + small", a, result);
    }

    @Test
    public void testReciprocal() {
        Dfp reciprocal = one.divide(two); // 0.5
        Dfp result = two.multiply(reciprocal);
        Assert.assertEquals("2 * (1/2) = 1", one, result);

        // Reciprocal of zero
        Dfp reciprocZero = one.divide(zero);
        Assert.assertTrue("1/0 = inf", reciprocZero.isInfinite());
    }

    @Test
    public void testNegateZero() {
        Dfp negZero = zero.negate();
        Assert.assertTrue("negate(zero) should be zero", negZero.isZero());
        // Ensure equals returns true
        Assert.assertEquals("zero and negated zero", zero, negZero);
    }

    @Test
    public void testNaNArithmetic() {
        // All arithmetic with NaN should yield NaN
        Assert.assertTrue("NaN + NaN", nan.add(nan).isNaN());
        Assert.assertTrue("NaN - NaN", nan.subtract(nan).isNaN());
        Assert.assertTrue("NaN * NaN", nan.multiply(nan).isNaN());
        Assert.assertTrue("NaN / NaN", nan.divide(nan).isNaN());
        // Mixed
        Assert.assertTrue("NaN + 1", nan.add(one).isNaN());
        Assert.assertTrue("1 + NaN", one.add(nan).isNaN());
    }

    @Test
    public void testInfinityArithmetic() {
        // inf + finite = inf
        Assert.assertTrue("inf + 1 = inf", inf.add(one).isInfinite());
        // inf - inf = NaN
        Assert.assertTrue("inf - inf = NaN", inf.subtract(inf).isNaN());
        // inf * 0 = NaN
        Assert.assertTrue("inf * 0 = NaN", inf.multiply(zero).isNaN());
        // inf / inf = NaN
        Assert.assertTrue("inf / inf = NaN", inf.divide(inf).isNaN());
    }

    @Test
    public void testRoundingModeSet() {
        // Verify setting rounding mode works (if DfpField supports it)
        // DfpField default is ROUND_HALF_UP
        DfpField customField = new DfpField(10);
        // This field will use ROUND_HALF_UP; we can test a rounding case.
        Dfp a = customField.newDfp("1.00000000005");
        Dfp b = customField.newDfp("1.00000000000");
        // Truncation? Actually addition: a + b = 2.00000000005 -> rounds to 2.0000000001 if rounding half up?
        // Not critical. Just ensure no exception.
        Dfp sum = a.add(b);
        Assert.assertNotNull(sum);
    }
}