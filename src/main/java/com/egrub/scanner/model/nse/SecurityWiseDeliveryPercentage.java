package com.egrub.scanner.model.nse;

import lombok.Data;

@Data
public class SecurityWiseDeliveryPercentage {
    private int quantityTraded;
    private int deliveryQuantity;
    private double deliveryToTradedQuantity;
    private String seriesRemarks;
    private String secWiseDelPosDate;
}
