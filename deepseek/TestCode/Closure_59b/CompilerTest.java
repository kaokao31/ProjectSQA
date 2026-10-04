package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for the Compiler class focusing on initialization,
 * source map handling, and basic compilation flow.
 * Targets Defects4J bug 59 which involved uninitialized source map.
 */
public class CompilerTest {

    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler();
    }

    @Test
    public void testDefaultConstructorInitializesSourceMap() {
        assertNotNull("Source map should be initialized after default constructor",
                compiler.getSourceMap());
    }

    @Test
    public void testSetAndGetSourceMap() {
        SourceMap sourceMap = new SourceMap();
        compiler.setSourceMap(sourceMap);
        assertSame("Setting and getting source map should return the same instance",
                sourceMap, compiler.getSourceMap());
    }

    @Test
    public void testGetErrorManagerNotNull() {
        assertNotNull("Error manager should never be null", compiler.getErrorManager());
    }

    @Test
    public void testGetResultNotNull() {
        assertNotNull("Result should never be null", compiler.getResult());
    }

    @Test
    public void testSourceMapAfterCompilationWithCode() {
        // Compile a trivial script to verify source map stays non-null
        String code = "var x = 1;";
        try {
            compiler.compile(
                    null,
                    code,
                    new JSSourceFile[]{
                            JSSourceFile.fromCode("test.js", code)
                    });
            assertNotNull("Source map should remain non-null after compilation",
                    compiler.getSourceMap());
        } catch (Exception e) {
            fail("Compilation should not throw: " + e.getMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testCompileWithAllNullThrows() {
        compiler.compile(null, null, null);
    }

    @Test
    public void testSetAndGetErrorManager() {
        ErrorManager em = new BasicErrorManager();
        compiler.setErrorManager(em);
        assertSame("Error manager should be replaceable", em, compiler.getErrorManager());
    }

    @Test
    public void testParseCommandLineOptions() {
        String[] args = {"--compilation_level", "ADVANCED"};
        assertTrue("Basic command line parsing should succeed",
                compiler.parseCommandLineOptions(args));
    }

    @Test
    public void testDefaultOptionsNonNull() {
        assertNotNull("Default options should not be null", compiler.getOptions());
    }

    @Test
    public void testSourceMapTypeDefault() {
        // The default source map implementation should be an instance of SourceMap
        assertTrue("Default source map should be a SourceMap instance",
                compiler.getSourceMap() instanceof SourceMap);
    }
}