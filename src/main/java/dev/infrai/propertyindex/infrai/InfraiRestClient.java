package dev.infrai.propertyindex.infrai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class InfraiRestClient {
    private static final int MAX_ATTEMPTS = 4;
    private final HttpClient http;
    private final ObjectMapper json;
    private final String baseUrl;
    private final String apiKey;
    private final Sleeper sleeper;

    public InfraiRestClient(HttpClient http, ObjectMapper json, String baseUrl, String apiKey, Sleeper sleeper) {
        this.http = http;
        this.json = json;
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.apiKey = apiKey;
        this.sleeper = sleeper;
    }

    public String scrape(URI url) {
        JsonNode data = post("/v1/web/scrape", Map.of("url", url.toString(), "format", "markdown"), null);
        return data.path("content").asText();
    }

    public void createCollection(String collection, int dimension) {
        post("/v1/vector/collection/create", Map.of(
                "collection", collection,
                "dimension", dimension,
                "metric", "cosine",
                "metadata", Map.of("owner", "property-operations")),
                "collection:" + collection);
    }

    public void upsert(String collection, List<Map<String, Object>> vectors, String runId) {
        post("/v1/vector/upsert", Map.of("collection", collection, "vectors", vectors), "reindex:" + runId);
    }

    public String createNightlySchedule(String taskUrl) {
        JsonNode data = post("/v1/cron/create", Map.of("cron_expr", "0 2 * * *", "task", taskUrl),
                "nightly-property-reindex:" + taskUrl);
        return data.path("job_id").asText();
    }

    private JsonNode post(String path, Object body, String idempotencyKey) {
        String endpoint = baseUrl + path.substring(3);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(write(body)));
            if (idempotencyKey != null) {
                builder.header("Idempotency-Key", idempotencyKey);
            }

            HttpResponse<String> response = send(builder.build());
            JsonNode envelope = readEnvelope(response.body());
            if (response.statusCode() == 429 && attempt + 1 < MAX_ATTEMPTS) {
                sleep(retryDelayMillis(response, attempt));
                continue;
            }
            if (!envelope.path("ok").asBoolean(false)) {
                JsonNode error = envelope.path("error");
                throw new InfraiException(error.path("code").asText("INFRAI_REJECTED"), error, response.statusCode());
            }
            if (response.statusCode() >= 500) {
                throw new IllegalStateException("Infrai transport status " + response.statusCode());
            }
            return envelope.path("data");
        }
        throw new IllegalStateException("Retry loop exhausted");
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("Infrai transport failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while calling Infrai", e);
        }
    }

    private JsonNode readEnvelope(String body) {
        try {
            return json.readTree(body);
        } catch (IOException e) {
            throw new IllegalStateException("Infrai returned an unreadable envelope", e);
        }
    }

    private String write(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalArgumentException("Request could not be encoded", e);
        }
    }

    private long retryDelayMillis(HttpResponse<?> response, int attempt) {
        return response.headers().firstValue("Retry-After")
                .map(value -> Long.parseLong(value) * 1_000L)
                .orElse(250L * (1L << attempt));
    }

    private void sleep(long millis) {
        try {
            sleeper.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during retry backoff", e);
        }
    }

    @FunctionalInterface
    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }
}
