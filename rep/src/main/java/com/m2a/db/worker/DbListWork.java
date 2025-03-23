package com.m2a.db.worker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class DbListWork<T> implements Work {

    private String sql;
    private Object[] params;
    private Class<T> type;
    private List<T> instance;

    public DbListWork(String sql, Object[] params, Class<T> type) {
        this.sql = sql;
        this.params = params;
        this.type = type;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        QueryRunner qr = new QueryRunner();
        ResultSetHandler<List<T>> handler = new BeanListHandler<>(type);
        instance = qr.query(connection, sql, handler, params);
    }
}
