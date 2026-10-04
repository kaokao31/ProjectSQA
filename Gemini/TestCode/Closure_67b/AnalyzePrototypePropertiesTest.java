package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class AnalyzePrototypePropertiesTest {

    private AbstractCompiler compiler;
    private AnalyzePrototypeProperties analyzer;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize with basic options/settings if necessary, or null where acceptable
        analyzer = new AnalyzePrototypeProperties(compiler, null, true, true);
    }

    @Test
    public void testGlobalFunctionProcessorEmpty() {
        AnalyzePrototypeProperties.GlobalFunctionProcessor processor =
                new AnalyzePrototypeProperties.GlobalFunctionProcessor(compiler);

        Node root = new Node(Token.BLOCK);
        NodeTraversal traversal = new NodeTraversal(compiler, processor);
        
        processor.process(traversal, root);
        assertTrue(processor.getNameGenerators().isEmpty());
    }

    @Test
    public void testGlobalFunctionProcessorWithAssignment() {
        AnalyzePrototypeProperties.GlobalFunctionProcessor processor =
                new AnalyzePrototypeProperties.GlobalFunctionProcessor(compiler);

        // Construct: var x = function() {}; x.foo = function() {};
        Node nameNode = Node.newString(Token.NAME, "x");
        Node funcNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        Node varNode = new Node(Token.VAR, nameNode);
        nameNode.addChildToFront(funcNode);

        Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString(Token.STR, "foo"));
        Node funcNode2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        Node assignNode = new Node(Token.ASSIGN, getProp, funcNode2);
        Node exprNode = new Node(Token.EXPR_RESULT, assignNode);

        Node root = new Node(Token.BLOCK, varNode, exprNode);
        NodeTraversal traversal = new NodeTraversal(compiler, processor);

        processor.process(traversal, root);
        assertNotNull(processor.getNameGenerators());
    }

    @Test
    public void testFindPrototypePropertiesWithEmptyAST() {
        Node root = new Node(Token.BLOCK);
        analyzer.process(root, root);
        Collection<AnalyzePrototypeProperties.NameInfo> props = analyzer.getNameInfo();
        assertNotNull(props);
        assertTrue(props.isEmpty());
    }

    @Test
    public void testNameInfoGetDeclarations() {
        AnalyzePrototypeProperties.NameInfo nameInfo = 
                new AnalyzePrototypeProperties.NameInfo("testProp");
        
        assertEquals("testProp", nameInfo.name);
        assertNotNull(nameInfo.getDeclarations());
        assertTrue(nameInfo.getDeclarations().isEmpty());
    }

    @Test
    public void testAssignmentProperty() {
        // Test creation of PrototypeProperty via assignment
        Node lhs = new Node(Token.GETPROP, 
                new Node(Token.GETPROP, Node.newString(Token.NAME, "A"), Node.newString(Token.STR, "prototype")),
                Node.newString(Token.STR, "b"));
        Node value = new Node(Token.NUMBER, 1.0);
        Node assign = new Node(Token.ASSIGN, lhs, value);
        Node node = new Node(Token.EXPR_RESULT, assign);

        AnalyzePrototypeProperties.LiteralDefinitionNodeTraversalCallback callback =
                new AnalyzePrototypeProperties.LiteralDefinitionNodeTraversalCallback(compiler);
        
        NodeTraversal traversal = new NodeTraversal(compiler, callback);
        // Should not throw exception
        callback.shouldTraverse(traversal, node, node);
    }
}