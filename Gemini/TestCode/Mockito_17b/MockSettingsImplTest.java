package org.mockito.internal.creation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.util.MockNameImpl;
import org.mockito.invocation.Location;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.SerializableMode;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class MockSettingsImplTest {

    private MockSettingsImpl settings;

    @Before
    public void setUp() {
        settings = new MockSettingsImpl();
    }

    @Test
    public void testDefaultValues() {
        assertNull(settings.getTypeToMock());
        assertFalse(settings.isExtraInterfacesSet());
        assertTrue(settings.getExtraInterfaces().isEmpty());
        assertNull(settings.getName());
        assertNull(settings.getSpiedInstance());
        assertNotNull(settings.getDefaultAnswer());
        assertFalse(settings.isSerializable());
        assertNull(settings.getSerializableMode());
        assertFalse(settings.reporter().hasAnswersForStubging());
        assertNull(settings.getOuterClassInstance());
        assertFalse(settings.isStripAnnotations());
    }

    @Test
    public void testSetExtraInterfaces() {
        Class<?>[] interfaces = new Class<?>[]{Serializable.class, Comparable.class};
        MockSettingsImpl result = settings.extraInterfaces(interfaces);

        assertSame(settings, result);
        assertTrue(settings.isExtraInterfacesSet());
        Set<Class<?>> expected = new HashSet<>();
        Collections.addAll(expected, interfaces);
        assertEquals(expected, settings.getExtraInterfaces());
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesNull() {
        settings.extraInterfaces((Class<?>[]) null);
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesEmpty() {
        settings.extraInterfaces(new Class<?>[0]);
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesNotAnInterface() {
        settings.extraInterfaces(String.class);
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesDuplicate() {
        settings.extraInterfaces(Serializable.class, Serializable.class);
    }

    @Test
    public void testName() {
        MockSettingsImpl result = settings.name("myMock");
        assertSame(settings, result);
        assertEquals("myMock", settings.getName());
        assertTrue(settings.isMockNameExplicitlySet());
    }

    @Test
    public void testSpiedInstance() {
        Object spied = new Object();
        MockSettingsImpl result = settings.spiedInstance(spied);
        assertSame(settings, result);
        assertEquals(spied, settings.getSpiedInstance());
    }

    @Test
    public void testDefaultAnswer() {
        Answer<Object> answer = invocation -> null;
        MockSettingsImpl result = settings.defaultAnswer(answer);
        assertSame(settings, result);
        assertEquals(answer, settings.getDefaultAnswer());
    }

    @Test(expected = MockitoException.class)
    public void testDefaultAnswerNull() {
        settings.defaultAnswer(null);
    }

    @Test
    public void testSerializable() {
        MockSettingsImpl result = settings.serializable();
        assertSame(settings, result);
        assertTrue(settings.isSerializable());
        assertEquals(SerializableMode.BASIC, settings.getSerializableMode());
    }

    @Test
    public void testSerializableWithMode() {
        MockSettingsImpl result = settings.serializable(SerializableMode.ACROSS_CLASSLOADERS);
        assertSame(settings, result);
        assertTrue(settings.isSerializable());
        assertEquals(SerializableMode.ACROSS_CLASSLOADERS, settings.getSerializableMode());
    }

    @Test(expected = MockitoException.class)
    public void testSerializableNullMode() {
        settings.serializable(null);
    }

    @Test
    public void testOuterClassInstance() {
        Object outer = new Object();
        MockSettingsImpl result = settings.outerClassInstance(outer);
        assertSame(settings, result);
        assertEquals(outer, settings.getOuterClassInstance());
    }

    @Test(expected = MockitoException.class)
    public void testOuterClassInstanceNull() {
        settings.outerClassInstance(null);
    }

    @Test
    public void testStripAnnotations() {
        MockSettingsImpl result = settings.stripAnnotations();
        assertSame(settings, result);
        assertTrue(settings.isStripAnnotations());
    }

    @Test
    public void testGetMockName() {
        // When name is not set explicitly, it should generate a default one based on type or fallback
        assertNotNull(settings.getMockName());
        
        settings.name("explicitName");
        assertEquals("explicitName", settings.getMockName().toString());
    }

    @Test
    public void testGetMockNameWithoutExplicitName() {
        settings.setTypeToMock(String.class);
        assertNotNull(settings.getMockName());
    }

    @Test
    public void testCopyAnnotations() {
        MockSettingsImpl result = settings.copyAnnotationsFrom(String.class);
        assertSame(settings, result);
        assertTrue(settings.isStripAnnotations()); // Depending on Mockito internal logic, but let's check basic execution
    }

    @Test
    public void testBuildWithValidSettings() {
        settings.setTypeToMock(List.class);
        MockCreationSettings<List> creationSettings = settings.build(List.class);
        assertNotNull(creationSettings);
        assertEquals(List.class, creationSettings.getTypeToMock());
    }

    @Test(expected = MockitoException.class)
    public void testBuildWithoutTypeToMock() {
        settings.build(null);
    }

    @Test
    public void testGetValidationInfo() {
        assertNotNull(settings.getValidationInfo());
    }
}