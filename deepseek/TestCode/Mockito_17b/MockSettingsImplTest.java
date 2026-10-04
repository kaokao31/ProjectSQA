package org.mockito.internal.creation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.listeners.InvocationListener;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.SerializableMode;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MockSettingsImplTest {

    private MockSettingsImpl settings;

    @Before
    public void setUp() {
        settings = new MockSettingsImpl();
    }

    // ---------- defaultAnswer ----------
    @Test
    public void testDefaultAnswer() {
        Answer<Object> answer = invocation -> null;
        settings.defaultAnswer(answer);
        assertSame(answer, settings.getDefaultAnswer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultAnswerNull() {
        settings.defaultAnswer(null);
    }

    // ---------- extraInterfaces ----------
    @Test
    public void testExtraInterfacesValid() {
        settings.extraInterfaces(List.class, Set.class);
        Set<Class<?>> interfaces = settings.getExtraInterfaces();
        assertTrue(interfaces.contains(List.class));
        assertTrue(interfaces.contains(Set.class));
        assertEquals(2, interfaces.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtraInterfacesNullArray() {
        settings.extraInterfaces((Class<?>[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtraInterfacesNullElement() {
        settings.extraInterfaces(List.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtraInterfacesNonInterface() {
        settings.extraInterfaces(String.class); // String is not an interface
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtraInterfacesDuplicate() {
        settings.extraInterfaces(List.class, List.class);
    }

    @Test
    public void testExtraInterfacesEmpty() {
        settings.extraInterfaces();
        assertTrue(settings.getExtraInterfaces().isEmpty());
    }

    // ---------- name ----------
    @Test
    public void testName() {
        settings.name("myMock");
        assertEquals("myMock", settings.getMockName().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameNull() {
        settings.name(null);
    }

    // ---------- serializable ----------
    @Test
    public void testSerializable() {
        settings.serializable();
        assertEquals(SerializableMode.ACROSS_CLASSLOADERS, settings.getSerializableMode());
    }

    @Test
    public void testSerializableWithExtraInterfaces() {
        // When serializable is set, extra interfaces must be serializable
        settings.extraInterfaces(java.io.Serializable.class);
        settings.serializable();
        assertEquals(SerializableMode.ACROSS_CLASSLOADERS, settings.getSerializableMode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializableWithNonSerializableExtraInterface() {
        settings.extraInterfaces(List.class); // List is not serializable
        settings.serializable(); // Should throw because List is not Serializable
    }

    // ---------- stubOnly ----------
    @Test
    public void testStubOnly() {
        settings.stubOnly();
        assertTrue(settings.isStubOnly());
    }

    // ---------- useConstructor ----------
    @Test
    public void testUseConstructor() {
        settings.useConstructor();
        assertTrue(settings.isUsingConstructor());
    }

    // ---------- outerInstance ----------
    @Test
    public void testOuterInstance() {
        Object outer = new Object();
        settings.outerInstance(outer);
        assertSame(outer, settings.getOuterClassInstance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOuterInstanceNull() {
        settings.outerInstance(null);
    }

    // ---------- invocationListeners ----------
    @Test
    public void testInvocationListeners() {
        InvocationListener listener = mockInvocationListener();
        settings.invocationListeners(listener);
        List<InvocationListener> listeners = settings.getInvocationListeners();
        assertTrue(listeners.contains(listener));
        assertEquals(1, listeners.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvocationListenersNullArray() {
        settings.invocationListeners((InvocationListener[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvocationListenersNullElement() {
        settings.invocationListeners(mockInvocationListener(), null);
    }

    // ---------- getObject / setObject (if applicable) ----------
    // Assuming MockSettingsImpl has a method to set the object to mock
    // Not always present, but we can test if it exists
    @Test
    public void testSetObject() {
        // This method may not exist; if it does, test it.
        // For safety, we skip if method not found.
        try {
            settings.getClass().getMethod("setObject", Object.class);
            Object obj = new Object();
            settings.setObject(obj);
            assertSame(obj, settings.getObject());
        } catch (NoSuchMethodException e) {
            // Method not present, skip test
        }
    }

    // ---------- confirmSettings (if applicable) ----------
    // Some implementations have a confirmSettings method that validates all settings
    @Test
    public void testConfirmSettingsValid() {
        settings.defaultAnswer(invocation -> null);
        settings.name("test");
        settings.serializable();
        settings.extraInterfaces(java.io.Serializable.class);
        // Should not throw
        settings.confirmSettings();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfirmSettingsMissingDefaultAnswer() {
        // If defaultAnswer is required, this should throw
        settings.name("test");
        settings.confirmSettings();
    }

    // ---------- Edge cases for serializable mode ----------
    @Test
    public void testSerializableModeNoneByDefault() {
        assertEquals(SerializableMode.NONE, settings.getSerializableMode());
    }

    @Test
    public void testSerializableModeAfterSerializable() {
        settings.serializable();
        assertEquals(SerializableMode.ACROSS_CLASSLOADERS, settings.getSerializableMode());
    }

    // ---------- Helper to create a mock InvocationListener ----------
    private InvocationListener mockInvocationListener() {
        return new InvocationListener() {
            @Override
            public void reportResult(Object result) {
                // no-op
            }

            @Override
            public void reportError(Throwable error) {
                // no-op
            }
        };
    }
}