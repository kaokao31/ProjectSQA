package some.package; // Replace with actual package from source

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class NamedTypeTest {

    private NamedType emptyNamedType;
    private NamedType sample1;
    private NamedType sample2;

    @Before
    public void setUp() {
        emptyNamedType = new NamedType();
        sample1 = new NamedType("TypeA", "String");
        sample2 = new NamedType("TypeB", "Integer");
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(emptyNamedType);
        assertNull(emptyNamedType.getName());
        assertNull(emptyNamedType.getType());
    }

    @Test
    public void testParameterizedConstructor() {
        NamedType nt = new NamedType("Test", "Long");
        assertEquals("Test", nt.getName());
        assertEquals("Long", nt.getType());
    }

    @Test
    public void testSettersAndGetters() {
        emptyNamedType.setName("NewName");
        emptyNamedType.setType("Boolean");
        assertEquals("NewName", emptyNamedType.getName());
        assertEquals("Boolean", emptyNamedType.getType());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(sample1.equals(sample1));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(sample1.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(sample1.equals("string"));
    }

    @Test
    public void testEqualsDifferentName() {
        NamedType nt = new NamedType("Other", "String");
        assertFalse(sample1.equals(nt));
    }

    @Test
    public void testEqualsDifferentType() {
        NamedType nt = new NamedType("TypeA", "Integer");
        assertFalse(sample1.equals(nt));
    }

    @Test
    public void testEqualsEqualObjects() {
        NamedType nt = new NamedType("TypeA", "String");
        assertTrue(sample1.equals(nt));
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = sample1.hashCode();
        int hash2 = sample1.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCodeDifferentObjects() {
        assertNotEquals(sample1.hashCode(), sample2.hashCode());
    }

    @Test
    public void testToStringNotEmpty() {
        assertNotNull(sample1.toString());
        assertFalse(sample1.toString().isEmpty());
    }

    @Test
    public void testNullNameEquality() {
        NamedType nt1 = new NamedType(null, "Type");
        NamedType nt2 = new NamedType(null, "Type");
        assertTrue(nt1.equals(nt2));
    }

    @Test
    public void testNullTypeEquality() {
        NamedType nt1 = new NamedType("Name", null);
        NamedType nt2 = new NamedType("Name", null);
        assertTrue(nt1.equals(nt2));
    }

    @Test
    public void testBothNullEquality() {
        NamedType nt1 = new NamedType(null, null);
        NamedType nt2 = new NamedType(null, null);
        assertTrue(nt1.equals(nt2));
    }

    @Test
    public void testEdgeCaseEmptyStrings() {
        NamedType nt = new NamedType("", "");
        assertEquals("", nt.getName());
        assertEquals("", nt.getType());
    }
}