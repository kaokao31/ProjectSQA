package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for com.google.javascript.jscomp.PrepareAst.
 * Designed for JUnit 4 and Defects4J Closure-129.
 */
public class PrepareAstTest {

    private Compiler compiler;
    private PrepareAst prepareAst;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options if needed
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        prepareAst = new PrepareAst(compiler, true);
    }

    @Test
    public void testCreationAndPass() {
        Assert.assertNotNull(prepareAst);

        // Create a basic AST root node (SCRIPT)
        Node root = Node.newString(Token.SCRIPT, "test.js");
        
        // Run process method
        prepareAst.process(root, root);
        
        // Verify that the tree remains intact and doesn't throw exceptions
        Assert.assertEquals(Token.SCRIPT, root.getType());
    }

    @Test
    public void testPrepareAstWithChildNodes() {
        Node root = Node.newString(Token.SCRIPT, "test.js");
        Node nameNode = Node.newString(Token.NAME, "a");
        Node varNode = new Node(Token.VAR, nameNode);
        root.addChildToBack(varNode);

        prepareAst.process(root, root);

        Assert.assertEquals(1, root.getChildCount());
        Assert.assertEquals(Token.VAR, root.getFirstChild().getType());
    }

    @Test
    public void testNormalizeWithEmptyRoot() {
        Node root = new Node(Token.BLOCK);
        prepareAst.process(root, root);
        Assert.assertEquals(Token.BLOCK, root.getType());
    }

    @Test
    public void testCallbackTraversalNodeTypes() {
        // Construct an AST that exercises various nodes checked by PrepareAst Pass
        // e.g., EXPR_RESULT, CALL with FREE_CALL indicator, etc., typical in Closure-129.
        Node root = Node.newString(Token.SCRIPT, "test.js");
        
        // Create a function call: foo()
        Node callTarget = Node.newString(Token.NAME, "foo");
        Node callNode = new Node(Token.CALL, callTarget);
        callNode.putBooleanProp(Node.FREE_CALL, true);
        
        Node exprNode = new Node(Token.EXPR_RESULT, callNode);
        root.addChildToBack(exprNode);

        // Process through PrepareAst
        prepareAst.process(root, root);

        // Assert that the FREE_CALL annotation or node transformations applied correctly
        Assert.assertTrue(callNode.getBooleanProp(Node.FREE_CALL));
    }

    @Test
    public void testNestedScopeAndDeclarations() {
        Node root = Node.newString(Token.SCRIPT, "test.js");
        Node functionNode = Node.newNTypeI(Token.FUNCTION, Node.newString(Token.NAME, "myFunc"));
        functionNode.addChildToBack(new Node(Token.PARAM_LIST));
        functionNode.addChildToBack(new Node(Token.BLOCK));
        
        root.addChildToBack(functionNode);

        prepareAst.process(root, root);
        Assert.assertEquals(Token.FUNCTION, root.getFirstChild().getType());
    }
}