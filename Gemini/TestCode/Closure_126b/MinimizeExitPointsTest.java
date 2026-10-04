package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MinimizeExitPointsTest {

    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testTryFinallyWithoutFinallyBlockOrEmpty() {
        // Try node with no finally block
        Node tryNode = new Node(Token.TRY, Node.newBlock(Token.EXPR_RESULT, Node.newNumber(1)));
        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));

        // Should handle gracefully without throwing exception
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);
        minimize.visit(traversal, tryNode, null);
    }

    @Test
    public void testTryWithFinallyAndReturn() {
        // try { return 1; } finally { return 2; }
        Node tryBlock = Node.newBlock(Token.EXPR_RESULT, Node.newNumber(1));
        Node finallyBlock = Node.newBlock(Token.RETURN, Node.newNumber(2));
        Node tryNode = new Node(Token.TRY, tryBlock, null, finallyBlock);

        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);
        
        // This exercises minimizeExits for TRY nodes, specifically checking finally blocks
        minimize.visit(traversal, tryNode, null);
    }

    @Test
    public void testLabelNodeWithBreak() {
        // label: { break label; }
        Node breakNode = new Node(Token.BREAK);
        Node labelBlock = Node.newBlock(Token.EXPR_RESULT, Node.newNumber(1), breakNode);
        Node labelNode = new Node(Token.LABEL, Node.newString("label"), labelBlock);

        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);

        minimize.visit(traversal, labelNode, null);
    }

    @Test
    public void testFunctionNodeWithReturn() {
        // function foo() { return 1; }
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        Node fnBody = Node.newBlock(returnNode);
        Node fnNode = new Node(Token.FUNCTION, Node.newString("foo"), new Node(Token.PARAM_LIST), fnBody);

        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);

        minimize.visit(traversal, fnNode, null);
    }

    @Test
    public void testSwitchStatementWithBreak() {
        // switch(x) { case 1: break; }
        Node caseNode = new Node(Token.CASE, Node.newNumber(1), Node.newBlock(new Node(Token.BREAK)));
        Node switchBody = Node.newBlock(caseNode);
        Node switchNode = new Node(Token.SWITCH, Node.newNumber(0), switchBody);

        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);

        minimize.visit(traversal, switchNode, null);
    }

    @Test
    public void testLoopNodeWithContinue() {
        // while(true) { continue; }
        Node continueNode = new Node(Token.CONTINUE);
        Node loopBody = Node.newBlock(continueNode);
        Node whileNode = new Node(Token.WHILE, Node.newTrue(), loopBody);

        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);

        minimize.visit(traversal, whileNode, null);
    }

    @Test
    public void testIfNodeWithReturn() {
        // if (true) { return 1; } else { return 2; }
        Node thenBranch = Node.newBlock(new Node(Token.RETURN, Node.newNumber(1)));
        Node elseBranch = Node.newBlock(new Node(Token.RETURN, Node.newNumber(2)));
        Node ifNode = new Node(Token.IF, Node.newTrue(), thenBranch, elseBranch);

        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);

        minimize.visit(traversal, ifNode, null);
    }

    @Test
    public void  testBlockNodeWithMultipleExits() {
        // { return 1; return 2; }
        Node block = Node.newBlock(
            new Node(Token.RETURN, Node.newNumber(1)),
            new Node(Token.RETURN, Node.newNumber(2))
        );

        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);

        minimize.visit(traversal, block, null);
    }

    @Test
    public void testTryWithCatchAndFinally() {
        // try { foo(); } catch (e) {} finally { return 1; }
        Node tryBlock = Node.newBlock(Node.newExpr(Node.newNumber(1)));
        Node catchBlock = Node.newBlock(Node.newEmpty());
        Node catchNode = new Node(Token.CATCH, Node.newString("e"), catchBlock);
        Node finallyBlock = Node.newBlock(new Node(Token.RETURN, Node.newNumber(1)));
        
        Node tryNode = new Node(Token.TRY, tryBlock, catchNode, finallyBlock);

        NodeTraversal traversal = new NodeTraversal(compiler, new MinimizeExitPoints(compiler));
        MinimizeExitPoints minimize = new MinimizeExitPoints(compiler);

        minimize.visit(traversal, tryNode, null);
    }
}