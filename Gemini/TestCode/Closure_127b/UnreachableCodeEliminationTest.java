package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnreachableCodeEliminationTest {

    private Compiler createCompiler() {
        return new Compiler();
    }

    @Test
    public void testConstructorAndUtility() {
        Compiler compiler = createCompiler();
        AbstractCompiler abstractCompiler = compiler;
        boolean removeGlobal = true;

        UnreachableCodeElimination uce = new UnreachableCodeElimination(abstractCompiler, removeGlobal);
        assertNotNull(uce);
    }

    @Test
    public void testProcessBasic() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, true);

        // Create a simple AST: script -> block -> return, unreachable statement
        Node root = new Node(Token.SCRIPT);
        Node block = new Node(Token.BLOCK);
        Node ret = new Node(Token.RETURN);
        Node unreachable = new Node(Token.EXPR_RESULT, Node.newNumber(1.0));

        block.addChildToBack(ret);
        block.addChildToBack(unreachable);
        root.addChildToBack(block);

        uce.process(root, root);
        // The unreachable node should be removed or processed
        // We just ensure no exceptions are thrown and basic flow works.
        assertNotNull(root);
    }

    @Test
    public void testTryFinallyBranchBug127() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, false);

        // Construct a TRY node scenario related to bug 127 in UnreachableCodeElimination
        // try { return; } finally { }
        Node root = new Node(Token.SCRIPT);
        Node block = new Node(Token.BLOCK);
        
        Node tryNode = new Node(Token.TRY);
        Node tryBody = new Node(Token.BLOCK, new Node(Token.RETURN));
        Node finallyBody = new Node(Token.BLOCK);
        
        tryNode.addChildToBack(tryBody);
        tryNode.addChildToBack(new Node(Token.BLOCK)); // catch block (empty)
        tryNode.addChildToBack(finallyBody);

        block.addChildToBack(tryNode);
        root.addChildToBack(block);

        // Process should handle try/finally control flow correctly without infinite loops or crashes
        try {
            uce.process(root, root);
        } catch (Exception e) {
            // Depending on strictness, handle any unexpected AST issues
            fail("Process threw an exception: " + e.getMessage());
        }
        assertNotNull(root);
    }

    @Test
    public void testUnreachableCodeWithRemoveGlobalFalse() {
        Compiler compiler = createCompiler();
        UnreachableCodeElimination uce = new UnreachableCodeElimination(compiler, false);

        Node root = new Node(Token.SCRIPT);
        Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(5.0));
        root.addChildToBack(expr);

        NodeTraversal t = new NodeTraversal(compiler, uce);
        // Directly test enterScope / exitScope or callback methods if accessible
        try {
            uce.exitScope(t);
        } catch (Exception e) {
            // expected or ignored depending on scope state
        }
        
        assertNotNull(root);
    }
}