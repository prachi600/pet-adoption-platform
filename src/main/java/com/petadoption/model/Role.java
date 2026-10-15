package com.petadoption.model;

public enum Role {
    ADMIN("Administrator"), SHELTER("Shelter"), ADOPTER("Adopter");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
