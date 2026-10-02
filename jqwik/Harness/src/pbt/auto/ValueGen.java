package pbt.auto;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import net.jqwik.api.*;

/** jqwik Arbitraries of {@link Recipe}s for (almost) any Java parameter type. Never runs code under test. */
public final class ValueGen {
    private ValueGen() {
    }

    static final int MAX_DEPTH = 2;
    static final double NULL_RATE = 0.05;

    /** types we never generate (side effects on disk / threads / class loading) -> method is skipped. */
    static final Set<String> FORBIDDEN = new HashSet<>(Arrays.asList(
            "java.io.File", "java.nio.file.Path", "java.io.FileDescriptor", "java.lang.Thread",
            "java.lang.ClassLoader", "java.lang.Runtime", "java.lang.Process", "java.lang.ProcessBuilder",
            "java.net.Socket", "java.net.ServerSocket", "java.net.URL", "java.net.URI",
            "java.lang.SecurityManager", "java.lang.reflect.Method", "java.lang.reflect.Field",
            "java.lang.reflect.Constructor", "java.io.RandomAccessFile", "java.nio.channels.FileChannel"));

    // strings that often matter for parsers / formatters / text utilities
    static final String[] INTERESTING = {"", " ", "  ", "a", "A", "ab", "abc", "ABC", "aBc", "null", "0", "-0", "1", "-1",
            "01", "0x10", "0X1F", "#FF", "0x80000000", "1e10", "1E-5", "1.5", "-1.5", ".5", "5.", "1.0f", "2L", "1d",
            "NaN", "Infinity", "\t", "\n", "a b", " a ", "a,b,c", "a;b", "a.b.c", "\u0000", "\u00e9", "\ud83d\ude00",
            "true", "false", "yes", "on", "<a>", "&amp;", "\"q\"", "'s'", "\\", "%s", "{0}", "${x}", "*", "?", "[a-z]",
            "http://x.y/z?a=b", "2024-01-31", "12:34:56", "Z", "UTC", "en_US", "  x  y  "};

    public static Arbitrary<Recipe> forType(Type t) {
        return forType(t, 0);
    }

    /** in-memory IO types we can supply safely (never files / sockets). */
    static final Set<Class<?>> IO_TYPES = new HashSet<>(Arrays.asList(
            java.io.Writer.class, java.io.StringWriter.class, Appendable.class,
            java.io.Reader.class, java.io.StringReader.class,
            java.io.InputStream.class, java.io.ByteArrayInputStream.class,
            java.io.OutputStream.class, java.io.ByteArrayOutputStream.class));

    public static boolean supported(Class<?> c) {
        if (FORBIDDEN.contains(c.getName())) return false;
        if (c.isArray()) return supported(c.getComponentType());
        if (IO_TYPES.contains(c)) return true;
        String n = c.getName();
        if (n.startsWith("java.io.File") || n.startsWith("java.nio.file") || n.startsWith("java.nio.channels")) return false;
        return !(java.io.InputStream.class.isAssignableFrom(c) || java.io.OutputStream.class.isAssignableFrom(c)
                || java.io.Reader.class.isAssignableFrom(c) || java.io.Writer.class.isAssignableFrom(c));
    }

    static boolean isJdk(Class<?> c) {
        String n = c.getName();
        return n.startsWith("java.") || n.startsWith("javax.") || n.startsWith("sun.") || n.startsWith("jdk.")
                || n.startsWith("com.sun.");
    }

    static Arbitrary<Recipe> forType(Type t, int depth) {
        Class<?> c = Types.raw(t);
        Arbitrary<Recipe> a = base(t, c, depth);
        if (!c.isPrimitive()) a = Arbitraries.frequencyOf(Tuple.of(19, a), Tuple.of(1, Arbitraries.just(Recipe.NULL)));
        return a;
    }

    private static Arbitrary<Recipe> leaf(Arbitrary<?> a) {
        return a.map(Recipe.Leaf::new);
    }

    static Arbitrary<Integer> smallOrAnyInt() {
        // mostly small (indexes, sizes, counts); boundaries for overflow; rarely the full range
        return Arbitraries.frequencyOf(Tuple.of(8, Arbitraries.integers().between(-3, 20)),
                Tuple.of(3, Arbitraries.integers().between(-10_000, 10_000)),
                Tuple.of(1, Arbitraries.of(Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, Integer.MAX_VALUE,
                        (int) Short.MAX_VALUE, 65535, 65536)),
                Tuple.of(1, Arbitraries.integers()));
    }

    static Arbitrary<String> strings() {
        Arbitrary<String> alnum = Arbitraries.strings().withCharRange('a', 'z').withCharRange('A', 'Z')
                .withCharRange('0', '9').withChars(" .,-_:;/#xXeE+").ofMaxLength(12);
        Arbitrary<String> numeric = Arbitraries.strings().withCharRange('0', '9').withChars(".-+eExXLlFfDd#")
                .ofMinLength(1).ofMaxLength(12);
        Arbitrary<String> any = Arbitraries.strings().all().ofMaxLength(8);
        return Arbitraries.frequencyOf(Tuple.of(4, alnum), Tuple.of(2, numeric), Tuple.of(3, numberLike()),
                Tuple.of(3, Arbitraries.of(INTERESTING)), Tuple.of(1, any));
    }

    /** well-formed-ish number literals: [sign][0x|0X|#|0]digits[.digits][e[sign]digits][L|F|D] */
    static Arbitrary<String> numberLike() {
        Arbitrary<String> sign = Arbitraries.of("", "", "-", "+");
        Arbitrary<String> dec = Arbitraries.strings().withCharRange('0', '9').ofMinLength(1).ofMaxLength(20);
        Arbitrary<String> hex = Combinators.combine(Arbitraries.of("0x", "0X", "#", "-0x"),
                Arbitraries.strings().withCharRange('0', '9').withCharRange('a', 'f').withCharRange('A', 'F')
                        .ofMinLength(1).ofMaxLength(18)).as((p, d) -> p + d);
        Arbitrary<String> frac = Arbitraries.oneOf(Arbitraries.just(""),
                Arbitraries.strings().withCharRange('0', '9').ofMaxLength(18).map(d -> "." + d));
        Arbitrary<String> exp = Arbitraries.oneOf(Arbitraries.just(""), Arbitraries.just(""),
                Combinators.combine(Arbitraries.of("e", "E"), Arbitraries.of("", "-", "+"),
                        Arbitraries.integers().between(0, 400)).as((e, s, n) -> e + s + n));
        Arbitrary<String> suffix = Arbitraries.of("", "", "", "L", "l", "F", "f", "D", "d");
        Arbitrary<String> decimal = Combinators.combine(sign, dec, frac, exp, suffix).as((s, d, f, e, x) -> s + d + f + e + x);
        return Arbitraries.frequencyOf(Tuple.of(3, decimal), Tuple.of(2, hex));
    }

    static final Locale[] LOCALES = {Locale.US, Locale.ROOT, Locale.ENGLISH, Locale.FRANCE, Locale.GERMANY, Locale.JAPAN,
            new Locale("tr", "TR"), new Locale("th", "TH")};
    static final String[] ZONES = {"UTC", "GMT", "America/Los_Angeles", "Europe/London", "Asia/Bangkok", "Australia/Sydney"};

    @SuppressWarnings("unchecked")
    private static Arbitrary<Recipe> base(Type t, Class<?> c, int depth) {
        // ---------------- primitives & boxes
        if (c == int.class || c == Integer.class) return leaf(smallOrAnyInt());
        if (c == long.class || c == Long.class)
            return leaf(Arbitraries.frequencyOf(Tuple.of(3, Arbitraries.longs().between(-3, 100)), Tuple.of(1, Arbitraries.longs())));
        if (c == short.class || c == Short.class) return leaf(Arbitraries.shorts());
        if (c == byte.class || c == Byte.class) return leaf(Arbitraries.bytes());
        if (c == boolean.class || c == Boolean.class) return leaf(Arbitraries.of(true, false));
        if (c == char.class || c == Character.class)
            return leaf(Arbitraries.frequencyOf(Tuple.of(4, Arbitraries.chars().ascii()), Tuple.of(1, Arbitraries.chars())));
        if (c == double.class || c == Double.class)
            return leaf(Arbitraries.frequencyOf(Tuple.of(4, Arbitraries.doubles()), Tuple.of(2, Arbitraries.doubles().between(-10, 10)),
                    Tuple.of(1, Arbitraries.of(0.0, -0.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                            Double.MIN_VALUE, Double.MAX_VALUE, 1.0, -1.0, 0.5))));
        if (c == float.class || c == Float.class)
            return leaf(Arbitraries.frequencyOf(Tuple.of(4, Arbitraries.floats()),
                    Tuple.of(1, Arbitraries.of(0f, -0f, Float.NaN, Float.POSITIVE_INFINITY, Float.MIN_VALUE, Float.MAX_VALUE, 1f))));
        // ---------------- text
        if (c == String.class || c == CharSequence.class || c == Comparable.class) return leaf(strings());
        if (c == StringBuilder.class || c == StringBuffer.class)
            return strings().map(s -> new Recipe.BuilderR(c.getSimpleName(), s));
        if (c == char[].class)
            return strings().map(s -> {
                List<Recipe> l = new ArrayList<>();
                for (char ch : s.toCharArray()) l.add(new Recipe.Leaf(ch));
                return new Recipe.ArrayR(char.class, l);
            });
        // ---------------- numbers
        if (c == BigInteger.class) return leaf(Arbitraries.longs().map(BigInteger::valueOf));
        if (c == BigDecimal.class)
            return leaf(Combinators.combine(Arbitraries.longs().between(-100000, 100000), Arbitraries.integers().between(0, 6))
                    .as(BigDecimal::valueOf));
        if (c == Number.class)
            return Arbitraries.oneOf(leaf(smallOrAnyInt()), leaf(Arbitraries.longs()), leaf(Arbitraries.doubles()));
        if (c == Object.class)
            return Arbitraries.oneOf(leaf(strings()), leaf(smallOrAnyInt()));
        // ---------------- misc JDK
        if (c == Locale.class) return leaf(Arbitraries.of(LOCALES));
        if (c == TimeZone.class) return leaf(Arbitraries.of(ZONES).map(TimeZone::getTimeZone));
        if (c == Class.class) return leaf(Arbitraries.of(String.class, Integer.class, Object.class, int.class, int[].class, List.class));
        if (c == Date.class || c == java.sql.Date.class || c == java.sql.Timestamp.class || Calendar.class.isAssignableFrom(c)) {
            String kind = c == java.sql.Date.class ? "sqldate" : c == java.sql.Timestamp.class ? "timestamp"
                    : Calendar.class.isAssignableFrom(c) ? "calendar" : "date";
            return Combinators.combine(Arbitraries.longs().between(-4_000_000_000_000L, 4_000_000_000_000L), Arbitraries.of(ZONES))
                    .as((m, z) -> new Recipe.DateR(kind, m, z));
        }
        if (c.isEnum()) {
            Object[] cs = c.getEnumConstants();
            return cs == null || cs.length == 0 ? Arbitraries.just(Recipe.NULL) : leaf(Arbitraries.of(cs));
        }
        // ---------------- arrays / collections / maps
        if (c.isArray()) {
            Class<?> comp = c.getComponentType();
            Type compT = t instanceof GenericArrayType ? ((GenericArrayType) t).getGenericComponentType() : comp;
            if (depth > MAX_DEPTH) return Arbitraries.just(new Recipe.ArrayR(comp, Collections.emptyList()));
            return forType(compT, depth + 1).list().ofMaxSize(6).map(l -> new Recipe.ArrayR(comp, l));
        }
        if (Map.class.isAssignableFrom(c)) {
            boolean sorted = SortedMap.class.isAssignableFrom(c);
            Arbitrary<Recipe> k = elem(Types.typeArg(t, 0), depth), v = elem(Types.typeArg(t, 1), depth);
            return Combinators.combine(k.list().ofMaxSize(4), v.list().ofSize(4))
                    .as((ks, vs) -> new Recipe.MapR(sorted, ks, vs.subList(0, ks.size())));
        }
        if (Collection.class.isAssignableFrom(c) || c == Iterable.class) {
            String kind = SortedSet.class.isAssignableFrom(c) ? "sortedset" : Set.class.isAssignableFrom(c) ? "set"
                    : Deque.class.isAssignableFrom(c) || Queue.class.isAssignableFrom(c) ? "deque" : "list";
            return elem(Types.typeArg(t, 0), depth).list().ofMaxSize(6).map(l -> new Recipe.CollR(kind, l));
        }
        if (c == Iterator.class || c == Enumeration.class || c == Runnable.class) return Arbitraries.just(Recipe.NULL);
        if (IO_TYPES.contains(c)) {
            String kind = java.io.Reader.class.isAssignableFrom(c) ? "reader"
                    : java.io.InputStream.class.isAssignableFrom(c) ? "in"
                    : java.io.OutputStream.class.isAssignableFrom(c) ? "out" : "writer";
            return strings().map(txt -> new Recipe.IoR(kind, kind.equals("writer") || kind.equals("out") ? "" : txt));
        }
        // JDK classes we do not know: do not call arbitrary JDK constructors (side effects)
        if (isJdk(c)) return Arbitraries.just(Recipe.NULL);
        // ---------------- any other class: build it through public constructors / static factories
        return viaConstructors(c, depth);
    }

    private static Arbitrary<Recipe> elem(Type t, int depth) {
        Class<?> c = Types.raw(t);
        if (c == Object.class || c.isInterface() && !Collection.class.isAssignableFrom(c) && c != CharSequence.class && c != Comparable.class)
            return leaf(strings());
        return depth > MAX_DEPTH ? Arbitraries.just(Recipe.NULL) : forType(t, depth + 1);
    }

    /** public constructors + public static factories returning c (sorted -> deterministic). */
    @SuppressWarnings("unchecked")
    static Arbitrary<Recipe> viaConstructors(Class<?> c, int depth) {
        if (depth > MAX_DEPTH || !supported(c)) return Arbitraries.just(Recipe.NULL);
        List<Arbitrary<? extends Recipe>> options = new ArrayList<>();
        for (Executable e : creators(c)) {
            Arbitrary<Recipe> a = creatorArbitrary(c, e, depth);
            if (a != null) options.add(a);
            if (options.size() >= 6) break;
        }
        if (options.isEmpty()) return Arbitraries.just(Recipe.NULL);
        return options.size() == 1 ? (Arbitrary<Recipe>) options.get(0) : Arbitraries.<Recipe>oneOf(options);
    }

    public static List<Executable> creators(Class<?> c) {
        List<Executable> res = new ArrayList<>();
        boolean concrete = !c.isInterface() && !Modifier.isAbstract(c.getModifiers())
                && !(c.isMemberClass() && !Modifier.isStatic(c.getModifiers()));
        try {
            if (concrete) {
                for (Constructor<?> k : c.getDeclaredConstructors())
                    if (!Modifier.isPrivate(k.getModifiers()) && !k.isSynthetic()) res.add(k);
            }
            for (Method m : c.getDeclaredMethods())
                if (!Modifier.isPrivate(m.getModifiers()) && Modifier.isStatic(m.getModifiers()) && !m.isSynthetic()
                        && c.isAssignableFrom(m.getReturnType()) && m.getReturnType() != Object.class)
                    res.add(m);
        } catch (Throwable ignore) {
            return res;
        }
        res.sort(Comparator.comparingInt((Executable e) -> e.getParameterCount())
                .thenComparing(e -> e instanceof Constructor ? Types.sig((Constructor<?>) e) : Types.sig((Method) e)));
        return res;
    }

    static Arbitrary<Recipe> creatorArbitrary(Class<?> owner, Executable e, int depth) {
        Type[] ps = e.getGenericParameterTypes();
        if (ps.length != e.getParameterTypes().length) ps = e.getParameterTypes(); // inner/enum synthetic params
        for (Class<?> p : e.getParameterTypes()) if (!supported(p)) return null;
        String sig = e instanceof Constructor ? Types.sig((Constructor<?>) e) : Types.sig((Method) e);
        String name = owner.getName();
        if (ps.length == 0) return Arbitraries.just(new Recipe.CtorR(name, sig, Collections.emptyList()));
        List<Arbitrary<Recipe>> args = new ArrayList<>();
        for (Type p : ps) args.add(forType(p, depth + 1));
        return Combinators.combine(args).as(l -> new Recipe.CtorR(name, sig, new ArrayList<>(l)));
    }
}
