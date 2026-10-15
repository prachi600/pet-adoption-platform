package com.petadoption.dao;

import com.petadoption.exception.DatabaseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Key/value store for the system-wide settings managed by the administrator. */
public class SettingsDao extends BaseDao {

    public Map<String, String> getAll() throws DatabaseException {
        Map<String, String> settings = new LinkedHashMap<>();
        List<String[]> rows = query("SELECT setting_key, setting_value FROM settings ORDER BY setting_key",
                rs -> new String[]{rs.getString(1), rs.getString(2)});
        for (String[] row : rows) {
            settings.put(row[0], row[1]);
        }
        return settings;
    }

    public String get(String key, String defaultValue) throws DatabaseException {
        List<String> rows = query("SELECT setting_value FROM settings WHERE setting_key = ?", rs -> rs.getString(1), key);
        return rows.isEmpty() ? defaultValue : rows.get(0);
    }

    public void set(String key, String value) throws DatabaseException {
        if (update("UPDATE settings SET setting_value = ? WHERE setting_key = ?", value, key) == 0) {
            update("INSERT INTO settings (setting_key, setting_value) VALUES (?, ?)", key, value);
        }
    }

    public void setIfAbsent(String key, String value) throws DatabaseException {
        if (get(key, null) == null) {
            update("INSERT INTO settings (setting_key, setting_value) VALUES (?, ?)", key, value);
        }
    }
}
