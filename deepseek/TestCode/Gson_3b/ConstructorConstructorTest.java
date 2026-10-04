package com.google.gson.internal;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConstructorConstructorTest {

    private ConstructorConstructor newConstructorConstructor() {
        return new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
    }

    static class ClassWithDefaultConstructor {
        public ClassWithDefaultConstructor() {}
    }

    static class ClassWithPrivateConstructor {
        private ClassWithPrivateConstructor() {}
    }

    static class ClassWithoutDefaultConstructor {
        final int value;
        public ClassWithoutDefaultConstructor(int value) {
            this.value = value;
        }
    }

    static class ClassWithThrowingConstructor {
        public ClassWithThrowingConstructor() {
            throw new IllegalStateException("boom");
        }
    }

    static class GenericClass<T> {
        public GenericClass() {}
    }

    static class CustomType {
        final String value;
        CustomType(String value) {
            this.value = value;
        }
    }

    interface TestInterface {}

    abstract static class AbstractClassWithoutNoArgConstructor {
        public AbstractClassWithoutNoArgConstructor(String value) {}
    }

    @Test
    public void testPublicDefaultConstructor() {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor<ClassWithDefaultConstructor> oc =
                cc.get(TypeToken.get(ClassWithDefaultConstructor.class));
        ClassWithDefaultConstructor instance = oc.construct();
        assertNotNull(instance);
        assertSame(ClassWithDefaultConstructor.class, instance.getClass());
    }

    @Test
    public void testPrivateDefaultConstructorIsMadeAccessible() {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor<ClassWithPrivateConstructor> oc =
                cc.get(TypeToken.get(ClassWithPrivateConstructor.class));
        assertNotNull(oc.construct());
    }

    @Test
    public void testClassWithoutDefaultConstructorUsesUnsafeAllocator() {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor<ClassWithoutDefaultConstructor> oc =
                cc.get(TypeToken.get(ClassWithoutDefaultConstructor.class));
        ClassWithoutDefaultConstructor instance = oc.construct();
        assertNotNull(instance);
        assertEquals(0, instance.value);
    }

    @Test
    public void testConstructorExceptionIsWrapped() {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor<ClassWithThrowingConstructor> oc =
                cc.get(TypeToken.get(ClassWithThrowingConstructor.class));
        try {
            oc.construct();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    @Test
    public void testInstanceCreatorForExactType() {
        TypeToken<CustomType> typeToken = TypeToken.get(CustomType.class);
        Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
        creators.put(CustomType.class, new InstanceCreator<CustomType>() {
            @Override
            public CustomType createInstance(Type type) {
                assertEquals(CustomType.class, type);
                return new CustomType("created");
            }
        });
        ConstructorConstructor cc = new ConstructorConstructor(creators);
        ObjectConstructor<CustomType> oc = cc.get(typeToken);
        assertEquals("created", oc.construct().value);
    }

    @Test
    public void testInstanceCreatorForRawTypeUsesRawClass() {
        final TypeToken<GenericClass<String>> typeToken =
                new TypeToken<GenericClass<String>>() {};
        Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
        creators.put(GenericClass.class, new InstanceCreator<GenericClass>() {
            @Override
            public GenericClass createInstance(Type type) {
                assertEquals(GenericClass.class, type);
                return new GenericClass();
            }
        });
        ConstructorConstructor cc = new ConstructorConstructor(creators);
        ObjectConstructor<GenericClass<String>> oc = cc.get(typeToken);
        assertNotNull(oc.construct());
    }

    @Test
    public void testCollectionInterfaces() {
        checkConstructedType(Collection.class, ArrayList.class);
        checkConstructedType(List.class, ArrayList.class);
        checkConstructedType(Set.class, LinkedHashSet.class);
        checkConstructedType(SortedSet.class, TreeSet.class);
        checkConstructedType(Queue.class, ArrayDeque.class);
    }

    @Test
    public void testMapInterfaces() {
        checkConstructedType(Map.class, LinkedHashMap.class);
        checkConstructedType(SortedMap.class, TreeMap.class);
        checkConstructedType(ConcurrentMap.class, ConcurrentHashMap.class);
        checkConstructedType(ConcurrentNavigableMap.class, ConcurrentSkipListMap.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void checkConstructedType(Class<?> rawType, Class<?> expectedImplementation) {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor oc = cc.get(TypeToken.get((Class) rawType));
        Object instance = oc.construct();
        assertNotNull(instance);
        assertTrue(rawType.isInstance(instance));
        assertTrue(expectedImplementation.isInstance(instance));
    }

    @Test(expected = JsonIOException.class)
    public void testEnumSetThrowsJsonIOException() {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor oc = cc.get(TypeToken.get(EnumSet.class));
        oc.construct();
    }

    @Test(expected = RuntimeException.class)
    public void testInterfaceConstructionFails() {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor<TestInterface> oc = cc.get(TypeToken.get(TestInterface.class));
        oc.construct();
    }

    @Test
    public void testAbstractClassWithoutNoArgConstructorUsesUnsafeAllocator() {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor<AbstractClassWithoutNoArgConstructor> oc =
                cc.get(TypeToken.get(AbstractClassWithoutNoArgConstructor.class));
        assertNotNull(oc.construct());
    }

    @Test
    public void testConstructReturnsNewInstances() {
        ConstructorConstructor cc = newConstructorConstructor();
        ObjectConstructor<ClassWithDefaultConstructor> oc =
                cc.get(TypeToken.get(ClassWithDefaultConstructor.class));
        assertNotSame(oc.construct(), oc.construct());
    }

    @Test
    public void testToStringReturnsNonNull() {
        ConstructorConstructor cc = newConstructorConstructor();
        assertNotNull(cc.toString());
    }
}