package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class NodeTraversalTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node parse(String code) {
    Node root = compiler.parseSyntheticCode("test", code);
    assertNotNull("Failed to parse: " + code, root);
    return root;
  }

  @Test
  public void testTraversesAllNodesInPostOrder() {
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });

    traversal.traverse(parse("var x = 1;"));

    assertEquals(4, visited.size());
    assertEquals(Token.NUMBER, visited.get(0).getType());
    assertEquals(Token.NAME, visited.get(1).getType());
    assertEquals(Token.VAR, visited.get(2).getType());
    assertEquals(Token.SCRIPT, visited.get(3).getType());
  }

  @Test
  public void testTraverseEmptyScript() {
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });

    traversal.traverse(parse(""));

    assertEquals(1, visited.size());
    assertEquals(Token.SCRIPT, visited.get(0).getType());
  }

  @Test
  public void testShouldTraverseSkipsChildren() {
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.Callback() {
          @Override
          public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
            return n.getType() != Token.VAR;
          }

          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });

    traversal.traverse(parse("var x = 1;"));

    assertEquals(2, visited.size());
    assertEquals(Token.VAR, visited.get(0).getType());
    assertEquals(Token.SCRIPT, visited.get(1).getType());
  }

  @Test
  public void testRootShouldTraverseFalseStillVisitsRoot() {
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.Callback() {
          @Override
          public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
            return parent != null;
          }

          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });

    traversal.traverse(parse("var x = 1;"));

    assertEquals(1, visited.size());
    assertEquals(Token.SCRIPT, visited.get(0).getType());
  }

  @Test
  public void testShouldTraverseFalseOnFunctionSkipsBody() {
    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.Callback() {
          @Override
          public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {
            return n.getType() != Token.FUNCTION;
          }

          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visited.add(n);
          }
        });

    traversal.traverse(parse("function f() { var x; }"));

    assertEquals(2, visited.size());
    assertEquals(Token.FUNCTION, visited.get(0).getType());
    assertEquals(Token.SCRIPT, visited.get(1).getType());
  }

  @Test
  public void testCurrentNodeAndParentDuringVisit() {
    final List<Node> currentNodes = new ArrayList<Node>();
    final List<Node> parents = new ArrayList<Node>();
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            assertSame("getCurrentNode() must match visited node", n, t.getCurrentNode());
            currentNodes.add(n);
            parents.add(parent);
          }
        });

    traversal.traverse(parse("var x = 1;"));

    assertEquals(4, currentNodes.size());
    assertEquals(4, parents.size());

    assertNull(parents.get(3));
    assertSame(currentNodes.get(1), parents.get(0));
    assertSame(currentNodes.get(2), parents.get(1));
    assertSame(currentNodes.get(3), parents.get(2));
  }

  @Test
  public void testScopeCallbacksAreBalanced() {
    final int[] enterCount = {0};
    final int[] exitCount = {0};
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.AbstractScopedCallback() {
          @Override
          public void enterScope(NodeTraversal t) {
            enterCount[0]++;
          }

          @Override
          public void exitScope(NodeTraversal t) {
            exitCount[0]++;
          }

          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
          }
        });

    traversal.traverse(parse("function f() { var x; }"));

    assertEquals(2, enterCount[0]);
    assertEquals(2, exitCount[0]);
  }

  @Test
  public void testNestedScopeCallbacksAreBalanced() {
    final int[] enterCount = {0};
    final int[] exitCount = {0};
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.AbstractScopedCallback() {
          @Override
          public void enterScope(NodeTraversal t) {
            enterCount[0]++;
          }

          @Override
          public void exitScope(NodeTraversal t) {
            exitCount[0]++;
          }

          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
          }
        });

    traversal.traverse(parse("function f() { function g() { var x; } }"));

    assertEquals(3, enterCount[0]);
    assertEquals(3, exitCount[0]);
  }

  @Test
  public void testGetScopeRootInsideFunction() {
    final Node[] scopeRoot = new Node[1];
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            if (n.getType() == Token.VAR) {
              scopeRoot[0] = t.getScope().getRootNode();
            }
          }
        });

    traversal.traverse(parse("function f() { var x; }"));

    assertNotNull(scopeRoot[0]);
    assertEquals(Token.FUNCTION, scopeRoot[0].getType());
  }

  @Test
  public void testGetScopeRootAtTopLevel() {
    final Node[] scopeRoot = new Node[1];
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            if (n.getType() == Token.VAR) {
              scopeRoot[0] = t.getScope().getRootNode();
            }
          }
        });

    traversal.traverse(parse("var x;"));

    assertNotNull(scopeRoot[0]);
    assertEquals(Token.SCRIPT, scopeRoot[0].getType());
  }

  @Test
  public void testTraverseMultipleStatements() {
    final int[] scriptVisits = {0};
    NodeTraversal traversal = new NodeTraversal(compiler,
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            if (n.getType() == Token.SCRIPT) {
              scriptVisits[0]++;
            }
          }
        });

    traversal.traverse(parse("var x; var y; function f() {}"));

    assertEquals(1, scriptVisits[0]);
  }
}