package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InlineObjectLiteralsTest {

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
    public void testCreationAndPassNullNode() {
        InlineObjectLiterals pass = new InlineObjectLiterals(
                abstractCompiler,
                CompilerPass.referenceRemovalPolicy.SAFE_KEEP_DOMINATED_RESULTS
        );

        // Process with a null or empty root node to ensure it doesn't throw NullPointerException
        pass.process(null, null);
    }

    @Test
    public void testProcessWithBasicNode() {
        InlineObjectLiterals pass = new InlineObjectLiterals(
                abstractCompiler,
                CompilerPass.referenceRemovalPolicy.EXPENSIVE_RETRIEVAL
        );

        // Create a dummy AST structure: script -> block -> var -> object literal
        Node root = new Node(Token.SCRIPT);
        Node block = new Node(Token.BLOCK);
        root.addChildToBack(block);

        // var x = {};
        Node name = Node.newString(Token.NAME, "x");
        Node objLit = new Node(Token.OBJECTLIT);
        name.addChildToBack(objLit);
        Node varNode = new Node(Token.VAR, name);
        block.addChildToBack(varNode);

        pass.process(root, root);
        assertNotNull(root);
    }
}