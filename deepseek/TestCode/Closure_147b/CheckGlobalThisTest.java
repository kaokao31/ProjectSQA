package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerPass;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CheckGlobalThisTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        options.setCodingConvention(new DefaultCodingConvention());
        compiler.initOptions(options);
    }

    private CompilerPass getCheckPass() {
        AbstractCompiler abstractCompiler = compiler;
        return new CheckGlobalThis(abstractCompiler);
    }

    private void testNoWarning(String js) {
        compiler.compile(SourceFile.fromCode("test.js", js),
                         SourceFile.fromCode("externs.js", ""),
                         options);
        getCheckPass().process(compiler.getExternsRoot(), compiler.getJsRoot());
        assertEquals(0, compiler.getWarnings().length);
    }

    private void testWarning(String js, String expectedWarning) {
        compiler.compile(SourceFile.fromCode("test.js", js),
                         SourceFile.fromCode("externs.js", ""),
                         options);
        getCheckPass().process(compiler.getExternsRoot(), compiler.getJsRoot());
        assertEquals(1, compiler.getWarnings().length);
        assertEquals(expectedWarning, compiler.getWarnings()[0].description);
    }

    @Test
    public void testGlobalThisInFunction() {
        testNoWarning("function f() { return this; }");
    }

    @Test
    public void testGlobalThisInGlobalScope() {
        testWarning("this.foo = 1;",
                    "dangerous use of the global `this` object");
    }

    @Test
    public void testGlobalThisInObjectLiteralMethod() {
        testNoWarning("var obj = { method: function() { return this; } };");
    }

    @Test
    public void testGlobalThisInArrowFunction() {
        // Arrow functions bind to outer scope; in global scope, 'this' is global.
        // This should still warn if top-level.
        testWarning("var f = () => this;",
                    "dangerous use of the global `this` object");
    }

    @Test
    public void testGlobalThisInArrowFunctionInsideFunction() {
        testNoWarning("function g() { return () => this; }");
    }

    @Test
    public void testGlobalThisInConstructor() {
        testNoWarning("function Foo() { this.x = 1; }");
    }

    @Test
    public void testGlobalThisInClassMethod() {
        testNoWarning("class A { method() { return this; } }");
    }

    @Test
    public void testGlobalThisInStaticMethod() {
        // Static methods have their own 'this' (class constructor)
        testNoWarning("class A { static method() { return this; } }");
    }

    @Test
    public void testGlobalThisInGetter() {
        testNoWarning("var obj = { get x() { return this; } }");
    }

    @Test
    public void testGlobalThisInSetter() {
        testNoWarning("var obj = { set x(v) { this._x = v; } }");
    }

    @Test
    public void testGlobalThisInNestedFunctionInsideMethod() {
        testWarning("var obj = { method: function() { return function() { return this; }; } };",
                    "dangerous use of the global `this` object");
    }

    @Test
    public void testGlobalThisInWithStatement() {
        // 'with' introduces a dynamic scope; this should warn
        testWarning("with(obj) { this; }",
                    "dangerous use of the global `this` object");
    }

    @Test
    public void testGlobalThisInEval() {
        // Eval context varies; assume global
        testWarning("eval('this.foo')",
                    "dangerous use of the global `this` object");
    }

    @Test
    public void testNoWarningForExterns() {
        // Externs should not trigger warnings
        compiler.compile(SourceFile.fromCode("externs.js", "/** @constructor */ function Foo() {}"),
                         SourceFile.fromCode("test.js", "Foo.prototype.bar = function() { return this; };"),
                         options);
        getCheckPass().process(compiler.getExternsRoot(), compiler.getJsRoot());
        assertEquals(0, compiler.getWarnings().length);
    }

    @Test
    public void testMultipleWarnings() {
        compiler.compile(SourceFile.fromCode("test.js", "this.a; this.b;"),
                         SourceFile.fromCode("externs.js", ""),
                         options);
        getCheckPass().process(compiler.getExternsRoot(), compiler.getJsRoot());
        assertEquals(2, compiler.getWarnings().length);
    }

    @Test
    public void testGlobalThisInCatchBlock() {
        // 'this' inside catch should be treated as global scope
        testWarning("try {} catch(e) { this; }",
                    "dangerous use of the global `this` object");
    }

    @Test
    public void testGlobalThisAssignedToVar() {
        testWarning("var x = this;",
                    "dangerous use of the global `this` object");
    }

    @Test
    public void testGlobalThisInComputedProperty() {
        testNoWarning("var obj = { [this] : 1 }");
    }
}