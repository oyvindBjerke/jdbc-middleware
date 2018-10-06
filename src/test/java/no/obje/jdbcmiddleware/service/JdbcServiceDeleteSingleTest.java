package no.obje.jdbcmiddleware.service;

import no.obje.jdbcmiddleware.HsqlUtil;
import no.obje.jdbcmiddleware.exception.MalformedSqlException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;

public class JdbcServiceDeleteSingleTest {

    private JdbcService jdbcService;
    private MapCustomer rowMapper = new MapCustomer();

    @Before
    public void setUp() throws Exception {
        final JDBCDataSource dataSource = HsqlUtil.setupDataSource();
        HsqlUtil.createCustomerTableAndInsertJonSnow(dataSource);
        jdbcService = new JdbcService(dataSource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_sql_should_throw_expected_exception() {
        jdbcService.deleteSingle(null);
    }

    @Test(expected = MalformedSqlException.class)
    public void deleting_row_in_non_existing_table_should_throw_expected_exception() {
        jdbcService.deleteSingle("DELETE FROM employee WHERE id = ?", 1);
    }

    @Test(expected = IllegalStateException.class)
    public void deleting_non_existing_row_in_existing_table_should_throw_expected_exception() {
        jdbcService.deleteSingle("DELETE FROM customer WHERE id = ?", 2);
    }

    @Test(expected = IllegalStateException.class)
    public void deleting_two_rows_should_throw_expected_exception() {
        jdbcService.insert("INSERT INTO customer (name) VALUES (?)", "Renly Boratheon");
        jdbcService.deleteSingle("DELETE FROM customer");
    }

    @Test
    public void deleting_row_should_result_in_row_being_deleted() {
        jdbcService.deleteSingle("DELETE FROM customer WHERE id = ?", 1);
        final Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer WHERE id = ?", rowMapper, 1);
        assertFalse(name.isPresent());
    }

}
