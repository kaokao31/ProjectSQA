package org.apache.commons.lang3;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class StringUtilsTest {

    private static final String[] ARRAY_EMPTY = new String[0];
    private static final String[] ARRAY_NULL = null;
    private static final String[] ARRAY_NULL_ELEMENT = new String[] { null };
    private static final String[] ARRAY_SINGLE = new String[] { "foo" };
    private static final String[] ARRAY_MULTIPLE = new String[] { "foo", "bar", "baz" };
    private static final String[] ARRAY_WITH_NULLS = new String[] { "foo", null, "bar" };

    @Test
    public void testJoin_Objectarray() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(ARRAY_EMPTY));
        assertEquals("", StringUtils.join(ARRAY_NULL_ELEMENT));
        assertEquals("foo", StringUtils.join(ARRAY_SINGLE));
        assertEquals("foobarbaz", StringUtils.join(ARRAY_MULTIPLE));
        assertEquals("foobar", StringUtils.join(ARRAY_WITH_NULLS));
        assertEquals("foobar", StringUtils.join(new Object[] { "foo", "", "bar" }));
    }

    @Test
    public void testJoin_ArrayChar() {
        assertNull(StringUtils.join((Object[]) null, ','));
        assertEquals("", StringUtils.join(ARRAY_EMPTY, ','));
        assertEquals("", StringUtils.join(ARRAY_NULL_ELEMENT, ','));
        assertEquals("foo", StringUtils.join(ARRAY_SINGLE, ','));
        assertEquals("foo,bar,baz", StringUtils.join(ARRAY_MULTIPLE, ','));
        assertEquals("foo,,bar", StringUtils.join(ARRAY_WITH_NULLS, ','));

        assertEquals("foo,bar", StringUtils.join(ARRAY_MULTIPLE, ',', 0, 2));
        assertEquals("bar,baz", StringUtils.join(ARRAY_MULTIPLE, ',', 1, 3));
        assertEquals("", StringUtils.join(ARRAY_MULTIPLE, ',', 2, 1));
        assertEquals("", StringUtils.join(ARRAY_MULTIPLE, ',', 2, 2));
        assertNull(StringUtils.join((Object[]) null, ',', 0, 1));
        assertEquals("", StringUtils.join(ARRAY_EMPTY, ',', 0, 0));
    }

    @Test
    public void testJoin_ArrayString() {
        assertNull(StringUtils.join((Object[]) null, ","));
        assertEquals("", StringUtils.join(ARRAY_EMPTY, ","));
        assertEquals("", StringUtils.join(ARRAY_NULL_ELEMENT, ","));
        assertEquals("foo", StringUtils.join(ARRAY_SINGLE, ","));
        assertEquals("foo,bar,baz", StringUtils.join(ARRAY_MULTIPLE, ","));
        assertEquals("foo,,bar", StringUtils.join(ARRAY_WITH_NULLS, ","));

        // Tests with null separator (triggers Lang-20 bug)
        assertEquals("foobarbaz", StringUtils.join(ARRAY_MULTIPLE, (String) null));
        assertEquals("foo", StringUtils.join(ARRAY_SINGLE, (String) null));
        assertEquals("", StringUtils.join(ARRAY_EMPTY, (String) null));
        assertEquals("", StringUtils.join(ARRAY_NULL_ELEMENT, (String) null));
        assertEquals("foobar", StringUtils.join(ARRAY_WITH_NULLS, (String) null));

        // Sub-array joins with null separator
        assertEquals("foobar", StringUtils.join(ARRAY_MULTIPLE, (String) null, 0, 2));
        assertEquals("barbaz", StringUtils.join(ARRAY_MULTIPLE, (String) null, 1, 3));
        assertEquals("", StringUtils.join(ARRAY_MULTIPLE, (String) null, 2, 1));
        assertEquals("", StringUtils.join(ARRAY_MULTIPLE, (String) null, 2, 2));
        assertNull(StringUtils.join((Object[]) null, (String) null, 0, 1));

        // Sub-array joins with non-null separator
        assertEquals("foo,bar", StringUtils.join(ARRAY_MULTIPLE, ",", 0, 2));
        assertEquals("bar,baz", StringUtils.join(ARRAY_MULTIPLE, ",", 1, 3));
        assertEquals("", StringUtils.join(ARRAY_MULTIPLE, ",", 2, 1));
        assertEquals("", StringUtils.join(ARRAY_MULTIPLE, ",", 2, 2));
        assertNull(StringUtils.join((Object[]) null, ",", 0, 1));
        assertEquals("", StringUtils.join(ARRAY_EMPTY, ",", 0, 0));

        // Sub-array joins where first element is null
        assertEquals(",bar", StringUtils.join(new Object[] { null, "bar" }, ",", 0, 2));
        assertEquals("bar", StringUtils.join(new Object[] { null, "bar" }, (String) null, 0, 2));
    }

    @Test
    public void testJoin_Iterator() {
        assertNull(StringUtils.join((Iterator<?>) null, ','));
        assertNull(StringUtils.join((Iterator<?>) null, ","));

        assertEquals("", StringUtils.join(Collections.emptyIterator(), ','));
        assertEquals("", StringUtils.join(Collections.emptyIterator(), ","));

        assertEquals("foo", StringUtils.join(Collections.singletonList("foo").iterator(), ','));
        assertEquals("foo", StringUtils.join(Collections.singletonList("foo").iterator(), ","));
        assertEquals("foo", StringUtils.join(Collections.singletonList("foo").iterator(), (String) null));

        assertEquals("foo,bar,baz", StringUtils.join(Arrays.asList("foo", "bar", "baz").iterator(), ','));
        assertEquals("foo,bar,baz", StringUtils.join(Arrays.asList("foo", "bar", "baz").iterator(), ","));
        assertEquals("foobarbaz", StringUtils.join(Arrays.asList("foo", "bar", "baz").iterator(), (String) null));

        assertEquals("foo,,bar", StringUtils.join(Arrays.asList("foo", null, "bar").iterator(), ','));
        assertEquals("foo,,bar", StringUtils.join(Arrays.asList("foo", null, "bar").iterator(), ","));
        assertEquals("foobar", StringUtils.join(Arrays.asList("foo", null, "bar").iterator(), (String) null));
    }

    @Test
    public void testJoin_Iterable() {
        assertNull(StringUtils.join((Iterable<?>) null, ','));
        assertNull(StringUtils.join((Iterable<?>) null, ","));

        assertEquals("", StringUtils.join(Collections.emptyList(), ','));
        assertEquals("", StringUtils.join(Collections.emptyList(), ","));

        assertEquals("foo", StringUtils.join(Collections.singletonList("foo"), ','));
        assertEquals("foo", StringUtils.join(Collections.singletonList("foo"), ","));
        assertEquals("foo", StringUtils.join(Collections.singletonList("foo"), (String) null));

        List<String> list = Arrays.asList("foo", "bar", "baz");
        assertEquals("foo,bar,baz", StringUtils.join(list, ','));
        assertEquals("foo,bar,baz", StringUtils.join(list, ","));
        assertEquals("foobarbaz", StringUtils.join(list, (String) null));
    }

    @Test
    public void testIsEmptyAndIsNotEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("foo"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("foo"));
    }

    @Test
    public void testIsBlankAndIsNotBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank("\t\r\n"));
        assertFalse(StringUtils.isBlank("foo"));
        assertFalse(StringUtils.isBlank("  foo  "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" "));
        assertFalse(StringUtils.isNotBlank("\t\r\n"));
        assertTrue(StringUtils.isNotBlank("foo"));
        assertTrue(StringUtils.isNotBlank("  foo  "));
    }

    @Test
    public void testTrimAndStrip() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("   "));
        assertEquals("foo", StringUtils.trim("  foo  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("foo", StringUtils.trimToNull("  foo  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("foo", StringUtils.trimToEmpty("  foo  "));
    }

    @Test
    public void testEqualsAndEqualsIgnoreCase() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
    }

    @Test
    public void testSubstringAndLeftRightMid() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("c", StringUtils.substring("abc", -1));
        assertEquals("bc", StringUtils.substring("abc", -2));

        assertEquals("ab", StringUtils.left("abcde", 2));
        assertNull(StringUtils.left(null, 2));
        assertEquals("", StringUtils.left("abcde", -1));
        assertEquals("abcde", StringUtils.left("abcde", 10));

        assertEquals("de", StringUtils.right("abcde", 2));
        assertNull(StringUtils.right(null, 2));
        assertEquals("", StringUtils.right("abcde", -1));
        assertEquals("abcde", StringUtils.right("abcde", 10));

        assertEquals("bcd", StringUtils.mid("abcde", 1, 3));
        assertNull(StringUtils.mid(null, 1, 3));
        assertEquals("", StringUtils.mid("abcde", 1, -1));
        assertEquals("abcde", StringUtils.mid("abcde", -1, 10));
    }

    @Test
    public void testReplace() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("aba", StringUtils.replace("aba", null, "b"));
        assertEquals("aba", StringUtils.replace("aba", "a", null));
        assertEquals("b", StringUtils.replace("aba", "a", ""));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replace("aba", "a", "z", 1));
    }

    @Test
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtils.split("a b c"));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtils.split("a  b  c"));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtils.split("a:b:c", ':'));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtils.split("a,b,c", ","));
    }

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("foo", StringUtils.defaultString("foo"));
        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("foo", StringUtils.defaultString("foo", "default"));
    }
}