package no.obje.jdbcmiddleware.service;

import no.obje.jdbcmiddleware.domain.ConnectionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);

    private final DataSource dataSource;
    private ThreadLocal<Connection> transactionConnection = new ThreadLocal<>();

    public ConnectionManager(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public <R> R doWithConnection(ConnectionCallback<R> callback) {
        if(transactionConnection.get() != null) {
            return callback.run(transactionConnection.get());
        }
        try(Connection connection = dataSource.getConnection()) {
            return callback.run(connection);
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void startTransaction() {
        LOGGER.debug("Starting new transaction");
        if(transactionConnection.get() != null) {
            throw new IllegalStateException("Unable to start transaction, transaction already in progress");
        }
        Connection connection;
        try {
            connection = dataSource.getConnection();
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            connection.setAutoCommit(false);
            transactionConnection.set(connection);
        }
        catch(SQLException e) {
            try {
                connection.close();
            }
            catch(SQLException e1) {
                throw new RuntimeException("Unable to close connection", e);
            }
            throw new RuntimeException(e);
        }

    }

    public void commitTransaction() {
        LOGGER.debug("Committing transaction");
        Connection connection = transactionConnection.get();
        if(connection == null) {
            throw new IllegalStateException("Unable to commit transaction with missing connection");
        }
        try {
            connection.commit();
            connection.close();
        }
        catch(SQLException e) {
            try {
                connection.close();
            }
            catch(SQLException e1) {
                throw new RuntimeException("Unable to close connection", e1);
            }
            throw new RuntimeException("Unable to commit transaction", e);
        }
        finally {
            transactionConnection.remove();
        }
    }

    public void rollbackTransaction() {
        LOGGER.debug("Rolling back transaction");
        Connection connection = transactionConnection.get();
        if(connection == null) {
            throw new IllegalStateException("Unable to rollback transaction with missing connection");
        }
        try {
            connection.rollback();
            connection.close();
        }
        catch(SQLException e) {
            try {
                connection.close();
            }
            catch(SQLException e1) {
                throw new RuntimeException("Unable to close connection", e1);
            }
            throw new RuntimeException("Failed to rollback transaction", e);
        }
        finally {
            transactionConnection.remove();
        }
    }

    public boolean isTransactionInProgress() {
        return transactionConnection.get() != null;
    }

}
