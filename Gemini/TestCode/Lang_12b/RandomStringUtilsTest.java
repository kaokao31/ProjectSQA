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
 * Unit tests for {@link RandomStringUtils}.
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
    public void testRandom() {
        String r1 = RandomStringUtils.random(50);
        assertEquals("random(50) length", 50, r1.length());
        String r2 = RandomStringUtils.random(50);
        assertEquals("random(50) length", 50, r2.length());
        assertFalse("!r1.equals(r2)", r1.equals(r2));

        assertEquals("", RandomStringUtils.random(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCount() {
        RandomStringUtils.random(-1);
    }

    @Test
    public void testRandomAscii() {
        String r1 = RandomStringUtils.randomAscii(50);
        assertEquals("randomAscii(50) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("char >= 32", c >= 32);
            assertTrue("char <= 127", c <= 127);
        }
        assertEquals("", RandomStringUtils.randomAscii(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAsciiNegativeCount() {
        RandomStringUtils.randomAscii(-1);
    }

    @Test
    public void testRandomAlphabetic() {
        String r1 = RandomStringUtils.randomAlphabetic(50);
        assertEquals("randomAlphabetic(50) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("isLetter", Character.isLetter(c));
        }
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAlphabeticNegativeCount() {
        RandomStringUtils.randomAlphabetic(-1);
    }

    @Test
    public void testRandomAlphanumeric() {
        String r1 = RandomStringUtils.randomAlphanumeric(50);
        assertEquals("randomAlphanumeric(50) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("isLetterOrDigit", Character.isLetterOrDigit(c));
        }
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAlphanumericNegativeCount() {
        RandomStringUtils.randomAlphanumeric(-1);
    }

    @Test
    public void testRandomNumeric() {
        String r1 = RandomStringUtils.randomNumeric(50);
        assertEquals("randomNumeric(50) length", 50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue("isDigit", Character.isDigit(c));
        }
        assertEquals("", RandomStringUtils.randomNumeric(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNumericNegativeCount() {
        RandomStringUtils.randomNumeric(-1);
    }

    @Test
    public void testRandomLettersNumbers() {
        String r1 = RandomStringUtils.random(50, true, true);
        assertEquals(50, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            char c = r1.charAt(i);
            assertTrue(Character.isLetterOrDigit(c));
        }

        String r2 = RandomStringUtils.random(50, true, false);
        assertEquals(50, r2.length());
        for (int i = 0; i < r2.length(); i++) {
            char c = r2.charAt(i);
            assertTrue(Character.isLetter(c));
        }

        String r3 = RandomStringUtils.random(50, false, true);
        assertEquals(50, r3.length());
        for (int i = 0; i < r3.length(); i++) {
            char c = r3.charAt(i);
            assertTrue(Character.isDigit(c));
        }

        String r4 = RandomStringUtils.random(50, false, false);
        assertEquals(50, r4.length());
    }

    @Test
    public void testRandomStringChars() {
        char[] chars = new char[]{'a', 'b', 'c'};
        String r = RandomStringUtils.random(50, chars);
        assertEquals(50, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue(c == 'a' || c == 'b' || c == 'c');
        }

        String set = "abc";
        r = RandomStringUtils.random(50, set);
        assertEquals(50, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue(c == 'a' || c == 'b' || c == 'c');
        }

        String nullSet = null;
        r = RandomStringUtils.random(50, nullSet);
        assertEquals(50, r.length());

        char[] nullChars = null;
        r = RandomStringUtils.random(50, nullChars);
        assertEquals(50, r.length());
    }

    @Test
    public void testRandomWithSpecificRandomGenerator() {
        Random fixedRandom = new Random(12345L);
        String r1 = RandomStringUtils.random(10, 0, 0, false, false, null, fixedRandom);
        Random fixedRandom2 = new Random(12345L);
        String r2 = RandomStringUtils.random(10, 0, 0, false, false, null, fixedRandom2);
        assertEquals(r1, r2);
    }

    @Test
    public void testLANG805() {
        // LANG-805: chars array provided with start=0, end=0
        long count = 10;
        char[] chars = {'a', 'b', 'c'};
        String result = RandomStringUtils.random((int) count, 0, 0, false, false, chars, new Random());
        assertEquals(count, result.length());
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            assertTrue(c == 'a' || c == 'b' || c == 'c');
        }
    }

    @Test
    public void testExceptions() {
        try {
            RandomStringUtils.random(-1);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(-1, true, true);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(-1, new char[0]);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(-1, "");
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(-1, 0, 0, false, false);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(-1, 0, 0, false, false, new char[0]);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(-1, 0, 0, false, false, new char[0], new Random());
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(1, 10, 5, false, false, new char[0], new Random());
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(1, 0, 0, false, false, new char[0], new Random());
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(1, new char[0]);
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
        try {
            RandomStringUtils.random(1, "");
            fail();
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testSurrogatesHandling() {
        // High surrogate \uD800, Low surrogate \uDC00
        char[] chars = new char[]{'\uD800', '\uDC00', 'a'};
        String result = RandomStringUtils.random(10, 0, chars.length, false, false, chars, new Random());
        assertEquals(10, result.length());

        // Test with start and end in surrogate range
        String resultSurr = RandomStringUtils.random(20, 56192, 56320, false, false, null, new Random());
        assertEquals(20, resultSurr.length());
    }

    @Test
    public void testZeroCountReturnsEmptyString() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.random(0, true, true));
        assertEquals("", RandomStringUtils.random(0, 0, 0, true, true));
        assertEquals("", RandomStringUtils.random(0, 0, 0, true, true, new char[]{'a'}));
        assertEquals("", RandomStringUtils.random(0, 0, 0, true, true, new char[]{'a'}, new Random()));
        assertEquals("", RandomStringUtils.random(0, "abc"));
        assertEquals("", RandomStringUtils.random(0, 'a', 'b'));
        assertEquals("", RandomStringUtils.randomAscii(0));
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        assertEquals("", RandomStringUtils.randomNumeric(0));
    }
}