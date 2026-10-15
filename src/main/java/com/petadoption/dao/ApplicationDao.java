package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ApplicationDao extends AbstractDao<AdoptionApplication, Integer> {

    @Override
    protected String tableName() {
        return "applications";
    }

    @Override
    protected String idColumn() {
        return "application_id";
    }

    @Override
    protected String baseSelect() {
        return "SELECT applications.*, pets.name AS pet_name, pets.shelter_id AS pet_shelter_id, "
                + "users.name AS adopter_name, users.email AS adopter_email "
                + "FROM applications "
                + "JOIN pets ON applications.pet_id = pets.pet_id "
                + "JOIN users ON applications.adopter_id = users.user_id";
    }

    @Override
    protected String defaultOrder() {
        return "applications.application_id DESC";
    }

    @Override
    protected AdoptionApplication mapRow(ResultSet rs) throws SQLException {
        AdoptionApplication a = new AdoptionApplication();
        a.setApplicationId(rs.getInt("application_id"));
        a.setPetId(rs.getInt("pet_id"));
        a.setAdopterId(rs.getInt("adopter_id"));
        a.setFullName(rs.getString("full_name"));
        a.setPhone(rs.getString("phone"));
        a.setAddress(rs.getString("address"));
        a.setReason(rs.getString("reason"));
        a.setStatus(ApprovalStatus.valueOf(rs.getString("status")));
        a.setAppliedAt(rs.getTimestamp("applied_at"));
        a.setPetName(rs.getString("pet_name"));
        a.setPetShelterId(rs.getInt("pet_shelter_id"));
        a.setAdopterName(rs.getString("adopter_name"));
        a.setAdopterEmail(rs.getString("adopter_email"));
        return a;
    }

    @Override
    public int save(AdoptionApplication a) throws DatabaseException {
        int id = insert("INSERT INTO applications (pet_id, adopter_id, full_name, phone, address, reason, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                a.getPetId(), a.getAdopterId(), a.getFullName(), a.getPhone(), a.getAddress(), a.getReason(), a.getStatus().name());
        a.setApplicationId(id);
        return id;
    }

    @Override
    public boolean update(AdoptionApplication a) throws DatabaseException {
        return update("UPDATE applications SET full_name = ?, phone = ?, address = ?, reason = ?, status = ? WHERE application_id = ?",
                a.getFullName(), a.getPhone(), a.getAddress(), a.getReason(), a.getStatus().name(), a.getApplicationId()) > 0;
    }

    public List<AdoptionApplication> findByAdopter(int adopterId) throws DatabaseException {
        return query(baseSelect() + " WHERE applications.adopter_id = ? ORDER BY " + defaultOrder(), this::mapRow, adopterId);
    }

    public List<AdoptionApplication> findByShelter(int shelterId) throws DatabaseException {
        return query(baseSelect() + " WHERE pets.shelter_id = ? ORDER BY " + defaultOrder(), this::mapRow, shelterId);
    }

    /** True when the adopter already has a PENDING or APPROVED application for this pet. */
    public boolean hasOpenApplication(int adopterId, int petId) throws DatabaseException {
        return count("SELECT COUNT(*) FROM applications WHERE adopter_id = ? AND pet_id = ? "
                + "AND status IN ('PENDING', 'APPROVED')", adopterId, petId) > 0;
    }

    public long countPendingByAdopter(int adopterId) throws DatabaseException {
        return count("SELECT COUNT(*) FROM applications WHERE adopter_id = ? AND status = 'PENDING'", adopterId);
    }

    public long countByStatus(ApprovalStatus status) throws DatabaseException {
        return count("SELECT COUNT(*) FROM applications WHERE status = ?", status.name());
    }

    public boolean updateStatus(int applicationId, ApprovalStatus status) throws DatabaseException {
        return update("UPDATE applications SET status = ? WHERE application_id = ?", status.name(), applicationId) > 0;
    }

    /**
     * JDBC transaction: approves one application, marks the pet ADOPTED and rejects every other
     * pending application for that pet. Either all three updates happen or none of them.
     */
    public void approveAndCloseOthers(int applicationId) throws DatabaseException {
        try (Connection con = DBConnection.getConnection()) {
            boolean previousAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                int petId;
                try (PreparedStatement ps = con.prepareStatement("SELECT pet_id FROM applications WHERE application_id = ?")) {
                    ps.setInt(1, applicationId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new DatabaseException("Application not found.");
                        }
                        petId = rs.getInt(1);
                    }
                }
                run(con, "UPDATE applications SET status = 'APPROVED' WHERE application_id = ?", applicationId);
                run(con, "UPDATE pets SET status = 'ADOPTED' WHERE pet_id = ?", petId);
                run(con, "UPDATE applications SET status = 'REJECTED' WHERE pet_id = ? AND application_id <> ? AND status = 'PENDING'",
                        petId, applicationId);
                con.commit();
            } catch (SQLException | DatabaseException e) {
                con.rollback();
                if (e instanceof DatabaseException) {
                    throw (DatabaseException) e;
                }
                throw new DatabaseException("Could not approve the application: " + e.getMessage(), e);
            } finally {
                con.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Transaction failed: " + e.getMessage(), e);
        }
    }

    private static void run(Connection con, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            ps.executeUpdate();
        }
    }
}
