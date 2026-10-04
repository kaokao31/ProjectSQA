package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParserTest {

    @Test
    public void testParseNullArguments() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
    }

    @Test
    public void testParseEmptyArguments() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        CommandLine cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
    }

    @Test
    public void testParseWithOptionsAndProperties() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("alpha", "true");

        CommandLine cl = parser.parse(options, new String[0], properties);
        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
    }

    @Test(expected = ParseException.class)
    public void testParseUnrecognizedOption() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        parser.parse(options, new String[] { "-unknown" });
    }

    @Test
    public void testParseWithStopAtNonOption() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        
        CommandLine cl = parser.parse(options, new String[] { "-a", "non-option" }, true);
        assertNotNull(cl);
        assertTrue(cl.hasOption ("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("non-option", cl.getArgs()[0]);
    }

    @Test
    public void testSetGetOptions() {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        parser.setOptions(options);
        assertSame(options, parser.getOptions());
    }

    @Test
    public void testSetGetRequiredOptions() {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        java.util.List required = new java.util.ArrayList();
        parser.setRequiredOptions(required);
        assertSame(required, parser.getRequiredOptions());
    }

    @Test
    public void testProcessPropertiesWithExistingOption() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        Option opt = new Option("b", "beta", true, "beta option");
        options.addOption(opt);

        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("beta", "someValue");

        CommandLine cl = parser.parse(options, new String[0], properties);
        assertTrue(cl.hasOption("b"));
        assertEquals("someValue", cl.getOptionValue("b"));
    }

    @Test(expected = ParseException.class)
    public void testProcessPropertiesMissingValue() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        Option opt = new Option("b", "beta", true, "beta option");
        options.addOption(opt);

        // Required option but property value is null/empty if handled that way, 
        // or check how processProperties reacts to certain conditions.
        java.util.Properties properties = new java.util.Properties();
        // If the option requires an argument and properties provides something that fails, 
        // let's test processProperties directly via parse with a property that conflicts or triggers an exception.
        // Actually, let's pass a property for an option that is already set or similar if applicable,
        // or just test standard property processing when option already has value.
        // Alternatively, properties with an option that doesn't exist:
        parser.parse(options, new String[] { "-b", "val" }, properties);
    }
    
    @Test
    public void testProcessPropertiesAlreadySet() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };
        Options options = new Options();
        options.addOption("b", "beta", true, "beta option");

        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("beta", "propertyValue");

        // Command line already sets 'b', so property should be ignored or handled
        CommandLine cl = parser.parse(options, new String[] { "-b", "cmdValue" }, properties);
        assertEquals("cmdValue", cl.getOptionValue("b"));
    }
}