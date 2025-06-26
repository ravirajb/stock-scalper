package com.egrub.scanner.model.eod;

import lombok.Data;

@Data
public class TickerRequest {
    private String apiToken;
    private String exchangeToken;
}
