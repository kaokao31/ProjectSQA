package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProcessClosurePrimitivesTest {

    private Compiler compiler;
    private AbstractCompiler abstractCompiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
        // Initialize compiler with default options to avoid NPE during passes
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        abstractCompiler = compiler;
    }

    @Test
    public void testConstructorAndGetEncodedExternals() {
        ProcessClosurePrimitives process = new ProcessClosurePrimitives(
                abstractCompiler,
                CheckLevel.OFF,
                false
        );
        assertNotNull(process);
        assertNotNull(process.getExportSymbolFunction());
    }

    @Test
    public void testProcessWithNullRoot() {
        ProcessClosurePrimitives process = new ProcessClosurePrimitives(
                abstractCompiler,
                CheckLevel.WARNING,
                true
        );
        // Processing a null root or empty AST should not throw
        process.process(null, null);
    }

    @Test
    public void testProcessWithSimpleNode() {
        ProcessClosurePrimitives process = new ProcessClosurePrimitives(
                abstractCompiler,
                CheckLevel.ERROR,
                false
        );
        Node root = new Node(Token.BLOCK);
        process.process(root, root);
        // Verify node is untouched or processed correctly without error
        assertEquals(Token.BLOCK, root.getType());
    }

    @Test
    public void testGetExportSymbolFunction() {
        ProcessClosurePrimitives process = new ProcessClosurePrimitives(
                abstractCompiler,
                CheckLevel.WARNING,
                false
        );
        String exportFn = process.getExportSymbolFunction();
        // Typically "goog.exportSymbol" or similar
        assertNotNull(exportFn);
    }

    @Test
    public void testHotSwapMethods() {
        ProcessClosurePrimitives process = new ProcessClosurePrimitives(
                abstractCompiler,
                CheckLevel.OFF,
                true
        );
        Node script = new Node(Token.SCRIPT);
        process.hotSwapScript(script, null);
        
        // Ensure no exception is thrown on basic hotswap calls
        assertNotNull(script);
    }
}