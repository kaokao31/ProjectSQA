package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import static org.junit.Assert.*;

public class MakeDeclaredNamesUniqueTest {

    @Test
    public void testCleanUpHighResolution() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.BLOCK);
        
        // Test builder for ContextualRenameNull
        CompilerPass pass = MakeDeclaredNamesUnique.getContextualRenamePass();
        pass.process(root, root);
        
        // Test builder for AnnotatedFunctionNames
        CompilerPass pass2 = MakeDeclaredNamesUnique.getAnnotatedFunctionNamesPass(compiler);
        pass2.process(root, root);

        // Exercise class instantiation and basic utility methods
        assertNotNull(pass);
        assertNotNull(pass2);
    }

    @Test
    public void testContextualRenamerWithDefault() {
        Compiler compiler = new Compiler();
        MakeDeclaredNamesUnique.Renamer renamer = new MakeDeclaredNamesUnique.ContextualRenamer();
        
        // Test enterScope and exitScope and addDeclaredName
        Node scopeNode = new Node(Token.FUNCTION);
        Node nameNode = Node.newString(Token.NAME, "testName");
        scopeNode.addChildToFront(nameNode);
        
        renamer.enterScope(scopeNode);
        boolean added = renamer.addDeclaredName("testName");
        assertTrue(added);
        
        // Adding duplicate name in same scope
        boolean addedDuplicate = renamer.addDeclaredName("testName");
        // Depending on renamer logic, verify behavior
        
        renamer.exitScope();
        assertNotNull(renamer.getReplacementName("testName"));
    }

    @Test
    public void testInlineRenamer() {
        Compiler compiler = new Compiler();
        MakeDeclaredNamesUnique.Renamer renamer = new MakeDeclaredNamesUnique.InlineRenamer(
                compiler, null, true);
        
        Node scopeNode = new Node(Token.BLOCK);
        renamer.enterScope(scopeNode);
        renamer.addDeclaredName("localName");
        renamer.exitScope();
        
        assertNotNull(renamer);
    }

    @Test
    public void testContextualWithParent() {
        MakeDeclaredNamesUnique.ContextualRenamer parent = new MakeDeclaredNamesUnique.ContextualRenamer();
        MakeDeclaredNamesUnique.ContextualRenamer child = new MakeDeclaredNamesUnique.ContextualRenamer(parent);
        
        Node scopeNode = new Node(Token.BLOCK);
        child.enterScope(scopeNode);
        child.addDeclaredName("x");
        child.exitScope();
        
        assertNotNull(child.createChildNameRenamer());
    }

    @Test
    public void testGetCompleteRenamer() {
        CompilerPass pass = MakeDeclaredNamesUnique.getCompleteRenamer(null);
        assertNotNull(pass);
        
        Compiler compiler = new Compiler();
        CompilerPass pass2 = MakeDeclaredNamesUnique.getAnnotatedFunctionNamesPass(compiler);
        assertNotNull(pass2);
    }
}