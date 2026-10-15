package com.petadoption.model;

public class ShelterUser extends User {
    private int shelterId;
    private String shelterName;

    public ShelterUser() {
        super(Role.SHELTER);
    }

    @Override
    public String getDashboardPath() {
        return "/app/shelter";
    }

    public int getShelterId() { return shelterId; }
    public void setShelterId(int shelterId) { this.shelterId = shelterId; }
    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }
}
