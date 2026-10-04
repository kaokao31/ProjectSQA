package com.google.gson.internal;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.lang.reflect.*;
import java.util.*;

public class $Gson$TypesTest {

    private Class<?> testClass;
    private Type testType;

    @Before
    public void setUp() throws Exception {
        testClass = String.class;
        testType = String.class;
    }

    // Helper method to create parameterized types for testing
    private ParameterizedType createParameterizedType(final Type rawType, final Type... typeArgs) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return typeArgs;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    // Test for getRawType method
    @Test
    public void testGetRawType_Class() {
        assertEquals(String.class, $Gson$Types.getRawType(String.class));
    }

    @Test
    public void testGetRawType_ParameterizedType() {
        ParameterizedType pt = createParameterizedType(List.class, String.class);
        assertEquals(List.class, $Gson$Types.getRawType(pt));
    }

    @Test
    public void testGetRawType_GenericArrayType() {
        GenericArrayType gat = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        assertEquals(String[].class, $Gson$Types.getRawType(gat));
    }

    @Test
    public void testGetRawType_WildcardType() {
        WildcardType wt = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };
        assertEquals(Object.class, $Gson$Types.getRawType(wt));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRawType_TypeVariable() {
        TypeVariable<?> tv = String.class.getTypeParameters().length > 0 ? String.class.getTypeParameters()[0] : null;
        if (tv == null) {
            // Create a synthetic TypeVariable for testing
            tv = new TypeVariable<String>() {
                @Override
                public Type[] getBounds() {
                    return new Type[]{Object.class};
                }

                @Override
                public DummyGenericDeclaration getGenericDeclaration() {
                    return null;
                }

                @Override
                public String getName() {
                    return "T";
                }

                @Override
                public AnnotatedType[] getAnnotatedBounds() {
                    return new AnnotatedType[0];
                }
            };
        }
        $Gson$Types.getRawType(tv);
    }

    // Test for equals method
    @Test
    public void testEquals_SameObject() {
        assertTrue($Gson$Types.equals(String.class, String.class));
    }

    @Test
    public void testEquals_NullFirst() {
        assertFalse($Gson$Types.equals(null, String.class));
    }

    @Test
    public void testEquals_NullSecond() {
        assertFalse($Gson$Types.equals(String.class, null));
    }

    @Test
    public void testEquals_BothNull() {
        assertTrue($Gson$Types.equals(null, null));
    }

    @Test
    public void testEquals_ClassVsClass() {
        assertTrue($Gson$Types.equals(Integer.class, Integer.class));
        assertFalse($Gson$Types.equals(Integer.class, String.class));
    }

    @Test
    public void testEquals_ParameterizedType() {
        ParameterizedType pt1 = createParameterizedType(List.class, String.class);
        ParameterizedType pt2 = createParameterizedType(List.class, String.class);
        ParameterizedType pt3 = createParameterizedType(List.class, Integer.class);
        assertTrue($Gson$Types.equals(pt1, pt2));
        assertFalse($Gson$Types.equals(pt1, pt3));
    }

    @Test
    public void testEquals_GenericArrayType() {
        GenericArrayType gat1 = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        GenericArrayType gat2 = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        GenericArrayType gat3 = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return Integer.class;
            }
        };
        assertTrue($Gson$Types.equals(gat1, gat2));
        assertFalse($Gson$Types.equals(gat1, gat3));
    }

    @Test
    public void testEquals_WildcardType() {
        WildcardType wt1 = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };
        WildcardType wt2 = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };
        assertTrue($Gson$Types.equals(wt1, wt2));
    }

    @Test
    public void testEquals_TypeVariable() {
        TypeVariable<?> tv1 = new TypeVariable<String>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public DummyGenericDeclaration getGenericDeclaration() {
                return null;
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public AnnotatedType[] getAnnotatedBounds() {
                return new AnnotatedType[0];
            }
        };
        TypeVariable<?> tv2 = new TypeVariable<String>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public DummyGenericDeclaration getGenericDeclaration() {
                return null;
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public AnnotatedType[] getAnnotatedBounds() {
                return new AnnotatedType[0];
            }
        };
        assertTrue($Gson$Types.equals(tv1, tv2));
    }

    // Test for hashCode method
    @Test
    public void testHashCode_Class() {
        assertEquals(String.class.hashCode(), $Gson$Types.hashCode(String.class));
    }

    @Test
    public void testHashCode_ParameterizedType() {
        ParameterizedType pt = createParameterizedType(List.class, String.class);
        int expected = pt.getRawType().hashCode() ^ Arrays.hashCode(pt.getActualTypeArguments());
        assertEquals(expected, $Gson$Types.hashCode(pt));
    }

    @Test
    public void testHashCode_GenericArrayType() {
        GenericArrayType gat = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        assertEquals(gat.getGenericComponentType().hashCode(), $Gson$Types.hashCode(gat));
    }

    @Test
    public void testHashCode_WildcardType() {
        WildcardType wt = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };
        assertEquals(Arrays.hashCode(wt.getUpperBounds()) ^ Arrays.hashCode(wt.getLowerBounds()), $Gson$Types.hashCode(wt));
    }

    @Test
    public void testHashCode_TypeVariable() {
        TypeVariable<?> tv = new TypeVariable<String>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public DummyGenericDeclaration getGenericDeclaration() {
                return null;
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public AnnotatedType[] getAnnotatedBounds() {
                return new AnnotatedType[0];
            }
        };
        assertEquals(tv.hashCode(), $Gson$Types.hashCode(tv));
    }

    // Test for canonicalize method
    @Test
    public void testCanonicalize_Class() {
        assertSame(String.class, $Gson$Types.canonicalize(String.class));
    }

    @Test
    public void testCanonicalize_ParameterizedType() {
        ParameterizedType pt = createParameterizedType(List.class, String.class);
        Type result = $Gson$Types.canonicalize(pt);
        assertTrue(result instanceof ParameterizedType);
        ParameterizedType resultPt = (ParameterizedType) result;
        assertEquals(List.class, resultPt.getRawType());
        assertEquals(1, resultPt.getActualTypeArguments().length);
        assertEquals(String.class, resultPt.getActualTypeArguments()[0]);
    }

    @Test
    public void testCanonicalize_GenericArrayType() {
        GenericArrayType gat = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        Type result = $Gson$Types.canonicalize(gat);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test
    public void testCanonicalize_WildcardType() {
        WildcardType wt = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };
        Type result = $Gson$Types.canonicalize(wt);
        assertTrue(result instanceof WildcardType);
    }

    @Test
    public void testCanonicalize_TypeVariable() {
        TypeVariable<?> tv = new TypeVariable<String>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public DummyGenericDeclaration getGenericDeclaration() {
                return null;
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public AnnotatedType[] getAnnotatedBounds() {
                return new AnnotatedType[0];
            }
        };
        Type result = $Gson$Types.canonicalize(tv);
        assertTrue(result instanceof TypeVariable);
    }

    // Test for getCollectionElementType method
    @Test
    public void testGetCollectionElementType_List() throws Exception {
        ParameterizedType listType = createParameterizedType(List.class, String.class);
        assertEquals(String.class, $Gson$Types.getCollectionElementType(listType, List.class));
    }

    @Test
    public void testGetCollectionElementType_ArrayList() throws Exception {
        ParameterizedType arrayListType = createParameterizedType(ArrayList.class, Integer.class);
        assertEquals(Integer.class, $Gson$Types.getCollectionElementType(arrayListType, ArrayList.class));
    }

    @Test
    public void testGetCollectionElementType_Subclass() throws Exception {
        ParameterizedType subListType = createParameterizedType(AbstractList.class, Double.class);
        assertEquals(Double.class, $Gson$Types.getCollectionElementType(subListType, AbstractList.class));
    }

    // Test for getMapKeyAndValueTypes method
    @Test
    public void testGetMapKeyAndValueTypes_HashMap() throws Exception {
        ParameterizedType mapType = createParameterizedType(HashMap.class, String.class, Integer.class);
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(mapType, HashMap.class);
        assertEquals(2, result.length);
        assertEquals(String.class, result[0]);
        assertEquals(Integer.class, result[1]);
    }

    @Test
    public void testGetMapKeyAndValueTypes_Map() throws Exception {
        ParameterizedType mapType = createParameterizedType(Map.class, String.class, Integer.class);
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
        assertEquals(2, result.length);
        assertEquals(String.class, result[0]);
        assertEquals(Integer.class, result[1]);
    }

    @Test
    public void testGetMapKeyAndValueTypes_Subclass() throws Exception {
        ParameterizedType subMapType = createParameterizedType(AbstractMap.class, String.class, Integer.class);
        Type[] result = $Gson$Types.getMapKeyAndValueTypes(subMapType, AbstractMap.class);
        assertEquals(2, result.length);
        assertEquals(String.class, result[0]);
        assertEquals(Integer.class, result[1]);
    }

    // Test for resolve method
    @Test
    public void testResolve_SimpleType() throws Exception {
        Type result = $Gson$Types.resolve(String.class, String.class, String.class);
        assertEquals(String.class, result);
    }

    @Test
    public void testResolve_TypeVariable() throws Exception {
        TypeVariable<?> tv = new TypeVariable<String>() {
            @Override
            public Type[] getBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public DummyGenericDeclaration getGenericDeclaration() {
                return null;
            }

            @Override
            public String getName() {
                return "T";
            }

            @Override
            public AnnotatedType[] getAnnotatedBounds() {
                return new AnnotatedType[0];
            }
        };
        Type result = $Gson$Types.resolve(String.class, String.class, tv);
        assertEquals(String.class, result);
    }

    @Test
    public void testResolve_ParameterizedType() throws Exception {
        ParameterizedType pt = createParameterizedType(List.class, String.class);
        Type result = $Gson$Types.resolve(String.class, String.class, pt);
        assertTrue(result instanceof ParameterizedType);
        ParameterizedType resultPt = (ParameterizedType) result;
        assertEquals(List.class, resultPt.getRawType());
        assertEquals(String.class, resultPt.getActualTypeArguments()[0]);
    }

    @Test
    public void testResolve_GenericArrayType() throws Exception {
        GenericArrayType gat = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        Type result = $Gson$Types.resolve(String.class, String.class, gat);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test
    public void testResolve_WildcardType() throws Exception {
        WildcardType wt = new WildcardType() {
            @Override
            public Type[] getUpperBounds() {
                return new Type[]{Object.class};
            }

            @Override
            public Type[] getLowerBounds() {
                return new Type[0];
            }
        };
        Type result = $Gson$Types.resolve(String.class, String.class, wt);
        assertTrue(result instanceof WildcardType);
    }

    // Test for getGenericSupertype method
    @Test
    public void testGetGenericSupertype_DirectSuperclass() throws Exception {
        Type result = $Gson$Types.getGenericSupertype(String.class, String.class, Object.class);
        assertEquals(Object.class, result);
    }

    @Test
    public void testGetGenericSupertype_Interface() throws Exception {
        Type result = $Gson$Types.getGenericSupertype(ArrayList.class, ArrayList.class, List.class);
        assertTrue(result instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) result;
        assertEquals(List.class, pt.getRawType());
        assertEquals(1, pt.getActualTypeArguments().length);
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
    }

    @Test
    public void testGetGenericSupertype_IndirectSuperclass() throws Exception {
        Type result = $Gson$Types.getGenericSupertype(ArrayList.class, ArrayList.class, AbstractList.class);
        assertTrue(result instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) result;
        assertEquals(AbstractList.class, pt.getRawType());
        assertEquals(1, pt.getActualTypeArguments().length);
        assertEquals(String.class, pt.getActualTypeArguments()[0]);
    }

    // Test for subtypeOf method
    @Test
    public void testSubtypeOf_Class() throws Exception {
        WildcardType wt = $Gson$Types.subtypeOf(String.class);
        assertArrayEquals(new Type[]{String.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{}, wt.getLowerBounds());
    }

    @Test
    public void testSubtypeOf_Object() throws Exception {
        WildcardType wt = $Gson$Types.subtypeOf(Object.class);
        assertArrayEquals(new Type[]{Object.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{}, wt.getLowerBounds());
    }

    // Test for supertypeOf method
    @Test
    public void testSupertypeOf_Class() throws Exception {
        WildcardType wt = $Gson$Types.supertypeOf(String.class);
        assertArrayEquals(new Type[]{Object.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{String.class}, wt.getLowerBounds());
    }

    @Test
    public void testSupertypeOf_Object() throws Exception {
        WildcardType wt = $Gson$Types.supertypeOf(Object.class);
        assertArrayEquals(new Type[]{Object.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{Object.class}, wt.getLowerBounds());
    }

    // Test for arrayOf method
    @Test
    public void testArrayOf_Class() throws Exception {
        Type result = $Gson$Types.arrayOf(String.class);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test
    public void testArrayOf_ParameterizedType() throws Exception {
        ParameterizedType pt = createParameterizedType(List.class, String.class);
        Type result = $Gson$Types.arrayOf(pt);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(pt, ((GenericArrayType) result).getGenericComponentType());
    }

    // Test for getArrayComponentType method
    @Test
    public void testGetArrayComponentType_GenericArrayType() throws Exception {
        GenericArrayType gat = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        assertEquals(String.class, $Gson$Types.getArrayComponentType(gat));
    }

    @Test
    public void testGetArrayComponentType_ClassArray() throws Exception {
        assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    }

    // Test for getGenericArrayType method
    @Test
    public void testGetGenericArrayType_Class() throws Exception {
        Type result = $Gson$Types.getGenericArrayType(String.class);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test
    public void testGetGenericArrayType_ParameterizedType() throws Exception {
        ParameterizedType pt = createParameterizedType(List.class, String.class);
        Type result = $Gson$Types.getGenericArrayType(pt);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(pt, ((GenericArrayType) result).getGenericComponentType());
    }

    // Helper interface for TypeVariable testing
    private interface DummyGenericDeclaration extends GenericDeclaration {
    }
}