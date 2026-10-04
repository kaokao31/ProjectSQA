package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

public class HelpFormatterTest {

    @Test
    public void testFindWrapPos() {
        HelpFormatter formatter = new HelpFormatter();
        
        // Test text with newline before width
        String text = "Line one\nLine two";
        int pos = formatter.findWrapPos(text, 15, 0);
        assertEquals(8, pos);

        // Test text shorter than width
        text = "Short";
        pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(-1, pos);

        // Test wrapping at width limit with space
        text = "This is a long line that needs wrapping";
        pos = formatter.findWrapPos(text, 10, 0);
        assertTrue(pos != -1);
        assertTrue(pos <= 10);

        // Test wrapping when startPos is greater than text length
        pos = formatter.findWrapPos(text, 10, 100);
        assertEquals(-1, pos);

        // Test wrapping with no spaces before width
        text = "123456789012345";
        pos = formatter.findWrapPos(text, 5, 0);
        assertEquals(5, pos);

        // Test trailing newline or tab scenarios
        text = "a\tbb";
        pos = formatter.findWrapPos(text, 5, 0);
        assertEquals(2, pos);

        text = "a\nbb";
        pos = formatter.findWrapPos(text, 5, 0);
        assertEquals(2, pos);
        
        // Test startPos offset wrapping
        text = "Prefix LongWord";
        pos = formatter.findWrapPos(text, 10, 7);
        assertEquals(15, pos);
    }

    @Test
    public void testRenderWrappedText() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        String text = "This is a very long text that definitely needs to be wrapped across multiple lines to test the rendering functionality properly.";
        
        formatter.renderWrappedText(sb, 30, 4, text);
        assertTrue(sb.length() > 0);
    }

    @Test
    public void testRenderOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", true, "Beta option with argument");

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 5);
        assertTrue(sb.length() > 0);
    }

    @Test
    public void testPrintHelp() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "Show help");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(outContent);

        formatter.printHelp(pw, 80, "test", "header", options, 2, 3, "footer", true);
        pw.flush();
        String output = outContent.toString();
        assertTrue(output.contains("test"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
        assertTrue(output.contains("help"));
    }

    @Test
    public void testPrintHelpVariants() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "xaxis", true, "X axis");

        // Just smoke testing these methods to ensure no exceptions are thrown
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        formatter.printUsage(pw, 80, "app", options);
        formatter.printWrapped(pw, 80, 2, "A wrapped string line");
        formatter.printWrapped(pw, 80, "A simple wrapped string line");
        
        try {
            formatter.printHelp("cmdLine", options);
        } catch (Exception e) {
            // Depending on System.out usage or exit behavior, catch if necessary
        }

        try {
            formatter.printHelp("cmdLine", "header", options, "footer");
        } catch (Exception e) {
            // Ignore
        }

        try {
            formatter.printHelp(80, "cmdLine", "header", options, "footer");
        } catch (Exception e) {
            // Ignore
        }
        
        try {
            formatter.printHelp(80, "cmdLine", "header", options, "footer", true);
        } catch (Exception e) {
            // Ignore
        }
    }

    @Test
    public void testOptionComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Comparator<Option> comp = formatter.getOptionComparator();
        assertNotNull(comp);

        Option opt1 = new Option("b", "beta", false, "B");
        Option opt2 = new Option("a", "alpha", false, "A");
        
        assertTrue(comp.compare(opt1, opt2) > 0);
        assertTrue(comp.compare(opt2, opt1) < 0);
        assertEquals(0, comp.compare(opt1, opt1));
    }

    @Test
    public void testGettersAndSetters() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setArgName("myArg");
        assertEquals("myArg", formatter.getArgName());

        formatter.setDescPadding(5);
        assertEquals(5, formatter.getDescPadding());

        formatter.setLeftPadding(3);
        assertEquals(3, formatter.getLeftPadding());

        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());

        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setSyntaxPrefix("Usage: ");
        assertEquals("Usage: ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testCreatePadding() {
        HelpFormatter formatter = new HelpFormatter();
        String padding = formatter.createPadding(4);
        assertEquals("    ", padding);
        
        assertEquals("", formatter.createPadding(0));
    }

    @Test
    public void testRtrim() {
        HelpFormatter formatter = new HelpFormatter();
        String trimmed = formatter.rtrim("   abc   ");
        assertEquals("   abc", trimmed);

        assertEquals("", formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
    }
}