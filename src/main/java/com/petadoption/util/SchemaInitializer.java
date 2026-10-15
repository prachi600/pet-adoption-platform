package com.petadoption.util;

import com.petadoption.dao.PetDao;
import com.petadoption.dao.SettingsDao;
import com.petadoption.dao.ShelterDao;
import com.petadoption.dao.UserDao;
import com.petadoption.exception.AppException;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.Shelter;
import com.petadoption.model.User;
import com.petadoption.service.AuthService;
import com.petadoption.service.RegistrationRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Creates the tables from schema.sql on start-up and inserts demo data the first time. */
public final class SchemaInitializer {
    private static final Logger LOG = Logger.getLogger(SchemaInitializer.class.getName());

    private SchemaInitializer() {
    }

    public static void init() throws AppException {
        createTables();
        seedDemoData();
    }

    private static void createTables() throws AppException {
        String script;
        try (InputStream in = SchemaInitializer.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                throw new AppException("schema.sql was not found on the classpath.");
            }
            script = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new AppException("Could not read schema.sql", e);
        }
        try (Connection con = DBConnection.getConnection(); Statement st = con.createStatement()) {
            for (String sql : script.split(";")) {
                if (!sql.trim().isEmpty()) {
                    st.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new AppException("Could not create the database tables: " + e.getMessage(), e);
        }
    }

    private static void seedDemoData() throws AppException {
        SettingsDao settings = new SettingsDao();
        settings.setIfAbsent("site_name", "Pet Adoption Platform");
        settings.setIfAbsent("adoption_fee", "0");
        settings.setIfAbsent("max_pending_applications", "3");
        settings.setIfAbsent("applications_open", "true");

        UserDao userDao = new UserDao();
        if (userDao.countAll() > 0) {
            return;
        }
        AuthService auth = new AuthService();
        auth.register(request("Site Admin", "admin@petadopt.com", "Admin@123", "ADMIN", null), true);
        User shelterUser = auth.register(request("Happy Paws Shelter", "shelter@petadopt.com", "Shelter@123", "SHELTER", "Happy Paws Shelter"), false);
        auth.register(request("Aarav Sharma", "adopter@petadopt.com", "Adopter@123", "ADOPTER", null), false);

        Shelter shelter = new ShelterDao().findByUserId(shelterUser.getId());
        shelter.setCity("New Delhi");
        shelter.setPhone("9876543210");
        shelter.setAddress("12 Green Park Road");
        new ShelterDao().update(shelter);

        PetDao petDao = new PetDao();
        addPet(petDao, shelter, "Bruno", "Dog", "Labrador Retriever", 2, "Friendly and playful. Loves fetch and is great with kids.", ApprovalStatus.APPROVED);
        addPet(petDao, shelter, "Misty", "Cat", "Persian", 1, "Calm lap cat who enjoys sunny windows.", ApprovalStatus.APPROVED);
        addPet(petDao, shelter, "Coco", "Rabbit", "Holland Lop", 1, "Curious and gentle. Litter trained.", ApprovalStatus.APPROVED);
        addPet(petDao, shelter, "Rocky", "Dog", "Indian Pariah", 3, "Smart, loyal and vaccinated. Needs a home with a yard.", ApprovalStatus.APPROVED);
        addPet(petDao, shelter, "Kiwi", "Bird", "Budgerigar", 1, "Chatty little budgie waiting for approval.", ApprovalStatus.PENDING);
        LOG.info("Demo data created. Log in with admin@petadopt.com / Admin@123");
    }

    private static RegistrationRequest request(String name, String email, String password, String role, String shelterName) {
        RegistrationRequest r = new RegistrationRequest();
        r.setName(name);
        r.setEmail(email);
        r.setPassword(password);
        r.setRole(role);
        r.setShelterName(shelterName);
        return r;
    }

    private static void addPet(PetDao dao, Shelter shelter, String name, String species, String breed, int age,
                               String description, ApprovalStatus status) throws AppException {
        Pet p = new Pet();
        p.setShelterId(shelter.getShelterId());
        p.setName(name);
        p.setSpecies(species);
        p.setBreed(breed);
        p.setAge(age);
        p.setDescription(description);
        p.setStatus(status);
        dao.save(p);
    }
}
