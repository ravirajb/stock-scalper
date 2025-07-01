package com.egrub.scanner.model.nse;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DateDeliveryPercent {
    private String date;
    private double percent;
    private double marketCap;
    private double freeFloat;
    private double dailyVolatility;
    private double annualVolatility;
    private String activeSeries;
    private double sectorPe;
    private double tickerPe;
}
