package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

/**
 * Test class for RandomStringUtils.
 * Designed to achieve high coverage and detect the bug reported in Defects4J bug 11.
 */
public class RandomStringUtilsTest {

    // -----------------------------------------------------------------------
    // Tests for random(int count)
    // -----------------------------------------------------------------------

    @Test
    public void testRandomCountPositive() {
        String result = RandomStringUtils.random(10);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomCountZero() {
        RandomStringUtils.random(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomCountNegative() {
        RandomStringUtils.random(-5);
    }

    // -----------------------------------------------------------------------
    // Tests for random(int count, boolean letters, boolean numbers)
    // -----------------------------------------------------------------------

    @Test
    public void testRandomLettersOnly() {
        String result = RandomStringUtils.random(20, true, false);
        assertNotNull(result);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character not a letter: " + c, Character.isLetter(c));
        }
    }

    @Test
    public void testRandomNumbersOnly() {
        String result = RandomStringUtils.random(15, false, true);
        assertNotNull(result);
        assertEquals(15, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character not a digit: " + c, Character.isDigit(c));
        }
    }

    @Test
    public void testRandomLettersAndNumbers() {
        String result = RandomStringUtils.random(25, true, true);
        assertNotNull(result);
        assertEquals(25, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character not alphanumeric: " + c, Character.isLetterOrDigit(c));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNoLettersNoNumbers() {
        RandomStringUtils.random(10, false, false);
    }

    // -----------------------------------------------------------------------
    // Tests for random(int count, int start, int end, boolean letters, boolean numbers)
    // -----------------------------------------------------------------------

    @Test
    public void testRandomStartEndValid() {
        String result = RandomStringUtils.random(10, 65, 91, true, false); // A-Z
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character out of range: " + c, c >= 65 && c <= 90);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomStartNegative() {
        RandomStringUtils.random(10, -1, 10, true, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomEndNegative() {
        RandomStringUtils.random(10, 0, -1, true, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomStartGreaterThanEnd() {
        RandomStringUtils.random(10, 10, 5, true, true);
    }

    // -----------------------------------------------------------------------
    // Tests for random(int count, int start, int end, boolean letters, boolean numbers, char[] chars)
    // -----------------------------------------------------------------------

    @Test
    public void testRandomWithChars() {
        char[] chars = {'a', 'b', 'c'};
        String result = RandomStringUtils.random(10, 0, 0, false, false, chars);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character not in set: " + c, c == 'a' || c == 'b' || c == 'c');
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomWithCharsEmpty() {
        RandomStringUtils.random(10, 0, 0, false, false, new char[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomWithCharsNull() {
        RandomStringUtils.random(10, 0, 0, false, false, (char[]) null);
    }

    // -----------------------------------------------------------------------
    // Tests for random(int count, int start, int end, boolean letters, boolean numbers, char[] chars, Random random)
    // This is the method with the bug (Defects4J bug 11)
    // -----------------------------------------------------------------------

    @Test
    public void testRandomWithRandomObject() {
        Random rnd = new Random(42);
        String result = RandomStringUtils.random(10, 0, 0, false, false, new char[]{'x', 'y', 'z'}, rnd);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testRandomWithRandomObjectLettersNumbers() {
        Random rnd = new Random(123);
        String result = RandomStringUtils.random(20, 0, 0, true, true, null, rnd);
        assertNotNull(result);
        assertEquals(20, result.length());
    }

    // Bug detection: testLANG807 expects exception message to contain "start"
    @Test
    public void testLANG807() {
        try {
            RandomStringUtils.random(5, -1, 10, true, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            // The bug is that the message says "bound must be positive" instead of containing "start"
            // We assert that the message contains "start" to detect the bug.
            assertTrue("Exception message should contain 'start', but was: " + msg, msg.contains("start"));
        }
    }

    // Additional edge cases for the buggy method
    @Test(expected = IllegalArgumentException.class)
    public void testRandomStartNegativeWithRandom() {
        RandomStringUtils.random(5, -1, 10, true, true, null, new Random());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomEndNegativeWithRandom() {
        RandomStringUtils.random(5, 0, -1, true, true, null, new Random());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomStartGreaterThanEndWithRandom() {
        RandomStringUtils.random(5, 10, 5, true, true, null, new Random());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomCountZeroWithRandom() {
        RandomStringUtils.random(0, 0, 0, false, false, new char[]{'a'}, new Random());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomCountNegativeWithRandom() {
        RandomStringUtils.random(-1, 0, 0, false, false, new char[]{'a'}, new Random());
    }

    // Test with both letters and numbers false but chars provided (should work)
    @Test
    public void testRandomNoLettersNoNumbersWithChars() {
        String result = RandomStringUtils.random(5, 0, 0, false, false, new char[]{'1', '2', '3'});
        assertNotNull(result);
        assertEquals(5, result.length());
    }

    // Test with large count to ensure no overflow issues
    @Test
    public void testRandomLargeCount() {
        String result = RandomStringUtils.random(1000, true, true);
        assertNotNull(result);
        assertEquals(1000, result.length());
    }

    // Test that random with start=end and letters/numbers false uses chars if provided
    @Test
    public void testRandomStartEqualsEndWithChars() {
        char[] chars = {'A', 'B', 'C'};
        String result = RandomStringUtils.random(10, 0, 0, false, false, chars);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character not in set: " + c, c == 'A' || c == 'B' || c == 'C');
        }
    }

    // Test that random with start=end and letters/numbers true uses default range
    @Test
    public void testRandomStartEqualsEndLettersNumbers() {
        String result = RandomStringUtils.random(10, 0, 0, true, true, null);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character not alphanumeric: " + c, Character.isLetterOrDigit(c));
        }
    }

    // Test with specific start and end range for digits
    @Test
    public void testRandomDigitsRange() {
        String result = RandomStringUtils.random(10, 48, 58, false, true, null); // '0'-'9'
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character not a digit: " + c, c >= '0' && c <= '9');
        }
    }

    // Test with start and end that produce only letters
    @Test
    public void testRandomLettersRange() {
        String result = RandomStringUtils.random(10, 97, 123, true, false, null); // 'a'-'z'
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character not a lowercase letter: " + c, c >= 'a' && c <= 'z');
        }
    }
}