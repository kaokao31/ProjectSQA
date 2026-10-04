package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CodeConsumerTest {

    private TestCodeConsumer consumer;

    // Concrete implementation of the abstract CodeConsumer class for testing
    private static class TestCodeConsumer extends CodeConsumer {
        private final StringBuilder sb = new StringBuilder();

        @Override
        void append(String str) {
            sb.append(str);
        }

        @Override
        char getLastChar() {
            if (sb.length() == 0) {
                return '\0';
            }
            return sb.charAt(sb.length() - 1);
        }

        public String getResult() {
            return sb.toString();
        }

        public void clear() {
            sb.setLength(0);
        }
    }

    @Before
    public void setUp() {
        consumer = new TestCodeConsumer();
    }

    @Test
    public void testAddNumberBasic() {
        // Test normal positive integer
        consumer.addNumber(10);
        assertEquals("10", consumer.getResult());
    }

    @Test
    public void testAddNumberZero() {
        consumer.addNumber(0);
        assertEquals("0", consumer.getResult());
    }

    @Test
    public void testAddNumberNegative() {
        // Closure CodeConsumer specifically handles negative numbers or unary minus interaction
        consumer.addNumber(-5);
        assertEquals("-5", consumer.getResult());
    }

    @Test
    public void testAddNumberWithDecimal() {
        consumer.addNumber(10.5);
        assertEquals("10.5", consumer.getResult());
    }

    @Test
    public void testAddNumberScientificNotation() {
        consumer.addNumber(1e6);
        assertEquals("1000000", consumer.getResult());
    }

    @Test
    public void testAddNumberIntegerBoundary() {
        consumer.addNumber(100.0);
        assertEquals("100", consumer.getResult());
    }

    @Test
    public void testAddNumberIntegerNegativeBoundary() {
        consumer.addNumber(-100.0);
        assertEquals("-100", consumer.getResult());
    }

    @Test
    public void testAddNumberFractionalZero() {
        // E.g. 3.0 vs 3
        consumer.addNumber(3.0);
        assertEquals("3", consumer.getResult());
    }

    @Test
    public void testAddNumberNegativeFractionalZero() {
        consumer.addNumber(-3.0);
        assertEquals("-3", consumer.getResult());
    }

    @Test
    public void testAddNumberNaNAndInfinity() {
        consumer.addNumber(Double.NaN);
        // Depending on implementation, NaN might format as "NaN"
        assertTrue(consumer.getResult().contains("NaN"));
        
        consumer.clear();
        consumer.addNumber(Double.POSITIVE_INFINITY);
        assertTrue(consumer.getResult().contains("Infinity"));
    }

    @Test
    public void testWhitespaceAndFormatting() {
        // Exercise helper methods if present in CodeConsumer
        consumer.maybeEndStatement();
        assertNotNull(consumer.getResult());
    }
}