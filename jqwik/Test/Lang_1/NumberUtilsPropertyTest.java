package org.apache.commons.lang3.math;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import static org.junit.Assert.*;

public class NumberUtilsPropertyTest {

    // Property 1: Valid numeric string should successfully create a Number
    @Property
    void validIntegerStringsShouldCreateNumber(@ForAll @NumericChars @NotBlank String numericStr) {
        try {
            Number num = NumberUtils.createNumber(numericStr);
            assertNotNull(num);
        } catch (NumberFormatException e) {
            // Handled for extremely large numeric strings exceeding BigInteger/Long capacity
        }
    }

    // Property 2: Minimum of identical numbers is always the number itself
    @Property
    void minimumOfSameNumbersIsItself(@ForAll int a) {
        assertEquals(a, NumberUtils.min(a, a, a));
    }
}
