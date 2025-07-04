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
public class MarketCapEntry {
    @JsonProperty("SYMBOL")
    private String symbol;
    @JsonProperty("VALUE")
    private String value;
}
