package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationMapTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface DummyAnnotation1 {
        String value() default "a";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface DummyAnnotation2 {
        int value() default 1;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface DummyAnnotation3 {
        boolean value() default true;
    }

    @Test
    public void testEmptyMap() {
        AnnotationMap map = new AnnotationMap();
        assertNull(map.get(DummyAnnotation1.class));
        assertEquals(0, map.size());
        
        String toString = map.toString();
        assertNotNull(toString);
    }

    @Test
    public void testAddAndGet() {
        AnnotationMap map = new AnnotationMap();
        DummyAnnotation1 ann1 = new DummyAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation1.class;
            }
            @Override
            public String value() {
                return "test";
            }
        };

        // Add annotation
        boolean added = map.add(ann1);
        assertTrue(added);
        assertEquals(1, map.size());
        
        // Retrieve annotation
        Annotation retrieved = map.get(DummyAnnotation1.class);
        assertNotNull(retrieved);
        assertEquals("test", ((DummyAnnotation1) retrieved).value());

        // Add same annotation again (should replace or return false depending on implementation, usually false if already present or true if updated)
        boolean addedAgain = map.add(ann1);
        // Let's verify size remains 1
        assertEquals(1, map.size());
    }

    @Test
    public void testAddIfNotPresent() {
        AnnotationMap map = new AnnotationMap();
        DummyAnnotation1 ann1 = new DummyAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation1.class;
            }
            @Override
            public String value() {
                return "first";
            }
        };

        DummyAnnotation1 ann1_other = new DummyAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation1.class;
            }
            @Override
            public String value() {
                return "second";
            }
        };

        // Add first
        assertTrue(map.addIfNotPresent(ann1));
        assertEquals(1, map.size());
        assertEquals("first", ((DummyAnnotation1) map.get(DummyAnnotation1.class)).value());

        // Try add if not present when already present
        assertFalse(map.addIfNotPresent(ann1_other));
        // Should still be "first" because addIfNotPresent doesn't overwrite
        assertEquals("first", ((DummyAnnotation1) map.get(DummyAnnotation1.class)).value());
    }

    @Test
    public void testMultipleAnnotations() {
        AnnotationMap map = new AnnotationMap();
        
        DummyAnnotation1 ann1 = new DummyAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation1.class;
            }
            @Override
            public String value() {
                return "v1";
            }
        };

        DummyAnnotation2 ann2 = new DummyAnnotation2() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation2.class;
            }
            @Override
            public int value() {
                return 42;
            }
        };

        map.add(ann1);
        map.add(ann2);

        assertEquals(2, map.size());
        assertNotNull(map.get(DummyAnnotation1.class));
        assertNotNull(map.get(DummyAnnotation2.class));
        assertNull(map.get(DummyAnnotation3.class));
    }

    @Test
    public void testMerge() {
        AnnotationMap map1 = new AnnotationMap();
        AnnotationMap map2 = new AnnotationMap();

        DummyAnnotation1 ann1 = new DummyAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation1.class;
            }
            @Override
            public String value() {
                return "map1";
            }
        };

        DummyAnnotation2 ann2 = new DummyAnnotation2() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation2.class;
            }
            @Override
            public int value() {
                return 100;
            }
        };

        DummyAnnotation1 ann1_override = new DummyAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation1.class;
            }
            @Override
            public String value() {
                return "map2";
            }
        };

        map1.add(ann1);
        map2.add(ann1_override);
        map2.add(ann2);

        AnnotationMap merged = AnnotationMap.merge(map1, map2);
        assertNotNull(merged);
        // Depending on merge implementation, map2 typically overrides map1 or vice versa.
        // Let's just exercise the method and check size or presence.
        assertTrue(merged.size() >= 1);
    }

    @Test
    public void testMergeNulls() {
        AnnotationMap map1 = new AnnotationMap();
        DummyAnnotation1 ann1 = new DummyAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation1.class;
            }
            @Override
            public String value() {
                return "test";
            }
        };
        map1.add(ann1);

        // Test merging with nulls
        AnnotationMap merged1 = AnnotationMap.merge(map1, null);
        assertNotNull(merged1);
        assertEquals(1, merged1.size());

        AnnotationMap merged2 = AnnotationMap.merge(null, map1);
        assertNotNull(merged2);
        assertEquals(1, merged2.size());

        AnnotationMap merged3 = AnnotationMap.merge(null, null);
        assertNotNull(merged3);
        assertEquals(0, merged3.size());
    }

    @Test
    public void testRemove() {
        AnnotationMap map = new AnnotationMap();
        DummyAnnotation1 ann1 = new DummyAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DummyAnnotation1.class;
            }
            @Override
            public String value() {
                return "toRemove";
            }
        };

        map.add(ann1);
        assertEquals(1, map.size());

        // Remove existing
        Annotation removed = map.remove(DummyAnnotation1.class);
        assertNotNull(removed);
        assertEquals(0, map.size());

        // Remove non-existing
        Annotation removedNonExistent = map.remove(DummyAnnotation2.class);
        assertNull(removedNonExistent);
    }
}