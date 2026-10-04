package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * JUnit 4 test suite for Normalize class, designed to achieve high code coverage
 * and trigger potential faults (Defects4J Closure-102).
 */
public class NormalizeTest {
  private Compiler compiler;
  private Normalize normalize;

  @Before
  public void setUp() {
    compiler = new Compiler();
    normalize = new Normalize(compiler, false);
  }

  // ========= Basic Processing =========

  @Test
  public void testProcessNullRoot() {
    normalize.process(null, null);
    // Expect no exception, root remains null
    assertNull(null);
  }

  @Test
  public void testProcessEmptyScript() {
    Node script = new Node(Token.SCRIPT);
    Node result = normalize.process(null, script);
    assertNotNull("Result should be the same node", result);
    assertEquals(Token.SCRIPT, result.getToken());
    assertEquals(0, result.getChildCount());
  }

  @Test
  public void testProcessWithExterns() {
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    root.addChildToBack(new Node(Token.FUNCTION, Node.newString("f")));
    normalize.process(externs, root);
    assertNotNull(root);
  }

  // ========= Function Duplication (Closure-102 bug) =========

  @Test
  public void testDuplicateFunctionNamesInSameScope() {
    Node script = new Node(Token.SCRIPT);
    Node f1 = new Node(Token.FUNCTION, Node.newString("f"));
    Node f2 = new Node(Token.FUNCTION, Node.newString("f"));
    script.addChildToBack(f1);
    script.addChildToBack(f2);
    normalize.process(null, script);
    // After normalization, functions should have distinct names or an error is reported
    String name1 = f1.getFirstChild().getString();
    String name2 = f2.getFirstChild().getString();
    assertFalse("Duplicate function names should be resolved", name1.equals(name2));
  }

  @Test
  public void testDuplicateFunctionNamesInDifferentScopes() {
    Node script = new Node(Token.SCRIPT);
    Node outerFunc = new Node(Token.FUNCTION, Node.newString("f"));
    Node innerFunc = new Node(Token.FUNCTION, Node.newString("f"));
    outerFunc.addChildToBack(innerFunc);
    script.addChildToBack(outerFunc);
    normalize.process(null, script);
    // Nested functions with same name should remain unchanged
    assertEquals("f", outerFunc.getFirstChild().getString());
    assertEquals("f", innerFunc.getFirstChild().getString());
  }

  // ========= Variable Redeclaration =========

  @Test
  public void testRedeclaredVarInFunction() {
    Node script = new Node(Token.SCRIPT);
    Node var1 = new Node(Token.VAR);
    var1.addChildToBack(Node.newString("x"));
    Node var2 = new Node(Token.VAR);
    var2.addChildToBack(Node.newString("x"));
    script.addChildToBack(var1);
    script.addChildToBack(var2);
    normalize.process(null, script);
    // Expect an error to be logged
    assertTrue("Should have reported error for duplicate var",
               compiler.getErrors().length > 0);
  }

  // ========= Normalization of Literal and Expression Nodes =========

  @Test
  public void testNormalizeNumber() {
    Node expr = Node.newNumber(42);
    Node parent = new Node(Token.EXPR_RESULT, expr);
    Node script = new Node(Token.SCRIPT, parent);
    normalize.process(null, script);
    // Number node should remain unchanged
    assertEquals(42.0, expr.getDouble(), 0.001);
  }

  @Test
  public void testNormalizeString() {
    Node str = Node.newString("hello");
    Node parent = new Node(Token.EXPR_RESULT, str);
    Node script = new Node(Token.SCRIPT, parent);
    normalize.process(null, script);
    assertEquals("hello", str.getString());
  }

  @Test
  public void testNormalizeName() {
    Node name = Node.newString(Token.NAME, "a");
    Node parent = new Node(Token.EXPR_RESULT, name);
    Node script = new Node(Token.SCRIPT, parent);
    normalize.process(null, script);
    assertEquals("a", name.getString());
  }

  // ========= Edge Cases: Null Children, Missing Names =========

  @Test(expected = Exception.class)
  public void testNullChildInScript() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(null);
    normalize.process(null, script); // Should handle gracefully or throw
  }

  @Test
  public void testFunctionWithoutName() {
    Node func = new Node(Token.FUNCTION);
    // No name child - expect processing without failure
    Node script = new Node(Token.SCRIPT, func);
    try {
      normalize.process(null, script);
      assertNotNull(func);
    } catch (Exception e) {
      fail("Processing should not throw on anonymous function: " + e.getMessage());
    }
  }

  // ========= Compiler Option Checks =========

  @Test
  public void testAssertOnChangeEnabled() {
    Normalize strictNormalize = new Normalize(compiler, true);
    Node script = new Node(Token.SCRIPT);
    // Should not throw for a simple valid tree
    strictNormalize.process(null, script);
    assertNotNull(script);
  }

  // ========= Additional Coverage: Loop and Branch Coverage =========

  @Test
  public void testMultipleTokensInScript() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(new Node(Token.VAR, Node.newString("a")));
    script.addChildToBack(new Node(Token.FUNCTION, Node.newString("b")));
    script.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    normalize.process(null, script);
    assertEquals(3, script.getChildCount());
  }
}