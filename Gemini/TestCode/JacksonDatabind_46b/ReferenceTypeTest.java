package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReferenceTypeTest {

    private TypeFactory typeFactory;
    private ReferenceType stringRefType;
    private JavaType referencedType;
    private JavaType bindings;
    private JavaType superClass;
    private Object valueHandler;
    private Object typeHandler;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
        referencedType = typeFactory.constructType(String.class);
        bindings = TypeBindings.emptyBindings();
        superClass = typeFactory.constructType(Object.class);
        valueHandler = new Object();
        typeHandler = new Object();

        stringRefType = ReferenceType.construct(
                String.class,
                (TypeBindings) bindings,
                superClass,
                null,
                referencedType
        );
    }

    @Test
    public void testConstructWithClass() {
        ReferenceType refType = ReferenceType.construct(String.class, referencedType);
        assertNotNull(refType);
        assertEquals(String.class, refType.getRawClass());
        assertEquals(referencedType, refType.getReferencedType());
        assertTrue(refType.isReferenceType());
    }

    @Test
    public void testConstructWithFullParameters() {
        ReferenceType refType = ReferenceType.construct(
                String.class,
                (TypeBindings) bindings,
                superClass,
                new JavaType[]{superClass},
                referencedType
        );
        assertNotNull(refType);
        assertEquals(String.class, refType.getRawClass());
        assertEquals(referencedType, refType.getReferencedType());
    }

    @Test
    public void testWithReferencedType() {
        JavaType newReferenced = typeFactory.constructType(Integer.class);
        ReferenceType modified = stringRefType.withReferencedType(newReferenced);
        assertNotSame(stringRefType, modified);
        assertEquals(newReferenced, modified.getReferencedType());
        
        // Test same referenced type returns this
        ReferenceType same = stringRefType.withReferencedType(referencedType);
        assertSame(stringRefType, same);
    }

    @Test
    public void testWithContentType() {
        JavaType newContent = typeFactory.constructType(Integer.class);
        JavaType modified = stringRefType.withContentType(newContent);
        assertTrue(modified instanceof ReferenceType);
        assertEquals(newContent, ((ReferenceType) modified).getReferencedType());
    }

    @Test
    public void testWithBindings() {
        TypeBindings newBindings = TypeBindings.create(String.class, new JavaType[0]);
        ReferenceType modified = (ReferenceType) stringRefType.withBindings(newBindings);
        assertNotNull(modified);
        assertEquals(newBindings, modified.getBindings());
    }

    @Test
    public void testWithStaticTyping() {
        ReferenceType modified = (ReferenceType) stringRefType.withStaticTyping();
        assertTrue(modified.useStaticType());

        ReferenceType unmodified = (ReferenceType) modified.withStaticTyping();
        assertSame(modified, unmodified);
    }

    @Test
    public void testWithContentTypeHandler() {
        ReferenceType modified = (ReferenceType) stringRefType.withContentTypeHandler(typeHandler);
        assertNotNull(modified);
        assertEquals(typeHandler, modified.getContentTypeHandler());
    }

    @Test
    public void testWithContentValueHandler() {
        ReferenceType modified = (ReferenceType) stringRefType.withContentValueHandler(valueHandler);
        assertNotNull(modified);
        assertEquals(valueHandler, modified.getContentValueHandler());
    }

    @Test
    public void testWithHandlers() {
        ReferenceType modified = (ReferenceType) stringRefType.withTypeHandler(typeHandler);
        modified = (ReferenceType) modified.withValueHandler(valueHandler);
        assertNotNull(modified);
        assertEquals(typeHandler, modified.getTypeHandler());
        assertEquals(valueHandler, modified.getValueHandler());
    }

    @Test
    public void testGetContentType() {
        assertEquals(referencedType, stringRefType.getContentType());
    }

    @Test
    public void testIsContainerType() {
        assertFalse(stringRefType.isContainerType());
    }

    @Test
    public void testGetSuperClass() {
        assertEquals(superClass, stringRefType.getSuperClass());
    }

    @Test
    public void testToCanonical() {
        String canonical = stringRefType.toCanonical();
        assertNotNull(canonical);
        assertTrue(canonical.contains(String.class.getName()));
    }

    @Test
    public void testEqualsAndHashCode() {
        ReferenceType sameRefType = ReferenceType.construct(
                String.class,
                (TypeBindings) bindings,
                superClass,
                null,
                referencedType
        );

        assertTrue(stringRefType.equals(stringRefType));
        assertTrue(stringRefType.equals(sameRefType));
        assertEquals(stringRefType.hashCode(), sameRefType.hashCode());

        assertFalse(stringRefType.equals(null));
        assertFalse(stringRefType.equals("some string"));

        ReferenceType diffReferenced = ReferenceType.construct(
                String.class,
                (TypeBindings) bindings,
                superClass,
                null,
                typeFactory.constructType(Integer.class)
        );
        assertFalse(stringRefType.equals(diffReferenced));

        ReferenceType diffClass = ReferenceType.construct(
                Object.class,
                (TypeBindings) bindings,
                superClass,
                null,
                referencedType
        );
        assertFalse(stringRefType.equals(diffClass));
    }

    @Test
    public void testToString() {
        String str = stringRefType.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("[reference-type"));
    }
}