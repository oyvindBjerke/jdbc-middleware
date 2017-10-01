package no.bjerke.jdbcmiddleware.service;

import no.bjerke.jdbcmiddleware.exception.MalformedSqlException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"SqlNoDataSourceIn-spection", "SqlResolve", "SqlNoDataSourceInspection"})
public class JdbcServiceQueryForSingleTest {

    private JdbcService jdbcService;
    private MapCustomer rowMapper = new MapCustomer();

    @Before
    public void setUp() throws Exception {
        final JDBCDataSource dataSource = HsqlUtil.setupDataSource();
        HsqlUtil.createCustomerTable(dataSource);
        jdbcService = new JdbcService(dataSource);
    }

    @Test
    public void query_for_single_should_return_empty_when_no_row_was_found() {
        Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer WHERE id = ?", rowMapper, 999);
        assertFalse(name.isPresent());
    }

    @Test
    public void query_for_single_should_return_optional_of_expected_name_when_row_was_found() {
        Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer WHERE id = ?", rowMapper, 1);
        assertTrue(name.isPresent());
        assertEquals("Jon Snow", name.get());
    }

    @Test(expected = MalformedSqlException.class)
    public void query_for_single_with_malformed_query_should_throw_expected_exception() {
        jdbcService.queryForSingle("asd", rowMapper, 1);
    }

    @Test(expected = MalformedSqlException.class)
    public void query_for_single_for_table_which_does_not_exist_should_throw_expected_exception() {
        jdbcService.queryForSingle("SELECT * FROM doesnotexist", rowMapper, 1);
    }

    @Test(expected = MalformedSqlException.class)
    public void query_for_single_for_column_which_does_not_exist_should_throw_expected_exception() {
        jdbcService.queryForSingle("SELECT id, name, age FROM customer WHERE id = ?", rowMapper, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_for_single_with_null_sql_should_throw_expected_exception() {
        jdbcService.queryForSingle(null, rowMapper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_for_single_with_null_row_mapper_should_throw_expected_exception() {
        jdbcService.queryForSingle("SELECT * FROM customer", null);
    }

}
