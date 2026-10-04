package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeadAssignmentsEliminationTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options minimally so passes can run
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testCreationAndProcess() {
        DeadAssignmentsElimination dae = new DeadAssignmentsElimination(abstractCompiler);
        assertNotNull(dae);

        // Create a simple dummy AST: root -> script -> return 1;
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        Node returnNode = new Node(Token.RETURN, Node.newNumber(1));
        script.addChildrenToBack(returnNode);
        root.addChildrenToBack(script);

        // Process should not throw exceptions on a basic tree
        dae.process(root, script);
    }

    @Test
    public void testProcessWithEmptyNode() {
        DeadAssignmentsElimination dae = new DeadAssignmentsElimination(abstractCompiler);
        Node emptyNode = new Node(Token.EMPTY);
        dae.process(emptyNode, emptyNode);
    }

    @Test
    public void testVariableAssignmentAndRead() {
        // var x = 1; x = 2; foo(x);
        // The first assignment to x is dead.
        Node script = new Node(Token.SCRIPT);

        // var x = 1;
        Node name1 = Node.newString(Token.NAME, "x");
        name1.addChildToFront(Node.newNumber(1));
        Node varNode = new Node(Token.VAR, name1);
        script.addChildToBack(varNode);

        // x = 2;
        Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(2));
        Node exprAssign = new Node(Token.EXPR_RESULT, assign);
        script.addChildToBack(exprAssign);

        // foo(x)
        Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"), Node.newString(Token.NAME, "x"));
        Node exprCall = new Node(Token.EXPR_RESULT, call);
        script.addChildToBack(exprCall);

        DeadAssignmentsElimination dae = new DeadAssignmentsElimination(abstractCompiler);
        dae.process(script, script);
    }

    @Test
    public void testSimpleFunctionScope() {
        // function f() { var x = 1; x = 5; return x; }
        Node script = new Node(Token.SCRIPT);
        Node fnName = Node.newString(Token.NAME, "f");
        Node lp = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);

        Node varX = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        body.addChildToBack(varX);

        Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(5));
        body.addChildToBack(new Node(Token.EXPR_RESULT, assign));

        Node ret = new Node(Token.RETURN, Node.newString(Token.NAME, "x"));
        body.addChildToBack(ret);

        Node fn = new Node(Token.FUNCTION, fnName, lp, body);
        script.addChildToBack(fn);

        DeadAssignmentsElimination dae = new DeadAssignmentsElimination(abstractCompiler);
        dae.process(script, script);
    }
}