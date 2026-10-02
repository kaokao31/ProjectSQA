package org.apache.commons.lang3.math;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.Assert.*;

/**
 * NOTE: example written by Claude while validating the pipeline, with knowledge of the bug.
 *       Decide as a team whether to keep it or re-write it blind (see jqwik/README.md rule 1).
 *
 * createNumber picks Float / Double / BigDecimal for a decimal string. A Float only carries ~7 significant
 * decimal digits, so a string with more digits after the decimal point must not be squeezed into a Float.
 *
 * History (kept on purpose for the report): two earlier, too-strong versions of this property
 * ("doubleValue() must equal", "every digit must be kept") failed on the FIXED version too and were
 * rejected by the pipeline as FALSE_ALARM_ON_FIXED.
 */
public class NumberUtilsPrecisionProperties {

    @Property
    void manyDecimalsAreNotFloat(@ForAll @IntRange(min = 0, max = 99999) int intPart,
                                 @ForAll @StringLength(min = 8, max = 15) @CharRange(from = '0', to = '9') String decimals) {
        Assume.that(decimals.charAt(decimals.length() - 1) != '0');
        String s = intPart + "." + decimals;
        Number n = NumberUtils.createNumber(s);
        assertFalse(s + " returned as Float " + n, n instanceof Float);
    }
}
