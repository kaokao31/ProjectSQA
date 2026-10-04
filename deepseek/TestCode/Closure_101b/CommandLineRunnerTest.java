package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class CommandLineRunnerTest {

  private static final String RUNNER_CLASS =
      "com.google.javascript.jscomp.CommandLineRunner";

  private ByteArrayOutputStream outBytes;
  private ByteArrayOutputStream errBytes;
  private PrintStream out;
  private PrintStream err;

  @Before
  public void setUp() {
    outBytes = new ByteArrayOutputStream();
    errBytes = new ByteArrayOutputStream();
    out = new PrintStream(outBytes, true);
    err = new PrintStream(errBytes, true);
  }

  private Object newRunner(String... args) throws Exception {
    Class<?> cls = Class.forName(RUNNER_CLASS);
    Constructor<?> ctor = cls.getDeclaredConstructor(
        String[].class, PrintStream.class, PrintStream.class);
    ctor.setAccessible(true);
    return ctor.newInstance(new Object[] {args, out, err});
  }

  private static Method findMethod(Class<?> cls, String name,
      Class<?>... parameterTypes) {
    try {
      return cls.getMethod(name, parameterTypes);
    } catch (NoSuchMethodException e) {
      for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
        try {
          Method m = c.getDeclaredMethod(name, parameterTypes);
          m.setAccessible(true);
          return m;
        } catch (NoSuchMethodException ignored) {
          // Continue searching up the hierarchy.
        }
      }
    }
    throw new IllegalArgumentException("No such method: " + name);
  }

  private static Object invoke(Object target, String name, Object... args)
      throws Exception {
    Class<?>[] parameterTypes = new Class<?>[args.length];
    for (int i = 0; i < args.length; i++) {
      parameterTypes[i] = args[i].getClass();
    }
    Method method = findMethod(target.getClass(), name, parameterTypes);
    return method.invoke(target, args);
  }

  private static boolean shouldRunCompiler(Object runner) throws Exception {
    return (Boolean) invoke(runner, "shouldRunCompiler");
  }

  private static int exitCode(Object runner) throws Exception {
    return (Integer) invoke(runner, "getExitCode");
  }

  private String outText() {
    return outBytes.toString();
  }

  private String errText() {
    return errBytes.toString();
  }

  private String allOutput() {
    return outText() + errText();
  }

  @Test
  public void testHelpFlag() throws Exception {
    Object runner = newRunner("--help");
    assertFalse(shouldRunCompiler(runner));
    assertEquals(0, exitCode(runner));

    String output = allOutput();
    assertTrue("Unexpected help output: " + output,
        output.contains("--") || output.contains("Usage") || output.contains("help"));
  }

  @Test
  public void testVersionFlag() throws Exception {
    Object runner = newRunner("--version");
    assertFalse(shouldRunCompiler(runner));
    assertEquals(0, exitCode(runner));

    String output = allOutput();
    assertTrue("Unexpected version output: " + output,
        output.contains("Closure") || output.contains("version") || output.contains("Version"));
  }

  @Test
  public void testUnknownFlagIsRejected() throws Exception {
    Object runner = newRunner("--no_such_cli_flag");
    assertFalse(shouldRunCompiler(runner));
    assertTrue(exitCode(runner) != 0);
    assertTrue("Expected unknown flag message, got: " + allOutput(),
        allOutput().contains("no_such_cli_flag"));
  }

  @Test
  public void testSingleDashFileArgument() throws Exception {
    Object runner = newRunner("input.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testJsFlagWithEqualsSyntax() throws Exception {
    Object runner = newRunner("--js=input.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testJsFlagWithSeparateSyntax() throws Exception {
    Object runner = newRunner("--js", "input.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testMultipleJsFiles() throws Exception {
    Object runner = newRunner("--js", "a.js", "--js", "b.js", "--js", "c.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testExternsFlag() throws Exception {
    Object runner = newRunner("--js", "input.js", "--externs", "extern.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testBooleanFlagWithoutValue() throws Exception {
    Object runner = newRunner("--debug", "--js", "input.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testBooleanFlagWithExplicitValue() throws Exception {
    Object runner = newRunner("--debug=true", "--js", "input.js");
    assertTrue(shouldRunCompiler(runner));

    runner = newRunner("--debug=false", "--js", "input.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testOutputFileFlags() throws Exception {
    Object runner = newRunner(
        "--js", "input.js",
        "--js_output_file", "output.js",
        "--create_source_map", "output.map");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testOutputWrapper() throws Exception {
    Object runner = newRunner(
        "--js", "input.js",
        "--output_wrapper", "(function(){%output%})();");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testCompilationLevelValidWithSeparateValue() throws Exception {
    Object runner = newRunner(
        "--compilation_level", "ADVANCED_OPTIMIZATIONS",
        "--js", "input.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testCompilationLevelValidWithEqualsSyntax() throws Exception {
    Object runner = newRunner(
        "--compilation_level=SIMPLE_OPTIMIZATIONS", "--js", "input.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testCompilationLevelInvalid() throws Exception {
    Object runner = newRunner(
        "--compilation_level=NOT_A_LEVEL", "--js", "input.js");
    assertFalse(shouldRunCompiler(runner));
    assertTrue(exitCode(runner) != 0);

    String err = errText();
    assertTrue("Expected invalid level message, got: " + err,
        err.contains("NOT_A_LEVEL") || err.toLowerCase().contains("level"));
  }

  @Test
  public void testWarningLevelValid() throws Exception {
    Object runner = newRunner("--warning_level=VERBOSE", "--js", "input.js");
    assertTrue(shouldRunCompiler(runner));
  }

  @Test
  public void testMissingValueForNonBooleanFlag() throws Exception {
    Object runner = newRunner("--js");
    assertFalse(shouldRunCompiler(runner));
    assertTrue(exitCode(runner) != 0);
    assertTrue("Expected missing value message, got: " + allOutput(),
        allOutput().contains("--js") || allOutput().contains("js"));
  }

  @Test
  public void testMissingValueForOutputFile() throws Exception {
    Object runner = newRunner("--js", "input.js", "--js_output_file");
    assertFalse(shouldRunCompiler(runner));
    assertTrue(exitCode(runner) != 0);
  }
}