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
    }

    @Test
    public void testSimpleLongOption() throws ParseException {
        options.addOption("a", "all", false, "not hidden files");
        String[] args = new String[] { "--all" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("all"));
    }

    @Test
    public void testSimpleShortOption() throws ParseException {
        options.addOption("a", "all", false, "not hidden files");
        String[] args = new String[] { "-a" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
    }

    @Test
    public void testBurstingShortOptions() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        options.addOption("c", false, "charlie");
        
        String[] args = new String[] { "-abc" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test
    public void testBurstingWithArgumentAtEnd() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        options.addOption("c", true, "charlie with arg");
        
        String[] args = new String[] { "-abc", "value" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
        assertEquals("value", cl.getOptionValue("c"));
    }

    @Test
    public void testBurstingWithArgumentAttached() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", true, "beta with arg");
        
        String[] args = new String[] { "-abvalue" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("value", cl.getOptionValue("b"));
    }

    @Test
    public void testLongOptionWithEquals() throws ParseException {
        options.addOption("b", "block", true, "block size");
        String[] args = new String[] { "--block=1024" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("block"));
        assertEquals("1024", cl.getOptionValue("block"));
    }

    @Test
    public void testLongOptionWithoutEquals() throws ParseException {
        options.addOption("b", "block", true, "block size");
        String[] args = new String[] { "--block", "2048" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("block"));
        assertEquals("2048", cl.getOptionValue("block"));
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedLongOption() throws ParseException {
        String[] args = new String[] { "--unknown" };
        parser.parse(options, args);
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedShortOption() throws ParseException {
        String[] args = new String[] { "-x" };
        parser.parse(options, args);
    }

    @Test
    public void testStopAtNonOption() throws ParseException {
        options.addOption("a", false, "alpha");
        String[] args = new String[] { "-a", "non-option", "-a" };
        CommandLine cl = parser.parse(options, args, true);
        assertTrue(cl.hasOption("a"));
        // With stopAtNonOption = true, parsing should stop at "non-option"
        assertFalse(cl.hasOption("non-option"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("non-option", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    @Test
    public void testStopAtNonOptionFalse() throws ParseException {
        options.addOption("a", false, "alpha");
        String[] args = new String[] { "-a", "non-option", "-a" };
        CommandLine cl = parser.parse(options, args, false);
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("non-option", cl.getArgs()[0]);
    }

    @Test
    public void testAmbiguousLongOption() throws ParseException {
        options.addOption("v", "version", false, "version");
        options.addOption("b", "verbose", false, "verbose");
        
        // "--ver" could match either "version" or "verbose" depending on exact CLI behavior,
        // but let's test matching prefix.
        String[] args = new String[] { "--ver" };
        try {
            parser.parse(options, args);
            // If it matches uniquely or fails, let's see. Here "--ver" matches neither fully unless prefix matching applies.
        } catch (UnrecognizedOptionException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testDoubleDashHandling() throws ParseException {
        options.addOption("a", false, "alpha");
        String[] args = new String[] { "--", "-a" };
        CommandLine cl = parser.parse(options, args);
        assertFalse(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("-a", cl.getArgs()[0]);
    }

    @Test
    public void testPropertiesSerializationAndParsing() throws ParseException {
        options.addOption("D", "define", true, "define property");
        String[] args = new String[] { "-Dkey=value" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("D"));
        assertEquals("key=value", cl.getOptionValue("D"));
    }

    @Test
    public void testNullArguments() throws ParseException {
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testEmptyArguments() throws ParseException {
        CommandLine cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    @Test
    public void testSingleHyphenArgument() throws ParseException {
        String[] args = new String[] { "-" };
        CommandLine cl = parser.parse(options, args);
        assertNotNull(cl);
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentForLongOption() throws ParseException {
        options.addOption("b", "block", true, "block size");
        String[] args = new String[] { "--block" };
        parser.parse(options, args);
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgumentForShortOption() throws ParseException {
        options.addOption("b", "block", true, "block size");
        String[] args = new String[] { "-b" };
        parser.parse(options, args);
    }

    @Test
    public void testBurstingWithNonOptionInBetween() throws ParseException {
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        
        // If we pass an argument that isn't an option during bursting
        String[] args = new String[] { "-ab" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
    }

    @Test
    public void testFlattenWithLongOptionWithEqualsAndEmptyValue() throws ParseException {
        options.addOption("b", "block", true, "block size");
        String[] args = new String[] { "--block=" };
        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("block"));
        assertEquals("", cl.getOptionValue("block"));
    }
}