package com.unyt.gym.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Prints a small startup banner once the application is fully booted.
 *
 * <p>All cross-cutting concerns at startup (audit-log table creation,
 * EventBus subscriber registration) are centralised in
 * {@link EventListenerRegistrar} so that a reviewer can find every
 * Observer-pattern subscription in a single class.</p>
 */
@Configuration
public class StartupConfig {

    private static final Logger log = LoggerFactory.getLogger(StartupConfig.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("====================================================");
        log.info("  Forge - Gym Management System is ready.");
        log.info("  Open http://localhost:8080  (or 8081 if configured)");
        log.info("====================================================");
    }
}
