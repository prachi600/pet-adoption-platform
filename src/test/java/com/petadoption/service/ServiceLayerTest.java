package com.petadoption.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.petadoption.exception.ValidationException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.ShelterUser;
import com.petadoption.model.User;
import com.petadoption.util.SchemaInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for the Service layer (business rules) on the in-memory H2 database. */
public class ServiceLayerTest {
    private static final AuthService AUTH = new AuthService();
    private static final PetService PETS = new PetService();
    private static final AdoptionService ADOPTION = new AdoptionService();
    private static final AdminService ADMIN = new AdminService();
    private static final MessageService MESSAGES = new MessageService();

    @BeforeClass
    public static void setUp() throws Exception {
        SchemaInitializer.init();
    }

    private static RegistrationRequest request(String label) {
        RegistrationRequest r = new RegistrationRequest();
        r.setName("Svc " + label);
        r.setEmail(label + System.nanoTime() + "@test.com");
        r.setPassword("Secret@123");
        r.setRole("ADOPTER");
        return r;
    }

    private static ShelterUser demoShelter() throws Exception {
        return (ShelterUser) AUTH.login("shelter@petadopt.com", "Shelter@123");
    }

    private static void expectMessage(ThrowingRunnable call, String part) {
        try {
            call.run();
            fail("Expected ValidationException containing: " + part);
        } catch (ValidationException e) {
            assertTrue("Unexpected message: " + e.getMessage(), e.getMessage().contains(part));
        } catch (Exception e) {
            fail("Wrong exception: " + e);
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    // ---------------- AuthService.registerPublic ----------------

    @Test
    public void registerPublic_success_createsActiveAdopter() throws Exception {
        User u = AUTH.registerPublic(request("pub"), "Secret@123");
        assertNotNull(u);
        assertTrue(u.isActive());
        assertTrue(u.isAdopter());
    }

    @Test
    public void registerPublic_passwordMismatch_throws() {
        expectMessage(() -> AUTH.registerPublic(request("mismatch"), "different"), "do not match");
    }

    @Test
    public void registerPublic_missingPassword_throws() {
        RegistrationRequest r = request("nopass");
        r.setPassword(null);
        expectMessage(() -> AUTH.registerPublic(r, "x"), "fill in all fields");
    }

    @Test
    public void registerPublic_cannotCreateAdmin() {
        RegistrationRequest r = request("sneaky");
        r.setRole("ADMIN");
        expectMessage(() -> AUTH.registerPublic(r, "Secret@123"), "Admin accounts cannot be created");
    }

    // ---------------- PetService ----------------

    @Test
    public void petService_getPublicPet_approvedPetIsReturned() throws Exception {
        Pet bruno = PETS.search("bruno", null).get(0);
        assertEquals("Bruno", PETS.getPublicPet(bruno.getPetId()).getName());
    }

    @Test
    public void petService_getPublicPet_pendingPetIsHidden() throws Exception {
        Pet pending = ADOPTION.addPet(demoShelter().getShelterId(), "Hidden Pet", "Fish", "", "1", "");
        expectMessage(() -> PETS.getPublicPet(pending.getPetId()), "could not be found");
    }

    @Test
    public void petService_getPublicPet_unknownIdThrows() {
        expectMessage(() -> PETS.getPublicPet(-5), "could not be found");
    }

    @Test
    public void petService_getAdoptablePet_onlyApprovedPets() throws Exception {
        Pet pet = ADOPTION.addPet(demoShelter().getShelterId(), "Maybe Pet", "Fish", "", "1", "");
        expectMessage(() -> PETS.getAdoptablePet(pet.getPetId()), "not available for adoption");
        ADOPTION.reviewPet(pet.getPetId(), true);
        assertEquals("Maybe Pet", PETS.getAdoptablePet(pet.getPetId()).getName());
    }

    @Test
    public void petService_approvedSpecies_excludesPendingOnlySpecies() throws Exception {
        List<String> species = PETS.approvedSpecies();
        assertTrue(species.contains("Dog"));
        assertFalse(species.contains("Bird"));
    }

    // ---------------- AdoptionService statistics ----------------

    @Test
    public void applicationStatistics_countsPerStatus() {
        List<AdoptionApplication> apps = new ArrayList<>();
        for (ApprovalStatus s : new ApprovalStatus[]{ApprovalStatus.PENDING, ApprovalStatus.PENDING, ApprovalStatus.APPROVED}) {
            AdoptionApplication a = new AdoptionApplication();
            a.setStatus(s);
            apps.add(a);
        }
        Map<String, Long> stats = ADOPTION.applicationStatistics(apps);
        assertEquals(3L, (long) stats.get("TOTAL"));
        assertEquals(2L, (long) stats.get("PENDING"));
        assertEquals(1L, (long) stats.get("APPROVED"));
    }

    @Test
    public void applicationStatistics_emptyList_allZero() {
        Map<String, Long> stats = ADOPTION.applicationStatistics(new ArrayList<AdoptionApplication>());
        assertEquals(0L, (long) stats.get("TOTAL"));
        assertEquals(0L, (long) stats.get("PENDING"));
    }

    // ---------------- AdminService read side ----------------

    @Test
    public void adminService_dashboardStats_hasAllCounters() throws Exception {
        Map<String, Long> stats = ADMIN.dashboardStats();
        assertTrue(stats.get("users") >= 3);
        assertTrue(stats.containsKey("pendingPets"));
        assertTrue(stats.containsKey("pendingApps"));
        assertTrue(stats.containsKey("adoptedPets"));
    }

    @Test
    public void adminService_applicationsByState_hasBothLists() throws Exception {
        Map<String, List<AdoptionApplication>> split = ADMIN.applicationsByState();
        assertNotNull(split.get("pending"));
        assertNotNull(split.get("processed"));
    }

    @Test
    public void adminService_getSettings_containsSeededKeys() throws Exception {
        assertTrue(ADMIN.getSettings().containsKey("site_name"));
    }

    // ---------------- MessageService ----------------

    @Test
    public void messageService_contactsFor_addsRequestedAdopterToShelterList() throws Exception {
        User adopter = AUTH.registerPublic(request("contact"), "Secret@123");
        User shelter = demoShelter();
        boolean found = false;
        for (User c : MESSAGES.contactsFor(shelter, adopter.getId())) {
            found |= c.getId() == adopter.getId();
        }
        assertTrue(found);
    }

    @Test
    public void messageService_contactsFor_ignoresInvalidTarget() throws Exception {
        User adopter = AUTH.login("adopter@petadopt.com", "Adopter@123");
        User anotherAdopter = AUTH.registerPublic(request("peer"), "Secret@123");
        for (User c : MESSAGES.contactsFor(adopter, anotherAdopter.getId())) {
            assertFalse("Adopters must not be able to message adopters", c.getId() == anotherAdopter.getId());
        }
    }
}
