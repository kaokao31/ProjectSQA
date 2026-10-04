package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test suite for FunctionInjector, targeting edge cases and fault detection.
 */
public class FunctionInjectorTest {

  private Compiler compiler;
  private FunctionInjector injector;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    injector = createInjector();
  }

  private FunctionInjector createInjector() {
    return new FunctionInjector(compiler, new Supplier<String>() {
      @Override
      public String get() {
        return "inject";
      }
    }, true);
  }

  @Test
  public void testDirectInliningOfSimpleFunction() {
    // function f(a) { return a; }  f(1);
    Node functionNode = IR.function(
        IR.name("f"),
        IR.paramList(IR.name("a")),
        IR.block(IR.returnNode(IR.name("a")))
    );
    Node callNode = IR.call(IR.name("f"), IR.number(1));
    FunctionInjector.InliningMode mode = injector.canInlineReferenceToFunction(callNode, functionNode);
    assertNotNull("Direct simple call should be inlinable", mode);
  }

  @Test
  public void testCannotInlineFunctionWithArgumentsReference() {
    // function f() { return arguments[0]; }  f(1);
    Node functionNode = IR.function(
        IR.name("f"),
        IR.paramList(),
        IR.block(IR.returnNode(IR.getProp(IR.name("arguments"), IR.string("0"))))
    );
    Node callNode = IR.call(IR.name("f"), IR.number(1));
    FunctionInjector.InliningMode mode = injector.canInlineReferenceToFunction(callNode, functionNode);
    assertNull("Functions referencing 'arguments' should not be inlined", mode);
  }

  @Test
  public void testCannotInlineFunctionWithInnerFunction() {
    // function f() { function g() {} }  f();
    Node innerFunction = IR.function(
        IR.name("g"),
        IR.paramList(),
        IR.block()
    );
    Node outerFunction = IR.function(
        IR.name("f"),
        IR.paramList(),
        IR.block(innerFunction)
    );
    Node callNode = IR.call(IR.name("f"));
    FunctionInjector.InliningMode mode = injector.canInlineReferenceToFunction(callNode, outerFunction);
    assertNull("Functions containing inner functions should not be inlined", mode);
  }

  @Test
  public void testCannotInlineFunctionUsingEval() {
    // function f() { eval("var x;"); }  f();
    Node evalCall = IR.call(IR.name("eval"), IR.string("var x;"));
    Node functionNode = IR.function(
        IR.name("f"),
        IR.paramList(),
        IR.block(IR.exprResult(evalCall))
    );
    Node callNode = IR.call(IR.name("f"));
    FunctionInjector.InliningMode mode = injector.canInlineReferenceToFunction(callNode, functionNode);
    assertNull("Functions using eval should not be inlined", mode);
  }

  @Test
  public void testCannotInlineFunctionWithThisReference() {
    // function f() { return this; }  f();
    Node functionNode = IR.function(
        IR.name("f"),
        IR.paramList(),
        IR.block(IR.returnNode(IR.thisNode()))
    );
    Node callNode = IR.call(IR.name("f"));
    FunctionInjector.InliningMode mode = injector.canInlineReferenceToFunction(callNode, functionNode);
    assertNull("Functions using 'this' should not be inlined", mode);
  }

  @Test
  public void testCanInlineFunctionWithNoSideEffects() {
    // function f(a) { var x = 1; return a + x; }  f(2);
    Node number1 = IR.number(1);
    Node varX = IR.var(IR.name("x"), number1);
    Node addNode = IR.add(IR.name("a"), IR.name("x"));
    Node functionNode = IR.function(
        IR.name("f"),
        IR.paramList(IR.name("a")),
        IR.block(varX, IR.returnNode(addNode))
    );
    Node callNode = IR.call(IR.name("f"), IR.number(2));
    FunctionInjector.InliningMode mode = injector.canInlineReferenceToFunction(callNode, functionNode);
    assertNotNull("Pure function should be inlinable", mode);
  }

  @Test
  public void testNullFunctionNodeThrowsNullPointer() {
    Node callNode = IR.call(IR.name("f"));
    try {
      injector.canInlineReferenceToFunction(callNode, null);
      fail("Expected NullPointerException when function is null");
    } catch (NullPointerException expected) {
      // expected
    }
  }

  @Test
  public void testNullCallNodeThrowsNullPointer() {
    Node functionNode = IR.function(IR.name("f"), IR.paramList(), IR.block());
    try {
      injector.canInlineReferenceToFunction(null, functionNode);
      fail("Expected NullPointerException when call is null");
    } catch (NullPointerException expected) {
      // expected
    }
  }

  @Test
  public void testSetAssumeStrictThisAndMinimumCapture() {
    // Check that setters work and do not throw.
    injector.setAssumeStrictThis(true);
    injector.setAssumeMinimumCapture(true);
    // Re-create injector with false values and verify behavior.
    injector = new FunctionInjector(compiler, new Supplier<String>() {
      @Override
      public String get() {
        return "inject";
      }
    }, true);
    injector.setAssumeStrictThis(false);
    injector.setAssumeMinimumCapture(false);
  }

  @Test
  public void testInlineSimpleFunction() throws Exception {
    Node functionNode = IR.function(
        IR.name("f"),
        IR.paramList(IR.name("a")),
        IR.block(IR.returnNode(IR.add(IR.name("a"), IR.number(1))))
    );
    Node callNode = IR.call(IR.name("f"), IR.name("a"));
    // Add call as expression statement to a script.
    Node script = IR.script(functionNode, IR.exprResult(callNode));
    // Ensure the call node is a direct child of script.
    Node result = injector.inline(callNode, functionNode, FunctionInjector.InliningMode.DIRECT);
    assertNotNull("Inlining should produce a replacement node", result);
  }

  private void fail(String message) {
    throw new AssertionError(message);
  }
}