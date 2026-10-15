package com.petadoption.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Sends e-mail style notifications on a background thread pool so web requests never wait for
 * them (multithreading). The list of recent notifications is shared between threads and is
 * protected with synchronized methods.
 */
public final class NotificationService {
    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());
    private static final NotificationService INSTANCE = new NotificationService();
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd MMM HH:mm:ss");
    private static final int MAX_RECENT = 25;

    private final ExecutorService pool = Executors.newFixedThreadPool(2, runnable -> {
        Thread t = new Thread(runnable, "notification-worker");
        t.setDaemon(true);
        return t;
    });
    private final Deque<String> recent = new ArrayDeque<>();

    private NotificationService() {
    }

    public static NotificationService getInstance() {
        return INSTANCE;
    }

    /** Queues a notification; returns immediately. */
    public void send(String toEmail, String subject, String body) {
        if (toEmail == null || toEmail.isEmpty()) {
            return;
        }
        try {
            pool.submit(() -> {
                try {
                    Thread.sleep(150); // pretend to talk to a mail server
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                String line = LocalDateTime.now().format(TIME) + "  to " + toEmail + "  -  " + subject;
                LOG.info("Notification sent: " + line + " | " + body);
                record(line);
            });
        } catch (RejectedExecutionException e) {
            LOG.warning("Notification dropped because the service is shutting down: " + subject);
        }
    }

    private synchronized void record(String line) {
        recent.addFirst(line);
        while (recent.size() > MAX_RECENT) {
            recent.removeLast();
        }
    }

    public synchronized List<String> getRecent() {
        return new ArrayList<>(recent);
    }

    public void shutdown() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
