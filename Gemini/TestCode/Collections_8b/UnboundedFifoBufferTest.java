package org.apache.commons.collections.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UnboundedFifoBufferTest {

    private UnboundedFifoBuffer buffer;

    @Before
    public void setUp() {
        buffer = new UnboundedFifoBuffer();
    }

    @Test
    public void testConstructorDefault() {
        Assert.assertNotNull(buffer);
        Assert.assertEquals(0, buffer.size());
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test
    public void testConstructorWithInitialSize() {
        UnboundedFifoBuffer customBuffer = new UnboundedFifoBuffer(16);
        Assert.assertNotNull(customBuffer);
        Assert.assertEquals(0, customBuffer.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidSizeZero() {
        new UnboundedFifoBuffer(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidSizeNegative() {
        new UnboundedFifoBuffer(-5);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCollection() {
        new UnboundedFifoBuffer(null);
    }

    @Test
    public void testConstructorWithCollection() {
        Collection<String> col = new ArrayList<String>();
        col.add("A");
        col.add("B");
        col.add("C");

        UnboundedFifoBuffer colBuffer = new UnboundedFifoBuffer(col);
        Assert.assertEquals(3, colBuffer.size());
        Assert.assertEquals("A", colBuffer.remove());
        Assert.assertEquals("B", colBuffer.remove());
        Assert.assertEquals("C", colBuffer.remove());
    }

    @Test
    public void testAddAndRemove() {
        Assert.assertTrue(buffer.add("One"));
        Assert.assertTrue(buffer.add("Two"));
        Assert.assertEquals(2, buffer.size());
        Assert.assertFalse(buffer.isEmpty());

        Assert.assertEquals("One", buffer.remove());
        Assert.assertEquals(1, buffer.size());

        Assert.assertEquals("Two", buffer.remove());
        Assert.assertEquals(0, buffer.size());
        Assert.assertTrue(buffer.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        buffer.add(null);
    }

    @Test(expected = BufferUnderflowException.class)
    public void testRemoveEmpty() {
        buffer.remove();
    }

    @Test
    public void testGetUnderflow() {
        try {
            buffer.get();
            Assert.fail("Expected BufferUnderflowException");
        } catch (BufferUnderflowException e) {
            // expected
        }
    }

    @Test
    public void testBufferGrowthAndWrapAround() {
        // Default initial buffer size is typically small (e.g., 32).
        // Let's push enough elements to force expansion and circular array wrap-around.
        int count = 50;
        for (int i = 0; i < count; i++) {
            buffer.add("Item " + i);
        }
        Assert.assertEquals(count, buffer.size());

        // Remove half
        for (int i = 0; i < 25; i++) {
            Assert.assertEquals("Item " + i, buffer.remove());
        }
        Assert.assertEquals(count - 25, buffer.size());

        // Add more to trigger wrap-around of pointers (tail wrapping around buffer length)
        for (int i = count; i < count + 40; i++) {
            buffer.add("Item " + i);
        }

        // Verify remaining and new items
        for (int i = 25; i < count + 40; i++) {
            Assert.assertEquals("Item " + i, buffer.remove());
        }

        Assert.assertTrue(buffer.isEmpty());
    }

    @Test
    public void testIterator() {
        buffer.add("A");
        buffer.add("B");
        buffer.add("C");

        Iterator<?> it = buffer.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNoSuchElement() {
        buffer.add("A");
        Iterator<?> it = buffer.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        it.next(); // Should throw NoSuchElementException
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemoveUnsupported() {
        buffer.add("A");
        Iterator<?> it = buffer.iterator();
        it.next();
        it.remove();
    }

    @Test
    public void testIteratorWithWrapAround() {
        // Fill and partially empty to shift head/tail and trigger wrap-around in iterator
        for (int i = 0; i < 40; i++) {
            buffer.add(i);
        }
        for (int i = 0; i < 20; i++) {
            buffer.remove();
        }
        for (int i = 40; i < 60; i++) {
            buffer.add(i);
        }

        int expectedValue = 20;
        for (Iterator<?> it = buffer.iterator(); it.hasNext();) {
            Assert.assertEquals(expectedValue++, it.next());
        }
        Assert.assertEquals(40, expectedValue);
    }
}