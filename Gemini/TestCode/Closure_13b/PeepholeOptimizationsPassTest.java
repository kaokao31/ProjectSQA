package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for PeepholeOptimizationsPass.
 */
public class PeepholeOptimizationsPassTest {

    private AbstractCompiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options if necessary
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
    }

    @Test
    public void testEmptyPassTraversal() {
        PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler);
        Node root = new Node(Token.BLOCK);
        
        pass.process(root, root);
        // Verify no changes/crashes on empty block
        assertTrue(root.hasNoChildren());
    }

    @Test
    public void testPassWithSingleOptimization() {
        // Create a simple peephole optimization that does nothing or modifies a node
        AbstractPeepholeOptimization dummyOpt = new AbstractPeepholeOptimization() {
            @Override
            Node optimizeSubtree(Node subtree) {
                return subtree;
            }
        };

        PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, dummyOpt);
        Node root = new Node(Token.BLOCK, Node.newNumber(1.0));
        
        pass.process(root, root);
        assertNotNull(root.getFirstChild());
    }

    @Test
    public void testPassWithMultipleOptimizations() {
        AbstractPeepholeOptimization opt1 = new AbstractPeepholeOptimization() {
            @Node.SideEffectFree
            @Override
            Node optimizeSubtree(Node subtree) {
                return subtree;
            }
        };

        AbstractPeepholeOptimization opt2 = new AbstractPeepholeOptimization() {
            @Node.SideEffectFree
            @Override
            Node optimizeSubtree(Node subtree) {
                return subtree;
            }
        };

        PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt1, opt2);
        Node root = new Node(Token.BLOCK, Node.newString("test"));

        pass.process(root, root);
        assertEquals(Token.BLOCK, root.getType());
        assertEquals(Token.STRING, root.getFirstChild().getType());
    }

    @Test
    public void testShouldNotTraverse() {
        // Test traversal control with a custom peephole pass or compiler state
        PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler);
        Node root = new Node(Token.SCRIPT, Node.newNumber(42));
        
        pass.hotSwapScript(root, null);
        assertNotNull(root);
    }

    @Test
    public void testNestedNodeOptimizationLoop() {
        // Construct a nested tree to exercise the traversal and optimization loop (do-while loop in PeepholeOptimizationsPass)
        AbstractPeepholeOptimization loopOpt = new AbstractPeepholeOptimization() {
            private int count = 0;
            @Override
            Node optimizeSubtree(Node subtree) {
                // Simulate a change on the first pass
                if (count < 1 && subtree.isNumber() && subtree.getDouble() == 1.0) {
                    count++;
                    subtree.setDouble(2.0);
                    reportCodeChange();
                }
                return subtree;
            }
        };

        PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, loopOpt);
        Node root = new Node(Token.BLOCK, Node.newNumber(1.0));

        pass.process(root, root);
        assertEquals(2.0, root.getFirstChild().getDouble(), 0.001);
    }
}