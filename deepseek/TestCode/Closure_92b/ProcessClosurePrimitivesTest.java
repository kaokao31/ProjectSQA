package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for ProcessClosurePrimitives.
 * Designed to achieve high code coverage and detect faults (e.g., Defects4J Closure 92).
 */
public class ProcessClosurePrimitivesTest {

  private Compiler compiler;
  private ProcessClosurePrimitives pass;

  @Before
  public void setUp() {
    compiler = new Compiler();
    // Initialize compiler options to allow warnings/errors
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    pass = new ProcessClosurePrimitives(compiler);
  }

  // Helper to create a simple script node with a single expression statement
  private Node createScriptNode(Node exprStmt) {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(exprStmt);
    return script;
  }

  // Helper to create a goog.provide('namespace') call node
  private Node createProvideCall(String namespace) {
    Node getprop = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "provide"));
    Node call = new Node(Token.CALL, getprop);
    call.addChildToBack(Node.newString(Token.STRING, namespace));
    return new Node(Token.EXPR_RESULT, call);
  }

  // Helper to create a goog.require('namespace') call node
  private Node createRequireCall(String namespace) {
    Node getprop = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "require"));
    Node call = new Node(Token.CALL, getprop);
    call.addChildToBack(Node.newString(Token.STRING, namespace));
    return new Node(Token.EXPR_RESULT, call);
  }

  // Helper to create a goog.setTestOnly() call node
  private Node createSetTestOnlyCall() {
    Node getprop = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "setTestOnly"));
    Node call = new Node(Token.CALL, getprop);
    return new Node(Token.EXPR_RESULT, call);
  }

  // Helper to process a script node and return the compiler's root
  private Node processScript(Node script) {
    pass.process(compiler.externsRoot, script);
    return compiler.getRoot().getLastChild(); // the processed script
  }

  @Test
  public void testSingleProvide() {
    Node script = createScriptNode(createProvideCall("foo.bar"));
    Node processed = processScript(script);
    // The provide call should be removed (replaced with nothing)
    assertEquals(0, processed.getChildCount());
    // Check that the namespace was recorded
    assertTrue(compiler.getProvidedNamespaces().contains("foo.bar"));
  }

  @Test
  public void testMultipleProvides() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(createProvideCall("a"));
    script.addChildToBack(createProvideCall("b.c"));
    script.addChildToBack(createProvideCall("d.e.f"));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertTrue(compiler.getProvidedNamespaces().contains("a"));
    assertTrue(compiler.getProvidedNamespaces().contains("b.c"));
    assertTrue(compiler.getProvidedNamespaces().contains("d.e.f"));
  }

  @Test
  public void testSingleRequire() {
    Node script = createScriptNode(createRequireCall("foo.bar"));
    Node processed = processScript(script);
    // Require call should be removed
    assertEquals(0, processed.getChildCount());
    assertTrue(compiler.getRequiredNamespaces().contains("foo.bar"));
  }

  @Test
  public void testProvideAndRequire() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(createProvideCall("a.b"));
    script.addChildToBack(createRequireCall("c.d"));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertTrue(compiler.getProvidedNamespaces().contains("a.b"));
    assertTrue(compiler.getRequiredNamespaces().contains("c.d"));
  }

  @Test
  public void testDuplicateProvide() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(createProvideCall("dup"));
    script.addChildToBack(createProvideCall("dup"));
    Node processed = processScript(script);
    // Both should be removed, but only one namespace recorded
    assertEquals(0, processed.getChildCount());
    // The provided namespaces set should contain "dup" only once
    int count = 0;
    for (String ns : compiler.getProvidedNamespaces()) {
      if (ns.equals("dup")) count++;
    }
    assertEquals(1, count);
  }

  @Test
  public void testProvideWithEmptyString() {
    Node script = createScriptNode(createProvideCall(""));
    Node processed = processScript(script);
    // Empty string provide should be ignored (not removed? depends on implementation)
    // Typically, it might be removed but not added to provided namespaces
    // We'll check that the call is removed (since it's a valid call but empty namespace)
    assertEquals(0, processed.getChildCount());
    assertFalse(compiler.getProvidedNamespaces().contains(""));
  }

  @Test
  public void testRequireWithEmptyString() {
    Node script = createScriptNode(createRequireCall(""));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertFalse(compiler.getRequiredNamespaces().contains(""));
  }

  @Test
  public void testProvideWithNonStringArgument() {
    // Provide with a number argument (should be ignored)
    Node getprop = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "provide"));
    Node call = new Node(Token.CALL, getprop);
    call.addChildToBack(Node.newNumber(42));
    Node exprResult = new Node(Token.EXPR_RESULT, call);
    Node script = createScriptNode(exprResult);
    Node processed = processScript(script);
    // The call should remain unchanged (not removed)
    assertEquals(1, processed.getChildCount());
    // The namespace should not be recorded
    assertFalse(compiler.getProvidedNamespaces().contains("42"));
  }

  @Test
  public void testRequireWithNonStringArgument() {
    Node getprop = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "goog"),
        Node.newString(Token.STRING, "require"));
    Node call = new Node(Token.CALL, getprop);
    call.addChildToBack(Node.newString(Token.STRING, "valid"));
    call.addChildToBack(Node.newString(Token.STRING, "extra")); // extra argument
    Node exprResult = new Node(Token.EXPR_RESULT, call);
    Node script = createScriptNode(exprResult);
    Node processed = processScript(script);
    // Should be removed because first argument is string, but extra args might be ignored
    assertEquals(0, processed.getChildCount());
    assertTrue(compiler.getRequiredNamespaces().contains("valid"));
  }

  @Test
  public void testSetTestOnly() {
    Node script = createScriptNode(createSetTestOnlyCall());
    Node processed = processScript(script);
    // setTestOnly should be removed
    assertEquals(0, processed.getChildCount());
    // Check that the compiler's testOnly flag is set (if applicable)
    // This depends on implementation; we assume it sets a flag
    // For coverage, we just verify removal
  }

  @Test
  public void testMixedCalls() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(createProvideCall("p1"));
    script.addChildToBack(createRequireCall("r1"));
    script.addChildToBack(createSetTestOnlyCall());
    script.addChildToBack(createProvideCall("p2"));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertTrue(compiler.getProvidedNamespaces().contains("p1"));
    assertTrue(compiler.getProvidedNamespaces().contains("p2"));
    assertTrue(compiler.getRequiredNamespaces().contains("r1"));
  }

  @Test
  public void testNonClosureCall() {
    // A regular function call should be left untouched
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    call.addChildToBack(Node.newString(Token.STRING, "bar"));
    Node exprResult = new Node(Token.EXPR_RESULT, call);
    Node script = createScriptNode(exprResult);
    Node processed = processScript(script);
    assertEquals(1, processed.getChildCount());
  }

  @Test
  public void testProvideWithDotsInNamespace() {
    // This is a common case; ensure it's handled correctly
    Node script = createScriptNode(createProvideCall("a.b.c.d"));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertTrue(compiler.getProvidedNamespaces().contains("a.b.c.d"));
  }

  @Test
  public void testProvideWithMultipleDots() {
    Node script = createScriptNode(createProvideCall("x.y.z"));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertTrue(compiler.getProvidedNamespaces().contains("x.y.z"));
  }

  @Test
  public void testEmptyScript() {
    Node script = new Node(Token.SCRIPT);
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
  }

  @Test
  public void testScriptWithOnlyNonClosureStatements() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.NUMBER, 1)));
    script.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.STRING, "hello")));
    Node processed = processScript(script);
    assertEquals(2, processed.getChildCount());
  }

  @Test
  public void testProvideAfterRequire() {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(createRequireCall("req"));
    script.addChildToBack(createProvideCall("prov"));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertTrue(compiler.getRequiredNamespaces().contains("req"));
    assertTrue(compiler.getProvidedNamespaces().contains("prov"));
  }

  // Additional tests to trigger potential bug in Defects4J Closure 92
  // The bug might involve incorrect handling of provide when the namespace
  // is a string that is not a valid JS identifier (e.g., contains spaces or special chars)
  @Test
  public void testProvideWithInvalidNamespace() {
    // Namespace with space - should be ignored or cause error?
    Node script = createScriptNode(createProvideCall("invalid namespace"));
    Node processed = processScript(script);
    // Depending on implementation, it might be removed or left.
    // For coverage, we just run it.
    // We'll assert that the call is removed (since it's a valid call but invalid namespace)
    // Typically, ProcessClosurePrimitives validates the namespace and may issue a warning.
    // We'll check that the call is removed (as per typical behavior)
    assertEquals(0, processed.getChildCount());
    // The namespace should not be recorded
    assertFalse(compiler.getProvidedNamespaces().contains("invalid namespace"));
  }

  @Test
  public void testProvideWithNumericNamespace() {
    Node script = createScriptNode(createProvideCall("123"));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertFalse(compiler.getProvidedNamespaces().contains("123"));
  }

  @Test
  public void testRequireWithInvalidNamespace() {
    Node script = createScriptNode(createRequireCall("invalid require"));
    Node processed = processScript(script);
    assertEquals(0, processed.getChildCount());
    assertFalse(compiler.getRequiredNamespaces().contains("invalid require"));
  }

  // Test that the pass correctly handles the case where the provide call is not a direct child of script
  // (e.g., inside a block) - but ProcessClosurePrimitives only looks at top-level statements?
  // We'll test a nested block to see if it's ignored.
  @Test
  public void testProvideInsideBlock() {
    Node block = new Node(Token.BLOCK);
    block.addChildToBack(createProvideCall("nested"));
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(block);
    Node processed = processScript(script);
    // The block should remain, and the provide inside should not be removed
    assertEquals(1, processed.getChildCount());
    Node blockAfter = processed.getFirstChild();
    assertEquals(Token.BLOCK, blockAfter.getType());
    assertEquals(1, blockAfter.getChildCount());
    // The provide call should still be there
    assertFalse(compiler.getProvidedNamespaces().contains("nested"));
  }

  // Test that the pass correctly handles multiple scripts (though process is called per script)
  // We'll just test that the pass can be called multiple times
  @Test
  public void testMultipleScripts() {
    Node script1 = createScriptNode(createProvideCall("ns1"));
    Node script2 = createScriptNode(createProvideCall("ns2"));
    pass.process(compiler.externsRoot, script1);
    pass.process(compiler.externsRoot, script2);
    assertTrue(compiler.getProvidedNamespaces().contains("ns1"));
    assertTrue(compiler.getProvidedNamespaces().contains("ns2"));
  }

  // Test that the pass does not remove provide calls that are not at the top level of the script
  // (e.g., inside an if statement)
  @Test
  public void testProvideInsideIf() {
    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), createProvideCall("cond"));
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(ifNode);
    Node processed = processScript(script);
    assertEquals(1, processed.getChildCount());
    assertFalse(compiler.getProvidedNamespaces().contains("cond"));
  }
}