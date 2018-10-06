package no.obje.jdbcmiddleware.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TransactionManagerTest {

    private TransactionManager transactionManager;
    private ConnectionManager connectionManager;

    @Before
    public void setUp() {
        connectionManager = mock(ConnectionManager.class);
        transactionManager = new TransactionManager(connectionManager);
    }

    @Test
    public void do_runnable_should_invoke_connection_manager() {
        transactionManager.doInTransaction(() -> System.out.println("Do something"));
        verify(connectionManager, times(1)).startTransaction();
        verify(connectionManager, times(1)).commitTransaction();
    }

    @Test
    public void do_callable_should_return_expected_result_and_invoke_connection_manager() {
        Integer result = transactionManager.doInTransaction(() -> 1 + 1);
        assertEquals((Integer)2, result);
        verify(connectionManager, times(1)).startTransaction();
        verify(connectionManager, times(1)).commitTransaction();
    }

    @Test
    public void exception_during_runnable_should_invoke_connection_manager() {
        try {
            transactionManager.doInTransaction(() -> {
                throw new RuntimeException();
            });
        }
        catch(RuntimeException e) {
            verify(connectionManager, times(1)).startTransaction();
            verify(connectionManager, times(1)).rollbackTransaction();
        }
    }

    @Test
    public void exception_during_callable_should_invoke_connection_manager() {
        try {
            transactionManager.doInTransaction(() -> {
                //noinspection ConstantConditions,ConstantIfStatement
                if(true) {
                    throw new RuntimeException();
                }
                return 1 + 1;
            });
        }
        catch(RuntimeException e) {
            verify(connectionManager, times(1)).startTransaction();
            verify(connectionManager, times(1)).rollbackTransaction();
        }
    }

    @Test
    public void runnable_inside_runnable_should_invoke_connection_manager() {
        transactionManager.doInTransaction(() -> {
            when(connectionManager.isTransactionInProgress()).thenReturn(true);
            transactionManager.doInTransaction(() -> System.out.println("Something"));
        });
        verify(connectionManager, times(1)).startTransaction();
        verify(connectionManager, times(2)).isTransactionInProgress();
        verify(connectionManager, times(1)).commitTransaction();
    }

    @Test
    public void callable_inside_callable_should_invoke_connection_manager() {
        final Integer result = transactionManager.doInTransaction(() -> {
            when(connectionManager.isTransactionInProgress()).thenReturn(true);
            return transactionManager.doInTransaction(() -> 1 + 1);
        });
        assertEquals((Integer)2, result);
        verify(connectionManager, times(1)).startTransaction();
        verify(connectionManager, times(2)).isTransactionInProgress();
        verify(connectionManager, times(1)).commitTransaction();
    }

}