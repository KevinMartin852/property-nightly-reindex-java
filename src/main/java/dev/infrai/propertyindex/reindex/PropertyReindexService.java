package dev.infrai.propertyindex.reindex;

import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.EmbeddingCreateParams;
import dev.infrai.propertyindex.config.InfraiProperties;
import dev.infrai.propertyindex.domain.PropertyRecord;
import dev.infrai.propertyindex.domain.ReindexPolicy;
import dev.infrai.propertyindex.infrai.InfraiRestClient;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public final class PropertyReindexService {
    private final PropertyRecordSource source;
    private final ReindexPolicy policy;
    private final InfraiRestClient infrai;
    private final OpenAIClient embeddings;
    private final InfraiProperties properties;
    private final Clock clock;

    public PropertyReindexService(PropertyRecordSource source, InfraiRestClient infrai,
                                  OpenAIClient embeddings, InfraiProperties properties, Clock clock) {
        this.source = source;
        this.policy = new ReindexPolicy(clock);
        this.infrai = infrai;
        this.embeddings = embeddings;
        this.properties = properties;
        this.clock = clock;
    }

    public ReindexResult reindex() {
        String runId = Instant.now(clock).toString();
        List<Map<String, Object>> vectors = new ArrayList<>();
        for (PropertyRecord record : source.findAll()) {
            if (!policy.shouldIndex(record)) {
                continue;
            }
            String document = infrai.scrape(record.sourceUrl());
            List<Float> embedding = embeddings.embeddings().create(EmbeddingCreateParams.builder()
                    .model(properties.embeddingModel())
                    .input(document)
                    .build()).data().getFirst().embedding();
            vectors.add(vector(record, embedding));
        }
        infrai.upsert(properties.collection(), vectors, runId);
        return new ReindexResult(runId, vectors.size(), properties.collection());
    }

    public SetupResult setup() {
        infrai.createCollection(properties.collection(), properties.embeddingDimension());
        String task = properties.publicBaseUrl() + "/maintenance/reindex";
        return new SetupResult(properties.collection(), infrai.createNightlySchedule(task), task);
    }

    private Map<String, Object> vector(PropertyRecord record, List<Float> embedding) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("record_type", record.getClass().getSimpleName());
        metadata.put("property_id", record.propertyId());
        metadata.put("source_url", record.sourceUrl().toString());
        return Map.of("id", record.id(), "embedding", embedding, "metadata", metadata);
    }

    public record ReindexResult(String runId, int indexed, String collection) {}
    public record SetupResult(String collection, String jobId, String task) {}
}
