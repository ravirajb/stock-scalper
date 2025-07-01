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

    @JsonProperty("isBetween10And20EMA")
    private boolean isBetween10And20EMA;

    @JsonProperty("averageDeliveryPc")
    private double averageDeliveryPc;

    @JsonProperty("todayDeliveryPc")
    private double todayDeliveryPc;

    @JsonProperty("marketCap")
    private double marketCap;

    @JsonProperty("freeFloat")
    private double freeFloat;

    @JsonProperty("dailyVolatility")
    private double dailyVolatility;

    @JsonProperty("annualVolatility")
    private double annualVolatility;

    @JsonProperty("activeSeries")
    private String activeSeries;

    @JsonProperty("is30PercentRiseAnd9PercentCorrection15Days")
    private boolean is30PercentRiseAnd9PercentCorrection15Days;

    @JsonProperty("is25PercentRiseAnd8PercentCorrection15Days")
    private boolean is25PercentRiseAnd8PercentCorrection15Days;

    @JsonProperty("is30PercentRiseAnd7PercentCorrection15Days")
    private boolean is30PercentRiseAnd7PercentCorrection15Days;

    @JsonProperty("is20PercentRiseAnd6PercentCorrection15Days")
    private boolean is20PercentRiseAnd6PercentCorrection15Days;

    @JsonProperty("is20PercentRiseAnd6PercentCorrectiony7Days")
    private boolean is20PercentRiseAnd6PercentCorrectiony7Days;

    @JsonProperty("is15PercentRiseAnd5PercentCorrection7Days")
    private boolean is15PercentRiseAnd5PercentCorrection7Days;

    @JsonProperty("is15PercentRiseAnd5PercentCorrection10Days")
    private boolean is15PercentRiseAnd5PercentCorrection10Days;

    @JsonProperty("is20PercentRiseAnd6PercentCorrection10Days")
    private boolean is20PercentRiseAnd6PercentCorrection10Days;


}
