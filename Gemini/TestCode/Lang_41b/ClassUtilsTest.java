package org.apache.commons.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link org.apache.commons.lang.ClassUtils}.
 */
public class ClassUtilsTest {

    private static class InnerChild extends ClassUtilsTest {
    }

    private interface InnerInterface {
    }

    private static class InnerImplementor implements InnerInterface {
    }

    // -------------------------------------------------------------------------
    // Constructor tests
    // -------------------------------------------------------------------------

    @Test
    public void testConstructor() {
        assertNotNull(new ClassUtils());
        Constructor<?>[] cons = ClassUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertTrue(Modifier.isPublic(cons[0].getModifiers()));
        assertTrue(Modifier.isPublic(ClassUtils.class.getModifiers()));
        assertFalse(Modifier.isFinal(ClassUtils.class.getModifiers()));
    }

    // -------------------------------------------------------------------------
    // getShortClassName(Class) & getShortClassName(Object, String) & getShortClassName(String)
    // -------------------------------------------------------------------------

    @Test
    public void test_getShortClassName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class));
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));

        // Array classes
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("int[][]", ClassUtils.getShortClassName(int[][].class));
        assertEquals("String[][]", ClassUtils.getShortClassName(String[][].class));
        assertEquals("ClassUtilsTest.InnerChild[]", ClassUtils.getShortClassName(InnerChild[].class));
    }

    @Test
    public void test_getShortClassName_Object() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(new ClassUtils(), "null"));
        assertEquals("null", ClassUtils.getShortClassName(null, "null"));
        assertEquals("String[]", ClassUtils.getShortClassName(new String[0], "null"));
        assertEquals("int[]", ClassUtils.getShortClassName(new int[0], "null"));
    }

    @Test
    public void test_getShortClassName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class.getName()));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));

        // Inner classes
        assertEquals("ClassUtilsTest.InnerChild", ClassUtils.getShortClassName(InnerChild.class.getName()));
        assertEquals("ClassUtilsTest.InnerChild", ClassUtils.getShortClassName("org.apache.commons.lang.ClassUtilsTest$InnerChild"));

        // Array string representations
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
        assertEquals("int[]", ClassUtils.getShortClassName("[I"));
        assertEquals("int[][]", ClassUtils.getShortClassName("[[I"));
        assertEquals("String[][]", ClassUtils.getShortClassName("[[Ljava.lang.String;"));
        assertEquals("InnerChild[]", ClassUtils.getShortClassName("[Lorg.apache.commons.lang.ClassUtilsTest$InnerChild;"));

        assertEquals("boolean", ClassUtils.getShortClassName("Z"));
        assertEquals("byte", ClassUtils.getShortClassName("B"));
        assertEquals("char", ClassUtils.getShortClassName("C"));
        assertEquals("short", ClassUtils.getShortClassName("S"));
        assertEquals("int", ClassUtils.getShortClassName("I"));
        assertEquals("long", ClassUtils.getShortClassName("J"));
        assertEquals("float", ClassUtils.getShortClassName("F"));
        assertEquals("double", ClassUtils.getShortClassName("D"));
    }

    // -------------------------------------------------------------------------
    // getPackageName(Class) & getPackageName(Object, String) & getPackageName(String)
    // -------------------------------------------------------------------------

    @Test
    public void test_getPackageName_Class() {
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(ClassUtils.class));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class));
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));

        // Array classes
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));
        assertEquals("java.lang", ClassUtils.getPackageName(String[][].class));
        assertEquals("", ClassUtils.getPackageName(int[].class));
        assertEquals("", ClassUtils.getPackageName(int[][].class));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(InnerChild[].class));
    }

    @Test
    public void test_getPackageName_Object() {
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(new ClassUtils(), "null"));
        assertEquals("null", ClassUtils.getPackageName(null, "null"));
        assertEquals("java.lang", ClassUtils.getPackageName(new String[0], "null"));
        assertEquals("", ClassUtils.getPackageName(new int[0], "null"));
    }

    @Test
    public void test_getPackageName_String() {
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(ClassUtils.class.getName()));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
        assertEquals("", ClassUtils.getPackageName("NoPackage"));

        // Array string representations
        assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
        assertEquals("java.lang", ClassUtils.getPackageName("[[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageName("[I"));
        assertEquals("", ClassUtils.getPackageName("[[I"));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName("Lorg.apache.commons.lang.ClassUtilsTest;"));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName("[Lorg.apache.commons.lang.ClassUtilsTest;"));
    }

    // -------------------------------------------------------------------------
    // getAllSuperclasses & getAllInterfaces
    // -------------------------------------------------------------------------

    @Test
    public void test_getAllSuperclasses_Class() {
        List<Class<?>> list = ClassUtils.getAllSuperclasses(InnerChild.class);
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(ClassUtilsTest.class, list.get(0));
        assertEquals(Object.class, list.get(1));

        assertNull(ClassUtils.getAllSuperclasses(null));
        assertEquals(0, ClassUtils.getAllSuperclasses(Object.class).size());
    }

    @Test
    public void test_getAllInterfaces_Class() {
        List<Class<?>> list = ClassUtils.getAllInterfaces(InnerImplementor.class);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(InnerInterface.class, list.get(0));

        assertNull(ClassUtils.getAllInterfaces(null));
        assertEquals(0, ClassUtils.getAllInterfaces(Object.class).size());
    }

    // -------------------------------------------------------------------------
    // convertClassNamesToClasses & convertClassesToClassNames
    // -------------------------------------------------------------------------

    @Test
    public void test_convertClassNamesToClasses() {
        List<String> list = new ArrayList<String>();
        list.add("java.lang.String");
        list.add("java.lang.Integer");
        list.add("non.existent.Class");

        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(list);
        assertNotNull(classes);
        assertEquals(3, classes.size());
        assertEquals(String.class, classes.get(0));
        assertEquals(Integer.class, classes.get(1));
        assertNull(classes.get(2));

        assertNull(ClassUtils.convertClassNamesToClasses(null));
    }

    @Test
    public void test_convertClassesToClassNames() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(String.class);
        list.add(Integer.class);
        list.add(null);

        List<String> names = ClassUtils.convertClassesToClassNames(list);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertEquals("java.lang.String", names.get(0));
        assertEquals("java.lang.Integer", names.get(1));
        assertNull(names.get(2));

        assertNull(ClassUtils.convertClassesToClassNames(null));
    }

    // -------------------------------------------------------------------------
    // isAssignable
    // -------------------------------------------------------------------------

    @Test
    public void test_isAssignable_ClassArray_ClassArray() {
        Class<?>[] array1 = new Class<?>[] { Object.class, String.class };
        Class<?>[] array2 = new Class<?>[] { Object.class, Object.class };
        Class<?>[] array3 = new Class<?>[] { String.class };

        assertTrue(ClassUtils.isAssignable(array1, array2));
        assertFalse(ClassUtils.isAssignable(array2, array1));
        assertFalse(ClassUtils.isAssignable(array1, array3));
        assertFalse(ClassUtils.isAssignable(array3, array1));

        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable(new Class<?>[0], (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, new Class<?>[0]));
        assertTrue(ClassUtils.isAssignable(new Class<?>[0], new Class<?>[0]));
    }

    @Test
    public void test_isAssignable_Class_Class() {
        assertTrue(ClassUtils.isAssignable(String.class, Object.class));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class));
        assertFalse(ClassUtils.isAssignable(String.class, null));
        assertFalse(ClassUtils.isAssignable(null, int.class));
        assertTrue(ClassUtils.isAssignable(null, String.class));
        assertTrue(ClassUtils.isAssignable(String.class, String.class));

        // Autoboxing / Widening
        assertTrue(ClassUtils.isAssignable(int.class, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, int.class, true));
        assertTrue(ClassUtils.isAssignable(int.class, long.class, true));
        assertTrue(ClassUtils.isAssignable(int.class, double.class, true));
        assertTrue(ClassUtils.isAssignable(byte.class, short.class, true));
        assertTrue(ClassUtils.isAssignable(byte.class, int.class, true));
        assertTrue(ClassUtils.isAssignable(short.class, int.class, true));
        assertTrue(ClassUtils.isAssignable(char.class, int.class, true));
        assertTrue(ClassUtils.isAssignable(float.class, double.class, true));

        assertFalse(ClassUtils.isAssignable(int.class, short.class, true));
        assertFalse(ClassUtils.isAssignable(boolean.class, int.class, true));
        assertFalse(ClassUtils.isAssignable(int.class, Integer.class, false));
    }

    // -------------------------------------------------------------------------
    // Primitive and Wrapper conversions
    // -------------------------------------------------------------------------

    @Test
    public void test_primitiveToWrapper() {
        assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(boolean.class));
        assertEquals(Byte.class, ClassUtils.primitiveToWrapper(byte.class));
        assertEquals(Character.class, ClassUtils.primitiveToWrapper(char.class));
        assertEquals(Short.class, ClassUtils.primitiveToWrapper(short.class));
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(int.class));
        assertEquals(Long.class, ClassUtils.primitiveToWrapper(long.class));
        assertEquals(Float.class, ClassUtils.primitiveToWrapper(float.class));
        assertEquals(Double.class, ClassUtils.primitiveToWrapper(double.class));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(void.class));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));
        assertNull(ClassUtils.primitiveToWrapper(null));
    }

    @Test
    public void test_primitivesToWrappers() {
        Class<?>[] primitives = new Class<?>[] { int.class, boolean.class, null, String.class };
        Class<?>[] wrappers = ClassUtils.primitivesToWrappers(primitives);
        assertNotNull(wrappers);
        assertEquals(4, wrappers.length);
        assertEquals(Integer.class, wrappers[0]);
        assertEquals(Boolean.class, wrappers[1]);
        assertNull(wrappers[2]);
        assertEquals(String.class, wrappers[3]);

        assertNull(ClassUtils.primitivesToWrappers(null));
        assertEquals(0, ClassUtils.primitivesToWrappers(new Class<?>[0]).length);
    }

    @Test
    public void test_wrapperToPrimitive() {
        assertEquals(boolean.class, ClassUtils.wrapperToPrimitive(Boolean.class));
        assertEquals(byte.class, ClassUtils.wrapperToPrimitive(Byte.class));
        assertEquals(char.class, ClassUtils.wrapperToPrimitive(Character.class));
        assertEquals(short.class, ClassUtils.wrapperToPrimitive(Short.class));
        assertEquals(int.class, ClassUtils.wrapperToPrimitive(Integer.class));
        assertEquals(long.class, ClassUtils.wrapperToPrimitive(Long.class));
        assertEquals(float.class, ClassUtils.wrapperToPrimitive(Float.class));
        assertEquals(double.class, ClassUtils.wrapperToPrimitive(Double.class));
        assertEquals(void.class, ClassUtils.wrapperToPrimitive(Void.TYPE));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
        assertNull(ClassUtils.wrapperToPrimitive(null));
    }

    @Test
    public void test_wrappersToPrimitives() {
        Class<?>[] wrappers = new Class<?>[] { Integer.class, Boolean.class, null, String.class };
        Class<?>[] primitives = ClassUtils.wrappersToPrimitives(wrappers);
        assertNotNull(primitives);
        assertEquals(4, primitives.length);
        assertEquals(int.class, primitives[0]);
        assertEquals(boolean.class, primitives[1]);
        assertNull(primitives[2]);
        assertNull(primitives[3]);

        assertNull(ClassUtils.wrappersToPrimitives(null));
        assertEquals(0, ClassUtils.wrappersToPrimitives(new Class<?>[0]).length);
    }

    // -------------------------------------------------------------------------
    // isInnerClass
    // -------------------------------------------------------------------------

    @Test
    public void test_isInnerClass() {
        assertTrue(ClassUtils.isInnerClass(InnerChild.class));
        assertTrue(ClassUtils.isInnerClass(InnerInterface.class));
        assertFalse(ClassUtils.isInnerClass(ClassUtilsTest.class));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertFalse(ClassUtils.isInnerClass(null));
    }

    // -------------------------------------------------------------------------
    // getClass / getPublicMethod / toClass
    // -------------------------------------------------------------------------

    @Test
    public void test_getClass() throws ClassNotFoundException {
        assertEquals(String.class, ClassUtils.getClass("java.lang.String"));
        assertEquals(int.class, ClassUtils.getClass("int"));
        assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        assertEquals(int[].class, ClassUtils.getClass("int[]"));
        assertEquals(int[][].class, ClassUtils.getClass("int[][]"));

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        assertEquals(String.class, ClassUtils.getClass(cl, "java.lang.String"));
        assertEquals(int.class, ClassUtils.getClass(cl, "int", true));
        assertEquals(String[].class, ClassUtils.getClass(cl, "java.lang.String[]", true));

        try {
            ClassUtils.getClass("non.existent.Class");
            fail("Expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    @Test
    public void test_getPublicMethod() throws Exception {
        Method method = ClassUtils.getPublicMethod(String.class, "indexOf", new Class<?>[] { String.class });
        assertNotNull(method);
        assertEquals("indexOf", method.getName());

        try {
            ClassUtils.getPublicMethod(String.class, "nonExistentMethod", new Class<?>[0]);
            fail("Expected NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            // expected
        }
    }

    @Test
    public void test_toClass() {
        Object[] array = new Object[] { "string", Integer.valueOf(1), null };
        Class<?>[] classes = ClassUtils.toClass(array);
        assertNotNull(classes);
        assertEquals(3, classes.length);
        assertEquals(String.class, classes[0]);
        assertEquals(Integer.class, classes[1]);
        assertNull(classes[2]);

        assertNull(ClassUtils.toClass(null));
        assertEquals(0, ClassUtils.toClass(new Object[0]).length);
    }
}