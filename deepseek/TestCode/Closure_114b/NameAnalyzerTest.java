package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerInput;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for NameAnalyzer (Closure Compiler Bug #114 context).
 * Achieves maximum code coverage and attempts to trigger faults related to
 * goog.scope variable removal and dead code elimination.
 */
public class NameAnalyzerTest {

    private Compiler compiler;
    private CompilerOptions options;
    private static final String EXTERNS = "function alert(x) {}\n" +
                                          "var console = {};\n" +
                                          "console.log = function(x) {};\n";

    @Before
    public void setUp() throws Exception {
        options = new CompilerOptions();
        options.setLanguageIn(LanguageMode.ECMASCRIPT6);
        options.setIdeMode(true);
        // Ensure no warnings are suppressed to expose possible faults
        options.setWarningLevel(DiagnosticGroups.UNUSED_VARIABLE, CheckLevel.ERROR);
        options.setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLE, CheckLevel.ERROR);
        options.setWarningLevel(DiagnosticGroups.DUPLICATE_VAR, CheckLevel.ERROR);
        compiler = new Compiler();
        // Initialize but do not compile yet
        compiler.initOptions(options);
    }

    // Helper to compile code and get the NameAnalyzer instance for the first input
    private NameAnalyzer analyzeCode(String code) {
        return analyzeCode(code, false);
    }

    private NameAnalyzer analyzeCode(String code, boolean useMultipleInputs) {
        SourceFile externs = SourceFile.fromCode("externs.js", EXTERNS);
        SourceFile mainSource = SourceFile.fromCode("test.js", code);
        SourceFile[] inputs = new SourceFile[] { mainSource };
        if (useMultipleInputs) {
            // Add another input to test multi-input analysis
            inputs = new SourceFile[] {
                mainSource,
                SourceFile.fromCode("extra.js", "var extra = 123;")
            };
        }
        compiler.compile(externs, inputs);
        // NameAnalyzer is invoked by the compiler passes; we retrieve its output indirectly
        // For direct unit testing, we instantiate it manually
        CompilerInput input = compiler.getInputForTesting("test.js");
        if (input == null) {
            fail("Input not found after compilation");
        }
        NameAnalyzer analyzer = new NameAnalyzer(compiler, input);
        return analyzer;
    }

    @Test
    public void testSimpleVariableUsed() {
        String code = "var x = 1; alert(x);";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        // Expect no unused variable errors; if NameAnalyzer incorrectly removes x, test fails
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        // Check that x is still in scope (by examining compiled JS or assertions)
        // We rely on compiler output – but we can check warnings
        // For fault detection: the bug #114 might cause false positive unused warning
        // So we assert no warnings
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testGoogScopeVariableUsedOnlyInside() {
        String code = "goog.scope(function() {\n"
                    + "  var internal = 42;\n"
                    + "  alert(internal);\n"
                    + "});";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        // Bug #114: internal might be removed because it's declared inside goog.scope
        // and not referenced via goog.global or exported. But it is used.
        // The correct behavior: keep internal.
        // Assert no errors/warnings about unused variable.
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testGoogScopeVariableUsedOutsideViaGoogGlobal() {
        String code = "goog.scope(function() {\n"
                    + "  var exposed = 'hello';\n"
                    + "  goog.global.exposed = exposed;\n"
                    + "});\n"
                    + "alert(goog.global.exposed);";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testGoogScopeUnusedVariableRemoved() {
        String code = "goog.scope(function() {\n"
                    + "  var unused = 0;  // should be removed\n"
                    + "  var used = 1; alert(used);\n"
                    + "});";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        // The unused variable should be removed and generate a warning/error
        // But actual behavior: NameAnalyzer may not remove it; the dead code pass does
        // We check that compiler warns about unused variable (if that pass is enabled)
        // We enabled UNUSED_VARIABLE warning above, so we expect one warning for 'unused'
        // However, bug #114 might cause removal of 'used' as well, causing a missing alert.
        // Here we just verify that at least 'unused' is flagged.
        assertTrue("Should have at least one warning", compiler.getWarningCount() > 0);
    }

    @Test
    public void testNestedGoogScope() {
        String code = "goog.scope(function() {\n"
                    + "  var outer = 1;\n"
                    + "  goog.scope(function() {\n"
                    + "    var inner = outer + 1;\n"
                    + "    alert(inner);\n"
                    + "  });\n"
                    + "});";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        // outer is referenced by inner scope, so it should be kept.
        // Bug #114 might incorrectly remove outer.
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testMultipleInputsAndGoogScope() {
        String code = "goog.scope(function() {\n"
                    + "  var a = 1;\n"
                    + "  alert(a);\n"
                    + "});";
        NameAnalyzer analyzer = analyzeCode(code, true); // with extra input
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testEmptyCode() {
        String code = "";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test(expected = NullPointerException.class)
    public void testNullInput() {
        // NameAnalyzer should throw NPE if input is null
        NameAnalyzer analyzer = new NameAnalyzer(compiler, null);
        analyzer.process();
    }

    @Test
    public void testScopeCrossFileReference() {
        // Simulate common pattern: variable defined in one file used in another via goog.scope
        // This is complex; we just test that no crash occurs.
        SourceFile externs = SourceFile.fromCode("externs.js", EXTERNS);
        SourceFile file1 = SourceFile.fromCode("file1.js",
            "goog.provide('a');\n" +
            "goog.scope(function() {\n" +
            "  a = {};\n" +
            "});");
        SourceFile file2 = SourceFile.fromCode("file2.js",
            "goog.require('a');\n" +
            "goog.scope(function() {\n" +
            "  var b = a;\n" +
            "  alert(b);\n" +
            "});");
        compiler.compile(externs, file1, file2);
        // After compilation, run NameAnalyzer on file2
        CompilerInput input = compiler.getInputForTesting("file2.js");
        if (input == null) fail("file2 not found");
        NameAnalyzer analyzer = new NameAnalyzer(compiler, input);
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        // No assertion on warnings because this depends on full compilation
    }

    @Test
    public void testVariableWithSameNameInDifferentScopes() {
        String code = "var x = 1;\n"
                    + "function f() {\n"
                    + "  var x = 2;\n"
                    + "  alert(x);\n"
                    + "}\n"
                    + "alert(x);";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        // Both x's are used, no warning
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testForLoopVariableUsed() {
        String code = "for (var i = 0; i < 10; i++) { alert(i); }";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testCatchBlockVariable() {
        String code = "try { throw 1; } catch (e) { alert(e); }";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testExternVariableNotAnalyzed() {
        // alert is from externs; NameAnalyzer should not mark it as unused
        String code = "var x = 1; alert(x);";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        assertEquals("No errors expected", 0, compiler.getErrorCount());
        assertEquals("No warnings expected", 0, compiler.getWarningCount());
    }

    @Test
    public void testDuplicateVariableDeclaration() {
        String code = "var y = 1; var y = 2; alert(y);";
        NameAnalyzer analyzer = analyzeCode(code);
        analyzer.process();
        // Duplicate var is a warning or error depending on warning level; we set DUPLICATE_VAR to ERROR
        assertTrue("Duplicate variable should be an error", compiler.getErrorCount() > 0);
    }
}