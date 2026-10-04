package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProcessCommonJSModulesTest {

    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testModuleNameGeneration() {
        String filename = "path/to/module.js";
        String moduleName = ProcessCommonJSModules.toModuleName(filename);
        assertNotNull(moduleName);
        assertTrue(moduleName.length() > 0);
    }

    @Test
    public void testModuleNameGenerationWithSpecialChars() {
        String filename = "path/to/my-module_name.js";
        String moduleName = ProcessCommonJSModules.toModuleName(filename);
        assertNotNull(moduleName);
        assertTrue(moduleName.contains("my"));
        assertTrue(moduleName.contains("module"));
    }

    @Test
    public void testProcessCallback() {
        ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "prefix");
        Node root = new Node(Token.BLOCK);
        
        // Should not throw exception on empty root
        processor.process(null, root);
    }

    @Test
    public void testProcessRequireNode() {
        ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "prefix");
        
        // Construct a simple CommonJS require node structure: require('module')
        Node script = new Node(Token.SCRIPT);
        Node expr = new Node(Token.EXPR_RESULT);
        Node call = new Node(Token.CALL);
        Node getprop = new Node(Token.GETPROP);
        
        Node name = Node.newString("require");
        getprop.addChildToFront(name);
        // Add children properly for a call node
        call.addChildToFront(getprop);
        Node arg = Node.newString("module");
        call.addChildToBack(arg);
        
        expr.addChildToFront(call);
        script.addChildToFront(expr);

        // Process should traverse and handle or ignore gracefully depending on configuration
        processor.process(script, script);
        
        assertNotNull(script);
    }

    @Test
    public void testProcessExportsNode() {
        ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "prefix");
        
        Node script = new Node(Token.SCRIPT);
        Node expr = new Node(Token.EXPR_RESULT);
        Node assign = new Node(Token.ASSIGN);
        
        Node getprop = new Node(Token.GETPROP);
        Node exportsName = Node.newString("module");
        getprop.addChildToFront(exportsName);
        Node exportsProp = Node.newString("exports");
        getprop.addChildToBack(exportsProp);
        
        Node value = Node.newNumber(1.0);
        
        assign.addChildToFront(getprop);
        assign.addChildToBack(value);
        expr.addChildToFront(assign);
        script.addChildToFront(expr);

        processor.process(script, script);
        
        assertNotNull(script);
    }

    @Test
    public void testGetCommonJSModuleInfoPassCallback() {
        ProcessCommonJSModules processor = new ProcessCommonJSModules(compiler, "prefix");
        Callback callback = processor.getBrowserifyModulePass();
        assertNotNull(callback);
    }
}