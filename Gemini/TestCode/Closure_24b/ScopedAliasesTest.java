package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for ScopedAliases (Closure Bug 24).
 * Designed for JUnit 4 and Java 8 compatibility.
 */
public class ScopedAliasesTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testScopedAliasesInstantiationAndBasicPass() {
        // Prepare a simple AST to exercise the CompilerPass interface of ScopedAliases
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        Node root = getNodeForCode("var goog = {}; goog.provide('goog.dom');");
        
        // Pass with transpile only = false/true or using PreprocessorSymbolTable
        PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable(root);
        ScopedAliases scopedAliases = new ScopedAliases(compiler, symbolTable, CompilerOptions.Alias Transformation.REMOVE);

        // Run the pass
        scopedAliases.process(root, root);
        assertNotNull(root);
    }

    @Test
    public void testWithAliasTransformation() {
        Node root = getNodeForCode("var a = {b: 1}; /** @const */ var c = a; function f() { c.b; }");
        PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable(root);
        
        ScopedAliases scopedAliases = new ScopedAliases(
                compiler, 
                symbolTable, 
                CompilerOptions.AliasTransformationHandler.getNoopHandler()
        );

        scopedAliases.hotSwapScript(root, null);
        assertNotNull(compiler);
    }

    @Test
    public void testAliasWithObjectLitTraversal() {
        // This targets potential edge cases in alias reporting and traversing object literals
        // (common trigger points in Closure 24 / aliases logic with scope tracking)
        Node root = getNodeForCode(
            "goog.scope(function() {" +
            "  var DOM = goog.dom;" +
            "  DOM.createElement('div');" +
            "});"
        );

        PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable(root);
        ScopedAliases scopedAliases = new ScopedAliases(
                compiler, 
                symbolTable, 
                CompilerOptions.AliasTransformation.CREATE_NAMESPACE
        );

        scopedAliases.process(root, root);
        // Verify compiler didn't crash and processed the scope
        assertFalse(compiler.hasHaltingErrors());
    }

    @Test
    public void testAliasReassignmentOrVarScope() {
        // Exercise variable declarations inside alias scopes that might trigger specific branches in ScopeHandler
        Node root = getNodeForCode(
            "var ns = {};" +
            "ns.Sub = function() {};" +
            "goog.scope(function() {" +
            "  var Sub = ns.Sub;" +
            "  var x = new Sub();" +
            "});"
        );

        PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable(root);
        ScopedAliases scopedAliases = new ScopedAliases(
                compiler, 
                symbolTable, 
                CompilerOptions.AliasTransformation.REMOVE
        );

        scopedAliases.process(root, root);
        assertNotNull(root);
    }

    private Node getNodeForCode(String code) {
        compiler.parse(SourceFile.fromCode("testcode", code));
        Node root = compiler.getRoot();
        return root;
    }
}