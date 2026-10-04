package expression;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for the Expression class.
 * Designed to achieve maximum line and branch coverage and to detect
 * common arithmetic, edge-case, and null-related faults.
 * 
 * Assumption: Expression class provides at least:
 *   - setExpression(String expr)
 *   - double evaluate()
 *   - possibly getValue() for binary expression nodes (optional)
 *
 * If the actual API differs, adapt accordingly.
 */
public class ExpressionTest {

    private Expression expr;

    @Before
    public void setUp() {
        expr = new Expression();
    }

    // ---------- Basic arithmetic ----------
    @Test
    public void testEvaluateSimpleAddition() {
        expr.setExpression("2+3");
        assertEquals(5.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateSimpleSubtraction() {
        expr.setExpression("10-4");
        assertEquals(6.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateSimpleMultiplication() {
        expr.setExpression("3*4");
        assertEquals(12.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateSimpleDivision() {
        expr.setExpression("15/3");
        assertEquals(5.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateOperatorPrecedence() {
        expr.setExpression("2+3*4");
        assertEquals(14.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateParentheses() {
        expr.setExpression("(2+3)*4");
        assertEquals(20.0, expr.evaluate(), 1e-12);
    }

    // ---------- Edge Cases ----------
    @Test
    public void testEvaluateWithZero() {
        expr.setExpression("0+0");
        assertEquals(0.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateNegativeNumbers() {
        expr.setExpression("-5+3");
        assertEquals(-2.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateLargeNumbers() {
        expr.setExpression("1e308*10");
        double result = expr.evaluate();
        assertTrue(Double.isInfinite(result)); // overflow to Infinity
    }

    @Test
    public void testEvaluateVerySmallNumbers() {
        expr.setExpression("1e-308/10");
        double result = expr.evaluate();
        assertEquals(0.0, result, 1e-320); // underflow to zero
    }

    // ---------- Division by zero ----------
    @Test
    public void testEvaluateDivisionByZeroPositive() {
        expr.setExpression("1/0");
        double result = expr.evaluate();
        assertTrue("Expected Infinity for 1/0", Double.isInfinite(result));
    }

    @Test
    public void testEvaluateDivisionByZeroNegative() {
        expr.setExpression("-1/0");
        double result = expr.evaluate();
        assertTrue("Expected -Infinity for -1/0", result == Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testEvaluateDivisionByZeroZero() {
        expr.setExpression("0/0");
        double result = expr.evaluate();
        assertTrue("Expected NaN for 0/0", Double.isNaN(result));
    }

    // ---------- Invalid expressions ----------
    @Test(expected = RuntimeException.class)
    public void testEvaluateNullExpression() {
        expr.setExpression(null);
        expr.evaluate(); // should throw RuntimeException or similar
    }

    @Test(expected = RuntimeException.class)
    public void testEvaluateEmptyExpression() {
        expr.setExpression("");
        expr.evaluate();
    }

    @Test(expected = RuntimeException.class)
    public void testEvaluateMalformedExpression() {
        expr.setExpression("2++3");
        expr.evaluate();
    }

    @Test(expected = RuntimeException.class)
    public void testEvaluateUnmatchedParenthesis() {
        expr.setExpression("(2+3");
        expr.evaluate();
    }

    @Test(expected = RuntimeException.class)
    public void testEvaluateInvalidCharacter() {
        expr.setExpression("2+a");
        expr.evaluate();
    }

    // ---------- Repeated evaluations ----------
    @Test
    public void testEvaluateReuseExpression() {
        expr.setExpression("4+5");
        assertEquals(9.0, expr.evaluate(), 1e-12);
        // change expression and re-evaluate
        expr.setExpression("10*2");
        assertEquals(20.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateExpressionReset() {
        expr.setExpression("1+1");
        double first = expr.evaluate();
        expr.setExpression("2+2");
        double second = expr.evaluate();
        assertEquals(2.0, first, 1e-12);
        assertEquals(4.0, second, 1e-12);
    }

    // ---------- Floating point precision ----------
    @Test
    public void testEvaluateFloatingPoint() {
        expr.setExpression("0.1+0.2");
        assertEquals(0.3, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateExponentNotation() {
        expr.setExpression("1e2+3e0");
        assertEquals(103.0, expr.evaluate(), 1e-12);
    }

    // ---------- Boolean operators (if supported) ----------
    // (If Expression supports <, >, ==, etc., add tests accordingly)

    // ---------- Additional branch coverage ----------
    @Test
    public void testEvaluateUnaryMinus() {
        expr.setExpression("-10");
        assertEquals(-10.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateUnaryMinusComplex() {
        expr.setExpression("-(-5)");
        assertEquals(5.0, expr.evaluate(), 1e-12);
    }

    @Test
    public void testEvaluateTernaryOperator() {
        // Hypothetical ternary: (2>1)?3:4
        expr.setExpression("(2>1)?3:4");
        double result = expr.evaluate();
        assertEquals(3.0, result, 1e-12);
    }

    // ---------- Memory / stress ----------
    @Test(timeout = 1000)
    public void testEvaluateDeepNesting() {
        StringBuilder sb = new StringBuilder();
        sb.append("1");
        for (int i = 0; i < 100; i++) {
            sb.insert(0, "(");
            sb.append("+1)");
        }
        expr.setExpression(sb.toString());
        // The deep nesting should not cause stack overflow (if parser is robust)
        expr.evaluate(); // at least it does not crash
    }

    // ---------- Null / empty argument for setExpression ----------
    @Test(expected = IllegalArgumentException.class)
    public void testSetExpressionNull() {
        expr.setExpression(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetExpressionEmpty() {
        expr.setExpression("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetExpressionWhitespace() {
        expr.setExpression("   ");
    }

    // ---------- Exception message check ----------
    @Test
    public void testEvaluateDivisionByZeroErrorMessage() {
        expr.setExpression("1/0");
        try {
            expr.evaluate();
        } catch (ArithmeticException e) {
            assertTrue(e.getMessage().contains("division by zero"));
        } catch (RuntimeException e) {
            // Accept any runtime exception with informative message
            assertNotNull(e.getMessage());
        }
    }
}