package no.obje.jdbcmiddleware.service;

import no.obje.jdbcmiddleware.HsqlUtil;
import no.obje.jdbcmiddleware.exception.MalformedSqlException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JdbcServiceUpdateSingleTest {

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
        jdbcService.updateSingle(null);
    }

    @Test(expected = MalformedSqlException.class)
    public void updating_row_in_non_existing_table_should_throw_expected_exception() {
        jdbcService.updateSingle("UPDATE employee SET name = ? WHERE id = ?", "Beric Dondarrion", 2);
    }

    @Test(expected = IllegalStateException.class)
    public void update_non_existing_row_in_existing_table_should_throw_expected_exception() {
        jdbcService.updateSingle("UPDATE customer SET name = ? WHERE id = ?", "Aerys Targaryen", 2);
    }

    @Test(expected = IllegalStateException.class)
    public void updating_two_rows_should_throw_expected_exception() {
        jdbcService.insert("INSERT INTO customer (name) VALUES (?)", "Theon Greyjoy");
        jdbcService.updateSingle("UPDATE customer SET name = ?", "Catelyn Stark");
    }

    @Test
    public void updating_row_should_result_in_row_being_updated() {
        jdbcService.updateSingle("UPDATE customer SET name = ? WHERE id = ?", "Robert Boratheon", 1);
        final Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer WHERE id = ?", rowMapper, 1);
        assertTrue(name.isPresent());
        assertEquals("Robert Boratheon", name.get());
    }

}
