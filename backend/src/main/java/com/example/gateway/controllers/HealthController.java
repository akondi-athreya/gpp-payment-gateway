package com.example.gateway.controllers;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.gateway.workers.WorkerStatusService;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationAvailability availability;
    private final WorkerStatusService workerStatusService;
    private final boolean redisOptional;
    private final boolean workerOptional;

    public HealthController(
            JdbcTemplate jdbcTemplate,
            StringRedisTemplate redisTemplate,
            ApplicationAvailability availability,
            WorkerStatusService workerStatusService,
            @Value("${health.redis.optional:false}") boolean redisOptional,
            @Value("${health.worker.optional:true}") boolean workerOptional) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.availability = availability;
        this.workerStatusService = workerStatusService;
        this.redisOptional = redisOptional;
        this.workerOptional = workerOptional;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthController() {
        String databaseStatus = checkDatabase();
        String redisStatus = checkRedis();
        String workerStatus = checkWorker();
        boolean ready = availability.getReadinessState() == ReadinessState.ACCEPTING_TRAFFIC;

        boolean databaseOk = "connected".equals(databaseStatus);
        boolean redisOk = "connected".equals(redisStatus) || (redisOptional && !"connected".equals(redisStatus));
        boolean workerOk = "running".equals(workerStatus) || (workerOptional && !"running".equals(workerStatus));

        Map<String, String> response = new LinkedHashMap<>();
        response.put("status", ready && databaseOk && redisOk && workerOk ? "healthy" : "unhealthy");
        response.put("database", databaseStatus);
        response.put("redis", redisStatus);
        response.put("worker", workerStatus);
        response.put("timestamp", ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private String checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return "connected";
        } catch (Exception e) {
            return "disconnected";
        }
    }

    private String checkRedis() {
        try {
            try (var connection = redisTemplate.getRequiredConnectionFactory().getConnection()) {
                String pong = connection.ping();
                return "PONG".equalsIgnoreCase(pong) ? "connected" : "disconnected";
            }
        } catch (Exception e) {
            return redisOptional ? "skipped" : "disconnected";
        }
    }

    private String checkWorker() {
        try {
            return workerStatusService.isRunning() ? "running" : workerOptional ? "skipped" : "stopped";
        } catch (Exception e) {
            return workerOptional ? "skipped" : "stopped";
        }
    }
}
