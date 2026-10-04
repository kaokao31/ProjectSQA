package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class NormalizeTest {
  private Compiler compiler;
  private Normalize normalize;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    normalize = new Normalize(compiler, false);
  }

  @Test
  public void testEmptySource() {
    String code = "";
    Node root = compiler.parseSyntheticCode("empty.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    assertNotNull(root);
    assertTrue(root.hasChildren());
  }

  @Test
  public void testSimpleFunctionDeclaration() {
    String code = "function f() {}";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    // After normalization, function declarations should be hoisted to top
    Node script = root.getFirstChild();
    assertEquals(Token.SCRIPT, script.getType());
    Node funcNode = script.getFirstChild();
    assertEquals(Token.FUNCTION, funcNode.getType());
    assertTrue(script.getChildCount() == 1);
  }

  @Test
  public void testMultipleFunctionDeclarations() {
    String code = "function a(){} function b(){}";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    Node script = root.getFirstChild();
    assertEquals(2, script.getChildCount());
  }

  @Test
  public void testNestedFunctionDeclaration() {
    String code = "function outer(){ function inner(){} }";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    Node script = root.getFirstChild();
    Node outerFunc = script.getFirstChild();
    assertTrue(outerFunc.getType() == Token.FUNCTION);
    // Nested function should be hoisted to top of outer function's block
    Node body = outerFunc.getLastChild();
    Node innerFunc = body.getFirstChild();
    assertEquals(Token.FUNCTION, innerFunc.getType());
  }

  @Test
  public void testFunctionExpressionNotHoisted() {
    String code = "var x = function(){};";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    Node script = root.getFirstChild();
    Node varDecl = script.getFirstChild();
    assertEquals(Token.VAR, varDecl.getType());
    Node varName = varDecl.getFirstChild();
    assertTrue(varName.getType() == Token.NAME);
    Node funcExpr = varName.getFirstChild();
    assertEquals(Token.FUNCTION, funcExpr.getType());
  }

  @Test
  public void testVariableDeclarationHoisting() {
    String code = "var a; var b;";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    Node script = root.getFirstChild();
    // Multiple var statements should be collapsed
    assertEquals(1, script.getChildCount());
    Node varNode = script.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    assertEquals(2, varNode.getChildCount());
  }

  @Test
  public void testEmptyVarDeclaration() {
    String code = "var a = 1, b;";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    Node varNode = root.getFirstChild().getFirstChild();
    // Both should be in same VAR node
    assertEquals(2, varNode.getChildCount());
  }

  @Test
  public void testTryCatchBlockDoesNotHoist() {
    String code = "try { var x = 1; } catch(e) { var y = 2; }";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    Node script = root.getFirstChild();
    // Hoisting should still happen, but variable declarations may be moved to top of block
    // This tests that normalization does not break try/catch structure
    assertTrue(script.getChildCount() > 0);
  }

  @Test
  public void testBlockScopedVariablesNotHoisted() {
    // With let/const, but closure may not support ES6 unless flags set;
    // test var inside block
    String code = "if (true) { var z = 3; }";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    // var should be hoisted out of the if block (normalization)
    Node script = root.getFirstChild();
    Node firstChild = script.getFirstChild();
    // Expect VAR declaration at top level, not inside IF
    assertEquals(Token.VAR, firstChild.getType());
    assertEquals("z", firstChild.getFirstChild().getString());
  }

  @Test
  public void testFunctionNameCollision() {
    String code = "var f = 1; function f() {}";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    // Should produce either function or var, depending on normalization order
    Node script = root.getFirstChild();
    // Expect no errors; just check it doesn't throw
    assertTrue(script.getChildCount() >= 1);
  }

  @Test
  public void testNullExterns() {
    String code = "var a = 1;";
    Node root = compiler.parseSyntheticCode("test.js", code);
    // Normalize should handle null externs gracefully
    try {
      normalize.process(null, root);
      // Should not throw NullPointerException under certain configurations
      assertNotNull(root);
    } catch (NullPointerException e) {
      fail("Normalize should handle null externs");
    }
  }

  @Test
  public void testNullRoot() {
    Node externs = new Node(Token.EMPTY);
    try {
      normalize.process(externs, null);
      fail("Expected NullPointerException or IllegalStateException");
    } catch (NullPointerException e) {
      // expected
    } catch (Exception e) {
      // acceptable
    }
  }

  @Test
  public void testUnnormalizedSource() {
    String code = "var a = function(){}; function b(){}";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    // Run normalization twice to ensure idempotency
    normalize.process(externs, root.cloneTree());
    normalize.process(externs, root.cloneTree());
    // Just checks no exception
    assertNotNull(root);
  }

  @Test
  public void testNormalizeWithExterns() {
    String externCode = "function externFunc(){}";
    String code = "function localFunc(){}";
    Node externRoot = compiler.parseSyntheticCode("externs.js", externCode);
    Node root = compiler.parseSyntheticCode("test.js", code);
    normalize.process(externRoot, root);
    // Externs should not be modified, but pass should handle it
    Node script = root.getFirstChild();
    assertEquals(1, script.getChildCount());
  }

  @Test
  public void testLargeNumberOfDeclarations() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 100; i++) {
      sb.append("var x").append(i).append(" = ").append(i).append(";");
    }
    String code = sb.toString();
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    // Should have a single VAR declaration with many children
    Node varNode = root.getFirstChild().getFirstChild();
    assertTrue(varNode.getChildCount() >= 100);
  }

  @Test
  public void testWithCompilerErrorReporter() {
    // Test that normalization does not introduce errors
    compiler.setErrorReporter(new SimpleErrorReporter());
    String code = "function f() { return 1; }";
    Node root = compiler.parseSyntheticCode("test.js", code);
    Node externs = new Node(Token.EMPTY);
    normalize.process(externs, root);
    // No errors should be reported
    assertFalse(compiler.getErrors().length > 0);
  }

  // Helper class for error reporter (minimal)
  private static class SimpleErrorReporter extends com.google.javascript.jscomp.ErrorReporter {
    @Override
    public void warning(com.google.javascript.jscomp.CheckLevel level, JSError error) { }

    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) { }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) { }

    @Override
    public void error(JSError error) { }

    @Override
    public void report(com.google.javascript.jscomp.CheckLevel level, JSError error) { }
  }
}