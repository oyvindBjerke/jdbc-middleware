package no.bjerke.jdbcmiddleware.domain;

public interface RowMapper<T> {
    T mapRow(QueryResult queryResult);
}
