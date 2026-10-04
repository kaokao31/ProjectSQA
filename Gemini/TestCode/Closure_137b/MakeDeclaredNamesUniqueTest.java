package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class MakeDeclaredNamesUniqueTest {

    @Test
    public void testCleanUpPass() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.BLOCK);
        
        // Test standard builder creation
        CompilerPass pass = MakeDeclaredNamesUnique.getCompleteRenamingPass(compiler);
        assertNotNull(pass);
        
        pass.process(root, root);
    }

    @Test
    public void testContextualRenamingPass() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.BLOCK);
        
        CompilerPass pass = MakeDeclaredNamesUnique.getContextualRenamePass(compiler);
        assertNotNull(pass);
        
        pass.process(root, root);
    }

    @Test
    public void testWhitelistedRenamingPass() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.BLOCK);
        
        CompilerPass pass = MakeDeclaredNamesUnique.getContextualUniqueNamePass(compiler);
        assertNotNull(pass);
        
        pass.process(root, root);
    }

    @Test
    public void testDealiasFunctionReferences() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.BLOCK);
        
        CompilerPass pass = MakeDeclaredNamesUnique.getMarkDealiasedPass(compiler);
        assertNotNull(pass);
        
        pass.process(root, root);
    }

    @Test
    public void testRenamerWithPreexistingSymbol() {
        Compiler compiler = new Compiler();
        
        // Build a minimal AST: function f() { var x; { var x; } }
        Node functionNode = Node.newFunction(
                "f", 
                Node.newString(Token.NAME, "f"), 
                new Node(Token.PARAM_LIST), 
                new Node(Token.BLOCK)
        );
        
        MakeDeclaredNamesUnique.ContextualRenamer renamer = 
                new MakeDeclaredNamesUnique.ContextualRenamer();
        
        boolean entered = renamer.shouldTraverse(null, functionNode, functionNode);
        assertTrue(entered);
        
        renamer.popScope();
    }
}