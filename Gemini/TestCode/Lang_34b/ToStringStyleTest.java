package org.apache.commons.lang3.builder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ToStringStyleTest {

    private static class Person {
        String name;
        int age;
        boolean smoker;
    }

    private static final class TestToStringStyle extends ToStringStyle {
        private static final long serialVersionUID = 1L;

        public TestToStringStyle() {
            super();
        }
    }

    private final Integer base = Integer.valueOf(5);
    private StringBuffer buffer;

    @Before
    public void setUp() {
        buffer = new StringBuffer();
    }

    @After
    public void tearDown() {
        buffer = null;
    }

    @Test
    public void testRegistryCleanedUp() {
        Object obj = new Object();
        assertNull("Registry should be null initially", ToStringStyle.getRegistry());
        ToStringStyle.register(obj);
        assertTrue("Object should be registered", ToStringStyle.isRegistered(obj));
        assertNotNull("Registry should not be null when populated", ToStringStyle.getRegistry());
        assertEquals(1, ToStringStyle.getRegistry().size());

        ToStringStyle.unregister(obj);
        assertFalse("Object should no longer be registered", ToStringStyle.isRegistered(obj));
        assertNull("Registry should be null when emptied", ToStringStyle.getRegistry());
    }

    @Test
    public void testRegistryMultipleObjects() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        ToStringStyle.register(obj1);
        ToStringStyle.register(obj2);

        assertTrue(ToStringStyle.isRegistered(obj1));
        assertTrue(ToStringStyle.isRegistered(obj2));
        assertEquals(2, ToStringStyle.getRegistry().size());

        ToStringStyle.unregister(obj1);
        assertFalse(ToStringStyle.isRegistered(obj1));
        assertTrue(ToStringStyle.isRegistered(obj2));
        assertNotNull(ToStringStyle.getRegistry());
        assertEquals(1, ToStringStyle.getRegistry().size());

        ToStringStyle.unregister(obj2);
        assertFalse(ToStringStyle.isRegistered(obj2));
        assertNull(ToStringStyle.getRegistry());
    }

    @Test
    public void testRegistryNullHandling() {
        ToStringStyle.register(null);
        assertFalse(ToStringStyle.isRegistered(null));
        assertNull(ToStringStyle.getRegistry());

        ToStringStyle.unregister(null);
        assertNull(ToStringStyle.getRegistry());
    }

    @Test
    public void testAppendSuper() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.appendSuper(buffer, null);
        assertEquals("", buffer.toString());

        style.appendSuper(buffer, "foo");
        assertEquals("foo", buffer.toString());

        StringBuffer buf2 = new StringBuffer();
        style.appendSuper(buf2, "foo,");
        assertEquals("foo", buf2.toString());

        StringBuffer buf3 = new StringBuffer();
        style.appendSuper(buf3, "foo" + style.getContentEnd());
        assertEquals("foo", buf3.toString());
    }

    @Test
    public void testAppendToString() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.appendToString(buffer, null);
        assertEquals("", buffer.toString());

        style.appendToString(buffer, "foo");
        assertEquals("foo", buffer.toString());

        StringBuffer buf2 = new StringBuffer();
        style.appendToString(buf2, "foo,");
        assertEquals("foo", buf2.toString());

        StringBuffer buf3 = new StringBuffer();
        style.appendToString(buf3, "foo" + style.getContentEnd());
        assertEquals("foo", buf3.toString());
    }

    @Test
    public void testAppendStartEnd() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.appendStart(buffer, base);
        style.appendEnd(buffer, base);
        assertEquals(base.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(base)) + "[]", buffer.toString());
    }

    @Test
    public void testAppendStartEndNullObject() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.appendStart(buffer, null);
        style.appendEnd(buffer, null);
        assertEquals("[]", buffer.toString());
    }

    @Test
    public void testAppendObject() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.append(buffer, "field", (Object) "value", Boolean.TRUE);
        assertEquals("field=value,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "field", (Object) null, Boolean.TRUE);
        assertEquals("field=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "field", (Object) "value", Boolean.FALSE);
        assertEquals("field=<String>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "field", (Object) null, Boolean.FALSE);
        assertEquals("field=<null>,", buffer.toString());
    }

    @Test
    public void testAppendPrimitives() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;

        buffer.setLength(0);
        style.append(buffer, "b", (byte) 12);
        assertEquals("b=12,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "s", (short) 1234);
        assertEquals("s=1234,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "i", 12345);
        assertEquals("i=12345,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "l", 123456789L);
        assertEquals("l=123456789,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "f", 12.34f);
        assertEquals("f=12.34,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "d", 12.3456);
        assertEquals("d=12.3456,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "bool", true);
        assertEquals("bool=true,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "c", 'A');
        assertEquals("c=A,", buffer.toString());
    }

    @Test
    public void testAppendPrimitiveArrays() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;

        buffer.setLength(0);
        style.append(buffer, "byteArray", new byte[]{1, 2, 3}, Boolean.TRUE);
        assertEquals("byteArray={1,2,3},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "byteArray", (byte[]) null, Boolean.TRUE);
        assertEquals("byteArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "byteArray", new byte[]{1, 2, 3}, Boolean.FALSE);
        assertEquals("byteArray=<size=3>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "shortArray", new short[]{10, 20}, Boolean.TRUE);
        assertEquals("shortArray={10,20},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "shortArray", (short[]) null, Boolean.TRUE);
        assertEquals("shortArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "shortArray", new short[]{10, 20}, Boolean.FALSE);
        assertEquals("shortArray=<size=2>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "intArray", new int[]{100, 200}, Boolean.TRUE);
        assertEquals("intArray={100,200},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "intArray", (int[]) null, Boolean.TRUE);
        assertEquals("intArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "intArray", new int[]{100, 200}, Boolean.FALSE);
        assertEquals("intArray=<size=2>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "longArray", new long[]{1000L, 2000L}, Boolean.TRUE);
        assertEquals("longArray={1000,2000},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "longArray", (long[]) null, Boolean.TRUE);
        assertEquals("longArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "longArray", new long[]{1000L, 2000L}, Boolean.FALSE);
        assertEquals("longArray=<size=2>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "floatArray", new float[]{1.5f, 2.5f}, Boolean.TRUE);
        assertEquals("floatArray={1.5,2.5},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "floatArray", (float[]) null, Boolean.TRUE);
        assertEquals("floatArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "floatArray", new float[]{1.5f, 2.5f}, Boolean.FALSE);
        assertEquals("floatArray=<size=2>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "doubleArray", new double[]{1.5, 2.5}, Boolean.TRUE);
        assertEquals("doubleArray={1.5,2.5},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "doubleArray", (double[]) null, Boolean.TRUE);
        assertEquals("doubleArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "doubleArray", new double[]{1.5, 2.5}, Boolean.FALSE);
        assertEquals("doubleArray=<size=2>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "charArray", new char[]{'a', 'b'}, Boolean.TRUE);
        assertEquals("charArray={a,b},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "charArray", (char[]) null, Boolean.TRUE);
        assertEquals("charArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "charArray", new char[]{'a', 'b'}, Boolean.FALSE);
        assertEquals("charArray=<size=2>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "booleanArray", new boolean[]{true, false}, Boolean.TRUE);
        assertEquals("booleanArray={true,false},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "booleanArray", (boolean[]) null, Boolean.TRUE);
        assertEquals("booleanArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "booleanArray", new boolean[]{true, false}, Boolean.FALSE);
        assertEquals("booleanArray=<size=2>,", buffer.toString());
    }

    @Test
    public void testAppendObjectArray() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;

        buffer.setLength(0);
        style.append(buffer, "objArray", new Object[]{"a", "b"}, Boolean.TRUE);
        assertEquals("objArray={a,b},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "objArray", (Object[]) null, Boolean.TRUE);
        assertEquals("objArray=<null>,", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "objArray", new Object[]{"a", "b"}, Boolean.FALSE);
        assertEquals("objArray=<size=2>,", buffer.toString());
    }

    @Test
    public void testAppendCollectionsAndMaps() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;

        Collection<String> col = Arrays.asList("a", "b");
        buffer.setLength(0);
        style.append(buffer, "col", col, Boolean.TRUE);
        assertEquals("col=[a, b],", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "col", col, Boolean.FALSE);
        assertEquals("col=<size=2>,", buffer.toString());

        Map<String, String> map = new HashMap<String, String>();
        map.put("k", "v");
        buffer.setLength(0);
        style.append(buffer, "map", map, Boolean.TRUE);
        assertEquals("map={k=v},", buffer.toString());

        buffer.setLength(0);
        style.append(buffer, "map", map, Boolean.FALSE);
        assertEquals("map=<size=1>,", buffer.toString());
    }

    @Test
    public void testMultiLineStyle() {
        ToStringStyle style = ToStringStyle.MULTI_LINE_STYLE;
        style.appendStart(buffer, base);
        style.append(buffer, "field", "value", Boolean.TRUE);
        style.appendEnd(buffer, base);
        String exp = base.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(base)) + "[" + System.getProperty("line.separator")
                + "  field=value" + System.getProperty("line.separator")
                + "]";
        assertEquals(exp, buffer.toString());
    }

    @Test
    public void testNoFieldNameStyle() {
        ToStringStyle style = ToStringStyle.NO_FIELD_NAMES_STYLE;
        style.appendStart(buffer, base);
        style.append(buffer, "field", "value", Boolean.TRUE);
        style.appendEnd(buffer, base);
        String exp = base.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(base)) + "[value]";
        assertEquals(exp, buffer.toString());
    }

    @Test
    public void testShortPrefixStyle() {
        ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
        style.appendStart(buffer, base);
        style.append(buffer, "field", "value", Boolean.TRUE);
        style.appendEnd(buffer, base);
        String exp = "Integer[field=value]";
        assertEquals(exp, buffer.toString());
    }

    @Test
    public void testSimpleStyle() {
        ToStringStyle style = ToStringStyle.SIMPLE_STYLE;
        style.appendStart(buffer, base);
        style.append(buffer, "field", "value", Boolean.TRUE);
        style.appendEnd(buffer, base);
        assertEquals("value", buffer.toString());
    }

    @Test
    public void testCustomToStringStyleProperties() {
        TestToStringStyle style = new TestToStringStyle();

        assertTrue(style.isUseClassName());
        style.setUseClassName(false);
        assertFalse(style.isUseClassName());

        assertTrue(style.isUseIdentityHashCode());
        style.setUseIdentityHashCode(false);
        assertFalse(style.isUseIdentityHashCode());

        assertTrue(style.isUseFieldNames());
        style.setUseFieldNames(false);
        assertFalse(style.isUseFieldNames());

        assertTrue(style.isDefaultFullDetail());
        style.setDefaultFullDetail(false);
        assertFalse(style.isDefaultFullDetail());

        assertTrue(style.isArrayContentDetail());
        style.setArrayContentDetail(false);
        assertFalse(style.isArrayContentDetail());

        assertEquals("[", style.getContentStart());
        style.setContentStart("{");
        assertEquals("{", style.getContentStart());
        style.setContentStart(null);
        assertEquals("", style.getContentStart());

        assertEquals("]", style.getContentEnd());
        style.setContentEnd("}");
        assertEquals("}", style.getContentEnd());
        style.setContentEnd(null);
        assertEquals("", style.getContentEnd());

        assertEquals("=", style.getFieldNameValueSeparator());
        style.setFieldNameValueSeparator("->");
        assertEquals("->", style.getFieldNameValueSeparator());
        style.setFieldNameValueSeparator(null);
        assertEquals("", style.getFieldNameValueSeparator());

        assertEquals(",", style.getFieldSeparator());
        style.setFieldSeparator(";");
        assertEquals(";", style.getFieldSeparator());
        style.setFieldSeparator(null);
        assertEquals("", style.getFieldSeparator());

        assertFalse(style.isFieldSeparatorAtStart());
        style.setFieldSeparatorAtStart(true);
        assertTrue(style.isFieldSeparatorAtStart());

        assertFalse(style.isFieldSeparatorAtEnd());
        style.setFieldSeparatorAtEnd(true);
        assertTrue(style.isFieldSeparatorAtEnd());

        assertEquals("<null>", style.getNullText());
        style.setNullText("NULL");
        assertEquals("NULL", style.getNullText());
        style.setNullText(null);
        assertEquals("", style.getNullText());

        assertEquals("<size=", style.getSizeStartText());
        style.setSizeStartText("(size=");
        assertEquals("(size=", style.getSizeStartText());
        style.setSizeStartText(null);
        assertEquals("", style.getSizeStartText());

        assertEquals(">", style.getSizeEndText());
        style.setSizeEndText(")");
        assertEquals(")", style.getSizeEndText());
        style.setSizeEndText(null);
        assertEquals("", style.getSizeEndText());

        assertEquals("<", style.getSummaryObjectStartText());
        style.setSummaryObjectStartText("[");
        assertEquals("[", style.getSummaryObjectStartText());
        style.setSummaryObjectStartText(null);
        assertEquals("", style.getSummaryObjectStartText());

        assertEquals(">", style.getSummaryObjectEndText());
        style.setSummaryObjectEndText("]");
        assertEquals("]", style.getSummaryObjectEndText());
        style.setSummaryObjectEndText(null);
        assertEquals("", style.getSummaryObjectEndText());

        assertEquals("{", style.getArrayStart());
        style.setArrayStart("<");
        assertEquals("<", style.getArrayStart());
        style.setArrayStart(null);
        assertEquals("", style.getArrayStart());

        assertEquals("}", style.getArrayEnd());
        style.setArrayEnd(">");
        assertEquals(">", style.getArrayEnd());
        style.setArrayEnd(null);
        assertEquals("", style.getArrayEnd());

        assertEquals(",", style.getArraySeparator());
        style.setArraySeparator("|");
        assertEquals("|", style.getArraySeparator());
        style.setArraySeparator(null);
        assertEquals("", style.getArraySeparator());

        assertFalse(style.isUseShortClassName());
        style.setUseShortClassName(true);
        assertTrue(style.isUseShortClassName());
    }

    @Test
    public void testReflectionObjectCycleRegistryCleanup() {
        Person p = new Person();
        p.name = "John";
        p.age = 30;
        p.smoker = false;

        String result = ToStringBuilder.reflectionToString(p);
        assertNotNull(result);
        assertNull("Registry should be null after reflectionToString completes", ToStringStyle.getRegistry());
    }

    @Test
    public void testArrayReflectionObjectCycleRegistryCleanup() {
        int[] array = new int[]{1, 2, 3};
        String result = ToStringBuilder.reflectionToString(array);
        assertNotNull(result);
        assertNull("Registry should be null after reflection array toString", ToStringStyle.getRegistry());

        Object[][] multiArray = new Object[][]{{1, 2}, {3, 4}};
        String multiResult = ToStringBuilder.reflectionToString(multiArray);
        assertNotNull(multiResult);
        assertNull("Registry should be null after reflection 2D array toString", ToStringStyle.getRegistry());
    }
}