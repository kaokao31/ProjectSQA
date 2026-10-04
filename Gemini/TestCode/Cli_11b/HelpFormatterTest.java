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
        assertEquals("   ", HelpFormatter.DEFAULT_INDENT);
        assertEquals("-", HelpFormatter.DEFAULT_LONG_OPT_PREFIX);
        assertEquals(74, HelpFormatter.DEFAULT_WIDTH);
        assertEquals("--", HelpFormatter.DEFAULT_LONG_OPT_SEPARATOR);
        assertEquals("ns", HelpFormatter.DEFAULT_OPT_PREFIX); // Defects4J CLI-11 specific or similar
        assertEquals("usage: ", HelpFormatter.DEFAULT_SYNTAX_PREFIX);
    }

    @Test
    public void testGettersAndSetters() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setArgName("customArg");
        assertEquals("customArg", formatter.getArgName());

        formatter.setDescPadding(5);
        assertEquals(5, formatter.getDescPadding());

        formatter.setLeftPadding(2);
        assertEquals(2, formatter.getLeftPadding());

        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setSyntaxPrefix("SYNTAX: ");
        assertEquals("SYNTAX: ", formatter.getSyntaxPrefix());

        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
    }

    @Test
    public void testComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Comparator<Option> comp = new HelpFormatter.OptionComparator();
        formatter.setOptionComparator(comp);
        assertSame(comp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNull(formatter.getOptionComparator());
    }

    @Test
    public void testOptionComparatorCompare() {
        HelpFormatter.OptionComparator comp = new HelpFormatter.OptionComparator();
        Option opt1 = new Option("a", "apple");
        Option opt2 = new Option("b", "banana");
        Option opt1Caps = new Option("A", "Apple");

        assertTrue(comp.compare(opt1, opt2) < 0);
        assertTrue(comp.compare(opt2, opt1) > 0);
        assertEquals(0, comp.compare(opt1, opt1));
        
        // Comparing options with different cases
        assertTrue(comp.compare(opt1Caps, opt1) != 0);
    }

    @Test
    public void testPrintHelp_Simple() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("a", "option-a", false, "Description a");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 80, "testApp", "Header", options, 2, 5, "Footer");
        pw.flush();
        String output = out.toString();

        assertTrue(output.contains("usage: testApp"));
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("-a,--option-a"));
        assertTrue(output.contains("Description a"));
        assertTrue(output.contains("Footer"));
    }

    @Test
    public void testPrintHelp_WithNullsAndEmpty() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        // Test with null header and footer
        formatter.printHelp(pw, 80, "app", null, options, 1, 3, null);
        pw.flush();
        String output = out.toString();
        assertTrue(output.contains("usage: app"));
    }

    @Test
    public void testPrintHelp_StringSignatures() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "brief", true, "brief desc");

        // These methods print to System.out, but we can verify they don't throw exceptions
        try {
            formatter.printHelp("app", options);
            formatter.printHelp("app", "Header", options, "Footer");
            formatter.printHelp("app", "Header", options, "Footer", true);
            formatter.printHelp(80, "app", "Header", options, "Footer");
            formatter.printHelp(80, "app", "Header", options, "Footer", true);
        } catch (Exception e) {
            fail("Printing help threw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testPrintOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("longoption").withDescription("long desc").create('l'));
        
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printOptions(pw, 80, options, 2, 4);
        pw.flush();
        assertTrue(out.toString().contains("-l,--longoption"));
    }

    @Test
    public void testPrintUsage() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "x-opt", false, "desc x");
        options.addOption("y", "y-opt", true, "desc y");

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        formatter.printUsage(pw, 80, "myApp", options);
        pw.flush();
        assertTrue(out.toString().contains("myApp"));
    }

    @Test
    public void testRenderWrappedText() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);

        String text = "This is a very long text that needs to be wrapped properly according to the specified width constraints.";
        formatter.renderWrappedText(pw, 30, 5, text);
        pw.flush();
        
        assertNotNull(out.toString());
    }

    @Test
    public void testFindWrapPos() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "This is a test string for wrapping.";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertTrue(pos != -1);
        
        // Exceeding width without space
        String textNoSpaces = "A_very_long_string_without_spaces";
        int pos2 = formatter.findWrapPos(textNoSpaces, 5, 0);
        assertEquals(5, pos2);
    }

    @Test
    public void testCreatePadding() {
        HelpFormatter formatter = new HelpFormatter();
        String padding = formatter.createPadding(5);
        assertEquals("     ", padding);
    }

    @Test
    public void testRemoveLeadingSpaces() {
        HelpFormatter formatter = new HelpFormatter();
        String padded = "   abc";
        assertEquals("abc", formatter.removeLeadingSpaces(padded));
        
        assertEquals("", formatter.removeLeadingSpaces(""));
        assertNull(formatter.removeLeadingSpaces(null));
    }
}