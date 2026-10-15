package com.petadoption.service;

import com.petadoption.dao.ApplicationDao;
import com.petadoption.dao.MessageDao;
import com.petadoption.dao.PetDao;
import com.petadoption.dao.SettingsDao;
import com.petadoption.dao.ShelterDao;
import com.petadoption.dao.UserDao;
import com.petadoption.exception.AppException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.Message;
import com.petadoption.model.Pet;
import com.petadoption.model.Shelter;
import com.petadoption.model.User;
import com.petadoption.util.Validator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Business rules for pets, adoption applications and messages. */
public class AdoptionService {
    /** Guards check-then-write sequences so two requests cannot adopt the same pet at once. */
    private static final Object LOCK = new Object();

    private final PetDao petDao = new PetDao();
    private final ApplicationDao applicationDao = new ApplicationDao();
    private final MessageDao messageDao = new MessageDao();
    private final UserDao userDao = new UserDao();
    private final ShelterDao shelterDao = new ShelterDao();
    private final SettingsDao settingsDao = new SettingsDao();
    private final NotificationService notifier = NotificationService.getInstance();

    // ------------------------------------------------------------------ pets

    public Pet addPet(int shelterId, String name, String species, String breed, String age, String description)
            throws AppException {
        Pet pet = new Pet();
        pet.setShelterId(shelterId);
        pet.setName(Validator.requireText("Pet name", name, 2, 80));
        pet.setSpecies(Validator.requireText("Species", species, 2, 30));
        pet.setBreed(Validator.optionalText("Breed", breed, 80));
        pet.setAge(Validator.requireInt("Age (years)", age, 0, 40));
        pet.setDescription(Validator.optionalText("Description", description, 1000));
        pet.setStatus(ApprovalStatus.PENDING);
        petDao.save(pet);
        return pet;
    }

    /** Admin decision on a pending pet listing. */
    public void reviewPet(int petId, boolean approve) throws AppException {
        Pet pet = petDao.findById(petId);
        if (pet == null) {
            throw new ValidationException("Pet listing not found.");
        }
        if (pet.getStatus() == ApprovalStatus.ADOPTED) {
            throw new ValidationException("This pet has already been adopted.");
        }
        if (approve) {
            pet.approve();
        } else {
            pet.reject();
        }
        petDao.updateStatus(petId, pet.getStatus());

        Shelter shelter = shelterDao.findById(pet.getShelterId());
        User owner = shelter == null ? null : userDao.findById(shelter.getUserId());
        if (owner != null) {
            notifier.send(owner.getEmail(), "Listing " + pet.getStatus().name().toLowerCase() + ": " + pet.getName(),
                    "Your pet listing was " + pet.getStatus().name().toLowerCase() + " by the administrator.");
        }
    }

    /** Counts of a shelter's pets per status (keys are the status names). */
    public Map<String, Integer> petStatistics(List<Pet> pets) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("TOTAL", pets.size());
        for (ApprovalStatus s : ApprovalStatus.values()) {
            stats.put(s.name(), 0);
        }
        for (Pet p : pets) {
            stats.merge(p.getStatus().name(), 1, Integer::sum);
        }
        return stats;
    }

    // ---------------------------------------------------------- applications

    public AdoptionApplication submitApplication(User adopter, int petId, String fullName, String phone,
                                                 String address, String reason) throws AppException {
        AdoptionApplication app = new AdoptionApplication();
        app.setAdopterId(adopter.getId());
        app.setPetId(petId);
        app.setFullName(Validator.requireText("Full name", fullName, 2, 100));
        app.setPhone(Validator.requirePhone("Phone", phone));
        app.setAddress(Validator.requireText("Address", address, 5, 255));
        app.setReason(Validator.requireText("Reason for adopting", reason, 10, 1000));
        app.setStatus(ApprovalStatus.PENDING);

        Pet pet;
        synchronized (LOCK) {
            if (!Boolean.parseBoolean(settingsDao.get("applications_open", "true"))) {
                throw new ValidationException("Adoption applications are currently closed.");
            }
            pet = petDao.findById(petId);
            if (pet == null || pet.getStatus() != ApprovalStatus.APPROVED) {
                throw new ValidationException("This pet is not available for adoption.");
            }
            if (applicationDao.hasOpenApplication(adopter.getId(), petId)) {
                throw new ValidationException("You have already applied for this pet.");
            }
            int max = Integer.parseInt(settingsDao.get("max_pending_applications", "3"));
            if (applicationDao.countPendingByAdopter(adopter.getId()) >= max) {
                throw new ValidationException("You can have at most " + max + " pending applications at a time.");
            }
            applicationDao.save(app);
        }

        User shelterOwner = userDao.findById(pet.getShelterUserId());
        if (shelterOwner != null) {
            notifier.send(shelterOwner.getEmail(), "New adoption application for " + pet.getName(),
                    app.getFullName() + " has applied to adopt " + pet.getName() + ".");
        }
        return app;
    }

    /** Shelter decision on an application that belongs to one of its pets. */
    public void decideApplication(int shelterId, int applicationId, boolean approve) throws AppException {
        AdoptionApplication app;
        synchronized (LOCK) {
            app = applicationDao.findById(applicationId);
            if (app == null || app.getPetShelterId() != shelterId) {
                throw new ValidationException("Application not found.");
            }
            if (!app.isPending()) {
                throw new ValidationException("This application has already been processed.");
            }
            if (approve) {
                Pet pet = petDao.findById(app.getPetId());
                if (pet == null || pet.getStatus() != ApprovalStatus.APPROVED) {
                    throw new ValidationException("This pet is no longer available.");
                }
                applicationDao.approveAndCloseOthers(applicationId);
            } else {
                applicationDao.updateStatus(applicationId, ApprovalStatus.REJECTED);
            }
        }
        String verdict = approve ? "approved" : "rejected";
        notifier.send(app.getAdopterEmail(), "Your application for " + app.getPetName() + " was " + verdict,
                "The shelter has " + verdict + " your adoption application.");
    }

    // -------------------------------------------------------------- messages

    public void sendMessage(User sender, int receiverId, Integer petId, String content) throws AppException {
        String text = Validator.requireText("Message", content, 1, 1000);
        User receiver = userDao.findById(receiverId);
        if (receiver == null || !receiver.isActive()) {
            throw new ValidationException("Recipient not found.");
        }
        boolean allowed = (sender.isAdopter() && receiver.isShelter()) || (sender.isShelter() && receiver.isAdopter());
        if (!allowed) {
            throw new ValidationException("Messages can only be exchanged between adopters and shelters.");
        }
        if (petId != null && petDao.findById(petId) == null) {
            petId = null;
        }
        Message m = new Message();
        m.setSenderId(sender.getId());
        m.setReceiverId(receiverId);
        m.setPetId(petId);
        m.setContent(text);
        messageDao.save(m);
        notifier.send(receiver.getEmail(), "New message from " + sender.getName(), text);
    }
}
