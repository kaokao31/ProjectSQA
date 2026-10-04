package org.mockito;

import org.junit.Test;
import org.mockito.internal.matchers.ArrayEquals;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.NotNull;
import org.mockito.internal.matchers.Null;
import org.mockito.internal.matchers.Same;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.internal.progress.ArgumentMatcherStorage;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MatchersTest {

    @Test
    public void testAnyObject() {
        assertNull(Matchers.anyObject());
        assertNull(Matchers.any());
        assertNull(Matchers.any(String.class));
    }

    @Test
    public void testAnyVararg() {
        assertNull(Matchers.anyVararg());
    }

    @Test
    public void testPrimitives() {
        assertEquals(0, Matchers.anyBoolean());
        assertEquals(0, Matchers.anyByte());
        assertEquals(0, Matchers.anyChar());
        assertEquals(0.0, Matchers.anyDouble(), 0.0);
        assertEquals(0.0f, Matchers.anyFloat(), 0.0f);
        assertEquals(0, Matchers.anyInt());
        assertEquals(0L, Matchers.anyLong());
        assertEquals(0, Matchers.anyShort());
    }

    @Test
    public void testPrimitiveWrappers() {
        assertNull(Matchers.anyBoolean());
        assertNull(Matchers.anyByte());
        assertNull(Matchers.anyChar());
        assertNull(Matchers.anyDouble());
        assertNull(Matchers.anyFloat());
        assertNull(Matchers.anyInt());
        assertNull(Matchers.anyLong());
        assertNull(Matchers.anyShort());
    }

    @Test
    public void testCollectionsAndMaps() {
        assertNull(Matchers.anyCollection());
        assertNull(Matchers.anyCollection(String.class));
        assertNull(Matchers.anyList());
        assertNull(Matchers.anyList(String.class));
        assertNull(Matchers.anyMap());
        assertNull(Matchers.anySet());
        assertNull(Matchers.anySet(String.class));
        assertNull(Matchers.anyString());
    }

    @Test
    public void testEq() {
        assertNull(Matchers.eq(null));
        assertNull(Matchers.eq("test"));
        assertNull(Matchers.eq(Boolean.TRUE));
        assertNull(Matchers.eq(Byte.valueOf((byte) 1)));
        assertNull(Matchers.eq(Character.valueOf('a')));
        assertNull(Matchers.eq(Double.valueOf(1.0)));
        assertNull(Matchers.eq(Float.valueOf(1.0f)));
        assertNull(Matchers.eq(Integer.valueOf(1)));
        assertNull(Matchers.eq(Long.valueOf(1L)));
        assertNull(Matchers.eq(Short.valueOf((short) 1)));
    }

    @Test
    public void testRefEq() {
        assertNull(Matchers.refEq("test", "exclude"));
    }

    @Test
    public void testSame() {
        assertNull(Matchers.same("test"));
    }

    @Test
    public void testNotNull() {
        assertNull(Matchers.isNotNull());
        assertNull(Matchers.notNull());
    }

    @Test
    public void testNull() {
        assertNull(Matchers.isNull());
    }

    @Test
    public void testApidocMethods() {
        assertNull(Matchers.contains("sub"));
        assertNull(Matchers.endsWith("suffix"));
        assertNull(Matchers.startsWith("prefix"));
        assertNull(Matchers.matches("regex"));
        assertNull(Matchers.anyString());
    }

    @Test
    public void testIsA() {
        assertNull(Matchers.isA(String.class));
    }

    @Test
    public void testFloatDoubleEq() {
        assertNull(Matchers.eq(1.0f, 0.1f));
        assertNull(Matchers.eq(1.0, 0.1));
    }
}