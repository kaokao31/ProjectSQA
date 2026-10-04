package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import com.google.javascript.rhino.Node;
import org.junit.Test;

/**
 * Tests for {@link CodeConsumer}.
 * Focuses on the number formatting logic and core output methods.
 */
public class CodeConsumerTest {

  /** Simple CodeConsumer implementation that captures output via getCode(). */
  private static class TestCodeConsumer extends CodeConsumer {
    @Override
    public void startSourceMapping(Node node) {
      // no-op
    }

    @Override
    public void endSourceMapping(Node node) {
      // no-op
    }
  }

  private CodeConsumer newConsumer() {
    return new TestCodeConsumer();
  }

  private void assertNumber(String expected, double value) {
    CodeConsumer c = newConsumer();
    c.addNumber(value);
    assertEquals(expected, c.getCode());
  }

  private void assertIdentifier(String expected, String identifier) {
    CodeConsumer c = newConsumer();
    c.addIdentifier(identifier);
    assertEquals(expected, c.getCode());
  }

  private void assertString(String expected, String str) {
    CodeConsumer c = newConsumer();
    c.addString(str);
    assertEquals(expected, c.getCode());
  }

  private void assertAddChar(String expected, char ch) {
    CodeConsumer c = newConsumer();
    c.add(ch);
    assertEquals(expected, c.getCode());
  }

  private void assertAddString(String expected, String str) {
    CodeConsumer c = newConsumer();
    c.add(str);
    assertEquals(expected, c.getCode());
  }

  // --------------------------------------------------------------------
  // Number formatting tests
  // --------------------------------------------------------------------

  @Test
  public void testZero() {
    assertNumber("0", 0.0);
  }

  @Test
  public void testNegativeZero() {
    // Should preserve the negative sign.
    assertNumber("-0", -0.0);
  }

  @Test
  public void testIntegers() {
    assertNumber("1", 1.0);
    assertNumber("-1", -1.0);
    assertNumber("123", 123.0);
    assertNumber("-123", -123.0);
  }

  @Test
  public void testDecimals() {
    assertNumber("0.5", 0.5);
    assertNumber("-0.5", -0.5);
    assertNumber("3.14159", 3.14159);
    assertNumber("123.456", 123.456);
  }

  @Test
  public void testLargeIntegers() {
    // These values are integers but exceed Long.MAX_VALUE; should not be cast to long.
    assertNumber("1e19", 1e19);
    assertNumber("-1e19", -1e19);
    assertNumber("1e21", 1e21);
    assertNumber("1e22", 1e22);
  }

  @Test
  public void testWithinLongRange() {
    // These fit in long and should be printed as integer literals.
    assertNumber("1000000000000000000", 1e18);
    assertNumber("-1000000000000000000", -1e18);
  }

  @Test
  public void testExponentNotation() {
    assertNumber("1e-7", 1e-7);
    assertNumber("-1e-7", -1e-7);
    assertNumber("1.5e-7", 1.5e-7);
    assertNumber("1.5e21", 1.5e21);
    assertNumber("1e6", 1e6);      // 1000000 -> "1e6" is shortest? actually "1000000" is shorter, but we use what the implementation does.
  }

  @Test
  public void testInfinity() {
    assertNumber("Infinity", Double.POSITIVE_INFINITY);
    assertNumber("-Infinity", Double.NEGATIVE_INFINITY);
  }

  @Test
  public void testNaN() {
    assertNumber("NaN", Double.NaN);
  }

  @Test
  public void testExtremes() {
    assertNumber("1.7976931348623157e308", Double.MAX_VALUE);
    assertNumber("4.9e-324", Double.MIN_VALUE);
  }

  // --------------------------------------------------------------------
  // Identifier formatting tests
  // --------------------------------------------------------------------

  @Test
  public void testIdentifier_Simple() {
    assertIdentifier("foo", "foo");
    assertIdentifier("bar1", "bar1");
    assertIdentifier("_private", "_private");
    assertIdentifier("$", "$");
  }

  @Test
  public void testIdentifier_Empty() {
    assertIdentifier("", "");
  }

  // --------------------------------------------------------------------
  // String literal formatting tests
  // --------------------------------------------------------------------

  @Test
  public void testString_Empty() {
    assertString("\"\"", "");
  }

  @Test
  public void testString_Simple() {
    assertString("\"hello\"", "hello");
  }

  @Test
  public void testString_DoubleQuote() {
    assertString("\"a\\\"b\"", "a\"b");
  }

  @Test
  public void testString_Backslash() {
    assertString("\"a\\\\b\"", "a\\b");
  }

  @Test
  public void testString_Newline() {
    assertString("\"a\\nb\"", "a\nb");
  }

  @Test
  public void testString_Tab() {
    assertString("\"a\\tb\"", "a\tb");
  }

  @Test
  public void testString_CarriageReturn() {
    assertString("\"a\\rb\"", "a\rb");
  }

  @Test
  public void testString_FormFeed() {
    assertString("\"a\\fb\"", "a\fb");
  }

  @Test
  public void testString_Backspace() {
    assertString("\"a\\bb\"", "a\bb");
  }

  @Test
  public void testString_UnicodeEscape() {
    assertString("\"\\u0041\"", "A");
  }

  // --------------------------------------------------------------------
  // Character and raw string addition tests
  // --------------------------------------------------------------------

  @Test
  public void testAddChar() {
    assertAddChar("a", 'a');
    assertAddChar("1", '1');
    assertAddChar("{", '{');
  }

  @Test
  public void testAddRawString() {
    assertAddString("hello", "hello");
    assertAddString("", "");
    assertAddString("hello world", "hello world");
  }

  // --------------------------------------------------------------------
  // Line handling and spacing tests
  // --------------------------------------------------------------------

  @Test
  public void testNewline() {
    CodeConsumer c = newConsumer();
    c.add("a");
    c.newline();
    c.add("b");
    assertEquals("a\nb", c.getCode());
  }

  @Test
  public void testBeginLineAndEndLine() {
    CodeConsumer c = newConsumer();
    c.beginLine();
    c.add("a");
    c.endLine();
    assertEquals("a", c.getCode());
  }

  @Test
  public void testEndLineDoesNotAddNewline() {
    CodeConsumer c = newConsumer();
    c.add("a");
    c.endLine();
    c.add("b");
    assertEquals("ab", c.getCode());
  }

  @Test
  public void testAddSpace() {
    CodeConsumer c = newConsumer();
    c.add("a");
    c.addSpace();
    c.add("b");
    assertEquals("a b", c.getCode());
  }

  // --------------------------------------------------------------------
  // Statement termination tests
  // --------------------------------------------------------------------

  @Test
  public void testEndStatement() {
    CodeConsumer c = newConsumer();
    c.add("x");
    c.endStatement();
    c.add("y");
    assertEquals("x;y", c.getCode());
  }

  @Test
  public void testMaybeEndStatement() {
    CodeConsumer c = newConsumer();
    c.add("x");
    c.maybeEndStatement();
    c.add("y");
    assertEquals("xy", c.getCode());  // may be called when not at line end.
  }

  @Test
  public void testMaybeEndStatementAfterSpace() {
    CodeConsumer c = newConsumer();
    c.add("x");
    c.addSpace();
    c.maybeEndStatement();
    c.add("y");
    assertEquals("x y", c.getCode());
  }

  // --------------------------------------------------------------------
  // Miscellaneous null safety checks
  // --------------------------------------------------------------------

  @Test
  public void testConsumerIsNotNull() {
    assertNotNull(newConsumer());
  }

  @Test
  public void testStartsEmpty() {
    CodeConsumer c = newConsumer();
    assertEquals("", c.getCode());
  }

  @Test
  public void testAddStringDoesNotThrow() {
    CodeConsumer c = newConsumer();
    c.addString("test");
    assertNotNull(c.getCode());
  }
}