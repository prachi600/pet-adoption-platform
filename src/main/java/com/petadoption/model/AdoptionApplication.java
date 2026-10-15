package com.petadoption.model;

import java.sql.Timestamp;

public class AdoptionApplication implements Approvable {
    private int applicationId;
    private int petId;
    private int adopterId;
    private String fullName;
    private String phone;
    private String address;
    private String reason;
    private ApprovalStatus status = ApprovalStatus.PENDING;
    private Timestamp appliedAt;

    // read-only values filled in by JOINs
    private String petName;
    private int petShelterId;
    private String adopterName;
    private String adopterEmail;

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }
    public int getAdopterId() { return adopterId; }
    public void setAdopterId(int adopterId) { this.adopterId = adopterId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    @Override public ApprovalStatus getStatus() { return status; }
    @Override public void setStatus(ApprovalStatus status) { this.status = status; }
    public Timestamp getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Timestamp appliedAt) { this.appliedAt = appliedAt; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public int getPetShelterId() { return petShelterId; }
    public void setPetShelterId(int petShelterId) { this.petShelterId = petShelterId; }
    public String getAdopterName() { return adopterName; }
    public void setAdopterName(String adopterName) { this.adopterName = adopterName; }
    public String getAdopterEmail() { return adopterEmail; }
    public void setAdopterEmail(String adopterEmail) { this.adopterEmail = adopterEmail; }
}
