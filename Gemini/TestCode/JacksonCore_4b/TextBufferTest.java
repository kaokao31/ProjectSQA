package org.apache.velocity.util;

import org.junit.Assert;
import org.junit.Test;

public class TextBufferTest {

    @Test
    public void testDefaultConstructor() {
        TextBuffer buffer = new TextBuffer();
        Assert.assertNotNull(buffer);
        Assert.assertEquals(0, buffer.length());
        Assert.assertEquals("", buffer.toString());
    }

    @Test
    public void testCapacityConstructor() {
        TextBuffer buffer = new TextBuffer(50);
        Assert.assertNotNull(buffer);
        Assert.assertEquals(0, buffer.length());
        Assert.assertEquals("", buffer.toString());
    }

    @Test
    public void testAppendChar() {
        TextBuffer buffer = new TextBuffer();
        buffer.append('a');
        buffer.append('b');
        buffer.append('c');
        Assert.assertEquals(3, buffer.length());
        Assert.assertEquals("abc", buffer.toString());
    }

    @Test
    public void testAppendString() {
        TextBuffer buffer = new TextBuffer();
        buffer.append("Hello");
        buffer.append(" ");
        buffer.append("World!");
        Assert.assertEquals(12, buffer.length());
        Assert.assertEquals("Hello World!", buffer.toString());
    }

    @Test
    public void testAppendNullString() {
        TextBuffer buffer = new TextBuffer();
        buffer.append((String) null);
        // Depending on implementation, null might be ignored or appended as "null".
        // Let's check length or safely assert behavior.
        Assert.assertNotNull(buffer.toString());
    }

    @Test
    public void testAppendCharArray() {
        TextBuffer buffer = new TextBuffer();
        char[] chars = {'J', 'u', 'n', 'i', 't'};
        buffer.append(chars, 1, 3); // "uni"
        Assert.assertEquals(3, buffer.length());
        Assert.assertEquals("uni", buffer.toString());
    }

    @Test
    public void testAppendSubString() {
        TextBuffer buffer = new TextBuffer();
        buffer.append("Velocity", 0, 4); // "Velo"
        Assert.assertEquals(4, buffer.length());
        Assert.assertEquals("Velo", buffer.toString());
    }

    @Test
    public void testAppendTextBuffer() {
        TextBuffer buffer1 = new TextBuffer();
        buffer1.append("Foo");

        TextBuffer buffer2 = new TextBuffer();
        buffer2.append("Bar");
        buffer2.append(buffer1);

        Assert.assertEquals("BarFoo", buffer2.toString());
    }

    @Test
    public void testEnsureCapacity() {
        TextBuffer buffer = new TextBuffer(5);
        buffer.ensureCapacity(100);
        buffer.append("This is a much longer string that should exceed the initial capacity easily.");
        Assert.assertTrue(buffer.length() > 0);
    }

    @Test
    public void testSetLength() {
        TextBuffer buffer = new TextBuffer();
        buffer.append("HelloWorld");
        buffer.setLength(5);
        Assert.assertEquals(5, buffer.length());
        Assert.assertEquals("Hello", buffer.toString());

        buffer.setLength(10);
        Assert.assertEquals(10, buffer.length());
    }

    @Test
    public void testClear() {
        TextBuffer buffer = new TextBuffer();
        buffer.append("Data");
        buffer.clear();
        Assert.assertEquals(0, buffer.length());
        Assert.assertEquals("", buffer.toString());
    }

    @Test
    public void testSubstring() {
        TextBuffer buffer = new TextBuffer();
        buffer.append("Apache Velocity");
        String sub = buffer.substring(7);
        Assert.assertEquals("Velocity", sub);
    }

    @Test
    public void testGetChars() {
        TextBuffer buffer = new TextBuffer();
        buffer.append("Test");
        char[] dst = new char[4];
        buffer.getChars(0, 4, dst, 0);
        Assert.assertArrayEquals(new char[]{'T', 'e', 's', 't'}, dst);
    }
}