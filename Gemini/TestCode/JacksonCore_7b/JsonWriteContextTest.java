package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonWriteContextTest {

    @Test
    public void testRootContextCreationAndType() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, root.writeValue());
        assertTrue(root.inRoot());
        assertFalse(root.inArray());
        assertFalse(root.inObject());
        assertNull(root.getCurrentName());
        assertNull(root.getParent());
    }

    @Test
    public void testCreateChildArrayContext() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext arrayContext = root.createChildArrayContext();

        assertEquals(JsonWriteContext.STATUS_OK_BEFORE_COMMA, arrayContext.writeValue());
        assertTrue(arrayContext.inArray());
        assertFalse(arrayContext.inObject());
        assertFalse(arrayContext.inRoot());
        assertEquals(root, arrayContext.getParent());
        assertNull(arrayContext.getCurrentName());
    }

    @Test
    public void testCreateChildObjectContext() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext objectContext = root.createChildObjectContext();

        assertEquals(JsonWriteContext.STATUS_EXPECT_NAME, objectContext.writeValue());
        assertFalse(objectContext.inArray());
        assertTrue(objectContext.inObject());
        assertFalse(objectContext.inRoot());
        assertEquals(root, objectContext.getParent());
        assertNull(objectContext.getCurrentName());
    }

    @Test
    public void testArrayWriteValueSequence() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext arrayContext = root.createChildArrayContext();

        // First value: no comma needed yet
        assertEquals(JsonWriteContext.STATUS_OK_BEFORE_COMMA, arrayContext.writeValue());
        // Second value: comma needed
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, arrayContext.writeValue());
        // Third value: comma needed
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, arrayContext.writeValue());
    }

    @Test
    public void testObjectWriteValueSequenceAndFieldNames() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext objectContext = root.createChildObjectContext();

        // Expecting name, writing value without name should fail or return expect name
        assertEquals(JsonWriteContext.STATUS_EXPECT_NAME, objectContext.writeValue());

        // Write a field name
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, objectContext.writeFieldName("field1"));
        assertEquals("field1", objectContext.getCurrentName());

        // After field name, writing value should be OK before comma (first entry in object)
        assertEquals(JsonWriteContext.STATUS_OK_BEFORE_COMMA, objectContext.writeValue());
        assertNull(objectContext.getCurrentName()); // Should be cleared or reset depending on impl

        // Write second field name
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, objectContext.writeFieldName("field2"));
        assertEquals("field2", objectContext.getCurrentName());

        // Writing value for second field should be OK after comma
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, objectContext.writeValue());
    }

    @Test(expected = com.fasterxml.jackson.core.JsonGenerationException.class)
    public void testObjectDuplicateFieldNameOrInvalidState() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext objectContext = root.createChildObjectContext();

        // Write field name
        objectContext.writeFieldName("field1");
        // Try writing another field name without writing value first (should throw exception in strict mode or depending on dupl/state checks)
        // Note: Depending on duplicate checker, let's see how reset works
        objectContext.writeFieldName("field2");
    }

    @Test
    public void testResetObjectContextReuse() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext objectContext = root.createChildObjectContext();
        
        JsonWriteContext resetContext = objectContext.reset(JsonWriteContext.TYPE_OBJECT);
        assertSame(objectContext, resetContext);
        assertTrue(resetContext.inObject());
    }

    @Test
    public void testResetArrayContextReuse() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext arrayContext = root.createChildArrayContext();
        
        JsonWriteContext resetContext = arrayContext.reset(JsonWriteContext.TYPE_ARRAY);
        assertSame(arrayContext, resetContext);
        assertTrue(resetContext.inArray());
    }

    @Test
    public void testResetRootContextReuse() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        JsonWriteContext resetContext = root.reset(JsonWriteContext.TYPE_ROOT);
        assertSame(root, resetContext);
        assertTrue(resetContext.inRoot());
    }

    @Test
    public void testWithDupDetector() {
        DupDetector dupDetector = DupDetector.rootDetector(null);
        JsonWriteContext root = JsonWriteContext.createRootContext(dupDetector);
        assertSame(dupDetector, root.getDupDetector());

        JsonWriteContext childObj = root.createChildObjectContext();
        assertSame(dupDetector, childObj.getDupDetector());
    }

    @Test
    public void testToStringOutput() {
        JsonWriteContext root = JsonWriteContext.createRootContext(null);
        assertNotNull(root.toString());

        JsonWriteContext arr = root.createChildArrayContext();
        assertNotNull(arr.toString());

        JsonWriteContext obj = root.createChildObjectContext();
        assertNotNull(obj.toString());
    }
}