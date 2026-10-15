package com.petadoption.service;

import com.petadoption.dao.ShelterDao;
import com.petadoption.dao.UserDao;
import com.petadoption.exception.AppException;
import com.petadoption.exception.AuthenticationException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.Role;
import com.petadoption.model.Shelter;
import com.petadoption.model.ShelterUser;
import com.petadoption.model.User;
import com.petadoption.model.UserFactory;
import com.petadoption.util.PasswordUtil;
import com.petadoption.util.Validator;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private final ShelterDao shelterDao = new ShelterDao();

    /**
     * Creates an account. Public sign-up passes allowAdmin=false; the admin panel and the demo
     * seeding pass true.
     */
    public User register(RegistrationRequest r, boolean allowAdmin) throws AppException {
        String name = Validator.requireText("Name", r.getName(), 2, 100);
        String email = Validator.requireEmail(r.getEmail());
        Validator.requirePassword(r.getPassword());

        Role role;
        try {
            role = Role.valueOf(r.getRole() == null ? "" : r.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Please choose a valid account type.");
        }
        if (role == Role.ADMIN && !allowAdmin) {
            throw new ValidationException("Admin accounts cannot be created through public registration.");
        }
        if (userDao.findByEmail(email) != null) {
            throw new ValidationException("An account with this email already exists.");
        }

        Shelter shelter = null;
        if (role == Role.SHELTER) {
            shelter = new Shelter();
            String shelterName = r.getShelterName() == null || r.getShelterName().trim().isEmpty() ? name : r.getShelterName();
            shelter.setShelterName(Validator.requireText("Shelter name", shelterName, 2, 150));
            shelter.setCity(Validator.optionalText("City", r.getCity(), 80));
            String phone = r.getPhone() == null ? "" : r.getPhone().trim();
            shelter.setPhone(phone.isEmpty() ? "" : Validator.requirePhone("Phone", phone));
            shelter.setAddress(Validator.optionalText("Address", r.getAddress(), 255));
        }

        User user = UserFactory.create(role);
        user.setName(name);
        user.setEmail(email);
        user.setSalt(PasswordUtil.newSalt());
        user.setPasswordHash(PasswordUtil.hash(r.getPassword(), user.getSalt()));
        user.setStatus("ACTIVE");
        userDao.save(user);

        if (shelter != null) {
            shelter.setUserId(user.getId());
            shelterDao.save(shelter);
        }
        return user;
    }

    public User login(String email, String password) throws AppException {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Please enter your email and password.");
        }
        User user = userDao.findByEmail(email.trim());
        if (user == null || !PasswordUtil.matches(password, user.getSalt(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password.");
        }
        if (!user.isActive()) {
            throw new AuthenticationException("This account has been suspended. Please contact the administrator.");
        }
        if (user instanceof ShelterUser) {
            Shelter shelter = shelterDao.findByUserId(user.getId());
            if (shelter == null) {
                throw new AuthenticationException("No shelter profile is linked to this account.");
            }
            ((ShelterUser) user).setShelterId(shelter.getShelterId());
            ((ShelterUser) user).setShelterName(shelter.getShelterName());
        }
        return user;
    }
}
