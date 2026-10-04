package org.mockito.internal.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.internal.configuration.injection.MockInjection;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.internal.util.reflection.InstanceField;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultInjectionEngineTest {

    @Mock
    private FieldSetter fieldSetterMock;

    @Mock
    private MockName mockNameMock;

    @InjectMocks
    private DefaultInjectionEngine engine;

    private static class TestClass {
        private String field1;
        private Integer field2;
        private List<String> field3;
    }

    private TestClass testInstance;
    private Field field1;
    private Field field2;
    private Field field3;

    @Before
    public void setUp() throws Exception {
        testInstance = new TestClass();
        field1 = TestClass.class.getDeclaredField("field1");
        field2 = TestClass.class.getDeclaredField("field2");
        field3 = TestClass.class.getDeclaredField("field3");
        field1.setAccessible(true);
        field2.setAccessible(true);
        field3.setAccessible(true);
    }

    @Test
    public void testInjectMocksWithNullTestClass() {
        try {
            engine.injectMocks(null, Collections.emptySet());
            fail("Expected NullPointerException for null testClass");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testInjectMocksWithNullMocks() {
        try {
            engine.injectMocks(testInstance, null);
            fail("Expected NullPointerException for null mocks");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testInjectMocksWithEmptyMocks() {
        engine.injectMocks(testInstance, Collections.emptySet());
        // No exception expected, no fields should be set
        assertNull(testInstance.field1);
        assertNull(testInstance.field2);
        assertNull(testInstance.field3);
    }

    @Test
    public void testInjectMocksWithSingleMatchingMock() throws Exception {
        String mockValue = "mockString";
        Set<Object> mocks = new HashSet<>(Collections.singletonList(mockValue));
        // Mock FieldSetter to set the field
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.injectMocks(testInstance, mocks);
        // Verify that fieldSetter was called for field1 (String type)
        verify(fieldSetterMock, times(1)).set(eq(field1), eq(mockValue));
        // field2 and field3 should not be set because no matching mock
        verify(fieldSetterMock, never()).set(eq(field2), any());
        verify(fieldSetterMock, never()).set(eq(field3), any());
    }

    @Test
    public void testInjectMocksWithMultipleMocks() throws Exception {
        String mockString = "mockString";
        Integer mockInt = 42;
        Set<Object> mocks = new HashSet<>(Arrays.asList(mockString, mockInt));
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.injectMocks(testInstance, mocks);
        verify(fieldSetterMock, times(1)).set(eq(field1), eq(mockString));
        verify(fieldSetterMock, times(1)).set(eq(field2), eq(mockInt));
        verify(fieldSetterMock, never()).set(eq(field3), any());
    }

    @Test
    public void testInjectMocksWithNoMatchingMock() {
        Set<Object> mocks = new HashSet<>(Collections.singletonList(99.99));
        engine.injectMocks(testInstance, mocks);
        // No fields should be set because no mock matches any field type
        assertNull(testInstance.field1);
        assertNull(testInstance.field2);
        assertNull(testInstance.field3);
    }

    @Test
    public void testInjectMocksWithNullMockInSet() {
        Set<Object> mocks = new HashSet<>();
        mocks.add(null);
        mocks.add("validString");
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.injectMocks(testInstance, mocks);
        // The null mock should be ignored, only the valid string should be injected
        verify(fieldSetterMock, times(1)).set(eq(field1), eq("validString"));
        verify(fieldSetterMock, never()).set(eq(field2), any());
        verify(fieldSetterMock, never()).set(eq(field3), any());
    }

    @Test
    public void testInjectMocksWithDuplicateMocks() {
        String mockString = "mockString";
        Set<Object> mocks = new HashSet<>(Arrays.asList(mockString, mockString));
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.injectMocks(testInstance, mocks);
        // Should only set once
        verify(fieldSetterMock, times(1)).set(eq(field1), eq(mockString));
    }

    @Test
    public void testInjectMocksWithFieldAlreadySet() throws Exception {
        testInstance.field1 = "existing";
        Set<Object> mocks = new HashSet<>(Collections.singletonList("newMock"));
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.injectMocks(testInstance, mocks);
        // Should still set the field (overwrite)
        verify(fieldSetterMock, times(1)).set(eq(field1), eq("newMock"));
    }

    @Test
    public void testInjectMocksWithPrimitiveField() throws Exception {
        // Test class with primitive int field
        class PrimitiveTestClass {
            int primitiveField;
        }
        PrimitiveTestClass primitiveInstance = new PrimitiveTestClass();
        Field primitiveField = PrimitiveTestClass.class.getDeclaredField("primitiveField");
        primitiveField.setAccessible(true);
        Set<Object> mocks = new HashSet<>(Collections.singletonList(42));
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.injectMocks(primitiveInstance, mocks);
        verify(fieldSetterMock, times(1)).set(eq(primitiveField), eq(42));
    }

    @Test
    public void testInjectMocksWithFinalField() throws Exception {
        // Test class with final field
        class FinalFieldClass {
            final String finalField = "initial";
        }
        FinalFieldClass finalInstance = new FinalFieldClass();
        Field finalField = FinalFieldClass.class.getDeclaredField("finalField");
        finalField.setAccessible(true);
        Set<Object> mocks = new HashSet<>(Collections.singletonList("newValue"));
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.injectMocks(finalInstance, mocks);
        // Should attempt to set final field (may fail silently or throw)
        verify(fieldSetterMock, times(1)).set(eq(finalField), eq("newValue"));
    }

    @Test
    public void testInjectMocksWithStaticField() throws Exception {
        // Test class with static field
        class StaticFieldClass {
            static String staticField;
        }
        StaticFieldClass staticInstance = new StaticFieldClass();
        Field staticField = StaticFieldClass.class.getDeclaredField("staticField");
        staticField.setAccessible(true);
        Set<Object> mocks = new HashSet<>(Collections.singletonList("staticValue"));
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.injectMocks(staticInstance, mocks);
        verify(fieldSetterMock, times(1)).set(eq(staticField), eq("staticValue"));
    }

    @Test
    public void testProcessWithNullInjection() {
        // Assuming process method exists and takes a MockInjection
        // This test verifies null handling
        try {
            engine.process(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testProcessWithEmptyInjection() {
        MockInjection injection = mock(MockInjection.class);
        when(injection.getFields()).thenReturn(Collections.emptyList());
        when(injection.getMocks()).thenReturn(Collections.emptySet());
        engine.process(injection);
        // No interactions expected
        verifyNoMoreInteractions(fieldSetterMock);
    }

    @Test
    public void testProcessWithSingleFieldAndMock() {
        MockInjection injection = mock(MockInjection.class);
        List<Field> fields = Collections.singletonList(field1);
        Set<Object> mocks = new HashSet<>(Collections.singletonList("mockValue"));
        when(injection.getFields()).thenReturn(fields);
        when(injection.getMocks()).thenReturn(mocks);
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.process(injection);
        verify(fieldSetterMock, times(1)).set(eq(field1), eq("mockValue"));
    }

    @Test
    public void testProcessWithMultipleFieldsAndMocks() {
        MockInjection injection = mock(MockInjection.class);
        List<Field> fields = Arrays.asList(field1, field2);
        Set<Object> mocks = new HashSet<>(Arrays.asList("mockString", 123));
        when(injection.getFields()).thenReturn(fields);
        when(injection.getMocks()).thenReturn(mocks);
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.process(injection);
        verify(fieldSetterMock, times(1)).set(eq(field1), eq("mockString"));
        verify(fieldSetterMock, times(1)).set(eq(field2), eq(123));
    }

    @Test
    public void testProcessWithUnmatchedMocks() {
        MockInjection injection = mock(MockInjection.class);
        List<Field> fields = Collections.singletonList(field1);
        Set<Object> mocks = new HashSet<>(Collections.singletonList(99.99));
        when(injection.getFields()).thenReturn(fields);
        when(injection.getMocks()).thenReturn(mocks);
        engine.process(injection);
        verify(fieldSetterMock, never()).set(any(Field.class), any());
    }

    @Test
    public void testProcessWithNullMockInSet() {
        MockInjection injection = mock(MockInjection.class);
        List<Field> fields = Collections.singletonList(field1);
        Set<Object> mocks = new HashSet<>();
        mocks.add(null);
        mocks.add("valid");
        when(injection.getFields()).thenReturn(fields);
        when(injection.getMocks()).thenReturn(mocks);
        when(fieldSetterMock.set(any(Field.class), any())).thenReturn(fieldSetterMock);
        engine.process(injection);
        verify(fieldSetterMock, times(1)).set(eq(field1), eq("valid"));
    }
}