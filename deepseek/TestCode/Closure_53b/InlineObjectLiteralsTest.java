package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class InlineObjectLiteralsTest {
    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Enable the InlineObjectLiterals pass via optimization options.
        options.setInlineProperties(true);
        options.setOptimizationLevel(CompilerOptions.OptimizationLevel.SIMPLE_OPTIMIZATIONS);
        // Disable other passes that might interfere.
        options.setColorizeErrors(false);
        options.setPrintInputDelimiter(false);
        // Use quiet mode to suppress warnings.
        options.setQuietMode(true);
    }

    private String compile(String js) {
        JSSourceFile input = JSSourceFile.fromCode("test.js", js);
        JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
        JSSourceFile[] inputs = new JSSourceFile[]{input};
        Result result = compiler.compile(extern, inputs, options);
        if (result.success) {
            return compiler.toSource();
        } else {
            fail("Compilation failed: " + result.errors);
            return null;
        }
    }

    // ----- Basic inlining (safe) -----
    @Test
    public void testSimplePropertyAccess() {
        String input = "var x = {a: 1}; use(x.a);";
        String expected = "var x = {a: 1}; use(1);";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testMultipleProperties() {
        String input = "var obj = {a:1, b:2}; print(obj.a, obj.b);";
        String expected = "var obj = {a:1, b:2}; print(1, 2);";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testInliningWithComputedProperty() {
        String input = "var key = 'a'; var obj = {[key]: 1}; use(obj.a);";
        String expected = "var key = 'a'; var obj = {[key]: 1}; use(obj.a);";
        assertEquals(expected, compile(input));
    }

    // ----- Scenarios where inlining is unsafe -----
    @Test
    public void testObjectReassigned() {
        String input = "var obj = {a: 1}; obj = {a: 2}; use(obj.a);";
        String expected = "var obj = {a: 1}; obj = {a: 2}; use(obj.a);";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testObjectPassedToFunction() {
        String input = "function f(o) { return o.a; } var obj = {a: 1}; f(obj);";
        String expected = "function f(o) { return o.a; } var obj = {a: 1}; f(obj);";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testObjectUsedInLoop() {
        String input = "var obj = {a: 1}; for(var i = 0; i < 5; i++) { use(obj.a); }";
        String expected = "var obj = {a: 1}; for(var i = 0; i < 5; i++) { use(1); }";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testObjectWithGetterSetter() {
        String input = "var obj = {get a() { return 1; }}; use(obj.a);";
        String expected = "var obj = {get a() { return 1; }}; use(obj.a);";
        assertEquals(expected, compile(input));
    }

    // ----- Default parameter edge case (related to bug 53) -----
    @Test
    public void testDefaultParamObjectLiteral() {
        String input = "function f(x = {a: 1}) { return x.a; }\nf(); f();";
        String expected = "function f(x = {a: 1}) { return 1; }\nf(); f();";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testDefaultParamObjectLiteralMultipleCalls() {
        String input = "var sum = 0;\n" +
                "function f(x = {a: 1}) { sum += x.a; }\n" +
                "f();\nf();\nvar result = sum;";
        String expected = "var sum = 0;\n" +
                "function f(x = {a: 1}) { sum += 1; }\n" +
                "f();\nf();\nvar result = sum;";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testDefaultParamObjectLiteralUsedElsewhere() {
        String input = "var def = {a: 1};\n" +
                "function f(x = def) { return x.a; }\n" +
                "f(); f();";
        // The object literal is not in the default directly, so it might be inlined.
        // But def is a variable, not a literal, so inlining should not happen.
        String expected = "var def = {a: 1};\n" +
                "function f(x = def) { return x.a; }\n" +
                "f(); f();";
        assertEquals(expected, compile(input));
    }

    // ----- Nested objects -----
    @Test
    public void testNestedObjectLiteral() {
        String input = "var obj = {a: {b: 2}}; use(obj.a.b);";
        String expected = "var obj = {a: {b: 2}}; use(2);";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testNestedObjectLiteralWithMultipleAccess() {
        String input = "var obj = {a: {b: 2, c: 3}}; use(obj.a.b, obj.a.c);";
        String expected = "var obj = {a: {b: 2, c: 3}}; use(2, 3);";
        assertEquals(expected, compile(input));
    }

    // ----- Mixed with other expressions -----
    @Test
    public void testObjectLiteralInVariableDeclarationWithComma() {
        String input = "var x = (1, {a: 2}); use(x.a);";
        String expected = "var x = (1, {a: 2}); use(2);";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testObjectLiteralInTernary() {
        String input = "var cond = true;\nvar obj = cond ? {a: 1} : {a: 2};\nuse(obj.a);";
        String expected = "var cond = true;\nvar obj = cond ? {a: 1} : {a: 2};\nuse(1);";
        assertEquals(expected, compile(input));
    }

    // ----- Object literal with method calls (side effects) -----
    @Test
    public void testObjectLiteralWithMethod() {
        String input = "var obj = {a: 1, foo: function() { return 2; }};\nuse(obj.a);";
        String expected = "var obj = {a: 1, foo: function() { return 2; }};\nuse(1);";
        assertEquals(expected, compile(input));
    }

    @Test
    public void testObjectLiteralMethodCallSameObject() {
        String input = "var obj = {a: 1, inc: function() { this.a++; }};\nobj.inc();\nuse(obj.a);";
        String expected = "var obj = {a: 1, inc: function() { this.a++; }};\nobj.inc();\nuse(obj.a);";
        assertEquals(expected, compile(input));
    }

    // ----- Array literals (should not be inlined by object literal pass) -----
    @Test
    public void testArrayLiteralNotInlined() {
        String input = "var arr = [1,2,3]; use(arr[0]);";
        String expected = "var arr = [1,2,3]; use(arr[0]);";
        assertEquals(expected, compile(input));
    }

    // ----- Empty object -----
    @Test
    public void testEmptyObjectLiteral() {
        String input = "var obj = {}; use(obj.a);";
        String expected = "var obj = {}; use(obj.a);";
        assertEquals(expected, compile(input));
    }

    // ----- Multiple assignments to same property (may block inlining) -----
    @Test
    public void testObjectLiteralWithSamePropertyReassigned() {
        String input = "var obj = {a: 1, a: 2}; use(obj.a);";
        String expected = "var obj = {a: 1, a: 2}; use(2);";
        assertEquals(expected, compile(input));
    }

    // ----- Object literal used as a property of another object -----
    @Test
    public void testObjectUsedAsProperty() {
        String input = "var outer = {inner: {a: 1}}; use(outer.inner.a);";
        String expected = "var outer = {inner: {a: 1}}; use(1);";
        assertEquals(expected, compile(input));
    }

    // ----- Object literal in export/expose (assume no external effects) -----
    @Test
    public void testObjectLiteralExported() {
        String input = "window.lib = {a: 1}; use(window.lib.a);";
        String expected = "window.lib = {a: 1}; use(window.lib.a);";
        assertEquals(expected, compile(input));
    }

    // ----- Object literal that is accessed via indirect property (bracket notation) -----
    @Test
    public void testObjectLiteralBracketAccess() {
        String input = "var obj = {a: 1}; var key = 'a'; use(obj[key]);";
        String expected = "var obj = {a: 1}; var key = 'a'; use(obj[key]);";
        assertEquals(expected, compile(input));
    }

    // ----- Object literal with numeric keys -----
    @Test
    public void testObjectLiteralNumericKey() {
        String input = "var obj = {1: 'one'}; use(obj[1]);";
        String expected = "var obj = {1: 'one'}; use('one');";
        assertEquals(expected, compile(input));
    }

    // ----- Ensure no inlining when object is used in a try/catch -----
    @Test
    public void testObjectInTryCatch() {
        String input = "var obj = {a: 1};\ntry {\n  throw obj;\n} catch(e) {\n  use(e.a);\n}";
        String expected = "var obj = {a: 1};\ntry {\n  throw obj;\n} catch(e) {\n  use(e.a);\n}";
        assertEquals(expected, compile(input));
    }

    // ----- Object literal with string values that are used elsewhere -----
    @Test
    public void testStringPropertyValue() {
        String input = "var obj = {a: 'hello'}; use(obj.a);";
        String expected = "var obj = {a: 'hello'}; use('hello');";
        assertEquals(expected, compile(input));
    }

    // ----- Object literal with boolean values -----
    @Test
    public void testBooleanPropertyValue() {
        String input = "var obj = {a: true}; if (obj.a) { use('true'); }";
        String expected = "var obj = {a: true}; if (true) { use('true'); }";
        assertEquals(expected, compile(input));
    }

    // ----- Edge case: object literal that is mutated via reference -----
    @Test
    public void testObjectLiteralMutatedViaReference() {
        String input = "var obj = {a: 1};\nvar ref = obj;\nref.a = 2;\nuse(obj.a);";
        String expected = "var obj = {a: 1};\nvar ref = obj;\nref.a = 2;\nuse(obj.a);";
        assertEquals(expected, compile(input));
    }
}