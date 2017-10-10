package no.bjerke.jdbcmiddleware.service;

import no.bjerke.jdbcmiddleware.HsqlUtil;
import no.bjerke.jdbcmiddleware.exception.MalformedSqlException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class JdbcServiceUpdateTest {

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
        jdbcService.update(null);
    }

    @Test(expected = MalformedSqlException.class)
    public void updating_row_in_non_existing_table_should_throw_expected_exception() {
        jdbcService.updateSingle("UPDATE employee SET name = ?", "Jorah Mormont");
    }

    @Test
    public void update_non_existing_row_in_existing_table_should_return_0_rows_affected() {
        final Integer rowsUpdated = jdbcService.update("UPDATE customer SET name = ? WHERE id = ?", "Samwell Tarly", 2);
        assertEquals((Integer)0, rowsUpdated);
    }

    @Test
    public void updating_two_rows_should_return_2_rows_affected() {
        jdbcService.insert("INSERT INTO customer (name) VALUES (?)", "Arya Stark");
        final Integer rowsUpdated = jdbcService.update("UPDATE customer SET name = ?", "Cersei Lannister");
        assertEquals((Integer)2, rowsUpdated);
    }

    @Test
    public void updating_two_rows_should_result_in_both_rows_being_updated() {
        jdbcService.insert("INSERT INTO customer (name) VALUES (?)", "Sansa Stark");
        jdbcService.update("UPDATE customer SET name = ?", "Jaime Lannister");
        final List<String> names = jdbcService.queryForList("SELECT * from customer", rowMapper);
        assertEquals(2, names.size());
        names.forEach(name -> assertEquals("Jaime Lannister", name));
    }

}
