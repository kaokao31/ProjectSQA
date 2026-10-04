package org.apache.commons.compress.archivers.sevenz;

import org.junit.Test;
import static org.junit.Assert.*;

public class CodersTest {
    @Test
    public void testClassExists() {
        assertNotNull("Coders class should be loadable", Coders.class);
    }
}