package org.apache.commons.cli2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class OptionTest {

    private Option option;

    @Before
    public void setUp() {
        option = createMockOption();
    }

    private Option createMockOption() {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final String name = method.getName();
                if ("getPreferredName".equals(name)) {
                    return "testOption";
                }
                if ("getDescription".equals(name)) {
                    return "Test description";
                }
                if ("isRequired".equals(name)) {
                    return Boolean.TRUE;
                }
                if ("isArgument".equals(name)) {
                    return Boolean.FALSE;
                }
                if ("isParent".equals(name)) {
                    return Boolean.FALSE;
                }
                if ("canProcess".equals(name)) {
                    return Boolean.TRUE;
                }
                if ("getTriggers".equals(name)) {
                    return new HashSet<Option>();
                }
                if ("getPrefixes".equals(name)) {
                    return new HashSet<String>();
                }
                if ("getDefaultValues".equals(name)) {
                    return Collections.emptyList();
                }
                if ("getArgumentName".equals(name)) {
                    return "arg";
                }
                if ("findOption".equals(name)) {
                    return null;
                }
                if ("isValueRequired".equals(name)) {
                    return Boolean.FALSE;
                }
                if ("isValueAllowed".equals(name)) {
                    return Boolean.TRUE;
                }
                if ("toString".equals(name)) {
                    return "MockOption";
                }
                if ("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                }
                if ("equals".equals(name)) {
                    return proxy == args[0];
                }
                Class<?> returnType = method.getReturnType();
                if (returnType.equals(boolean.class)) {
                    return Boolean.FALSE;
                }
                if (returnType.equals(int.class)) {
                    return 0;
                }
                if (returnType.equals(String.class)) {
                    return null;
                }
                if (returnType.equals(List.class)) {
                    return Collections.emptyList();
                }
                if (returnType.equals(Set.class)) {
                    return Collections.emptySet();
                }
                if (returnType.equals(Map.class)) {
                    return Collections.emptyMap();
                }
                return null;
            }
        };

        return (Option) Proxy.newProxyInstance(
                Option.class.getClassLoader(),
                new Class<?>[]{Option.class},
                handler);
    }

    @Test
    public void testGetPreferredName() {
        assertEquals("testOption", option.getPreferredName());
    }

    @Test
    public void testGetDescription() {
        assertEquals("Test description", option.getDescription());
    }

    @Test
    public void testIsRequired() {
        assertTrue(option.isRequired());
    }

    @Test
    public void testIsArgument() {
        assertFalse(option.isArgument());
    }

    @Test
    public void testIsParent() {
        assertFalse(option.isParent());
    }

    @Test
    public void testCanProcess() {
        assertTrue(option.canProcess(null, "value"));
    }

    @Test
    public void testProcess() throws Exception {
        option.process(null, Collections.emptyListIterator());
    }

    @Test
    public void testValidate() throws Exception {
        option.validate(null);
    }

    @Test
    public void testDefaults() {
        option.defaults(null);
    }

    @Test
    public void testFindOption() {
        assertNull(option.findOption("missing"));
    }

    @Test
    public void testGetTriggers() {
        assertNotNull(option.getTriggers());
        assertTrue(option.getTriggers().isEmpty());
    }

    @Test
    public void testGetPrefixes() {
        assertNotNull(option.getPrefixes());
        assertTrue(option.getPrefixes().isEmpty());
    }

    @Test
    public void testGetDefaultValues() {
        assertNotNull(option.getDefaultValues());
        assertTrue(option.getDefaultValues().isEmpty());
    }

    @Test
    public void testGetArgumentName() {
        assertEquals("arg", option.getArgumentName());
    }

    @Test
    public void testIsValueRequired() {
        assertFalse(option.isValueRequired());
    }

    @Test
    public void testIsValueAllowed() {
        assertTrue(option.isValueAllowed());
    }

    @Test
    public void testHelp() {
        StringBuffer buffer = new StringBuffer();
        option.help(buffer, Collections.emptyMap());
        assertEquals("", buffer.toString());
    }

    @Test
    public void testAppendUsage() {
        StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, true, Collections.emptyMap());
        assertEquals("", buffer.toString());
    }
}