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
public class ValueAtRisk {
    private double securityVar;
    private double indexVar;
    private double varMargin;
    private double extremeLossMargin;
    private double adhocMargin;
    private double applicableMargin;
}
