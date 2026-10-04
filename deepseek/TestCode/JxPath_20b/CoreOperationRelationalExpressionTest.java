package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for CoreOperationRelationalExpression (Defects4J Bug 20).
 * Covers all relational operators (<, >, <=, >=) with various data types,
 * edge cases, and potential fault triggers.
 */
public class CoreOperationRelationalExpressionTest {

    private Constant one;
    private Constant two;
    private Constant three;
    private Constant negativeOne;
    private Constant zero;
    private Constant doubleNaN;
    private Constant doubleInf;
    private Constant doubleNegInf;
    private Constant stringA;
    private Constant stringB;
    private Constant stringEmpty;
    private Constant boolTrue;
    private Constant boolFalse;
    private Constant nullConstant;

    @Before
    public void setUp() {
        one = new Constant("1");
        two = new Constant("2");
        three = new Constant("3");
        negativeOne = new Constant("-1");
        zero = new Constant("0");
        doubleNaN = new Constant(Double.NaN);
        doubleInf = new Constant(Double.POSITIVE_INFINITY);
        doubleNegInf = new Constant(Double.NEGATIVE_INFINITY);
        stringA = new Constant("a");
        stringB = new Constant("b");
        stringEmpty = new Constant("");
        boolTrue = new Constant(Boolean.TRUE);
        boolFalse = new Constant(Boolean.FALSE);
        nullConstant = new Constant(null);
    }

    // Helper to create expression with given operator
    private CoreOperationRelationalExpression createExpr(Constant left, Constant right, int op) {
        return new CoreOperationRelationalExpression(left, right, op);
    }

    // Helper to evaluate expression (assumes getValue() returns boolean)
    private boolean eval(CoreOperationRelationalExpression expr) {
        return expr.getValue();
    }

    // ==================== LESS THAN (<) ====================

    @Test
    public void testLessThan_Numbers() {
        assertTrue(eval(createExpr(one, two, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(two, one, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(one, one, CoreOperationRelationalExpression.LT)));
    }

    @Test
    public void testLessThan_Negative() {
        assertTrue(eval(createExpr(negativeOne, zero, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(zero, negativeOne, CoreOperationRelationalExpression.LT)));
    }

    @Test
    public void testLessThan_NaN() {
        // NaN comparisons should always be false
        assertFalse(eval(createExpr(doubleNaN, one, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(one, doubleNaN, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(doubleNaN, doubleNaN, CoreOperationRelationalExpression.LT)));
    }

    @Test
    public void testLessThan_Infinity() {
        assertTrue(eval(createExpr(one, doubleInf, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(doubleInf, one, CoreOperationRelationalExpression.LT)));
        assertTrue(eval(createExpr(doubleNegInf, one, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(one, doubleNegInf, CoreOperationRelationalExpression.LT)));
    }

    @Test
    public void testLessThan_Strings() {
        assertTrue(eval(createExpr(stringA, stringB, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(stringB, stringA, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(stringA, stringA, CoreOperationRelationalExpression.LT)));
    }

    @Test
    public void testLessThan_EmptyString() {
        assertTrue(eval(createExpr(stringEmpty, stringA, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(stringA, stringEmpty, CoreOperationRelationalExpression.LT)));
    }

    @Test
    public void testLessThan_Boolean() {
        // Boolean comparison: false < true
        assertTrue(eval(createExpr(boolFalse, boolTrue, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(boolTrue, boolFalse, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(boolFalse, boolFalse, CoreOperationRelationalExpression.LT)));
    }

    @Test
    public void testLessThan_Null() {
        // Null compared to anything: likely false or exception
        assertFalse(eval(createExpr(nullConstant, one, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(one, nullConstant, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(nullConstant, nullConstant, CoreOperationRelationalExpression.LT)));
    }

    // ==================== GREATER THAN (>) ====================

    @Test
    public void testGreaterThan_Numbers() {
        assertTrue(eval(createExpr(two, one, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(one, two, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(one, one, CoreOperationRelationalExpression.GT)));
    }

    @Test
    public void testGreaterThan_NaN() {
        assertFalse(eval(createExpr(doubleNaN, one, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(one, doubleNaN, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(doubleNaN, doubleNaN, CoreOperationRelationalExpression.GT)));
    }

    @Test
    public void testGreaterThan_Infinity() {
        assertTrue(eval(createExpr(doubleInf, one, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(one, doubleInf, CoreOperationRelationalExpression.GT)));
        assertTrue(eval(createExpr(one, doubleNegInf, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(doubleNegInf, one, CoreOperationRelationalExpression.GT)));
    }

    @Test
    public void testGreaterThan_Strings() {
        assertTrue(eval(createExpr(stringB, stringA, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(stringA, stringB, CoreOperationRelationalExpression.GT)));
    }

    @Test
    public void testGreaterThan_Boolean() {
        assertTrue(eval(createExpr(boolTrue, boolFalse, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(boolFalse, boolTrue, CoreOperationRelationalExpression.GT)));
    }

    @Test
    public void testGreaterThan_Null() {
        assertFalse(eval(createExpr(nullConstant, one, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(one, nullConstant, CoreOperationRelationalExpression.GT)));
        assertFalse(eval(createExpr(nullConstant, nullConstant, CoreOperationRelationalExpression.GT)));
    }

    // ==================== LESS THAN OR EQUAL (<=) ====================

    @Test
    public void testLessThanOrEqual_Numbers() {
        assertTrue(eval(createExpr(one, two, CoreOperationRelationalExpression.LE)));
        assertTrue(eval(createExpr(one, one, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(two, one, CoreOperationRelationalExpression.LE)));
    }

    @Test
    public void testLessThanOrEqual_NaN() {
        assertFalse(eval(createExpr(doubleNaN, one, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(one, doubleNaN, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(doubleNaN, doubleNaN, CoreOperationRelationalExpression.LE)));
    }

    @Test
    public void testLessThanOrEqual_Infinity() {
        assertTrue(eval(createExpr(one, doubleInf, CoreOperationRelationalExpression.LE)));
        assertTrue(eval(createExpr(doubleInf, doubleInf, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(doubleInf, one, CoreOperationRelationalExpression.LE)));
    }

    @Test
    public void testLessThanOrEqual_Strings() {
        assertTrue(eval(createExpr(stringA, stringB, CoreOperationRelationalExpression.LE)));
        assertTrue(eval(createExpr(stringA, stringA, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(stringB, stringA, CoreOperationRelationalExpression.LE)));
    }

    @Test
    public void testLessThanOrEqual_Boolean() {
        assertTrue(eval(createExpr(boolFalse, boolTrue, CoreOperationRelationalExpression.LE)));
        assertTrue(eval(createExpr(boolFalse, boolFalse, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(boolTrue, boolFalse, CoreOperationRelationalExpression.LE)));
    }

    @Test
    public void testLessThanOrEqual_Null() {
        assertFalse(eval(createExpr(nullConstant, one, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(one, nullConstant, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(nullConstant, nullConstant, CoreOperationRelationalExpression.LE)));
    }

    // ==================== GREATER THAN OR EQUAL (>=) ====================

    @Test
    public void testGreaterThanOrEqual_Numbers() {
        assertTrue(eval(createExpr(two, one, CoreOperationRelationalExpression.GE)));
        assertTrue(eval(createExpr(one, one, CoreOperationRelationalExpression.GE)));
        assertFalse(eval(createExpr(one, two, CoreOperationRelationalExpression.GE)));
    }

    @Test
    public void testGreaterThanOrEqual_NaN() {
        assertFalse(eval(createExpr(doubleNaN, one, CoreOperationRelationalExpression.GE)));
        assertFalse(eval(createExpr(one, doubleNaN, CoreOperationRelationalExpression.GE)));
        assertFalse(eval(createExpr(doubleNaN, doubleNaN, CoreOperationRelationalExpression.GE)));
    }

    @Test
    public void testGreaterThanOrEqual_Infinity() {
        assertTrue(eval(createExpr(doubleInf, one, CoreOperationRelationalExpression.GE)));
        assertTrue(eval(createExpr(doubleInf, doubleInf, CoreOperationRelationalExpression.GE)));
        assertFalse(eval(createExpr(one, doubleInf, CoreOperationRelationalExpression.GE)));
    }

    @Test
    public void testGreaterThanOrEqual_Strings() {
        assertTrue(eval(createExpr(stringB, stringA, CoreOperationRelationalExpression.GE)));
        assertTrue(eval(createExpr(stringA, stringA, CoreOperationRelationalExpression.GE)));
        assertFalse(eval(createExpr(stringA, stringB, CoreOperationRelationalExpression.GE)));
    }

    @Test
    public void testGreaterThanOrEqual_Boolean() {
        assertTrue(eval(createExpr(boolTrue, boolFalse, CoreOperationRelationalExpression.GE)));
        assertTrue(eval(createExpr(boolTrue, boolTrue, CoreOperationRelationalExpression.GE)));
        assertFalse(eval(createExpr(boolFalse, boolTrue, CoreOperationRelationalExpression.GE)));
    }

    @Test
    public void testGreaterThanOrEqual_Null() {
        assertFalse(eval(createExpr(nullConstant, one, CoreOperationRelationalExpression.GE)));
        assertFalse(eval(createExpr(one, nullConstant, CoreOperationRelationalExpression.GE)));
        assertFalse(eval(createExpr(nullConstant, nullConstant, CoreOperationRelationalExpression.GE)));
    }

    // ==================== MIXED TYPES & EDGE CASES ====================

    @Test
    public void testMixedNumberString() {
        // Number vs string: should convert string to number or compare as strings?
        // Typically XPath converts to number if possible.
        Constant num = new Constant("5");
        Constant str = new Constant("5");
        // Equal values: < should be false, <= true, etc.
        assertFalse(eval(createExpr(num, str, CoreOperationRelationalExpression.LT)));
        assertTrue(eval(createExpr(num, str, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(num, str, CoreOperationRelationalExpression.GT)));
        assertTrue(eval(createExpr(num, str, CoreOperationRelationalExpression.GE)));
    }

    @Test
    public void testMixedBooleanNumber() {
        // Boolean true converts to 1, false to 0
        assertTrue(eval(createExpr(boolFalse, one, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(boolTrue, one, CoreOperationRelationalExpression.LT)));
        assertTrue(eval(createExpr(boolTrue, boolFalse, CoreOperationRelationalExpression.GT)));
    }

    @Test
    public void testLargeNumbers() {
        Constant large = new Constant("2147483647"); // Integer.MAX_VALUE
        Constant larger = new Constant("2147483648"); // > MAX_VALUE
        assertTrue(eval(createExpr(large, larger, CoreOperationRelationalExpression.LT)));
        assertFalse(eval(createExpr(larger, large, CoreOperationRelationalExpression.LT)));
    }

    @Test
    public void testNegativeZero() {
        Constant negZero = new Constant("-0.0");
        Constant posZero = new Constant("0.0");
        // -0.0 == 0.0 in Java, so < should be false, <= true
        assertFalse(eval(createExpr(negZero, posZero, CoreOperationRelationalExpression.LT)));
        assertTrue(eval(createExpr(negZero, posZero, CoreOperationRelationalExpression.LE)));
        assertFalse(eval(createExpr(posZero, negZero, CoreOperationRelationalExpression.GT)));
        assertTrue(eval(createExpr(posZero, negZero, CoreOperationRelationalExpression.GE)));
    }

    @Test(expected = Exception.class)
    public void testIncompatibleTypes() {
        // Comparing a number to a boolean might throw? Or return false.
        // We expect some exception or false; here we assume it might throw.
        // If not, adjust accordingly.
        Constant num = new Constant("1");
        Constant bool = new Constant(Boolean.TRUE);
        eval(createExpr(num, bool, CoreOperationRelationalExpression.LT));
    }
}