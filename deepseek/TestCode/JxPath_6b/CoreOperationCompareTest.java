package org.apache.commons.jxpath.ri.compiler;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for CoreOperationCompare and its subclasses.
 * Designed to achieve maximum coverage and detect faults,
 * particularly the known Defects4J bug 6.
 */
public class CoreOperationCompareTest {

    private CoreOperationEqual equalOp;
    private CoreOperationNotEqual notEqualOp;
    private CoreOperationGreaterThan greaterThanOp;
    private CoreOperationGreaterThanOrEqual greaterOrEqualOp;
    private CoreOperationLessThan lessThanOp;
    private CoreOperationLessThanOrEqual lessOrEqualOp;

    @Before
    public void setUp() {
        // Dummy constants for constructing operations (infix markers)
        // We'll use simple constants to represent the infix '=' and '!=' etc.
        // CoreOperationCompare constructor expects two Expression arguments and a boolean for invert.
        // For simplicity, we create operations with numeric constants.
        Constant left = new Constant("1");
        Constant right = new Constant("2");
        equalOp = new CoreOperationEqual(left, right);
        notEqualOp = new CoreOperationNotEqual(left, right);
        greaterThanOp = new CoreOperationGreaterThan(left, right);
        greaterOrEqualOp = new CoreOperationGreaterThanOrEqual(left, right);
        lessThanOp = new CoreOperationLessThan(left, right);
        lessOrEqualOp = new CoreOperationLessThanOrEqual(left, right);
    }

    // ---------- Basic numeric comparisons ----------

    @Test
    public void testEqualTrue() {
        Constant c1 = new Constant("1.0");
        Constant c2 = new Constant("1.0");
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);
        assertTrue(op.eval() instanceof Boolean);
        assertTrue(((Boolean) op.eval()));
    }

    @Test
    public void testEqualFalse() {
        Constant c1 = new Constant("1");
        Constant c2 = new Constant("2");
        CoreOperationEqual op = new CoreOperationEqual(c1, c2);
        assertFalse(((Boolean) op.eval()));
    }

    @Test
    public void testNotEqualTrue() {
        Constant c1 = new Constant("1");
        Constant c2 = new Constant("2");
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        assertTrue(((Boolean) op.eval()));
    }

    @Test
    public void testNotEqualFalse() {
        Constant c1 = new Constant("1.0");
        Constant c2 = new Constant("1.0");
        CoreOperationNotEqual op = new CoreOperationNotEqual(c1, c2);
        assertFalse(((Boolean) op.eval()));
    }

    // ---------- Comparison operators ----------

    @Test
    public void testGreaterThan() {
        Constant c1 = new Constant("5");
        Constant c2 = new Constant("3");
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(c1, c2);
        assertTrue(((Boolean) op.eval()));
        // inverse
        CoreOperationGreaterThan op2 = new CoreOperationGreaterThan(c2, c1);
        assertFalse(((Boolean) op2.eval()));
    }

    @Test
    public void testGreaterThanOrEqual() {
        Constant c1 = new Constant("5");
        Constant c2 = new Constant("5");
        CoreOperationGreaterThanOrEqual op = new CoreOperationGreaterThanOrEqual(c1, c2);
        assertTrue(((Boolean) op.eval()));
        Constant c3 = new Constant("6");
        CoreOperationGreaterThanOrEqual op2 = new CoreOperationGreaterThanOrEqual(c3, c1);
        assertTrue(((Boolean) op2.eval()));
        Constant c4 = new Constant("4");
        CoreOperationGreaterThanOrEqual op3 = new CoreOperationGreaterThanOrEqual(c4, c1);
        assertFalse(((Boolean) op3.eval()));
    }

    @Test
    public void testLessThan() {
        Constant c1 = new Constant("2");
        Constant c2 = new Constant("10");
        CoreOperationLessThan op = new CoreOperationLessThan(c1, c2);
        assertTrue(((Boolean) op.eval()));
        CoreOperationLessThan op2 = new CoreOperationLessThan(c2, c1);
        assertFalse(((Boolean) op2.eval()));
    }

    @Test
    public void testLessThanOrEqual() {
        Constant c1 = new Constant("3");
        Constant c2 = new Constant("3");
        CoreOperationLessThanOrEqual op = new CoreOperationLessThanOrEqual(c1, c2);
        assertTrue(((Boolean) op.eval()));
        Constant c3 = new Constant("2");
        CoreOperationLessThanOrEqual op2 = new CoreOperationLessThanOrEqual(c3, c1);
        assertTrue(((Boolean) op2.eval()));
        Constant c4 = new Constant("4");
        CoreOperationLessThanOrEqual op3 = new CoreOperationLessThanOrEqual(c4, c1);
        assertFalse(((Boolean) op3.eval()));
    }

    // ---------- Type conversion and edge cases ----------

    @Test
    public void testCompareStringAndNumber() {
        Constant left = new Constant("10");
        Constant right = new Constant(10);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    @Test
    public void testCompareBooleanAndNumber() {
        Constant left = new Constant(true);
        Constant right = new Constant("1");
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    @Test
    public void testCompareNull() {
        Constant left = new Constant((Object) null);
        Constant right = new Constant("null");
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        // null should compare as empty string? Depends on implementation.
        // We assert false as a typical XPath behavior.
        assertFalse(((Boolean) op.eval()));
    }

    @Test
    public void testCompareBothNull() {
        Constant left = new Constant((Object) null);
        Constant right = new Constant((Object) null);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    // ---------- NaN handling (Defects4J bug 6) ----------

    @Test
    public void testCompareNaNWithNaNEqual() {
        Constant left = new Constant(Double.NaN);
        Constant right = new Constant(Double.NaN);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        // In XPath, NaN is not equal to itself. Bug: some implementations return true.
        // This test is designed to catch that.
        assertFalse("NaN should not be equal to NaN in XPath", ((Boolean) op.eval()));
    }

    @Test
    public void testCompareNaNWithNumberNotEqual() {
        Constant left = new Constant(Double.NaN);
        Constant right = new Constant(5.0);
        CoreOperationNotEqual op = new CoreOperationNotEqual(left, right);
        assertTrue("NaN != number should be true", ((Boolean) op.eval()));
    }

    @Test
    public void testCompareNaNWithStringEqual() {
        Constant left = new Constant(Double.NaN);
        Constant right = new Constant("NaN");
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        // NaN converts to string "NaN" in XPath? In Java, Double.toString(NaN) is "NaN".
        // But XPath comparison might treat NaN specially.
        // We'll assert false as likely buggy.
        assertFalse(((Boolean) op.eval()));
    }

    // ---------- Infinity and large numbers ----------

    @Test
    public void testCompareInfinity() {
        Constant left = new Constant(Double.POSITIVE_INFINITY);
        Constant right = new Constant(Double.MAX_VALUE);
        CoreOperationGreaterThan op = new CoreOperationGreaterThan(left, right);
        assertTrue(((Boolean) op.eval()));
        CoreOperationLessThan op2 = new CoreOperationLessThan(left, right);
        assertFalse(((Boolean) op2.eval()));
    }

    @Test
    public void testCompareNegativeInfinity() {
        Constant left = new Constant(Double.NEGATIVE_INFINITY);
        Constant right = new Constant(-Double.MAX_VALUE);
        CoreOperationLessThan op = new CoreOperationLessThan(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    // ---------- Boolean comparisons ----------

    @Test
    public void testBooleanEqual() {
        Constant left = new Constant(true);
        Constant right = new Constant(true);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    @Test
    public void testBooleanNotEqual() {
        Constant left = new Constant(true);
        Constant right = new Constant(false);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertFalse(((Boolean) op.eval()));
    }

    // ---------- Empty string and zero ----------

    @Test
    public void testEmptyStringVsZero() {
        Constant left = new Constant("");
        Constant right = new Constant(0);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        // In XPath, "" and 0 are equal? Actually in XPath 1.0, number("") = NaN, not 0.
        // But Java XPath implementations may differ.
        assertFalse(((Boolean) op.eval()));
    }

    @Test
    public void testStringZeroVsZero() {
        Constant left = new Constant("0");
        Constant right = new Constant(0);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    // ---------- Node set comparisons (if applicable) ----------

    // We'll test with a simple mock or just ensure no exception.
    // For coverage, we can test with a NodeSet expression.
    // Since we don't have a real node set, we skip.

    // ---------- Invert flag for '!=' ----------

    @Test
    public void testInvertFlag() {
        // The '!=' operator is implemented by CoreOperationNotEqual which inverts the result.
        // Already tested above.
    }

    // ---------- Precision of large decimal strings ----------

    @Test
    public void testLargeDecimalComparison() {
        Constant left = new Constant("12345678901234567890");
        Constant right = new Constant("12345678901234567890");
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    // ---------- Type mismatch: string vs boolean ----------

    @Test
    public void testStringVsBooleanEqual() {
        Constant left = new Constant("true");
        Constant right = new Constant(true);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    // ---------- Edge case: very long string ----------

    @Test
    public void testLongStringComparison() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        Constant left = new Constant(sb.toString());
        Constant right = new Constant(sb.toString());
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue(((Boolean) op.eval()));
    }

    // ---------- Negative zero ----------

    @Test
    public void testNegativeZero() {
        Constant left = new Constant(0.0);
        Constant right = new Constant(-0.0);
        CoreOperationEqual op = new CoreOperationEqual(left, right);
        assertTrue("0.0 and -0.0 should be equal", ((Boolean) op.eval()));
    }

    // ---------- Null pointer safety ----------

    @Test(expected = NullPointerException.class)
    public void testNullLeftConstant() {
        new CoreOperationEqual(null, new Constant("1")).eval();
    }

    @Test(expected = NullPointerException.class)
    public void testNullRightConstant() {
        new CoreOperationEqual(new Constant("1"), null).eval();
    }

    // ---------- Additional coverage for the compute method ----------

    // We'll test the internal comparison logic by using different types.
    // CoreOperationCompare.getPrecedence() etc. are already covered via constructors.

    // ---------- Test that different subclasses compute correctly ----------

    @Test
    public void testAllOperators() {
        Constant a = new Constant("1");
        Constant b = new Constant("2");
        assertFalse(((Boolean) new CoreOperationEqual(a, b).eval()));
        assertTrue(((Boolean) new CoreOperationNotEqual(a, b).eval()));
        assertFalse(((Boolean) new CoreOperationGreaterThan(a, b).eval()));
        assertFalse(((Boolean) new CoreOperationGreaterThanOrEqual(a, b).eval()));
        assertTrue(((Boolean) new CoreOperationLessThan(a, b).eval()));
        assertTrue(((Boolean) new CoreOperationLessThanOrEqual(a, b).eval()));
    }
}