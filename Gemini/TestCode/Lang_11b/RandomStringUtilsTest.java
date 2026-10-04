package org.apache.commons.lang3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Random;

import org.junit.Test;

/**
 * Unit tests for {@link org.apache.commons.lang3.RandomStringUtils}.
 */
public class RandomStringUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new RandomStringUtils());
        Constructor<?>[] cons = RandomStringUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(RandomStringUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(RandomStringUtils.class.getModifiers()));
    }

    @Test
    public void testRandomStringUtils() {
        String r1 = RandomStringUtils.random(50);
        assertEquals("random(50) length", 50, r1.length());
        String r2 = RandomStringUtils.random(50);
        assertEquals("random(50) length", 50, r2.length());
        assertFalse("RandomStringUtils.random(50) generates distinct results", r1.equals(r2));

        r1 = RandomStringUtils.randomAscii(50);
        assertEquals("randomAscii(50) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("char between 32 and 127", c >= 32 && c <= 127);
        }
        r2 = RandomStringUtils.randomAscii(50);
        assertFalse("RandomStringUtils.randomAscii(50) generates distinct results", r1.equals(r2));

        r1 = RandomStringUtils.randomAlphabetic(50);
        assertEquals("randomAlphabetic(50) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("char is letter", Character.isLetter(c));
        }
        r2 = RandomStringUtils.randomAlphabetic(50);
        assertFalse("RandomStringUtils.randomAlphabetic(50) generates distinct results", r1.equals(r2));

        r1 = RandomStringUtils.randomAlphanumeric(50);
        assertEquals("randomAlphanumeric(50) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("char is letter or digit", Character.isLetterOrDigit(c));
        }
        r2 = RandomStringUtils.randomAlphanumeric(50);
        assertFalse("RandomStringUtils.randomAlphanumeric(50) generates distinct results", r1.equals(r2));

        r1 = RandomStringUtils.randomNumeric(50);
        assertEquals("randomNumeric(50) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("char is digit", Character.isDigit(c));
        }
        r2 = RandomStringUtils.randomNumeric(50);
        assertFalse("RandomStringUtils.randomNumeric(50) generates distinct results", r1.equals(r2));

        r1 = RandomStringUtils.random(50, false, true);
        assertEquals("random(50, false, true) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("char is digit", Character.isDigit(c));
        }

        r1 = RandomStringUtils.random(50, true, false);
        assertEquals("random(50, true, false) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("char is letter", Character.isLetter(c));
        }

        r1 = RandomStringUtils.random(50, false, false);
        assertEquals("random(50, false, false) length", 50, r1.length());

        r1 = RandomStringUtils.random(50, (String) null);
        assertEquals("random(50, null) length", 50, r1.length());

        r1 = RandomStringUtils.random(50, (char[]) null);
        assertEquals("random(50, (char[]) null) length", 50, r1.length());
    }

    @Test
    public void testRandomStringZeroAndNegativeCount() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.randomAscii(0));
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        assertEquals("", RandomStringUtils.randomNumeric(0));
        assertEquals("", RandomStringUtils.random(0, true, true));
        assertEquals("", RandomStringUtils.random(0, 0, 0, true, true));
        assertEquals("", RandomStringUtils.random(0, 'a', 'b', 'c'));
        assertEquals("", RandomStringUtils.random(0, "abc"));
        assertEquals("", RandomStringUtils.random(0, new char[]{'a', 'b', 'c'}));

        try {
            RandomStringUtils.random(-1);
            fail("Expected IllegalArgumentException for negative count");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            RandomStringUtils.random(-1, true, true);
            fail("Expected IllegalArgumentException for negative count");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            RandomStringUtils.random(-1, 0, 10, true, true);
            fail("Expected IllegalArgumentException for negative count");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            RandomStringUtils.random(-1, "abc");
            fail("Expected IllegalArgumentException for negative count");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            RandomStringUtils.random(-1, 'a', 'b');
            fail("Expected IllegalArgumentException for negative count");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testRandomStringWithCharArray() {
        char[] set = new char[]{'a', 'b', 'c', 'd'};
        String r = RandomStringUtils.random(100, set);
        assertEquals(100, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("char is in set", c == 'a' || c == 'b' || c == 'c' || c == 'd');
        }

        r = RandomStringUtils.random(100, 'x', 'y', 'z');
        assertEquals(100, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("char is in set", c == 'x' || c == 'y' || c == 'z');
        }
    }

    @Test
    public void testRandomStringWithString() {
        String set = "abc123";
        String r = RandomStringUtils.random(100, set);
        assertEquals(100, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("char is in set", set.indexOf(c) >= 0);
        }
    }

    @Test
    public void testRandomWithSpecificRandom() {
        Random random = new Random(12345L);
        String r1 = RandomStringUtils.random(10, 0, 0, true, true, null, random);
        random = new Random(12345L);
        String r2 = RandomStringUtils.random(10, 0, 0, true, true, null, random);
        assertEquals("Seeded Random should yield deterministic output", r1, r2);
    }

    @Test
    public void testRandomWithStartAndEndBounds() {
        String r = RandomStringUtils.random(50, 32, 64, false, false);
        assertEquals(50, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("char within bounds", c >= 32 && c < 64);
        }

        try {
            RandomStringUtils.random(50, 64, 32, false, false);
            fail("Expected IllegalArgumentException when start > end");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testLANG807() {
        try {
            RandomStringUtils.random(3, new char[0]);
            fail("Expected IllegalArgumentException for empty char array");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            RandomStringUtils.random(3, 0, 0, false, false, new char[0]);
            fail("Expected IllegalArgumentException for empty char array");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        try {
            RandomStringUtils.random(3, 0, 0, false, false, new char[0], new Random());
            fail("Expected IllegalArgumentException for empty char array");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testSurrogatePairs() {
        // High surrogate (0xD800 - 0xDBFF) followed by Low surrogate (0xDC00 - 0xDFFF)
        char highSurrogate = '\uD83D';
        char lowSurrogate = '\uDE00';
        char[] chars = new char[]{highSurrogate, lowSurrogate, 'a', 'b', 'c'};
        String r = RandomStringUtils.random(10, 0, 0, false, false, chars, new Random(42L));
        assertNotNull(r);
        assertEquals(10, r.length());
    }

    @Test
    public void testRandomCharsWithStartEndOffset() {
        char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        String r = RandomStringUtils.random(20, 2, 6, false, false, chars, new Random(100L));
        assertEquals(20, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("char within subarray range", c >= '2' && c <= '5');
        }
    }
}