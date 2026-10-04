package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CollectionTypeTest {

    @Test
    public void testConstructionAndWithContentType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(java.util.List.class);
        JavaType elemType1 = tf.constructType(String.class);
        JavaType elemType2 = tf.constructType(Integer.class);

        // Construct CollectionType using standard constructor or factory method
        CollectionType collType = CollectionType.construct(baseType.getRawClass(), elemType1);
        assertNotNull(collType);
        assertEquals(elemType1, collType.getContentType());

        // Test withContentType
        CollectionType modified = collType.withContentType(elemType2);
        assertNotNull(modified);
        assertEquals(elemType2, modified.getContentType());
        
        // Same content type should return this
        assertSame(modified, modified.withContentType(elemType2));
    }

    @Test
    public void testWithStaticTyping() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(java.util.List.class);
        JavaType elemType = tf.constructType(Object.class);

        CollectionType collType = CollectionType.construct(baseType.getRawClass(), elemType);
        
        CollectionType staticTyped = collType.withStaticTyping();
        assertNotNull(staticTyped);
        assertTrue(staticTyped.useStaticType());

        // Calling again or with same should handle gracefully
        assertSame(staticTyped, staticTyped.withStaticTyping());
    }

    @Test
    public void testWithValueHandlerAndTypeHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(java.util.List.class);
        JavaType elemType = tf.constructType(String.class);

        CollectionType collType = CollectionType.construct(baseType.getRawClass(), elemType);

        Object valueHandler = new Object();
        CollectionType withValue = (CollectionType) collType.withValueHandler(valueHandler);
        assertSame(valueHandler, withValue.getValueHandler());

        Object typeHandler = new Object();
        CollectionType withType = (CollectionType) collType.withTypeHandler(typeHandler);
        assertSame(typeHandler, withType.getTypeHandler());
    }

    @Test
    public void testContainedTypeMethods() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(java.util.List.class);
        JavaType elemType = tf.constructType(String.class);

        CollectionType collType = CollectionType.construct(baseType.getRawClass(), elemType);

        assertEquals(1, collType.containedTypeCount());
        assertEquals(elemType, collType.containedType(0));
        assertNull(collType.containedType(1));
        assertNull(collType.containedType(-1));

        assertEquals("E", collType.containedTypeName(0));
        assertNull(collType.containedTypeName(1));
        assertNull(collType.containedTypeName(-1));
    }

    @Test
    public void testToStringAndEquals() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(java.util.List.class);
        JavaType elemType = tf.constructType(String.class);

        CollectionType collType1 = CollectionType.construct(baseType.getRawClass(), elemType);
        CollectionType collType2 = CollectionType.construct(baseType.getRawClass(), elemType);
        JavaType otherType = tf.constructType(String.class);

        assertTrue(collType1.equals(collType2));
        assertFalse(collType1.equals(otherType));
        assertFalse(collType1.equals(null));

        String desc = collType1.toString();
        assertNotNull(desc);
        assertTrue(desc.length() > 0);
    }
}