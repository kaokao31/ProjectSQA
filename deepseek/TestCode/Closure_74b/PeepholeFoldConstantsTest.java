package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test suite for PeepholeFoldConstants, aiming for maximum coverage and fault detection.
 * Includes tests for bug #74 where typeof/bitwise folding may be incorrect.
 */
@RunWith(JUnit4.class)
public class PeepholeFoldConstantsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new PeepholeFoldConstants(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1; // Run only once to avoid masking bugs
  }

  @Test
  public void testFoldAddNumbers() {
    test("1 + 2", "3");
    test("x + 0", "x + 0");
    test("0 + x", "0 + x");
    test("x + 1", "x + 1");
    test("1 + x", "1 + x");
    test("Infinity + Infinity", "Infinity + Infinity");
    test("1 + 2 + 3", "6");
  }

  @Test
  public void testFoldSubtractNumbers() {
    test("5 - 3", "2");
    test("x - 0", "x");
    test("0 - x", "-x");
    test("x - x", "0");
    test("1 - 2", "-1");
  }

  @Test
  public void testFoldMultiplyNumbers() {
    test("2 * 3", "6");
    test("0 * x", "0");
    test("x * 0", "0");
    test("2 * x", "2 * x");
    test("x * 2", "x * 2");
    test("Infinity * 0", "NaN");
  }

  @Test
  public void testFoldDivideNumbers() {
    test("6 / 2", "3");
    test("0 / 5", "0");
    test("5 / 0", "Infinity");
    test("0 / 0", "NaN");
    test("x / 1", "x");
    test("x / 0", "x / 0");
  }

  @Test
  public void testFoldModuloNumbers() {
    test("10 % 3", "1");
    test("x % 1", "0");
    test("5 % 0", "NaN");
    test("0 % 5", "0");
  }

  @Test
  public void testFoldBitwiseAnd() {
    test("3 & 5", "1");
    test("x & 0", "0");
    test("0 & x", "0");
    test("x & -1", "x");
    test("-1 & x", "x");
    test("x & x", "x");
    // Bug #74 related: left shift by 0 with side effects should not be removed
    testSame("(x = 1) << 0");
    testSame("(x = 1) >> 0");
    testSame("(x = 1) >>> 0");
  }

  @Test
  public void testFoldBitwiseOr() {
    test("3 | 5", "7");
    test("x | 0", "x");
    test("0 | x", "x");
    test("x | -1", "-1");
    test("-1 | x", "-1");
    // Bug #74 might involve incorrect folding of | with side effects
    testSame("(x = 1) | 0");
  }

  @Test
  public void testFoldBitwiseXor() {
    test("3 ^ 5", "6");
    test("x ^ 0", "x");
    test("0 ^ x", "x");
    test("x ^ x", "0");
  }

  @Test
  public void testFoldLeftShift() {
    test("5 << 2", "20");
    test("x << 0", "x");
    test("0 << x", "0");
    test("x << 32", "x << 32");
    // Side effects must be preserved
    testSame("(x = 1) << 0");
  }

  @Test
  public void testFoldRightShift() {
    test("20 >> 2", "5");
    test("x >> 0", "x");
    test("0 >> x", "0");
    testSame("(x = 1) >> 0");
  }

  @Test
  public void testFoldUnsignedRightShift() {
    test("20 >>> 2", "5");
    test("x >>> 0", "x");
    test("0 >>> x", "0");
    testSame("(x = 1) >>> 0");
  }

  @Test
  public void testFoldLogicalNot() {
    test("!true", "false");
    test("!false", "true");
    test("!x", "!x");
    test("!!x", "!!x");
    test("!!true", "true");
  }

  @Test
  public void testFoldLogicalAnd() {
    test("true && x", "x");
    test("false && x", "false");
    test("x && true", "x && true");
    test("x && false", "x && false");
    // Side effects in left operand must be preserved
    testSame("(x = 1) && y");
  }

  @Test
  public void testFoldLogicalOr() {
    test("true || x", "true");
    test("false || x", "x");
    test("x || true", "x || true");
    test("x || false", "x || false");
    testSame("(x = 1) || y");
  }

  @Test
  public void testFoldComparison() {
    test("1 < 2", "true");
    test("1 > 2", "false");
    test("1 <= 1", "true");
    test("2 >= 3", "false");
    test("1 == 1", "true");
    test("1 != 1", "false");
    test("null == undefined", "true");
    test("null === undefined", "false");
  }

  @Test
  public void testFoldTypeof() {
    test("typeof 1", "\"number\"");
    test("typeof true", "\"boolean\"");
    test("typeof \"string\"", "\"string\"");
    test("typeof undefined", "\"undefined\"");
    test("typeof null", "\"object\"");
    test("typeof x", "typeof x");
    // Bug #74: typeof on a variable that might be undefined should not fold away
    testSame("typeof x");
    // Folding when operand is known to be defined
    test("typeof (x = 1)", "\"number\"");
  }

  @Test
  public void testFoldStringConcat() {
    test("\"a\" + \"b\"", "\"ab\"");
    test("\"a\" + 1", "\"a1\"");
    test("1 + \"a\"", "\"1a\"");
    test("\"a\" + x", "\"a\" + x");
  }

  @Test
  public void testFoldArrayLiterals() {
    test("[1, 2, 3]", "[1, 2, 3]");
    test("[]", "[]");
    test("[,1]", "[,1]");
  }

  @Test
  public void testFoldObjectLiterals() {
    test("({a: 1})", "({a: 1})");
    test("({})", "({})");
  }

  @Test
  public void testFoldUnaryOperators() {
    test("+5", "5");
    test("-5", "-5");
    test("~5", "-6");
    test("+x", "+x");
    test("-x", "-x");
    test("~x", "~x");
  }

  @Test
  public void testFoldConditional() {
    test("true ? 1 : 2", "1");
    test("false ? 1 : 2", "2");
    test("x ? 1 : 2", "x ? 1 : 2");
    test("x ? 1 : 2", "x ? 1 : 2");
  }

  @Test
  public void testFoldComma() {
    test("(1, 2)", "2");
    test("(x, 1)", "1");
    test("(x, y)", "y");
    testSame("(x = 1, y)");
  }

  @Test
  public void testFoldInOperator() {
    test("'a' in {}", "false");
    test("'toString' in {}", "true");
    test("x in y", "x in y");
  }

  @Test
  public void testFoldInstanceOf() {
    test("1 instanceof Number", "false");
    test("x instanceof y", "x instanceof y");
  }

  @Test
  public void testFoldDelete() {
    test("delete x", "delete x");
    test("delete x.prop", "delete x.prop");
  }

  @Test
  public void testFoldVoid() {
    test("void 0", "void 0");
    test("void x", "void x");
  }

  @Test
  public void testFoldExponentiation() {
    // Only if ES7 is supported (likely not in Closure Compiler), skip or test as same
    // Assume not, so just test that it's not folded incorrectly
    testSame("2 ** 3"); // should remain same
  }

  // Additional edge cases for maximum branch coverage
  @Test
  public void testFoldNaN() {
    test("NaN", "NaN");
    test("NaN + 1", "NaN + 1");
    test("NaN * 1", "NaN * 1");
  }

  @Test
  public void testFoldLargeNumbers() {
    test("2147483647 + 1", "2147483648");
    test("-2147483648 - 1", "-2147483649");
  }

  @Test
  public void testFoldStringsWithEscape() {
    test("\"\\n\" + \"\\t\"", "\"\\n\\t\"");
  }

  @Test
  public void testBug74TypeofSideEffect() {
    // Bug #74: typeof should not be removed when operand has side effects
    testSame("typeof (x = 1)");
    testSame("typeof (x++)");
  }

  @Test
  public void testBug74BitwiseSideEffect() {
    // Bug #74: bitwise operations with side effects should not be simplified incorrectly
    testSame("(x = 1) | (y = 2)");
    testSame("(x++) & (y--)");
    testSame("(x = 1) << (y = 2)");
    testSame("(x++) >> (y--)");
    testSame("(x = 1) >>> (y = 2)");
  }

  @Test
  public void testFoldNegativeZero() {
    test("-0", "-0");
    test("1 / -0", "-Infinity");
    test("1 / 0", "Infinity");
  }

  @Test
  public void testFoldStringToNumber() {
    test("+\"5\"", "5");
    test("-\"5\"", "-5");
    test("+\"abc\"", "NaN");
  }
}