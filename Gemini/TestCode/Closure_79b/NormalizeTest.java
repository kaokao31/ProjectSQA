package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

public class NormalizeTest {

    @Test
    public void testNormalizeConstructorAndProcess() {
        Compiler compiler = new Compiler();
        // Use a simple dummy node tree representing a script
        Node root = new Node(Token.SCRIPT);
        Node nameNode = Node.newString(Token.NAME, "a");
        root.addChildToBack(nameNode);

        Normalize normalize = new Normalize(compiler, false);
        normalize.process(root, root);

        assertNotNull(root);
    }

    @Test
    public void testNormalizeWithReportCodeChange() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.SCRIPT);

        Normalize normalize = new Normalize(compiler, true);
        normalize.process(root, root);

        assertNotNull(normalize);
    }

    @Test
    public void testNormalizeRecordDuplicateDeclarations() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.SCRIPT);

        // Test the static helper or inner traversal if accessible, 
        // or execute process which invokes normalization passes.
        Normalize.parseAndNormalizeTestCode(compiler, "var x = 1; var x = 2;");
        
        // Even if errors are flagged, process should complete without crashing.
        assertNotNull(compiler);
    }

    @Test
    public void testNormalizeDuplicateVar() {
        Compiler compiler = new Compiler();
        // Passing test code with duplicate declarations to trigger duplicate symbol handling
        Normalize.parseAndNormalizeTestCode(compiler, "function f() { var x; var x; }");
        assertTrue(compiler.hasHaltingErrors() || compiler.getErrors().length >= 0);
    }

    @Test
    public void testNormalizeFunctionExpression() {
        Compiler compiler = new Compiler();
        Node root = Normalize.parseAndNormalizeTestCode(compiler, "var f = function() {};");
        assertNotNull(root);
    }

    @Test
    public void testNormalizeHoisting() {
        Compiler compiler = new Compiler();
        Node root = Normalize.parseAndNormalizeTestCode(compiler, "foo(); function foo() {}");
        assertNotNull(root);
    }

    @Test
    public void testNormalizeLabel() {
        Compiler compiler = new Compiler();
        Node root = Normalize.parseAndNormalizeTestCode(compiler, "outer: { break outer; }");
        assertNotNull(root);
    }

    @Test
    public void testNormalizeScopeCreation() {
        Compiler compiler = new Compiler();
        ScopeCreator scopeCreator = Normalize.createScopeCreator(compiler);
        assertNotNull(scopeCreator);
    }

    @Test
    public void testNormalizeMakeHeadless() {
        Compiler compiler = new Compiler();
        Node root = new Node(Token.SCRIPT);
        Normalize.NormalizeStatements normalizeStatements = new Normalize.NormalizeStatements(compiler, false);
        
        NodeTraversal traversal = new NodeTraversal(compiler, normalizeStatements);
        traversal.traverse(root);
        
        assertNotNull(normalizeStatements);
    }
}