package com.petadoption.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.Message;
import com.petadoption.model.Pet;
import com.petadoption.model.Role;
import com.petadoption.model.Shelter;
import com.petadoption.model.User;
import com.petadoption.service.AuthService;
import com.petadoption.service.RegistrationRequest;
import com.petadoption.util.SchemaInitializer;

import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration tests for the DAO layer. They run against an in-memory H2 database
 * (see src/test/resources/db.properties) that SchemaInitializer fills with the demo data.
 */
public class DaoIntegrationTest {

    private static final UserDao USERS = new UserDao();
    private static final ShelterDao SHELTERS = new ShelterDao();
    private static final PetDao PETS = new PetDao();
    private static final ApplicationDao APPLICATIONS = new ApplicationDao();
    private static final MessageDao MESSAGES = new MessageDao();
    private static final SettingsDao SETTINGS = new SettingsDao();

    @BeforeClass
    public static void createSchemaAndDemoData() throws Exception {
        SchemaInitializer.init();
    }

    // ---------- helpers: every test creates its own data so tests do not depend on each other ----------

    private static User newAdopter(String label) throws Exception {
        RegistrationRequest r = new RegistrationRequest();
        r.setName("Test " + label);
        r.setEmail(label + System.nanoTime() + "@test.com");
        r.setPassword("Secret@123");
        r.setRole("ADOPTER");
        return new AuthService().register(r, false);
    }

    private static Shelter demoShelter() throws Exception {
        User shelterUser = USERS.findByEmail("shelter@petadopt.com");
        return SHELTERS.findByUserId(shelterUser.getId());
    }

    private static Pet newPet(String name, String species, ApprovalStatus status) throws Exception {
        Pet p = new Pet();
        p.setShelterId(demoShelter().getShelterId());
        p.setName(name);
        p.setSpecies(species);
        p.setBreed("Mixed");
        p.setAge(2);
        p.setDescription("Test pet");
        p.setStatus(status);
        PETS.save(p);
        return p;
    }

    private static AdoptionApplication newApplication(Pet pet, User adopter) throws Exception {
        AdoptionApplication a = new AdoptionApplication();
        a.setPetId(pet.getPetId());
        a.setAdopterId(adopter.getId());
        a.setFullName("Test Adopter");
        a.setPhone("9876543210");
        a.setAddress("1 Test Street");
        a.setReason("I love animals");
        a.setStatus(ApprovalStatus.PENDING);
        APPLICATIONS.save(a);
        return a;
    }

    // ---------- UserDao ----------

    @Test
    public void userDao_findByEmail_isCaseInsensitive() throws Exception {
        User user = USERS.findByEmail("ADMIN@PetAdopt.com");
        assertNotNull(user);
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    public void userDao_findByEmail_unknownEmailReturnsNull() throws Exception {
        assertNull(USERS.findByEmail("nobody@nowhere.com"));
    }

    @Test
    public void userDao_save_thenFindById_returnsSameUser() throws Exception {
        User saved = newAdopter("findbyid");
        User loaded = USERS.findById(saved.getId());
        assertNotNull(loaded);
        assertEquals(saved.getEmail(), loaded.getEmail());
        assertEquals(Role.ADOPTER, loaded.getRole());
    }

    @Test
    public void userDao_storesHashedPasswordNotPlainText() throws Exception {
        User saved = newAdopter("hashcheck");
        User loaded = USERS.findById(saved.getId());
        assertNotNull(loaded.getPasswordHash());
        assertFalse(loaded.getPasswordHash().contains("Secret@123"));
    }

    @Test
    public void userDao_delete_removesUser() throws Exception {
        User saved = newAdopter("deleteme");
        assertTrue(USERS.delete(saved.getId()));
        assertNull(USERS.findById(saved.getId()));
    }

    @Test
    public void userDao_updateRole_changesRole() throws Exception {
        User saved = newAdopter("rolechange");
        assertTrue(USERS.updateRole(saved.getId(), Role.SHELTER));
        assertEquals(Role.SHELTER, USERS.findById(saved.getId()).getRole());
    }

    // ---------- PetDao ----------

    @Test
    public void petDao_search_returnsOnlyApprovedPets() throws Exception {
        List<String> names = new ArrayList<>();
        for (Pet p : PETS.search("", "")) {
            names.add(p.getName());
            assertEquals(ApprovalStatus.APPROVED, p.getStatus());
        }
        assertTrue(names.contains("Bruno"));
        assertFalse("Pending pet Kiwi must not be public", names.contains("Kiwi"));
    }

    @Test
    public void petDao_search_filtersBySpecies() throws Exception {
        List<Pet> dogs = PETS.search(null, "dog");
        assertFalse(dogs.isEmpty());
        for (Pet p : dogs) {
            assertEquals("dog", p.getSpecies().toLowerCase());
        }
    }

    @Test
    public void petDao_search_filtersByKeyword() throws Exception {
        List<Pet> result = PETS.search("bruno", null);
        assertEquals(1, result.size());
        assertEquals("Bruno", result.get(0).getName());
    }

    @Test
    public void petDao_search_unknownKeywordReturnsEmptyList() throws Exception {
        assertTrue(PETS.search("zzzz-no-such-pet", null).isEmpty());
    }

    @Test
    public void petDao_findApprovedSpecies_excludesSpeciesWithOnlyPendingPets() throws Exception {
        List<String> species = PETS.findApprovedSpecies();
        assertTrue(species.contains("Dog"));
        assertFalse(species.contains("Bird")); // only Kiwi (PENDING) is a bird
    }

    @Test
    public void petDao_updateStatus_approvesPendingPet() throws Exception {
        Pet pet = newPet("Pending Pet", "Turtle", ApprovalStatus.PENDING);
        assertTrue(PETS.updateStatus(pet.getPetId(), ApprovalStatus.APPROVED));
        assertEquals(ApprovalStatus.APPROVED, PETS.findById(pet.getPetId()).getStatus());
    }

    @Test
    public void petDao_findByShelter_includesNewPet() throws Exception {
        Pet pet = newPet("Shelter Pet", "Goat", ApprovalStatus.PENDING);
        boolean found = false;
        for (Pet p : PETS.findByShelter(demoShelter().getShelterId())) {
            found |= p.getPetId() == pet.getPetId();
        }
        assertTrue(found);
    }

    // ---------- ApplicationDao ----------

    @Test
    public void applicationDao_save_thenHasOpenApplication() throws Exception {
        Pet pet = newPet("App Pet", "Hamster", ApprovalStatus.APPROVED);
        User adopter = newAdopter("applicant");
        assertFalse(APPLICATIONS.hasOpenApplication(adopter.getId(), pet.getPetId()));
        newApplication(pet, adopter);
        assertTrue(APPLICATIONS.hasOpenApplication(adopter.getId(), pet.getPetId()));
        assertEquals(1, APPLICATIONS.countPendingByAdopter(adopter.getId()));
    }

    @Test
    public void applicationDao_findByAdopter_returnsJoinedPetName() throws Exception {
        Pet pet = newPet("Joined Pet", "Hamster", ApprovalStatus.APPROVED);
        User adopter = newAdopter("joined");
        newApplication(pet, adopter);
        List<AdoptionApplication> list = APPLICATIONS.findByAdopter(adopter.getId());
        assertEquals(1, list.size());
        assertEquals("Joined Pet", list.get(0).getPetName());
    }

    @Test
    public void applicationDao_findByShelter_includesNewApplication() throws Exception {
        Pet pet = newPet("Shelter App Pet", "Hamster", ApprovalStatus.APPROVED);
        AdoptionApplication app = newApplication(pet, newAdopter("shelterview"));
        boolean found = false;
        for (AdoptionApplication a : APPLICATIONS.findByShelter(demoShelter().getShelterId())) {
            found |= a.getApplicationId() == app.getApplicationId();
        }
        assertTrue(found);
    }

    @Test
    public void applicationDao_approveAndCloseOthers_runsAsOneTransaction() throws Exception {
        Pet pet = newPet("Transaction Pet", "Hamster", ApprovalStatus.APPROVED);
        AdoptionApplication winner = newApplication(pet, newAdopter("winner"));
        AdoptionApplication loser = newApplication(pet, newAdopter("loser"));

        APPLICATIONS.approveAndCloseOthers(winner.getApplicationId());

        assertEquals(ApprovalStatus.APPROVED, APPLICATIONS.findById(winner.getApplicationId()).getStatus());
        assertEquals(ApprovalStatus.REJECTED, APPLICATIONS.findById(loser.getApplicationId()).getStatus());
        assertEquals(ApprovalStatus.ADOPTED, PETS.findById(pet.getPetId()).getStatus());
    }

    @Test
    public void applicationDao_approveAndCloseOthers_unknownApplicationThrows() {
        try {
            APPLICATIONS.approveAndCloseOthers(-1);
            org.junit.Assert.fail("Expected DatabaseException");
        } catch (com.petadoption.exception.DatabaseException e) {
            assertTrue(e.getMessage().contains("not found"));
        }
    }

    // ---------- MessageDao ----------

    @Test
    public void messageDao_save_thenFindForUser() throws Exception {
        User adopter = newAdopter("chat");
        User shelterUser = USERS.findByEmail("shelter@petadopt.com");
        Message m = new Message();
        m.setSenderId(adopter.getId());
        m.setReceiverId(shelterUser.getId());
        m.setPetId(null);
        m.setContent("Is Bruno still available?");
        MESSAGES.save(m);

        List<Message> inbox = MESSAGES.findForUser(shelterUser.getId());
        boolean found = false;
        for (Message msg : inbox) {
            found |= "Is Bruno still available?".equals(msg.getContent());
        }
        assertTrue(found);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void messageDao_update_isNotAllowed() throws Exception {
        MESSAGES.update(new Message());
    }

    // ---------- SettingsDao ----------

    @Test
    public void settingsDao_seededDefaults_exist() throws Exception {
        assertEquals("3", SETTINGS.get("max_pending_applications", null));
        assertEquals("true", SETTINGS.get("applications_open", null));
    }

    @Test
    public void settingsDao_set_insertsThenUpdates() throws Exception {
        SETTINGS.set("test_key", "one");
        assertEquals("one", SETTINGS.get("test_key", null));
        SETTINGS.set("test_key", "two");
        assertEquals("two", SETTINGS.get("test_key", null));
    }

    @Test
    public void settingsDao_setIfAbsent_doesNotOverwrite() throws Exception {
        SETTINGS.set("keep_key", "original");
        SETTINGS.setIfAbsent("keep_key", "changed");
        assertEquals("original", SETTINGS.get("keep_key", null));
    }

    @Test
    public void settingsDao_get_missingKeyReturnsDefault() throws Exception {
        assertEquals("fallback", SETTINGS.get("no_such_key", "fallback"));
    }
}
