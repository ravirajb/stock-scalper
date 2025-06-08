package com.egrub.scanner.model;

import lombok.Data;

import java.util.Map;

@Data
public class StockAnalyzerRequest {
    private Map<String, String> scripMap;
    private int lookBackPeriod;
    private String accessToken;
    private String startDate;
}
