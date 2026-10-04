package org.example.parser;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParserTest {

    private Parser parser;

    @Before
    public void setUp() {
        parser = new Parser();
    }

    @Test
    public void testParseNull() {
        assertNull(parser.parse(null));
    }

    @Test
    public void testParseEmptyString() {
        assertArrayEquals(new String[0], parser.parse(""));
    }

    @Test
    public void testParseWhitespace() {
        assertArrayEquals(new String[0], parser.parse("   "));
    }

    @Test
    public void testParseSingleToken() {
        assertArrayEquals(new String[]{"hello"}, parser.parse("hello"));
    }

    @Test
    public void testParseMultipleTokens() {
        assertArrayEquals(new String[]{"a", "b", "c"}, parser.parse("a,b,c"));
    }

    @Test
    public void testParseWithSpacesAroundTokens() {
        assertArrayEquals(new String[]{"a", "b"}, parser.parse(" a , b "));
    }

    @Test
    public void testParseQuotedToken() {
        assertArrayEquals(new String[]{"a, b"}, parser.parse("\"a, b\""));
    }

    @Test
    public void testParseQuotedTokenWithEscapedQuote() {
        assertArrayEquals(new String[]{"a\"b"}, parser.parse("\"a\\\"b\""));
    }

    @Test
    public void testParseMixedQuotedAndUnquoted() {
        assertArrayEquals(new String[]{"a", "b c", "d"}, parser.parse("a,\"b c\",d"));
    }

    @Test
    public void testParseTrailingComma() {
        assertArrayEquals(new String[]{"a"}, parser.parse("a,"));
    }

    @Test
    public void testParseLeadingComma() {
        assertArrayEquals(new String[]{"a"}, parser.parse(",a"));
    }

    @Test
    public void testParseConsecutiveCommas() {
        assertArrayEquals(new String[]{"a", "", "b"}, parser.parse("a,,b"));
    }

    @Test
    public void testParseOnlyCommas() {
        assertArrayEquals(new String[]{"", "", ""}, parser.parse(",,,"));
    }

    @Test
    public void testParseNewlineSeparator() {
        assertArrayEquals(new String[]{"a", "b"}, parser.parse("a\nb"));
    }

    @Test
    public void testParseTabSeparator() {
        assertArrayEquals(new String[]{"a", "b"}, parser.parse("a\tb"));
    }

    @Test
    public void testParseInvalidQuoteSequence() {
        assertArrayEquals(new String[]{"a", "b"}, parser.parse("a,\"b")); // should treat " as literal? likely bug
    }

    @Test
    public void testParseUnclosedQuote() {
        assertArrayEquals(new String[]{"a", "b\"c"}, parser.parse("a,\"b\"c")); // behavior? assume bug
    }

    @Test
    public void testParseNumericTokens() {
        assertArrayEquals(new String[]{"123", "456"}, parser.parse("123,456"));
    }

    @Test
    public void testParseVeryLongToken() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append('x');
        assertArrayEquals(new String[]{sb.toString()}, parser.parse(sb.toString()));
    }

    @Test
    public void testParseSpecialCharacters() {
        assertArrayEquals(new String[]{"a!@#$%^&*()_+{}[];':\",/?-="}, parser.parse("a!@#$%^&*()_+{}[];':\",/?-="));
    }

    @Test
    public void testParseEmptyQuotedToken() {
        assertArrayEquals(new String[]{""}, parser.parse("\"\""));
    }
}