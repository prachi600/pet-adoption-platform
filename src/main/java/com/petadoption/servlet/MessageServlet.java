package com.petadoption.servlet;

import com.petadoption.dao.MessageDao;
import com.petadoption.dao.UserDao;
import com.petadoption.exception.AppException;
import com.petadoption.model.User;
import com.petadoption.service.AdoptionService;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Interaction history + "send message" form shared by adopters and shelters. */
@WebServlet(name = "MessageServlet", urlPatterns = "/app/messages")
public class MessageServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final transient MessageDao messageDao = new MessageDao();
    private final transient UserDao userDao = new UserDao();
    private final transient AdoptionService adoptionService = new AdoptionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        try {
            req.setAttribute("messages", messageDao.findForUser(user.getId()));
            java.util.List<User> contacts = userDao.findContactsFor(user);
            // allow starting a conversation with a specific person (e.g. from a pet page or a reply link)
            String to = param(req, "to");
            if (!to.isEmpty()) {
                User target = userDao.findById(intParam(req, "to"));
                boolean valid = target != null && target.isActive()
                        && ((user.isAdopter() && target.isShelter()) || (user.isShelter() && target.isAdopter()));
                if (valid && contacts.stream().noneMatch(c -> c.getId() == target.getId())) {
                    contacts.add(target);
                }
            }
            req.setAttribute("contacts", contacts);
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
