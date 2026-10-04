package com.google.javascript.jscomp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import java.util.Arrays;
import org.junit.Test;

public class ControlFlowAnalysisTest {

  private ControlFlowGraph<Node> analyze(String js) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    Node root = compiler.parse(SourceFile.fromCode("testcode", js));
    assertNotNull("Failed to parse: " + js, root);
    assertTrue("Parse errors: " + Arrays.toString(compiler.getErrors()),
        compiler.getErrors().length == 0);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, true);
    cfa.process(null, root);

    ControlFlowGraph<Node> cfg = cfa.getCfg();
    assertNotNull("CFG should not be null", cfg);
    assertNotNull("CFG entry should not be null", cfg.getEntry());
    assertNotNull("CFG implicit return should not be null", cfg.getImplicitReturn());
    assertTrue("Entry must have an outgoing edge", cfg.getEntry().getOutEdges().size() > 0);
    assertTrue("Implicit return must be reachable", cfg.getImplicitReturn().getInEdges().size() > 0);

    return cfg;
  }

  private void assertSaneCfg(String js) {
    analyze(js);
  }

  @Test
  public void testEmptyScript() {
    assertSaneCfg("");
  }

  @Test
  public void testSimpleExpressionStatement() {
    assertSaneCfg("x = 1;");
  }

  @Test
  public void testVarDeclaration() {
    assertSaneCfg("var x = 1;");
  }

  @Test
  public void testIfStatement() {
    assertSaneCfg("if (x) { y(); }");
  }

  @Test
  public void testIfElseStatement() {
    assertSaneCfg("if (x) { y(); } else { z(); }");
  }

  @Test
  public void testNestedIfStatement() {
    assertSaneCfg("if (a) { if (b) { c(); } } else { d(); }");
  }

  @Test
  public void testWhileLoop() {
    assertSaneCfg("while (x) { y(); }");
  }

  @Test
  public void testWhileLoopWithBreak() {
    assertSaneCfg("while (true) { break; }");
  }

  @Test
  public void testDoWhileLoop() {
    assertSaneCfg("do { y(); } while (x);");
  }

  @Test
  public void testForLoop() {
    assertSaneCfg("for (var i = 0; i < 10; i++) { f(i); }");
  }

  @Test
  public void testForLoopWithoutVar() {
    assertSaneCfg("for (i = 0; i < 10; i++) { f(i); }");
  }

  @Test
  public void testForInLoop() {
    assertSaneCfg("for (var k in obj) { f(k); }");
  }

  @Test
  public void testForInLoopWithoutVar() {
    assertSaneCfg("for (k in obj) { f(k); }");
  }

  @Test
  public void testForInLoopWithContinue() {
    assertSaneCfg("for (var k in obj) { if (k) { continue; } }");
  }

  @Test
  public void testForInLoopWithBreak() {
    assertSaneCfg("for (var k in obj) { break; }");
  }

  @Test
  public void testNestedForInLoop() {
    assertSaneCfg("for (var k in obj) { for (var j in obj2) { f(k, j); } }");
  }

  @Test
  public void testLabeledForInLoopWithContinue() {
    assertSaneCfg("outer: for (var k in obj) { continue outer; }");
  }

  @Test
  public void testForInWithFunctionExpression() {
    assertSaneCfg("for (var k in obj) { var f = function() {}; }");
  }

  @Test
  public void testNestedFunctionInForIn() {
    assertSaneCfg("function outer() { for (var k in obj) { var f = function() {}; } }");
  }

  @Test
  public void testSwitchStatement() {
    assertSaneCfg("switch (x) { case 1: y(); break; default: z(); }");
  }

  @Test
  public void testSwitchWithoutDefault() {
    assertSaneCfg("switch (x) { case 1: y(); break; case 2: z(); }");
  }

  @Test
  public void testEmptySwitchStatement() {
    assertSaneCfg("switch (x) {}");
  }

  @Test
  public void testTryCatch() {
    assertSaneCfg("try { f(); } catch (e) { g(); }");
  }

  @Test
  public void testTryFinally() {
    assertSaneCfg("try { f(); } finally { g(); }");
  }

  @Test
  public void testTryCatchFinally() {
    assertSaneCfg("try { f(); } catch (e) { g(); } finally { h(); }");
  }

  @Test
  public void testEmptyTryCatch() {
    assertSaneCfg("try {} catch (e) {}");
  }

  @Test
  public void testReturnInFunction() {
    assertSaneCfg("function f() { return 1; }");
  }

  @Test
  public void testReturnInTryFinally() {
    assertSaneCfg("function f() { try { return 1; } finally { g(); } }");
  }

  @Test
  public void testContinueInTryFinally() {
    assertSaneCfg("while (x) { try { continue; } finally { y(); } }");
  }

  @Test
  public void testBreakInTryFinally() {
    assertSaneCfg("for (;;) { try { break; } finally { y(); } }");
  }

  @Test
  public void testThrowStatement() {
    assertSaneCfg("function f() { throw new Error('x'); }");
  }

  @Test
  public void testBreakStatement() {
    assertSaneCfg("while (x) { break; }");
  }

  @Test
  public void testContinueStatement() {
    assertSaneCfg("while (x) { continue; }");
  }

  @Test
  public void testLabeledBreak() {
    assertSaneCfg("outer: while (x) { while (y) { break outer; } }");
  }

  @Test
  public void testLabeledContinue() {
    assertSaneCfg("outer: while (x) { while (y) { continue outer; } }");
  }

  @Test
  public void testLabeledBlock() {
    assertSaneCfg("var x = 1; label: { x = 2; break label; }");
  }

  @Test
  public void testLabeledContinueInDoWhile() {
    assertSaneCfg("outer: do { continue outer; } while (x);");
  }

  @Test
  public void testLogicalOperators() {
    assertSaneCfg("if (a && b || c) { d(); }");
  }

  @Test
  public void testConditionalOperator() {
    assertSaneCfg("var x = a ? b : c;");
  }

  @Test
  public void testCommaOperator() {
    assertSaneCfg("x = (a, b);");
  }

  @Test
  public void testFunctionDeclaration() {
    assertSaneCfg("function f() { g(); }");
  }

  @Test
  public void testFunctionExpression() {
    assertSaneCfg("var f = function() { g(); };");
  }

  @Test
  public void testNestedFunctions() {
    assertSaneCfg("function f() { function g() {} return g; }");
  }

  @Test
  public void testWithStatement() {
    assertSaneCfg("with (obj) { x = 1; }");
  }

  @Test
  public void testObjectLiteral() {
    assertSaneCfg("var x = {a: 1, b: 2};");
  }

  @Test
  public void testArrayLiteral() {
    assertSaneCfg("var x = [1, 2, 3];");
  }

  @Test
  public void testDebuggerStatement() {
    assertSaneCfg("debugger;");
  }

  @Test
  public void testNewExpression() {
    assertSaneCfg("var x = new Foo();");
  }

  @Test
  public void testCallExpression() {
    assertSaneCfg("foo();");
  }

  @Test
  public void testGetPropExpression() {
    assertSaneCfg("var x = obj.prop;");
  }

  @Test
  public void testAssignmentOperators() {
    assertSaneCfg("x += 1; y -= 2; z *= 3;");
  }

  @Test
  public void testUnaryOperators() {
    assertSaneCfg("x = -y; x = !z; x = ~q;");
  }

  @Test
  public void testTypeofInstanceofIn() {
    assertSaneCfg("var a = typeof x; var b = x instanceof y; var c = 'prop' in obj;");
  }

  @Test
  public void testShouldNotTraverseFunctions() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    Node root = compiler.parse(SourceFile.fromCode("testcode", "function f() { return 1; }"));
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, root);

    assertNotNull(cfa.getCfg());
    assertNotNull(cfa.getCfg().getEntry());
  }

  @Test
  public void testEdgeReachableFalse() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    Node root = compiler.parse(SourceFile.fromCode("testcode", "var x = 1; if (x) { y(); }"));
    assertNotNull(root);
    assertTrue(compiler.getErrors().length == 0);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true, false);
    cfa.process(null, root);

    assertNotNull(cfa.getCfg());
    assertNotNull(cfa.getCfg().getEntry());
    assertNotNull(cfa.getCfg().getImplicitReturn());
  }
}