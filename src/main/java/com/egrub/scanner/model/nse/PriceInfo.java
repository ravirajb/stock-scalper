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
public class PriceInfo {
    private double lastPrice;
    private double change;
    private double pChange;
    private double previousClose;
    private double open;
    private double close;
    private double vwap;
    private double stockIndClosePrice;
    private String lowerCP;
    private String upperCP;
    private String pPriceBand;
    private double basePrice;
    private IntraDayHighLow intraDayHighLow;
    private WeekHighLow weekHighLow;
    private Object iNavValue;
    private boolean checkINAV;
    private double tickSize;
    private String ieq;
}