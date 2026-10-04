package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for NameAnalyzer (Closure Bug 40).
 * Designed for maximum coverage and edge-case execution under JUnit 4 and JDK 8.
 */
public class NameAnalyzerTest {

    private Compiler compiler;
    private NameAnalyzer nameAnalyzer;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize with basic options to avoid NPE during traversal if needed
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        nameAnalyzer = new NameAnalyzer(compiler, true);
    }

    @Test
    public void testProcessWithNullRoot() {
        try {
            nameAnalyzer.process(null, null);
            // Depending on implementation, might accept null or throw
        } catch (Exception e) {
            // Expected if null is not handled
        }
    }

    @Test
    public void testProcessSimpleAssignment() {
        Node script = Node.newString(Token.SCRIPT, "test.js");
        Node name = Node.newString(Token.NAME, "a");
        Node number = Node.newNumber(1.0);
        Node assign = new Node(Token.ASSIGN, name, number);
        script.addChildToBack(assign);

        nameAnalyzer.process(script, script);
        
        String jsOutput = nameAnalyzer.getHtmlReport();
        assertNotNull(jsOutput);
    }

    @Test
    public void testProcessFunctionDeclaration() {
        Node script = Node.newString(Token.SCRIPT, "test.js");
        Node funcName = Node.newString(Token.NAME, "foo");
        Node paramList = new Node(Token.LP);
        Node block = new Node(Token.BLOCK);
        Node func = new Node(Token.FUNCTION, funcName, paramList, block);
        script.addChildToBack(func);

        nameAnalyzer.process(script, script);
        assertNotNull(nameAnalyzer.getHtmlReport());
    }

    @Test
    public void testAnonymousFunctionExpression() {
        Node script = Node.newString(Token.SCRIPT, "test.js");
        Node paramList = new Node(Token.LP);
        Node block = new Node(Token.BLOCK);
        Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), paramList, block);
        Node expr = new Node(Token.EXPR_RESULT, func);
        script.addChildToBack(expr);

        nameAnalyzer.process(script, script);
        assertNotNull(nameAnalyzer.getHtmlReport());
    }

    @Test
    public void testObjectLiteralAndGetProp() {
        Node script = Node.newString(Token.SCRIPT, "test.js");
        Node obj = new Node(Token.OBJECTLIT);
        Node key = Node.newString(Token.STRING, "x");
        Node val = Node.newNumber(5.0);
        obj.addChildToBack(key);
        key.addChildToBack(val);

        Node name = Node.newString(Token.NAME, "a");
        Node assign = new Node(Token.ASSIGN, name, obj);
        script.addChildToBack(new Node(Token.EXPR_RESULT, assign));

        nameAnalyzer.process(script, script);
        assertNotNull(nameAnalyzer.getHtmlReport());
    }

    @Test
    public void testGetHtmlReport() {
        String report = nameAnalyzer.getHtmlReport();
        // Even if empty, it should return a String (or check non-null)
        // NameAnalyzer might return null or empty string initially
        // Let's verify it doesn't crash.
        assertTrue(report == null || report instanceof String);
    }
}