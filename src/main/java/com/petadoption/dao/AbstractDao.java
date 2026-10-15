package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Template implementation of {@link Dao}. Sub-classes only describe their table and how a row
 * becomes an object ({@link #mapRow}); the generic CRUD plumbing lives here.
 */
public abstract class AbstractDao<T, ID> extends BaseDao implements Dao<T, ID> {

    protected abstract String tableName();

    protected abstract String idColumn();

    protected abstract T mapRow(ResultSet rs) throws SQLException;

    /** SELECT used by findById/findAll. Sub-classes override it to add JOINs. */
    protected String baseSelect() {
        return "SELECT * FROM " + tableName();
    }

    protected String defaultOrder() {
        return tableName() + "." + idColumn() + " ASC";
    }

    @Override
    public T findById(ID id) throws DatabaseException {
        List<T> rows = query(baseSelect() + " WHERE " + tableName() + "." + idColumn() + " = ?", this::mapRow, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public List<T> findAll() throws DatabaseException {
        return query(baseSelect() + " ORDER BY " + defaultOrder(), this::mapRow);
    }

    @Override
    public boolean delete(ID id) throws DatabaseException {
        return update("DELETE FROM " + tableName() + " WHERE " + idColumn() + " = ?", id) > 0;
    }
}
