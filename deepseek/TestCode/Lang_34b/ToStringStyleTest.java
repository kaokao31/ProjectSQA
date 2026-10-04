package org.apache.commons.lang3.builder;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class ToStringStyleTest {

    private ToStringStyle underTest;

    @Before
    public void setUp() {
        underTest = new ToStringStyle() {
            private static final long serialVersionUID = 1L;
            // Concrete subclass for testing abstract ToStringStyle
        };
    }

    @Test
    public void testDefaultStyleNullValues() {
        underTest.setUseShortClassName(true);
        underTest.setUseIdentityHashCode(true);
        underTest.setContentStart("[");
        underTest.setContentEnd("]");
        underTest.setFieldNameValueSeparator("=");
        underTest.setFieldSeparator(",");
        underTest.setNullText("<null>");
        underTest.setSummaryObjectStartText("{");
        underTest.setSummaryObjectEndText("}");
        underTest.setSizeStartText("<size=");
        underTest.setSizeEndText(">");
        
        StringBuilder buffer = new StringBuilder();
        underTest.appendStart(buffer, null);
        String result = buffer.toString();
        assertTrue(result.contains("[") || result.isEmpty());
    }

    @Test
    public void testAppendNullField() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", null, Boolean.class);
        assertEquals("", buffer.toString());
    }

    @Test
    public void testAppendWithNullBuffer() {
        Object obj = new Object();
        try {
            underTest.append(obj, null, "field", "value", String.class);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testAppendFieldSeparator() {
        StringBuilder buffer = new StringBuilder();
        
        // First append - should NOT add field separator
        underTest.append(null, buffer, "field1", "value1", String.class);
        
        // Reset buffer for clarity in test
        buffer.setLength(0);
        buffer.append("initial");
        
        underTest.appendFieldSeparator(buffer);
        assertEquals("initial,", buffer.toString());
    }

    @Test
    public void testAppendFieldStart() {
        StringBuilder buffer = new StringBuilder();
        underTest.appendFieldStart(buffer, "testField");
        assertEquals("testField=", buffer.toString());
    }

    @Test
    public void testAppendFieldEnd() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("testValue");
        underTest.appendFieldEnd(buffer, "testField");
        assertEquals("testValue", buffer.toString());
    }

    @Test
    public void testAppendSummaryObject() {
        StringBuilder buffer = new StringBuilder();
        Object obj = new Object();
        underTest.appendSummary(buffer, null, obj);
        assertTrue(buffer.toString().startsWith("{"));
        assertTrue(buffer.toString().endsWith("}"));
    }

    @Test
    public void testAppendSummarySize() {
        StringBuilder buffer = new StringBuilder();
        underTest.appendSummarySize(buffer, null, 5);
        assertEquals("<size=5>", buffer.toString());
    }

    @Test
    public void testAppendIdentityHashCode() {
        StringBuilder buffer = new StringBuilder();
        underTest.setUseIdentityHashCode(true);
        Object obj = new Object();
        underTest.appendIdentityHashCode(buffer, null);
        assertTrue(buffer.length() > 0);
    }

    @Test
    public void testAppendIdentityHashCodeDisabled() {
        StringBuilder buffer = new StringBuilder();
        underTest.setUseIdentityHashCode(false);
        Object obj = new Object();
        underTest.appendIdentityHashCode(buffer, null);
        assertEquals("", buffer.toString());
    }

    @Test
    public void testAppendContentStart() {
        StringBuilder buffer = new StringBuilder();
        underTest.appendContentStart(buffer);
        assertEquals("[", buffer.toString());
    }

    @Test
    public void testAppendContentEnd() {
        StringBuilder buffer = new StringBuilder();
        underTest.appendContentEnd(buffer);
        assertEquals("]", buffer.toString());
    }

    @Test
    public void testAppendStartWithNullObject() {
        StringBuilder buffer = new StringBuilder();
        underTest.appendStart(buffer, null);
        assertTrue(buffer.toString().contains("[") || buffer.toString().isEmpty());
    }

    @Test
    public void testAppendEndWithNullObject() {
        StringBuilder buffer = new StringBuilder();
        underTest.appendEnd(buffer, null);
        assertTrue(buffer.toString().contains("]") || buffer.toString().isEmpty());
    }

    @Test
    public void testRemoveLastFieldSeparator() {
        StringBuilder buffer = new StringBuilder("field1=value1,");
        underTest.removeLastFieldSeparator(buffer);
        assertEquals("field1=value1", buffer.toString());
    }

    @Test
    public void testRemoveLastFieldSeparatorWithNoSeparator() {
        StringBuilder buffer = new StringBuilder("test");
        underTest.removeLastFieldSeparator(buffer);
        assertEquals("test", buffer.toString());
    }

    @Test
    public void testRemoveLastFieldSeparatorWithEmptyBuffer() {
        StringBuilder buffer = new StringBuilder();
        underTest.removeLastFieldSeparator(buffer);
        assertEquals("", buffer.toString());
    }

    @Test
    public void testAppendInternalWithNullValue() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", null, String.class);
        assertEquals("", buffer.toString());
    }

    @Test
    public void testAppendInternalWithStringValue() {
        StringBuilder buffer = new StringBuilder("prefix");
        underTest.append(null, buffer, "field", "testValue", String.class);
        assertTrue(buffer.toString().contains("testValue"));
    }

    @Test
    public void testAppendInternalWithBooleanArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new boolean[]{true, false}, boolean.class);
        assertTrue(buffer.toString().contains("{true,false}"));
    }

    @Test
    public void testAppendInternalWithBooleanArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (boolean[]) null, boolean.class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithByteArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new byte[]{1, 2}, byte.class);
        assertTrue(buffer.toString().contains("{1,2}"));
    }

    @Test
    public void testAppendInternalWithByteArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (byte[]) null, byte.class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithCharArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new char[]{'a', 'b'}, char.class);
        assertTrue(buffer.toString().contains("{a,b}"));
    }

    @Test
    public void testAppendInternalWithCharArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (char[]) null, char.class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithDoubleArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new double[]{1.0, 2.0}, double.class);
        assertTrue(buffer.toString().contains("{1.0,2.0}") || buffer.toString().contains("{1.0,2.0}"));
    }

    @Test
    public void testAppendInternalWithDoubleArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (double[]) null, double.class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithFloatArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new float[]{1.0f, 2.0f}, float.class);
        assertTrue(buffer.toString().contains("{1.0,2.0}") || buffer.toString().contains("{1.0f,2.0f}"));
    }

    @Test
    public void testAppendInternalWithFloatArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (float[]) null, float.class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithIntArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new int[]{1, 2}, int.class);
        assertTrue(buffer.toString().contains("{1,2}"));
    }

    @Test
    public void testAppendInternalWithIntArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (int[]) null, int.class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithLongArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new long[]{1L, 2L}, long.class);
        assertTrue(buffer.toString().contains("{1,2}"));
    }

    @Test
    public void testAppendInternalWithLongArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (long[]) null, long.class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithShortArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new short[]{1, 2}, short.class);
        assertTrue(buffer.toString().contains("{1,2}"));
    }

    @Test
    public void testAppendInternalWithShortArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (short[]) null, short.class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithObjectArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new Object[]{"a", "b"}, Object[].class);
        assertTrue(buffer.toString().contains("{a,b}"));
    }

    @Test
    public void testAppendInternalWithObjectArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (Object[]) null, Object[].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithLongArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new long[][]{{1L, 2L}, {3L, 4L}}, long[][].class);
        assertTrue(buffer.toString().contains("[{1,2},{3,4}]"));
    }

    @Test
    public void testAppendInternalWithLongArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (long[][]) null, long[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithIntArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new int[][]{{1, 2}, {3, 4}}, int[][].class);
        assertTrue(buffer.toString().contains("[{1,2},{3,4}]"));
    }

    @Test
    public void testAppendInternalWithIntArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (int[][]) null, int[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithShortArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new short[][]{{1, 2}, {3, 4}}, short[][].class);
        assertTrue(buffer.toString().contains("[{1,2},{3,4}]"));
    }

    @Test
    public void testAppendInternalWithShortArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (short[][]) null, short[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithByteArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new byte[][]{{1, 2}, {3, 4}}, byte[][].class);
        assertTrue(buffer.toString().contains("[{1,2},{3,4}]"));
    }

    @Test
    public void testAppendInternalWithByteArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (byte[][]) null, byte[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithCharArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new char[][]{{'a', 'b'}, {'c', 'd'}}, char[][].class);
        assertTrue(buffer.toString().contains("[{a,b},{c,d}]"));
    }

    @Test
    public void testAppendInternalWithCharArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (char[][]) null, char[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithDoubleArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new double[][]{{1.0, 2.0}, {3.0, 4.0}}, double[][].class);
        assertTrue(buffer.toString().contains("[{1.0,2.0},{3.0,4.0}]") || buffer.toString().contains("[{1.0,2.0},{3.0,4.0}]"));
    }

    @Test
    public void testAppendInternalWithDoubleArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (double[][]) null, double[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithFloatArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new float[][]{{1.0f, 2.0f}, {3.0f, 4.0f}}, float[][].class);
        assertTrue(buffer.toString().contains("[{1.0,2.0},{3.0,4.0}]") || buffer.toString().contains("[{1.0f,2.0f},{3.0f,4.0f}]"));
    }

    @Test
    public void testAppendInternalWithFloatArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (float[][]) null, float[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithBooleanArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new boolean[][]{{true, false}, {false, true}}, boolean[][].class);
        assertTrue(buffer.toString().contains("[{true,false},{false,true}]"));
    }

    @Test
    public void testAppendInternalWithBooleanArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (boolean[][]) null, boolean[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testAppendInternalWithObjectArrayArray() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", new Object[][]{{"a", "b"}, {"c", "d"}}, Object[][].class);
        assertTrue(buffer.toString().contains("[{a,b},{c,d}]"));
    }

    @Test
    public void testAppendInternalWithObjectArrayArrayNull() {
        StringBuilder buffer = new StringBuilder();
        underTest.append(null, buffer, "field", (Object[][]) null, Object[][].class);
        assertTrue(buffer.toString().contains("<null>"));
    }

    @Test
    public void testUnregisterObject() {
        Object obj = new Object();
        underTest.unregister(obj);
        // Should not throw exception
    }

    @Test
    public void testUnregisterNull() {
        underTest.unregister(null);
        // Should not throw exception
    }

    @Test
    public void testIsRegistered() {
        Object obj = new Object();
        assertFalse(underTest.isRegistered(obj));
    }

    @Test
    public void testIsRegisteredAfterUnregister() {
        Object obj = new Object();
        underTest.unregister(obj);
        assertFalse(underTest.isRegistered(obj));
    }

    @Test
    public void testAppendCyclicReference() {
        StringBuilder buffer = new StringBuilder();
        Object obj = new Object();
        underTest.append(obj, buffer, "self", obj, Object.class);
        assertTrue(buffer.toString().contains("{") || buffer.toString().contains("}"));
    }
}