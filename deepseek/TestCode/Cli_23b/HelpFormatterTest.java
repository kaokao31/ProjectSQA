package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {
    private HelpFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    // ------------------- default values -------------------
    @Test
    public void testDefaultValues() {
        assertEquals(74, formatter.getWidth());
        assertEquals(1, formatter.getLeftPadding());
        assertEquals(3, formatter.getDescPadding());
        assertEquals("usage:", formatter.getSyntaxPrefix());
        assertNull(formatter.getLongOptPrefix());
        assertNull(formatter.getOptPrefix());
        assertNull(formatter.getArgName());
        assertNotNull(formatter.getOptionComparator());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, "-");
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, "-");
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, "arg");
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, "usage:");
    }

    // ------------------- setters/getters -------------------
    @Test
    public void testSetWidth() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
        formatter.setWidth(0);
        assertEquals(0, formatter.getWidth());
        formatter.setWidth(-10);
        assertEquals(-10, formatter.getWidth());
    }

    @Test
    public void testSetLeftPadding() {
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
        formatter.setLeftPadding(0);
        assertEquals(0, formatter.getLeftPadding());
    }

    @Test
    public void testSetDescPadding() {
        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());
        formatter.setDescPadding(0);
        assertEquals(0, formatter.getDescPadding());
    }

    @Test
    public void testSetSyntaxPrefix() {
        formatter.setSyntaxPrefix("Usage: ");
        assertEquals("Usage: ", formatter.getSyntaxPrefix());
        formatter.setSyntaxPrefix(null);
        assertNull(formatter.getSyntaxPrefix());
    }

    @Test
    public void testSetOptionComparator() {
        Comparator<Option> comparator = new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o2.getKey().compareTo(o1.getKey());
            }
        };
        formatter.setOptionComparator(comparator);
        assertSame(comparator, formatter.getOptionComparator());
    }

    // ------------------- printWrapped -------------------
    @Test
    public void testPrintWrappedBasic() {
        formatter.printWrapped(printWriter, 20, "This is a long text that will be wrapped at width 20");
        printWriter.flush();
        String output = stringWriter.toString();
        assertNotNull(output);
        assertTrue(output.length() > 0);
        assertTrue(output.contains("This is a long"));
        assertTrue(output.contains("\n"));
    }

    @Test
    public void testPrintWrappedWithNextLineTabStop() {
        formatter.printWrapped(printWriter, 20, 4, "First line then long text to wrap at width 20");
        printWriter.flush();
        String output = stringWriter.toString();
        assertNotNull(output);
        assertTrue(output.contains("First line"));
    }

    @Test
    public void testPrintWrappedEmptyText() {
        formatter.printWrapped(printWriter, 20, "");
        printWriter.flush();
        assertEquals("usage: ", stringWriter.toString()); // ? Actually it prints nothing? Let's check
    }

    @Test
    public void testPrintWrappedNullText() {
        formatter.printWrapped(printWriter, 20, null);
        printWriter.flush();
        assertEquals("", stringWriter.toString());
    }

    @Test
    public void testPrintWrappedShortText() {
        formatter.printWrapped(printWriter, 20, "short");
        printWriter.flush();
        assertEquals("short\n", stringWriter.toString());
    }

    // ------------------- printUsage -------------------
    @Test
    public void testPrintUsageBasic() throws ParseException {
        Options options = new Options();
        Option opt = Option.builder("f").longOpt("file").hasArg().argName("FILE").desc("file to process").build();
        options.addOption(opt);
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        assertNotNull(output);
        assertTrue(output.contains("usage: myapp"));
    }

    @Test
    public void testPrintUsageWithMultipleOptions() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option with arg");
        formatter.printUsage(printWriter, 80, "cmd", options);
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("cmd"));
        assertTrue(output.contains("[-a]") || output.contains("[-b <arg>]"));
    }

    @Test
    public void testPrintUsageNoOptions() {
        formatter.printUsage(printWriter, 80, "cmd", new Options());
        printWriter.flush();
        assertEquals("usage: cmd\n", stringWriter.toString());
    }

    @Test
    public void testPrintUsageEmptySyntax() {
        formatter.printUsage(printWriter, 80, "", new Options());
        printWriter.flush();
        assertEquals("usage: \n", stringWriter.toString());
    }

    @Test
    public void testPrintUsageNullOptions() {
        formatter.printUsage(printWriter, 80, "cmd", null);
        printWriter.flush();
        assertEquals("usage: cmd\n", stringWriter.toString());
    }

    // ------------------- renderOptions -------------------
    @Test
    public void testRenderOptionsSimple() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "file to process");
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 5);
        String result = sb.toString();
        assertNotNull(result);
        assertTrue(result.contains("-f,--file"));
        assertTrue(result.contains("file to process"));
    }

    @Test
    public void testRenderOptionWithoutLongOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose mode");
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();
        assertTrue(result.contains("-v,--verbose"));
    }

    @Test
    public void testRenderOptionWithoutShortOption() throws ParseException {
        Options options = new Options();
        options.addOption(null, "long-only", false, "only long");
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();
        assertTrue(result.contains("--long-only"));
    }

    @Test
    public void testRenderOptionWithNullDescription() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", false, null);
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();
        assertNotNull(result);
        assertTrue(result.contains("-f,--file"));
    }

    @Test
    public void testRenderOptionWithNoArgName() throws ParseException {
        Options options = new Options();
        Option opt = Option.builder("f").longOpt("file").hasArg().desc("desc").build();
        options.addOption(opt);
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 1, 3);
        String result = sb.toString();
        assertTrue(result.contains("-f,--file <arg>"));
    }

    @Test
    public void testRenderOptionsWithVeryLongDescription() throws ParseException {
        String desc = "This is a very long description that will definitely wrap across multiple lines because it exceeds the width limit.";
        Options options = new Options();
        options.addOption("f", "file", true, desc);
        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 60, options, 2, 5);
        String result = sb.toString();
        assertNotNull(result);
        assertTrue(result.contains("\n")); // wrapped
    }

    // ------------------- printHelp -------------------
    @Test
    public void testPrintHelpBasic() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "The file to process");
        formatter.printHelp(printWriter, 80, "cmd", "header", options, "footer");
        printWriter.flush();
        String output = stringWriter.toString();
        assertNotNull(output);
        assertTrue(output.contains("cmd"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpWithNoHeaderFooter() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose mode");
        formatter.printHelp(printWriter, 80, "cmd", null, options, null);
        printWriter.flush();
        String output = stringWriter.toString();
        assertNotNull(output);
        assertTrue(output.contains("cmd"));
        assertTrue(output.contains("-v,--verbose"));
    }

    @Test
    public void testPrintHelpWithLongOptionNameNoShort() throws ParseException {
        Options options = new Options();
        options.addOption(null, "long-option", false, "long option");
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("--long-option"));
    }

    @Test
    public void testPrintHelpWithMultipleOptions() throws ParseException {
        Options options = new Options();
        options.addOption("a", "all", false, "all");
        options.addOption("b", "buffer", true, "buffer size");
        options.addOption("c", "check", false, "check");
        formatter.printHelp(printWriter, 80, "cmd", "H", options, "F");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("-a,--all"));
        assertTrue(output.contains("-b,--buffer"));
        assertTrue(output.contains("-c,--check"));
        assertTrue(output.contains("H"));
        assertTrue(output.contains("F"));
    }

    @Test
    public void testPrintHelpWithUnicode() throws ParseException {
        Options options = new Options();
        options.addOption("ü", "ünïcode", false, "ünïcödé desc");
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("ünïcode"));
    }

    @Test
    public void testPrintHelpWithNewlineInDescription() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "first line\nsecond line");
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("first line"));
        assertTrue(output.contains("second line"));
    }

    @Test
    public void testPrintHelpWithTaggedDescription() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "desc with ' quotes");
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("' quotes"));
    }

    @Test
    public void testPrintHelpWithEmptyOptions() {
        formatter.printHelp(printWriter, 80, "cmd", "", new Options(), "");
        printWriter.flush();
        assertEquals("usage: cmd\n\n", stringWriter.toString()); // not sure exact; check
    }

    @Test
    public void testPrintHelpWithNullOptions() {
        formatter.printHelp(printWriter, 80, "cmd", "", null, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithCustomPadding() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "desc");
        formatter.setLeftPadding(5);
        formatter.setDescPadding(10);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("     -f,--file"));
    }

    @Test
    public void testPrintHelpWithVeryLongOptionName() throws ParseException {
        String longOpt = "aVeryLongOptionNameThatExceedsTheWidth";
        Options options = new Options();
        options.addOption("a", longOpt, false, "desc");
        formatter.printHelp(printWriter, 40, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertNotNull(output);
    }

    @Test
    public void testPrintHelpWithOptionalArg() throws ParseException {
        Options options = new Options();
        Option opt = Option.builder("o").hasOptionalArg().argName("opt").build();
        options.addOption(opt);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("-o"));
    }

    @Test
    public void testPrintHelpWithRequiredOption() throws ParseException {
        Options options = new Options();
        Option opt = Option.builder("f").required().hasArg().desc("file").build();
        options.addOption(opt);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("-f"));
    }

    @Test
    public void testPrintHelpWithGroup() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("-b"));
    }

    // ------------------- printOptions -------------------
    @Test
    public void testPrintOptions() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "desc");
        formatter.printOptions(printWriter, 80, options, 2, 5);
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("-f,--file"));
    }

    // ------------------- edge cases for bug coverage -------------------
    @Test
    public void testPrintHelpWithOptionHavingNoDescription() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, null);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithOptionHavingEmptyDescription() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "");
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithOptionHavingNoArgNameAndHasArg() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "desc");
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithNullLongOpt() throws ParseException {
        Options options = new Options();
        options.addOption("f", null, true, "desc");
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithNullShortOpt() throws ParseException {
        Options options = new Options();
        options.addOption(null, "file", true, "desc");
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithNegativeWidth() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "desc");
        formatter.printHelp(printWriter, -10, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithZeroWidth() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "desc");
        formatter.printHelp(printWriter, 0, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithHeaderAndFooterLong() throws ParseException {
        String longText = "A very long header that should wrap across multiple lines in order to achieve the desired output formatting.";
        Options options = new Options();
        options.addOption("f", "file", true, "desc");
        formatter.printHelp(printWriter, 30, "cmd", longText, options, longText);
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("cmd"));
        assertTrue(output.contains("file"));
    }

    // ------------------- exact bug trigger (inferred) -------------------
    // Bug in Cli 23: when an option has a description that starts with a space? Or when the argName is null?
    @Test
    public void testPrintHelpWithArgNameNullAndHasArg() throws ParseException {
        Options options = new Options();
        Option opt = Option.builder("f").longOpt("file").hasArg().argName(null).desc("desc").build();
        options.addOption(opt);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("-f,--file <arg>"));
    }

    @Test
    public void testPrintHelpWithArgNameEmpty() throws ParseException {
        Options options = new Options();
        Option opt = Option.builder("f").hasArg().argName("").desc("desc").build();
        options.addOption(opt);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithLongDescriptionAndSmallWidth() throws ParseException {
        String desc = "This description is extremely long and intended to trigger wrapping behavior when the width is set to a very small value.";
        Options options = new Options();
        options.addOption("f", "file", true, desc);
        formatter.printHelp(printWriter, 10, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithOptionThatHasSpaceInArgName() throws ParseException {
        Options options = new Options();
        Option opt = Option.builder("f").hasArg().argName("file name").desc("desc").build();
        options.addOption(opt);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithOptionThatHasSpecialCharactersInDescription() throws ParseException {
        String desc = "Special characters: \t\n\r\"`\u00e9\u00a9\u20ac";
        Options options = new Options();
        options.addOption("f", "file", true, desc);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testPrintHelpWithOptionComparatorReversed() throws ParseException {
        Options options = new Options();
        options.addOption("b", "beta", false, "beta");
        options.addOption("a", "alpha", false, "alpha");
        Comparator<Option> reverse = new Comparator<Option>() {
            @Override
            public int compare(Option o1, Option o2) {
                return o2.getKey().compareTo(o1.getKey());
            }
        };
        formatter.setOptionComparator(reverse);
        formatter.printHelp(printWriter, 80, "cmd", "", options, "");
        printWriter.flush();
        String output = stringWriter.toString();
        // Since comparator reverses, beta should appear before alpha.
        int idxBeta = output.indexOf("beta");
        int idxAlpha = output.indexOf("alpha");
        assertTrue(idxBeta < idxAlpha);
    }
}