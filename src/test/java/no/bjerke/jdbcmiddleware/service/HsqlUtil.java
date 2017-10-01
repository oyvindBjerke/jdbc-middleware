package no.bjerke.jdbcmiddleware.service;

import org.hsqldb.jdbc.JDBCDataSource;

import java.sql.SQLException;
import java.sql.Statement;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public class HsqlUtil {

    public static JDBCDataSource setupDataSource() throws SQLException {
        final JDBCDataSource jdbcDataSource = new JDBCDataSource();
        jdbcDataSource.setDatabase("jdbc:hsqldb:mem:db");
        jdbcDataSource.setUrl("sa");
        Statement statement = jdbcDataSource.getConnection().createStatement();
        statement.addBatch("DROP SCHEMA public CASCADE");
        statement.executeBatch();
        return jdbcDataSource;
    }

    public static void createCustomerTable(JDBCDataSource dataSource) throws SQLException {
        Statement statement = dataSource.getConnection().createStatement();
        statement.addBatch("CREATE TABLE customer (id INTEGER PRIMARY KEY, name VARCHAR(128) NOT NULL);");
        statement.addBatch("INSERT INTO customer (id, name) VALUES (1, 'Jon Snow');");
        statement.executeBatch();
    }

}
