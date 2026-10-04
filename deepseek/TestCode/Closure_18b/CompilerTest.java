```java
package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;

/**
 * Comprehensive unit tests for the Compiler class (Defects4J Closure bug 18).
 * Targets maximum code coverage and fault detection.
 */
@RunWith(JUnit4.class)
public class CompilerTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Standard options for testing
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
        options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);
        options.setChecksOnly(true); // avoid actual code generation overhead
    }

    // ========== Basic compile tests ==========

    @Test
    public void testCompileNullInput() {
        try {
            compiler.compile(null, (JSSourceFile) null, options);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testCompileEmptyInput() {
        JSSourceFile[] externs = new JSSourceFile[0];
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("empty.js", "")
        };
        Result result = compiler.compile(externs, inputs, options);
        assertTrue("Expected no errors for empty input", result.success);
        assertEquals("Expected 0 errors", 0, result.errors.length);
        assertEquals("Expected 0 warnings", 0, result.warnings.length);
    }

    @Test
    public void testCompileSimpleProgram() {
        JSSourceFile[] externs = new JSSourceFile[0];
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "var x = 1;")
        };
        Result result = compiler.compile(externs, inputs, options);
        assertTrue("Simple program should compile", result.success);
        assertNotNull("Compiled source should not be null", compiler.toSource());
    }

    @Test
    public void testCompileWithSyntaxError() {
        JSSourceFile[] externs = new JSSourceFile[0];
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("error.js", "var x = ;")
        };
        Result result = compiler.compile(externs, inputs, options);
        assertFalse("Should have compilation error", result.success);
        assertTrue("Error count > 0", result.errors.length > 0);
    }

    // ========== Edge cases for externs ==========

    @Test
    public void testCompileWithExterns() {
        JSSourceFile[] externs = new JSSourceFile[] {
            JSSourceFile.fromCode("externs.js", "/** @externs */ function alert(x) {}")
        };
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "alert('hello');")
        };
        Result result = compiler.compile(externs, inputs, options);
        assertTrue("Externs should allow alert call", result.success);
    }

    @Test
    public void testCompileWithEmptyExterns() {
        JSSourceFile[] externs = new JSSourceFile[] {
            JSSourceFile.fromCode("emptyExterns.js", "")
        };
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "var y = 2;")
        };
        Result result = compiler.compile(externs, inputs, options);
        assertTrue("Empty externs should be fine", result.success);
    }

    // ========== Options and error handling ==========

    @Test
    public void testCompileWithNullOptions() {
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "var a = 1;")
        };
        try {
            compiler.compile(JSSourceFile.EMPTY, inputs, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testCompileWithWarningLevelVerbose() {
        options.setWarningLevel(CheckLevel.VERBOSE);
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "var x; x = 1;")
        };
        Result result = compiler.compile(JSSourceFile.EMPTY, inputs, options);
        // VERBOSE might cause warnings even for valid code, but should still succeed
        assertTrue("Should compile with warnings", result.success);
    }

    @Test
    public void testCompileWithBadOptionCombination() {
        // Set a combination that might cause internal errors (e.g., ES6 modules with no module processing)
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT6);
        options.setProcessCommonJSModules(false);
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "export const a = 1;")
        };
        Result result = compiler.compile(JSSourceFile.EMPTY, inputs, options);
        // This may produce errors because ES6 module syntax requires appropriate processing
        // We just ensure no crash and result is not null
        assertNotNull("Result should not be null", result);
    }

    // ========== Custom bug-revealing tests ==========

    @Test
    public void testCompileWithDuplicateVariableDeclaration() {
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "var x = 1; var x = 2;")
        };
        Result result = compiler.compile(JSSourceFile.EMPTY, inputs, options);
        // In strict mode, duplicate var might be a warning, not error
        assertTrue("Duplicate var should still compile (allowed)", result.success);
        // But if options have warnings, test further; default checksOnly may catch it
    }

    @Test
    public void testCompileWithStrictModeCheck() {
        options.setWarningLevel(CheckLevel.ERROR, CheckLevel.WARNING); // Upgrade warnings to errors
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "function f() { var x = 1; }")
        };
        Result result = compiler.compile(JSSourceFile.EMPTY, inputs, options);
        assertTrue("Should compile without error", result.success);
    }

    @Test
    public void testCompileWithLargeInput() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("var v").append(i).append(" = ").append(i).append(";\n");
        }
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("large.js", sb.toString())
        };
        Result result = compiler.compile(JSSourceFile.EMPTY, inputs, options);
        assertTrue("Large file should compile", result.success);
    }

    // ========== Integration with internal methods ==========

    @Test
    public void testGetRoot() {
        compiler.compile(JSSourceFile.EMPTY,
            new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var a = 1;") },
            options);
        Node root = compiler.getRoot();
        assertNotNull("Root should exist after compilation", root);
        assertTrue("Root should be a script", root.isScript());
    }

    @Test
    public void testCompileToSourceWithWarnings() {
        options.setWarningLevel(CheckLevel.WARNING);
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "var x = 1; x = 2;") // unused var?
        };
        compiler.compile(JSSourceFile.EMPTY, inputs, options);
        String source = compiler.toSource();
        assertNotNull("toSource() should not throw", source);
    }

    @Test(expected = RuntimeException.class)
    public void testCompileAfterShutdown() {
        compiler.disableThreads();
        // Attempt to compile after shutdown - should throw RuntimeEx
        compiler.compile(JSSourceFile.EMPTY,
            new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var a = 1;") },
            options);
    }

    @Test
    public void testParseInputsWithEmptyList() {
        // Test known internal method: parseInputs (protected, but accessible via test utilities?)
        // We can call compile with empty inputs list
        JSSourceFile[] inputs = new JSSourceFile[0];
        Result result = compiler.compile(JSSourceFile.EMPTY, inputs, options);
        assertTrue("Empty input list should compile to empty output", result.success);
        assertEquals("Source should be empty", "", compiler.toSource());
    }

    // ========== Fault detection: null checks on internal state ==========

    @Test
    public void testGetWarningsBeforeCompile() {
        // Should return empty array or throw? Check behavior
        JSError[] warnings = compiler.getWarnings();
        assertNotNull("Warnings should not be null even before compile", warnings);
        assertEquals("Expected zero warnings", 0, warnings.length);
    }

    @Test
    public void testGetErrorsBeforeCompile() {
        JSError[] errors = compiler.getErrors();
        assertNotNull("Errors should not be null before compile", errors);
        assertEquals("Expected zero errors", 0, errors.length);
    }

    @Test
    public void testGetErrorsAfterFailedCompile() {
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("test.js", "if (true { }")
        };
        Result result = compiler.compile(JSSourceFile.EMPTY, inputs, options);
        assertFalse("Should fail due to parse error", result.success);
        assertTrue("Errors should be populated", result.errors.length > 0);
    }

    // ========== Advanced: module processing (related to bug) ==========

    @Test
    public void testCompileWithCommonJSModule() {
        options.setProcessCommonJSModules(true);
        JSSourceFile[] inputs = new JSSourceFile[] {
            JSSourceFile.fromCode("module.js", "var x = require('./module2');")
        };
        JSSourceFile[] externs = new JSSourceFile[] {
            JSSourceFile.fromCode("externs.js", "function require(s) {}")
        };
        // This may need module resolution; we don't have module2, so expect error
        Result result = compiler.compile(externs, inputs, options);
        // It may succeed if require is considered an extern, or fail if module not found
        // We just verify no crash
        assertNotNull("Result should not be null", result);
    }

    @Test