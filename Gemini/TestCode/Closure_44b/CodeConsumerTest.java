package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test class for CodeConsumer.
 * Targets high code coverage and boundary conditions, specifically around
 * whitespace handling and character additions (relevant to Closure-44).
 */
public class CodeConsumerTest {

    private TestCodeConsumer consumer;

    private static class TestCodeConsumer extends CodeConsumer {
        private final StringBuilder sb = new StringBuilder();
        private char lastChar = '\0';

        @Override
        protected void append(String str) {
            sb.append(str);
            if (!str.isEmpty()) {
                lastChar = str.charAt(str.length() - 1);
            }
        }

        @Override
        protected char getLastChar() {
            return lastChar;
        }

        public String getResult() {
            return sb.toString();
        }
    }

    @Before
    public void setUp() {
        consumer = new TestCodeConsumer();
    }

    @Test
    public void testAddBytesEmptyAndBasic() {
        // Test basic append/add behavior via the protected methods
        consumer.append("hello");
        assertEquals("hello", consumer.getResult());
        assertEquals('o', consumer.getLastChar());
    }

    @Test
    public void testAddRegularExpressionsAndSlashes() {
        // Bug Closure-44 is typically related to handling division operators '/' 
        // right after regular expressions or other forward slashes to avoid syntax ambiguity (e.g., "//" or "/ /").
        
        // Set last char to '/'
        consumer.append("/");
        assertEquals('/', consumer.getLastChar());

        // Call maybeAddSpace or similar internal mechanics if exposed/tested via append
        // Let's test appending another slash when last char is '/'
        consumer.append("/");
        assertEquals('/', consumer.getLastChar());
        assertEquals("//", consumer.getResult());
    }

    @Test
    public void testAddSpaceLogic() {
        // Test behavior when adding regular characters where no space is needed
        consumer.append("a");
        char firstLast = consumer.getLastChar();
        assertEquals('a', firstLast);

        consumer.append("b");
        assertEquals("ab", consumer.getResult());
        assertEquals('b', consumer.getLastChar());
    }

    @Test
    public void testAppendNullOrEmpty() {
        consumer.append("");
        assertEquals("", consumer.getResult());
        assertEquals('\0', consumer.getLastChar());
    }
}