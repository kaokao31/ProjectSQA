package org.apache.commons.lang.text;

import org.junit.Test;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

public class StrBuilderTest {

    @Test
    public void testConstructors() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.length());
        assertEquals(32, sb.capacity());
        assertTrue(sb.isEmpty());

        sb = new StrBuilder(10);
        assertEquals(0, sb.length());
        assertEquals(10, sb.capacity());

        sb = new StrBuilder(-5);
        assertEquals(0, sb.length());
        assertEquals(32, sb.capacity());

        sb = new StrBuilder("hello");
        assertEquals(5, sb.length());
        assertEquals("hello", sb.toString());

        sb = new StrBuilder((String) null);
        assertEquals(0, sb.length());
    }

    @Test
    public void testLang299AppendFixedWidthPadRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("foo", 1, ' ');
        assertEquals("f", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 2, ' ');
        assertEquals("fo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 3, ' ');
        assertEquals("foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("foo", 4, ' ');
        assertEquals("foo ", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(null, 3, 'x');
        assertEquals("xxx", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("foo", 1, ' ');
        assertEquals("o", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 2, ' ');
        assertEquals("oo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 3, ' ');
        assertEquals("foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", 4, ' ');
        assertEquals(" foo", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(null, 3, 'x');
        assertEquals("xxx", sb.toString());
    }

    @Test
    public void testAppendPrimitives() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(false);
        assertEquals("truefalse", sb.toString());

        sb.clear();
        sb.append('a').append(123).append(456L).append(7.8f).append(9.1d);
        assertEquals("a1234567.89.1", sb.toString());
    }

    @Test
    public void testAppendObjectsAndStrings() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) null);
        assertEquals("", sb.toString());

        sb.setNullText("<null>");
        sb.append((Object) null);
        assertEquals("<null>", sb.toString());

        sb.clear();
        sb.append((String) null);
        assertEquals("<null>", sb.toString());

        sb.setNullText(null);
        sb.clear();
        sb.append(new StringBuffer("buf"));
        sb.append(new StrBuilder("builder"));
        assertEquals("bufbuilder", sb.toString());

        sb.clear();
        sb.append("abcdef", 1, 3);
        assertEquals("bcd", sb.toString());

        sb.clear();
        sb.append(new StringBuffer("abcdef"), 1, 3);
        assertEquals("bcd", sb.toString());

        sb.clear();
        sb.append(new StrBuilder("abcdef"), 1, 3);
        assertEquals("bcd", sb.toString());
    }

    @Test
    public void testAppendCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append((char[]) null);
        assertEquals(0, sb.length());

        char[] chars = new char[]{'a', 'b', 'c', 'd'};
        sb.append(chars);
        assertEquals("abcd", sb.toString());

        sb.clear();
        sb.append(chars, 1, 2);
        assertEquals("bc", sb.toString());

        try {
            sb.append(chars, -1, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }

        try {
            sb.append(chars, 1, 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void testAppendWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("x", "y", "z"), "-");
        assertEquals("x-y-z", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Collection<?>) null, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Iterator<?>) null, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Collections.singletonList("only").iterator(), ",");
        assertEquals("only", sb.toString());
    }

    @Test
    public void testAppendSeparator() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        assertEquals("", sb.toString());

        sb.append("a");
        sb.appendSeparator(",");
        sb.append("b");
        assertEquals("a,b", sb.toString());

        sb.appendSeparator(',', "default");
        assertEquals("a,b,", sb.toString());

        sb.clear();
        sb.appendSeparator(',', "default");
        assertEquals("default", sb.toString());

        sb.clear();
        sb.appendSeparator((String) null);
        assertEquals("", sb.toString());

        sb.append("a");
        sb.appendSeparator((String) null);
        assertEquals("a", sb.toString());
    }

    @Test
    public void testAppendPaddingAndNewLine() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(3, 'x');
        assertEquals("xxx", sb.toString());

        sb.appendPadding(-1, 'y');
        assertEquals("xxx", sb.toString());

        sb.clear();
        sb.setNewLineText("\n");
        sb.appendNewLine();
        assertEquals("\n", sb.toString());

        sb.clear();
        sb.appendln("line1");
        assertEquals("line1\n", sb.toString());

        sb.clear();
        sb.appendln(123);
        assertEquals("123\n", sb.toString());
    }

    @Test
    public void testInsertMethods() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());

        sb.insert(0, (String) null);
        assertEquals("abc", sb.toString());

        sb.setNullText("<null>");
        sb.insert(0, (String) null);
        assertEquals("<null>abc", sb.toString());

        sb.clear();
        sb.append("hello");
        sb.insert(0, true);
        assertEquals("truehello", sb.toString());

        sb.insert(4, 99);
        assertEquals("true99hello", sb.toString());

        sb.insert(0, new char[]{'1', '2'});
        assertEquals("12true99hello", sb.toString());

        sb.insert(0, new char[]{'x', 'y', 'z'}, 1, 1);
        assertEquals("y12true99hello", sb.toString());

        try {
            sb.insert(-1, "err");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }

        try {
            sb.insert(1000, "err");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void testDeleteAndClear() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.delete(5, 11);
        assertEquals("hello", sb.toString());

        sb.deleteCharAt(0);
        assertEquals("ello", sb.toString());

        try {
            sb.deleteCharAt(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }

        try {
            sb.deleteCharAt(10);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }

        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test
    public void testDeleteAllFirstMatches() {
        StrBuilder sb = new StrBuilder("abracadabra");
        sb.deleteAll('a');
        assertEquals("brcdbr", sb.toString());

        sb = new StrBuilder("abracadabra");
        sb.deleteFirst('a');
        assertEquals("bracadabra", sb.toString());

        sb = new StrBuilder("hello hello world");
        sb.deleteAll("hello");
        assertEquals("  world", sb.toString());

        sb = new StrBuilder("hello hello world");
        sb.deleteFirst("hello");
        assertEquals(" hello world", sb.toString());

        sb = new StrBuilder("abc 123 def");
        sb.deleteAll(StrMatcher.stringMatcher("123"));
        assertEquals("abc  def", sb.toString());

        sb = new StrBuilder("a b c");
        sb.deleteFirst(StrMatcher.spaceMatcher());
        assertEquals("ab c", sb.toString());
    }

    @Test
    public void testReplaceMethods() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.replace(0, 5, "goodbye");
        assertEquals("goodbye world", sb.toString());

        sb = new StrBuilder("banana");
        sb.replaceAll('a', 'o');
        assertEquals("bonono", sb.toString());

        sb = new StrBuilder("banana");
        sb.replaceFirst('a', 'o');
        assertEquals("bonana", sb.toString());

        sb = new StrBuilder("foo bar foo");
        sb.replaceAll("foo", "baz");
        assertEquals("baz bar baz", sb.toString());

        sb = new StrBuilder("foo bar foo");
        sb.replaceFirst("foo", "baz");
        assertEquals("baz bar foo", sb.toString());

        sb = new StrBuilder("foo bar foo");
        sb.replace(StrMatcher.stringMatcher("foo"), "qux", 0, sb.length(), -1);
        assertEquals("qux bar qux", sb.toString());

        sb = new StrBuilder("foo bar foo");
        sb.replace(StrMatcher.stringMatcher("foo"), "qux", 0, sb.length(), 1);
        assertEquals("qux bar foo", sb.toString());
    }

    @Test
    public void testSubstringsAndSlices() {
        StrBuilder sb = new StrBuilder("hello world");
        assertEquals("hello", sb.substring(0, 5));
        assertEquals("world", sb.substring(6));
        assertEquals("hello", sb.leftString(5));
        assertEquals("world", sb.rightString(5));
        assertEquals("lo wo", sb.midString(3, 5));
        assertEquals("hello world", sb.midString(-1, 20));
        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.rightString(-1));
        assertEquals("hello world", sb.leftString(100));
        assertEquals("hello world", sb.rightString(100));

        CharSequence cs = sb.subSequence(0, 5);
        assertEquals("hello", cs.toString());
    }

    @Test
    public void testSearching() {
        StrBuilder sb = new StrBuilder("the quick brown fox jumps over the lazy dog");
        assertTrue(sb.contains('e'));
        assertFalse(sb.contains('z') == false);
        assertFalse(sb.contains('1'));

        assertTrue(sb.contains("quick"));
        assertFalse(sb.contains("cat"));
        assertFalse(sb.contains((String) null));

        assertTrue(sb.contains(StrMatcher.stringMatcher("fox")));

        assertEquals(2, sb.indexOf('e'));
        assertEquals(33, sb.lastIndexOf('e'));

        assertEquals(4, sb.indexOf("quick"));
        assertEquals(31, sb.indexOf("the", 10));
        assertEquals(-1, sb.indexOf("notfound"));

        assertEquals(31, sb.lastIndexOf("the"));
        assertEquals(0, sb.lastIndexOf("the", 10));
        assertEquals(-1, sb.lastIndexOf("notfound"));

        assertEquals(4, sb.indexOf(StrMatcher.stringMatcher("quick")));
        assertEquals(31, sb.lastIndexOf(StrMatcher.stringMatcher("the")));
    }

    @Test
    public void testTrimmingAndReversing() {
        StrBuilder sb = new StrBuilder("   trim me   ");
        sb.trim();
        assertEquals("trim me", sb.toString());

        sb = new StrBuilder("   ");
        sb.trim();
        assertEquals("", sb.toString());

        sb = new StrBuilder("abcde");
        sb.reverse();
        assertEquals("edcba", sb.toString());

        sb = new StrBuilder("");
        sb.reverse();
        assertEquals("", sb.toString());
    }

    @Test
    public void testCapacityAndLength() {
        StrBuilder sb = new StrBuilder(10);
        assertEquals(10, sb.capacity());
        sb.ensureCapacity(50);
        assertTrue(sb.capacity() >= 50);

        sb.append("hello");
        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());

        sb.setLength(3);
        assertEquals(3, sb.length());
        assertEquals("hel", sb.toString());

        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals("hel\0\0", sb.toString());

        try {
            sb.setLength(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void testGetAndSetCharAt() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals('h', sb.charAt(0));
        assertEquals('o', sb.charAt(4));

        sb.setCharAt(0, 'y');
        assertEquals("yello", sb.toString());

        try {
            sb.charAt(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }

        try {
            sb.charAt(5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }

        try {
            sb.setCharAt(-1, 'a');
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }

        try {
            sb.setCharAt(5, 'a');
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Test
    public void testGetChars() {
        StrBuilder sb = new StrBuilder("hello world");
        char[] target = new char[5];
        sb.getChars(0, 5, target, 0);
        assertArrayEquals(new char[]{'h', 'e', 'l', 'l', 'o'}, target);

        char[] allChars = sb.toCharArray();
        assertEquals("hello world", new String(allChars));

        char[] subChars = sb.toCharArray(6, 11);
        assertEquals("world", new String(subChars));
    }

    @Test
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("hello");
        StrBuilder sb2 = new StrBuilder("hello");
        StrBuilder sb3 = new StrBuilder("world");

        assertEquals(sb1, sb2);
        assertNotEquals(sb1, sb3);
        assertNotEquals(sb1, "hello");
        assertNotEquals(sb1, null);
        assertEquals(sb1.hashCode(), sb2.hashCode());

        assertTrue(sb1.equalsIgnoreCase(sb2));
        assertTrue(sb1.equalsIgnoreCase(new StrBuilder("HELLO")));
        assertFalse(sb1.equalsIgnoreCase(sb3));
        assertFalse(sb1.equalsIgnoreCase(null));
    }

    @Test
    public void testStartsWithEndsWith() {
        StrBuilder sb = new StrBuilder("hello world");
        assertTrue(sb.startsWith("hello"));
        assertTrue(sb.startsWith(""));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith(null));

        assertTrue(sb.endsWith("world"));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith(null));
    }

    @Test
    public void testAsReaderAndWriter() throws Exception {
        StrBuilder sb = new StrBuilder("hello world");
        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        char[] buf = new char[5];
        int read = reader.read(buf);
        assertEquals(5, read);
        assertEquals("hello", new String(buf));

        Writer writer = sb.asWriter();
        writer.write(" foo");
        assertEquals("hello world foo", sb.toString());
    }

    @Test
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tok = sb.asTokenizer();
        assertArrayEquals(new String[]{"a", "b", "c"}, tok.getTokenArray());
    }
}