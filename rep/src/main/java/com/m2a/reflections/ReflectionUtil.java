package com.m2a.reflections;

import com.m2a.util.StringUtil;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

public class ReflectionUtil {

    private static final Logger logger = Logger.getLogger(ReflectionUtil.class.getName());

    private static final Class<?>[] WRAPPER_TYPES =
            {int.class, long.class, short.class, float.class, double.class, byte.class, boolean.class, char.class};

    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> superClass = clazz;
             superClass != Object.class;
             superClass = superClass.getSuperclass()) {
            Collections.addAll(fields, superClass.getDeclaredFields());
        }
        return fields;
    }

    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        ArrayList<E> list = new ArrayList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    public static Class<?> getGenericFieldClassType(Field field) throws SecurityException {
        field.setAccessible(true);
        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }

    public static Field getField(Class<?> clazz, String name) {
        for (Class<?> superClass = clazz;
             superClass != Object.class;
             superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(name);
            } catch (NoSuchFieldException nsf) {
                logger.info(name + " field not found");
                nsf.fillInStackTrace();
            }
        }
        return null;
    }

    public static Collection<?> instantiateCollection(Class<?> t) {

        if (t == Set.class) {
            return new HashSet<>();
        } else if (t == List.class) {
            return new ArrayList<>();
        } else if (t == Map.class) {
            throw new RuntimeException("can not instantiate map");
        } else if (t == Vector.class) {
            throw new RuntimeException("can not instantiate vector");
        } else
            throw new RuntimeException("unknown type");
    }

    public static void set(Field field, Object target, Object value) throws Exception {
        try {
            field.set(target, value);
        } catch (IllegalArgumentException iae) {
            // target may be null if field is static so use
            // field.getDeclaringClass() instead
            String message = "Could not set field value by reflection: " + field + " on: " + field.getDeclaringClass().getName();
            if (value == null) {
                message += " with null value";
            } else {
                message += " with value: " + value.getClass();
            }
            throw new IllegalArgumentException(message, iae);
        }
    }

    public static boolean isPrimitive(Class<?> type) {
        return primitiveTypeFor(type) != null;
    }

    public static boolean isWrapper(Class<?> clazz) {
        if (clazz == null)
            throw new RuntimeException("null value");
        for (Class<?> wrapperType : WRAPPER_TYPES) {
            if (clazz == wrapperType)
                return true;
        }
        return false;
    }

    public static boolean isArrayOrCollection(Class<?> clazz) {
        if (clazz == null)
            throw new RuntimeException("null value");
        return clazz.isArray() || isSubclass(clazz, Collection.class);
    }

    public static boolean isMap(Class<?> clazz) {
        if (clazz == null)
            throw new RuntimeException("null value");
        return isSubclass(clazz, Map.class);
    }

    public static boolean isEnum(Class<?> clazz) {
        if (clazz == null)
            throw new RuntimeException("null value");
        return clazz.isEnum();
    }

    @SuppressWarnings("unchecked")
    public static boolean isSubclass(Class<?> class1, Class<?> class2) {
        List<Class<?>> superClasses = (List<Class<?>>) getAllSuperclasses(class1);
        List<Class<?>> superInterfaces = (List<Class<?>>) getAllInterfaces(class1);
        for (Class<?> c : superClasses) {
            if (class2 == c)
                return true;
        }
        for (Class<?> c : superInterfaces) {
            if (class2 == c)
                return true;
        }
        return false;
    }

    public static List<?> getAllSuperclasses(Class<?> cls) {
        if (cls == null) {
            return null;
        }
        List<Object> classes = new ArrayList<>();
        Class<?> superclass = cls.getSuperclass();
        while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    public static List<?> getAllInterfaces(Class<?> cls) {
        if (cls == null)
            return null;
        List<Object> list = new ArrayList<>();
        while (cls != null) {
            Class<?>[] interfaces = cls.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                if (!list.contains(anInterface))
                    list.add(anInterface);
                List<?> superInterfaces = getAllInterfaces(anInterface);
                for (Object superInterface : superInterfaces) {
                    Class<?> _interface = (Class<?>) superInterface;
                    if (!list.contains(_interface))
                        list.add(_interface);
                }
            }
            cls = cls.getSuperclass();
        }
        return list;
    }

    public static Class<?> primitiveTypeFor(Class<?> wrapper) {
        if (wrapper == Boolean.class)
            return Boolean.TYPE;
        if (wrapper == Byte.class)
            return Byte.TYPE;
        if (wrapper == Character.class)
            return Character.TYPE;
        if (wrapper == Short.class)
            return Short.TYPE;
        if (wrapper == BigDecimal.class)
            return BigDecimal.class;
        if (wrapper == Date.class)
            return Date.class;
        if (wrapper == java.sql.Date.class)
            return java.sql.Date.class;
        if (wrapper == Integer.class)
            return Integer.TYPE;
        if (wrapper == Long.class)
            return Long.TYPE;
        if (wrapper == Float.class)
            return Float.TYPE;
        if (wrapper == Double.class)
            return Double.TYPE;
        if (wrapper == Void.class)
            return Void.TYPE;
        if (wrapper == String.class)
            return String.class;
        return null;
    }

    public static Object toObject(Class<?> clazz, Object value) {
        if (value == null)
            return null;
        if (value instanceof String v)
            if (StringUtil.isEmpty(v))
                return null;
        if (Boolean.class == clazz || boolean.class == clazz)
            return value;
        if (Date.class == clazz) {
            if (value instanceof String) {
                throw new IllegalArgumentException("persian date can not be handled yet");
            } else {
                return value;
            }
        }
        if (Byte.class == clazz || byte.class == clazz)
            return Byte.parseByte(String.valueOf(value));
        if (Short.class == clazz || short.class == clazz)
            return Short.parseShort(String.valueOf(value));
        if (BigDecimal.class == clazz)
            return new BigDecimal(String.valueOf(value));
        if (Integer.class == clazz || int.class == clazz)
            return Integer.parseInt(String.valueOf(value));
        if (Long.class == clazz || long.class == clazz)
            return Long.parseLong(String.valueOf(value));
        if (Float.class == clazz || float.class == clazz)
            return Float.parseFloat(String.valueOf(value));
        if (Double.class == clazz || double.class == clazz)
            return Double.parseDouble(String.valueOf(value));
        if (String.class == clazz)
            return String.valueOf(value);
        return value;
    }


    public static <T> T cast(Object value, Class<T> clz) {
        if (value == null)
            return null;
        try {
            return clz.cast(value);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cloneBean(Object source, Class<T> target, String... ignoreProperties) {
        T clone = null;
        List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;
        try {
            clone = (T) target.getInterfaces();
            if (source == null)
                return clone;
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(target);
            getPropertyDescriptor(source, clone, ignoreList, propertyDescriptors, ignoreProperties);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.fillInStackTrace();
        }
        return clone;
    }

    private static <T> void getPropertyDescriptor(Object source, T clone, List<String> ignoreList,
                                                  PropertyDescriptor[] propertyDescriptors, String[] ignoreProperties)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (propertyDescriptors == null) return;
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getWriteMethod() != null
                    && (ignoreProperties == null || (!ignoreList.contains(propertyDescriptor.getName())))) {
                if (PropertyUtils.isReadable(source, propertyDescriptor.getName())) {
                    Object propertyValue = PropertyUtils.getProperty(source, propertyDescriptor.getName());
                    if (PropertyUtils.isWriteable(clone, propertyDescriptor.getName()))
                        PropertyUtils.setProperty(clone, propertyDescriptor.getName(), propertyValue);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cloneBean(Class<T> target, Object source, String... withProperties) {
        T clone = null;
        List<String> ignoreList = (withProperties != null && withProperties.length > 0) ? Arrays.asList(withProperties) : null;
        try {
            clone = (T) target.getInterfaces();
            if (source == null)
                return clone;
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(target);
            if (propertyDescriptors != null) {
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (propertyDescriptor.getWriteMethod() != null
                            && (ignoreList == null || (ignoreList.contains(propertyDescriptor.getName())))) {
                        if (PropertyUtils.isReadable(source, propertyDescriptor.getName())) {
                            Object propertyValue = PropertyUtils.getProperty(source, propertyDescriptor.getName());
                            if (PropertyUtils.isWriteable(clone, propertyDescriptor.getName()))
                                PropertyUtils.setProperty(clone, propertyDescriptor.getName(), propertyValue);
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.fillInStackTrace();
        }
        return clone;
    }

    public static Object cloneBeanInstance(Object target, Object source, String... ignoreProperties) {
        if (target == null || source == null)
            throw new NullPointerException();
        List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;
        try {
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(target);
            getPropertyDescriptor(source, target, ignoreList, propertyDescriptors, ignoreProperties);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return target;
    }

    public static Object cloneBeanInstance(Object target, Object source, boolean excludeNull, String... ignoreProperties) {
        if (target == null || source == null)
            throw new NullPointerException();
        List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;
        try {
            PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(target);
            if (propertyDescriptors != null) {
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (propertyDescriptor.getWriteMethod() != null
                            && (ignoreProperties == null || (!ignoreList.contains(propertyDescriptor.getName())))) {
                        if (PropertyUtils.isReadable(source, propertyDescriptor.getName())) {
                            Object propertyValue = PropertyUtils.getProperty(source, propertyDescriptor.getName());
                            if (!excludeNull) {
                                if (PropertyUtils.isWriteable(target, propertyDescriptor.getName()))
                                    PropertyUtils.setProperty(target, propertyDescriptor.getName(), propertyValue);
                            } else {
                                if (propertyValue != null) {
                                    if (PropertyUtils.isWriteable(target, propertyDescriptor.getName()))
                                        PropertyUtils.setProperty(target, propertyDescriptor.getName(), propertyValue);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return target;
    }

    public static <E> Object handleNull(E obj, Class<E> clz) {
        if (obj != null)
            return obj;

        if (clz == Long.class || clz == long.class)
            return 0L;
        if (clz == Double.class || clz == double.class)
            return 0d;
        if (clz == BigDecimal.class)
            return BigDecimal.ZERO;
        if (clz == Integer.class || clz == int.class)
            return 0;
        if (clz == Float.class || clz == float.class)
            return 0f;
        if (clz == Short.class || clz == short.class)
            return 0;
        if (clz == Byte.class || clz == byte.class)
            return 0;
        if (clz == Boolean.class || clz == boolean.class)
            return false;
        if (clz == Character.class || clz == char.class)
            return ' ';
        if (clz == String.class)
            return "";
        throw new IllegalArgumentException("class not supported : " + clz.getName());
    }

    public static Class<?> wrapperToPrimitive(Class<?> clz) {
        if (clz == Long.class || clz == long.class)
            return Long.class;
        if (clz == Double.class || clz == double.class)
            return Double.class;
        if (clz == BigDecimal.class)
            return BigDecimal.class;
        if (clz == Integer.class || clz == int.class)
            return Integer.class;
        if (clz == Float.class || clz == float.class)
            return Float.class;
        if (clz == Short.class || clz == short.class)
            return Short.class;
        if (clz == Byte.class || clz == byte.class)
            return Byte.class;
        if (clz == Boolean.class || clz == boolean.class)
            return Boolean.class;
        if (clz == Character.class || clz == char.class)
            return Character.class;
        if (clz == String.class)
            return String.class;
        return null;
    }

    public static boolean isCollection(Object value) {
        return value instanceof Collection;
    }

    public static Class<?> getGenericMethodClassType(Class<?> clz, Method method) {
        Type type = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        if (type instanceof TypeVariable)
            return null;
        return (Class<?>) type;
    }
}
