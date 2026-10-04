package org.apache.velocity.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class TextBufferTest {

    @Test
    public void testDefaultConstructor() {
        TextBuffer tb = new TextBuffer();
        assertNotNull(tb);
        assertEquals(0, tb.length());
    }

    @Test
    public void testCapacityConstructor() {
        TextBuffer tb = new TextBuffer(50);
        assertNotNull(tb);
        assertEquals(0, tb.length());
    }

    @Test
    public void testStringConstructor() {
        TextBuffer tb = new TextBuffer("Hello");
        assertNotNull(tb);
        assertEquals(5, tb.length());
        assertEquals("Hello", tb.toString());
    }

    @Test
    public void testAppendChar() {
        TextBuffer tb = new TextBuffer();
        tb.append('a');
        tb.append('b');
        assertEquals(2, tb.length());
        assertEquals("ab", tb.toString());
    }

    @Test
    public void testAppendString() {
        TextBuffer tb = new TextBuffer();
        tb.append("Hello");
        tb.append(" World");
        assertEquals(11, tb.length());
        assertEquals("Hello World", tb.toString());
    }

    @Test
    public void testAppendNullString() {
        TextBuffer tb = new TextBuffer();
        tb.append((String) null);
        assertEquals(4, tb.length());
        assertEquals("null", tb.toString());
    }

    @Test
    public void testAppendCharArray() {
        TextBuffer tb = new TextBuffer();
        char[] chars = {'t', 'e', 's', 't'};
        tb.append(chars, 0, 4);
        assertEquals(4, tb.length());
        assertEquals("test", tb.toString());
    }

    @Test
    public void testAppendCharArrayWithOffset() {
        TextBuffer tb = new TextBuffer();
        char[] chars = {'a', 'b', 'c', 'd'};
        tb.append(chars, 1, 2);
        assertEquals(2, tb.length());
        assertEquals("bc", tb.toString());
    }

    @Test
    public void testAppendStringBuffer() {
        TextBuffer tb = new TextBuffer();
        StringBuffer sb = new StringBuffer("Buffer");
        tb.append(sb);
        assertEquals(6, tb.length());
        assertEquals("Buffer", tb.toString());
    }

    @Test
    public void testAppendNullStringBuffer() {
        TextBuffer tb = new TextBuffer();
        tb.append((StringBuffer) null);
        assertEquals(4, tb.length());
        assertEquals("null", tb.toString());
    }

    @Test
    public void testAppendObject() {
        TextBuffer tb = new TextBuffer();
        tb.append(Integer.valueOf(123));
        assertEquals(3, tb.length());
        assertEquals("123", tb.toString());
    }

    @Test
    public void testAppendNullObject() {
        TextBuffer tb = new TextBuffer();
        tb.append((Object) null);
        assertEquals(4, tb.length());
        assertEquals("null", tb.toString());
    }

    @Test
    public void testEnsureCapacity() {
        TextBuffer tb = new TextBuffer(5);
        tb.ensureCapacity(100);
        assertTrue(tb.capacity() >= 100);
    }

    @Test
    public void testGetChars() {
        TextBuffer tb = new TextBuffer("Velocity");
        char[] dst = new char[4];
        tb.getChars(0, 4, dst, 0);
        assertArrayEquals(new char[]{'V', 'e', 'l', 'o'}, dst);
    }

    @Test
    public void testSubstring() {
        TextBuffer tb = new TextBuffer("Velocity");
        assertEquals("loc", tb.substring(2, 5));
    }

    @Test
    public void testSubstringFromIndex() {
        TextBuffer tb = new TextBuffer("Velocity");
        assertEquals("city", tb.substring(4));
    }

    @Test
    public void testSetLength() {
        TextBuffer tb = new TextBuffer("Velocity");
        tb.setLength(3);
        assertEquals(3, tb.length());
        assertEquals("Vel", tb.toString());
    }

    @Test
    public void testSetLengthExpand() {
        TextBuffer tb = new TextBuffer("Vel");
        tb.setLength(6);
        assertEquals(6, tb.length());
    }

    @Test
    public void testCharAt() {
        TextBuffer tb = new TextBuffer("Test");
        assertEquals('e', tb.charAt(1));
    }

    @Test
    public void testSetCharAt() {
        TextBuffer tb = new TextBuffer("Test");
        tb.setCharAt(1, 'a');
        assertEquals("Tast", tb.toString());
    }

    @Test
    public void testCapacity() {
        TextBuffer tb = new TextBuffer(10);
        assertTrue(tb.capacity() >= 10);
    }

    @Test
    public void testLength() {
        TextBuffer tb = new TextBuffer("abc");
        assertEquals(3, tb.length());
    }

    @Test
    public void testReset() {
        TextBuffer tb = new TextBuffer("abc");
        tb.reset();
        assertEquals(0, tb.length());
        assertEquals("", tb.toString());
    }

    @Test
    public void testGetBufferAndLength() {
        TextBuffer tb = new TextBuffer("abc");
        char[] buf = tb.getBuffer();
        assertNotNull(buf);
        assertTrue(tb.length() <= buf.length);
    }
}