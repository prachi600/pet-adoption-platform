package com.petadoption.listener;

import com.petadoption.exception.AppException;
import com.petadoption.service.NotificationService;
import com.petadoption.util.SchemaInitializer;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Creates the database tables on start-up and stops the background threads on shutdown. */
@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            SchemaInitializer.init();
            LOG.info("Database is ready.");
        } catch (AppException e) {
            LOG.log(Level.SEVERE, "Database initialisation failed", e);
            throw new IllegalStateException("Database initialisation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        NotificationService.getInstance().shutdown();
    }
}
