package org.jfree.chart.imagemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link StandardToolTipTagFragmentGenerator}.
 * Designed to achieve high coverage and detect potential bugs,
 * especially the failure to escape HTML special characters in tooltip text
 * (Defects4J Chart-10).
 */
public class StandardToolTipTagFragmentGeneratorTest {

    private StandardToolTipTagFragmentGenerator generator;

    @Before
    public void setUp() {
        generator = new StandardToolTipTagFragmentGenerator();
    }

    /**
     * Test that the generator is properly instantiated.
     */
    @Test
    public void testGeneratorNotNull() {
        assertNotNull("Generator should not be null", generator);
    }

    /**
     * Test with plain text containing no special characters.
     */
    @Test
    public void testPlainText() {
        String toolTip = "Sales 2023";
        String fragment = generator.generateToolTipFragment(toolTip);
        String expected = "title=\"Sales 2023\"";
        assertEquals("Plain text should be wrapped unescaped", expected, fragment);
    }

    /**
     * Test with empty tooltip text.
     */
    @Test
    public void testEmptyText() {
        String fragment = generator.generateToolTipFragment("");
        String expected = "title=\"\"";
        assertEquals("Empty text should produce empty attribute", expected, fragment);
    }

    /**
     * Test with text containing an ampersand.
     * The bug in Chart-10 does not escape & to &amp; .
     * This test will fail if the bug is present, thereby detecting it.
     */
    @Test
    public void testAmpersand() {
        String toolTip = "Profit & Loss";
        String fragment = generator.generateToolTipFragment(toolTip);
        // Expected from a fixed implementation: "title=\"Profit &amp; Loss\""
        // Buggy implementation will produce: "title=\"Profit & Loss\""
        // We assert the correct (escaped) version:
        String expected = "title=\"Profit &amp; Loss\"";
        assertEquals("Ampersand should be escaped to &amp;", expected, fragment);
    }

    /**
     * Test with text containing less-than and greater-than signs.
     */
    @Test
    public void testAngleBrackets() {
        String toolTip = "if (x < 0 && y > 0)";
        String fragment = generator.generateToolTipFragment(toolTip);
        // Expected: "title=\"if (x &lt; 0 &amp;&amp; y &gt; 0)\""
        String expected = "title=\"if (x &lt; 0 &amp;&amp; y &gt; 0)\"";
        assertEquals("Angle brackets and ampersand should be escaped", expected, fragment);
    }

    /**
     * Test with text containing double quotes.
     * The generated attribute value uses double quotes, so internal double quotes must be escaped.
     */
    @Test
    public void testDoubleQuotes() {
        String toolTip = "Height: 10\"";
        String fragment = generator.generateToolTipFragment(toolTip);
        // Expected: "title=\"Height: 10&quot;\""
        String expected = "title=\"Height: 10&quot;\"";
        assertEquals("Double quotes should be escaped to &quot;", expected, fragment);
    }

    /**
     * Test with text containing single quotes (apostrophes).
     */
    @Test
    public void testSingleQuotes() {
        String toolTip = "John's data";
        String fragment = generator.generateToolTipFragment(toolTip);
        // Single quotes are safe inside double-quoted attribute values.
        String expected = "title=\"John's data\"";
        assertEquals("Single quotes need not be escaped", expected, fragment);
    }

    /**
     * Test with text containing multiple special characters.
     */
    @Test
    public void testMixedSpecialChars() {
        String toolTip = "Price < $50 & discount 20%";
        String fragment = generator.generateToolTipFragment(toolTip);
        // Expected: "title=\"Price &lt; $50 &amp; discount 20%\""
        String expected = "title=\"Price &lt; $50 &amp; discount 20%\"";
        assertEquals("Multiple special characters should be escaped", expected, fragment);
    }

    /**
     * Test that the fragment starts with "title=\"" and ends with "\"".
     */
    @Test
    public void testFragmentFormat() {
        String toolTip = "test";
        String fragment = generator.generateToolTipFragment(toolTip);
        assertTrue("Fragment should start with title=\"", fragment.startsWith("title=\""));
        assertTrue("Fragment should end with double quote", fragment.endsWith("\""));
        // Ensure the text is enclosed exactly once
        int firstQuote = fragment.indexOf("\"");
        int lastQuote = fragment.lastIndexOf("\"");
        assertEquals("There should be exactly two double quotes", firstQuote, 6); // title=" starts at index 0, quote at 6
        assertEquals("Last quote should be at the end", lastQuote, fragment.length() - 1);
    }

    /**
     * Test with null input (should throw NullPointerException).
     * We explicitly test that the method does not handle null gracefully.
     */
    @Test(expected = NullPointerException.class)
    public void testNullInput() {
        generator.generateToolTipFragment(null);
    }
}