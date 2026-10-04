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
        assertEquals(" ", HelpFormatter.DEFAULT_ARG_NAME);
        assertEquals(74, HelpFormatter.DEFAULT_WIDTH);
        assertEquals(1, HelpFormatter.DEFAULT_LEFT_PAD);
        assertEquals(3, HelpFormatter.DEFAULT_DESC_PAD);
        assertEquals("usage: ", HelpFormatter.DEFAULT_SYNTAX_PREFIX);
    }

    @Test
    public void testGettersAndSetters() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(10);
        assertEquals(10, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());

        formatter.setArgName("argument");
        assertEquals("argument", formatter.getArgName());
    }

    @Test
    public void testFindWrapPos() {
        String text = "This is a test string to check wrapping positions properly.";
        
        // normal wrap within width
        int pos = formatter.findWrapPos(text, 10, 0);
        assertTrue(pos != -1);
        assertTrue(pos <= 10);

        // startPos greater than text length
        assertEquals(-1, formatter.findWrapPos(text, 10, 100));

        // wrapPos within startPos + width exceeding text length
        int wrapPos = formatter.findWrapPos("short", 20, 0);
        assertEquals(-1, wrapPos);

        // newline character encountered
        String textWithNewline = "line1\nline2";
        assertEquals(5, formatter.findWrapPos(textWithNewline, 20, 0));

        // tab character encountered
        String textWithTab = "line1\tline2";
        assertEquals(5, formatter.findWrapPos(textWithTab, 20, 0));

        // no space found, forces wrap at width
        String noSpaces = "123456789012345";
        assertEquals(5, formatter.findWrapPos(noSpaces, 5, 0));
        
        // startPos + width finds whitespace exactly at width
        assertEquals(5, formatter.findWrapPos("12345 7890", 5, 0));
    }

    @Test
    public void testRpad() {
        assertEquals("abc  ", formatter.rpad("abc", 5));
        assertEquals("abc", formatter.rpad("abc", 2));
    }

    @Test
    public void testCreatePadding() {
        assertEquals("   ", formatter.createPadding(3));
        
        try {
            formatter.createPadding(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRemoveLeadingSpaces() {
        assertEquals("abc", formatter.removeLeadingSpaces("   abc"));
        assertEquals("abc", formatter.removeLeadingSpaces("abc"));
        assertEquals("", formatter.removeLeadingSpaces("   "));
        assertEquals("", formatter.removeLeadingSpaces(""));
    }

    @Test(expected = NullPointerException.class)
    public void testPrintHelpNullCommand() {
        PrintWriter pw = new PrintWriter(new StringWriter());
        formatter.printHelp(pw, 80, null, "header", new Options(), 2, 5, "footer");
    }

    @Test
    public void testPrintHelpBasic() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", true, "Beta option with argument");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 80, "testCmd", "Header text", options, 2, 5, "Footer text", true);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("usage: testCmd"));
        assertTrue(output.contains("Header text"));
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("Footer text"));
    }

    @Test
    public void testPrintHelpWithoutHeaderAndFooter() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);

        formatter.printHelp(pw, 80, "testCmd", null, options, 2, 5, null);
        pw.flush();

        String output = out.toString();
        assertTrue(output.contains("usage: testCmd"));
        assertTrue(output.contains("-a,--alpha"));
    }

    @Test
    public void testPrintHelpWithOptionsOnly() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");

        formatter.printHelp("testCmd", options);
        formatter.printHelp("testCmd", "Header", options, "Footer");
        formatter.printHelp("testCmd", "Header", options, "Footer", true);
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("x", "x-opt", false, "X option");
        Option opt2 = new Option("y", "y-opt", false, "Y option");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        options.addOption(new Option("z", "z-opt", true, "Z option with long description that needs wrapping across multiple lines to test the wrap functionality properly."));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printOptions(pw, 40, options, 2, 5);
        pw.flush();

        assertTrue(sw.toString().length() > 0);
    }

    @Test
    public void testRenderOptions() {
        Options options = new Options();
        options.addOption("s", "short", false, "Short description");

        StringBuffer sb = new StringBuffer();
        StringBuffer rendered = formatter.renderOptions(sb, 80, options, 2, 5);
        assertNotNull(rendered);
        assertTrue(rendered.toString().contains("-s,--short"));
    }

    @Test
    public void testRenderWrappedText() {
        StringBuffer sb = new StringBuffer();
        String text = "This is a very long line of text that should be wrapped correctly by the HelpFormatter when rendering wrapped text method is called.";
        
        StringBuffer result = formatter.renderWrappedText(sb, 40, 5, text);
        assertNotNull(result);
        assertTrue(result.toString().length() > 0);
    }

    @Test
    public void testOptionComparator() {
        Comparator<Option> comparator = formatter.getOptionComparator();
        assertNotNull(comparator);

        Option opt1 = new Option("b", "beta", false, "B");
        Option opt2 = new Option("a", "alpha", false, "A");
        
        assertTrue(comparator.compare(opt1, opt2) > 0);
        assertTrue(comparator.compare(opt2, opt1) < 0);
        assertEquals(0, comparator.compare(opt1, opt1));

        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
        
        assertNotNull(formatter.getOptionComparator());
    }
}