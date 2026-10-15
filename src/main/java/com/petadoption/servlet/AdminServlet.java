package com.petadoption.servlet;

import com.petadoption.dao.ApplicationDao;
import com.petadoption.dao.PetDao;
import com.petadoption.dao.SettingsDao;
import com.petadoption.dao.UserDao;
import com.petadoption.exception.AppException;
import com.petadoption.model.AdoptionApplication;
import com.petadoption.model.ApprovalStatus;
import com.petadoption.model.User;
import com.petadoption.service.AdminService;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.NotificationService;
import com.petadoption.service.RegistrationRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Admin dashboard: users, pet listing approval, application tracking and system settings. */
@WebServlet(name = "AdminServlet", urlPatterns = "/app/admin")
public class AdminServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient UserDao userDao = new UserDao();
    private final transient PetDao petDao = new PetDao();
    private final transient ApplicationDao applicationDao = new ApplicationDao();
    private final transient SettingsDao settingsDao = new SettingsDao();
    private final transient AdminService adminService = new AdminService();
    private final transient AdoptionService adoptionService = new AdoptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String tab = param(req, "tab").isEmpty() ? "users" : param(req, "tab");
        try {
            req.setAttribute("statUsers", userDao.countAll());
            req.setAttribute("statPendingPets", petDao.countByStatus(ApprovalStatus.PENDING));
            req.setAttribute("statPendingApps", applicationDao.countByStatus(ApprovalStatus.PENDING));
            req.setAttribute("statAdopted", petDao.countByStatus(ApprovalStatus.ADOPTED));

            switch (tab) {
                case "pets":
                    req.setAttribute("pets", petDao.findAll());
                    break;
                case "applications":
                    List<AdoptionApplication> all = applicationDao.findAll();
                    req.setAttribute("pendingList", all.stream().filter(AdoptionApplication::isPending).collect(Collectors.toList()));
                    req.setAttribute("processedList", all.stream().filter(a -> !a.isPending()).collect(Collectors.toList()));
                    break;
                case "settings":
                    req.setAttribute("settings", settingsDao.getAll());
                    req.setAttribute("notifications", NotificationService.getInstance().getRecent());
                    break;
                default:
                    tab = "users";
                    req.setAttribute("users", userDao.findAll());
            }
            req.setAttribute("tab", tab);
            forward(req, resp, "admin.jsp");
        } catch (AppException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User admin = currentUser(req);
        String action = param(req, "action");
        String tab = "users";
        try {
            switch (action) {
                case "createUser":
                    RegistrationRequest r = new RegistrationRequest();
                    r.setName(param(req, "name"));
                    r.setEmail(param(req, "email"));
                    r.setPassword(req.getParameter("password"));
                    r.setRole(param(req, "role"));
                    r.setShelterName(param(req, "name"));
                    adminService.createUser(r);
                    flash(req, "success", "User created successfully.");
                    break;
                case "updateUser":
                    adminService.updateUser(admin, intParam(req, "userId"), param(req, "name"), param(req, "email"),
                            param(req, "role"), param(req, "status"));
                    flash(req, "success", "User updated successfully.");
                    break;
                case "deleteUser":
                    adminService.deleteUser(admin, intParam(req, "userId"));
                    flash(req, "success", "User deleted successfully.");
                    break;
                case "reviewPet":
                    tab = "pets";
                    boolean approve = "approve".equals(param(req, "decision"));
                    adoptionService.reviewPet(intParam(req, "petId"), approve);
                    flash(req, "success", approve ? "Pet listing approved." : "Pet listing rejected.");
                    break;
                case "updateSettings":
                    tab = "settings";
                    Map<String, String> input = new HashMap<>();
                    input.put("site_name", param(req, "site_name"));
                    input.put("adoption_fee", param(req, "adoption_fee"));
                    input.put("max_pending_applications", param(req, "max_pending_applications"));
                    input.put("applications_open", param(req, "applications_open"));
                    adminService.updateSettings(input);
                    flash(req, "success", "Settings updated successfully.");
                    break;
                default:
                    flash(req, "error", "Unknown action.");
            }
        } catch (AppException e) {
            flash(req, "error", e.getMessage());
        }
        redirect(req, resp, "/app/admin?tab=" + tab);
    }
}
