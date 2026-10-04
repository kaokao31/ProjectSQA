package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TokenQueueTest {
    private TokenQueue tq;

    @Before
    public void setUp() {
        tq = new TokenQueue("");
    }

    @Test
    public void testChompBalancedWithParentheses() {
        tq = new TokenQueue("(one two (three) four)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one two (three) four", result);
    }

    @Test
    public void testChompBalancedWithBrackets() {
        tq = new TokenQueue("[one two [three] four]");
        String result = tq.chompBalanced('[', ']');
        assertEquals("one two [three] four", result);
    }

    @Test
    public void testChompBalancedWithBraces() {
        tq = new TokenQueue("{one two {three} four}");
        String result = tq.chompBalanced('{', '}');
        assertEquals("one two {three} four", result);
    }

    @Test
    public void testChompBalancedWithNestedSameType() {
        tq = new TokenQueue("((()))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("(())", result);
    }

    @Test
    public void testChompBalancedWithEmptyContent() {
        tq = new TokenQueue("()");
        String result = tq.chompBalanced('(', ')');
        assertEquals("", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChompBalancedWithUnclosed() {
        tq = new TokenQueue("(one two");
        tq.chompBalanced('(', ')');
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChompBalancedWithNoOpening() {
        tq = new TokenQueue("one two)");
        tq.chompBalanced('(', ')');
    }

    @Test
    public void testChompBalancedWithSingleChar() {
        tq = new TokenQueue("(a)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a", result);
    }

    @Test
    public void testChompBalancedWithMultiplePairs() {
        tq = new TokenQueue("(a)(b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a", result);
    }

    @Test
    public void testChompBalancedWithEscapedCharacters() {
        tq = new TokenQueue("(one \\(two)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \\(two", result);
    }

    @Test
    public void testChompBalancedWithStringLiteral() {
        tq = new TokenQueue("(one \"two\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \"two\"", result);
    }

    @Test
    public void testChompBalancedWithSingleQuoteString() {
        tq = new TokenQueue("(one 'two')");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one 'two'", result);
    }

    @Test
    public void testChompBalancedWithMixedQuotes() {
        tq = new TokenQueue("(one \"two's\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \"two's\"", result);
    }

    @Test
    public void testChompBalancedWithEscapedQuote() {
        tq = new TokenQueue("(one \\\"two)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \\\"two", result);
    }

    @Test
    public void testChompBalancedWithMultipleEscapes() {
        tq = new TokenQueue("(one \\\\(two)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \\\\(two", result);
    }

    @Test
    public void testChompBalancedWithUnclosedString() {
        tq = new TokenQueue("(one \"two)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \"two", result);
    }

    @Test
    public void testChompBalancedWithUnclosedSingleQuote() {
        tq = new TokenQueue("(one 'two)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one 'two", result);
    }

    @Test
    public void testChompBalancedWithBackslashAtEnd() {
        tq = new TokenQueue("(one \\");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \\", result);
    }

    @Test
    public void testChompBalancedWithOnlyBackslash() {
        tq = new TokenQueue("(\\");
        String result = tq.chompBalanced('(', ')');
        assertEquals("\\", result);
    }

    @Test
    public void testChompBalancedWithNullInput() {
        tq = new TokenQueue(null);
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithEmptyQueue() {
        tq = new TokenQueue("");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithWhitespaceOnly() {
        tq = new TokenQueue("   ");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithSameOpenClose() {
        tq = new TokenQueue("\"hello\"");
        String result = tq.chompBalanced('"', '"');
        assertEquals("hello", result);
    }

    @Test
    public void testChompBalancedWithNestedSameOpenClose() {
        tq = new TokenQueue("\"hello \"world\"\"");
        String result = tq.chompBalanced('"', '"');
        assertEquals("hello \"world\"", result);
    }

    @Test
    public void testChompBalancedWithMultipleNested() {
        tq = new TokenQueue("((a)(b))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("(a)(b)", result);
    }

    @Test
    public void testChompBalancedWithDeepNesting() {
        tq = new TokenQueue("((((a))))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("(((a)))", result);
    }

    @Test
    public void testChompBalancedWithUnclosedNested() {
        tq = new TokenQueue("((a)");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithExtraClosing() {
        tq = new TokenQueue("(a))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a", result);
    }

    @Test
    public void testChompBalancedWithStringContainingBrackets() {
        tq = new TokenQueue("(one \"(two)\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \"(two)\"", result);
    }

    @Test
    public void testChompBalancedWithEscapedBracketInString() {
        tq = new TokenQueue("(one \"\\\"two\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \"\\\"two\"", result);
    }

    @Test
    public void testChompBalancedWithMixedBrackets() {
        tq = new TokenQueue("([one two])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[one two]", result);
    }

    @Test
    public void testChompBalancedWithUnclosedStringAndBracket() {
        tq = new TokenQueue("(one \"two");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \"two", result);
    }

    @Test
    public void testChompBalancedWithBackslashBeforeQuote() {
        tq = new TokenQueue("(one \\\"two\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \\\"two\"", result);
    }

    @Test
    public void testChompBalancedWithMultipleBackslashes() {
        tq = new TokenQueue("(one \\\\\"two\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \\\\\"two\"", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndQuote() {
        tq = new TokenQueue("(one \\\"two)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("one \\\"two", result);
    }

    @Test
    public void testChompBalancedWithOnlyQuotes() {
        tq = new TokenQueue("\"\"");
        String result = tq.chompBalanced('"', '"');
        assertEquals("", result);
    }

    @Test
    public void testChompBalancedWithNestedQuotes() {
        tq = new TokenQueue("\"a\"b\"c\"");
        String result = tq.chompBalanced('"', '"');
        assertEquals("a\"b\"c", result);
    }

    @Test
    public void testChompBalancedWithUnclosedQuoteAtEnd() {
        tq = new TokenQueue("(a \"b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAtEndOfString() {
        tq = new TokenQueue("(a \"b\\");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b\\", result);
    }

    @Test
    public void testChompBalancedWithMultipleStrings() {
        tq = new TokenQueue("(a \"b\" c 'd')");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b\" c 'd'", result);
    }

    @Test
    public void testChompBalancedWithEscapedBackslash() {
        tq = new TokenQueue("(a \\\\b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\\\b", result);
    }

    @Test
    public void testChompBalancedWithEscapedBackslashAndQuote() {
        tq = new TokenQueue("(a \\\\\\\"b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\\\\\\"b", result);
    }

    @Test
    public void testChompBalancedWithUnclosedEscaped() {
        tq = new TokenQueue("(a \\");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\", result);
    }

    @Test
    public void testChompBalancedWithOnlyEscapedBackslash() {
        tq = new TokenQueue("(\\\\)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("\\\\", result);
    }

    @Test
    public void testChompBalancedWithEscapedBackslashAtEnd() {
        tq = new TokenQueue("(a \\\\");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\\\", result);
    }

    @Test
    public void testChompBalancedWithStringAndEscapedBracket() {
        tq = new TokenQueue("(a \"b\\)c)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b\\)c", result);
    }

    @Test
    public void testChompBalancedWithMultipleEscapedChars() {
        tq = new TokenQueue("(a \\\"b\\\"c)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\\"b\\\"c", result);
    }

    @Test
    public void testChompBalancedWithBackslashBeforeClosing() {
        tq = new TokenQueue("(a \\");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\", result);
    }

    @Test
    public void testChompBalancedWithBackslashBeforeOpening() {
        tq = new TokenQueue("\\(");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAtStart() {
        tq = new TokenQueue("\\()");
        String result = tq.chompBalanced('(', ')');
        assertEquals("", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndString() {
        tq = new TokenQueue("(a \\\"b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\\"b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndSingleQuote() {
        tq = new TokenQueue("(a \\'b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\'b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndDoubleQuote() {
        tq = new TokenQueue("(a \\\"b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\\"b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndBothQuotes() {
        tq = new TokenQueue("(a \\\"b\\'c)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\\"b\\'c", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndNestedBrackets() {
        tq = new TokenQueue("(a \\[b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\[b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndNestedBraces() {
        tq = new TokenQueue("(a \\{b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\{b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndNestedParens() {
        tq = new TokenQueue("(a \\(b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\(b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndAllBrackets() {
        tq = new TokenQueue("(a \\[b\\{c\\(d)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \\[b\\{c\\(d", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndStringWithBrackets() {
        tq = new TokenQueue("(a \"b\\[c\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b\\[c\"", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndStringWithEscapedBrackets() {
        tq = new TokenQueue("(a \"b\\[c\\]d\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b\\[c\\]d\"", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndStringWithEscapedQuotes() {
        tq = new TokenQueue("(a \"b\\\"c\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b\\\"c\"", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndStringWithEscapedBackslash() {
        tq = new TokenQueue("(a \"b\\\\c\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b\\\\c\"", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndStringWithAllEscapes() {
        tq = new TokenQueue("(a \"b\\\\\\\"c\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b\\\\\\\"c\"", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndSingleQuoteString() {
        tq = new TokenQueue("(a 'b\\'c')");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a 'b\\'c'", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndDoubleQuoteInSingle() {
        tq = new TokenQueue("(a 'b\"c')");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a 'b\"c'", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndSingleQuoteInDouble() {
        tq = new TokenQueue("(a \"b'c\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b'c\"", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndMixedQuotes() {
        tq = new TokenQueue("(a \"b'c\" d 'e\"f')");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b'c\" d 'e\"f'", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedString() {
        tq = new TokenQueue("(a \"b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedSingleQuote() {
        tq = new TokenQueue("(a 'b)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a 'b", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedBoth() {
        tq = new TokenQueue("(a \"b 'c)");
        String result = tq.chompBalanced('(', ')');
        assertEquals("a \"b 'c", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndEmptyString() {
        tq = new TokenQueue("(\"\")");
        String result = tq.chompBalanced('(', ')');
        assertEquals("\"\"", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndEmptySingleQuote() {
        tq = new TokenQueue("('')");
        String result = tq.chompBalanced('(', ')');
        assertEquals("''", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndNestedEmpty() {
        tq = new TokenQueue("(()())");
        String result = tq.chompBalanced('(', ')');
        assertEquals("()()", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndDeepNestedEmpty() {
        tq = new TokenQueue("((()))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("(())", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndMixedNested() {
        tq = new TokenQueue("([{}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndAllTypes() {
        tq = new TokenQueue("([{()}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{()}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedAll() {
        tq = new TokenQueue("([{()}");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingAll() {
        tq = new TokenQueue("([{()}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{()}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndStringInNested() {
        tq = new TokenQueue("([{\"a\"}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\"a\"}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndEscapedInNested() {
        tq = new TokenQueue("([{\\\"a\\\"}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\\\"a\\\"}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndBackslashInNested() {
        tq = new TokenQueue("([{\\\\}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\\\\}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndAllEscapesInNested() {
        tq = new TokenQueue("([{\\\\\\\"a\\\"}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\\\\\\\"a\\\"}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedNestedString() {
        tq = new TokenQueue("([{\"a})");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedNestedEscaped() {
        tq = new TokenQueue("([{\\\"a})");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedNestedBackslash() {
        tq = new TokenQueue("([{\\\\})");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedNestedAll() {
        tq = new TokenQueue("([{\\\\\\\"a})");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingNested() {
        tq = new TokenQueue("([{\"a\"}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\"a\"}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingEscaped() {
        tq = new TokenQueue("([{\\\"a\\\"}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\\\"a\\\"}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingBackslash() {
        tq = new TokenQueue("([{\\\\}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\\\\}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingAll() {
        tq = new TokenQueue("([{\\\\\\\"a\\\"}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\\\\\\\"a\\\"}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndMultipleStringsInNested() {
        tq = new TokenQueue("([{\"a\" \"b\"}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\"a\" \"b\"}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndMixedStringsInNested() {
        tq = new TokenQueue("([{\"a\" 'b'}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\"a\" 'b'}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndEscapedStringsInNested() {
        tq = new TokenQueue("([{\\\"a\\\" \\'b\\'}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\\\"a\\\" \\'b\\'}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndAllCombinations() {
        tq = new TokenQueue("([{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedAllCombinations() {
        tq = new TokenQueue("([{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e})");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingAllCombinations() {
        tq = new TokenQueue("([{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e}])");
        String result = tq.chompBalanced('(', ')');
        assertEquals("[{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e}]", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndDeepNestedAllCombinations() {
        tq = new TokenQueue("((([{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e}])))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("(([{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e}]))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedDeepNested() {
        tq = new TokenQueue("((([{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e})))");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingDeepNested() {
        tq = new TokenQueue("((([{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e}])))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("(([{\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e}]))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndVeryDeepNested() {
        tq = new TokenQueue("((((((((((a))))))))))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("((((((((a))))))))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedVeryDeep() {
        tq = new TokenQueue("((((((((((a)))))))))");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingVeryDeep() {
        tq = new TokenQueue("((((((((((a)))))))))))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("((((((((a))))))))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndStringInVeryDeep() {
        tq = new TokenQueue("(((((((((\"a\")))))))))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("((((((((\"a\"))))))))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndEscapedInVeryDeep() {
        tq = new TokenQueue("(((((((((\"\\\"a\\\"\")))))))))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("((((((((\"\\\"a\\\"\"))))))))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndBackslashInVeryDeep() {
        tq = new TokenQueue("(((((((((\"\\\\a\\\\\")))))))))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("((((((((\"\\\\a\\\\\"))))))))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndAllInVeryDeep() {
        tq = new TokenQueue("(((((((((\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e)))))))))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("((((((((\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e))))))))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedVeryDeepAll() {
        tq = new TokenQueue("(((((((((\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e))))))))");
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingVeryDeepAll() {
        tq = new TokenQueue("(((((((((\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e))))))))))");
        String result = tq.chompBalanced('(', ')');
        assertEquals("((((((((\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e))))))))", result);
    }

    @Test
    public void testChompBalancedWithBackslashAndMaxDepth() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append('a');
        for (int i = 0; i < 100; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        String result = tq.chompBalanced('(', ')');
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedMaxDepth() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append('a');
        for (int i = 0; i < 99; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingMaxDepth() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append('a');
        for (int i = 0; i < 101; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        String result = tq.chompBalanced('(', ')');
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testChompBalancedWithBackslashAndStringInMaxDepth() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append("\"a\"");
        for (int i = 0; i < 100; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        String result = tq.chompBalanced('(', ')');
        assertNotNull(result);
        assertTrue(result.contains("\"a\""));
    }

    @Test
    public void testChompBalancedWithBackslashAndEscapedInMaxDepth() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append("\"\\\"a\\\"\"");
        for (int i = 0; i < 100; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        String result = tq.chompBalanced('(', ')');
        assertNotNull(result);
        assertTrue(result.contains("\"\\\"a\\\"\""));
    }

    @Test
    public void testChompBalancedWithBackslashAndBackslashInMaxDepth() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append("\"\\\\a\\\\\"");
        for (int i = 0; i < 100; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        String result = tq.chompBalanced('(', ')');
        assertNotNull(result);
        assertTrue(result.contains("\"\\\\a\\\\\""));
    }

    @Test
    public void testChompBalancedWithBackslashAndAllInMaxDepth() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append("\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e");
        for (int i = 0; i < 100; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        String result = tq.chompBalanced('(', ')');
        assertNotNull(result);
        assertTrue(result.contains("\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e"));
    }

    @Test
    public void testChompBalancedWithBackslashAndUnclosedMaxDepthAll() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append("\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e");
        for (int i = 0; i < 99; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        try {
            tq.chompBalanced('(', ')');
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testChompBalancedWithBackslashAndExtraClosingMaxDepthAll() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append('(');
        }
        sb.append("\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e");
        for (int i = 0; i < 101; i++) {
            sb.append(')');
        }
        tq = new TokenQueue(sb.toString());
        String result = tq.chompBalanced('(', ')');
        assertNotNull(result);
        assertTrue(result.contains("\"a\" 'b' \\\"c\\\" \\'d\\' \\\\e"));
    }
}