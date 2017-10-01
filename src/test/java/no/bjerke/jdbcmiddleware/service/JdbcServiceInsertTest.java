package no.bjerke.jdbcmiddleware.service;

import no.bjerke.jdbcmiddleware.exception.MalformedSqlException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JdbcServiceInsertTest {

    private JdbcService jdbcService;
    private MapCustomer rowMapper = new MapCustomer();

    @Before
    public void setUp() throws Exception {
        final JDBCDataSource dataSource = HsqlUtil.setupDataSource();
        HsqlUtil.createCustomerTable(dataSource);
        jdbcService = new JdbcService(dataSource);
    }

    @Test
    public void insert_into_existing_table_should_insert_given_row() {
        jdbcService.insert("INSERT INTO customer (id, name) VALUES (?, ?)", 1, "Daenerys Targaryen");
        Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer WHERE id = ?", rowMapper, 1);
        assertTrue(name.isPresent());
        assertEquals("Daenerys Targaryen", name.get());
    }

    @Test(expected = MalformedSqlException.class)
    public void insert_into_table_which_does_not_exist_should_throw_expected_exception() {
        jdbcService.insert("INSERT INTO doesnotexist (id, name) VALUES (?, ?)", 1, "Daenerys Targaryen");
    }

    @Test(expected = MalformedSqlException.class)
    public void insert_into_with_column_which_does_not_exist_should_throw_expected_exception() {
        jdbcService.insert("INSERT INTO doesnotexist (id, name, age) VALUES (?, ?, ?)", 1, "Daenerys Targaryen", 22);
    }

    @Test
    public void insert_into_table_with_auto_generated_id_should_return_an_id_which_matches_id_of_actual_row() {
        final Long key = jdbcService.insertAndReturnKey("INSERT INTO customer (name) VALUES (?)", "id", "Thormund Giantsbane");
        assertEquals((Long)1L, key);
        Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer WHERE id = ?", rowMapper, key);
        assertTrue(name.isPresent());
        assertEquals("Thormund Giantsbane", name.get());
    }

    @Test(expected = MalformedSqlException.class)
    public void insert_with_auto_generated_key_where_key_column_does_not_exist_should_throw_expected_exception() {
        jdbcService.insertAndReturnKey("INSERT INTO customer (name) VALUES (?)", "key", "Tywin Lannister");
    }

    @Test
    public void insert_multiple_rows_should_return_id_of_first_insert_and_insert_multiple_rows() {
        Long key = jdbcService.insertAndReturnKey(
                "INSERT INTO customer (name) VALUES (?), (?), (?)",
                "id",
                "Tywin Lannister",
                "Gregor Clegane",
                "Ramsey Bolton"
        );
        assertEquals((Long)1L, key);
        final List<String> names = jdbcService.queryForList("SELECT * FROM customer", rowMapper);
        assertEquals(3, names.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void insert_with_null_sql_should_throw_expected_exception() {
        jdbcService.insert(null);
    }

    @Test(expected = MalformedSqlException.class)
    public void insert_with_malformed_sql_should_throw_expected_exception() {
        jdbcService.insert("asd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void insert_with_null_key_column_should_throw_expected_exception() {
        jdbcService.insertAndReturnKey("INSERT INTO customer (name) VALUES (?)", null, "Tywin Lannister");
    }

}
