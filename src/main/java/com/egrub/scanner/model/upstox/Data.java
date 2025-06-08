package com.egrub.scanner.model.upstox;

import com.egrub.scanner.model.CandleData;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public class Data {
    @JsonProperty("candles")
    private List<List<Object>> candles;

    public List<List<Object>> getCandles() {
        return candles;
    }

    public void setCandles(List<List<Object>> candles) {
        this.candles = candles;
    }

    public List<CandleData> getParsedCandles() {
        return candles.stream()
                .map(CandleData::fromList)
                .collect(Collectors.toUnmodifiableList());
    }
}
