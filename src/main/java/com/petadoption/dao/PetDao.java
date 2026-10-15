package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.Pet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PetDao extends AbstractDao<Pet, Integer> {

    @Override
    protected String tableName() {
        return "pets";
    }

    @Override
    protected String idColumn() {
        return "pet_id";
    }

    @Override
    protected String baseSelect() {
        return "SELECT pets.*, shelters.shelter_name AS shelter_name, shelters.city AS shelter_city, "
                + "shelters.user_id AS shelter_user_id "
                + "FROM pets JOIN shelters ON pets.shelter_id = shelters.shelter_id";
    }

    @Override
    protected String defaultOrder() {
        return "pets.pet_id DESC";
    }

    @Override
    protected Pet mapRow(ResultSet rs) throws SQLException {
        Pet p = new Pet();
        p.setPetId(rs.getInt("pet_id"));
        p.setShelterId(rs.getInt("shelter_id"));
        p.setName(rs.getString("name"));
        p.setSpecies(rs.getString("species"));
        p.setBreed(rs.getString("breed"));
        p.setAge(rs.getInt("age"));
        p.setDescription(rs.getString("description"));
        p.setStatus(ApprovalStatus.valueOf(rs.getString("status")));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setShelterName(rs.getString("shelter_name"));
        p.setShelterCity(rs.getString("shelter_city"));
        p.setShelterUserId(rs.getInt("shelter_user_id"));
        return p;
    }

    @Override
    public int save(Pet p) throws DatabaseException {
        int id = insert("INSERT INTO pets (shelter_id, name, species, breed, age, description, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                p.getShelterId(), p.getName(), p.getSpecies(), p.getBreed(), p.getAge(), p.getDescription(), p.getStatus().name());
        p.setPetId(id);
        return id;
    }

    @Override
    public boolean update(Pet p) throws DatabaseException {
        return update("UPDATE pets SET name = ?, species = ?, breed = ?, age = ?, description = ? WHERE pet_id = ?",
                p.getName(), p.getSpecies(), p.getBreed(), p.getAge(), p.getDescription(), p.getPetId()) > 0;
    }

    public boolean updateStatus(int petId, ApprovalStatus status) throws DatabaseException {
        return update("UPDATE pets SET status = ? WHERE pet_id = ?", status.name(), petId) > 0;
    }

    /** Public search: only APPROVED pets, optional keyword and species filters. */
    public List<Pet> search(String keyword, String species) throws DatabaseException {
        StringBuilder sql = new StringBuilder(baseSelect()).append(" WHERE pets.status = 'APPROVED'");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(pets.name) LIKE ? OR LOWER(pets.breed) LIKE ? OR LOWER(pets.description) LIKE ?)");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (species != null && !species.trim().isEmpty()) {
            sql.append(" AND LOWER(pets.species) = ?");
            params.add(species.trim().toLowerCase());
        }
        sql.append(" ORDER BY pets.pet_id DESC");
        return query(sql.toString(), this::mapRow, params.toArray());
    }

    public List<String> findApprovedSpecies() throws DatabaseException {
        return query("SELECT DISTINCT species FROM pets WHERE status = 'APPROVED' ORDER BY species", rs -> rs.getString(1));
    }

    public List<Pet> findByShelter(int shelterId) throws DatabaseException {
        return query(baseSelect() + " WHERE pets.shelter_id = ? ORDER BY pets.pet_id DESC", this::mapRow, shelterId);
    }

    public long countByStatus(ApprovalStatus status) throws DatabaseException {
        return count("SELECT COUNT(*) FROM pets WHERE status = ?", status.name());
    }
}
