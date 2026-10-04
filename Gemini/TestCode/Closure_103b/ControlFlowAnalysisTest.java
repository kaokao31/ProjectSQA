package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ControlFlowAnalysisTest {

    private AbstractCompiler compiler;
    private boolean computeCfg;
    private ControlFlowAnalysis cfa;

    @Before
    public void setUp() {
        compiler = new Compiler();
        computeCfg = true;
    }

    @Test
    public void testSimpleFunctionTraversal() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node name = Node.newString(Token.NAME, "f");
        Node fn = new Node(Token.FUNCTION, name, new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        script.addChildToBack(fn);

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testIfElseStructure() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node cond = new Node(Token.TRUE);
        Node thenBranch = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, "1")));
        Node elseBranch = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, "2")));
        Node ifNode = new Node(Token.IF, cond, thenBranch, elseBranch);
        script.addChildToBack(ifNode);

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testTryCatchFinally() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node tryBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, "1")));
        Node catchName = Node.newString(Token.NAME, "e");
        Node catchBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, "2")));
        Node catchNode = new Node(Token.CATCH, catchName, catchBody);
        Node catchBlock = new Node(Token.BLOCK, catchNode);
        Node finallyBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, "3")));

        Node tryNode = new Node(Token.TRY, tryBody, catchBlock, finallyBody);
        script.addChildToBack(tryNode);

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testSwitchCaseDefault() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node switchCond = new Node(Token.NUMBER, "1");
        Node case1 = new Node(Token.CASE, new Node(Token.NUMBER, "1"), new Node(Token.BLOCK));
        Node defaultCase = new Node(Token.DEFAULT_KEY, new Node(Token.BLOCK));
        Node switchBody = new Node(Token.BLOCK, case1, defaultCase);
        Node switchNode = new Node(Token.SWITCH, switchCond, switchBody);
        script.addChildToBack(switchNode);

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testLoopsBreakContinue() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node cond = new Node(Token.TRUE);
        Node body = new Node(Token.BLOCK, new Node(Token.BREAK), new Node(Token.CONTINUE));
        Node whileNode = new Node(Token.WHILE, cond, body);
        script.addChildToBack(whileNode);

        Node forCond = new Node(Token.TRUE);
        Node forBody = new Node(Token.BLOCK);
        Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), forCond, new Node(Token.EMPTY), forBody);
        script.addChildToBack(forNode);

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testDoWhileAndLabels() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node doBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, "1")));
        Node doCond = new Node(Token.FALSE);
        Node doNode = new Node(Token.DO, doBody, doCond);

        Node labelName = Node.newString(Token.LABEL_NAME, "myLabel");
        Node labelNode = new Node(Token.LABEL, labelName, doNode);
        script.addChildToBack(labelNode);

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testReturnAndThrow() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node name = Node.newString(Token.NAME, "f");
        Node retNode = new Node(Token.RETURN, new Node(Token.NUMBER, "0"));
        Node throwNode = new Node(Token.THROW, new Node(Token.STRING, "error"));
        Node fnBody = new Node(Token.BLOCK, retNode, throwNode);
        Node fn = new Node(Token.FUNCTION, name, new Node(Token.PARAM_LIST), fnBody);
        script.addChildToBack(fn);

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testHookNode() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node cond = new Node(Token.TRUE);
        Node left = new Node(Token.NUMBER, "1");
        Node right = new Node(Token.NUMBER, "2");
        Node hook = new Node(Token.HOOK, cond, left, right);
        script.addChildToBack(new Node(Token.EXPR_RESULT, hook));

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testLogicalAndOr() {
        cfa = new ControlFlowAnalysis(compiler, computeCfg, true);
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
        Node orNode = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
        script.addChildToBack(new Node(Token.EXPR_RESULT, andNode));
        script.addChildToBack(new Node(Token.EXPR_RESULT, orNode));

        cfa.process(root, script);
        assertNotNull(cfa.getCfg());
    }

    @Test
    public void testIsStraddling() {
        Node root = new Node(Token.BLOCK);
        assertTrue(ControlFlowAnalysis.isStraddling(root, root));
    }
}