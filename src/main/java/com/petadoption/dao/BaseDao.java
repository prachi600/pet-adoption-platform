package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;
import com.petadoption.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC helper methods shared by all DAOs (PreparedStatements, try-with-resources, no leaks). */
public abstract class BaseDao {

    @FunctionalInterface
    public interface RowMapper<R> {
        R map(ResultSet rs) throws SQLException;
    }

    protected <R> List<R> query(String sql, RowMapper<R> mapper, Object... params) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<R> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database query failed: " + e.getMessage(), e);
        }
    }

    protected int update(String sql, Object... params) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Database update failed: " + e.getMessage(), e);
        }
    }

    /** Runs an INSERT and returns the auto-generated key. */
    protected int insert(String sql, Object... params) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database insert failed: " + e.getMessage(), e);
        }
    }

    protected long count(String sql, Object... params) throws DatabaseException {
        List<Long> result = query(sql, rs -> rs.getLong(1), params);
        return result.isEmpty() ? 0L : result.get(0);
    }

    protected static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
