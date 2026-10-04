package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for CommandLineRunner, targeting Closure bug 151.
 * Covers flag parsing, edge cases, and null/empty inputs.
 */
public class CommandLineRunnerTest {

    private CommandLineRunner runner;

    @Before
    public void setUp() throws Exception {
        // No initialization needed for static methods; 
        // but we can create an instance for non-static tests if needed.
        runner = null; // placeholder
    }

    // ==================== Main method tests ====================
    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testMainWithNullArgs() {
        // Passing null to main should trigger FlagUsageException or similar
        CommandLineRunner.main((String[]) null);
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testMainWithEmptyArgs() {
        CommandLineRunner.main(new String[0]);
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testMainWithOnlyHelpFlag() {
        CommandLineRunner.main(new String[]{"--help"});
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testMainWithOnlyVersionFlag() {
        CommandLineRunner.main(new String[]{"--version"});
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testMainWithInvalidFlag() {
        CommandLineRunner.main(new String[]{"--unknown_flag"});
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testMainWithFlagMissingValue() {
        CommandLineRunner.main(new String[]{"--compilation_level"});
    }

    // ==================== createCommandLineOptions tests ====================
    @Test
    public void testCreateOptionsWithJsInput() throws Exception {
        String[] args = {"--js=file1.js", "--js_output_file=out.js"};
        CommandLineRunner.Options options = CommandLineRunner.createOptions(args);
        assertNotNull(options);
        assertEquals(1, options.jsFiles.size());
        assertEquals("file1.js", options.jsFiles.get(0).getName());
        assertEquals("out.js", options.jsOutputFile);
    }

    @Test
    public void testCreateOptionsWithMultipleJsInputs() throws Exception {
        String[] args = {"--js=a.js", "--js=b.js", "--js_output_file=out.js"};
        CommandLineRunner.Options options = CommandLineRunner.createOptions(args);
        assertEquals(2, options.jsFiles.size());
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testCreateOptionsMissingOutput() throws Exception {
        // When no --js_output_file is provided, should throw
        CommandLineRunner.createOptions(new String[]{"--js=file.js"});
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testCreateOptionsWithNullArgs() throws Exception {
        CommandLineRunner.createOptions(null);
    }

    // ==================== getDefaultOptions tests ====================
    @Test
    public void testGetDefaultOptions() {
        CompilerOptions options = CommandLineRunner.getDefaultOptions();
        assertNotNull(options);
        // Verify some default settings
        assertTrue(options.getLanguageIn() == LanguageMode.ECMASCRIPT3);
        // Add more assertions based on expected defaults
    }

    // ==================== Bug-specific tests (Closure 151) ====================
    // The bug: when no js files are specified but other flags are present, 
    // the runner should fail gracefully instead of crashing.
    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testNoJsFilesSpecified() {
        CommandLineRunner.main(new String[]{"--compilation_level=SIMPLE_OPTIMIZATIONS", "--js_output_file=out.js"});
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testJsFileEmptyString() {
        CommandLineRunner.main(new String[]{"--js=", "--js_output_file=out.js"});
    }

    // Test that --js flag with nonexistent file does not crash immediately
    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testJsFileNotExists() {
        CommandLineRunner.main(new String[]{"--js=nonexistent.js", "--js_output_file=out.js"});
    }

    // ==================== Edge cases for flag values ====================
    @Test
    public void testCompilationLevelFlag() throws Exception {
        String[] args = {"--js=test.js", "--js_output_file=test.js", "--compilation_level=ADVANCED_OPTIMIZATIONS"};
        CommandLineRunner.Options options = CommandLineRunner.createOptions(args);
        assertEquals(CompilationLevel.ADVANCED_OPTIMIZATIONS, options.compilationLevel);
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testInvalidCompilationLevel() {
        CommandLineRunner.main(new String[]{"--js=test.js", "--js_output_file=test.js", "--compilation_level=INVALID_LEVEL"});
    }

    @Test
    public void testWarningLevelFlag() throws Exception {
        String[] args = {"--js=test.js", "--js_output_file=test.js", "--warning_level=VERBOSE"};
        CommandLineRunner.Options options = CommandLineRunner.createOptions(args);
        assertEquals(WarningLevel.VERBOSE, options.warningLevel);
    }

    // ==================== Internal flag parsing (if exposed) ====================
    // If there is a method to parse individual flags, test it here.
    // For now, we rely on createOptions and main.

    // ==================== Assertions on output (compile-level tests) ====================
    // Note: Full compilation tests would require temporary files, but we can test flag setup.

    @Test
    public void testCommandLineRunnerConstructor() {
        // Test that a non-null instance can be created with options
        String[] args = {"--js=test.js", "--js_output_file=test.js", "--compilation_level=SIMPLE"};
        try {
            CommandLineRunner.Options options = CommandLineRunner.createOptions(args);
            CommandLineRunner instance = new CommandLineRunner(options);
            assertNotNull(instance);
            // Additional state checks if applicable
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // ==================== Negative tests for null/empty in options setters ====================
    @Test(expected = NullPointerException.class)
    public void testOptionsWithNullJsFiles() {
        CommandLineRunner.Options opts = new CommandLineRunner.Options();
        opts.jsFiles = null;
        // Assuming setter or validation fails; if not, skip
    }

    // ==================== Test that --version and --help exit nicely ====================
    // These flags typically print and exit; we cannot test System.exit easily,
    // but FlagUsageException might be thrown instead.
    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testVersionFlagUsedWithOtherFlags() {
        // --version should be exclusive; using it with --js should trigger exception
        CommandLineRunner.main(new String[]{"--version", "--js=file.js"});
    }

    @Test(expected = CommandLineRunner.FlagUsageException.class)
    public void testHelpFlagUsedWithOtherFlags() {
        CommandLineRunner.main(new String[]{"--help", "--js=file.js"});
    }
}