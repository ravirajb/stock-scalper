package com.egrub.scanner.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class DoubleNaNDeserializer extends JsonDeserializer<Double> {
    @Override
    public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if ("NA".equalsIgnoreCase(value) || value.isEmpty()) {
            return null; // or return 0.0 if you prefer
        }
        return Double.valueOf(value);
    }
}