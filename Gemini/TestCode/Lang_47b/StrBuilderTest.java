package org.apache.commons.lang.text;

import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class StrBuilderTest {

    @Test
    public void testLang412Left() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 10, '*');
        assertEquals("**********", sb.toString());
    }

    @Test
    public void testLang412Right() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 10, '*');
        assertEquals("**********", sb.toString());
    }

    @Test
    public void testConstructors() {
        StrBuilder sb1 = new StrBuilder();
        assertEquals(32, sb1.capacity());
        assertEquals(0, sb1.length());
        assertEquals(0, sb1.size());
        assertTrue(sb1.isEmpty());

        StrBuilder sb2 = new StrBuilder(50);
        assertEquals(50, sb2.capacity());
        assertEquals(0, sb2.length());

        StrBuilder sb3 = new StrBuilder("Hello World");
        assertEquals("Hello World", sb3.toString());
        assertEquals(11, sb3.length());

        StrBuilder sb4 = new StrBuilder((String) null);
        assertEquals(32, sb4.capacity());
        assertEquals(0, sb4.length());
    }

    @Test
    public void testAppendFixedWidthPadLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abc", 5, ' ');
        assertEquals("  abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 3, ' ');
        assertEquals("def", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 0, ' ');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", -1, ' ');
        assertEquals("", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadLeft(null, 6, '-');
        assertEquals("--null", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abc", 5, ' ');
        assertEquals("abc  ", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 3, ' ');
        assertEquals("abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 0, ' ');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", -1, ' ');
        assertEquals("", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadRight(null, 6, '-');
        assertEquals("null--", sb.toString());
    }

    @Test
    public void testAppendAllAndSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll(new Object[]{"a", "b", "c"});
        assertEquals("abc", sb.toString());

        sb.clear();
        sb.appendAll((Object[]) null);
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("x", "y", "z"), "-");
        assertEquals("x-y-z", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("x", "y", "z").iterator(), "/");
        assertEquals("x/y/z", sb.toString());

        sb.clear();
        sb.appendSeparator(",", "default");
        assertEquals("default", sb.toString());
        sb.appendSeparator(",", "default");
        assertEquals("default,", sb.toString());

        sb.clear();
        sb.appendSeparator(',');
        assertEquals("", sb.toString());
        sb.append("test");
        sb.appendSeparator(',');
        assertEquals("test,", sb.toString());

        sb.appendSeparator(',', 1);
        assertEquals("test,,", sb.toString());
    }

    @Test
    public void testAppendPrimitives() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(false)
          .append('c')
          .append(new char[]{'h', 'a', 'r'})
          .append(new char[]{'x', 'y', 'z'}, 1, 1)
          .append(12)
          .append(34L)
          .append(5.6f)
          .append(7.8d)
          .append(new StringBuffer("buf"))
          .append(new StrBuilder("sbu"));
        assertEquals("truefalsechar12345.67.8bufsbu", sb.toString());
    }

    @Test
    public void testAppendNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());
        sb.append((String) null);
        sb.append((Object) null);
        sb.append((StringBuffer) null);
        sb.append((StrBuilder) null);
        sb.append((char[]) null);
        assertEquals("NULLNULLNULLNULLNULL", sb.toString());
    }

    @Test
    public void testAppendPadding() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(5, '0');
        assertEquals("00000", sb.toString());
        sb.appendPadding(-2, 'x');
        assertEquals("00000", sb.toString());
    }

    @Test
    public void testInsert() {
        StrBuilder sb = new StrBuilder("012345");
        sb.insert(0, "prefix-");
        assertEquals("prefix-012345", sb.toString());
        sb.insert(sb.length(), "-suffix");
        assertEquals("prefix-012345-suffix", sb.toString());
        sb.insert(7, true);
        assertEquals("prefix-true012345-suffix", sb.toString());
        sb.insert(0, 'X');
        assertEquals("Xprefix-true012345-suffix", sb.toString());
        sb.insert(0, 100);
        assertEquals("100Xprefix-true012345-suffix", sb.toString());
        sb.insert(0, 200L);
        assertEquals("200100Xprefix-true012345-suffix", sb.toString());
        sb.insert(0, 1.5f);
        assertEquals("1.5200100Xprefix-true012345-suffix", sb.toString());
        sb.insert(0, 2.5d);
        assertEquals("2.51.5200100Xprefix-true012345-suffix", sb.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertIndexOutOfBoundsNegative() {
        new StrBuilder().insert(-1, "test");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertIndexOutOfBoundsPositive() {
        new StrBuilder().insert(1, "test");
    }

    @Test
    public void testDeleteAndClear() {
        StrBuilder sb = new StrBuilder("Hello World!");
        sb.delete(5, 11);
        assertEquals("Hello!", sb.toString());

        sb.deleteCharAt(5);
        assertEquals("Hello", sb.toString());

        sb.deleteAll('l');
        assertEquals("Heo", sb.toString());

        sb.append("lo World");
        sb.deleteFirst('o');
        assertEquals("Hel World", sb.toString());

        sb.deleteAll("l");
        assertEquals("He Word", sb.toString());

        sb.deleteFirst("Word");
        assertEquals("He ", sb.toString());

        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test
    public void testReplace() {
        StrBuilder sb = new StrBuilder("one two three two one");
        sb.replace(0, 3, "1");
        assertEquals("1 two three two one", sb.toString());

        sb.replaceAll("two", "2");
        assertEquals("1 2 three 2 one", sb.toString());

        sb.replaceFirst("2", "two");
        assertEquals("1 two three 2 one", sb.toString());

        sb.replaceAll('e', 'E');
        assertEquals("1 two thrEE 2 onE", sb.toString());

        sb.replaceFirst('o', 'O');
        assertEquals("1 twO thrEE 2 onE", sb.toString());

        StrMatcher matcher = StrMatcher.stringMatcher("thrEE");
        sb.replace(matcher, "3", 0, sb.length(), -1);
        assertEquals("1 twO 3 2 onE", sb.toString());
    }

    @Test
    public void testSubstringAndLeftRightMid() {
        StrBuilder sb = new StrBuilder("hello world");
        assertEquals("hello", sb.substring(0, 5));
        assertEquals("world", sb.substring(6));
        assertEquals("hello", sb.leftString(5));
        assertEquals("world", sb.rightString(5));
        assertEquals("world", sb.midString(6, 5));
        assertEquals("hello world", sb.midString(0, 100));
        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.rightString(-1));
        assertEquals("", sb.midString(-1, 5));
    }

    @Test
    public void testSearchMethods() {
        StrBuilder sb = new StrBuilder("the quick brown fox jumps over the lazy dog");
        assertEquals(4, sb.indexOf('q'));
        assertEquals(4, sb.indexOf('q', 0));
        assertEquals(-1, sb.indexOf('q', 5));
        assertEquals(31, sb.lastIndexOf('t'));
        assertEquals(31, sb.lastIndexOf('t', 40));

        assertEquals(0, sb.indexOf("the"));
        assertEquals(31, sb.indexOf("the", 1));
        assertEquals(31, sb.lastIndexOf("the"));
        assertEquals(0, sb.lastIndexOf("the", 30));

        assertTrue(sb.contains('q'));
        assertFalse(sb.contains('z') == false);
        assertTrue(sb.contains("fox"));
        assertFalse(sb.contains("cat"));

        StrMatcher matcher = StrMatcher.stringMatcher("fox");
        assertEquals(16, sb.indexOf(matcher));
        assertEquals(16, sb.lastIndexOf(matcher));
        assertTrue(sb.contains(matcher));
    }

    @Test
    public void testCharAtAndSetCharAt() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals('a', sb.charAt(0));
        assertEquals('f', sb.charAt(5));

        sb.setCharAt(0, 'z');
        assertEquals('z', sb.charAt(0));

        char[] chars = new char[3];
        sb.getChars(0, 3, chars, 0);
        assertArrayEquals(new char[]{'z', 'b', 'c'}, chars);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAtOutOfBounds() {
        new StrBuilder("test").charAt(10);
    }

    @Test
    public void testTrim() {
        StrBuilder sb = new StrBuilder("   \t  hello \r\n  ");
        sb.trim();
        assertEquals("hello", sb.toString());

        sb = new StrBuilder("   ");
        sb.trim();
        assertEquals("", sb.toString());
    }

    @Test
    public void testReverse() {
        StrBuilder sb = new StrBuilder("0123456789");
        sb.reverse();
        assertEquals("9876543210", sb.toString());
    }

    @Test
    public void testSetLengthAndCapacity() {
        StrBuilder sb = new StrBuilder(10);
        sb.append("hello");
        sb.ensureCapacity(100);
        assertTrue(sb.capacity() >= 100);

        sb.minimizeCapacity();
        assertEquals(sb.length(), sb.capacity());

        sb.setLength(2);
        assertEquals("he", sb.toString());

        sb.setLength(5);
        assertEquals(5, sb.length());
    }

    @Test
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        StrBuilder sb3 = new StrBuilder("xyz");

        assertTrue(sb1.equals(sb2));
        assertTrue(sb1.equals((Object) sb2));
        assertTrue(sb1.equalsIgnoreCase(new StrBuilder("ABC")));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals("abc"));
        assertEquals(sb1.hashCode(), sb2.hashCode());
    }

    @Test
    public void testAsReaderAndWriter() throws Exception {
        StrBuilder sb = new StrBuilder("read me");
        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertEquals('r', (char) reader.read());
        char[] buf = new char[6];
        assertEquals(6, reader.read(buf));
        assertEquals("ead me", new String(buf));
        assertEquals(-1, reader.read());

        Writer writer = sb.asWriter();
        writer.write(" more");
        assertEquals("read me more", sb.toString());
    }

    @Test
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tok = sb.asTokenizer();
        assertArrayEquals(new String[]{"a", "b", "c"}, tok.getTokenArray());
    }

    @Test
    public void testNewLine() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\r\n");
        assertEquals("\r\n", sb.getNewLineText());
        sb.appendln("test");
        assertEquals("test\r\n", sb.toString());
    }
}