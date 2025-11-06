package uk.gov.moj.cpp.defence.command.helper;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class JsonHelper {

    private JsonHelper() {

    }

    public static JsonObject removeProperty(final JsonObject origin, final String key) {
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        for (final Map.Entry<String, JsonValue> entry : origin.entrySet()) {
            if (!entry.getKey().equals(key)) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }
}
