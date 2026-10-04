package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for CodeConsumer, targeting maximum coverage and fault detection.
 * Based on Defects4J Closure bug 38 context.
 */
public class CodeConsumerTest {

    private TestCodeConsumer consumer;
    private StringBuilder output;

    @Before
    public void setUp() {
        output = new StringBuilder();
        consumer = new TestCodeConsumer(output);
    }

    // --- Helper concrete subclass of abstract CodeConsumer ---
    private static class TestCodeConsumer extends CodeConsumer {
        private final StringBuilder sb;
        private boolean lastCharSpace = false;
        private boolean newLine = true;

        TestCodeConsumer(StringBuilder sb) {
            this.sb = sb;
        }

        @Override
        void append(String str) {
            sb.append(str);
            lastCharSpace = str.endsWith(" ");
            newLine = false;
        }

        @Override
        void startNewLine() {
            sb.append("\n");
            newLine = true;
            lastCharSpace = false;
        }

        @Override
        boolean isWordChar(char ch) {
            return Character.isLetterOrDigit(ch) || ch == '_' || ch == '$';
        }

        @Override
        boolean isWordBreak(char ch) {
            return !isWordChar(ch);
        }

        @Override
        void maybeLineBreak() {
            // no-op for testing
        }

        @Override
        void maybeCutLine() {
            // no-op for testing
        }

        @Override
        void endFile() {
            // no-op
        }

        @Override
        boolean hasMultipleSpaces() {
            return false;
        }

        @Override
        boolean isNewLine() {
            return newLine;
        }

        @Override
        boolean isLastCharSpace() {
            return lastCharSpace;
        }

        @Override
        void setLastCharSpace(boolean b) {
            lastCharSpace = b;
        }

        @Override
        void setNewLine(boolean b) {
            newLine = b;
        }
    }

    // --- Basic add tests ---
    @Test
    public void testAddSimpleString() {
        consumer.add("hello");
        assertEquals("hello", output.toString());
    }

    @Test
    public void testAddEmptyString() {
        consumer.add("");
        assertEquals("", output.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        consumer.add(null);
    }

    @Test
    public void testAddMultipleStrings() {
        consumer.add("foo");
        consumer.add("bar");
        assertEquals("foobar", output.toString());
    }

    @Test
    public void testAddWithSpaceBefore() {
        consumer.add("foo");
        consumer.add(" bar");
        assertEquals("foo bar", output.toString());
    }

    @Test
    public void testAddIdentifier() {
        consumer.addIdentifier("x");
        assertEquals("x", output.toString());
    }

    @Test
    public void testAddIdentifierWithKeyword() {
        consumer.addIdentifier("if");
        assertEquals("if", output.toString());
    }

    @Test
    public void testAddIdentifierAfterSpace() {
        consumer.add(" ");
        consumer.addIdentifier("y");
        assertEquals(" y", output.toString());
    }

    @Test
    public void testAddIdentifierAfterNewLine() {
        consumer.startNewLine();
        consumer.addIdentifier("z");
        assertEquals("\nz", output.toString());
    }

    // --- addNumber tests ---
    @Test
    public void testAddNumberInteger() {
        consumer.addNumber(42);
        assertEquals("42", output.toString());
    }

    @Test
    public void testAddNumberDouble() {
        consumer.addNumber(3.14);
        assertEquals("3.14", output.toString());
    }

    @Test
    public void testAddNumberNegative() {
        consumer.addNumber(-5);
        assertEquals("-5", output.toString());
    }

    @Test
    public void testAddNumberZero() {
        consumer.addNumber(0);
        assertEquals("0", output.toString());
    }

    @Test
    public void testAddNumberLarge() {
        consumer.addNumber(1e10);
        assertEquals("10000000000", output.toString());
    }

    // --- addConstant tests ---
    @Test
    public void testAddConstantString() {
        consumer.addConstant("'hello'");
        assertEquals("'hello'", output.toString());
    }

    @Test
    public void testAddConstantNumber() {
        consumer.addConstant("123");
        assertEquals("123", output.toString());
    }

    @Test
    public void testAddConstantBoolean() {
        consumer.addConstant("true");
        assertEquals("true", output.toString());
    }

    @Test
    public void testAddConstantNull() {
        consumer.addConstant("null");
        assertEquals("null", output.toString());
    }

    // --- startNewLine tests ---
    @Test
    public void testStartNewLine() {
        consumer.add("a");
        consumer.startNewLine();
        consumer.add("b");
        assertEquals("a\nb", output.toString());
    }

    @Test
    public void testStartNewLineMultiple() {
        consumer.startNewLine();
        consumer.startNewLine();
        consumer.add("c");
        assertEquals("\n\nc", output.toString());
    }

    // --- maybeInsertSpace tests ---
    @Test
    public void testMaybeInsertSpaceAfterWord() {
        consumer.add("foo");
        consumer.maybeInsertSpace();
        consumer.add("bar");
        assertEquals("foo bar", output.toString());
    }

    @Test
    public void testMaybeInsertSpaceAfterSpace() {
        consumer.add("foo ");
        consumer.maybeInsertSpace();
        consumer.add("bar");
        assertEquals("foo bar", output.toString());
    }

    @Test
    public void testMaybeInsertSpaceAfterNewLine() {
        consumer.startNewLine();
        consumer.maybeInsertSpace();
        consumer.add("bar");
        assertEquals("\nbar", output.toString());
    }

    @Test
    public void testMaybeInsertSpaceAfterPunctuation() {
        consumer.add(";");
        consumer.maybeInsertSpace();
        consumer.add("x");
        assertEquals("; x", output.toString());
    }

    // --- Edge cases for bug detection (Closure bug 38 context) ---
    @Test
    public void testNoExtraSpaceBeforeStringLiteralAfterColon() {
        // Simulate object literal: {key: "value"}
        consumer.add("{");
        consumer.addIdentifier("key");
        consumer.add(":");
        // This should not add a space before the string literal
        consumer.add("\"value\"");
        consumer.add("}");
        assertEquals("{key:\"value\"}", output.toString());
    }

    @Test
    public void testNoExtraSpaceBeforeNumberAfterColon() {
        consumer.add("{");
        consumer.addIdentifier("a");
        consumer.add(":");
        consumer.addNumber(1);
        consumer.add("}");
        assertEquals("{a:1}", output.toString());
    }

    @Test
    public void testSpaceAfterCommaInObject() {
        consumer.add("{");
        consumer.addIdentifier("a");
        consumer.add(":");
        consumer.addNumber(1);
        consumer.add(",");
        consumer.addIdentifier("b");
        consumer.add(":");
        consumer.addNumber(2);
        consumer.add("}");
        assertEquals("{a:1,b:2}", output.toString());
    }

    @Test
    public void testSpaceAfterCommaInArray() {
        consumer.add("[");
        consumer.addNumber(1);
        consumer.add(",");
        consumer.addNumber(2);
        consumer.add("]");
        assertEquals("[1,2]", output.toString());
    }

    @Test
    public void testSpaceAfterSemicolon() {
        consumer.add("x");
        consumer.add(";");
        consumer.add("y");
        assertEquals("x;y", output.toString());
    }

    @Test
    public void testSpaceAfterBinaryOperator() {
        consumer.add("a");
        consumer.add("+");
        consumer.add("b");
        assertEquals("a+b", output.toString());
    }

    @Test
    public void testSpaceAfterUnaryOperator() {
        consumer.add("!");
        consumer.add("x");
        assertEquals("!x", output.toString());
    }

    @Test
    public void testSpaceAfterDot() {
        consumer.add("obj");
        consumer.add(".");
        consumer.addIdentifier("prop");
        assertEquals("obj.prop", output.toString());
    }

    @Test
    public void testSpaceAfterOpenParen() {
        consumer.add("(");
        consumer.add("a");
        assertEquals("(a", output.toString());
    }

    @Test
    public void testSpaceBeforeCloseParen() {
        consumer.add("a");
        consumer.add(")");
        assertEquals("a)", output.toString());
    }

    @Test
    public void testSpaceAfterOpenBracket() {
        consumer.add("[");
        consumer.add("1");
        assertEquals("[1", output.toString());
    }

    @Test
    public void testSpaceBeforeCloseBracket() {
        consumer.add("1");
        consumer.add("]");
        assertEquals("1]", output.toString());
    }

    @Test
    public void testSpaceAfterOpenBrace() {
        consumer.add("{");
        consumer.add("a");
        assertEquals("{a", output.toString());
    }

    @Test
    public void testSpaceBeforeCloseBrace() {
        consumer.add("a");
        consumer.add("}");
        assertEquals("a}", output.toString());
    }

    @Test
    public void testSpaceAfterQuestionMark() {
        consumer.add("a");
        consumer.add("?");
        consumer.add("b");
        assertEquals("a?b", output.toString());
    }

    @Test
    public void testSpaceAfterColonInTernary() {
        consumer.add("a");
        consumer.add("?");
        consumer.add("b");
        consumer.add(":");
        consumer.add("c");
        assertEquals("a?b:c", output.toString());
    }

    // --- Tests for word boundaries and identifier detection ---
    @Test
    public void testAddIdentifierAfterNumber() {
        consumer.addNumber(1);
        consumer.addIdentifier("x");
        assertEquals("1 x", output.toString());
    }

    @Test
    public void testAddIdentifierAfterIdentifier() {
        consumer.addIdentifier("foo");
        consumer.addIdentifier("bar");
        assertEquals("foo bar", output.toString());
    }

    @Test
    public void testAddIdentifierAfterStringLiteral() {
        consumer.add("\"str\"");
        consumer.addIdentifier("x");
        assertEquals("\"str\" x", output.toString());
    }

    @Test
    public void testAddIdentifierAfterCloseParen() {
        consumer.add(")");
        consumer.addIdentifier("x");
        assertEquals(") x", output.toString());
    }

    @Test
    public void testAddIdentifierAfterOpenParen() {
        consumer.add("(");
        consumer.addIdentifier("x");
        assertEquals("(x", output.toString());
    }

    // --- Tests for newline handling ---
    @Test
    public void testNewLineAfterStatement() {
        consumer.add("x");
        consumer.add(";");
        consumer.startNewLine();
        consumer.add("y");
        assertEquals("x;\ny", output.toString());
    }

    @Test
    public void testMultipleNewLines() {
        consumer.startNewLine();
        consumer.startNewLine();
        consumer.startNewLine();
        consumer.add("a");
        assertEquals("\n\n\na", output.toString());
    }

    // --- Tests for maybeCutLine (no-op in test, but ensure no exception) ---
    @Test
    public void testMaybeCutLineDoesNotThrow() {
        consumer.maybeCutLine();
        consumer.add("test");
        assertEquals("test", output.toString());
    }

    // --- Tests for endFile (no-op) ---
    @Test
    public void testEndFileDoesNotThrow() {
        consumer.endFile();
        consumer.add("end");
        assertEquals("end", output.toString());
    }

    // --- Tests for isWordChar and isWordBreak (indirectly) ---
    @Test
    public void testAddIdentifierWithUnderscore() {
        consumer.addIdentifier("_foo");
        assertEquals("_foo", output.toString());
    }

    @Test
    public void testAddIdentifierWithDollar() {
        consumer.addIdentifier("$bar");
        assertEquals("$bar", output.toString());
    }

    @Test
    public void testAddIdentifierWithDigits() {
        consumer.addIdentifier("x1");
        assertEquals("x1", output.toString());
    }

    @Test
    public void testAddIdentifierStartingWithDigit() {
        // This is not a valid identifier, but CodeConsumer may still add it
        consumer.addIdentifier("1x");
        assertEquals("1x", output.toString());
    }

    // --- Tests for special characters ---
    @Test
    public void testAddSpecialChars() {
        consumer.add("~!@#$%^&*()_+-=[]{}|;':\",./<>?");
        assertEquals("~!@#$%^&*()_+-=[]{}|;':\",./<>?", output.toString());
    }

    @Test
    public void testAddUnicode() {
        consumer.add("héllo");
        assertEquals("héllo", output.toString());
    }

    // --- Tests for state transitions ---
    @Test
    public void testStateAfterAdd() {
        assertFalse(consumer.isNewLine());
        assertFalse(consumer.isLastCharSpace());
        consumer.add(" ");
        assertTrue(consumer.isLastCharSpace());
        consumer.add("x");
        assertFalse(consumer.isLastCharSpace());
    }

    @Test
    public void testStateAfterNewLine() {
        consumer.startNewLine();
        assertTrue(consumer.isNewLine());
        assertFalse(consumer.isLastCharSpace());
    }

    @Test
    public void testSetLastCharSpace() {
        consumer.setLastCharSpace(true);
        assertTrue(consumer.isLastCharSpace());
        consumer.setLastCharSpace(false);
        assertFalse(consumer.isLastCharSpace());
    }

    @Test
    public void testSetNewLine() {
        consumer.setNewLine(true);
        assertTrue(consumer.isNewLine());
        consumer.setNewLine(false);
        assertFalse(consumer.isNewLine());
    }

    // --- Tests for potential bug: space before string literal after operator ---
    @Test
    public void testNoSpaceBeforeStringAfterPlus() {
        consumer.add("a");
        consumer.add("+");
        consumer.add("\"b\"");
        assertEquals("a+\"b\"", output.toString());
    }

    @Test
    public void testNoSpaceBeforeStringAfterEquals() {
        consumer.add("x");
        consumer.add("=");
        consumer.add("\"y\"");
        assertEquals("x=\"y\"", output.toString());
    }

    @Test
    public void testSpaceBeforeStringAfterKeyword() {
        consumer.add("return");
        consumer.maybeInsertSpace();
        consumer.add("\"value\"");
        assertEquals("return \"value\"", output.toString());
    }

    // --- Tests for multiple spaces (should not happen normally) ---
    @Test
    public void testMultipleSpacesNotAdded() {
        consumer.add(" ");
        consumer.add(" ");
        consumer.add("a");
        assertEquals("  a", output.toString());
    }

    // --- Tests for hasMultipleSpaces (always false in test) ---
    @Test
    public void testHasMultipleSpaces() {
        assertFalse(consumer.hasMultipleSpaces());
    }

    // --- Tests for maybeLineBreak (no-op) ---
    @Test
    public void testMaybeLineBreakDoesNotThrow() {
        consumer.maybeLineBreak();
        consumer.add("test");
        assertEquals("test", output.toString());
    }
}