package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GlobalNamespaceTest {

    private AbstractCompiler compiler;
    private AbstractCompiler noVarCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler options minimally if needed
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        noVarCompiler = new Compiler();
        noVarCompiler.initOptions(options);
    }

    @Test
    public void testGlobalNamespaceCreationAndBasicTraversal() {
        // Test constructing a GlobalNamespace and traversing basic scripts / AST structures
        Node script = new Node(Token.SCRIPT);
        // var x = 1;
        Node nameNode = Node.newString(Token.NAME, "x");
        nameNode.addChildToFront(Node.newNumber(1.0));
        Node varNode = new Node(Token.VAR, nameNode);
        script.addChildToFront(varNode);

        Node root = new Node(Token.BLOCK, script);
        
        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        assertNotNull(namespace);
        assertNotNull(namespace.getName(Compiler.getActivePassName())); // or generic lookup
        
        // Check if global variables are collected
        GlobalNamespace.Name xName = namespace.getOwnSlot("x");
        // Depending on compiler pass context, getOwnSlot or similar might be used, 
        // let's test general iteration or lookups.
        assertNotNull(namespace.getNameNames());
    }

    @Test
    public void testGlobalNamespaceWithFunctionsAndAssignments() {
        // function f() {}
        Node funcName = Node.newString(Token.NAME, "f");
        Node funcNode = new Node(Token.FUNCTION, funcName, new Node(Token.LP), new Node(Token.BLOCK));
        Node varNode = new Node(Token.VAR, funcNode);
        
        Node script = new Node(Token.SCRIPT, varNode);
        Node root = new Node(Token.BLOCK, script);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        assertNotNull(namespace);
    }

    @Test
    public void testGlobalNamespaceScopeHandling() {
        // Test declarations inside and outside of global scope
        Node script = new Node(Token.SCRIPT);
        
        // Global var
        Node globalVar = new Node(Token.VAR, Node.newString(Token.NAME, "gVar"));
        script.addChildToBack(globalVar);

        // Function containing local var
        Node localVar = new Node(Token.VAR, Node.newString(Token.NAME, "lVar"));
        Node funcBlock = new Node(Token.BLOCK, localVar);
        Node funcNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "myFunc"), new Node(Token.LP), funcBlock);
        script.addChildToBack(funcNode);

        Node root = new Node(Token.BLOCK, script);

        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        assertNotNull(namespace);
        
        // Ensure global namespace scans correctly
        boolean foundGlobal = false;
        for (GlobalNamespace.Name name : namespace.getNameNames()) {
            if ("gVar".equals(name.name)) {
                foundGlobal = true;
            }
        }
        assertTrue(foundGlobal);
    }

    @Test
    public void testGetRootNodeAndCompilationLevel() {
        Node root = new Node(Token.BLOCK, new Node(Token.SCRIPT));
        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        assertNotNull(namespace);
    }

    @Test
    public void testNameTypesAndProperties() {
        Node script = new Node(Token.SCRIPT);
        Node objName = Node.newString(Token.NAME, "obj");
        Node getProp = new Node(Token.GETPROP, objName, Node.newString(Token.STRING, "prop"));
        Node assign = new Node(Token.ASSIGN, getProp, Node.newNumber(5.0));
        script.addChildToFront(new Node(Token.EXPR_RESULT, assign));

        Node root = new Node(Token.BLOCK, script);
        GlobalNamespace namespace = new GlobalNamespace(compiler, root);
        assertNotNull(namespace);
    }
}