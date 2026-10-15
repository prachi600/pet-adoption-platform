package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;
import com.petadoption.model.Shelter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ShelterDao extends AbstractDao<Shelter, Integer> {

    @Override
    protected String tableName() {
        return "shelters";
    }

    @Override
    protected String idColumn() {
        return "shelter_id";
    }

    @Override
    protected Shelter mapRow(ResultSet rs) throws SQLException {
        Shelter s = new Shelter();
        s.setShelterId(rs.getInt("shelter_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setShelterName(rs.getString("shelter_name"));
        s.setCity(rs.getString("city"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getString("address"));
        return s;
    }

    @Override
    public int save(Shelter s) throws DatabaseException {
        int id = insert("INSERT INTO shelters (user_id, shelter_name, city, phone, address) VALUES (?, ?, ?, ?, ?)",
                s.getUserId(), s.getShelterName(), s.getCity(), s.getPhone(), s.getAddress());
        s.setShelterId(id);
        return id;
    }

    @Override
    public boolean update(Shelter s) throws DatabaseException {
        return update("UPDATE shelters SET shelter_name = ?, city = ?, phone = ?, address = ? WHERE shelter_id = ?",
                s.getShelterName(), s.getCity(), s.getPhone(), s.getAddress(), s.getShelterId()) > 0;
    }

    public Shelter findByUserId(int userId) throws DatabaseException {
        List<Shelter> rows = query("SELECT * FROM shelters WHERE user_id = ?", this::mapRow, userId);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
