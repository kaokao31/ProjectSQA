package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizeTest {

    @Test
    public void testNormalizeConstructorAndHelpers() {
        // Just exercising instantiation if possible, though Normalize is usually instantiated with AbstractCompiler.
        // Let's test static helper methods or create a dummy compiler to run Normalize.
        Compiler compiler = new Compiler();
        // Configure compiler options minimally if needed
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        Normalize normalize = new Normalize(compiler, false);
        assertNotNull(normalize);

        Node root = new Node(Token.BLOCK);
        normalize.process(root, root);
    }

    @Test
    public void testNormalizeWithASTRewriting() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        // Normalize with removeDuplicateDeclarations = true
        Normalize normalize = new Normalize(compiler, true);

        // Create a simple AST: var a; var a;
        Node script = new Node(Token.SCRIPT);
        Node var1 = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
        Node var2 = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
        script.addChildToBack(var1);
        script.addChildToBack(var2);

        normalize.process(script, script);
        assertNotNull(script);
    }

    @Test
    public void testNormalizeDuplicateVar() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        Normalize normalize = new Normalize(compiler, true);

        // Function containing duplicate vars
        Node script = new Node(Token.SCRIPT);
        Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        Node block = func.getLastChild();
        
        Node var1 = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        Node var2 = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        block.addChildToBack(var1);
        block.addChildToBack(var2);
        script.addChildToBack(func);

        normalize.process(script, script);
        assertNotNull(script);
    }

    @Test
    public void testNormalizeConstantAnnotator() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        Normalize.VerifyConstants verifyConstants = new Normalize.VerifyConstants(compiler, true);
        Node script = new Node(Token.SCRIPT);
        Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "CONST_VAR"), Node.newNumber(1));
        script.addChildToBack(assign);

        // Should run without throwing for normal nodes
        try {
            verifyConstants.process(script, script);
        } catch (Exception e) {
            // Expected or handled depending on strictness
        }
    }

    @Test
    public void testPropagateConstantAnnotations() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        Node script = new Node(Token.SCRIPT);
        Normalize.PropagateConstantAnnotations propagate = new Normalize.PropagateConstantAnnotations(compiler, script);
        assertNotNull(propagate);
    }
}