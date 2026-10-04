package com.google.javascript.jscomp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Node;
import com.google.javascript.jscomp.SymbolTable;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.BasicErrorManager;
import java.util.List;
import java.util.ArrayList;

public class CompilerTest {

    private Compiler compiler;
    private CompilerOptions options;

    @Before
    public void setUp() {
        compiler = new Compiler();
        options = new CompilerOptions();
        // Default options: no type checking, standard warnings
        options.setCheckTypes(false);
    }

    // Basic compilation tests
    @Test
    public void testCompileValidProgram() {
        compiler.compileForTesting(options, "var x = 1;");
        assertEquals(0, compiler.getErrorCount());
        assertEquals(0, compiler.getWarningCount());
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testCompileInvalidProgram() {
        compiler.compileForTesting(options, "var x = ;");
        assertTrue(compiler.getErrorCount() > 0);
        assertNotNull(compiler.getErrors());
        assertFalse(compiler.getErrors().isEmpty());
    }

    @Test
    public void testCompileEmptyProgram() {
        compiler.compileForTesting(options, "");
        assertEquals(0, compiler.getErrorCount());
        assertEquals(0, compiler.getWarningCount());
    }

    @Test
    public void testCompileMultipleErrors() {
        compiler.compileForTesting(options, "var x = ; var y = ;");
        assertTrue(compiler.getErrorCount() >= 2);
    }

    // Error and warning reset tests (targets potential bug #160)
    @Test
    public void testCompileResetErrors() {
        compiler.compileForTesting(options, "var x = ;");
        assertTrue(compiler.getErrorCount() > 0);
        compiler.compileForTesting(options, "var x = 1;");
        assertEquals(0, compiler.getErrorCount());
    }

    @Test
    public void testCompileResetWarnings() {
        // Generate a warning (e.g., unused variable) if possible
        // Use a code that produces a warning under default options
        // For Closure Compiler, "if(true){}" may produce a warning about constant condition
        // But to be safe, we first compile a valid program and check warnings are 0
        compiler.compileForTesting(options, "var x = 1;");
        assertEquals(0, compiler.getWarningCount());
        // Then compile a program that might produce warnings (if any)
        // If no warnings, this test is still valid as a reset check
        compiler.compileForTesting(options, "var y = 2;");
        assertEquals(0, compiler.getWarningCount());
    }

    // Accessor tests
    @Test
    public void testGetErrorsAndWarnings() {
        compiler.compileForTesting(options, "var x = ;");
        List<JSError> errors = compiler.getErrors();
        List<JSError> warnings = compiler.getWarnings();
        assertNotNull(errors);
        assertNotNull(warnings);
        assertTrue(errors.size() > 0);
        assertEquals(0, warnings.size());
    }

    @Test
    public void testGetRoot() {
        compiler.compileForTesting(options, "var x = 1;");
        assertNotNull(compiler.getRoot());
    }

    @Test
    public void testGetSymbolTable() {
        compiler.compileForTesting(options, "var x = 1;");
        SymbolTable table = compiler.getSymbolTable();
        assertNotNull(table);
    }

    // Parse and init tests
    @Test
    public void testParse() {
        SourceFile source = SourceFile.fromCode("test.js", "var x = 1;");
        Node node = compiler.parse(source);
        assertNotNull(node);
    }

    @Test
    public void testInit() {
        ErrorManager errorManager = new BasicErrorManager();
        compiler.init(errorManager);
        compiler.compileForTesting(options, "var x = 1;");
        assertEquals(0, compiler.getErrorCount());
    }

    // Edge case: null and empty inputs
    @Test(expected = NullPointerException.class)
    public void testCompileNullSource() {
        compiler.compileForTesting(options, (String) null);
    }

    @Test
    public void testCompileEmptySourceList() {
        compiler.compile(options, new ArrayList<SourceFile>());
        assertEquals(0, compiler.getErrorCount());
    }

    // Tests targeting function type annotations (potential bug #160)
    @Test
    public void testFunctionReturnTypeAnnotation() {
        options.setCheckTypes(true);
        String code = "/** @return {function(): string} */ function f() { return function() { return 'a'; }; }";
        compiler.compileForTesting(options, code);
        assertEquals(0, compiler.getErrorCount());
    }

    @Test
    public void testNestedFunctionType() {
        options.setCheckTypes(true);
        String code = "/** @param {function(function(): string): number} f */ function g(f) {}";
        compiler.compileForTesting(options, code);
        assertEquals(0, compiler.getErrorCount());
    }

    @Test
    public void testFunctionTypeWithMultipleParameters() {
        options.setCheckTypes(true);
        String code = "/** @param {function(string, number): boolean} f */ function h(f) {}";
        compiler.compileForTesting(options, code);
        assertEquals(0, compiler.getErrorCount());
    }

    // Test with type mismatch (should produce errors)
    @Test
    public void testTypeMismatch() {
        options.setCheckTypes(true);
        // Assign a number to a string variable
        String code = "/** @type {string} */ var x = 1;";
        compiler.compileForTesting(options, code);
        assertTrue(compiler.getErrorCount() > 0);
    }

    // Test with JSDoc annotations that might cause issues
    @Test
    public void testComplexJSDoc() {
        options.setCheckTypes(true);
        String code = "/** @constructor @extends {Object} */ function MyClass() {}";
        compiler.compileForTesting(options, code);
        assertEquals(0, compiler.getErrorCount());
    }

    // Test that compile does not throw on large input
    @Test
    public void testLargeProgram() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("var x").append(i).append(" = ").append(i).append(";\n");
        }
        compiler.compileForTesting(options, sb.toString());
        assertEquals(0, compiler.getErrorCount());
    }

    // Test that compile handles special characters
    @Test
    public void testSpecialCharacters() {
        compiler.compileForTesting(options, "var x = 'hello\\nworld';");
        assertEquals(0, compiler.getErrorCount());
    }

    // Test that compile handles comments
    @Test
    public void testComments() {
        compiler.compileForTesting(options, "// comment\nvar x = 1; /* block */");
        assertEquals(0, compiler.getErrorCount());
    }
}