package com.m2a.db.worker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.SQLException;

@Getter
@Setter
@RequiredArgsConstructor
public class DbScalarWork<T> implements Work {

    private String sql;
    private T result;
    private Object[] params;

    public DbScalarWork(String sql, Object[] params) {
        this.sql = sql;
        this.params = params;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        QueryRunner qr = new QueryRunner();
        ScalarHandler<T> handler = new ScalarHandler<>();
        result = qr.query(connection, sql, handler, params);
    }
}
