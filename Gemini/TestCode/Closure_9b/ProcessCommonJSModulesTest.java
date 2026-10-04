package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProcessCommonJSModulesTest {

    private Compiler compiler;
    private ProcessCommonJSModules processor;
    private static final String DEFAULT_FILENAME = "test.js";

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options sufficiently for module processing
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        // Default module prefix
        processor = new ProcessCommonJSModules(compiler, DEFAULT_FILENAME);
    }

    @Test
    public void testProcessNonCommonJSModule() {
        // A simple script node that doesn't use CommonJS patterns
        Node root = new Node(Token.SCRIPT);
        Node expr = new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, 1.0));
        root.addChildToBack(expr);

        processor.process(root, root);
        // Should not crash and should leave the tree intact or untouched regarding CommonJS
        assertNotNull(root);
    }

    @Test
    public void testModuleNameGeneration() {
        // Test various filenames for module name conversion
        assertEquals("module$test", ProcessCommonJSModules.toModuleName(DEFAULT_FILENAME));
        assertEquals("module$dir$file", ProcessCommonJSModules.toModuleName("dir/file.js"));
        assertEquals("module$index", ProcessCommonJSModules.toModuleName("index.js"));
        assertEquals("module$foo_bar", ProcessCommonJSModules.toModuleName("foo-bar.js"));
        assertEquals("module$relative$path", ProcessCommonJSModules.toModuleName("./relative/path.js"));
        assertEquals("module$parent$path", ProcessCommonJSModules.toModuleName("../parent/path.js"));
    }

    @Test
    public void testModuleNameAbsoluteAndNodeModules() {
        assertEquals("module$my_module$index", ProcessCommonJSModules.toModuleName("/abs/path/node_modules/my-module/index.js"));
        assertEquals("module$my_module", ProcessCommonJSModules.toModuleName("node_modules/my-module.js"));
    }

    @Test
    public void testRequireCallNoModuleName() {
        // Test require with invalid or missing argument
        Node root = new Node(Token.SCRIPT);
        // require() without arguments
        Node requireCall = new Node(Token.CALL, Node.newString(Token.NAME, "require"));
        Node expr = new Node(Token.EXPR_RESULT, requireCall);
        root.addChildToBack(expr);

        processor.process(root, root);
        // Should report a compiler error for invalid require
        assertTrue(compiler.hasErrors());
    }

    @Test
    public void testRequireCallLiteral() {
        // require('moduleName')
        Node root = new Node(Token.SCRIPT);
        Node requireCall = new Node(Token.CALL, 
                Node.newString(Token.NAME, "require"),
                Node.newString("otherModule"));
        Node expr = new Node(Token.EXPR_RESULT, requireCall);
        root.addChildToBack(expr);

        processor.process(root, root);
        assertFalse(compiler.hasErrors());
    }

    @Test
    public void testModuleExportsAssignment() {
        // module.exports = ...
        Node root = new Node(Token.SCRIPT);
        Node exportsAccess = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "module"), 
                Node.newString("exports"));
        Node assign = new Node(Token.ASSIGN, exportsAccess, new Node(Token.NUMBER, 42.0));
        Node expr = new Node(Token.EXPR_RESULT, assign);
        root.addChildToBack(expr);

        processor.process(root, root);
        assertFalse(compiler.hasErrors());
    }

    @Test
    public void testExportsMemberAssignment() {
        // exports.foo = ...
        Node root = new Node(Token.SCRIPT);
        Node exportsAccess = new Node(Token.GETPROP, 
                Node.newString(Token.NAME, "exports"), 
                Node.newString("foo"));
        Node assign = new Node(Token.ASSIGN, exportsAccess, new Node(Token.NUMBER, 42.0));
        Node expr = new Node(Token.EXPR_RESULT, assign);
        root.addChildToBack(expr);

        processor.process(root, root);
        assertFalse(compiler.hasErrors());
    }

    @Test
    public void testGetFilename() {
        assertEquals(DEFAULT_FILENAME, processor.getFilename());
    }

    @Test
    public void testGetModuleId() {
        assertEquals("test", processor.getModuleId(DEFAULT_FILENAME));
        assertEquals("dir/file", processor.getModuleId("dir/file.js"));
    }
}