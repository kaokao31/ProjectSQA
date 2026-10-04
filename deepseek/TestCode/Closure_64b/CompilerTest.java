package com.google.javascript.jscomp;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for Compiler class, targeting bug #64 in Defects4J.
 * Focuses on module detection and dependency ordering.
 */
public class CompilerTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Enable module processing to trigger the bug
        options.setManageClosureDependencies(true);
        options.setClosurePass(true);
    }

    /**
     * Test that a file with only goog.provide (no goog.module) is NOT treated as a module.
     * This is the core of bug #64.
     */
    @Test
    public void testFileWithProvideOnlyIsNotModule() {
        String code = "goog.provide('foo.bar');\n foo.bar = {};";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(), // externs
                inputs,
                options);

        assertTrue("Compilation should succeed", result.success);
        // The bug would cause the file to be incorrectly treated as a module,
        // leading to a module count > 0 or an error.
        // After fix, there should be no modules.
        assertEquals("No modules should be created for provide-only files",
                0, compiler.getModules().length);
    }

    /**
     * Test that a file with goog.module is correctly treated as a module.
     */
    @Test
    public void testFileWithGoogModuleIsModule() {
        String code = "goog.module('my.module');\n exports = {};";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);

        assertTrue("Compilation should succeed", result.success);
        // Should be exactly one module
        assertEquals("One module should be created for goog.module file",
                1, compiler.getModules().length);
    }

    /**
     * Test that a file with both goog.provide and goog.module is treated as a module.
     */
    @Test
    public void testFileWithProvideAndModuleIsModule() {
        String code = "goog.module('my.module');\n goog.provide('my.module.provided');\n exports = {};";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);

        assertTrue("Compilation should succeed", result.success);
        assertEquals("One module should be created", 1, compiler.getModules().length);
    }

    /**
     * Test that a file with no goog.provide or goog.module is not a module.
     */
    @Test
    public void testPlainFileIsNotModule() {
        String code = "var x = 1;";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);

        assertTrue("Compilation should succeed", result.success);
        assertEquals("No modules should be created", 0, compiler.getModules().length);
    }

    /**
     * Test that multiple files with provides and requires are correctly ordered.
     * This exercises dependency resolution.
     */
    @Test
    public void testDependencyOrdering() {
        String codeA = "goog.provide('A');\n A = {};";
        String codeB = "goog.provide('B');\n goog.require('A');\n B = {a: A};";
        JSSourceFile inputA = JSSourceFile.fromCode("a.js", codeA);
        JSSourceFile inputB = JSSourceFile.fromCode("b.js", codeB);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(inputA);
        inputs.add(inputB);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);

        assertTrue("Compilation should succeed", result.success);
        // No modules expected since no goog.module
        assertEquals("No modules should be created", 0, compiler.getModules().length);
        // The order should be A then B (A is dependency of B)
        // We can check the compiled output order indirectly via the result
        // For now, just ensure no errors
    }

    /**
     * Test that a file with goog.module and goog.require works.
     */
    @Test
    public void testModuleWithRequire() {
        String codeA = "goog.module('A');\n exports = {};";
        String codeB = "goog.module('B');\n var A = goog.require('A');\n exports = {a: A};";
        JSSourceFile inputA = JSSourceFile.fromCode("a.js", codeA);
        JSSourceFile inputB = JSSourceFile.fromCode("b.js", codeB);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(inputA);
        inputs.add(inputB);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);

        assertTrue("Compilation should succeed", result.success);
        assertEquals("Two modules should be created", 2, compiler.getModules().length);
    }

    /**
     * Test that an empty input list does not cause an exception.
     */
    @Test
    public void testEmptyInputList() {
        List<SourceFile> inputs = new ArrayList<>();
        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);
        assertTrue("Compilation with empty inputs should succeed", result.success);
        assertEquals("No modules", 0, compiler.getModules().length);
    }

    /**
     * Test that null inputs are handled gracefully (should throw or return failure).
     * Depending on implementation, we expect an exception or a failed result.
     */
    @Test(expected = NullPointerException.class)
    public void testNullInputList() {
        compiler.compile(
                new ArrayList<SourceFile>(),
                null,
                options);
    }

    /**
     * Test that a file with a syntax error fails compilation.
     */
    @Test
    public void testSyntaxError() {
        String code = "var x = ;";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);
        assertFalse("Compilation should fail with syntax error", result.success);
    }

    /**
     * Test that a file with a missing required namespace fails.
     */
    @Test
    public void testMissingRequire() {
        String code = "goog.require('nonexistent');";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);
        assertFalse("Compilation should fail due to missing require", result.success);
    }

    /**
     * Test that a file with goog.provide and goog.require works correctly.
     */
    @Test
    public void testProvideAndRequire() {
        String codeA = "goog.provide('A');\n A = {};";
        String codeB = "goog.provide('B');\n goog.require('A');\n B = {a: A};";
        JSSourceFile inputA = JSSourceFile.fromCode("a.js", codeA);
        JSSourceFile inputB = JSSourceFile.fromCode("b.js", codeB);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(inputA);
        inputs.add(inputB);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);
        assertTrue("Compilation should succeed", result.success);
        // No modules expected
        assertEquals("No modules", 0, compiler.getModules().length);
    }

    /**
     * Test that the compiler handles multiple goog.provide in one file.
     */
    @Test
    public void testMultipleProvides() {
        String code = "goog.provide('A');\n goog.provide('B');\n A = {};\n B = {};";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);
        assertTrue("Compilation should succeed", result.success);
        assertEquals("No modules", 0, compiler.getModules().length);
    }

    /**
     * Test that a file with goog.module and goog.provide is treated as a module.
     */
    @Test
    public void testModuleWithProvide() {
        String code = "goog.module('my.module');\n goog.provide('my.module.provided');\n exports = {};";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);
        assertTrue("Compilation should succeed", result.success);
        assertEquals("One module", 1, compiler.getModules().length);
    }

    /**
     * Test that the compiler correctly identifies a file as a module when it has
     * goog.module even if it also has goog.provide.
     */
    @Test
    public void testModuleDetectionPrecedence() {
        String code = "goog.module('M');\n goog.provide('P');\n exports = {};";
        JSSourceFile input = JSSourceFile.fromCode("test.js", code);
        List<SourceFile> inputs = new ArrayList<>();
        inputs.add(input);

        Result result = compiler.compile(
                new ArrayList<SourceFile>(),
                inputs,
                options);
        assertTrue("Compilation should succeed", result.success);
        // The file should be a module, not a provide-only file
        assertEquals("One module", 1, compiler.getModules().length);
    }
}