package org.example.backend;

import lombok.RequiredArgsConstructor;
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

    @GetMapping("/randomQuantumNumber/singleGeneration")
    public ResponseEntity<String> singleGeneration() {
        long timestamp = Instant.now().toEpochMilli();
        try {
            String result = quantumService.fetchOne(timestamp);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching quantum number: " + e.getMessage());
        }
    }

    @SuppressWarnings("resource") // scheduler lifecycle managed (cancel + shutdown) in emitter callbacks
    @GetMapping(path = "/randomQuantumNumber/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(name = "frequency", defaultValue = "1") long frequencySeconds) {
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        final Runnable task = () -> {
            long timestamp = Instant.now().toEpochMilli();
            try {
                String value = quantumService.fetchOne(timestamp);
                emitter.send(SseEmitter.event().data(value));
            } catch (IOException ioe) {
                // client disconnected or IO error -> complete and shutdown
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().data("ERROR: " + e.getMessage()));
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
