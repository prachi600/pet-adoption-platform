package com.petadoption.service;

import com.petadoption.dao.MessageDao;
import com.petadoption.dao.UserDao;
import com.petadoption.exception.AppException;
import com.petadoption.model.Message;
import com.petadoption.model.User;

import java.util.List;

/** Business rules for reading messages and choosing who a user may write to. */
public class MessageService {
    private final MessageDao messageDao = new MessageDao();
    private final UserDao userDao = new UserDao();

    public List<Message> inboxFor(User user) throws AppException {
        return messageDao.findForUser(user.getId());
    }

    /**
     * People the user may message. When requestedId is given (e.g. from a pet page or a reply link)
     * and that person is an active adopter/shelter counterpart, they are added to the list.
     */
    public List<User> contactsFor(User user, Integer requestedId) throws AppException {
        List<User> contacts = userDao.findContactsFor(user);
        if (requestedId != null) {
            User target = userDao.findById(requestedId);
            boolean valid = target != null && target.isActive()
                    && ((user.isAdopter() && target.isShelter()) || (user.isShelter() && target.isAdopter()));
            if (valid && contacts.stream().noneMatch(c -> c.getId() == target.getId())) {
                contacts.add(target);
            }
        }
        return contacts;
    }
}
