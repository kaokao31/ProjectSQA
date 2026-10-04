package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.Test;

public class JsDocInfoParserTest {

  private static Class<?> parserClass;
  private static Object config;
  private static Object errorReporter;
  private static Constructor<?> parserConstructor;
  private static Method parserParseMethod;

  @Before
  public void setUp() throws Exception {
    ensureInitialized();
  }

  private static synchronized void ensureInitialized() throws Exception {
    if (parserClass != null) {
      return;
    }
    config = createConfig();
    errorReporter = createErrorReporter();
    parserClass = Class.forName("com.google.javascript.jscomp.parsing.JsDocInfoParser");
    parserConstructor = findParserConstructor();
  }

  private static Object createConfig() throws Exception {
    Class<?> configClass = Class.forName("com.google.javascript.jscomp.parsing.Config");

    for (Constructor<?> ctor : configClass.getDeclaredConstructors()) {
      ctor.setAccessible(true);
      Class<?>[] types = ctor.getParameterTypes();
      int boolCount = 0;
      Object[] base = new Object[types.length];

      for (int i = 0; i < types.length; i++) {
        Class<?> t = types[i];
        if (t == boolean.class) {
          boolCount++;
          base[i] = Boolean.TRUE;
        } else if (t == int.class) {
          base[i] = 1;
        } else if (t == long.class) {
          base[i] = 1L;
        } else if (t == double.class) {
          base[i] = 1.0d;
        } else if (t == float.class) {
          base[i] = 1.0f;
        } else if (t == char.class) {
          base[i] = ' ';
        } else if (t.isEnum()) {
          base[i] = t.getEnumConstants()[0];
        } else if (t == String.class) {
          base[i] = "";
        } else {
          base[i] = null;
        }
      }

      int combos = 1 << Math.min(boolCount, 5);
      for (int mask = 0; mask < combos; mask++) {
        Object[] args = base.clone();
        int boolIndex = 0;
        for (int i = 0; i < types.length; i++) {
          if (types[i] == boolean.class) {
            args[i] = ((mask >> boolIndex) & 1) == 0 ? Boolean.FALSE : Boolean.TRUE;
            boolIndex++;
          }
        }
        try {
          return ctor.newInstance(args);
        } catch (Exception ignored) {
          // Try the next combination/constructor.
        }
      }
    }

    // Fallback to a zero-argument static factory, if available.
    Class<?> configClass2 = Class.forName("com.google.javascript.jscomp.parsing.Config");
    for (Method m : configClass2.getMethods()) {
      if (Modifier.isStatic(m.getModifiers())
          && m.getReturnType() == configClass2
          && m.getParameterTypes().length == 0) {
        try {
          return m.invoke(null);
        } catch (Exception ignored) {
          // Continue.
        }
      }
    }

    throw new IllegalStateException("Cannot create Config");
  }

  private static Object createErrorReporter() throws Exception {
    String[] candidates = {
        "com.google.javascript.jscomp.ErrorReporter",
        "com.google.javascript.jscomp.parsing.ErrorReporter",
        "com.google.javascript.jscomp.mozilla.rhino.ErrorReporter"
    };

    Class<?> reporterInterface = null;
    for (String candidate : candidates) {
      try {
        Class<?> clazz = Class.forName(candidate);
        if (clazz.isInterface()) {
          reporterInterface = clazz;
          break;
        }
      } catch (ClassNotFoundException ignored) {
        // Try next candidate.
      }
    }

    if (reporterInterface == null) {
      throw new IllegalStateException("Cannot find ErrorReporter interface");
    }

    InvocationHandler handler = new InvocationHandler() {
      @Override
      public Object invoke(Object proxy, Method method, Object[] args) {
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
          return null;
        }
        if (returnType.isPrimitive()) {
          return primitiveDefault(returnType);
        }
        return null;
      }
    };

    return Proxy.newProxyInstance(
        reporterInterface.getClassLoader(),
        new Class<?>[] {reporterInterface},
        handler);
  }

  private static Constructor<?> findParserConstructor() throws Exception {
    for (Constructor<?> ctor : parserClass.getDeclaredConstructors()) {
      ctor.setAccessible(true);
      try {
        ctor.newInstance(makeParserArgs(ctor, "/** test */"));
        return ctor;
      } catch (InvocationTargetException | InstantiationException | IllegalAccessException
          | IllegalArgumentException ignored) {
        // Try another constructor.
      }
    }
    throw new IllegalStateException("Cannot find JsDocInfoParser constructor");
  }

  private static Object[] makeParserArgs(Constructor<?> ctor, String comment) {
    Class<?>[] types = ctor.getParameterTypes();
    Object[] args = new Object[types.length];
    int stringIndex = 0;

    for (int i = 0; i < types.length; i++) {
      Class<?> t = types[i];

      if (t == String.class) {
        if (stringIndex == 0) {
          args[i] = comment;
        } else if (stringIndex == 1) {
          args[i] = "test-source";
        } else {
          args[i] = "";
        }
        stringIndex++;
      } else if (config != null && t.isInstance(config)) {
        args[i] = config;
      } else if (errorReporter != null && t.isInstance(errorReporter)) {
        args[i] = errorReporter;
      } else if (t.isPrimitive()) {
        args[i] = primitiveDefault(t);
      } else if (t.isEnum()) {
        args[i] = t.getEnumConstants()[0];
      } else {
        args[i] = null;
      }
    }

    return args;
  }

  private static Object primitiveDefault(Class<?> t) {
    if (t == boolean.class) {
      return Boolean.TRUE;
    }
    if (t == int.class) {
      return 0;
    }
    if (t == long.class) {
      return 0L;
    }
    if (t == double.class) {
      return 0.0d;
    }
    if (t == float.class) {
      return 0.0f;
    }
    if (t == short.class) {
      return (short) 0;
    }
    if (t == byte.class) {
      return (byte) 0;
    }
    if (t == char.class) {
      return '\0';
    }
    throw new IllegalArgumentException("Unknown primitive type: " + t);
  }

  private static Object parse(String comment) throws Exception {
    ensureInitialized();
    Object parser = parserConstructor.newInstance(makeParserArgs(parserConstructor, comment));

    if (parserParseMethod == null) {
      try {
        parserParseMethod = parserClass.getMethod("parse");
      } catch (NoSuchMethodException e) {
        parserParseMethod = parserClass.getDeclaredMethod("parse");
      }
      parserParseMethod.setAccessible(true);
    }

    return parserParseMethod.invoke(parser);
  }

  private static boolean flag(Object info, String methodName) throws Exception {
    return (Boolean) info.getClass().getMethod(methodName).invoke(info);
  }

  private static boolean flagIfExists(Object info, String methodName) throws Exception {
    try {
      return flag(info, methodName);
    } catch (NoSuchMethodException e) {
      return true;
    }
  }

  private static boolean hasParam(Object info, String name) throws Exception {
    return (Boolean) info.getClass().getMethod("hasParameter", String.class).invoke(info, name);
  }

  private static void parseNoCrash(String comment) {
    try {
      parse(comment);
    } catch (Throwable t) {
      fail("Parse crashed for [" + comment + "]: " + t);
    }
  }

  @Test
  public void testEmptyComment() throws Exception {
    assertNotNull(parse("/** */"));
  }

  @Test
  public void testPlainComment() throws Exception {
    assertNotNull(parse("/** hello world */"));
  }

  @Test
  public void testValidTypeExpressions() throws Exception {
    String[] comments = {
        "/** @type {string} */",
        "/** @type {number} */",
        "/** @type {boolean} */",
        "/** @type {null} */",
        "/** @type {undefined} */",
        "/** @type {Array} */",
        "/** @type {Array.<string>} */",
        "/** @type {!Array.<string>} */",
        "/** @type {Array.<Array.<string>>} */",
        "/** @type {Object.<string, number>} */",
        "/** @type {!Object} */",
        "/** @type {?Object} */",
        "/** @type {*} */",
        "/** @type {?} */",
        "/** @type {string|number} */",
        "/** @type {?string|number} */",
        "/** @type {!Object|string} */",
        "/** @type {function()} */",
        "/** @type {function(string): number} */",
        "/** @type {function(new: Object): number} */",
        "/** @type {function(this: Object): number} */",
        "/** @type {function(...number): void} */",
        "/** @type {function(string=): void} */",
        "/** @type {{foo: string}} */",
        "/** @type {{foo: string, bar: number}} */",
        "/** @type {goog.ui.Button} */",
        "/** @type {!goog.ui.Button} */",
        "/** @type {Array.<Object.<string, number>>} */",
        "/** @type {function(new: goog.Foo, string): boolean} */"
    };

    for (String comment : comments) {
      Object info = parse(comment);
      assertNotNull("Info should not be null for " + comment, info);
      assertTrue("hasType should be true for " + comment, flag(info, "hasType"));
    }
  }

  @Test(timeout = 5000)
  public void testInvalidTypeExpressionsDoNotCrash() throws Exception {
    String[] comments = {
        "/** @type {} */",
        "/** @type { } */",
        "/** @type {string */",
        "/** @type {Array.<} */",
        "/** @type {Object.<,>} */",
        "/** @type {function(} */",
        "/** @type {function(:)} */",
        "/** @type {{foo:}} */",
        "/** @type {boolean||string} */",
        "/** @type {!} */",
        "/** @type {string "
    };

    for (String comment : comments) {
      parseNoCrash(comment);
    }
  }

  @Test
  public void testParamTags() throws Exception {
    Object info = parse("/** @param {string} name */");
    assertTrue(hasParam(info, "name"));
    assertFalse(hasParam(info, "age"));

    info = parse("/** @param name */");
    assertTrue(hasParam(info, "name"));

    info = parse("/**\n * @param {string} a\n * @param {number} b\n */");
    assertTrue(hasParam(info, "a"));
    assertTrue(hasParam(info, "b"));

    info = parse("/** @param {string=} opt_name */");
    assertTrue(hasParam(info, "opt_name"));

    info = parse("/** @param {...string} var_args */");
    assertTrue(hasParam(info, "var_args"));

    info = parse("/** @param {function(string): number} callback */");
    assertTrue(hasParam(info, "callback"));

    info = parse("/** @param {!Object} obj */");
    assertTrue(hasParam(info, "obj"));

    info = parse("/** @param {{a: number}} record */");
    assertTrue(hasParam(info, "record"));

    info = parse("/** @param {Array.<string>} arr */");
    assertTrue(hasParam(info, "arr"));
  }

  @Test
  public void testReturnTags() throws Exception {
    Object info = parse("/** @return {number} */");
    assertTrue(flag(info, "hasReturnType"));

    info = parse("/** @return {function(string): boolean} */");
    assertTrue(flag(info, "hasReturnType"));

    info = parse("/** @return {Array.<string>} */");
    assertTrue(flag(info, "hasReturnType"));
  }

  @Test
  public void testConstructorAndInterface() throws Exception {
    Object info = parse("/** @constructor */");
    assertTrue(flag(info, "isConstructor"));

    info = parse("/** @interface */");
    assertTrue(flag(info, "isInterface"));

    info = parse("/**\n * @constructor\n * @param {string} x\n */");
    assertTrue(flag(info, "isConstructor"));
    assertTrue(hasParam(info, "x"));
  }

  @Test
  public void testMiscAnnotations() throws Exception {
    Object info = parse("/** @deprecated */");
    assertTrue(flag(info, "isDeprecated"));

    info = parse("/** @override */");
    assertTrue(flag(info, "isOverride"));

    info = parse("/** @extends {Base} */");
    assertTrue(flag(info, "hasBaseType"));

    info = parse("/** @enum {string} */");
    assertTrue(flag(info, "hasEnumParameterType"));

    info = parse("/** @this {!Object} */");
    assertTrue(flag(info, "hasThisType"));

    info = parse("/** @typedef {Object} */");
    assertTrue(flagIfExists(info, "hasTypedefType"));
  }

  @Test
  public void testManyMiscTagsDoNotCrash() throws Exception {
    String[] comments = {
        "/** @fileoverview description */",
        "/** @author a@example.com */",
        "/** @license MIT */",
        "/** @preserve copyright */",
        "/** @suppress {deprecated} */",
        "/** @const */",
        "/** @define {boolean} */",
        "/** @throws {Error} description */",
        "/** @see http://example.com */",
        "/** @link http://example.com */",
        "/** @lends {foo} */",
        "/** @borrows foo as bar */",
        "/** @version 1.0 */",
        "/** @since 2011 */",
        "/** @return {string} @param {number} x */",
        "/** @foobar */",
        "/** @unknownTag {some text} */",
        "/** @template T */"
    };

    for (String comment : comments) {
      parseNoCrash(comment);
    }
  }

  @Test
  public void testVisibilityAndExportTagsDoNotCrash() throws Exception {
    String[] comments = {
        "/** @private */",
        "/** @protected */",
        "/** @public */",
        "/** @package */",
        "/** @export */",
        "/** @expose */"
    };

    for (String comment : comments) {
      parseNoCrash(comment);
    }
  }

  @Test(timeout = 5000)
  public void testManyInvalidTagsDoNotCrash() throws Exception {
    String[] comments = {
        "/** @ */",
        "/** @type */",
        "/** @type { */",
        "/** @type {} */",
        "/** @type { } */",
        "/** @type {string */",
        "/** @param */",
        "/** @param { */",
        "/** @param {} x */",
        "/** @param {number */",
        "/** @param {string} */",
        "/** @return */",
        "/** @return { */",
        "/** @return {number */",
        "/** @constructor extra */",
        "/** @interface extra */",
        "/** @extends */",
        "/** @implements */",
        "/** @enum */",
        "/** @this */",
        "/** @typedef */",
        "/** @type {Array.<} */",
        "/** @type {Object.<,>} */",
        "/** @type {function(} */",
        "/** @type {function(:)} */",
        "/** @type {{foo:}} */",
        "/** @type {boolean||string} */",
        "/** @type {!} */",
        "/** @type {string "
    };

    for (String comment : comments) {
      parseNoCrash(comment);
    }
  }

  @Test
  public void testLineEndingsAndLeadingStars() throws Exception {
    Object info = parse("/**\n * @type {string}\n */");
    assertTrue(flag(info, "hasType"));

    info = parse("/**\r\n * @type {string}\r\n */");
    assertTrue(flag(info, "hasType"));

    info = parse("/**\r * @type {string}\r */");
    assertTrue(flag(info, "hasType"));
  }

  @Test
  public void testNoFalsePositives() throws Exception {
    Object info = parse("/** @type {string} */");
    assertTrue(flag(info, "hasType"));
    assertFalse(flag(info, "isConstructor"));
    assertFalse(flag(info, "isInterface"));
    assertFalse(flag(info, "hasReturnType"));
    assertFalse(hasParam(info, "name"));

    Object plain = parse("/** hello */");
    assertFalse(flag(plain, "hasType"));
  }

  @Test(timeout = 5000)
  public void testDeeplyNestedTypeDoesNotStackOverflow() throws Exception {
    int depth = 100;
    StringBuilder sb = new StringBuilder("/** @type {");
    for (int i = 0; i < depth; i++) {
      sb.append("Array.<");
    }
    sb.append("string");
    for (int i = 0; i < depth; i++) {
      sb.append(">");
    }
    sb.append("} */");
    parse(sb.toString());
  }
}