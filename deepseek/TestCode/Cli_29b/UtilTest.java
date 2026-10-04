package org.apache.commons.cli;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class UtilTest {

    private Util util;

    @Before
    public void setUp() {
        util = new Util();
    }

    @Test
    public void testStripLeadingHyphens_NullInput() {
        assertEquals(null, Util.stripLeadingHyphens(null));
    }

    @Test
    public void testStripLeadingHyphens_EmptyString() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    @Test
    public void testStripLeadingHyphens_SingleHyphenPrefix() {
        assertEquals("option", Util.stripLeadingHyphens("-option"));
    }

    @Test
    public void testStripLeadingHyphens_DoubleHyphenPrefix() {
        assertEquals("option", Util.stripLeadingHyphens("--option"));
    }

    @Test
    public void testStripLeadingHyphens_MultipleHyphens() {
        assertEquals("option", Util.stripLeadingHyphens("---option"));
    }

    @Test
    public void testStripLeadingHyphens_OnlyHyphens() {
        assertEquals("", Util.stripLeadingHyphens("-"));
        assertEquals("", Util.stripLeadingHyphens("--"));
        assertEquals("", Util.stripLeadingHyphens("---"));
    }

    @Test
    public void testStripLeadingHyphens_NoHyphenPrefix() {
        assertEquals("option", Util.stripLeadingHyphens("option"));
        assertEquals("test", Util.stripLeadingHyphens("test"));
    }

    @Test
    public void testStripLeadingHyphens_HyphenInMiddle() {
        assertEquals("option-value", Util.stripLeadingHyphens("option-value"));
        assertEquals("opt-ion", Util.stripLeadingHyphens("opt-ion"));
    }

    @Test
    public void testStripLeadingHyphens_LeadingAndTrailingHyphens() {
        assertEquals("option-", Util.stripLeadingHyphens("--option-"));
        assertEquals("option--", Util.stripLeadingHyphens("---option--"));
    }

    @Test
    public void testStripLeadingHyphens_WhitespaceCheck() {
        assertEquals(" option", Util.stripLeadingHyphens("- option"));
        assertEquals("  option", Util.stripLeadingHyphens("--  option"));
    }

    @Test
    public void testStripLeadingHyphens_SpecialCharacters() {
        assertEquals("op!tion", Util.stripLeadingHyphens("--op!tion"));
        assertEquals("opt1on", Util.stripLeadingHyphens("-opt1on"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_NullInput() {
        assertEquals(null, Util.stripLeadingAndTrailingQuotes(null));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_EmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_NoQuotes() {
        assertEquals("test", Util.stripLeadingAndTrailingQuotes("test"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_SingleQuote() {
        assertEquals("test'", Util.stripLeadingAndTrailingQuotes("'test'"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_DoubleQuotes() {
        assertEquals("test", Util.stripLeadingAndTrailingQuotes("\"test\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_LeadingQuoteOnly() {
        assertEquals("\"test", Util.stripLeadingAndTrailingQuotes("\"\"test"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_TrailingQuoteOnly() {
        assertEquals("test\"", Util.stripLeadingAndTrailingQuotes("test\"\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_MismatchedQuotes() {
        assertEquals("\"test", Util.stripLeadingAndTrailingQuotes("\"test"));
        assertEquals("test\"", Util.stripLeadingAndTrailingQuotes("test\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_NestedQuotes() {
        assertEquals("\"test\"", Util.stripLeadingAndTrailingQuotes("\"\"test\"\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_WhitespaceInQuotes() {
        assertEquals(" te st ", Util.stripLeadingAndTrailingQuotes("\" te st \""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotes_SingleCharactersInsideQuotes() {
        assertEquals("a", Util.stripLeadingAndTrailingQuotes("\"a\""));
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }
}