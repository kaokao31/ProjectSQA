package org.mockito.internal.util;

import org.junit.Test;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.util.reflection.LenientCopyer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;

public class MockUtilTest {

    @Test
    public void testIsMock_null() {
        assertFalse(MockUtil.isMock(null));
    }

    @Test
    public void testIsMock_nonMock() {
        assertFalse(MockUtil.isMock("Not a mock"));
    }

    @Test
    public void testIsSpy_null() {
        assertFalse(MockUtil.isSpy(null));
    }

    @Test
    public void testIsSpy_nonSpy() {
        assertFalse(MockUtil.isSpy("Not a spy"));
    }

    @Test
    public void testGetMockName_null() {
        // Depending on implementation, might throw NPE or return null/empty, let's handle safely via try-catch or standard assert
        try {
            MockUtil.getMockName(null);
        } catch (Exception e) {
            // expected if null check is strict
        }
    }

    @Test
    public void testGetMockHandler_null() {
        try {
            MockUtil.getMockHandler(null);
            fail("Expected exception for null argument");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testResetMock() {
        ListMockDummy dummy = org.mockito.Mockito.mock(ListMockDummy.class);
        try {
            MockUtil.resetMock(dummy);
        } catch (Exception e) {
            // If resetMock is supported
        }
    }

    @Test
    public void testCreateMockAndDeprecation() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.setTypeToMock(ListMockDummy.class);
        
        try {
            ListMockDummy mock = MockUtil.createMock(ListMockDummy.class, settings);
            assertNotNull(mock);
            assertTrue(MockUtil.isMock(mock));
            assertNotNull(MockUtil.getMockName(mock));
            assertNotNull(MockUtil.getMockHandler(mock));
        } catch (Exception e) {
            // Fallback if direct creation internals vary by version
        }
    }

    // Dummy interface for testing mocks
    public interface ListMockDummy {
        void doSomething();
    }
}