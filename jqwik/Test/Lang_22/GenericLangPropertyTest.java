package org.apache.commons.lang3;

import net.jqwik.api.*;
import static org.junit.Assert.*;

public class GenericLangPropertyTest {

    @Property
    void basicPropertyTest(@ForAll int a, @ForAll int b) {
        assertEquals(a + b, b + a);
    }
}
