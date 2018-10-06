package no.obje.jdbcmiddleware.domain;

public interface RowMapper<T> {
    T mapRow(QueryResult queryResult);
}
