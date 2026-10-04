package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void testNotImplementedParser() {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        Options options = new Options();
        try {
            parser.parse(options, new String[] { "-a" });
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        } catch (ParseException e) {
            fail("Unexpected ParseException");
        }

        try {
            parser.parse(options, new String[] { "-a" }, true);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        } catch (ParseException e) {
            fail("Unexpected ParseException");
        }

        try {
            parser.parse(options, new String[] { "-a" }, null, true);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        } catch (ParseException e) {
            fail("Unexpected ParseException");
        }
    }

    @Test
    public void testSetGetOptions() {
        Parser parser = new PosixParser();
        Options options = new Options();
        parser.setOptions(options);
        assertSame(options, parser.getOptions());
    }

    @Test
    public void testSetGetRequiredOptions() {
        Parser parser = new PosixParser();
        java.util.List required = new java.util.ArrayList();
        parser.setRequiredOptions(required);
        assertSame(required, parser.getRequiredOptions());
    }

    @Test
    public void testParseWithPropertiesAndStopAt() throws ParseException {
        Parser parser = new PosixParser();
        Options options = new Options();
        options.addOption("b", "block", false, "block");
        
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("b", "true");

        CommandLine cl = parser.parse(options, new String[] { "-b" }, properties, true);
        assertNotNull(cl);
        assertTrue(cl.hasOption("b"));
    }

    @Test
    public void testParseWithUnrecognizedOption() {
        Parser parser = new PosixParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[] { "--unknown" });
            fail("Expected ParseException");
        } catch (UnrecognizedOptionException e) {
            assertEquals("--unknown", e.getOption());
        } catch (ParseException e) {
            fail("Unexpected ParseException type: " + e.getClass());
        }
    }

    @Test
    public void testParseWithMissingRequiredOption() {
        Parser parser = new PosixParser();
        Options options = new Options();
        Option opt = new Option("r", "required", false, "req");
        opt.setRequired(true);
        options.addOption(opt);

        try {
            parser.parse(options, new String[] {});
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertNotNull(e.getMessage());
        } catch (ParseException e) {
            fail("Unexpected ParseException type: " + e.getClass());
        }
    }

    @Test
    public void testParseWithMissingArgument() {
        Parser parser = new PosixParser();
        Options options = new Options();
        Option opt = new Option("f", "file", true, "file");
        options.addOption(opt);

        try {
            parser.parse(options, new String[] { "-f" });
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertEquals(opt, e.getOption());
        } catch (ParseException e) {
            fail("Unexpected ParseException type: " + e.getClass());
        }
    }

    @Test
    public void testParseNullArguments() throws ParseException {
        Parser parser = new PosixParser();
        Options options = new Options();
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertTrue(cl.getArgs().length == 0);
    }

    @Test
    public void testCheckRequiredOptions() throws ParseException {
        Parser parser = new PosixParser();
        // Just executing checkRequiredOptions via package-visible or subclassing if needed, 
        // but it's protected. We can test it indirectly or via a concrete subclass instance.
        // Actually PosixParser inherits checkRequiredOptions.
        Options options = new Options();
        Option opt = new Option("x", "val", false, "val");
        opt.setRequired(true);
        options.addOption(opt);
        parser.setOptions(options);

        try {
            parser.checkRequiredOptions();
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            // expected
        }
    }

    @Test
    public void testProcessProperties() throws ParseException {
        Parser parser = new PosixParser();
        Options options = new Options();
        Option opt = new Option("p", "prop", true, "prop");
        options.addOption(opt);
        parser.setOptions(options);

        java.util.Properties props = new java.util.Properties();
        props.setProperty("p", "propValue");

        CommandLine cl = new CommandLine();
        parser.processProperties(props);
        
        // After processProperties, the option should be added to cl or processed
        // Let's test processProperties by calling parse with properties which invokes it.
        CommandLine cl2 = parser.parse(options, new String[] {}, props, false);
        assertTrue(cl2.hasOption("p"));
        assertEquals("propValue", cl2.getOptionValue("p"));
    }
}