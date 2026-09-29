package dev.infrai.propertyindex.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import dev.infrai.propertyindex.infrai.InfraiRestClient;
import java.net.http.HttpClient;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfraiConfiguration {
    @Bean
    OpenAIClient openAIClient(InfraiProperties properties) {
        return OpenAIOkHttpClient.builder()
                .apiKey(properties.apiKey())
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Bean
    InfraiRestClient infraiRestClient(InfraiProperties properties, ObjectMapper objectMapper) {
        return new InfraiRestClient(
                HttpClient.newBuilder().build(), objectMapper,
                properties.baseUrl(), properties.apiKey(), Thread::sleep);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
