package dev.infrai.propertyindex.reindex;

import dev.infrai.propertyindex.domain.PropertyRecord;
import java.util.List;

public interface PropertyRecordSource {
    List<PropertyRecord> findAll();
}
