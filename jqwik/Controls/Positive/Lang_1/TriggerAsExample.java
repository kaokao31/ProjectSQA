package org.apache.commons.lang3.math;

import net.jqwik.api.*;
import static org.junit.Assert.*;

/** POSITIVE CONTROL: the Defects4J trigger test (NumberUtilsTest::TestLang747) as a jqwik @Example.
 *  The pipeline MUST report fault_detected=True for this bug. */
public class TriggerAsExample {
    @Example
    void lang747() {
        assertEquals(Long.valueOf(0x80000000L), NumberUtils.createNumber("0x80000000"));
        assertEquals(Long.valueOf(0x800000000L), NumberUtils.createNumber("0x800000000"));
        assertEquals(Integer.valueOf(0x7FFFFFFF), NumberUtils.createNumber("0x07FFFFFFF"));
    }
}
