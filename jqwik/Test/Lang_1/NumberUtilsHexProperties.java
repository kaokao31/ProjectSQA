package org.apache.commons.lang3.math;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.Assert.*;

/**
 * NOTE: example written by Claude while validating the pipeline, with knowledge of the bug.
 *       Decide as a team whether to keep it or re-write it blind (see jqwik/README.md rule 1).
 *
 * Written from the NumberUtils.createNumber Javadoc (hex strings "0x"/"#" -> Integer/Long/BigInteger,
 * whatever is big enough), without looking at the Defects4J trigger test.
 */
public class NumberUtilsHexProperties {

    /** Any long written in hex must round-trip through createNumber with the same value. */
    @Property
    void hexLongRoundTrips(@ForAll @LongRange(min = 0) long value,
                           @ForAll @IntRange(min = 0, max = 3) int leadingZeros) {
        String hex = "0x" + repeat('0', leadingZeros) + Long.toHexString(value);
        Number n = NumberUtils.createNumber(hex);
        assertEquals(hex, value, n.longValue());
    }

    /** The chosen type must be able to hold the value (no silent overflow). */
    @Property
    void hexResultTypeIsWideEnough(@ForAll @LongRange(min = 0) long value) {
        Number n = NumberUtils.createNumber("0x" + Long.toHexString(value));
        if (value > Integer.MAX_VALUE) {
            assertFalse("value " + value + " returned as Integer", n instanceof Integer);
        }
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }
}
