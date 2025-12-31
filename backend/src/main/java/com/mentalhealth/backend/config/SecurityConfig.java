package com.mentalhealth.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // JavaFX cannot send CSRF tokens
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // allow registration and login
                        .requestMatchers(
                                "/api/users/register",
                                "/api/instructors/register",
                                "/auth/login",
                                "/debug/**",
                                // User feature endpoints
                                "/api/journal/**",
                                "/api/sleep/**",
                                "/api/habits/**",
                                "/api/mood/**",
                                "/api/stress/**",
                                "/api/progress/**",
                                // Instructor dashboard endpoints
                                "/api/sessions/**",
                                "/api/session-requests/**",
                                "/api/therapy-notes/**",
                                "/api/resources/**",
                                "/api/messages/**",
                                "/api/notifications/**",
                                // WebSocket endpoints
                                "/ws/**",
                                "/app/**",
                                "/topic/**",
                                "/queue/**",
                                // Static resources
                                "/uploads/**"
                        ).permitAll()

                        // TEMPORARILY allow everything else
                        .anyRequest().permitAll()
                );

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
