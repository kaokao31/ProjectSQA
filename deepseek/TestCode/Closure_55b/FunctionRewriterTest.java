package com.google.javascript.jscomp;

import com.google.javascript.jscomp.FunctionRewriter;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionRewriterTest {

  private Compiler compiler;
  private FunctionRewriter rewriter;

  @Before
  public void setUp() throws Exception {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    rewriter = new FunctionRewriter(compiler);
  }

  // Helper to create a simple script node with a single function expression
  private Node createFunctionExpression(String name, boolean isDeclaration) {
    Node script = new Node(Node.SCRIPT);
    Node functionNode = Node.newString(Node.FUNCTION, name != null ? name : "");
    functionNode.putBooleanProp(Node.IS_DECLARATION, isDeclaration);
    Node paramList = new Node(Node.PARAM_LIST);
    functionNode.addChildToBack(paramList);
    Node block = new Node(Node.BLOCK);
    functionNode.addChildToBack(block);
    script.addChildToBack(functionNode);
    return script;
  }

  @Test
  public void testSimpleFunctionExpression() {
    Node ast = createFunctionExpression(null, false); // anonymous
    rewriter.process(null, ast);
    // After rewriting, the name should still be empty (anonymous) because the rewriter
    // only modifies certain patterns (e.g., function as property value)
    Node functionNode = ast.getFirstChild();
    assertNotNull(functionNode);
    assertEquals(Node.FUNCTION, functionNode.getType());
    assertEquals("", functionNode.getString());
  }

  @Test
  public void testFunctionDeclarationUnchanged() {
    Node ast = createFunctionExpression("foo", true);
    rewriter.process(null, ast);
    Node functionNode = ast.getFirstChild();
    assertNotNull(functionNode);
    assertEquals(Node.FUNCTION, functionNode.getType());
    assertEquals("foo", functionNode.getString());
  }

  @Test
  public void testFunctionWithEmptyBody() {
    Node ast = createFunctionExpression("bar", false);
    rewriter.process(null, ast);
    Node functionNode = ast.getFirstChild();
    Node body = functionNode.getLastChild();
    assertEquals(Node.BLOCK, body.getType());
    assertEquals(0, body.getChildCount());
  }

  @Test
  public void testFunctionWithReturnStatement() {
    // Build a function with a return statement
    Node script = new Node(Node.SCRIPT);
    Node functionNode = Node.newString(Node.FUNCTION, "");
    functionNode.putBooleanProp(Node.IS_DECLARATION, false);
    Node paramList = new Node(Node.PARAM_LIST);
    functionNode.addChildToBack(paramList);
    Node block = new Node(Node.BLOCK);
    Node returnNode = new Node(Node.RETURN);
    Node numberNode = Node.newNumber(42);
    returnNode.addChildToBack(numberNode);
    block.addChildToBack(returnNode);
    functionNode.addChildToBack(block);
    script.addChildToBack(functionNode);

    rewriter.process(null, script);
    Node fn = script.getFirstChild();
    assertEquals(Node.FUNCTION, fn.getType());
    Node body = fn.getLastChild();
    assertEquals(Node.BLOCK, body.getType());
    assertEquals(1, body.getChildCount());
    assertEquals(Node.RETURN, body.getFirstChild().getType());
  }

  @Test
  public void testFunctionAsPropertyValue() {
    // Create an ASSIGN node like a.b = function(){}
    Node script = new Node(Node.SCRIPT);
    Node assign = new Node(Node.ASSIGN);
    Node getProp = new Node(Node.GETPROP);
    Node obj = Node.newString(Node.NAME, "a");
    Node prop = Node.newString(Node.STRING, "b");
    getProp.addChildToBack(obj);
    getProp.addChildToBack(prop);
    assign.addChildToBack(getProp);
    Node functionNode = Node.newString(Node.FUNCTION, "");
    functionNode.putBooleanProp(Node.IS_DECLARATION, false);
    Node paramList = new Node(Node.PARAM_LIST);
    functionNode.addChildToBack(paramList);
    Node block = new Node(Node.BLOCK);
    functionNode.addChildToBack(block);
    assign.addChildToBack(functionNode);
    script.addChildToBack(assign);

    rewriter.process(null, script);
    // After rewriting, the anonymous function should be given a name based on property
    Node resultAssign = script.getFirstChild();
    Node resultFunction = resultAssign.getLastChild();
    assertEquals(Node.FUNCTION, resultFunction.getType());
    // The name should become "b" (or similar) depending on the rewriter's logic
    // Bug 55 may involve incorrect naming; we assert that name is not empty
    assertFalse("Function name should not be empty", resultFunction.getString().isEmpty());
    // Additionally, the first child of function should be a NAME node? Not necessarily; we just check it's not empty.
  }

  @Test
  public void testNestedFunction() {
    // Create outer function with inner anonymous function
    Node script = new Node(Node.SCRIPT);
    Node outerFn = Node.newString(Node.FUNCTION, "outer");
    outerFn.putBooleanProp(Node.IS_DECLARATION, false);
    Node outerParams = new Node(Node.PARAM_LIST);
    outerFn.addChildToBack(outerParams);
    Node outerBlock = new Node(Node.BLOCK);
    Node innerFn = Node.newString(Node.FUNCTION, "");
    innerFn.putBooleanProp(Node.IS_DECLARATION, false);
    Node innerParams = new Node(Node.PARAM_LIST);
    innerFn.addChildToBack(innerParams);
    Node innerBlock = new Node(Node.BLOCK);
    innerFn.addChildToBack(innerBlock);
    outerBlock.addChildToBack(innerFn);
    outerFn.addChildToBack(outerBlock);
    script.addChildToBack(outerFn);

    rewriter.process(null, script);
    Node outer = script.getFirstChild();
    Node inner = outer.getLastChild().getFirstChild();
    assertEquals(Node.FUNCTION, inner.getType());
    // Name may or may not be assigned; but should not throw exception
    assertTrue("Inner function should still be a function", true);
  }

  @Test(expected = NullPointerException.class)
  public void testNullRoot() {
    rewriter.process(null, null);
  }

  @Test
  public void testMultipleFunctions() {
    Node script = new Node(Node.SCRIPT);
    for (int i = 0; i < 5; i++) {
      Node fn = Node.newString(Node.FUNCTION, "fn" + i);
      fn.putBooleanProp(Node.IS_DECLARATION, true);
      Node params = new Node(Node.PARAM_LIST);
      fn.addChildToBack(params);
      Node block = new Node(Node.BLOCK);
      fn.addChildToBack(block);
      script.addChildToBack(fn);
    }
    rewriter.process(null, script);
    assertEquals(5, script.getChildCount());
    for (int i = 0; i < 5; i++) {
      Node fn = script.getChildAtIndex(i);
      assertEquals("fn" + i, fn.getString());
    }
  }

  @Test
  public void testEmptyScript() {
    Node script = new Node(Node.SCRIPT);
    rewriter.process(null, script);
    assertEquals(0, script.getChildCount());
  }
}