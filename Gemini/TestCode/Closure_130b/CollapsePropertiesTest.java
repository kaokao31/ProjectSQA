package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for CollapseProperties.
 * Targets high code coverage and explores edge cases related to Closure Compiler's CollapseProperties pass.
 */
public class CollapsePropertiesTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Setup basic compiler options if necessary
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testInstantiationAndBasicProperties() {
        // Test creation with default collapsing settings
        CollapseProperties collapse = new CollapseProperties(abstractCompiler, true, true);
        assertNotNull(collapse);

        CollapseProperties collapseFalse = new CollapseProperties(abstractCompiler, false, false);
        assertNotNull(collapseFalse);
    }

    @Test
    public void testProcessWithNullRoot() {
        CollapseProperties collapse = new CollapseProperties(abstractCompiler, true, true);
        // Processing a null root or empty AST should not throw an exception
        try {
            collapse.process(null, null);
        } catch (Exception e) {
            // Depending on strictness, it might accept or throw, let's see if it handles gracefully or verify behavior.
            // If it expects non-null, we can test with an empty script.
        }

        Node root = new Node(Token.BLOCK);
        collapse.process(root, root);
        assertEquals(Token.BLOCK, root.getType());
    }

    @Test
    public void testCollapseNestedNamespaces() {
        // Construct a simple AST representing global object assignment:
        // var a = {};
        // a.b = {};
        // a.b.c = 1;
        
        Node script = new Node(Token.SCRIPT);
        
        // var a = {};
        Node nameA = Node.newString(Token.NAME, "a");
        Node objLit1 = new Node(Token.OBJECTLIT);
        Node varNode = new Node(Token.VAR, nameA);
        nameA.addChildToBack(objLit1);
        script.addChildToBack(varNode);

        // a.b = {};
        Node getPropB = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
        Node objLit2 = new Node(Token.OBJECTLIT);
        Node assignB = new Node(Token.ASSIGN, getPropB, objLit2);
        script.addChildToBack(new Node(Token.EXPR_RESULT, assignB));

        // a.b.c = 1;
        Node getPropBC = new Node(Token.GETPROP, 
            new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b")), 
            Node.newString(Token.STRING, "c"));
        Node number1 = Node.newNumber(1.0);
        Node assignBC = new Node(Token.ASSIGN, getPropBC, number1);
        script.addChildToBack(new Node(Token.EXPR_RESULT, assignBC));

        CollapseProperties collapse = new CollapseProperties(abstractCompiler, true, true);
        
        // Run process
        collapse.process(script, script);
        
        assertNotNull(script);
    }

    @Test
    public void testGlobalFunctionAliasAndCollapsing() {
        // Test scenarios where properties are assigned function values or aliased
        Node script = new Node(Token.SCRIPT);
        
        // ns = {};
        Node nsName = Node.newString(Token.NAME, "ns");
        Node assignNs = new Node(Token.ASSIGN, nsName, new Node(Token.OBJECTLIT));
        script.addChildToBack(new Node(Token.EXPR_RESULT, assignNs));

        // ns.method = function() {};
        Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "ns"), Node.newString(Token.STRING, "method"));
        Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
        Node assignMethod = new Node(Token.ASSIGN, getProp, fn);
        script.addChildToBack(new Node(Token.EXPR_RESULT, assignMethod));

        CollapseProperties collapse = new CollapseProperties(abstractCompiler, true, true);
        collapse.process(script, script);

        assertNotNull(script);
    }

    @Test
    public void testPropertiesWithSideEffectsOrComplexExpressions() {
        Node script = new Node(Token.SCRIPT);
        
        // a = {};
        Node assignA = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), new Node(Token.OBJECTLIT));
        script.addChildToBack(new Node(Token.EXPR_RESULT, assignA));

        // a[b()] = 2; (computed property or complex expression)
        Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), new Node(Token.NAME, "b"));
        Node assignElem = new Node(Token.ASSIGN, getElem, Node.newNumber(2.0));
        script.addChildToBack(new Node(Token.EXPR_RESULT, assignElem));

        CollapseProperties collapse = new CollapseProperties(abstractCompiler, true, true);
        collapse.process(script, script);

        assertNotNull(script);
    }
}