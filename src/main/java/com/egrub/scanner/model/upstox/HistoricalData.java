package com.egrub.scanner.model.upstox;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoricalData {
    @JsonProperty("status")
    private String status;

    @JsonProperty("data")
    private Data data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
