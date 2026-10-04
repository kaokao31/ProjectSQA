package com.google.javascript.jscomp.parsing;

import com.google.javascript.jscomp.parsing.IRFactory;
import com.google.javascript.jscomp.parsing.ParserRunner;
import com.google.javascript.jscomp.parsing.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class IRFactoryTest {

    private ParserRunner parserRunner;
    private Set<String> annotationNames;

    @Before
    public void setUp() {
        annotationNames = new HashSet<>();
        parserRunner = new ParserRunner();
    }

    // Helper to parse a JavaScript string and return the AST root
    private Node parseScript(String code) throws IOException {
        SourceFile sourceFile = SourceFile.builder()
                .withCode(code)
                .withFileName("test.js")
                .build();
        return parserRunner.parse(sourceFile, annotationNames, false);
    }

    // Helper to transform a parsed AST using IRFactory
    private Node transform(Node astRoot) {
        return IRFactory.transformTree(astRoot);
    }

    // Test basic for-in loop with var (should work)
    @Test
    public void testForInVar() throws IOException {
        String code = "for (var x in obj) { }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull("Transformed AST should not be null", transformed);
        // Verify that the transformed tree contains a FOR_IN node
        assertTrue("Expected FOR_IN node", containsToken(transformed, Token.FOR_IN));
    }

    // Test for-in loop with let (bug trigger for Closure 42)
    @Test
    public void testForInLet() throws IOException {
        String code = "for (let x in obj) { }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull("Transformed AST should not be null", transformed);
        assertTrue("Expected FOR_IN node", containsToken(transformed, Token.FOR_IN));
    }

    // Test for-in loop with const (similar edge case)
    @Test
    public void testForInConst() throws IOException {
        String code = "for (const x in obj) { }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull("Transformed AST should not be null", transformed);
        assertTrue("Expected FOR_IN node", containsToken(transformed, Token.FOR_IN));
    }

    // Test for-in with empty body
    @Test
    public void testForInEmptyBody() throws IOException {
        String code = "for (var x in obj);";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertTrue(containsToken(transformed, Token.FOR_IN));
    }

    // Test for-in with nested block
    @Test
    public void testForInNestedBlock() throws IOException {
        String code = "for (var x in obj) { if (x) { break; } }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertTrue(containsToken(transformed, Token.FOR_IN));
    }

    // Test for loop (not for-in) to ensure no regression
    @Test
    public void testForLoop() throws IOException {
        String code = "for (var i = 0; i < 10; i++) { }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertTrue(containsToken(transformed, Token.FOR));
    }

    // Test while loop
    @Test
    public void testWhileLoop() throws IOException {
        String code = "while (true) { break; }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertTrue(containsToken(transformed, Token.WHILE));
    }

    // Test do-while loop
    @Test
    public void testDoWhileLoop() throws IOException {
        String code = "do { } while (false);";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertTrue(containsToken(transformed, Token.DO));
    }

    // Test if-else branch
    @Test
    public void testIfElse() throws IOException {
        String code = "if (a) { b; } else { c; }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertTrue(containsToken(transformed, Token.IF));
    }

    // Test try-catch-finally
    @Test
    public void testTryCatchFinally() throws IOException {
        String code = "try { throw e; } catch (ex) { } finally { }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertTrue(containsToken(transformed, Token.TRY));
    }

    // Test switch statement
    @Test
    public void testSwitch() throws IOException {
        String code = "switch (x) { case 1: break; default: break; }";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertTrue(containsToken(transformed, Token.SWITCH));
    }

    // Test empty program
    @Test
    public void testEmptyProgram() throws IOException {
        String code = "";
        Node ast = parseScript(code);
        Node transformed = transform(ast);
        assertNotNull(transformed);
        assertEquals("Empty program should be a BLOCK", Token.BLOCK, transformed.getToken());
    }

    // Test null input (should throw NullPointerException or similar)
    @Test(expected = NullPointerException.class)
    public void testNullInput() {
        IRFactory.transformTree(null);
    }

    // Helper to recursively check if a node of given token exists
    private boolean containsToken(Node node, int token) {
        if (node.getToken() == token) {
            return true;
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (containsToken(child, token)) {
                return true;
            }
        }
        return false;
    }
}