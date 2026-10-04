package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/**
 * JUnit 4 test suite for FunctionInjector designed to achieve high coverage
 * and reveal potential faults (e.g., Closure bug #175).
 */
public class FunctionInjectorTest {

  private Compiler compiler;
  private FunctionInjector injector;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    // Typical constructor: allowDecomposing=true, allowInlineFunctions=true
    injector = new FunctionInjector(compiler, true, true);
  }

  // ==============================
  // canInline tests
  // ==============================

  @Test
  public void testCanInlineSimpleCall() {
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node call = IR.call(IR.name("f"));
    assertTrue("Simple call should be inlinable", injector.canInline(call, fn));
  }

  @Test
  public void testCanInlineWithThisKeyword() {
    // Function that uses 'this' – typically not inlinable in a non-method call.
    Node body = IR.block(IR.exprResult(IR.thisNode()));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    Node call = IR.call(IR.name("f"));
    // Bug #175 may cause true instead of false – we assert false to reveal it.
    assertFalse("Function with 'this' should NOT be inlined into a simple call",
                injector.canInline(call, fn));
  }

  @Test
  public void testCanInlineWithArguments() {
    // Function that uses 'arguments' object – should prevent inlining.
    Node body = IR.block(IR.exprResult(IR.name("arguments")));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    Node call = IR.call(IR.name("f"));
    assertFalse("Function using 'arguments' should not be inlined",
                injector.canInline(call, fn));
  }

  @Test
  public void testCanInlineWithNewCall() {
    Node fn = IR.function(IR.name("F"), IR.paramList(), IR.block());
    Node newCall = IR.newNode(IR.name("F"));
    // Constructor calls are typically not inlined.
    assertFalse("Constructor calls should not be inlined",
                injector.canInline(newCall, fn));
  }

  @Test
  public void testCanInlineWithCallMethod() {
    // Call via .call() – should prevent inlining.
    Node call = IR.call(IR.getprop(IR.name("f"), IR.string("call")), IR.name("obj"));
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    assertFalse("Calls via .call() should not be inlined",
                injector.canInline(call, fn));
  }

  @Test
  public void testCanInlineWithApplyMethod() {
    Node call = IR.call(IR.getprop(IR.name("f"), IR.string("apply")), IR.name("this"));
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    assertFalse("Calls via .apply() should not be inlined",
                injector.canInline(call, fn));
  }

  @Test
  public void testCanInlineWithInnerFunction() {
    // Function containing an inner function – may be inlinable or not.
    Node inner = IR.function(IR.name("g"), IR.paramList(), IR.block());
    Node body = IR.block(IR.exprResult(IR.call(IR.name("g"))));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    Node call = IR.call(IR.name("f"));
    // Expect true as inner functions can be lifted during inlining.
    assertTrue("Function with inner function can be inlined",
               injector.canInline(call, fn));
  }

  @Test
  public void testCanInlineRecursiveFunction() {
    // Recursive function call – should not be inlined.
    Node body = IR.block(IR.exprResult(IR.call(IR.name("f"))));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    Node call = IR.call(IR.name("f"));
    assertFalse("Recursive function should not be inlined",
                injector.canInline(call, fn));
  }

  @Test
  public void testCanInlineWithEval() {
    // Function using eval() – should not be inlined.
    Node body = IR.block(IR.exprResult(IR.call(IR.name("eval"), IR.string(""))));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    Node call = IR.call(IR.name("f"));
    assertFalse("Function with eval should not be inlined",
                injector.canInline(call, fn));
  }

  // ==============================
  // canInlineReturnValue tests
  // ==============================

  @Test
  public void testCanInlineReturnValueSimple() {
    Node fn = IR.function(IR.name("f"), IR.paramList(),
                          IR.block(IR.returnNode(IR.number(42))));
    assertTrue("Simple return value should be inlinable",
               injector.canInlineReturnValue(fn));
  }

  @Test
  public void testCanInlineReturnValueWithSideEffects() {
    // Function has side effects before return – should not be inlinable.
    Node body = IR.block(
        IR.exprResult(IR.call(IR.name("sideEffect"))),
        IR.returnNode(IR.number(1)));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    assertFalse("Return value with side effects should not be inlined",
                injector.canInlineReturnValue(fn));
  }

  @Test
  public void testCanInlineReturnValueNoReturn() {
    // Function without return statement – cannot inline return value.
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    assertFalse("Function without return cannot be inlined for return value",
                injector.canInlineReturnValue(fn));
  }

  @Test
  public void testCanInlineReturnValueMultipleReturns() {
    Node body = IR.block(
        IR.ifNode(IR.trueNode(), IR.block(IR.returnNode(IR.number(1)))),
        IR.returnNode(IR.number(2)));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    assertTrue("Multiple returns can still be inlined (no side effects)",
               injector.canInlineReturnValue(fn));
  }

  // ==============================
  // maybePrepareCall tests
  // ==============================

  @Test
  public void testMaybePrepareCallNormal() {
    Node call = IR.call(IR.name("f"));
    Node prepared = injector.maybePrepareCall(call);
    assertNotNull("Prepared call should not be null", prepared);
    // Usually the same node or a wrapped one.
    assertSame("Prepared call should be the same node", call, prepared);
  }

  @Test
  public void testMaybePrepareCallWithCallMethod() {
    Node call = IR.call(IR.getprop(IR.name("f"), IR.string("call")), IR.name("obj"));
    Node prepared = injector.maybePrepareCall(call);
    assertNotNull("Prepared call should not be null", prepared);
    // For .call() it might transform into a direct call.
    assertNotSame("Prepared call may be transformed", call, prepared);
  }

  @Test
  public void testMaybePrepareCallWithApplyMethod() {
    Node call = IR.call(IR.getprop(IR.name("f"), IR.string("apply")), IR.thisNode());
    Node prepared = injector.maybePrepareCall(call);
    assertNotNull("Prepared call should not be null", prepared);
  }

  // ==============================
  // Inline operations (inlineCall, inlineReturnValue)
  // ==============================

  @Test
  public void testInlineCallBasic() {
    // Inline a function that returns a constant.
    Node fn = IR.function(IR.name("f"), IR.paramList(),
                          IR.block(IR.returnNode(IR.number(7))));
    Node call = IR.call(IR.name("f"));
    Node result = injector.inline(call, fn);
    assertNotNull("Inline result should not be null", result);
    // Result should be a number node with value 7.
    assertTrue("Result should be a number", result.isNumber());
    assertEquals("Inlined value should be 7", 7.0, result.getDouble(), 0.0);
  }

  @Test
  public void testInlineCallWithSideEffects() {
    // Function that has a side effect and returns a value.
    Node body = IR.block(
        IR.exprResult(IR.call(IR.name("sideEffect"))),
        IR.returnNode(IR.number(3)));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    Node call = IR.call(IR.name("f"));
    // Inlining with side effects should handle the expressions.
    Node result = injector.inline(call, fn);
    assertNotNull("Inlined result should not be null", result);
  }

  @Test
  public void testInlineReturnValueBasic() {
    Node fn = IR.function(IR.name("f"), IR.paramList(),
                          IR.block(IR.returnNode(IR.number(10))));
    Node call = IR.call(IR.name("f"));
    Node result = injector.inlineReturnValue(call, fn);
    assertNotNull("Inline return value result should not be null", result);
    assertTrue("Result should be a number", result.isNumber());
    assertEquals("Inlined value should be 10", 10.0, result.getDouble(), 0.0);
  }

  @Test
  public void testInlineReturnValueNoReturn() {
    // Function without return – inlineReturnValue should return null or throw?
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node call = IR.call(IR.name("f"));
    try {
      Node result = injector.inlineReturnValue(call, fn);
      assertNull("No return should result in null", result);
    } catch (Exception e) {
      // Exception is also acceptable if contract requires it.
      // We just want to exercise the path.
    }
  }

  // ==============================
  // Edge cases and null handling
  // ==============================

  @Test(expected = NullPointerException.class)
  public void testCanInlineNullCall() {
    injector.canInline(null, IR.function(IR.name("f"), IR.paramList(), IR.block()));
  }

  @Test(expected = NullPointerException.class)
  public void testCanInlineNullFunction() {
    injector.canInline(IR.call(IR.name("f")), null);
  }

  @Test(expected = NullPointerException.class)
  public void testMaybePrepareCallNull() {
    injector.maybePrepareCall(null);
  }

  @Test
  public void testCanInlineFunctionWithEmptyBody() {
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node call = IR.call(IR.name("f"));
    assertTrue("Empty function should be inlinable", injector.canInline(call, fn));
  }

  @Test
  public void testConstructorWithAllowInlineFunctionsFalse() {
    // Create injector that disallows function inlining
    FunctionInjector restrictedInjector = new FunctionInjector(compiler, true, false);
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node call = IR.call(IR.name("f"));
    assertFalse("Should not inline when allowInlineFunctions=false",
                restrictedInjector.canInline(call, fn));
  }

  @Test
  public void testCanInlineWithThisInNestedFunction() {
    // Function that contains an inner function that uses 'this'.
    Node inner = IR.function(IR.name("g"), IR.paramList(),
                             IR.block(IR.exprResult(IR.thisNode())));
    Node body = IR.block(IR.exprResult(IR.call(IR.name("g"))));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    Node call = IR.call(IR.name("f"));
    // 'this' inside inner function should also prevent inlining.
    assertFalse("Function with nested 'this' usage should not be inlined",
                injector.canInline(call, fn));
  }

  @Test
  public void testCanInlineWithGoogBind() {
    // Simulate a goog.bind call – should not be inlined.
    Node bindCall = IR.call(IR.getprop(IR.name("goog"), IR.string("bind")),
                            IR.name("f"), IR.thisNode());
    // Function node is not used directly; canInline should return false.
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    assertFalse("goog.bind should not be inlined",
                injector.canInline(bindCall, fn));
  }

  @Test
  public void testCanInlineReturnValueWithThis() {
    // Function that contains 'this' but still returns a value.
    Node body = IR.block(IR.exprResult(IR.thisNode()), IR.returnNode(IR.number(1)));
    Node fn = IR.function(IR.name("f"), IR.paramList(), body);
    assertFalse("Return value inlining should be blocked by 'this'",
                injector.canInlineReturnValue(fn));
  }
}