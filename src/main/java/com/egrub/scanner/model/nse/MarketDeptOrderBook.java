package com.egrub.scanner.model.nse;

import lombok.Data;
import java.util.List;

@Data
public class MarketDeptOrderBook {
    private int totalBuyQuantity;
    private int totalSellQuantity;
    private double open;
    private List<PriceLevel> bid;
    private List<PriceLevel> ask;
    private TradeInfo tradeInfo;
}
