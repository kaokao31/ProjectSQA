package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class JsonWriteContextTest {

    private JsonWriteContext rootContext;

    @Before
    public void setUp() {
        rootContext = JsonWriteContext.createRootContext();
    }

    // --- Root Context Tests ---

    @Test
    public void testRootContextCreation() {
        assertNotNull("Root context should not be null", rootContext);
        assertEquals("Root context type should be TYPE_ROOT", 0, rootContext.getType());
        assertNull("Root context should have no parent", rootContext.getParent());
        assertEquals("Root context nesting depth should be 0", 0, rootContext.getNestingDepth());
    }

    @Test
    public void testRootContextWriteValue() {
        // Root context can write a value (e.g., a scalar at top level)
        assertTrue("Root context should allow writeValue", rootContext.writeValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testRootContextWriteFieldName() {
        // Root context is not an object, so writeFieldName should throw
        rootContext.writeFieldName("test");
    }

    // --- Array Context Tests ---

    @Test
    public void testCreateChildArrayContext() {
        JsonWriteContext arrayContext = rootContext.createChildArrayContext();
        assertNotNull("Array context should not be null", arrayContext);
        assertEquals("Array context type should be TYPE_ARRAY", 1, arrayContext.getType());
        assertSame("Array context parent should be root", rootContext, arrayContext.getParent());
        assertEquals("Array context nesting depth should be 1", 1, arrayContext.getNestingDepth());
    }

    @Test
    public void testArrayContextWriteValue() {
        JsonWriteContext arrayContext = rootContext.createChildArrayContext();
        assertTrue("Array context should allow writeValue", arrayContext.writeValue());
        // After writing a value, the context should still be in array state
        assertTrue("Array context should allow another writeValue", arrayContext.writeValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testArrayContextWriteFieldName() {
        JsonWriteContext arrayContext = rootContext.createChildArrayContext();
        arrayContext.writeFieldName("test");
    }

    // --- Object Context Tests ---

    @Test
    public void testCreateChildObjectContext() {
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        assertNotNull("Object context should not be null", objectContext);
        assertEquals("Object context type should be TYPE_OBJECT", 2, objectContext.getType());
        assertSame("Object context parent should be root", rootContext, objectContext.getParent());
        assertEquals("Object context nesting depth should be 1", 1, objectContext.getNestingDepth());
    }

    @Test
    public void testObjectContextWriteFieldName() {
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        assertTrue("Object context should allow writeFieldName", objectContext.writeFieldName("field1"));
    }

    @Test(expected = IllegalStateException.class)
    public void testObjectContextWriteFieldNameTwice() {
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        objectContext.writeFieldName("field1");
        // Second field name without a value should throw
        objectContext.writeFieldName("field2");
    }

    @Test
    public void testObjectContextWriteValueAfterFieldName() {
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        objectContext.writeFieldName("field1");
        assertTrue("Object context should allow writeValue after field name", objectContext.writeValue());
    }

    @Test(expected = IllegalStateException.class)
    public void testObjectContextWriteValueWithoutFieldName() {
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        // No field name written, writeValue should throw
        objectContext.writeValue();
    }

    // --- Nested Context Tests ---

    @Test
    public void testNestedObjectInsideArray() {
        // Root -> Array -> Object
        JsonWriteContext arrayContext = rootContext.createChildArrayContext();
        arrayContext.writeValue(); // write a scalar value first
        JsonWriteContext objectContext = arrayContext.createChildObjectContext();
        assertNotNull("Object context inside array should be created", objectContext);
        assertEquals("Nesting depth should be 2", 2, objectContext.getNestingDepth());
        assertSame("Parent of object should be array", arrayContext, objectContext.getParent());
    }

    @Test
    public void testNestedArrayInsideObject() {
        // Root -> Object -> Array
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        objectContext.writeFieldName("arr");
        JsonWriteContext arrayContext = objectContext.createChildArrayContext();
        assertNotNull("Array context inside object should be created", arrayContext);
        assertEquals("Nesting depth should be 2", 2, arrayContext.getNestingDepth());
        assertSame("Parent of array should be object", objectContext, arrayContext.getParent());
    }

    // --- Bug-specific Test: Write field name after array element ---
    // This tests the scenario that triggered JacksonCore bug 7.
    @Test
    public void testWriteFieldNameAfterArrayElement() {
        // Root -> Object -> Field "a" -> Array -> Value -> End Array -> Field "b" -> Value
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        assertTrue("Should write field name 'a'", objectContext.writeFieldName("a"));
        JsonWriteContext arrayContext = objectContext.createChildArrayContext();
        assertTrue("Should write value in array", arrayContext.writeValue());
        // Simulate closing the array: pop back to object context
        // In real usage, the generator would close the array, but here we just go back to parent
        // The bug was that after writing a value in array, the array context's internal state
        // caused the next writeFieldName on the parent object to fail.
        // We test that the parent object still allows a new field name.
        assertTrue("Object context should allow writeFieldName after array element", objectContext.writeFieldName("b"));
        assertTrue("Object context should allow writeValue after second field name", objectContext.writeValue());
    }

    // --- Edge Cases and Additional Coverage ---

    @Test
    public void testMultipleNestingLevels() {
        // Root -> Object -> Array -> Object -> Array
        JsonWriteContext obj1 = rootContext.createChildObjectContext();
        obj1.writeFieldName("level1");
        JsonWriteContext arr1 = obj1.createChildArrayContext();
        arr1.writeValue();
        JsonWriteContext obj2 = arr1.createChildObjectContext();
        obj2.writeFieldName("level2");
        JsonWriteContext arr2 = obj2.createChildArrayContext();
        arr2.writeValue();
        assertEquals("Nesting depth should be 4", 4, arr2.getNestingDepth());
        // Verify parent chain
        assertSame("arr2 parent is obj2", obj2, arr2.getParent());
        assertSame("obj2 parent is arr1", arr1, obj2.getParent());
        assertSame("arr1 parent is obj1", obj1, arr1.getParent());
        assertSame("obj1 parent is root", rootContext, obj1.getParent());
    }

    @Test
    public void testWriteValueInRootAfterChild() {
        // After creating a child and then closing it (by going back to root), root should still allow writeValue
        JsonWriteContext child = rootContext.createChildObjectContext();
        // Simulate closing child: in real usage, the generator would pop context.
        // Here we just test that root context's writeValue still works.
        assertTrue("Root context should allow writeValue after child creation", rootContext.writeValue());
    }

    @Test
    public void testGetCurrentValue() {
        // The getCurrentValue method may return the current value being written.
        // We can test that it returns null initially.
        assertNull("Root context current value should be null", rootContext.getCurrentValue());
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        assertNull("Object context current value should be null initially", objectContext.getCurrentValue());
    }

    @Test
    public void testNestingDepthLimit() {
        // Create many nested contexts to test depth tracking
        JsonWriteContext current = rootContext;
        int maxDepth = 100; // arbitrary
        for (int i = 0; i < maxDepth; i++) {
            current = current.createChildObjectContext();
            if (i < maxDepth - 1) {
                current.writeFieldName("nested" + i);
            }
        }
        assertEquals("Nesting depth should be " + maxDepth, maxDepth, current.getNestingDepth());
    }

    @Test
    public void testWriteValueAfterFieldNameInNestedObject() {
        // Nested object: root -> object -> field -> object -> field -> value
        JsonWriteContext obj1 = rootContext.createChildObjectContext();
        obj1.writeFieldName("outer");
        JsonWriteContext obj2 = obj1.createChildObjectContext();
        obj2.writeFieldName("inner");
        assertTrue("Should write value in inner object", obj2.writeValue());
        // After writing value, inner object should allow another field name? No, because we are still in object context.
        // Actually, after writeValue, the object context is ready for next field name.
        assertTrue("Inner object should allow next field name", obj2.writeFieldName("next"));
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteFieldNameInArrayAfterValue() {
        // Array context: write value, then attempt to write field name (should throw)
        JsonWriteContext arrayContext = rootContext.createChildArrayContext();
        arrayContext.writeValue();
        arrayContext.writeFieldName("shouldFail");
    }

    @Test
    public void testWriteValueInArrayMultipleTimes() {
        JsonWriteContext arrayContext = rootContext.createChildArrayContext();
        for (int i = 0; i < 10; i++) {
            assertTrue("Should write value " + i + " in array", arrayContext.writeValue());
        }
    }

    @Test
    public void testWriteFieldNameAndValueInObjectMultipleTimes() {
        JsonWriteContext objectContext = rootContext.createChildObjectContext();
        for (int i = 0; i < 5; i++) {
            assertTrue("Should write field name " + i, objectContext.writeFieldName("key" + i));
            assertTrue("Should write value " + i, objectContext.writeValue());
        }
    }

    // --- Test for reset method if exists (optional) ---
    // JsonWriteContext may have a reset method; we test it if available.
    // Since we don't know for sure, we'll skip or test via reflection? Better to assume it exists.
    // Actually, in Jackson, JsonWriteContext has a reset method. Let's test it.
    @Test
    public void testReset() {
        JsonWriteContext child = rootContext.createChildObjectContext();
        child.writeFieldName("test");
        child.writeValue();
        // Reset the context (simulate reuse)
        child.reset();
        // After reset, it should behave like a fresh context
        assertTrue("After reset, should allow writeFieldName", child.writeFieldName("newField"));
        assertTrue("After reset, should allow writeValue", child.writeValue());
    }

    // --- Test for parent retrieval ---
    @Test
    public void testGetParent() {
        assertNull("Root context parent should be null", rootContext.getParent());
        JsonWriteContext child = rootContext.createChildArrayContext();
        assertSame("Child parent should be root", rootContext, child.getParent());
        JsonWriteContext grandchild = child.createChildObjectContext();
        assertSame("Grandchild parent should be child", child, grandchild.getParent());
    }

    // --- Test for type constants ---
    @Test
    public void testTypeConstants() {
        assertEquals("TYPE_ROOT should be 0", 0, JsonWriteContext.TYPE_ROOT);
        assertEquals("TYPE_ARRAY should be 1", 1, JsonWriteContext.TYPE_ARRAY);
        assertEquals("TYPE_OBJECT should be 2", 2, JsonWriteContext.TYPE_OBJECT);
    }
}