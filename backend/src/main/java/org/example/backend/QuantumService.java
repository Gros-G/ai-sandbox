package org.example.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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
    }

    public String fetchOne(long timestamp) throws Exception {
        String uri = qrngUrl + "?_=" + timestamp;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(readTimeoutSeconds))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String body = response.body();
        if (status >= 200 && status < 300) {
            return body;
        } else {
            throw new RuntimeException("Upstream returned status " + status + ": " + body);
        }
    }
}
