package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.internal.util.reflection.GenericMetadataSupport;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class GenericMetadataSupportTest {

    // Helper interfaces/classes for reflection tests
    private interface SimpleInterface<T> {
        T execute();
    }

    private interface ExtendsInterface extends SimpleInterface<String> {
    }

    private static class ConcreteClass implements ExtendsInterface {
        @Override
        public String execute() {
            return "test";
        }
    }

    private static class GenericClass<T, U extends List<T>> {
        U getList() { return null; }
    }

    private static class SubGenericClass extends GenericClass<String, List<String>> {
    }

    private interface NestedGenericInterface<T> {
        Map<String, List<T>> getNested();
    }

    @Test
    public void testInferFrom() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(ConcreteClass.class);
        assertNotNull(support);
    }

    @Test
    public void testResolveParameterizedType() throws Exception {
        Method method = GenericClass.class.getMethod("getList");
        Type genericReturnType = method.getGenericReturnType();

        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(SubGenericClass.class);
        GenericMetadataSupport resolved = support.resolveGenericType(genericReturnType);
        assertNotNull(resolved);
    }

    @Test
    public void testTypeVariableResolution() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(ConcreteClass.class);
        assertNotNull(support);
    }

    @Test(expected = NullPointerException.class)
    public void testInferFromNull() {
        GenericMetadataSupport.inferFrom(null);
    }

    @Test
    public void testBoundedType() {
        class BoundedClass<E extends Comparable<E>> {
            E element;
        }
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(BoundedClass.class);
        assertNotNull(support);
    }

    @Test
    public void testRawType() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(List.class);
        assertNotNull(support);
    }
}