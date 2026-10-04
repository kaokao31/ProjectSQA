package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for AbstractCommandLineRunner, targeting bug 149 in Closure Compiler.
 * Focuses on the interaction between --output_wrapper and --create_source_map flags.
 */
public class AbstractCommandLineRunnerTest {

    private static final String OUTPUT_WRAPPER = "(function(){%output%})();";
    private static final String SOURCE_MAP_FLAG = "--create_source_map";
    private static final String OUTPUT_WRAPPER_FLAG = "--output_wrapper";

    // Concrete subclass to expose protected methods for testing
    private static class TestRunner extends AbstractCommandLineRunner {
        public TestRunner(String[] args) {
            super(args);
        }

        @Override
        protected String getOutputWrapper() {
            return super.getOutputWrapper();
        }

        @Override
        protected boolean shouldCreateSourceMap() {
            return super.shouldCreateSourceMap();
        }

        // Expose other protected methods if needed
    }

    private TestRunner runner;

    @Before
    public void setUp() {
        // Default runner with no flags
        runner = new TestRunner(new String[]{});
    }

    @Test
    public void testDefaultOutputWrapperIsEmpty() {
        assertEquals("Default output wrapper should be empty", "", runner.getOutputWrapper());
    }

    @Test
    public void testOutputWrapperSetViaFlag() {
        runner = new TestRunner(new String[]{OUTPUT_WRAPPER_FLAG, OUTPUT_WRAPPER});
        assertEquals("Output wrapper should be set from flag", OUTPUT_WRAPPER, runner.getOutputWrapper());
    }

    @Test
    public void testCreateSourceMapFlagWithoutOutputWrapper() {
        runner = new TestRunner(new String[]{SOURCE_MAP_FLAG, "map.out"});
        assertTrue("shouldCreateSourceMap should be true", runner.shouldCreateSourceMap());
        assertEquals("Output wrapper should remain empty", "", runner.getOutputWrapper());
    }

    @Test
    public void testOutputWrapperWithSourceMapFlag() {
        // This test targets bug 149: output wrapper should be preserved when source map is enabled
        runner = new TestRunner(new String[]{OUTPUT_WRAPPER_FLAG, OUTPUT_WRAPPER, SOURCE_MAP_FLAG, "map.out"});
        assertTrue("shouldCreateSourceMap should be true", runner.shouldCreateSourceMap());
        assertEquals("Output wrapper should still be set", OUTPUT_WRAPPER, runner.getOutputWrapper());
    }

    @Test
    public void testMultipleOutputWrapperFlags() {
        // Last flag wins
        runner = new TestRunner(new String[]{OUTPUT_WRAPPER_FLAG, "first", OUTPUT_WRAPPER_FLAG, OUTPUT_WRAPPER});
        assertEquals("Last output wrapper should be used", OUTPUT_WRAPPER, runner.getOutputWrapper());
    }

    @Test
    public void testEmptyArgs() {
        runner = new TestRunner(new String[]{});
        assertEquals("", runner.getOutputWrapper());
        assertFalse(runner.shouldCreateSourceMap());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidFlagThrows() {
        // Assuming invalid flags cause RuntimeException (adjust if different)
        runner = new TestRunner(new String[]{"--invalid_flag"});
        // The constructor should throw; if not, fail
        fail("Expected RuntimeException for invalid flag");
    }

    @Test
    public void testNullArgs() {
        // Null args should be handled gracefully (e.g., treated as empty)
        runner = new TestRunner(null);
        assertEquals("", runner.getOutputWrapper());
        assertFalse(runner.shouldCreateSourceMap());
    }

    @Test
    public void testOutputWrapperWithEmptyValue() {
        runner = new TestRunner(new String[]{OUTPUT_WRAPPER_FLAG, ""});
        assertEquals("Output wrapper should be empty string", "", runner.getOutputWrapper());
    }

    @Test
    public void testSourceMapFlagWithoutValue() {
        // --create_source_map without a value might be invalid; test behavior
        runner = new TestRunner(new String[]{SOURCE_MAP_FLAG});
        // Depending on implementation, it might default or throw; we assume it defaults to false
        assertFalse("Without value, source map should not be created", runner.shouldCreateSourceMap());
    }
}