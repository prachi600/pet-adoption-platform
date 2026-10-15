package com.petadoption.filter;

import com.petadoption.model.User;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Protects everything under /app/*: the visitor must be logged in, must hold the right role for
 * the section, and every POST must carry the session's CSRF token.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/app/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath();
        boolean allowed;
        if (path.startsWith("/app/admin")) {
            allowed = user.isAdmin();
        } else if (path.startsWith("/app/shelter")) {
            allowed = user.isShelter();
        } else if (path.startsWith("/app/adopter")) {
            allowed = user.isAdopter();
        } else if (path.startsWith("/app/messages")) {
            allowed = user.isShelter() || user.isAdopter();
        } else {
            allowed = false;
        }
        if (!allowed) {
            deny(req, resp, 403, "You do not have permission to open this page.");
            return;
        }

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String token = req.getParameter("csrf");
            if (token == null || !token.equals(session.getAttribute("csrf"))) {
                deny(req, resp, 400, "Your session token is invalid or expired. Please go back and try again.");
                return;
            }
        }
        resp.setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }

    private void deny(HttpServletRequest req, HttpServletResponse resp, int status, String message)
            throws ServletException, IOException {
        resp.setStatus(status);
        req.setAttribute("message", message);
        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
    }
}
