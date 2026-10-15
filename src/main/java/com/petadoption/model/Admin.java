package com.petadoption.model;

public class Admin extends User {
    public Admin() {
        super(Role.ADMIN);
    }

    @Override
    public String getDashboardPath() {
        return "/app/admin";
    }
}
