package no.bjerke.jdbcmiddleware.domain;

import java.sql.Connection;

public interface ConnectionCallback<R> {
    R run(Connection connection);
}
