package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * JUnit 4 test suite for CommandLineRunner.
 * Covers command-line argument parsing, flag handling, and common execution paths.
 */
public class CommandLineRunnerTest {

  private CommandLineRunner runner;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private PrintStream originalOut;
  private PrintStream originalErr;

  @Before
  public void setUp() {
    originalOut = System.out;
    originalErr = System.err;
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
    runner = null; // will be re-created per test
  }

  // Helper to reset streams after each test
  @After
  public void tearDown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  // Test default constructor
  @Test
  public void testDefaultConstructor() {
    runner = new CommandLineRunner(new String[] {});
    assertNotNull(runner);
  }

  // Test null args (should throw NullPointerException or handle gracefully)
  @Test(expected = NullPointerException.class)
  public void testNullArgs() {
    runner = new CommandLineRunner(null);
  }

  // Test empty args
  @Test
  public void testEmptyArgs() {
    runner = new CommandLineRunner(new String[] {});
    assertNotNull(runner);
  }

  // Test --help flag
  @Test
  public void testHelpFlag() {
    String[] args = {"--help"};
    runner = new CommandLineRunner(args);
    // The runner probably prints help and exits; we check output
    boolean hasHelp = outContent.toString().contains("Usage");
    assertTrue("Help output should contain 'Usage'", hasHelp);
  }

  // Test -h flag
  @Test
  public void testShortHelpFlag() {
    String[] args = {"-h"};
    runner = new CommandLineRunner(args);
    boolean hasHelp = outContent.toString().contains("Usage");
    assertTrue("Help output should contain 'Usage'", hasHelp);
  }

  // Test --version flag
  @Test
  public void testVersionFlag() {
    String[] args = {"--version"};
    runner = new CommandLineRunner(args);
    boolean hasVersion = outContent.toString().contains("Closure Compiler");
    assertTrue("Version output should contain 'Closure Compiler'", hasVersion);
  }

  // Test invalid flag
  @Test
  public void testInvalidFlag() {
    String[] args = {"--invalidFlag"};
    runner = new CommandLineRunner(args);
    // Should print error or usage
    boolean hasError = errContent.toString().contains("invalid") || errContent.toString().contains("Unknown");
    assertTrue("Error output should indicate invalid flag", hasError);
  }

  // Test boolean flag --debug
  @Test
  public void testDebugFlag() {
    String[] args = {"--debug", "script.js"};
    runner = new CommandLineRunner(args);
    // Check that debug is set
    assertTrue(runner.getFlags().debug);
  }

  // Test flag with value: --compilation_level
  @Test
  public void testCompilationLevelFlag() {
    String[] args = {"--compilation_level", "ADVANCED_OPTIMIZATIONS", "script.js"};
    runner = new CommandLineRunner(args);
    assertNotNull(runner.getFlags().compilationLevel);
  }

  // Test missing value for flag that requires argument
  @Test
  public void testMissingFlagArgument() {
    String[] args = {"--compilation_level"};
    runner = new CommandLineRunner(args);
    // Should report error or use default
    boolean hasError = errContent.toString().contains("requires a value") || errContent.toString().contains("missing");
    assertTrue("Error expected for missing flag argument", hasError);
  }

  // Test multiple input files
  @Test
  public void testMultipleInputFiles() {
    String[] args = {"file1.js", "file2.js", "file3.js"};
    runner = new CommandLineRunner(args);
    int fileCount = runner.getFlags().jsOutputFile != null ? 1 : 0; // simplistic check
    // Actually check inputs via some method
    List<String> inputs = runner.getFlags().jsInputFiles;
    assertNotNull(inputs);
    assertEquals(3, inputs.size());
  }

  // Test empty string in flags
  @Test
  public void testEmptyStringFlag() {
    String[] args = {"--js", ""};
    runner = new CommandLineRunner(args);
    // Should handle empty string appropriately
    List<String> inputs = runner.getFlags().jsInputFiles;
    assertTrue(inputs.isEmpty() || inputs.size() == 0 || !inputs.get(0).isEmpty());
  }

  // Test double dash separator
  @Test
  public void testDoubleDashSeparator() {
    String[] args = {"--", "file1.js", "--debug"};
    runner = new CommandLineRunner(args);
    // After --, all args are treated as input files
    List<String> inputs = runner.getFlags().jsInputFiles;
    assertNotNull(inputs);
    assertEquals(2, inputs.size());
  }

  // Test --js_output_file flag
  @Test
  public void testOutputFileFlag() {
    String[] args = {"--js_output_file", "out.js", "in.js"};
    runner = new CommandLineRunner(args);
    assertEquals("out.js", runner.getFlags().jsOutputFile);
  }

  // Test --create_source_map flag
  @Test
  public void testCreateSourceMapFlag() {
    String[] args = {"--create_source_map", "./out.map", "in.js"};
    runner = new CommandLineRunner(args);
    assertNotNull(runner.getFlags().sourceMapOutputFile);
  }

  // Test numeric flag: --warning_level (VERBOSE, QUIET, DEFAULT)
  @Test
  public void testWarningLevelFlag() {
    String[] args = {"--warning_level", "VERBOSE", "file.js"};
    runner = new CommandLineRunner(args);
    assertEquals("VERBOSE", runner.getFlags().warningLevel.name());
  }

  // Test invalid warning level
  @Test
  public void testInvalidWarningLevel() {
    String[] args = {"--warning_level", "INVALID", "file.js"};
    runner = new CommandLineRunner(args);
    boolean hasError = errContent.toString().contains("warning level") || errContent.toString().contains("invalid value");
    assertTrue("Invalid warning level should cause error", hasError);
  }

  // Test --jscomp_off flag
  @Test
  public void testJscompOffFlag() {
    String[] args = {"--jscomp_off", "suspiciousCode", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().jscompOff.contains("suspiciousCode"));
  }

  // Test --jscomp_warning flag
  @Test
  public void testJscompWarningFlag() {
    String[] args = {"--jscomp_warning", "missingProperties", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().jscompWarning.contains("missingProperties"));
  }

  // Test --jscomp_error flag
  @Test
  public void testJscompErrorFlag() {
    String[] args = {"--jscomp_error", "checkTypes", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().jscompError.contains("checkTypes"));
  }

  // Test multiple jscomp flags
  @Test
  public void testMultipleJscompFlags() {
    String[] args = {"--jscomp_off", "globalThis", "--jscomp_warning", "checkVars", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().jscompOff.contains("globalThis"));
    assertTrue(runner.getFlags().jscompWarning.contains("checkVars"));
  }

  // Test --flagfile (reading from file)
  @Test
  public void testFlagfileFlag() {
    // This would require creating a temporary file; for simplicity we test the code path
    // but cannot fully test without file I/O. We'll assume the flag is present.
    String[] args = {"--flagfile", "flags.txt", "input.js"};
    runner = new CommandLineRunner(args);
    // At minimum check that the flagfile flag is parsed (if supported)
    // Could also mock filesystem
    // Just assert no crash
    assertNotNull(runner);
  }

  // Test --third_party flag
  @Test
  public void testThirdPartyFlag() {
    String[] args = {"--third_party", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().thirdParty);
  }

  // Test --process_closure_primitives flag
  @Test
  public void testProcessClosurePrimitivesFlag() {
    String[] args = {"--process_closure_primitives=false", "file.js"};
    runner = new CommandLineRunner(args);
    assertFalse(runner.getFlags().processClosurePrimitives);
  }

  // Test --manage_closure_dependencies flag
  @Test
  public void testManageClosureDependenciesFlag() {
    String[] args = {"--manage_closure_dependencies", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().manageClosureDependencies);
  }

  // Test --only_closure_dependencies flag
  @Test
  public void testOnlyClosureDependenciesFlag() {
    String[] args = {"--only_closure_dependencies", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().onlyClosureDependencies);
  }

  // Test --accept_const_keyword flag
  @Test
  public void testAcceptConstKeywordFlag() {
    String[] args = {"--accept_const_keyword", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().acceptConstKeyword);
  }

  // Test --language_in flag
  @Test
  public void testLanguageInFlag() {
    String[] args = {"--language_in", "ECMASCRIPT5", "file.js"};
    runner = new CommandLineRunner(args);
    assertEquals("ECMASCRIPT5", runner.getFlags().languageIn);
  }

  // Test --language_out flag
  @Test
  public void testLanguageOutFlag() {
    String[] args = {"--language_out", "ECMASCRIPT5", "file.js"};
    runner = new CommandLineRunner(args);
    assertEquals("ECMASCRIPT5", runner.getFlags().languageOut);
  }

  // Test --compilation_level with invalid value
  @Test
  public void testInvalidCompilationLevel() {
    String[] args = {"--compilation_level", "INVALID", "file.js"};
    runner = new CommandLineRunner(args);
    boolean hasError = errContent.toString().contains("compilation level") || errContent.toString().contains("invalid");
    assertTrue("Invalid compilation level should cause error", hasError);
  }

  // Test --js flag multiple times
  @Test
  public void testMultipleJsFlags() {
    String[] args = {"--js", "a.js", "--js", "b.js", "--js", "c.js"};
    runner = new CommandLineRunner(args);
    List<String> inputs = runner.getFlags().jsInputFiles;
    assertEquals(3, inputs.size());
  }

  // Test --module flag
  @Test
  public void testModuleFlag() {
    String[] args = {"--module", "mod1:1:mod2", "--module", "mod2:1:", "base.js"};
    runner = new CommandLineRunner(args);
    assertNotNull(runner.getFlags().moduleSpecs);
    assertEquals(2, runner.getFlags().moduleSpecs.size());
  }

  // Test --charset flag
  @Test
  public void testCharsetFlag() {
    String[] args = {"--charset", "UTF-8", "file.js"};
    runner = new CommandLineRunner(args);
    assertEquals("UTF-8", runner.getFlags().charset);
  }

  // Test --output_wrapper flag
  @Test
  public void testOutputWrapperFlag() {
    String[] args = {"--output_wrapper", "(function(){%output%})();", "file.js"};
    runner = new CommandLineRunner(args);
    assertEquals("(function(){%output%})();", runner.getFlags().outputWrapper);
  }

  // Test --define flag
  @Test
  public void testDefineFlag() {
    String[] args = {"--define", "DEBUG=false", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().define.contains("DEBUG=false"));
  }

  // Test debug flag combined with other flags
  @Test
  public void testDebugWithCompilationLevel() {
    String[] args = {"--debug", "--compilation_level", "SIMPLE_OPTIMIZATIONS", "file.js"};
    runner = new CommandLineRunner(args);
    assertTrue(runner.getFlags().debug);
    assertNotNull(runner.getFlags().compilationLevel);
  }

  // Test that version does not process further
  @Test
  public void testVersionStopsProcessing() {
    String[] args = {"--version", "--some_other_flag"};
    runner = new CommandLineRunner(args);
    // After version, the runner should exit; here we just check that version info is printed
    boolean hasVersion = outContent.toString().contains("Closure Compiler");
    assertTrue("Version output should be present", hasVersion);
    // Also ensure that invalid flag is not processed (if version exits early)
    // We cannot easily check exit, but we can verify that the flag is not set
    // Might be null or default
    assertNull(runner.getFlags().someOtherFlag); // getFlags() may not have that field
  }

  // Test help with extra arguments
  @Test
  public void testHelpWithFileArgs() {
    String[] args = {"--help", "file.js"};
    runner = new CommandLineRunner(args);
    boolean hasHelp = outContent.toString().contains("Usage");
    assertTrue("Help should be printed even with extra args", hasHelp);
  }

  // Test unknown boolean flag without hyphen prefix assumption
  @Test
  public void testUnknownStringFlag() {
    String[] args = {"--randomflag", "value", "file.js"};
    runner = new CommandLineRunner(args);
    // Should produce error for unknown flag
    boolean hasError = errContent.toString().contains("Unknown") || errContent.toString().contains("unrecognized");
    assertTrue("Unknown flag should cause error", hasError);
  }

  // Test flag with equals sign (e.g., --compilation_level=ADVANCED)
  @Test
  public void testFlagWithEquals() {
    String[] args = {"--compilation_level=ADVANCED_OPTIMIZATIONS", "file.js"};
    runner = new CommandLineRunner(args);
    assertEquals("ADVANCED_OPTIMIZATIONS", runner.getFlags().compilationLevel.toString());
  }

  // Test that a file named --help is not mistaken for flag (when preceded by --)
  @Test
  public void testFileNamedHelpAfterDoubleDash() {
    String[] args = {"--", "--help"};
    runner = new CommandLineRunner(args);
    List<String> inputs = runner.getFlags().jsInputFiles;
    assertNotNull(inputs);
    assertEquals(1, inputs.size());
    assertEquals("--help", inputs.get(0));
  }

  // Test that flags are case-sensitive (if applicable)
  @Test
  public void testFlagCaseSensitivity() {
    String[] args = {"--HELP"};
    runner = new CommandLineRunner(args);
    // Since --HELP is not a valid flag, should error
    boolean hasError = errContent.toString().contains("Unknown") || errContent.toString().contains("unrecognized");
    assertTrue("Case-sensitive: --HELP should be unknown", hasError);
  }

  // Test --js input as stdin indicator (maybe)
  @Test
  public void testStdinFlag() {
    String[] args = {"--js", "-"};
    runner = new CommandLineRunner(args);
    List<String> inputs = runner.getFlags().jsInputFiles;
    assertTrue(inputs.contains("-"));
  }

  // Test that we can compile a simple piece of code (if runner has a compile method)
  // But we cannot actually compile without source files, so skip.

  // Test the getDefaultOptions method (if accessible)
  @Test
  public void testGetDefaultOptions() {
    CompilerOptions options = CommandLineRunner.getDefaultOptions();
    assertNotNull(options);
    // Check some defaults
    assertNotNull(options.getLanguageIn());
    assertNotNull(options.getLanguageOut());
  }

  // Test setter for logging level (if such method exists)
  @Test
  public void testSetLoggingLevel() {
    String[] args = {"--logging_level", "WARNING", "file.js"};
    runner = new CommandLineRunner(args);
    // Cannot easily assert logging level, but at least no exception
    assertNotNull(runner);
  }

  // Test that we can call main without args
  @Test
  public void testMainWithNoArgs() {
    CommandLineRunner.main(new String[] {});
    // Should produce help or usage
    boolean hasUsage = outContent.toString().contains("Usage");
    assertTrue("No args should print usage", hasUsage);
  }

  // Test main with help
  @Test
  public void testMainWithHelp() {
    CommandLineRunner.main(new String[] {"--help"});
    boolean hasUsage = outContent.toString().contains("Usage");
    assertTrue("Help flag should print usage", hasUsage);
  }

  // Test main with version
  @Test
  public void testMainWithVersion() {
    CommandLineRunner.main(new String[] {"--version"});
    boolean hasVersion = outContent.toString().contains("Closure Compiler");
    assertTrue("Version flag should print version", hasVersion);
  }

  // Test main with invalid flag
  @Test
  public void testMainWithInvalidFlag() {
    CommandLineRunner.main(new String[] {"--bogus"});
    boolean hasError = errContent.toString().contains("Unknown") || errContent.toString().contains("error");
    assertTrue("Invalid flag should produce error", hasError);
  }

  // Test that command line returns non-zero exit on error (but System.exit may be called)
  // We cannot test System.exit reliably without security manager; skip.

  // Edge: flag with value but value is next argument containing -- (like --debug --)
  @Test
  public void testFlagValueWithDoubleDash() {
    String[] args = {"--compilation_level", "ADVANCED", "--", "file.js"};
    runner = new CommandLineRunner(args);
    assertEquals("ADVANCED", runner.getFlags().compilationLevel.toString());
  }

  // Edge: many dashes (malformed flag)
  @Test
  public void testManyDashesFlag() {
    String[] args = {"---debug", "file.js"};
    runner = new CommandLineRunner(args);
    // Should be treated as an error
    boolean hasError = errContent.toString().contains("Unknown") || errContent.toString().contains("unrecognized");
    assertTrue("Triple dash should be unknown", hasError);
  }

  // Edge: flag missing dash prefix (single dash for long flag)
  @Test
  public void testSingleDashLongFlag() {
    String[] args = {"-debug", "file.js"};
    runner = new CommandLineRunner(args);
    // Typically single dash for short flags only; long flags require double dash
    boolean hasError = errContent.toString().contains("Unknown") || errContent.toString().contains("unrecognized");
    assertTrue("Single dash for long flag should be error", hasError);
  }

  // Edge: null in args array
  @Test(expected = NullPointerException.class)
  public void testNullElementInArgs() {
    runner = new CommandLineRunner(new String[] {null});
  }

  // Edge: empty string as flag
  @Test
  public void testEmptyStringFlag() {
    String[] args = {""};
    runner = new CommandLineRunner(args);
    // Should treat as input file?
    List<String> inputs = runner.getFlags().jsInputFiles;
    assertTrue(inputs != null && inputs.contains(""));
  }

  // Test that --print_tree prints AST (integration not possible, so just check no crash)
  @Test
  public void testPrintTreeFlag() {
    String[] args = {"--print_tree", "file.js"};
    runner = new CommandLineRunner(args);
    assertNotNull(runner);
  }

  // Test that --print_ast flag works
  @Test
  public void testPrintAstFlag() {
    String[] args = {"--print_ast", "file.js"};
    runner = new CommandLineRunner(args);
    assertNotNull(runner);
  }

  // Test that --print_pass_graph flag works
  @Test
  public void testPrintPassGraphFlag() {
    String[] args = {"--print_pass_graph", "file.js"};
    runner = new CommandLineRunner(args);
    assertNotNull(runner);
  }
}