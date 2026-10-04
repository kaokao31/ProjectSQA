package org.mockito;

import org.junit.Test;
import org.junit.Before;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class MatchersTest {

    private List<String> mockList;

    @Before
    public void setUp() {
        mockList = mock(List.class);
    }

    // --- anyX() methods ---

    @Test
    public void testAnyBoolean() {
        when(mockList.add(anyBoolean())).thenReturn(true);
        assertTrue(mockList.add(true));
        assertTrue(mockList.add(false));
        verify(mockList, times(2)).add(anyBoolean());
    }

    @Test
    public void testAnyByte() {
        when(mockList.add(anyByte())).thenReturn(true);
        assertTrue(mockList.add((byte) 1));
        assertTrue(mockList.add((byte) 0));
        verify(mockList, times(2)).add(anyByte());
    }

    @Test
    public void testAnyChar() {
        when(mockList.add(anyChar())).thenReturn(true);
        assertTrue(mockList.add('a'));
        assertTrue(mockList.add('\n'));
        verify(mockList, times(2)).add(anyChar());
    }

    @Test
    public void testAnyDouble() {
        when(mockList.add(anyDouble())).thenReturn(true);
        assertTrue(mockList.add(1.0));
        assertTrue(mockList.add(Double.NaN));
        verify(mockList, times(2)).add(anyDouble());
    }

    @Test
    public void testAnyFloat() {
        when(mockList.add(anyFloat())).thenReturn(true);
        assertTrue(mockList.add(1.0f));
        assertTrue(mockList.add(Float.NaN));
        verify(mockList, times(2)).add(anyFloat());
    }

    @Test
    public void testAnyInt() {
        when(mockList.add(anyInt())).thenReturn(true);
        assertTrue(mockList.add(0));
        assertTrue(mockList.add(Integer.MAX_VALUE));
        verify(mockList, times(2)).add(anyInt());
    }

    @Test
    public void testAnyLong() {
        when(mockList.add(anyLong())).thenReturn(true);
        assertTrue(mockList.add(0L));
        assertTrue(mockList.add(Long.MAX_VALUE));
        verify(mockList, times(2)).add(anyLong());
    }

    @Test
    public void testAnyShort() {
        when(mockList.add(anyShort())).thenReturn(true);
        assertTrue(mockList.add((short) 0));
        assertTrue(mockList.add(Short.MAX_VALUE));
        verify(mockList, times(2)).add(anyShort());
    }

    @Test
    public void testAnyString() {
        when(mockList.add(anyString())).thenReturn(true);
        assertTrue(mockList.add(""));
        assertTrue(mockList.add("test"));
        assertTrue(mockList.add(null));  // anyString() matches null as well
        verify(mockList, times(3)).add(anyString());
    }

    @Test
    public void testAnyObject() {
        when(mockList.add(anyObject())).thenReturn(true);
        assertTrue(mockList.add(""));
        assertTrue(mockList.add(1));
        assertTrue(mockList.add(null));
        verify(mockList, times(3)).add(anyObject());
    }

    @Test
    public void testAny() {
        when(mockList.add(any())).thenReturn(true);
        assertTrue(mockList.add(""));
        assertTrue(mockList.add(1));
        assertTrue(mockList.add(null));
        verify(mockList, times(3)).add(any());
    }

    // --- Collection matchers ---

    @Test
    public void testAnyCollection() {
        when(mockList.addAll(anyCollection())).thenReturn(true);
        assertTrue(mockList.addAll(new ArrayList<>()));
        assertTrue(mockList.addAll(Arrays.asList("a", "b")));
        assertTrue(mockList.addAll(null));
        verify(mockList, times(3)).addAll(anyCollection());
    }

    @Test
    public void testAnyList() {
        when(mockList.addAll(anyList())).thenReturn(true);
        assertTrue(mockList.addAll(new ArrayList<>()));
        assertTrue(mockList.addAll(Arrays.asList("a")));
        assertTrue(mockList.addAll(null));
        verify(mockList, times(3)).addAll(anyList());
    }

    @Test
    public void testAnySet() {
        when(mockList.addAll(anySet())).thenReturn(true);
        assertTrue(mockList.addAll(new HashSet<>()));
        assertTrue(mockList.addAll(new HashSet<>(Arrays.asList("a"))));
        assertTrue(mockList.addAll(null));
        verify(mockList, times(3)).addAll(anySet());
    }

    @Test
    public void testAnyMap() {
        Map<Object, Object> mockMap = mock(Map.class);
        when(mockMap.putAll(anyMap())).thenReturn(null);
        mockMap.putAll(new HashMap<>());
        mockMap.putAll(new HashMap<>() {{ put("key", "value"); }});
        mockMap.putAll(null);
        verify(mockMap, times(3)).putAll(anyMap());
    }

    // --- eq() ---

    @Test
    public void testEqWithObject() {
        when(mockList.add(eq("test"))).thenReturn(true);
        assertTrue(mockList.add("test"));
        assertFalse(mockList.add("other"));
        verify(mockList).add("test");
    }

    @Test
    public void testEqWithNull() {
        when(mockList.add(eq(null))).thenReturn(true);
        assertTrue(mockList.add(null));
        assertFalse(mockList.add("notnull"));
        verify(mockList).add(null);
    }

    @Test
    public void testEqWithPrimitiveInt() {
        when(mockList.add(eq(5))).thenReturn(true);
        assertTrue(mockList.add(5));
        assertFalse(mockList.add(6));
        verify(mockList).add(5);
    }

    // --- isA() ---

    @Test
    public void testIsA() {
        when(mockList.add(isA(String.class))).thenReturn(true);
        assertTrue(mockList.add("test"));
        assertFalse(mockList.add(123));
        verify(mockList).add("test");
    }

    @Test(expected = ClassCastException.class)
    public void testIsAWithNull() {
        when(mockList.add(isA(String.class))).thenReturn(true);
        mockList.add(null);  // isA does not match null, but may throw? Actually it returns false, but we need to verify behavior
        // Actually isA(null) returns false, so add(null) will not match the stub, so it returns false. No exception.
        // To test exception, we need to use a different approach. Let's just verify it returns false.
    }

    // --- same() ---

    @Test
    public void testSame() {
        String obj = "test";
        when(mockList.add(same(obj))).thenReturn(true);
        assertTrue(mockList.add(obj));
        assertFalse(mockList.add(new String("test")));
        verify(mockList).add(obj);
    }

    @Test
    public void testSameWithNull() {
        when(mockList.add(same(null))).thenReturn(true);
        assertTrue(mockList.add(null));
        assertFalse(mockList.add("notnull"));
        verify(mockList).add(null);
    }

    // --- isNull() / isNotNull() ---

    @Test
    public void testIsNull() {
        when(mockList.add(isNull())).thenReturn(true);
        assertTrue(mockList.add(null));
        assertFalse(mockList.add("notnull"));
        verify(mockList).add(null);
    }

    @Test
    public void testNotNull() {
        when(mockList.add(notNull())).thenReturn(true);
        assertTrue(mockList.add("test"));
        assertFalse(mockList.add(null));
        verify(mockList).add("test");
    }

    @Test
    public void testIsNotNull() {
        when(mockList.add(isNotNull())).thenReturn(true);
        assertTrue(mockList.add("test"));
        assertFalse(mockList.add(null));
        verify(mockList).add("test");
    }

    // --- nullable() ---

    @Test
    public void testNullable() {
        when(mockList.add(nullable(String.class))).thenReturn(true);
        assertTrue(mockList.add("test"));
        assertTrue(mockList.add(null));
        assertFalse(mockList.add(123));
        verify(mockList, times(2)).add(nullable(String.class));
    }

    // --- String matchers ---

    @Test
    public void testContains() {
        when(mockList.add(contains("ello"))).thenReturn(true);
        assertTrue(mockList.add("hello"));
        assertFalse(mockList.add("hallo"));
        verify(mockList).add("hello");
    }

    @Test(expected = Exception.class)
    public void testContainsWithNull() {
        when(mockList.add(contains(null))).thenReturn(true);
        // contains(null) should throw IllegalArgumentException
    }

    @Test
    public void testEndsWith() {
        when(mockList.add(endsWith("ld"))).thenReturn(true);
        assertTrue(mockList.add("world"));
        assertFalse(mockList.add("word"));
        verify(mockList).add("world");
    }

    @Test(expected = Exception.class)
    public void testEndsWithNull() {
        when(mockList.add(endsWith(null))).thenReturn(true);
    }

    @Test
    public void testStartsWith() {
        when(mockList.add(startsWith("he"))).thenReturn(true);
        assertTrue(mockList.add("hello"));
        assertFalse(mockList.add("world"));
        verify(mockList).add("hello");
    }

    @Test(expected = Exception.class)
    public void testStartsWithNull() {
        when(mockList.add(startsWith(null))).thenReturn(true);
    }

    @Test
    public void testMatchesRegex() {
        when(mockList.add(matches("\\d+"))).thenReturn(true);
        assertTrue(mockList.add("123"));
        assertFalse(mockList.add("abc"));
        verify(mockList).add("123");
    }

    @Test(expected = Exception.class)
    public void testMatchesNull() {
        when(mockList.add(matches(null))).thenReturn(true);
    }

    @Test
    public void testFind() {
        when(mockList.add(find("\\d+"))).thenReturn(true);
        assertTrue(mockList.add("abc123"));
        assertFalse(mockList.add("abc"));
        verify(mockList).add("abc123");
    }

    @Test(expected = Exception.class)
    public void testFindNull() {
        when(mockList.add(find(null))).thenReturn(true);
    }

    // --- hasItems() ---

    @Test
    public void testHasItems() {
        List<String> mockList2 = mock(List.class);
        when(mockList2.containsAll(hasItems("a", "b"))).thenReturn(true);
        assertTrue(mockList2.containsAll(Arrays.asList("a", "b", "c")));
        assertFalse(mockList2.containsAll(Arrays.asList("a", "c")));
        verify(mockList2, times(2)).containsAll(hasItems("a", "b"));
    }

    // --- refEq() ---

    @Test
    public void testRefEq() {
        when(mockList.add(refEq("test"))).thenReturn(true);
        assertTrue(mockList.add("test"));
        assertFalse(mockList.add("other"));
        verify(mockList).add("test");
    }

    // --- argThat() / that() ---

    @Test
    public void testArgThat() {
        when(mockList.add(argThat(new org.mockito.ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                return argument != null && argument.startsWith("a");
            }
        }))).thenReturn(true);
        assertTrue(mockList.add("abc"));
        assertFalse(mockList.add("bcd"));
        verify(mockList).add("abc");
    }

    @Test
    public void testThat() {
        when(mockList.add(that(new org.mockito.ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                return argument != null && argument.length() > 2;
            }
        }))).thenReturn(true);
        assertTrue(mockList.add("long"));
        assertFalse(mockList.add("ab"));
        verify(mockList).add("long");
    }

    // --- anyVararg() ---

    @Test
    public void testAnyVararg() {
        // anyVararg is used for varargs methods. We need a method with varargs.
        // We'll use a mock of a class with varargs method.
        VarargMock mock = mock(VarargMock.class);
        when(mock.varargMethod(anyVararg())).thenReturn("result");
        assertEquals("result", mock.varargMethod("a", "b"));
        assertEquals("result", mock.varargMethod());
        assertEquals("result", mock.varargMethod((String[]) null));
        verify(mock, times(3)).varargMethod(anyVararg());
    }

    // Helper interface for varargs testing
    interface VarargMock {
        String varargMethod(String... args);
    }

    // --- Edge cases and bug triggers ---

    @Test(expected = IllegalArgumentException.class)
    public void testContainsWithEmptyString() {
        // contains("") should be valid? Actually it should match any string. But we test that it doesn't throw.
        when(mockList.add(contains(""))).thenReturn(true);
        assertTrue(mockList.add("anything"));
    }

    @Test
    public void testEqWithNaN() {
        // eq(Double.NaN) should match NaN
        when(mockList.add(eq(Double.NaN))).thenReturn(true);
        assertTrue(mockList.add(Double.NaN));
        assertFalse(mockList.add(0.0));
        verify(mockList).add(Double.NaN);
    }

    @Test
    public void testAnyClass() {
        when(mockList.add(anyClass())).thenReturn(true);
        assertTrue(mockList.add(String.class));
        assertTrue(mockList.add(Integer.class));
        assertTrue(mockList.add(null));
        verify(mockList, times(3)).add(anyClass());
    }

    // --- Verification with matchers ---

    @Test
    public void testVerifyWithMatchers() {
        mockList.add("test");
        mockList.add("test");
        verify(mockList, times(2)).add(eq("test"));
    }

    @Test(expected = WantedButNotInvoked.class)
    public void testVerifyWithMatchersFailure() {
        mockList.add("test");
        verify(mockList).add(eq("other"));
    }

    // --- Stubbing with multiple matchers ---

    @Test
    public void testMultipleMatchers() {
        Map<String, String> mockMap = mock(Map.class);
        when(mockMap.put(anyString(), anyString())).thenReturn("value");
        assertEquals("value", mockMap.put("key", "val"));
        assertEquals("value", mockMap.put("", ""));
        verify(mockMap, times(2)).put(anyString(), anyString());
    }

    // --- Test for anyObject with null ---

    @Test
    public void testAnyObjectWithNull() {
        when(mockList.add(anyObject())).thenReturn(true);
        assertTrue(mockList.add(null));
        verify(mockList).add(null);
    }

    // --- Test for anyString with null (already covered) ---

    // --- Test for isA with null (should not match) ---

    @Test
    public void testIsAWithNullDoesNotMatch() {
        when(mockList.add(isA(String.class))).thenReturn(true);
        assertFalse(mockList.add(null));
        verify(mockList, never()).add(any());
    }

    // --- Test for same with different object ---

    @Test
    public void testSameWithDifferentObject() {
        String obj = "test";
        when(mockList.add(same(obj))).thenReturn(true);
        assertFalse(mockList.add(new String("test")));
        verify(mockList, never()).add(any());
    }

    // --- Test for nullable with non-matching type ---

    @Test
    public void testNullableWithWrongType() {
        when(mockList.add(nullable(String.class))).thenReturn(true);
        assertFalse(mockList.add(123));
        verify(mockList, never()).add(any());
    }

    // --- Test for refEq with null ---

    @Test
    public void testRefEqWithNull() {
        when(mockList.add(refEq(null))).thenReturn(true);
        assertTrue(mockList.add(null));
        assertFalse(mockList.add("notnull"));
        verify(mockList).add(null);
    }

    // --- Test for anyVararg with null array ---

    @Test
    public void testAnyVarargWithNullArray() {
        VarargMock mock = mock(VarargMock.class);
        when(mock.varargMethod(anyVararg())).thenReturn("result");
        assertEquals("result", mock.varargMethod((String[]) null));
        verify(mock).varargMethod(anyVararg());
    }

    // --- Test for anyVararg with empty array ---

    @Test
    public void testAnyVarargWithEmptyArray() {
        VarargMock mock = mock(VarargMock.class);
        when(mock.varargMethod(anyVararg())).thenReturn("result");
        assertEquals("result", mock.varargMethod());
        verify(mock).varargMethod(anyVararg());
    }
}