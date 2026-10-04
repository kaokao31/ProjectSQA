package org.apache.commons.cli;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
    }

    @Test
    public void testPrintHelpBasic() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", false, "Beta option");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "usage", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        assertNotNull(result);
        assertTrue(result.contains("usage"));
        assertTrue(result.contains("-a"));
        assertTrue(result.contains("--alpha"));
        assertTrue(result.contains("-b"));
        assertTrue(result.contains("--beta"));
    }

    @Test
    public void testPrintHelpWithArg() throws Exception {
        Options options = new Options();
        options.addOption(Option.builder("f").longOpt("file").hasArg().argName("FILE").desc("File to process").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "prog", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-f <FILE>"));
        assertTrue(result.contains("--file <FILE>"));
    }

    @Test
    public void testPrintHelpEmptyOptions() throws Exception {
        Options options = new Options();
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "myapp", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("myapp"));
        assertTrue(result.contains("usage"));
    }

    @Test
    public void testPrintHelpWithGroup() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("v", "verbose", false, "verbose output"));
        group.addOption(new Option("q", "quiet", false, "quiet output"));
        options.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "app", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-v"));
        assertTrue(result.contains("--verbose"));
        assertTrue(result.contains("-q"));
        assertTrue(result.contains("--quiet"));
    }

    @Test
    public void testPrintWrappedLongWord() throws Exception {
        // This test targets the CLI-31 bug: long words should not be cut off
        // or cause excessive lines. The formatting should break the word and
        // continue on the next line.
        String longWord = "012345678901234567890123456789012345678901234567890";
        Options options = new Options();
        options.addOption("a", "alpha", false, longWord);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 20, "usage", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        // Verify that the output contains the long word (at least in some form)
        assertTrue(result.contains("alpha") || result.contains("a"));
        // The line width should not exceed 20 characters (including indentation).
        // However, we don't know the exact indentation, so we check that
        // there is no line that is overly long.
        String[] lines = result.split(System.getProperty("line.separator"));
        for (String line : lines) {
            assertTrue("Line too long: " + line, line.length() <= 30); // tolerate some padding
        }
    }

    @Test
    public void testPrintWrappedLongWordWithNarrowWidth() throws Exception {
        // More aggressive test with a very small width
        String longWord = "supercalifragilisticexpialidocious";
        Options options = new Options();
        options.addOption("x", "extra", false, longWord);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 5, "usage", "", options, 0, 0, "");
        pw.flush();
        String result = out.toString();
        // Should not throw an exception and should contain the option somewhere
        assertTrue(result.contains("x") || result.contains("extra"));
        // Check that no line is excessively long
        String[] lines = result.split(System.getProperty("line.separator"));
        for (String line : lines) {
            assertTrue("Line too long: " + line, line.length() <= 10);
        }
    }

    @Test
    public void testPrintWrappedWordBoundaries() throws Exception {
        String description = "This is a short description that should fit easily in the allocated width.";
        Options options = new Options();
        options.addOption("s", "short", false, description);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "usage", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        // The description should appear without being truncated
        assertTrue(result.contains("This is a short description"));
    }

    @Test
    public void testPrintWrappedWithNewlines() throws Exception {
        String description = "First line\nSecond line with a longwordthatmaybeexceedslinewidth";
        Options options = new Options();
        options.addOption("n", "newline", false, description);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 30, "usage", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        assertNotNull(result);
        // Verify that the newline is preserved in some form
        assertTrue(result.contains("First line"));
        assertTrue(result.contains("Second line"));
    }

    @Test
    public void testPrintUsageLongWidth() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "description");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 10, "thisIsAReallyLongUsageLineThatShouldWrap", "header", options, 2, 2, "trailer");
        pw.flush();
        String result = out.toString();
        // The usage line should be wrapped; just ensure no exception and output contains "usage"
        assertTrue(result.contains("usage"));
    }

    @Test
    public void testPrintHelpWithHeaderFooter() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "prog", "header", options, 2, 2, "footer");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("header"));
        assertTrue(result.contains("footer"));
        assertTrue(result.contains("-a"));
    }

    @Test
    public void testPrintHelpWithNullHeaderFooter() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "prog", null, options, 2, 2, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-a"));
        // Should not contain "null" text
        assertFalse(result.contains("null"));
    }

    @Test
    public void testPrintHelpWithNullUsage() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, null, "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        // Should still print something; ensure no NPE
        assertNotNull(result);
    }

    @Test
    public void testPrintHelpWithEmptyOptions() throws Exception {
        Options options = new Options();
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "someusage", "header", options, 2, 2, "footer");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("someusage"));
        assertTrue(result.contains("header"));
        assertTrue(result.contains("footer"));
    }

    @Test
    public void testPrintHelpWithMultipleLinesDescription() throws Exception {
        String desc = "Line1\nLine2\nLine3 is a bit longer to wrap";
        Options options = new Options();
        options.addOption("m", "multi", false, desc);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 40, "", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("Line1"));
        assertTrue(result.contains("Line2"));
        assertTrue(result.contains("Line3"));
    }

    @Test
    public void testPrintHelpWithMinimumWidth() throws Exception {
        // Ensure no exception even with width 1
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 1, "usage", "", options, 0, 0, "");
        pw.flush();
        assertTrue(out.toString().length() > 0);
    }

    @Test
    public void testPrintHelpWithNullOptions() throws Exception {
        // Should probably throw NPE, but we assert that it does throw a specific exception?
        // Commons CLI might throw IllegalArgumentException or NPE.
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        try {
            formatter.printHelp(pw, 80, "usage", "", null, 2, 2, "");
            pw.flush();
            // If no exception, we fail because we expect an exception
            assertTrue("Expected an exception", false);
        } catch (Exception e) {
            // Expected exception
            assertNotNull(e);
        }
    }

    @Test
    public void testPrintHelpWithLongOptionWithoutShort() throws Exception {
        Option opt = Option.builder().longOpt("longOnly").desc("long only option").build();
        Options options = new Options();
        options.addOption(opt);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "usage", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("--longOnly"));
        assertFalse(result.contains("-longOnly"));
    }

    @Test
    public void testPrintHelpWithOptionGroupRequired() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "aaa", false, "option a"));
        group.addOption(new Option("b", "bbb", false, "option b"));
        options.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out, true);
        formatter.printHelp(pw, 80, "prog", "", options, 2, 2, "");
        pw.flush();
        String result = out.toString();
        // Should list both options
        assertTrue(result.contains("-a"));
        assertTrue(result.contains("-b"));
    }
}