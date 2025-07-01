package com.egrub.scanner.model.nse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityWiseDP {
    @JsonProperty("deliveryToTradedQuantity")
    private double deliveryPercent;

    @JsonProperty("secWiseDelPosDate")
    private String date;

    @JsonProperty("quantityTraded")
    private long quantityTraded;

    @JsonProperty("deliveryQuantity")
    private long deliveryQuantity;

}
