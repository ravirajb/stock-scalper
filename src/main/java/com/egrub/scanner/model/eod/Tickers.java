package com.egrub.scanner.model.eod;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Tickers {
    @JsonProperty("Code")
    private String code;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Country")
    private String country;
    @JsonProperty("Exchange")
    private String exchange;
    @JsonProperty("Currency")
    private String currency;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("Isin")
    private String isin;
}
