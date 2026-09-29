package dev.infrai.propertyindex.domain;

import java.time.Clock;
import java.time.LocalDate;

public final class ReindexPolicy {
    private final Clock clock;

    public ReindexPolicy(Clock clock) {
        this.clock = clock;
    }

    public boolean shouldIndex(PropertyRecord record) {
        LocalDate today = LocalDate.now(clock);
        return switch (record) {
            case PropertyRecord.MaintenanceRequest request -> request.open();
            case PropertyRecord.TenantDocument document ->
                    !today.isBefore(document.validFrom()) && !today.isAfter(document.validUntil());
            case PropertyRecord.InspectionReminder reminder ->
                    !reminder.dueOn().isBefore(today) && !reminder.dueOn().isAfter(today.plusDays(30));
        };
    }
}
