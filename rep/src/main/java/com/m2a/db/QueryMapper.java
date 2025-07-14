package com.m2a.db;

import com.m2a.db.query.Sort;
import com.m2a.enums.Operator;
import com.m2a.reflections.Property;
import com.m2a.reflections.ReflectionUtil;
import com.m2a.rest.QueryWhereParser;
import com.m2a.util.CollectionUtil;
import com.m2a.util.DateUtil;
import com.m2a.util.StringUtil;
import com.m2a.web.ParameterItem;
import com.m2a.web.QParam;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.Filter;

import java.beans.PropertyDescriptor;
import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class QueryMapper {

    public static <E> E toObject(List<Map<String, Object>> queryResult, Class<E> clz) {
        List<E> list = toList(queryResult, clz);
        if (CollectionUtil.isNotEmpty(list))
            return list.getFirst();
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E> List<E> toList(List<Map<String, Object>> queryResult, Class<E> clz) {
        List<E> instanceList = new ArrayList<E>();
        try {
            if (queryResult != null) {
                List<Property> properties = getProperties(clz);
                Map<String, String> maps = new HashMap<String, String>();
                if (clz.isAnnotationPresent(AttributeOverrides.class))
                    findRealColumnWithAttributeOverrides(clz, maps);
                if (clz.isAnnotationPresent(AttributeOverride.class)) {
                    AttributeOverride annotation = clz.getAnnotation(AttributeOverride.class);
                    findRealColumnWithAttributeOverride(annotation, maps);
                }
                for (Map<String, Object> dbRecord : queryResult) {

                    E instance = clz.getDeclaredConstructor().newInstance();
                    for (Property prop : properties) {
                        String columnName = prop.getColumnName();
                        if (!maps.isEmpty()) {
                            // do we need to check for every iteration ?
                            columnName = maps.get(columnName) == null ? columnName : maps.get(columnName);
                        }
                        Object recordValue = dbRecord.get(columnName);
                        Field field = ReflectionUtil.getField(clz, prop.getFieldName());
                        if (field != null && recordValue != null) {
                            field.setAccessible(true);
                            if (ReflectionUtil.isPrimitive(field.getType()) || ReflectionUtil.isWrapper(field.getType())) {
                                Object val = ReflectionUtil.toObject(field.getType(), recordValue);
                                ReflectionUtil.set(field, instance, val);
                            } else if (field.getType().isEnum()) {
                                Enum<?> enVal = Enum.valueOf((Class<Enum>) field.getType(), String.valueOf(recordValue));
                                ReflectionUtil.set(field, instance, enVal);
                            } else {
                                Object entityInstance = field.getType().getDeclaredConstructor().newInstance();
                                if (entityInstance instanceof EntityModel<?> po) {
                                    Method[] m = po.getClass().getMethods();
                                    for (Method method : m) {
                                        if (method.getName().equals("setId")) {
                                            Class<?> c = method.getParameterTypes()[0];
                                            method.invoke(po, ReflectionUtil.toObject(c, recordValue));
                                        }
                                    }
                                    ReflectionUtil.set(field, instance, entityInstance);
                                }
                            }
                        }
                    }
                    instanceList.add(instance);
                }
                return instanceList;
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return instanceList;
    }

    private static void findRealColumnWithAttributeOverride(AttributeOverride attributeOverride, Map<String, String> maps) {
        String attributeName = attributeOverride.name();
        Column column = attributeOverride.column();
        maps.put(attributeName, column.name());
    }

    private static void findRealColumnWithAttributeOverrides(Class<?> clz, Map<String, String> maps) {
        if (clz.isAnnotationPresent(AttributeOverrides.class)) {
            AttributeOverrides annotation = clz.getAnnotation(AttributeOverrides.class);
            AttributeOverride[] value = annotation.value();
            for (AttributeOverride attributeOverride : value)
                findRealColumnWithAttributeOverride(attributeOverride, maps);
        }
    }

    public static List<Property> getProperties(Class<?> clz) {
        List<Property> props = new ArrayList<>();
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(clz);
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod != null && !readMethod.isAnnotationPresent(Transient.class)) {
                Column annotation = readMethod.getAnnotation(Column.class);
                if (annotation != null) {
                    String dbColumnName = annotation.name();
                    props.add(new Property(dbColumnName, propertyDescriptor.getName()));
                }
                JoinColumn joinAnnotation = readMethod.getAnnotation(JoinColumn.class);
                if (joinAnnotation != null) {
                    String dbColumnName = joinAnnotation.name();
                    props.add(new Property(dbColumnName, propertyDescriptor.getName()));
                }
            }
        }
        if (props.isEmpty()) {
            //check for field
            List<Field> fields = ReflectionUtil.getFields(clz);
            for (Field field : fields) {
                Column annotation = field.getAnnotation(Column.class);
                if (annotation != null) {
                    String dbColumnName = annotation.name();
                    props.add(new Property(dbColumnName, field.getName()));
                }
                JoinColumn joinAnnotation = field.getAnnotation(JoinColumn.class);
                if (joinAnnotation != null) {
                    String dbColumnName = joinAnnotation.name();
                    props.add(new Property(dbColumnName, field.getName()));
                }
            }
        }
        return props;
    }

    public static String sortQueryParams(Sort sort) {
        if (sort == null)
            return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Sort.Order order : sort) {
            if (!first)
                sb.append("&");
            sb.append("sort=")
                    .append(order.getProperty())
                    .append(",")
                    .append(order.getDirection().name().toLowerCase());
            first = false;
        }
        return sb.toString();
    }

    public static String queryParams(Filter filter) {
        Class<? extends Filter> clz = filter.getClass();
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(clz);
        StringBuilder sb = new StringBuilder();
        for (PropertyDescriptor pd : pds) {
            if (pd.getPropertyType().equals(Class.class))
                continue;
            try {
                Method getter = pd.getReadMethod();
                if (getter != null) {
                    Object val = pd.getReadMethod().invoke(filter, (Object) null);
                    String propertyName = pd.getName();
                    if (val == null)
                        continue;
                    if (val instanceof String && StringUtil.isEmpty(String.valueOf(val))) {
                        continue;
                    }
                    sb.append("&");
                    sb.append(propertyName).append("=").append(val);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.fillInStackTrace();
            }
        }
        if (StringUtil.isEmpty(sb.toString()))
            return null;
        return sb.toString();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<Predicate<?>> filterMap(List<QueryWhereParser.FilterProperty> filterProperties, CriteriaBuilder cb, CriteriaQuery criteria, Root root, Class<?> clz) {
        List<Predicate<?>> predicateList = new ArrayList<>();
        for (QueryWhereParser.FilterProperty item : filterProperties) {
            String property = item.getPropertyName();
            Object value = item.getValue();
            String operator = item.getOperator();
            if (value == null)
                continue;
            if (value instanceof String && StringUtil.isEmpty(String.valueOf(value))) {
                continue;
            }
            if (property.indexOf(".") > 0) {
                System.out.println();
            } else {
                Field field = ReflectionUtil.getField(clz, property);
                if (field == null) return Collections.emptyList();
                if (QueryWhereParser.EQ.equals(operator)) {
                    if (field.getType().isEnum()) {
                        Enum<?> valueOf = Enum.valueOf((Class<Enum>) field.getType(), String.valueOf(value));
                        predicateList.add((Predicate<?>) cb.equal(root.get(property), valueOf));
                    } else {
                        predicateList.add((Predicate<?>) cb.equal(root.get(property), value));
                    }
                } else if (QueryWhereParser.NEQ.equals(operator)) {
                    if (field.getType().isEnum()) {
                        Enum<?> valueOf = Enum.valueOf((Class<Enum>) field.getType(), String.valueOf(value));
                        predicateList.add((Predicate<?>) cb.not(cb.equal(root.get(property), valueOf)));
                    } else {
                        predicateList.add((Predicate<?>) cb.not(cb.equal(root.get(property), value)));
                    }
                } else if (QueryWhereParser.LK.equals(operator))
                    predicateList.add((Predicate<?>) cb.like(root.<String>get(property), "%" + value + "%"));
                else if (QueryWhereParser.BW.equals(operator))
                    predicateList.add((Predicate<?>) cb.like(root.<String>get(property), value + "%"));
                else if (QueryWhereParser.EW.equals(operator))
                    predicateList.add((Predicate<?>) cb.like(root.<String>get(property), "%" + value));
                else if (QueryWhereParser.GT.equals(operator)) {
                    if (Date.class.isAssignableFrom(field.getType())) {
                        predicateList.add((Predicate<?>) cb.greaterThan(root.<Date>get(property), DateUtil.toDate(value)));
                    } else if (Number.class.isAssignableFrom(field.getType())) {
                        String v = String.valueOf(value);
                        predicateList.add((Predicate<?>) cb.greaterThan(root.get(property), Long.parseLong(v)));
                    }
                } else if (QueryWhereParser.GTE.equals(operator)) {
                    if (Date.class.isAssignableFrom(field.getType())) {

                        predicateList.add((Predicate<?>) cb.greaterThanOrEqualTo(root.<Date>get(property), DateUtil.toDate(value)));
                    } else if (Number.class.isAssignableFrom(field.getType())) {
                        String v = String.valueOf(value);
                        predicateList.add((Predicate<?>) cb.greaterThanOrEqualTo(root.<Long>get(property), Long.parseLong(v)));
                    }
                } else if (QueryWhereParser.LT.equals(operator)) {
                    if (Date.class.isAssignableFrom(field.getType())) {

                        predicateList.add((Predicate<?>) cb.lessThan(root.<Date>get(property), DateUtil.toDate(value)));
                    } else if (Number.class.isAssignableFrom(field.getType())) {
                        String v = String.valueOf(value);
                        predicateList.add((Predicate<?>) cb.lessThan(root.get(property), Long.parseLong(v)));
                    }
                } else if (QueryWhereParser.LTE.equals(operator)) {
                    if (Date.class.isAssignableFrom(field.getType())) {

                        predicateList.add((Predicate<?>) cb.lessThanOrEqualTo(root.<Date>get(property), DateUtil.toDate(value)));
                    } else if (Number.class.isAssignableFrom(field.getType())) {
                        String v = String.valueOf(value);
                        predicateList.add((Predicate<?>) cb.lessThanOrEqualTo(root.get(property), Long.parseLong(v)));
                    }
                } else if (QueryWhereParser.IN.equals(operator) && value instanceof List) {
                    predicateList.add((Predicate<?>) cb.in(root.get(property)).value(value));
                }
            }
        }
        return predicateList;
    }

    public static List<Predicate<?>> filterMap(Filter filter, CriteriaBuilder cb, CriteriaQuery<?> criteria, Root<?> root) {
        List<ParameterItem> filterItems = new ArrayList<>();
        Class<? extends Filter> clz = filter.getClass();
        PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(clz);
        for (PropertyDescriptor pd : pds) {
            if (pd.getPropertyType().equals(Class.class))
                continue;
            try {
                Method getter = pd.getReadMethod();
                if (getter != null && getter.isAnnotationPresent(QParam.class)) {
                    QParam annotation = getter.getAnnotation(QParam.class);
                    Object invoke = pd.getReadMethod().invoke(filter, (Object) null);
                    String propertyName = pd.getName();
                    if (StringUtil.isNotEmpty(annotation.propertyName()))
                        propertyName = annotation.propertyName();
                    filterItems.add(new ParameterItem(propertyName, annotation.operator(), invoke));
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.fillInStackTrace();
            }
        }
        //todo must be check val must not empty
        List<Predicate<?>> predicateList = new ArrayList<>();
        for (ParameterItem item : filterItems) {
            Object val = item.getValue();
            switch (val) {
                case null -> {
                    continue;
                }
                case Date date -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.set(Calendar.HOUR, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    val = cal.getTime();
                }
                default -> {
                }
            }
            if (Operator.LIKE.equals(item.getOperator())) {
                predicateList.add((Predicate<?>) cb.like(root.get(item.getPropertyName()), "%" + val + "%"));
            }
            if (Operator.EQUAL.equals(item.getOperator())) {
                predicateList.add((Predicate<?>) cb.equal(root.get(item.getPropertyName()), val));
            }
            if (Operator.NOT_EQUAL.equals(item.getOperator())) {
                predicateList.add((Predicate<?>) cb.not(cb.equal(root.get(item.getPropertyName()), val)));
            }
            if (Operator.GT.equals(item.getOperator())) {
                assert val instanceof Long;
                predicateList.add((Predicate<?>) cb.greaterThan(root.get(item.getPropertyName()), (Long) val));
            }
            if (Operator.LT.equals(item.getOperator())) {
                assert val instanceof Long;
                predicateList.add((Predicate<?>) cb.lessThan(root.get(item.getPropertyName()), (Long) val));
            }
            if (Operator.LTE.equals(item.getOperator())) {
                assert val instanceof Long;
                predicateList.add((Predicate<?>) cb.lessThanOrEqualTo(root.get(item.getPropertyName()), (Long) val));
            }
            if (Operator.GTE.equals(item.getOperator())) {
                assert val instanceof Long;
                predicateList.add((Predicate<?>) cb.greaterThanOrEqualTo(root.get(item.getPropertyName()), (Long) val));
            }
            if (Operator.SQL_QUERY.equals(item.getOperator())) {
                throw new UnsupportedOperationException("not supported in JPA");
            }
        }
        return predicateList;
    }
}
