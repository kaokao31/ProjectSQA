package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for TypeInference pass in Closure Compiler.
 * Targets maximum line/branch coverage and fault detection (Defects4J bug 35).
 */
public class TypeInferenceTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable type inference
    options.setInferTypes(true);
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT);
    options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT);
  }

  // Helper to compile and run type inference, then get typed AST
  private Node compileAndInfer(String code) {
    SourceFile input = SourceFile.fromCode("test.js", code);
    compiler.compile(
        SourceFile.fromCode("externs.js", "var window;"),
        input,
        options);
    return compiler.getRoot();
  }

  @Test
  public void testBasicNumberInference() {
    String js = "var x = 5;";
    Node root = compileAndInfer(js);
    // Verify that the variable x has type number
    Node varNode = findFirstVarNode(root, "x");
    assertNotNull("Variable x not found", varNode);
    Node numberNode = varNode.getFirstChild(); // the initializer
    assertNotNull("Initializer missing", numberNode);
    assertEquals("Expected type number", "number", numberNode.getJSType().toString());
  }

  @Test
  public void testStringInference() {
    String js = "var s = 'hello';";
    Node root = compileAndInfer(js);
    Node varNode = findFirstVarNode(root, "s");
    assertNotNull("Variable s not found", varNode);
    Node stringNode = varNode.getFirstChild();
    assertNotNull("Initializer missing", stringNode);
    assertEquals("Expected type string", "string", stringNode.getJSType().toString());
  }

  @Test
  public void testAdditionInference() {
    String js = "var a = 1 + 2;";
    Node root = compileAndInfer(js);
    Node varNode = findFirstVarNode(root, "a");
    assertNotNull("Variable a not found", varNode);
    Node addNode = varNode.getFirstChild();
    assertNotNull("Addition node missing", addNode);
    assertEquals("Expected type number", "number", addNode.getJSType().toString());
  }

  @Test
  public void testFunctionReturnType() {
    String js = "function f() { return 42; } var r = f();";
    Node root = compileAndInfer(js);
    Node varNode = findFirstVarNode(root, "r");
    assertNotNull("Variable r not found", varNode);
    Node callNode = varNode.getFirstChild();
    assertNotNull("Call node missing", callNode);
    assertEquals("Expected return type number", "number", callNode.getJSType().toString());
  }

  @Test
  public void testIfElseBranches() {
    String js = "var x; if (true) { x = 10; } else { x = 'str'; }";
    Node root = compileAndInfer(js);
    Node varNode = findFirstVarNode(root, "x");
    assertNotNull("Variable x not found", varNode);
    // After branch, x should be union of number and string
    Node decl = varNode.getFirstChild(); // could be empty if no initializer
    if (decl == null) {
      // x is declared without init, but assigned in branches; type should be union
      Node assign = findFirstAssignment(root, "x");
      assertNotNull("Assignment to x not found", assign);
      assertTrue("Type should be union", assign.getJSType().isUnionType());
    }
  }

  @Test
  public void testNullCatchBlock() {
    String js = "try { throw 'error'; } catch(e) { var y = e; }";
    Node root = compileAndInfer(js);
    Node varNode = findFirstVarNode(root, "y");
    assertNotNull("Variable y not found", varNode);
    Node assign = varNode.getFirstChild();
    assertNotNull("Assignment missing", assign);
    // e is the exception, usually type string or unknown
    assertNotNull("Type should not be null", assign.getJSType());
  }

  @Test
  public void testLoopVariableInference() {
    String js = "var sum = 0; for (var i = 0; i < 10; i++) { sum += i; }";
    Node root = compileAndInfer(js);
    Node varSum = findFirstVarNode(root, "sum");
    assertNotNull("Variable sum not found", varSum);
    // sum should be number
    Node initSum = varSum.getFirstChild();
    assertNotNull("Initializer missing", initSum);
    assertEquals("Expected type number", "number", initSum.getJSType().toString());
  }

  @Test
  public void testTypeNarrowingInIf() {
    String js = "var x = 'hello'; if (typeof x === 'string') { x = 1; }";
    Node root = compileAndInfer(js);
    // x after if should be union number? Actually assignment in branch
    Node varNode = findFirstVarNode(root, "x");
    assertNotNull("Variable x not found", varNode);
    Node assign = findFirstAssignment(root, "x");
    if (assign != null) {
      assertNotNull("Type should not be null", assign.getJSType());
    }
  }

  // Helper methods to find specific nodes (simplistic, assumes specific structure)
  private Node findFirstVarNode(Node root, String varName) {
    // Walk the tree to find a VAR node with child NAME matching varName
    // This is a naive traversal; real tests would use more robust utilities.
    // For brevity, we use recursion limited to small scripts.
    return findVarNodeRecursive(root, varName);
  }

  private Node findVarNodeRecursive(Node node, String varName) {
    if (node == null) return null;
    if (node.getToken() == Token.VAR) {
      Node child = node.getFirstChild();
      while (child != null) {
        if (child.getToken() == Token.NAME && varName.equals(child.getString())) {
          return node;
        }
        child = child.getNext();
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findVarNodeRecursive(child, varName);
      if (result != null) return result;
    }
    return null;
  }

  private Node findFirstAssignment(Node root, String varName) {
    // Find an ASSIGN node where left child is NAME with varName
    return findAssignRecursive(root, varName);
  }

  private Node findAssignRecursive(Node node, String varName) {
    if (node == null) return null;
    if (node.getToken() == Token.ASSIGN) {
      Node left = node.getFirstChild();
      if (left != null && left.getToken() == Token.NAME && varName.equals(left.getString())) {
        return node;
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findAssignRecursive(child, varName);
      if (result != null) return result;
    }
    return null;
  }
}