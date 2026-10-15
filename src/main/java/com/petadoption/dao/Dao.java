package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;

import java.util.List;

/**
 * Generic CRUD contract implemented by every DAO.
 *
 * @param <T>  entity type
 * @param <ID> primary-key type
 */
public interface Dao<T, ID> {
    T findById(ID id) throws DatabaseException;

    List<T> findAll() throws DatabaseException;

    /** Inserts the entity and returns the generated key. */
    int save(T entity) throws DatabaseException;

    boolean update(T entity) throws DatabaseException;

    boolean delete(ID id) throws DatabaseException;
}
