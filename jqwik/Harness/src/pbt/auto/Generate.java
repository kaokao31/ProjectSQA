package pbt.auto;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Generates jqwik property classes for the classes under test of one Defects4J bug.
 *
 *   java -cp <harness>:<jqwik jars> pbt.auto.Generate --targets a.B,a.C --fixed-cp CP --buggy-cp CP --out DIR
 *
 * Only members that exist with the same signature in BOTH versions are used (tests must compile/run on both).
 * The generator never looks at the fix, the diff or the developer tests: it only knows the public API.
 */
public final class Generate {

    static final Set<String> SKIP_NAMES = new HashSet<>(Arrays.asList(
            "main", "wait", "notify", "notifyAll", "getClass", "finalize", "clone", "exit", "halt", "gc",
            "runFinalization", "load", "loadLibrary", "setOut", "setErr", "setIn", "setSecurityManager"));

    public static void main(String[] argv) throws Exception {
        Map<String, String> a = new HashMap<>();
        for (int i = 0; i + 1 < argv.length; i += 2) a.put(argv[i], argv[i + 1]);
        List<String> targets = new ArrayList<>();
        for (String t : a.get("--targets").split("[,;]")) if (!t.trim().isEmpty()) targets.add(t.trim());
        int maxMethods = Integer.parseInt(a.getOrDefault("--max-methods", "150"));
        Path out = Paths.get(a.get("--out"));
        ClassLoader fixed = loader(a.get("--fixed-cp")), buggy = loader(a.get("--buggy-cp"));

        Files.createDirectories(out);
        StringBuilder report = new StringBuilder("{\n  \"generator\": \"pbt.auto.Generate\",\n  \"classes\": [\n");
        int generatedClasses = 0;
        for (int ti = 0; ti < targets.size(); ti++) {
            String t = targets.get(ti);
            String line;
            try {
                Class<?> cf = Class.forName(t, false, fixed), cb = Class.forName(t, false, buggy);
                line = generate(cf, cb, out, maxMethods);
                if (line.contains("\"written\": true")) generatedClasses++;
            } catch (Throwable e) {
                line = "{\"class\": \"" + t + "\", \"written\": false, \"reason\": \"" + e.getClass().getSimpleName() + "\"}";
            }
            report.append("    ").append(line).append(ti + 1 < targets.size() ? ",\n" : "\n");
        }
        report.append("  ]\n}\n");
        Files.write(out.resolve("AUTO_GENERATED.json"), report.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("generated test classes: " + generatedClasses + " / " + targets.size());
    }

    static ClassLoader loader(String cp) throws Exception {
        List<URL> urls = new ArrayList<>();
        for (String p : cp.split(File.pathSeparator)) if (!p.isEmpty()) urls.add(Paths.get(p).toUri().toURL());
        return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    }

    static String generate(Class<?> cf, Class<?> cb, Path out, int maxMethods) throws IOException {
        String fq = cf.getName();
        if (cf.isAnonymousClass() || cf.isLocalClass() || cf.isSynthetic() || cf.getPackage() == null)
            return "{\"class\": \"" + fq + "\", \"written\": false, \"reason\": \"anonymous/local class\"}";

        // creators (constructors + static factories) common to both versions
        Set<String> credB = new TreeSet<>();
        for (Executable e : safeCreators(cb)) credB.add(sigOf(e));
        List<String> creators = new ArrayList<>();
        for (Executable e : safeCreators(cf)) {
            String s = sigOf(e);
            if (credB.contains(s) && paramsSupported(e)) creators.add(s);
        }

        if (creators.isEmpty()) {
            // abstract class / interface / no accessible constructor: use concrete subclasses of the same code base
            for (Class<?> sub : subclasses(cf)) {
                Class<?> subB;
                try {
                    subB = Class.forName(sub.getName(), false, cb.getClassLoader());
                } catch (Throwable t) {
                    continue;
                }
                Set<String> sb = new HashSet<>();
                for (Executable e : safeCreators(subB)) sb.add(sigOf(e));
                int n = 0;
                for (Executable e : safeCreators(sub)) {
                    String sig = sigOf(e);
                    if (sb.contains(sig) && paramsSupported(e) && n++ < 3) creators.add(sub.getName() + "#" + sig);
                }
                if (creators.size() >= 10) break;
            }
        }

        Map<String, Method> mb = new HashMap<>();
        for (Method m : safeMethods(cb)) mb.put(Types.sig(m), m);
        List<Method> chosen = new ArrayList<>();
        int publicMethods = 0, notInBuggy = 0, unsupported = 0, noReceiver = 0;
        List<Method> ms = new ArrayList<>(safeMethods(cf));
        ms.sort(Comparator.comparing(Types::sig));
        for (Method m : ms) {
            publicMethods++;
            Method other = mb.get(Types.sig(m));
            if (other == null || Modifier.isStatic(other.getModifiers()) != Modifier.isStatic(m.getModifiers())) {
                notInBuggy++;
                continue;
            }
            if (!paramsSupported(m)) {
                unsupported++;
                continue;
            }
            if (!Modifier.isStatic(m.getModifiers()) && creators.isEmpty()) {
                noReceiver++;
                continue;
            }
            if (chosen.size() < maxMethods) chosen.add(m);
        }
        boolean eq = declares(cf, "equals", Object.class) && !creators.isEmpty();
        boolean cmp = Comparable.class.isAssignableFrom(cf) && declaresAnyCompareTo(cf) && !creators.isEmpty();

        String stats = String.format("\"public_methods\": %d, \"properties\": %d, \"skipped_not_in_buggy\": %d, "
                        + "\"skipped_unsupported_params\": %d, \"skipped_no_receiver\": %d, \"creators\": %d, \"contracts\": %d",
                publicMethods, chosen.size(), notInBuggy, unsupported, noReceiver, creators.size(), (eq ? 1 : 0) + (cmp ? 1 : 0));
        if (chosen.isEmpty() && !eq && !cmp)
            return "{\"class\": \"" + fq + "\", \"written\": false, \"reason\": \"no testable public API\", " + stats + "}";

        String pkg = cf.getPackage().getName();
        String simple = fq.substring(pkg.isEmpty() ? 0 : pkg.length() + 1).replace('$', '_') + "_AutoPbtTest";
        StringBuilder s = new StringBuilder();
        if (!pkg.isEmpty()) s.append("package ").append(pkg).append(";\n\n");
        s.append("import net.jqwik.api.*;\nimport pbt.auto.*;\n\n");
        s.append("/** AUTO-GENERATED by pbt.auto.Generate for ").append(fq).append(" - do not edit.\n");
        s.append(" *  One regression property per public method (snapshot recorded on the fixed version) + contracts. */\n");
        s.append("public class ").append(simple).append(" {\n\n");
        s.append("    static final AutoTarget T = AutoTarget.of(\"").append(fq).append('"');
        for (String c : creators) s.append(",\n            \"").append(c).append('"');
        s.append(");\n\n");
        int i = 0;
        for (Method m : chosen) {
            String id = String.format("%03d", i++);
            String sig = Types.sig(m);
            s.append("    @Property(shrinking = ShrinkingMode.OFF, edgeCases = EdgeCasesMode.NONE)\n");
            s.append("    void m").append(id).append('_').append(m.getName()).append("(@ForAll(\"c").append(id)
                    .append("\") AutoTarget.Call c) {\n");
            s.append("        T.check(\"").append(fq).append('#').append(sig).append("\", c);\n    }\n\n");
            s.append("    @Provide\n    Arbitrary<AutoTarget.Call> c").append(id).append("() {\n");
            s.append("        return T.calls(\"").append(sig).append("\");\n    }\n\n");
        }
        if (eq || cmp) {
            s.append("    @Provide\n    Arbitrary<Recipe> receiver() {\n        return T.receivers();\n    }\n\n");
        }
        if (eq) {
            s.append("    @Property(shrinking = ShrinkingMode.OFF, edgeCases = EdgeCasesMode.NONE)\n");
            s.append("    void contract_equalsHashCode(@ForAll(\"receiver\") Recipe r) {\n        T.checkEqualsContract(r);\n    }\n\n");
        }
        if (cmp) {
            s.append("    @Property(shrinking = ShrinkingMode.OFF, edgeCases = EdgeCasesMode.NONE)\n");
            s.append("    void contract_compareTo(@ForAll(\"receiver\") Recipe r) {\n        T.checkCompareContract(r);\n    }\n\n");
        }
        s.append("}\n");
        Path dir = pkg.isEmpty() ? out : out.resolve(pkg.replace('.', '/'));
        Files.createDirectories(dir);
        Files.write(dir.resolve(simple + ".java"), s.toString().getBytes(StandardCharsets.UTF_8));
        return "{\"class\": \"" + fq + "\", \"written\": true, " + stats + "}";
    }

    /** concrete subclasses of c found in the same classes directory (sorted, deterministic). */
    static List<Class<?>> subclasses(Class<?> c) {
        List<Class<?>> res = new ArrayList<>();
        try {
            Path root = Paths.get(c.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!Files.isDirectory(root)) return res;
            List<String> names = new ArrayList<>();
            try (java.util.stream.Stream<Path> st = Files.walk(root)) {
                st.filter(p -> p.toString().endsWith(".class")).forEach(p -> {
                    String rel = root.relativize(p).toString();
                    names.add(rel.substring(0, rel.length() - 6).replace(File.separatorChar, '.'));
                });
            }
            Collections.sort(names);
            for (String n : names) {
                if (n.equals(c.getName())) continue;
                try {
                    Class<?> k = Class.forName(n, false, c.getClassLoader());
                    if (c.isAssignableFrom(k) && !k.isInterface() && !Modifier.isAbstract(k.getModifiers())
                            && !k.isAnonymousClass() && !k.isLocalClass()
                            && !(k.isMemberClass() && !Modifier.isStatic(k.getModifiers()))) res.add(k);
                } catch (Throwable ignore) {
                }
                if (res.size() >= 5) break;
            }
        } catch (Throwable ignore) {
        }
        return res;
    }

    static List<Method> safeMethods(Class<?> c) {
        List<Method> res = new ArrayList<>();
        try {
            for (Method m : c.getDeclaredMethods()) {
                if (Modifier.isPrivate(m.getModifiers()) || m.isSynthetic() || m.isBridge()) continue;
                if (Modifier.isAbstract(m.getModifiers())) continue;
                if (SKIP_NAMES.contains(m.getName())) continue;
                if (c.isEnum() && (m.getName().equals("values") || m.getName().equals("valueOf"))) continue;
                res.add(m);
            }
        } catch (Throwable ignore) {
            // NoClassDefFoundError for optional dependencies -> nothing testable
        }
        return res;
    }

    static List<Executable> safeCreators(Class<?> c) {
        try {
            return ValueGen.creators(c);
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    static String sigOf(Executable e) {
        return e instanceof Constructor ? Types.sig((Constructor<?>) e) : Types.sig((Method) e);
    }

    static boolean paramsSupported(Executable e) {
        try {
            for (Class<?> p : e.getParameterTypes()) if (!ValueGen.supported(p)) return false;
            e.getGenericParameterTypes();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    static boolean declares(Class<?> c, String name, Class<?>... ps) {
        try {
            c.getDeclaredMethod(name, ps);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean declaresAnyCompareTo(Class<?> c) {
        try {
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals("compareTo") && m.getParameterCount() == 1) return true;
        } catch (Throwable ignore) {
        }
        return false;
    }
}
