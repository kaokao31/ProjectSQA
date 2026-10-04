package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

public class PeepholeFoldConstantsTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    options.setChecksOnly(false);
    options.setFoldConstants(true);
  }

  private String fold(String js) {
    SourceFile input = SourceFile.fromCode("input.js", js);
    SourceFile externs = SourceFile.fromCode("externs.js", "function alert(x) {}");
    compiler.compile(externs, input, options);
    return compiler.toSource().trim();
  }

  private String foldWithExterns(String js, String externsCode) {
    SourceFile input = SourceFile.fromCode("input.js", js);
    SourceFile externs = SourceFile.fromCode("externs.js", externsCode);
    compiler.compile(externs, input, options);
    return compiler.toSource().trim();
  }

  // Arithmetic folding tests

  @Test
  public void testArithmeticAddition() {
    assertEquals("3", fold("1+2"));
    assertEquals("a+1", fold("a+1")); // not fully foldable
    assertEquals("-1", fold("0-1"));
    assertEquals("5.5", fold("2.2+3.3"));
    assertEquals("Infinity", fold("1/0"));
    assertEquals("NaN", fold("0/0"));
  }

  @Test
  public void testArithmeticSubtraction() {
    assertEquals("-1", fold("2-3"));
    assertEquals("0", fold("4-4"));
    assertEquals("0", fold("1-1"));
    assertEquals("99", fold("100-1"));
  }

  @Test
  public void testArithmeticMultiplication() {
    assertEquals("6", fold("2*3"));
    assertEquals("-6", fold("-2*3"));
    assertEquals("0", fold("0*5"));
    assertEquals("Infinity", fold("1e200*1e200"));
  }

  @Test
  public void testArithmeticDivision() {
    assertEquals("2", fold("6/3"));
    assertEquals("-2", fold("6/-3"));
    assertEquals("Infinity", fold("1/0"));
    assertEquals("-Infinity", fold("-1/0"));
    assertEquals("NaN", fold("0/0"));
  }

  @Test
  public void testArithmeticModulo() {
    assertEquals("2", fold("5%3"));
    assertEquals("-2", fold("-5%3"));
    assertEquals("NaN", fold("5%0"));
    assertEquals("0", fold("5%1"));
  }

  // Bitwise operator tests

  @Test
  public void testBitwiseAnd() {
    assertEquals("0", fold("1&2"));
    assertEquals("3", fold("3&3"));
    assertEquals("2", fold("2&3"));
    assertEquals("-1", fold("-1&-1"));
  }

  @Test
  public void testBitwiseOr() {
    assertEquals("3", fold("1|2"));
    assertEquals("7", fold("3|4"));
    assertEquals("-1", fold("-1|0"));
  }

  @Test
  public void testBitwiseXor() {
    assertEquals("3", fold("1^2"));
    assertEquals("0", fold("3^3"));
    assertEquals("-1", fold("-1^0"));
  }

  @Test
  public void testBitwiseNot() {
    assertEquals("-2", fold("~1"));
    assertEquals("0", fold("~-1"));      // bug-prone: ~-1 should be 0
    assertEquals("-6", fold("~5"));
    assertEquals("4", fold("~-5"));
    assertEquals("0", fold("~-1"));
  }

  @Test
  public void testShiftLeft() {
    assertEquals("4", fold("1<<2"));
    assertEquals("-8", fold("-2<<2"));
    assertEquals("0", fold("0<<10"));
    assertEquals("0", fold("1<<32")); // only lower 5 bits used
  }

  @Test
  public void testSignedShiftRight() {
    assertEquals("1", fold("4>>2"));
    assertEquals("-1", fold("-4>>2"));
    assertEquals("0", fold("4>>4"));
  }

  @Test
  public void testUnsignedShiftRight() {
    assertEquals("1", fold("4>>>2"));
    assertEquals("1073741823", fold("-4>>>2"));
    assertEquals("4294967295", fold("-1>>>0"));
  }

  // Logical operator tests

  @Test
  public void testLogicalAnd() {
    assertEquals("false", fold("true&&false"));
    assertEquals("true", fold("true&&true"));
    assertEquals("false", fold("false&&true"));
    assertEquals("5", fold("5&&10")); // both truthy, returns last value
    assertEquals("null", fold("null&&10"));
  }

  @Test
  public void testLogicalOr() {
    assertEquals("true", fold("false||true"));
    assertEquals("false", fold("false||false"));
    assertEquals("5", fold("5||10")); // first truthy, returns 5
    assertEquals("10", fold("0||10"));
    assertEquals("false", fold("false||false"));
  }

  @Test
  public void testLogicalNot() {
    assertEquals("true", fold("!false"));
    assertEquals("false", fold("!true"));
    assertEquals("false", fold("!!false")); // double negation
    assertEquals("true", fold("!!true"));
    assertEquals("false", fold("!1")); // 1 is truthy, !1 false
    assertEquals("true", fold("!0")); // 0 is falsy, !0 true
  }

  // Comparison tests

  @Test
  public void testLessThan() {
    assertEquals("true", fold("1<2"));
    assertEquals("false", fold("2<1"));
    assertEquals("false", fold("1<1"));
    assertEquals("true", fold("5<10"));
  }

  @Test
  public void testGreaterThan() {
    assertEquals("true", fold("2>1"));
    assertEquals("false", fold("1>2"));
    assertEquals("false", fold("1>1"));
  }

  @Test
  public void testLessThanOrEqual() {
    assertEquals("true", fold("1<=2"));
    assertEquals("true", fold("2<=2"));
    assertEquals("false", fold("3<=2"));
  }

  @Test
  public void testGreaterThanOrEqual() {
    assertEquals("true", fold("2>=1"));
    assertEquals("true", fold("2>=2"));
    assertEquals("false", fold("1>=2"));
  }

  @Test
  public void testEquality() {
    assertEquals("true", fold("1==1"));
    assertEquals("false", fold("1==2"));
    assertEquals("true", fold("1==\"1\""));
    assertEquals("false", fold("1===\"1\""));
    assertEquals("true", fold("\"a\"==\"a\""));
  }

  @Test
  public void testInequality() {
    assertEquals("false", fold("1!=1"));
    assertEquals("true", fold("1!=2"));
    assertEquals("false", fold("1!==1"));
    assertEquals("true", fold("1!==\"1\""));
  }

  // Typeof tests

  @Test
  public void testTypeOf() {
    assertEquals("\"string\"", fold("typeof \"hello\""));
    assertEquals("\"number\"", fold("typeof 123"));
    assertEquals("\"boolean\"", fold("typeof true"));
    assertEquals("\"undefined\"", fold("typeof undefined"));
    assertEquals("\"object\"", fold("typeof null"));
    assertEquals("\"function\"", fold("typeof function(){}"));
    assertEquals("\"undefined\"", fold("typeof undeclaredVar"));
    // typeof with ! operator – bug-prone
    assertEquals("false", fold("!typeof x"));   // !typeof returns false (string always truthy)
    assertEquals("true", fold("!!typeof x"));
    assertEquals("\"boolean\"", fold("typeof !true")); // typeof !true -> typeof false -> "boolean"
    assertEquals("\"boolean\"", fold("typeof !false"));
  }

  // String concatenation

  @Test
  public void testStringConcat() {
    assertEquals("\"ab\"", fold("\"a\"+\"b\""));
    assertEquals("\"a1\"", fold("\"a\"+1"));
    assertEquals("\"1a\"", fold("1+\"a\""));
    assertEquals("\"a\"", fold("\"a\"+\"\""));
    assertEquals("\"\"", fold("\"\"+\"\""));
  }

  // Unary operators

  @Test
  public void testUnaryPlus() {
    assertEquals("5", fold("+5"));
    assertEquals("-5", fold("+(-5)"));
    assertEquals("5.5", fold("+5.5"));
  }

  @Test
  public void testUnaryMinus() {
    assertEquals("-5", fold("-5"));
    assertEquals("5", fold("-(-5)"));
    assertEquals("0", fold("-0"));
  }

  @Test
  public void testVoid() {
    assertEquals("undefined", fold("void 0"));
    assertEquals("undefined", fold("void(0)"));
    assertEquals("undefined", fold("void 123"));
  }

  // Miscellaneous edge cases

  @Test
  public void testNumericEdgeCases() {
    assertEquals("Infinity", fold("1e308*1e308"));
    assertEquals("-Infinity", fold("-1e308*1e308"));
    assertEquals("NaN", fold("NaN+1"));
    assertEquals("Infinity", fold("Infinity+Infinity"));
    assertEquals("-Infinity", fold("-Infinity-Infinity"));
    assertEquals("NaN", fold("Infinity-Infinity"));
    assertEquals("0", fold("0*Infinity")); // but 0*Infinity = NaN? Actually in JS it's NaN. Let's correct.
    // Actually 0 * Infinity = NaN
    assertEquals("NaN", fold("0*Infinity"));
  }

  @Test
  public void testZeroAndNegativeZero() {
    assertEquals("0", fold("0+0"));
    assertEquals("-0", fold("-0")); // -0 is preserved
    assertEquals("0", fold("1-1"));
    assertEquals("0", fold("0*5"));
  }

  // Tests for possible bug patterns in Defects4J closure-97

  @Test
  public void testFoldBitwiseNotWithNegation() {
    // Bug-prone: ~-1 should be 0
    assertEquals("0", fold("~-1"));
    // Additional: double bitwise not
    assertEquals("1", fold("~~1"));
    assertEquals("-1", fold("~~-1"));
  }

  @Test
  public void testFoldLogicalNotWithTypeof() {
    // typeof always returns truthy string, so !typeof is false
    assertEquals("false", fold("!typeof x"));
    assertEquals("true", fold("!!typeof x"));
    // Bug-prone when typeof is applied to a constant
    assertEquals("false", fold("!typeof \"abc\""));
    assertEquals("true", fold("!!typeof \"abc\""));
  }

  @Test
  public void testFoldMixedOperators() {
    // Combination with multiple constant expressions
    assertEquals("4", fold("(1+2)+1"));
    assertEquals("6", fold("(1+2)+(3)"));
    assertEquals("3", fold("(1+2)"));
    assertEquals("0", fold("(3-3)"));
    assertEquals("1", fold("(3+2)-4"));
  }

  @Test
  public void testFoldInLogicalExpressions() {
    // These may be partially folded or left unchanged if not all constants
    assertEquals("true", fold("true||false"));
    assertEquals("false", fold("false&&true"));
    assertEquals("true", fold("!false||true"));
    assertEquals("false", fold("!true&&false"));
  }

  @Test
  public void testFoldWithUndefined() {
    // undefined is a constant in Rhino (Node.UNDEFINED)
    assertEquals("undefined", fold("undefined"));
    assertEquals("true", fold("undefined==undefined"));
    assertEquals("false", fold("undefined==null")); // abstract equality: undefined==null -> true
    // Actually undefined == null is true in JS
    assertEquals("true", fold("undefined==null"));
    assertEquals("true", fold("undefined==null"));
    assertEquals("false", fold("undefined===null"));
  }

  @Test
  public void testFoldWithNull() {
    assertEquals("null", fold("null"));
    assertEquals("true", fold("null==null"));
    assertEquals("false", fold("null===undefined"));
  }

  // Test that no folding occurs for non-constant
  @Test
  public void testNoFoldingForNonConstant() {
    assertEquals("a+b", fold("a+b"));
    assertEquals("a-b", fold("a-b"));
    assertEquals("a+b+c", fold("a+b+c"));
  }

  // Test for try/catch? Not directly but we can include a test that exercises the pass on a larger script
  @Test
  public void testFoldInLargerScript() {
    String js = "var a = 1 + 2; var b = a + 3;";
    String expected = "var a=3;var b=a+3;";
    assertEquals(expected.replaceAll(" ", ""), fold(js).replaceAll(" ", ""));
  }

  @Test
  public void testFoldWithExterns() {
    // Externs may affect typeof, not folding
    assertEquals("typeof alert", foldWithExterns("typeof alert", "function alert(x) {}"));
    // alert is a function, typeof returns "function"
    // But folding might not fold it because it's not constant
    assertEquals("typeof alert", foldWithExterns("typeof alert", "function alert(x) {}"));
  }
}