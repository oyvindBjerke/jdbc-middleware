package no.bjerke.jdbcmiddleware.service;

import no.bjerke.jdbcmiddleware.exception.MalformedSqlException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"SqlNoDataSourceIn-spection", "SqlResolve", "SqlNoDataSourceInspection"})
public class JdbcServiceQueryForListTest {

    private JdbcService jdbcService;
    private MapCustomer mapCustomer = new MapCustomer();

    @Before
    public void setUp() throws Exception {
        final JDBCDataSource dataSource = HsqlUtil.setupDataSource();
        HsqlUtil.createCustomerTableAndInsertJonSnow(dataSource);
        jdbcService = new JdbcService(dataSource);
    }

    @Test
    public void query_for_list_should_return_empty_list_when_no_row_was_found() {
        final List<String> result = jdbcService.queryForList("SELECT * from customer where id = ?", mapCustomer, 999);
        assertTrue(result.isEmpty());
    }

    @Test
    public void query_for_list_should_return_list_with_expected_result_when_rows_was_found() {
        final List<String> result = jdbcService.queryForList("SELECT * from customer", mapCustomer);
        assertEquals(1, result.size());
        assertEquals("Jon Snow", result.get(0));
    }

    @Test(expected = MalformedSqlException.class)
    public void query_for_list_with_malformed_query_should_throw_expected_exception() {
        jdbcService.queryForList("asd", mapCustomer);
    }

    @Test(expected = MalformedSqlException.class)
    public void query_for_list_for_table_which_does_not_exist_should_throw_expected_exception() {
        jdbcService.queryForList("SELECT * from doesnotexist", mapCustomer);
    }

    @Test(expected = MalformedSqlException.class)
    public void query_for_list_for_column_which_does_not_exist_should_throw_expected_exception() {
        jdbcService.queryForList("SELECT id, name, age FROM customer WHERE id = ?", mapCustomer, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_for_list_with_null_sql_should_throw_expected_exception() {
        jdbcService.queryForList(null, mapCustomer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void query_for_list_with_null_row_mapper_should_throw_expected_exception() {
        jdbcService.queryForList("SELECT * FROM customer", null);
    }

}
