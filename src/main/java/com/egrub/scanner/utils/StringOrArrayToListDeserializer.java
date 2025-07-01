package com.egrub.scanner.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringOrArrayToListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        if (node.isArray()) {
            List<String> list = new ArrayList<>();
            for (JsonNode element : node) {
                list.add(element.asText());
            }
            return list;
        } else if (node.isTextual()) {
            String text = node.asText();
            if ("NA".equalsIgnoreCase(text) || text.isEmpty()) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(text);
            }
        }

        return Collections.emptyList();
    }
}