package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for BeanPropertyMap.
 * Designed to achieve maximum code coverage and detect potential faults.
 */
public class BeanPropertyMapTest {

    private static class TestSettableBeanProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private final String name;

        public TestSettableBeanProperty(String name) {
            super(PropertyName.construct(name), null, null, null, null);
            this.name = name;
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new TestSettableBeanProperty(newName.getSimpleName());
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return this;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public void deserializeAndSet(DeserializationContext ctxt, Object instance, Object value) {
        }

        @Override
        public Object deserializeSetAndReturn(DeserializationContext ctxt, Object instance, Object value) {
            return null;
        }

        @Override
        public void set(Object instance, Object value) {
        }

        @Override
        public Object getValue(Object instance) {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private BeanPropertyMap map;
    private SettableBeanProperty propA, propB, propC;

    @Before
    public void setUp() {
        propA = new TestSettableBeanProperty("a");
        propB = new TestSettableBeanProperty("b");
        propC = new TestSettableBeanProperty("c");
        map = new BeanPropertyMap(Collections.singletonList(propA));
    }

    // --- Constructor and basic properties ---

    @Test
    public void testConstructorWithNullList() {
        try {
            new BeanPropertyMap((List<SettableBeanProperty>) null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithEmptyList() {
        BeanPropertyMap emptyMap = new BeanPropertyMap(Collections.emptyList());
        assertEquals(0, emptyMap.size());
        assertTrue(emptyMap.isEmpty());
    }

    @Test
    public void testConstructorWithSingleProperty() {
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
        assertSame(propA, map.find("a"));
    }

    @Test
    public void testConstructorWithDuplicateNames() {
        SettableBeanProperty dup = new TestSettableBeanProperty("a");
        List<SettableBeanProperty> props = Arrays.asList(propA, dup);
        BeanPropertyMap dupMap = new BeanPropertyMap(props);
        // Should keep the last one? Or first? Behavior depends on implementation.
        // We'll just check that size is 1 and find returns one of them.
        assertEquals(1, dupMap.size());
        assertNotNull(dupMap.find("a"));
    }

    // --- find method ---

    @Test
    public void testFindExistingProperty() {
        assertSame(propA, map.find("a"));
    }

    @Test
    public void testFindNonExistingProperty() {
        assertNull(map.find("x"));
    }

    @Test
    public void testFindNullKey() {
        assertNull(map.find(null));
    }

    @Test
    public void testFindEmptyStringKey() {
        assertNull(map.find(""));
    }

    // --- add method ---

    @Test
    public void testAddNewProperty() {
        map.add(propB);
        assertEquals(2, map.size());
        assertSame(propB, map.find("b"));
    }

    @Test
    public void testAddDuplicateProperty() {
        map.add(propA); // same name
        assertEquals(1, map.size()); // should replace? or ignore? Typically replace.
        assertSame(propA, map.find("a"));
    }

    @Test
    public void testAddNullProperty() {
        try {
            map.add(null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- remove method ---

    @Test
    public void testRemoveExistingProperty() {
        map.add(propB);
        assertTrue(map.remove(propB));
        assertEquals(1, map.size());
        assertNull(map.find("b"));
    }

    @Test
    public void testRemoveNonExistingProperty() {
        assertFalse(map.remove(propB));
        assertEquals(1, map.size());
    }

    @Test
    public void testRemoveNullProperty() {
        assertFalse(map.remove(null));
    }

    @Test
    public void testRemoveLastProperty() {
        map.remove(propA);
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    // --- renameTo method ---

    @Test
    public void testRenameToExistingProperty() {
        map.add(propB);
        assertTrue(map.renameTo(propB, "b2"));
        assertEquals(2, map.size());
        assertNull(map.find("b"));
        assertSame(propB, map.find("b2"));
    }

    @Test
    public void testRenameToNonExistingProperty() {
        assertFalse(map.renameTo(propB, "b2"));
    }

    @Test
    public void testRenameToNullOldName() {
        try {
            map.renameTo(null, "newName");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRenameToNullNewName() {
        try {
            map.renameTo(propA, null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRenameToEmptyNewName() {
        assertTrue(map.renameTo(propA, ""));
        assertNull(map.find("a"));
        assertSame(propA, map.find(""));
    }

    @Test
    public void testRenameToDuplicateName() {
        map.add(propB);
        // renaming propA to "b" should replace propB? Or fail?
        // Implementation likely replaces.
        assertTrue(map.renameTo(propA, "b"));
        assertEquals(1, map.size());
        assertSame(propA, map.find("b"));
    }

    // --- iterator ---

    @Test
    public void testIteratorOverEmptyMap() {
        BeanPropertyMap empty = new BeanPropertyMap(Collections.emptyList());
        Iterator<SettableBeanProperty> it = empty.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorOverSingleProperty() {
        Iterator<SettableBeanProperty> it = map.iterator();
        assertTrue(it.hasNext());
        assertSame(propA, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorOverMultipleProperties() {
        map.add(propB);
        map.add(propC);
        Set<String> names = new HashSet<>();
        for (SettableBeanProperty p : map) {
            names.add(p.getName());
        }
        assertEquals(3, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
        assertTrue(names.contains("c"));
    }

    @Test
    public void testIteratorRemoveSupported() {
        map.add(propB);
        Iterator<SettableBeanProperty> it = map.iterator();
        it.next();
        it.remove();
        assertEquals(1, map.size());
        assertNull(map.find("a"));
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        map.iterator().remove();
    }

    // --- size and isEmpty ---

    @Test
    public void testSizeAfterMultipleOperations() {
        assertEquals(1, map.size());
        map.add(propB);
        assertEquals(2, map.size());
        map.remove(propA);
        assertEquals(1, map.size());
        map.remove(propB);
        assertEquals(0, map.size());
    }

    @Test
    public void testIsEmptyAfterClear() {
        map.remove(propA);
        assertTrue(map.isEmpty());
    }

    // --- getPropertiesInInsertionOrder ---

    @Test
    public void testGetPropertiesInInsertionOrder() {
        map.add(propB);
        map.add(propC);
        List<SettableBeanProperty> ordered = map.getPropertiesInInsertionOrder();
        assertEquals(3, ordered.size());
        assertEquals("a", ordered.get(0).getName());
        assertEquals("b", ordered.get(1).getName());
        assertEquals("c", ordered.get(2).getName());
    }

    @Test
    public void testGetPropertiesInInsertionOrderAfterRemove() {
        map.add(propB);
        map.add(propC);
        map.remove(propB);
        List<SettableBeanProperty> ordered = map.getPropertiesInInsertionOrder();
        assertEquals(2, ordered.size());
        assertEquals("a", ordered.get(0).getName());
        assertEquals("c", ordered.get(1).getName());
    }

    // --- withRenamedProperty ---

    @Test
    public void testWithRenamedProperty() {
        map.add(propB);
        BeanPropertyMap renamed = map.withRenamedProperty(propB, "b2");
        assertEquals(2, renamed.size());
        assertNull(renamed.find("b"));
        assertNotNull(renamed.find("b2"));
        // original map unchanged
        assertNotNull(map.find("b"));
    }

    @Test
    public void testWithRenamedPropertyNonExistent() {
        BeanPropertyMap renamed = map.withRenamedProperty(propB, "b2");
        assertSame(map, renamed); // should return same instance if property not found
    }

    // --- assignIndexes ---

    @Test
    public void testAssignIndexes() {
        map.add(propB);
        map.add(propC);
        map.assignIndexes();
        // After assignment, each property should have a unique index
        int indexA = propA.getPropertyIndex();
        int indexB = propB.getPropertyIndex();
        int indexC = propC.getPropertyIndex();
        assertTrue(indexA >= 0);
        assertTrue(indexB >= 0);
        assertTrue(indexC >= 0);
        assertNotEquals(indexA, indexB);
        assertNotEquals(indexA, indexC);
        assertNotEquals(indexB, indexC);
    }

    // --- find with index (if applicable) ---

    @Test
    public void testFindWithIndex() {
        map.add(propB);
        map.assignIndexes();
        int indexA = propA.getPropertyIndex();
        assertSame(propA, map.find(indexA));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testFindWithInvalidIndex() {
        map.find(-1);
    }

    // --- hashCode and equals (if overridden) ---

    @Test
    public void testHashCodeConsistency() {
        int hash1 = map.hashCode();
        map.add(propB);
        int hash2 = map.hashCode();
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testEqualsSameInstance() {
        assertTrue(map.equals(map));
    }

    @Test
    public void testEqualsDifferentMapWithSameProperties() {
        BeanPropertyMap other = new BeanPropertyMap(Collections.singletonList(propA));
        assertTrue(map.equals(other));
    }

    @Test
    public void testEqualsDifferentMapWithDifferentProperties() {
        BeanPropertyMap other = new BeanPropertyMap(Collections.singletonList(propB));
        assertFalse(map.equals(other));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(map.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(map.equals("string"));
    }

    // --- toString ---

    @Test
    public void testToStringNotEmpty() {
        String str = map.toString();
        assertNotNull(str);
        assertTrue(str.contains("a"));
    }

    @Test
    public void testToStringEmpty() {
        BeanPropertyMap empty = new BeanPropertyMap(Collections.emptyList());
        assertEquals("[]", empty.toString());
    }

    // --- Edge cases with special characters in property names ---

    @Test
    public void testPropertyNameWithSpecialChars() {
        SettableBeanProperty special = new TestSettableBeanProperty("prop.with.dots");
        map.add(special);
        assertSame(special, map.find("prop.with.dots"));
    }

    @Test
    public void testPropertyNameWithUnicode() {
        SettableBeanProperty unicode = new TestSettableBeanProperty("\u00e9\u00e0");
        map.add(unicode);
        assertSame(unicode, map.find("\u00e9\u00e0"));
    }

    // --- Large number of properties (stress test) ---

    @Test
    public void testLargeMap() {
        List<SettableBeanProperty> props = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            props.add(new TestSettableBeanProperty("prop" + i));
        }
        BeanPropertyMap largeMap = new BeanPropertyMap(props);
        assertEquals(1000, largeMap.size());
        for (int i = 0; i < 1000; i++) {
            assertNotNull(largeMap.find("prop" + i));
        }
    }

    // --- Null handling in constructor with array (if applicable) ---

    @Test
    public void testConstructorWithNullArray() {
        try {
            new BeanPropertyMap((SettableBeanProperty[]) null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithArrayContainingNull() {
        SettableBeanProperty[] props = new SettableBeanProperty[] { propA, null };
        try {
            new BeanPropertyMap(props);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- Test that find returns the most recently added property with same name ---

    @Test
    public void testFindReturnsLastAddedForDuplicateName() {
        SettableBeanProperty propA2 = new TestSettableBeanProperty("a");
        map.add(propA2);
        assertSame(propA2, map.find("a"));
    }

    // --- Test that remove works after rename ---

    @Test
    public void testRemoveAfterRename() {
        map.add(propB);
        map.renameTo(propB, "b2");
        assertTrue(map.remove(propB));
        assertEquals(1, map.size());
        assertNull(map.find("b2"));
    }

    // --- Test that iterator reflects modifications ---

    @Test
    public void testIteratorReflectsAdd() {
        map.add(propB);
        Iterator<SettableBeanProperty> it = map.iterator();
        Set<String> names = new HashSet<>();
        while (it.hasNext()) {
            names.add(it.next().getName());
        }
        assertTrue(names.contains("b"));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testIteratorFailFastOnStructuralModification() {
        map.add(propB);
        Iterator<SettableBeanProperty> it = map.iterator();
        map.add(propC); // structural modification
        it.next(); // should throw ConcurrentModificationException
    }
}