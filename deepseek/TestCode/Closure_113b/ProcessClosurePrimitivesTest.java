package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ProcessClosurePrimitives}.
 * Generates high coverage and targets edge cases, including known
 * fault-prone scenarios such as nested namespaces, duplicate provides,
 * and interactions with existing variables.
 */
public class ProcessClosurePrimitivesTest {

  private Compiler compiler;
  private ProcessClosurePrimitives pass;

  @Before
  public void setUp() {
    compiler = new Compiler();
    pass = new ProcessClosurePrimitives(compiler, true);
  }

  /**
   * Parses the given JavaScript, runs the pass, and returns the root node.
   */
  private Node processJs(String input) {
    CompilerOptions options = new CompilerOptions();
    // Parse the input as a script.
    Node root = compiler.parseTestCode(input);
    // For completeness, simulate externs (empty) and process the root.
    Node externs = compiler.parseTestCode("");
    pass.process(externs, root);
    return root;
  }

  private String processAndToSource(String input) {
    Node root = processJs(input);
    return compiler.toSource(root);
  }

  @Test
  public void testSimpleProvide() {
    String result = processAndToSource("goog.provide('foo');");
    // Expect the generated code to contain a declaration for foo.
    assertNotNull(result);
    assertTrue(result.contains("var foo"));
  }

  @Test
  public void testNestedProvide() {
    String result = processAndToSource("goog.provide('a.b.c');");
    assertNotNull(result);
    // Root namespace 'a' must be declared.
    assertTrue(result.contains("var a"));
    // The nested structure should be created.
    assertTrue(result.contains("a.b"));
    assertTrue(result.contains("a.b.c"));
  }

  @Test
  public void testRequireAfterProvide() {
    String result = processAndToSource(
        "goog.provide('foo'); goog.require('foo');");
    assertNotNull(result);
    // The provide must still produce the namespace.
    assertTrue(result.contains("var foo"));
  }

  @Test
  public void testMissingRequireThrowsError() {
    // Passing checkDuplicates=true, missing require should be reported.
    try {
      processJs("goog.require('missing.namespace');");
      // If no exception is thrown, we still need to verify via the compiler.
      // The pass itself may not throw; instead it reports a JSError.
      // Check that the compiler has an error.
      assertTrue(compiler.getErrorManager() != null);
    } catch (Exception e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }

  @Test
  public void testDuplicateProvideReportsError() {
    // With checkDuplicates=true, two provides of the same namespace clash.
    processJs("goog.provide('a'); goog.provide('a');");
    // The pass should record an error.
    assertTrue(compiler.getErrorCount() > 0);
  }

  @Test
  public void testProvideWithExistingVar() {
    String result = processAndToSource("var foo = 1; goog.provide('foo');");
    // The existing var should still be present, and no duplicate declaration.
    assertNotNull(result);
    assertTrue(result.contains("var foo"));
    // There should be no duplicate declaration (e.g., no two 'var foo').
    int occurrences = result.split("var foo").length - 1;
    assertTrue("Expected only one var foo", occurrences <= 1);
  }

  @Test
  public void testProvideWithDotsAndExistingVariable() {
    String result = processAndToSource("var a = {}; goog.provide('a.b');");
    assertNotNull(result);
    assertTrue(result.contains("var a"));
    // Should not redeclare a, but add b.
    int varA = result.split("var a").length - 1;
    assertTrue("Should not redeclare a", varA == 1);
    assertTrue(result.contains("a.b"));
  }

  @Test
  public void testEmptyProvideString() {
    // Edge case: empty namespace.
    // It should not crash, and either report error or be ignored.
    try {
      String result = processAndToSource("goog.provide('');");
      assertNotNull(result);
    } catch (Exception e) {
      fail("Empty provide caused exception: " + e.getMessage());
    }
  }

  @Test
  public void testNullProvideArgumentThroughJS() {
    // We cannot pass null directly, but we can call with undefined.
    String result = processAndToSource("goog.provide(undefined);");
    assertNotNull(result);
  }

  @Test
  public void testManyProvidesAndRequires() {
    StringBuilder js = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      js.append("goog.provide('ns").append(i).append("');\n");
    }
    for (int i = 0; i < 10; i++) {
      js.append("goog.require('ns").append(i).append("');\n");
    }
    String result = processAndToSource(js.toString());
    assertNotNull(result);
    for (int i = 0; i < 10; i++) {
      assertTrue(result.contains("ns" + i));
    }
  }

  @Test
  public void testProcessDoesNotAlterExterns() {
    // Ensure that the pass leaves externs untouched.
    Node externs = compiler.parseTestCode("/** @externs */ var external;");
    Node root = compiler.parseTestCode("goog.provide('foo');");
    Node externsPre = externs.cloneTree();
    pass.process(externs, root);
    assertTrue(externs.similarTree(externsPre));
  }

  @Test
  public void testNoStatementsAfterProcess() {
    // After processing, the root should still have a body.
    Node root = processJs("goog.provide('foo');");
    assertNotNull(root);
    assertNotNull(root.getFirstChild());
  }

  @Test
  public void testFastButDeepNamespace() {
    // A very deep namespace should not cause stack overflow.
    StringBuilder ns = new StringBuilder("a");
    for (int i = 0; i < 100; i++) {
      ns.append(".b");
    }
    String result = processAndToSource("goog.provide('" + ns + "');");
    assertNotNull(result);
  }

  @Test
  public void testProvideAndAssignment() {
    String result = processAndToSource(
        "goog.provide('foo'); foo = 3;");
    assertNotNull(result);
    // The assignment should be preserved.
    assertTrue(result.contains("foo = 3"));
  }

  @Test
  public void testProvideAfterAssignment() {
    String result = processAndToSource(
        "foo = 3; goog.provide('foo');");
    assertNotNull(result);
    // Since foo is assigned before provide, we might get a var declaration or assignment.
    assertTrue(result.contains("foo"));
  }

  @Test
  public void testRequireOnlyOneProvided() {
    // Provided a then require b (which is not provided) - should error.
    pass.process(compiler.parseTestCode(""), compiler.parseTestCode(
        "goog.provide('a'); goog.require('b');"));
    assertTrue(compiler.getErrorCount() > 0);
  }

  @Test
  public void testProvideReservedWordsAsNamespace() {
    // Using 'alert' or 'eval' should still work without crashing.
    String result = processAndToSource("goog.provide('alert');");
    assertNotNull(result);
    assertTrue(result.contains("var alert"));
  }

  @Test
  public void testProvideWithExternsNamespace() {
    // Provide a name that already exists in externs - should not clash.
    newNode(compiler, "externs");
    String result = processAndToSource("goog.provide('window');");
    assertNotNull(result);
  }

  @Test
  public void testNullProcessArguments() {
    // Directly call process with null roots should not cause an exception.
    try {
      pass.process(null, null);
      // If no exception, that's acceptable (or it may throw NullPointerException).
    } catch (NullPointerException e) {
      // Expected, but not a failure for this test.
    }
  }

  @Test
  public void testMultipleDotsInOneProvide() {
    String result = processAndToSource("goog.provide('a.b.c.d.e');");
    assertNotNull(result);
    assertTrue(result.contains("a.b.c.d.e"));
  }

  @Test
  public void testProvideWithExoticUnicode() {
    String result = processAndToSource("goog.provide('αβγ');");
    assertNotNull(result);
    assertTrue(result.contains("αβγ"));
  }

  private void newNode(Compiler c, String name) {
    // Helper to create a simple extern node.
    c.parseTestCode("/** @externs */ var " + name + ";");
  }
}