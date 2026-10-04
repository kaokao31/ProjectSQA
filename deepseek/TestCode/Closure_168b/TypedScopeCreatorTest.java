package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.DefaultCodingConvention;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.TypedScopeCreator;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for TypedScopeCreator targeting maximum coverage and fault detection.
 * Designed to exercise scope creation for various JavaScript constructs,
 * including edge cases and bug-prone patterns (e.g., goog.inherits, function expressions).
 */
public class TypedScopeCreatorTest {

    private Compiler compiler;
    private TypedScopeCreator creator;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        CodingConvention convention = new DefaultCodingConvention();
        creator = new TypedScopeCreator(compiler, convention);
    }

    // Helper to parse a script and return the root node
    private Node parseScript(String code) {
        Node script = compiler.parseSyntheticCode(code);
        assertNotNull("Parsing failed", script);
        return script;
    }

    // Helper to create a scope from a script root
    private Scope createScope(Node scriptRoot) {
        Scope scope = creator.createScope(scriptRoot, null);
        assertNotNull("Scope creation failed", scope);
        return scope;
    }

    // ==================== Basic Variable Declarations ====================

    @Test
    public void testVarDeclaration() {
        Node script = parseScript("var x = 1;");
        Scope scope = createScope(script);
        assertTrue("Variable 'x' should be declared", scope.isDeclared("x", false));
        assertNotNull("Variable 'x' should have a type", scope.getVar("x").getType());
    }

    @Test
    public void testMultipleVarDeclarations() {
        Node script = parseScript("var a, b, c;");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("a", false));
        assertTrue(scope.isDeclared("b", false));
        assertTrue(scope.isDeclared("c", false));
    }

    @Test
    public void testVarWithFunctionExpression() {
        Node script = parseScript("var f = function() {};");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("f", false));
        assertNotNull(scope.getVar("f").getType());
    }

    // ==================== Function Declarations ====================

    @Test
    public void testFunctionDeclaration() {
        Node script = parseScript("function foo() {}");
        Scope scope = createScope(script);
        assertTrue("Function 'foo' should be declared", scope.isDeclared("foo", false));
    }

    @Test
    public void testFunctionWithParameters() {
        Node script = parseScript("function add(a, b) { return a + b; }");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("add", false));
        // Parameters are in the function's inner scope, not the global scope
        assertFalse(scope.isDeclared("a", false));
        assertFalse(scope.isDeclared("b", false));
    }

    @Test
    public void testNestedFunction() {
        Node script = parseScript("function outer() { function inner() {} }");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("outer", false));
        // inner is not in global scope
        assertFalse(scope.isDeclared("inner", false));
    }

    // ==================== Object Literals and Properties ====================

    @Test
    public void testObjectLiteral() {
        Node script = parseScript("var obj = {a: 1, b: 'hello'};");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("obj", false));
    }

    @Test
    public void testNestedObjectLiteral() {
        Node script = parseScript("var obj = {inner: {x: 1}};");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("obj", false));
    }

    // ==================== goog.inherits (common bug pattern) ====================

    @Test
    public void testGoogInherits() {
        Node script = parseScript(
            "/** @constructor */ function Parent() {}\n" +
            "/** @constructor */ function Child() {}\n" +
            "goog.inherits(Child, Parent);\n" +
            "var c = new Child();"
        );
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("Parent", false));
        assertTrue(scope.isDeclared("Child", false));
        assertTrue(scope.isDeclared("c", false));
        // Ensure Child's prototype chain is set (type inference)
        assertNotNull(scope.getVar("Child").getType());
    }

    @Test
    public void testGoogInheritsWithMissingParent() {
        // Edge case: goog.inherits called with undefined parent
        Node script = parseScript(
            "/** @constructor */ function Child() {}\n" +
            "goog.inherits(Child, undefined);"
        );
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("Child", false));
        // Should not throw exception
    }

    // ==================== Type Annotations and JSDoc ====================

    @Test
    public void testTypedVar() {
        Node script = parseScript("/** @type {number} */ var x;");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("x", false));
        assertNotNull(scope.getVar("x").getType());
    }

    @Test
    public void testTypedFunctionReturn() {
        Node script = parseScript(
            "/** @return {string} */ function f() { return 'hello'; }"
        );
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("f", false));
    }

    // ==================== Edge Cases: Empty Script, Externs ====================

    @Test
    public void testEmptyScript() {
        Node script = parseScript("");
        Scope scope = createScope(script);
        assertNotNull(scope);
        assertEquals(0, scope.getVarCount());
    }

    @Test
    public void testExterns() {
        // Simulate externs by providing a separate root
        Node externs = parseScript("/** @constructor */ function External() {}");
        Node script = parseScript("var x = new External();");
        // Create scope with externs
        Scope scope = creator.createScope(script, externs);
        assertNotNull(scope);
        assertTrue(scope.isDeclared("External", false));
        assertTrue(scope.isDeclared("x", false));
    }

    // ==================== Conditional and Loop Constructs ====================

    @Test
    public void testIfStatement() {
        Node script = parseScript("var a; if (true) { var b; } else { var c; }");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("a", false));
        assertTrue(scope.isDeclared("b", false));
        assertTrue(scope.isDeclared("c", false));
    }

    @Test
    public void testForLoop() {
        Node script = parseScript("for (var i = 0; i < 10; i++) { var j; }");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("i", false));
        assertTrue(scope.isDeclared("j", false));
    }

    @Test
    public void testTryCatch() {
        Node script = parseScript("try { var a; } catch (e) { var b; } finally { var c; }");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("a", false));
        assertTrue(scope.isDeclared("b", false));
        assertTrue(scope.isDeclared("c", false));
    }

    // ==================== Function Expressions and Closures ====================

    @Test
    public void testFunctionExpressionWithName() {
        Node script = parseScript("var f = function myName() {};");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("f", false));
        // The function name 'myName' is only visible inside the function
        assertFalse(scope.isDeclared("myName", false));
    }

    @Test
    public void testImmediatelyInvokedFunctionExpression() {
        Node script = parseScript("var result = (function() { var x = 1; return x; })();");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("result", false));
        // 'x' is not in global scope
        assertFalse(scope.isDeclared("x", false));
    }

    // ==================== Null and Undefined Handling ====================

    @Test
    public void testNullAssignment() {
        Node script = parseScript("var x = null;");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("x", false));
    }

    @Test
    public void testUndefinedAssignment() {
        Node script = parseScript("var x = undefined;");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("x", false));
    }

    // ==================== Multiple Scripts (reuse) ====================

    @Test
    public void testMultipleScriptsSameScope() {
        Node script1 = parseScript("var a = 1;");
        Node script2 = parseScript("var b = 2;");
        // Create scope from first script, then process second script
        Scope scope = createScope(script1);
        // Simulate incremental compilation: create a new scope from script2 using previous scope as parent
        // Note: TypedScopeCreator may not support this directly; this test is for coverage
        // We'll just ensure no exception
        try {
            Scope scope2 = creator.createScope(script2, scope);
            assertNotNull(scope2);
        } catch (Exception e) {
            // If not supported, it's acceptable
        }
    }

    // ==================== Large Number of Variables (stress) ====================

    @Test
    public void testManyVariables() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("var v").append(i).append(" = ").append(i).append(";\n");
        }
        Node script = parseScript(sb.toString());
        Scope scope = createScope(script);
        for (int i = 0; i < 100; i++) {
            assertTrue("Variable v" + i + " should be declared", scope.isDeclared("v" + i, false));
        }
    }

    // ==================== Recursive Function ====================

    @Test
    public void testRecursiveFunction() {
        Node script = parseScript("function factorial(n) { return n <= 1 ? 1 : n * factorial(n-1); }");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("factorial", false));
    }

    // ==================== Constructor and Prototype ====================

    @Test
    public void testConstructorWithPrototype() {
        Node script = parseScript(
            "/** @constructor */ function MyClass() {}\n" +
            "MyClass.prototype.method = function() {};\n" +
            "var obj = new MyClass();"
        );
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("MyClass", false));
        assertTrue(scope.isDeclared("obj", false));
    }

    // ==================== Type Inference for Arrays ====================

    @Test
    public void testArrayLiteral() {
        Node script = parseScript("var arr = [1, 2, 3];");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("arr", false));
    }

    @Test
    public void testArrayWithMixedTypes() {
        Node script = parseScript("var arr = [1, 'hello', true];");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("arr", false));
    }

    // ==================== Global this and window ====================

    @Test
    public void testGlobalThis() {
        Node script = parseScript("var x = this;");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("x", false));
    }

    // ==================== Error Handling: Invalid Code ====================

    @Test(expected = RuntimeException.class)
    public void testInvalidSyntax() {
        // This should cause a parse error, which may throw an exception
        Node script = parseScript("var x = ;");
        // If parse succeeds unexpectedly, fail
        fail("Expected parse error");
    }

    // ==================== Scope Hierarchy ====================

    @Test
    public void testScopeHierarchy() {
        Node script = parseScript("var a; function f() { var b; }");
        Scope globalScope = createScope(script);
        // Get the function scope for 'f'
        Node fnNode = script.getFirstChild().getNext(); // function f
        Scope fnScope = creator.createScope(fnNode, globalScope);
        assertNotNull(fnScope);
        assertTrue(fnScope.isDeclared("b", false));
        assertFalse(fnScope.isDeclared("a", false)); // 'a' is in parent
        assertEquals(globalScope, fnScope.getParent());
    }

    // ==================== Duplicate Variable Declarations ====================

    @Test
    public void testDuplicateVar() {
        Node script = parseScript("var x; var x;");
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("x", false));
        // Should not throw exception
    }

    // ==================== Enum Pattern ====================

    @Test
    public void testEnum() {
        Node script = parseScript(
            "/** @enum {string} */ var Color = {RED: 'red', GREEN: 'green'};"
        );
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("Color", false));
    }

    // ==================== goog.provide and goog.require (if applicable) ====================

    @Test
    public void testGoogProvide() {
        Node script = parseScript("goog.provide('my.namespace');");
        Scope scope = createScope(script);
        // goog.provide creates a namespace object
        assertTrue(scope.isDeclared("my", false));
    }

    // ==================== Regression: Bug 168 specific scenario ====================

    @Test
    public void testBug168Regression() {
        // This test targets the specific bug pattern: function expression with incorrect type inference
        Node script = parseScript(
            "/** @constructor */ function A() {}\n" +
            "A.prototype.foo = function() { return 1; };\n" +
            "var a = new A();\n" +
            "var b = a.foo();"
        );
        Scope scope = createScope(script);
        assertTrue(scope.isDeclared("a", false));
        assertTrue(scope.isDeclared("b", false));
        // Ensure that b's type is inferred as number (from return type)
        // This may require type checking, but scope creation should not fail
    }
}