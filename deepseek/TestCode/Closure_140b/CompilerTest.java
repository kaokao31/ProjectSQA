package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.google.javascript.rhino.Node;

public class CompilerTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testConstructor() {
        assertNotNull(compiler);
    }

    @Test
    public void testCompileWithSimpleSource() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "var x = 1;");
        Result result = compiler.compile(externs, input, options);
        assertTrue(result.success);
        assertNotNull(compiler.toSource());
        assertTrue(compiler.toSource().contains("x"));
    }

    @Test
    public void testCompileWithEmptyExterns() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "var y = 2;");
        Result result = compiler.compile(new SourceFile[]{externs}, new SourceFile[]{input}, options);
        assertTrue(result.success);
        assertNotNull(compiler.toSource());
    }

    @Test
    public void testCompileWithNoInput() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        Result result = compiler.compile(new SourceFile[]{externs}, new SourceFile[0], options);
        assertTrue(result.success);
    }

    @Test
    public void testCompileNullInput() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        try {
            compiler.compile(externs, null, options);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testCompileNullOptions() {
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "var z = 3;");
        try {
            compiler.compile(externs, input, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testParse() {
        SourceFile input = SourceFile.fromCode("input.js", "function f(){return 1;}");
        Node ast = compiler.parse(input);
        assertNotNull(ast);
        assertTrue(ast.isScript());
    }

    @Test
    public void testParseWithSyntaxError() {
        SourceFile input = SourceFile.fromCode("input.js", "function {");
        Node ast = compiler.parse(input);
        assertNull(ast);
        assertTrue(compiler.getErrorCount() > 0);
    }

    @Test
    public void testToSourceBeforeCompile() {
        assertNull(compiler.toSource());
    }

    @Test
    public void testToSourceAfterCompile() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "var a = 42;");
        compiler.compile(externs, input, options);
        String source = compiler.toSource();
        assertNotNull(source);
        assertTrue(source.contains("42"));
    }

    @Test
    public void testGetRoot() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "var b = 5;");
        compiler.compile(externs, input, options);
        Node root = compiler.getRoot();
        assertNotNull(root);
    }

    @Test
    public void testGetErrors() {
        assertEquals(0, compiler.getErrorCount());

        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "var c = 6;");
        compiler.compile(externs, input, options);
        assertEquals(0, compiler.getErrorCount());

        SourceFile badInput = SourceFile.fromCode("bad.js", "var");
        compiler.compile(externs, badInput, options);
        assertTrue(compiler.getErrorCount() > 0);
    }

    @Test
    public void testEnableTraversal() {
        CompilerOptions options = new CompilerOptions();
        options.setCheckTypes(true);
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "/** @type {number} */ var x = 'not a number';");
        Result result = compiler.compile(externs, input, options);
        assertFalse(result.success);
        assertTrue(compiler.getErrorCount() > 0);
    }

    @Test
    public void testCompileWithEmptyInput() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "");
        Result result = compiler.compile(externs, input, options);
        assertTrue(result.success);
    }

    @Test
    public void testCompileWithEmptyExternsAndInputs() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input = SourceFile.fromCode("input.js", "var e;");
        Result result = compiler.compile(new SourceFile[0], new SourceFile[]{input}, options);
        assertTrue(result.success);
    }

    @Test
    public void testCompileWithMultipleInputs() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        SourceFile input1 = SourceFile.fromCode("input1.js", "var a = 1;");
        SourceFile input2 = SourceFile.fromCode("input2.js", "var b = a + 1;");
        Result result = compiler.compile(externs, new SourceFile[]{input1, input2}, options);
        assertTrue(result.success);
        assertNotNull(compiler.toSource());
    }

    @Test
    public void testCompileWithNullExterns() {
        CompilerOptions options = new CompilerOptions();
        SourceFile input = SourceFile.fromCode("input.js", "var d = 1;");
        try {
            compiler.compile(null, input, options);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testCompileWithNullExternsArray() {
        CompilerOptions options = new CompilerOptions();
        SourceFile input = SourceFile.fromCode("input.js", "var f = 1;");
        try {
            compiler.compile(null, new SourceFile[]{input}, options);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testCompileWithNullInputArray() {
        CompilerOptions options = new CompilerOptions();
        SourceFile externs = SourceFile.fromCode("externs.js", "");
        try {
            compiler.compile(new SourceFile[]{externs}, null, options);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
}