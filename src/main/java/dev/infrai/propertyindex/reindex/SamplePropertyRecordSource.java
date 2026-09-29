package dev.infrai.propertyindex.reindex;

import dev.infrai.propertyindex.domain.PropertyRecord;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public final class SamplePropertyRecordSource implements PropertyRecordSource {
    private final Clock clock;

    public SamplePropertyRecordSource(Clock clock) {
        this.clock = clock;
    }

    @Override
    public List<PropertyRecord> findAll() {
        LocalDate today = LocalDate.now(clock);
        return List.of(
                new PropertyRecord.MaintenanceRequest("mr-1042", URI.create("https://property.example/maintenance/1042"), "building-a", true, "urgent"),
                new PropertyRecord.TenantDocument("lease-88", URI.create("https://property.example/tenant-documents/88"), "building-a", today.minusMonths(2), today.plusMonths(10)),
                new PropertyRecord.InspectionReminder("inspection-31", URI.create("https://property.example/inspections/31"), "building-b", today.plusDays(14)));
    }
}
