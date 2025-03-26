package com.m2a.db.query;

import com.m2a.db.query.GroupBy.GroupByProperty;
import com.m2a.db.query.Sort.Order;
import com.m2a.db.query.WhereClause.QueryParam;
import com.m2a.db.worker.DBUtil;
import com.m2a.enums.Direction;
import com.m2a.enums.Operator;
import com.m2a.reflections.ReflectionUtil;
import com.m2a.util.ArrayUtil;
import com.m2a.util.CollectionUtil;
import com.m2a.util.RegexUtil;
import com.m2a.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * this is a copy of Seam EntityQuery for native queries
 * </p>
 * <p>
 * <blockquote>
 *
 * <pre>
 *         QueryController qc = new QueryController();
 *         qc.setQuery();
 *         qc.addWhereClause();
 *         qc.execute();
 *         </pre>
 *
 * </blockquote>
 * <p>
 * <br />
 * steps to use this controller <br />
 * 1. set query (also available in constructor)<br />
 * 2. add where clause (OPTIONAL) <br />
 * 3. add order by (Optional) <br />
 * 4. add group by (Optional) <br />
 * 5. call createQuery method <br />
 * 6. call execute method <br />
 */
@Getter
@Setter
public class QueryController {

    private static final Logger logger = Logger.getLogger(QueryController.class.getName());
    private final static Pattern pattern =
            Pattern.compile("from(.+?)(LEFT|RIGHT|INNER)", Pattern.CASE_INSENSITIVE);
    private final static Pattern FROM_PATTERN =
            Pattern.compile("(from)\\s(.*)", Pattern.CASE_INSENSITIVE);
    private static final String SPACE = " ";

    private String query;

    private StringBuffer queryBuffer;

    private StringBuffer countQueryBuffer;

    private List<WhereClause> whereClauseList;

    private List<String> queryAppender;

    private Sort sort;

    private GroupBy groupBy;

    private Root root;

    private Integer firstResult;

    private Integer maxResults;

    public Integer pageNumber;

    private List<?> resultList;

    private Long resultCount;

    private String orderColumn;

    private String orderDirection;

    private SortDecorator sortDecorator;

    private final DBUtil dbUtil;

    public QueryController(DBUtil dbUtil) {
        if (dbUtil == null)
            throw new IllegalArgumentException("db util can not be null");
        this.dbUtil = dbUtil;
    }

    /**
     * values inject into query directly
     */
    private Object[] queryParamValues;

    private static final int PAGE_INDEX = 10;

    public Integer getStartRange() {
        int startRange = 0;
        if (getPageNumber() != null)
            startRange = (int) getPageNumber() / PAGE_INDEX;
        return (PAGE_INDEX * startRange) + 1;
    }

    public Integer getEndRange() {
        int endRange = getStartRange() + PAGE_INDEX;
        if (getPageCount() != null && endRange >= getPageCount())
            return getPageCount();
        return endRange;
    }

    /**
     * all sql values inject into queries
     */
    private List<Object> values = new ArrayList<Object>();

    public void setQueryParamValues(Object... queryParamValues) {
        this.queryParamValues = queryParamValues;
    }

    public void addWhereClause(WhereClause whereClause) {
        if (CollectionUtil.isEmpty(whereClauseList))
            whereClauseList = new ArrayList<>();
        if (whereClause != null && CollectionUtil.isNotEmpty(whereClause.params))
            this.whereClauseList.add(whereClause);
    }

    /**
     * this method is used for appending dynamic query in addRestriction method
     */
    public void appendQuery(String joinQuery) {
        if (this.query == null) {
            throw new IllegalArgumentException("You have to set query first");
        }
        if (queryAppender == null)
            queryAppender = new ArrayList<String>();
        queryAppender.add(joinQuery);
    }

    public Query createQuery() {
        if (this.query == null) {
            throw new IllegalArgumentException("You have to provide query");
        }
        //
        queryBuffer = new StringBuffer(this.query.replaceAll("[\\r|\\t\\n]", " "));
        // where clause is not null means it specifies outside of restrictions
        // method so don't add restrictions
        if (whereClauseList == null) {
            whereClauseList = new ArrayList<WhereClause>();
            addRestrictions();
        }

        applyAppender(queryBuffer);

        applyWhere(queryBuffer);
        // Group
        if (groupBy != null)
            applyGroupBy(queryBuffer);
        // Sort
        applyOrderBy();
        //
        if (dbUtil.isDatabaseOracle()) {
            StringBuilder oracleQ = new StringBuilder();
            oracleQ.append(" SELECT *   FROM (SELECT a.*, rownum rn  FROM ( ");
            oracleQ.append(queryBuffer.toString());
            if (getMaxResults() != null) {
                oracleQ.append(" WHERE rownum <= ").append(getMaxResults() + 1).append("  ) a  ) ");
            } else {
                oracleQ.append(" a  ) ");
            }
            if (getFirstResult() != null) {
                if (getFirstResult() > 0) {
                    oracleQ.append(" WHERE rn >= ").append(getFirstResult());
                }
            }
            return new Query(oracleQ.toString(), values);
        } else if (dbUtil.isDatabaseMySQL()) {
            //Mysql            
            if (getMaxResults() != null) {
                if (getFirstResult() != null) {
                    if (getFirstResult() > 0)
                        queryBuffer.append(" limit ").append(getFirstResult() - 1).append(",").append(getMaxResults() + 1);
                } else {
                    queryBuffer.append(" limit ").append(getMaxResults() + 1);
                }
            }

            return new Query(queryBuffer.toString(), values);
        } else {
            // not oracle
            if (getMaxResults() != null)
                queryBuffer.append(" limit ").append(getMaxResults() + 1);

            if (getFirstResult() != null) {
                if (getFirstResult() > 0)
                    queryBuffer.append(" offset ").append(getFirstResult());
            }
            return new Query(queryBuffer.toString(), values);
        }

    }

    private void applyAppender(StringBuffer qb) {
        if (CollectionUtil.isNotEmpty(queryAppender)) {
            for (String q : queryAppender) {
                qb.append(q);
            }
        }
    }

    public <E> List<E> truncResultList(List<E> results) {
        Integer mr = getMaxResults();
        if (mr != null && results.size() > mr) {
            return results.subList(0, mr);
        } else {
            return results;
        }
    }

    private void applyGroupBy(StringBuffer sb) {
        if (!RegexUtil.find(sb.toString(), "Group By"))
            sb.append(" Group By ");
        int i = 0;
        for (GroupByProperty prop : groupBy) {
            if (i > 0)
                sb.append(", ");
            if (prop.getPropertyName().contains("."))
                sb.append(prop.getPropertyName());
            else
                sb.append(this.root.getAlias()).append(".").append(prop.getPropertyName());
            i++;
        }
    }

    /**
     * <p>
     * sort first priority is based on order column from query parameter second.
     * it's based on cookie third. it's based on default sort api
     * </p>
     */
    private void applyOrderBy() {
        StringBuffer orderWrapper = new StringBuffer().append("select * from ( ");
        orderWrapper.append(queryBuffer).append(" ) ").append(root.getAlias());
        String sortExpression = "";
        if (getSortDecorator() != null) {
            sortExpression = getSortDecorator().sortExpression();
        }
        if (sort != null || StringUtil.isNotEmpty(getOrderColumn()) || StringUtil.isNotEmpty(sortExpression)) {
            orderWrapper.append(" Order By ");
        }
        if (StringUtil.isNotEmpty(getOrderColumn())) {
            // first priority
            orderWrapper.append(getOrderColumn()).append(" ").append(getOrderDirection() == null ? "desc" : getOrderDirection());
        } else {
            if (StringUtil.isNotEmpty(sortExpression)) {
                orderWrapper.append(" ").append(sortExpression);
            } else {
                if (sort != null) {
                    int i = 0;
                    for (Order order : sort) {
                        String orderProperty = order.getProperty();
                        if (i > 0)
                            orderWrapper.append(", ");
                        Direction direction = order.getDirection();
                        if (direction.equals(Direction.QUERY)) {
                            // put all query in order by
                            orderWrapper.append(" ").append(orderProperty);
                        } else {
                            if (orderProperty.contains("."))
                                orderWrapper.append(orderProperty).append(" ").append(direction.getLabel());
                            else
                                orderWrapper.append(this.root.getAlias()).append(".").append(orderProperty).append(" ")
                                        .append(direction.getLabel());
                        }
                        i++;
                    }
                }
            }
        }
        this.queryBuffer = orderWrapper;
    }

    public interface SortDecorator {
        String sortExpression();
    }

    private void applyWhere(StringBuffer queryBuffer) {
        values = new ArrayList<>(); // must reset values
        int wcCount = 0;
        queryBuffer.append(" ");
        for (WhereClause wc : whereClauseList) {
            parseWhereClause(wc, queryBuffer, wcCount);
            wcCount++;
        }
        queryBuffer.append(" ");
    }

    private void parseWhereClause(WhereClause wc, StringBuffer mainQuery, int clauseCount) {
        StringBuilder whereStringBuffer = new StringBuilder();
        int queryCount = 0;
        boolean useOperandGrouping = false;
        Operator logicalOperand = wc.getLogicalOperand();
        for (QueryParam qp : wc) {
            Object value = qp.getObject(); // can be comma separated seam
            // expression
            Operator paramOperator = qp.getOperator();
            if (!(Operator.IS_NULL.equals(paramOperator)
                    || Operator.NOT_NULL.equals(paramOperator))) {
                if (shouldIgnoreClause(value))
                    continue;
            }
            //
            useOperandGrouping = true;
            //
            String columnExpression = qp.getColumnExpression();
            if (!columnExpression.contains("."))
                columnExpression = root.getAlias() + "." + columnExpression;

            Function function = qp.getFunction();
            if (function != null)
                columnExpression = function.parseColumn(columnExpression);

            value = convertToSqlDate(value);

            if (queryCount > 0)
                whereStringBuffer.append(SPACE).append(logicalOperand.name()).append(SPACE);
            queryCount++;

            if (Operator.QUERY.equals(paramOperator)) {
                whereStringBuffer.append(columnExpression);
                parseValue(value, paramOperator, function);
            } else {
                whereStringBuffer.append(columnExpression);
                if (Operator.EQUAL.equals(paramOperator))
                    whereStringBuffer.append(" = ?");
                if (Operator.NOT_EQUAL.equals(paramOperator))
                    whereStringBuffer.append(" <> ?");
                if (Operator.GT.equals(paramOperator))
                    whereStringBuffer.append(" > ?");
                if (Operator.GTE.equals(paramOperator))
                    whereStringBuffer.append(" >= ?");
                if (Operator.LT.equals(paramOperator))
                    whereStringBuffer.append(" < ?");
                if (Operator.LTE.equals(paramOperator))
                    whereStringBuffer.append(" <= ?");
                if (Operator.LIKE.equals(paramOperator) ||
                        Operator.BEGIN_WITH.equals(paramOperator)
                        || Operator.END_WITH.equals(paramOperator)) {
                    whereStringBuffer.append(" LIKE ? ");
                }
                if (Operator.IS_NULL.equals(paramOperator)) {
                    whereStringBuffer.append(" is null ");
                    continue;
                }
                if (Operator.NOT_NULL.equals(paramOperator)) {
                    whereStringBuffer.append(" is not null ");
                    continue;
                }

                if (Operator.IN.equals(paramOperator) || Operator.NOT_IN.equals(paramOperator)) {
                    value = In(value);
                    if (Operator.IN.equals(paramOperator))
                        whereStringBuffer.append(" IN( ");
                    if (Operator.NOT_IN.equals(paramOperator))
                        whereStringBuffer.append(" NOT IN( ");
                    whereStringBuffer.append(value);
                    whereStringBuffer.append(" ) ");
                    continue;
                }
                parseValue(value, paramOperator, function);
            }
        }
        if (useOperandGrouping) {
            if (RegexUtil.find(mainQuery.toString(), "where")) {
                mainQuery.append(" AND ");
            }
            if (queryBuffer != null && !RegexUtil.find(queryBuffer.toString(), "where")) {
                mainQuery.append(" WHERE ");
            }
            if (countQueryBuffer != null && !RegexUtil.find(countQueryBuffer.toString(), "where")) {
                mainQuery.append(" WHERE ");
            }
        }
        if (useOperandGrouping) {
            StringBuilder groupBuffer = new StringBuilder();
            if (clauseCount > 0)
                groupBuffer.append(SPACE).append(wc.getLogicalOperandClause().name()).append(SPACE);
            Operator groupOperand = wc.getGroupOperand();
            if (groupOperand.equals(Operator.GROUP))
                groupBuffer.append(" ( ");
            groupBuffer.append(whereStringBuffer.toString());
            if (groupOperand.equals(Operator.GROUP))
                groupBuffer.append(" ) ");
            mainQuery.append(groupBuffer);
        }
    }

    private Object convertToSqlDate(Object value) {
        if (value instanceof Date d)
            // jdbc uses sql date (convert java util date to sql date
            value = new java.sql.Date(d.getTime());
        return value;
    }

    private boolean shouldIgnoreClause(Object value) {
        if (value == null)
            return true;
        if (value instanceof String)
            return String.valueOf(value).trim().isEmpty();
        return false;
    }

    /**
     * @param value comma separated String/Int or List of String/Int
     */
    private String In(Object value) {
        StringBuilder sb = new StringBuilder();
        if (value.getClass().isArray()) {
            Object[] inObject = (Object[]) value;
            for (int j = 0; j < inObject.length; j++) {
                Object objectVal = inObject[j];
                if (j > 0)
                    sb.append(", ");
                if (objectVal instanceof String)
                    sb.append("'").append(objectVal).append("'");
                else
                    sb.append(objectVal);
            }
        }
        if (ReflectionUtil.isSubclass(value.getClass(), Collection.class)) {
            assert value instanceof List<?>;
            List<?> inObject = (List<?>) value;
            for (int j = 0; j < inObject.size(); j++) {
                Object objectVal = inObject.get(j);
                if (j > 0)
                    sb.append(", ");
                if (objectVal instanceof String)
                    sb.append("'").append(objectVal).append("'");
                else
                    sb.append(objectVal);
            }
        }
        if (value instanceof String inValue) {
            if (isSeamExpression(inValue))
                throw new UnsupportedOperationException("in values can not be Seam Expression");
            sb.append(inValue);
        }
        return sb.toString();
    }

    /**
     * used only for query operator
     */
    private void parseValue(Object val, Operator paramOperator, Function function) {

        if (val instanceof String valueExpression) {
            if (valueExpression.contains(",")) {
                // more than one parameter value
                String[] valueItem = valueExpression.split(",");
                for (String item : valueItem) {

                    parseExpression(item, paramOperator, function);
                }
            } else {
                parseExpression(valueExpression, paramOperator, function);
            }

        } else {
            // is not string
            if (val != null) {
                if (function != null)
                    val = function.parseColumnValue(val);
                val = convertToSqlDate(val);
                values.add(val);
            }
        }
    }

    private void parseExpression(String valueExpression, Operator paramOperator, Function function) {
        // is seam expression
        Object evalValue = null;
        if (StringUtil.isNotEmpty(valueExpression)) {
            evalValue = valueExpression;
        }

        if (evalValue != null) {
            if (Operator.LIKE.equals(paramOperator)) {
                evalValue = "%" + evalValue + "%";
            }
            if (Operator.BEGIN_WITH.equals(paramOperator)) {
                evalValue = evalValue + "%";
            }
            if (Operator.END_WITH.equals(paramOperator)) {
                evalValue = "%" + evalValue;
            }
            if (function != null)
                evalValue = function.parseColumnValue(evalValue);
            evalValue = convertToSqlDate(evalValue);
            values.add(evalValue);
        }
    }

    private boolean isSeamExpression(Object valueExpression) {
        if (valueExpression instanceof String ve)
            return ve.startsWith("#{") && ve.endsWith("}");
        return false;
    }

    protected Query createCountQuery() {
        if (StringUtil.isEmpty(this.query))
            throw new IllegalArgumentException("query is not set");
        countQueryBuffer = new StringBuffer()
                .append(this.query.replaceAll("[\\r|\\t\\n]", " "));

        if (whereClauseList == null) {
            whereClauseList = new ArrayList<>();
            addRestrictions();
        }
        applyAppender(countQueryBuffer);
        applyWhere(countQueryBuffer);

        //
        StringBuffer sb = new StringBuffer().append("select count(*) from ( ");
        sb.append(countQueryBuffer.toString());
        sb.append(" ) ").append(root.getAlias());
        //

        if (groupBy != null)
            applyGroupBy(sb);
        return new Query(sb.toString(), values);
    }

    @Getter
    @Setter
    private static class Root {
        private String alias;

        public Root(String alias) {
            if (StringUtil.isEmpty(alias))
                throw new IllegalArgumentException("query is not valid");
            alias = alias.replaceAll("[\\r|\\t\\n]", " ");
            final Matcher matcher = pattern.matcher(alias);
            String[] split = null;
            if (matcher.find())
                split = matcher.group(1).split("\\s");
            else
                split = alias.split("\\s");
            this.alias = split[split.length - 1];
            logger.info(this.alias);
        }
    }

    private Object[] bindParameters(Object[] params, List<Object> paramValues) {
        if (params != null && CollectionUtil.isNotEmpty(paramValues)) {
            return ArrayUtil.addAll(params, paramValues.toArray(new Object[0]));
        }
        if (params != null) {
            return params;
        }
        if (CollectionUtil.isNotEmpty(paramValues))
            return paramValues.toArray(new Object[0]);
        return null;
    }

    /**
     * params take precedence over query where values
     */
    public List<Map<String, Object>> execute(Query queryRunner) {
        String queryToExecute = queryRunner.getQueryToExecute();
        List<Object> paramValues = queryRunner.getVals();
        boolean hasQueryParams = CollectionUtil.isNotEmpty(paramValues)
                || this.queryParamValues != null;

        if (!hasQueryParams) {
            List<Map<String, Object>> list = dbUtil.executeQuery(queryToExecute);
            resultList = list;
            return list;
        }
        //
        Object[] parameters = bindParameters(this.queryParamValues, paramValues);
        List<Map<String, Object>> list = dbUtil.executeQuery(queryToExecute, parameters);

        resultList = list;
        return list;
    }

    public <T> T executeForObject(Class<T> clz, Query queryRunner) {
        List<Object> paramValues = queryRunner.getVals();
        boolean hasQueryParams = CollectionUtil.isNotEmpty(paramValues)
                || this.queryParamValues != null;
        if (!hasQueryParams)
            return dbUtil.executeQuery(clz, queryRunner.getQueryToExecute());
        Object[] parameters = bindParameters(this.queryParamValues, paramValues);
        return dbUtil.executeQuery(clz, queryRunner.getQueryToExecute(), parameters);
    }

    public <E> List<E> executeForList(Class<E> clz, Query queryRunner) {
        List<Object> paramValues = queryRunner.getVals();
        boolean hasQueryParams = CollectionUtil.isNotEmpty(paramValues) || this.queryParamValues != null;
        if (!hasQueryParams) {
            List<E> queryList = dbUtil.executeQueryList(clz, queryRunner.getQueryToExecute());
            resultList = queryList;
            return queryList;
        }
        Object[] parameters = bindParameters(this.queryParamValues, paramValues);
        List<E> queryList = dbUtil.executeQueryList(clz, queryRunner.getQueryToExecute(), parameters);
        resultList = queryList;
        return queryList;
    }

    public void addOrderBy(Sort sort) {
        this.sort = sort;
    }

    public void addGroupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Query implements Serializable {
        private String queryToExecute;
        private List<Object> vals;

        public Query(String queryToExecute, List<Object> vals) {
            this.queryToExecute = queryToExecute;
            this.vals = vals;
        }
    }

    public Integer getFirstResult() {
        if (firstResult != null)
            return firstResult;
        if (pageNumber != null && getPageCount() != null)
            return getPageNumber() * getMaxResults();
        return firstResult;
    }

    public Integer getPageCount() {
        if (getMaxResults() == null) {
            return null;
        } else {
            int rc = getResultCount().intValue();
            int mr = getMaxResults();
            int pages = rc / mr;
            return rc % mr == 0 ? pages : pages + 1;
        }
    }

    public Long getResultCount() {
        Query countQuery = createCountQuery();
        if (isAnyParameterDirty()) {
            refresh();
        }
        if (resultCount != null)
            return resultCount;
        List<Object> paramValues = countQuery.getVals();
        boolean hasParam = CollectionUtil.isNotEmpty(paramValues) || this.queryParamValues != null;
        Long count = null;
        if (hasParam) {
            Object[] parameters = bindParameters(this.queryParamValues, paramValues);
            Number scalar = dbUtil.executeScalar(countQuery.getQueryToExecute(), Number.class, parameters);
            count = scalar == null ? 0 : scalar.longValue();
        } else {
            Number scalar = dbUtil.executeScalar(countQuery.getQueryToExecute(), Number.class).longValue();
            count = scalar.longValue();
        }
        resultCount = count;
        return resultCount;
    }

    protected boolean isAnyParameterDirty() {
        return false;
    }

    public Integer getPageNumber() {
        if (pageNumber == null || pageNumber < 0)
            pageNumber = 0;
        return pageNumber;
    }

    public boolean isPreviousExists() {
        return (getFirstResult() != null && getFirstResult() != 0)
                && CollectionUtil.isNotEmpty(resultList);
    }

    public boolean isNextExists() {
        return resultList != null && getMaxResults() != null
                && resultList.size() > getMaxResults();
    }

    public int getNextFirstResult() {
        Integer fr = getFirstResult();
        return (fr == null ? 0 : fr) + getMaxResults();
    }

    /**
     * Get the index of the first result of the previous page
     */
    public int getPreviousFirstResult() {
        Integer fr = getFirstResult();
        Integer mr = getMaxResults();
        return mr >= (fr == null ? 0 : fr) ? 0 : (fr == null ? 0 : fr) - mr;
    }

    public Long getLastFirstResult() {
        Integer pc = getPageCount();
        return pc == null ? null : (pc.longValue() - 1) * getMaxResults();
    }

    protected Operator operandValueOf(String op) {
        return StringUtil.isNotEmpty(op) ? Operator.valueOf(op) : Operator.AND;
    }

    protected void reset() {
        setMaxResults(null);
        setFirstResult(null);
        addGroupBy(null);
        addOrderBy(null);
        this.queryParamValues = null;
        this.pageNumber = 0;
        this.whereClauseList = null;
        this.queryAppender = null;
    }

    protected void refresh() {
        resultCount = null;
        resultList = null;
    }

    protected void addRestrictions() {
    }
}
