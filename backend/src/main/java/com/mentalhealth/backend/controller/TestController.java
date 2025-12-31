package com.mentalhealth.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Controller for Database Diagnostics
 * Use this to verify database connectivity and data
 */
@RestController
@RequestMapping("/api/test")
// CORS handled globally in WebConfig.java
public class TestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Test database connection
     * GET /api/test/db-connection
     */
    @GetMapping("/db-connection")
    public ResponseEntity<?> testConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Database connection is working!",
                "datasource", dataSource.getClass().getName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", "Database connection failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Check if journal_entries table exists and has data
     * GET /api/test/journal-table
     */
    @GetMapping("/journal-table")
    public ResponseEntity<?> checkJournalTable() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Check if table exists
            String checkTable = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'journal_entries')";
            Boolean tableExists = jdbcTemplate.queryForObject(checkTable, Boolean.class);
            result.put("tableExists", tableExists);

            if (tableExists) {
                // Count entries
                String countQuery = "SELECT COUNT(*) FROM journal_entries";
                Integer count = jdbcTemplate.queryForObject(countQuery, Integer.class);
                result.put("totalEntries", count);

                // Get entries per user
                String userCountQuery = "SELECT user_id, COUNT(*) as count FROM journal_entries GROUP BY user_id";
                List<Map<String, Object>> userCounts = jdbcTemplate.queryForList(userCountQuery);
                result.put("entriesPerUser", userCounts);

                // Get latest entry
                if (count > 0) {
                    String latestQuery = "SELECT id, user_id, created_at FROM journal_entries ORDER BY created_at DESC LIMIT 1";
                    Map<String, Object> latest = jdbcTemplate.queryForMap(latestQuery);
                    result.put("latestEntry", latest);
                }
            }

            result.put("status", "success");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Check if habits table exists and has data
     * GET /api/test/habits-table
     */
    @GetMapping("/habits-table")
    public ResponseEntity<?> checkHabitsTable() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Check if table exists
            String checkTable = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'habits')";
            Boolean tableExists = jdbcTemplate.queryForObject(checkTable, Boolean.class);
            result.put("tableExists", tableExists);

            if (tableExists) {
                // Count habits
                String countQuery = "SELECT COUNT(*) FROM habits";
                Integer count = jdbcTemplate.queryForObject(countQuery, Integer.class);
                result.put("totalHabits", count);

                // Get habits per user
                String userCountQuery = "SELECT user_id, COUNT(*) as count, COUNT(CASE WHEN is_active THEN 1 END) as active_count FROM habits GROUP BY user_id";
                List<Map<String, Object>> userCounts = jdbcTemplate.queryForList(userCountQuery);
                result.put("habitsPerUser", userCounts);

                // Get latest habit
                if (count > 0) {
                    String latestQuery = "SELECT id, user_id, name, created_at FROM habits ORDER BY created_at DESC LIMIT 1";
                    Map<String, Object> latest = jdbcTemplate.queryForMap(latestQuery);
                    result.put("latestHabit", latest);
                }
            }

            result.put("status", "success");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * List all tables in the database
     * GET /api/test/tables
     */
    @GetMapping("/tables")
    public ResponseEntity<?> listTables() {
        try {
            String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name";
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(query);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "tables", tables,
                "count", tables.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}

