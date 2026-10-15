package com.petadoption.model;

import java.sql.Timestamp;

/**
 * Abstract parent of every account type. Admin, ShelterUser and Adopter inherit from it and
 * override {@link #getDashboardPath()} (runtime polymorphism).
 */
public abstract class User {
    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String salt;
    private final Role role;
    private String status = "ACTIVE";
    private Timestamp createdAt;

    protected User(Role role) {
        this.role = role;
    }

    /** Context-relative URL of the dashboard this kind of user lands on after login. */
    public abstract String getDashboardPath();

    public String getRoleLabel() {
        return role.getLabel();
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isShelter() {
        return role == Role.SHELTER;
    }

    public boolean isAdopter() {
        return role == Role.ADOPTER;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public Role getRole() { return role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
