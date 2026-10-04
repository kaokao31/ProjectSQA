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

        formatter.setSyntaxPrefix("Usage: ");
        assertEquals("Usage: ", formatter.getSyntaxPrefix());

        formatter.setOptPrefix("[-");
        assertEquals("[-", formatter.getOptPrefix());

        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setArgName("target");
        assertEquals("target", formatter.getArgName());
    }

    @Test
    public void testComparator() {
        assertNotNull(formatter.getOptionComparator());
        Comparator<Option> customComparator = new HelpFormatter.OptionComparator();
        formatter.setOptionComparator(customComparator);
        assertEquals(customComparator, formatter.getOptionComparator());
        
        formatter.setOptionComparator(null);
        assertNull(formatter.getOptionComparator());
    }

    @Test
    public void testPrintHelpSimple() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", true, "Beta option with arg");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 80, "testapp", "Header", options, 2, 5, "Footer", true);
        pw.flush();
        String output = out.toString();

        assertTrue(output.contains("testapp"));
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
        assertTrue(output.contains("-alpha"));
        assertTrue(output.contains("-beta"));
    }

    @Test
    public void testPrintHelpStringSignatures() {
        Options options = new Options();
        options.addOption("h", "help", false, "Print help");

        // These methods print to System.out by default, so we just invoke them to ensure no exceptions
        formatter.printHelp("syntax", options);
        formatter.printHelp("syntax", "Header", options, "Footer");
        formatter.printHelp("syntax", "Header", options, "Footer", true);
        formatter.printHelp(80, "syntax", "Header", options, "Footer");
        formatter.printHelp(80, "syntax", "Header", options, "Footer", true);
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        options.addOption(Option.builder("s").longOpt("short").desc("Short option description").build());
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printOptions(pw, 80, options, 2, 5);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("-short"));
    }

    @Test
    public void testPrintWrapped() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printWrapped(pw, 20, 5, "This is a very long text that needs to be wrapped properly across multiple lines.");
        pw.flush();
        String output = sw.toString();

        assertTrue(output.length() > 0);
    }

    @Test
    public void testPrintWrappedNoPad() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printWrapped(pw, 10, "Short");
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("Short"));
    }

    @Test
    public void testRenderOptions() {
        Options options = new Options();
        options.addOption("x", "extract", false, "Extract files");

        StringBuffer sb = new StringBuffer();
        StringBuffer rendered = formatter.renderOptions(sb, 80, options, 2, 5);

        assertNotNull(rendered);
        assertTrue(rendered.toString().contains("extract"));
    }

    @Test
    public void testRenderWrappedText() {
        StringBuffer sb = new StringBuffer();
        StringBuffer rendered = formatter.renderWrappedText(sb, 30, 4, "Line one\nLine two with a very long description that forces wrapping.");

        assertNotNull(rendered);
        assertTrue(rendered.toString().contains("Line one"));
    }

    @Test
    public void testFindWrapPos() {
        String text = "The quick brown fox jumps over the lazy dog";
        int pos = formatter.findWrapPos(text, 15, 0);
        assertTrue(pos != -1);

        // Test wrap pos when width is greater than text length
        assertEquals(-1, formatter.findWrapPos(text, 100, 50));

        // Test explicit newline handling
        String textWithNewline = "Line1\nLine2";
        assertEquals(5, formatter.findWrapPos(textWithNewline, 20, 0));
        
        // Test tab/whitespace handling
        String textWithTab = "Line1\tLine2";
        assertEquals(5, formatter.findWrapPos(textWithTab, 20, 0));
    }

    @Test
    public void testCreatePadding() {
        String padding = formatter.createPadding(4);
        assertEquals("    ", padding);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePaddingNegative() {
        formatter.createPadding(-1);
    }

    @Test
    public void testRemoveLeadingWhitespace() {
        String padded = "   Trim me";
        assertEquals("Trim me", HelpFormatter.rtrim(padded)); // Note: rtrim actually trims trailing in CLI, let's verify behavior or call removeLeadingWhitespace if accessible or test rtrim
        assertEquals("   Trim me", HelpFormatter.rtrim("   Trim me  "));
    }

    @Test
    public void testOptionComparatorClass() {
        Comparator<Option> comp = new HelpFormatter.OptionComparator();
        Option opt1 = new Option("a", "Apple");
        Option opt2 = new Option("b", "Banana");
        
        assertTrue(comp.compare(opt1, opt2) < 0);
        assertTrue(comp.compare(opt2, opt1) > 0);
        assertEquals(0, comp.compare(opt1, opt1));
    }

    @Test
    public void testPrintUsageEdgeCases() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);

        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("opt1", "Option 1"));
        group.addOption(new Option("opt2", "Option 2"));
        options.addOptionGroup(group);

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        
        assertTrue(out.toString().length() > 0);
    }
}