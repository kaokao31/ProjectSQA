package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private Options options;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        options = new Options();
    }

    @Test
    public void testPrintHelpSimple() {
        Option opt = Option.builder("f")
                .longOpt("file")
                .hasArg()
                .desc("the file to be processed")
                .build();
        options.addOption(opt);
        options.addOption(Option.builder("v").longOpt("verbose").desc("verbose output").build());

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "myapp", "header", options, "footer");
        pw.flush();
        String result = out.toString();

        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("header"));
        assertTrue(result.contains("footer"));
        assertTrue(result.contains("-f,--file <arg>"));
        assertTrue(result.contains("the file to be processed"));
        assertTrue(result.contains("-v,--verbose"));
        assertTrue(result.contains("verbose output"));
    }

    @Test
    public void testPrintHelpDefaultWidth() {
        Option opt = Option.builder("o").hasArg().desc("a very long description that should be wrapped properly when the width is default").build();
        options.addOption(opt);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, "myapp", null, options, null);
        pw.flush();
        String result = out.toString();

        assertTrue(result.contains("usage: myapp"));
        assertTrue(result.contains("a very long description that should be wrapped properly when the width is"));
    }

    @Test
    public void testPrintHelpWithOptionGroup() {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").desc("option a").build());
        group.addOption(Option.builder("b").desc("option b").build());
        group.setRequired(true);
        options.addOptionGroup(group);

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 100, "prog", "header", options, "footer");
        pw.flush();
        String result = out.toString();

        assertTrue(result.contains("-a"));
        assertTrue(result.contains("-b"));
        assertTrue(result.contains("option a"));
        assertTrue(result.contains("option b"));
    }

    @Test
    public void testPrintHelpWithEmptyOptions() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "prog", null, options, null);
        pw.flush();
        String result = out.toString();
        assertEquals("usage: prog\n", result);
    }

    @Test
    public void testPrintHelpWithNullOptions() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        try {
            formatter.printHelp(pw, 80, "prog", null, null, null);
            pw.flush();
            String result = out.toString();
            assertTrue(result.contains("usage: prog"));
        } catch (NullPointerException e) {
            // expected behavior? Options is required, but we'll assert that it doesn't crash unexpectedly
            fail("Should not throw NPE for null Options");
        }
    }

    @Test
    public void testPrintHelpWithNullWriter() {
        try {
            formatter.printHelp(null, 80, "prog", null, options, null);
            fail("Expected IllegalArgumentException for null writer");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrintHelpWithNullCmdLine() {
        try {
            formatter.printHelp(new PrintWriter(new StringWriter()), 80, null, null, options, null);
            fail("Expected IllegalArgumentException for null cmdLine");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testPrintHelpWithZeroWidth() {
        options.addOption(Option.builder("x").hasArg().desc("description").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        try {
            formatter.printHelp(pw, 0, "prog", null, options, null);
            pw.flush();
            // Should not throw, but output may be truncated
            assertTrue(out.toString().length() > 0);
        } catch (Exception e) {
            fail("Should not throw exception for zero width: " + e.getMessage());
        }
    }

    @Test
    public void testPrintHelpWithNegativeWidth() {
        options.addOption(Option.builder("x").hasArg().desc("description").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        try {
            formatter.printHelp(pw, -1, "prog", null, options, null);
            pw.flush();
            // Should not throw, but output may be empty
        } catch (Exception e) {
            fail("Should not throw exception for negative width: " + e.getMessage());
        }
    }

    @Test
    public void testPrintHelpWithNarrowWidth() {
        options.addOption(Option.builder("long-option").hasArg().desc("this is a very long description that will definitely wrap").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 20, "myapp", null, options, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("this is a very long"));
        assertTrue(result.contains("description that"));
    }

    @Test
    public void testPrintHelpWithLongUsage() {
        options.addOption(Option.builder("a").desc("desc").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "very-long-command-line-application-name-that-exceeds-width", null, options, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("usage: very-long-command-line-application-name-that-exceeds-width"));
        // Even if the usage is too long, it should still contain the full string
    }

    @Test
    public void testFindWrapPosNull() {
        try {
            formatter.findWrapPos(null, 10, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFindWrapPosWidthZero() {
        String text = "Hello world";
        try {
            int pos = formatter.findWrapPos(text, 0, 0);
            assertEquals("Expected position 0?", 0, pos);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testFindWrapPosWidthOne() {
        String text = "Hello";
        assertEquals(1, formatter.findWrapPos(text, 1, 0));
    }

    @Test
    public void testFindWrapPosAtEnd() {
        String text = "Hello";
        assertEquals(5, formatter.findWrapPos(text, 10, 0));
    }

    @Test
    public void testFindWrapPosWithLineBreak() {
        String text = "Hello\nWorld";
        assertEquals(6, formatter.findWrapPos(text, 10, 0));
    }

    @Test
    public void testFindWrapPosAtSpace() {
        String text = "Hello world test";
        assertEquals(6, formatter.findWrapPos(text, 10, 0));
    }

    @Test
    public void testFindWrapPosWithLongWord() {
        String text = "supercalifragilisticexpialidocious";
        int result = formatter.findWrapPos(text, 10, 0);
        assertTrue(result == 10 || result == 30);
        // Depends on implementation - should not be negative
        assertTrue(result >= 0);
    }

    @Test
    public void testFindWrapPosWithStartOffset() {
        String text = "Hello world test";
        assertEquals(14, formatter.findWrapPos(text, 10, 5));
    }

    @Test
    public void testCreatePadding() {
        assertEquals("    ", formatter.createPadding(4));
        assertEquals("", formatter.createPadding(0));
        try {
            formatter.createPadding(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRtrim() {
        assertEquals("hello", formatter.rtrim("hello   "));
        assertEquals("hello", formatter.rtrim("hello"));
        assertEquals("", formatter.rtrim(""));
        assertNull(formatter.rtrim(null));
    }

    @Test
    public void testDefaultLeftPad() {
        assertEquals("   ", formatter.getLeftPadding());
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
    }

    @Test
    public void testDefaultDescPad() {
        assertEquals(1, formatter.getDescPadding());
        formatter.setDescPadding(2);
        assertEquals(2, formatter.getDescPadding());
    }

    @Test
    public void testDefaultWidth() {
        assertEquals(74, formatter.getWidth());
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
    }

    @Test
    public void testDefaultSyntaxPrefix() {
        assertEquals("usage: ", formatter.getSyntaxPrefix());
        formatter.setSyntaxPrefix("Usage: ");
        assertEquals("Usage: ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testDefaultNewLine() {
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());
    }

    @Test
    public void testDefaultOptPrefix() {
        assertEquals("-", formatter.getOptPrefix());
        formatter.setOptPrefix("--");
        assertEquals("--", formatter.getOptPrefix());
    }

    @Test
    public void testDefaultLongOptPrefix() {
        assertEquals("--", formatter.getLongOptPrefix());
        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());
    }

    @Test
    public void testDefaultArgName() {
        assertEquals("arg", formatter.getArgName());
        formatter.setArgName("FILE");
        assertEquals("FILE", formatter.getArgName());
    }

    @Test
    public void testDefaultOptionComparator() {
        assertNotNull(formatter.getOptionComparator());
    }

    @Test
    public void testSetOptionComparator() {
        Comparator<Option> comp = new Comparator<Option>() {
            public int compare(Option o1, Option o2) {
                return o2.getKey().compareTo(o1.getKey());
            }
        };
        formatter.setOptionComparator(comp);
        assertSame(comp, formatter.getOptionComparator());
    }

    @Test
    public void testRenderOptionsWithCustomPadding() {
        options.addOption(Option.builder("a").hasArg().desc("description").build());
        options.addOption(Option.builder("b").desc("desc").build());

        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.setLeftPadding(5);
        formatter.setDescPadding(3);
        formatter.printHelp(pw, 80, "prog", null, options, null);
        pw.flush();
        String result = out.toString();

        assertTrue(result.contains("    -a <arg>"));
        assertTrue(result.contains("       description"));
        assertTrue(result.contains("    -b"));
        assertTrue(result.contains("       desc"));
    }

    @Test
    public void testPrintWrappedSingleLine() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 20, "This is a short line");
        pw.flush();
        String result = out.toString();
        assertEquals("This is a short line\n", result);
    }

    @Test
    public void testPrintWrappedMultiLine() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 10, "This line should wrap at some point");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("This line"));
        assertTrue(result.contains("should wrap"));
        assertTrue(result.contains("at some"));
        assertTrue(result.contains("point"));
    }

    @Test
    public void testPrintWrappedWithPrefix() {
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printWrapped(pw, 10, "    >", "This is a very long text that needs wrapping");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("    >This is"));
        assertTrue(result.contains("very long"));
    }

    @Test
    public void testPrintHelpWithHeaderAndFooter() {
        options.addOption(Option.builder("x").desc("desc").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "prog", "header line", options, "footer line");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("header line"));
        assertTrue(result.contains("footer line"));
        assertTrue(result.indexOf("header line") < result.indexOf("usage"));
    }

    @Test
    public void testPrintHelpWithMultipleOptions() {
        for (int i = 0; i < 10; i++) {
            options.addOption(Option.builder(String.valueOf(i)).desc("Option " + i).build());
        }
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 50, "prog", null, options, null);
        pw.flush();
        String result = out.toString();
        for (int i = 0; i < 10; i++) {
            assertTrue(result.contains("-" + i));
        }
    }

    @Test
    public void testPrintHelpWithRequiredOptions() {
        Option req = Option.builder("r").required().desc("required option").build();
        options.addOption(req);
        options.addOption(Option.builder("o").desc("optional").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "prog", null, options, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-r"));
        assertTrue(result.contains("required option"));
        // Check that optional is also present
        assertTrue(result.contains("-o"));
    }

    @Test
    public void testPrintHelpWithArgNames() {
        options.addOption(Option.builder("f").hasArg().argName("file").desc("file to read").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "prog", null, options, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-f <file>"));
    }

    @Test
    public void testPrintHelpWithOptionWithNoShortOpt() {
        options.addOption(Option.builder().longOpt("long").hasArg().desc("long only option").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "prog", null, options, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("--long <arg>"));
        assertTrue(result.contains("long only option"));
    }

    @Test
    public void testPrintHelpWithOptionWithNoLongOpt() {
        options.addOption(Option.builder("s").hasArg().desc("short only").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "prog", null, options, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-s <arg>"));
        assertTrue(result.contains("short only"));
    }

    @Test
    public void testPrintHelpWithOptionGroupRequired() {
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").desc("a").build());
        group.addOption(Option.builder("b").desc("b").build());
        group.setRequired(true);
        options.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "prog", null, options, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-a"));
        assertTrue(result.contains("-b"));
        // Both should be in brackets? Actually, group is optional by default, but required exists.
        // Just check presence.
    }

    @Test
    public void testPrintHelpWithComplicatedOptions() {
        options.addOption(Option.builder("a").hasArg().desc("alpha").build());
        options.addOption(Option.builder("b").hasArg().desc("beta").build());
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("c").desc("gamma").build());
        group.addOption(Option.builder("d").desc("delta").build());
        options.addOptionGroup(group);
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 80, "prog", "header", options, "footer");
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("[-a <arg>]"));
        assertTrue(result.contains("[-b <arg>]"));
        assertTrue(result.contains("{c,d}"));
    }

    @Test
    public void testRenderOptionsWithWideArg() {
        options.addOption(Option.builder("o").hasArg().argName("VERY-LONG-ARGUMENT-NAME").desc("desc").build());
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, 40, "prog", null, options, null);
        pw.flush();
        String result = out.toString();
        assertTrue(result.contains("-o <VERY-LONG-ARGUMENT-NAME>"));
    }

    @Test
    public void testDefaults() {
        assertEquals(74, formatter.getWidth());
        assertEquals("usage: ", formatter.getSyntaxPrefix());
        assertEquals("-", formatter.getOptPrefix());
        assertEquals("--", formatter.getLongOptPrefix());
        assertEquals("arg", formatter.getArgName());
        assertEquals(1, formatter.getDescPadding());
        assertEquals(3, formatter.getLeftPadding());
        assertNotNull(formatter.getNewLine());
        assertNotNull(formatter.getOptionComparator());
    }
}