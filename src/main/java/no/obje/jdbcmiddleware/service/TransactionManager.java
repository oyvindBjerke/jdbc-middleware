package no.obje.jdbcmiddleware.service;

import java.util.Objects;
import java.util.concurrent.Callable;

public class TransactionManager {

    private final ConnectionManager connectionManager;

    public TransactionManager(ConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager);
    }

    public void doInTransaction(Runnable runnable) {
        boolean transactionInProgress = connectionManager.isTransactionInProgress();
        if(!transactionInProgress) {
            connectionManager.startTransaction();
        }
        try {
            runnable.run();
            if(!transactionInProgress) {
                connectionManager.commitTransaction();
            }
        }
        catch(Throwable throwable) {
            if(!transactionInProgress) {
                connectionManager.rollbackTransaction();
            }
            throw new RuntimeException("Something went wrong during execution, rolled back transaction");
        }
    }

    public <T> T doInTransaction(Callable<T> callable) {
        boolean transactionInProgress = connectionManager.isTransactionInProgress();
        if(!transactionInProgress) {
            connectionManager.startTransaction();
        }
        try {
            final T result = callable.call();
            if(!transactionInProgress) {
                connectionManager.commitTransaction();
            }
            return result;
        }
        catch(Throwable throwable) {
            if(!transactionInProgress) {
                connectionManager.rollbackTransaction();
            }
            throw new RuntimeException("Something went wrong during execution, rolled back transaction");
        }
    }

}
