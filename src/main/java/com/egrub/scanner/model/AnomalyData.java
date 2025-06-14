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
    private Double volumeRatio;
    private double pivot, r1, s1, r2, s2;

    @Override
    public String toString() {
        return "AnomalyData {\n" +
                "  timeStamp: " + timeStamp + "\n" +
                "  instrumentCode: " + instrumentCode + "\n" +
                "  close: " + close + "\n" +
                "  currentVolume: " + currentVolume + "\n" +
                "  cumulativeVolume: " + cumulativeVolume + "\n" +
                "  volumeSMA: " + volumeSMA + "\n" +
                "  pivot: " + pivot + "\n" +
                "  r1: " + r1 + "\n" +
                "  s1: " + s1 + "\n" +
                "  r2: " + r2 + "\n" +
                "  s2: " + s2 + "\n" +
                '}';
    }

}
