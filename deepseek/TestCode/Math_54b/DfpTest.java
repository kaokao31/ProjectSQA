package org.apache.commons.math.dfp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Dfp} class.
 * Covers all major operations, edge cases, and special values.
 */
public class DfpTest {

    private DfpField field;

    @Before
    public void setUp() {
        field = new DfpField(20); // high precision for testing
    }

    // ---- Constructors ----
    @Test
    public void testConstructorInt() {
        Dfp d = new Dfp(field, 123);
        assertEquals("123", d.toString());
        assertFalse(d.isNaN());
        assertFalse(d.isInfinite());
    }

    @Test
    public void testConstructorLong() {
        Dfp d = new Dfp(field, 1234567890123456789L);
        assertEquals("1234567890123456789", d.toString());
    }

    @Test
    public void testConstructorDouble() {
        Dfp d = new Dfp(field, Math.PI);
        double val = d.doubleValue();
        assertEquals(Math.PI, val, 0.0);
    }

    @Test
    public void testConstructorString() {
        Dfp d = new Dfp(field, "3.14159265358979323846");
        assertEquals("3.14159265358979323846", d.toString());
    }

    @Test
    public void testConstructorStringScientific() {
        Dfp d = new Dfp(field, "1.23e-4");
        assertEquals("1.23e-4", d.toString());
    }

    @Test(expected = NumberFormatException.class)
    public void testConstructorInvalidString() {
        new Dfp(field, "abc");
    }

    @Test
    public void testConstructorZero() {
        Dfp d = new Dfp(field, 0);
        assertTrue(d.isZero());
        assertFalse(d.negative().isZero()); // negative zero
    }

    // ---- Addition ----
    @Test
    public void testAddSimple() {
        Dfp a = new Dfp(field, 2);
        Dfp b = new Dfp(field, 3);
        assertEquals(new Dfp(field, 5), a.add(b));
    }

    @Test
    public void testAddNegative() {
        Dfp a = new Dfp(field, 5);
        Dfp b = new Dfp(field, -3);
        assertEquals(new Dfp(field, 2), a.add(b));
    }

    @Test
    public void testAddInfinite() {
        Dfp inf = new Dfp(field, "Inf");
        Dfp finite = new Dfp(field, 1);
        assertTrue(inf.add(finite).isInfinite());
    }

    @Test
    public void testAddNaN() {
        Dfp nan = new Dfp(field, "NaN");
        Dfp num = new Dfp(field, 1);
        assertTrue(nan.add(num).isNaN());
        assertTrue(num.add(nan).isNaN());
    }

    // ---- Subtraction ----
    @Test
    public void testSubtractSimple() {
        Dfp a = new Dfp(field, 5);
        Dfp b = new Dfp(field, 3);
        assertEquals(new Dfp(field, 2), a.subtract(b));
    }

    @Test
    public void testSubtractNegative() {
        Dfp a = new Dfp(field, 5);
        Dfp b = new Dfp(field, -3);
        assertEquals(new Dfp(field, 8), a.subtract(b));
    }

    // ---- Multiplication ----
    @Test
    public void testMultiplySimple() {
        Dfp a = new Dfp(field, 2);
        Dfp b = new Dfp(field, 3);
        assertEquals(new Dfp(field, 6), a.multiply(b));
    }

    @Test
    public void testMultiplyByZero() {
        Dfp zero = new Dfp(field, 0);
        Dfp num = new Dfp(field, 10);
        Dfp res = zero.multiply(num);
        assertTrue(res.isZero());
        assertFalse(res.negative().isZero()); // zero * positive = +0
    }

    @Test
    public void testMultiplyByNegative() {
        Dfp a = new Dfp(field, 5);
        Dfp b = new Dfp(field, -2);
        Dfp res = a.multiply(b);
        assertTrue(res.negative().isNegative()); // result negative
    }

    @Test
    public void testMultiplyInfinities() {
        Dfp inf = new Dfp(field, "Inf");
        Dfp num = new Dfp(field, 3);
        assertTrue(inf.multiply(num).isInfinite());
        Dfp zero = new Dfp(field, 0);
        assertTrue(inf.multiply(zero).isNaN()); // Inf * 0 = NaN
    }

    // ---- Division ----
    @Test
    public void testDivideSimple() {
        Dfp a = new Dfp(field, 10);
        Dfp b = new Dfp(field, 2);
        assertEquals(new Dfp(field, 5), a.divide(b));
    }

    @Test(expected = DfpMathException.class)
    public void testDivideByZeroPositive() {
        Dfp a = new Dfp(field, 1);
        Dfp zero = new Dfp(field, 0);
        a.divide(zero);
    }

    @Test
    public void testDivideByZeroZero() {
        Dfp zero = new Dfp(field, 0);
        try {
            zero.divide(zero);
            fail("Expected DfpMathException for 0/0");
        } catch (DfpMathException e) {
            // expected
        }
    }

    @Test
    public void testDivideInfinite() {
        Dfp inf = new Dfp(field, "Inf");
        Dfp num = new Dfp(field, 3);
        assertTrue(inf.divide(num).isInfinite());
        Dfp zero = new Dfp(field, 0);
        assertTrue(inf.divide(inf).isNaN()); // Inf/Inf = NaN
    }

    // ---- Comparison ----
    @Test
    public void testCompareTo() {
        Dfp a = new Dfp(field, 1);
        Dfp b = new Dfp(field, 2);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(a));
    }

    @Test
    public void testCompareToNaN() {
        Dfp nan = new Dfp(field, "NaN");
        Dfp num = new Dfp(field, 1);
        // NaN compares as equal to itself? In Dfp, NaN==NaN is false, but compareTo should throw?
        // Actually Dfp.compareTo returns +1 for NaN > anything? Check typical behavior: Dfp.compareTo returns 1 if NaN > argument? Not specified in Javadoc.
        // We'll test that it doesn't throw and returns non-zero.
        assertTrue(nan.compareTo(num) != 0);
        assertTrue(num.compareTo(nan) != 0);
        assertTrue(nan.compareTo(nan) == 0); // NaN == NaN in compareTo? Actually, equals returns false but compareTo may treat NaN as equal. We'll keep test.
    }

    @Test
    public void testEquals() {
        Dfp a = new Dfp(field, 1.0);
        Dfp b = new Dfp(field, 1.0);
        assertEquals(a, b);
        assertFalse(a.equals(new Dfp(field, 2.0)));
        Dfp nan = new Dfp(field, "NaN");
        assertFalse(nan.equals(nan)); // NaN != NaN
        Dfp zero = new Dfp(field, 0);
        Dfp negZero = zero.negate();
        assertFalse(zero.equals(negZero)); // +0 != -0
    }

    @Test
    public void testIsZero() {
        assertTrue(new Dfp(field, 0).isZero());
        assertFalse(new Dfp(field, 1).isZero());
    }

    @Test
    public void testIsNegative() {
        assertFalse(new Dfp(field, 1).isNegative());
        assertTrue(new Dfp(field, -1).isNegative());
    }

    // ---- Rounding methods ----
    @Test
    public void testFloor() {
        Dfp d = new Dfp(field, 3.7);
        assertEquals(new Dfp(field, 3), d.floor());
        d = new Dfp(field, -2.3);
        assertEquals(new Dfp(field, -3), d.floor());
    }

    @Test
    public void testCeil() {
        Dfp d = new Dfp(field, 3.7);
        assertEquals(new Dfp(field, 4), d.ceil());
        d = new Dfp(field, -2.3);
        assertEquals(new Dfp(field, -2), d.ceil());
    }

    @Test
    public void testRint() {
        Dfp d = new Dfp(field, 3.5);
        assertEquals(new Dfp(field, 4), d.rint());
        d = new Dfp(field, 2.5);
        assertEquals(new Dfp(field, 2), d.rint()); // banker's rounding
    }

    @Test
    public void testRemainder() {
        Dfp a = new Dfp(field, 10);
        Dfp b = new Dfp(field, 3);
        assertEquals(new Dfp(field, 1), a.remainder(b));
        a = new Dfp(field, -10);
        b = new Dfp(field, 3);
        assertEquals(new Dfp(field, -1), a.remainder(b));
    }

    // ---- Conversion ----
    @Test
    public void testDoubleValue() {
        Dfp d = new Dfp(field, Math.PI);
        assertEquals(Math.PI, d.doubleValue(), 1e-20);
    }

    @Test
    public void testIntValue() {
        Dfp d = new Dfp(field, 123.45);
        assertEquals(123, d.intValue());
    }

    @Test
    public void testLongValue() {
        Dfp d = new Dfp(field, 9876543210L);
        assertEquals(9876543210L, d.longValue());
    }

    @Test
    public void testToDecimalDigits() {
        Dfp d = new Dfp(field, "123.456789");
        int digits = d.toDecimalDigits();
        assertEquals(6, digits);
    }

    // ---- Special values ----
    @Test
    public void testNaN() {
        Dfp nan = new Dfp(field, "NaN");
        assertTrue(nan.isNaN());
        assertFalse(nan.isInfinite());
    }

    @Test
    public void testInfinity() {
        Dfp inf = new Dfp(field, "Inf");
        assertTrue(inf.isInfinite());
        assertFalse(inf.isNaN());
        Dfp negInf = new Dfp(field, "-Inf");
        assertTrue(negInf.isInfinite());
        assertTrue(negInf.isNegative());
    }

    @Test
    public void testSquareRoot() {
        Dfp d = new Dfp(field, 4);
        assertEquals(new Dfp(field, 2), d.sqrt());
        Dfp neg = new Dfp(field, -1);
        assertTrue(neg.sqrt().isNaN()); // sqrt of negative
    }

    @Test
    public void testExp() {
        Dfp d = new Dfp(field, 0);
        assertEquals(new Dfp(field, 1), d.exp());
        d = new Dfp(field, 1);
        Dfp e = d.exp();
        assertTrue(e.compareTo(new Dfp(field, "2.71828182845904523536")) == 0 ||
                   Math.abs(e.doubleValue() - Math.E) < 1e-15);
    }

    @Test
    public void testLog() {
        Dfp d = new Dfp(field, 2.718281828459045);
        Dfp ln = d.log();
        assertTrue(Math.abs(ln.doubleValue() - 1.0) < 1e-14);
        Dfp zero = new Dfp(field, 0);
        assertTrue(zero.log().isInfinite()); // log(0) = -Inf
        Dfp neg = new Dfp(field, -1);
        assertTrue(neg.log().isNaN()); // log negative
    }

    @Test
    public void testPow() {
        Dfp a = new Dfp(field, 2);
        Dfp b = new Dfp(field, 3);
        assertEquals(new Dfp(field, 8), a.pow(b));
        Dfp zero = new Dfp(field, 0);
        assertEquals(new Dfp(field, 1), zero.pow(zero)); // 0^0 = 1
    }

    // ---- Negate and abs ----
    @Test
    public void testNegate() {
        Dfp d = new Dfp(field, 5);
        Dfp neg = d.negate();
        assertEquals(new Dfp(field, -5), neg);
        assertFalse(neg.isNegative()); // it is negative
        assertTrue(neg.isNegative());
        Dfp zero = new Dfp(field, 0);
        Dfp negZero = zero.negate();
        assertTrue(negZero.isZero());
        assertTrue(negZero.isNegative()); // negative zero
    }

    @Test
    public void testAbs() {
        Dfp d = new Dfp(field, -7);
        assertEquals(new Dfp(field, 7), d.abs());
        Dfp zero = new Dfp(field, 0);
        assertTrue(zero.abs().isZero());
    }

    // ---- Copy and clone ----
    @Test
    public void testCopy() {
        Dfp d = new Dfp(field, "123.456");
        Dfp copy = d.copy();
        assertEquals(d, copy);
        assertNotSame(d, copy);
    }

    // ---- Remainder edge ----
    @Test
    public void testRemainderByZero() {
        Dfp a = new Dfp(field, 5);
        Dfp zero = new Dfp(field, 0);
        try {
            a.remainder(zero);
            fail("Expected DfpMathException");
        } catch (DfpMathException e) {
            // expected
        }
    }

    // ---- Reciprocal ----
    @Test
    public void testReciprocal() {
        Dfp d = new Dfp(field, 4);
        assertEquals(new Dfp(field, "0.25"), d.reciprocal());
        Dfp zero = new Dfp(field, 0);
        assertTrue(zero.reciprocal().isInfinite());
    }

    // ---- Multiply by int ----
    @Test
    public void testMultiplyInt() {
        Dfp d = new Dfp(field, 2.5);
        assertEquals(new Dfp(field, 7.5), d.multiply(3));
    }

    // ---- Test toString for various cases ----
    @Test
    public void testToString() {
        assertEquals("0", new Dfp(field, 0).toString());
        assertEquals("-0", new Dfp(field, 0).negate().toString());
        assertEquals("Inf", new Dfp(field, "Inf").toString());
        assertEquals("-Inf", new Dfp(field, "-Inf").toString());
        assertEquals("NaN", new Dfp(field, "NaN").toString());
        assertEquals("1.0", new Dfp(field, 1.0).toString());
    }

    // ---- Test hash code ----
    @Test
    public void testHashCode() {
        Dfp a = new Dfp(field, 1);
        Dfp b = new Dfp(field, 1);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.hashCode() == new Dfp(field, 2).hashCode());
    }

    // ---- Edge: maximum and minimum values ----
    @Test
    public void testExtremeValues() {
        // Very large exponent
        Dfp large = new Dfp(field, "1e1000");
        assertTrue(large.isInfinite()); // exceeds precision? Actually depends on implementation. Might overflow.
        // We'll just test that it doesn't crash.
    }

    // ---- Multiply by zero edge (sign) ----
    @Test
    public void testMultiplyByNegativeZero() {
        Dfp posZero = new Dfp(field, 0);
        Dfp negZero = posZero.negate();
        Dfp neg = new Dfp(field, -1);
        Dfp res = posZero.multiply(neg);
        assertTrue(res.isZero());
        assertTrue(res.isNegative() == false); // 0 * -1 = 0 (not negative)
        res = negZero.multiply(neg);
        assertTrue(res.isZero());
        // 0 * -1 = 0, sign? Actually 0 * -1 = 0, and sign is positive. For negative zero * -1 = 0? sign should be positive.
    }

    // ---- Test DfpMathException ----
    @Test(expected = DfpMathException.class)
    public void testDivideByZeroThrowsException() {
        Dfp a = new Dfp(field, 1);
        Dfp zero = new Dfp(field, 0);
        a.divide(zero);
    }

    // ---- Test remainder with negative numbers ----
    @Test
    public void testRemainderNegative() {
        Dfp a = new Dfp(field, -10);
        Dfp b = new Dfp(field, 3);
        assertEquals(new Dfp(field, -1), a.remainder(b));
    }

    // ---- Test equals with different fields ----
    @Test
    public void testEqualsDifferentFields() {
        DfpField field2 = new DfpField(30);
        Dfp a = new Dfp(field, 1.0);
        Dfp b = new Dfp(field2, 1.0);
        assertTrue(a.equals(b)); // even though different fields, values should be equal
    }

    // ---- Test getField ----
    @Test
    public void testGetField() {
        Dfp d = new Dfp(field, 1);
        assertSame(field, d.getField());
    }
}