package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for com.google.javascript.jscomp.NameAnalyzer.
 * Designed for maximum coverage and edge-case execution for Closure Bug 114.
 */
public class NameAnalyzerTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testCreationAndBasicAnalysis() {
        NameAnalyzer analyzer = new NameAnalyzer(compiler, true);
        assertNotNull(analyzer);

        // Test process with an empty/simple AST
        Node root = Node.newBlock();
        analyzer.process(root, root);
        
        String report = analyzer.getHtmlReport();
        assertNotNull(report);
    }

    @Test
    public void testNameAnalyzerWithOptions() {
        NameAnalyzer analyzer = new NameAnalyzer(compiler, false);
        assertNotNull(analyzer);

        Node root = Node.newBlock();
        // Add a simple variable assignment to exercise traversal logic
        Node varNode = Node.newVar(Node.newString("foo"), Node.newNumber(1));
        root.addChildToBack(varNode);

        analyzer.process(root, root);
        assertNotNull(analyzer.getHtmlReport());
    }

    @Test
    public void testScopeAndNameAssignmentEdgeCases() {
        NameAnalyzer analyzer = new NameAnalyzer(compiler, true);

        // Construct a more complex AST to trigger various node types (e.g., FUNCTION, ASSIGN, CALL, etc.)
        Node script = Node.newScript();
        Node nameNode = Node.newString("globalVar");
        Node assignNode = new Node(Token.ASSIGN, nameNode, Node.newNumber(42));
        script.addChildToBack(assignNode);

        analyzer.process(script, script);
        assertNotNull(analyzer.getHtmlReport());
    }
}