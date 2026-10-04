package org.apache.commons.lang.text;

import org.junit.Test;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StrBuilderTest {

    @Test
    public void testLang294() {
        StrBuilder sb = new StrBuilder("MIndexOutOfBoundsException");
        sb.ensureCapacity(100);
        sb.deleteAll(StrMatcher.stringMatcher("IndexOutOfBoundsException"));
        assertEquals("M", sb.toString());
    }

    @Test
    public void testIndexOfLang294() {
        StrBuilder sb = new StrBuilder("MIndexOutOfBoundsException");
        sb.ensureCapacity(100);
        assertEquals(-1, sb.indexOf(StrMatcher.stringMatcher("IndexOutOfBoundsException"), 2));
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

        StrBuilder sb3 = new StrBuilder(-5);
        assertEquals(32, sb3.capacity());

        StrBuilder sb4 = new StrBuilder("Hello");
        assertEquals("Hello", sb4.toString());
        assertEquals(5, sb4.length());

        StrBuilder sb5 = new StrBuilder((String) null);
        assertEquals(32, sb5.capacity());
        assertEquals(0, sb5.length());
    }

    @Test
    public void testCapacityAndLength() {
        StrBuilder sb = new StrBuilder();
        assertEquals(32, sb.capacity());
        sb.ensureCapacity(10);
        assertEquals(32, sb.capacity());
        sb.ensureCapacity(100);
        assertTrue(sb.capacity() >= 100);

        sb.append("Hello World");
        assertEquals(11, sb.length());
        sb.minimizeCapacity();
        assertEquals(11, sb.capacity());

        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals("Hello", sb.toString());

        sb.setLength(10);
        assertEquals(10, sb.length());
        assertEquals("Hello\0\0\0\0\0", sb.toString());

        try {
            sb.setLength(-1);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testCharAtAndSetCharAt() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals('a', sb.charAt(0));
        assertEquals('f', sb.charAt(5));

        try {
            sb.charAt(-1);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
        try {
            sb.charAt(6);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }

        sb.setCharAt(0, 'z');
        assertEquals("zbcdef", sb.toString());

        try {
            sb.setCharAt(-1, 'x');
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
        try {
            sb.setCharAt(6, 'x');
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }

        sb.deleteCharAt(0);
        assertEquals("bcdef", sb.toString());
        try {
            sb.deleteCharAt(-1);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
        try {
            sb.deleteCharAt(5);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testToCharArrayAndGetChars() {
        StrBuilder sb = new StrBuilder("hello");
        char[] chars = sb.toCharArray();
        assertArrayEquals(new char[]{'h', 'e', 'l', 'l', 'o'}, chars);

        char[] sub = sb.toCharArray(1, 4);
        assertArrayEquals(new char[]{'e', 'l', 'l'}, sub);

        char[] dest = new char[10];
        sb.getChars(dest);
        assertEquals('h', dest[0]);
        assertEquals('o', dest[4]);
        assertEquals('\0', dest[5]);

        char[] dest2 = new char[5];
        sb.getChars(1, 4, dest2, 1);
        assertEquals('\0', dest2[0]);
        assertEquals('e', dest2[1]);
        assertEquals('l', dest2[2]);
        assertEquals('l', dest2[3]);
        assertEquals('\0', dest2[4]);

        try {
            sb.getChars(-1, 2, dest2, 0);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
        try {
            sb.getChars(0, 10, dest2, 0);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
        try {
            sb.getChars(3, 2, dest2, 0);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testAppendMethods() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\n");
        sb.setNullText("<null>");

        sb.appendNewLine();
        assertEquals("\n", sb.toString());
        sb.clear();

        sb.append((Object) null);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append((String) null);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append("abc");
        sb.append((StringBuffer) null);
        sb.append((StrBuilder) null);
        assertEquals("abc<null><null>", sb.toString());
        sb.clear();

        sb.append(new StringBuffer("def"));
        assertEquals("def", sb.toString());
        sb.clear();

        sb.append(new StrBuilder("ghi"));
        assertEquals("ghi", sb.toString());
        sb.clear();

        sb.append(new char[]{'j', 'k', 'l'});
        assertEquals("jkl", sb.toString());
        sb.clear();

        sb.append(new char[]{'j', 'k', 'l'}, 1, 2);
        assertEquals("kl", sb.toString());
        sb.clear();

        sb.append((char[]) null);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append((char[]) null, 0, 0);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append(true);
        sb.append(false);
        assertEquals("truefalse", sb.toString());
        sb.clear();

        sb.append('c');
        sb.append(123);
        sb.append(456L);
        sb.append(1.5f);
        sb.append(2.5d);
        assertEquals("c1234561.52.5", sb.toString());
        sb.clear();

        sb.appendln("line1");
        sb.appendln((String) null);
        assertEquals("line1\n<null>\n", sb.toString());
        sb.clear();

        sb.appendln((Object) "obj");
        sb.appendln(new StringBuffer("buf"));
        sb.appendln(new StrBuilder("bld"));
        sb.appendln(new char[]{'c'});
        sb.appendln(new char[]{'a', 'b'}, 0, 1);
        sb.appendln(true);
        sb.appendln('x');
        sb.appendln(1);
        sb.appendln(2L);
        sb.appendln(3.0f);
        sb.appendln(4.0d);
        assertTrue(sb.toString().endsWith("4.0\n"));
    }

    @Test
    public void testAppendAllAndWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll((Object[]) null);
        assertEquals(0, sb.length());

        sb.appendAll(new Object[]{"a", "b", "c"});
        assertEquals("abc", sb.toString());
        sb.clear();

        sb.appendAll((Collection<?>) null);
        assertEquals(0, sb.length());

        sb.appendAll(Arrays.asList("x", "y", "z"));
        assertEquals("xyz", sb.toString());
        sb.clear();

        sb.appendAll((Iterator<?>) null);
        assertEquals(0, sb.length());

        sb.appendAll(Arrays.asList("1", "2").iterator());
        assertEquals("12", sb.toString());
        sb.clear();

        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals(0, sb.length());
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());
        sb.clear();

        sb.appendWithSeparators(Arrays.asList("1", "2", "3"), "-");
        assertEquals("1-2-3", sb.toString());
        sb.clear();

        sb.appendWithSeparators(Arrays.asList("a", "b").iterator(), ":");
        assertEquals("a:b", sb.toString());
        sb.clear();

        sb.appendSeparator(",", "default");
        assertEquals("default", sb.toString());
        sb.appendSeparator(",", "default");
        assertEquals("default,", sb.toString());
        sb.clear();

        sb.appendSeparator(",");
        assertEquals("", sb.toString());
        sb.append("test");
        sb.appendSeparator(",");
        assertEquals("test,", sb.toString());

        sb.clear();
        sb.appendSeparator(',', 'd');
        assertEquals("d", sb.toString());
        sb.appendSeparator(',', 'd');
        assertEquals("d,", sb.toString());

        sb.clear();
        sb.appendSeparator(',');
        assertEquals("", sb.toString());
        sb.append("a");
        sb.appendSeparator(',');
        assertEquals("a,", sb.toString());

        sb.clear();
        sb.appendSeparator(",", 1);
        assertEquals("", sb.toString());
        sb.appendSeparator(",", 0);
        assertEquals(",", sb.toString());

        sb.clear();
        sb.appendSeparator(',', 1);
        assertEquals("", sb.toString());
        sb.appendSeparator(',', 0);
        assertEquals(",", sb.toString());
    }

    @Test
    public void testAppendPaddingAndFixedWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(3, '-');
        assertEquals("---", sb.toString());

        sb.appendPadding(-1, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 5, ' ');
        assertEquals("abc  ", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 3, ' ');
        assertEquals("abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(null, 3, 'x');
        assertEquals("xxx", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 5, ' ');
        assertEquals("  abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 3, ' ');
        assertEquals("def", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(null, 3, 'x');
        assertEquals("xxx", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft((Object) 123, 5, '0');
        assertEquals("00123", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight((Object) 123, 5, '0');
        assertEquals("12300", sb.toString());
    }

    @Test
    public void testInsertMethods() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, "b");
        assertEquals("abc", sb.toString());

        sb.insert(0, (Object) null);
        assertEquals("abc", sb.toString());

        sb.setNullText("<null>");
        sb.insert(0, (Object) null);
        assertEquals("<null>abc", sb.toString());
        sb.clear();

        sb.append("ac");
        sb.insert(1, new char[]{'b'});
        assertEquals("abc", sb.toString());

        sb.clear();
        sb.append("ad");
        sb.insert(1, new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        assertEquals("abcd", sb.toString());

        sb.clear();
        sb.append("ac");
        sb.insert(1, (char[]) null);
        assertEquals("a<null>c", sb.toString());

        sb.clear();
        sb.append("ac");
        sb.insert(1, (char[]) null, 0, 0);
        assertEquals("a<null>c", sb.toString());

        sb.clear();
        sb.append("world");
        sb.insert(0, (StringBuffer) null);
        assertEquals("<null>world", sb.toString());

        sb.clear();
        sb.append("world");
        sb.insert(0, new StringBuffer("hello "));
        assertEquals("hello world", sb.toString());

        sb.clear();
        sb.append("world");
        sb.insert(0, (StrBuilder) null);
        assertEquals("<null>world", sb.toString());

        sb.clear();
        sb.append("world");
        sb.insert(0, new StrBuilder("hello "));
        assertEquals("hello world", sb.toString());

        sb.clear();
        sb.insert(0, true);
        sb.insert(4, ' ');
        sb.insert(5, false);
        sb.insert(10, 1);
        sb.insert(11, 2L);
        sb.insert(12, 3.0f);
        sb.insert(16, 4.0d);
        assertTrue(sb.toString().startsWith("true false123.04.0"));

        try {
            sb.insert(-1, "test");
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
        try {
            sb.insert(sb.length() + 1, "test");
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testDeleteAndReplace() {
        StrBuilder sb = new StrBuilder("hello world hello");
        sb.delete(5, 11);
        assertEquals("hello hello", sb.toString());

        sb.delete(0, 0);
        assertEquals("hello hello", sb.toString());

        sb.delete(0, 100);
        assertEquals("", sb.toString());

        try {
            sb.delete(-1, 0);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }

        sb = new StrBuilder("hello world hello");
        sb.deleteAll('l');
        assertEquals("heo word heo", sb.toString());

        sb = new StrBuilder("hello world hello");
        sb.deleteFirst('l');
        assertEquals("helo world hello", sb.toString());

        sb = new StrBuilder("hello world hello");
        sb.deleteAll("hello");
        assertEquals(" world ", sb.toString());

        sb = new StrBuilder("hello world hello");
        sb.deleteFirst("hello");
        assertEquals(" world hello", sb.toString());

        sb = new StrBuilder("hello world hello");
        sb.deleteAll((String) null);
        assertEquals("hello world hello", sb.toString());
        sb.deleteFirst((String) null);
        assertEquals("hello world hello", sb.toString());

        sb = new StrBuilder("hello world hello");
        sb.deleteAll(StrMatcher.stringMatcher("hello"));
        assertEquals(" world ", sb.toString());

        sb = new StrBuilder("hello world hello");
        sb.deleteFirst(StrMatcher.stringMatcher("hello"));
        assertEquals(" world hello", sb.toString());

        sb = new StrBuilder("hello world hello");
        sb.deleteAll((StrMatcher) null);
        assertEquals("hello world hello", sb.toString());

        sb = new StrBuilder("hello world");
        sb.replace(0, 5, "hi");
        assertEquals("hi world", sb.toString());

        sb.replace(2, 20, " there");
        assertEquals("hi there", sb.toString());

        try {
            sb.replace(-1, 2, "a");
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }

        sb = new StrBuilder("a b c a b c");
        sb.replaceAll('a', 'x');
        assertEquals("x b c x b c", sb.toString());

        sb = new StrBuilder("a b c a b c");
        sb.replaceFirst('a', 'x');
        assertEquals("x b c a b c", sb.toString());

        sb = new StrBuilder("a b c a b c");
        sb.replaceAll("a", "xx");
        assertEquals("xx b c xx b c", sb.toString());

        sb = new StrBuilder("a b c a b c");
        sb.replaceFirst("a", "xx");
        assertEquals("xx b c a b c", sb.toString());

        sb = new StrBuilder("a b c a b c");
        sb.replaceAll((String) null, "xx");
        assertEquals("a b c a b c", sb.toString());
        sb.replaceFirst((String) null, "xx");
        assertEquals("a b c a b c", sb.toString());

        sb = new StrBuilder("a b c a b c");
        sb.replaceAll(StrMatcher.stringMatcher("a"), "xx");
        assertEquals("xx b c xx b c", sb.toString());

        sb = new StrBuilder("a b c a b c");
        sb.replaceFirst(StrMatcher.stringMatcher("a"), "xx");
        assertEquals("xx b c a b c", sb.toString());

        sb = new StrBuilder("a b c a b c");
        sb.replaceAll((StrMatcher) null, "xx");
        assertEquals("a b c a b c", sb.toString());

        sb = new StrBuilder("a b c a b c");
        sb.replace(StrMatcher.stringMatcher("a"), "xx", 0, 5, 1);
        assertEquals("xx b c a b c", sb.toString());
    }

    @Test
    public void testSearchMethods() {
        StrBuilder sb = new StrBuilder("the quick brown fox jumps over the lazy dog");

        assertEquals(0, sb.indexOf('t'));
        assertEquals(31, sb.indexOf('t', 1));
        assertEquals(-1, sb.indexOf('z', 40));
        assertEquals(-1, sb.indexOf('Q'));
        assertEquals(0, sb.indexOf('t', -5));

        assertEquals(31, sb.lastIndexOf('t'));
        assertEquals(0, sb.lastIndexOf('t', 30));
        assertEquals(-1, sb.lastIndexOf('t', -1));
        assertEquals(-1, sb.lastIndexOf('Z'));
        assertEquals(42, sb.lastIndexOf('g', 100));

        assertTrue(sb.contains('e'));
        assertFalse(sb.contains('!'));

        assertEquals(0, sb.indexOf("the"));
        assertEquals(31, sb.indexOf("the", 1));
        assertEquals(-1, sb.indexOf("the", 35));
        assertEquals(-1, sb.indexOf((String) null));
        assertEquals(-1, sb.indexOf("not found"));

        assertEquals(31, sb.lastIndexOf("the"));
        assertEquals(0, sb.lastIndexOf("the", 30));
        assertEquals(-1, sb.lastIndexOf("the", -1));
        assertEquals(-1, sb.lastIndexOf((String) null));
        assertEquals(-1, sb.lastIndexOf("not found"));

        assertTrue(sb.contains("quick"));
        assertFalse(sb.contains("slow"));
        assertFalse(sb.contains((String) null));

        StrMatcher matcherThe = StrMatcher.stringMatcher("the");
        assertEquals(0, sb.indexOf(matcherThe));
        assertEquals(31, sb.indexOf(matcherThe, 1));
        assertEquals(-1, sb.indexOf(matcherThe, 35));
        assertEquals(-1, sb.indexOf((StrMatcher) null));

        assertEquals(31, sb.lastIndexOf(matcherThe));
        assertEquals(0, sb.lastIndexOf(matcherThe, 30));
        assertEquals(-1, sb.lastIndexOf(matcherThe, -1));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));

        assertTrue(sb.contains(matcherThe));
        assertFalse(sb.contains(StrMatcher.noneMatcher()));
        assertFalse(sb.contains((StrMatcher) null));
    }

    @Test
    public void testSubstringsAndStartsEndsWith() {
        StrBuilder sb = new StrBuilder("hello world");

        assertEquals("hello", sb.substring(0, 5));
        assertEquals("world", sb.substring(6));
        assertEquals("", sb.substring(5, 5));
        assertEquals("hello world", sb.substring(0));

        try {
            sb.substring(-1, 5);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
        try {
            sb.substring(0, 20);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }
        try {
            sb.substring(5, 3);
            fail();
        } catch (StringIndexOutOfBoundsException expected) {
        }

        assertEquals("hello", sb.leftString(5));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.leftString(-2));
        assertEquals("hello world", sb.leftString(50));

        assertEquals("world", sb.rightString(5));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.rightString(-2));
        assertEquals("hello world", sb.rightString(50));

        assertEquals("world", sb.midString(6, 5));
        assertEquals("", sb.midString(6, 0));
        assertEquals("", sb.midString(6, -2));
        assertEquals("", sb.midString(20, 5));
        assertEquals("hello", sb.midString(-2, 5));
        assertEquals("world", sb.midString(6, 50));

        assertTrue(sb.startsWith("hello"));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith(null));
        assertTrue(sb.startsWith(""));

        assertTrue(sb.endsWith("world"));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith(null));
        assertTrue(sb.endsWith(""));
    }

    @Test
    public void testTrimAndReverse() {
        StrBuilder sb = new StrBuilder("   hello world   ");
        sb.trim();
        assertEquals("hello world", sb.toString());

        sb = new StrBuilder("    ");
        sb.trim();
        assertEquals("", sb.toString());

        sb = new StrBuilder("");
        sb.trim();
        assertEquals("", sb.toString());

        sb = new StrBuilder("abcde");
        sb.reverse();
        assertEquals("edcba", sb.toString());

        sb = new StrBuilder("abcd");
        sb.reverse();
        assertEquals("dcba", sb.toString());

        sb = new StrBuilder("");
        sb.reverse();
        assertEquals("", sb.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        StrBuilder sb3 = new StrBuilder("ABC");
        StrBuilder sb4 = new StrBuilder("abcd");

        assertTrue(sb1.equals(sb1));
        assertTrue(sb1.equals(sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals(sb4));
        assertFalse(sb1.equals("abc"));
        assertFalse(sb1.equals(null));

        assertTrue(sb1.equalsIgnoreCase(sb2));
        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertFalse(sb1.equalsIgnoreCase(sb4));
        assertFalse(sb1.equalsIgnoreCase(null));
        assertTrue(sb1.equalsIgnoreCase(sb1));

        assertEquals(sb1.hashCode(), sb2.hashCode());

        StrBuilder sbEmpty1 = new StrBuilder();
        StrBuilder sbEmpty2 = new StrBuilder();
        assertTrue(sbEmpty1.equals(sbEmpty2));
        assertEquals(sbEmpty1.hashCode(), sbEmpty2.hashCode());
    }

    @Test
    public void testConversionAndAdapters() throws Exception {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("hello", sb.toStringBuffer().toString());
        assertEquals("hello", sb.toStrBuilder().toString());
        assertNotSame(sb, sb.toStrBuilder());

        StrTokenizer tok = sb.asTokenizer();
        assertNotNull(tok);
        assertEquals("hello", tok.nextToken());

        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertEquals('h', (char) reader.read());
        char[] buf = new char[4];
        assertEquals(4, reader.read(buf, 0, 4));
        assertArrayEquals(new char[]{'e', 'l', 'l', 'o'}, buf);
        assertEquals(-1, reader.read());
        reader.close();

        StrBuilder sbOut = new StrBuilder();
        Writer writer = sbOut.asWriter();
        writer.write('a');
        writer.write(new char[]{'b', 'c'});
        writer.write(new char[]{'x', 'd', 'e', 'y'}, 1, 2);
        writer.write("fg");
        writer.write("12hi34", 2, 2);
        writer.flush();
        writer.close();
        assertEquals("abcdefghi", sbOut.toString());
    }

    @Test
    public void testNullTextAndNewLine() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNullText());
        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());
        sb.setNullText("");
        assertNull(sb.getNullText());

        assertNull(sb.getNewLineText());
        sb.setNewLineText("\r\n");
        assertEquals("\r\n", sb.getNewLineText());
    }
}