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
public class WeekHighLow {
    private double min;
    private String minDate;
    private double max;
    private String maxDate;
    private double value;
}
