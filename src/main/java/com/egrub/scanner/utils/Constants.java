package com.egrub.scanner.utils;

import com.egrub.scanner.model.AnomalyData;
import com.egrub.scanner.model.CandleData;
import com.egrub.scanner.model.upstox.Instrument;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {

    public static final String INTRADAY_BASE_URL = "https://api.upstox.com/v3/historical-candle/intraday/";
    public static final String HISTORICAL_BASE_URL = "https://api.upstox.com/v3/historical-candle/";

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter NSE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");


    public static final List<Instrument> VALID_INSTRUMENT = new ArrayList<>();
    public static final Map<String, List<AnomalyData>> ANOMALY_MAP = new HashMap<>();

    public static List<CandleData> getCandlesPriorTo(List<CandleData> candles, String timestampStr) {
        OffsetDateTime targetTime = OffsetDateTime.parse(timestampStr);

        List<CandleData> result = new ArrayList<>();

        for (CandleData candle : candles) {
            OffsetDateTime candleTime = OffsetDateTime.parse(candle.getTimestamp());

            // candleTime <= targetTime
            if (!candleTime.isAfter(targetTime)) {
                result.add(candle);
            }
        }

        return result;
    }

    public static boolean isPriceInBoxWithDecreasingVolume(List<CandleData> candles, int days) {
        if (candles.size() < days) return false;

        List<CandleData> subList = candles.subList(0, days); // Most recent N days

        // Check volume strictly decreasing
        /*for (int i = 0; i < subList.size() - 1; i++) {
            if (subList.get(i).getVolume() >= subList.get(i + 1).getVolume()) {
                return false;
            }
        }*/

        // Check price in box: all high-low ranges are within 5% of the max range
        double maxRange = subList.stream()
                .mapToDouble(c -> c.getClose() - c.getOpen())
                .max()
                .orElse(0.0);

        return subList.stream()
                .allMatch(c -> {
                    double range = c.getClose() - c.getOpen();
                    return Math.abs(range - maxRange) / maxRange <= 0.03; // within 5% deviation
                });
    }

    public static long computeDelayUntilNext5MinuteMark() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        int nextMinute = ((now.getMinute() / 5) + 1) * 5;
        LocalDateTime nextExecution = now.withMinute(0).withSecond(0).plusMinutes(nextMinute);
        if (nextMinute >= 60) {
            nextExecution = nextExecution.plusHours(1).withMinute(0);
        }
        return Duration.between(now, nextExecution).getSeconds();
    }

    public static boolean isTodayWorkingDay(String dateStr) {
        DateTimeFormatter formatter = DATE_FORMATTER;
        LocalDate date =
                LocalDate.parse(dateStr, formatter);

        return !(date.getDayOfWeek() == DayOfWeek.SUNDAY
                || date.getDayOfWeek() == DayOfWeek.SATURDAY);

    }

    public static String getPreviousDate(String dateStr, boolean isNSE) {
        DateTimeFormatter formatter = DATE_FORMATTER;
        if (isNSE) {
            formatter = NSE_DATE_FORMATTER;
        }
        LocalDate date =
                LocalDate.parse(dateStr, formatter);
        do {
            date = date.minus(1, ChronoUnit.DAYS);
        } while (date.getDayOfWeek() == DayOfWeek.SUNDAY
                || date.getDayOfWeek() == DayOfWeek.SATURDAY);

        return date.format(formatter);
    }

    public static String getStartDate(String dateStr, int lookBackPeriod, boolean isNSE) {
        DateTimeFormatter formatter = DATE_FORMATTER;
        if (isNSE) {
            formatter = NSE_DATE_FORMATTER;
        }

        LocalDate date =
                LocalDate.parse(dateStr, formatter);

        List<LocalDate> workingDays = new ArrayList<>();
        while (workingDays.size() < lookBackPeriod - 1) {
            date = date.minus(1, ChronoUnit.DAYS);
            if (!(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                workingDays.add(date);
            }
        }

        return workingDays.get(workingDays.size() - 1).format(formatter);
    }

    public static void rungc() {
        Runtime runtime = Runtime.getRuntime();

        // Run garbage collector
        runtime.gc();

        long totalMemory = runtime.totalMemory(); // total memory in JVM
        long freeMemory = runtime.freeMemory();   // free memory in JVM
        long usedMemory = totalMemory - freeMemory;

        System.out.println("Total Memory: " + (totalMemory / (1024 * 1024)) + " MB");
        System.out.println("Free Memory: " + (freeMemory / (1024 * 1024)) + " MB");
        System.out.println("Used Memory: " + (usedMemory / (1024 * 1024)) + " MB");
    }
}
