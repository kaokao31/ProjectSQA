package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreOperationRelationalExpressionTest {

    // Helper class to expose protected compare method
    private static class TestRelationalExpression extends CoreOperationRelationalExpression {
        private final String operator;

        public TestRelationalExpression(String operator) {
            super(null, null, operator);
            this.operator = operator;
        }

        @Override
        public String getOperator() {
            return operator;
        }

        @Override
        public boolean compare(Object left, Object right) {
            return super.compare(left, right);
        }
    }

    @Test
    public void testGetOperator() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertEquals("<", expr.getOperator());
        expr = new TestRelationalExpression(">");
        assertEquals(">", expr.getOperator());
        expr = new TestRelationalExpression("<=");
        assertEquals("<=", expr.getOperator());
        expr = new TestRelationalExpression(">=");
        assertEquals(">=", expr.getOperator());
    }

    // Tests for less than operator
    @Test
    public void testCompareLessThanNumbers() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare(1, 2));
        assertFalse(expr.compare(2, 1));
        assertFalse(expr.compare(1, 1));
        assertTrue(expr.compare(-1, 0));
        assertFalse(expr.compare(0, -1));
    }

    @Test
    public void testCompareLessThanDoubles() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare(1.5, 2.5));
        assertFalse(expr.compare(2.5, 1.5));
        assertFalse(expr.compare(1.5, 1.5));
        assertTrue(expr.compare(-0.5, 0.5));
    }

    @Test
    public void testCompareLessThanStringWithNumber() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        // Numeric string
        assertTrue(expr.compare("1", 2));
        assertFalse(expr.compare("2", 1));
        assertFalse(expr.compare("1", 1));
        // Non-numeric string should be NaN, comparison false
        assertFalse(expr.compare("abc", 1));
        assertFalse(expr.compare("abc", -1));
        // Empty string
        assertFalse(expr.compare("", 1));
        // String with spaces
        assertFalse(expr.compare("  ", 1));
    }

    @Test
    public void testCompareLessThanNumberWithString() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare(1, "2"));
        assertFalse(expr.compare(2, "1"));
        assertFalse(expr.compare(1, "1"));
        assertFalse(expr.compare(1, "abc"));
        assertFalse(expr.compare(-1, "abc"));
    }

    @Test
    public void testCompareLessThanBothStrings() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        // Both numeric strings
        assertTrue(expr.compare("1", "2"));
        assertFalse(expr.compare("2", "1"));
        assertFalse(expr.compare("1", "1"));
        // Both non-numeric strings
        assertFalse(expr.compare("abc", "def"));
        assertFalse(expr.compare("def", "abc"));
        // Mixed
        assertFalse(expr.compare("abc", "1"));
        assertFalse(expr.compare("1", "abc"));
    }

    @Test
    public void testCompareLessThanBoolean() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        // Boolean to number: true=1, false=0
        assertTrue(expr.compare(false, true));
        assertFalse(expr.compare(true, false));
        assertFalse(expr.compare(true, true));
        assertFalse(expr.compare(false, false));
        // Boolean with number
        assertTrue(expr.compare(false, 1));
        assertFalse(expr.compare(true, 1));
        assertTrue(expr.compare(false, 2));
        assertFalse(expr.compare(true, 0));
        // Boolean with string
        assertTrue(expr.compare(false, "1"));
        assertFalse(expr.compare(true, "1"));
        assertFalse(expr.compare(true, "abc"));
        assertFalse(expr.compare(false, "abc"));
    }

    @Test
    public void testCompareLessThanNull() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        // Null should be treated as NaN or empty; comparison false
        assertFalse(expr.compare(null, 1));
        assertFalse(expr.compare(1, null));
        assertFalse(expr.compare(null, null));
        assertFalse(expr.compare(null, "abc"));
        assertFalse(expr.compare("abc", null));
    }

    @Test
    public void testCompareLessThanNaN() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        Double nan = Double.NaN;
        assertFalse(expr.compare(nan, 1));
        assertFalse(expr.compare(1, nan));
        assertFalse(expr.compare(nan, nan));
        assertFalse(expr.compare(nan, "abc"));
        assertFalse(expr.compare("abc", nan));
    }

    @Test
    public void testCompareLessThanInfinity() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        Double posInf = Double.POSITIVE_INFINITY;
        Double negInf = Double.NEGATIVE_INFINITY;
        assertTrue(expr.compare(1, posInf));
        assertFalse(expr.compare(posInf, 1));
        assertTrue(expr.compare(negInf, 1));
        assertFalse(expr.compare(1, negInf));
        assertFalse(expr.compare(posInf, posInf));
        assertFalse(expr.compare(negInf, negInf));
        assertTrue(expr.compare(negInf, posInf));
        assertFalse(expr.compare(posInf, negInf));
    }

    @Test
    public void testCompareLessThanNegativeZero() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        Double negZero = -0.0;
        Double posZero = 0.0;
        // In Java, -0.0 < 0.0 is false, but in XPath, -0.0 equals 0.0, so comparison should be false.
        assertFalse(expr.compare(negZero, posZero));
        assertFalse(expr.compare(posZero, negZero));
        assertFalse(expr.compare(negZero, negZero));
        assertFalse(expr.compare(posZero, posZero));
        // With numbers
        assertTrue(expr.compare(negZero, 1));
        assertFalse(expr.compare(1, negZero));
    }

    // Similar tests for greater than operator
    @Test
    public void testCompareGreaterThanNumbers() {
        TestRelationalExpression expr = new TestRelationalExpression(">");
        assertTrue(expr.compare(2, 1));
        assertFalse(expr.compare(1, 2));
        assertFalse(expr.compare(1, 1));
    }

    @Test
    public void testCompareGreaterThanStringWithNumber() {
        TestRelationalExpression expr = new TestRelationalExpression(">");
        assertTrue(expr.compare("2", 1));
        assertFalse(expr.compare("1", 2));
        assertFalse(expr.compare("1", 1));
        assertFalse(expr.compare("abc", 1));
        assertFalse(expr.compare("abc", -1));
    }

    // Tests for less than or equal operator
    @Test
    public void testCompareLessThanOrEqualNumbers() {
        TestRelationalExpression expr = new TestRelationalExpression("<=");
        assertTrue(expr.compare(1, 2));
        assertFalse(expr.compare(2, 1));
        assertTrue(expr.compare(1, 1));
    }

    @Test
    public void testCompareLessThanOrEqualStringWithNumber() {
        TestRelationalExpression expr = new TestRelationalExpression("<=");
        assertTrue(expr.compare("1", 2));
        assertFalse(expr.compare("2", 1));
        assertTrue(expr.compare("1", 1));
        assertFalse(expr.compare("abc", 1));
    }

    // Tests for greater than or equal operator
    @Test
    public void testCompareGreaterThanOrEqualNumbers() {
        TestRelationalExpression expr = new TestRelationalExpression(">=");
        assertTrue(expr.compare(2, 1));
        assertFalse(expr.compare(1, 2));
        assertTrue(expr.compare(1, 1));
    }

    @Test
    public void testCompareGreaterThanOrEqualStringWithNumber() {
        TestRelationalExpression expr = new TestRelationalExpression(">=");
        assertTrue(expr.compare("2", 1));
        assertFalse(expr.compare("1", 2));
        assertTrue(expr.compare("1", 1));
        assertFalse(expr.compare("abc", 1));
    }

    // Additional edge cases
    @Test
    public void testCompareWithVeryLargeNumbers() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare(Double.MAX_VALUE, Double.POSITIVE_INFINITY));
        assertFalse(expr.compare(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
        assertTrue(expr.compare(Double.NEGATIVE_INFINITY, Double.MIN_VALUE));
    }

    @Test
    public void testCompareWithMinValue() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare(Double.MIN_VALUE, 1));
        assertFalse(expr.compare(1, Double.MIN_VALUE));
    }

    @Test
    public void testCompareWithIntegerMinMax() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertFalse(expr.compare(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertFalse(expr.compare(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    public void testCompareWithFloatAndDouble() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare(1.0f, 2.0));
        assertFalse(expr.compare(2.0, 1.0f));
        assertFalse(expr.compare(1.0f, 1.0));
    }

    @Test
    public void testCompareWithStringContainingLeadingZeros() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare("001", 2));
        assertFalse(expr.compare("002", 1));
        assertFalse(expr.compare("001", 1));
    }

    @Test
    public void testCompareWithStringContainingDecimal() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare("1.5", 2));
        assertFalse(expr.compare("2.5", 1));
        assertFalse(expr.compare("1.5", 1.5));
    }

    @Test
    public void testCompareWithStringContainingNegativeNumber() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare("-1", 0));
        assertFalse(expr.compare("0", -1));
        assertFalse(expr.compare("-1", -1));
    }

    @Test
    public void testCompareWithStringContainingExponent() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertTrue(expr.compare("1e2", 200));
        assertFalse(expr.compare("2e2", 100));
        assertFalse(expr.compare("1e2", 100));
    }

    @Test
    public void testCompareWithBooleanAndString() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        // Boolean to number: true=1, false=0; string to number if numeric
        assertTrue(expr.compare(false, "1"));
        assertFalse(expr.compare(true, "1"));
        assertFalse(expr.compare(false, "0"));
        assertFalse(expr.compare(true, "0"));
        // Non-numeric string
        assertFalse(expr.compare(false, "abc"));
        assertFalse(expr.compare(true, "abc"));
    }

    @Test
    public void testCompareWithNullAndNaN() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        assertFalse(expr.compare(null, Double.NaN));
        assertFalse(expr.compare(Double.NaN, null));
    }

    @Test
    public void testCompareWithDifferentTypes() {
        TestRelationalExpression expr = new TestRelationalExpression("<");
        // Compare number with boolean
        assertTrue(expr.compare(0, true));
        assertFalse(expr.compare(1, true));
        assertTrue(expr.compare(0, false));
        assertFalse(expr.compare(1, false));
        // Compare string with boolean
        assertTrue(expr.compare("0", true));
        assertFalse(expr.compare("1", true));
        assertFalse(expr.compare("abc", true));
    }
}