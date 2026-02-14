package ai.sandbox.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Slf4j
@Configuration
public class HttpClientConfig {

    @Value("${quantum.connectTimeoutSeconds:5}")
    private int connectTimeoutSeconds;

    @Bean
    public HttpClient httpClient() {
        log.info("Creating HttpClient with connection timeout: {} seconds", connectTimeoutSeconds);
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
    }
}
