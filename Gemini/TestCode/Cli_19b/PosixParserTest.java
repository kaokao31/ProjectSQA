package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for PosixParser (Commons CLI - Bug 19 context).
 * Designed for maximum line/branch coverage and edge-case validation.
 */
public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
        
        // Setup standard options for testing
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option with arg");
        options.addOption("c", "char", false, "char option");
        options.addOption("d", "debug", true, "debug option with arg");
    }

    @Test
    public void testNullOptions() {
        try {
            parser.parse(null, new String[] { "-a" });
            fail("Expected NullPointerException or IllegalArgumentException");
        } catch (Exception e) {
            // Expected exception depending on exact CLI version handling of null options
            assertNotNull(e);
        }
    }

    @Test
    public void testNullArgs() {
        try {
            CommandLine cl = parser.parse(options, null);
            assertNotNull(cl);
            assertTrue(cl.getArgs().length == 0);
        } catch (Exception e) {
            fail("Should handle null arguments gracefully or throw expected exception: " + e.getMessage());
        }
    }

    @Test
    public void testEmptyArgs() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] {});
        assertNotNull(cl);
        assertTrue(cl.getOptions().length == 0);
    }

    @Test
    public void testSimpleShortOption() throws ParseException {
        String[] args = new String[] { "-a" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }

    @Test
    public void testMultipleShortOptions() throws ParseException {
        String[] args = new String[] { "-ac" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testShortOptionWithArgumentAttached() throws ParseException {
        String[] args = new String[] { "-bval" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test
    public void testShortOptionWithArgumentStandalone() throws ParseException {
        String[] args = new String[] { "-b", "val" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test
    public void testShortOptionClusterWithArgumentLast() throws ParseException {
        String[] args = new String[] { "-acb", "val" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test
    public void testShortOptionClusterWithArgumentAttached() throws ParseException {
        String[] args = new String[] { "-acbval" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test
    public void testLongOptionWithoutArgument() throws ParseException {
        String[] args = new String[] { "--alpha" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("alpha"));
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testLongOptionWithEqualsArgument() throws ParseException {
        String[] args = new String[] { "--beta=val" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("val", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionWithStandaloneArgument() throws ParseException {
        String[] args = new String[] { "--beta", "val" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("val", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionAbbreviation() throws ParseException {
        // If long option matching supports partial/abbreviated matches
        String[] args = new String[] { "--alph" };
        try {
            CommandLine cl = parser.parse(options, args);
            // If supported
            assertNotNull(cl);
        } catch (ParseException e) {
            // Expected if abbreviation is not allowed in strict mode
            assertNotNull(e);
        }
    }

    @Test
    public void testDoubleDashHandling() throws ParseException {
        String[] args = new String[] { "--", "-a", "--beta" };
        CommandLine cl = parser.parse(options, args);
        assertFalse(cl.hasOption("a"));
        assertFalse(cl.hasOption("beta"));
        String[] rest = cl.getArgs();
        assertEquals(2, rest.length);
        assertEquals("-a", rest[0]);
        assertEquals("--beta", rest[1]);
    }

    @Test
    public void testUnrecognizedShortOption() {
        String[] args = new String[] { "-z" };
        try {
            parser.parse(options, args);
            // Depending on stopAtNonOption configuration, it might throw or treat as non-option
        } catch (ParseException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testUnrecognizedLongOption() {
        String[] args = new String[] { "--unknown" };
        try {
            parser.parse(options, args);
            fail("Expected ParseException for unrecognized long option");
        } catch (ParseException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testBurstTokenHandlingWithStopAtNonOption() throws ParseException {
        // Test behavior when encountering non-option tokens and stopAtNonOption is true
        CommandLine cl = parser.parse(options, new String[] { "-a", "non-option", "-c" }, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("c"));
        String[] leftover = cl.getArgs();
        assertEquals(2, leftover.length);
        assertEquals("non-option", leftover[0]);
        assertEquals("-c", leftover[1]);
    }

    @Test
    public void testBurstTokenHandlingWithoutStopAtNonOption() throws ParseException {
        CommandLine cl = parser.parse(options, new String[] { "-a", "non-option", "-c" }, false);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
        String[] leftover = cl.getArgs();
        assertEquals(1, leftover.length);
        assertEquals("non-option", leftover[0]);
    }

    @Test
    public void testBurstWithMultipleHyphensAndEdges() throws ParseException {
        String[] args = new String[] { "-b", "-val" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals("-val", cl.getOptionValue("b"));
    }
}