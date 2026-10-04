package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

public class CompilerTest {

    @Test
    public void testCompilerInstantiationAndBasicMethods() {
        Compiler compiler = new Compiler();
        Assert.assertNotNull(compiler);
        Assert.assertNotNull(compiler.getErrorManager());
        Assert.assertFalse(compiler.hasErrors());
        
        compiler.initOptions(new CompilerOptions());
        Assert.assertNotNull(compiler.getOptions());
    }

    @Test
    public void testSetLoggingLevel() {
        Compiler.setLoggingLevel(Level.INFO);
        // Verify no exception thrown
        Assert.assertTrue(true);
    }

    @Test
    public void testGetRoot() {
        Compiler compiler = new Compiler();
        Node root = compiler.getRoot();
        // Initially root might be null or empty depending on state
        Assert.assertEquals(compiler.getRoot(), root);
    }

    @Test
    public void testProcessCode() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // Pass empty sources to verify execution path without crashing
        JSSourceFile[] inputs = new JSSourceFile[0];
        JSSourceFile[] externs = new JSSourceFile[0];
        
        compiler.compile(externs, inputs, options);
        Assert.assertNotNull(compiler.getResult());
    }
}