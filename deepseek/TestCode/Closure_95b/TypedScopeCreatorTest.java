package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.ScopeCreator;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.UnknownType;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for TypedScopeCreator targeting high coverage and fault detection.
 */
public class TypedScopeCreatorTest {

    private Compiler compiler;
    private TypedScopeCreator creator;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setCodingConvention(new GoogleCodingConvention());
        compiler.initOptions(options);
        creator = new TypedScopeCreator(compiler);
    }

    @Test
    public void testNullRootNode() {
        assertNull(creator.createScope(null, null));
    }

    @Test
    public void testEmptyScript() {
        String source = "";
        Node script = compiler.parseTestCode(source);
        assertNotNull(script);
        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        assertTrue(scope.getAllSymbols().isEmpty());
    }

    @Test
    public void testVariableDeclaration() {
        String source = "var x = 10;";
        Node script = compiler.parseTestCode(source);
        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        assertTrue(scope.isDeclared("x", true));
        Var var = scope.getVar("x");
        assertNotNull(var);
        assertEquals("x", var.getName());
    }

    @Test
    public void testFunctionDeclaration() {
        String source = "function foo() {}";
        Node script = compiler.parseTestCode(source);
        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        assertTrue(scope.isDeclared("foo", true));
        Var var = scope.getVar("foo");
        assertNotNull(var);
        assertTrue(var.getType() instanceof ObjectType);
    }

    @Test
    public void testNestedFunctionScope() {
        String source = "function outer() { var inner = 5; }";
        Node script = compiler.parseTestCode(source);
        Scope globalScope = creator.createScope(script, null);
        assertNotNull(globalScope);
        // Find the outer function scope
        for (Scope child : globalScope.getChildren()) {
            if (child.getRootNode().isFunction()) {
                assertTrue(child.isDeclared("inner", true));
                assertTrue(child.isVarMode());
            }
        }
    }

    @Test
    public void testFunctionExpressionScope() {
        String source = "var f = function() { var a; };";
        Node script = compiler.parseTestCode(source);
        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        // f should be in global scope
        assertTrue(scope.isDeclared("f", true));
        // The function expression creates a child scope for 'a'
        boolean foundChild = false;
        for (Scope child : scope.getChildren()) {
            if (child.getRootNode().isFunction()) {
                assertTrue(child.isDeclared("a", true));
                foundChild = true;
            }
        }
        assertTrue("Should have a function child scope", foundChild);
    }

    @Test
    public void testInheritsTypeNotUnknown() {
        // This test targets the bug: goog.inherits should produce a resolved type
        String source = ""
            + "/** @constructor */\n"
            + "function Base() {}\n"
            + "/** @constructor @extends {Base} */\n"
            + "function Sub() {}\n"
            + "goog.inherits(Sub, Base);\n"
            + "new Sub();";
        Node script = compiler.parseTestCode(source);
        compiler.parse(SourceFile.fromCode("test", source));
        Node root = compiler.getRoot();
        Scope globalScope = creator.createScope(root, null);
        assertNotNull(globalScope);
        Var subVar = globalScope.getVar("Sub");
        assertNotNull(subVar);
        JSType subType = subVar.getType();
        assertNotNull("Sub type should not be null", subType);
        assertFalse("Sub type should not be unknown", subType.isUnknownType());
        // Verify it is a subtype of Base
        Var baseVar = globalScope.getVar("Base");
        assertNotNull(baseVar);
        JSType baseType = baseVar.getType();
        assertTrue("Sub should be a subtype of Base", subType.isSubtype(baseType));
    }

    @Test
    public void testGenericClassInheritance() {
        // Bug scenario: inheritance from generic class using @template
        String source = ""
            + "/** @constructor @template T */\n"
            + "function Container() {}\n"
            + "/** @constructor @extends {Container<string>} */\n"
            + "function StringContainer() {}\n"
            + "goog.inherits(StringContainer, Container);";
        compiler.compile(
            CompilerOptions.getDefaultOptions(),
            SourceFile.fromCode("test", source));
        Node root = compiler.getRoot();
        Scope globalScope = creator.createScope(root, null);
        Var var = globalScope.getVar("StringContainer");
        assertNotNull(var);
        JSType type = var.getType();
        assertNotNull("Type should not be null", type);
        assertFalse("Type should not be unknown", type.isUnknownType());
    }

    @Test
    public void testGoogProvideAndRequire() {
        String source = ""
            + "goog.provide('my.namespace.Class');\n"
            + "goog.require('other.Class');\n"
            + "/** @constructor */ my.namespace.Class = function() {};";
        compiler.compile(
            CompilerOptions.getDefaultOptions(),
            SourceFile.fromCode("test", source));
        Node root = compiler.getRoot();
        Scope globalScope = creator.createScope(root, null);
        // my.namespace.Class should be defined
        assertTrue(globalScope.isDeclared("my.namespace.Class", true));
    }

    @Test
    public void testMultipleVariableDeclarations() {
        String source = "var a, b = 1, c = 'hi';";
        Node script = compiler.parseTestCode(source);
        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        assertTrue(scope.isDeclared("a", true));
        assertTrue(scope.isDeclared("b", true));
        assertTrue(scope.isDeclared("c", true));
        Var varB = scope.getVar("b");
        assertNotNull(varB.getType());
        assertTrue(varB.getType().isNumber());
    }

    @Test
    public void testForLoopVariable() {
        String source = "for (var i = 0; i < 10; i++) { var j = i; }";
        Node script = compiler.parseTestCode(source);
        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        assertTrue(scope.isDeclared("i", true));
        assertTrue(scope.isDeclared("j", true));
    }

    @Test
    public void testInnerFunctionClosure() {
        String source = "function outer() { function inner() { var x; } return inner; }";
        Node script = compiler.parseTestCode(source);
        Scope outerScope = creator.createScope(script, null);
        assertNotNull(outerScope);
        // Find outer and inner scopes
        for (Scope child : outerScope.getChildren()) {
            if (child.getRootNode().isFunction() && child.getRootNode().getFirstChild().getString().equals("outer")) {
                for (Scope inner : child.getChildren()) {
                    if (inner.getRootNode().isFunction() && inner.getRootNode().getFirstChild().getString().equals("inner")) {
                        assertTrue(inner.isDeclared("x", true));
                    }
                }
            }
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullCompilerInConstructor() {
        new TypedScopeCreator(null);
    }

    @Test
    public void testEvalInScope() {
        String source = "eval('var y = 3;');";
        Node script = compiler.parseTestCode(source);
        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        // eval may not introduce variable in outer scope (depends on coding convention)
        // but at least no exception
    }

    @Test
    public void testCatchBlock() {
        String source = "try { throw 1; } catch (e) { var handled = true; }";
        Node script = compiler.parseTestCode(source);
        Scope scope = creator.createScope(script, null);
        assertNotNull(scope);
        assertTrue(scope.isDeclared("handled", true));
        // 'e' is only in catch block scope, but due to hoisting it may appear in global? In JS it's block scoped.
        // Scope creation might create a child scope for the catch block.
    }

    @Test
    public void testTypedef() {
        String source = "/** @typedef {string|number} */ var MyType;";
        compiler.compile(
            CompilerOptions.getDefaultOptions(),
            SourceFile.fromCode("test", source));
        Node root = compiler.getRoot();
        Scope globalScope = creator.createScope(root, null);
        Var myType = globalScope.getVar("MyType");
        assertNotNull(myType);
        assertNotNull(myType.getType());
        // Typedefs are typically not unknown
        assertFalse(myType.getType().isUnknownType());
    }
}