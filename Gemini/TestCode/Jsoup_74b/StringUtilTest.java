package org.jsoup.helper;

import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

public class StringUtilTest {

    @Test
    public void testJoinCollection() {
        Collection<String> empty = Collections.emptyList();
        assertEquals("", StringUtil.join(empty, ","));

        Collection<String> single = Collections.singletonList("a");
        assertEquals("a", StringUtil.join(single, ","));

        Collection<String> multiple = Arrays.asList("a", "b", "c");
        assertEquals("a,b,c", StringUtil.join(multiple, ","));
    }

    @Test
    public void testJoinIterator() {
        Iterator<String> empty = Collections.<String>emptyList().iterator();
        assertEquals("", StringUtil.join(empty, ","));

        Iterator<String> single = Collections.singletonList("a").iterator();
        assertEquals("a", StringUtil.join(single, ","));

        Iterator<String> multiple = Arrays.asList("a", "b", "c").iterator();
        assertEquals("a,b,c", StringUtil.join(multiple, ","));
    }

    @Test
    public void testJoinArray() {
        assertEquals("", StringUtil.join(new String[]{}, ","));
        assertEquals("a", StringUtil.join(new String[]{"a"}, ","));
        assertEquals("a,b,c", StringUtil.join(new String[]{"a", "b", "c"}, ","));
        
        // Test primitive or Object array overload if applicable
        assertEquals("1,2,3", StringUtil.join(new Integer[]{1, 2, 3}, ","));
    }

    @Test
    public void testPadding() {
        assertEquals("", StringUtil.padding(0));
        assertEquals(" ", StringUtil.padding(1));
        assertEquals("     ", StringUtil.padding(5));
        
        try {
            StringUtil.padding(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank("   "));
        assertTrue(StringUtil.isBlank("\t\n\r"));
        assertFalse(StringUtil.isBlank("a"));
        assertFalse(StringUtil.isBlank(" a "));
    }

    @Test
    public void testIsNumeric() {
        assertFalse(StringUtil.isNumeric(null));
        assertFalse(StringUtil.isNumeric(""));
        assertFalse(StringUtil.isNumeric("   "));
        assertFalse(StringUtil.isNumeric("12a34"));
        assertFalse(StringUtil.isNumeric("12.34"));
        assertTrue(StringUtil.isNumeric("123456"));
    }

    @Test
    public void testIsWhitespace() {
        assertFalse(StringUtil.isWhitespace(null));
        assertTrue(StringUtil.isWhitespace(""));
        assertTrue(StringUtil.isWhitespace("   "));
        assertTrue(StringUtil.isWhitespace("\t\n\r"));
        assertFalse(StringUtil.isWhitespace("a"));
        assertFalse(StringUtil.isWhitespace(" a "));
    }

    @Test
    public void testNormaliseWhitespace() {
        assertNull(StringUtil.normaliseWhitespace(null));
        assertEquals("", StringUtil.normaliseWhitespace(""));
        assertEquals("a b c", StringUtil.normaliseWhitespace("  a   b \n c  "));
        assertEquals("abc", StringUtil.normaliseWhitespace("abc"));
    }

    @Test
    public void testResolveUrl() throws MalformedURLException {
        URL base = new URL("http://example.com/path/file.html");
        
        // Test absolute URL resolution
        assertEquals("http://other.com/index.html", StringUtil.resolve(base, "http://other.com/index.html"));
        
        // Test relative URL resolution
        assertEquals("http://example.com/path/other.html", StringUtil.resolve(base, "other.html"));
        assertEquals("http://example.com/other.html", StringUtil.resolve(base, "/other.html"));
        
        // Test invalid base/rel edge cases
        assertEquals("", StringUtil.resolve(base, ""));
        
        URL nullBase = null;
        assertEquals("http://example.com", StringUtil.resolve(nullBase, "http://example.com"));
        
        // Malformed cases returning empty string or handling exceptions gracefully
        assertEquals("", StringUtil.resolve(base, ":invalid"));
    }

    @Test
    public void testAppendableToStringBuilder() {
        // Specifically targeting the resolve or string builder helpers if needed
        StringBuilder sb = new StringBuilder();
        StringUtil.appendUrl(new URL("http://example.com"), sb);
        assertTrue(sb.length() > 0);
    }
}