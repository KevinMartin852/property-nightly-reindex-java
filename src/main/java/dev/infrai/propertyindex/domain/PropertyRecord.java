package dev.infrai.propertyindex.domain;

import java.net.URI;
import java.time.LocalDate;

public sealed interface PropertyRecord
        permits PropertyRecord.MaintenanceRequest, PropertyRecord.TenantDocument, PropertyRecord.InspectionReminder {
    String id();
    URI sourceUrl();
    String propertyId();

    record MaintenanceRequest(
            String id, URI sourceUrl, String propertyId, boolean open, String severity) implements PropertyRecord {
    }

    record TenantDocument(
            String id, URI sourceUrl, String propertyId, LocalDate validFrom, LocalDate validUntil) implements PropertyRecord {
    }

    record InspectionReminder(
            String id, URI sourceUrl, String propertyId, LocalDate dueOn) implements PropertyRecord {
    }
}
