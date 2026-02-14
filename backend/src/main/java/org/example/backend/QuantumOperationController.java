package org.example.backend;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@RestController
public class QuantumOperationController {

    private static final String QRNG_URL = "https://qrng.anu.edu.au/wp-content/plugins/colours-plugin/get_one_colour.php";

    @GetMapping("/randomQuantumNumber")
    public ResponseEntity<String> randomQuantumNumber() {
        long timestamp = Instant.now().toEpochMilli();
        String uri = QRNG_URL + "?_=" + timestamp;

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status >= 200 && status < 300) {
                // Return raw body as-is. The upstream returns a plain string for this endpoint.
                return ResponseEntity.ok(body);
            } else {
                return ResponseEntity.status(status).body("Upstream returned status " + status + ": " + body);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching quantum number: " + e.getMessage());
        }
    }
}

