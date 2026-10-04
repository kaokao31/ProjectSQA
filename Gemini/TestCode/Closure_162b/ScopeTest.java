package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class ScopeTest {

    @Test
    public void testScopeCreationAndBasicProperties() {
        Node root = new Node(Token.BLOCK);
        Scope scope = new Scope(root, null);

        Assert.assertSame(root, scope.getRootNode());
        Assert.assertNull(scope.getParent());
        Assert.assertSame(scope, scope.getGlobalScope());
        Assert.assertNull(scope.getCatchNode());
        Assert.assertEquals(Scope.Type.GLOBAL, scope.getType());
    }

    @Test
    public void testChildScopeCreation() {
        Node parentNode = new Node(Token.BLOCK);
        Scope parentScope = new Scope(parentNode, null);

        Node childNode = new Node(Token.BLOCK);
        Scope childScope = new Scope(childNode, parentScope);

        Assert.assertSame(childNode, childScope.getRootNode());
        Assert.assertSame(parentScope, childScope.getParent());
        Assert.assertSame(parentScope, childScope.getGlobalScope());
        Assert.assertEquals(Scope.Type.LOCAL, childScope.getType());
    }

    @Test
    public void testFunctionScopeCreation() {
        Node parentNode = new Node(Token.BLOCK);
        Scope parentScope = new Scope(parentNode, null);

        Node funcNode = new Node(Token.FUNCTION);
        Scope funcScope = new Scope(funcNode, parentScope);

        Assert.assertEquals(Scope.Type.FUNCTION, funcScope.getType());
    }

    @Test
    public void testCatchScopeCreation() {
        Node parentNode = new Node(Token.BLOCK);
        Scope parentScope = new Scope(parentNode, null);

        Node catchNode = new Node(Token.CATCH);
        Scope catchScope = new Scope(catchNode, parentScope);

        Assert.assertEquals(Scope.Type.CATCH, catchScope.getType());
        Assert.assertSame(catchNode, catchScope.getCatchNode());
    }

    @Test
    public void testVarOperations() {
        Node root = new Node(Token.BLOCK);
        Scope scope = new Scope(root, null);

        Assert.assertFalse(scope.isDeclaredInLocalScope("x"));
        Assert.assertNull(scope.getVar("x"));
        Assert.assertFalse(scope.hasOwn("x"));

        Var var = scope.declare("x", new Node(Token.NAME), null, null);
        Assert.assertNotNull(var);
        Assert.assertEquals("x", var.getName());

        Assert.assertSame(var, scope.getVar("x"));
        Assert.assertTrue(scope.hasOwn("x"));
        Assert.assertTrue(scope.isDeclaredInLocalScope("x"));

        scope.undeclare(var);
        Assert.assertNull(scope.getVar("x"));
        Assert.assertFalse(scope.hasOwn("x"));
    }

    @Test
    public void testGetSlot() {
        Node root = new Node(Token.BLOCK);
        Scope scope = new Scope(root, null);
        
        Assert.assertNull(scope.getSlot("nonexistent"));

        Var var = scope.declare("y", new Node(Token.NAME), null, null);
        Assert.assertSame(var, scope.getSlot("y"));
    }

    @Test
    public void testGetVars() {
        Node root = new Node(Token.BLOCK);
        Scope scope = new Scope(root, null);

        scope.declare("a", new Node(Token.NAME), null, null);
        scope.declare("b", new Node(Token.NAME), null, null);

        Iterable<Var> vars = scope.getVars();
        int count = 0;
        for (Var v : vars) {
            count++;
        }
        Assert.assertEquals(2, count);
    }

    @Test
    public void testGetVarCount() {
        Node root = new Node(Token.BLOCK);
        Scope scope = new Scope(root, null);

        Assert.assertEquals(0, scope.getVarCount());
        scope.declare("a", new Node(Token.NAME), null, null);
        Assert.assertEquals(1, scope.getVarCount());
    }

    @Test
    public void testIsGlobal() {
        Node root = new Node(Token.BLOCK);
        Scope globalScope = new Scope(root, null);
        Assert.assertTrue(globalScope.isGlobal());

        Scope localScope = new Scope(new Node(Token.BLOCK), globalScope);
        Assert.assertFalse(localScope.isGlobal());
    }
}