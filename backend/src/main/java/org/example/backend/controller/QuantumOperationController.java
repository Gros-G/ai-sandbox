package org.example.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backend.dto.QuantumResponse;
import org.example.backend.service.QuantumService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class QuantumOperationController {

    private final QuantumService quantumService;
    private final ObjectMapper objectMapper;

    @GetMapping(path = "/randomQuantumNumber/singleGeneration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuantumResponse> singleGeneration() {
        long timestamp = Instant.now().toEpochMilli();
        try {
            String result = quantumService.fetchOne(timestamp);
            QuantumResponse qr = new QuantumResponse(result, timestamp);
            return ResponseEntity.ok(qr);
        } catch (Exception e) {
            QuantumResponse err = new QuantumResponse("ERROR: " + e.getMessage(), Instant.now().toEpochMilli());
            return ResponseEntity.status(500).body(err);
        }
    }

    @GetMapping(path = "/randomQuantumNumber/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(name = "frequency", defaultValue = "1") long frequencySeconds) {
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        final Runnable task = () -> {
            long timestamp = Instant.now().toEpochMilli();
            try {
                String value = quantumService.fetchOne(timestamp);
                QuantumResponse qr = new QuantumResponse(value, timestamp);
                String json = objectMapper.writeValueAsString(qr);
                // send as SSE data with JSON payload
                emitter.send(SseEmitter.event().data(json).name("quantum"));
            } catch (IOException ioe) {
                // client disconnected or IO error -> complete and shutdown
                emitter.complete();
            } catch (Exception e) {
                try {
                    QuantumResponse err = new QuantumResponse("ERROR: " + e.getMessage(), Instant.now().toEpochMilli());
                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(err)).name("quantum-error"));
                } catch (IOException ioException) {
                    // ignore
                }
            }
        };

        final ScheduledFuture<?> scheduled = scheduler.scheduleAtFixedRate(task, 0, Math.max(1, frequencySeconds), TimeUnit.SECONDS);

        // on completion, shutdown scheduler
        emitter.onCompletion(() -> {
            scheduled.cancel(true);
            scheduler.shutdown();
        });
        emitter.onTimeout(() -> {
            scheduled.cancel(true);
            scheduler.shutdown();
            emitter.complete();
        });

        return emitter;
    }
}
