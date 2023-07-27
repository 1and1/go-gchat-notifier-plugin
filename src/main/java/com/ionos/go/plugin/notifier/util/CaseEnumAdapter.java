package com.ionos.go.plugin.notifier.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/** Deserializes enums with their lower case representation. */
public class CaseEnumAdapter implements JsonDeserializer<Enum> {
    @Override
    public Enum deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            if (type instanceof Class && ((Class<?>) type).isEnum()) {
                return Enum.valueOf((Class<Enum>) type, json.getAsString().toLowerCase());
            }
            return null;
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
}
