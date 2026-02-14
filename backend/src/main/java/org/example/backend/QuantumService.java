package org.example.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class QuantumService {

    @Value("${quantum.url}")
    private String qrngUrl;

    @Value("${quantum.connectTimeoutSeconds:5}")
    private int connectTimeoutSeconds;

    @Value("${quantum.readTimeoutSeconds:10}")
    private int readTimeoutSeconds;

    private HttpClient client;

    @PostConstruct
    public void init() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
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
