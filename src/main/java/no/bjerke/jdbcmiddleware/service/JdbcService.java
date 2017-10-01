package no.bjerke.jdbcmiddleware.service;

import no.bjerke.jdbcmiddleware.domain.ConnectionCallback;
import no.bjerke.jdbcmiddleware.domain.QueryResult;
import no.bjerke.jdbcmiddleware.domain.RowMapper;
import no.bjerke.jdbcmiddleware.domain.StatementCallback;
import no.bjerke.jdbcmiddleware.exception.MalformedSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class JdbcService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcService.class);

    private final DataSource dataSource;

    public JdbcService(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public <T> Optional<T> queryForSingle(String sql, RowMapper<T> rowMapper, Object... args) {
        return withConnection(connection ->
                withStatement(connection, sql, Arrays.asList(args), statement ->
                        executeSingle(statement, rowMapper)
                )
        );
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return withConnection(connection ->
                withStatement(connection, sql, Arrays.asList(args), statement ->
                        executeList(statement, rowMapper)
                )
        );
    }

    private <R> R withConnection(ConnectionCallback<R> callback) {
        try(Connection connection = dataSource.getConnection()) {
            return callback.run(connection);
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <R> R withStatement(Connection connection, String sql, List<Object> args, StatementCallback<R> callback) {
        if(sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("SQL String cannot be blank or null");
        }
        LOGGER.debug("Executing query: '{}' with values {}", sql, args);
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            for(int i = 1; i < args.size() + 1; i++) {
                setArg(statement, i, args.get(i - 1));
            }
            final R result = callback.run(statement);
            LOGGER.info("Executed query: '{}' with values {}", sql, args);
            return result;
        }
        catch(SQLSyntaxErrorException e) {
            throw new MalformedSqlException(e);
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <R> Optional<R> executeSingle(PreparedStatement preparedStatement, RowMapper<R> rowMapper) {
        if(rowMapper == null) {
            throw new IllegalArgumentException("Row mapper must be specified");
        }
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if(resultSet.next()) {
                return Optional.of(rowMapper.mapRow(new QueryResult(resultSet)));
            }
            return Optional.empty();
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private <R> List<R> executeList(PreparedStatement preparedStatement, RowMapper<R> rowMapper) {
        if(rowMapper == null) {
            throw new IllegalArgumentException("Row mapper must be specified");
        }
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            final ArrayList<R> resultList = new ArrayList<>();
            while(resultSet.next()) {
                final QueryResult queryResult = new QueryResult(resultSet);
                resultList.add(rowMapper.mapRow(queryResult));
            }
            return resultList;
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setArg(PreparedStatement statement, int index, Object arg) {
        try {
            if(arg instanceof Integer) {
                statement.setInt(index, (Integer)arg);
            }
            else if(arg instanceof Boolean) {
                statement.setBoolean(index, (Boolean)arg);
            }
            else if(arg instanceof Long) {
                statement.setLong(index, (Long)arg);
            }
            else if(arg instanceof LocalDate) {
                statement.setDate(index, Date.valueOf((LocalDate)arg));
            }
            else if(arg instanceof String) {
                statement.setString(index, (String)arg);
            }
            else {
                throw new RuntimeException("Unsupported argument type: " + arg.getClass());
            }
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
