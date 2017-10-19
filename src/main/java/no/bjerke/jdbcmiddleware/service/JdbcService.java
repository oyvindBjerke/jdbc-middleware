package no.bjerke.jdbcmiddleware.service;

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

    private final ConnectionManager connectionManager;


    /**
     * Use this constructor if you intend to use transaction functionality
     *
     * @param connectionManager connection manager used for managing connections
     */
    public JdbcService(ConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager);
    }

    /**
     * Use this constructor if you don't intend to use transaction functionality
     *
     * @param dataSource the data source to be used for connection management
     */
    public JdbcService(DataSource dataSource) {
        this.connectionManager = new ConnectionManager(Objects.requireNonNull(dataSource));
    }

    public <T> Optional<T> queryForSingle(String sql, RowMapper<T> rowMapper, Object... args) {
        return connectionManager.doWithConnection(connection ->
                withStatement(connection, sql, Arrays.asList(args), statement ->
                        executeSingle(statement, rowMapper)
                )
        );
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return connectionManager.doWithConnection(connection ->
                withStatement(connection, sql, Arrays.asList(args), statement ->
                        executeList(statement, rowMapper)
                )
        );
    }

    public Long insertAndReturnKey(String sql, String keyColumn, Object... args) {
        return connectionManager.doWithConnection(connection ->
                withStatementAndReturnGeneratedKeys(connection, sql, Arrays.asList(args), statement ->
                        executeInsert(statement, keyColumn)
                )
        );

    }

    public void insert(String sql, Object... args) {
        connectionManager.doWithConnection(connection ->
                withStatement(connection, sql, Arrays.asList(args), statement -> {
                    executeInsert(statement);
                    return null;
                })
        );
    }

    public void updateSingle(String sql, Object... args) {
        connectionManager.doWithConnection(connection ->
                withStatement(connection, sql, Arrays.asList(args), statement -> {
                    try {
                        int rowsAffected = statement.executeUpdate();
                        if(rowsAffected > 1) {
                            throw new IllegalStateException("More than one row was affected by query: '" + sql + "'");
                        }
                        if(rowsAffected < 1) {
                            throw new IllegalStateException("No row was affected by query: '" + sql + "'");
                        }
                    }
                    catch(SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                })
        );
    }

    public Integer update(String sql, Object... args) {
        return connectionManager.doWithConnection(connection ->
                withStatement(connection, sql, Arrays.asList(args), statement -> {
                    try {
                        return statement.executeUpdate();
                    }
                    catch(SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
        );
    }

    public void deleteSingle(String sql, Object... args) {
        updateSingle(sql, args);
    }

    public Integer delete(String sql, Object... args) {
        return update(sql, args);
    }

    private <R> R withStatement(Connection connection, String sql, List<Object> args, StatementCallback<R> callback) {
        if(sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("SQL String cannot be blank or null");
        }
        LOGGER.debug("Executing query: '{}' with values {}", sql, args);
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            final R result = executeStatement(statement, args, callback);
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

    private <R> R withStatementAndReturnGeneratedKeys(
            Connection connection,
            String sql,
            List<Object> args,
            StatementCallback<R> callback
    ) {
        if(sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("SQL String cannot be blank or null");
        }
        LOGGER.debug("Executing query: '{}' with values {}", sql, args);
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            final R result = executeStatement(statement, args, callback);
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

    private <R> R executeStatement(PreparedStatement statement, List<Object> args, StatementCallback<R> callback) {
        for(int i = 1; i < args.size() + 1; i++) {
            setArg(statement, i, args.get(i - 1));
        }
        return callback.run(statement);
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

    private Long executeInsert(PreparedStatement statement, String keyColumn) {
        if(keyColumn == null || keyColumn.isEmpty()) {
            throw new IllegalArgumentException("Key column must be specified");
        }
        try {
            final int numberOfRowsAffected = statement.executeUpdate();
            if(numberOfRowsAffected <= 0) {
                throw new IllegalStateException("No rows was inserted");
            }
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if(generatedKeys.next()) {
                try {
                    return generatedKeys.getLong(keyColumn);
                }
                catch(SQLException e) {
                    LOGGER.warn("Unable to find generated key for column '{}'", keyColumn);
                    throw new MalformedSqlException(e);
                }
            }
            throw new IllegalStateException("No ID (key) was generated");
        }
        catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void executeInsert(PreparedStatement statement) {
        try {
            final int numberOfRowsAffected = statement.executeUpdate();
            if(numberOfRowsAffected <= 0) {
                throw new IllegalStateException("No rows was inserted");
            }
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
