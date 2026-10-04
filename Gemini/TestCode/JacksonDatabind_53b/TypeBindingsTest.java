package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;

public class TypeBindingsTest {

    @Test
    public void testEmptyBindings() {
        TypeBindings tb = TypeBindings.emptyBindings();
        assertNotNull(tb);
        assertEquals(0, tb.size());
        assertNull(tb.findBoundName(0));
        assertNull(tb.findBoundName(-1));
        assertNull(tb.getBoundName(0));
        assertNull(tb.getBoundType(0));
        assertEquals(Collections.emptyList(), tb.getTypeParameters());
        assertNotNull(tb.toString());
    }

    @Test
    public void testCreateWithSingleParam() {
        Class<?> clazz = String.class;
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        TypeBindings tb = TypeBindings.create(clazz, type);
        assertNotNull(tb);
        assertEquals(1, tb.size());
        assertEquals(type, tb.getBoundType(0));
    }

    @Test
    public void testCreateWithPair() {
        Class<?> clazz = java.util.Map.class;
        JavaType keyType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType valueType = TypeFactory.defaultInstance().constructType(Integer.class);
        TypeBindings tb = TypeBindings.create(clazz, keyType, valueType);
        assertNotNull(tb);
        assertEquals(2, tb.size());
        assertEquals(keyType, tb.getBoundType(0));
        assertEquals(valueType, tb.getBoundType(1));
    }

    @Test
    public void testCreateWithArray() {
        Class<?> clazz = java.util.Map.class;
        JavaType keyType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType valueType = TypeFactory.defaultInstance().constructType(Integer.class);
        TypeBindings tb = TypeBindings.create(clazz, new JavaType[] { keyType, valueType });
        assertNotNull(tb);
        assertEquals(2, tb.size());
        assertEquals(keyType, tb.getBoundType(0));
        assertEquals(valueType, tb.getBoundType(1));
    }

    @Test
    public void testCreateWithList() {
        Class<?> clazz = java.util.Map.class;
        JavaType keyType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType valueType = TypeFactory.defaultInstance().constructType(Integer.class);
        List<JavaType> typeList = java.util.Arrays.asList(keyType, valueType);
        TypeBindings tb = TypeBindings.create(clazz, typeList);
        assertNotNull(tb);
        assertEquals(2, tb.size());
        assertEquals(keyType, tb.getBoundType(0));
        assertEquals(valueType, tb.getBoundType(1));
    }

    @Test
    public void testCreateUnbound() {
        TypeBindings tb = TypeBindings.createUnbound(String.class);
        assertNotNull(tb);
    }

    @Test
    public void testAsKey() {
        Class<?> clazz = String.class;
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        TypeBindings tb = TypeBindings.create(clazz, type);
        Object key1 = tb.asKey(clazz);
        Object key2 = tb.asKey(clazz);
        assertNotNull(key1);
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertTrue(key1.equals(key2));
        assertFalse(key1.equals("other"));
        assertFalse(key1.equals(null));
    }

    @Test
    public void testEqualsAndHashCode() {
        JavaType type1 = TypeFactory.defaultInstance().constructType(Integer.class);
        JavaType type2 = TypeFactory.defaultInstance().constructType(Integer.class);
        JavaType type3 = TypeFactory.defaultInstance().constructType(String.class);

        TypeBindings tb1 = TypeBindings.create(String.class, type1);
        TypeBindings tb2 = TypeBindings.create(String.class, type2);
        TypeBindings tb3 = TypeBindings.create(String.class, type3);
        TypeBindings empty1 = TypeBindings.emptyBindings();
        TypeBindings empty2 = TypeBindings.emptyBindings();

        assertEquals(tb1, tb2);
        assertEquals(tb1.hashCode(), tb2.hashCode());
        assertEquals(empty1, empty2);

        assertNotEquals(tb1, tb3);
        assertNotEquals(tb1, empty1);
        assertNotEquals(tb1, null);
        assertNotEquals(tb1, "some string");
    }

    @Test
    public void testWithUnboundVariable() {
        TypeBindings tb = TypeBindings.emptyBindings();
        TypeVariable<?>[] vars = java.util.Map.class.getTypeParameters();
        if (vars.length > 0) {
            JavaType resolved = tb.findBoundType(vars[0].getName());
            assertNull(resolved);
        }
    }
}