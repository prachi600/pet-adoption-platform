package com.petadoption.model;

/** Factory that returns the right User subclass for a Role. */
public final class UserFactory {
    private UserFactory() {
    }

    public static User create(Role role) {
        switch (role) {
            case ADMIN:
                return new Admin();
            case SHELTER:
                return new ShelterUser();
            default:
                return new Adopter();
        }
    }
}
