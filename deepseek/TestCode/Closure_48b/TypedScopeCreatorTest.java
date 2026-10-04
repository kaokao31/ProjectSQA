package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.testing.Asserts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests for TypedScopeCreator, targeting Defects4J Closure bug 48.
 */
@RunWith(JUnit4.class)
public class TypedScopeCreatorTest {

    private Compiler compiler;
    private TypedScopeCreator creator;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setClosurePass(true);
        compiler.init(Collections.emptyList(), Collections.emptyList(), options);
        creator = new TypedScopeCreator(compiler);
    }

    // Helper to parse source and create a scope.
    private Scope createScope(String source) {
        Node ast = compiler.parseTestCode(source);
        assertNotNull("Parsing failed", ast);
        Node script = ast.getFirstChild();
        assertNotNull("No script node", script);
        Scope globalScope = new Scope(script, Scope.CreationMode.BOTTOM_UP);
        creator.createScope(globalScope, new Node(Token.BLOCK));
        return globalScope;
    }

    // Helper to get a variable and its type.
    private TypedVar getVar(Scope scope, String name) {
        return scope.getVar(name);
    }

    // Basic test: global variable declaration.
    @Test
    public void testGlobalVariable() {
        Scope scope = createScope("var x = 10;");
        TypedVar x = getVar(scope, "x");
        assertNotNull("x should exist", x);
        assertNotNull("x should have type", x.getType());
        assertEquals("number", x.getType().toString());
    }

    // Test function declaration inside global scope.
    @Test
    public void testFunctionDeclaration() {
        Scope scope = createScope("function foo() {}");
        TypedVar foo = getVar(scope, "foo");
        assertNotNull("foo should exist", foo);
        assertEquals("function(undefined): undefined", foo.getType().toString());
    }

    // Test scope for a function (inner scope).
    @Test
    public void testFunctionInnerScope() {
        String source = "function foo() { var y = 1; }";
        Node ast = compiler.parseTestCode(source);
        Node script = ast.getFirstChild();
        Scope globalScope = new Scope(script, Scope.CreationMode.BOTTOM_UP);
        creator.createScope(globalScope, new Node(Token.BLOCK));
        // Get the function node
        Node fnNode = script.getFirstChild();
        assertNotNull(fnNode);
        Scope fnScope = new Scope(fnNode, globalScope);
        creator.createScope(fnScope, fnNode);
        TypedVar y = getVar(fnScope, "y");
        assertNotNull("y should exist in function scope", y);
        assertEquals("number", y.getType().toString());
    }

    // Test "this" type in a constructor.
    @Test
    public void testThisInConstructor() {
        String source = "/** @constructor */ function Foo() { this.prop = 1; }";
        Scope scope = createScope(source);
        TypedVar Foo = getVar(scope, "Foo");
        assertNotNull(Foo);
        // Trigger type resolution of Foo's this type.
        // The bug might be related to this.type not being set properly.
        // We'll access the function's this type via the function scope.
        Node fnNode = compiler.parseTestCode(source).getFirstChild().getFirstChild();
        Scope fnScope = new Scope(fnNode, scope);
        creator.createScope(fnScope, fnNode);
        // Check that this has a type in the function scope.
        TypedVar thisVar = fnScope.getVar("this");
        assertNotNull("this should be in function scope", thisVar);
        assertNotNull("this.type should exist", thisVar.getType());
        // For a constructor, this should be an object type.
        assertTrue("this type should be an object", thisVar.getType().isObjectType());
    }

    // Test goog.provide introduces a property on window.
    @Test
    public void testGoogProvide() {
        String source = "goog.provide('foo.bar.Baz');";
        Scope scope = createScope(source);
        TypedVar foo = getVar(scope, "foo");
        assertNotNull("foo should be provided", foo);
        // foo.bar.Baz should be typed.
        TypedVar bar = getVar(scope, "foo.bar.Baz");
        assertNotNull("foo.bar.Baz should exist", bar);
        assertNotNull("foo.bar.Baz should have type", bar.getType());
    }

    // Test goog.inherits chains.
    @Test
    public void testGoogInherits() {
        String source = "/** @constructor */ function A() {}\n"
                + "/** @constructor */ function B() {}\n"
                + "goog.inherits(B, A);";
        Scope scope = createScope(source);
        TypedVar A = getVar(scope, "A");
        TypedVar B = getVar(scope, "B");
        assertNotNull(A);
        assertNotNull(B);
        // After inheritance, B's prototype chain should include A.
        // This can be verified by checking the type of a B instance.
        // We'll try to access the type of new B.
        // We need to execute the code? No, but the type inference might set it.
        // Simpler: just check that the type of B is a constructor function that inherits A.
        String instanceType = B.getType().toString();
        // The exact string depends, but should contain A.
        assertTrue("B's type should mention A", instanceType.contains("A"));
    }

    // Test that undefined and null are handled.
    @Test
    public void testUndefinedAndNull() {
        Scope scope = createScope("/** @type {undefined} */ var u; /** @type {null} */ var n;");
        TypedVar u = getVar(scope, "u");
        TypedVar n = getVar(scope, "n");
        assertNotNull(u);
        assertNotNull(n);
        assertEquals("undefined", u.getType().toString());
        assertEquals("null", n.getType().toString());
    }

    // Test with empty source.
    @Test
    public void testEmptySource() {
        Scope scope = createScope("");
        assertNotNull(scope);
        // No variables.
        assertNull(getVar(scope, "anything"));
    }

    // Test that an unknown variable reference does not crash.
    @Test
    public void testUnknownVariable() {
        // This should not throw.
        Scope scope = createScope("x;");
        // x is undeclared; but the scope might not contain it.
        assertNull(getVar(scope, "x"));
    }

    // Test that a type annotation with unknown types does not crash.
    @Test
    public void testUnknownTypeAnnotation() {
        Scope scope = createScope("/** @type {UnknownType} */ var x;");
        TypedVar x = getVar(scope, "x");
        assertNotNull(x);
        // The type might be unknown, but should not be null.
        assertNotNull(x.getType());
    }

    // Test a function with @param and @return.
    @Test
    public void testFunctionWithAnnotations() {
        String source = "/** @param {string} s @return {number} */ function f(s) { return 1; }";
        Scope scope = createScope(source);
        TypedVar f = getVar(scope, "f");
        assertNotNull(f);
        String typeStr = f.getType().toString();
        assertTrue("Should be a function with string param and number return",
                typeStr.contains("string") && typeStr.contains("number"));
    }

    // Test that throwing an Error in code does not cause scope creation to fail.
    @Test
    public void testThrowStatement() {
        Scope scope = createScope("function t() { throw new Error(); }");
        TypedVar t = getVar(scope, "t");
        assertNotNull(t);
        // Just verify it doesn't crash.
    }

    // Test enum type.
    @Test
    public void testEnum() {
        String source = "/** @enum {string} */ var Color = {RED: 'r', GREEN: 'g'};";
        Scope scope = createScope(source);
        TypedVar Color = getVar(scope, "Color");
        assertNotNull(Color);
        // Enums become object types with enum elements.
        assertTrue("Color should be an object type", Color.getType().isObjectType());
    }

    // Test interface.
    @Test
    public void testInterface() {
        String source = "/** @interface */ function MyInterface() {}";
        Scope scope = createScope(source);
        TypedVar MyInterface = getVar(scope, "MyInterface");
        assertNotNull(MyInterface);
        // Interfaces are also object types.
        assertTrue(MyInterface.getType().isObjectType());
    }

    // Test that a variable declared after usage in the same scope works (hoisting).
    @Test
    public void testHoisting() {
        Scope scope = createScope("x = 1; var x;");
        TypedVar x = getVar(scope, "x");
        assertNotNull(x);
        // x should have type number.
        assertEquals("number", x.getType().toString());
    }

    // Test that @const annotation works.
    @Test
    public void testConstAnnotation() {
        String source = "/** @const */ var CONST = 5;";
        Scope scope = createScope(source);
        TypedVar c = getVar(scope, "CONST");
        assertNotNull(c);
        // @const may not change the type; just verify it's there.
        assertEquals("number", c.getType().toString());
    }

    // Test nested function scope visibility.
    @Test
    public void testNestedFunctionScope() {
        String source = "function outer() { var x = 1; function inner() { var y = 2; } }";
        Node ast = compiler.parseTestCode(source);
        Node script = ast.getFirstChild();
        Scope globalScope = new Scope(script, Scope.CreationMode.BOTTOM_UP);
        creator.createScope(globalScope, new Node(Token.BLOCK));
        // Outer function scope
        Node outerFn = script.getFirstChild();
        Scope outerScope = new Scope(outerFn, globalScope);
        creator.createScope(outerScope, outerFn);
        TypedVar x = getVar(outerScope, "x");
        assertNotNull("x should be in outer scope", x);
        // Inner function scope
        Node innerFn = outerFn.getLastChild(); // assuming inner is last child
        Scope innerScope = new Scope(innerFn, outerScope);
        creator.createScope(innerScope, innerFn);
        TypedVar y = getVar(innerScope, "y");
        assertNotNull("y should be in inner scope", y);
        // x should not be directly in inner scope? Actually closure scoping includes outer.
        // But var declaration is only in outer scope.
        TypedVar xInInner = getVar(innerScope, "x");
        assertNull("x should not be declared in inner scope (only via closure)", xInInner);
    }

    // Test that a getter/setter property annotation does not crash.
    @Test
    public void testGetterSetter() {
        String source = "/** @type {number} */ var x;\n"
                + "/** @return {number} */ function getX() { return x; }\n"
                + "/** @param {number} n */ function setX(n) { x = n; }";
        Scope scope = createScope(source);
        TypedVar getX = getVar(scope, "getX");
        TypedVar setX = getVar(scope, "setX");
        assertNotNull(getX);
        assertNotNull(setX);
        // Just ensure no NPE.
    }

    // Test that a recursive type does not cause infinite loop.
    @Test
    public void testRecursiveType() {
        String source = "/** @type {?} */ var x; x = {a: x};";
        // This might cause an infinite recursion in type inference. The scope creator should handle it.
        Scope scope = createScope(source);
        // The variable x should have a type.
        TypedVar x = getVar(scope, "x");
        assertNotNull(x);
        // The type might be UNKNOWN, but should not be null.
        assertNotNull(x.getType());
    }

    // Test that declaring a variable with a function expression that references itself.
    @Test
    public void testNamedFunctionExpression() {
        String source = "var f = function foo() {};";
        Scope scope = createScope(source);
        TypedVar f = getVar(scope, "f");
        TypedVar foo = getVar(scope, "foo");
        assertNotNull(f);
        // The name foo should be bound in the function scope, not global.
        assertNull("foo should not be global", foo);
    }

    // Test that a constructor invoked with new is typed correctly.
    @Test
    public void testNewExpression() {
        String source = "/** @constructor */ function A() {}\n var a = new A();";
        Scope scope = createScope(source);
        TypedVar a = getVar(scope, "a");
        assertNotNull(a);
        // The type of a should be A (an object type with name A).
        assertTrue(a.getType().isObjectType());
        String typeStr = a.getType().toString();
        assertTrue("Type should contain 'A'", typeStr.contains("A"));
    }

    // Test that a @return of undefined works.
    @Test
    public void testReturnUndefined() {
        String source = "/** @return {undefined} */ function f() {}";
        Scope scope = createScope(source);
        TypedVar f = getVar(scope, "f");
        assertNotNull(f);
        assertTrue("Should return undefined", f.getType().toString().contains("undefined"));
    }

    // Test that a void function (no return) has undefined type.
    @Test
    public void testVoidFunction() {
        String source = "function f() {}";
        Scope scope = createScope(source);
        TypedVar f = getVar(scope, "f");
        assertNotNull(f);
        // The return type should be undefined.
        String typeStr = f.getType().toString();
        // For a simple function, the type string may be "function(undefined): undefined"
        assertTrue("Return type should be undefined", typeStr.contains("undefined"));
    }

    // Test that a parameterized type (generic) does not crash.
    @Test
    public void testGenericType() {
        String source = "/** @template T\n @param {T} x @return {T} */ function id(x) { return x; }";
        Scope scope = createScope(source);
        TypedVar id = getVar(scope, "id");
        assertNotNull(id);
        // The type may be templatized.
        assertNotNull(id.getType());
    }

    // Additional edge-case: multiple var declarations in one statement.
    @Test
    public void testMultipleVarDeclarations() {
        String source = "var a = 1, b = 2, c = 3;";
        Scope scope = createScope(source);
        assertNotNull(getVar(scope, "a"));
        assertNotNull(getVar(scope, "b"));
        assertNotNull(getVar(scope, "c"));
    }

    // Test that an external shortcut (like window) is handled.
    @Test
    public void testWindowProperty() {
        String source = "window.foo = 1;";
        // This modifies a property on window. TypedScopeCreator does not create scope for window?
        // But it should not crash.
        Scope scope = createScope(source);
        // Just ensure no exception.
        assertNotNull(scope);
    }

    // Test that a try-catch block creates its own scope for the catch variable.
    @Test
    public void testCatchVariableScope() {
        String source = "try { var x; } catch (e) { var y = e; }";
        Scope scope = createScope(source);
        // x and y should be in global scope? Actually catch variables are scoped to the catch block.
        // But in global scope, the var declarations are hoisted? In the scope creation, catch block
        // might create a new scope. Let's test that 'e' is not global.
        TypedVar e = getVar(scope, "e");
        assertNull("e should not be in global scope", e);
        TypedVar x = getVar(scope, "x");
        assertNotNull("x should be hoisted", x);
        TypedVar y = getVar(scope, "y");
        assertNotNull("y should be hoisted", y);
        // Also verify that 'e' exists within the catch scope.
        // We need to create the catch scope separately.
    }
}