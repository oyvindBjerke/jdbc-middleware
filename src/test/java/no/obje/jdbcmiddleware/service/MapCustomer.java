package no.obje.jdbcmiddleware.service;

import no.obje.jdbcmiddleware.domain.QueryResult;
import no.obje.jdbcmiddleware.domain.RowMapper;

public class MapCustomer implements RowMapper<String> {

    @Override
    public String mapRow(QueryResult queryResult) {
        return queryResult.requireString("name");
    }
}
