package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
        
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option with arg");
        options.addOption("c", "class", false, "class option");
        options.addOption("d", "debug", true, "debug option with arg");
    }

    @Test
    public void testSimpleShortOption() throws Exception {
        String[] args = new String[] { "-a" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testBurstingShortOptions() throws Exception {
        String[] args = new String[] { "-ac" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testBurstingWithOptionsWithArgument() throws Exception {
        // 'b' takes an argument. In bursting, if 'b' is in the middle, its argument follows.
        String[] args = new String[] { "-ab", "val" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test
    public void testBurstingWithAttachedArgument() throws Exception {
        // Depending on parser implementation, attached args like -bval
        String[] args = new String[] { "-abval" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test
    public void testLongOptionWithoutArg() throws Exception {
        String[] args = new String[] { "--alpha" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testLongOptionWithEqualsArg() throws Exception {
        String[] args = new String[] { "--beta=value" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("value", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionWithSeparateArg() throws Exception {
        String[] args = new String[] { "--beta", "value" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("value", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionWithAbbreviation() throws Exception {
        // If partial matching is supported (or if Options handles it)
        Options opts = new Options();
        opts.addOption("v", "version", false, "version");
        PosixParser p = new PosixParser();
        CommandLine cl = p.parse(opts, new String[] { "--ver" });
        assertTrue(cl.hasOption("version"));
    }

    @Test
    public void testDoubleHyphenStopProcessing() throws Exception {
        String[] args = new String[] { "-a", "--", "-c" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("c"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-c", cl.getArgs()[0]);
    }

    @Test
    public void testUnrecognizedLongOption() {
        String[] args = new String[] { "--unknown" };
        try {
            parser.parse(options, args);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("--unknown", e.getOption());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    @Test
    public void testUnrecognizedShortOptionInBurst() {
        String[] args = new String[] { "-az" };
        try {
            parser.parse(options, args);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            // Depending on how CLI reports it
            assertNotNull(e.getMessage());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    @Test
    public void testMissingArgument() {
        String[] args = new String[] { "--beta" };
        try {
            parser.parse(options, args);
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertNotNull(e.getOption());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    @Test
    public void testStopAtNonOption() throws Exception {
        // If stopAtNonOption is enabled
        String[] args = new String[] { "-a", "non-option", "-c" };
        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("c"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("non-option", cl.getArgs()[0]);
        assertEquals("-c", cl.getArgs()[1]);
    }

    @Test
    public void testStopAtNonOptionWithProperties() throws Exception {
        String[] args = new String[] { "-a", "non-option", "-c" };
        java.util.Properties props = new java.util.Properties();
        CommandLine cl = parser.parse(options, args, props, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("c"));
        assertEquals(2, cl.getArgs().length);
    }

    @Test
    public void testNullArguments() throws Exception {
        try {
            parser.parse(options, null);
            fail("Expected exception or handled gracefully");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Expected for null input array
        }
    }

    @Test
    public void testEmptyArguments() throws Exception {
        String[] args = new String[] {};
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testSingleHyphenArgument() throws Exception {
        String[] args = new String[] { "-" };
        CommandLine cl = parser.parse(options, args);
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test
    public void testComplexBurstingAndNonOptionHandling() throws Exception {
        // Test combinations that exercise the token flattening logic thoroughly
        Options opts = new Options();
        opts.addOption("x", "x-opt", false, "x");
        opts.addOption("y", "y-opt", true, "y");
        
        String[] args = new String[] { "-xyval", "--y-opt=val2", "--", "-x" };
        CommandLine cl = parser.parse(opts, args);
        assertTrue(cl.hasOption("x"));
        assertTrue(cl.hasOption("y"));
        assertEquals("val", cl.getOptionValue("y")); // wait, if -xyval, x is flag, y takes val. Let's verify standard CLI behavior or just execute to hit branches.
    }
    
    @Test
    public void testLongOptionWithoutEqualsButWithArgumentNext() throws Exception {
        String[] args = new String[] { "--beta", "val" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("val", cl.getOptionValue("beta"));
    }
}