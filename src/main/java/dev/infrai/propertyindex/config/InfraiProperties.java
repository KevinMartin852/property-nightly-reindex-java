package dev.infrai.propertyindex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "infrai")
public record InfraiProperties(
        String apiKey,
        String baseUrl,
        String embeddingModel,
        int embeddingDimension,
        String collection,
        String publicBaseUrl) {
}
