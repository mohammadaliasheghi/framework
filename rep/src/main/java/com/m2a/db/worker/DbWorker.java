package com.m2a.db.worker;

import org.hibernate.jdbc.Work;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DbWorker implements Work {

    private final String query;

    public DbWorker(String query) {
        this.query = query;
    }

    @Override
    public void execute(Connection con) throws SQLException {
        exec(con, query);
    }

    public abstract void exec(Connection con, String q) throws SQLException;
}
