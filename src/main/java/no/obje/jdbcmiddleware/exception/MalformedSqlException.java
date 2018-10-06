package no.obje.jdbcmiddleware.exception;

public class MalformedSqlException extends RuntimeException {
    public MalformedSqlException(Throwable throwable) {
        super(throwable);
    }
}
