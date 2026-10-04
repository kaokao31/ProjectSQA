package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ControlFlowAnalysisTest {

    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testConstructorAndBasicProperties() {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false);
        assertNotNull(cfa);
        assertFalse(cfa.isGetterOrSetter(null));
    }

    @Test
    public void testProcessWithNullRoot() {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);
        // Processing a null root or empty AST should not throw exceptions
        Node root = null;
        cfa.process(root, root);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testSimpleControlFlow() {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);
        // Create a simple script with a single statement: var x = 1;
        Node script = new Node(Token.SCRIPT);
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
        script.addChildToBack(varNode);

        cfa.process(script, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testBranchingControlFlow() {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);
        
        // if (true) { x = 1; } else { x = 2; }
        Node script = new Node(Token.SCRIPT);
        Node ifNode = new Node(Token.IF, 
            Node.newTrue(),
            new Node(Token.BLOCK, Node.newNumber(1)),
            new Node(Token.BLOCK, Node.newNumber(2))
        );
        script.addChildToBack(ifNode);

        cfa.process(script, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testTryFinallyControlFlow() {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);

        // try { x = 1; } finally { x = 2; }
        Node script = new Node(Token.SCRIPT);
        Node tryNode = new Node(Token.TRY,
            new Node(Token.BLOCK, Node.newNumber(1)),
            new Node(Token.BLOCK), // catch block placeholder/empty
            new Node(Token.BLOCK, Node.newNumber(2))
        );
        script.addChildToBack(tryNode);

        cfa.process(script, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testLoopControlFlow() {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);

        // while (true) { break; }
        Node script = new Node(Token.SCRIPT);
        Node whileNode = new Node(Token.WHILE,
            Node.newTrue(),
            new Node(Token.BLOCK, new Node(Token.BREAK))
        );
        script.addChildToBack(whileNode);

        cfa.process(script, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testSwitchControlFlow() {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);

        // switch (x) { case 1: break; default: break; }
        Node script = new Node(Token.SCRIPT);
        Node switchNode = new Node(Token.SWITCH,
            Node.newString(Token.NAME, "x"),
            new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK, new Node(Token.BREAK))),
            new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK, new Node(Token.BREAK)))
        );
        script.addChildToBack(switchNode);

        cfa.process(script, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testFunctionAndReturn() {
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);

        // function f() { return 1; }
        Node script = new Node(Token.SCRIPT);
        Node fnNode = new Node(Token.FUNCTION,
            Node.newString(Token.NAME, "f"),
            new Node(Token.PARAM_LIST),
            new Node(Token.BLOCK, new Node(Token.RETURN, Node.newNumber(1)))
        );
        script.addChildToBack(fnNode);

        cfa.process(script, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testFinallyEdgeCaseClosure14() {
        // Closure Bug 14 often relates to finally blocks and control flow transitions (like return/break/continue inside/outside finally)
        ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, true);

        // try { return 1; } finally { foo(); }
        Node script = new Node(Token.SCRIPT);
        Node tryNode = new Node(Token.TRY,
            new Node(Token.BLOCK, new Node(Token.RETURN, Node.newNumber(1))),
            new Node(Token.BLOCK),
            new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "foo")))
        );
        script.addChildToBack(tryNode);

        cfa.process(script, script);
        assertNotNull(cfa.getCfg());
    }
}