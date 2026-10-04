package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test suite for CoalesceVariableNames (Closure Bug 142).
 * Designed for JUnit 4 and Java 8 compatibility.
 */
public class CoalesceVariableNamesTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testInitializationAndPass() {
        // Test basic compiler pass invocation with CoalesceVariableNames
        AbstractCompiler abstractCompiler = compiler;
        CoalesceVariableNames coalesce = new CoalesceVariableNames(abstractCompiler, true);
        
        Node root = getNodeForCode("var x = 1; var y = 2;");
        coalesce.process(null, root);
        
        assertNotNull(root);
    }

    @Test
    public void testCoalesceVariableNamesHotSwap() {
        AbstractCompiler abstractCompiler = compiler;
        CoalesceVariableNames coalesce = new CoalesceVariableNames(abstractCompiler, false);
        
        Node root = getNodeForCode("function f() { var a = 1; var b = 2; return a + b; }");
        coalesce.hotSwapScript(root, null);
        
        assertNotNull(root);
    }

    @Test
    public void testCoalesceWithOptionsAndConstants() {
        AbstractCompiler abstractCompiler = compiler;
        // Test with coalesceConstants = true and false
        CoalesceVariableNames coalesce1 = new CoalesceVariableNames(abstractCompiler, true);
        CoalesceVariableNames coalesce2 = new CoalesceVariableNames(abstractCompiler, false);

        Node root = getNodeForCode("var x = 10; { var y = 20; x = y; }");
        
        coalesce1.process(root, root);
        coalesce2.process(root, root);
        
        assertNotNull(root);
    }

    @Test
    public void testComplexScopesAndVars() {
        AbstractCompiler abstractCompiler = compiler;
        CoalesceVariableNames coalesce = new CoalesceVariableNames(abstractCompiler, true);

        Node root = getNodeForCode(
            "function outer() {" +
            "  var a = 1;" +
            "  function inner() {" +
            "    var b = 2;" +
            "    return b;" +
            "  }" +
            "  return a + inner();" +
            "}"
        );

        coalesce.process(root, root);
        assertNotNull(root);
    }

    @Test
    public void testEmptyRoot() {
        AbstractCompiler abstractCompiler = compiler;
        CoalesceVariableNames coalesce = new CoalesceVariableNames(abstractCompiler, true);

        Node root = new Node(Token.BLOCK);
        coalesce.process(root, root);
        
        assertNotNull(root);
    }

    private Node getNodeForCode(String jsCode) {
        CompilerOptions options = new CompilerOptions();
        compiler.init(
            new JSSourceFile[] {},
            new JSSourceFile[] { JSSourceFile.fromCode("input.js", jsCode) },
            options
        );
        Node root = compiler.parse();
        return root;
    }
}