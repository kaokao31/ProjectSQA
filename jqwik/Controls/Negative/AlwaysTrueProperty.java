package pbt.controls;

import net.jqwik.api.*;

import static org.junit.Assert.*;

/** NEGATIVE CONTROL: never touches the code under test -> must never be counted as "fault detected". */
public class AlwaysTrueProperty {
    @Property
    void additionCommutes(@ForAll int a, @ForAll int b) {
        assertEquals(a + b, b + a);
    }
}
