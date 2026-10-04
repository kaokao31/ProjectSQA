package org.apache.commons.lang.text;

import org.junit.Test;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
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
    public void testLang295() {
        StrBuilder sb = new StrBuilder("123456789012345678901234567890");
        sb.setLength(10);
        assertFalse("The contains(char) method is looking beyond the end of the string", sb.contains('h'));
        assertEquals(-1, sb.indexOf('h'));
        assertEquals(-1, sb.lastIndexOf('h'));
        assertFalse(sb.contains("12345678901"));
        assertEquals(-1, sb.indexOf("12345678901"));
        assertEquals(-1, sb.lastIndexOf("12345678901"));
        assertFalse(sb.contains(StrMatcher.charMatcher('h')));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('h')));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('h')));
    }

    @Test
    public void testConstructors() {
        StrBuilder sb1 = new StrBuilder();
        assertEquals(32, sb1.capacity());
        assertEquals(0, sb1.length());
        assertEquals(0, sb1.size());

        StrBuilder sb2 = new StrBuilder(64);
        assertEquals(64, sb2.capacity());
        assertEquals(0, sb2.length());

        StrBuilder sb3 = new StrBuilder(-5);
        assertEquals(32, sb3.capacity());

        StrBuilder sb4 = new StrBuilder("hello");
        assertEquals("hello", sb4.toString());
        assertEquals(5, sb4.length());
        assertEquals(5 + 32, sb4.capacity());

        StrBuilder sb5 = new StrBuilder((String) null);
        assertEquals("", sb5.toString());
        assertEquals(32, sb5.capacity());
    }

    @Test
    public void testGetSetNewLineText() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNewLineText());
        sb.setNewLineText("\r\n");
        assertEquals("\r\n", sb.getNewLineText());
        sb.setNewLineText(null);
        assertNull(sb.getNewLineText());
    }

    @Test
    public void testGetSetNullText() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNullText());
        sb.setNullText("<null>");
        assertEquals("<null>", sb.getNullText());
        sb.append((String) null);
        assertEquals("<null>", sb.toString());
        sb.setNullText(null);
        assertNull(sb.getNullText());
    }

    @Test
    public void testLengthAndCapacity() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals(4, sb.length());
        assertEquals(4, sb.size());
        assertFalse(sb.isEmpty());

        sb.setLength(2);
        assertEquals("te", sb.toString());
        assertEquals(2, sb.length());

        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals("te\0\0\0", sb.toString());

        try {
            sb.setLength(-1);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());

        sb.ensureCapacity(100);
        assertTrue(sb.capacity() >= 100);

        sb.append("12345");
        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());
    }

    @Test
    public void testCharAtAndSetCharAt() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals('a', sb.charAt(0));
        assertEquals('f', sb.charAt(5));

        try {
            sb.charAt(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            sb.charAt(6);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        sb.setCharAt(0, 'z');
        assertEquals("zbcdef", sb.toString());

        try {
            sb.setCharAt(-1, 'x');
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            sb.setCharAt(6, 'x');
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        sb.deleteCharAt(0);
        assertEquals("bcdef", sb.toString());

        try {
            sb.deleteCharAt(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            sb.deleteCharAt(5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testToCharArrayAndGetChars() {
        StrBuilder sb = new StrBuilder("hello world");
        char[] array = sb.toCharArray();
        assertArrayEquals("hello world".toCharArray(), array);

        char[] emptyArray = new StrBuilder().toCharArray();
        assertEquals(0, emptyArray.length);

        char[] dest = new char[5];
        sb.getChars(0, 5, dest, 0);
        assertArrayEquals("hello".toCharArray(), dest);

        char[] fullDest = new char[sb.length()];
        sb.getChars(fullDest);
        assertArrayEquals("hello world".toCharArray(), fullDest);

        try {
            sb.getChars(-1, 5, dest, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            sb.getChars(0, 12, dest, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }

        try {
            sb.getChars(5, 2, dest, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testAppendPrimitives() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(false);
        assertEquals("truefalse", sb.toString());

        sb.clear();
        sb.append('c').append(123).append(456L).append(1.5f).append(2.5d);
        assertEquals("c1234561.52.5", sb.toString());
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
        sb.setNullText(null);
        sb.append("foo");
        sb.append(new StringBuffer("bar"));
        sb.append(new StrBuilder("baz"));
        assertEquals("foobarbaz", sb.toString());

        sb.clear();
        sb.append("abcdef", 1, 3);
        assertEquals("bcd", sb.toString());

        sb.append((String) null, 0, 5);
        assertEquals("bcd", sb.toString());

        sb.setNullText("null");
        sb.append((String) null, 0, 5);
        assertEquals("bcdnull", sb.toString());

        try {
            sb.append("test", -1, 2);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.append("test", 2, -1);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.append("test", 2, 5);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testAppendCharArrays() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b', 'c'});
        assertEquals("abc", sb.toString());

        sb.append((char[]) null);
        assertEquals("abc", sb.toString());

        sb.setNullText("<null>");
        sb.append((char[]) null);
        assertEquals("abc<null>", sb.toString());

        sb.clear();
        sb.append(new char[]{'a', 'b', 'c', 'd', 'e'}, 1, 3);
        assertEquals("bcd", sb.toString());

        sb.append((char[]) null, 0, 2);
        assertEquals("bcd<null>", sb.toString());

        try {
            sb.append(new char[]{'a'}, -1, 1);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.append(new char[]{'a'}, 0, -1);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.append(new char[]{'a'}, 0, 2);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testAppendBufferAndBuilderWithBounds() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StringBuffer("hello"), 1, 3);
        assertEquals("ell", sb.toString());

        sb.append((StringBuffer) null, 0, 1);
        assertEquals("ell", sb.toString());

        sb.append(new StrBuilder("world"), 1, 3);
        assertEquals("ellorl", sb.toString());

        sb.append((StrBuilder) null, 0, 1);
        assertEquals("ellorl", sb.toString());

        try {
            sb.append(new StringBuffer("test"), -1, 2);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.append(new StrBuilder("test"), -1, 2);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testAppendNewLine() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\n");
        sb.appendNewLine();
        assertEquals("\n", sb.toString());

        sb.clear();
        sb.appendln("line1");
        sb.appendln("line2");
        assertEquals("line1\nline2\n", sb.toString());

        sb.clear();
        sb.appendln((String) null);
        assertEquals("\n", sb.toString());

        sb.clear();
        sb.appendln(new Object() {
            public String toString() {
                return "obj";
            }
        });
        assertEquals("obj\n", sb.toString());

        sb.clear();
        sb.appendln(123);
        sb.appendln(true);
        sb.appendln('x');
        assertEquals("123\ntrue\nx\n", sb.toString());
    }

    @Test
    public void testAppendAllAndWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll(new Object[]{"a", "b", "c"});
        assertEquals("abc", sb.toString());

        sb.clear();
        sb.appendAll(Arrays.asList("x", "y", "z"));
        assertEquals("xyz", sb.toString());

        sb.clear();
        sb.appendAll(Arrays.asList("1", "2").iterator());
        assertEquals("12", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("a", "b", "c"), ",");
        assertEquals("a,b,c", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("a", "b").iterator(), "-");
        assertEquals("a-b", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Collection<?>) null, ",");
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Iterator<?>) null, ",");
        assertEquals("", sb.toString());
    }

    @Test
    public void testAppendSeparator() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        assertEquals("", sb.toString());
        sb.append("foo");
        sb.appendSeparator(",");
        assertEquals("foo,", sb.toString());
        sb.appendSeparator((String) null);
        assertEquals("foo,", sb.toString());

        sb.clear();
        sb.appendSeparator(',', "default");
        assertEquals("default", sb.toString());
        sb.appendSeparator(',', "default");
        assertEquals("default,", sb.toString());

        sb.clear();
        sb.appendSeparator(",", "default");
        assertEquals("default", sb.toString());
        sb.appendSeparator(",", "default");
        assertEquals("default,", sb.toString());

        sb.clear();
        sb.appendSeparator(',', -1);
        assertEquals("", sb.toString());
        sb.appendSeparator(',', 0);
        assertEquals("", sb.toString());
        sb.appendSeparator(',', 1);
        assertEquals(",", sb.toString());

        sb.clear();
        sb.appendSeparator("sep", 1);
        assertEquals("sep", sb.toString());
        sb.appendSeparator("sep", 0);
        assertEquals("sep", sb.toString());
    }

    @Test
    public void testAppendPadding() {
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
        sb.appendFixedWidthPadLeft("abc", 5, ' ');
        assertEquals("  abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 3, ' ');
        assertEquals("def", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(null, 3, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(null, 3, '-');
        assertEquals("---", sb.toString());
    }

    @Test
    public void testInsertOperations() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.insert(5, " beautiful");
        assertEquals("hello beautiful world", sb.toString());

        sb.insert(0, (String) null);
        assertEquals("hello beautiful world", sb.toString());

        sb.setNullText("<null>");
        sb.insert(0, (String) null);
        assertEquals("<null>hello beautiful world", sb.toString());

        sb.clear();
        sb.append("world");
        sb.insert(0, new char[]{'h', 'e', 'l', 'l', 'o', ' '});
        assertEquals("hello world", sb.toString());

        sb.clear();
        sb.append("world");
        sb.insert(0, new char[]{'a', 'h', 'e', 'l', 'l', 'o', ' ', 'z'}, 1, 6);
        assertEquals("hello world", sb.toString());

        sb.insert(0, true);
        assertEquals("truehello world", sb.toString());

        sb.insert(0, 'x');
        assertEquals("xtruehello world", sb.toString());

        sb.insert(0, 10);
        assertEquals("10xtruehello world", sb.toString());

        sb.insert(0, 20L);
        assertEquals("2010xtruehello world", sb.toString());

        sb.insert(0, 1.5f);
        assertEquals("1.52010xtruehello world", sb.toString());

        sb.insert(0, 2.5d);
        assertEquals("2.51.52010xtruehello world", sb.toString());

        try {
            sb.insert(-1, "bad");
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.insert(sb.length() + 1, "bad");
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testDeleteOperations() {
        StrBuilder sb = new StrBuilder("0123456789");
        sb.delete(2, 5);
        assertEquals("0156789", sb.toString());

        sb.delete(5, 100);
        assertEquals("01567", sb.toString());

        sb.delete(2, 2);
        assertEquals("01567", sb.toString());

        try {
            sb.delete(-1, 2);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.delete(2, 1);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        sb.clear();
        sb.append("hello world hello");
        sb.deleteAll('l');
        assertEquals("heo word heo", sb.toString());

        sb.deleteFirst('o');
        assertEquals("he word heo", sb.toString());

        sb.clear();
        sb.append("hello world hello");
        sb.deleteAll("ll");
        assertEquals("heo world heo", sb.toString());

        sb.deleteFirst("he");
        assertEquals("o world heo", sb.toString());

        sb.deleteAll((String) null);
        sb.deleteFirst((String) null);
        sb.deleteAll("");
        sb.deleteFirst("");
        assertEquals("o world heo", sb.toString());

        sb.clear();
        sb.append("hello world");
        sb.deleteAll(StrMatcher.charMatcher('o'));
        assertEquals("hell wrld", sb.toString());

        sb.deleteFirst(StrMatcher.charMatcher('l'));
        assertEquals("hel wrld", sb.toString());

        sb.deleteAll((StrMatcher) null);
        sb.deleteFirst((StrMatcher) null);
        assertEquals("hel wrld", sb.toString());
    }

    @Test
    public void testReplaceOperations() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.replace(0, 5, "goodbye");
        assertEquals("goodbye world", sb.toString());

        sb.replace(0, 7, null);
        assertEquals(" world", sb.toString());

        sb.clear();
        sb.append("foo bar foo bar foo");
        sb.replaceAll('o', 'z');
        assertEquals("fzz bar fzz bar fzz", sb.toString());

        sb.replaceFirst('z', 'a');
        assertEquals("faz bar fzz bar fzz", sb.toString());

        sb.clear();
        sb.append("foo bar foo bar foo");
        sb.replaceAll("foo", "qux");
        assertEquals("qux bar qux bar qux", sb.toString());

        sb.replaceFirst("bar", "baz");
        assertEquals("qux baz qux bar qux", sb.toString());

        sb.replaceAll((String) null, "test");
        sb.replaceAll("", "test");
        sb.replaceFirst((String) null, "test");
        sb.replaceFirst("", "test");
        assertEquals("qux baz qux bar qux", sb.toString());

        sb.clear();
        sb.append("foo bar foo bar foo");
        sb.replaceAll(StrMatcher.stringMatcher("foo"), "qux");
        assertEquals("qux bar qux bar qux", sb.toString());

        sb.replaceFirst(StrMatcher.stringMatcher("bar"), "baz");
        assertEquals("qux baz qux bar qux", sb.toString());

        sb.replaceAll((StrMatcher) null, "test");
        sb.replaceFirst((StrMatcher) null, "test");
        assertEquals("qux baz qux bar qux", sb.toString());

        sb.clear();
        sb.append("a b c");
        sb.replace(StrMatcher.charMatcher(' '), "X", 0, 4, 1);
        assertEquals("aXb c", sb.toString());

        try {
            sb.replace(-1, 2, "test");
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testReverseAndTrim() {
        StrBuilder sb = new StrBuilder("abcde");
        sb.reverse();
        assertEquals("edcba", sb.toString());

        sb.clear();
        sb.append("abcd");
        sb.reverse();
        assertEquals("dcba", sb.toString());

        sb.clear();
        sb.reverse();
        assertEquals("", sb.toString());

        sb.append("   hello world   ");
        sb.trim();
        assertEquals("hello world", sb.toString());

        sb.clear();
        sb.append("   ");
        sb.trim();
        assertEquals("", sb.toString());
    }

    @Test
    public void testStartsWithAndEndsWith() {
        StrBuilder sb = new StrBuilder("hello world");
        assertTrue(sb.startsWith("hello"));
        assertTrue(sb.startsWith("hello world"));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith((String) null));
        assertFalse(sb.startsWith("hello world long string"));

        assertTrue(sb.endsWith("world"));
        assertTrue(sb.endsWith("hello world"));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith((String) null));
        assertFalse(sb.endsWith("hello world long string"));
    }

    @Test
    public void testSubstrings() {
        StrBuilder sb = new StrBuilder("hello world");
        assertEquals("hello", sb.substring(0, 5));
        assertEquals("world", sb.substring(6));
        assertEquals("world", sb.midString(6, 5));
        assertEquals("world", sb.midString(6, 10));
        assertEquals("", sb.midString(15, 2));
        assertEquals("hel", sb.midString(-2, 3));

        assertEquals("hello", sb.leftString(5));
        assertEquals("hello world", sb.leftString(20));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.leftString(-1));

        assertEquals("world", sb.rightString(5));
        assertEquals("hello world", sb.rightString(20));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.rightString(-1));

        assertEquals("hello", sb.subSequence(0, 5));

        try {
            sb.substring(-1, 5);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.substring(0, 20);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }

        try {
            sb.substring(5, 2);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
        }
    }

    @Test
    public void testSearchMethods() {
        StrBuilder sb = new StrBuilder("the quick brown fox jumps over the lazy dog");

        assertEquals(0, sb.indexOf('t'));
        assertEquals(31, sb.indexOf('t', 5));
        assertEquals(-1, sb.indexOf('z', 40));
        assertEquals(-1, sb.indexOf('!', 0));

        assertEquals(31, sb.lastIndexOf('t'));
        assertEquals(0, sb.lastIndexOf('t', 5));
        assertEquals(-1, sb.lastIndexOf('z', 20));

        assertEquals(4, sb.indexOf("quick"));
        assertEquals(4, sb.indexOf("quick", 0));
        assertEquals(-1, sb.indexOf("quick", 10));
        assertEquals(-1, sb.indexOf((String) null));
        assertEquals(0, sb.indexOf(""));
        assertEquals(5, sb.indexOf("", 5));

        assertEquals(31, sb.lastIndexOf("the"));
        assertEquals(0, sb.lastIndexOf("the", 10));
        assertEquals(-1, sb.lastIndexOf((String) null));
        assertEquals(sb.length(), sb.lastIndexOf(""));
        assertEquals(5, sb.lastIndexOf("", 5));

        StrMatcher foxMatcher = StrMatcher.stringMatcher("fox");
        assertEquals(16, sb.indexOf(foxMatcher));
        assertEquals(16, sb.indexOf(foxMatcher, 0));
        assertEquals(-1, sb.indexOf(foxMatcher, 20));
        assertEquals(-1, sb.indexOf((StrMatcher) null));

        assertEquals(16, sb.lastIndexOf(foxMatcher));
        assertEquals(16, sb.lastIndexOf(foxMatcher, 20));
        assertEquals(-1, sb.lastIndexOf(foxMatcher, 10));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));

        assertTrue(sb.contains('q'));
        assertFalse(sb.contains('1'));
        assertTrue(sb.contains("brown"));
        assertFalse(sb.contains("black"));
        assertTrue(sb.contains(foxMatcher));
        assertFalse(sb.contains(StrMatcher.stringMatcher("cat")));
    }

    @Test
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("hello");
        StrBuilder sb2 = new StrBuilder("hello");
        StrBuilder sb3 = new StrBuilder("world");

        assertTrue(sb1.equals(sb1));
        assertTrue(sb1.equals(sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals("hello"));
        assertFalse(sb1.equals(null));

        assertEquals(sb1.hashCode(), sb2.hashCode());
        assertTrue(sb1.equalsIgnoreCase(sb2));

        StrBuilder sbCase = new StrBuilder("HELLO");
        assertFalse(sb1.equals(sbCase));
        assertTrue(sb1.equalsIgnoreCase(sbCase));
        assertFalse(sb1.equalsIgnoreCase(null));
        assertFalse(sb1.equalsIgnoreCase(sb3));
    }

    @Test
    public void testReadersAndWriters() throws IOException {
        StrBuilder sb = new StrBuilder("hello world");
        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        char[] buf = new char[5];
        assertEquals(5, reader.read(buf));
        assertEquals("hello", new String(buf));
        assertEquals(' ', (char) reader.read());
        assertEquals(5, reader.skip(5));
        assertEquals(-1, reader.read());
        reader.close();

        Writer writer = sb.asWriter();
        writer.write("!");
        writer.write("!!".toCharArray());
        writer.write("12345", 0, 3);
        writer.flush();
        writer.close();
        assertEquals("hello world!!!!123", sb.toString());
    }

    @Test
    public void testTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tokenizer = sb.asTokenizer();
        assertNotNull(tokenizer);
        assertEquals("a", tokenizer.next());
        assertEquals("b", tokenizer.next());
        assertEquals("c", tokenizer.next());
    }
}