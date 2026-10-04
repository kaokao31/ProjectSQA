package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.PeepholeFoldConstants;
import com.google.javascript.rhino.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for PeepholeFoldConstants, focusing on constant folding
 * with particular attention to NaN and undefined edge cases.
 */
@RunWith(JUnit4.class)
public class PeepholeFoldConstantsTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new PeepholeFoldConstants(compiler);
  }

  // ------------------------------------------------------------------
  // Arithmetic and numeric constants
  // ------------------------------------------------------------------

  @Test
  public void testArithmeticBasic() {
    test("1 + 2", "3");
    test("3 - 4", "-1");
    test("5 * 6", "30");
    test("10 / 2", "5");
    test("7 % 3", "1");
    test("-8", "-8");
    test("+9", "9");
  }

  @Test
  public void testArithmeticWithInfinity() {
    test("Infinity + 1", "Infinity");
    test("Infinity - Infinity", "NaN");
    test("0 * Infinity", "NaN");
    test("Infinity / Infinity", "NaN");
    test("Infinity % 1", "NaN");
  }

  @Test
  public void testArithmeticWithNaN() {
    test("NaN + 1", "NaN");
    test("NaN - NaN", "NaN");
    test("NaN * 0", "NaN");
    test("NaN / 1", "NaN");
    test("NaN % 2", "NaN");
    test("-NaN", "NaN");
  }

  // ------------------------------------------------------------------
  // Comparisons with NaN (critical for bug #23)
  // ------------------------------------------------------------------

  @Test
  public void testNaNComparisons() {
    // According to IEEE 754, all comparisons with NaN are false,
    // except NaN != which is true.
    test("NaN == NaN", "false");
    test("NaN === NaN", "false");
    test("NaN != NaN", "true");
    test("NaN !== NaN", "true");
    test("NaN < NaN", "false");
    test("NaN > NaN", "false");
    test("NaN <= NaN", "false");
    test("NaN >= NaN", "false");
    test("1 < NaN", "false");
    test("NaN < 1", "false");
    test("1 > NaN", "false");
    test("NaN > 1", "false");
    test("1 <= NaN", "false");
    test("NaN <= 1", "false");
    test("1 >= NaN", "false");
    test("NaN >= 1", "false");
  }

  // ------------------------------------------------------------------
  // Comparisons with undefined
  // ------------------------------------------------------------------

  @Test
  public void testUndefinedComparisons() {
    test("undefined == undefined", "true");
    test("undefined === undefined", "true");
    test("undefined != undefined", "false");
    test("undefined !== undefined", "false");
    test("undefined < undefined", "false");
    test("undefined > undefined", "false");
    test("undefined <= undefined", "false");
    test("undefined >= undefined", "false");
    test("1 < undefined", "false");
    test("undefined < 1", "false");
  }

  // ------------------------------------------------------------------
  // Logical operators
  // ------------------------------------------------------------------

  @Test
  public void testLogicalAnd() {
    test("true && false", "false");
    test("false && true", "false");
    test("true && true", "true");
    test("false && false", "false");
  }

  @Test
  public void testLogicalOr() {
    test("true || false", "true");
    test("false || true", "true");
    test("true || true", "true");
    test("false || false", "false");
  }

  @Test
  public void testLogicalNot() {
    test("!true", "false");
    test("!false", "true");
    test("!0", "true");
    test("!1", "false");
  }

  // ------------------------------------------------------------------
  // Bitwise operators
  // ------------------------------------------------------------------

  @Test
  public void testBitwiseAnd() {
    test("3 & 5", "1");
    test("-1 & 0", "0");
  }

  @Test
  public void testBitwiseOr() {
    test("3 | 5", "7");
    test("-1 | 0", "-1");
  }

  @Test
  public void testBitwiseXor() {
    test("3 ^ 5", "6");
  }

  @Test
  public void testShiftOperators() {
    test("5 << 2", "20");
    test("5 >> 2", "1");
    test("5 >>> 2", "1");
    test("-5 >>> 2", "1073741822");
  }

  @Test
  public void testBitwiseNot() {
    test("~0", "-1");
    test("~1", "-2");
  }

  // ------------------------------------------------------------------
  // String concatenation
  // ------------------------------------------------------------------

  @Test
  public void testStringConcat() {
    test("'a' + 'b'", "\"ab\"");
    test("1 + 'a'", "\"1a\"");
    test("'a' + 1", "\"a1\"");
  }

  // ------------------------------------------------------------------
  // typeof operator
  // ------------------------------------------------------------------

  @Test
  public void testTypeof() {
    test("typeof 1", "\"number\"");
    test("typeof 'a'", "\"string\"");
    test("typeof true", "\"boolean\"");
    test("typeof undefined", "\"undefined\"");
    test("typeof null", "\"object\"");
    test("typeof NaN", "\"number\"");
    test("typeof Infinity", "\"number\"");
  }

  // ------------------------------------------------------------------
  // delete operator (not foldable but should not crash)
  // ------------------------------------------------------------------

  @Test
  public void testDelete() {
    testSame("delete x");
    testSame("delete (1)");
  }

  // ------------------------------------------------------------------
  // Unary plus/minus on non-numeric
  // ------------------------------------------------------------------

  @Test
  public void testUnaryPlusMinus() {
    test("+'1'", "1");
    test("-'1'", "-1");
    test("+true", "1");
    test("-true", "-1");
    test("+null", "0");
    test("-null", "-0");
  }

  // ------------------------------------------------------------------
  // Compound expressions and nested folds
  // ------------------------------------------------------------------

  @Test
  public void testNestedArithmetic() {
    test("(1 + 2) * 3", "9");
    test("5 + (3 - 1)", "7");
  }

  @Test
  public void testWithParentheses() {
    test("(1)", "1");
    test("((2))", "2");
  }

  // ------------------------------------------------------------------
  // Edge cases: null, boolean conversion
  // ------------------------------------------------------------------

  @Test
  public void testNullComparisons() {
    test("null == null", "true");
    test("null === null", "true");
    test("null != null", "false");
    test("null !== null", "false");
    test("null < null", "false");
    test("null > null", "false");
    test("null <= null", "true");
    test("null >= null", "true");
  }

  @Test
  public void testBooleanConversion() {
    test("!!1", "true");
    test("!!0", "false");
    test("!!'a'", "true");
    test("!!''", "false");
  }

  // ------------------------------------------------------------------
  // Folding of NaN in more complex expressions
  // ------------------------------------------------------------------

  @Test
  public void testNaNWithOtherOperators() {
    test("NaN && true", "NaN");
    test("true && NaN", "NaN");
    test("NaN || false", "NaN");
    test("false || NaN", "NaN");
    test("!NaN", "true");
  }

  // ------------------------------------------------------------------
  // Folding of Infinity in comparisons
  // ------------------------------------------------------------------

  @Test
  public void testInfinityComparisons() {
    test("Infinity == Infinity", "true");
    test("Infinity === Infinity", "true");
    test("Infinity != Infinity", "false");
    test("Infinity !== Infinity", "false");
    test("Infinity < Infinity", "false");
    test("Infinity > Infinity", "false");
    test("Infinity <= Infinity", "true");
    test("Infinity >= Infinity", "true");
    test("1 < Infinity", "true");
    test("Infinity < 1", "false");
  }

  // ------------------------------------------------------------------
  // Tests that should not fold (side effects or runtime calls)
  // ------------------------------------------------------------------

  @Test
  public void testNoFoldForSideEffects() {
    testSame("a++");
    testSame("a = 1");
    testSame("a()");
  }

  // ------------------------------------------------------------------
  // Compound assignment operators (usually not folded)
  // ------------------------------------------------------------------

  @Test
  public void testCompoundAssignment() {
    testSame("a += 1");
    testSame("a -= 1");
    testSame("a *= 1");
    testSame("a /= 1");
    testSame("a %= 1");
  }

  // ------------------------------------------------------------------
  // Folding of string comparisons (lexicographic)
  // ------------------------------------------------------------------

  @Test
  public void testStringComparisons() {
    test("'a' < 'b'", "true");
    test("'a' > 'b'", "false");
    test("'a' == 'b'", "false");
    test("'a' != 'b'", "true");
    test("'a' === 'a'", "true");
  }

  // ------------------------------------------------------------------
  // Folding of numeric comparisons with zero
  // ------------------------------------------------------------------

  @Test
  public void testZeroComparisons() {
    test("0 < 0", "false");
    test("0 > 0", "false");
    test("0 <= 0", "true");
    test("0 >= 0", "true");
    test("0 == 0", "true");
    test("0 != 0", "false");
  }

  // ------------------------------------------------------------------
  // Folding of negative zero
  // ------------------------------------------------------------------

  @Test
  public void testNegativeZero() {
    test("-0", "-0");
    test("1 / -0", "-Infinity");
    test("0 / -0", "NaN");
  }

  // ------------------------------------------------------------------
  // Folding of NaN from division by zero
  // ------------------------------------------------------------------

  @Test
  public void testDivisionByZero() {
    test("1 / 0", "Infinity");
    test("-1 / 0", "-Infinity");
    test("0 / 0", "NaN");
  }

  // ------------------------------------------------------------------
  // Folding of Math.* constants used as globals
  // (PeepholeFoldConstants may fold some Math properties)
  // ------------------------------------------------------------------

  @Test
  public void testMathConstants() {
    // Note: Math.E, Math.PI, etc. are not folded in source code
    // unless the pass is configured to fold them.
    // Here we test that they are not incorrectly folded.
    testSame("Math.E");
    testSame("Math.PI");
    testSame("Math.LN2");
  }

  // ------------------------------------------------------------------
  // Folding of typeof on undefined/null
  // ------------------------------------------------------------------

  @Test
  public void testTypeofSpecialValues() {
    test("typeof undefined", "\"undefined\"");
    test("typeof null", "\"object\"");
    test("typeof NaN", "\"number\"");
  }

  // ------------------------------------------------------------------
  // Folding of void operator
  // ------------------------------------------------------------------

  @Test
  public void testVoid() {
    test("void 0", "undefined");
    test("void (1 + 1)", "undefined");
  }

  // ------------------------------------------------------------------
  // Folding of comma operator (should not fold)
  // ------------------------------------------------------------------

  @Test
  public void testComma() {
    testSame("(1, 2)");
  }

  // ------------------------------------------------------------------
  // Folding of conversion to number
  // ------------------------------------------------------------------

  @Test
  public void testUnaryToNumber() {
    test("+ '123'", "123");
    test("+ '0x10'", "16");
    test("+ 'abc'", "NaN");
  }
}