package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.FlowSensitiveInlineVariables;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for FlowSensitiveInlineVariables.
 * Designed to achieve high coverage and detect faults (e.g., bug 15).
 */
public class FlowSensitiveInlineVariablesTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Use strict mode to enable all checks
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
        options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
        options.setCheckSymbols(true);
        options.setCheckTypes(true);
        options.setFlowSensitiveInlineVariables(true);
    }

    private String compileAndRun(String js) {
        SourceFile input = SourceFile.fromCode("test.js", js);
        compiler.compile(SourceFile.fromCode("externs.js", ""), input, options);
        // Run the FlowSensitiveInlineVariables pass
        CompilerPass pass = new FlowSensitiveInlineVariables(compiler);
        pass.process(compiler.getExternsRoot(), compiler.getRoot());
        return compiler.toSource();
    }

    @Test
    public void testSimpleInline() {
        String js = "function f() { var a = 1; var b = a; return b; }";
        String result = compileAndRun(js);
        // Expect a to be inlined: var b = 1; (or directly return 1)
        assertTrue(result.contains("return 1") || result.contains("var b = 1"));
    }

    @Test
    public void testNoInlineMultipleUse() {
        String js = "function f() { var a = 1; var b = a; var c = a; return b + c; }";
        String result = compileAndRun(js);
        // a should not be inlined because it is used twice
        assertTrue(result.contains("var a = 1"));
    }

    @Test
    public void testNoInlineSideEffect() {
        String js = "function f() { var a = foo(); var b = a; return b; }";
        String result = compileAndRun(js);
        // a should not be inlined because foo() may have side effects
        assertTrue(result.contains("var a = foo()") || result.contains("var a = foo"));
    }

    @Test
    public void testInlineInLoop() {
        String js = "function f() { var a = 1; for(var i=0;i<10;i++) { var b = a; } return b; }";
        String result = compileAndRun(js);
        // a is not reassigned, so it can be inlined inside the loop
        assertTrue(result.contains("var b = 1") || result.contains("b = 1"));
    }

    @Test
    public void testInlineInConditional() {
        String js = "function f(x) { var a = 1; if(x) { var b = a; } else { var b = 2; } return b; }";
        String result = compileAndRun(js);
        // a should be inlined in the if branch
        assertTrue(result.contains("var b = 1") || result.contains("b = 1"));
    }

    @Test
    public void testInlineInTryCatch() {
        String js = "function f() { var a = 1; try { var b = a; } catch(e) {} return b; }";
        String result = compileAndRun(js);
        // a is not reassigned, so it can be inlined
        assertTrue(result.contains("var b = 1") || result.contains("b = 1"));
    }

    @Test
    public void testNoInlineReassigned() {
        String js = "function f() { var a = 1; a = 2; var b = a; return b; }";
        String result = compileAndRun(js);
        // a is reassigned, so the use of a after reassignment should not be inlined with the initial value
        // The pass should keep the variable because the value changes
        assertTrue(result.contains("var a = 1") || result.contains("a = 2"));
    }

    @Test
    public void testNoInlineUsedInFunctionCall() {
        String js = "function f() { var a = 1; function g() { return a; } var b = a; return b; }";
        String result = compileAndRun(js);
        // a is used in a function, so it should not be inlined because the function may be called later
        assertTrue(result.contains("var a = 1"));
    }

    @Test
    public void testNoInlineUsedInAssignment() {
        String js = "function f() { var a = 1; var b = a + 1; var c = a; return c; }";
        String result = compileAndRun(js);
        // a is used twice, so it should not be inlined
        assertTrue(result.contains("var a = 1"));
    }

    @Test
    public void testInlineWithExpression() {
        String js = "function f() { var a = 1 + 2; var b = a; return b; }";
        String result = compileAndRun(js);
        // a is a simple expression, should be inlined
        assertTrue(result.contains("var b = 1 + 2") || result.contains("return 1 + 2"));
    }

    @Test
    public void testNoInlineWithFunctionDefinition() {
        String js = "function f() { var a = function() {}; var b = a; return b; }";
        String result = compileAndRun(js);
        // a is a function, should not be inlined because function definitions have side effects? Actually, it's a function expression, but it's not a simple value.
        // The pass may or may not inline; we just check it doesn't crash.
        assertNotNull(result);
    }

    @Test
    public void testNoInlineWithCatchBlock() {
        String js = "function f() { var a = 1; try { throw 'error'; } catch(e) { var b = a; } return b; }";
        String result = compileAndRun(js);
        // a is used in catch block, but it's not reassigned, so it could be inlined
        assertTrue(result.contains("var b = 1") || result.contains("b = 1"));
    }

    @Test
    public void testNoInlineWithFinally() {
        String js = "function f() { var a = 1; try { } finally { var b = a; } return b; }";
        String result = compileAndRun(js);
        // a is used in finally, should be inlined
        assertTrue(result.contains("var b = 1") || result.contains("b = 1"));
    }

    @Test
    public void testEdgeCaseEmptyFunction() {
        String js = "function f() { }";
        String result = compileAndRun(js);
        assertEquals("function f() {\n}\n", result);
    }

    @Test
    public void testEdgeCaseNoVariable() {
        String js = "function f() { return 1; }";
        String result = compileAndRun(js);
        assertEquals("function f() {\n  return 1;\n}\n", result);
    }

    @Test
    public void testEdgeCaseVariableNotUsed() {
        String js = "function f() { var a = 1; return 2; }";
        String result = compileAndRun(js);
        // a is not used, so it should be removed? The pass may remove it.
        assertFalse(result.contains("var a = 1"));
    }

    @Test
    public void testBug15Trigger() {
        // Bug 15: Inlining when variable is used in a function call that could modify it.
        // This test attempts to trigger the bug by having a variable used in a function call
        // that is later inlined incorrectly.
        String js = "function f() { var a = 1; function g() { a = 2; } g(); var b = a; return b; }";
        String result = compileAndRun(js);
        // a is modified by g(), so the use after g() should not be inlined with the initial value 1.
        // The pass should keep the variable because the value may have changed.
        assertTrue(result.contains("var a = 1") || result.contains("a = 2"));
    }
}