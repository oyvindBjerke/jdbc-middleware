package no.bjerke.jdbcmiddleware.service;

import no.bjerke.jdbcmiddleware.HsqlUtil;
import no.bjerke.jdbcmiddleware.exception.MalformedSqlException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class JdbcServiceDeleteTest {

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
        jdbcService.delete(null);
    }

    @Test(expected = MalformedSqlException.class)
    public void deleting_row_in_non_existing_table_should_throw_expected_exception() {
        jdbcService.delete("DELETE FROM employee");
    }

    @Test
    public void deleting_non_existing_row_in_existing_table_should_return_0_rows_affected() {
        final Integer rowsDeleted = jdbcService.delete("DELETE FROM customer WHERE id = ?", 2);
        assertEquals((Integer)0, rowsDeleted);
    }

    @Test
    public void deleting_two_rows_should_return_2_rows_affected() {
        jdbcService.insert("INSERT INTO customer (name) VALUES (?)", "Loras Tyrell");
        final Integer rowsDeleted = jdbcService.delete("DELETE FROM customer");
        assertEquals((Integer)2, rowsDeleted);
    }

    @Test
    public void deleting_two_rows_should_result_in_both_rows_being_deleted() {
        jdbcService.insert("INSERT INTO customer (name) VALUES (?)", "Petyr Baelish");
        jdbcService.delete("DELETE FROM customer");
        final List<String> names = jdbcService.queryForList("SELECT * from customer", rowMapper);
        assertEquals(0, names.size());
    }

}
