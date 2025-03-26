package com.m2a.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QueryWhereParser {

    private static Logger log = LoggerFactory.getLogger(QueryWhereParser.class);
    public static final QueryWhereParser EMPTY = new QueryWhereParser();
    public static final String AND = "AND";
    private String match = AND;
    private List<FilterProperty> filterProperties = new ArrayList<>();

    public static final String OPERATOR_EXPRESSION = "^\\$[a-z]{2}$";
    public static final String OPERATOR_EXPRESSION_3 = "^\\$[a-z]{3}$";

    public static final String GT = "$gt"; // greater than
    public static final String GTE = "$gte"; // greater than equal
    public static final String LT = "$lt"; // less than
    public static final String LTE = "$lte"; // less than equal
    public static final String LK = "$lk"; // like
    public static final String EQ = "$eq"; // equal
    public static final String NEQ = "$neq"; // not equal
    public static final String IN = "$in"; //
    public static final String BW = "$bw";// begins with
    public static final String EW = "$ew"; // ends with
    public static final String NE = "$ne"; // null
    public static final String NN = "$nn"; // not null

    public QueryWhereParser() {
    }

    public QueryWhereParser(String content) {
        buildParameters(content);
    }

    public void buildParameters(String content) {
        try {
            JsonObject jsonObject = new JsonObject().getAsJsonObject(content);
            parseJSON(jsonObject);
        } catch (Exception e) {
            log.error("could not parse query", e);
        }
    }

    private void parseFilter(String key, JsonArray jsonArray) throws JsonIOException {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object object = jsonArray.get(i);
            if (object instanceof JsonObject obj)
                parseFilter(key, obj, i);
        }
    }

    private void parseFilter(String key, JsonObject jsonObject, int index) {
        FilterProperty fp = new FilterProperty();
        Object op = jsonObject.keySet().toArray()[index];
        fp.setOperator(String.valueOf(op));
        Object objectValue = jsonObject.get(String.valueOf(op));
        fp.setValue(objectValue);
        fp.setPropertyName(key);
        filterProperties.add(fp);
    }

    private void parseJSON(JsonObject jsonObject) {
        try {
            int index = 0;
            for (String key : jsonObject.keySet()) {
                Object value = jsonObject.get(key);
                if (value instanceof JsonObject) {
                    parseFilter(key, (JsonObject) value, index++);
                } else if (value instanceof JsonArray) {
                    parseFilter(key, (JsonArray) value);
                }
            }
        } catch (Exception e) {
            log.error("could not parse query", e);
        }
    }

    @Getter
    @Setter
    public static class FilterProperty {
        String operator;
        String propertyName;
        Object value;
    }
}