package com.petadoption.servlet;

import com.petadoption.exception.AppException;
import com.petadoption.model.User;
import com.petadoption.service.AuthService;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        if (user != null) {
            redirect(req, resp, user.getDashboardPath());
            return;
        }
        forward(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = authService.login(param(req, "email"), req.getParameter("password"));
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate(); // new session id after login (prevents session fixation)
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("csrf", UUID.randomUUID().toString());
            session.setMaxInactiveInterval(30 * 60);
            redirect(req, resp, user.getDashboardPath()); // polymorphic call
        } catch (AppException e) {
            req.setAttribute("error", e.getMessage());
            forward(req, resp, "login.jsp");
        }
    }
}
