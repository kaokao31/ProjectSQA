package org.apache.commons.collections4.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.collections4.BoundedCollection;
import org.junit.Test;

public class UnmodifiableBoundedCollectionTest {

    @Test
    public void testUnmodifiableBoundedCollectionCreationAndWrapping() {
        // Create an underlying bounded collection (e.g., ArrayStack or a mock/simple BoundedCollection)
        // Since commons-collections4 has CircularFifoQueue or similar BoundedCollection implementations, 
        // let's create a custom simple BoundedCollection or use a known one.
        // Alternatively, implement BoundedCollection for testing purposes.
        
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        underlying.add("A");
        underlying.add("B");

        UnmodifiableBoundedCollection<String> wrapped = 
                (UnmodifiableBoundedCollection<String>) UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);

        assertNotNull(wrapped);
        assertTrue(wrapped instanceof BoundedCollection);
        assertEquals(2, wrapped.size());
        assertEquals(5, wrapped.maxSize());
        assertTrue(wrapped.isFull());
        assertEquals(3, wrapped.remainingCapacity());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddUnsupported() {
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        BoundedCollection<String> wrapped = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);
        wrapped.add("Test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllUnsupported() {
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        BoundedCollection<String> wrapped = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);
        wrapped.addAll(Collections.singleton("Test"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClearUnsupported() {
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        underlying.add("A");
        BoundedCollection<String> wrapped = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);
        wrapped.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveUnsupported() {
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        underlying.add("A");
        BoundedCollection<String> wrapped = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);
        wrapped.remove("A");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAllUnsupported() {
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        underlying.add("A");
        BoundedCollection<String> wrapped = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);
        wrapped.removeAll(Collections.singleton("A"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAllUnsupported() {
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        underlying.add("A");
        BoundedCollection<String> wrapped = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);
        wrapped.retainAll(Collections.emptySet());
    }

    @Test
    public void testIteratorUnmodifiable() {
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        underlying.add("A");
        underlying.add("B");
        BoundedCollection<String> wrapped = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);

        Iterator<String> it = wrapped.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());

        boolean exceptionThrown = false;
        try {
            it.remove();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Test
    public void testAlreadyUnmodifiableBoundedCollection() {
        TestBoundedCollection<String> underlying = new TestBoundedCollection<>(5);
        BoundedCollection<String> wrapped1 = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(underlying);
        BoundedCollection<String> wrapped2 = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(wrapped1);

        assertSame(wrapped1, wrapped2);
    }

    @Test(expected = NullPointerException.class)
    public void testNullCollection() {
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotABoundedCollection() {
        Collection<String> normalCollection = new ArrayList<>();
        UnmodifiableBoundedCollection.unmodifiableBoundedCollection(normalCollection);
    }

    // Helper implementation of BoundedCollection for testing
    private static class TestBoundedCollection<E> implements BoundedCollection<E> {
        private final int maxSize;
        private final ListDelegate<E> delegate = new ListDelegate<>();

        public TestBoundedCollection(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public int maxSize() {
            return maxSize;
        }

        @Override
        public boolean isFull() {
            return delegate.size() >= maxSize;
        }

        @Override
        public int remainingCapacity() {
            return maxSize - delegate.size();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<E> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean add(E e) {
            if (isFull()) {
                return false;
            }
            return delegate.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            if (delegate.size() + c.size() > maxSize) {
                return false;
            }
            return delegate.addAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        private static class ListDelegate<E> extends ArrayList<E> {
            private static final long serialVersionUID = 1L;
        }
    }
}