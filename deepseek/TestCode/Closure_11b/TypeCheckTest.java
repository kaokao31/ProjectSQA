package com.google.javascript.jscomp;

import static com.google.javascript.rhino.Token.NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.NodeUtil;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.SimpleSlot;
import com.google.javascript.rhino.jstype.StaticSlot;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class TypeCheckTest {

  private Compiler compiler;
  private Node root;
  private TypeCheck typeCheck;

  @Before
  public void setUp() throws Exception {
    compiler = new Compiler();
    typeCheck = new TypeCheck(compiler);
  }

  private Node parse(String code) {
    return compiler.parse(compiler.parseSyntheticCode("test", code));
  }

  private void process(Node node) {
    typeCheck.process(node);
  }

  private JSType getType(Node node) {
    return node.getJSType();
  }

  private void assertTypeEquals(String expected, Node node) {
    assertEquals(expected, getType(node).toString());
  }

  // Test for buggy path: TypeCheck should handle empty statement blocks
  @Test
  public void testEmptyBlock() {
    Node block = new Node(Token.BLOCK);
    process(block);
    // No exception should be thrown
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck should handle functions with object literal expressions
  @Test
  public void testObjectLiteralAsExpression() {
    Node objLit = new Node(Token.OBJECTLIT);
    objLit.addChildToBack(new Node(Token.STRING, "key"));
    Node expr = new Node(Token.EXPR_RESULT, objLit);
    Node script = new Node(Token.SCRIPT, expr);
    process(script);
    // No exception should be thrown
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck should handle for-in loops with unknown object types
  @Test
  public void testForInLoop() {
    Node forIn = new Node(Token.FOR);
    Node varDecl = new Node(Token.VAR);
    Node name = new Node(Token.NAME, "i");
    varDecl.addChildToBack(name);
    Node obj = new Node(Token.NAME, "obj");
    Node block = new Node(Token.BLOCK);
    forIn.addChildToBack(varDecl);
    forIn.addChildToBack(obj);
    forIn.addChildToBack(block);
    process(forIn);
    // No exception should be thrown
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles ternary operator with boolean condition
  @Test
  public void testTernaryOperator() {
    Node cond = new Node(Token.HOOK);
    Node ifTrue = new Node(Token.NUMBER, 1.0);
    Node ifFalse = new Node(Token.NUMBER, 2.0);
    cond.addChildToBack(new Node(Token.TRUE));
    cond.addChildToBack(ifTrue);
    cond.addChildToBack(ifFalse);
    process(new Node(Token.SCRIPT, new Node(Token.EXPR_RESULT, cond)));
    assertNotNull(cond.getJSType());
  }

  // Test for buggy path: TypeCheck handles Array literals with various element types
  @Test
  public void testArrayLiteral() {
    Node arrayLit = new Node(Token.ARRAYLIT);
    arrayLit.addChildToBack(new Node(Token.NUMBER, 1.0));
    arrayLit.addChildToBack(new Node(Token.STRING, "test"));
    arrayLit.addChildToBack(new Node(Token.TRUE));
    Node expr = new Node(Token.EXPR_RESULT, arrayLit);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(arrayLit.getJSType());
  }

  // Test for buggy path: TypeCheck handles nested function declarations correctly
  @Test
  public void testNestedFunctionDeclarations() {
    Node outerFunc = new Node(Token.FUNCTION);
    Node body = new Node(Token.BLOCK);
    Node innerFunc = new Node(Token.FUNCTION);
    innerFunc.addChildToBack(new Node(Token.NAME, ""));
    Node innerBody = new Node(Token.BLOCK);
    innerFunc.addChildToBack(innerBody);
    body.addChildToBack(innerFunc);
    outerFunc.addChildToBack(new Node(Token.NAME, "outer"));
    outerFunc.addChildToBack(body);
    process(outerFunc);
    // Should not throw NPE
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck should handle declared functions with no body
  @Test
  public void testDeclaredFunctionNoBody() {
    Node func = new Node(Token.FUNCTION);
    func.addChildToBack(new Node(Token.NAME, "f"));
    Node body = new Node(Token.BLOCK);
    func.addChildToBack(body);
    process(func);
    assertNotNull(func.getJSType());
  }

  // Test for buggy path: TypeCheck handles infinite loops without crashing
  @Test
  public void testInfiniteLoop() {
    Node whileNode = new Node(Token.WHILE);
    whileNode.addChildToBack(new Node(Token.TRUE));
    whileNode.addChildToBack(new Node(Token.BLOCK));
    process(new Node(Token.SCRIPT, whileNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles function calls with undefined arguments
  @Test
  public void testUndefinedArguments() {
    Node call = new Node(Token.CALL);
    Node callee = new Node(Token.NAME, "foo");
    call.addChildToBack(callee);
    call.addChildToBack(new Node(Token.VOID, new Node(Token.NUMBER, 0.0)));
    Node expr = new Node(Token.EXPR_RESULT, call);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(call.getJSType());
  }

  // Test for buggy path: TypeCheck handles getprop with unresolved types
  @Test
  public void testGetPropUnresolvedBase() {
    Node getProp = new Node(Token.GETPROP);
    Node base = new Node(Token.NAME, "x");
    getProp.addChildToBack(base);
    getProp.addChildToBack(new Node(Token.STRING, "prop"));
    Node expr = new Node(Token.EXPR_RESULT, getProp);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(getProp.getJSType());
  }

  // Test for buggy path: TypeCheck handles typeof operator with unknown operand
  @Test
  public void testTypeOfOperator() {
    Node typeofNode = new Node(Token.TYPEOF);
    typeofNode.addChildToBack(new Node(Token.NAME, "z"));
    Node expr = new Node(Token.EXPR_RESULT, typeofNode);
    process(new Node(Token.SCRIPT, expr));
    assertEquals("string", getType(typeofNode).toString());
  }

  // Test for buggy path: TypeCheck handles regexp literals
  @Test
  public void testRegexpLiteral() {
    Node regexp = new Node(Token.REGEXP);
    regexp.addChildToBack(new Node(Token.STRING, "pattern"));
    regexp.addChildToBack(new Node(Token.STRING, "g"));
    Node expr = new Node(Token.EXPR_RESULT, regexp);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(regexp.getJSType());
  }

  // Test for buggy path: TypeCheck handles 'new' expressions with no constructor type
  @Test
  public void testNewExpressionUnknownConstructor() {
    Node newExpr = new Node(Token.NEW);
    newExpr.addChildToBack(new Node(Token.NAME, "Foo"));
    Node expr = new Node(Token.EXPR_RESULT, newExpr);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(newExpr.getJSType());
  }

  // Test for buggy path: TypeCheck handles switch statements with multiple cases
  @Test
  public void testSwitchStatement() {
    Node switchNode = new Node(Token.SWITCH);
    Node cond = new Node(Token.NUMBER, 1.0);
    switchNode.addChildToBack(cond);
    for (int i = 0; i < 3; i++) {
      Node caseNode = new Node(Token.CASE);
      caseNode.addChildToBack(new Node(Token.NUMBER, (double) i));
      caseNode.addChildToBack(new Node(Token.BLOCK));
      switchNode.addChildToBack(caseNode);
    }
    process(new Node(Token.SCRIPT, switchNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles try-catch-finally blocks
  @Test
  public void testTryCatchFinally() {
    Node tryNode = new Node(Token.TRY);
    Node body = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH);
    Node catchVar = new Node(Token.NAME, "e");
    Node catchBody = new Node(Token.BLOCK);
    catchNode.addChildToBack(catchVar);
    catchNode.addChildToBack(catchBody);
    Node finallyNode = new Node(Token.BLOCK);
    tryNode.addChildToBack(body);
    tryNode.addChildToBack(catchNode);
    tryNode.addChildToBack(finallyNode);
    process(new Node(Token.SCRIPT, tryNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles variable declarations with no initializer
  @Test
  public void testVarDeclNoInit() {
    Node varDecl = new Node(Token.VAR);
    Node name = new Node(Token.NAME, "a");
    varDecl.addChildToBack(name);
    process(new Node(Token.SCRIPT, varDecl));
    assertNull(getType(name));
  }

  // Test for buggy path: TypeCheck handles comma expressions
  @Test
  public void testCommaExpression() {
    Node comma = new Node(Token.COMMA);
    comma.addChildToBack(new Node(Token.NUMBER, 1.0));
    comma.addChildToBack(new Node(Token.NUMBER, 2.0));
    Node expr = new Node(Token.EXPR_RESULT, comma);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(comma.getJSType());
  }

  // Test for buggy path: TypeCheck handles assignment with destructuring (when applicable)
  @Test
  public void testAssignmentDestructuring() {
    Node assign = new Node(Token.ASSIGN);
    Node destPattern = new Node(Token.ARRAYLIT);
    destPattern.addChildToBack(new Node(Token.NAME, "a"));
    destPattern.addChildToBack(new Node(Token.NAME, "b"));
    assign.addChildToBack(destPattern);
    assign.addChildToBack(new Node(Token.NAME, "arr"));
    Node expr = new Node(Token.EXPR_RESULT, assign);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(assign.getJSType());
  }

  // Test for buggy path: TypeCheck handles 'for' loop with var declaration
  @Test
  public void testForLoopWithVarDeclaration() {
    Node forNode = new Node(Token.FOR);
    Node init = new Node(Token.VAR);
    init.addChildToBack(new Node(Token.NAME, "i"));
    init.addChildToBack(new Node(Token.NUMBER, 0.0));
    Node cond = new Node(Token.LT);
    cond.addChildToBack(new Node(Token.NAME, "i"));
    cond.addChildToBack(new Node(Token.NUMBER, 10.0));
    Node incr = new Node(Token.INC, new Node(Token.NAME, "i"));
    Node body = new Node(Token.BLOCK);
    forNode.addChildToBack(init);
    forNode.addChildToBack(cond);
    forNode.addChildToBack(incr);
    forNode.addChildToBack(body);
    process(new Node(Token.SCRIPT, forNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles 'with' statements
  @Test
  public void testWithStatement() {
    Node withNode = new Node(Token.WITH);
    Node obj = new Node(Token.NAME, "obj");
    Node body = new Node(Token.BLOCK);
    withNode.addChildToBack(obj);
    withNode.addChildToBack(body);
    process(new Node(Token.SCRIPT, withNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles 'finally' block without catch
  @Test
  public void testTryFinallyNoCatch() {
    Node tryNode = new Node(Token.TRY);
    Node tryBody = new Node(Token.BLOCK);
    Node finallyNode = new Node(Token.BLOCK);
    tryNode.addChildToBack(tryBody);
    tryNode.addChildToBack(finallyNode);
    process(new Node(Token.SCRIPT, tryNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles label statements
  @Test
  public void testLabelStatement() {
    Node labelNode = new Node(Token.LABEL);
    Node name = new Node(Token.NAME, "loop");
    Node statement = new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, 1.0));
    labelNode.addChildToBack(name);
    labelNode.addChildToBack(statement);
    process(new Node(Token.SCRIPT, labelNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles 'break' and 'continue' statements
  @Test
  public void testBreakContinue() {
    Node breakNode = new Node(Token.BREAK);
    Node continueNode = new Node(Token.CONTINUE);
    Node block = new Node(Token.BLOCK);
    block.addChildToBack(breakNode);
    block.addChildToBack(continueNode);
    process(new Node(Token.SCRIPT, block));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles 'debugger' statement
  @Test
  public void testDebuggerStatement() {
    Node debuggerNode = new Node(Token.DEBUGGER);
    process(new Node(Token.SCRIPT, debuggerNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles empty program
  @Test
  public void testEmptyProgram() {
    process(new Node(Token.SCRIPT));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles Node with multiple child scripts
  @Test
  public void testMultipleScripts() {
    Node script1 = new Node(Token.SCRIPT);
    Node script2 = new Node(Token.SCRIPT);
    script1.addChildToBack(new Node(Token.NUMBER, 1.0));
    script2.addChildToBack(new Node(Token.NUMBER, 2.0));
    Node block = new Node(Token.BLOCK);
    block.addChildToBack(script1);
    block.addChildToBack(script2);
    process(block);
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles function hoisting
  @Test
  public void testFunctionHoisting() {
    Node script = new Node(Token.SCRIPT);
    Node funcDecl = new Node(Token.FUNCTION);
    funcDecl.addChildToBack(new Node(Token.NAME, "hoisted"));
    funcDecl.addChildToBack(new Node(Token.BLOCK));
    script.addChildToBack(funcDecl);
    script.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.NAME, "hoisted")));
    process(script);
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles parameter default values (if supported)
  @Test
  public void testParamDefaultValues() {
    Node func = new Node(Token.FUNCTION);
    func.addChildToBack(new Node(Token.NAME, "f"));
    Node params = new Node(Token.PARAM_LIST);
    Node param1 = new Node(Token.NAME, "a");
    Node param2 = new Node(Token.NAME, "b");
    params.addChildToBack(param1);
    params.addChildToBack(param2);
    Node body = new Node(Token.BLOCK);
    func.addChildToBack(params);
    func.addChildToBack(body);
    process(func);
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles catch with no exception object
  @Test
  public void testCatchOnly() {
    Node tryNode = new Node(Token.TRY);
    Node tryBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH);
    Node catchVar = new Node(Token.NAME, "e");
    Node catchBody = new Node(Token.BLOCK);
    catchNode.addChildToBack(catchVar);
    catchNode.addChildToBack(catchBody);
    tryNode.addChildToBack(tryBody);
    tryNode.addChildToBack(catchNode);
    process(new Node(Token.SCRIPT, tryNode));
    assertTrue(true);
  }

  // Test for buggy path: TypeCheck handles 'this' at global scope
  @Test
  public void testThisAtGlobalScope() {
    Node thisNode = new Node(Token.THIS);
    Node expr = new Node(Token.EXPR_RESULT, thisNode);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(thisNode.getJSType());
  }

  // Test for buggy path: TypeCheck handles 'new' with function expression
  @Test
  public void testNewWithFunctionExpression() {
    Node newExpr = new Node(Token.NEW);
    Node funcExpr = new Node(Token.FUNCTION);
    funcExpr.addChildToBack(new Node(Token.NAME, ""));
    funcExpr.addChildToBack(new Node(Token.BLOCK));
    newExpr.addChildToBack(funcExpr);
    Node expr = new Node(Token.EXPR_RESULT, newExpr);
    process(new Node(Token.SCRIPT, expr));
    assertNotNull(newExpr.getJSType());
  }

  // Test for buggy path: TypeCheck handles large numeric values
  @Test
  public void testLargeNumbers() {
    Node bigNumber = new Node(Token.NUMBER, Double.MAX_VALUE);
    Node expr = new Node(Token.EXPR_RESULT, bigNumber);
    process(new Node(Token.SCRIPT, expr));
    assertEquals("number", getType(bigNumber).toString());
  }

  // Test for buggy path: TypeCheck handles NaN and Infinity
  @Test
  public void testSpecialNumbers() {
    Node nan = new Node(Token.NUMBER, Double.NaN);
    Node inf = new Node(Token.NUMBER, Double.POSITIVE_INFINITY);
    Node negInf = new Node(Token.NUMBER, Double.NEGATIVE_INFINITY);
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(new Node(Token.EXPR_RESULT, nan));
    script.addChildToBack(new Node(Token.EXPR_RESULT, inf));
    script.addChildToBack(new Node(Token.EXPR_RESULT, negInf));
    process(script);
    assertEquals("number", getType(nan).toString());
    assertEquals("number", getType(inf).toString());
    assertEquals("number", getType(negInf).toString());
  }

  // Test for buggy path: TypeCheck handles string literals with special characters
  @Test
  public void testSpecialCharsInString() {
    Node str = new Node(Token.STRING, "hello\nworld\t!");
    Node expr = new Node(Token.EXPR_RESULT, str);
    process(new Node(Token.SCRIPT, expr));
    assertEquals("string", getType(str).toString());
  }

  // Test for buggy path: TypeCheck handles boolean literals
  @Test
  public void testBooleanLiterals() {
    Node trueNode = new Node(Token.TRUE);
    Node falseNode = new Node(Token.FALSE);
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(new Node(Token.EXPR_RESULT, trueNode));
    script.addChildToBack(new Node(Token.EXPR_RESULT, falseNode));
    process(script);
    assertEquals("boolean", getType(trueNode).toString());
    assertEquals("boolean", getType(falseNode).toString());
  }

  // Test for buggy path: TypeCheck handles 'void 0'
  @Test
  public void testVoidExpression() {
    Node voidNode = new Node(Token.VOID, new Node(Token.NUMBER, 0.0));
    Node expr = new Node(Token.EXPR_RESULT, voidNode);
    process(new Node(Token.SCRIPT, expr));
    assertEquals("undefined", getType(voidNode).toString());
  }

  // Test for buggy path: TypeCheck handles 'typeof' with all operand types
  @Test
  public void testTypeOfAllOperands() {
    Node[] operands = {new Node(Token.NUMBER, 1.0), new Node(Token.STRING, "s"),
                       new Node(Token.TRUE), new Node(Token.THIS),
                       new Node(Token.VOID, new Node(Token.NUMBER, 0.0))};
    for (Node operand : operands) {
      Node typeofNode = new Node(Token.TYPEOF, operand);
      Node expr = new Node(Token.EXPR_RESULT, typeofNode);
      process(new Node(Token.SCRIPT, expr));
      assertEquals("string", getType(typeofNode).toString());
    }
  }

  // Test for buggy path: TypeCheck handles 'delete' operator
  @Test
  public void testDeleteOperator() {
    Node deleteNode = new Node(Token.DELPROP, new Node(Token.NAME, "x"));
    Node expr = new Node(Token.EXPR_RESULT, deleteNode);
    process(new Node(Token.SCRIPT, expr));
    assertEquals("boolean", getType(deleteNode).toString());
  }

  // Test for buggy path: TypeCheck handles unary operators
  @Test
  public void testUnaryOperators() {
    Node[] unaryNodes = {new Node(Token.POS, new Node(Token.NUMBER, 1.0)),
                         new Node(Token.NEG, new Node(Token.NUMBER, 1.0)),
                         new Node(Token.BITNOT, new Node(Token.NUMBER, 1.0)),
                         new Node(Token.NOT, new Node(Token.TRUE)),
                         new Node(Token.INC, new Node(Token.NAME, "x")),
                         new Node(Token.DEC, new Node(Token.NAME, "y"))};
    for (Node node : unaryNodes) {
      process(new Node(Token.SCRIPT, new Node(Token.EXPR_RESULT, node)));
      assertNotNull("Unary node type should not be null", node.getJSType());
    }
  }

  // Test for buggy path: TypeCheck handles binary operators
  @Test
  public void testBinaryOperators() {
    Node[] binOps = {Token.ADD, Token.SUB, Token.MUL, Token.DIV, Token.MOD,
                     Token.SHL, Token.SHR, Token.USHR, Token.BITAND, Token.BITOR,
                     Token.BITXOR, Token.EQ, Token.NE, Token.LT, Token.GT, Token.LE,
                     Token.GE, Token.SHEQ, Token.SHNE, Token.IN, Token.INSTANCEOF};
    Node left = new Node(Token.NUMBER, 5.0);
    Node right = new Node(Token.NUMBER, 3.0);
    for (int op : binOps) {
      Node binNode = new Node(op, left.cloneTree(), right.cloneTree());
      Node expr = new Node(Token.EXPR_RESULT, binNode);
      process(new Node(Token.SCRIPT, expr));
      assertNotNull("Binary operator " + Token.name(op) + " type should not be null", binNode.getJSType());
    }
  }

  // Test for buggy path: TypeCheck handles assignment operators
  @Test
  public void testAssignmentOperators() {
    Node[] assignOps = {Token.ASSIGN, Token.ASSIGN_ADD, Token.ASSIGN_SUB, Token.ASSIGN_MUL,
                        Token.ASSIGN_DIV, Token.ASSIGN_MOD, Token.ASSIGN_SHL, Token.ASSIGN_SHR,
                        Token.ASSIGN_USHR, Token.ASSIGN_BITAND, Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR};
    Node target = new Node(Token.NAME, "x");
    Node source = new Node(Token.NUMBER, 10.0);
    for (int op : assignOps) {
      Node assignNode = new Node(op, target.cloneTree(), source.cloneTree());
      Node expr = new Node(Token.EXPR_RESULT, assignNode);
      process(new Node(Token.SCRIPT, expr));
      assertNotNull("Assignment operator " + Token.name(op) + " type should not be null", assignNode.getJSType());
    }
  }

  // Test for buggy path: TypeCheck handles logical operators
  @Test
  public void testLogicalOperators() {
    Node and = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    Node or = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.TRUE));
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(new Node(Token.EXPR_RESULT, and));
    script.addChildToBack(new Node(Token.EXPR_RESULT, or));
    process(script);
    assertNotNull(and.getJSType());
    assertNotNull(or.getJSType());
  }
}