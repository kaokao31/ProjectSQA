package org.mockito.internal.verification.argumentmatching;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.Matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ArgumentMatchingToolTest {

    private ArgumentMatchingTool tool;

    @Before
    public void setUp() {
        tool = new ArgumentMatchingTool();
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_AllMatch() {
        List<Matcher> matchers = Arrays.asList(
                new Equals("foo"),
                new Equals(123),
                new Equals(true)
        );
        List<Object> arguments = Arrays.asList("foo", 123, true);
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        assertTrue("Expected empty suspicious list when all match", suspicious.isEmpty());
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_OneMismatch() {
        List<Matcher> matchers = Arrays.asList(
                new Equals("foo"),
                new Equals(456),
                new Equals(true)
        );
        List<Object> arguments = Arrays.asList("foo", 123, true);
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        assertEquals("Expected one suspicious argument", 1, suspicious.size());
        assertEquals("Suspicious argument should be 123", 123, suspicious.get(0));
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_AllMismatch() {
        List<Matcher> matchers = Arrays.asList(
                new Equals("bar"),
                new Equals(999),
                new Equals(false)
        );
        List<Object> arguments = Arrays.asList("foo", 123, true);
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        assertEquals("Expected three suspicious arguments", 3, suspicious.size());
        assertEquals("foo", suspicious.get(0));
        assertEquals(123, suspicious.get(1));
        assertEquals(true, suspicious.get(2));
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_EmptyLists() {
        List<Matcher> matchers = Collections.emptyList();
        List<Object> arguments = Collections.emptyList();
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        assertTrue("Expected empty suspicious list for empty inputs", suspicious.isEmpty());
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_MatchersNull() {
        List<Object> arguments = Arrays.asList("a", "b");
        try {
            tool.getSuspiciouslyNotMatchingArgs(null, arguments);
            fail("Expected NullPointerException when matchers list is null");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_ArgumentsNull() {
        List<Matcher> matchers = Arrays.asList(new Equals("a"));
        try {
            tool.getSuspiciouslyNotMatchingArgs(matchers, null);
            fail("Expected NullPointerException when arguments list is null");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_NullMatcherInList() {
        // This test targets the bug: a null matcher in the list should not cause NPE
        List<Matcher> matchers = new ArrayList<>();
        matchers.add(new Equals("foo"));
        matchers.add(null);  // null matcher
        matchers.add(new Equals("baz"));
        List<Object> arguments = Arrays.asList("foo", "bar", "baz");
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        // Depending on implementation, null matcher may be treated as mismatch or skipped.
        // We expect at least the second argument to be suspicious because matcher is null.
        assertFalse("Suspicious list should not be empty when a matcher is null", suspicious.isEmpty());
        assertTrue("Second argument should be suspicious", suspicious.contains("bar"));
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_NullArgumentInList() {
        List<Matcher> matchers = Arrays.asList(new Equals("foo"), new Equals(null), new Equals("baz"));
        List<Object> arguments = Arrays.asList("foo", null, "baz");
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        assertTrue("All arguments match even with null", suspicious.isEmpty());
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_DifferentSizes() {
        List<Matcher> matchers = Arrays.asList(new Equals("a"), new Equals("b"));
        List<Object> arguments = Arrays.asList("a", "b", "c");
        // If sizes differ, behavior may vary; we test that it doesn't throw
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        // Should handle gracefully, likely only compare up to min size
        assertNotNull("Should not return null", suspicious);
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_MatcherToStringThrows() {
        // Matcher that throws on toString() – bug scenario
        Matcher throwingMatcher = new Matcher() {
            @Override
            public boolean matches(Object actual) {
                return false;
            }

            @Override
            public String toString() {
                throw new RuntimeException("toString failure");
            }
        };
        List<Matcher> matchers = Collections.singletonList(throwingMatcher);
        List<Object> arguments = Collections.singletonList("anything");
        // Should not propagate the exception; instead handle gracefully
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        assertFalse("Should have suspicious argument", suspicious.isEmpty());
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_MatcherMatchesNull() {
        Matcher matcher = new Equals(null);
        List<Matcher> matchers = Collections.singletonList(matcher);
        List<Object> arguments = Collections.singletonList(null);
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        assertTrue("Null argument should match null matcher", suspicious.isEmpty());
    }

    @Test
    public void testGetSuspiciouslyNotMatchingArgs_MatcherDoesNotMatchNull() {
        Matcher matcher = new Equals("notnull");
        List<Matcher> matchers = Collections.singletonList(matcher);
        List<Object> arguments = new ArrayList<>();
        arguments.add(null);
        List<Object> suspicious = tool.getSuspiciouslyNotMatchingArgs(matchers, arguments);
        assertEquals("Should have one suspicious argument", 1, suspicious.size());
        assertNull("Suspicious argument should be null", suspicious.get(0));
    }
}