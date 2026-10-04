package com.google.javascript.jscomp.type;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.TypedScopeCreator;
import com.google.javascript.jscomp.TypeValidator;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for SemanticReverseAbstractInterpreter.
 *
 * The test cases exercise typical uses of the reverse abstract interpreter,
 * including conditions with typeof, instanceof, and possibly problematic
 * patterns that may throw NullPointerException or incorrectly narrow types.
 */
public class SemanticReverseAbstractInterpreterTest {

  private Compiler compiler;
  private TypeValidator validator;
  private SemanticReverseAbstractInterpreter interpreter;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  /**
   * Helper: compiles the given JavaScript code and returns the first IF condition node.
   */
  private Node compileAndGetCondition(String code) {
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = new ArrayList<>();
    List<SourceFile> inputs = new ArrayList<>();
    inputs.add(SourceFile.fromCode("test.js", code));
    compiler.compile(externs, inputs, options);
    if (compiler.getErrors().length > 0) {
      fail("Compiler errors: " + compiler.getErrors()[0]);
    }
    Node root = compiler.getRoot();
    Node ifNode = findFirstNodeOfType(root, Token.IF);
    assertNotNull("Expected an IF node in the input code", ifNode);
    return ifNode.getFirstChild();
  }

  private Node findFirstNodeOfType(Node node, int type) {
    if (node.getType() == type) {
      return node;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFirstNodeOfType(child, type);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private Scope createScope() {
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    return scopeCreator.createScope(compiler.getRoot(), null);
  }

  private void prepareInterpreter() {
    validator = new TypeValidator(compiler);
    interpreter = new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), validator);
  }

  // === Tests for typeof ===

  @Test
  public void testTypeofSimpleVariable() {
    String code = "function f(x) { if (typeof x == 'number') { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull("Result scope should not be null", result);
  }

  @Test
  public void testTypeofComparisonWithNonString() {
    // This can reveal a bug if the interpreter assumes the RHS is always a string literal.
    String code = "function f(x, y) { if (typeof x == y) { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  @Test
  public void testTypeofWithPropertyAccess() {
    // Known buggy pattern: the interpreter may throw on a property access in typeof.
    String code = "function f(x) { if (typeof x.foo == 'number') { return x.foo; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  @Test
  public void testTypeofWithUndefined() {
    String code = "function f(x) { if (typeof x == 'undefined') { return 0; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  // === Tests for instanceof ===

  @Test
  public void testInstanceofSimple() {
    String code = "function f(x, C) { if (x instanceof C) { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  @Test
  public void testInstanceofWithUnknownConstructor() {
    // If y is not known to be a constructor, the interpreter should not crash.
    String code = "function f(x, y) { if (x instanceof y) { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  @Test
  public void testInstanceofWithNull() {
    // Left operand could be a primitive or null.
    String code = "function f(x, y) { if (x == null) { return x; } if (x instanceof y) { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  // === Tests for logical operators ===

  @Test
  public void testLogicalAnd() {
    String code = "function f(x) { if (x != null && typeof x == 'number') { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  @Test
  public void testLogicalOr() {
    String code = "function f(x) { if (x == null || typeof x == 'number') { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  // === Edge cases and null/empty scopes ===

  @Test
  public void testConditionOnThis() {
    String code = "function f() { if (this instanceof Object) { return this; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  @Test
  public void testEmptyFunctionBody() {
    String code = "function f(x) { if (typeof x == 'number') {} }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  @Test
  public void testConditionWithAssignment() {
    String code = "function f(x) { if (x = true) { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }

  @Test
  public void testConditionWithConstants() {
    String code = "function f() { var x; if (typeof x == 'number') { return x; } }";
    Node condition = compileAndGetCondition(code);
    Scope scope = createScope();
    prepareInterpreter();
    Scope result = interpreter.getPreciserScopeKnowingConditionOutcome(condition, scope);
    assertNotNull(result);
  }
}