package com.egrub.scanner.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class AnomalyNotificationFlags {
    private boolean is6SigmaNotified;
    private boolean isConsolidationNotified;
    private boolean isConsolidationBreakoutNotified;
    private boolean isNormalAnomalyNotified;
    private int consolidationCount;
    private int greenP99OutlierCount;
    private boolean isValidFirstAnomalyNotified;
}
