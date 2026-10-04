package org.apache.commons.cli;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link GnuParser}.
 */
public class GnuParserTest {

    private GnuParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new GnuParser();
        options = new Options();
        options.addOption("a", "alpha", false, "toggle alpha");
        options.addOption("b", "beta", true, "set beta");
        options.addOption("c", "config", true, "set config");
    }

    @Test
    public void testSimpleShortOption() throws Exception {
        String[] args = new String[] { "-a" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("a"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testShortOptionWithValueAttached() throws Exception {
        String[] args = new String[] { "-bval" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testShortOptionWithValueStandalone() throws Exception {
        String[] args = new String[] { "-b", "val" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testMultipleShortOptionsClustered() throws Exception {
        options.addOption("x", false, "toggle x");
        options.addOption("y", false, "toggle y");
        String[] args = new String[] { "-axy" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("a"));
        assertEquals(true, cl.hasOption("x"));
        assertEquals(true, cl.hasOption("y"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testClusteredShortOptionsWithValueLast() throws Exception {
        options.addOption("x", false, "toggle x");
        String[] args = new String[] { "-abval" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("a"));
        assertEquals(true, cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testLongOptionWithEquals() throws Exception {
        String[] args = new String[] { "--beta=val" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("beta"));
        assertEquals("val", cl.getOptionValue("beta"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testLongOptionStandalone() throws Exception {
        String[] args = new String[] { "--beta", "val" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("beta"));
        assertEquals("val", cl.getOptionValue("beta"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testLongOptionSingleHyphenExpansion() throws Exception {
        // GnuParser treats single-hyphen long options (e.g. "-beta") as long options
        String[] args = new String[] { "-beta", "val" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("beta"));
        assertEquals("val", cl.getOptionValue("beta"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testLongOptionSingleHyphenWithEquals() throws Exception {
        String[] args = new String[] { "-beta=val" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("beta"));
        assertEquals("val", cl.getOptionValue("beta"));
        assertArrayEquals(new String[0], cl.getArgs());
    }

    @Test
    public void testDoubleDashStopProcessing() throws Exception {
        String[] args = new String[] { "-a", "--", "-b", "val" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("a"));
        assertEquals(false, cl.hasOption("b"));
        assertArrayEquals(new String[] { "-b", "val" }, cl.getArgs());
    }

    @Test
    public void testUnrecognizedOptionInMiddle() throws Exception {
        String[] args = new String[] { "-a", "--unknown", "-b", "val" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("a"));
        assertEquals(false, cl.hasOption("unknown"));
        // Depending on stopAtNonOption configuration, unknown options might be treated as arguments or cause exceptions.
        // Default stopAtNonOption is false in parse(Options, String[]).
        assertArrayEquals(new String[] { "--unknown", "-b", "val" }, cl.getArgs());
    }

    @Test
    public void testUnrecognizedOptionWithStopAtNonOption() throws Exception {
        String[] args = new String[] { "-a", "--unknown", "-b", "val" };
        CommandLine cl = parser.parse(options, true);
        assertNotNull(cl);
        assertEquals(true, cl.hasOption("a"));
        assertArrayEquals(new String[] { "--unknown", "-b", "val" }, cl.getArgs());
    }

    @Test
    public void testLongOptionWithoutEqualButValueIsNextArg() throws Exception {
        String[] args = new String[] { "--beta", "someval" };
        CommandLine cl = parser.parse(options, args);
        assertEquals("someval", cl.getOptionValue("beta"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testInvalidOptionThrowsException() throws Exception {
        String[] args = new String[] { "-z" };
        parser.parse(options, args);
    }

    @Test
    public void testNullAndEmptyArguments() throws Exception {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertArrayEquals(new String[0], cl.getArgs());

        cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
        assertArrayEquals(new String[0], cl.getArgs());
    }
}