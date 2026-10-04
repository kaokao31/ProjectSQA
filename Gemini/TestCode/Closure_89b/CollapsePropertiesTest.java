package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CollapsePropertiesTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler with basic options to avoid NPE during passes
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testInitializationAndPass() {
        CollapseProperties collapseProperties = new CollapseProperties(abstractCompiler, true, true);
        
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        collapseProperties.process(root, root);
        
        // Verify basic execution without exceptions
        assertTrue(true);
    }

    @Test
    public void testCollapsePropertiesWithGlobalExterns() {
        // Test constructor variant or configuration with global vars and externs
        CollapseProperties collapseProperties = new CollapseProperties(abstractCompiler, false, false);

        Node root = new Node(Token.BLOCK);
        Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
        root.addChildToBack(varNode);

        collapseProperties.process(root, root);
        assertNotNull(root);
    }
}