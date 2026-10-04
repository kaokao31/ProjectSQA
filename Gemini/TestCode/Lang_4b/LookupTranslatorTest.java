package org.apache.commons.lang3.text.translate;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link LookupTranslator}.
 */
public class LookupTranslatorTest {

    @Test
    public void testBasicLookup() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "one", "two" },
            { "three", "four" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int result = lt.translate("one", 0, out);
        assertEquals("Failed to translate basic string", 3, result);
        assertEquals("two", out.toString());
    }

    @Test
    public void testLookupWithNoMatch() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "one", "two" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int result = lt.translate("three", 0, out);
        assertEquals(0, result);
        assertEquals("", out.toString());
    }

    @Test
    public void testLookupWithOffset() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "two", "2" },
            { "three", "3" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int result = lt.translate("onetwothree", 3, out);
        assertEquals(3, result);
        assertEquals("2", out.toString());
    }

    @Test
    public void testLookupWithVaryingLengths() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "a", "A" },
            { "ab", "AB" },
            { "abc", "ABC" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);

        // Should match the longest prefix possible
        StringWriter out = new StringWriter();
        int result = lt.translate("abcdef", 0, out);
        assertEquals(3, result);
        assertEquals("ABC", out.toString());

        out = new StringWriter();
        result = lt.translate("ab", 0, out);
        assertEquals(2, result);
        assertEquals("AB", out.toString());

        out = new StringWriter();
        result = lt.translate("a", 0, out);
        assertEquals(1, result);
        assertEquals("A", out.toString());
    }

    @Test
    public void testLookupAtEndOfInput() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "test", "TEST" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        // Index + longest > input.length()
        final int result = lt.translate("abc", 2, out);
        assertEquals(0, result);
        assertEquals("", out.toString());
    }

    @Test
    public void testNullLookupArray() throws IOException {
        final LookupTranslator lt = new LookupTranslator((CharSequence[][]) null);
        final StringWriter out = new StringWriter();
        final int result = lt.translate("test", 0, out);
        assertEquals(0, result);
        assertEquals("", out.toString());
    }

    @Test
    public void testEmptyLookupArray() throws IOException {
        final LookupTranslator lt = new LookupTranslator(new CharSequence[0][0]);
        final StringWriter out = new StringWriter();
        final int result = lt.translate("test", 0, out);
        assertEquals(0, result);
        assertEquals("", out.toString());
    }

    @Test
    public void testTranslateCharSequenceObject() {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "foo", "bar" },
            { "baz", "qux" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final String result = lt.translate("foo and baz");
        assertEquals("bar and qux", result);
    }

    @Test
    public void testLookupWithNonStringCharSequence() throws IOException {
        final StringBuilder key = new StringBuilder("key");
        final StringBuffer value = new StringBuffer("val");
        final CharSequence[][] lookup = new CharSequence[][] {
            { key, value }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int result = lt.translate(new StringBuilder("key_test"), 0, out);
        assertEquals(3, result);
        assertEquals("val", out.toString());
    }

    @Test
    public void testTranslateNullOrEmptyInput() {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "a", "b" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        assertEquals(null, lt.translate(null));
        assertEquals("", lt.translate(""));
    }

    @Test
    public void testShortestKeyGreaterThanRemainingInput() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "longerKey", "replacement" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        // Remaining length is shorter than shortest key
        final int result = lt.translate("short", 2, out);
        assertEquals(0, result);
        assertEquals("", out.toString());
    }

    @Test
    public void testMultipleKeysWithSameLength() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "cat", "feline" },
            { "dog", "canine" }
        };
        final LookupTranslator lt = new LookupTranslator(lookup);

        StringWriter out = new StringWriter();
        int result = lt.translate("cat", 0, out);
        assertEquals(3, result);
        assertEquals("feline", out.toString());

        out = new StringWriter();
        result = lt.translate("dog", 0, out);
        assertEquals(3, result);
        assertEquals("canine", out.toString());
    }

    @Test
    public void testConstructorNonNullCreation() {
        final LookupTranslator lt = new LookupTranslator(new CharSequence[][] { { "key", "value" } });
        assertNotNull(lt);
    }
}