package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.PeepholeOptimizationsPass;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test suite for PeepholeOptimizationsPass.
 * Designed to achieve high coverage and detect potential faults,
 * including the known bug in Defects4J Closure bug 13.
 */
public class PeepholeOptimizationsPassTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Enable all peephole optimizations to maximize coverage
        options.setCodingConvention(new GoogleCodingConvention());
        options.setIdeLevel(true);
    }

    // Helper to compile source and run the pass
    private Node compileAndRunPass(String source, List<AbstractPeepholeOptimization> optimizations) {
        compiler.init(
                new ArrayList<SourceFile>(),
                new ArrayList<SourceFile>(),
                options);
        Node root = compiler.parse(SourceFile.fromCode("test", source));
        assertNotNull("Parsing failed", root);
        PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, optimizations);
        pass.process(null, root);
        return root;
    }

    // Test empty input
    @Test
    public void testEmptyInput() {
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new PeepholeFoldConstants());
        Node root = compileAndRunPass("", optimizations);
        assertNotNull(root);
        assertEquals("EMPTY", root.getToken().toString());
    }

    // Test simple constant folding
    @Test
    public void testConstantFolding() {
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new PeepholeFoldConstants());
        Node root = compileAndRunPass("var a = 1 + 2;", optimizations);
        // After folding, the addition should be replaced by 3
        String code = compiler.toSource();
        assertTrue("Expected constant folding", code.contains("3"));
    }

    // Test dead code elimination with if(true)
    @Test
    public void testDeadCodeElimination() {
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new PeepholeFoldConstants());
        optimizations.add(new PeepholeRemoveDeadCode());
        Node root = compileAndRunPass("if(true){ var x = 1; } else { var y = 2; }", optimizations);
        String code = compiler.toSource();
        assertFalse("Dead code should be removed", code.contains("y"));
        assertTrue("Live code should remain", code.contains("x"));
    }

    // Test that the pass does not infinite loop when optimizations keep modifying
    @Test(timeout = 2000)
    public void testNoInfiniteLoopOnRepeatedModification() {
        // Create a custom optimization that always replaces a simple expression with itself
        // This simulates a scenario that could cause infinite loop if not handled
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new AbstractPeepholeOptimization() {
            @Override
            public Node optimizeSubtree(Node subtree) {
                // If subtree is a number literal, replace it with the same number
                if (subtree.isNumber()) {
                    Node replacement = Node.newNumber(subtree.getDouble());
                    subtree.getParent().replaceChild(subtree, replacement);
                    return replacement;
                }
                return subtree;
            }
        });
        // Compile a simple script with a number literal
        Node root = compileAndRunPass("var a = 42;", optimizations);
        // The pass should complete without timeout
        assertNotNull(root);
    }

    // Test that the pass correctly re-traverses after modifications (bug 13 scenario)
    @Test
    public void testReTraversalAfterModification() {
        // Create an optimization that modifies a node and expects re-traversal
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new AbstractPeepholeOptimization() {
            private boolean firstPass = true;

            @Override
            public Node optimizeSubtree(Node subtree) {
                // On first encounter of a STRING node, replace it with a different string
                if (subtree.isString() && firstPass) {
                    firstPass = false;
                    Node replacement = Node.newString("modified");
                    subtree.getParent().replaceChild(subtree, replacement);
                    return replacement;
                }
                return subtree;
            }
        });
        // Add a second optimization that only works on the modified string
        optimizations.add(new AbstractPeepholeOptimization() {
            @Override
            public Node optimizeSubtree(Node subtree) {
                if (subtree.isString() && "modified".equals(subtree.getString())) {
                    // This should be reached if re-traversal happens
                    Node replacement = Node.newString("final");
                    subtree.getParent().replaceChild(subtree, replacement);
                    return replacement;
                }
                return subtree;
            }
        });
        Node root = compileAndRunPass("var a = 'original';", optimizations);
        String code = compiler.toSource();
        // The final string should be "final" if re-traversal worked
        assertTrue("Expected re-traversal to apply second optimization", code.contains("'final'"));
    }

    // Test that null compiler throws exception
    @Test(expected = NullPointerException.class)
    public void testNullCompiler() {
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        new PeepholeOptimizationsPass(null, optimizations);
    }

    // Test that null optimizations list throws exception
    @Test(expected = NullPointerException.class)
    public void testNullOptimizations() {
        new PeepholeOptimizationsPass(compiler, null);
    }

    // Test with multiple optimizations including fold and remove dead code
    @Test
    public void testMultipleOptimizations() {
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new PeepholeFoldConstants());
        optimizations.add(new PeepholeRemoveDeadCode());
        optimizations.add(new PeepholeMinimizeConditions());
        Node root = compileAndRunPass(
                "function f() { if(false) { return 1; } return 2; }",
                optimizations);
        String code = compiler.toSource();
        // Dead branch should be removed, condition minimized
        assertFalse("Dead branch should be removed", code.contains("return 1"));
        assertTrue("Live code should remain", code.contains("return 2"));
    }

    // Test that the pass handles large AST without stack overflow
    @Test(timeout = 5000)
    public void testLargeAST() {
        StringBuilder sb = new StringBuilder();
        sb.append("var a = 0;");
        for (int i = 0; i < 1000; i++) {
            sb.append("a = a + 1;");
        }
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new PeepholeFoldConstants());
        Node root = compileAndRunPass(sb.toString(), optimizations);
        assertNotNull(root);
    }

    // Test that the pass correctly handles nested control structures
    @Test
    public void testNestedControlStructures() {
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new PeepholeFoldConstants());
        optimizations.add(new PeepholeRemoveDeadCode());
        Node root = compileAndRunPass(
                "if (true) { if (false) { var x = 1; } else { var y = 2; } }",
                optimizations);
        String code = compiler.toSource();
        assertFalse("Dead code should be removed", code.contains("x"));
        assertTrue("Live code should remain", code.contains("y"));
    }

    // Test that the pass does not modify the AST when no optimizations apply
    @Test
    public void testNoOptimizationApplied() {
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        // Empty list of optimizations
        Node root = compileAndRunPass("var a = 1;", optimizations);
        String code = compiler.toSource();
        assertTrue("Code should remain unchanged", code.contains("var a = 1"));
    }

    // Test that the pass handles syntax errors gracefully
    @Test
    public void testSyntaxError() {
        List<AbstractPeepholeOptimization> optimizations = new ArrayList<>();
        optimizations.add(new PeepholeFoldConstants());
        compiler.init(
                new ArrayList<SourceFile>(),
                new ArrayList<SourceFile>(),
                options);
        Node root = compiler.parse(SourceFile.fromCode("test", "var a = ;"));
        // Even with syntax error, the pass should not throw
        PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, optimizations);
        try {
            pass.process(null, root);
        } catch (Exception e) {
            fail("Pass should not throw on syntax error: " + e.getMessage());
        }
    }
}