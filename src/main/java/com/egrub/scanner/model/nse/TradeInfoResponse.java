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
public class TradeInfoResponse {
    private boolean noBlockDeals;
    private List<BulkBlockDeal> bulkBlockDeals;
    private MarketDeptOrderBook marketDeptOrderBook;
    private ValueAtRisk valueAtRisk;
    private SecurityWiseDeliveryPercentage securityWiseDP;
}