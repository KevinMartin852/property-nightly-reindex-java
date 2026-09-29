package dev.infrai.propertyindex.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReindexPolicyTest {
    private final LocalDate today = LocalDate.of(2026, 9, 18);
    private final ReindexPolicy policy = new ReindexPolicy(
            Clock.fixed(Instant.parse("2026-09-18T02:00:00Z"), ZoneOffset.UTC));
    private final URI source = URI.create("https://property.example/record");

    @Test
    void selectsOnlyOperationallyRelevantRecords() {
        List<PropertyRecord> records = List.of(
                new PropertyRecord.MaintenanceRequest("open", source, "a", true, "urgent"),
                new PropertyRecord.MaintenanceRequest("closed", source, "a", false, "low"),
                new PropertyRecord.TenantDocument("current", source, "a", today.minusDays(1), today.plusDays(1)),
                new PropertyRecord.TenantDocument("expired", source, "a", today.minusDays(2), today.minusDays(1)),
                new PropertyRecord.InspectionReminder("due-soon", source, "b", today.plusDays(30)),
                new PropertyRecord.InspectionReminder("later", source, "b", today.plusDays(31)));

        List<String> selected = records.stream().filter(policy::shouldIndex).map(PropertyRecord::id).toList();

        assertThat(selected).containsExactly("open", "current", "due-soon");
    }
}
