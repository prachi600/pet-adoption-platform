package com.petadoption.model;

import java.sql.Timestamp;

public class Pet implements Approvable {
    private int petId;
    private int shelterId;
    private String name;
    private String species;
    private String breed;
    private int age;
    private String description;
    private ApprovalStatus status = ApprovalStatus.PENDING;
    private Timestamp createdAt;

    // read-only values filled in by the JOIN with the shelters table
    private String shelterName;
    private String shelterCity;
    private int shelterUserId;

    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }
    public int getShelterId() { return shelterId; }
    public void setShelterId(int shelterId) { this.shelterId = shelterId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    @Override public ApprovalStatus getStatus() { return status; }
    @Override public void setStatus(ApprovalStatus status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }
    public String getShelterCity() { return shelterCity; }
    public void setShelterCity(String shelterCity) { this.shelterCity = shelterCity; }
    public int getShelterUserId() { return shelterUserId; }
    public void setShelterUserId(int shelterUserId) { this.shelterUserId = shelterUserId; }
}
