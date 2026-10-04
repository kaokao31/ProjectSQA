package com.google.javascript.jscomp;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

/**
 * JUnit 4 test suite for com.google.javascript.jscomp.JsAst.
 * Designed for maximum coverage and fault detection in Defects4J Closure 174.
 */
public class JsAstTest {

    @Test
    public void testSourceFileConstructorAndGetAstRoot() {
        // Test creation with a valid SourceFile
        SourceFile sourceFile = SourceFile.fromCode("testcode.js", "var x = 10;");
        JsAst jsAst = new JsAst(sourceFile);

        Assert.assertNotNull(jsAst);
        Assert.assertEquals("testcode.js", jsAst.getSourceFile().getName());

        // First call to getAstRoot parses and caches
        Node root1 = jsAst.getAstRoot(null);
        Assert.assertNotNull(root1);

        // Second call should return the cached root
        Node root2 = jsAst.getAstRoot(null);
        Assert.assertSame(root1, root2);
    }

    @Test
    public void testClearAst() {
        SourceFile sourceFile = SourceFile.fromCode("testcode.js", "function foo() {}");
        JsAst jsAst = new JsAst(sourceFile);

        Node root1 = jsAst.getAstRoot(null);
        Assert.assertNotNull(root1);

        // Clear the AST
        jsAst.clearAst();

        // Getting the AST root again should reparse
        Node root2 = jsAst.getAstRoot(null);
        Assert.assertNotNull(root2);
        // Due to reparsing, the object reference might be different (or newly generated)
    }

    @Test
    public void testGetInputId() {
        SourceFile sourceFile = SourceFile.fromCode("input_id_test.js", "var a = 1;");
        JsAst jsAst = new JsAst(sourceFile);

        InputId inputId = jsAst.getInputId();
        Assert.assertNotNull(inputId);
        Assert.assertEquals("input_id_test.js", inputId.getIdName());
    }

    @Test
    public void testReorder() {
        SourceFile sourceFile = SourceFile.fromCode("reorder.js", "var b = 2;");
        JsAst jsAst = new JsAst(sourceFile);

        // Just ensure reorder() doesn't throw exceptions
        jsAst.clearAst();
        jsAst.getAstRoot(null);
        jsAst.reorder();
        
        // After reorder, root should still be accessible
        Assert.assertNotNull(jsAst.getAstRoot(null));
    }

    @Test
    public void testSourceFileWithSyntaxErrorOrParseIssue() {
        // Passing malformed JavaScript or empty code to check robustness of parser integration
        SourceFile sourceFile = SourceFile.fromCode("malformed.js", "var = ;");
        JsAst jsAst = new JsAst(sourceFile);

        // Even with syntax errors, getAstRoot typically returns a root node with error reporting attached
        Node root = jsAst.getAstRoot(null);
        Assert.assertNotNull(root);
    }

    @Test
    public void testWithCompilerAccessor() {
        SourceFile sourceFile = SourceFile.fromCode("compiler_test.js", "let y = 20;");
        JsAst jsAst = new JsAst(sourceFile);

        Compiler compiler = new Compiler();
        // Initialize compiler flags/options minimally if needed, or pass directly
        Node root = jsAst.getAstRoot(compiler);
        Assert.assertNotNull(root);
    }
}