package com.google.javascript.jscomp;

import com.google.javascript.jscomp.graph.DiGraph;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for {@link ControlFlowAnalysis}.
 * Designed to achieve high code coverage and detect faults,
 * including the known defect in Defects4J Closure bug 14.
 */
public class ControlFlowAnalysisTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  /** Parses a JavaScript string and returns the computed control flow graph. */
  private ControlFlowGraph<Node> computeCfg(String js) {
    CompilerOptions options = new CompilerOptions();
    // Disable all optimizations to keep the raw AST
    options.setAllNewTypeInference(true);
    options.setContinueAfterErrors(true);
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("test.js", js);
    Result result = compiler.compile(extern, input, options);
    assertNotNull("Compilation should not produce null result", result);
    assertTrue("Compilation should succeed (warnings/errors allowed)", result.success);
    Node script = result.root;
    // The root is a synthetic block; the actual script is the first child
    Node firstChild = script.getFirstChild();
    assertNotNull("Script must have a non-null child", firstChild);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.analyze(firstChild);
    return cfa.getCfg();
  }

  /** Asserts that the graph has at least the expected number of nodes. */
  private void assertMinNodeCount(ControlFlowGraph<Node> cfg, int min) {
    assertTrue("Graph must have at least " + min + " nodes, but has " + cfg.getNodeCount(),
        cfg.getNodeCount() >= min);
  }

  // ======================== Basic control flow tests ========================

  @Test
  public void testSimpleBlock() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { var x = 1; }");
    assertNotNull("CFG must not be null", cfg);
    assertMinNodeCount(cfg, 2); // entry and exit
  }

  @Test
  public void testIfElse() {
    ControlFlowGraph<Node> cfg = computeCfg("function f(a) { if (a) { return 1; } else { return 2; } }");
    assertNotNull(cfg);
    assertMinNodeCount(cfg, 4);
  }

  @Test
  public void testWhileLoop() {
    ControlFlowGraph<Node> cfg = computeCfg("function f(a) { while (a) { a--; } }");
    assertNotNull(cfg);
    assertMinNodeCount(cfg, 4);
  }

  @Test
  public void testForLoop() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { for (var i = 0; i < 10; i++) { } }");
    assertNotNull(cfg);
    assertMinNodeCount(cfg, 5);
  }

  @Test
  public void testLabeledBreak() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { x: { break x; } }");
    assertNotNull(cfg);
  }

  @Test
  public void testLabeledContinueInsideLoop() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { x: while (true) { continue x; } }");
    assertNotNull(cfg);
    // The continue should create a back edge to the loop header.
    // With a correct analysis this should succeed without exception.
  }

  // ======================== Try / Catch / Finally ========================

  @Test
  public void testTryCatch() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { try { throw new Error(); } catch (e) { } }");
    assertNotNull(cfg);
  }

  @Test
  public void testTryFinally() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { try { } finally { } }");
    assertNotNull(cfg);
  }

  @Test
  public void testTryCatchFinally() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() { try { } catch (e) { } finally { } }");
    assertNotNull(cfg);
  }

  // ======================== Switch statement tests ========================

  @Test
  public void testSwitchWithBreak() {
    ControlFlowGraph<Node> cfg = computeCfg(
        "function f(a) { switch (a) { case 1: break; case 2: break; default: break; } }");
    assertNotNull(cfg);
  }

  @Test
  public void testSwitchWithReturn() {
    ControlFlowGraph<Node> cfg = computeCfg(
        "function f(a) { switch (a) { case 1: return 1; case 2: return 2; } }");
    assertNotNull(cfg);
  }

  // ======================== Labeled continue / break edge cases ========================
  // This section targets Defects4J Closure bug 14:
  // ControlFlowAnalysis mishandles `continue` with a label that is not on a loop.
  // For example, `continue` inside a switch targeting a label attached to the switch.

  @Test
  public void testContinueWithLabelOnBlockInvalid() {
    // 'continue' with a label that is on a block is invalid in JavaScript.
    // The parser may still produce an AST, and the analysis must handle it correctly.
    // The bug is that it might create an incorrect backward edge.
    String js = "function f() { x: { continue x; } }";
    try {
      ControlFlowGraph<Node> cfg = computeCfg(js);
      // If we reach here, the analysis did not throw; check the graph does not contain
      // a back edge to the start of the labeled block.
      Node script = compiler.getRoot().getFirstChild();
      // Locate the LABEL node
      Node labelNode = findNodeByType(script, Token.LABEL);
      assertNotNull("LABEL node must exist", labelNode);
      Node blockNode = labelNode.getLastChild(); // The block after the label
      Node continueNode = findNodeByType(blockNode, Token.CONTINUE);
      assertNotNull("CONTINUE node must exist", continueNode);

      DiGraph.DiGraphNode<Node> cfgLabel = cfg.getNode(labelNode);
      DiGraph.DiGraphNode<Node> cfgContinue = cfg.getNode(continueNode);
      assertNotNull("Graph node for LABEL must exist", cfgLabel);
      assertNotNull("Graph node for CONTINUE must exist", cfgContinue);

      // The continue should NOT have an edge to the label's start (that would be a back edge).
      // If it did, it would indicate a bug.
      for (DiGraph.DiGraphEdge<Node, ControlFlowAnalysis.Branch> edge :
          cfgContinue.getOutEdges()) {
        assertFalse("Continue should not have an edge targeting the start of the label",
            edge.getDestination().equals(cfgLabel));
      }
    } catch (Exception e) {
      // An exception is acceptable because the JavaScript is syntactically incorrect.
      // The analysis may throw a runtime exception.
      assertTrue("Exception message should mention 'continue' or 'label': " + e.getMessage(),
          e.getMessage().toLowerCase().contains("continue") ||
          e.getMessage().toLowerCase().contains("label"));
    }
  }

  @Test
  public void testContinueWithLabelOnSwitch() {
    // This is the exact scenario from the bug: continue targeting a label on a switch.
    String js = "function f(a) { x: switch (a) { case 1: continue x; } }";
    try {
      ControlFlowGraph<Node> cfg = computeCfg(js);
      Node script = compiler.getRoot().getFirstChild();
      Node labelNode = findNodeByType(script, Token.LABEL);
      assertNotNull("LABEL node must exist", labelNode);
      Node switchNode = labelNode.getLastChild();
      assertEquals("Label must wrap a switch statement", Token.SWITCH, switchNode.getType());
      Node continueNode = findNodeByType(switchNode, Token.CONTINUE);
      assertNotNull("CONTINUE node must exist inside switch", continueNode);

      DiGraph.DiGraphNode<Node> cfgLabel = cfg.getNode(labelNode);
      DiGraph.DiGraphNode<Node> cfgSwitch = cfg.getNode(switchNode);
      DiGraph.DiGraphNode<Node> cfgContinue = cfg.getNode(continueNode);

      assertNotNull("Graph node for LABEL", cfgLabel);
      assertNotNull("Graph node for SWITCH", cfgSwitch);
      assertNotNull("Graph node for CONTINUE", cfgContinue);

      // The continue should NOT have an edge back to the switch's condition or to the label start.
      // It should either go to the switch's exit or cause an error.
      for (DiGraph.DiGraphEdge<Node, ControlFlowAnalysis.Branch> edge :
          cfgContinue.getOutEdges()) {
        assertFalse("Continue should not have an edge to the label start",
            edge.getDestination().equals(cfgLabel));
        assertFalse("Continue should not have an edge to the switch condition",
            edge.getDestination().equals(cfgSwitch));
      }
    } catch (Exception e) {
      // Acceptable if an error is thrown because the JavaScript is invalid.
      assertTrue("Exception should relate to continue/label: " + e.getMessage(),
          e.getMessage().toLowerCase().contains("continue") ||
          e.getMessage().toLowerCase().contains("label"));
    }
  }

  @Test
  public void testLabeledBreakInsideSwitch() {
    // 'break' with a label that is on the switch is valid.
    String js = "function f(a) { x: switch (a) { case 1: break x; } }";
    ControlFlowGraph<Node> cfg = computeCfg(js);
    assertNotNull("CFG must not be null", cfg);
  }

  // ======================== Helper methods ========================

  /** Searches depth‑first for the first node with the given token type. */
  private static Node findNodeByType(Node root, int type) {
    if (root == null) {
      return null;
    }
    if (root.getType() == type) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findNodeByType(child, type);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  // ======================== Negative tests (edge cases) ========================

  @Test(expected = NullPointerException.class)
  public void testNullCompiler() {
    new ControlFlowAnalysis(null, false, false);
  }

  @Test
  public void testEmptyFunction() {
    ControlFlowGraph<Node> cfg = computeCfg("function f() {}");
    assertNotNull(cfg);
    assertMinNodeCount(cfg, 2);
  }

  @Test
  public void testNestedLoops() {
    String js = "function f(a, b) { while (a) { while (b) { break; } } }";
    ControlFlowGraph<Node> cfg = computeCfg(js);
    assertNotNull(cfg);
  }

  @Test
  public void testTernaryOperator() {
    // The ternary is not a control flow statement but appears as an rvalue.
    String js = "function f(a) { return a ? 1 : 2; }";
    ControlFlowGraph<Node> cfg = computeCfg(js);
    assertNotNull(cfg);
  }
}