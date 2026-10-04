package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for com.google.javascript.jscomp.Compiler targeting Closure Bug 64.
 */
public class CompilerTest {

    @Test
    public void testCompilerInstantiationAndBasicMethods() {
        Compiler compiler = new Compiler();
        assertNotNull(compiler);
        assertNotNull(compiler.getErrorManager());
        assertFalse(compiler.hasErrors());
        assertEquals(0, compiler.getErrorCount());
        assertEquals(0, compiler.getWarningCount());
    }

    @Test
    public void testGetStateTypeAndVersion() {
        Compiler compiler = new Compiler();
        // Trigger pre-init or basic getter/setter coverage
        assertNull(compiler.getRoot());
        assertNotNull(compiler.getOptions());
    }

    @Test
    public void testToSourceWithNullRoot() {
        Compiler compiler = new Compiler();
        // Testing behavior when no AST is present
        String source = compiler.toSource();
        assertEquals("", source);
    }
}