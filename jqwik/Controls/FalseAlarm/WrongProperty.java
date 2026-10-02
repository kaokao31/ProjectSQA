package pbt.controls;

import net.jqwik.api.*;

import static org.junit.Assert.*;

/** FALSE-ALARM CONTROL: a wrong property fails on BOTH versions -> must be reported as
 *  FALSE_ALARM_ON_FIXED and must NOT count as fault detected. */
public class WrongProperty {
    @Property
    void everyIntIsPositive(@ForAll int a) {
        assertTrue(a > 0);
    }
}
