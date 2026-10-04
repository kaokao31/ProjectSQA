package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FlowSensitiveInlineVariablesTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testInitialization() {
        AbstractCompiler abstractCompiler = new Compiler();
        FlowSensitiveInlineVariables fiv = new FlowSensitiveInlineVariables(abstractCompiler);
        assertNotNull(fiv);
    }

    @Test
    public void testProcessWithoutNode() {
        FlowSensitiveInlineVariables fiv = new FlowSensitiveInlineVariables(compiler);
        // Passing null or empty roots to check robustness against null pointer/empty structures
        fiv.process(null, null);
    }

    @Test
    public void testProcessWithEmptyNode() {
        FlowSensitiveInlineVariables fiv = new FlowSensitiveInlineVariables(compiler);
        Node root = new Node(Token.BLOCK);
        fiv.process(root, root);
        assertTrue(compiler.getErrors().isEmpty());
    }

    @Test
    public void testGatherCandidatesSimpleAssign() {
        // Construct a simple AST: 
        // {
        //   var x = 1;
        //   use(x);
        // }
        Node block = new Node(Token.BLOCK);
        
        Node varNode = new Node(Token.VAR);
        Node nameNode = new Node(Token.NAME, Node.newString("x"));
        nameNode.addChildToBack(Node.newNumber(1));
        varNode.addChildToBack(nameNode);
        block.addChildToBack(varNode);

        Node exprNode = new Node(Token.EXPR_RESULT);
        Node callNode = new Node(Token.CALL);
        callNode.addChildToBack(Node.newString("use"));
        callNode.addChildToBack(Node.newString("x").cloneTree());
        exprNode.addChildToBack(callNode);
        block.addChildToBack(exprNode);

        FlowSensitiveInlineVariables fiv = new FlowSensitiveInlineVariables(compiler);
        fiv.process(block, block);
        
        // Ensure no exception is thrown and process completes successfully
        assertNotNull(block);
    }

    @Test
    public void testWithLabelAndBreak() {
        // Testing complex control flow structures which FlowSensitiveInlineVariables analyzes
        // label: { break label; }
        Node block = new Node(Token.BLOCK);
        Node labelNode = new Node(Token.LABEL);
        Node labelName = new Node(Token.LABEL_NAME, Node.newString("l"));
        labelNode.addChildToBack(labelName);
        
        Node innerBlock = new Node(Token.BLOCK);
        Node breakNode = new Node(Token.BREAK, Node.newString("l"));
        innerBlock.addChildToBack(breakNode);
        labelNode.addChildToBack(innerBlock);
        
        block.addChildToBack(labelNode);

        FlowSensitiveInlineVariables fiv = new FlowSensitiveInlineVariables(compiler);
        fiv.process(block, block);
        assertNotNull(block);
    }

    @Test
    public void testWithForLoop() {
        // for (var i = 0; i < 10; i++) { foo(i); }
        Node block = new Node(Token.BLOCK);
        Node forNode = new Node(Token.FOR);
        
        Node init = new Node(Token.VAR);
        Node name = new Node(Token.NAME, Node.newString("i"));
        name.addChildToBack(Node.newNumber(0));
        init.addChildToBack(name);
        
        Node cond = new Node(Token.LT, Node.newString("i").cloneTree(), Node.newNumber(10));
        Node incr = new Node(Token.INC, Node.newString("i").cloneTree());
        
        Node body = new Node(Token.BLOCK);
        Node expr = new Node(Token.EXPR_RESULT, new Node(Token.CALL, Node.newString("foo"), Node.newString("i").cloneTree()));
        body.addChildToBack(expr);

        forNode.addChildToBack(init);
        forNode.addChildToBack(cond);
        forNode.addChildToBack(incr);
        forNode.addChildToBack(body);
        
        block.addChildToBack(forNode);

        FlowSensitiveInlineVariables fiv = new FlowSensitiveInlineVariables(compiler);
        fiv.process(block, block);
        assertNotNull(forNode);
    }
}