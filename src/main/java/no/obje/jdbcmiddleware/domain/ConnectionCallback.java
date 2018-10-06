package no.obje.jdbcmiddleware.domain;

import java.sql.Connection;

public interface ConnectionCallback<R> {
    R run(Connection connection);
}
