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
        String text = "This is a test text to check the wrapping position logic in HelpFormatter.";
        
        // width = 10, startPos = 0
        int pos = formatter.findWrapPos(text, 10, 0);
        assertTrue(pos != -1);
        assertTrue(pos <= 10);

        // startPos greater than text length
        assertEquals(-1, formatter.findWrapPos(text, 10, 100));

        // startPos + width >= text.length()
        int wrap = formatter.findWrapPos(text, 100, 0);
        assertEquals(-1, wrap);

        // find newline
        String textWithNewline = "Line1\nLine2";
        assertEquals(5, formatter.findWrapPos(textWithNewline, 10, 0));

        // find tab or spaces
        String textWithTab = "Line1\tLine2";
        assertEquals(5, formatter.findWrapPos(textWithTab, 10, 0));

        // no wrap pos found within width, forces wrap at width
        String noSpaces = "123456789012345";
        assertEquals(5, formatter.findWrapPos(noSpaces, 5, 0));
        
        // startPos pointing to a newline already
        String textStartNewline = "\nLine2";
        assertEquals(0, formatter.findWrapPos(textStartNewline, 10, 0));
        
        // last line wrap fallback
        assertEquals(-1, formatter.findWrapPos("short", 10, 2));
    }

    @Test
    public void testRenderOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "option-a", false, "Description A");
        options.addOption("b", "option-b", true, "Description B that is quite long and should span multiple lines when rendered properly by the formatter.");

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 3, 5);
        assertTrue(sb.length() > 0);
    }

    @Test
    public void testRenderWrappedText() {
        HelpFormatter formatter = new HelpFormatter();
        StringBuffer sb = new StringBuffer();
        String text = "This is a very long description text that needs to be wrapped across multiple lines with some indentation.";
        formatter.renderWrappedText(sb, 40, 5, text);
        assertTrue(sb.length() > 0);
    }

    @Test
    public void testPrintHelpCustomStream() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "print this message");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bytes);

        formatter.printHelp(pw, 80, "test", "header", options, 2, 5, "footer", true);
        pw.flush();
        String output = bytes.toString();
        assertTrue(output.contains("test"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
        assertTrue(output.contains("help"));
    }

    @Test
    public void testPrintHelpStringVariants() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");

        // Just ensure these methods execute completely without throwing exceptions
        try {
            formatter.printHelp(80, "cmdLine", "header", options, "footer");
            formatter.printHelp("cmdLine", options);
            formatter.printHelp("cmdLine", options, true);
            formatter.printHelp(80, "cmdLine", "header", options, "footer", true);
        } catch (Exception e) {
            fail("PrintHelp threw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testPrintUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "apple", false, "apple");
        options.addOption("b", "boy", true, "boy");

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printUsage(pw, 40, "app", options);
        pw.flush();
        assertTrue(out.toString().length() > 0);
        
        // Test with arg
        StringWriter out2 = new StringWriter();
        PrintWriter pw2 = new PrintWriter(out2);
        formatter.printUsage(pw2, 80, "app", "syntax");
        pw2.flush();
        assertTrue(out2.toString().contains("syntax"));
    }

    @Test
    public void testGettersAndSetters() {
        HelpFormatter formatter = new HelpFormatter();
        
        formatter.setArgName("arg");
        assertEquals("arg", formatter.getArgName());

        formatter.setDescPadding(5);
        assertEquals(5, formatter.getDescPadding());

        formatter.setLeftPadding(3);
        assertEquals(3, formatter.getLeftPadding());

        formatter.setLongOptPrefix("--");
        assertEquals("--", formatter.getLongOptPrefix());

        formatter.setOptPrefix("-");
        assertEquals("-", formatter.getOptPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setSyntaxPrefix("usage: ");
        assertEquals("usage: ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testOptionComparator() {
        HelpFormatter formatter = new HelpFormatter();
        assertNotNull(formatter.getOptionComparator());
        
        Option optA = new Option("a", "alpha");
        Option optB = new Option("b", "beta");
        int cmp = formatter.getOptionComparator().compare(optA, optB);
        assertTrue(cmp < 0);
    }

    @Test
    public void testCreatePadding() {
        HelpFormatter formatter = new HelpFormatter();
        String padding = formatter.createPadding(5);
        assertEquals("     ", padding);
    }

    @Test
    public void testRemoveWrappedLeadingSpaces() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "   lead spaces";
        String cleaned = formatter.removeWrappedLeadingSpaces(text);
        assertEquals("lead spaces", cleaned);
        
        assertEquals("", formatter.removeWrappedLeadingSpaces(""));
        assertNull(formatter.removeWrappedLeadingSpaces(null));
    }
}