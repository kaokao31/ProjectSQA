package org.apache.commons.jxpath.ri;

import org.apache.commons.jxpath.Pointer;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class NamespaceResolverTest {

    private NamespaceResolver parentResolver;
    private NamespaceResolver childResolver;

    @Before
    public void setUp() {
        parentResolver = new NamespaceResolver();
        childResolver = new NamespaceResolver(parentResolver);
    }

    @Test
    public void testGetNamespaceURIByPrefixLocal() {
        childResolver.registerNamespace("prefix1", "http://uri1");
        assertEquals("http://uri1", childResolver.getNamespaceURI("prefix1"));
    }

    @Test
    public void testGetNamespaceURIByPrefixParent() {
        parentResolver.registerNamespace("prefixParent", "http://uriParent");
        assertEquals("http://uriParent", childResolver.getNamespaceURI("prefixParent"));
    }

    @Test
    public void testGetNamespaceURIByPrefixNotFound() {
        assertNull(childResolver.getNamespaceURI("nonexistent"));
    }

    @Test
    public void testGetPrefixByURILocal() {
        childResolver.registerNamespace("prefix1", "http://uri1");
        assertEquals("prefix1", childResolver.getPrefix("http://uri1"));
    }

    @Test
    public void testGetPrefixByURIParent() {
        parentResolver.registerNamespace("prefixParent", "http://uriParent");
        assertEquals("prefixParent", childResolver.getPrefix("http://uriParent"));
    }

    @Test
    public void testGetPrefixByURINotFound() {
        assertNull(childResolver.getPrefix("http://nonexistent"));
    }

    @Test
    public void testGetNamespaceContextPointerWithoutPointer() {
        assertNull(childResolver.getNamespaceContextPointer());
    }

    @Test
    public void testGetNamespaceContextPointerWithLocalPointer() {
        DummyPointer dummyPointer = new DummyPointer();
        childResolver.setNamespaceContextPointer(dummyPointer);
        assertSame(dummyPointer, childResolver.getNamespaceContextPointer());
    }

    @Test
    public void testGetNamespaceContextPointerWithParentPointer() {
        DummyPointer parentPointer = new DummyPointer();
        parentResolver.setNamespaceContextPointer(parentPointer);
        
        // Child has no pointer set, should delegate to parent
        assertSame(parentPointer, childResolver.getNamespaceContextPointer());
    }

    @Test
    public void testIsSealed() {
        assertFalse(childResolver.isSealed());
        childResolver.seal();
        assertTrue(childResolver.isSealed());
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterNamespaceWhenSealed() {
        childResolver.seal();
        childResolver.registerNamespace("prefix", "uri");
    }

    @Test
    public void testClone() {
        childResolver.registerNamespace("p1", "u1");
        DummyPointer pointer = new DummyPointer();
        childResolver.setNamespaceContextPointer(pointer);
        childResolver.seal();

        NamespaceResolver clone = (NamespaceResolver) childResolver.clone();
        assertNotNull(clone);
        assertEquals("u1", clone.getNamespaceURI("p1"));
        assertTrue(clone.isSealed());
        // Pointer might be cloned or shared depending on implementation, 
        // but context pointer should be accessible.
        assertNotNull(clone.getNamespaceContextPointer());
    }

    private static class DummyPointer extends org.apache.commons.jxpath.ri.pointer.AbstractPointer {
        private static final long serialVersionUID = 1L;

        @Override
        public Object getNode() {
            return null;
        }

        @Override
        public String getAsPath() {
            return "/";
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }

        @Override
        public QName getName() {
            return new QName("dummy");
        }

        @Override
        public Object getImmediateNode() {
            return null;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public void setValue(Object value) {
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    }
}