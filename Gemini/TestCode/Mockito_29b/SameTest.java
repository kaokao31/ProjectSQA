package org.mockito.internal.matchers;

import org.junit.Test;
import org.mockito.internal.matchers.Same;
import org.hamcrest.StringDescription;

import static org.junit.Assert.*;

public class SameTest {

    @Test
    public void testSameObject() {
        Object obj = new Object();
        Same matcher = new Same(obj);

        assertTrue(matcher.matches(obj));
    }

    @Test
    public void testDifferentObjectSameValue() {
        // Creates two different object instances with the same content/value
        String obj1 = new String("test");
        String obj2 = new String("test");

        Same matcher = new Same(obj1);

        // Even though values are equal, references are different so matches() should return false
        assertFalse(matcher.matches(obj2));
    }

    @Test
    public void testNullArgument() {
        Object obj = new Object();
        Same matcher = new Same(obj);

        assertFalse(matcher.matches(null));
    }

    @Test
    public void testMatcherWithNullTarget() {
        Same matcher = new Same(null);

        assertTrue(matcher.matches(null));
        assertFalse(matcher.matches(new Object()));
    }

    @Test
    public void testDescribeTo() {
        Object obj = "SampleObject";
        Same matcher = new Same(obj);
        StringDescription description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("same(" + obj + ")", description.toString());
    }
}