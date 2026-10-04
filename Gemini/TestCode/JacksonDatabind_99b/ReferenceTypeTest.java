package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReferenceTypeTest {

    private TypeFactory getTypeFactory() {
        return TypeFactory.defaultInstance();
    }

    @Test
    public void testConstructionAndBasicGetters() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType = tf.constructType(String.class);
        JavaType anchorType = tf.constructType(java.util.List.class);

        ReferenceType refType = ReferenceType.construct(anchorType.getRawClass(), anchorType, null, null, referencedType);

        assertSame(referencedType, refType.getReferencedType());
        assertSame(referencedType, refType.getContentType());
        assertSame(anchorType, refType.getAnchorType());
        assertTrue(refType.isReferenceType());
        assertFalse(refType.isContainerType());
        assertEquals(1, refType.containedTypeCount());
        assertSame(referencedType, refType.containedType(0));
        assertNull(refType.containedType(1));
        assertEquals("String", refType.containedTypeName(0));
        assertNull(refType.containedTypeName(1));
    }

    @Test
    public void testWithContentType() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType1 = tf.constructType(String.class);
        JavaType referencedType2 = tf.constructType(Integer.class);
        JavaType anchorType = tf.constructType(java.util.List.class);

        ReferenceType refType = ReferenceType.construct(anchorType.getRawClass(), anchorType, null, null, referencedType1);
        ReferenceType newRefType = (ReferenceType) refType.withContentType(referencedType2);

        assertSame(referencedType2, newRefType.getReferencedType());
        assertSame(anchorType, newRefType.getAnchorType());
    }

    @Test
    public void testWithContentTypeHandler() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType = tf.constructType(String.class);
        JavaType anchorType = tf.constructType(java.util.List.class);

        ReferenceType refType = ReferenceType.construct(anchorType.getRawClass(), anchorType, null, null, referencedType);
        Object h1 = new Object();
        ReferenceType updated = (ReferenceType) refType.withContentTypeHandler(h1);

        assertSame(h1, updated.getContentTypeHandler());
        assertSame(h1, updated.getReferencedType().getTypeHandler());
    }

    @Test
    public void testWithTypeHandler() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType = tf.constructType(String.class);
        JavaType anchorType = tf.constructType(java.util.List.class);

        ReferenceType refType = ReferenceType.construct(anchorType.getRawClass(), anchorType, null, null, referencedType);
        Object h1 = new Object();
        ReferenceType updated = (ReferenceType) refType.withTypeHandler(h1);

        assertSame(h1, updated.getTypeHandler());
    }

    @Test
    public void testWithStaticTyping() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType = tf.constructType(String.class);
        JavaType anchorType = tf.constructType(java.util.List.class);

        ReferenceType refType = ReferenceType.construct(anchorType.getRawClass(), anchorType, null, null, referencedType);
        ReferenceType updated = (ReferenceType) refType.withStaticTyping();

        assertTrue(updated.useStaticType());
    }

    @Test
    public void testRefine() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType = tf.constructType(String.class);
        JavaType anchorType = tf.constructType(java.util.List.class);

        ReferenceType refType = ReferenceType.construct(anchorType.getRawClass(), anchorType, null, null, referencedType);

        JavaType refined = refType.refine(anchorType.getRawClass(), TypeBindings.emptyBindings(), anchorType, new JavaType[]{referencedType});
        assertNotNull(refined);
        assertTrue(refined instanceof ReferenceType);
        assertEquals(anchorType.getRawClass(), refined.getRawClass());
    }

    @Test
    public void testBuildCanonicalName() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType = tf.constructType(String.class);
        JavaType anchorType = tf.constructType(java.util.concurrent.atomic.AtomicReference.class);

        ReferenceType refType = ReferenceType.construct(anchorType.getRawClass(), anchorType, null, null, referencedType);
        String canonical = refType.toCanonical();
        assertNotNull(canonical);
        assertTrue(canonical.contains(anchorType.getRawClass().getName()));
        assertTrue(canonical.contains(String.class.getName()));
    }

    @Test
    public void testEqualsAndHashCode() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType1 = tf.constructType(String.class);
        JavaType referencedType2 = tf.constructType(String.class);
        JavaType referencedType3 = tf.constructType(Integer.class);
        JavaType anchorType1 = tf.constructType(java.util.concurrent.atomic.AtomicReference.class);
        JavaType anchorType2 = tf.constructType(java.util.concurrent.atomic.AtomicReference.class);

        ReferenceType ref1 = ReferenceType.construct(anchorType1.getRawClass(), anchorType1, null, null, referencedType1);
        ReferenceType ref2 = ReferenceType.construct(anchorType2.getRawClass(), anchorType2, null, null, referencedType2);
        ReferenceType ref3 = ReferenceType.construct(anchorType1.getRawClass(), anchorType1, null, null, referencedType3);

        assertEquals(ref1, ref2);
        assertEquals(ref1.hashCode(), ref2.hashCode());
        assertNotEquals(ref1, ref3);
        assertNotEquals(ref1, null);
        assertNotEquals(ref1, "some string");
    }

    @Test
    public void testToString() {
        TypeFactory tf = getTypeFactory();
        JavaType referencedType = tf.constructType(String.class);
        JavaType anchorType = tf.constructType(java.util.concurrent.atomic.AtomicReference.class);

        ReferenceType refType = ReferenceType.construct(anchorType.getRawClass(), anchorType, null, null, referencedType);
        String str = refType.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("[reference type"));
    }
}