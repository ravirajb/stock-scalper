package com.egrub.scanner.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnomalyData {
    private String timeStamp;
    private String instrumentCode;
    private Double close;
    private Long currentVolume;
    private Long cumulativeVolume;
    private Double volumeSMA;
}
