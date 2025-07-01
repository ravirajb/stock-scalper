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
public class Info {
    private String symbol;
    private String companyName;
    private String industry;
    private List<String> activeSeries;
    private List<String> debtSeries;
    private boolean isFNOSec;
    private boolean isCASec;
    private boolean isSLBSec;
    private boolean isDebtSec;
    private boolean isSuspended;
    private List<String> tempSuspendedSeries;
    private boolean isETFSec;
    private boolean isDelisted;
    private String isin;
    private String listingDate;
    private boolean isMunicipalBond;
    private boolean isHybridSymbol;
    private boolean isTop10;
    private String identifier;
}
