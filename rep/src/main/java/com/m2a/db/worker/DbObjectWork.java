package com.m2a.db.worker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
@Getter
@Setter
public class DbObjectWork<T> implements Work {

    private String sql;
    private Object[] params;
    private Class<T> type;
    private T instance;

    public DbObjectWork(String sql, Object[] params, Class<T> type) {
        this.sql = sql;
        this.params = params;
        this.type = type;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        QueryRunner qr = new QueryRunner();
        ResultSetHandler<T> handler = new BeanHandler<>(type);
        instance = qr.query(connection, sql, handler, params);
    }
}
