package com.egrub.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PotentialInstrument {
    @JsonProperty("Symbol")
    private String symbol;
    @JsonProperty("isSwingStock")
    private boolean swingStock;
    @JsonProperty("IsbreakoutReady")
    private boolean isBreakoutReady;
    @JsonProperty("is3WhiteSoldiers")
    private boolean isThreeWhiteSoldiers;
    @JsonProperty("isNearResistance")
    private boolean isNR4Nr7;
    @JsonProperty("is3DaysPause")
    private boolean is3DaysPause;
    @JsonProperty("is4DaysPause")
    private boolean is4DaysPause;
    @JsonProperty("is5DaysPause")
    private boolean is5DaysPause;

    @JsonProperty("is6PercentInLast7Days")
    private boolean is6PercentInLast7Days;


    @JsonProperty("is3DayVolumeDecreasing")
    private boolean is3DayVolumeDecreasing;
    @JsonProperty("is4DayVolumeDecreasing")
    private boolean is4DayVolumeDecreasing;
    @JsonProperty("is5DayVolumeDecreasing")
    private boolean is5DayVolumeDecreasing;

    @JsonProperty("is3Day1Percent")
    private boolean is3Day1Percent;

    @JsonProperty("is4Day1Percent")
    private boolean is4Day1Percent;

    @JsonProperty("is5Day1Percent")
    private boolean is5Day1Percent;

    @JsonProperty("is3Day2Percent")
    private boolean is3Day2Percent;

    @JsonProperty("is4Day2Percent")
    private boolean is4Day2Percent;

    @JsonProperty("is5Day2Percent")
    private boolean is5Day2Percent;

    @JsonProperty("is3Day3Percent")
    private boolean is3Day3Percent;

    @JsonProperty("is4Day3Percent")
    private boolean is4Day3Percent;

    @JsonProperty("is5Day3Percent")
    private boolean is5Day3Percent;

    @JsonProperty("is21Day2Percent")
    private boolean is21Day2Percent;

    @JsonProperty("is21Day3Percent")
    private boolean is21Day3Percent;

    @JsonProperty("atrLast20Days")
    private double atrLast20Days;

}
