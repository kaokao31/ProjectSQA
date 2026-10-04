package org.apache.commons.validator.routines;

import org.junit.Test;
import static org.junit.Assert.*;

public class TypeCheckTest {

    @Test
    public void testGetInstance() {
        TypeCheck checker = TypeCheck.getInstance();
        assertNotNull(checker);
        assertSame(TypeCheck.getInstance(), checker);
    }

    @Test
    public void testValidate() {
        TypeCheck checker = new TypeCheck();
        
        // Exercise the method with various objects to cover branches /logic
        assertTrue(checker.validate("test", null));
        assertTrue(checker.validate(Integer.valueOf(123), null));
        assertTrue(checker.validate(null, null));
    }
}