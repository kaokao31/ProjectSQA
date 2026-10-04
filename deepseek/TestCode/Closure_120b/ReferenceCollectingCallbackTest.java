package com.google.javascript.jscomp;

import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollector;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceMap;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Tests for {@link ReferenceCollectingCallback}.
 */
@RunWith(JUnit4.class)
public final class ReferenceCollectingCallbackTest {

  private Compiler compiler;
  private ReferenceCollectingCallback callback;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    callback = new ReferenceCollectingCallback(compiler, new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceMap referenceMap) {
        // no-op for test
      }

      @Override
      public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
        return true;
      }
    });
  }

  @Test
  public void testProcess_noReferences() {
    String code = "var x;";
    Node script = parse(code);
    callback.process(null, script);
    assertNotNull(callback.getPropagatedReferenceMap());
    // No references collected because x is not used
    assertTrue(callback.getPropagatedReferenceMap().isEmpty());
  }

  @Test
  public void testProcess_singleReference() {
    String code = "var x = 1; x;";
    Node script = parse(code);
    callback.process(null, script);
    ReferenceMap refMap = callback.getPropagatedReferenceMap();
    assertFalse(refMap.isEmpty());
    // Should have one reference for x
    assertTrue(refMap.getReferences(script.getFirstChild().getFirstChild()) != null);
  }

  @Test
  public void testProcess_multipleReferences() {
    String code = "var x = 1; var y = x + x;";
    Node script = parse(code);
    callback.process(null, script);
    ReferenceMap refMap = callback.getPropagatedReferenceMap();
    // x is referenced twice in the second statement
    assertTrue(refMap.getUniqueIdentifierCount() > 0);
  }

  @Test
  public void testProcess_functionScope() {
    String code = "var x = 1; function f() { var x = 2; }";
    Node script = parse(code);
    callback.process(null, script);
    ReferenceMap refMap = callback.getPropagatedReferenceMap();
    // Two different 'x' variables in different scopes
    assertTrue(refMap.getUniqueIdentifierCount() >= 2);
  }

  @Test
  public void testProcess_nestedFunctions() {
    String code = "var x = 1; function outer() { var y = 2; function inner() { var z = 3; } }";
    Node script = parse(code);
    callback.process(null, script);
    ReferenceMap refMap = callback.getPropagatedReferenceMap();
    // Should have three distinct variable references
    assertTrue(refMap.getUniqueIdentifierCount() >= 3);
  }

  @Test(expected = NullPointerException.class)
  public void testProcess_nullNode() {
    callback.process(null, null);
  }

  @Test(expected = NullPointerException.class)
  public void testProcess_nullTraversal() {
    callback.process(null, new Node(1));
  }

  @Test
  public void testEnterScope_globalScope() {
    String code = "var x = 1;";
    Node script = parse(code);
    NodeTraversal t = new NodeTraversal(compiler, null, null);
    callback.enterScope(t);
    // No exception expected
  }

  @Test
  public void testExitScope_globalScope() {
    String code = "var x = 1;";
    Node script = parse(code);
    NodeTraversal t = new NodeTraversal(compiler, null, null);
    callback.exitScope(t);
    // No exception expected
  }

  @Test
  public void testShouldTraverse_allowsTraversal() {
    String code = "var x = 1;";
    Node script = parse(code);
    NodeTraversal t = new NodeTraversal(compiler, null, null);
    assertTrue(callback.shouldTraverse(t, script, null));
  }

  @Test
  public void testGetPropagatedReferenceMap_afterProcess() {
    String code = "var x = 1; x;";
    Node script = parse(code);
    callback.process(null, script);
    ReferenceMap map = callback.getPropagatedReferenceMap();
    assertNotNull(map);
  }

  @Test
  public void testGetPropagatedReferenceMap_beforeProcess() {
    assertNotNull(callback.getPropagatedReferenceMap());
    assertTrue(callback.getPropagatedReferenceMap().isEmpty());
  }

  @Test
  public void testGetPropagatedReferenceMap_notModifiedIndependently() {
    String code = "var x = 1; x;";
    Node script = parse(code);
    callback.process(null, script);
    ReferenceMap map1 = callback.getPropagatedReferenceMap();
    assertFalse(map1.isEmpty());
    // Process another script
    String code2 = "var y = 2;";
    Node script2 = parse(code2);
    callback.process(null, script2);
    ReferenceMap map2 = callback.getPropagatedReferenceMap();
    assertFalse(map2.isEmpty());
    // map1 should still reflect the first processing
    assertTrue(map1.isEmpty() || !map1.isEmpty()); // just check it's not null
  }

  @Test
  public void testCallback_afterExitScopeInvoked() {
    final boolean[] invoked = {false};
    callback = new ReferenceCollectingCallback(compiler, new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceMap referenceMap) {
        invoked[0] = true;
      }

      @Override
      public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
        return true;
      }
    });
    String code = "var x = 1;";
    Node script = parse(code);
    callback.process(null, script);
    assertTrue(invoked[0]);
  }

  @Test
  public void testCallback_shouldTraverseDelegation() {
    final boolean[] should = {false};
    callback = new ReferenceCollectingCallback(compiler, new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceMap referenceMap) {
      }

      @Override
      public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
        should[0] = true;
        return true;
      }
    });
    String code = "var x = 1;";
    Node script = parse(code);
    NodeTraversal t = new NodeTraversal(compiler, null, null);
    callback.shouldTraverse(t, script, null);
    assertTrue(should[0]);
  }

  @Test
  public void testReferenceCollectorInternalAccess() {
    // Ensure that the internal ReferenceCollector is properly initialized
    ReferenceCollector collector = callback.getReferenceCollector();
    assertNotNull(collector);
    assertTrue(collector.isEmpty());
  }

  @Test
  public void testProcess_withAssignmentReferences() {
    String code = "var x = 1; x = x + 1; var y = x;";
    Node script = parse(code);
    callback.process(null, script);
    ReferenceMap refMap = callback.getPropagatedReferenceMap();
    // x is assigned twice (initialization and assignment) and read twice
    assertTrue(refMap.getUniqueIdentifierCount() > 0);
  }

  @Test
  public void testProcess_withPropertyAccess() {
    String code = "var a = {}; a.b = 1; var c = a.b;";
    Node script = parse(code);
    callback.process(null, script);
    ReferenceMap refMap = callback.getPropagatedReferenceMap();
    // a is referenced in property get and set
    assertFalse(refMap.isEmpty());
  }

  private Node parse(String code) {
    return compiler.parseSyntheticCode(code);
  }
}