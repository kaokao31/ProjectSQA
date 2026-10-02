package pbt.auto;

import java.lang.reflect.*;
import java.util.*;

/** Signature helpers: members are identified by stable strings like "indexOf(java.lang.String,int)". */
public final class Types {
    private Types() {
    }

    private static final Map<String, Class<?>> PRIMS = new HashMap<>();

    static {
        for (Class<?> c : new Class<?>[]{boolean.class, byte.class, short.class, char.class, int.class,
                long.class, float.class, double.class, void.class}) PRIMS.put(c.getName(), c);
    }

    public static Class<?> load(String name) throws ClassNotFoundException {
        return load(name, Types.class.getClassLoader());
    }

    public static Class<?> load(String name, ClassLoader cl) throws ClassNotFoundException {
        if (name.endsWith("[]")) {
            Class<?> comp = load(name.substring(0, name.length() - 2), cl);
            return Array.newInstance(comp, 0).getClass();
        }
        Class<?> p = PRIMS.get(name);
        if (p != null) return p;
        return Class.forName(name, false, cl);
    }

    public static String typeName(Class<?> c) {
        return c.getTypeName(); // int[], java.lang.String, a.b.Outer$Inner
    }

    public static String sig(Method m) {
        return m.getName() + params(m.getParameterTypes());
    }

    public static String sig(Constructor<?> k) {
        return "<init>" + params(k.getParameterTypes());
    }

    static String params(Class<?>[] ps) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < ps.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(typeName(ps[i]));
        }
        return sb.append(')').toString();
    }

    static Class<?>[] paramTypes(String sig, ClassLoader cl) throws ClassNotFoundException {
        String inner = sig.substring(sig.indexOf('(') + 1, sig.lastIndexOf(')'));
        if (inner.isEmpty()) return new Class<?>[0];
        String[] parts = inner.split(",");
        Class<?>[] res = new Class<?>[parts.length];
        for (int i = 0; i < parts.length; i++) res[i] = load(parts[i], cl);
        return res;
    }

    public static Method method(Class<?> c, String sig) throws Exception {
        String name = sig.substring(0, sig.indexOf('('));
        return c.getDeclaredMethod(name, paramTypes(sig, c.getClassLoader() == null ? Types.class.getClassLoader() : c.getClassLoader()));
    }

    public static Constructor<?> constructor(Class<?> c, String sig) throws Exception {
        return c.getDeclaredConstructor(paramTypes(sig, c.getClassLoader() == null ? Types.class.getClassLoader() : c.getClassLoader()));
    }

    /** element type of a generic parameter such as List<String>; Object if unknown. */
    public static Type typeArg(Type t, int i) {
        if (t instanceof ParameterizedType) {
            Type[] a = ((ParameterizedType) t).getActualTypeArguments();
            if (i < a.length) return a[i];
        }
        return Object.class;
    }

    public static Class<?> raw(Type t) {
        if (t instanceof Class) return (Class<?>) t;
        if (t instanceof ParameterizedType) return raw(((ParameterizedType) t).getRawType());
        if (t instanceof GenericArrayType) return Array.newInstance(raw(((GenericArrayType) t).getGenericComponentType()), 0).getClass();
        if (t instanceof WildcardType) {
            Type[] ub = ((WildcardType) t).getUpperBounds();
            return ub.length > 0 ? raw(ub[0]) : Object.class;
        }
        if (t instanceof TypeVariable) {
            Type[] b = ((TypeVariable<?>) t).getBounds();
            return b.length > 0 ? raw(b[0]) : Object.class;
        }
        return Object.class;
    }
}
