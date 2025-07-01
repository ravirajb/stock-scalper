package com.egrub.scanner.model.nse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class TradeInfo {
    private double totalTradedVolume;
    private double totalTradedValue;
    private double totalMarketCap;
    private double ffmc; // Free Float Market Cap
    private double impactCost;
    private double cmDailyVolatility;
    private double cmAnnualVolatility;
    private String marketLot;
    private String activeSeries;
}
