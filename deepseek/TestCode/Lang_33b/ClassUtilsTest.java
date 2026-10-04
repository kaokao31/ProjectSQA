package org.apache.commons.lang3;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ClassUtilsTest {

    // -----------------------------------------------------------------------
    // Tests for toClass (Defects4J Bug 33 – NullPointerException on null elements)
    // -----------------------------------------------------------------------

    @Test
    public void testToClass_NullArray() {
        assertNull(ClassUtils.toClass(null));
    }

    @Test
    public void testToClass_EmptyArray() {
        assertArrayEquals(new Class<?>[0], ClassUtils.toClass(new Object[0]));
    }

    @Test
    public void testToClass_NullElements() {
        Object[] array = new Object[] { null, "hello", null };
        Class<?>[] result = ClassUtils.toClass(array);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertNull(result[0]);
        assertEquals(String.class, result[1]);
        assertNull(result[2]);
    }

    @Test
    public void testToClass_AllNull() {
        Object[] array = new Object[] { null, null, null };
        Class<?>[] result = ClassUtils.toClass(array);
        assertNotNull(result);
        assertEquals(3, result.length);
        for (Class<?> c : result) {
            assertNull(c);
        }
    }

    @Test
    public void testToClass_WithPrimitiveWrapper() {
        Object[] array = new Object[] { Integer.valueOf(1), Double.valueOf(2.0) };
        Class<?>[] result = ClassUtils.toClass(array);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Integer.class, result[0]);
        assertEquals(Double.class, result[1]);
    }

    @Test
    public void testToClass_WithArray() {
        Object[] innerArray = new Object[] { 1, 2 };
        Object[] array = new Object[] { innerArray };
        Class<?>[] result = ClassUtils.toClass(array);
        assertEquals(Object[].class, result[0]);
    }

    // -----------------------------------------------------------------------
    // Tests for getShortClassName
    // -----------------------------------------------------------------------

    @Test
    public void testGetShortClassName_Object() {
        assertEquals("String", ClassUtils.getShortClassName(String.class));
        assertEquals("int", ClassUtils.getShortClassName(int.class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
    }

    @Test
    public void testGetShortClassName_String() {
        assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
    }

    // -----------------------------------------------------------------------
    // Tests for getPackageName
    // -----------------------------------------------------------------------

    @Test
    public void testGetPackageName_Object() {
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("", ClassUtils.getPackageName(int.class));
        assertEquals("", ClassUtils.getPackageName(int[].class));
    }

    @Test
    public void testGetPackageName_String() {
        assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
        assertEquals("", ClassUtils.getPackageName("int"));
    }

    // -----------------------------------------------------------------------
    // Tests for getAbbreviatedName
    // -----------------------------------------------------------------------

    @Test
    public void testGetAbbreviatedName_Class() {
        assertEquals("c.l.String", ClassUtils.getAbbreviatedName(String.class, 2));
        assertEquals("j.l.String", ClassUtils.getAbbreviatedName(String.class, 3));
    }

    // -----------------------------------------------------------------------
    // Tests for getAllSuperclasses and getAllInterfaces
    // -----------------------------------------------------------------------

    @Test
    public void testGetAllSuperclasses() {
        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(ArrayList.class);
        assertTrue(superclasses.contains(List.class));
        assertTrue(superclasses.contains(java.util.Collection.class));
        assertTrue(superclasses.contains(Object.class));
    }

    @Test
    public void testGetAllInterfaces() {
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        assertTrue(interfaces.contains(List.class));
        assertTrue(interfaces.contains(java.util.RandomAccess.class));
        assertTrue(interfaces.contains(java.lang.Cloneable.class));
    }

    // -----------------------------------------------------------------------
    // Tests for convertClassesToClassNames and convertClassNamesToClasses
    // -----------------------------------------------------------------------

    @Test
    public void testConvertClassesToClassNames() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(String.class);
        classes.add(null);
        classes.add(Integer.class);
        List<String> names = ClassUtils.convertClassesToClassNames(classes);
        assertEquals("java.lang.String", names.get(0));
        assertNull(names.get(1));
        assertEquals("java.lang.Integer", names.get(2));
    }

    @Test
    public void testConvertClassNamesToClasses() throws ClassNotFoundException {
        List<String> names = new ArrayList<>();
        names.add("java.lang.String");
        names.add(null);
        names.add("java.lang.Integer");
        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertEquals(String.class, classes.get(0));
        assertNull(classes.get(1));
        assertEquals(Integer.class, classes.get(2));
    }

    // -----------------------------------------------------------------------
    // Tests for isInnerClass
    // -----------------------------------------------------------------------

    @Test
    public void testIsInnerClass() {
        assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertFalse(ClassUtils.isInnerClass(int.class));
    }

    // -----------------------------------------------------------------------
    // Tests for getClass (by name)
    // -----------------------------------------------------------------------

    @Test
    public void testGetClassByNormalName() throws ClassNotFoundException {
        assertEquals(String.class, ClassUtils.getClass("java.lang.String"));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testGetClassByNormalName_NotFound() throws ClassNotFoundException {
        ClassUtils.getClass("non.existent.Class");
    }

}