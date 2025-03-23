package com.m2a.db.query;

import com.m2a.enums.Operator;
import com.m2a.util.CollectionUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WhereClause implements Iterable<WhereClause.QueryParam> {

    List<QueryParam> params;

    /**
     * apply between all query params
     */
    @Getter
    @Setter
    private Operator logicalOperand = Operator.AND;

    /**
     * between operand
     */
    @Getter
    @Setter
    private Operator logicalOperandClause = Operator.AND;

    /**
     * apply between around each where clause
     */
    @Getter
    @Setter
    private Operator groupOperand = Operator.NON_GROUP;

    public WhereClause(QueryParam... param) {
        this(Arrays.asList(param));
    }

    public WhereClause(List<QueryParam> parameters) {
        this.params = parameters;
    }

    public void add(QueryParam qp) {
        if (CollectionUtil.isEmpty(params))
            params = new ArrayList<>();
        params.add(qp);
    }

    @Override
    public Iterator<QueryParam> iterator() {
        return this.params.iterator();
    }

    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class QueryParam implements Serializable {
        private String columnExpression;
        private Operator operator;
        private Object object;
        private Function function;
    }
}
