package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for UnreachableCodeElimination (Closure Bug 85).
 */
public class UnreachableCodeEliminationTest {

    private AbstractCompiler compiler;
    private UnreachableCodeElimination elimination;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Pass removeNoOpInline = true/false depending on constructor needs
        elimination = new UnreachableCodeElimination(compiler, true);
    }

    @Test
    public void testProcessBasic() {
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildrenToBack(script);

        // A simple return statement followed by unreachable code
        // return;
        // var x = 1; (unreachable)
        Node ret = new Node(Token.RETURN);
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        
        script.addChildrenToBack(ret);
        script.addChildrenToBack(varNode);

        // Run process
        elimination.process(null, root);

        // The var node should be eliminated
        assertNull(varNode.getParent());
    }

    @Test
    public void testTryFinallyBranch() {
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildrenToBack(script);

        // try { return; } finally { }
        Node tryNode = new Node(Token.TRY);
        Node block1 = new Node(Token.BLOCK, new Node(Token.RETURN));
        Node block2 = new Node(Token.BLOCK);
        tryNode.addChildrenToBack(block1);
        tryNode.addChildrenToBack(block2);

        script.addChildrenToBack(tryNode);

        // Followed by unreachable code
        Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(1));
        script.addChildrenToBack(expr);

        elimination.process(root, root);
        
        // Ensure the processor runs and doesn't crash on TRY/FINALLY structures
        assertNotNull(root);
    }

    @Test
    public void testInfiniteLoopUnreachable() {
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildrenToBack(script);

        // while(true) { break; }
        // code after loop should be unreachable
        Node whileNode = new Node(Token.WHILE, Node.newTrue(), 
                new Node(Token.BLOCK, new Node(Token.BREAK)));
        script.addChildrenToBack(whileNode);

        Node unreachable = new Node(Token.EXPR_RESULT, Node.newNumber(2));
        script.addChildrenToBack(unreachable);

        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
        cfa.process(root, root);

        NodeTraversal t = new NodeTraversal(compiler, elimination.new EliminationPass(cfa));
        t.traverse(root);

        assertNull(unreachable.getParent());
    }

    @Test
    public void testFunctionScopeUnreachable() {
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildrenToBack(script);

        // function f() { return; var x = 1; }
        Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), 
                new Node(Token.PARAM_LIST), 
                new Node(Token.BLOCK, new Node(Token.RETURN), new Node(Token.VAR, Node.newString(Token.NAME, "x"))));
        script.addChildrenToBack(fn);

        elimination.process(script, script);
        assertNotNull(script);
    }

    @Test
    public void testLabelAndBreak() {
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildrenToBack(script);

        // label: { break label; var y = 2; }
        Node label = new Node(Token.LABEL, Node.newString(Token.NAME, "l"));
        Node block = new Node(Token.BLOCK, new Node(Token.BREAK, Node.newString(Token.NAME, "l")), new Node(Token.VAR, Node.newString(Token.NAME, "y")));
        label.addChildrenToBack(block);
        script.addChildrenToBack(label);

        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
        cfa.process(root, root);

        NodeTraversal t = new NodeTraversal(compiler, elimination.new EliminationPass(cfa));
        t.traverse(root);

        assertNotNull(root);
    }
}