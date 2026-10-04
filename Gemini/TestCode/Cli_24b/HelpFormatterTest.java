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
        assertEquals(" ", HelpFormatter.DEFAULT_LONG_OPT_PREFIX);
        assertEquals("--", HelpFormatter.DEFAULT_LONG_OPT_SEPARATOR);
        assertEquals(74, HelpFormatter.DEFAULT_WIDTH);
        assertEquals(1, HelpFormatter.DEFAULT_LEFT_PAD);
        assertEquals(3, HelpFormatter.DEFAULT_DESC_PAD);
        assertEquals("-", HelpFormatter.DEFAULT_OPT_PREFIX);
        assertEquals("arg", HelpFormatter.DEFAULT_ARG_NAME);
    }

    @Test
    public void testGettersAndSetters() {
        HelpFormatter formatter = new HelpFormatter();

        formatter.setWidth(80);
        assertEquals(80, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(10);
        assertEquals(10, formatter.getDescPadding());

        formatter.setSyntaxPrefix("SYNTAX: ");
        assertEquals("SYNTAX: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());

        formatter.setOptPrefix("!");
        assertEquals("!", formatter.getOptPrefix());

        formatter.setLongOptPrefix("==");
        assertEquals("==", formatter.getLongOptPrefix());

        formatter.setArgName("target");
        assertEquals("target", formatter.getArgName());
    }

    @Test
    public void testComparator() {
        HelpFormatter formatter = new HelpFormatter();
        assertNotNull(formatter.getOptionComparator());

        Comparator<Option> comp = new Comparator<Option>() {
            public int compare(Option o1, Option o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        };
        formatter.setOptionComparator(comp);
        assertEquals(comp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNull(formatter.getOptionComparator());
    }

    @Test
    public void testPrintHelpCustomStream() {
        HelpFormatter formatter = new HelpFormatter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(out);

        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");

        formatter.printHelp(pw, 80, "testCmd", "header", options, 2, 5, "footer", true);
        pw.flush();
        String output = out.toString();

        assertTrue(output.contains("testCmd"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
        assertTrue(output.contains("alpha"));
    }

    @Test
    public void testPrintHelpSimple() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("b", "beta", true, "beta option description");

        // Use printHelp with string parameters to cover standard streams / wrappers
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        formatter.printHelp(pw, 60, "cmd", "header", options, 1, 3, "footer");
        pw.flush();
        assertTrue(sw.toString().length() > 0);
    }

    @Test
    public void testPrintHelpWithOptionsOnly() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("file")
                                       .hasArg()
                                       .withDescription("file to process")
                                       .create('f'));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 40, "cmd", "", options, 0, 0, "", false);
        pw.flush();
        assertTrue(sw.toString().contains("-f"));
    }

    @Test
    public void testFindWrapPos() {
        HelpFormatter formatter = new HelpFormatter();
        String text = "The quick brown fox jumps over the lazy dog";
        
        // Test normal wrapping
        int pos = formatter.findWrapPos(text, 15, 0);
        assertTrue(pos != -1);
        
        // Test wrap pos with \n
        String textWithNewline = "Line1\nLine2";
        assertEquals(5, formatter.findWrapPos(textWithNewline, 20, 0));

        // Test wrap pos with \r\n
        String textWithCrLf = "Line1\r\nLine2";
        assertEquals(5, formatter.findWrapPos(textWithCrLf, 20, 0));

        // Test width exceeds text length
        assertEquals(-1, formatter.findWrapPos("Short", 10, 0));

        // Test startPos + width >= text.length()
        assertEquals(-1, formatter.findWrapPos("Short text", 20, 2));

        // Test no spaces found before width
        assertEquals(5, formatter.findWrapPos("1234567890", 5, 0));
        
        // Test startPos handling
        int pos2 = formatter.findWrapPos("abc def ghi", 4, 4);
        assertTrue(pos2 >= 0);
    }

    @Test
    public void testRenderWrappedText() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.renderWrappedText(pw, 20, 2, "This is a very long description that needs to be wrapped properly.");
        pw.flush();
        assertTrue(sw.toString().length() > 0);
    }

    @Test
    public void testRenderOptions() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        options.addOption("x", "extra", false, "extra option");
        options.addOption("y", "y-opt", true, "y option with arg");

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 4);
        assertTrue(sb.length() > 0);
    }

    @Test
    public void testGetOptionGroup() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("1", "one");
        Option opt2 = new Option("2", "two");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "cmd", "", options, 2, 4, "", true);
        pw.flush();
        assertTrue(sw.toString().contains("[one | two]"));
    }

    @Test
    public void testRpad() {
        HelpFormatter formatter = new HelpFormatter();
        String padded = formatter.rpad("test", 10);
        assertEquals(10, padded.length());
    }

    @Test
    public void testCreatePadding() {
        HelpFormatter formatter = new HelpFormatter();
        String padding = formatter.createPadding(5);
        assertEquals("     ", padding);
    }

    @Test(expected = NullPointerException.class)
    public void testPrintHelpNullOptions() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("cmd", null);
    }

    @Test
    public void testPrintUsageEdgeCases() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        Options options = new Options();
        options.addOption("p", "port", true, "port number");
        
        formatter.printUsage(pw, 30, "app", options);
        pw.flush();
        assertTrue(sw.toString().contains("app"));
    }
}