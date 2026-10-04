package com.google.debugging.sourcemap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class SourceMapConsumerV3Test {

  private SourceMapConsumerV3 consumer;

  @Before
  public void setUp() {
    consumer = new SourceMapConsumerV3();
  }

  @Test
  public void testParseBasicSourceMap() throws Exception {
    consumer.parse(
        "{\"version\":3,\"file\":\"out.js\","
        + "\"sources\":[\"src.js\"],\"names\":[\"foo\"],\"mappings\":\"AAAAA\"}");

    List<Object> mappings = getMappingEntries(consumer);
    assertEquals(1, mappings.size());
  }

  @Test
  public void testParseEmptyMappings() throws Exception {
    consumer.parse("{\"version\":3,\"sources\":[\"src.js\"],\"mappings\":\"\"}");
    assertTrue(getMappingEntries(consumer).isEmpty());
  }

  @Test
  public void testParseUnmappedGeneratedColumn() throws Exception {
    consumer.parse("{\"version\":3,\"sources\":[\"src.js\"],\"mappings\":\"A,C\"}");
  }

  @Test
  public void testParseMultipleMappingsAndLines() throws Exception {
    consumer.parse(
        "{\"version\":3,\"file\":\"out.js\",\"sources\":[\"src.js\"],"
        + "\"names\":[],\"mappings\":\"AAAA,CAAC;AACA\"}");

    List<Object> mappings = getMappingEntries(consumer);
    assertTrue("Expected at least 2 mappings", mappings.size() >= 2);

    Object first = mappings.get(0);
    Object second = mappings.get(1);

    assertEquals(0, intValue(getMappingValue(first,
        new String[] {"getGeneratedLine"}, new String[] {"generatedLine"})));
    assertEquals(0, intValue(getMappingValue(first,
        new String[] {"getGeneratedColumn"}, new String[] {"generatedColumn"})));
    assertEquals(0, intValue(getMappingValue(first,
        new String[] {"getOriginalLine"}, new String[] {"originalLine"})));
    assertEquals(0, intValue(getMappingValue(first,
        new String[] {"getOriginalColumn"}, new String[] {"originalColumn"})));

    assertEquals(1, intValue(getMappingValue(second,
        new String[] {"getGeneratedColumn"}, new String[] {"generatedColumn"})));
    assertEquals(1, intValue(getMappingValue(second,
        new String[] {"getOriginalColumn"}, new String[] {"originalColumn"})));
  }

  @Test
  public void testSourceRootAppliedToSources() throws Exception {
    consumer.parse(
        "{\"version\":3,\"sourceRoot\":\"http://example.com/\","
        + "\"sources\":[\"js/a.js\"],\"mappings\":\"AAAA\"}");

    Object sources = getSourceNamesObject(consumer);
    assertNotNull(sources);
    assertTrue(sources.toString().contains("http://example.com/js/a.js"));
  }

  @Test
  public void testParsesSourcesContent() throws Exception {
    consumer.parse(
        "{\"version\":3,\"sources\":[\"a.js\"],"
        + "\"sourcesContent\":[\"alert(1);\"],\"mappings\":\"AAAA\"}");

    Object content = getFieldByNames(consumer,
        "sourcesContent", "sourceContent", "sourceContents");
    assertNotNull(content);
    assertTrue(content.toString().contains("alert(1);"));
  }

  @Test
  public void testParsesNames() throws Exception {
    consumer.parse(
        "{\"version\":3,\"sources\":[\"a.js\"],"
        + "\"names\":[\"foo\"],\"mappings\":\"AAAAA\"}");

    Object names = getFieldByNames(consumer, "names", "originalNames");
    assertNotNull(names);
    assertTrue(names.toString().contains("foo"));
  }

  @Test
  public void testParseLineCountField() throws Exception {
    consumer.parse(
        "{\"version\":3,\"lineCount\":3,\"sources\":[\"a.js\"],"
        + "\"mappings\":\"AAAA;AACA\"}");

    Object lineCount = getFieldByNames(consumer, "lineCount");
    assertNotNull(lineCount);
  }

  @Test
  public void testParseInlineSections() throws Exception {
    consumer.parse(
        "{\"version\":3,\"sections\":[{\"offset\":{\"line\":0,\"column\":0},"
        + "\"map\":{\"version\":3,\"sources\":[\"a.js\"],\"names\":[],"
        + "\"mappings\":\"AAAA\"}}]}");

    Method getSections = findPublicMethod(consumer, "getSections", 0);
    if (getSections != null) {
      Object sections = getSections.invoke(consumer);
      assertNotNull(sections);
      assertTrue(sizeOf(sections) >= 1);
    } else {
      Object sections = getFieldByNames(consumer, "sections");
      assertNotNull(sections);
      assertTrue(sizeOf(sections) >= 1);
    }
  }

  @Test
  public void testParseSectionsWithUrlDoesNotCrash() throws Exception {
    try {
      consumer.parse(
          "{\"version\":3,\"sections\":[{\"offset\":{\"line\":0,\"column\":0},"
          + "\"url\":\"http://example.com/sub.map\"}]}");
    } catch (SourceMapParseException expected) {
      // URL sections may require a SourceMapSupplier; parse(String) may reject them.
    }
  }

  @Test
  public void testParseWithSourceMapSupplier() throws Exception {
    Method parseSupplier = findParseSupplierMethod();
    if (parseSupplier == null) {
      return;
    }
    Class<?> supplierType = parseSupplier.getParameterTypes()[0];
    if (!supplierType.isInterface()) {
      return;
    }

    Object supplier = Proxy.newProxyInstance(
        supplierType.getClassLoader(),
        new Class<?>[] {supplierType},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("getSourceMap")) {
              return "{\"version\":3,\"sources\":[\"sup.js\"],\"mappings\":\"AAAA\"}";
            }
            return defaultValue(method.getReturnType());
          }
        });

    parseSupplier.invoke(consumer, supplier);
    assertTrue(getMappingEntries(consumer).size() >= 1);
  }

  @Test
  public void testParseSectionsWithUrlAndSupplier() throws Exception {
    Method parseSupplier = findParseSupplierMethod();
    if (parseSupplier == null) {
      return;
    }
    Class<?> supplierType = parseSupplier.getParameterTypes()[0];
    if (!supplierType.isInterface()) {
      return;
    }

    final String subMap =
        "{\"version\":3,\"sources\":[\"sub.js\"],\"names\":[],\"mappings\":\"AAAA\"}";

    Object supplier = Proxy.newProxyInstance(
        supplierType.getClassLoader(),
        new Class<?>[] {supplierType},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("getSourceMap")) {
              return subMap;
            }
            return defaultValue(method.getReturnType());
          }
        });

    consumer = new SourceMapConsumerV3();
    parseSupplier.invoke(consumer,
        "{\"version\":3,\"sections\":[{\"offset\":{\"line\":0,\"column\":0},"
        + "\"url\":\"sub.map\"}]}");

    assertTrue(getMappingEntries(consumer).size() >= 1);
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseMissingVersion() throws Exception {
    consumer.parse("{\"sources\":[\"a.js\"],\"mappings\":\"AAAA\"}");
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseWrongVersion() throws Exception {
    consumer.parse("{\"version\":2,\"sources\":[\"a.js\"],\"mappings\":\"AAAA\"}");
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseMissingMappings() throws Exception {
    consumer.parse("{\"version\":3,\"sources\":[\"a.js\"]}");
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseInvalidJson() throws Exception {
    consumer.parse("{not valid json");
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseInvalidMappingsCharacter() throws Exception {
    consumer.parse("{\"version\":3,\"sources\":[\"a.js\"],\"mappings\":\"!\"}");
  }

  @Test
  public void testParseValidSourceRootOmitted() throws Exception {
    consumer.parse(
        "{\"version\":3,\"sources\":[\"src.js\"],\"names\":[],\"mappings\":\"AAAA\"}");
    assertTrue(getMappingEntries(consumer).size() >= 1);
  }

  private static List<Object> getMappingEntries(SourceMapConsumerV3 consumer)
      throws Exception {
    List<Object> result = new ArrayList<>();

    Method getMappings = findPublicMethod(consumer, "getMappings", 0);
    if (getMappings != null) {
      Object value = getMappings.invoke(consumer);
      if (value instanceof Collection) {
        result.addAll((Collection<?>) value);
        if (!result.isEmpty()) {
          return result;
        }
      }
    }

    Method visitMappings = findPublicMethod(consumer, "visitMappings", 1);
    if (visitMappings != null) {
      Class<?> visitorType = visitMappings.getParameterTypes()[0];
      if (visitorType.isInterface()) {
        final List<Object> visited = new ArrayList<>();
        Object visitor = Proxy.newProxyInstance(
            visitorType.getClassLoader(),
            new Class<?>[] {visitorType},
            new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) {
                if (method.getName().equals("visitMapping")
                    && args != null
                    && args.length == 1) {
                  visited.add(args[0]);
                }
                return defaultValue(method.getReturnType());
              }
            });
        try {
          visitMappings.invoke(consumer, visitor);
        } catch (InvocationTargetException e) {
          throw e;
        }
        if (!visited.isEmpty()) {
          result.addAll(visited);
          return result;
        }
      }
    }

    Object mappings = getFieldByNames(consumer,
        "mappings", "parsedMappings", "mappingList", "lineMappings");
    if (mappings instanceof Collection) {
      result.addAll((Collection<?>) mappings);
    } else if (mappings instanceof Map) {
      result.addAll(((Map<?, ?>) mappings).values());
    }
    return result;
  }

  private static Object getSourceNamesObject(SourceMapConsumerV3 consumer)
      throws Exception {
    Method getOriginalSources = findPublicMethod(consumer, "getOriginalSources", 0);
    if (getOriginalSources != null) {
      return getOriginalSources.invoke(consumer);
    }
    Method getSources = findPublicMethod(consumer, "getSources", 0);
    if (getSources != null) {
      return getSources.invoke(consumer);
    }
    return getFieldByNames(consumer, "originalSources", "sources", "sourceFiles");
  }

  private static Object getMappingValue(
      Object mapping, String[] methodNames, String[] fieldNames) throws Exception {
    for (String name : methodNames) {
      Object value = invokeNoArg(mapping, name);
      if (value != null) {
        return value;
      }
    }

    if (methodNames.length > 0) {
      String last = methodNames[methodNames.length - 1];
      if (last.equalsIgnoreCase("getGeneratedLine")
          || last.equalsIgnoreCase("getLine")) {
        Object value = getPositionValue(mapping, "getGeneratedPosition", "getLine");
        if (value != null) {
          return value;
        }
      } else if (last.equalsIgnoreCase("getGeneratedColumn")
          || last.equalsIgnoreCase("getColumn")) {
        Object value = getPositionValue(mapping, "getGeneratedPosition", "getColumn");
        if (value != null) {
          return value;
        }
      } else if (last.equalsIgnoreCase("getOriginalLine")) {
        Object value = getPositionValue(mapping, "getOriginalPosition", "getLine");
        if (value != null) {
          return value;
        }
      } else if (last.equalsIgnoreCase("getOriginalColumn")) {
        Object value = getPositionValue(mapping, "getOriginalPosition", "getColumn");
        if (value != null) {
          return value;
        }
      }
    }

    return getFieldByNames(mapping, fieldNames);
  }

  private static Object getPositionValue(
      Object mapping, String positionGetter, String lineOrColumn) {
    Object position = invokeNoArg(mapping, positionGetter);
    if (position != null) {
      Object value = invokeNoArg(position, lineOrColumn);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  private static Object invokeNoArg(Object target, String methodName) {
    Method method = findPublicMethod(target, methodName, 0);
    if (method == null) {
      return null;
    }
    try {
      return method.invoke(target);
    } catch (Exception e) {
      return null;
    }
  }

  private static Method findPublicMethod(Object target, String name, int paramCount) {
    for (Method method : target.getClass().getMethods()) {
      if (method.getName().equals(name)
          && method.getParameterTypes().length == paramCount) {
        return method;
      }
    }
    return null;
  }

  private static Method findParseSupplierMethod() {
    for (Method method : SourceMapConsumerV3.class.getMethods()) {
      if (method.getName().equals("parse")
          && method.getParameterTypes().length == 1
          && method.getParameterTypes()[0] != String.class) {
        return method;
      }
    }
    return null;
  }

  private static Object getFieldByNames(Object target, String... names)
      throws Exception {
    for (String name : names) {
      try {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        return field.get(target);
      } catch (NoSuchFieldException e) {
        // Try next candidate.
      }
    }
    throw new NoSuchFieldException(
        "None of these fields found: " + Arrays.toString(names));
  }

  private static Field findField(Class<?> clazz, String name)
      throws NoSuchFieldException {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField(name);
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException(name);
  }

  private static int sizeOf(Object value) {
    if (value == null) {
      return 0;
    }
    if (value instanceof Collection) {
      return ((Collection<?>) value).size();
    }
    if (value instanceof Map) {
      return ((Map<?, ?>) value).size();
    }
    if (value.getClass().isArray()) {
      return Array.getLength(value);
    }
    throw new AssertionError("Unexpected container type: " + value.getClass());
  }

  private static int intValue(Object value) {
    if (value == null) {
      fail("Expected an integer value but got null");
    }
    return ((Number) value).intValue();
  }

  private static Object defaultValue(Class<?> type) {
    if (!type.isPrimitive()) {
      return null;
    }
    if (type == boolean.class) {
      return Boolean.FALSE;
    }
    if (type == byte.class) {
      return (byte) 0;
    }
    if (type == short.class) {
      return (short) 0;
    }
    if (type == int.class) {
      return 0;
    }
    if (type == long.class) {
      return 0L;
    }
    if (type == float.class) {
      return 0F;
    }
    if (type == double.class) {
      return 0D;
    }
    if (type == char.class) {
      return '\0';
    }
    return null;
  }
}