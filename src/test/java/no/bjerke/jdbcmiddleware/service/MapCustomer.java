package no.bjerke.jdbcmiddleware.service;

import no.bjerke.jdbcmiddleware.domain.QueryResult;
import no.bjerke.jdbcmiddleware.domain.RowMapper;

public class MapCustomer implements RowMapper<String> {

    @Override
    public String mapRow(QueryResult queryResult) {
        return queryResult.requireString("name");
    }
}
