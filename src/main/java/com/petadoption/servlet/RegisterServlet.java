package com.petadoption.servlet;

import com.petadoption.exception.AppException;
import com.petadoption.service.AuthService;
import com.petadoption.service.RegistrationRequest;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forward(req, resp, "register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RegistrationRequest r = new RegistrationRequest();
        r.setName(param(req, "name"));
        r.setEmail(param(req, "email"));
        r.setPassword(req.getParameter("password"));
        r.setRole(param(req, "role"));
        r.setShelterName(param(req, "shelterName"));
        r.setCity(param(req, "city"));
        r.setPhone(param(req, "phone"));
        r.setAddress(param(req, "address"));
        try {
            if (!r.getPassword().equals(req.getParameter("confirmPassword"))) {
                throw new com.petadoption.exception.ValidationException("Passwords do not match.");
            }
            authService.register(r, false); // public sign-up can never create an admin
            flash(req, "success", "Account created. You can log in now.");
            redirect(req, resp, "/login");
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            forward(req, resp, "register.jsp");
        } catch (NullPointerException e) {
            req.setAttribute("error", "Please fill in all fields.");
            forward(req, resp, "register.jsp");
        }
    }
}
