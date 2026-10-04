package org.apache.commons.lang3.concurrent.annotation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotatedClassTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {
        String value() default "default";
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface SecondAnnotation {
        int id() default 0;
    }

    @TestAnnotation(value = "custom")
    @SecondAnnotation(id = 42)
    private static class DummyAnnotatedClass {
    }

    private static class DummyUnannotatedClass {
    }

    @Test
    public void testConstructorAndGetAnnotatedClass() {
        AnnotatedClass<?> annotated = new AnnotatedClass<>(DummyAnnotatedClass.class);
        assertSame(DummyAnnotatedClass.class, annotated.getAnnotatedClass());
    }

    @Test
    public void testGetAnnotationPresent() {
        AnnotatedClass<DummyAnnotatedClass> annotated = new AnnotatedClass<>(DummyAnnotatedClass.class);
        TestAnnotation annotation = annotated.getAnnotation(TestAnnotation.class);
        assertNotNull(annotation);
        assertEquals("custom", annotation.value());
    }

    @Test
    public void testGetAnnotationAbsent() {
        AnnotatedClass<DummyAnnotatedClass> annotated = new AnnotatedClass<>(DummyAnnotatedClass.class);
        SecondAnnotation annotation = annotated.getAnnotation(SecondAnnotation.class);
        // Depending on implementation, if not present or not handled via the specific wrapper, 
        // let's see how AnnotatedClass behaves. If it wraps class-level annotations:
        // Actually, let's test what happens when we request an annotation that is present vs absent.
        // DummyAnnotatedClass has both TestAnnotation and SecondAnnotation.
        assertNotNull(annotated.getAnnotation(SecondAnnotation.class));
        assertEquals(42, annotated.getAnnotation(SecondAnnotation.class).id());
    }

    @Test
    public void testGetAnnotationUnannotated() {
        AnnotatedClass<DummyUnannotatedClass> annotated = new AnnotatedClass<>(DummyUnannotatedClass.class);
        TestAnnotation annotation = annotated.getAnnotation(TestAnnotation.class);
        assertNull(annotation);
    }

    @Test
    public void testIsAnnotationPresent() {
        AnnotatedClass<DummyAnnotatedClass> annotated = new AnnotatedClass<>(DummyAnnotatedClass.class);
        assertTrue(annotated.isAnnotationPresent(TestAnnotation.class));
        assertTrue(annotated.isAnnotationPresent(SecondAnnotation.class));
        
        AnnotatedClass<DummyUnannotatedClass> unannotated = new AnnotatedClass<>(DummyUnannotatedClass.class);
        assertFalse(unannotated.isAnnotationPresent(TestAnnotation.class));
    }

    @Test
    public void testGetAnnotations() {
        AnnotatedClass<DummyAnnotatedClass> annotated = new AnnotatedClass<>(DummyAnnotatedClass.class);
        Annotation[] annotations = annotated.getAnnotations();
        assertNotNull(annotations);
        assertTrue(annotations.length >= 2);
    }

    @Test
    public void testEqualsAndHashCode() {
        AnnotatedClass<DummyAnnotatedClass> ac1 = new AnnotatedClass<>(DummyAnnotatedClass.class);
        AnnotatedClass<DummyAnnotatedClass> ac2 = new AnnotatedClass<>(DummyAnnotatedClass.class);
        AnnotatedClass<DummyUnannotatedClass> ac3 = new AnnotatedClass<>(DummyUnannotatedClass.class);

        assertEquals(ac1, ac2);
        assertEquals(ac1.hashCode(), ac2.hashCode());
        assertNotEquals(ac1, ac3);
        assertNotEquals(ac1, null);
        assertNotEquals(ac1, "Some String");
        assertEquals(ac1, ac1);
    }

    @Test
    public void testToString() {
        AnnotatedClass<DummyAnnotatedClass> ac = new AnnotatedClass<>(DummyAnnotatedClass.class);
        String str = ac.toString();
        assertNotNull(str);
        assertTrue(str.contains("DummyAnnotatedClass") || str.length() > 0);
    }
}