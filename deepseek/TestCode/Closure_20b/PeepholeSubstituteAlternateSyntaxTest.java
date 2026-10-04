package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test suite for PeepholeSubstituteAlternateSyntax, focusing on the bug
 * in Closure-20 where !!x was incorrectly simplified to x for non-boolean types.
 */
public class PeepholeSubstituteAlternateSyntaxTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Enable the peephole optimization
        options.setFoldConstants(true);
        options.setRemoveDeadCode(false);
        options.setCheckTypes(false);
    }

    /**
     * Helper to compile a JavaScript snippet and return the optimized source.
     */
    private String optimize(String js) {
        SourceFile input = SourceFile.fromCode("test.js", js);
        compiler.compile(
                SourceFile.fromCode("externs.js", ""),
                input,
                options);
        return compiler.toSource();
    }

    // ========== Tests for !!x simplification (Closure-20 bug) ==========

    @Test
    public void testDoubleNotBooleanLiteral() {
        // !!true should become true
        assertEquals("true", optimize("!!true"));
        assertEquals("false", optimize("!!false"));
    }

    @Test
    public void testDoubleNotNumberLiteral() {
        // !!1 should NOT become 1 (type change)
        assertEquals("!!1", optimize("!!1"));
        assertEquals("!!0", optimize("!!0"));
        assertEquals("!!NaN", optimize("!!NaN"));
    }

    @Test
    public void testDoubleNotStringLiteral() {
        // !!"a" should NOT become "a"
        assertEquals("!!\"a\"", optimize("!!\"a\""));
        assertEquals("!!\"\"", optimize("!!\"\""));
    }

    @Test
    public void testDoubleNotNullLiteral() {
        // !!null should become false (since null is falsy)
        assertEquals("false", optimize("!!null"));
    }

    @Test
    public void testDoubleNotUndefined() {
        // !!undefined should become false
        assertEquals("false", optimize("!!undefined"));
    }

    @Test
    public void testDoubleNotVariable() {
        // !!x where x is not boolean should NOT be simplified
        assertEquals("!!x", optimize("var x = 1; !!x"));
        assertEquals("!!x", optimize("var x = 'a'; !!x"));
        assertEquals("!!x", optimize("var x; !!x"));
    }

    @Test
    public void testDoubleNotBooleanVariable() {
        // !!x where x is known boolean should become x
        // Note: The compiler may not infer type without type checking.
        // This test assumes type information is available (e.g., from JSDoc).
        // For simplicity, we test with a boolean literal assignment.
        assertEquals("x", optimize("var x = true; !!x"));
        assertEquals("x", optimize("var x = false; !!x"));
    }

    @Test
    public void testDoubleNotWithParentheses() {
        // Parentheses should not affect the optimization
        assertEquals("true", optimize("!!(true)"));
        assertEquals("!!(1)", optimize("!!(1)"));
        assertEquals("!!(x)", optimize("var x = 1; !!(x)"));
    }

    @Test
    public void testDoubleNotInExpression() {
        // !!x used in a larger expression
        assertEquals("true && y", optimize("!!true && y"));
        assertEquals("!!1 && y", optimize("!!1 && y"));
    }

    // ========== Additional tests for other optimizations ==========

    @Test
    public void testNotBooleanLiteral() {
        // !true should become false
        assertEquals("false", optimize("!true"));
        assertEquals("true", optimize("!false"));
    }

    @Test
    public void testNotNumberLiteral() {
        // !1 should become false (since 1 is truthy)
        assertEquals("false", optimize("!1"));
        assertEquals("true", optimize("!0"));
    }

    @Test
    public void testNotStringLiteral() {
        assertEquals("false", optimize("!\"a\""));
        assertEquals("true", optimize("!\"\""));
    }

    @Test
    public void testNotNull() {
        assertEquals("true", optimize("!null"));
    }

    @Test
    public void testNotUndefined() {
        assertEquals("true", optimize("!undefined"));
    }

    @Test
    public void testNotVariable() {
        // !x should not be simplified unless type is known
        assertEquals("!x", optimize("var x = 1; !x"));
    }

    @Test
    public void testDoubleNotWithSideEffects() {
        // !!x where x has side effects should not be removed
        assertEquals("!!a()", optimize("!!a()"));
        assertEquals("!!(x = 1)", optimize("!!(x = 1)"));
    }

    @Test
    public void testConditionalExpression() {
        // Test that other optimizations like ?: are not broken
        assertEquals("true ? 1 : 2", optimize("true ? 1 : 2"));
        assertEquals("false ? 1 : 2", optimize("false ? 1 : 2"));
    }

    @Test
    public void testTypeOfOptimization() {
        // typeof x == 'undefined' -> x == null (if safe)
        // This is another common optimization in PeepholeSubstituteAlternateSyntax
        // but not directly related to the bug. Include for coverage.
        assertEquals("x == null", optimize("typeof x == 'undefined'"));
        assertEquals("x != null", optimize("typeof x != 'undefined'"));
        assertEquals("x == null", optimize("'undefined' == typeof x"));
    }

    @Test
    public void testEmptyInput() {
        assertEquals("", optimize(""));
    }

    @Test
    public void testNoOptimizationNeeded() {
        assertEquals("var x = 1;", optimize("var x = 1;"));
        assertEquals("x + y", optimize("x + y"));
    }
}