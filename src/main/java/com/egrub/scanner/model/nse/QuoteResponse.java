package com.egrub.scanner.model.nse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuoteResponse {
    private Info info;
    private Metadata metadata;
    private SecurityInfo securityInfo;
    private SddDetails sddDetails;
    private String currentMarketType;
    private PriceInfo priceInfo;
    private IndustryInfo industryInfo;
    private PreOpenMarket preOpenMarket;
}
