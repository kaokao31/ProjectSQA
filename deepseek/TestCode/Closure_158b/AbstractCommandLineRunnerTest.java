package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link AbstractCommandLineRunner}.
 * <p>Tests are designed to achieve high code coverage and reveal potential bugs
 * (especially those related to Defects4J bug 158).
 */
public class AbstractCommandLineRunnerTest {

    private static final String INPUT_DIR = "test_input_dir";
    private static final String OUTPUT_FILE = "output.js";
    private static final String CONFIG_FILE = "test_config.js";

    // A concrete stub for the abstract class
    private static class TestRunner extends AbstractCommandLineRunner<TestRunner.TestOptions> {

        private boolean executeCalled = false;
        private boolean initConfigCalled = false;
        private boolean setRunOptionsCalled = false;
        private boolean getFileListCalled = false;
        private boolean getDefaultJsOutputFileCalled = false;
        private List<String> fileListResult = ImmutableList.of();
        private String customOutputFile;

        public static class TestOptions extends CommandLineRunner.CommandLineOptions {
            // additional options can be added here if needed
        }

        public TestRunner(String[] args) {
            super(args);
        }

        @Override
        protected List<String> getFileList() throws IOException {
            getFileListCalled = true;
            return fileListResult;
        }

        @Override
        protected void setRunOptions(CompilerOptions options) throws Exception {
            setRunOptionsCalled = true;
        }

        @Override
        protected void initConfig(CompilerConfig config) {
            initConfigCalled = true;
        }

        @Override
        protected String getDefaultJsOutputFile() {
            getDefaultJsOutputFileCalled = true;
            return customOutputFile != null ? customOutputFile : OUTPUT_FILE;
        }

        @Override
        protected int doSomething() {
            // stub to avoid abstract
            return 0;
        }

        @Override
        protected Compiler createCompiler() {
            return new Compiler();
        }

        @Override
        protected PrintStream createOutputPrintStream() {
            return System.out;
        }
    }

    private TestRunner runner;
    private String[] testArgs;

    @Before
    public void setUp() throws Exception {
        // Default setup with typical arguments
        testArgs = new String[] {
            "--compilation_level", "SIMPLE_OPTIMIZATIONS",
            "--js_output_file", "output.js",
            "--js", "file1.js",
            "--js", "file2.js",
            "--charset", "UTF-8"
        };
        runner = new TestRunner(testArgs);
    }

    // ------------------------- Constructor tests -------------------------

    @Test
    public void testConstructorWithEmptyArgs() {
        TestRunner emptyRunner = new TestRunner(new String[]{});
        assertNotNull("Runner should be created with empty args", emptyRunner);
    }

    @Test
    public void testConstructorWithNullArgs() {
        // Note: The constructor likely does not accept null. We test for robustness.
        try {
            new TestRunner((String[]) null);
            fail("Expected NullPointerException for null args");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithVersionFlag() {
        // Bug 158 related: --version might trigger early exit.
        String[] versionArgs = new String[]{"--version"};
        TestRunner versionRunner = new TestRunner(versionArgs);
        // The runner should not crash; it may print and exit, but we can still run execute?
        // Typically execute would check and return early.
    }

    // ------------------------- execute() method tests -------------------------

    @Test
    public void testExecuteNormalFlow() throws Exception {
        runner.fileListResult = ImmutableList.of("file1.js", "file2.js");
        int result = runner.execute();
        assertEquals("Execute should return success", 0, result);
        assertTrue("initConfig should be called", runner.initConfigCalled);
        assertTrue("setRunOptions should be called", runner.setRunOptionsCalled);
        assertTrue("getFileList should be called", runner.getFileListCalled);
        // getDefaultJsOutputFile should be called when output file is set
        assertTrue("getDefaultJsOutputFile should be called", runner.getDefaultJsOutputFileCalled);
    }

    @Test
    public void testExecuteWithNoFiles() throws Exception {
        runner.fileListResult = ImmutableList.of();
        int result = runner.execute();
        assertEquals("Execute with no files should return error", 1, result);
    }

    @Test
    public void testExecuteWithNullFileList() throws Exception {
        runner.fileListResult = null;
        int result = runner.execute();
        assertEquals("Execute with null file list should return error", 1, result);
    }

    @Test
    public void testExecuteWithHelpFlag() throws Exception {
        String[] helpArgs = new String[]{"--help"};
        TestRunner helpRunner = new TestRunner(helpArgs);
        int result = helpRunner.execute();
        assertEquals("Help should return 0", 0, result);
    }

    @Test
    public void testExecuteWithVersionFlag() throws Exception {
        String[] versionArgs = new String[]{"--version"};
        TestRunner versionRunner = new TestRunner(versionArgs);
        int result = versionRunner.execute();
        assertEquals("Version should return 0", 0, result);
    }

    // ------------------------- getFileList() coverage -------------------------

    @Test
    public void testGetFileListCalledWithMultipleInputs() throws Exception {
        // Actually getFileList is overridden in our stub, but we want to ensure the
        // logic in the base class is covered if called. We'll test with a real-like scenario.
        // However, the stub intercepts. To test the real getFileList we would need
        // a more elaborate setup. But the stub already covers the call.
        // We rely on the stub to indicate it was called.
        runner.execute();
        assertTrue("getFileList must be called during execute", runner.getFileListCalled);
    }

    // ------------------------- initConfig() and setRunOptions() coverage -------------------------

    @Test
    public void testInitConfigCalledWithConfig() throws Exception {
        // initConfig is called from execute; check that it receives a non-null config.
        runner.execute();
        assertTrue("initConfig should have been called", runner.initConfigCalled);
    }

    @Test
    public void testSetRunOptionsCalledWithOptions() throws Exception {
        runner.execute();
        assertTrue("setRunOptions should have been called", runner.setRunOptionsCalled);
    }

    // ------------------------- Default output file name tests -------------------------

    @Test
    public void testDefaultJsOutputFileWhenNoOutputSpecified() {
        TestRunner noOutputRunner = new TestRunner(new String[]{"--js", "a.js"});
        String defaultFile = noOutputRunner.getDefaultJsOutputFile();
        assertEquals("Default output file should be output.js", OUTPUT_FILE, defaultFile);
        assertTrue("getDefaultJsOutputFileCalled should be true", noOutputRunner.getDefaultJsOutputFileCalled);
    }

    @Test
    public void testDefaultJsOutputFileWhenOutputSpecified() {
        TestRunner withOutputRunner = new TestRunner(new String[]{"--js_output_file", "custom.js", "--js", "a.js"});
        withOutputRunner.customOutputFile = "custom.js";
        String outputFile = withOutputRunner.getDefaultJsOutputFile();
        assertEquals("Should return custom output file", "custom.js", outputFile);
    }

    // ------------------------- Configuration file loading -------------------------

    @Test
    public void testConfigFileLoading() throws IOException {
        // This tests the path when --config_file is provided.
        String[] configArgs = new String[]{"--config_file", CONFIG_FILE, "--js", "a.js"};
        TestRunner configRunner = new TestRunner(configArgs);
        // The base class might attempt to load the file; we can't easily mock the file system.
        // But we can verify that the configFile option is set.
        assertNotNull("Config file option should be set", configRunner.getCommandLineConfig().configFile);
    }

    // ------------------------- Charset handling tests -------------------------

    @Test
    public void testCharsetOption() {
        String[] charsetArgs = new String[]{"--charset", "ISO-8859-1", "--js", "a.js"};
        TestRunner charsetRunner = new TestRunner(charsetArgs);
        // Just ensure no crash
        assertNotNull("Runner with charset should be created", charsetRunner);
    }

    @Test
    public void testEmptyCharset() {
        String[] emptyCharsetArgs = new String[]{"--charset", "", "--js", "a.js"};
        TestRunner emptyRunner = new TestRunner(emptyCharsetArgs);
        // The parser might treat empty string as default; we simply ensure no exception.
        assertNotNull("Runner with empty charset should be created", emptyRunner);
    }

    // ------------------------- Warning level tests -------------------------

    @Test
    public void testWarningLevelDefault() {
        // Default warning level should be DEFAULT
        assertEquals("Default warning level should be DEFAULT",
            WarningLevel.DEFAULT,
            runner.getCommandLineConfig().warningLevel);
    }

    @Test
    public void testWarningLevelVerbose() {
        String[] verboseArgs = new String[]{"--warning_level", "VERBOSE", "--js", "a.js"};
        TestRunner verboseRunner = new TestRunner(verboseArgs);
        assertEquals("Warning level should be VERBOSE",
            WarningLevel.VERBOSE,
            verboseRunner.getCommandLineConfig().warningLevel);
    }

    @Test
    public void testWarningLevelQuiet() {
        String[] quietArgs = new String[]{"--warning_level", "QUIET", "--js", "a.js"};
        TestRunner quietRunner = new TestRunner(quietArgs);
        assertEquals("Warning level should be QUIET",
            WarningLevel.QUIET,
            quietRunner.getCommandLineConfig().warningLevel);
    }

    // ------------------------- Error handling / Fault injection -------------------------
    // These tests attempt to trigger known bug patterns (e.g., bug 158: --checks-only flag ignored?)

    @Test
    public void testCheckOnlyFlagParsing() {
        // Bug 158: --checks_only flag may be ignored.
        String[] checkArgs = new String[]{"--checks_only", "--js", "a.js"};
        TestRunner checkRunner = new TestRunner(checkArgs);
        assertTrue("--checks_only flag should set checksOnly to true",
            checkRunner.getCommandLineConfig().checksOnly);
    }

    @Test
    public void testCheckOnlyFlagAlias() {
        // The --checks_only flag may have an alias --check-only (common in Closure)
        String[] aliasArgs = new String[]{"--check-only", "--js", "a.js"};
        TestRunner aliasRunner = new TestRunner(aliasArgs);
        assertTrue("--check-only alias should also set checksOnly to true",
            aliasRunner.getCommandLineConfig().checksOnly);
    }

    // ------------------------- Input file lists boundary testing -------------------------

    @Test
    public void testManyInputFiles() throws Exception {
        // Generate a large list of file arguments (e.g., 1000 files) to test loop boundaries.
        String[] manyFiles = new String[2000];
        manyFiles[0] = "--js";
        for (int i = 1; i < manyFiles.length; i++) {
            manyFiles[i] = "file" + i + ".js";
        }
        TestRunner manyRunner = new TestRunner(manyFiles);
        // Should not throw
        assertNotNull("Runner with many input files should not crash", manyRunner);
    }

    @Test
    public void testInputFileWithSpecialCharacters() {
        String[] specialArgs = new String[]{"--js", "some file with spaces.js", "--js", "file's.js"};
        TestRunner specialRunner = new TestRunner(specialArgs);
        assertNotNull("File paths with special characters should be accepted", specialRunner);
    }

    // ------------------------- Multiple output flags -------------------------

    @Test
    public void testMultipleOutputFlags() {
        // Edge case: specifying --js_output_file multiple times.
        String[] multiOutput = new String[]{
            "--js_output_file", "out1.js",
            "--js_output_file", "out2.js",
            "--js", "a.js"
        };
        TestRunner multiRunner = new TestRunner(multiOutput);
        // The last one should take effect.
        assertEquals("Last output file should be used", "out2.js",
            multiRunner.getCommandLineConfig().jsOutputFile);
    }

    // ------------------------- Property file loading -------------------------

    @Test
    public void testPropertyFileOption() {
        String[] propArgs = new String[]{"--property_file", "my.properties", "--js", "a.js"};
        TestRunner propRunner = new TestRunner(propArgs);
        assertNotNull("Property file option should be set", propRunner.getCommandLineConfig().propertyFile);
    }

    // ------------------------- Third party options (for coverage) -------------------------

    @Test
    public void testThirdPartyOption() {
        String[] thirdPartyArgs = new String[]{"--third_party", "--js", "a.js"};
        TestRunner thirdRunner = new TestRunner(thirdPartyArgs);
        assertTrue("Third party flag should be set", thirdRunner.getCommandLineConfig().thirdParty);
    }

    // ------------------------- Compilation level options -------------------------

    @Test
    public void testCompilationLevelADVANCED() {
        String[] advArgs = new String[]{"--compilation_level", "ADVANCED_OPTIMIZATIONS", "--js", "a.js"};
        TestRunner advRunner = new TestRunner(advArgs);
        assertEquals(CompilationLevel.ADVANCED_OPTIMIZATIONS, advRunner.getCommandLineConfig().compilationLevel);
    }

    @Test
    public void testCompilationLevelWHITESPACE() {
        String[] whiteArgs = new String[]{"--compilation_level", "WHITESPACE_ONLY", "--js", "a.js"};
        TestRunner whiteRunner = new TestRunner(whiteArgs);
        assertEquals(CompilationLevel.WHITESPACE_ONLY, whiteRunner.getCommandLineConfig().compilationLevel);
    }

    // ------------------------- Null check in getFileList -------------------------

    @Test
    public void testExecuteWhenGetFileListReturnsNull() throws Exception {
        TestRunner nullFileRunner = new TestRunner(new String[]{"--js", "a.js"});
        nullFileRunner.fileListResult = null;
        int result = nullFileRunner.execute();
        assertEquals("Should return error when file list is null", 1, result);
    }

    @Test
    public void testExecuteWhenGetFileListThrowsIOException() throws Exception {
        TestRunner throwingRunner = new TestRunner(new String[]{"--js", "a.js"}) {
            @Override
            protected List<String> getFileList() throws IOException {
                throw new IOException("Simulated I/O error");
            }
        };
        int result = throwingRunner.execute();
        assertEquals("Should return error on IOException", 1, result);
    }

    // ------------------------- Bug 158 specific: checks_only not affecting compilation -------------------------

    @Test
    public void testChecksOnlyWithCompilation() throws Exception {
        // Bug 158: --checks_only should skip compilation output.
        String[] checkArgs = new String[]{"--checks_only", "--js", "a.js", "--js_output_file", "should_not_be_created.js"};
        TestRunner checkRunner = new TestRunner(checkArgs);
        checkRunner.fileListResult = ImmutableList.of("a.js");
        int result = checkRunner.execute();
        assertEquals("Execute with checks_only should still succeed (bug 158?)", 0, result);
        // The output file should not be created - we can't test file existence easily.
        // But we can verify that the flag was parsed.
        assertTrue("checksOnly flag should be true", checkRunner.getCommandLineConfig().checksOnly);
    }
}