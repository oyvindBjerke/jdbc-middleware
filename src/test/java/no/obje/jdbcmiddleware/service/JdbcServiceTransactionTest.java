package no.obje.jdbcmiddleware.service;

import no.obje.jdbcmiddleware.HsqlUtil;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JdbcServiceTransactionTest {

    private JdbcService jdbcService;
    private TransactionManager transactionManager;
    private MapCustomer rowMapper = new MapCustomer();

    @Before
    public void setUp() throws Exception {
        final JDBCDataSource dataSource = HsqlUtil.setupDataSource();
        HsqlUtil.createCustomerTable(dataSource);
        final ConnectionManager connectionManager = new ConnectionManager(dataSource);
        transactionManager = new TransactionManager(connectionManager);
        jdbcService = new JdbcService(connectionManager);
    }

    @Test
    public void error_inside_transaction_should_cause_insert_to_be_rolled_back() {
        assertTrue(jdbcService.queryForList("SELECT * FROM customer", rowMapper).isEmpty());
        try {
            transactionManager.doInTransaction(() -> {
                jdbcService.insert("INSERT INTO customer (id, name) VALUES (?, ?)", 1, "Melisandre");
                assertEquals(1, jdbcService.queryForList("SELECT * FROM customer", rowMapper).size());
                throw new RuntimeException();
            });
        }
        catch(RuntimeException e) {
            assertTrue(jdbcService.queryForList("SELECT * FROM customer", rowMapper).isEmpty());
        }
    }

    @Test
    public void error_inside_transaction_should_cause_update_to_be_rolled_back() {
        jdbcService.insert("INSERT INTO customer (id, name) VALUES (?, ?)", 1, "Joffrey Baratheon");
        try {
            transactionManager.doInTransaction(() -> {
                jdbcService.updateSingle("UPDATE customer SET name = ? WHERE id = ?", "Daario Naharis", 2);
                final Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer", rowMapper);
                assertTrue(name.isPresent());
                assertEquals("Daario Naharis", name.get());
                throw new RuntimeException();
            });
        }
        catch(RuntimeException e) {
            final Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer", rowMapper);
            assertTrue(name.isPresent());
            assertEquals("Joffrey Baratheon", name.get());
        }
    }

    @Test
    public void error_inside_transaction_should_case_delete_to_be_rolled_back() {
        jdbcService.insert("INSERT INTO customer (id, name) VALUES (?, ?)", 1, "Barristan Selmy");
        try {
            transactionManager.doInTransaction(() -> {
                jdbcService.deleteSingle("DELETE FROM customer");
                final Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer", rowMapper);
                assertFalse(name.isPresent());
                throw new RuntimeException();
            });
        }
        catch(RuntimeException e) {
            final Optional<String> name = jdbcService.queryForSingle("SELECT * FROM customer", rowMapper);
            assertTrue(name.isPresent());
            assertEquals("Barristan Selmy", name.get());
        }
    }

}
