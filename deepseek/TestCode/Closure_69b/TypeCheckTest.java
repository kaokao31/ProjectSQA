package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.TypeCheck;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypeCheckTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Enable type checking
        options.setCheckTypes(true);
        // Set warning level
        options.setWarningLevel(DiagnosticGroups.TYPE_CHECKING, CheckLevel.WARNING);
    }

    // Helper to parse and type-check a snippet
    private void checkSnippet(String snippet) {
        SourceFile source = SourceFile.fromCode("test.js", snippet);
        compiler.compile(
            SourceFile.fromCode("externs.js", "function alert(x) {}"),
            source,
            options);
        // After compilation, type checks are performed
        // Errors/warnings are stored in compiler.getWarnings() and compiler.getErrors()
    }

    // Basic type tests
    @Test
    public void testLiteralTypes() {
        checkSnippet("var x = 1;");
        assertTrue(compiler.getWarnings().isEmpty());
        assertTrue(compiler.getErrors().isEmpty());
    }

    @Test
    public void testStringLiteralType() {
        checkSnippet("var s = 'hello';");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testBooleanLiteralType() {
        checkSnippet("var b = true;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testNullType() {
        checkSnippet("var n = null;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testUndefinedType() {
        checkSnippet("var u = undefined;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Variable type inference
    @Test
    public void testTypeInferenceNumber() {
        checkSnippet("var x = 5; x = 10;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testTypeInferenceString() {
        checkSnippet("var s = 'a'; s = 'b';");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Function type checks
    @Test
    public void testFunctionReturnType() {
        checkSnippet("/** @return {number} */ function f() { return 1; }");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testFunctionReturnTypeMismatch() {
        checkSnippet("/** @return {number} */ function f() { return 'string'; }");
        // Expect a warning due to type mismatch
        assertFalse(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testFunctionParameterType() {
        checkSnippet("/** @param {string} s */ function f(s) {}");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testFunctionParameterTypeMismatch() {
        checkSnippet("/** @param {string} s */ function f(s) {}; f(123);");
        assertFalse(compiler.getWarnings().isEmpty());
    }

    // Array type checks
    @Test
    public void testArrayLiteralType() {
        checkSnippet("var a = [1, 2, 3];");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testArrayTypeAnnotation() {
        checkSnippet("/** @type {Array.<number>} */ var a = [1,2];");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Object type checks
    @Test
    public void testObjectLiteral() {
        checkSnippet("var o = {x: 1, y: 'hello'};");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Union types
    @Test
    public void testUnionType() {
        checkSnippet("/** @type {(number|string)} */ var x = 1; x = 'hello';");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testUnionTypeMismatch() {
        checkSnippet("/** @type {(number|string)} */ var x = 1; x = true;");
        assertFalse(compiler.getWarnings().isEmpty());
    }

    // Nullable types
    @Test
    public void testNullableType() {
        checkSnippet("/** @type {?number} */ var x = null; x = 5;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Non-nullable type
    @Test
    public void testNonNullableType() {
        checkSnippet("/** @type {!number} */ var x = 1;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // typeof operator checks (not fully type-checked but test coverage)
    @Test
    public void testTypeOf() {
        checkSnippet("var t = typeof 1;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // instanceof check
    @Test
    public void testInstanceOf() {
        checkSnippet("var d = new Date(); var b = d instanceof Date;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Generic function (potential bug 69)
    @Test
    public void testGenericFunctionCall() {
        // This simulates a generic function where type of argument may be inferred incorrectly
        checkSnippet("/** @param {T} x @return {T} */ function identity(x) { return x; }" +
                     "var num = identity(1);");
        // Should not warn if type inference works
        assertTrue(compiler.getWarnings().isEmpty());
    }

    @Test
    public void testGenericFunctionCallMismatch() {
        // If generic is constrained, call with wrong type may produce warning
        checkSnippet("/** @param {Array.<T>} arr @return {T} */ function first(arr) { return arr[0]; }" +
                     "var res = first([1,2]);"); // Should be number
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Edge case: empty function
    @Test
    public void testEmptyFunction() {
        checkSnippet("function f() {}");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Edge case: nested functions
    @Test
    public void testNestedFunctions() {
        checkSnippet("function outer() { function inner() { return 1; } return inner(); }");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Edge case: undefined variable access (should produce warning)
    @Test
    public void testUndefinedVariable() {
        checkSnippet("x = 10;"); // No var -> global, may be warned
        // Depending on strictness, might be warning. We just check no crash.
    }

    // Test for bug 69: TypeCheck of function call with generic return type and no annotation
    @Test
    public void testBug69Regression() {
        // Known issue: When a generic function is called without explicit type,
        // TypeCheck might not compute the correct return type and cause a warning.
        // This test ensures that no spurious warnings appear.
        checkSnippet("/** @constructor */ function MyClass() {};" +
                     "MyClass.prototype.method = function() { return this; };" +
                     "var obj = new MyClass(); var result = obj.method();");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Property access type check
    @Test
    public void testPropertyAccess() {
        checkSnippet("var obj = {a: 1}; var val = obj.a;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Property access with wrong type
    @Test
    public void testPropertyAccessMismatch() {
        checkSnippet("/** @type {{a: number}} */ var obj = {a: 'hello'};");
        assertFalse(compiler.getWarnings().isEmpty());
    }

    // Test with @type annotation on variable
    @Test
    public void testTypeAnnotationOnVariable() {
        checkSnippet("/** @type {number} */ var x = 10;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test with @type annotation mismatch
    @Test
    public void testTypeAnnotationVariableMismatch() {
        checkSnippet("/** @type {number} */ var x = 'not a number';");
        assertFalse(compiler.getWarnings().isEmpty());
    }

    // Test typeof comparison (not strict type check but needs coverage)
    @Test
    public void testTypeOfComparison() {
        checkSnippet("var x = 1; if (typeof x === 'number') { alert('num'); }");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test null vs undefined
    @Test
    public void testNullAndUndefined() {
        checkSnippet("var a = null; var b = undefined;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test arithmetic operations
    @Test
    public void testArithmeticOperations() {
        checkSnippet("var sum = 1 + 2; var diff = 2 - 1; var prod = 2 * 3; var quot = 6 / 2;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test concatenation
    @Test
    public void testStringConcatenation() {
        checkSnippet("var s = 'hello' + ' ' + 'world';");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test mixed types in addition (potential warning)
    @Test
    public void testMixedTypeAddition() {
        checkSnippet("var x = 1 + 'string';"); // Should warn about mixing types
        assertFalse(compiler.getWarnings().isEmpty());
    }

    // Test type cast annotation (JSDoc @type)
    @Test
    public void testTypeCast() {
        checkSnippet("/** @type {number} */ (someUnknown);");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test for loops with type inference
    @Test
    public void testForLoop() {
        checkSnippet("for (var i = 0; i < 10; i++) { var j = i; }");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test while loop
    @Test
    public void testWhileLoop() {
        checkSnippet("var x = 0; while (x < 5) { x++; }");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test do-while loop
    @Test
    public void testDoWhileLoop() {
        checkSnippet("var x = 0; do { x++; } while (x < 5);");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test conditional operator
    @Test
    public void testConditionalOperator() {
        checkSnippet("var x = true ? 1 : 2;");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test function that returns undefined
    @Test
    public void testFunctionReturningUndefined() {
        checkSnippet("function f() { return; }");
        assertTrue(compiler.getWarnings().isEmpty());
    }

    // Test that TypeCheck does not crash on complex expressions
    @Test
    public void testComplexExpression() {
        checkSnippet("var x = (function() { return 1; })() + (function() { return 2; })();");
        assertTrue(compiler.getWarnings().isEmpty());
    }
}