package com.petadoption.servlet;

import com.petadoption.dao.ApplicationDao;
import com.petadoption.dao.PetDao;
import com.petadoption.exception.AppException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.Pet;
import com.petadoption.model.User;
import com.petadoption.service.AdoptionService;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Adopter dashboard: my applications + the "apply for a pet" form. */
@WebServlet(name = "AdopterServlet", urlPatterns = "/app/adopter")
public class AdopterServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient ApplicationDao applicationDao = new ApplicationDao();
    private final transient PetDao petDao = new PetDao();
    private final transient AdoptionService adoptionService = new AdoptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String tab = param(req, "tab").isEmpty() ? "applications" : param(req, "tab");
        try {
            List<AdoptionApplication> apps = applicationDao.findByAdopter(user.getId());
            long pending = apps.stream().filter(a -> a.getStatus() == ApprovalStatus.PENDING).count();
            long approved = apps.stream().filter(a -> a.getStatus() == ApprovalStatus.APPROVED).count();
            req.setAttribute("applications", apps);
            req.setAttribute("totalApps", apps.size());
            req.setAttribute("pendingApps", pending);
            req.setAttribute("approvedApps", approved);

            if ("apply".equals(tab)) {
                Pet pet = petDao.findById(intParam(req, "petId"));
                if (pet == null || pet.getStatus() != ApprovalStatus.APPROVED) {
                    flash(req, "error", "That pet is not available for adoption.");
                    redirect(req, resp, "/pets");
                    return;
                }
                req.setAttribute("pet", pet);
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
