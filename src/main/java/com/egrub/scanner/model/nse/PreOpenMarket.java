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
public class PreOpenMarket {
    private List<Preopen> preopen;
    private Ato ato;
    private int IEP;
    private int totalTradedVolume;
    private int finalPrice;
    private int finalQuantity;
    private String lastUpdateTime;
    private int totalBuyQuantity;
    private int totalSellQuantity;
    private int atoBuyQty;
    private int atoSellQty;
    private double Change;
    private double perChange;
    private double prevClose;
}
