package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

public class CheckGlobalThisTest {

    private Compiler compiler;
    private CompilerOptions options;
    private List<SourceFile> externs;
    
    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        options.ideMode = true;
        options.checkGlobalThisLevel = CheckLevel.WARNING;
        externs = new ArrayList<>();
    }

    @Test
    public void testFunctionInGlobalScopeNoThis() {
        // Function without 'this' should not produce warning
        String source = "function foo() { var x = 1; }";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testFunctionInGlobalScopeWithThis() {
        // Function in global scope using 'this' should produce warning
        String source = "function foo() { this.bar = 1; }";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
        assertTrue(result.warnings[0].description.contains("dangerous use of 'this'"));
    }

    @Test
    public void testFunctionInGlobalScopeNestedThis() {
        // Nested function with this in global scope should still warn
        String source = "function foo() { function bar() { this.baz = 1; } }";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testMethodInLiteral() {
        // Object literal method should not warn
        String source = "var obj = { method: function() { this.x = 1; } };";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testConstructorFunction() {
        // Regular constructor using 'this' should not warn (by convention)
        String source = "function Foo() { this.x = 1; }";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testPrototypeMethod() {
        // Prototype method using 'this' should not warn
        String source = "function Foo() {}; Foo.prototype.method = function() { this.x = 1; };";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testGoogBindMethod() {
        // goog.bind should suppress warnings
        String source = "goog.bind(function() { this.x = 1; }, this);";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testArrowFunctionGlobalScope() {
        // Arrow functions in global scope using 'this' should warn
        String source = "var f = () => { this.x = 1; };";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testArrowFunctionInMethod() {
        // Arrow function as method should not warn (lexical this)
        String source = "var obj = { method: () => { this.x = 1; } };";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testThisInCatchClause() {
        // Using 'this' in a catch clause should warn (global scope)
        String source = "try { } catch(e) { this.x = 1; }";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testThisInWithStatement() {
        // Using 'this' in with block should warn
        String source = "with(obj) { this.x = 1; }";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testFunctionInIfBlock() {
        // Function defined inside if block in global scope
        String source = "if (true) { function f() { this.x = 1; } }";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testFunctionInsideArrayLiteral() {
        // Function inside array literal, should warn
        String source = "var a = [function() { this.x = 1; }];";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testFunctionAsPropertyOfGlobal() {
        // Function assigned to global property should warn
        String source = "var obj = {}; obj.method = function() { this.x = 1; };";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testThisInTernaryCondition() {
        // Using this inside a ternary condition in global scope
        String source = "var x = (this ? 1 : 0);";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testThisInDefaultParameter() {
        // Default parameter using this in global scope
        String source = "function foo(x = this.bar) {}";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testMultipleNestedFunctions() {
        // Multiple levels of nesting
        String source = "function a() { function b() { function c() { this.x = 1; } } }";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testNoWarningForGlobalThisAssignment() {
        // Simple global this assignment (expected warning)
        String source = "this.foo = 1;";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testFunctionExpressionInGlobalScope() {
        // Named function expression
        String source = "var foo = function bar() { this.x = 1; };";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testGetElemWithThis() {
        // Using this with bracket notation
        String source = "function foo() { this['bar'] = 1; }";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testEmptyProgram() {
        // Empty program should have no warnings
        String source = "";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testThisInGlobalIfCondition() {
        // Using this inside global if condition
        String source = "if (this) { var x = 1; }";
        Result result = compile(source);
        assertEquals(1, result.warnings.length);
    }

    @Test
    public void testClassDefinitionWithThis() {
        // ES6 class method should not warn
        String source = "class Foo { constructor() { this.x = 1; } }";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testStaticMethod() {
        // Static method in class using this should not warn
        String source = "class Foo { static method() { this.x = 1; } }";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testGetterSetter() {
        // Getters/setters should not warn
        String source = "var obj = { get x() { return this._x; }, set x(v) { this._x = v; } };";
        Result result = compile(source);
        assertEquals(0, result.warnings.length);
    }

    // Helper method to compile source and return result
    private Result compile(String source) {
        SourceFile file = SourceFile.fromCode("test.js", source);
        Result result = compiler.compile(externs, file, options);
        return result;
    }
}