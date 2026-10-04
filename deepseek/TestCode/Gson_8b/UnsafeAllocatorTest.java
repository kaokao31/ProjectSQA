package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class UnsafeAllocatorTest {

    // Test with a simple class that has a no-arg constructor
    @Test
    public void testCreateWithNoArgConstructor() throws Exception {
        Object instance = UnsafeAllocator.create(String.class);
        assertNotNull(instance);
        assertTrue(instance instanceof String);
    }

    // Test with a class that has only a parameterized constructor
    @Test
    public void testCreateWithParameterizedConstructor() throws Exception {
        Object instance = UnsafeAllocator.create(Integer.class);
        assertNotNull(instance);
        assertTrue(instance instanceof Integer);
        assertEquals(0, ((Integer) instance).intValue());
    }

    // Test with an abstract class
    @Test(expected = InstantiationException.class)
    public void testCreateWithAbstractClass() throws Exception {
        UnsafeAllocator.create(Runnable.class);
    }

    // Test with an interface
    @Test(expected = InstantiationException.class)
    public void testCreateWithInterface() throws Exception {
        UnsafeAllocator.create(Cloneable.class);
    }

    // Test with a class that has a private constructor
    @Test
    public void testCreateWithPrivateConstructor() throws Exception {
        Object instance = UnsafeAllocator.create(PrivateConstructorClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof PrivateConstructorClass);
    }

    // Test with a class that throws an exception in constructor
    @Test
    public void testCreateWithExceptionInConstructor() throws Exception {
        Object instance = UnsafeAllocator.create(ExceptionInConstructorClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof ExceptionInConstructorClass);
    }

    // Test with a final class
    @Test
    public void testCreateWithFinalClass() throws Exception {
        Object instance = UnsafeAllocator.create(FinalClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof FinalClass);
    }

    // Test with a class that has a constructor with default access modifier
    @Test
    public void testCreateWithDefaultAccessConstructor() throws Exception {
        Object instance = UnsafeAllocator.create(DefaultAccessConstructorClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof DefaultAccessConstructorClass);
    }

    // Test with null input
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNullClass() throws Exception {
        UnsafeAllocator.create(null);
    }

    // Test with primitive type
    @Test(expected = InstantiationException.class)
    public void testCreateWithPrimitiveType() throws Exception {
        UnsafeAllocator.create(int.class);
    }

    // Test with array class
    @Test(expected = InstantiationException.class)
    public void testCreateWithArrayClass() throws Exception {
        UnsafeAllocator.create(int[].class);
    }

    // Test with void class
    @Test(expected = InstantiationException.class)
    public void testCreateWithVoidClass() throws Exception {
        UnsafeAllocator.create(void.class);
    }

    // Test with a class that has a protected constructor
    @Test
    public void testCreateWithProtectedConstructor() throws Exception {
        Object instance = UnsafeAllocator.create(ProtectedConstructorClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof ProtectedConstructorClass);
    }

    // Test with a class that has a constructor that throws a checked exception
    @Test
    public void testCreateWithCheckedExceptionConstructor() throws Exception {
        Object instance = UnsafeAllocator.create(CheckedExceptionConstructorClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof CheckedExceptionConstructorClass);
    }

    // Test with a class that has a static initializer that fails
    @Test
    public void testCreateWithFailingStaticInitializer() throws Exception {
        Object instance = UnsafeAllocator.create(FailingStaticInitializerClass.class);
        assertNotNull(instance);
        assertTrue(instance instanceof FailingStaticInitializerClass);
    }

    // Test with a class that has a constructor that invokes super with arguments
    @Test
    public void testCreateWithSuperConstructorArguments() throws Exception {
        Object instance = UnsafeAllocator.create(SubClassWithSuperConstructor.class);
        assertNotNull(instance);
        assertTrue(instance instanceof SubClassWithSuperConstructor);
    }

    // Helper classes for testing

    public static class PrivateConstructorClass {
        private PrivateConstructorClass() {
            // private constructor
        }
    }

    public static class ExceptionInConstructorClass {
        public ExceptionInConstructorClass() throws Exception {
            throw new Exception("Constructor failed");
        }
    }

    public static final class FinalClass {
        // final class with no explicit constructor
    }

    public static class DefaultAccessConstructorClass {
        DefaultAccessConstructorClass() {
            // default access constructor
        }
    }

    public static class ProtectedConstructorClass {
        protected ProtectedConstructorClass() {
            // protected constructor
        }
    }

    public static class CheckedExceptionConstructorClass {
        public CheckedExceptionConstructorClass() throws java.io.IOException {
            throw new java.io.IOException("IO Exception in constructor");
        }
    }

    public static class FailingStaticInitializerClass {
        static {
            if (true) {
                throw new RuntimeException("Static initializer failed");
            }
        }
    }

    public static class BaseClassWithConstructor {
        public BaseClassWithConstructor(String arg) {
            // base class with parameterized constructor
        }
    }

    public static class SubClassWithSuperConstructor extends BaseClassWithConstructor {
        public SubClassWithSuperConstructor() {
            super("test");
        }
    }
}