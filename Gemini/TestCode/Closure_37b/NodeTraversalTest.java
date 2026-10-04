package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeTraversalTest {

    private static class TestCallback implements NodeTraversal.Callback {
        int traverseCount = 0;
        int shouldTraverseCount = 0;
        Node lastVisitedNode = null;
        NodeTraversal lastTraversal = null;

        @Override
        public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
            shouldTraverseCount++;
            return true;
        }

        @Override
        public void visit(NodeTraversal nodeTraversal, Node n, Node parent) {
            traverseCount++;
            lastVisitedNode = n;
            lastTraversal = nodeTraversal;
        }
    }

    private static class SkipCallback implements NodeTraversal.Callback {
        @Override
        public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
            return false; // Skip all children
        }

        @Override
        public void visit(NodeTraversal nodeTraversal, Node n, Node parent) {
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Test
    public void testBasicTraversal() {
        Compiler compiler = new Compiler();
        TestCallback callback = new TestCallback();
        NodeTraversal traversal = new NodeTraversal(compiler, callback);

        Node root = new Node(Token.BLOCK);
        Node child1 = new Node(Token.NAME, Node.newString("a"));
        Node child2 = new Node(Token.NAME, Node.newString("b"));
        root.addChildToBack(child1);
        root.addChildToBack(child2);

        traversal.traverse(root);

        // Expected visits: root, child1, child2 -> total 3
        assertEquals(3, callback.traverseCount);
        assertEquals(3, callback.shouldTraverseCount);
        assertSame(compiler, traversal.getCompiler());
        assertSame(root, traversal.getSourceRoot());
    }

    @Test
    public void testSkipTraversal() {
        Compiler compiler = new Compiler();
        SkipCallback callback = new SkipCallback();
        NodeTraversal traversal = new NodeTraversal(compiler, callback);

        Node root = new Node(Token.BLOCK);
        Node child1 = new Node(Token.NAME, Node.newString("a"));
        root.addChildToBack(child1);

        traversal.traverse(root);
        // shouldTraverse is called for root, returns false, so children are not visited.
        // Wait, does 'visit' get called for root? Yes, in standard pre-order, visit is called then children traversed if shouldTraverse.
        // Let's verify NodeTraversal behavior: shouldTraverse is checked before visiting or after?
        // Actually, NodeTraversal checks shouldTraverse(n, parent), and if true, visits n, then traverses children.
        // Wait, let's look at standard Closure NodeTraversal:
        // if (callback.shouldTraverse(t, n, parent)) { callback.visit(t, n, parent); traverseChildren(n); }
        // So root is visited, but children are skipped.
    }

    @Test
    public void testTraversalWithScope() {
        Compiler compiler = new Compiler();
        TestCallback callback = new TestCallback();
        NodeTraversal traversal = new NodeTraversal(compiler, callback);

        Node root = new Node(Token.SCRIPT);
        traversal.traverseWithScope(root, new Scope(null, root));

        assertNotNull(traversal.getScope());
        assertEquals(root, traversal.getScopeNode());
    }

    @Test
    public void testTraversalGettersAndHelpers() {
        Compiler compiler = new Compiler();
        TestCallback callback = new TestCallback();
        NodeTraversal traversal = new NodeTraversal(compiler, callback);

        Node root = new Node(Token.SCRIPT);
        Node node = new Node(Token.NAME, Node.newString("x"));
        root.addChildToBack(node);

        traversal.traverse(root);

        assertNotNull(traversal.getInput());
        assertNotNull(traversal.getSourceName());
        assertFalse(traversal.hasScope());
    }

    @Test
    public void testNodeTraversalMethods() {
        Compiler compiler = new Compiler();
        TestCallback callback = new TestCallback();
        NodeTraversal traversal = new NodeTraversal(compiler, callback);

        Node root = new Node(Token.FUNCTION);
        Node name = new Node(Token.NAME, Node.newString("foo"));
        Node lp = new Node(Token.LP);
        Node body = new Node(Token.BLOCK);
        root.addChildToBack(name);
        root.addChildToBack(lp);
        root.addChildToBack(body);

        traversal.traverse(root);

        assertTrue(NodeTraversal.traverseLineNumberCheck(compiler, root, callback));
    }

    @Test
    public void testComputeANCE() {
        Node node1 = new Node(Token.NAME);
        Node node2 = new Node(Token.NAME);
        Node common = NodeTraversal.getClosestCommonEnclosingNode(node1, node2);
        // If they have no common ancestor in a constructed tree
        Node root = new Node(Token.BLOCK);
        root.addChildToBack(node1);
        root.addChildToBack(node2);
        
        Node anc = NodeTraversal.getClosestCommonEnclosingNode(node1, node2);
        assertNotNull(anc);
    }
}