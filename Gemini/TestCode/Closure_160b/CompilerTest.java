package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

import java.io.PrintStream;
import java.util.logging.Level;

/**
 * JUnit 4 test suite for com.google.javascript.jscomp.Compiler (Closure Bug 160 / general coverage).
 */
public class CompilerTest {

    @Test
    public void testCompilerInitializationAndBasics() {
        Compiler compiler = new Compiler();
        Assert.assertNotNull(compiler);

        // Test error manager
        Assert.assertNotNull(compiler.getErrorManager());

        // Test options / config defaults
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        Assert.assertSame(options, compiler.getOptions());

        // Test logger level
        Compiler.setLoggingLevel(Level.INFO);
        Assert.assertEquals(Level.INFO, Compiler.getLogger().getLevel());
        
        // Reset logging level back
        Compiler.setLoggingLevel(Level.WARNING);
    }

    @Test
    public void testRunSanityCheck() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.checkSymbols = true;
        compiler.initOptions(options);

        // Create a simple AST
        Node root = new Node(Token.BLOCK);
        Node script = new Node(Token.SCRIPT);
        root.addChildToBack(script);

        compiler.runSanityCheck();
        Assert.assertFalse(compiler.hasErrors());
    }

    @Test
    public void testGetErrorCountAndWarnings() {
        Compiler compiler = new Compiler();
        Assert.assertEquals(0, compiler.getErrorCount());
        Assert.assertEquals(0, compiler.getWarningCount());
        Assert.assertFalse(compiler.hasErrors());
        Assert.assertFalse(compiler.hasWarnings());
    }

    @Test
    public void testGetRootWithoutInit() {
        Compiler compiler = new Compiler();
        // Depending on state, getRoot might be null or throw, let's verify it doesn't crash unexpectedly
        try {
            Node root = compiler.getRoot();
            // If it returns null, that's expected before init
            Assert.assertNull(root);
        } catch (Exception e) {
            // If it throws an IllegalStateException, that's also acceptable behavior for uninitialized compiler
            Assert.assertTrue(e instanceof IllegalStateException || e instanceof NullPointerException);
        }
    }

    @Test
    public void testProcessMethods() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);

        // Test pre-process or pass execution hooks safely if exposed
        Assert.assertNotNull(compiler.getLifeCycleStage());
    }

    @Test
    public void testCodeGenerationProperties() {
        Compiler compiler = new Compiler();
        Assert.assertNull(compiler.getSourceFileByName("nonexistent.js"));
    }
}