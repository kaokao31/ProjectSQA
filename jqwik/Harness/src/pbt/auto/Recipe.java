package pbt.auto;

import java.lang.reflect.*;
import java.util.*;

/**
 * A generated input is a pure-data "recipe". It is materialised into a real object inside the property body
 * (fresh object for every call), so generation never runs code under test and the input description (repr)
 * is identical on the buggy and the fixed version for the same seed.
 */
public abstract class Recipe {

    public abstract Object make() throws Throwable;

    public abstract String repr();

    @Override
    public String toString() {
        return repr();
    }

    // ------------------------------------------------------------------ immutable leaf values
    public static final class Leaf extends Recipe {
        final Object value;

        public Leaf(Object value) {
            this.value = value;
        }

        public Object make() {
            return value;
        }

        public String repr() {
            return Repr.of(value);
        }
    }

    public static final Recipe NULL = new Recipe() {
        public Object make() {
            return null;
        }

        public String repr() {
            return "null";
        }
    };

    // ------------------------------------------------------------------ arrays / collections / maps
    public static final class ArrayR extends Recipe {
        final Class<?> component;
        final List<Recipe> elems;

        public ArrayR(Class<?> component, List<Recipe> elems) {
            this.component = component;
            this.elems = elems;
        }

        public Object make() throws Throwable {
            Object a = Array.newInstance(component, elems.size());
            for (int i = 0; i < elems.size(); i++) Array.set(a, i, elems.get(i).make());
            return a;
        }

        public String repr() {
            return component.getSimpleName() + elems;
        }
    }

    public static final class CollR extends Recipe {
        final String kind; // list | set | sortedset | deque
        final List<Recipe> elems;

        public CollR(String kind, List<Recipe> elems) {
            this.kind = kind;
            this.elems = elems;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public Object make() throws Throwable {
            Collection c;
            switch (kind) {
                case "set": c = new LinkedHashSet(); break;
                case "sortedset": c = new TreeSet(); break;
                case "deque": c = new ArrayDeque(); break;
                default: c = new ArrayList();
            }
            for (Recipe r : elems) {
                Object o = r.make();
                if (o == null && !kind.equals("list")) continue;
                c.add(o);
            }
            return c;
        }

        public String repr() {
            return kind + elems;
        }
    }

    public static final class MapR extends Recipe {
        final boolean sorted;
        final List<Recipe> keys, values;

        public MapR(boolean sorted, List<Recipe> keys, List<Recipe> values) {
            this.sorted = sorted;
            this.keys = keys;
            this.values = values;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public Object make() throws Throwable {
            Map m = sorted ? new TreeMap() : new LinkedHashMap();
            for (int i = 0; i < keys.size(); i++) {
                Object k = keys.get(i).make();
                if (k == null && sorted) continue;
                m.put(k, values.get(i).make());
            }
            return m;
        }

        public String repr() {
            StringBuilder sb = new StringBuilder(sorted ? "sortedmap{" : "map{");
            for (int i = 0; i < keys.size(); i++) sb.append(keys.get(i).repr()).append('=').append(values.get(i).repr()).append(',');
            return sb.append('}').toString();
        }
    }

    // ------------------------------------------------------------------ mutable JDK values
    public static final class DateR extends Recipe {
        final long millis;
        final String kind; // date | calendar | sqldate
        final String tz;

        public DateR(String kind, long millis, String tz) {
            this.kind = kind;
            this.millis = millis;
            this.tz = tz;
        }

        public Object make() {
            switch (kind) {
                case "calendar":
                    Calendar c = new GregorianCalendar(TimeZone.getTimeZone(tz), Locale.US);
                    c.setTimeInMillis(millis);
                    return c;
                case "sqldate":
                    return new java.sql.Date(millis);
                case "timestamp":
                    return new java.sql.Timestamp(millis);
                default:
                    return new Date(millis);
            }
        }

        public String repr() {
            return kind + "(" + millis + (kind.equals("calendar") ? "," + tz : "") + ")";
        }
    }

    public static final class BuilderR extends Recipe {
        final String kind; // StringBuilder | StringBuffer
        final String text;

        public BuilderR(String kind, String text) {
            this.kind = kind;
            this.text = text;
        }

        public Object make() {
            return kind.equals("StringBuffer") ? new StringBuffer(text) : new StringBuilder(text);
        }

        public String repr() {
            return kind + "(" + Repr.of(text) + ")";
        }
    }

    // ------------------------------------------------------------------ in-memory IO
    public static final class IoR extends Recipe {
        final String kind; // writer | reader | in | out
        final String text;

        public IoR(String kind, String text) {
            this.kind = kind;
            this.text = text;
        }

        public Object make() {
            switch (kind) {
                case "reader": return new java.io.StringReader(text);
                case "in": return new java.io.ByteArrayInputStream(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                case "out": return new java.io.ByteArrayOutputStream();
                default: return new java.io.StringWriter();
            }
        }

        public String repr() {
            return kind + "(" + Repr.of(text) + ")";
        }
    }

    // ------------------------------------------------------------------ objects built through a constructor / factory
    public static final class CtorR extends Recipe {
        final String owner;   // class name
        final String sig;     // "<init>(int,java.lang.String)"  or  "factoryName(...)" (static)
        final List<Recipe> args;

        public CtorR(String owner, String sig, List<Recipe> args) {
            this.owner = owner;
            this.sig = sig;
            this.args = args;
        }

        public Object make() throws Throwable {
            Class<?> c = Types.load(owner);
            Object[] a = new Object[args.size()];
            for (int i = 0; i < a.length; i++) a[i] = args.get(i).make();
            try {
                if (sig.startsWith("<init>")) {
                    Constructor<?> k = Types.constructor(c, sig);
                    k.setAccessible(true);
                    return k.newInstance(a);
                }
                Method m = Types.method(c, sig);
                m.setAccessible(true);
                return m.invoke(null, a);
            } catch (InvocationTargetException e) {
                throw new ConstructionFailed(e.getCause());
            }
        }

        public String repr() {
            String simple = owner.substring(owner.lastIndexOf('.') + 1);
            return "new " + simple + "." + sig.substring(0, sig.indexOf('(')) + args;
        }
    }

    /** thrown when the receiver/argument could not be built (part of the observable behaviour). */
    public static final class ConstructionFailed extends Exception {
        public ConstructionFailed(Throwable cause) {
            super(cause);
        }
    }
}
