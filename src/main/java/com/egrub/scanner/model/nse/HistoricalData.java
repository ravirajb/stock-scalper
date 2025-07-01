package com.egrub.scanner.model.nse;

import com.egrub.scanner.utils.CustomDateDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoricalData {

    @JsonProperty("mTIMESTAMP")
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private String date;

    @JsonProperty("CH_SERIES")
    private String series;

    @JsonProperty("COP_DELIV_PERC")
    private String deliverablePercentage;

    @JsonProperty("VWAP")
    private double vwap;

    @JsonProperty("CH_TOT_TRADED_QTY")
    private long totalTradedQty;

    @JsonProperty("COP_DELIV_QTY")
    private long deliveryQuantity;
}
