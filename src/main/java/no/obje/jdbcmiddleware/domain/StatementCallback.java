package no.obje.jdbcmiddleware.domain;

import java.sql.PreparedStatement;

public interface StatementCallback<T> {
    T run(PreparedStatement preparedStatement);
}
