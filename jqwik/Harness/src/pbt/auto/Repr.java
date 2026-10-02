package pbt.auto;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

/** Deterministic, canonical text form of observable behaviour (return values, exceptions, object state). */
public final class Repr {
    private Repr() {
    }

    static final int MAX = 1500;
    private static final Pattern HASH = Pattern.compile("@[0-9a-fA-F]{4,}");
    private static final Pattern LAMBDA = Pattern.compile("\\$\\$Lambda\\$[0-9]+/(0x)?[0-9a-fA-F]+");

    public static String of(Object o) {
        StringBuilder sb = new StringBuilder();
        append(sb, o, 0, Collections.newSetFromMap(new IdentityHashMap<>()));
        return cut(sb.toString());
    }

    static String cut(String s) {
        return s.length() > MAX ? s.substring(0, MAX) + "...(" + s.length() + ")" : s;
    }

    static String norm(String s) {
        return LAMBDA.matcher(HASH.matcher(s).replaceAll("@H")).replaceAll("\\$\\$Lambda");
    }

    private static void append(StringBuilder sb, Object o, int depth, Set<Object> seen) {
        if (sb.length() > MAX) return;
        if (o == null) {
            sb.append("null");
            return;
        }
        Class<?> c = o.getClass();
        if (o instanceof String) {
            sb.append('"').append(escape((String) o)).append('"');
            return;
        }
        if (o instanceof Character) {
            sb.append("'").append(escape(o.toString())).append("'");
            return;
        }
        if (o instanceof Number || o instanceof Boolean) {
            // the TYPE matters (Integer 1 vs Long 1 is a real behavioural difference)
            sb.append(c.getSimpleName()).append(':').append(o);
            return;
        }
        if (o instanceof Enum) {
            sb.append(((Enum<?>) o).getDeclaringClass().getSimpleName()).append('.').append(((Enum<?>) o).name());
            return;
        }
        if (o instanceof Class) {
            sb.append("class ").append(((Class<?>) o).getName());
            return;
        }
        if (o instanceof Throwable) {
            sb.append("throws ").append(c.getName());
            return;
        }
        if (depth > 4 || !seen.add(o)) {
            sb.append("<").append(c.getSimpleName()).append(">");
            return;
        }
        if (c.isArray()) {
            int n = Array.getLength(o);
            sb.append(c.getComponentType().getSimpleName()).append('[');
            for (int i = 0; i < n && sb.length() < MAX; i++) {
                if (i > 0) sb.append(',');
                append(sb, Array.get(o, i), depth + 1, seen);
            }
            sb.append(']');
            return;
        }
        if (o instanceof Date) {
            sb.append(c.getSimpleName()).append('(').append(((Date) o).getTime()).append(')');
            return;
        }
        if (o instanceof Calendar) {
            Calendar cal = (Calendar) o;
            sb.append("Calendar(").append(cal.getTimeInMillis()).append(',').append(cal.getTimeZone().getID()).append(')');
            return;
        }
        if (o instanceof Map) {
            List<String> es = new ArrayList<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) es.add(of(e.getKey()) + "=" + of(e.getValue()));
            if (!(o instanceof SortedMap) && !(o instanceof LinkedHashMap)) Collections.sort(es);
            sb.append("Map").append(es);
            return;
        }
        if (o instanceof Collection) {
            List<String> es = new ArrayList<>();
            for (Object e : (Collection<?>) o) {
                es.add(of(e));
                if (es.size() > 50) break;
            }
            if (o instanceof Set && !(o instanceof SortedSet) && !(o instanceof LinkedHashSet)) Collections.sort(es);
            sb.append(o instanceof Set ? "Set" : "List").append(es);
            return;
        }
        if (o instanceof CharSequence) {
            sb.append(c.getSimpleName()).append('"').append(escape(o.toString())).append('"');
            return;
        }
        if (o instanceof java.io.ByteArrayOutputStream) {
            sb.append("bytes").append(Arrays.toString(((java.io.ByteArrayOutputStream) o).toByteArray()));
            return;
        }
        if (o instanceof java.io.StringWriter) {
            sb.append("StringWriter\"").append(escape(o.toString())).append('"');
            return;
        }
        if (o instanceof Iterator || o instanceof Iterable) {
            sb.append(c.getSimpleName());
            return;
        }
        // any other object: its own toString() if it has one, otherwise only the type
        try {
            Method ts = c.getMethod("toString");
            if (ts.getDeclaringClass() != Object.class) {
                sb.append(c.getSimpleName()).append(':').append(norm(escape(String.valueOf(o))));
                return;
            }
        } catch (Throwable t) {
            sb.append(c.getSimpleName()).append(":toString-throws ").append(t.getClass().getName());
            return;
        }
        sb.append("obj ").append(c.getName());
    }

    static String escape(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length() && b.length() < MAX; i++) {
            char ch = s.charAt(i);
            if (ch == '\\') b.append("\\\\");
            else if (ch == '\n') b.append("\\n");
            else if (ch == '\t') b.append("\\t");
            else if (ch == '\r') b.append("\\r");
            else if (ch < 32 || ch > 126) b.append(String.format("\\u%04x", (int) ch));
            else b.append(ch);
        }
        return b.toString();
    }
}
