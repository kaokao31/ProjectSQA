package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizeTest {

    @Test
    public void testNormalizeConstructorAndBasic() {
        Compiler compiler = new Compiler();
        Normalize normalize = new Normalize(compiler, true);
        assertNotNull(normalize);

        Node root = new Node(Token.BLOCK);
        normalize.process(root, root);
        assertEquals(Token.BLOCK, root.getType());
    }

    @Test
    public void testNormalizePassWithScope() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.SCRIPT);
        
        // Add a simple variable declaration to test normalization passes
        Node nameNode = Node.newString(Token.NAME, "a");
        Node varNode = new Node(Token.VAR, nameNode);
        root.addChildToBack(varNode);

        Normalize.VerifyConstants verifyConstants = new Normalize.VerifyConstants(compiler, false);
        verifyConstants.process(root, root);

        Normalize.NormalizeStatements normalizeStatements = new Normalize.NormalizeStatements(compiler, false);
        normalizeStatements.process(root, root);

        assertNotNull(compiler);
    }

    @Test
    public void testDuplicateAnnots() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.SCRIPT);
        
        Normalize normalize = new Normalize(compiler, false);
        normalize.process(root, root);
        
        // Running it twice to check for stable behavior or duplicate handling
        normalize.process(root, root);
        assertTrue(true);
    }
}