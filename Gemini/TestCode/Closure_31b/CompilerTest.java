package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for com.google.javascript.jscomp.Compiler (Closure Bug 31).
 */
public class CompilerTest {

    private Compiler compiler;
    private MockErrorManager errorManager;

    @Before
    public void setUp() {
        errorManager = new MockErrorManager();
        compiler = new Compiler(errorManager);
    }

    @After
    public void tearDown() {
        compiler = null;
        errorManager = null;
    }

    @Test
    public void testBasicInitialization() {
        assertNotNull(compiler);
        assertNotNull(compiler.getOptions());
        assertFalse(compiler.hasErrors());
    }

    @Test
    public void testCompileWithNullInputs() {
        CompilerOptions options = new CompilerOptions();
        Result result = compiler.compile((JSSourceFile) null, (JSSourceFile) null, options);
        assertNotNull(result);
    }

    @Test
    public void testCompileSingleValidSource() {
        CompilerOptions options = new CompilerOptions();
        JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
        JSSourceFile source = JSSourceFile.fromCode("input.js", "var x = 1;");

        Result result = compiler.compile(extern, source, options);
        assertNotNull(result);
        assertEquals(0, result.errors.length);
        assertEquals(0, result.warnings.length);
    }

    @Test
    public void testCompileMultipleSourcesList() {
        CompilerOptions options = new CompilerOptions();
        List<JSSourceFile> externs = Lists.newArrayList(JSSourceFile.fromCode("externs.js", ""));
        List<JSSourceFile> sources = Lists.newArrayList(
                JSSourceFile.fromCode("input1.js", "var a = 10;"),
                JSSourceFile.fromCode("input2.js", "var b = 20;")
        );

        Result result = compiler.compile(externs, sources, options);
        assertNotNull(result);
        assertTrue(compiler.getErrorManager().getErrorCount() == 0);
    }

    @Test
    public void testParseSyntheticCode() {
        Node root = compiler.parseSyntheticCode("synthetic.js", "function foo() { return true; }");
        assertNotNull(root);
        assertTrue(root.isScript() || root.isBlock());
    }

    @Test
    public void testParseTestCode() {
        Node root = compiler.parseTestCode("var x = 'test';");
        assertNotNull(root);
    }

    @Test
    public void testGetRootBeforeCompile() {
        Node root = compiler.getRoot();
        assertNull(root);
    }

    @Test
    public void testGetSourceLineAndRegion() {
        compiler.init(
                Lists.newArrayList(JSSourceFile.fromCode("externs.js", "")),
                Lists.newArrayList(JSSourceFile.fromCode("test.js", "line1\nline2\nline3")),
                new CompilerOptions()
        );

        String line = compiler.getSourceLine("test.js", 2);
        assertEquals("line2", line);

        SourceRegion region = compiler.getSourceRegion("test.js", 2);
        assertNotNull(region);
    }

    @Test
    public void testGetSourceRegionOutOfBounds() {
        compiler.init(
                Lists.newArrayList(JSSourceFile.fromCode("externs.js", "")),
                Lists.newArrayList(JSSourceFile.fromCode("test.js", "line1")),
                new CompilerOptions()
        );

        SourceRegion region = compiler.getSourceRegion("test.js", 100);
        assertNull(region);

        String line = compiler.getSourceLine("nonexistent.js", 1);
        assertNull(line);
    }

    @Test
    public void testGetProgress() {
        double progress = compiler.getProgress();
        assertEquals(0.0, progress, 0.001);
    }

    @Test
    public void testGetUniqueIdSupplier() {
        assertNotNull(compiler.getUniqueIdSupplier());
    }

    @Test
    public void testToSource() {
        CompilerOptions options = new CompilerOptions();
        JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
        JSSourceFile source = JSSourceFile.fromCode("input.js", "var x = 1;");

        compiler.compile(extern, source, options);
        String code = compiler.toSource();
        assertNotNull(code);
    }

    @Test
    public void testDisables() {
        compiler.disableIdeMode();
        compiler.setHasRegExpGlobalVarDependencies(true);
        compiler.setCssRenamingMap(null);
        assertNotNull(compiler.getOptions());
    }

    @Test
    public void testGetDefaultExterns() throws Exception {
        List<JSSourceFile> defaultExterns = Compiler.getDefaultExterns();
        assertNotNull(defaultExterns);
    }

    @Test
    public void testRunPostCompilationChecks() {
        CompilerOptions options = new CompilerOptions();
        JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
        JSSourceFile source = JSSourceFile.fromCode("input.js", "var x = 1;");
        compiler.compile(extern, source, options);

        compiler.processEscapedString("test");
        assertFalse(compiler.hasErrors());
    }

    @Test
    public void testReportCodeChangeAndVersion() {
        compiler.reportCodeChange();
        assertNotNull(Compiler.getReleaseDate());
        assertNotNull(Compiler.getReleaseVersion());
    }

    @Test
    public void testGetErrorManager() {
        assertNotNull(compiler.getErrorManager());
    }

    // Simple mock error manager for testing
    private static class MockErrorManager extends BasicErrorManager {
        private final List<JSError> errors = new ArrayList<>();
        private final List<JSError> warnings = new ArrayList<>();

        @Override
        public void println(CheckLevel level, JSError error) {
            if (level == CheckLevel.ERROR) {
                errors.add(error);
            } else if (level == CheckLevel.WARNING) {
                warnings.add(error);
            }
        }

        @Override
        protected void printSummary() {
            // No-op
        }

        @Override
        public int getErrorCount() {
            return errors.size();
        }

        @Override
        public int getWarningCount() {
            return warnings.size();
        }
    }
}