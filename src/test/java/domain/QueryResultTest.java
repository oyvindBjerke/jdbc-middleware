package domain;

import org.junit.Before;
import org.junit.Test;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QueryResultTest {

    private ResultSet resultSetMock;
    private QueryResult queryResult;

    @Before
    public void setUp() {
        resultSetMock = mock(ResultSet.class);
        queryResult = new QueryResult(resultSetMock);
    }

    @Test
    public void get_string_with_no_value_should_return_empty() throws SQLException {
        when(resultSetMock.getString(any())).thenReturn(null);
        final Optional<String> firstname = queryResult.getString("firstname");
        assertFalse(firstname.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void require_string_with_no_value_should_throw_an_exception() throws SQLException {
        when(resultSetMock.getString(any())).thenReturn(null);
        queryResult.requireString("firstname");
    }

    @Test
    public void get_string_with_value_should_return_optional_of_expected_value() throws SQLException {
        when(resultSetMock.getString(any())).thenReturn("foobar");
        final Optional<String> lastname = queryResult.getString("lastname");
        assertTrue(lastname.isPresent());
        assertEquals("foobar", lastname.get());
    }

    @Test
    public void require_string_with_value_should_return_expected_value() throws SQLException {
        when(resultSetMock.getString(any())).thenReturn("foobar");
        final String status = queryResult.requireString("status");
        assertEquals("foobar", status);
    }

    @Test
    public void get_long_with_no_value_should_return_empty() throws SQLException {
        when(resultSetMock.getLong(any())).thenReturn(0L);
        when(resultSetMock.wasNull()).thenReturn(true);
        final Optional<Long> id = queryResult.getLong("id");
        assertFalse(id.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void require_long_with_no_value_should_throw_an_exception() throws SQLException {
        when(resultSetMock.getLong(any())).thenReturn(0L);
        when(resultSetMock.wasNull()).thenReturn(true);
        queryResult.requireLong("id");
    }

    @Test
    public void get_long_with_value_should_return_optional_of_expected_value() throws SQLException {
        when(resultSetMock.getLong(any())).thenReturn(1L);
        final Optional<Long> balance = queryResult.getLong("balance");
        assertTrue(balance.isPresent());
        assertEquals((Long)1L, balance.get());
    }

    @Test
    public void require_long_with_value_should_return_expected_value() throws SQLException {
        when(resultSetMock.getLong(any())).thenReturn(1L);
        final Long balance = queryResult.requireLong("balance");
        assertEquals((Long)1L, balance);
    }

    @Test
    public void get_integer_with_no_value_should_return_empty() throws SQLException {
        when(resultSetMock.getInt(any())).thenReturn(0);
        when(resultSetMock.wasNull()).thenReturn(true);
        final Optional<Integer> age = queryResult.getInteger("age");
        assertFalse(age.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void require_integer_with_no_value_should_throw_an_exception() throws SQLException {
        when(resultSetMock.getInt(any())).thenReturn(0);
        when(resultSetMock.wasNull()).thenReturn(true);
        queryResult.requireInteger("age");
    }

    @Test
    public void get_integer_with_value_should_return_optional_of_expected_value() throws SQLException {
        when(resultSetMock.getInt(any())).thenReturn(1);
        final Optional<Integer> amount = queryResult.getInteger("amount");
        assertTrue(amount.isPresent());
        assertEquals((Integer)1, amount.get());
    }

    @Test
    public void require_integer_with_value_should_return_expected_value() throws SQLException {
        when(resultSetMock.getInt(any())).thenReturn(1);
        final Integer amount = queryResult.requireInteger("amount");
        assertEquals((Integer)1, amount);
    }

    @Test
    public void get_boolean_with_no_value_should_return_empty() throws SQLException {
        when(resultSetMock.getBoolean(any())).thenReturn(false);
        when(resultSetMock.wasNull()).thenReturn(true);
        final Optional<Boolean> single = queryResult.getBoolean("single");
        assertFalse(single.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void require_boolean_with_no_value_should_throw_an_exception() throws SQLException {
        when(resultSetMock.getBoolean(any())).thenReturn(false);
        when(resultSetMock.wasNull()).thenReturn(true);
        queryResult.requireBoolean("single");
    }

    @Test
    public void get_boolean_with_value_should_return_optional_of_expected_value() throws SQLException {
        when(resultSetMock.getBoolean(any())).thenReturn(true);
        final Optional<Boolean> flagged = queryResult.getBoolean("flagged");
        assertTrue(flagged.isPresent());
        assertTrue(flagged.get());
    }

    @Test
    public void require_boolean_with_value_should_return_expected_value() throws SQLException {
        when(resultSetMock.getBoolean(any())).thenReturn(false);
        final boolean flagged = queryResult.requireBoolean("flagged");
        assertFalse(flagged);
    }

    @Test
    public void get_date_with_no_value_should_return_empty() throws SQLException {
        when(resultSetMock.getDate(any())).thenReturn(null);
        final Optional<LocalDate> birthdate = queryResult.getDate("birthdate");
        assertFalse(birthdate.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void require_date_with_no_value_should_throw_an_exception() throws SQLException {
        when(resultSetMock.getDate(any())).thenReturn(null);
        queryResult.requireDate("birthdate");
    }

    @Test
    public void get_date_with_value_should_return_optional_of_expected_value() throws SQLException {
        final Date dateMock = mock(Date.class);
        LocalDate now = LocalDate.now();
        when(dateMock.toLocalDate()).thenReturn(now);
        when(resultSetMock.getDate(any())).thenReturn(dateMock);
        final Optional<LocalDate> registeredDate = queryResult.getDate("registeredDate");
        assertTrue(registeredDate.isPresent());
        assertEquals(now, registeredDate.get());
    }

    @Test
    public void require_date_with_value_should_return_expected_value() throws SQLException {
        final Date dateMock = mock(Date.class);
        LocalDate now = LocalDate.now();
        when(dateMock.toLocalDate()).thenReturn(now);
        when(resultSetMock.getDate(any())).thenReturn(dateMock);
        final LocalDate registeredDate = queryResult.requireDate("registeredDate");
        assertEquals(now, registeredDate);
    }

}