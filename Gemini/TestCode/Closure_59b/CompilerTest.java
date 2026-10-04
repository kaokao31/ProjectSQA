package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for com.google.javascript.jscomp.Compiler (Closure Bug 59).
 */
public class CompilerTest {

    @Test
    public void testCompilerConstructionAndBasicOptions() {
        Compiler compiler = new Compiler();
        assertNotNull(compiler);
        
        CompilerOptions options = new CompilerOptions();
        options.setCheckGlobalThisLevel(CheckLevel.WARNING);
        
        Result result = compiler.compile(
            JSSourceFile.fromCode("input1.js", "var x = 1;"),
            JSSourceFile.fromCode("input2.js", "var y = 2;"),
            options
        );
        
        assertNotNull(result);
        assertTrue(result.success);
    }

    @Test
    public void testCodeChangeHandler() {
        Compiler compiler = new Compiler();
        compiler.addChangeHandler(new CodeChangeHandler() {
            @Override
            public void reportChange() {
                // Dummy change handler for coverage
            }
        });
        
        CompilerOptions options = new CompilerOptions();
        Result result = compiler.compile(
            JSSourceFile.fromCode("input.js", "function foo() { return 1; }"),
            options
        );
        assertNotNull(result);
    }

    @Test
    public void testInitOptions() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        assertSame(options, compiler.getOptions());
    }

    @Test
    public void testGetRootAndErrorManager() {
        Compiler compiler = new Compiler();
        assertNotNull(compiler.getErrorManager());
        
        CompilerOptions options = new CompilerOptions();
        compiler.init(
            java.util.Collections.singletonList(JSSourceFile.fromCode("externs.js", "")),
            java.util.Collections.singletonList(JSSourceFile.fromCode("source.js", "var a = 1;")),
            options
        );
        
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testGetSourceLineAndRegion() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.compile(
            JSSourceFile.fromCode("test.js", "line1\nline2\nline3"),
            options
        );
        
        // Test source line fetching
        String line = compiler.getSourceLine("test.js", 2);
        assertEquals("line2", line);
        
        assertNull(compiler.getSourceLine("nonexistent.js", 1));
        
        // Test region fetching
        assertNotNull(compiler.getSourceRegion("test.js", 2));
    }

    @Test
    public void testParseSyntheticCode() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node node = compiler.parseSyntheticCode("synthetic.js", "var z = 10;");
        assertNotNull(node);
    }
}