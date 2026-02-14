package ai.sandbox.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ai.sandbox.backend.dto.QuantumResponse;
import ai.sandbox.backend.service.QuantumService;
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

@Slf4j
@RestController
@RequiredArgsConstructor
public class QuantumOperationController {

    private final QuantumService quantumService;

    private final ObjectMapper objectMapper;

    @GetMapping(path = "/randomQuantumNumber/singleGeneration", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuantumResponse> singleGeneration() {
        long timestamp = Instant.now().toEpochMilli();
        log.info("Received request for single quantum number generation");

        try {
            String result = quantumService.fetchOne(timestamp);
            QuantumResponse qr = new QuantumResponse(result, timestamp);
            log.debug("Successfully generated quantum response: {}", qr);
            return ResponseEntity.ok(qr);
        } catch (Exception e) {
            log.error("Error generating quantum number", e);
            QuantumResponse err = new QuantumResponse("ERROR: " + e.getMessage(), Instant.now().toEpochMilli());
            return ResponseEntity.status(500).body(err);
        }
    }

    @GetMapping(path = "/randomQuantumNumber/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(name = "frequency", defaultValue = "1") long frequencySeconds) {
        log.info("Received request for quantum number stream with frequency: {} seconds", frequencySeconds);

        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        final Runnable task = () -> {
            long timestamp = Instant.now().toEpochMilli();
            try {
                String value = quantumService.fetchOne(timestamp);
                QuantumResponse qr = new QuantumResponse(value, timestamp);
                String json = objectMapper.writeValueAsString(qr);
                log.debug("Sending quantum event: {}", json);
                emitter.send(SseEmitter.event().data(json).name("quantum"));
            } catch (IOException ioe) {
                log.warn("Client disconnected or IO error", ioe);
                emitter.complete();
            } catch (Exception e) {
                log.error("Error in quantum stream task", e);
                try {
                    QuantumResponse err = new QuantumResponse("ERROR: " + e.getMessage(), Instant.now().toEpochMilli());
                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(err)).name("quantum-error"));
                } catch (IOException ioException) {
                    log.debug("Failed to send error event to client", ioException);
                }
            }
        };

        final ScheduledFuture<?> scheduled = scheduler.scheduleAtFixedRate(task, 0, Math.max(1, frequencySeconds), TimeUnit.SECONDS);

        emitter.onCompletion(() -> {
            log.debug("SSE emitter completed");
            scheduled.cancel(true);
            scheduler.shutdown();
        });
        emitter.onTimeout(() -> {
            log.debug("SSE emitter timed out");
            scheduled.cancel(true);
            scheduler.shutdown();
            emitter.complete();
        });

        return emitter;
    }
}
