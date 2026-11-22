package com.petadoption.service;

import com.petadoption.dao.PetDao;
import com.petadoption.exception.AppException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.Pet;

import java.util.List;

/** Read-side business rules for pets: what the public may see and what can be adopted. */
public class PetService {
    private final PetDao petDao = new PetDao();

    /** A pet whose detail page may be shown to everyone (APPROVED or already ADOPTED). */
    public Pet getPublicPet(int petId) throws AppException {
        Pet pet = petDao.findById(petId);
        if (pet == null || !(pet.getStatus() == ApprovalStatus.APPROVED || pet.getStatus() == ApprovalStatus.ADOPTED)) {
            throw new ValidationException("That pet could not be found.");
        }
        return pet;
    }

    /** A pet that can still receive applications (APPROVED only). */
    public Pet getAdoptablePet(int petId) throws AppException {
        Pet pet = petDao.findById(petId);
        if (pet == null || pet.getStatus() != ApprovalStatus.APPROVED) {
            throw new ValidationException("That pet is not available for adoption.");
        }
        return pet;
    }

    /** Public search: only approved pets, optional keyword and species filters. */
    public List<Pet> search(String keyword, String species) throws AppException {
        return petDao.search(keyword, species);
    }

    public List<String> approvedSpecies() throws AppException {
        return petDao.findApprovedSpecies();
    }

    public List<Pet> findByShelter(int shelterId) throws AppException {
        return petDao.findByShelter(shelterId);
    }
}
