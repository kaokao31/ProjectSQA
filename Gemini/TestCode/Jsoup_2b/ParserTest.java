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
            parser.parse(options, new String[] { "-a" }, null);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        } catch (ParseException e) {
            fail("Unexpected ParseException");
        }
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
        assertNull(parser.getOptions());
        assertNull(parser.getRequiredOptions());

        parser.setOptions(options);
        assertEquals(options, parser.getOptions());
    }
}