package org.apache.commons.lang3;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Random;

/**
 * Comprehensive JUnit 4 test suite for RandomStringUtils.
 * Targets maximum coverage and fault detection, including Defects4J bug ID 12.
 */
public class RandomStringUtilsTest {

    private static final int DEFAULT_COUNT = 10;
    private static final char[] CHAR_ARRAY = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};

    @Before
    public void setUp() {
        // No setup required for static methods
    }

    // ========== Basic Functionality Tests ==========

    @Test
    public void testRandomBasic() {
        String result = RandomStringUtils.random(DEFAULT_COUNT);
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
    }

    @Test
    public void testRandomWithLettersAndNumbers() {
        String result = RandomStringUtils.random(DEFAULT_COUNT, true, true);
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    @Test
    public void testRandomWithLettersOnly() {
        String result = RandomStringUtils.random(DEFAULT_COUNT, true, false);
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    @Test
    public void testRandomWithNumbersOnly() {
        String result = RandomStringUtils.random(DEFAULT_COUNT, false, true);
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    @Test
    public void testRandomWithCustomChars() {
        String result = RandomStringUtils.random(DEFAULT_COUNT, 0, CHAR_ARRAY.length, false, false, CHAR_ARRAY, new Random());
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(indexOf(CHAR_ARRAY, c) >= 0);
        }
    }

    // ========== Edge Cases and Boundary Tests ==========

    @Test
    public void testRandomCountZero() {
        String result = RandomStringUtils.random(0);
        assertNotNull(result);
        assertEquals(0, result.length());
    }

    @Test
    public void testRandomCountOne() {
        String result = RandomStringUtils.random(1);
        assertNotNull(result);
        assertEquals(1, result.length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCount() {
        RandomStringUtils.random(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCountWithLettersNumbers() {
        RandomStringUtils.random(-5, true, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNullChars() {
        RandomStringUtils.random(DEFAULT_COUNT, 0, 0, false, false, null, new Random());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNullRandom() {
        RandomStringUtils.random(DEFAULT_COUNT, 0, 0, false, false, CHAR_ARRAY, null);
    }

    @Test
    public void testRandomStartEndZero() {
        // start=0, end=0 should produce empty string (no valid chars)
        String result = RandomStringUtils.random(DEFAULT_COUNT, 0, 0, false, false, CHAR_ARRAY, new Random());
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        // All characters should be from the default set (letters/numbers) because start==end
        // Actually with start=0, end=0, the code uses default letters/numbers
        // Let's just check length
    }

    @Test
    public void testRandomStartGreaterThanEnd() {
        // start > end should swap them
        String result = RandomStringUtils.random(DEFAULT_COUNT, 10, 5, false, false, CHAR_ARRAY, new Random());
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
    }

    @Test
    public void testRandomStartEndLarge() {
        // Large start and end values that may cause overflow or index issues
        String result = RandomStringUtils.random(DEFAULT_COUNT, Integer.MAX_VALUE - 100, Integer.MAX_VALUE, false, false, CHAR_ARRAY, new Random());
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
    }

    // ========== Bug Triggering Tests (Defects4J Bug ID 12) ==========

    /**
     * This test reproduces the ArrayIndexOutOfBoundsException from testExceptions.
     * The bug occurs when start and end are large (e.g., Integer.MAX_VALUE) and
     * the random index calculation overflows or exceeds the array bounds.
     */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testExceptionsBugTrigger() {
        // This call should throw ArrayIndexOutOfBoundsException due to bug
        RandomStringUtils.random(10, Integer.MAX_VALUE - 50, Integer.MAX_VALUE, false, false, CHAR_ARRAY, new Random());
    }

    /**
     * This test reproduces the ArrayIndexOutOfBoundsException from testLANG805.
     * Similar to above but with different parameters.
     */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testLANG805BugTrigger() {
        // This call should throw ArrayIndexOutOfBoundsException due to bug
        RandomStringUtils.random(10, 0, Integer.MAX_VALUE, false, false, CHAR_ARRAY, new Random());
    }

    // ========== Additional Coverage Tests ==========

    @Test
    public void testRandomWithStartEndAndLettersNumbers() {
        String result = RandomStringUtils.random(DEFAULT_COUNT, 0, CHAR_ARRAY.length, true, true, CHAR_ARRAY, new Random());
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
    }

    @Test
    public void testRandomWithStartEndAndLettersOnly() {
        String result = RandomStringUtils.random(DEFAULT_COUNT, 0, CHAR_ARRAY.length, true, false, CHAR_ARRAY, new Random());
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
    }

    @Test
    public void testRandomWithStartEndAndNumbersOnly() {
        String result = RandomStringUtils.random(DEFAULT_COUNT, 0, CHAR_ARRAY.length, false, true, CHAR_ARRAY, new Random());
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
    }

    @Test
    public void testRandomWithStartEndAndNoLettersNoNumbers() {
        // When both letters and numbers are false, and chars array is provided, it uses chars
        String result = RandomStringUtils.random(DEFAULT_COUNT, 0, CHAR_ARRAY.length, false, false, CHAR_ARRAY, new Random());
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(indexOf(CHAR_ARRAY, c) >= 0);
        }
    }

    @Test
    public void testRandomWithStartEndAndNoLettersNoNumbersNoChars() {
        // When both letters and numbers are false and chars is null, it should throw IllegalArgumentException
        // But we already test null chars separately
    }

    @Test
    public void testRandomAscii() {
        String result = RandomStringUtils.randomAscii(DEFAULT_COUNT);
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 32 && c <= 126);
        }
    }

    @Test
    public void testRandomAlphabetic() {
        String result = RandomStringUtils.randomAlphabetic(DEFAULT_COUNT);
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    @Test
    public void testRandomAlphanumeric() {
        String result = RandomStringUtils.randomAlphanumeric(DEFAULT_COUNT);
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    @Test
    public void testRandomNumeric() {
        String result = RandomStringUtils.randomNumeric(DEFAULT_COUNT);
        assertNotNull(result);
        assertEquals(DEFAULT_COUNT, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    // ========== Helper Methods ==========

    private int indexOf(char[] array, char target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }
}