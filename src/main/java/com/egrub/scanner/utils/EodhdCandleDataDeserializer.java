package com.egrub.scanner.utils;

import com.egrub.scanner.model.CandleData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class EodhdCandleDataDeserializer extends StdDeserializer<CandleData> {

    public EodhdCandleDataDeserializer() {
        super(CandleData.class);
    }

    @Override
    public CandleData deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);

        CandleData candle = new CandleData();
        candle.setTimestamp(node.get("date").asText());
        candle.setOpen(getDoubleValue(node, "open"));
        candle.setHigh(getDoubleValue(node, "high"));
        candle.setLow(getDoubleValue(node, "low"));
        candle.setClose(getDoubleValue(node, "close"));
        candle.setVolume(getLongValue(node, "volume"));
        candle.setOpenInteretest(getLongValue(node, "open_interest")); // optional

        return candle;
    }

    private Double getDoubleValue(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asDouble() : null;
    }

    private Long getLongValue(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asLong() : null;
    }
}
