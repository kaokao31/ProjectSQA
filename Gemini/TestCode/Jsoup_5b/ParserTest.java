package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    @Test
    public void testNotToBeInstantiatedOrTestedDirectly() {
        // Since Parser is an abstract class, we can create an anonymous subclass to test its implemented/protected methods.
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option");

        CommandLine cl = null;
        try {
            cl = parser.parse(options, new String[] { "-a" });
            assertNotNull(cl);
            assertTrue(cl.hasOption("a"));
        } catch (ParseException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseWithOptionsAndArgumentsAndStopAtNonOption() {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        Options options = new Options();
        options.addOption("a", false, "alpha");

        try {
            CommandLine cl = parser.parse(options, new String[] { "-a", "extra" }, true);
            assertNotNull(cl);
            assertTrue(cl.hasOption("a"));
            assertEquals(1, cl.getArgs().length);
            assertEquals("extra", cl.getArgs()[0]);
        } catch (ParseException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test(expected = MissingOptionException.class)
    public void testParseMissingRequiredOptions() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        Options options = new Options();
        Option req = new Option("r", "required", false, "req");
        req.setRequired(true);
        options.addOption(req);

        // Should throw MissingOptionException
        parser.parse(options, new String[] {});
    }

    @Test(expected = UnrecognizedOptionException.class)
    public void testParseUnrecognizedOption() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        Options options = new Options();
        parser.parse(options, new String[] { "-unknown" });
    }

    @Test
    public void testSetGetOptions() {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        Options options = new Options();
        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
    }

    @Test
    public void testSetGetRequiredOptions() {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        java.util.List required = new java.util.ArrayList();
        parser.setRequiredOptions(required);
        assertEquals(required, parser.getRequiredOptions());
    }

    @Test
    public void testParseWithProperties() throws ParseException {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        Options options = new Options();
        options.addOption("b", "beta", true, "beta option");

        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("beta", "trueval");

        CommandLine cl = parser.parse(options, new String[] {}, properties);
        assertNotNull(cl);
        assertTrue(cl.hasOption("b"));
        assertEquals("trueval", cl.getOptionValue("b"));
    }

    @Test
    public void testParseWithOptionsAndPropertiesNullArgs() {
        Parser parser = new Parser() {
            @Override
            protected String[] flatten(Options options, String[] arguments, boolean stopAtNonOption) {
                return arguments;
            }
        };

        Options options = new Options();
        try {
            CommandLine cl = parser.parse(options, null);
            assertNotNull(cl);
        } catch (ParseException e) {
            fail("Should handle null arguments gracefully or as expected");
        }
    }
}