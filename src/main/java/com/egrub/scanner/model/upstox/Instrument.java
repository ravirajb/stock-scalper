package com.egrub.scanner.model.upstox;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Instrument {
    @JsonProperty("instrument_key")
    private String instrumentKey;
    @JsonProperty("exchange_token")
    private int exchangeToken;
    @JsonProperty("Symbol") // Assuming the updated CSV uses "Symbol" as header
    private String symbol;
    private String name; // Assuming "name" is consistent
    @JsonProperty("last_price")
    private double lastPrice;
    private String expiry; // Assuming "expiry" is consistent
    private Double strike; // Assuming "strike" is consistent
    @JsonProperty("tick_size")
    private double tickSize;
    @JsonProperty("lot_size")
    private double lotSize;
    @JsonProperty("instrument_type")
    private String instrumentType;
    @JsonProperty("option_type")
    private String optionType;
    private String exchange; // Assuming "exchange" is consistent
    @JsonProperty("Industry") // Assuming the updated CSV uses "Industry" as header
    private String industry; // New column added
    @JsonProperty("VALUE") //
    private Double marketCapitalInCrores;
}
