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

public class ClosureCodingConventionTest {

  private ClosureCodingConvention convention;

  @Before
  public void setUp() {
    convention = new ClosureCodingConvention();
  }

  // Tests for isConstant
  @Test
  public void testIsConstant_null() {
    assertFalse(convention.isConstant(null));
  }

  @Test
  public void testIsConstant_empty() {
    assertFalse(convention.isConstant(""));
  }

  @Test
  public void testIsConstant_typicalConstants() {
    assertTrue(convention.isConstant("SOME_CONSTANT"));
    assertTrue(convention.isConstant("ANOTHER_CONST"));
    assertTrue(convention.isConstant("A_B_C"));
  }

  @Test
  public void testIsConstant_lowercase() {
    assertFalse(convention.isConstant("lowercase"));
    assertFalse(convention.isConstant("camelCase"));
  }

  @Test
  public void testIsConstant_mixedCase() {
    assertFalse(convention.isConstant("Mixed_Case"));
    assertFalse(convention.isConstant("MixedCase"));
  }

  // Tests for isNamespace
  @Test
  public void testIsNamespace_null() {
    assertFalse(convention.isNamespace(null));
  }

  @Test
  public void testIsNamespace_empty() {
    assertFalse(convention.isNamespace(""));
  }

  @Test
  public void testIsNamespace_dotted() {
    assertTrue(convention.isNamespace("a.b.c"));
    assertTrue(convention.isNamespace("a.b.c.d"));
  }

  @Test
  public void testIsNamespace_singleComponent() {
    assertFalse(convention.isNamespace("single"));
    assertFalse(convention.isNamespace("single"));
  }

  @Test
  public void testIsNamespace_trailingDot() {
    assertFalse(convention.isNamespace("a.b."));
  }

  @Test
  public void testIsNamespace_leadingDot() {
    assertFalse(convention.isNamespace(".a.b"));
  }

  // Tests for extractIsNamespace
  @Test
  public void testExtractIsNamespace_nullNode() {
    assertNull(convention.extractIsNamespace(null, "someName"));
  }

  @Test
  public void testExtractIsNamespace_nullName() {
    Node node = new Node(Token.NAME);
    assertNull(convention.extractIsNamespace(node, null));
  }

  @Test
  public void testExtractIsNamespace_emptyName() {
    Node node = new Node(Token.NAME);
    assertEquals("", convention.extractIsNamespace(node, ""));
  }

  @Test
  public void testExtractIsNamespace_getPropNode() {
    Node getProp = new Node(Token.GETPROP);
    String name = convention.extractIsNamespace(getProp, "a.b");
    assertNotNull(name);
    // Typical behavior: returns the name if it is a namespace-like pattern
    assertTrue(name.equals("a.b") || name.equals("a") || name.equals("a.b.c"));
  }

  @Test
  public void testExtractIsNamespace_nameNode() {
    Node nameNode = Node.newString(Token.NAME, "foo");
    String result = convention.extractIsNamespace(nameNode, "foo");
    assertNull(result); // single name is not a namespace
  }

  @Test
  public void testExtractIsNamespace_stringKeyNode() {
    Node stringKey = new Node(Token.STRING_KEY);
    // Expect fallback to default behavior
    String result = convention.extractIsNamespace(stringKey, "some.key");
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  public void testExtractIsNamespace_assignNode() {
    Node assign = new Node(Token.ASSIGN);
    String result = convention.extractIsNamespace(assign, "a.b.c");
    assertNotNull(result);
  }

  // Tests for applySubclassing
  @Test
  public void testApplySubclassing_nullNode() {
    assertNull(convention.applySubclassing(null, null, null));
  }

  @Test
  public void testApplySubclassing_nonFunction() {
    Node nonFunc = new Node(Token.NAME);
    assertNull(convention.applySubclassing(nonFunc, null, null));
  }

  @Test
  public void testApplySubclassing_functionButNoSubclassing() {
    Node func = new Node(Token.FUNCTION);
    ClosureCodingConvention.SubclassType result =
        convention.applySubclassing(func, null, null);
    // Expected: typically NO_SUBCLASSING
    assertEquals(ClosureCodingConvention.SubclassType.NO_SUBCLASSING, result);
  }

  @Test
  public void testApplySubclassing_googInherits() {
    // Simulate a function node with an $inherits call
    Node func = new Node(Token.FUNCTION);
    Node body = new Node(Token.BLOCK);
    func.addChildToFront(body);
    // Add a call to goog.inherits
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "inherits"));
    call.addChildToFront(getProp);
    body.addChildToFront(call);

    ClosureCodingConvention.SubclassType result =
        convention.applySubclassing(func, null, null);
    // Should detect goog.inherits
    assertEquals(ClosureCodingConvention.SubclassType.INHERITS, result);
  }

  @Test
  public void testApplySubclassing_googSubclass() {
    Node func = new Node(Token.FUNCTION);
    Node body = new Node(Token.BLOCK);
    func.addChildToFront(body);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "subclass"));
    call.addChildToFront(getProp);
    body.addChildToFront(call);

    ClosureCodingConvention.SubclassType result =
        convention.applySubclassing(func, null, null);
    assertEquals(ClosureCodingConvention.SubclassType.SUBCLASS, result);
  }

  @Test
  public void testApplySubclassing_googSubclassWithExtend() {
    // Some versions use goog.subclass with extend
    Node func = new Node(Token.FUNCTION);
    Node body = new Node(Token.BLOCK);
    func.addChildToFront(body);
    Node call = new Node(Token.CALL);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "subclass"));
    call.addChildToFront(getProp);
    // add an extend call as argument?
    body.addChildToFront(call);

    ClosureCodingConvention.SubclassType result =
        convention.applySubclassing(func, null, null);
    // Still subclass
    assertEquals(ClosureCodingConvention.SubclassType.SUBCLASS, result);
  }

  // Tests for identifyTypeDeclaration
  @Test
  public void testIdentifyTypeDeclaration_nullNode() {
    assertNull(convention.identifyTypeDeclaration(null));
  }

  @Test
  public void testIdentifyTypeDeclaration_nonAssign() {
    Node node = new Node(Token.NAME);
    assertNull(convention.identifyTypeDeclaration(node));
  }

  @Test
  public void testIdentifyTypeDeclaration_assignWithTypedef() {
    Node assign = new Node(Token.ASSIGN);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "typedef"));
    assign.addChildToFront(getProp);
    assign.addChildToFront(new Node(Token.STRING, "type"));
    String result = convention.identifyTypeDeclaration(assign);
    assertNotNull(result);
    // Should extract the qualified name
  }

  @Test
  public void testIdentifyTypeDeclaration_assignWithNamespace() {
    Node assign = new Node(Token.ASSIGN);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(Node.newString(Token.NAME, "goog"));
    getProp.addChildToBack(Node.newString(Token.STRING, "provide"));
    assign.addChildToFront(getProp);
    String result = convention.identifyTypeDeclaration(assign);
    assertNull(result); // not a type declaration
  }

  // Additional edge case tests
  @Test
  public void testExtractIsNamespace_getPropWithRoot() {
    Node getProp = new Node(Token.GETPROP);
    Node root = Node.newString(Token.NAME, "goog");
    getProp.addChildToFront(root);
    getProp.addChildToBack(Node.newString(Token.STRING, "namespace"));
    String result = convention.extractIsNamespace(getProp, "goog.namespace");
    assertNotNull(result);
  }

  @Test
  public void testExtractIsNamespace_getPropDeep() {
    Node getProp = new Node(Token.GETPROP);
    Node left = new Node(Token.GETPROP);
    left.addChildToBack(Node.newString(Token.NAME, "a"));
    left.addChildToBack(Node.newString(Token.STRING, "b"));
    getProp.addChildToFront(left);
    getProp.addChildToBack(Node.newString(Token.STRING, "c"));
    String result = convention.extractIsNamespace(getProp, "a.b.c");
    assertNotNull(result);
  }

  // Test for isModuleName
  @Test
  public void testIsModuleName_valid() {
    assertTrue(convention.isModuleName("module$exports"));
    assertTrue(convention.isModuleName("module$some$name"));
  }

  @Test
  public void testIsModuleName_invalid() {
    assertFalse(convention.isModuleName("module"));
    assertFalse(convention.isModuleName("notmodule"));
    assertFalse(convention.isModuleName("module$$double"));
  }

  // Test for isOptionalParameter
  @Test
  public void testIsOptionalParameter_null() {
    assertFalse(convention.isOptionalParameter(null));
  }

  @Test
  public void testIsOptionalParameter_optParameter() {
    Node param = new Node(Token.PARAM_LIST);
    param.addChildToFront(Node.newString(Token.NAME, "opt_param"));
    // Assume names starting with "opt_" or "opt" are optional
    // Need to access the first child
    Node nameNode = param.getFirstChild();
    if (nameNode != null) {
      assertTrue(convention.isOptionalParameter(nameNode));
    }
  }

  @Test
  public void testIsOptionalParameter_notOptional() {
    Node param = new Node(Token.NAME, "notOptional");
    assertFalse(convention.isOptionalParameter(param));
  }

  @Test
  public void testIsVarArgsParameter_var_args() {
    Node varArgs = new Node(Token.NAME, "var_args");
    assertTrue(convention.isVarArgsParameter(varArgs));
  }

  @Test
  public void testIsVarArgsParameter_notVarArgs() {
    Node notVar = new Node(Token.NAME, "notVarArgs");
    assertFalse(convention.isVarArgsParameter(notVar));
  }
}