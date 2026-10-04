package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for TypeCheck, targeting maximum coverage and fault detection.
 */
public class TypeCheckTest {

    private Compiler compiler;
    private TypeCheck typeCheck;

    @Before
    public void setUp() {
        compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        // Create a simple scope for type checking (minimal externs)
        Node externsRoot = new Node(Token.SCRIPT);
        typeCheck = new TypeCheck(compiler, externsRoot);
    }

    // Helper to parse a script and return the root node
    private Node parse(String code) {
        return compiler.parseSyntheticCode(code);
    }

    // Helper to run type checking and return errors
    private JSError[] check(Node root) {
        typeCheck.process(null, root);
        return compiler.getErrors();
    }

    // ========== Basic/Empty Scripts ==========
    @Test
    public void testEmptyScript() {
        Node root = parse("");
        JSError[] errors = check(root);
        assertEquals("Empty script should have no errors.", 0, errors.length);
    }

    @Test
    public void testNullRoot() {
        try {
            typeCheck.process(null, null);
            fail("Expected NullPointerException or similar for null root.");
        } catch (NullPointerException e) {
            // Expected: null root should cause an error.
            assertNotNull(e);
        }
    }

    // ========== Variable Declarations ==========
    @Test
    public void testVarDeclarationNumber() {
        Node root = parse("var x = 1;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testVarDeclarationString() {
        Node root = parse("var s = 'hello';");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testVarDeclarationBoolean() {
        Node root = parse("var b = true;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testVarDeclarationNull() {
        Node root = parse("var n = null;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testVarDeclarationUndefined() {
        Node root = parse("var u;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    // ========== Typeof Operator (Bug 154 focus) ==========
    @Test
    public void testTypeofUndefinedName() {
        // typeof on a name that is not declared should NOT cause NPE
        Node root = parse("typeof unknownVar;");
        JSError[] errors = check(root);
        // There might be a warning about unknown variable, but no errors.
        assertNotNull(errors);
    }

    @Test
    public void testTypeofNumber() {
        Node root = parse("typeof 42;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTypeofString() {
        Node root = parse("typeof 'hello';");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTypeofBoolean() {
        Node root = parse("typeof true;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTypeofUndefinedLiteral() {
        Node root = parse("typeof undefined;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTypeofArrayLiteral() {
        Node root = parse("typeof [1,2];");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTypeofObjectLiteral() {
        Node root = parse("typeof {a:1};");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTypeofFunction() {
        Node root = parse("var f = function(){}; typeof f;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTypeofQualifiedName() {
        // typeof on a qualified name with undeclared root
        Node root = parse("typeof unknownObj.prop;");
        JSError[] errors = check(root);
        // Should not crash; a warning may be emitted.
        assertNotNull(errors);
    }

    @Test
    public void testTypeofThis() {
        Node root = parse("typeof this;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    // ========== Instanceof Operator ==========
    @Test
    public void testInstanceofDate() {
        Node root = parse("var d = new Date(); d instanceof Date;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testInstanceofUnknown() {
        Node root = parse("var x = {}; x instanceof UnknownClass;");
        JSError[] errors = check(root);
        // Should not crash, might have warning
        assertNotNull(errors);
    }

    // ========== Function Calls ==========
    @Test
    public void testSimpleFunctionCall() {
        Node root = parse("function foo() {}; foo();");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testFunctionCallWithArgs() {
        Node root = parse("function add(a,b) { return a+b; }; add(1,2);");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testUndefinedFunctionCall() {
        Node root = parse("undefinedFunc();");
        JSError[] errors = check(root);
        // Should still process without crash; may have errors.
        assertNotNull(errors);
    }

    // ========== Control Flow ==========
    @Test
    public void testIfStatement() {
        Node root = parse("if (true) { var x = 1; } else { var y = 2; }");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testWhileLoop() {
        Node root = parse("var i = 0; while (i < 10) { i++; }");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testForLoop() {
        Node root = parse("for (var i = 0; i < 10; i++) { }");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTryCatch() {
        Node root = parse("try { throw new Error(); } catch (e) { var handled = true; }");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testSwitchStatement() {
        Node root = parse("var x = 'a'; switch(x) { case 'a': break; default: break; }");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    // ========== Binary/Unary Operators ==========
    @Test
    public void testBinaryAddition() {
        Node root = parse("var sum = 1 + 2;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testBinaryLogical() {
        Node root = parse("var result = true && false;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testUnaryNegation() {
        Node root = parse("var neg = -5;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testUnaryNot() {
        Node root = parse("var not = !true;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    // ========== Arrays and Objects ==========
    @Test
    public void testArrayLiteral() {
        Node root = parse("var arr = [1, 'two', true];");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testObjectLiteral() {
        Node root = parse("var obj = {a: 1, b: 'two'};");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testNestedObject() {
        Node root = parse("var obj = {inner: {a: 1}};");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    // ========== Edge Cases and Bug Triggers ==========
    @Test
    public void testFunctionExpressionWithoutName() {
        Node root = parse("var f = function() {};");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testMultipleStatements() {
        Node root = parse("var a = 1; var b = a + 2; var c = b * 3;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testTypeofAfterAssignment() {
        Node root = parse("var x = undefined; typeof x;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testInstanceofWithLiteral() {
        Node root = parse("1 instanceof Number;");
        JSError[] errors = check(root);
        // may warn, but should not crash
        assertNotNull(errors);
    }

    @Test
    public void testConditionalOperator() {
        Node root = parse("var x = true ? 1 : 'two';");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testCommaOperator() {
        Node root = parse("var x = (1, 2);");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testEmptyReturn() {
        Node root = parse("function f() { return; }");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testThrowStatement() {
        Node root = parse("throw 'error';");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testWithStatement() {
        Node root = parse("var obj = {a:1}; with(obj) { var b = a; }");
        JSError[] errors = check(root);
        // 'with' may be flagged but should not crash
        assertNotNull(errors);
    }

    @Test
    public void testDebuggerStatement() {
        Node root = parse("debugger;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testNewExpression() {
        Node root = parse("var d = new Date();");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testNewWithArgs() {
        Node root = parse("var r = new RegExp('test');");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testArrayIndex() {
        Node root = parse("var arr = [1,2]; var x = arr[0];");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testPropertyAccess() {
        Node root = parse("var obj = {a:1}; var val = obj.a;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testPropertyAccessOnUndefined() {
        Node root = parse("var x = undefined; var y = x.property;");
        JSError[] errors = check(root);
        // Should not crash; may produce warning.
        assertNotNull(errors);
    }

    @Test
    public void testCallOnNull() {
        Node root = parse("var x = null; x();");
        JSError[] errors = check(root);
        assertNotNull(errors);
    }

    @Test
    public void testTypeofOnFunctionResult() {
        Node root = parse("function f() { return 42; }; typeof f();");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testNestedTypeof() {
        Node root = parse("typeof typeof 42;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testLabelledStatement() {
        Node root = parse("loop: for (var i = 0; i < 10; i++) { break loop; }");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }

    @Test
    public void testRegularExpression() {
        Node root = parse("var re = /test/gi;");
        JSError[] errors = check(root);
        assertEquals(0, errors.length);
    }
}