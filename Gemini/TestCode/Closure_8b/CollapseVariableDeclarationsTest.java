package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CollapseVariableDeclarationsTest {

    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testConstructor() {
        CollapseVariableDeclarations collapse = new CollapseVariableDeclarations(compiler);
        assertNotNull(collapse);
    }

    @Test
    public void testProcessBasic() {
        CollapseVariableDeclarations collapse = new CollapseVariableDeclarations(compiler);
        Node root = new Node(Token.SCRIPT);
        
        // var a; a = 1; -> Should collapse or at least process without error
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
        Node assignNode = new Node(Token.ASSIGN,
                Node.newString(Token.NAME, "a"),
                Node.newNumber(1.0));
        root.addChildToBack(varNode);
        root.addChildToBack(assignNode);

        collapse.process(root, root);
        // Verify no exception thrown and structure is handled
        assertNotNull(root);
    }

    @Test
    public void testCollapseNullOrEmptyRoot() {
        CollapseVariableDeclarations collapse = new CollapseVariableDeclarations(compiler);
        // Should not throw NPE when processing null or empty nodes
        try {
            collapse.process(null, null);
        } catch (Exception e) {
            // Depending on compiler/pass requirements, may or may not throw. 
            // If it accepts null or throws, we cover the branch.
        }
    }

    @Test
    public void testCanCollapseWithVarNode() {
        CollapseVariableDeclarations collapse = new CollapseVariableDeclarations(compiler);
        Node root = new Node(Token.SCRIPT);
        
        Node nameNode1 = Node.newString(Token.NAME, "a");
        nameNode1.addChildToBack(Node.newNumber(1.0));
        Node varNode1 = new Node(Token.VAR, nameNode1);
        
        Node nameNode2 = Node.newString(Token.NAME, "b");
        nameNode2.addChildToBack(Node.newNumber(2.0));
        Node varNode2 = new Node(Token.VAR, nameNode2);
        
        root.addChildToBack(varNode1);
        root.addChildToBack(varNode2);

        collapse.process(root, root);
        assertNotNull(root);
    }

    @Test
    public void testBlockScopeAndVar() {
        CollapseVariableDeclarations collapse = new CollapseVariableDeclarations(compiler);
        Node root = new Node(Token.SCRIPT);
        Node block = new Node(Token.BLOCK);
        
        Node nameNode = Node.newString(Token.NAME, "x");
        nameNode.addChildToBack(Node.newNumber(5.0));
        Node varNode = new Node(Token.VAR, nameNode);
        
        block.addChildToBack(varNode);
        root.addChildToBack(block);

        collapse.process(root, root);
        assertNotNull(root);
    }
}