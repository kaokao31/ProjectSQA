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
        options.addOption("c", "config", true, "config option");
        options.addOption("d", "debug", false, "debug option");
    }

    @Test
    public void testSimpleShortOption() throws Exception {
        String[] args = new String[] { "-a" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }

    @Test
    public void testMultipleShortOptions() throws Exception {
        String[] args = new String[] { "-ad" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("d"));
    }

    @Test
    public void testShortOptionWithAttachedArgument() throws Exception {
        String[] args = new String[] { "-bvalue" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    @Test
    public void testShortOptionWithSeparateArgument() throws Exception {
        String[] args = new String[] { "-b", "value" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    @Test
    public void testBurstingShortOptionsWithArgumentAtEnd() throws Exception {
        // -adbvalue should burst into -a, -d, and -b with argument "value"
        String[] args = new String[] { "-adbvalue" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("d"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    @Test
    public void testLongOptionWithoutArgument() throws Exception {
        String[] args = new String[] { "--alpha" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testLongOptionWithEqualsArgument() throws Exception {
        String[] args = new String[] { "--beta=value" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("value", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionWithSeparateArgument() throws Exception {
        String[] args = new String[] { "--beta", "value" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("value", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionAbbreviation() throws Exception {
        // Assuming Commons CLI supports long option abbreviation if unique
        String[] args = new String[] { "--al" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testDoubleDashStopProcessing() throws Exception {
        String[] args = new String[] { "-a", "--", "-b" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-b", cl.getArgs()[0]);
    }

    @Test
    public void testUnrecognizedOption() throws Exception {
        String[] args = new String[] { "-u" };
        CommandLine cl = parser.parse(options, args);
        assertFalse(cl.hasOption("u"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-u", cl.getArgs()[0]);
    }

    @Test
    public void testUnrecognizedLongOption() throws Exception {
        String[] args = new String[] { "--unknown" };
        CommandLine cl = parser.parse(options, args);
        assertFalse(cl.hasOption("unknown"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("--unknown", cl.getArgs()[0]);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOptionStopAtNonOption() throws Exception {
        parser.parse(options, new String[] { "-u" }, true);
    }

    @Test
    public void testStopAtNonOption() throws Exception {
        String[] args = new String[] { "-a", "some-arg", "-d" };
        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("d"));
        
        String[] leftover = cl.getArgs();
        assertEquals(2, leftover.length);
        assertEquals("some-arg", leftover[0]);
        assertEquals("-d", leftover[1]);
    }

    @Test
    public void testBurstingThenNonOptionToken() throws Exception {
        // Burst options where one requires an argument, but next token looks like a non-option or argument scenario
        String[] args = new String[] { "-ab", "value" };
        // 'a' is no arg, 'b' requires arg. So -ab should treat 'b' as taking 'value'.
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    @Test
    public void testNullAndEmptyArguments() throws Exception {
        CommandLine cl1 = parser.parse(options, null);
        assertNotNull(cl1);

        CommandLine cl2 = parser.parse(options, new String[0]);
        assertNotNull(cl2);
    }

    @Test
    public void testSingleDashArgument() throws Exception {
        String[] args = new String[] { "-" };
        CommandLine cl = parser.parse(options, args);
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test
    public void testBurstingUnknownOptionInMiddle() throws Exception {
        // -axd should burst -a, then 'x' (unknown), then 'd'
        String[] args = new String[] { "-axd" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("d")); // Depending on PosixParser implementation, does it stop bursting on unknown?
    }
}