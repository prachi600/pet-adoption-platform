package com.petadoption.servlet;

import com.petadoption.dao.SettingsDao;
import com.petadoption.exception.DatabaseException;
import com.petadoption.exception.ValidationException;
import com.petadoption.model.User;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Shared helpers for all servlets (session user, flash messages, forwarding, parameter parsing). */
public abstract class BaseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected static final String VIEWS = "/WEB-INF/views/";

    private final transient SettingsDao settingsDao = new SettingsDao();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String siteName = "Pet Adoption Platform";
        try {
            siteName = settingsDao.get("site_name", siteName);
        } catch (DatabaseException e) {
            log("Could not load site name", e);
        }
        req.setAttribute("siteName", siteName);
        super.service(req, resp);
    }

    protected User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    /** Stores a one-time message that the next page shows (type: "success" or "error"). */
    protected void flash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMsg", message);
    }

    protected void forward(HttpServletRequest req, HttpServletResponse resp, String view) throws ServletException, IOException {
        req.getRequestDispatcher(VIEWS + view).forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    protected int intParam(HttpServletRequest req, String name) throws ValidationException {
        try {
            return Integer.parseInt(param(req, name));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid or missing value for '" + name + "'.");
        }
    }
}
