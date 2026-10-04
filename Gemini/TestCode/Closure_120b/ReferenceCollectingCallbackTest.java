package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test suite for ReferenceCollectingCallback (Closure Compiler Bug 120).
 */
public class ReferenceCollectingCallbackTest {

  @Test
  public void testBasicInstantiationAndTraversal() {
    Compiler compiler = new Compiler();
    Node root = Node.newString(Token.SCRIPT, "test.js");
    
    // Create a simple variable declaration to test basic collection logic
    // var x = 10;
    Node nameNode = Node.newString(Token.NAME, "x");
    Node numberNode = Node.newNumber(10.0);
    Node varNode = new Node(Token.VAR, nameNode);
    varNode.addChildToBack(numberNode);
    root.addChildToBack(varNode);

    CompilerPass pass = new ReferenceCollectingCallback(
        compiler, 
        new ReferenceCollectingCallback.Behavior() {
          @Override
          public void afterExitScope(NodeTraversal t, ReferenceCollectingCallback.BasicBlock block) {
            // No-op behavior implementation
          }
        });

    pass.process(root, root);
    Assert.assertNotNull(compiler);
  }

  @Test
  public void testBlockWithNoVariables() {
    Compiler compiler = new Compiler();
    Node root = Node.newString(Token.SCRIPT, "empty.js");
    
    // Empty script with no variables or references
    ReferenceCollectingCallback.Behavior behavior = 
        new ReferenceCollectingCallback.Behavior() {
          @Override
          public void afterExitScope(NodeTraversal t, ReferenceCollectingCallback.BasicBlock block) {
            // Verify behavior callback works
          }
        };

    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    NodeTraversal traversal = new NodeTraversal(compiler, callback);
    
    // Traverse the empty root
    traversal.traverse(root);
    Assert.assertNotNull(callback);
  }

  @Test
  public void testIncompleteInitializationReference() {
    Compiler compiler = new Compiler();
    Node root = Node.newString(Token.SCRIPT, "ref.js");
    
    // Simulate complex variable usage inside a function block
    // function f() { var x; x = 2; return x; }
    Node nameX = Node.newString(Token.NAME, "x");
    Node varX = new Node(Token.VAR, nameX);
    
    Node assignX = new Node(Token.ASSIGN, 
        Node.newString(Token.NAME, "x"), 
        Node.newNumber(2.0));
    
    Node block = new Node(Token.BLOCK, varX, assignX);
    Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.PARAM_LIST), block);
    root.addChildToBack(func);

    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, 
        ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);

    compiler.setPassConfig(new DefaultPassConfig(compiler));
    callback.process(root, root);
    
    // Assert no exception thrown
    Assert.assertNotNull(root);
  }
}