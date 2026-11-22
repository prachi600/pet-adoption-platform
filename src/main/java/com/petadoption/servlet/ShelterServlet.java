package com.petadoption.servlet;

import com.petadoption.exception.AppException;
import com.petadoption.model.Pet;
import com.petadoption.model.ShelterUser;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.PetService;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Shelter dashboard: list pets, track adoption applications and approve / reject them. */
@WebServlet(name = "ShelterServlet", urlPatterns = "/app/shelter")
public class ShelterServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient PetService petService = new PetService();
    private final transient AdoptionService adoptionService = new AdoptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShelterUser shelter = (ShelterUser) currentUser(req);
        String tab = param(req, "tab").isEmpty() ? "pets" : param(req, "tab");
        try {
            List<Pet> pets = petService.findByShelter(shelter.getShelterId());
            req.setAttribute("pets", pets);
            req.setAttribute("stats", adoptionService.petStatistics(pets));
            req.setAttribute("applications", adoptionService.applicationsForShelter(shelter.getShelterId()));
            req.setAttribute("tab", tab);
            forward(req, resp, "shelter.jsp");
        } catch (AppException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShelterUser shelter = (ShelterUser) currentUser(req);
        String action = param(req, "action");
        String tab = "pets";
        try {
            if ("addPet".equals(action)) {
                adoptionService.addPet(shelter.getShelterId(), param(req, "name"), param(req, "species"),
                        param(req, "breed"), param(req, "age"), param(req, "description"));
                flash(req, "success", "Pet listed. It will appear publicly once the administrator approves it.");
            } else if ("decide".equals(action)) {
                tab = "applications";
                boolean approve = "approve".equals(param(req, "decision"));
                adoptionService.decideApplication(shelter.getShelterId(), intParam(req, "applicationId"), approve);
                flash(req, "success", approve ? "Application approved. The pet is now marked as adopted." : "Application rejected.");
            }
        } catch (AppException e) {
            flash(req, "error", e.getMessage());
        }
        redirect(req, resp, "/app/shelter?tab=" + tab);
    }
}
