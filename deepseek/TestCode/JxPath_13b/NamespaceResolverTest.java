package org.apache.commons.jxpath.ri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class NamespaceResolverTest {

    @Test
    public void testNewResolverHasNoNamespaces() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertNull(resolver.getNamespaceURI("p"));
        assertNull(resolver.getPrefix("urn:test"));
        assertNull(resolver.getNamespaceMap());
    }

    @Test
    public void testRegisterNamespace() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        assertEquals("urn:a", resolver.getNamespaceURI("a"));
        assertEquals("a", resolver.getPrefix("urn:a"));
    }

    @Test
    public void testRegisterNamespaceWhenMapWasNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("x", "urn:x");
        assertNotNull(resolver.getNamespaceMap());
        assertEquals("urn:x", resolver.getNamespaceURI("x"));
    }

    @Test
    public void testRegisterDefaultEmptyPrefix() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("", "urn:default");
        assertEquals("urn:default", resolver.getNamespaceURI(""));
        assertEquals("", resolver.getPrefix("urn:default"));
    }

    @Test
    public void testUnregisterNamespace() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        resolver.unregisterNamespace("a");
        assertNull(resolver.getNamespaceURI("a"));
        assertNull(resolver.getPrefix("urn:a"));
    }

    @Test
    public void testUnregisterNamespaceWhenMapIsNullDoesNotThrow() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.unregisterNamespace("missing");
        assertNull(resolver.getNamespaceURI("missing"));
    }

    @Test
    public void testSetNamespaceMap() {
        NamespaceResolver resolver = new NamespaceResolver();
        Map<String, String> map = new HashMap<String, String>();
        map.put("x", "urn:x");
        resolver.setNamespaceMap(map);
        assertEquals(map, resolver.getNamespaceMap());
        assertEquals("urn:x", resolver.getNamespaceURI("x"));
        assertEquals("x", resolver.getPrefix("urn:x"));
    }

    @Test
    public void testSetNamespaceMapNullThenRegister() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.setNamespaceMap(null);
        resolver.registerNamespace("y", "urn:y");
        assertEquals("urn:y", resolver.getNamespaceURI("y"));
    }

    @Test
    public void testGetNamespaceURIWithNullPrefixReturnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("p", "urn:p");
        assertNull(resolver.getNamespaceURI(null));
    }

    @Test
    public void testGetPrefixWithNullNamespaceURIReturnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("p", "urn:p");
        assertNull(resolver.getPrefix(null));
    }

    @Test
    public void testGetPrefixIgnoresNullValuesInNamespaceMap() {
        NamespaceResolver resolver = new NamespaceResolver();
        Map<String, String> map = new HashMap<String, String>();
        map.put("bad", null);
        resolver.setNamespaceMap(map);
        assertNull(resolver.getPrefix("urn:any"));
    }

    @Test
    public void testGetPrefixWithMultipleNamespaces() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        resolver.registerNamespace("b", "urn:b");
        assertEquals("a", resolver.getPrefix("urn:a"));
        assertEquals("b", resolver.getPrefix("urn:b"));
    }

    @Test
    public void testParentDelegatesNamespaceURI() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "urn:p");
        NamespaceResolver child = new NamespaceResolver(parent);
        assertEquals("urn:p", child.getNamespaceURI("p"));
    }

    @Test
    public void testParentDelegatesPrefix() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "urn:p");
        NamespaceResolver child = new NamespaceResolver(parent);
        assertEquals("p", child.getPrefix("urn:p"));
    }

    @Test
    public void testChildOverridesParentNamespaceURI() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "urn:parent");
        NamespaceResolver child = new NamespaceResolver(parent);
        child.registerNamespace("p", "urn:child");
        assertEquals("urn:child", child.getNamespaceURI("p"));
        assertEquals("urn:parent", parent.getNamespaceURI("p"));
    }

    @Test
    public void testChildOverridesParentPrefix() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "urn:shared");
        NamespaceResolver child = new NamespaceResolver(parent);
        child.registerNamespace("c", "urn:shared");
        assertEquals("c", child.getPrefix("urn:shared"));
    }

    @Test
    public void testGetNamespaceURIFromParentWhenChildMapExists() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "urn:p");
        NamespaceResolver child = new NamespaceResolver(parent);
        child.registerNamespace("c", "urn:c");
        assertEquals("urn:p", child.getNamespaceURI("p"));
        assertEquals("urn:c", child.getNamespaceURI("c"));
    }

    @Test
    public void testGetPrefixFromParentWhenChildMapExists() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "urn:p");
        NamespaceResolver child = new NamespaceResolver(parent);
        child.registerNamespace("c", "urn:c");
        assertEquals("p", child.getPrefix("urn:p"));
        assertEquals("c", child.getPrefix("urn:c"));
    }

    @Test
    public void testUnregisterNamespaceDoesNotAffectParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "urn:p");
        NamespaceResolver child = new NamespaceResolver(parent);
        child.unregisterNamespace("p");
        assertEquals("urn:p", child.getNamespaceURI("p"));
    }

    @Test
    public void testGetPrefixNotFoundReturnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        assertNull(resolver.getPrefix("urn:missing"));
    }

    @Test
    public void testGetNamespaceURINotFoundReturnsNull() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        assertNull(resolver.getNamespaceURI("missing"));
    }

    @Test
    public void testSealSetsSealedFlag() {
        NamespaceResolver resolver = new NamespaceResolver();
        assertFalse(resolver.isSealed());
        resolver.seal();
        assertTrue(resolver.isSealed());
    }

    @Test(expected = IllegalStateException.class)
    public void testSealPreventsRegisterNamespace() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        resolver.registerNamespace("a", "urn:a");
    }

    @Test(expected = IllegalStateException.class)
    public void testSealPreventsUnregisterNamespace() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        resolver.seal();
        resolver.unregisterNamespace("a");
    }

    @Test(expected = IllegalStateException.class)
    public void testSealPreventsSetNamespaceMap() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.seal();
        resolver.setNamespaceMap(new HashMap<String, String>());
    }

    @Test
    public void testGetMethodsStillWorkAfterSeal() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        resolver.seal();
        assertEquals("urn:a", resolver.getNamespaceURI("a"));
        assertEquals("a", resolver.getPrefix("urn:a"));
    }

    @Test
    public void testCloneCopiesNamespaces() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        NamespaceResolver clone = (NamespaceResolver) resolver.clone();
        assertNotNull(clone);
        assertEquals("urn:a", clone.getNamespaceURI("a"));
        assertEquals("a", clone.getPrefix("urn:a"));
    }

    @Test
    public void testCloneIsDeepCopy() {
        NamespaceResolver resolver = new NamespaceResolver();
        resolver.registerNamespace("a", "urn:a");
        NamespaceResolver clone = (NamespaceResolver) resolver.clone();
        clone.registerNamespace("b", "urn:b");
        assertNull(resolver.getNamespaceURI("b"));
        resolver.registerNamespace("c", "urn:c");
        assertNull(clone.getNamespaceURI("c"));
    }

    @Test
    public void testCloneOfResolverWithNoNamespaces() {
        NamespaceResolver resolver = new NamespaceResolver();
        NamespaceResolver clone = (NamespaceResolver) resolver.clone();
        assertNull(clone.getNamespaceURI("a"));
        assertNull(clone.getPrefix("urn:a"));
    }

    @Test
    public void testClonePreservesParent() {
        NamespaceResolver parent = new NamespaceResolver();
        parent.registerNamespace("p", "urn:p");
        NamespaceResolver child = new NamespaceResolver(parent);
        NamespaceResolver clone = (NamespaceResolver) child.clone();
        assertEquals("urn:p", clone.getNamespaceURI("p"));
        assertEquals("p", clone.getPrefix("urn:p"));
    }
}