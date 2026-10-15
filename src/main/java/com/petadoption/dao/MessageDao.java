package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;
import com.petadoption.model.Message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MessageDao extends AbstractDao<Message, Integer> {

    @Override
    protected String tableName() {
        return "messages";
    }

    @Override
    protected String idColumn() {
        return "message_id";
    }

    @Override
    protected String baseSelect() {
        return "SELECT messages.*, s.name AS sender_name, r.name AS receiver_name, pets.name AS pet_name "
                + "FROM messages "
                + "JOIN users s ON messages.sender_id = s.user_id "
                + "JOIN users r ON messages.receiver_id = r.user_id "
                + "LEFT JOIN pets ON messages.pet_id = pets.pet_id";
    }

    @Override
    protected String defaultOrder() {
        return "messages.message_id DESC";
    }

    @Override
    protected Message mapRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setMessageId(rs.getInt("message_id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        int petId = rs.getInt("pet_id");
        m.setPetId(rs.wasNull() ? null : petId);
        m.setContent(rs.getString("content"));
        m.setSentAt(rs.getTimestamp("sent_at"));
        m.setSenderName(rs.getString("sender_name"));
        m.setReceiverName(rs.getString("receiver_name"));
        m.setPetName(rs.getString("pet_name"));
        return m;
    }

    @Override
    public int save(Message m) throws DatabaseException {
        int id = insert("INSERT INTO messages (sender_id, receiver_id, pet_id, content) VALUES (?, ?, ?, ?)",
                m.getSenderId(), m.getReceiverId(), m.getPetId(), m.getContent());
        m.setMessageId(id);
        return id;
    }

    /** Messages are immutable once sent. */
    @Override
    public boolean update(Message m) {
        throw new UnsupportedOperationException("Messages cannot be edited after they are sent.");
    }

    public List<Message> findForUser(int userId) throws DatabaseException {
        return query(baseSelect() + " WHERE messages.sender_id = ? OR messages.receiver_id = ? ORDER BY " + defaultOrder(),
                this::mapRow, userId, userId);
    }
}
