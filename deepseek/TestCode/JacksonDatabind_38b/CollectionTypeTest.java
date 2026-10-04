package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.CollectionType;

public class CollectionTypeTest {

    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        typeFactory = TypeFactory.defaultInstance();
    }

    // Test construction with valid type parameters
    @Test
    public void testConstructWithValidType() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType collectionType = CollectionType.construct(elementType);
        assertNotNull(collectionType);
        assertEquals(Collection.class, collectionType.getRawClass());
        assertTrue(collectionType.isContainerType());
        assertTrue(collectionType.isCollectionLikeType());
        assertEquals(elementType, collectionType.getContentType());
    }

    // Test construction with null element type
    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithNullElementType() {
        CollectionType.construct(null);
    }

    // Test construction with a specific collection class
    @Test
    public void testConstructWithSpecificCollectionClass() {
        JavaType elementType = typeFactory.constructType(Integer.class);
        CollectionType collectionType = CollectionType.construct(List.class, elementType);
        assertNotNull(collectionType);
        assertEquals(List.class, collectionType.getRawClass());
        assertEquals(elementType, collectionType.getContentType());
    }

    // Test withTypeHandler
    @Test
    public void testWithTypeHandler() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        Object typeHandler = new Object();
        CollectionType modified = base.withTypeHandler(typeHandler);
        assertNotNull(modified);
        assertNotSame(base, modified);
        assertSame(typeHandler, modified.getTypeHandler());
        // Ensure content type is preserved
        assertEquals(elementType, modified.getContentType());
    }

    // Test withValueHandler
    @Test
    public void testWithValueHandler() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        Object valueHandler = new Object();
        CollectionType modified = base.withValueHandler(valueHandler);
        assertNotNull(modified);
        assertNotSame(base, modified);
        assertSame(valueHandler, modified.getValueHandler());
        assertEquals(elementType, modified.getContentType());
    }

    // Test withContentTypeHandler
    @Test
    public void testWithContentTypeHandler() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        Object contentTypeHandler = new Object();
        CollectionType modified = base.withContentTypeHandler(contentTypeHandler);
        assertNotNull(modified);
        assertNotSame(base, modified);
        assertSame(contentTypeHandler, modified.getContentTypeHandler());
        assertEquals(elementType, modified.getContentType());
    }

    // Test withContentValueHandler
    @Test
    public void testWithContentValueHandler() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        Object contentValueHandler = new Object();
        CollectionType modified = base.withContentValueHandler(contentValueHandler);
        assertNotNull(modified);
        assertNotSame(base, modified);
        assertSame(contentValueHandler, modified.getContentValueHandler());
        assertEquals(elementType, modified.getContentType());
    }

    // Test refine method with a subclass of the collection type
    @Test
    public void testRefineWithSubclass() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(Collection.class, elementType);
        // Refine to ArrayList
        JavaType refined = base.refine(ArrayList.class, typeFactory);
        assertNotNull(refined);
        assertTrue(refined instanceof CollectionType);
        assertEquals(ArrayList.class, refined.getRawClass());
        assertEquals(elementType, refined.getContentType());
    }

    // Test refine with same class (should return same instance)
    @Test
    public void testRefineWithSameClass() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(ArrayList.class, elementType);
        JavaType refined = base.refine(ArrayList.class, typeFactory);
        assertSame(base, refined);
    }

    // Test refine with null type factory
    @Test(expected = IllegalArgumentException.class)
    public void testRefineWithNullTypeFactory() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        base.refine(ArrayList.class, null);
    }

    // Test refine with null subclass
    @Test(expected = IllegalArgumentException.class)
    public void testRefineWithNullSubclass() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        base.refine(null, typeFactory);
    }

    // Test equals and hashCode
    @Test
    public void testEqualsAndHashCode() {
        JavaType elementType1 = typeFactory.constructType(String.class);
        JavaType elementType2 = typeFactory.constructType(Integer.class);
        CollectionType ct1 = CollectionType.construct(ArrayList.class, elementType1);
        CollectionType ct2 = CollectionType.construct(ArrayList.class, elementType1);
        CollectionType ct3 = CollectionType.construct(LinkedList.class, elementType1);
        CollectionType ct4 = CollectionType.construct(ArrayList.class, elementType2);

        assertEquals(ct1, ct2);
        assertEquals(ct1.hashCode(), ct2.hashCode());
        assertNotEquals(ct1, ct3);
        assertNotEquals(ct1, ct4);
        assertNotEquals(ct1, null);
        assertNotEquals(ct1, "some string");
    }

    // Test toString
    @Test
    public void testToString() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(ArrayList.class, elementType);
        String str = ct.toString();
        assertNotNull(str);
        assertTrue(str.contains("ArrayList"));
        assertTrue(str.contains("String"));
    }

    // Test isAbstract when collection class is abstract
    @Test
    public void testIsAbstract() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(Collection.class, elementType);
        assertTrue(ct.isAbstract());
    }

    // Test isAbstract when collection class is concrete
    @Test
    public void testIsNotAbstract() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(ArrayList.class, elementType);
        assertFalse(ct.isAbstract());
    }

    // Test that CollectionType is a container type
    @Test
    public void testIsContainerType() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(elementType);
        assertTrue(ct.isContainerType());
    }

    // Test that CollectionType is collection-like
    @Test
    public void testIsCollectionLikeType() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(elementType);
        assertTrue(ct.isCollectionLikeType());
    }

    // Test that CollectionType is not a map-like type
    @Test
    public void testIsNotMapLikeType() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(elementType);
        assertFalse(ct.isMapLikeType());
    }

    // Test narrowing with type parameters (potential bug area)
    @Test
    public void testNarrowContentsWithTypeParameters() {
        // Create a CollectionType with a generic element type
        JavaType elementType = typeFactory.constructType(new TypeReference<List<String>>() {}.getType());
        CollectionType ct = CollectionType.construct(ArrayList.class, elementType);
        // Ensure content type is correctly parameterized
        assertTrue(ct.getContentType() instanceof ParameterizedType);
        assertEquals(List.class, ct.getContentType().getRawClass());
    }

    // Test that construct with raw class and element type preserves raw class
    @Test
    public void testConstructPreservesRawClass() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(HashSet.class, elementType);
        assertEquals(HashSet.class, ct.getRawClass());
    }

    // Test that construct with only element type uses Collection as raw class
    @Test
    public void testConstructDefaultRawClass() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(elementType);
        assertEquals(Collection.class, ct.getRawClass());
    }

    // Test withTypeHandler with null handler
    @Test
    public void testWithTypeHandlerNull() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        CollectionType modified = base.withTypeHandler(null);
        assertSame(base, modified); // Should return same instance if handler is null
    }

    // Test withValueHandler with null handler
    @Test
    public void testWithValueHandlerNull() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        CollectionType modified = base.withValueHandler(null);
        assertSame(base, modified);
    }

    // Test withContentTypeHandler with null handler
    @Test
    public void testWithContentTypeHandlerNull() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        CollectionType modified = base.withContentTypeHandler(null);
        assertSame(base, modified);
    }

    // Test withContentValueHandler with null handler
    @Test
    public void testWithContentValueHandlerNull() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType base = CollectionType.construct(elementType);
        CollectionType modified = base.withContentValueHandler(null);
        assertSame(base, modified);
    }

    // Test that CollectionType is serializable (if applicable)
    @Test
    public void testSerialization() throws Exception {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(ArrayList.class, elementType);
        // Serialize and deserialize using Java serialization
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(ct);
        oos.close();
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        CollectionType deserialized = (CollectionType) ois.readObject();
        assertEquals(ct, deserialized);
    }

    // Test that CollectionType handles type narrowing correctly (potential bug)
    @Test
    public void testNarrowTypeWithDifferentElementType() {
        JavaType elementType = typeFactory.constructType(Number.class);
        CollectionType ct = CollectionType.construct(ArrayList.class, elementType);
        // Narrow to Integer element type
        JavaType narrowed = ct.narrowContentsBy(Integer.class);
        assertNotNull(narrowed);
        assertTrue(narrowed instanceof CollectionType);
        assertEquals(Integer.class, narrowed.getContentType().getRawClass());
    }

    // Test that CollectionType handles type widening correctly
    @Test
    public void testWidenTypeWithDifferentElementType() {
        JavaType elementType = typeFactory.constructType(Integer.class);
        CollectionType ct = CollectionType.construct(ArrayList.class, elementType);
        // Widen to Number element type
        JavaType widened = ct.widenContentsBy(Number.class);
        assertNotNull(widened);
        assertTrue(widened instanceof CollectionType);
        assertEquals(Number.class, widened.getContentType().getRawClass());
    }

    // Test that CollectionType forcesOutput() returns false by default
    @Test
    public void testForcesOutput() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(elementType);
        assertFalse(ct.forceOutput());
    }

    // Test that CollectionType is concrete when raw class is concrete
    @Test
    public void testIsConcrete() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(ArrayList.class, elementType);
        assertTrue(ct.isConcrete());
    }

    // Test that CollectionType is not concrete when raw class is abstract
    @Test
    public void testIsNotConcrete() {
        JavaType elementType = typeFactory.constructType(String.class);
        CollectionType ct = CollectionType.construct(Collection.class, elementType);
        assertFalse(ct.isConcrete());
    }
}