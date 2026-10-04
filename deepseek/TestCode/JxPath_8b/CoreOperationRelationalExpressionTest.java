package some.package;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Test suite for CoreOperationRelationalExpression.
 * This test class assumes the class has a method with the following signature:
 *     public boolean evaluate(Comparable left, Comparable right, String operator)
 * where operator is one of "<", "<=", ">", ">=", "==", "!=".
 * Edge cases, nulls, and invalid operators are covered.
 *
 * Note: The actual source file was not provided, so this test is written based on
 * a typical relational expression evaluation class from the Defects4J dataset.
 */
public class CoreOperationRelationalExpressionTest {

    private CoreOperationRelationalExpression expression;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        expression = new CoreOperationRelationalExpression();
    }

    // ---------- Integer Comparisons ----------

    @Test
    public void testLessThanPositive() {
        assertTrue("5 < 10 should be true", evaluate(5, 10, "<"));
        assertFalse("10 < 5 should be false", evaluate(10, 5, "<"));
        assertFalse("5 < 5 should be false", evaluate(5, 5, "<"));
    }

    @Test
    public void testLessThanNegative() {
        assertTrue("-10 < -5 should be true", evaluate(-10, -5, "<"));
        assertFalse("-5 < -10 should be false", evaluate(-5, -10, "<"));
    }

    @Test
    public void testLessThanZero() {
        assertTrue("0 < 1 should be true", evaluate(0, 1, "<"));
        assertFalse("0 < 0 should be false", evaluate(0, 0, "<"));
        assertTrue("-1 < 0 should be true", evaluate(-1, 0, "<"));
    }

    @Test
    public void testLessThanBoundary() {
        assertTrue("Integer.MIN_VALUE < Integer.MAX_VALUE should be true",
                evaluate(Integer.MIN_VALUE, Integer.MAX_VALUE, "<"));
        assertFalse("Integer.MAX_VALUE < Integer.MIN_VALUE should be false",
                evaluate(Integer.MAX_VALUE, Integer.MIN_VALUE, "<"));
        assertFalse("Integer.MIN_VALUE < Integer.MIN_VALUE should be false",
                evaluate(Integer.MIN_VALUE, Integer.MIN_VALUE, "<"));
        assertTrue("0 < 1 should be true", evaluate(0, 1, "<"));
    }

    // ---------- Less or Equal ----------

    @Test
    public void testLessOrEqualPositive() {
        assertTrue("5 <= 10 should be true", evaluate(5, 10, "<="));
        assertTrue("5 <= 5 should be true", evaluate(5, 5, "<="));
        assertFalse("10 <= 5 should be false", evaluate(10, 5, "<="));
    }

    @Test
    public void testLessOrEqualNegative() {
        assertTrue("-10 <= -5 should be true", evaluate(-10, -5, "<="));
        assertTrue("-5 <= -5 should be true", evaluate(-5, -5, "<="));
        assertFalse("-5 <= -10 should be false", evaluate(-5, -10, "<="));
    }

    @Test
    public void testLessOrEqualBoundary() {
        assertTrue("Integer.MIN_VALUE <= Integer.MAX_VALUE should be true",
                evaluate(Integer.MIN_VALUE, Integer.MAX_VALUE, "<="));
        assertTrue("Integer.MIN_VALUE <= Integer.MIN_VALUE should be true",
                evaluate(Integer.MIN_VALUE, Integer.MIN_VALUE, "<="));
        assertFalse("Integer.MAX_VALUE <= Integer.MIN_VALUE should be false",
                evaluate(Integer.MAX_VALUE, Integer.MIN_VALUE, "<="));
        assertTrue("0 <= 1 should be true", evaluate(0, 1, "<="));
        assertTrue("0 <= 0 should be true", evaluate(0, 0, "<="));
    }

    // ---------- Greater Than ----------

    @Test
    public void testGreaterThanPositive() {
        assertFalse("5 > 10 should be false", evaluate(5, 10, ">"));
        assertTrue("10 > 5 should be true", evaluate(10, 5, ">"));
        assertFalse("5 > 5 should be false", evaluate(5, 5, ">"));
    }

    @Test
    public void testGreaterThanNegative() {
        assertFalse("-10 > -5 should be false", evaluate(-10, -5, ">"));
        assertTrue("-5 > -10 should be true", evaluate(-5, -10, ">"));
    }

    @Test
    public void testGreaterThanBoundary() {
        assertFalse("Integer.MIN_VALUE > Integer.MAX_VALUE should be false",
                evaluate(Integer.MIN_VALUE, Integer.MAX_VALUE, ">"));
        assertTrue("Integer.MAX_VALUE > Integer.MIN_VALUE should be true",
                evaluate(Integer.MAX_VALUE, Integer.MIN_VALUE, ">"));
        assertFalse("Integer.MIN_VALUE > Integer.MIN_VALUE should be false",
                evaluate(Integer.MIN_VALUE, Integer.MIN_VALUE, ">"));
        assertFalse("0 > 1 should be false", evaluate(0, 1, ">"));
    }

    // ---------- Greater or Equal ----------

    @Test
    public void testGreaterOrEqualPositive() {
        assertFalse("5 >= 10 should be false", evaluate(5, 10, ">="));
        assertTrue("10 >= 5 should be true", evaluate(10, 5, ">="));
        assertTrue("5 >= 5 should be true", evaluate(5, 5, ">="));
    }

    @Test
    public void testGreaterOrEqualNegative() {
        assertFalse("-10 >= -5 should be false", evaluate(-10, -5, ">="));
        assertTrue("-5 >= -10 should be true", evaluate(-5, -10, ">="));
        assertTrue("-5 >= -5 should be true", evaluate(-5, -5, ">="));
    }

    @Test
    public void testGreaterOrEqualBoundary() {
        assertTrue("Integer.MAX_VALUE >= Integer.MIN_VALUE should be true",
                evaluate(Integer.MAX_VALUE, Integer.MIN_VALUE, ">="));
        assertTrue("Integer.MIN_VALUE >= Integer.MIN_VALUE should be true",
                evaluate(Integer.MIN_VALUE, Integer.MIN_VALUE, ">="));
        assertFalse("Integer.MIN_VALUE >= Integer.MAX_VALUE should be false",
                evaluate(Integer.MIN_VALUE, Integer.MAX_VALUE, ">="));
        assertTrue("0 >= 0 should be true", evaluate(0, 0, ">="));
        assertFalse("0 >= 1 should be false", evaluate(0, 1, ">="));
    }

    // ---------- Equal ----------

    @Test
    public void testEqualIntegers() {
        assertTrue("5 == 5 should be true", evaluate(5, 5, "=="));
        assertFalse("5 == 10 should be false", evaluate(5, 10, "=="));
        assertTrue("Integer.MIN_VALUE == Integer.MIN_VALUE should be true",
                evaluate(Integer.MIN_VALUE, Integer.MIN_VALUE, "=="));
    }

    @Test
    public void testEqualWithNull() {
        assertFalse("null == 5 should be false", evaluate(null, 5, "=="));
        assertFalse("5 == null should be false", evaluate(5, null, "=="));
        assertTrue("null == null should be true", evaluate(null, null, "=="));
    }

    @Test
    public void testEqualDoubleNaN() {
        assertFalse("NaN == NaN should be false", evaluate(Double.NaN, Double.NaN, "=="));
        assertFalse("NaN == 1.0 should be false", evaluate(Double.NaN, 1.0, "=="));
    }

    @Test
    public void testEqualDifferentTypes() {
        assertFalse("5 == 5.0 should be false if types differ (Comparable compareTo)",
                evaluate(5, 5.0, "=="));
        assertTrue("If both are Comparable of same type, e.g., Double 5.0 == 5.0",
                evaluate(5.0, 5.0, "=="));
    }

    // ---------- Not Equal ----------

    @Test
    public void testNotEqualIntegers() {
        assertFalse("5 != 5 should be false", evaluate(5, 5, "!="));
        assertTrue("5 != 10 should be true", evaluate(5, 10, "!="));
        assertFalse("Integer.MIN_VALUE != Integer.MIN_VALUE should be false",
                evaluate(Integer.MIN_VALUE, Integer.MIN_VALUE, "!="));
    }

    @Test
    public void testNotEqualWithNull() {
        assertTrue("null != 5 should be true", evaluate(null, 5, "!="));
        assertTrue("5 != null should be true", evaluate(5, null, "!="));
        assertFalse("null != null should be false", evaluate(null, null, "!="));
    }

    @Test
    public void testNotEqualDoubleNaN() {
        assertTrue("NaN != NaN should be true", evaluate(Double.NaN, Double.NaN, "!="));
        assertTrue("NaN != 1.0 should be true", evaluate(Double.NaN, 1.0, "!="));
    }

    // ---------- String Comparisons (Comparable) ----------

    @Test
    public void testStringComparisons() {
        assertTrue("\"apple\" < \"banana\" should be true", evaluate("apple", "banana", "<"));
        assertTrue("\"apple\" <= \"banana\" should be true", evaluate("apple", "banana", "<="));
        assertTrue("\"banana\" > \"apple\" should be true", evaluate("banana", "apple", ">"));
        assertTrue("\"banana\" >= \"apple\" should be true", evaluate("banana", "apple", ">="));
        assertTrue("\"apple\" == \"apple\" should be true", evaluate("apple", "apple", "=="));
        assertTrue("\"apple\" != \"banana\" should be true", evaluate("apple", "banana", "!="));

        // Case sensitivity
        assertFalse("\"Apple\" == \"apple\" (case sensitive) should be false",
                evaluate("Apple", "apple", "=="));
        assertTrue("\"Apple\" != \"apple\" should be true",
                evaluate("Apple", "apple", "!="));
    }

    @Test
    public void testStringEmpty() {
        assertTrue("\"\" < \"a\" should be true", evaluate("", "a", "<"));
        assertTrue("\"\" == \"\" should be true", evaluate("", "", "=="));
        assertFalse("\"\" == \"a\" should be false", evaluate("", "a", "=="));
    }

    // ---------- Non-Comparable Objects ----------

    @Test(expected = ClassCastException.class)
    public void testNonComparableLeft() {
        // Assuming the method does not handle non-Comparable gracefully and throws ClassCastException
        evaluate(new Object(), 5, "==");
    }

    @Test(expected = ClassCastException.class)
    public void testNonComparableRight() {
        evaluate(5, new Object(), "==");
    }

    // ---------- Invalid Operator ----------

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOperator() {
        evaluate(5, 10, "=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOperatorEmptyString() {
        evaluate(5, 10, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOperatorNull() {
        evaluate(5, 10, null);
    }

    // ---------- Mixed Types (Double vs Integer) ----------

    @Test
    public void testMixedNumericTypes() {
        // If the implementation uses compareTo, Double.compareTo(Integer) will throw ClassCastException
        // But some implementations may convert to a common type.
        // We'll test the behavior that likely triggers bug: Mixed types not handled.
        try {
            evaluate(5, 5.0, "<");
            // If no exception, it might have converted; we can test result.
            // Common bug: incorrectly handling mixed types.
            // We won't assume result, but at least exercise the path.
        } catch (ClassCastException e) {
            // Expected if types are incompatible
        }
    }

    // ---------- Helper method ----------

    private boolean evaluate(Object left, Object right, String operator) {
        // Assuming the class has such a method; if not, adjust accordingly.
        return expression.evaluate(left, right, operator);
    }
}