package com.petadoption.servlet;

import com.petadoption.exception.AppException;
import com.petadoption.model.User;
import com.petadoption.service.AdoptionService;
import com.petadoption.service.MessageService;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Interaction history + "send message" form shared by adopters and shelters. */
@WebServlet(name = "MessageServlet", urlPatterns = "/app/messages")
public class MessageServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient MessageService messageService = new MessageService();
    private final transient AdoptionService adoptionService = new AdoptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            String to = param(req, "to");
            Integer toId = to.isEmpty() ? null : intParam(req, "to");
            req.setAttribute("messages", messageService.inboxFor(user));
            req.setAttribute("contacts", messageService.contactsFor(user, toId));
            req.setAttribute("selectedTo", to);
            req.setAttribute("petId", param(req, "petId"));
            forward(req, resp, "messages.jsp");
        } catch (AppException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            String petParam = param(req, "petId");
            Integer petId = petParam.isEmpty() ? null : intParam(req, "petId");
            adoptionService.sendMessage(user, intParam(req, "receiverId"), petId, param(req, "content"));
            flash(req, "success", "Message sent.");
        } catch (AppException e) {
            flash(req, "error", e.getMessage());
        }
        redirect(req, resp, "/app/messages");
    }
}
