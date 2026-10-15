package com.petadoption.model;

public class Adopter extends User {
    public Adopter() {
        super(Role.ADOPTER);
    }

    @Override
    public String getDashboardPath() {
        return "/app/adopter";
    }
}
