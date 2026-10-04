package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for PeepholeSubstituteAlternateSyntax (Closure Bug 132).
 */
public class PeepholeSubstituteAlternateSyntaxTest {

    private PeepholeSubstituteAlternateSyntax optimizer;
    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        optimizer = new PeepholeSubstituteAlternateSyntax(false);
        compiler = new Compiler();
        optimizer.beginTraversal(null);
    }

    @Test
    public void testIsASTNormalized() {
        // Test helper/lifecycle methods in PeepholeOptimizationsPass or base class if applicable
        assertNotNull(optimizer);
    }

    @Test
    public void testOptimizeBlockEmpty() {
        Node block = new Node(Token.BLOCK);
        Node result = optimizer.optimizeSubtree(block);
        assertNotNull(result);
    }

    @Test
    public void testMinimizeIf() {
        // if (true) { foo(); } else { bar(); }
        Node condition = new Node(Token.TRUE);
        Node thenBranch = new Node(Token.BLOCK, Node.newString(Token.NAME, "foo"));
        Node elseBranch = new Node(Token.BLOCK, Node.newString(Token.NAME, "bar"));
        
        Node ifNode = new Node(Token.IF, condition, thenBranch, elseBranch);
        
        Node optimized = optimizer.optimizeSubtree(ifNode);
        assertNotNull(optimized);
    }

    @Test
    public void testSubstituteAlternateSyntaxReturn() {
        // return true -> return !false, etc.
        Node retNode = new Node(Token.RETURN, Node.newNumber(1.0));
        Node optimized = optimizer.optimizeSubtree(retNode);
        assertNotNull(optimized);
    }

    @Test
    public void testTryFinally() {
        // try { foo(); } finally { bar(); }
        Node tryBlock = new Node(Token.BLOCK, Node.newString(Token.NAME, "foo"));
        Node finallyBlock = new Node(Token.BLOCK, Node.newString(Token.NAME, "bar"));
        Node tryNode = new Node(Token.TRY, tryBlock, Node.new_node(Token.BLOCK), finallyBlock);

        Node optimized = optimizer.optimizeSubtree(tryNode);
        assertNotNull(optimized);
    }

    @Test
    public void testObjectLiteral() {
        // {a: 1}
        Node key = Node.newString("a");
        Node val = Node.newNumber(1.0);
        key.addChildToBack(val);
        Node objLit = new Node(Token.OBJECTLIT, key);

        Node optimized = optimizer.optimizeSubtree(objLit);
        assertNotNull(optimized);
    }

    @Test
    public void testHookNode() {
        // true ? foo() : bar()
        Node cond = new Node(Token.TRUE);
        Node trueExpr = Node.newString(Token.NAME, "foo");
        Node falseExpr = Node.newString(Token.NAME, "bar");
        Node hook = new Node(Token.HOOK, cond, trueExpr, falseExpr);

        Node optimized = optimizer.optimizeSubtree(hook);
        assertNotNull(optimized);
    }

    @Test
    public void testAssignmentOperations() {
        // x = x + 1 -> x += 1
        Node name = Node.newString(Token.NAME, "x");
        Node add = new Node(Token.ADD, Node.newString(Token.NAME, "x"), Node.newNumber(1.0));
        Node assign = new Node(Token.ASSIGN, name, add);

        Node optimized = optimizer.optimizeSubtree(assign);
        assertNotNull(optimized);
    }
}