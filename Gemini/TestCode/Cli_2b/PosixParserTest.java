package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

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
        options.addOption("c", "counter", true, "counter option");
        options.addOption("d", "debug", false, "debug option");
        options.addOption("e", "edge", false, "edge option");
    }

    @Test
    public void testFlattenNullArguments() {
        assertNull(parser.flatten(null, null, true));
        assertNull(parser.flatten(null, new String[0], true));
    }

    @Test
    public void testFlattenEmptyArguments() {
        String[] args = new String[0];
        String[] flattened = parser.flatten(options, args, true);
        assertNotNull(flattened);
        assertEquals(0, flattened.length);
    }

    @Test
    public void testSimpleLongOption() throws Exception {
        String[] args = new String[] { "--alpha" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("alpha"));
    }

    @Test
    public void testLongOptionWithEquals() throws Exception {
        String[] args = new String[] { "--beta=value" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testLongOptionWithEqualsButNoArg() throws Exception {
        // Option 'a' does not take an argument, let's see how PosixParser handles --alpha=value
        String[] args = new String[] { "--alpha=value" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        // Depending on parser implementation, value might be treated as leftover or part of token
    }

    @Test
    public void testLongOptionWithInvalidPrefix() {
        String[] args = new String[] { "--invalid-long-option" };
        try {
            parser.parse(options, args);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("--invalid-long-option", e.getOption());
        } catch (ParseException e) {
            fail("Expected UnrecognizedOptionException, got " + e.getClass());
        }
    }

    @Test
    public void testShortOptionNoArg() throws Exception {
        String[] args = new String[] { "-a", "-d" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("d"));
    }

    @Test
    public void testShortOptionGroup() throws Exception {
        // -ad should burst into -a and -d if neither takes args
        String[] args = new String[] { "-ad" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("d"));
    }

    @Test
    public void testShortOptionGroupWithArgAtEnd() throws Exception {
        // -abvalue -> '-a' (no arg), '-b' (takes arg, 'value' is its arg)
        String[] args = new String[] { "-abvalue" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testShortOptionWithAttachedArgWithoutEquals() throws Exception {
        String[] args = new String[] { "-bvalue" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testShortOptionWithSeparateArg() throws Exception {
        String[] args = new String[] { "-b", "value" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test
    public void testBurstingMultipleShortOptionsWhereMiddleTakesArg() throws Exception {
        // -abc -> '-a' (no arg), '-b' (takes arg, 'c' becomes the argument for 'b')
        String[] args = new String[] { "-abc" };
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("c", cmd.getOptionValue("b"));
        assertFalse(cmd.hasOption("c"));
    }

    @Test
    public void testStopAtNonOption() throws Exception {
        // When stopAtNonOption is true, parsing should stop at the first non-option
        String[] args = new String[] { "-a", "non-option", "-d" };
        CommandLine cmd = parser.parse(options, args, true);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("d"));
        
        List<String> leftovers = cmd.getArgList();
        assertEquals(2, leftovers.size());
        assertEquals("non-option", leftovers.get(0));
        assertEquals("-d", leftovers.get(1));
    }

    @Test
    public void testStopAtNonOptionFalse() throws Exception {
        // When stopAtNonOption is false, non-options are treated as arguments or leftovers, but options still parsed
        String[] args = new String[] { "-a", "non-option", "-d" };
        CommandLine cmd = parser.parse(options, args, false);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("d"));
        
        List<String> leftovers = cmd.getArgList();
        assertEquals(1, leftovers.size());
        assertEquals("non-option", leftovers.get(0));
    }

    @Test
    public void testDoubleHyphenHandling() throws Exception {
        String[] args = new String[] { "-a", "--", "-d" };
        CommandLine cmd = parser.parse(options, args, true);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("d"));
        
        List<String> leftovers = cmd.getArgList();
        assertEquals(1, leftovers.size());
        assertEquals("-d", leftovers.get(0));
    }

    @Test
    public void testUnrecognizedShortOption() {
        String[] args = new String[] { "-x" };
        try {
            parser.parse(options, args);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("-x", e.getOption());
        } catch (ParseException e) {
            fail("Expected UnrecognizedOptionException, got " + e.getClass());
        }
    }

    @Test
    public void testBurstingWithUnrecognizedOption() {
        // -ax should treat -x as unrecognized or stop bursting depending on rules
        String[] args = new String[] { "-ax" };
        try {
            parser.parse(options, args);
            fail("Expected ParseException");
        } catch (ParseException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testSingleHyphenArgument() throws Exception {
        String[] args = new String[] { "-" };
        CommandLine cmd = parser.parse(options, args);
        assertNotNull(cmd);
        assertEquals(1, cmd.getArgList().size());
        assertEquals("-", cmd.getArgList().get(0));
    }

    @Test
    public void testBurstingWithStopAtNonOption() throws Exception {
        // Test behavior when encountering something that looks like non-option during bursting
        Options opts = new Options();
        opts.addOption("n", "name", true, "name");
        opts.addOption("t", "test", false, "test");
        
        PosixParser p = new PosixParser();
        // If stopAtNonOption is true, a token that is not a valid option part might burst or stop.
        String[] args = new String[] { "-t", "unknown-token" };
        CommandLine cmd = p.parse(opts, args, true);
        assertTrue(cmd.hasOption("t"));
        assertEquals(1, cmd.getArgList().size());
        assertEquals("unknown-token", cmd.getArgList().get(0));
    }

    @Test
    public void testComplexBurstingScenario() throws Exception {
        // Let's test a mix of long options, short options, equals signs, and arguments
        String[] args = new String[] { "--alpha", "-b", "val1", "-c", "val2", "extra" };
        CommandLine cmd = parser.parse(options, args, true);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("val1", cmd.getOptionValue("b"));
        assertTrue(cmd.hasOption("c"));
        assertEquals("val2", cmd.getOptionValue("c"));
        
        List<String> argList = cmd.getArgList();
        assertEquals(1, argList.size());
        assertEquals("extra", argList.get(0));
    }
    
    @Test
    public void testFlattenWithBurstingEnabledAndDisabled() {
        // Direct testing of flatten method protected/public contract if accessible or via parse
        String[] args = new String[] { "-ab", "val" };
        String[] flattenedTrue = parser.flatten(options, args, true);
        assertNotNull(flattenedTrue);
        
        String[] flattenedFalse = parser.flatten(options, args, false);
        assertNotNull(flattenedFalse);
    }
}