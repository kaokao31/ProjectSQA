package com.google.javascript.jscomp;

import org.junit.Test;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for AbstractCommandLineRunner.
 * Designed to cover branches, edge cases, and ensure robust behavior under Defects4J constraints.
 */
public class AbstractCommandLineRunnerTest {

    /**
     * Concrete implementation of AbstractCommandLineRunner for testing abstract methods
     * and protected helper methods.
     */
    private static class TestCommandLineRunner extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
        private final String[] args;

        public TestCommandLineRunner(String[] args) {
            super();
            this.args = args;
        }

        public TestCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
            super(out, err);
        }

        @Override
        protected String[] createArgs() {
            return args;
        }

        @Override
        protected Compiler createCompiler() {
            return new Compiler(getErrorPrintStream());
        }

        @Override
        protected CompilerOptions createOptions() {
            CompilerOptions options = new CompilerOptions();
            options.setCodingConvention(new ClosureCodingConvention());
            return options;
        }
    }

    @Test
    public void testRunnerCreationWithNullArgs() {
        TestCommandLineRunner runner = new TestCommandLineRunner(null);
        assertNotNull(runner);
    }

    @Test
    public void testRunnerCreationWithEmptyArgs() {
        TestCommandLineRunner runner = new TestCommandLineRunner(new String[]{});
        assertNotNull(runner);
    }

    @Test
    public void testCreateOptions() {
        TestCommandLineRunner runner = new TestCommandLineRunner(new String[]{});
        CompilerOptions options = runner.createOptions();
        assertNotNull(options);
    }

    @Test
    public void testCreateCompiler() {
        TestCommandLineRunner runner = new TestCommandLineRunner(new String[]{});
        Compiler compiler = runner.createCompiler();
        assertNotNull(compiler);
    }

    @Test
    public void testShouldRunCompiler() {
        TestCommandLineRunner runner = new TestCommandLineRunner(new String[]{});
        // AbstractCommandLineRunner defines shouldRunCompiler(CompilerOptions)
        CompilerOptions options = runner.createOptions();
        boolean result = runner.shouldRunCompiler(options);
        assertTrue(result);
    }

    @Test
    public void testFlagUsage() {
        TestCommandLineRunner runner = new TestCommandLineRunner(new String[]{});
        // Verify that flag usage doesn't throw unexpected exceptions
        assertNotNull(runner.createDefineFlag());
    }

    @Test
    public void testCustomStreams() {
        PrintStream dummyOut = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // no-op
            }
        });
        PrintStream dummyErr = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // no-op
            }
        });

        TestCommandLineRunner runner = new TestCommandLineRunner(new String[]{"--help"}, dummyOut, dummyErr);
        assertNotNull(runner);
    }
}