package org.apache.commons.cli2.builder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PatternBuilderTest {

    private PatternBuilder patternBuilder;

    @Before
    public void setUp() {
        patternBuilder = new PatternBuilder();
    }

    @Test
    public void testEmptyPattern() {
        patternBuilder.withPattern("");
        // Ensure no exception is thrown and builder functions correctly with empty string
        assertNotNull(patternBuilder.create());
    }

    @Test(expected = NullPointerException.class)
    public void testNullPattern() {
        patternBuilder.withPattern(null);
    }

    @Test
    public void testSimpleFlagOption() {
        // 'a' without following characters: should create a switch/flag option
        patternBuilder.withPattern("a");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testOptionWithColon() {
        // 'a:' means 'a' takes an argument
        patternBuilder.withPattern("a:");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testOptionWithDoubleColon() {
        // 'a::' means 'a' takes an optional argument
        patternBuilder.withPattern("a::");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testOptionWithAt() {
        // 'a@' means 'a' takes multiple values/list
        patternBuilder.withPattern("a@");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testOptionWithPlus() {
        // 'a+' means 'a' is required and takes arguments/values
        patternBuilder.withPattern("a+");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testOptionWithExclamation() {
        // 'a!' means 'a' is required
        patternBuilder.withPattern("a!");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testMultipleOptions() {
        // Combining various flags and modifiers
        patternBuilder.withPattern("a:b!c@d+e#f");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testUnknownPatternCharacter() {
        // Test handling of an unrecognized character in the pattern
        patternBuilder.withPattern("a?");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testMultipleConsecutiveColons() {
        patternBuilder.withPattern("a:::");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testPatternStartingWithModifiers() {
        // Pattern starting directly with a modifier without a preceding option character
        patternBuilder.withPattern(":a");
        assertNotNull(patternBuilder.create());
    }

    @Test
    public void testRepeatedPatternCalls() {
        patternBuilder.withPattern("a");
        assertNotNull(patternBuilder.create());
        
        // Re-use builder with a new pattern
        patternBuilder.withPattern("b:");
        assertNotNull(patternBuilder.create());
    }
}