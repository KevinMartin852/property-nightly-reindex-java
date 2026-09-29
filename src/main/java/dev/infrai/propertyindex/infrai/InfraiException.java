package dev.infrai.propertyindex.infrai;

import com.fasterxml.jackson.databind.JsonNode;

public final class InfraiException extends RuntimeException {
    private final String code;
    private final int status;
    private final JsonNode details;

    public InfraiException(String code, JsonNode details, int status) {
        super(code + ": " + details.path("message").asText("request rejected"));
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public String code() { return code; }
    public int status() { return status; }
    public JsonNode details() { return details; }
}
