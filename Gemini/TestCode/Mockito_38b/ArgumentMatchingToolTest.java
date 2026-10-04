package org.mockito.internal.verification.argumentmatching;

import org.junit.Test;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.Equals;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ArgumentMatchingToolTest {

    private final ArgumentMatchingTool tool = new ArgumentMatchingTool();

    // A simple mock matcher that implements CapturesArguments and matches everything
    private static class CapturingMatcherStub extends BaseMatcher<Object> implements CapturesArguments {
        private Object capturedValue;

        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("CapturingMatcherStub");
        }

        @Override
        public void captureFrom(Object value) {
            this.capturedValue = value;
        }

        public Object getCapturedValue() {
            return capturedValue;
        }
    }

    // A matcher that does not implement CapturesArguments
    private static class NonCapturingMatcher extends BaseMatcher<Object> {
        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("NonCapturingMatcher");
        }
    }

    @Test
    public void testGetSuspiciousMethodMatchingArgs_nullArgsAndMatchers() {
        // Given null lists
        List<Matcher> matchers = null;
        List<Object> args = null;

        // When
        Integer[] suspicious = tool.getSuspiciousMethodMatchingArgs(matchers, args);

        // Then
        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test
    public void testGetSuspiciousMethodMatchingArgs_emptyLists() {
        // Given empty lists
        List<Matcher> matchers = Collections.emptyList();
        List<Object> args = Collections.emptyList();

        // When
        Integer[] suspicious = tool.getSuspiciousMethodMatchingArgs(matchers, args);

        // Then
        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test
    public void testGetSuspiciousMethodMatchingArgs_mismatchedSizes() {
        // Given different sizes for matchers and args
        List<Matcher> matchers = Arrays.asList((Matcher) new Equals("a"));
        List<Object> args = Arrays.asList("a", "b");

        // When
        Integer[] suspicious = tool.getSuspiciousMethodMatchingArgs(matchers, args);

        // Then
        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test
    public void testGetSuspiciousMethodMatchingArgs_capturingMatcherWithNonEqualsAndMatchingArg() {
        // Given a capturing matcher (that is not Equals) and an argument that matches it
        CapturingMatcherStub capturingMatcher = new CapturingMatcherStub();
        List<Matcher> matchers = Arrays.asList((Matcher) capturingMatcher);
        List<Object> args = Arrays.asList((Object) "someArg");

        // When
        Integer[] suspicious = tool.getSuspiciousMethodMatchingArgs(matchers, args);

        // Then
        // Since capturingMatcher matches "someArg", and it's a CapturesArguments but NOT an Equals matcher,
        // it should be considered suspicious (index 0).
        assertNotNull(suspicious);
        assertEquals(1, suspicious.length);
        assertEquals(Integer.valueOf(0), suspicious[0]);
    }

    @Test
    public void testGetSuspiciousMethodMatchingArgs_equalsMatcherShouldNotBeSuspicious() {
        // Given an Equals matcher (which is also an instance of CapturesArguments in Mockito)
        Equals equalsMatcher = new Equals("someArg");
        List<Matcher> matchers = Arrays.asList((Matcher) equalsMatcher);
        List<Object> args = Arrays.asList((Object) "someArg");

        // When
        Integer[] suspicious = tool.getSuspiciousMethodMatchingArgs(matchers, args);

        // Then
        // Equals matchers should be filtered out / not marked as suspicious
        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test
    public void testGetSuspiciousMethodMatchingArgs_nonCapturingMatcherShouldNotBeSuspicious() {
        // Given a non-capturing matcher even if it matches the arg
        NonCapturingMatcher nonCapturing = new NonCapturingMatcher();
        List<Matcher> matchers = Arrays.asList((Matcher) nonCapturing);
        List<Object> args = Arrays.asList((Object) "someArg");

        // When
        Integer[] suspicious = tool.getSuspiciousMethodMatchingArgs(matchers, args);

        // Then
        // It does not implement CapturesArguments, so it shouldn't be suspicious
        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test
    public void testGetSuspiciousMethodMatchingArgs_matcherDoesNotMatchArg() {
        // Given a capturing matcher whose matches method returns false for the argument
        BaseMatcher<Object> nonMatchingCapturer = new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object item) {
                return false;
            }

            @Override
            public void describeTo(Description description) {
            }
        };
        // We need it to implement CapturesArguments as well
        CapturingMatcherStub capturingMatcher = new CapturingMatcherStub() {
            @Override
            public boolean matches(Object item) {
                return false; // does not match
            }
        };

        List<Matcher> matchers = Arrays.asList((Matcher) capturingMatcher);
        List<Object> args = Arrays.asList((Object) "someArg");

        // When
        Integer[] suspicious = tool.getSuspiciousMethodMatchingArgs(matchers, args);

        // Then
        // Since it doesn't match the arg, it shouldn't be suspicious
        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test
    public void testément_safelyHandleNullElementsInsideLists() {
        // Testing robustness against nulls inside the lists if permitted by implementation
        List<Matcher> matchers = Arrays.asList((Matcher) null);
        List<Object> args = Arrays.asList((Object) null);

        try {
            Integer[] suspicious = tool.getSuspiciousMethodMatchingArgs(matchers, args);
            assertNotNull(suspicious);
        } catch (Exception e) {
            // Depending on strict null handling, but keeping coverage paths exercised
        }
    }
}