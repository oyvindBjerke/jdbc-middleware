package no.bjerke.jdbcmiddleware.domain;

import no.bjerke.jdbcmiddleware.util.ExceptionUtil;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class QueryResult {

    private final ResultSet resultSet;

    public QueryResult(ResultSet resultSet) {
        this.resultSet = Objects.requireNonNull(resultSet);
    }

    public String requireString(String columnName) {
        return getString(columnName).orElseThrow(getMissingColumnException(columnName));
    }

    public Optional<String> getString(String columnName) {
        return Optional.ofNullable(ExceptionUtil.soften(() -> resultSet.getString(columnName)));
    }

    public Integer requireInteger(String columnName) {
        return getInteger(columnName).orElseThrow(getMissingColumnException(columnName));
    }

    public Optional<Integer> getInteger(String columnName) {
        return emptyOrValueIfNotWasNull(rs -> ExceptionUtil.soften(() -> rs.getInt(columnName)));
    }

    public Long requireLong(String columnName) {
        return getLong(columnName).orElseThrow(getMissingColumnException(columnName));
    }

    public Optional<Long> getLong(String columnName) {
        return emptyOrValueIfNotWasNull(rs -> ExceptionUtil.soften(() -> rs.getLong(columnName)));
    }

    public boolean requireBoolean(String columnName) {
        return getBoolean(columnName).orElseThrow(getMissingColumnException(columnName));
    }

    public Optional<Boolean> getBoolean(String columnName) {
        return emptyOrValueIfNotWasNull(rs -> ExceptionUtil.soften(() -> rs.getBoolean(columnName)));
    }

    public Optional<LocalDate> getDate(String columnName) {
        return Optional.ofNullable(ExceptionUtil.soften(() -> resultSet.getDate(columnName))).map(Date::toLocalDate);
    }

    public LocalDate requireDate(String columnName) {
        return getDate(columnName).orElseThrow(getMissingColumnException(columnName));
    }

    private Supplier<IllegalStateException> getMissingColumnException(String columnName) {
        return () -> new IllegalStateException("Required column with name '" + columnName + "' was not present");
    }

    private <T> Optional<T> emptyOrValueIfNotWasNull(Function<ResultSet, T> function) {
        T value = function.apply(resultSet);
        return ExceptionUtil.soften(resultSet::wasNull) ? Optional.empty() : Optional.of(value);
    }

}
