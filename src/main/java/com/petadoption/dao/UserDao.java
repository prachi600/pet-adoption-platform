package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;
import com.petadoption.model.Role;
import com.petadoption.model.User;
import com.petadoption.model.UserFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDao extends AbstractDao<User, Integer> {

    @Override
    protected String tableName() {
        return "users";
    }

    @Override
    protected String idColumn() {
        return "user_id";
    }

    @Override
    protected User mapRow(ResultSet rs) throws SQLException {
        User user = UserFactory.create(Role.valueOf(rs.getString("role")));
        user.setId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setSalt(rs.getString("salt"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }

    @Override
    public int save(User u) throws DatabaseException {
        int id = insert("INSERT INTO users (name, email, password_hash, salt, role, status) VALUES (?, ?, ?, ?, ?, ?)",
                u.getName(), u.getEmail(), u.getPasswordHash(), u.getSalt(), u.getRole().name(), u.getStatus());
        u.setId(id);
        return id;
    }

    /** Updates name, email and status (role changes go through {@link #updateRole}). */
    @Override
    public boolean update(User u) throws DatabaseException {
        return update("UPDATE users SET name = ?, email = ?, status = ? WHERE user_id = ?",
                u.getName(), u.getEmail(), u.getStatus(), u.getId()) > 0;
    }

    public boolean updateRole(int userId, Role role) throws DatabaseException {
        return update("UPDATE users SET role = ? WHERE user_id = ?", role.name(), userId) > 0;
    }

    public User findByEmail(String email) throws DatabaseException {
        List<User> rows = query("SELECT * FROM users WHERE LOWER(email) = LOWER(?)", this::mapRow, email);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public long countAll() throws DatabaseException {
        return count("SELECT COUNT(*) FROM users");
    }

    /** People a user is allowed to message: adopters see shelters, shelters see their applicants. */
    public List<User> findContactsFor(User user) throws DatabaseException {
        if (user.isAdopter()) {
            return query("SELECT users.* FROM users JOIN shelters ON shelters.user_id = users.user_id "
                    + "WHERE users.status = 'ACTIVE' ORDER BY users.name", this::mapRow);
        }
        if (user.isShelter()) {
            return query("SELECT DISTINCT users.* FROM users "
                    + "JOIN applications ON applications.adopter_id = users.user_id "
                    + "JOIN pets ON pets.pet_id = applications.pet_id "
                    + "JOIN shelters ON shelters.shelter_id = pets.shelter_id "
                    + "WHERE shelters.user_id = ? AND users.status = 'ACTIVE' ORDER BY users.name",
                    this::mapRow, user.getId());
        }
        return java.util.Collections.emptyList();
    }
}
