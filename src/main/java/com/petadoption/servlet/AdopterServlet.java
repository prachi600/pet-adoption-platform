package com.petadoption.servlet;

import com.petadoption.exception.AppException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.User;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.PetService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Adopter dashboard: my applications + the "apply for a pet" form. */
@WebServlet(name = "AdopterServlet", urlPatterns = "/app/adopter")
public class AdopterServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient PetService petService = new PetService();
    private final transient AdoptionService adoptionService = new AdoptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String tab = param(req, "tab").isEmpty() ? "applications" : param(req, "tab");
        try {
            List<AdoptionApplication> apps = adoptionService.applicationsForAdopter(user.getId());
            Map<String, Long> stats = adoptionService.applicationStatistics(apps);
            req.setAttribute("applications", apps);
            req.setAttribute("totalApps", stats.get("TOTAL"));
            req.setAttribute("pendingApps", stats.get("PENDING"));
            req.setAttribute("approvedApps", stats.get("APPROVED"));

            if ("apply".equals(tab)) {
                req.setAttribute("pet", petService.getAdoptablePet(intParam(req, "petId")));
            }
            req.setAttribute("tab", tab);
            forward(req, resp, "adopter.jsp");
        } catch (AppException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, "/pets");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String petId = param(req, "petId");
        try {
            if ("apply".equals(param(req, "action"))) {
                adoptionService.submitApplication(user, intParam(req, "petId"), param(req, "fullName"),
                        param(req, "phone"), param(req, "address"), param(req, "reason"));
                flash(req, "success", "Your application was submitted. The shelter will review it soon.");
            }
            redirect(req, resp, "/app/adopter?tab=applications");
        } catch (AppException e) {
            flash(req, "error", e.getMessage());
            redirect(req, resp, petId.isEmpty() ? "/app/adopter" : "/app/adopter?tab=apply&petId=" + petId);
        }
    }
}
