package com.egrub.scanner.model;

import lombok.Data;

@Data
public class ScripListRequest {
    private String lookupDate;
    private int lookBackPeriod;
    private String accessToken;
}
