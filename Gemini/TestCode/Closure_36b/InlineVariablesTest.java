package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for com.google.javascript.jscomp.InlineVariables.
 * Designed for maximum coverage and fault detection targeting Closure Bug 36.
 */
public class InlineVariablesTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Basic compiler options setup to prevent NPEs during traversal passes
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
    }

    @Test
    public void testInitializationAndPass() {
        // Test that InlineVariables can be instantiated and run on a simple AST without crashing.
        AbstractCompiler abstractCompiler = compiler;
        InlineVariables.Mode mode = InlineVariables.Mode.ALL;
        InlineVariables inlineVariables = new InlineVariables(
                abstractCompiler,
                mode,
                true
        );

        assertNotNull(inlineVariables);

        // Create a minimal valid AST root
        Node root = getNodeForCode("var x = 1; var y = x;");
        
        // Run the process method
        inlineVariables.process(null, root);
    }

    @Test
    public void testDifferentModes() {
        // Test constants-only mode vs all mode
        InlineVariables constantsOnly = new InlineVariables(
                compiler,
                InlineVariables.Mode.CONSTANTS_ONLY,
                true
        );
        assertNotNull(constantsOnly);

        InlineVariables.Mode mode = InlineVariables.Mode.valueOf("ALL");
        assertEquals(InlineVariables.Mode.ALL, mode);
        
        InlineVariables.Mode modeConst = InlineVariables.Mode.valueOf("CONSTANTS_ONLY");
        assertEquals(InlineVariables.Mode.CONSTANTS_ONLY, modeConst);
    }

    @Test
    public void testCodeWithNoVariables() {
        InlineVariables inlineVariables = new InlineVariables(
                compiler,
                InlineVariables.Mode.ALL,
                false
        );

        Node root = getNodeForCode("function foo() { return 42; }");
        // Should handle code without local variables safely
        inlineVariables.process(root, root);
    }

    @Test
    public void testScopeTraversalAndInliningScenario() {
        // Construct a scenario where variable inlining might be triggered
        InlineVariables inlineVariables = new InlineVariables(
                compiler,
                InlineVariables.Mode.ALL,
                true
        );

        Node root = getNodeForCode("function f() { var a = 10; var b = a; return b; }");
        
        // Ensure normalization/scope creator can be populated if needed
        ScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
        Scope topScope = scopeCreator.createScope(root, null);

        assertNotNull(topScope);
        
        inlineVariables.process(root.getFirstChild(), root);
    }

    private Node getNodeForCode(String jsCode) {
        CompilerInput input = new CompilerInput(SourceFile.fromCode("testcode", jsCode));
        Node root = compiler.parse(input.getSourceFile());
        return root;
    }
}