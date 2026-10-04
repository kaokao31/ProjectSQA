package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class DefaultParserTest {

    private DefaultParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new DefaultParser();
        options = new Options();
    }

    @Test
    public void testSimpleLongOption() throws Exception {
        options.addOption(Option.builder("a").longOpt("apple").build());
        String[] args = new String[]{"--apple"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("apple"));
        assertTrue(cmd.hasOption("a"));
    }

    @Test
    public void testSimpleShortOption() throws Exception {
        options.addOption("a", "apple", false, "it's an apple");
        String[] args = new String[]{"-a"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("apple"));
    }

    @Test(expected = MissingOptionException.class)
    public void testMissingRequiredOption() throws Exception {
        options.addOption(Option.builder("r").required().build());
        parser.parse(options, new String[]{});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOption() throws Exception {
        parser.parse(options, new String[]{"-u"});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testUnrecognizedLongOption() throws Exception {
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test
    public void testOptionWithArgument() throws Exception {
        options.addOption(Option.builder("b").longOpt("file").hasArg().build());
        String[] args = new String[]{"--file", "test.txt"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("file"));
        assertEquals("test.txt", cmd.getOptionValue("file"));
    }

    @Test
    public void testOptionWithEqualsArgument() throws Exception {
        options.addOption(Option.builder("b").longOpt("file").hasArg().build());
        String[] args = new String[]{"--file=test.txt"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("file"));
        assertEquals("test.txt", cmd.getOptionValue("file"));
    }

    @Test
    public void testShortOptionWithAttachedArgument() throws Exception {
        options.addOption(Option.builder("f").hasArg().build());
        String[] args = new String[]{"-ffile.txt"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("f"));
        assertEquals("file.txt", cmd.getOptionValue("f"));
    }

    @Test
    public void testShortOptionCluster() throws Exception {
        options.addOption("x", false, "first");
        options.addOption("y", false, "second");
        options.addOption(Option.builder("z").hasArg().build());
        String[] args = new String[]{"-xyzval"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("x"));
        assertTrue(cmd.hasOption("y"));
        assertTrue(cmd.hasOption("z"));
        assertEquals("val", cmd.getOptionValue("z"));
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingArgument() throws Exception {
        options.addOption(Option.builder("f").hasArg().build());
        parser.parse(options, new String[]{"-f"});
    }

    @Test(expected = MissingArgumentException.class)
    public void testMissingLongArgument() throws Exception {
        options.addOption(Option.builder("f").longOpt("file").hasArg().build());
        parser.parse(options, new String[]{"--file"});
    }

    @Test
    public void testStopAtNonOption() throws Exception {
        options.addOption("a", false, "apple");
        String[] args = new String[]{"-a", "non-option", "-a"};
        CommandLine cmd = parser.parse(options, args, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("non-option", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    @Test
    public void testStopAtNonOptionFalse() throws Exception {
        options.addOption("a", false, "apple");
        String[] args = new String[]{"-a", "non-option"};
        CommandLine cmd = parser.parse(options, args, false);
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("non-option", cmd.getArgs()[0]);
    }

    @Test
    public void testPropertiesSupport() throws Exception {
        options.addOption(Option.builder("p").longOpt("prop").hasArg().build());
        Properties props = new Properties();
        props.setProperty("prop", "propValue");
        
        String[] args = new String[]{};
        CommandLine cmd = parser.parse(options, args, props);
        assertTrue(cmd.hasOption("prop"));
        assertEquals("propValue", cmd.getOptionValue("prop"));
    }

    @Test
    public void testPropertiesOverrideByArgs() throws Exception {
        options.addOption(Option.builder("p").longOpt("prop").hasArg().build());
        Properties props = new Properties();
        props.setProperty("prop", "propValue");
        
        String[] args = new String[]{"--prop", "argValue"};
        CommandLine cmd = parser.parse(options, args, props);
        assertTrue(cmd.hasOption("prop"));
        assertEquals("argValue", cmd.getOptionValue("prop"));
    }

    @Test
    public void testMultipleValuesForOption() throws Exception {
        options.addOption(Option.builder("m").longOpt("multi").hasArgs().build());
        String[] args = new String[]{"--multi", "val1", "--multi", "val2"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("multi"));
        assertArrayEquals(new String[]{"val1", "val2"}, cmd.getOptionValues("multi"));
    }

    @Test
    public void testDoubleDashHandling() throws Exception {
        options.addOption("a", false, "apple");
        String[] args = new String[]{"--", "-a"};
        CommandLine cmd = parser.parse(options, args);
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-a", cmd.getArgs()[0]);
    }

    @Test
    public void testAmbiguousLongOption() throws Exception {
        options.addOption(Option.builder().longOpt("version").build());
        options.addOption(Option.builder().longOpt("verbose").build());
        
        try {
            parser.parse(options, new String[]{"--ver"});
            fail("Expected AmbiguousOptionException");
        } catch (AmbiguousOptionException e) {
            assertNotNull(e.getMatchingOptions());
            assertTrue(e.getMatchingOptions().size() > 1);
        }
    }

    @Test
    public void testExactLongOptionMatch() throws Exception {
        options.addOption(Option.builder().longOpt("version").build());
        options.addOption(Option.builder().longOpt("verbose").build());
        
        CommandLine cmd = parser.parse(options, new String[]{"--version"});
        assertTrue(cmd.hasOption("version"));
        assertFalse(cmd.hasOption("verbose"));
    }

    @Test(expected = ParseException.class)
    public void testUnrecognizedOptionWithPartialMatch() throws Exception {
        options.addOption(Option.builder().longOpt("version").build());
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test
    public void testBurstingMultipleShortOptionsWithArg() throws Exception {
        options.addOption("a", false, "opt a");
        options.addOption("b", false, "opt b");
        options.addOption(Option.builder("c").hasArg().build());
        
        String[] args = new String[]{"-abcvalue"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
        assertEquals("value", cmd.getOptionValue("c"));
    }

    @Test
    public void testNullArguments() throws Exception {
        options.addOption("a", false, "apple");
        CommandLine cmd = parser.parse(options, (String[]) null);
        assertNotNull(cmd);
        assertFalse(cmd.hasOption("a"));
    }

    @Test
    public void testNullOptions() throws Exception {
        try {
            parser.parse(null, new String[]{"-a"});
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testOptionWithOptionalArgument() throws Exception {
        Option opt = Option.builder("o").hasOptionalArg().build();
        options.addOption(opt);
        
        String[] args = new String[]{"-o"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("o"));
        assertNull(cmd.getOptionValue("o"));
    }

    @Test
    public void testOptionWithOptionalArgumentAndValue() throws Exception {
        Option opt = Option.builder("o").hasOptionalArg().build();
        options.addOption(opt);
        
        String[] args = new String[]{"-o", "val"};
        CommandLine cmd = parser.parse(options, args);
        assertTrue(cmd.hasOption("o"));
        assertEquals("val", cmd.getOptionValue("o"));
    }
}