package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import java.lang.reflect.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for TypeFactory.
 * Designed to achieve high line/branch coverage and detect faults
 * (including Defects4J bug 11 scenario).
 */
public class TypeFactoryTest {

    private TypeFactory tf;

    @Before
    public void setUp() {
        tf = TypeFactory.defaultInstance();
    }

    // --- constructType(Class<?>) ---

    @Test
    public void testConstructTypeSimpleClass() {
        JavaType type = tf.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.isReferenceType() || type.isValueType());
    }

    @Test
    public void testConstructTypePrimitive() {
        JavaType type = tf.constructType(int.class);
        assertNotNull(type);
        assertTrue(type.isPrimitive());
    }

    @Test
    public void testConstructTypeArray() {
        JavaType type = tf.constructType(int[].class);
        assertNotNull(type);
        assertTrue(type.isArrayType());
        assertEquals(int.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeVoid() {
        JavaType type = tf.constructType(void.class);
        assertNotNull(type);
        assertTrue(type.isPrimitive());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructTypeNullClass() {
        tf.constructType((Class<?>) null);
    }

    // --- constructType(Type) ---

    @Test
    public void testConstructTypeParameterizedType() throws Exception {
        // Create a ParameterizedType for List<String>
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        JavaType result = tf.constructType(type);
        assertNotNull(result);
        assertEquals(List.class, result.getRawClass());
        assertEquals(1, result.containedTypeCount());
        assertEquals(String.class, result.containedType(0).getRawClass());
    }

    @Test
    public void testConstructTypeGenericArrayType() throws Exception {
        // Create a GenericArrayType for List<String>[]
        Type genericArray = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[]{String.class};
                    }

                    @Override
                    public Type getRawType() {
                        return List.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                };
            }
        };
        JavaType result = tf.constructType(genericArray);
        assertNotNull(result);
        assertTrue(result.isArrayType());
        JavaType content = result.getContentType();
        assertEquals(List.class, content.getRawClass());
        assertEquals(String.class, content.containedType(0).getRawClass());
    }

    @Test
    public void testConstructTypeWildcardType() throws Exception {
        // Wildcard ? extends Number
        Type wildcard = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Number.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[]{};
            }
        };
        JavaType result = tf.constructType(wildcard);
        assertNotNull(result);
        // Wildcard types are typically resolved to upper bound
        assertEquals(Number.class, result.getRawClass());
    }

    @Test
    public void testConstructTypeTypeVariable() throws Exception {
        // Simulate a TypeVariable from a generic class
        TypeVariable<?> tv = new TypeVariable<?>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Comparable.class};
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public GenericDeclaration getGenericDeclaration() {
                return null;
            }
        };
        JavaType result = tf.constructType(tv);
        assertNotNull(result);
        // TypeVariable should resolve to its first bound
        assertEquals(Comparable.class, result.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructTypeNullType() {
        tf.constructType((Type) null);
    }

    // --- constructType(Type, TypeBindings) ---

    @Test
    public void testConstructTypeWithBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, Integer.class);
        // Construct a parameterized type using bindings
        JavaType type = tf.constructType(new Type() {
            @Override
            public String toString() {
                return "custom";
            }
        }, bindings);
        // This may return unknown type; just check not null
        assertNotNull(type);
    }

    // --- _resolveType --- (indirectly via constructType with TypeBindings)

    @Test
    public void testResolveTypeWithRecursiveBounds() {
        // Create a type variable with recursive bound: T extends Comparable<T>
        TypeVariable<?> tv = new TypeVariable<?>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Comparable.class}; // simplified
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public GenericDeclaration getGenericDeclaration() {
                return null;
            }
        };
        JavaType result = tf.constructType(tv);
        assertNotNull(result);
        // Should resolve to Comparable
        assertEquals(Comparable.class, result.getRawClass());
    }

    // --- findTypeParameters ---

    @Test
    public void testFindTypeParametersSimple() {
        // Map<String,Integer> -> find type parameters for Map
        JavaType mapType = tf.constructType(new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class, Integer.class};
            }

            @Override
            public Type getRawType() {
                return Map.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        });
        JavaType[] params = tf.findTypeParameters(mapType, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        assertEquals(String.class, params[0].getRawClass());
        assertEquals(Integer.class, params[1].getRawClass());
    }

    @Test
    public void testFindTypeParametersNonGeneric() {
        JavaType stringType = tf.constructType(String.class);
        JavaType[] params = tf.findTypeParameters(stringType, Comparable.class);
        assertNotNull(params);
        assertEquals(0, params.length);
    }

    @Test
    public void testFindTypeParametersNullType() {
        try {
            tf.findTypeParameters(null, Map.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- defaultInstance ---

    @Test
    public void testDefaultInstanceSingleton() {
        TypeFactory instance1 = TypeFactory.defaultInstance();
        TypeFactory instance2 = TypeFactory.defaultInstance();
        assertSame(instance1, instance2);
    }

    // --- clearCache ---

    @Test
    public void testClearCache() {
        // Construct a type to populate cache
        tf.constructType(String.class);
        // Clear cache
        tf.clearCache();
        // Reconstruct should work
        JavaType type = tf.constructType(String.class);
        assertNotNull(type);
    }

    // --- more edge cases ---

    @Test
    public void testConstructTypeRawList() {
        JavaType type = tf.constructType(List.class);
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        // Raw List should have no type parameters resolved
        assertEquals(0, type.containedTypeCount());
    }

    @Test
    public void testConstructTypeNestedParameterized() {
        // Map<String, List<Integer>>
        Type nested = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{String.class, new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[]{Integer.class};
                    }

                    @Override
                    public Type getRawType() {
                        return List.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                }};
            }

            @Override
            public Type getRawType() {
                return Map.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        JavaType result = tf.constructType(nested);
        assertNotNull(result);
        assertEquals(Map.class, result.getRawClass());
        JavaType valueType = result.containedType(1);
        assertEquals(List.class, valueType.getRawClass());
        assertEquals(Integer.class, valueType.containedType(0).getRawClass());
    }

    @Test
    public void testConstructTypeWithEmptyTypeBindings() {
        TypeBindings empty = TypeBindings.emptyBindings();
        JavaType type = tf.constructType(String.class, empty);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructTypeWithBindingsNull() {
        // Should treat null bindings as empty
        JavaType type = tf.constructType(String.class, (TypeBindings) null);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    // --- potential bug-triggering tests (Defects4J bug 11) ---
    // Bug 11 likely involves type resolution with TypeVariable that has no bounds
    // or handling of recursive type variables.

    @Test
    public void testTypeVariableWithNoBounds() {
        // Type variable with no explicit bounds (extends Object)
        TypeVariable<?> tv = new TypeVariable<?>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Object.class}; // default bound
            }

            @Override
            public String getName() {
                return "E";
            }

            @Override
            public GenericDeclaration getGenericDeclaration() {
                return null;
            }
        };
        JavaType result = tf.constructType(tv);
        assertNotNull(result);
        // Should resolve to Object
        assertEquals(Object.class, result.getRawClass());
    }

    @Test
    public void testTypeVariableWithMultipleBounds() {
        // T extends Comparable & Serializable
        TypeVariable<?> tv = new TypeVariable<?>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Comparable.class, Serializable.class};
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public GenericDeclaration getGenericDeclaration() {
                return null;
            }
        };
        JavaType result = tf.constructType(tv);
        assertNotNull(result);
        // Should resolve to first bound (Comparable)
        assertEquals(Comparable.class, result.getRawClass());
    }

    @Test
    public void testConstructTypeWithRecursiveTypeVariable() {
        // Simulate a recursive type: T extends Comparable<T>
        // This may cause infinite recursion if not handled properly.
        TypeVariable<?> tv = new TypeVariable<?>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[]{this}; // recursive reference
                    }

                    @Override
                    public Type getRawType() {
                        return Comparable.class;
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }
                }};
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public GenericDeclaration getGenericDeclaration() {
                return null;
            }
        };
        // This should not throw StackOverflowError
        JavaType result = tf.constructType(tv);
        assertNotNull(result);
        // Should resolve to Comparable (with type variable as parameter)
        assertEquals(Comparable.class, result.getRawClass());
    }

    @Test
    public void testConstructTypeWithSelfReferencingClass() {
        // A class that implements Comparable<Itself>
        // This is a common pattern that may cause issues.
        // We'll use a mock class that is not available; instead we test via parameterized type.
        Type selfRef = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{this}; // self reference
            }

            @Override
            public Type getRawType() {
                return Comparable.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        JavaType result = tf.constructType(selfRef);
        assertNotNull(result);
        assertEquals(Comparable.class, result.getRawClass());
        // The type argument should be a reference to the same type
        assertNotNull(result.containedType(0));
    }

    // --- additional coverage for internal methods ---

    @Test
    public void testConstructArrayTypeFromClass() {
        JavaType type = tf.constructType(Integer[].class);
        assertTrue(type.isArrayType());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructPrimitiveArrayType() {
        JavaType type = tf.constructType(byte[].class);
        assertTrue(type.isArrayType());
        assertEquals(byte.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeWithUnboundWildcard() {
        // ? (unbounded wildcard)
        Type wildcard = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[]{};
            }
        };
        JavaType result = tf.constructType(wildcard);
        assertNotNull(result);
        assertEquals(Object.class, result.getRawClass());
    }

    @Test
    public void testConstructTypeWithLowerBoundedWildcard() {
        // ? super Integer
        Type wildcard = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[]{Integer.class};
            }
        };
        JavaType result = tf.constructType(wildcard);
        assertNotNull(result);
        // Lower bounded wildcard should resolve to the lower bound? Typically Object.
        // This depends on implementation; just check not null.
        assertNotNull(result);
    }

    // --- test for TypeBindings creation ---

    @Test
    public void testTypeBindingsCreate() {
        TypeBindings bindings = TypeBindings.create(String.class, Integer.class);
        assertNotNull(bindings);
        assertEquals(2, bindings.size());
    }

    @Test
    public void testTypeBindingsEmpty() {
        TypeBindings empty = TypeBindings.emptyBindings();
        assertNotNull(empty);
        assertEquals(0, empty.size());
    }

    // --- test for JavaType methods ---

    @Test
    public void testJavaTypeIsAbstract() {
        JavaType type = tf.constructType(AbstractList.class);
        assertTrue(type.isAbstract());
    }

    @Test
    public void testJavaTypeIsInterface() {
        JavaType type = tf.constructType(List.class);
        assertTrue(type.isInterface());
    }

    @Test
    public void testJavaTypeIsFinal() {
        JavaType type = tf.constructType(String.class);
        assertTrue(type.isFinal());
    }

    // --- test for handling of java.lang.Object ---

    @Test
    public void testConstructTypeObject() {
        JavaType type = tf.constructType(Object.class);
        assertNotNull(type);
        assertEquals(Object.class, type.getRawClass());
        assertFalse(type.isPrimitive());
    }

    // --- test for handling of void.class (already done) ---

    // --- test for null TypeBindings in constructType(Type, TypeBindings) ---

    @Test
    public void testConstructTypeWithNullBindingsAndType() {
        try {
            tf.constructType((Type) null, (TypeBindings) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- test for constructType with Type that is a Class<?> ---

    @Test
    public void testConstructTypeWithClassAsType() {
        JavaType type = tf.constructType((Type) Integer.class);
        assertNotNull(type);
        assertEquals(Integer.class, type.getRawClass());
    }

    // --- test for constructType with ParameterizedType that has owner type ---

    @Test
    public void testConstructTypeWithOwnerType() {
        // Simulate Map.Entry<String,Integer>
        Type owner = Map.class;
        Type rawType = Map.Entry.class;
        Type[] args = new Type[]{String.class, Integer.class};
        ParameterizedType pt = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return args;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return owner;
            }
        };
        JavaType result = tf.constructType(pt);
        assertNotNull(result);
        assertEquals(Map.Entry.class, result.getRawClass());
        assertEquals(2, result.containedTypeCount());
    }

    // --- test for constructType with GenericArrayType of primitive ---

    @Test
    public void testConstructGenericArrayOfPrimitive() {
        // int[] as generic array type (unusual but possible)
        GenericArrayType gat = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return int.class;
            }
        };
        JavaType result = tf.constructType(gat);
        assertNotNull(result);
        assertTrue(result.isArrayType());
        assertEquals(int.class, result.getContentType().getRawClass());
    }

    // --- test for findTypeParameters with null type ---

    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParametersNullJavaType() {
        tf.findTypeParameters((JavaType) null, Map.class);
    }

    // --- test for findTypeParameters with null class ---

    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParametersNullClass() {
        JavaType mapType = tf.constructType(Map.class);
        tf.findTypeParameters(mapType, (Class<?>) null);
    }

    // --- test for constructType with TypeBindings containing TypeVariable ---

    @Test
    public void testConstructTypeWithBindingsContainingTypeVariable() {
        // Create a TypeVariable for "T"
        TypeVariable<?> tv = new TypeVariable<?>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public GenericDeclaration getGenericDeclaration() {
                return null;
            }
        };
        TypeBindings bindings = TypeBindings.create(tv, String.class);
        // Construct a parameterized type that uses T
        Type paramType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{tv};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        JavaType result = tf.constructType(paramType, bindings);
        assertNotNull(result);
        assertEquals(List.class, result.getRawClass());
        // The type argument should be resolved to String
        assertEquals(String.class, result.containedType(0).getRawClass());
    }

    // --- test for clearCache after many constructions ---

    @Test
    public void testClearCacheAfterMultipleTypes() {
        tf.constructType(String.class);
        tf.constructType(Integer.class);
        tf.constructType(List.class);
        tf.clearCache();
        // Ensure cache is cleared by constructing again
        JavaType type = tf.constructType(String.class);
        assertNotNull(type);
    }

    // --- test for defaultInstance thread safety? Not required, but basic ---

    @Test
    public void testDefaultInstanceNotNull() {
        assertNotNull(TypeFactory.defaultInstance());
    }
}