package org.mockito;

import org.junit.Test;
import static org.junit.Assert.*;

public class MatchersTest {

    @Test
    public void testAnyInt() {
        int result = Matchers.anyInt();
        assertEquals(0, result);
    }

    @Test
    public void testAnyLong() {
        long result = Matchers.anyLong();
        assertEquals(0L, result);
    }

    @Test
    public void testAnyDouble() {
        double result = Matchers.anyDouble();
        assertEquals(0.0, result, 0.0);
    }

    @Test
    public void testAnyFloat() {
        float result = Matchers.anyFloat();
        assertEquals(0.0f, result, 0.0f);
    }

    @Test
    public void testAnyBoolean() {
        boolean result = Matchers.anyBoolean();
        assertFalse(result);
    }

    @Test
    public void testAnyChar() {
        char result = Matchers.anyChar();
        assertEquals('\0', result);
    }

    @Test
    public void testAnyByte() {
        byte result = Matchers.anyByte();
        assertEquals((byte) 0, result);
    }

    @Test
    public void testAnyShort() {
        short result = Matchers.anyShort();
        assertEquals((short) 0, result);
    }

    @Test
    public void testAnyObject() {
        Object result = Matchers.anyObject();
        assertNull(result);
    }

    @Test
    public void testAnyString() {
        String result = Matchers.anyString();
        assertNull(result);
    }

    @Test
    public void testAnyCollection() {
        java.util.Collection result = Matchers.anyCollection();
        assertNull(result);
    }

    @Test
    public void testAnyMap() {
        java.util.Map result = Matchers.anyMap();
        assertNull(result);
    }

    @Test
    public void testAnySet() {
        java.util.Set result = Matchers.anySet();
        assertNull(result);
    }

    @Test
    public void testAnyList() {
        java.util.List result = Matchers.anyList();
        assertNull(result);
    }

    @Test
    public void testAnyVararg() {
        Object result = Matchers.anyVararg();
        assertNull(result);
    }

    @Test
    public void testEq() {
        assertEquals(Integer.valueOf(5), Matchers.eq(5));
        assertEquals("test", Matchers.eq("test"));
        assertNull(Matchers.eq((Object) null));
    }
}