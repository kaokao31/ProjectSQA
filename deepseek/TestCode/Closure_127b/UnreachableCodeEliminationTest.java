package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Tests for {@link UnreachableCodeElimination}.
 */
@RunWith(JUnit4.class)
public class UnreachableCodeEliminationTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    // Enable the pass
    options.setUnreachableCodeElimination(CompilerOptions.Reach.NO);
    // Disable other passes to isolate this one
    options.setChecksOnly(false);
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
    options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.OFF);
  }

  private void test(String input, String expected) {
    SourceFile[] inputs = new SourceFile[] { SourceFile.fromCode("test.js", input) };
    Result result = compiler.compile(
        new SourceFile[] { SourceFile.fromCode("externs.js", "") },
        inputs,
        options);
    assertTrue("Compilation failed: " + compiler.getErrors(), result.success);
    String output = compiler.toSource();
    assertEquals(expected, output);
  }

  private void testSame(String input) {
    test(input, input);
  }

  // Basic unreachable after return
  @Test
  public void testUnreachableAfterReturn() {
    test("function f() { return 1; var x = 2; }",
         "function f() { return 1; }");
  }

  // Unreachable after throw
  @Test
  public void testUnreachableAfterThrow() {
    test("function f() { throw new Error(); var x = 2; }",
         "function f() { throw new Error(); }");
  }

  // Unreachable after break in loop
  @Test
  public void testUnreachableAfterBreak() {
    test("function f() { while(true) { break; var x = 2; } }",
         "function f() { while(true) { break; } }");
  }

  // Unreachable after continue in loop
  @Test
  public void testUnreachableAfterContinue() {
    test("function f() { for(var i=0;i<10;i++) { continue; var x = 2; } }",
         "function f() { for(var i=0;i<10;i++) { continue; } }");
  }

  // Conditional return: only one branch returns, other branch should not be removed
  @Test
  public void testConditionalReturnNotRemoved() {
    testSame("function f(a) { if(a) { return 1; } else { return 2; } var x = 3; }");
    // The var x after if-else is unreachable because both branches return
    test("function f(a) { if(a) { return 1; } else { return 2; } var x = 3; }",
         "function f(a) { if(a) { return 1; } else { return 2; } }");
  }

  // Nested if-else with return in inner
  @Test
  public void testNestedIfElse() {
    test("function f(a,b) { if(a) { if(b) { return 1; } else { return 2; } } else { return 3; } var x = 4; }",
         "function f(a,b) { if(a) { if(b) { return 1; } else { return 2; } } else { return 3; } }");
  }

  // Try-catch-finally: return in try, finally still executes, code after try is unreachable
  @Test
  public void testTryCatchFinallyReturn() {
    test("function f() { try { return 1; } catch(e) { return 2; } finally { var x = 3; } var y = 4; }",
         "function f() { try { return 1; } catch(e) { return 2; } finally { var x = 3; } }");
  }

  // Switch with break in each case
  @Test
  public void testSwitchWithBreak() {
    test("function f(a) { switch(a) { case 1: break; var x = 2; case 3: break; } }",
         "function f(a) { switch(a) { case 1: break; case 3: break; } }");
  }

  // Switch with return in case
  @Test
  public void testSwitchWithReturn() {
    test("function f(a) { switch(a) { case 1: return 1; var x = 2; default: return 3; } var y = 4; }",
         "function f(a) { switch(a) { case 1: return 1; default: return 3; } }");
  }

  // Labeled break
  @Test
  public void testLabeledBreak() {
    test("function f() { outer: while(true) { while(true) { break outer; var x = 2; } var y = 3; } var z = 4; }",
         "function f() { outer: while(true) { while(true) { break outer; } } }");
  }

  // Infinite loop with break inside conditional
  @Test
  public void testInfiniteLoopWithBreak() {
    test("function f(a) { while(true) { if(a) { break; } else { return 1; } var x = 2; } var y = 3; }",
         "function f(a) { while(true) { if(a) { break; } else { return 1; } } }");
  }

  // For loop with break
  @Test
  public void testForLoopWithBreak() {
    test("function f() { for(var i=0;i<10;i++) { break; var x = 2; } var y = 3; }",
         "function f() { for(var i=0;i<10;i++) { break; } }");
  }

  // Do-while loop with break
  @Test
  public void testDoWhileWithBreak() {
    test("function f() { do { break; var x = 2; } while(true); var y = 3; }",
         "function f() { do { break; } while(true); }");
  }

  // Empty statement after return
  @Test
  public void testEmptyStatementAfterReturn() {
    test("function f() { return 1; ; }",
         "function f() { return 1; }");
  }

  // Multiple returns in sequence
  @Test
  public void testMultipleReturns() {
    test("function f() { return 1; return 2; return 3; }",
         "function f() { return 1; }");
  }

  // Return inside a block
  @Test
  public void testReturnInsideBlock() {
    test("function f() { { return 1; } var x = 2; }",
         "function f() { { return 1; } }");
  }

  // Nested function: unreachable code inside nested function should be removed
  @Test
  public void testNestedFunction() {
    test("function f() { function g() { return 1; var x = 2; } return 3; }",
         "function f() { function g() { return 1; } return 3; }");
  }

  // Code after throw in try block (catch will handle)
  @Test
  public void testThrowInTry() {
    test("function f() { try { throw new Error(); var x = 2; } catch(e) { return 1; } var y = 3; }",
         "function f() { try { throw new Error(); } catch(e) { return 1; } }");
  }

  // Code after return in catch block
  @Test
  public void testReturnInCatch() {
    test("function f() { try { var x = 1; } catch(e) { return 2; var y = 3; } var z = 4; }",
         "function f() { try { var x = 1; } catch(e) { return 2; } var z = 4; }");
  }

  // Code after return in finally block
  @Test
  public void testReturnInFinally() {
    test("function f() { try { var x = 1; } finally { return 2; var y = 3; } var z = 4; }",
         "function f() { try { var x = 1; } finally { return 2; } }");
  }

  // Conditional with return in one branch only, code after if is reachable
  @Test
  public void testConditionalReturnOneBranch() {
    testSame("function f(a) { if(a) { return 1; } var x = 2; }");
  }

  // Switch with fall-through (no break) - code after case is reachable
  @Test
  public void testSwitchFallThrough() {
    testSame("function f(a) { switch(a) { case 1: var x = 2; case 3: return 3; } }");
  }

  // Code after return in a loop with break inside conditional
  @Test
  public void testLoopWithConditionalBreakAndReturn() {
    test("function f(a) { while(true) { if(a) { break; } else { return 1; } var x = 2; } var y = 3; }",
         "function f(a) { while(true) { if(a) { break; } else { return 1; } } }");
  }

  // Ensure that code after a return in a function that is never called is still removed
  @Test
  public void testUnreachableInUnusedFunction() {
    test("function f() { return 1; var x = 2; } function g() { return 3; }",
         "function f() { return 1; } function g() { return 3; }");
  }

  // Edge: return with no expression
  @Test
  public void testReturnNoExpression() {
    test("function f() { return; var x = 2; }",
         "function f() { return; }");
  }

  // Edge: throw with no argument
  @Test
  public void testThrowNoArgument() {
    test("function f() { throw; var x = 2; }",
         "function f() { throw; }");
  }

  // Edge: break without label in switch
  @Test
  public void testBreakInSwitch() {
    test("function f(a) { switch(a) { case 1: break; var x = 2; } }",
         "function f(a) { switch(a) { case 1: break; } }");
  }

  // Edge: continue in for-in loop
  @Test
  public void testContinueInForIn() {
    test("function f(obj) { for(var k in obj) { continue; var x = 2; } }",
         "function f(obj) { for(var k in obj) { continue; } }");
  }

  // Edge: return in a block inside a block
  @Test
  public void testReturnInNestedBlock() {
    test("function f() { { { return 1; } var x = 2; } var y = 3; }",
         "function f() { { { return 1; } } }");
  }

  // Edge: code after return in a try block that has no catch/finally
  @Test
  public void testReturnInTryNoCatch() {
    test("function f() { try { return 1; var x = 2; } finally { var y = 3; } var z = 4; }",
         "function f() { try { return 1; } finally { var y = 3; } }");
  }

  // Edge: code after return in a catch block with finally
  @Test
  public void testReturnInCatchWithFinally() {
    test("function f() { try { var x = 1; } catch(e) { return 2; var y = 3; } finally { var z = 4; } var w = 5; }",
         "function f() { try { var x = 1; } catch(e) { return 2; } finally { var z = 4; } }");
  }

  // Edge: code after return in a finally block with catch
  @Test
  public void testReturnInFinallyWithCatch() {
    test("function f() { try { var x = 1; } catch(e) { var y = 2; } finally { return 3; var z = 4; } var w = 5; }",
         "function f() { try { var x = 1; } catch(e) { var y = 2; } finally { return 3; } }");
  }

  // Edge: multiple labels
  @Test
  public void testMultipleLabels() {
    test("function f() { a: b: while(true) { break a; var x = 2; } var y = 3; }",
         "function f() { a: b: while(true) { break a; } }");
  }

  // Edge: break to outer label from inner loop
  @Test
  public void testBreakToOuterLabel() {
    test("function f() { outer: for(var i=0;i<10;i++) { inner: while(true) { break outer; var x = 2; } var y = 3; } var z = 4; }",
         "function f() { outer: for(var i=0;i<10;i++) { inner: while(true) { break outer; } } }");
  }

  // Edge: continue with label
  @Test
  public void testContinueWithLabel() {
    test("function f() { outer: for(var i=0;i<10;i++) { inner: while(true) { continue outer; var x = 2; } var y = 3; } var z = 4; }",
         "function f() { outer: for(var i=0;i<10;i++) { inner: while(true) { continue outer; } } }");
  }

  // Edge: code after return in a function expression
  @Test
  public void testFunctionExpression() {
    test("var f = function() { return 1; var x = 2; };",
         "var f = function() { return 1; };");
  }

  // Edge: code after return in a getter/setter? Not applicable in JS.
  // Edge: code after return in a method in a class? Not in ES5.
}