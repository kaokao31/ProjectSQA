package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AnnotationMapTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        String value() default "";
    }

    @TestAnnotation("foo")
    public void methodWithTestAnnotation() {}

    @TestAnnotation("bar")
    public void methodWithTestAnnotation2() {}

    @Deprecated
    public void methodWithDeprecated() {}

    private AnnotationMap map;
    private Annotation testAnn;
    private Annotation testAnn2;
    private Annotation deprecatedAnn;

    @Before
    public void setUp() throws Exception {
        map = new AnnotationMap();
        Method m1 = AnnotationMapTest.class.getMethod("methodWithTestAnnotation");
        testAnn = m1.getAnnotation(TestAnnotation.class);
        Method m2 = AnnotationMapTest.class.getMethod("methodWithTestAnnotation2");
        testAnn2 = m2.getAnnotation(TestAnnotation.class);
        Method m3 = AnnotationMapTest.class.getMethod("methodWithDeprecated");
        deprecatedAnn = m3.getAnnotation(Deprecated.class);
    }

    @Test
    public void testAddAndGet() {
        map.add(testAnn);
        assertSame(testAnn, map.get(TestAnnotation.class));
        assertEquals(1, map.size());
    }

    @Test
    public void testAddDuplicateSameInstance() {
        map.add(testAnn);
        map.add(testAnn);
        assertEquals(1, map.size());
        assertSame(testAnn, map.get(TestAnnotation.class));
    }

    @Test
    public void testAddDifferentInstanceReplaces() {
        map.add(testAnn);
        map.add(testAnn2);
        assertSame(testAnn2, map.get(TestAnnotation.class));
        assertEquals(1, map.size());
    }

    @Test
    public void testGetNonExistentReturnsNull() {
        assertNull(map.get(TestAnnotation.class));
    }

    @Test
    public void testSizeEmpty() {
        assertEquals(0, map.size());
    }

    @Test
    public void testAddMultipleTypes() {
        map.add(testAnn);
        map.add(deprecatedAnn);
        assertEquals(2, map.size());
        assertSame(testAnn, map.get(TestAnnotation.class));
        assertSame(deprecatedAnn, map.get(Deprecated.class));
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullAnnotation() {
        map.add(null);
    }

    @Test
    public void testEqualsWithSameContent() {
        AnnotationMap map1 = new AnnotationMap();
        map1.add(testAnn);
        AnnotationMap map2 = new AnnotationMap();
        map2.add(testAnn);
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test
    public void testNotEqualsDifferentContent() {
        AnnotationMap map1 = new AnnotationMap();
        map1.add(testAnn);
        AnnotationMap map2 = new AnnotationMap();
        map2.add(deprecatedAnn);
        assertFalse(map1.equals(map2));
    }

    @Test
    public void testEqualsWithEmptyMaps() {
        AnnotationMap map1 = new AnnotationMap();
        AnnotationMap map2 = new AnnotationMap();
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test
    public void testHashCodeConsistency() {
        AnnotationMap map1 = new AnnotationMap();
        map1.add(testAnn);
        AnnotationMap map2 = new AnnotationMap();
        map2.add(testAnn);
        assertEquals(map1.hashCode(), map2.hashCode());
    }

    @Test
    public void testConstructorWithMap() {
        Map<Class<? extends Annotation>, Annotation> initial = new HashMap<>();
        initial.put(TestAnnotation.class, testAnn);
        AnnotationMap mapWithMap = new AnnotationMap(initial);
        assertSame(testAnn, mapWithMap.get(TestAnnotation.class));
        assertEquals(1, mapWithMap.size());
    }
}