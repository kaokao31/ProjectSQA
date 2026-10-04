package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void testStripLeadingHyphensNull() {
        assertNull(Util.stripLeadingHyphens(null));
    }

    @Test
    public void testStripLeadingHyphensEmpty() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    @Test
    public void testStripLeadingHyphensNoHyphens() {
        assertEquals("foo", Util.stripLeadingHyphens("foo"));
    }

    @Test
    public void testStripLeadingHyphensSingleHyphen() {
        assertEquals("", Util.stripLeadingHyphens("-"));
        assertEquals("f", Util.stripLeadingHyphens("-f"));
    }

    @Test
    public void testStripLeadingHyphensDoubleHyphens() {
        assertEquals("", Util.stripLeadingHyphens("--"));
        assertEquals("foo", Util.stripLeadingHyphens("--foo"));
    }

    @Test
    public void testStripLeadingHyphensManyHyphens() {
        assertEquals("-foo", Util.stripLeadingHyphens("---foo"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesNull() {
        assertNull(Util.stripLeadingAndTrailingQuotes(null));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesEmpty() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesNoQuotes() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesOnlyLeadingQuote() {
        assertEquals("\"foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesOnlyTrailingQuote() {
        assertEquals("foo\"", Util.stripLeadingAndTrailingQuotes("foo\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesBothQuotes() {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesSingleQuoteChar() {
        assertEquals("\"", Util.stripLeadingAndTrailingQuotes("\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesTwoQuotes() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    @Test
    public void testStripLeadingAndTrailingQuotesNestedQuotes() {
        assertEquals("\"foo\"", Util.stripLeadingAndTrailingQuotes("\"\"foo\"\""));
    }
}