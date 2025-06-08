package com.egrub.scanner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportData {
    private String title;
    private String description;
    private List<AnomalyData> anomalyData;
    private int totalCount;
}
