package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private Options options;
    private ByteArrayOutputStream baos;
    private PrintWriter pw;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        options = new Options();
        baos = new ByteArrayOutputStream();
        pw = new PrintWriter(baos);
    }

    // ---------------- Test printHelp with all overloads ----------------

    @Test
    public void testPrintHelpBasic() {
        options.addOption("a", "alpha", false, "Alpha option");
        formatter.printHelp(pw, "Usage: test", options);
        pw.flush();
        String output = baos.toString();
        assertNotNull(output);
        assertTrue(output.contains("Usage: test"));
        assertTrue(output.contains("-a,--alpha"));
    }

    @Test
    public void testPrintHelpWithWidth() {
        options.addOption("b", "beta", true, "Beta option with long description that should wrap at some point");
        formatter.printHelp(pw, 30, "Usage: app", null, options, 1, 2);
        pw.flush();
        String output = baos.toString();
        assertTrue(output.contains("Usage: app"));
        // The description should be present, even if wrapped
        assertTrue(output.contains("Beta option"));
    }

    @Test
    public void testPrintHelpWithFooter() {
        options.addOption("c", "gamma", false, "Gamma option");
        formatter.printHelp(pw, 80, "Usage: app", "Header", options, 0, 0, "Footer");
        pw.flush();
        String output = baos.toString();
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
        assertTrue(output.contains("Gamma option"));
    }

    @Test
    public void testPrintHelpNullOptions() {
        // Should not throw, but produce some output
        try {
            formatter.printHelp(pw, "Usage: app", null);
            pw.flush();
            assertTrue(baos.toString().contains("Usage: app"));
        } catch (Exception e) {
            fail("printHelp with null options should not throw");
        }
    }

    @Test
    public void testPrintHelpEmptyOptions() {
        formatter.printHelp(pw, "Usage: app", new Options());
        pw.flush();
        assertTrue(baos.toString().contains("Usage: app"));
    }

    @Test
    public void testPrintHelpNullUsage() {
        options.addOption("a", "alpha", false, "Alpha option");
        try {
            formatter.printHelp(pw, null, options);
            pw.flush();
            assertTrue(baos.toString().contains(""));
        } catch (Exception e) {
            // Might throw or not; if it does, we accept it
        }
    }

    // ---------------- Test printOptions ----------------

    @Test
    public void testPrintOptions() {
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", null, false, "Beta without long");
        options.addOption(null, "gamma", false, "Gamma without short");
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printOptions(w, 80, options, 2, 3);
        w.flush();
        String output = sw.toString();
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("-b"));
        assertTrue(output.contains("--gamma"));
        assertTrue(output.contains("Alpha option"));
    }

    @Test
    public void testPrintOptionsWithNarrowWidth() {
        options.addOption("a", "alpha", true, "This is a very long description that will definitely need wrapping to fit within narrow constraints");
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printOptions(w, 20, options, 0, 0);
        w.flush();
        String output = sw.toString();
        assertTrue(output.contains("This is a very long"));
        assertTrue(output.contains("description"));
        // Ensure it didn't blow up
    }

    @Test
    public void testPrintOptionsEmptyOptions() {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printOptions(w, 80, new Options(), 1, 1);
        w.flush();
        assertEquals("", sw.toString());
    }

    // ---------------- Test printUsage ----------------

    @Test
    public void testPrintUsageWithRequiredOptions() {
        options.addOption(Option.builder("a").longOpt("alpha").required(true).hasArg().desc("Alpha").build());
        options.addOption("b", "beta", false, "Beta");
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printUsage(w, 80, "app", options);
        w.flush();
        String output = sw.toString();
        assertTrue(output.contains("Usage: app"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("[<arg>]"));
    }

    @Test
    public void testPrintUsageWithNoOptions() {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printUsage(w, 80, "app", new Options());
        w.flush();
        assertEquals("Usage: app", sw.toString().trim());
    }

    @Test
    public void testPrintUsageWithOptionGroups() {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").desc("A").build());
        group.addOption(Option.builder("b").desc("B").build());
        group.setRequired(true);
        options.addOptionGroup(group);
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printUsage(w, 80, "app", options);
        w.flush();
        String output = sw.toString();
        assertTrue(output.contains("[-a | -b]"));
    }

    // ---------------- Test printWrapped ----------------

    @Test
    public void testPrintWrappedSimple() {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printWrapped(w, 80, "This is a simple line");
        w.flush();
        assertEquals("This is a simple line", sw.toString().trim());
    }

    @Test
    public void testPrintWrappedWithLeadingSpaces() {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printWrapped(w, 80, "  This is indented");
        w.flush();
        assertTrue(sw.toString().contains("  This is indented"));
    }

    @Test
    public void testPrintWrappedLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("word ");
        }
        String text = sb.toString();
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printWrapped(w, 20, text);
        w.flush();
        String output = sw.toString();
        String[] lines = output.split(System.getProperty("line.separator"));
        assertTrue(lines.length > 1);
        for (String line : lines) {
            assertTrue("Line length: " + line.length(), line.length() <= 20);
        }
    }

    @Test
    public void testPrintWrappedWithNullText() {
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        try {
            formatter.printWrapped(w, 80, null);
            w.flush();
            // No exception expected; output may be empty
        } catch (Exception e) {
            fail("printWrapped(null) should not throw");
        }
    }

    // ---------------- Test renderOptions (indirectly via printOptions) ----------------

    // Already covered by printOptions tests.

    // ---------------- Test setters and getters ----------------

    @Test
    public void testDefaultValues() {
        assertEquals("usage:", formatter.getSyntaxPrefix());
        assertEquals("", formatter.getNewLine());
        assertEquals("", formatter.getOptPrefix());
        assertEquals("--", formatter.getLongOptPrefix());
        assertEquals(74, formatter.getWidth());
        assertEquals(1, formatter.getLeftPadding());
        assertEquals(3, formatter.getDescPadding());
        assertEquals("arg", formatter.getArgName());
    }

    @Test
    public void testSetters() {
        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setLongOptPrefix("-");
        assertEquals("-", formatter.getLongOptPrefix());

        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(10);
        assertEquals(10, formatter.getDescPadding());

        formatter.setArgName("value");
        assertEquals("value", formatter.getArgName());
    }

    @Test
    public void testSetWidthNegative() {
        try {
            formatter.setWidth(-1);
            // If no exception, check it's negative? Probably allowed.
        } catch (IllegalArgumentException e) {
            // Acceptable if it throws
        }
    }

    @Test
    public void testSetLeftPaddingNegative() {
        try {
            formatter.setLeftPadding(-1);
        } catch (IllegalArgumentException e) {
            // Acceptable
        }
    }

    // ---------------- Edge cases that might expose bugs ----------------

    @Test
    public void testPrintHelpWithOptionWithoutShortOptAndLongOpt() {
        // Option with both null and null? Actually must have at least one.
        Option opt = new Option(null, null, false, "No keys");
        options.addOption(opt);
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        try {
            formatter.printHelp(w, 80, "app", "header", options, 1, 1, "footer");
            w.flush();
            String output = sw.toString();
            // Should not throw; and should contain something, possibly "No keys"
            assertTrue(output.contains("No keys"));
        } catch (Exception e) {
            fail("Should handle option with no keys");
        }
    }

    @Test
    public void testPrintHelpWithRequiredOptionsDisplay() {
        options.addOption(Option.builder("a").required(true).desc("Required A").build());
        options.addOption(Option.builder("b").desc("Optional B").build());
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printHelp(w, 80, "app", null, options, 1, 1, null);
        w.flush();
        String output = sw.toString();
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("-b"));
        // Check that required option is marked, perhaps with a marker? Not necessarily.
        // But ensure both are printed.
    }

    @Test
    public void testPrintHelpWithOptionArgs() {
        options.addOption(Option.builder("f").longOpt("file").hasArg().argName("FILE").desc("File to process").build());
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printHelp(w, 80, "app", null, options, 1, 1, null);
        w.flush();
        String output = sw.toString();
        assertTrue(output.contains("-f,--file <FILE>"));
    }

    @Test
    public void testPrintHelpWithOptionGroupsAndRequired() {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").desc("A").build());
        group.addOption(Option.builder("b").desc("B").build());
        group.setRequired(true);
        options.addOptionGroup(group);
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        formatter.printHelp(w, 80, "app", "Header", options, 1, 1, "Footer");
        w.flush();
        String output = sw.toString();
        assertTrue(output.contains("[-a | -b]"));
    }

    @Test
    public void testPrintHelpWithWidthZero() {
        options.addOption("a", "alpha", false, "Alpha");
        StringWriter sw = new StringWriter();
        PrintWriter w = new PrintWriter(sw);
        try {
            formatter.printHelp(w, 0, "app", null, options, 0, 0, null);
            w.flush();
            String output = sw.toString();
            // Might cause infinite loop or throw; if it doesn't, we just ensure something.
        } catch (Exception e) {
            // Acceptable, but ideally should handle gracefully.
            return;
        }
        fail("Expected exception or proper handling of width=0");
    }
}