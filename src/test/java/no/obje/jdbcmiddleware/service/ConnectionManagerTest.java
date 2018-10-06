package no.obje.jdbcmiddleware.service;

import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class ConnectionManagerTest {

    private ConnectionManager connectionManager;
    private DataSource dataSource;
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        connectionManager = new ConnectionManager(dataSource);
    }

    @Test
    public void starting_a_transaction_should_get_a_connection_and_set_auto_commit_to_false() throws SQLException {
        connectionManager.startTransaction();
        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).setAutoCommit(false);
    }

    @Test(expected = IllegalStateException.class)
    public void starting_a_transaction_when_one_is_already_in_progress_should_throw_an_exception() {
        connectionManager.startTransaction();
        connectionManager.startTransaction();
    }

    @Test(expected = IllegalStateException.class)
    public void committing_a_transaction_when_there_is_none_in_progress_should_throw_an_exception() {
        connectionManager.commitTransaction();
    }

    @Test
    public void committing_a_transaction_should_commit_and_close_connection() throws SQLException {
        connectionManager.startTransaction();
        connectionManager.commitTransaction();
        verify(connection, times(1)).commit();
        verify(connection, times(1)).close();
    }

    @Test(expected = IllegalStateException.class)
    public void rolling_back_a_transaction_when_there_is_none_in_progress_should_throw_an_exception() {
        connectionManager.rollbackTransaction();
    }

    @Test
    public void rolling_back_a_transaction_should_roll_back_and_close_connection() throws SQLException {
        connectionManager.startTransaction();
        connectionManager.rollbackTransaction();
        verify(connection, times(1)).rollback();
        verify(connection, times(1)).close();
    }
}