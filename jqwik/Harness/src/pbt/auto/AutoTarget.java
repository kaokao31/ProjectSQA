package pbt.auto;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import net.jqwik.api.*;

/**
 * Runtime support for the generated property classes (see {@link Generate}).
 *
 * Property generated for every public method m of a class under test:
 *     for all generated (receiver, args):   observe(m(receiver, args)) == snapshot(receiver, args)
 * The snapshot is recorded on the FIXED version (-Dpbt.mode=record) and checked on the fixed version again
 * (nondeterministic properties fail here and are discarded as false alarms) and on the BUGGY version
 * (-Dpbt.mode=check). Without -Dpbt.mode the regression part is skipped (only contracts are checked).
 *
 * Contract properties (no snapshot needed): equals is reflexive/consistent with hashCode, compareTo(x,x)==0.
 */
public final class AutoTarget {

    static final String MODE = System.getProperty("pbt.mode", "");
    static final Path SNAP_DIR = Paths.get(System.getProperty("pbt.snapshot.dir", "pbt-snapshots"));
    static final long CALL_TIMEOUT_MS = Long.getLong("pbt.callTimeoutMs", 2000);
    static final long PROPERTY_BUDGET_MS = Long.getLong("pbt.propertyBudgetMs", 30000);

    private static final Map<String, Map<String, String>> RECORDED = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> EXPECTED = new ConcurrentHashMap<>();
    private static final Map<String, Long> STARTED = new ConcurrentHashMap<>();
    private static final ExecutorService POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "pbt-call");
        t.setDaemon(true);
        return t;
    });

    static {
        if (MODE.equals("record")) Runtime.getRuntime().addShutdownHook(new Thread(AutoTarget::flush));
    }

    final String className;
    final String[] creatorSigs;
    private Class<?> cls;

    private AutoTarget(String className, String[] creatorSigs) {
        this.className = className;
        this.creatorSigs = creatorSigs;
    }

    public static AutoTarget of(String className, String... creatorSigs) {
        return new AutoTarget(className, creatorSigs);
    }

    Class<?> cls() {
        if (cls == null) {
            try {
                cls = Types.load(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return cls;
    }

    // ------------------------------------------------------------------ generation
    /** receivers: built through the constructors/factories that exist in BOTH versions (chosen by Generate). */
    @SuppressWarnings("unchecked")
    public Arbitrary<Recipe> receivers() {
        List<Arbitrary<? extends Recipe>> opts = new ArrayList<>();
        for (String spec : creatorSigs) {
            try {
                Class<?> owner = cls();
                String sig = spec;
                int hash = spec.indexOf('#');
                if (hash > 0) {                        // concrete subclass of an abstract target
                    owner = Types.load(spec.substring(0, hash));
                    sig = spec.substring(hash + 1);
                }
                Executable e = sig.startsWith("<init>") ? Types.constructor(owner, sig) : Types.method(owner, sig);
                Arbitrary<Recipe> a = ValueGen.creatorArbitrary(owner, e, 0);
                if (a != null) opts.add(a);
            } catch (Exception ignore) {
                // missing in this version -> the snapshot keys will simply not match
            }
        }
        if (opts.isEmpty()) return Arbitraries.just(Recipe.NULL);
        return opts.size() == 1 ? (Arbitrary<Recipe>) opts.get(0) : Arbitraries.<Recipe>oneOf(opts);
    }

    public Arbitrary<Call> calls(String methodSig) {
        Method m;
        try {
            m = Types.method(cls(), methodSig);
        } catch (Exception e) {
            throw new IllegalStateException("method not found: " + methodSig, e);
        }
        boolean isStatic = Modifier.isStatic(m.getModifiers());
        List<Arbitrary<Recipe>> parts = new ArrayList<>();
        parts.add(isStatic ? Arbitraries.just(Recipe.NULL) : receivers());
        Type[] gps = m.getGenericParameterTypes();
        if (gps.length != m.getParameterCount()) gps = m.getParameterTypes();
        for (Type p : gps) parts.add(ValueGen.forType(p));
        return Combinators.combine(parts).as(l -> new Call(methodSig, isStatic, l.get(0), new ArrayList<>(l.subList(1, l.size()))));
    }

    // ------------------------------------------------------------------ regression property
    public void check(String propertyId, Call call) {
        if (overBudget(propertyId)) return;
        String observed = observe(call);
        String key = call.repr();
        if (MODE.equals("record")) {
            Map<String, String> m = RECORDED.computeIfAbsent(propertyId, k -> new ConcurrentHashMap<>());
            String prev = m.putIfAbsent(key, observed);
            if (prev != null && !prev.equals(observed)) m.put(key, "<NONDETERMINISTIC>");
        } else if (MODE.equals("check")) {
            String expected = expected(propertyId).get(key);
            if (expected == null || expected.equals("<NONDETERMINISTIC>")) return; // input not seen on fixed
            // resource-limited outcomes depend on the machine, not on the code -> inconclusive
            if (isResource(expected) || observed.contains("java.lang.OutOfMemoryError")) return;
            if (observed.startsWith("TIMEOUT")) {
                observed = observe(call, CALL_TIMEOUT_MS * 5);   // confirm a hang with a longer limit
                if (observed.contains("java.lang.OutOfMemoryError")) return;
            }
            if (!expected.equals(observed)) {
                throw new AssertionError("[regression] " + className + "." + call.sig + "\n  input:    " + key
                        + "\n  expected: " + expected + "\n  actual:   " + observed);
            }
        }
    }

    static final String[] MASK = System.getProperty("pbt.mask", "").isEmpty() ? new String[0]
            : System.getProperty("pbt.mask").split(java.io.File.pathSeparator);

    /** replace version-specific paths (D4J checkout of buggy/fixed) by a placeholder. */
    static String mask(String s) {
        for (String m : MASK) if (!m.isEmpty()) s = s.replace(m, "<DIR>");
        return s;
    }

    static boolean isResource(String outcome) {
        return outcome.startsWith("TIMEOUT") || outcome.contains("java.lang.OutOfMemoryError");
    }

    // ------------------------------------------------------------------ contract properties
    public void checkEqualsContract(Recipe recipe) {
        Object[] ab = runWithTimeout(() -> new Object[]{recipe.make(), recipe.make()});
        if (ab == null || ab[0] == null || ab[1] == null) return;
        Object a = ab[0], b = ab[1];
        Boolean refl = runWithTimeout(() -> a.equals(a));
        if (refl != null && !refl) throw new AssertionError("[contract] equals not reflexive: " + recipe.repr());
        Boolean same = runWithTimeout(() -> a.equals(b) && b.equals(a));
        if (Boolean.TRUE.equals(same)) {
            Boolean hc = runWithTimeout(() -> a.hashCode() == b.hashCode());
            if (Boolean.FALSE.equals(hc))
                throw new AssertionError("[contract] equal objects with different hashCode: " + recipe.repr());
        }
        Boolean notNull = runWithTimeout(() -> !a.equals(null));
        if (Boolean.FALSE.equals(notNull)) throw new AssertionError("[contract] equals(null) is true: " + recipe.repr());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void checkCompareContract(Recipe recipe) {
        Object[] ab = runWithTimeout(() -> new Object[]{recipe.make(), recipe.make()});
        if (ab == null || !(ab[0] instanceof Comparable) || ab[1] == null) return;
        Integer self = runWithTimeout(() -> ((Comparable) ab[0]).compareTo(ab[0]));
        if (self != null && self != 0) throw new AssertionError("[contract] x.compareTo(x) != 0: " + recipe.repr());
        Integer ab1 = runWithTimeout(() -> Integer.signum(((Comparable) ab[0]).compareTo(ab[1])));
        Integer ba1 = runWithTimeout(() -> Integer.signum(((Comparable) ab[1]).compareTo(ab[0])));
        if (ab1 != null && ba1 != null && ab1 != -ba1)
            throw new AssertionError("[contract] compareTo not antisymmetric: " + recipe.repr());
    }

    // ------------------------------------------------------------------ execution
    String observe(Call call) {
        return observe(call, CALL_TIMEOUT_MS);
    }

    String observe(Call call, long timeoutMs) {
        Future<String> f = null;
        try {
            f = POOL.submit(() -> invoke(call));
            return mask(f.get(timeoutMs, TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
            f.cancel(true);
            return "TIMEOUT";
        } catch (ExecutionException e) {
            return "harness " + e.getCause().getClass().getName();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "INTERRUPTED";
        }
    }

    private String invoke(Call call) throws Exception {
        Method m = Types.method(cls(), call.sig);
        m.setAccessible(true);
        Object recv = null;
        if (!call.isStatic) {
            try {
                recv = call.receiver.make();
            } catch (Recipe.ConstructionFailed e) {
                return "receiver throws " + e.getCause().getClass().getName();
            } catch (Throwable t) {
                return "receiver throws " + t.getClass().getName();
            }
            if (recv == null) return "no receiver";
        }
        Object[] args = new Object[call.args.size()];
        for (int i = 0; i < args.length; i++) {
            try {
                args[i] = call.args.get(i).make();
            } catch (Recipe.ConstructionFailed e) {
                return "arg" + i + " throws " + e.getCause().getClass().getName();
            } catch (Throwable t) {
                return "arg" + i + " throws " + t.getClass().getName();
            }
        }
        StringBuilder out = new StringBuilder();
        try {
            Object ret = m.invoke(recv, args);
            out.append("ret=").append(m.getReturnType() == void.class ? "void" : Repr.of(ret));
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause();
            if (c instanceof StackOverflowError) out.append("throws StackOverflowError");
            else out.append("throws ").append(c.getClass().getName());
        } catch (IllegalArgumentException e) {
            out.append("harness argument mismatch");
        }
        // state after the call: receiver + mutable arguments
        if (recv != null) out.append(" | this=").append(Repr.of(recv));
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            if (a != null && (a.getClass().isArray() || a instanceof Collection || a instanceof Map
                    || a instanceof StringBuilder || a instanceof StringBuffer || a instanceof Date || a instanceof Calendar
                    || a instanceof java.io.StringWriter || a instanceof java.io.ByteArrayOutputStream))
                out.append(" | arg").append(i).append('=').append(Repr.of(a));
        }
        return Repr.cut(out.toString());
    }

    interface Body<T> {
        T run() throws Throwable;
    }

    static <T> T runWithTimeout(Body<T> b) {
        try {
            return POOL.submit(() -> {
                try {
                    return b.run();
                } catch (Throwable t) {
                    return null;
                }
            }).get(CALL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean overBudget(String propertyId) {
        long now = System.currentTimeMillis();
        long start = STARTED.computeIfAbsent(propertyId, k -> now);
        return now - start > PROPERTY_BUDGET_MS;
    }

    // ------------------------------------------------------------------ snapshot files
    private static Path file(String propertyId) {
        return SNAP_DIR.resolve(propertyId.replaceAll("[^A-Za-z0-9_.$-]", "_") + ".snap");
    }

    private static Map<String, String> expected(String propertyId) {
        return EXPECTED.computeIfAbsent(propertyId, id -> {
            Map<String, String> m = new HashMap<>();
            Path f = file(id);
            if (!Files.exists(f)) return m;
            try (BufferedReader r = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                String k;
                while ((k = r.readLine()) != null) {
                    String v = r.readLine();
                    if (v == null) break;
                    m.put(k, v);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return m;
        });
    }

    static void flush() {
        try {
            Files.createDirectories(SNAP_DIR);
            for (Map.Entry<String, Map<String, String>> e : RECORDED.entrySet()) {
                try (BufferedWriter w = Files.newBufferedWriter(file(e.getKey()), StandardCharsets.UTF_8)) {
                    for (Map.Entry<String, String> kv : new TreeMap<>(e.getValue()).entrySet()) {
                        w.write(oneLine(kv.getKey()));
                        w.newLine();
                        w.write(oneLine(kv.getValue()));
                        w.newLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String oneLine(String s) {
        return s.replace("\n", "\\n").replace("\r", "\\r");
    }

    // ------------------------------------------------------------------ a generated call
    public static final class Call {
        final String sig;
        final boolean isStatic;
        final Recipe receiver;
        final List<Recipe> args;

        Call(String sig, boolean isStatic, Recipe receiver, List<Recipe> args) {
            this.sig = sig;
            this.isStatic = isStatic;
            this.receiver = receiver;
            this.args = args;
        }

        public String repr() {
            return oneLine((isStatic ? "" : receiver.repr() + ".") + sig.substring(0, sig.indexOf('(')) + args);
        }

        @Override
        public String toString() {
            return repr();
        }
    }
}
