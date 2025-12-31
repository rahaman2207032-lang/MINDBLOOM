package com.mentalhealth.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduler Configuration
 * Enables scheduled tasks for session reminders, notifications, etc.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Scheduling is now enabled for the application
    // Use @Scheduled annotation in services for scheduled tasks
}

