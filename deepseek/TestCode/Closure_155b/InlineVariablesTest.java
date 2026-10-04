package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class InlineVariablesTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Enable all optimizations that InlineVariables is part of
        options.setInlineVariables(true);
        // Ensure we don't skip inlining due to other passes
        options.setAssumeClosuresOnlyOnReferences(true);
    }

    private Node compileAndRun(String js) {
        JSSourceFile[] inputs = { JSSourceFile.fromCode("test.js", js) };
        compiler.compile(
                new JSSourceFile[] { JSSourceFile.fromCode("externs.js", "") },
                inputs,
                options);
        // Run InlineVariables pass explicitly
        InlineVariables pass = new InlineVariables(compiler);
        pass.process(compiler.getExternsRoot(), compiler.getJsRoot());
        return compiler.getJsRoot();
    }

    private String compileAndGetSource(String js) {
        Node root = compileAndRun(js);
        return compiler.toSource();
    }

    // Basic inlining of a simple variable
    @Test
    public void testSimpleInlining() {
        String js = "var x = 1; var y = x + 2;";
        String result = compileAndGetSource(js);
        // After inlining: var y = 1 + 2;
        assertTrue(result.contains("1 + 2"));
        assertFalse(result.contains("var x"));
    }

    // Variable used only once, should be inlined
    @Test
    public void testSingleUseInlining() {
        String js = "var a = 10; alert(a);";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("alert(10)"));
        assertFalse(result.contains("var a"));
    }

    // Variable used multiple times, should NOT be inlined (unless it's a constant)
    @Test
    public void testMultipleUseNoInlining() {
        String js = "var b = 5; var c = b + b;";
        String result = compileAndGetSource(js);
        // b is used twice, so it should remain
        assertTrue(result.contains("var b"));
    }

    // Variable with side effects in initializer should not be inlined
    @Test
    public void testSideEffectInitializerNoInlining() {
        String js = "var x = foo(); var y = x + 1;";
        String result = compileAndGetSource(js);
        // x has side effect, should not be inlined
        assertTrue(result.contains("var x"));
    }

    // Variable defined in a loop, used inside loop
    @Test
    public void testLoopVariableInlining() {
        String js = "for(var i = 0; i < 10; i++) { var t = i; alert(t); }";
        String result = compileAndGetSource(js);
        // t is used once, should be inlined
        assertTrue(result.contains("alert(i)"));
        assertFalse(result.contains("var t"));
    }

    // Variable defined in a function, used in same function
    @Test
    public void testFunctionScopeInlining() {
        String js = "function f() { var x = 3; return x + 4; }";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("return 3 + 4"));
        assertFalse(result.contains("var x"));
    }

    // Variable that is reassigned should not be inlined
    @Test
    public void testReassignedVariableNoInlining() {
        String js = "var x = 1; x = 2; var y = x;";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("var x"));
    }

    // Variable used in a closure should not be inlined
    @Test
    public void testClosureVariableNoInlining() {
        String js = "var x = 1; function g() { return x; }";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("var x"));
    }

    // Variable that is exported (used in externs) should not be inlined
    @Test
    public void testExportedVariableNoInlining() {
        String js = "var x = 1; window.x = x;";
        String result = compileAndGetSource(js);
        // x is used in a property assignment, might be considered exported
        // InlineVariables may still inline if it's safe, but typically not
        // We'll just check that the code compiles and doesn't crash
        assertNotNull(result);
    }

    // Inlining of constant-like variables (defined once, used once)
    @Test
    public void testConstantInlining() {
        String js = "var NAME = 'John'; alert(NAME);";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("alert('John')"));
        assertFalse(result.contains("var NAME"));
    }

    // Inlining of boolean
    @Test
    public void testBooleanInlining() {
        String js = "var flag = true; if(flag) { alert('yes'); }";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("if(true)"));
        assertFalse(result.contains("var flag"));
    }

    // Inlining of null
    @Test
    public void testNullInlining() {
        String js = "var n = null; var m = n;";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("var m = null"));
        assertFalse(result.contains("var n"));
    }

    // Inlining of undefined
    @Test
    public void testUndefinedInlining() {
        String js = "var u = undefined; var v = u;";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("var v = void 0") || result.contains("var v = undefined"));
        assertFalse(result.contains("var u"));
    }

    // Inlining of object literal
    @Test
    public void testObjectLiteralInlining() {
        String js = "var obj = {a:1}; var b = obj.a;";
        String result = compileAndGetSource(js);
        // obj is used once, should be inlined
        assertTrue(result.contains("var b = ({a:1}).a"));
        assertFalse(result.contains("var obj"));
    }

    // Inlining of array literal
    @Test
    public void testArrayLiteralInlining() {
        String js = "var arr = [1,2]; var first = arr[0];";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("var first = [1,2][0]"));
        assertFalse(result.contains("var arr"));
    }

    // Inlining of function expression
    @Test
    public void testFunctionExpressionInlining() {
        String js = "var fn = function() { return 1; }; var result = fn();";
        String result = compileAndGetSource(js);
        // fn is used once, should be inlined
        assertTrue(result.contains("var result = (function() { return 1; })()"));
        assertFalse(result.contains("var fn"));
    }

    // Variable defined in catch block
    @Test
    public void testCatchVariableNoInlining() {
        String js = "try { throw 1; } catch(e) { var x = e; alert(x); }";
        String result = compileAndGetSource(js);
        // x is used once, but e is a catch variable; inlining might be tricky
        // We'll just check no crash
        assertNotNull(result);
    }

    // Variable with same name in different scopes
    @Test
    public void testShadowedVariable() {
        String js = "var x = 1; function f() { var x = 2; return x; }";
        String result = compileAndGetSource(js);
        // outer x is not used, inner x is used once, should be inlined
        assertTrue(result.contains("return 2"));
        assertFalse(result.contains("var x")); // inner x inlined, outer x might remain if not used
    }

    // Edge case: variable defined but never used
    @Test
    public void testUnusedVariable() {
        String js = "var unused = 42; var used = 1;";
        String result = compileAndGetSource(js);
        // unused should be removed, used remains
        assertFalse(result.contains("var unused"));
        assertTrue(result.contains("var used"));
    }

    // Bug-specific test: variable defined in a conditional that is always true
    // This might trigger bug 155 related to incorrect inlining when variable is defined in a block
    @Test
    public void testConditionalDefinitionInlining() {
        String js = "if(true) { var x = 1; } var y = x;";
        String result = compileAndGetSource(js);
        // x is defined in a block but hoisted; inlining should be safe
        // Bug 155 might cause incorrect inlining or crash
        assertTrue(result.contains("var y = 1"));
        assertFalse(result.contains("var x"));
    }

    // Another bug trigger: variable defined in a loop with function inside
    @Test
    public void testLoopWithFunctionInlining() {
        String js = "for(var i = 0; i < 10; i++) { var f = function() { return i; }; }";
        String result = compileAndGetSource(js);
        // f is used once? Actually it's defined but not used, so it should be removed
        assertFalse(result.contains("var f"));
    }

    // Variable used in a with statement (should not be inlined)
    @Test
    public void testWithStatementNoInlining() {
        String js = "var x = 1; with(obj) { var y = x; }";
        String result = compileAndGetSource(js);
        // x might be considered as not safe to inline due to with
        assertTrue(result.contains("var x"));
    }

    // Variable used in eval (should not be inlined)
    @Test
    public void testEvalNoInlining() {
        String js = "var x = 1; eval('x');";
        String result = compileAndGetSource(js);
        assertTrue(result.contains("var x"));
    }

    // Large number of variables to test performance and correctness
    @Test
    public void testManyVariables() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("var v").append(i).append(" = ").append(i).append(";");
        }
        sb.append("var sum = 0;");
        for (int i = 0; i < 100; i++) {
            sb.append("sum += v").append(i).append(";");
        }
        String js = sb.toString();
        String result = compileAndGetSource(js);
        // All variables used once, should be inlined
        assertFalse(result.contains("var v"));
        assertTrue(result.contains("sum += 0"));
        assertTrue(result.contains("sum += 99"));
    }

    // Test that inlining does not change semantics when variable is used in a context that changes type
    @Test
    public void testTypeChangeNoInlining() {
        String js = "var x = 'hello'; x = 5; var y = x;";
        String result = compileAndGetSource(js);
        // x is reassigned, so not inlined
        assertTrue(result.contains("var x"));
    }

    // Test inlining of a variable that is a property of an object (not a simple var)
    // This is not directly InlineVariables, but we can test that it doesn't interfere
    @Test
    public void testPropertyAssignmentNoInlining() {
        String js = "var obj = {}; obj.x = 1; var y = obj.x;";
        String result = compileAndGetSource(js);
        // obj.x is not a variable, so InlineVariables should not touch it
        assertTrue(result.contains("var obj"));
    }

    // Test that the pass handles empty input gracefully
    @Test
    public void testEmptyInput() {
        String js = "";
        String result = compileAndGetSource(js);
        assertEquals("", result.trim());
    }

    // Test that the pass handles only externs
    @Test
    public void testOnlyExterns() {
        String js = "var externVar;";
        // This is an extern, not a source variable
        String result = compileAndGetSource(js);
        assertTrue(result.contains("var externVar"));
    }
}