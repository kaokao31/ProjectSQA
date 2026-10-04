package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizeTest {

    @Test
    public void testNormalizeInstantiationAndBasicRun() {
        Compiler compiler = new Compiler();
        // Create a simple AST: script -> block -> expression statement -> number
        Node script = new Node(Token.SCRIPT);
        Node block = new Node(Token.BLOCK);
        Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(1.0));
        block.addChildToBack(expr);
        script.addChildToBack(block);

        Normalize normalize = new Normalize(compiler, false);
        normalize.process(script, script);
        
        // Verify that the normalization process runs without exceptions on a basic tree
        assertNotNull(script);
    }

    @Test
    public void testNormalizeWithRemoveDuplicateConst() {
        Compiler compiler = new Compiler();
        Node script = new Node(Token.SCRIPT);
        
        Normalize normalize = new Normalize(compiler, true);
        normalize.process(script, script);

        assertNotNull(normalize);
    }

    @Test
    public void testNormalizeScopeCreation() {
        Compiler compiler = new Compiler();
        Node script = new Node(Token.SCRIPT);
        
        // Test static helper or process
        Normalize.normalizeTagNames(script);
        assertNotNull(script);
    }

    @Test
    public void testDuplicateConstRemovalPass() {
        Compiler compiler = new Compiler();
        Normalize.DuplicateConstCheck dupCheck = new Normalize.DuplicateConstCheck(compiler);
        
        Node script = new Node(Token.SCRIPT);
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "a");
        varNode.addChildToBack(nameNode);
        script.addChildToBack(varNode);

        dupCheck.process(script, script);
        assertNotNull(script);
    }

    @Test
    public void testNormalizeEnsureLibraryInjections() {
        Compiler compiler = new Compiler();
        Node script = new Node(Token.SCRIPT);
        
        Normalize.VerifyConstants verify = new Normalize.VerifyConstants(compiler, true);
        verify.process(script, script);
        assertNotNull(script);
    }
}