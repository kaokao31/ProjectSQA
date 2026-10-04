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
        options.addOption("c", "char", false, "char option");
        options.addOption("d", "debug", true, "debug option with arg");
    }

    @Test
    public void testSimpleShortOption() throws Exception {
        String[] args = new String[] { "-a" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
    }

    @Test
    public void testBurstingShortOptions() throws Exception {
        // -ac should burst into -a and -c
        String[] args = new String[] { "-ac" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testBurstingWithArgument() throws Exception {
        // -b takes an argument. In bursting like -abval, 'b' consumes 'val'
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
        String[] args = new String[] { "--beta=valueB" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("valueB", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionWithSeparateArg() throws Exception {
        String[] args = new String[] { "--beta", "valueSeparate" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("beta"));
        assertEquals("valueSeparate", cl.getOptionValue("beta"));
    }

    @Test
    public void testLongOptionWithSingleHyphen() throws Exception {
        // PosixParser supports single hyphen for long options too (-alpha)
        String[] args = new String[] { "-alpha" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testLongOptionPartialMatch() throws Exception {
        // PosixParser supports partial matching for long options if unique
        String[] args = new String[] { "--alp" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("alpha"));
    }

    @Test
    public void testStopAtNonOption() throws Exception {
        // If stopAtNonOption is true, parsing stops at the first non-option
        String[] args = new String[] { "-a", "non-option", "-c" };
        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("c"));
        
        String[] leftover = cl.getArgs();
        assertEquals(2, leftover.length);
        assertEquals("non-option", leftover[0]);
        assertEquals("-c", leftover[1]);
    }

    @Test
    public void testStopAtNonOptionFalse() throws Exception {
        String[] args = new String[] { "-a", "non-option", "-c" };
        CommandLine cl = parser.parse(options, args, false);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
        
        String[] leftover = cl.getArgs();
        assertEquals(1, leftover.length);
        assertEquals("non-option", leftover[0]);
    }

    @Test
    public void testDoubleHyphenHandling() throws Exception {
        String[] args = new String[] { "-a", "--", "-c" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("c"));
        
        String[] leftover = cl.getArgs();
        assertEquals(1, leftover.length);
        assertEquals("-c", leftover[0]);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOption() throws Exception {
        String[] args = new String[] { "-z" };
        parser.parse(options, args);
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgument() throws Exception {
        // Option 'b' requires an argument, but nothing follows it
        String[] args = new String[] { "-b" };
        parser.parse(options, args);
    }

    @Test
    public void testNullAndEmptyArguments() throws Exception {
        CommandLine cl1 = parser.parse(options, null);
        assertNotNull(cl1);
        
        CommandLine cl2 = parser.parse(options, new String[0]);
        assertNotNull(cl2);
    }

    @Test
    public void testPropertiesOverride() throws Exception {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("alpha", "true");
        
        String[] args = new String[] { "-c" };
        CommandLine cl = parser.parse(options, args, props);
        assertTrue(cl.hasOption("a")); // from properties
        assertTrue(cl.hasOption("c")); // from args
    }

    @Test
    public void testComplexBurstingAndNonOptions() throws Exception {
        // Test scenario where non-options are mixed with bursting and arguments
        String[] args = new String[] { "-b", "val1", "nonopt", "-ac" };
        CommandLine cl = parser.parse(options, args, false);
        assertTrue(cl.hasOption("b"));
        assertEquals("val1", cl.getOptionValue("b"));
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("c"));
        
        assertEquals(1, cl.getArgs().length);
        assertEquals("nonopt", cl.getArgs()[0]);
    }
}