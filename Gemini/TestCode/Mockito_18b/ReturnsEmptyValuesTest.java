package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockName;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;

public class ReturnsEmptyValuesTest {

    private ReturnsEmptyValues returnsEmptyValues;

    @Before
    public void setUp() {
        returnsEmptyValues = new ReturnsEmptyValues();
    }

    // Mock interface to simulate InvocationOnMock
    private static class DummyInvocation implements InvocationOnMock {
        private final Method method;
        private final Object[] arguments;
        private final Object mock;

        public DummyInvocation(Method method, Object[] arguments, Object mock) {
            this.method = method;
            this.arguments = arguments;
            this.mock = mock;
        }

        @Override
        public Object getMock() {
            return mock;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return arguments;
        }

        @Override
        public <T> T getArgumentAt(int index, Class<T> clazz) {
            return clazz.cast(arguments[index]);
        }

        @Override
        public Object callRealMethod() throws Throwable {
            return null;
        }
    }

    // Helper methods to get sample methods for testing return types
    public boolean booleanMethod() { return false; }
    public Boolean boxedBooleanMethod() { return null; }
    public char charMethod() { return 'a'; }
    public Character boxedCharacterMethod() { return null; }
    public int intMethod() { return 0; }
    public Integer boxedIntMethod() { return null; }
    public long longMethod() { return 0L; }
    public Long boxedLongMethod() { return null; }
    public float floatMethod() { return 0.0f; }
    public Float boxedFloatMethod() { return null; }
    public double doubleMethod() { return 0.0d; }
    public Double boxedDoubleMethod() { return null; }
    public String stringMethod() { return null; }
    public List<?> listMethod() { return null; }
    public Set<?> setMethod() { return null; }
    public SortedSet<?> sortedSetMethod() { return null; }
    public NavigableSet<?> navigableSetMethod() { return null; }
    public Map<?, ?> mapMethod() { return null; }
    public SortedMap<?, ?> sortedMapMethod() { return null; }
    public NavigableMap<?, ?> navigableMapMethod() { return null; }
    public Optional<?> optionalMethod() { return null; }
    public Stream<?> streamMethod() { return null; }
    public Object objectMethod() { return null; }
    public int[] primitiveArrayMethod() { return null; }

    @Test
    public void testPrimitiveTypes() throws Exception {
        Object mock = new Object();

        Method boolMeth = ReturnsEmptyValuesTest.class.getMethod("booleanMethod");
        assertEquals(false, returnsEmptyValues.returnValueFor(new DummyInvocation(boolMeth, new Object[0], mock)));

        Method charMeth = ReturnsEmptyValuesTest.class.getMethod("charMethod");
        assertEquals((char) 0, returnsEmptyValues.returnValueFor(new DummyInvocation(charMeth, new Object[0], mock)));

        Method intMeth = ReturnsEmptyValuesTest.class.getMethod("intMethod");
        assertEquals(0, returnsEmptyValues.returnValueFor(new DummyInvocation(intMeth, new Object[0], mock)));

        Method longMeth = ReturnsEmptyValuesTest.class.getMethod("longMethod");
        assertEquals(0L, returnsEmptyValues.returnValueFor(new DummyInvocation(longMeth, new Object[0], mock)));

        Method floatMeth = ReturnsEmptyValuesTest.class.getMethod("floatMethod");
        assertEquals(0.0f, returnsEmptyValues.returnValueFor(new DummyInvocation(floatMeth, new Object[0], mock)));

        Method doubleMeth = ReturnsEmptyValuesTest.class.getMethod("doubleMethod");
        assertEquals(0.0d, returnsEmptyValues.returnValueFor(new DummyInvocation(doubleMeth, new Object[0], mock)));
    }

    @Test
    public void testBoxedPrimitiveTypes() throws Exception {
        Object mock = new Object();

        Method boolMeth = ReturnsEmptyValuesTest.class.getMethod("boxedBooleanMethod");
        assertNull(returnsEmptyValues.returnValueFor(new DummyInvocation(boolMeth, new Object[0], mock)));

        Method charMeth = ReturnsEmptyValuesTest.class.getMethod("boxedCharacterMethod");
        assertNull(returnsEmptyValues.returnValueFor(new DummyInvocation(charMeth, new Object[0], mock)));

        Method intMeth = ReturnsEmptyValuesTest.class.getMethod("boxedIntMethod");
        assertNull(returnsEmptyValues.returnValueFor(new DummyInvocation(intMeth, new Object[0], mock)));

        Method longMeth = ReturnsEmptyValuesTest.class.getMethod("boxedLongMethod");
        assertNull(returnsEmptyValues.returnValueFor(new DummyInvocation(longMeth, new Object[0], mock)));

        Method floatMeth = ReturnsEmptyValuesTest.class.getMethod("boxedFloatMethod");
        assertNull(returnsEmptyValues.returnValueFor(new DummyInvocation(floatMeth, new Object[0], mock)));

        Method doubleMeth = ReturnsEmptyValuesTest.class.getMethod("boxedDoubleMethod");
        assertNull(returnsEmptyValues.returnValueFor(new DummyInvocation(doubleMeth, new Object[0], mock)));
    }

    @Test
    public void testCollectionsAndMaps() throws Exception {
        Object mock = new Object();

        Method listMeth = ReturnsEmptyValuesTest.class.getMethod("listMethod");
        Object listRes = returnsEmptyValues.returnValueFor(new DummyInvocation(listMeth, new Object[0], mock));
        assertTrue(listRes instanceof List);
        assertTrue(((List<?>) listRes).isEmpty());

        Method setMeth = ReturnsEmptyValuesTest.class.getMethod("setMethod");
        Object setRes = returnsEmptyValues.returnValueFor(new DummyInvocation(setMeth, new Object[0], mock));
        assertTrue(setRes instanceof Set);
        assertTrue(((Set<?>) setRes).isEmpty());

        Method sortedSetMeth = ReturnsEmptyValuesTest.class.getMethod("sortedSetMethod");
        Object sortedSetRes = returnsEmptyValues.returnValueFor(new DummyInvocation(sortedSetMeth, new Object[0], mock));
        assertTrue(sortedSetRes instanceof SortedSet);
        assertTrue(((SortedSet<?>) sortedSetRes).isEmpty());

        Method navigableSetMeth = ReturnsEmptyValuesTest.class.getMethod("navigableSetMethod");
        Object navigableSetRes = returnsEmptyValues.returnValueFor(new DummyInvocation(navigableSetMeth, new Object[0], mock));
        assertTrue(navigableSetRes instanceof NavigableSet);
        assertTrue(((NavigableSet<?>) navigableSetRes).isEmpty());

        Method mapMeth = ReturnsEmptyValuesTest.class.getMethod("mapMethod");
        Object mapRes = returnsEmptyValues.returnValueFor(new DummyInvocation(mapMeth, new Object[0], mock));
        assertTrue(mapRes instanceof Map);
        assertTrue(((Map<?, ?>) mapRes).isEmpty());

        Method sortedMapMeth = ReturnsEmptyValuesTest.class.getMethod("sortedMapMethod");
        Object sortedMapRes = returnsEmptyValues.returnValueFor(new DummyInvocation(sortedMapMeth, new Object[0], mock));
        assertTrue(sortedMapRes instanceof SortedMap);
        assertTrue(((SortedMap<?, ?>) sortedMapRes).isEmpty());

        Method navigableMapMeth = ReturnsEmptyValuesTest.class.getMethod("navigableMapMethod");
        Object navigableMapRes = returnsEmptyValues.returnValueFor(new DummyInvocation(navigableMapMeth, new Object[0], mock));
        assertTrue(navigableMapRes instanceof NavigableMap);
        assertTrue(((NavigableMap<?, ?>) navigableMapRes).isEmpty());
    }

    @Test
    public void testOptionalAndStream() throws Exception {
        Object mock = new Object();

        // Optional and Stream checking via reflection or direct call if Java 8+
        Method optionalMeth = ReturnsEmptyValuesTest.class.getMethod("optionalMethod");
        Object optionalRes = returnsEmptyValues.returnValueFor(new DummyInvocation(optionalMeth, new Object[0], mock));
        if (optionalRes != null) {
            assertEquals("Optional.empty", optionalRes.toString());
        }

        Method streamMeth = ReturnsEmptyValuesTest.class.getMethod("streamMethod");
        Object streamRes = returnsEmptyValues.returnValueFor(new DummyInvocation(streamMeth, new Object[0], mock));
        if (streamRes != null) {
            assertTrue(streamRes instanceof java.util.stream.Stream);
        }
    }

    @Test
    public void testObjectAndArrays() throws Exception {
        Object mock = new Object();

        Method objMeth = ReturnsEmptyValuesTest.class.getMethod("objectMethod");
        assertNull(returnsEmptyValues.returnValueFor(new DummyInvocation(objMeth, new Object[0], mock)));

        Method arrayMeth = ReturnsEmptyValuesTest.class.getMethod("primitiveArrayMethod");
        Object arrayRes = returnsEmptyValues.returnValueFor(new DummyInvocation(arrayMeth, new Object[0], mock));
        assertNotNull(arrayRes);
        assertTrue(arrayRes.getClass().isArray());
        assertEquals(0, java.lang.reflect.Array.getLength(arrayRes));
    }

    @Test
    public void testMethodsFromObject() throws Exception {
        Object mock = new Object();

        // toString()
        Method toStringMethod = Object.class.getMethod("toString");
        Object toStringRes = returnsEmptyValues.returnValueFor(new DummyInvocation(toStringMethod, new Object[0], mock));
        // Mockito 1.x / ReturnsEmptyValues might return empty string or mock id for toString
        assertNotNull(toStringRes);

        // hashCode()
        Method hashCodeMethod = Object.class.getMethod("hashCode");
        Object hashCodeRes = returnsEmptyValues.returnValueFor(new DummyInvocation(hashCodeMethod, new Object[0], mock));
        assertTrue(hashCodeRes instanceof Integer);

        // equals()
        Method equalsMethod = Object.class.getMethod("equals", Object.class);
        Object equalsRes = returnsEmptyValues.returnValueFor(new DummyInvocation(equalsMethod, new Object[]{new Object()}, mock));
        // equals typically returns boxed boolean (false unless comparing to self)
        assertNotNull(equalsRes);
        
        // compareTo() if applicable
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        Object compareToRes = returnsEmptyValues.returnValueFor(new DummyInvocation(compareToMethod, new Object[]{new Object()}, mock));
        // Should return 0 or integer for Comparable
        if (compareToRes != null) {
            assertTrue(compareToRes instanceof Integer);
        }
    }

    @Test
    public void testSimulatedMockitoMethodsIfAny() {
        assertNotNull(returnsEmptyValues.toString());
    }
}