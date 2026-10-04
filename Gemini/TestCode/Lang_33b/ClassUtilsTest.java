package org.apache.commons.lang3;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ClassUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new ClassUtils());
        Constructor<?>[] cons = ClassUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(ClassUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(ClassUtils.class.getModifiers()));
    }

    @Test
    public void testToClass_object() {
        assertNull(ClassUtils.toClass(null));
        assertArrayEquals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass(new Object[0]));

        Object[] objects = new Object[] { "a", Integer.valueOf(1), null, Double.valueOf(2.0d) };
        Class<?>[] classes = ClassUtils.toClass(objects);
        assertNotNull(classes);
        assertEquals(4, classes.length);
        assertEquals(String.class, classes[0]);
        assertEquals(Integer.class, classes[1]);
        assertNull(classes[2]);
        assertEquals(Double.class, classes[3]);

        assertArrayEquals(new Class<?>[] { null }, ClassUtils.toClass(new Object[] { null }));
    }

    @Test
    public void testGetShortClassName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class));
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("int[][]", ClassUtils.getShortClassName(int[][].class));
    }

    @Test
    public void testGetShortClassName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class.getName()));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
        assertEquals("int[]", ClassUtils.getShortClassName("[I"));
        assertEquals("int[][]", ClassUtils.getShortClassName("[[I"));
        assertEquals("Inner", ClassUtils.getShortClassName("org.apache.commons.lang3.ClassUtilsTest$Inner"));
    }

    @Test
    public void testGetPackageName_Class() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(ClassUtils.class));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class));
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));
        assertEquals("", ClassUtils.getPackageName(int[].class));
    }

    @Test
    public void testGetPackageName_String() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(ClassUtils.class.getName()));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
        assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageName("[I"));
    }

    @Test
    public void testGetShortCanonicalName() {
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(ClassUtils.class));
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(ClassUtils.class.getName()));
        assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
        assertEquals("", ClassUtils.getShortCanonicalName((String) null));
        assertEquals("", ClassUtils.getShortCanonicalName(""));
        assertEquals("String[]", ClassUtils.getShortCanonicalName(String[].class));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(int[].class));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(int[][].class));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("[[I"));
    }

    @Test
    public void testGetPackageCanonicalName() {
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils.class));
        assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(ClassUtils.class.getName()));
        assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
        assertEquals("", ClassUtils.getPackageCanonicalName((String) null));
        assertEquals("", ClassUtils.getPackageCanonicalName(""));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName(String[].class));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[].class));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
    }

    @Test
    public void testGetAllSuperclasses() {
        assertNull(ClassUtils.getAllSuperclasses(null));
        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(ArrayList.class);
        assertTrue(superclasses.contains(java.util.AbstractList.class));
        assertTrue(superclasses.contains(java.util.AbstractCollection.class));
        assertTrue(superclasses.contains(Object.class));
    }

    @Test
    public void testGetAllInterfaces() {
        assertNull(ClassUtils.getAllInterfaces(null));
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        assertTrue(interfaces.contains(java.util.List.class));
        assertTrue(interfaces.contains(java.util.RandomAccess.class));
        assertTrue(interfaces.contains(Cloneable.class));
        assertTrue(interfaces.contains(java.io.Serializable.class));
    }

    @Test
    public void testConvertClassNamesToClasses() {
        assertNull(ClassUtils.convertClassNamesToClasses(null));
        List<String> names = new ArrayList<String>();
        names.add("java.lang.String");
        names.add("java.lang.Integer");
        names.add("non.existent.ClassName");

        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertEquals(3, classes.size());
        assertEquals(String.class, classes.get(0));
        assertEquals(Integer.class, classes.get(1));
        assertNull(classes.get(2));
    }

    @Test
    public void testConvertClassesToClassNames() {
        assertNull(ClassUtils.convertClassesToClassNames(null));
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(String.class);
        classes.add(Integer.class);
        classes.add(null);

        List<String> names = ClassUtils.convertClassesToClassNames(classes);
        assertEquals(3, names.size());
        assertEquals("java.lang.String", names.get(0));
        assertEquals("java.lang.Integer", names.get(1));
        assertNull(names.get(2));
    }

    @Test
    public void testIsAssignable_Autoboxing() {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, null));
        assertFalse(ClassUtils.isAssignable(String.class, null));
        assertTrue(ClassUtils.isAssignable(null, String.class));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));

        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));

        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Long.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.class, true));
        assertFalse(ClassUtils.isAssignable(Integer.class, Long.class, true));

        assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
    }

    @Test
    public void testIsAssignable_ClassArrays() {
        assertFalse(ClassUtils.isAssignable(new Class<?>[] { String.class }, new Class<?>[] { String.class, Integer.class }));
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable(new Class<?>[0], (Class<?>) null));
        assertTrue(ClassUtils.isAssignable(new Class<?>[] { String.class }, new Class<?>[] { Object.class }));
        assertFalse(ClassUtils.isAssignable(new Class<?>[] { Object.class }, new Class<?>[] { String.class }));
    }

    @Test
    public void testPrimitiveToWrapper() {
        assertNull(ClassUtils.primitiveToWrapper(null));
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));
        assertEquals(Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        assertEquals(Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        assertEquals(Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        assertEquals(Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        assertEquals(Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        assertEquals(Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));

        assertNull(ClassUtils.primitivesToWrappers((Class<?>[]) null));
        assertArrayEquals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.primitivesToWrappers(ArrayUtils.EMPTY_CLASS_ARRAY));
        Class<?>[] primitives = new Class<?>[] { Integer.TYPE, String.class, null };
        Class<?>[] wrappers = ClassUtils.primitivesToWrappers(primitives);
        assertArrayEquals(new Class<?>[] { Integer.class, String.class, null }, wrappers);
    }

    @Test
    public void testWrapperToPrimitive() {
        assertNull(ClassUtils.wrapperToPrimitive(null));
        assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        assertEquals(Boolean.TYPE, ClassUtils.wrapperToPrimitive(Boolean.class));
        assertEquals(Byte.TYPE, ClassUtils.wrapperToPrimitive(Byte.class));
        assertEquals(Character.TYPE, ClassUtils.wrapperToPrimitive(Character.class));
        assertEquals(Short.TYPE, ClassUtils.wrapperToPrimitive(Short.class));
        assertEquals(Long.TYPE, ClassUtils.wrapperToPrimitive(Long.class));
        assertEquals(Float.TYPE, ClassUtils.wrapperToPrimitive(Float.class));
        assertEquals(Double.TYPE, ClassUtils.wrapperToPrimitive(Double.class));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));

        assertNull(ClassUtils.wrappersToPrimitives((Class<?>[]) null));
        assertArrayEquals(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.wrappersToPrimitives(ArrayUtils.EMPTY_CLASS_ARRAY));
        Class<?>[] wrappers = new Class<?>[] { Integer.class, String.class, null };
        Class<?>[] primitives = ClassUtils.wrappersToPrimitives(wrappers);
        assertArrayEquals(new Class<?>[] { Integer.TYPE, null, null }, primitives);
    }

    @Test
    public void testIsInnerClass() {
        assertFalse(ClassUtils.isInnerClass(null));
        assertFalse(ClassUtils.isInnerClass(ClassUtils.class));
        assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
        assertTrue(ClassUtils.isInnerClass(Inner.class));
    }

    @Test
    public void testGetClass() throws ClassNotFoundException {
        assertSame(String.class, ClassUtils.getClass("java.lang.String"));
        assertSame(String[].class, ClassUtils.getClass("java.lang.String[]"));
        assertSame(int.class, ClassUtils.getClass("int"));
        assertSame(int[].class, ClassUtils.getClass("int[]"));
        assertSame(int[][].class, ClassUtils.getClass("int[][]"));
        assertSame(Map.Entry.class, ClassUtils.getClass("java.util.Map$Entry"));
        assertSame(Map.Entry.class, ClassUtils.getClass("java.util.Map.Entry"));

        try {
            ClassUtils.getClass("non.existing.ClassName");
            fail("Expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testGetPublicMethod() throws Exception {
        assertNotNull(ClassUtils.getPublicMethod(String.class, "indexOf", new Class<?>[] { String.class }));
        try {
            ClassUtils.getPublicMethod(String.class, "nonExistingMethod", new Class<?>[0]);
            fail("Expected NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            // expected
        }
    }

    private static class Inner {
    }
}