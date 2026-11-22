package com.petadoption.service;

import com.petadoption.dao.ApplicationDao;
import com.petadoption.dao.PetDao;
import com.petadoption.dao.SettingsDao;
import com.petadoption.dao.ShelterDao;
import com.petadoption.dao.UserDao;
import com.petadoption.exception.AppException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.Role;
import com.petadoption.model.Shelter;
import com.petadoption.model.User;
import com.petadoption.util.Validator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Operations only the administrator may perform: user management and system settings. */
public class AdminService {
    private final UserDao userDao = new UserDao();
    private final ShelterDao shelterDao = new ShelterDao();
    private final SettingsDao settingsDao = new SettingsDao();
    private final PetDao petDao = new PetDao();
    private final ApplicationDao applicationDao = new ApplicationDao();
    private final AuthService authService = new AuthService();

    // ---------------------------------------------------- dashboard (read side)

    /** Headline numbers for the admin dashboard. */
    public Map<String, Long> dashboardStats() throws AppException {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("users", userDao.countAll());
        stats.put("pendingPets", petDao.countByStatus(ApprovalStatus.PENDING));
        stats.put("pendingApps", applicationDao.countByStatus(ApprovalStatus.PENDING));
        stats.put("adoptedPets", petDao.countByStatus(ApprovalStatus.ADOPTED));
        return stats;
    }

    public List<User> listUsers() throws AppException {
        return userDao.findAll();
    }

    public List<Pet> listAllPets() throws AppException {
        return petDao.findAll();
    }

    /** All applications split into "pending" and "processed". */
    public Map<String, List<AdoptionApplication>> applicationsByState() throws AppException {
        List<AdoptionApplication> all = applicationDao.findAll();
        Map<String, List<AdoptionApplication>> result = new LinkedHashMap<>();
        result.put("pending", all.stream().filter(AdoptionApplication::isPending).collect(Collectors.toList()));
        result.put("processed", all.stream().filter(a -> !a.isPending()).collect(Collectors.toList()));
        return result;
    }

    public Map<String, String> getSettings() throws AppException {
        return settingsDao.getAll();
    }

    public User createUser(RegistrationRequest request) throws AppException {
        return authService.register(request, true);
    }

    public void updateUser(User actor, int userId, String name, String email, String role, String status)
            throws AppException {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new ValidationException("User not found.");
        }
        Role newRole;
        try {
            newRole = Role.valueOf(role == null ? "" : role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid role.");
        }
        if (!"ACTIVE".equals(status) && !"SUSPENDED".equals(status)) {
            throw new ValidationException("Invalid status.");
        }
        if (actor.getId() == userId && (newRole != user.getRole() || !"ACTIVE".equals(status))) {
            throw new ValidationException("You cannot change your own role or suspend your own account.");
        }
        String newEmail = Validator.requireEmail(email);
        User other = userDao.findByEmail(newEmail);
        if (other != null && other.getId() != userId) {
            throw new ValidationException("Another account already uses this email.");
        }
        user.setName(Validator.requireText("Name", name, 2, 100));
        user.setEmail(newEmail);
        user.setStatus(status);
        userDao.update(user);

        if (newRole != user.getRole()) {
            userDao.updateRole(userId, newRole);
            if (newRole == Role.SHELTER && shelterDao.findByUserId(userId) == null) {
                Shelter s = new Shelter();
                s.setUserId(userId);
                s.setShelterName(user.getName());
                shelterDao.save(s);
            }
        }
    }

    public void deleteUser(User actor, int userId) throws AppException {
        if (actor.getId() == userId) {
            throw new ValidationException("You cannot delete your own account.");
        }
        if (!userDao.delete(userId)) {
            throw new ValidationException("User not found.");
        }
    }

    public void updateSettings(Map<String, String> input) throws AppException {
        String siteName = Validator.requireText("Site name", input.get("site_name"), 2, 80);
        String fee = input.get("adoption_fee") == null ? "" : input.get("adoption_fee").trim();
        try {
            if (Double.parseDouble(fee) < 0) {
                throw new ValidationException("Adoption fee cannot be negative.");
            }
        } catch (NumberFormatException e) {
            throw new ValidationException("Adoption fee must be a number.");
        }
        int max = Validator.requireInt("Max pending applications", input.get("max_pending_applications"), 1, 20);
        boolean open = "on".equals(input.get("applications_open")) || "true".equals(input.get("applications_open"));

        settingsDao.set("site_name", siteName);
        settingsDao.set("adoption_fee", fee);
        settingsDao.set("max_pending_applications", String.valueOf(max));
        settingsDao.set("applications_open", String.valueOf(open));
    }
}
