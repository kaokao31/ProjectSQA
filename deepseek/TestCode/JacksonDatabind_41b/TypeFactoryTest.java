package com.fasterxml.jackson.databind.type;

import static org.junit.Assert.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeParser;

public class TypeFactoryTest {
    
    private TypeFactory tf;
    
    @Before
    public void setUp() {
        tf = TypeFactory.defaultInstance();
    }
    
    // Test basic construction
    @Test
    public void testDefaultInstance() {
        assertNotNull(tf);
    }
    
    // Test with simple class types
    @Test
    public void testConstructSimpleType() {
        JavaType type = tf.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }
    
    @Test
    public void testConstructPrimitiveType() {
        JavaType type = tf.constructType(int.class);
        assertNotNull(type);
        assertTrue(type.isPrimitive());
    }
    
    @Test
    public void testConstructArrayType() {
        JavaType type = tf.constructType(String[].class);
        assertNotNull(type);
        assertTrue(type.isArrayType());
    }
    
    // Test with parameterized types
    @Test
    public void testConstructParameterizedType() throws Exception {
        // Create a parameterized type for List<String>
        Type type = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { String.class };
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
    
    // Test with TypeBindings
    @Test
    public void testConstructWithTypeBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, Integer.class);
        JavaType type = tf.constructType(Map.class, bindings);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
    }
    
    // Test with TypeParser
    @Test
    public void testConstructWithTypeParser() {
        TypeParser parser = new TypeParser(tf);
        JavaType type = parser.parse("java.util.List<java.lang.String>");
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
    }
    
    // Test edge cases
    @Test
    public void testConstructWithNull() {
        try {
            tf.constructType((Class<?>) null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test
    public void testConstructWithVoid() {
        JavaType type = tf.constructType(void.class);
        assertNotNull(type);
        assertEquals(void.class, type.getRawClass());
    }
    
    @Test
    public void testConstructWithWildcard() throws Exception {
        Type wildcardType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { new java.lang.reflect.WildcardType() {
                    @Override
                    public Type[] getUpperBounds() {
                        return new Type[] { Object.class };
                    }
                    
                    @Override
                    public Type[] getLowerBounds() {
                        return new Type[] { };
                    }
                }};
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
        
        JavaType result = tf.constructType(wildcardType);
        assertNotNull(result);
    }
    
    // Test with generic array type
    @Test
    public void testConstructGenericArrayType() throws Exception {
        Type genericArrayType = new java.lang.reflect.GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        
        JavaType result = tf.constructType(genericArrayType);
        assertNotNull(result);
        assertTrue(result.isArrayType());
    }
    
    // Test with TypeVariable
    @Test
    public void testConstructWithTypeVariable() throws Exception {
        TypeVariable<?> tv = new TypeVariable<String>() {
            @Override
            public Type[] getBounds() {
                return new Type[] { Object.class };
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
    }
    
    // Test with raw collections
    @Test
    public void testConstructRawCollection() {
        JavaType type = tf.constructType(ArrayList.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
    }
    
    @Test
    public void testConstructRawMap() {
        JavaType type = tf.constructType(HashMap.class);
        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
    }
    
    // Test with nested generics
    @Test
    public void testConstructNestedGenerics() throws Exception {
        Type nestedType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { 
                    new ParameterizedType() {
                        @Override
                        public Type[] getActualTypeArguments() {
                            return new Type[] { String.class };
                        }
                        
                        @Override
                        public Type getRawType() {
                            return List.class;
                        }
                        
                        @Override
                        public Type getOwnerType() {
                            return null;
                        }
                    }
                };
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
        
        JavaType result = tf.constructType(nestedType);
        assertNotNull(result);
        assertEquals(Map.class, result.getRawClass());
    }
    
    // Test with multiple type parameters
    @Test
    public void testConstructMultipleTypeParams() throws Exception {
        Type multiParamType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { String.class, Integer.class };
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
        
        JavaType result = tf.constructType(multiParamType);
        assertNotNull(result);
        assertEquals(2, result.containedTypeCount());
        assertEquals(String.class, result.containedType(0).getRawClass());
        assertEquals(Integer.class, result.containedType(1).getRawClass());
    }
    
    // Test with bounded wildcards
    @Test
    public void testConstructWithBoundedWildcard() throws Exception {
        Type boundedWildcard = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { 
                    new java.lang.reflect.WildcardType() {
                        @Override
                        public Type[] getUpperBounds() {
                            return new Type[] { Number.class };
                        }
                        
                        @Override
                        public Type[] getLowerBounds() {
                            return new Type[] { };
                        }
                    }
                };
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
        
        JavaType result = tf.constructType(boundedWildcard);
        assertNotNull(result);
    }
    
    // Test with lower bounded wildcard
    @Test
    public void testConstructWithLowerBoundedWildcard() throws Exception {
        Type lowerBoundedWildcard = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { 
                    new java.lang.reflect.WildcardType() {
                        @Override
                        public Type[] getUpperBounds() {
                            return new Type[] { Object.class };
                        }
                        
                        @Override
                        public Type[] getLowerBounds() {
                            return new Type[] { Integer.class };
                        }
                    }
                };
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
        
        JavaType result = tf.constructType(lowerBoundedWildcard);
        assertNotNull(result);
    }
    
    // Test with enum type
    @Test
    public void testConstructEnumType() {
        JavaType type = tf.constructType(DayOfWeek.class);
        assertNotNull(type);
        assertTrue(type.isEnumType());
    }
    
    // Test with interface type
    @Test
    public void testConstructInterfaceType() {
        JavaType type = tf.constructType(Runnable.class);
        assertNotNull(type);
        assertTrue(type.isInterface());
    }
    
    // Test with abstract class
    @Test
    public void testConstructAbstractClass() {
        JavaType type = tf.constructType(AbstractList.class);
        assertNotNull(type);
        assertTrue(type.isAbstract());
    }
    
    // Test with final class
    @Test
    public void testConstructFinalClass() {
        JavaType type = tf.constructType(String.class);
        assertNotNull(type);
        assertTrue(type.isFinal());
    }
    
    // Test with inner class
    @Test
    public void testConstructInnerClass() {
        JavaType type = tf.constructType(Map.Entry.class);
        assertNotNull(type);
        assertEquals(Map.Entry.class, type.getRawClass());
    }
    
    // Test with anonymous class
    @Test
    public void testConstructAnonymousClass() {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "test";
            }
        };
        JavaType type = tf.constructType(callable.getClass());
        assertNotNull(type);
    }
    
    // Test with primitive array
    @Test
    public void testConstructPrimitiveArray() {
        JavaType type = tf.constructType(int[].class);
        assertNotNull(type);
        assertTrue(type.isArrayType());
        assertTrue(type.getContentType().isPrimitive());
    }
    
    // Test with multi-dimensional array
    @Test
    public void testConstructMultiDimensionalArray() {
        JavaType type = tf.constructType(String[][].class);
        assertNotNull(type);
        assertTrue(type.isArrayType());
        assertTrue(type.getContentType().isArrayType());
    }
    
    // Test with TypeBindings containing multiple types
    @Test
    public void testConstructWithMultipleTypeBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, Integer.class, Boolean.class);
        JavaType type = tf.constructType(Map.class, bindings);
        assertNotNull(type);
        assertEquals(Map.class, type.getRawClass());
    }
    
    // Test with empty TypeBindings
    @Test
    public void testConstructWithEmptyTypeBindings() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        JavaType type = tf.constructType(String.class, bindings);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }
    
    // Test with null TypeBindings
    @Test
    public void testConstructWithNullTypeBindings() {
        JavaType type = tf.constructType(String.class, (TypeBindings) null);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }
    
    // Test with recursive type
    @Test
    public void testConstructRecursiveType() throws Exception {
        Type recursiveType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { this };
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
        
        JavaType result = tf.constructType(recursiveType);
        assertNotNull(result);
    }
    
    // Test with self-referencing generic
    @Test
    public void testConstructSelfReferencingGeneric() throws Exception {
        Type selfRefType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { Comparable.class };
            }
            
            @Override
            public Type getRawType() {
                return Enum.class;
            }
            
            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        
        JavaType result = tf.constructType(selfRefType);
        assertNotNull(result);
    }
    
    // Test with intersection type
    @Test
    public void testConstructIntersectionType() throws Exception {
        Type intersectionType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { 
                    new java.lang.reflect.WildcardType() {
                        @Override
                        public Type[] getUpperBounds() {
                            return new Type[] { Serializable.class, Comparable.class };
                        }
                        
                        @Override
                        public Type[] getLowerBounds() {
                            return new Type[] { };
                        }
                    }
                };
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
        
        JavaType result = tf.constructType(intersectionType);
        assertNotNull(result);
    }
    
    // Test with type variable bounds
    @Test
    public void testConstructWithTypeVariableBounds() throws Exception {
        TypeVariable<?> tv = new TypeVariable<String>() {
            @Override
            public Type[] getBounds() {
                return new Type[] { Number.class, Serializable.class };
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
    }
    
    // Test with parameterized inner class
    @Test
    public void testConstructParameterizedInnerClass() throws Exception {
        Type innerParamType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { String.class };
            }
            
            @Override
            public Type getRawType() {
                return Map.Entry.class;
            }
            
            @Override
            public Type getOwnerType() {
                return Map.class;
            }
        };
        
        JavaType result = tf.constructType(innerParamType);
        assertNotNull(result);
    }
    
    // Test with owner type
    @Test
    public void testConstructWithOwnerType() throws Exception {
        Type ownerType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] { String.class };
            }
            
            @Override
            public Type getRawType() {
                return Map.Entry.class;
            }
            
            @Override
            public Type getOwnerType() {
                return new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[] { String.class, Integer.class };
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
            }
        };
        
        JavaType result = tf.constructType(ownerType);
        assertNotNull(result);
    }
    
    // Test with generic declaration
    @Test
    public void testConstructWithGenericDeclaration() throws Exception {
        GenericDeclaration gd = new GenericDeclaration() {
            @Override
            public TypeVariable<?>[] getTypeParameters() {
                return new TypeVariable<?>[] {
                    new TypeVariable<String>() {
                        @Override
                        public Type[] getBounds() {
                            return new Type[] { Object.class };
                        }
                        
                        @Override
                        public String getName() {
                            return "T";
                        }
                        
                        @Override
                        public GenericDeclaration getGenericDeclaration() {
                            return this;
                        }
                    }
                };
            }
        };
        
        TypeVariable<?> tv = gd.getTypeParameters()[0];
        JavaType result = tf.constructType(tv);
        assertNotNull(result);
    }
    
    // Helper enum for testing
    private enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}