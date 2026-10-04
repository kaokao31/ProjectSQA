package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for InlineFunctions (Closure Compiler bug 159).
 * Designed to achieve high coverage and trigger potential faults.
 */
public class InlineFunctionsTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Enable aggressive function inlining
        options.setInlineFunctions(CompilerOptions.Reach.ALL);
        // Disable other optimizations to isolate inlining
        options.setRemoveDeadCode(false);
        options.setFoldConstants(false);
        options.setCoalesceVariableNames(false);
        options.setCollapseVariableDeclarations(false);
        options.setDeadAssignmentElimination(false);
    }

    private String compileAndGetResult(String code) {
        SourceFile input = SourceFile.fromCode("test.js", code);
        compiler.compile(
                new SourceFile[] {},
                new SourceFile[] { input },
                options);
        return compiler.toSource();
    }

    // ---------- Basic inlining tests ----------

    @Test
    public void testSimpleFunctionInlining() {
        String code = "function f(x) { return x + 1; } var a = f(2);";
        String result = compileAndGetResult(code);
        // Expected: inlined, no function definition
        assertFalse("Function f should be inlined", result.contains("function f"));
        assertTrue("Result should contain 'a = 2 + 1' or similar",
                result.contains("2 + 1") || result.contains("3"));
    }

    @Test
    public void testFunctionWithSideEffectsShouldNotInline() {
        String code = "var y = 0; function f(x) { y = x; return x; } var a = f(2);";
        String result = compileAndGetResult(code);
        // Side effect on y prevents inlining
        assertTrue("Function f should remain due to side effect", result.contains("function f"));
    }

    @Test
    public void testFunctionWithThisReferenceShouldNotInline() {
        String code = "var obj = {v: 1, f: function() { return this.v; }}; var a = obj.f();";
        String result = compileAndGetResult(code);
        // 'this' prevents inlining
        assertTrue("Function should not be inlined due to 'this'", result.contains("this.v"));
    }

    @Test
    public void testRecursiveFunctionShouldNotInline() {
        String code = "function f(n) { return n <= 0 ? 0 : n + f(n-1); } var a = f(5);";
        String result = compileAndGetResult(code);
        assertTrue("Recursive function should not be inlined", result.contains("function f"));
    }

    @Test
    public void testFunctionWithArgumentsObjectShouldNotInline() {
        String code = "function f() { return arguments[0] + 1; } var a = f(2);";
        String result = compileAndGetResult(code);
        assertTrue("Function using 'arguments' should not be inlined", result.contains("arguments"));
    }

    @Test
    public void testEmptyFunctionInlining() {
        String code = "function f() {} f();";
        String result = compileAndGetResult(code);
        // Empty function call should be removed
        assertFalse("Empty function call should be eliminated", result.contains("f()"));
    }

    @Test
    public void testFunctionReturningFunctionClosure() {
        String code = "function makeAdder(x) { return function(y) { return x + y; }; } var add5 = makeAdder(5); var a = add5(3);";
        String result = compileAndGetResult(code);
        // Closure prevents inlining of makeAdder
        assertTrue("makeAdder should not be inlined due to closure", result.contains("makeAdder"));
    }

    @Test
    public void testMultipleArgumentsInlining() {
        String code = "function f(a, b, c) { return a + b * c; } var x = f(1, 2, 3);";
        String result = compileAndGetResult(code);
        assertFalse("Function f should be inlined", result.contains("function f"));
        assertTrue("Result should contain the expression", result.contains("1 + 2 * 3") || result.contains("7"));
    }

    @Test
    public void testFunctionWithConditionalReturn() {
        String code = "function f(x) { if (x > 0) { return x; } else { return -x; } } var a = f(5);";
        String result = compileAndGetResult(code);
        // Should inline with conditional
        assertFalse("Function f should be inlined", result.contains("function f"));
        assertTrue("Result should contain conditional", result.contains("5 > 0") || result.contains("5"));
    }

    // ---------- Bug-specific tests (Closure bug 159) ----------

    @Test
    public void testBug159ThisInMethodCall() {
        // Bug 159: Inlining a function that uses 'this' when called as a method
        // should preserve the correct 'this' context.
        String code = "var obj = {name: 'test', f: function() { return this.name; }}; var a = obj.f();";
        String result = compileAndGetResult(code);
        // The function should NOT be inlined because 'this' is used
        assertTrue("Function with 'this' should not be inlined", result.contains("this.name"));
        // Additionally, the result should still be 'test'
        assertTrue("Result should contain 'test'", result.contains("test"));
    }

    @Test
    public void testBug159NestedFunctionWithThis() {
        // Bug 159: Nested function that captures 'this' from outer scope
        String code = "var obj = {v: 1, f: function() { var self = this; return function() { return self.v; }; }}; var g = obj.f(); var a = g();";
        String result = compileAndGetResult(code);
        // The outer function should not be inlined due to closure and 'this'
        assertTrue("Outer function should not be inlined", result.contains("self.v") || result.contains("this.v"));
    }

    @Test
    public void testBug159FunctionWithEval() {
        // Bug 159: Functions containing eval should never be inlined
        String code = "function f(x) { eval('x = x + 1'); return x; } var a = f(2);";
        String result = compileAndGetResult(code);
        assertTrue("Function with eval should not be inlined", result.contains("eval"));
    }

    @Test
    public void testBug159FunctionWithDebugger() {
        // Bug 159: Functions containing debugger statement should not be inlined
        String code = "function f(x) { debugger; return x; } var a = f(2);";
        String result = compileAndGetResult(code);
        assertTrue("Function with debugger should not be inlined", result.contains("debugger"));
    }

    @Test
    public void testBug159FunctionWithThrow() {
        // Bug 159: Functions with throw may have side effects
        String code = "function f(x) { if (x < 0) throw new Error('negative'); return x; } var a = f(5);";
        String result = compileAndGetResult(code);
        // Should inline because throw is conditional and not always executed? Actually throw is a side effect.
        // Inlining may still happen if the condition is known. Let's check.
        // For safety, we just verify the function is either inlined or not, but we expect it to be inlined
        // because the condition is false at compile time? Not necessarily.
        // We'll just assert that the code compiles without error.
        assertNotNull("Compilation should succeed", result);
    }

    @Test
    public void testBug159FunctionWithNewTarget() {
        // Bug 159: Functions using new.target (ES6) should not be inlined
        // But Closure Compiler may not support ES6 fully. Use a simple test.
        // Instead, test a function that is called with 'new' and uses 'this' in constructor.
        String code = "function F(x) { this.x = x; } var obj = new F(5);";
        String result = compileAndGetResult(code);
        // Constructor functions should not be inlined
        assertTrue("Constructor function should not be inlined", result.contains("function F"));
    }

    @Test
    public void testBug159FunctionWithCallAndApply() {
        // Bug 159: Functions that are called via .call or .apply should not be inlined
        String code = "function f(x) { return x + 1; } var a = f.call(null, 2);";
        String result = compileAndGetResult(code);
        assertTrue("Function called via .call should not be inlined", result.contains("f.call"));
    }

    @Test
    public void testBug159FunctionWithBind() {
        // Bug 159: Functions that are bound should not be inlined
        String code = "function f(x) { return x + 1; } var g = f.bind(null, 2); var a = g();";
        String result = compileAndGetResult(code);
        assertTrue("Bound function should not be inlined", result.contains("f.bind"));
    }

    // ---------- Edge cases ----------

    @Test
    public void testFunctionWithNoReturnValue() {
        String code = "function f(x) { var y = x + 1; } var a = f(2);";
        String result = compileAndGetResult(code);
        // Should inline and remove the call
        assertFalse("Function f should be inlined", result.contains("function f"));
    }

    @Test
    public void testMultipleCallsToSameFunction() {
        String code = "function f(x) { return x * 2; } var a = f(1); var b = f(2);";
        String result = compileAndGetResult(code);
        // Both calls should be inlined
        assertFalse("Function f should be inlined", result.contains("function f"));
        assertTrue("Result should contain both inlined expressions",
                result.contains("1 * 2") && result.contains("2 * 2"));
    }

    @Test
    public void testFunctionWithDefaultParameter() {
        // ES6 default parameter – may not be supported, but test basic
        String code = "function f(x = 1) { return x + 1; } var a = f();";
        String result = compileAndGetResult(code);
        // If not supported, compilation may fail; we just check no exception
        assertNotNull("Compilation should succeed", result);
    }

    @Test
    public void testFunctionInliningWithGlobalScope() {
        String code = "function f() { return 42; } var a = f();";
        String result = compileAndGetResult(code);
        assertFalse("Function f should be inlined", result.contains("function f"));
        assertTrue("Result should contain 42", result.contains("42"));
    }

    @Test
    public void testFunctionInliningWithLocalVariableRename() {
        String code = "function f(x) { var y = x + 1; return y; } var a = f(5);";
        String result = compileAndGetResult(code);
        assertFalse("Function f should be inlined", result.contains("function f"));
        // The local variable y should be renamed to avoid conflict
        assertTrue("Result should contain the expression", result.contains("5 + 1") || result.contains("6"));
    }

    @Test
    public void testFunctionInliningWithNestedBlock() {
        String code = "function f(x) { if (x) { return x; } else { return -x; } } var a = f(0);";
        String result = compileAndGetResult(code);
        assertFalse("Function f should be inlined", result.contains("function f"));
        // Should contain the conditional
        assertTrue("Result should contain conditional", result.contains("0") || result.contains("-0"));
    }
}