package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class CommandLineRunnerTest {

    @Test
    public void testCommandLineRunnerCreationAndFlags() {
        String[] args = new String[] {
            "--js", "test.js"
        };
        
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(errStream);
        
        CommandLineRunner runner = new CommandLineRunner(args, System.out, printStream);
        assertNotNull(runner);
    }

    @Test
    public void testCreateOptions() {
        String[] args = new String[] {
            "--js", "test.js",
            "--jscomp_error", "visibility"
        };
        
        CommandLineRunner runner = new CommandLineRunner(args);
        CompilerOptions options = runner.createOptions();
        assertNotNull(options);
    }

    @Test
    public void testCreateCompiler() {
        String[] args = new String[] {
            "--js", "test.js"
        };
        
        CommandLineRunner runner = new CommandLineRunner(args);
        Compiler compiler = runner.createCompiler();
        assertNotNull(compiler);
    }

    @Test
    public void testShouldRunCompiler() {
        String[] args = new String[] {
            "--js", "test.js"
        };
        
        CommandLineRunner runner = new CommandLineRunner(args);
        assertTrue(runner.shouldRunCompiler());
    }

    @Test
    public void testVersionFlag() {
        String[] args = new String[] {
            "--version"
        };
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        CommandLineRunner runner = new CommandLineRunner(args, new PrintStream(outStream), System.err);
        
        // Depending on implementation, --version might prevent running compiler
        assertFalse(runner.shouldRunCompiler());
    }
}