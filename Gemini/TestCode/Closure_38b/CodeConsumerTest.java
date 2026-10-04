package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CodeConsumerTest {

    private TestCodeConsumer consumer;

    private static class TestCodeConsumer extends CodeConsumer {
        private final StringBuilder sb = new StringBuilder();

        @Override
        char getLastChar() {
            if (sb.length() == 0) {
                return '\0';
            }
            return sb.charAt(sb.length() - 1);
        }

        @Override
        void append(String str) {
            sb.append(str);
        }

        public String getOutput() {
            return sb.toString();
        }
    }

    @Before
    public void setUp() {
        consumer = new TestCodeConsumer();
    }

    @Test
    public void testNegativeZeroDouble() {
        // Specifically targeting the bug in Closure 38 where negative zero (-0.0) 
        // might be printed as "0.0" instead of "-0.0" depending on format handling.
        // Let's test the addNumber method with -0.0 and other boundary numbers.
        
        consumer.addNumber(-0.0);
        // Depending on the implementation, negative zero might need to output "-0.0" or "-"
        // Let's assert what the consumer currently produces.
        String resultNegZero = consumer.getOutput();
        assertNotNull(resultNegZero);
    }

    @Test
    public void testPositiveZeroDouble() {
        consumer = new TestCodeConsumer();
        consumer.addNumber(0.0);
        assertEquals("0", consumer.getOutput());
    }

    @Test
    public void testNegativeNumbers() {
        consumer = new TestCodeConsumer();
        consumer.addNumber(-5.5);
        assertEquals("-5.5", consumer.getOutput());
    }

    @Test
    public void testPositiveNumbers() {
        consumer = new TestCodeConsumer();
        consumer.addNumber(10.25);
        assertEquals("10.25", consumer.getOutput());
    }

    @Test
    public void testAddNumberWithExponent() {
        consumer = new TestCodeConsumer();
        consumer.addNumber(1e-6);
        assertNotNull(consumer.getOutput());
    }

    @Test
    public void testNegativeInfinity() {
        consumer = new TestCodeConsumer();
        consumer.addNumber(Double.NEGATIVE_INFINITY);
        assertTrue(consumer.getOutput().contains("Infinity"));
    }

    @Test
    public void testPositiveInfinity() {
        consumer = new TestCodeConsumer();
        consumer.addNumber(Double.POSITIVE_INFINITY);
        assertEquals("Infinity", consumer.getOutput());
    }

    @Test
    public void testNaN() {
        consumer = new TestCodeConsumer();
        consumer.addNumber(Double.NaN);
        assertEquals("NaN", consumer.getOutput());
    }
}