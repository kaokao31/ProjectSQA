package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.ControlFlowGraph;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.Var;
import com.google.javascript.rhino.Node;

/**
 * JUnit 4 test suite for LiveVariablesAnalysis.
 * Designed to maximize code coverage and expose the known Defects4J bug (Closure 58)
 * where variables declared in for-in loops are incorrectly considered live at entry.
 */
public class LiveVariablesAnalysisTest {

  private Compiler compiler;
  private Scope topScope;

  @Before
  public void setUp() throws Exception {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    topScope = compiler.getTopScope();
  }

  // Helper: compile JS and return control flow graph of the first function.
  private ControlFlowGraph<Node> compileAndGetCfg(String js) {
    Node scriptRoot = compiler.parse(js, new DiagnosticCollector());
    Node functionNode = scriptRoot.getFirstChild();
    if (functionNode == null || !functionNode.isFunction()) {
      fail("Input must contain a function");
    }
    return compiler.getControlFlowGraph(functionNode);
  }

  // Helper: compute live variable analysis for a single function.
  private LiveVariablesAnalysis analyze(String js) {
    ControlFlowGraph<Node> cfg = compileAndGetCfg(js);
    Node functionNode = cfg.getEntry().getValue();
    Scope functionScope = new Scope(topScope, functionNode);
    LiveVariablesAnalysis analysis = new LiveVariablesAnalysis(cfg, functionScope);
    analysis.analyze();
    return analysis;
  }

  // ===================== Test Cases =====================

  @Test
  public void testSimpleAssignment() {
    String js = "function f() { var x = 1; x = x + 1; return x; }";
    LiveVariablesAnalysis analysis = analyze(js);
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    assertNotNull(liveOut);
    // x should not be live at entry (it is defined inside)
    Var x = analysis.getScope().getVar("x");
    assertNotNull(x);
    assertFalse("x should not be live at function entry", liveOut.isLive(x));
  }

  @Test
  public void testIfElse() {
    String js = "function f(a) { var r = 0; if (a > 0) { r = 1; } else { r = 2; } return r; }";
    LiveVariablesAnalysis analysis = analyze(js);
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    // a is live at entry (used in condition and not defined)
    Var a = analysis.getScope().getVar("a");
    assertTrue("a should be live at entry", liveOut.isLive(a));
    // r should not be live at entry (defined inside)
    Var r = analysis.getScope().getVar("r");
    assertFalse("r should not be live at entry", liveOut.isLive(r));
  }

  @Test
  public void testWhileLoop() {
    String js = "function f(n) { var s = 0; while (n > 0) { s = s + n; n = n - 1; } return s; }";
    LiveVariablesAnalysis analysis = analyze(js);
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    Var n = analysis.getScope().getVar("n");
    assertTrue("n should be live at entry", liveOut.isLive(n));
    Var s = analysis.getScope().getVar("s");
    assertFalse("s should not be live at entry", liveOut.isLive(s));
  }

  @Test
  public void testTryCatch() {
    String js = "function f() { var x; try { x = 1; } catch(e) { x = 2; } return x; }";
    LiveVariablesAnalysis analysis = analyze(js);
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    Var x = analysis.getScope().getVar("x");
    assertNotNull(x);
    // x is defined but not live at entry (no external use)
    assertFalse("x should not be live at entry", liveOut.isLive(x));
  }

  @Test
  public void testForInBug() {
    // Known Defects4J bug (Closure 58):
    // Variable declared in for-in is incorrectly treated as use (gen) instead of definition (kill).
    // As a result, it appears live at function entry instead of being dead.
    String js = "function f() { for (var a in b) { c(a); } }";
    LiveVariablesAnalysis analysis = analyze(js);
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    Var a = analysis.getScope().getVar("a");
    assertNotNull("variable 'a' should exist", a);
    // The bug would cause this assertion to fail (a would be live).
    assertFalse("for-in variable 'a' should NOT be live at entry (it is defined inside)",
        liveOut.isLive(a));
  }

  @Test
  public void testMultipleVarDeclarations() {
    String js = "function f() { var x = 1, y = 2; x = x + y; return x; }";
    LiveVariablesAnalysis analysis = analyze(js);
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    Var x = analysis.getScope().getVar("x");
    Var y = analysis.getScope().getVar("y");
    assertFalse("x should not be live at entry", liveOut.isLive(x));
    assertFalse("y should not be live at entry", liveOut.isLive(y));
  }

  @Test
  public void testNestedFunction() {
    String js = "function outer() { var x = 1; function inner() { return x; } return inner(); }";
    LiveVariablesAnalysis analysis = analyze(js);
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    Var x = analysis.getScope().getVar("x");
    // x is defined inside outer, but also used in inner -> should be live at exit of outer
    // At entry, x is not live yet
    assertFalse("x should not be live at function entry", liveOut.isLive(x));
  }

  @Test
  public void testEmptyFunction() {
    String js = "function f() {}";
    LiveVariablesAnalysis analysis = analyze(js);
    assertNotNull(analysis);
    // No variables, nothing to check.
  }

  @Test
  public void testParameterUsed() {
    String js = "function f(x, y) { return x + y; }";
    LiveVariablesAnalysis analysis = analyze(js);
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    Var x = analysis.getScope().getVar("x");
    Var y = analysis.getScope().getVar("y");
    assertTrue("parameter x should be live at entry", liveOut.isLive(x));
    assertTrue("parameter y should be live at entry", liveOut.isLive(y));
  }

  @Test
  public void testDeadAssignment() {
    String js = "function f() { var x = 1; x = 2; return x; }";
    LiveVariablesAnalysis analysis = analyze(js);
    // x is defined and then redefined; first assignment is dead.
    // At entry, x should not be live.
    LiveVariablesAnalysis.LiveSet liveOut =
        analysis.getLiveOut(analysis.getCfg().getEntry());
    Var x = analysis.getScope().getVar("x");
    assertFalse("x should not be live at entry", liveOut.isLive(x));
  }
}