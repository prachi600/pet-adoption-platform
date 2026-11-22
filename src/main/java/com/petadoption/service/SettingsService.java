package com.petadoption.service;

import com.petadoption.dao.SettingsDao;
import com.petadoption.exception.DatabaseException;

/** Read access to system settings for the web layer. */
public class SettingsService {
    public static final String DEFAULT_SITE_NAME = "Pet Adoption Platform";
    private final SettingsDao settingsDao = new SettingsDao();

    public String getSiteName() throws DatabaseException {
        return settingsDao.get("site_name", DEFAULT_SITE_NAME);
    }
}
