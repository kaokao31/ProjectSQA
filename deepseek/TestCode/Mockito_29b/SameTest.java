package org.mockito.internal.matchers;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for {@link Same} matcher.
 * Designed to achieve high code coverage and detect potential faults,
 * especially the known bug (ID 29) where describeTo throws NPE for null expected.
 */
public class SameTest {

    private Object expected;
    private Same sameMatcher;

    @Before
    public void setUp() {
        // Default setup, can be overridden in specific tests
        expected = new Object();
        sameMatcher = new Same(expected);
    }

    // --- matches() tests ---

    @Test
    public void testMatchesWithSameObject() {
        assertTrue("Should match the same object", sameMatcher.matches(expected));
    }

    @Test
    public void testMatchesWithDifferentObject() {
        Object different = new Object();
        assertFalse("Should not match a different object", sameMatcher.matches(different));
    }

    @Test
    public void testMatchesWithNullExpectedAndNullActual() {
        Same nullSame = new Same(null);
        assertTrue("Null expected should match null actual", nullSame.matches(null));
    }

    @Test
    public void testMatchesWithNullExpectedAndNonNullActual() {
        Same nullSame = new Same(null);
        assertFalse("Null expected should not match non-null actual", nullSame.matches(new Object()));
    }

    @Test
    public void testMatchesWithNonNullExpectedAndNullActual() {
        assertFalse("Non-null expected should not match null actual", sameMatcher.matches(null));
    }

    @Test
    public void testMatchesWithBothNull() {
        Same nullSame = new Same(null);
        assertTrue("Both null should match", nullSame.matches(null));
    }

    // --- describeTo() tests ---

    @Test
    public void testDescribeToWithNonNullExpected() {
        StringDescription description = new StringDescription();
        sameMatcher.describeTo(description);
        String expectedStr = expected.toString();
        assertTrue("Description should contain expected toString",
                description.toString().contains(expectedStr));
        // Also verify it starts with "same(" and ends with ")"
        assertTrue("Description should start with 'same('",
                description.toString().startsWith("same("));
        assertTrue("Description should end with ')'",
                description.toString().endsWith(")"));
    }

    @Test
    public void testDescribeToWithNullExpected() {
        Same nullSame = new Same(null);
        StringDescription description = new StringDescription();
        // This should not throw NullPointerException (bug ID 29)
        nullSame.describeTo(description);
        assertEquals("Description for null expected should be 'same(null)'",
                "same(null)", description.toString());
    }

    @Test
    public void testDescribeToWithNullExpectedAndEmptyDescription() {
        Same nullSame = new Same(null);
        StringDescription description = new StringDescription();
        nullSame.describeTo(description);
        assertNotNull("Description should not be null", description.toString());
        assertEquals("Description should be exactly 'same(null)'",
                "same(null)", description.toString());
    }

    // --- Constructor tests ---

    @Test
    public void testConstructorWithNull() {
        try {
            new Same(null);
            // Success: no exception
        } catch (Exception e) {
            fail("Constructor should not throw exception for null argument");
        }
    }

    @Test
    public void testConstructorWithNonNull() {
        try {
            new Same(new Object());
            // Success: no exception
        } catch (Exception e) {
            fail("Constructor should not throw exception for non-null argument");
        }
    }

    // --- Edge cases and additional coverage ---

    @Test
    public void testMatchesWithSameObjectAfterMultipleCalls() {
        assertTrue("First call should match", sameMatcher.matches(expected));
        assertTrue("Second call should still match", sameMatcher.matches(expected));
    }

    @Test
    public void testMatchesWithNullExpectedAndNullActualMultipleCalls() {
        Same nullSame = new Same(null);
        assertTrue("First call with null", nullSame.matches(null));
        assertTrue("Second call with null", nullSame.matches(null));
    }

    @Test
    public void testDescribeToWithNonNullExpectedAndMultipleAppends() {
        StringDescription description = new StringDescription();
        sameMatcher.describeTo(description);
        // Ensure description is not empty
        assertFalse("Description should not be empty", description.toString().isEmpty());
    }

    // --- Helper class for Description ---

    private static class StringDescription implements Description {
        private final StringBuilder text = new StringBuilder();

        @Override
        public Description appendText(String value) {
            text.append(value);
            return this;
        }

        @Override
        public Description appendList(String start, String separator, String end, Iterable<?> list) {
            // Not used by Same, but implement to avoid compilation errors
            text.append(start);
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    text.append(separator);
                }
                text.append(item);
                first = false;
            }
            text.append(end);
            return this;
        }

        @Override
        public String toString() {
            return text.toString();
        }
    }
}