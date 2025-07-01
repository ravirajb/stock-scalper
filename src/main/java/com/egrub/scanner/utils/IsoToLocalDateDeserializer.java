package com.egrub.scanner.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;

public class IsoToLocalDateDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String isoString = p.getText();
        try {
            return OffsetDateTime.parse(isoString).toLocalDate().toString();// strips time & zone
        } catch (Exception e) {
            return null;
        }
    }
}
