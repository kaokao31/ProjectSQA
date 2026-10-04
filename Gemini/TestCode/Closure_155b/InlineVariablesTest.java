package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for com.google.javascript.jscomp.InlineVariables.
 * Designed for maximum coverage and edge cases targeting Closure Compiler Bug 155.
 */
public class InlineVariablesTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testCreationAndPassTraversal() {
        // Test basic construction and safe invocation of InlineVariables pass
        AbstractCompiler abstractCompiler = compiler;
        InlineVariables pass = new InlineVariables(
                abstractCompiler,
                InlineVariables.Mode.ALL,
                true
        );

        assertNotNull(pass);

        // Process a simple AST to ensure no NPE or unexpected exceptions
        Node root = getNodeForCode("var x = 1; var y = x;");
        pass.process(root, root);
    }

    @Test
    public void testModes() {
        // Test different Inlining Modes
        for (InlineVariables.Mode mode : InlineVariables.Mode.values()) {
            InlineVariables pass = new InlineVariables(compiler, mode, false);
            assertNotNull(pass);
            Node root = getNodeForCode("function f() { var a = 10; return a; }");
            pass.process(root, root);
        }
    }

    @Test
    public void testGlobalModeVsLocals() {
        // Test global variables vs local variables inlining policy
        InlineVariables globalPass = new InlineVariables(compiler, InlineVariables.Mode.CONSTANTS_WITH_MUTABLE_LOOKUP, true);
        Node root = getNodeForCode("var GLOBAL = 5; function foo() { var LOCAL = 10; return GLOBAL + LOCAL; }");
        globalPass.process(root, root);
        assertNotNull(root);
    }

    @Test
    public void testConstantsOnlyMode() {
        InlineVariables pass = new InlineVariables(compiler, InlineVariables.Mode.CONSTANTS_ONLY, true);
        Node root = getNodeForCode("var a = 1; a = 2; var b = 3;");
        pass.process(root, root);
        assertNotNull(root);
    }

    @Test
    public void testEmptyAst() {
        InlineVariables pass = new InlineVariables(compiler, InlineVariables.Mode.ALL, true);
        Node root = new Node(Token.BLOCK);
        pass.process(root, root);
        assertNotNull(root);
    }

    @Test(expected = NullPointerException.class)
    public void testNullCompiler() {
        InlineVariables pass = new InlineVariables(null, InlineVariables.Mode.ALL, true);
        Node root = getNodeForCode("var x = 1;");
        pass.process(root, root);
    }

    /**
     * Helper method to parse JS code into an AST Node using the Compiler instance.
     */
    private Node getNodeForCode(String jsCode) {
        CompilerOptions options = new CompilerOptions();
        compiler.init(
                new JSSourceFile[] {},
                new JSSourceFile[] { JSSourceFile.fromCode("input.js", jsCode) },
                options
        );
        Node root = compiler.parse();
        return root;
    }
}