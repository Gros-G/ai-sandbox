package ai.sandbox.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class QuantumService {

    private final HttpClient client;
    private final String qrngUrl;
    private final int readTimeoutSeconds;

    public QuantumService(HttpClient client,
                          @Value("${quantum.url}") String qrngUrl,
                          @Value("${quantum.readTimeoutSeconds:10}") int readTimeoutSeconds) {
        this.client = client;
        this.qrngUrl = qrngUrl;
        this.readTimeoutSeconds = readTimeoutSeconds;
        log.info("QuantumService initialized with URL: {}", qrngUrl);
    }

    public String fetchOne(long timestamp) throws Exception {
        log.debug("Fetching quantum random number with timestamp: {}", timestamp);

        URI uri = UriComponentsBuilder.fromUriString(qrngUrl)
                .queryParam("_", timestamp)
                .build()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(readTimeoutSeconds))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status >= 200 && status < 300) {
                log.debug("Successfully fetched quantum number: {}", body);
                return body;
            } else {
                log.warn("Quantum service returned status {}: {}", status, body);
                throw new RuntimeException("Quantum service returned status " + status + ": " + body);
            }
        } catch (Exception e) {
            log.error("Error fetching quantum number", e);
            throw e;
        }
    }
}