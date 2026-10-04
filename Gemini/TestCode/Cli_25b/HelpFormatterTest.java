package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Test;

public class HelpFormatterTest {

    @Test
    public void testConstants() {
        assertEquals(" ", HelpFormatter.DEFAULT_ARG_NAME);
        assertEquals(3, HelpFormatter.DEFAULT_DESC_PAD);
        assertEquals(1, HelpFormatter.DEFAULT_LEFT_PAD);
        assertEquals("---", HelpFormatter.DEFAULT_LONG_OPT_PREFIX);
        assertEquals(74, HelpFormatter.DEFAULT_WIDTH);
        assertEquals("-", HelpFormatter.DEFAULT_OPT_PREFIX);
        assertEquals("usage: ", HelpFormatter.DEFAULT_SYNTAX_PREFIX);
    }

    @Test
    public void testAccessorsAndMutators() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setArgName("customArg");
        assertEquals("customArg", formatter.getArgName());

        formatter.setDescPadding(5);
        assertEquals(5, formatter.getDescPadding());

        formatter.setLeftPadding(2);
        assertEquals(2, formatter.getLeftPadding());

        formatter.setLongOptPrefix("==");
        assertEquals("==", formatter.getLongOptPrefix());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        Comparator<Option> comp = new HelpFormatter.OptionComparator();
        formatter.setOptionComparator(comp);
        assertEquals(comp, formatter.getOptionComparator());
    }

    @Test
    public void testOptionComparator() {
        Comparator<Option> comp = new HelpFormatter.OptionComparator();
        Option opt1 = new Option("a", "Apple");
        Option opt2 = new Option("b", "Banana");
        Option optZ = new Option("z", "Zebra");

        assertTrue(comp.compare(opt1, opt2) < 0);
        assertTrue(comp.compare(opt2, opt1) > 0);
        assertEquals(0, comp.compare(opt1, opt1));
        
        // Null or differing lengths/names if applicable
        Option optLong = new Option(null, "zeta", false, "desc");
        assertTrue(comp.compare(optZ, optLong) != 0);
    }

    @Test
    public void testPrintHelpWithPrintWriter() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 80, "testApp", "Header", options, 2, 5, "Footer");
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("testApp"));
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
        assertTrue(output.contains("-alpha"));
    }

    @Test
    public void testPrintHelpWithOutputStream() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(OptionBuilder.withLongName("beta").withDescription("Beta option").create('b'));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        
        formatter.printHelp(pw, 80, "testApp", "", options, 2, 5, "", true);
        pw.flush();
        
        String output = baos.toString();
        assertTrue(output.contains("-b"));
        assertTrue(output.contains("--beta"));
    }

    @Test
    public void testPrintOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "x-opt", true, "An option with an argument");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printOptions(pw, 80, options, 2, 5);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("-x"));
        assertTrue(output.contains("--x-opt"));
    }

    @Test
    public void testPrintUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha");
        options.addOption("b", "beta", true, "Beta");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 40, "app", options);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("usage:"));
        assertTrue(output.contains("app"));
    }

    @Test
    public void testRenderWrappedText() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        String text = "This is a very long description that needs to be wrapped properly across multiple lines to test the wrapping logic of the help formatter.";
        formatter.renderWrappedText(pw, 30, 5, text);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.length() > 0);
    }

    @Test
    public void testFindWrapPos() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "The quick brown fox jumps over the lazy dog";
        
        int pos = formatter.findWrapPos(text, 15, 0);
        assertTrue(pos != -1);
        
        // Test wrap pos when width exceeds text length
        int pos2 = formatter.findWrapPos(text, 100, 0);
        assertEquals(-1, pos2);

        // Test wrap pos with explicit newlines or tab
        String textWithNewline = "Line1\nLine2";
        int pos3 = formatter.findWrapPos(textWithNewline, 20, 0);
        assertEquals(5, pos3);
    }

    @Test
    public void testCreatePadding() {
        HelpFormatter formatter = new HelpFormatter();
        String padding = formatter.createPadding(4);
        assertEquals("    ", padding);
        
        try {
            formatter.createPadding(-1);
            fail("Expected IllegalArgumentException for negative padding");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRemoveLeadingWhitespaces() {
        String padded = "   trim me";
        String trimmed = HelpFormatter.defaultBootstrap(padded); 
        // Calling via reflection or direct method if accessible, 
        // wait, removeLeadingWhitespace is protected static in HelpFormatter
        String result = HelpFormatter.removeLeadingWhitespace(padded);
        assertEquals("trim me", result);
    }

    @Test
    public void testPrintHelpSimpleSignatures() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("h", "help", false, "print help");

        // These call System.out internally or catch exceptions, better test safely if possible
        // Or wrap standard printHelp overloads
        try {
            formatter.printHelp("cmdLine", options);
            formatter.printHelp("cmdLine", "header", options, "footer");
            formatter.printHelp("cmdLine", "header", options, "footer", true);
        } catch (Exception e) {
            fail("PrintHelp threw an unexpected exception: " + e.getMessage());
        }
    }
}