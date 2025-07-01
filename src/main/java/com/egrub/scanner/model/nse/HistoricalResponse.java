package com.egrub.scanner.model.nse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricalResponse {
    private List<HistoricalData> data;

    public List<HistoricalData> getData() {
        return data;
    }

    public void setData(List<HistoricalData> data) {
        this.data = data;
    }
}
