package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultParserTest {

    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new DefaultParser();
        options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option with arg");
        options.addOption("c", "config", true, "config option");
        options.addOption("d", "debug", false, "debug option");
    }

    @Test
    public void testSimpleShortOption() throws Exception {
        String[] args = new String[]{"-a"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }

    @Test
    public void testShortOptionGrouping() throws Exception {
        String[] args = new String[]{"-ad"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("d"));
    }

    @Test
    public void testShortOptionWithArgValueAttached() throws Exception {
        String[] args = new String[]{"-bval"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test
    public void testShortOptionWithArgValueStandalone() throws Exception {
        String[] args = new String[]{"-b", "val"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test
    public void testShortOptionGroupWithArgAtEnd() throws Exception {
        String[] args = new String[]{"-adbval"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("d"));
        assertTrue(cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
    }

    @Test(expected = ParseException.class)
    public void testShortOptionGroupWithArgNotAtEndFails() throws Exception {
        // -b requires an argument, but it's followed by 'd' in the same token
        String[] args = new String[]{"-bdval"};
        parser.parse(options, args);
    }

    @Test
    public void testLongOptionSimple() throws Exception {
        String[] args = new String[]{"--alpha"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testLongOptionWithEqualsArg() throws Exception {
        String[] args = new String[]{"--beta=hello"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("hello", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionStandaloneArg() throws Exception {
        String[] args = new String[]{"--beta", "world"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("world", cl.getOptionValue("beta"));
    }

    @Test(expected = ParseException.class)
    public void testLongOptionWithUnexpectedEquals() throws Exception {
        // 'alpha' does not take an argument
        String[] args = new String[]{"--alpha=notallowed"};
        parser.parse(options, args);
    }

    @Test(expected = ParseException.class)
    public void testLongOptionWithoutRequiredArg() throws Exception {
        String[] args = new String[]{"--beta"};
        parser.parse(options, args);
    }

    @Test(expected = ParseException.class)
    public void testUnrecognizedLongOption() throws Exception {
        String[] args = new String[]{"--unknown"};
        parser.parse(options, args);
    }

    @Test(expected = ParseException.class)
    public void testUnrecognizedShortOption() throws Exception {
        String[] args = new String[]{"-x"};
        parser.parse(options, args);
    }

    @Test
    public void testStopAtNonOption() throws Exception {
        // Configure parser to stop at non-option
        DefaultParser p = new DefaultParser();
        String[] args = new String[]{"-a", "nonoption", "-d"};
        CommandLine cl = p.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("d"));
        assertArrayEquals(new String[]{"nonoption", "-d"}, cl.getArgs());
    }

    @Test
    public void testStopAtNonOptionWithoutStopAtFlag() throws Exception {
        String[] args = new String[]{"-a", "nonoption", "-d"};
        CommandLine cl = parser.parse(options, args, false);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("d"));
        assertArrayEquals(new String[]{"nonoption"}, cl.getArgs());
    }

    @Test
    public void testDoubleDashHandling() throws Exception {
        String[] args = new String[]{"-a", "--", "-d"};
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("d"));
        assertArrayEquals(new String[]{"-d"}, cl.getArgs());
    }

    @Test
    public void testMultipleParseCallsResetState() throws Exception {
        String[] args1 = new String[]{"-a"};
        CommandLine cl1 = parser.parse(options, args1);
        assertTrue(cl1.hasOption("a"));

        String[] args2 = new String[]{"-d"};
        CommandLine cl2 = parser.parse(options, args2);
        assertFalse(cl2.hasOption("a"));
        assertTrue(cl2.hasOption("d"));
    }

    @Test
    public void testLongOptionPartialMatchAmbiguous() throws Exception {
        Options opts = new Options();
        opts.addOption("ver", "version", false, "version");
        opts.addOption("vib", "vibrate", false, "vibrate");

        DefaultParser p = new DefaultParser();
        // Ambiguous prefix --v should fail or handle appropriately
        try {
            p.parse(opts, new String[]{"--v"});
            fail("Expected ParseException for ambiguous long option prefix");
        } catch (ParseException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testLongOptionPartialMatchUnique() throws Exception {
        Options opts = new Options();
        opts.addOption("ver", "version", false, "version");
        opts.addOption("vib", "vibrate", false, "vibrate");

        DefaultParser p = new DefaultParser();
        CommandLine cl = p.parse(opts, new String[]{"--ver"});
        assertTrue(cl.hasOption("version"));
    }

    @Test
    public void testPropertiesOverriding() throws Exception {
        Options opts = new Options();
        opts.addOption(Option.builder("b").hasArg().build());

        java.util.Properties props = new java.util.Properties();
        props.setProperty("b", "propValue");

        CommandLine cl = parser.parse(opts, new String[0], props);
        assertTrue(cl.hasOption("b"));
        assertEquals("propValue", cl.getOptionValue("b"));
    }

    @Test
    public void testCommandLinePropertiesOptionValueOverriddenByArgs() throws Exception {
        Options opts = new Options();
        opts.addOption(Option.builder("b").hasArg().build());

        java.util.Properties props = new java.util.Properties();
        props.setProperty("b", "propValue");

        // Command line arg should take precedence or combine depending on setup
        CommandLine cl = parser.parse(opts, new String[]{"-b", "argValue"}, props);
        assertEquals("argValue", cl.getOptionValue("b"));
    }

    @Test(expected = ParseException.class)
    public void testMissingRequiredOption() throws Exception {
        Options opts = new Options();
        opts.addOption(Option.builder("req").required(true).build());
        parser.parse(opts, new String[0]);
    }

    @Test
    public void testNullArgs() {
        try {
            parser.parse(options, null);
            fail("Expected IllegalArgumentException or ParseException");
        } catch (Exception e) {
            // Expected exception due to null input
        }
    }
}