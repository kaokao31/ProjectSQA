package org.apache.commons.lang3.math;

import net.jqwik.api.*;
import static org.junit.Assert.*;

/** POSITIVE CONTROL: NumberUtilsTest::testStringCreateNumberEnsureNoPrecisionLoss as a jqwik @Example. */
public class TriggerAsExample {
    @Example
    void noPrecisionLoss() {
        assertTrue(NumberUtils.createNumber("1.23") instanceof Float);
        assertTrue(NumberUtils.createNumber("3.40282354e+38") instanceof Double);
        assertTrue(NumberUtils.createNumber("1.797693134862315759e+308") instanceof java.math.BigDecimal);
    }
}
