package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for MethodCompilerPass designed to achieve high coverage
 * and reveal potential faults (e.g., Defects4J Closure bug #136).
 */
public class MethodCompilerPassTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        compiler.initOptions(options);
    }

    private Node parse(String js) {
        return compiler.parseSyntheticCode("test", js);
    }

    private MethodCompilerPass createPass() {
        return new MethodCompilerPass(compiler);
    }

    // ----- Basic method definitions -----

    @Test
    public void testNoMethods() {
        Node root = parse("var x = 1;");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue("Expected no errors", compiler.getErrors().isEmpty());
        assertTrue("Expected no warnings", compiler.getWarnings().isEmpty());
    }

    @Test
    public void testObjectLiteralMethod() {
        Node root = parse("var obj = { foo: function() {} };");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testClassMethod() {
        Node root = parse("class A { bar() {} }");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testMultipleObjectMethods() {
        Node root = parse("var obj = { a: function() {}, b: function() {} };");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // ----- Functions inside blocks (known bug trigger) -----

    @Test
    public void testNamedFunctionInBlock() {
        Node root = parse("if (true) { function f() {} }");
        createPass().process(compiler.getExternsRoot(), root);
        // In buggy version, this would produce a false warning for method 'f'
        assertTrue("Function in block should not cause warning", compiler.getWarnings().isEmpty());
    }

    @Test
    public void testNamedFunctionInIfBlock() {
        Node root = parse("if (x) { function g() {} } else { function h() {} }");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testNamedFunctionInTryBlock() {
        Node root = parse("try { function i() {} } catch(e) {}");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // ----- Edge cases: empty scripts, global functions -----

    @Test
    public void testEmptyScript() {
        Node root = parse("");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testGlobalFunctionDeclaration() {
        Node root = parse("function globalFunc() {}");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        // Possibly a warning if global function considered a method? In standard Closure, no.
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testAnonymousFunctionExpression() {
        Node root = parse("var f = function() {};");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // ----- Nested functions / closures -----

    @Test
    public void testNestedFunctionDefinedInScope() {
        Node root = parse("(function() { function inner() {} })();");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testMethodInIIFE() {
        Node root = parse("var obj = (function() { return { method: function() {} }; })();");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // ----- Exports (common in Closure library) -----

    @Test
    public void testExportedMethod() {
        Node root = parse("goog.exportSymbol('myMethod', function() {});");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testPrototypeMethod() {
        Node root = parse("A.prototype.foo = function() {};");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testMultiplePrototypeMethods() {
        Node root = parse("A.prototype.foo = function() {}; A.prototype.bar = function() {};");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // ----- Conditional / computed property names -----

    @Test
    public void testComputedPropertyMethod() {
        Node root = parse("var obj = { [Symbol.iterator]: function() {} };");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testConditionalFunctionAssignment() {
        Node root = parse("if (x) { obj.method = function() {} } else { obj.method = function() {} }");
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // ----- Edge cases with null nodes (defensive) -----

    @Test(expected = NullPointerException.class)
    public void testNullExternsRoot() {
        createPass().process(null, parse("var a = 1;"));
    }

    @Test(expected = NullPointerException.class)
    public void testNullRoot() {
        createPass().process(compiler.getExternsRoot(), null);
    }

    // ----- Synthetic test to trigger Defects4J bug #136 -----
    // The bug is related to functions defined inside blocks and their
    // detection as methods. This test checks that no false warnings appear.
    @Test
    public void testFunctionInBlockShouldNotBeReportedAsMethod() {
        Node root = parse("while (true) { function w() {} }");
        createPass().process(compiler.getExternsRoot(), root);
        // If bug exists, a warning "method w defined but never used" or similar may appear.
        // Fixed version does not warn.
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue("Function in block should not trigger warning", compiler.getWarnings().isEmpty());
    }

    // ----- Branch coverage: loops, multiple passes -----

    @Test
    public void testMultipleCallsToProcess() {
        Node root1 = parse("var o = { m1: function() {} };");
        Node root2 = parse("var p = { m2: function() {} };");
        MethodCompilerPass pass = createPass();
        pass.process(compiler.getExternsRoot(), root1);
        pass.process(compiler.getExternsRoot(), root2);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testMixOfMethodPatterns() {
        Node root = parse(
            "function a() {}" +
            "var b = function() {};" +
            "var c = { d: function() {} };" +
            "if (true) { function e() {} }" +
            "f.prototype.g = function() {};"
        );
        createPass().process(compiler.getExternsRoot(), root);
        assertTrue(compiler.getErrors().isEmpty());
        assertTrue(compiler.getWarnings().isEmpty());
    }
}