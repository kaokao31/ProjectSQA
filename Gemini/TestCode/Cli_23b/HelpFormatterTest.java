package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
    }

    @Test
    public void testConstants() {
        assertEquals("-", HelpFormatter.DEFAULT_OPT_PREFIX);
        assertEquals("--", HelpFormatter.DEFAULT_LONG_OPT_PREFIX);
        assertEquals(" ", HelpFormatter.DEFAULT_SYNTAX_PREFIX);
        assertEquals(74, HelpFormatter.DEFAULT_WIDTH);
        assertEquals(1, HelpFormatter.DEFAULT_LEFT_PAD);
        assertEquals(3, HelpFormatter.DEFAULT_DESC_PAD);
        assertEquals("arg", HelpFormatter.DEFAULT_ARG_NAME);
    }

    @Test
    public void testAccessorsAndMutators() {
        formatter.setWidth(80);
        assertEquals(80, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(10);
        assertEquals(10, formatter.getDescPadding());

        formatter.setSyntaxPrefix("SYNTAX: ");
        assertEquals("SYNTAX: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("-opt");
        assertEquals("-opt", formatter.getOptPrefix());

        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());

        formatter.setArgName("customArg");
        assertEquals("customArg", formatter.getArgName());
    }

    @Test
    public void testComparator() {
        Comparator<Option> comp = formatter.getOptionComparator();
        assertNotNull(comp);

        Option opt1 = new Option("a", "alpha", false, "desc");
        Option opt2 = new Option("b", "beta", false, "desc");
        assertTrue(comp.compare(opt1, opt2) < 0);
    }

    @Test
    public void testPrintHelpWithPrintWriter() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        Options options = new Options();
        options.addOption("a", "all", false, "all option");

        formatter.printHelp(pw, 80, "testCmd", "Header", options, 2, 5, "Footer", true);
        pw.flush();
        
        String output = sw.toString();
        assertTrue(output.contains("testCmd"));
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
        assertTrue(output.contains("-all"));
    }

    @Test
    public void testPrintHelpWithStreams() {
        Options options = new Options();
        options.addOption(Option.builder("b").longOpt("beta").desc("beta option").build());

        // Just ensure these methods execute without throwing exceptions
        formatter.printHelp("cmd", options);
        formatter.printHelp("cmd", "Header", options, "Footer");
        formatter.printHelp("cmd", "Header", options, "Footer", true);
        formatter.printHelp(80, "cmd", "Header", options, "Footer");
        formatter.printHelp(80, "cmd", "Header", options, "Footer", true);
    }

    @Test
    public void testPrintOptions() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        Option opt = new Option("x", "extra", true, "extra desc");
        opt.setArgName("file");
        options.addOption(opt);

        formatter.printOptions(pw, 80, options, 2, 4);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("-extra"));
        assertTrue(output.contains("<file>"));
    }

    @Test
    public void testPrintUsage() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Options options = new Options();
        options.addOption("a", false, "opt a");
        options.addOption("b", true, "opt b");

        formatter.printUsage(pw, 80, "myApp", options);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("myApp"));
    }

    @Test
    public void testRenderWrappedText() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        String text = "This is a very long text that needs to be wrapped properly across multiple lines when rendered by the help formatter.";
        
        formatter.renderWrappedText(pw, 40, 5, text);
        pw.flush();

        String output = sw.toString();
        assertNotNull(output);
        assertTrue(output.length() > 0);
    }

    @Test
    public void testFindWrapPos() {
        // Accessing findWrapPos via rendering or typical string manipulation methods
        String text = "Line 1\nLine 2 is quite long and needs wrapping at some specific position.";
        // We can test wrap via renderWrappedText which internally uses findWrapPos
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.renderWrappedText(pw, 20, 0, text);
        assertTrue(sw.toString().length() > 0);
    }

    @Test
    public void testCreatePadding() {
        String padding = formatter.createPadding(5);
        assertEquals("     ", padding);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePaddingNegative() {
        formatter.createPadding(-1);
    }

    @Test
    public void testRemoveLeadingSpaces() {
        String padded = "   trim me";
        String trimmed = HelpFormatter.defaultIgnoreCaseFlag ? formatter.stripLeadingSpaces(padded) : formatter.stripLeadingSpaces(padded);
        // Note: stripLeadingSpaces is protected, let's verify via subclass or direct call if accessible (it is in same package)
        assertEquals("trim me", formatter.stripLeadingSpaces(padded));
    }

    @Test
    public void testOptionComparator() {
        HelpFormatter.OptionComparator comp = new HelpFormatter.OptionComparator();
        Option o1 = new Option("b", "beta", false, "d");
        Option o2 = new Option("a", "alpha", false, "d");
        
        // "alpha" should come before "beta"
        assertTrue(comp.compare(o1, o2) > 0);
        assertTrue(comp.compare(o2, o1) < 0);
        assertEquals(0, comp.compare(o1, o1));
    }
}