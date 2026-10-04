package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private Options options;
    private ByteArrayOutputStream outContent;
    private PrintWriter pw;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        options = new Options();
        outContent = new ByteArrayOutputStream();
        pw = new PrintWriter(outContent);
    }

    // ---------- Helper methods ----------
    private String getOutput() {
        pw.flush();
        return outContent.toString();
    }

    private void printHelp(String cmdLineSyntax, String header, Options opts, String footer, int width) {
        formatter.printHelp(pw, width, cmdLineSyntax, header, opts, formatter.getLeftPadding(),
                formatter.getDescPadding(), footer, true);
    }

    private void printHelp(String cmdLineSyntax, String header, Options opts, String footer) {
        formatter.printHelp(pw, formatter.getWidth(), cmdLineSyntax, header, opts, formatter.getLeftPadding(),
                formatter.getDescPadding(), footer, true);
    }

    // ---------- Default behavior ----------
    @Test
    public void testDefaultWidth() {
        assertEquals(74, formatter.getWidth());
        assertEquals(1, formatter.getLeftPadding());
        assertEquals(3, formatter.getDescPadding());
        assertEquals("usage: ", formatter.getSyntaxPrefix());
        assertEquals("--", formatter.getLongOptPrefix());
        assertEquals("-", formatter.getShortOptPrefix());
        assertEquals("arg", formatter.getArgName());
        assertNotNull(formatter.getNewLine());
        assertEquals("line.separator", System.getProperty("line.separator"), formatter.getNewLine());
        assertNull(formatter.getOptPrefix());
        assertNull(formatter.getLongOptPrefix());
        assertNull(formatter.getArgName());
        assertNull(formatter.getDescriptionPadding());
        assertNull(formatter.getLeftPadding());
        assertNull(formatter.getWidth());
        assertNull(formatter.getSyntaxPrefix());
        assertNull(formatter.getHelpOptions());
    }

    @Test
    public void testDefaultPrintHelp() {
        String cmdLineSyntax = "myapp";
        printHelp(cmdLineSyntax, null, new Options(), null);
        String output = getOutput();
        assertEquals("usage: myapp\n", output);
    }

    @Test
    public void testNullHeaderFooter() {
        options.addOption("a", "alpha", false, "Alpha option");
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("usage: app"));
        assertTrue(output.contains("-a,--alpha"));
    }

    // ---------- Options handling ----------
    @Test
    public void testPrintHelpWithOptions() {
        options.addOption("a", "alpha", false, "Alpha");
        options.addOption("b", "beta", true, "Beta needs arg");
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("usage: app"));
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("-b,--beta <arg>"));
        assertTrue(output.contains("Alpha"));
        assertTrue(output.contains("Beta needs arg"));
    }

    @Test
    public void testPrintHelpWithHeaderAndFooter() {
        options.addOption("h", "help", false, "print help");
        printHelp("app", "HEADER", options, "FOOTER");
        String output = getOutput();
        assertTrue(output.contains("HEADER"));
        assertTrue(output.contains("FOOTER"));
    }

    // ---------- OptionGroup ----------
    @Test
    public void testPrintOptionGroup() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "Option A"));
        group.addOption(new Option("b", "beta", false, "Option B"));
        group.setRequired(true);
        options.addOptionGroup(group);
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("-b,--beta"));
        assertTrue(output.contains("[ -a,--alpha | -b,--beta ]"));
    }

    // ---------- Padding and width ----------
    @Test
    public void testCustomWidth() {
        options.addOption("a", "alpha", false, "Short desc");
        formatter.printHelp(pw, 20, "app", null, options, 1, 3, null, true);
        String output = getOutput();
        assertTrue(output.contains("usage: app"));
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("Short desc"));
    }

    @Test
    public void testSetWidth() {
        formatter.setWidth(80);
        assertEquals(80, formatter.getWidth());
        options.addOption("a", "alpha", false, "Short desc");
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        assertTrue(output.contains("usage: app"));
    }

    @Test
    public void testSetLeftPadding() {
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
        options.addOption("a", "alpha", false, "Short desc");
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        assertTrue(output.contains("     -a,--alpha"));
    }

    @Test
    public void testSetDescPadding() {
        formatter.setDescPadding(2);
        assertEquals(2, formatter.getDescPadding());
        options.addOption("a", "alpha", false, "Short desc");
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        assertTrue(output.contains("Short desc"));
    }

    // ---------- Long descriptions and wrapping ----------
    @Test
    public void testLongDescriptionWrapping() {
        String longDesc = "This is a very long description that will definitely exceed the width of " +
                "the help output and should be wrapped across multiple lines for readability.";
        options.addOption("a", "alpha", false, longDesc);
        formatter.setWidth(40);
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        String[] lines = output.split(System.getProperty("line.separator"));
        // First line is usage
        // For each option line, ensure we have at least one line with the option and description.
        boolean foundOption = false;
        for (String line : lines) {
            if (line.contains("-a") && line.contains("--alpha")) {
                foundOption = true;
                break;
            }
        }
        assertTrue(foundOption);
        // The description should be present in the output
        assertTrue(output.contains("This is a very long description"));
    }

    @Test
    public void testWrapPosMethod() {
        // Indirectly tested via printHelp with a long option and small width
        options.addOption("a", "alpha", false, "A description that is very long and must wrap correctly");
        formatter.setWidth(20);
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("A description that is very"));
        assertTrue(output.contains("long and must wrap"));
        assertTrue(output.contains("correctly"));
    }

    // ---------- Null/empty options ----------
    @Test
    public void testNullOptions() {
        try {
            formatter.printHelp(pw, "app", null);
            // If no options, just usage should be printed
            String output = getOutput();
            assertEquals("usage: app\n", output);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testEmptyOptions() {
        options = new Options();
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        assertEquals("usage: app\n", output);
    }

    // ---------- Specific option handling ----------
    @Test
    public void testOptionWithNoShortOpt() {
        options.addOption(Option.builder("longOnly").longOpt("longOnly").hasArg(false)
                .desc("Long only option").build());
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("--longOnly"));
        assertTrue(output.contains("Long only option"));
    }

    @Test
    public void testOptionWithNoLongOpt() {
        options.addOption("s", "short", false, "Short only");
        options.addOption("x", null, false, "Only short x");
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-s"));
        assertTrue(output.contains("-x"));
    }

    @Test
    public void testOptionWithArgName() {
        options.addOption(Option.builder("f").longOpt("file").hasArg().argName("FILE")
                .desc("File to process").build());
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-f,--file <FILE>"));
        assertTrue(output.contains("File to process"));
    }

    @Test
    public void testRequiredOption() {
        options.addOption(Option.builder("r").longOpt("required").required(true)
                .desc("Required option").build());
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-r,--required"));
        assertTrue(output.contains("Required option"));
    }

    @Test
    public void testOptionWithDescriptionBlank() {
        options.addOption("b", "blank", false, "");
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-b,--blank"));
    }

    @Test
    public void testOptionWithDescriptionNull() {
        options.addOption("n", "null", false, null);
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-n,--null"));
    }

    // ---------- printUsage ----------
    @Test
    public void testPrintUsage() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        options.addOption("a", "alpha", true, "Alpha arg");
        formatter.printUsage(pw, 80, "cmd [options]");
        pw.flush();
        String output = out.toString();
        assertEquals("usage: cmd [options]\n", output);
    }

    @Test
    public void testPrintUsageWithOptions() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        options.addOption("a", "alpha", true, "Alpha arg");
        options.addOption("b", "beta", false, "Beta flag");
        formatter.printUsage(pw, 80, "cmd", options);
        pw.flush();
        String output = out.toString();
        assertTrue(output.contains("usage: cmd"));
        assertTrue(output.contains("-a <arg>"));
        assertTrue(output.contains("-b"));
    }

    // ---------- printOptions ----------
    @Test
    public void testPrintOptions() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        options.addOption("a", "alpha", false, "Alpha");
        options.addOption("b", "beta", true, "Beta");
        formatter.printOptions(pw, formatter.getWidth(), options, formatter.getLeftPadding(),
                formatter.getDescPadding());
        pw.flush();
        String output = out.toString();
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("-b,--beta <arg>"));
    }

    // ---------- printWrapped ----------
    @Test
    public void testPrintWrapped() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 20, "This is a long string that should be wrapped");
        pw.flush();
        String output = out.toString();
        assertTrue(output.contains("This is a long"));
        assertTrue(output.contains("string that should"));
        assertTrue(output.contains("be wrapped"));
    }

    @Test
    public void testPrintWrappedWithPad() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 20, 2, "This is a long string");
        pw.flush();
        String output = out.toString();
        assertTrue(output.contains("  This is a long"));
    }

    // ---------- Edge cases ----------
    @Test
    public void testWidthZero() {
        options.addOption("a", "alpha", false, "desc");
        try {
            formatter.setWidth(0);
            formatter.printHelp(pw, "app", options);
            String output = getOutput();
            // Should still print something, but probably not wrap correctly
            assertTrue(output.contains("usage: app"));
        } catch (IllegalArgumentException e) {
            // Accept if setWidth throws; but we expect not to throw for zero width
            fail("Should not throw for width=0");
        }
    }

    @Test
    public void testWidthNegative() {
        try {
            formatter.setWidth(-1);
            // No validation in some versions; we may just print
            options.addOption("a", "alpha", false, "desc");
            formatter.printHelp(pw, "app", options);
            String output = getOutput();
            assertTrue(output.contains("usage: app"));
        } catch (IllegalArgumentException e) {
            // Accept if setWidth throws
        }
    }

    @Test
    public void testLargeWidth() {
        options.addOption("a", "alpha", false, "desc");
        formatter.setWidth(200);
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("usage: app"));
        assertTrue(output.contains("-a,--alpha"));
    }

    // ---------- Find wrap position specifics ----------
    @Test
    public void testFindWrapPosWithNoSpaces() {
        // Input with no spaces should break at width-1
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 10, "abcdefghijklmnop");
        pw.flush();
        String output = out.toString();
        // Expect wrapping at character 9 (0-index) => first line "abcdefghi"
        // second line "jklmnop"
        assertTrue(output.contains("abcdefghi"));
        assertTrue(output.contains("jklmnop"));
    }

    @Test
    public void testFindWrapPosAtLimit() {
        // Width exactly equal to string length
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 5, "abcde");
        pw.flush();
        String output = out.toString();
        assertEquals("abcde\n", output);
    }

    @Test
    public void testFindWrapPosWithLeadingSpaces() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 10, "  abcdefghij");
        pw.flush();
        String output = out.toString();
        // Should wrap at some point, but the leading spaces should be kept
        assertTrue(output.contains("  abcdefgh"));
        assertTrue(output.contains("ij"));
    }

    // ---------- Option formatting nuances ----------
    @Test
    public void testOptionWithMultipleValues() {
        options.addOption(Option.builder("m").longOpt("multi").hasArgs().desc("Multiple args").build());
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-m,--multi <arg>"));
    }

    @Test
    public void testOptionWithValueSeparator() {
        options.addOption(Option.builder("v").longOpt("value").hasArg().valueSeparator(',').desc("Values").build());
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-v,--value <arg>"));
    }

    @Test
    public void testOptionWithNoDescription() {
        options.addOption(Option.builder("d").hasArg(false).build());
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("-d"));
    }

    // ---------- Test setters and getters ----------
    @Test
    public void testSetSyntaxPrefix() {
        formatter.setSyntaxPrefix("Usage: ");
        assertEquals("Usage: ", formatter.getSyntaxPrefix());
        options.addOption("a", "alpha", false, "desc");
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.startsWith("Usage: app"));
    }

    @Test
    public void testSetNewLine() {
        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());
    }

    @Test
    public void testSetLongOptPrefix() {
        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());
        options.addOption(Option.builder("a").longOpt("alpha").desc("desc").build());
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("---alpha"));
    }

    @Test
    public void testSetShortOptPrefix() {
        formatter.setShortOptPrefix("+");
        assertEquals("+", formatter.getShortOptPrefix());
        options.addOption("a", "alpha", false, "desc");
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("+a"));
    }

    @Test
    public void testSetArgName() {
        formatter.setArgName("value");
        assertEquals("value", formatter.getArgName());
        options.addOption(Option.builder("a").hasArg().desc("desc").build());
        printHelp("app", null, options, null);
        String output = getOutput();
        assertTrue(output.contains("<value>"));
    }

    @Test
    public void testSetHelpOption() {
        Option helpOpt = new Option("?", "help", false, "Show help");
        formatter.setHelpOption(helpOpt);
        assertSame(helpOpt, formatter.getHelpOption());
    }

    @Test
    public void testRtrim() {
        // Indirectly tested through printHelp with trailing spaces
        options.addOption("a", "alpha", false, "desc   ");
        printHelp("app", null, options, null);
        String output = getOutput();
        assertFalse(output.contains("desc   "));
        assertTrue(output.contains("desc"));
    }

    // ---------- Potential bug: Cli-32 ----------
    // Bug might be in wrapping with long argument names or descriptions.
    @Test
    public void testCli32WrappingWithLongArgName() {
        // Create an option with a very long argument name, enough to cause wrapping issues.
        Option opt = Option.builder("a").longOpt("alpha").hasArg().argName("VERY_LONG_ARGUMENT_NAME").desc("desc").build();
        options.addOption(opt);
        formatter.setWidth(30);
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        // The option line might wrap; just check that the output contains both the option and the arg name.
        assertTrue(output.contains("--alpha <VERY_LONG_ARGUMENT_NAME>"));
    }

    @Test
    public void testCli32LongDescriptionWithLongArgName() {
        Option opt = Option.builder("a").longOpt("alpha").hasArg().argName("LONG").desc("This is a very long description that should wrap but still show the option and argument correctly with proper alignment.").build();
        options.addOption(opt);
        formatter.setWidth(40);
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        assertTrue(output.contains("--alpha <LONG>"));
        assertTrue(output.contains("This is a very long description"));
    }

    @Test
    public void testCli32OptionWithNoShortOptAndLongArgName() {
        Option opt = Option.builder().longOpt("alpha").hasArg().argName("A_LONG_ARGUMENT_NAME").desc("desc").build();
        options.addOption(opt);
        formatter.setWidth(30);
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        assertTrue(output.contains("--alpha <A_LONG_ARGUMENT_NAME>"));
    }

    @Test
    public void testCli32MultipleOptionsWithWrapping() {
        options.addOption(Option.builder("a").longOpt("alpha").hasArg().argName("ARG1").desc("desc1").build());
        options.addOption(Option.builder("b").longOpt("beta").hasArg().argName("A_VERY_LONG_ARGUMENT_NAME_EXCEEDING_WIDTH").desc("desc2").build());
        formatter.setWidth(30);
        formatter.printHelp(pw, "app", options);
        String output = getOutput();
        assertTrue(output.contains("--alpha"));
        assertTrue(output.contains("--beta"));
        assertTrue(output.contains("A_VERY_LONG_ARGUMENT_NAME_EXCEEDING_WIDTH"));
    }

    // ---------- Corner cases: empty command line syntax ----------
    @Test
    public void testEmptyCmdLineSyntax() {
        options.addOption("a", "alpha", false, "desc");
        formatter.printHelp(pw, "", options);
        String output = getOutput();
        // Should not throw; output should start with "usage: " and maybe nothing after.
        assertTrue(output.startsWith("usage: "));
    }

    @Test
    public void testNullCmdLineSyntax() {
        options.addOption("a", "alpha", false, "desc");
        try {
            formatter.printHelp(pw, null, options);
            String output = getOutput();
            // Some implementations may throw or print "usage: "
            assertNotNull(output);
        } catch (Exception e) {
            fail("Should not throw on null cmdLineSyntax");
        }
    }

    // ---------- PrintHelp with auto usage ----------
    @Test
    public void testPrintHelpAutoUsage() {
        options.addOption("a", "alpha", false, "desc");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        formatter.printHelp(writer, "app", options);
        writer.flush();
        String output = out.toString();
        assertTrue(output.contains("usage: app"));
    }

    // ---------- Verify that we cover renderOptions private method through printOptions ----------
    @Test
    public void testRenderOptionsWithEmptyOptions() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        formatter.printOptions(pw, formatter.getWidth(), new Options(), 1, 3);
        pw.flush();
        assertEquals("", out.toString().trim());
    }

    // ---------- Test createPadding indirectly ----------
    @Test
    public void testCreatePadding() {
        // Indirectly via printOptions with padding
        options.addOption("a", "alpha", false, "desc");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);
        formatter.printOptions(pw, 80, options, 5, 3);
        pw.flush();
        String output = out.toString();
        assertTrue(output.startsWith("     -a,--alpha   desc"));
    }
}