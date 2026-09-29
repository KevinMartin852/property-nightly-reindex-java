package dev.infrai.propertyindex.reindex;

import dev.infrai.propertyindex.infrai.InfraiException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maintenance")
public final class PropertyReindexController {
    private final PropertyReindexService service;

    public PropertyReindexController(PropertyReindexService service) {
        this.service = service;
    }

    @PostMapping("/setup")
    PropertyReindexService.SetupResult setup() {
        return service.setup();
    }

    @PostMapping("/reindex")
    PropertyReindexService.ReindexResult reindex() {
        return service.reindex();
    }

    @ExceptionHandler(InfraiException.class)
    ResponseEntity<Map<String, Object>> rejected(InfraiException error) {
        int status = error.status() >= 400 && error.status() < 500 ? error.status() : 502;
        return ResponseEntity.status(status).body(Map.of(
                "code", error.code(),
                "message", error.getMessage()));
    }
}
