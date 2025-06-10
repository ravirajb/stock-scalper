package com.egrub.scanner.utils;

import com.egrub.scanner.model.AnomalyData;
import com.egrub.scanner.model.upstox.Instrument;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public static final List<Instrument> VALID_INSTRUMENT = new ArrayList<>();
    public static final Map<String, List<AnomalyData>> ANOMALY_MAP = new HashMap<>();

    public static long computeDelayUntilNext5MinuteMark() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        int nextMinute = ((now.getMinute() / 5) + 1) * 5;
        LocalDateTime nextExecution = now.withMinute(0).withSecond(0).plusMinutes(nextMinute);
        if (nextMinute >= 60) {
            nextExecution = nextExecution.plusHours(1).withMinute(0);
        }
        return Duration.between(now, nextExecution).getSeconds();
    }

    public static String getPreviousDate(String dateStr) {
        LocalDate date =
                LocalDate.parse(dateStr, DATE_FORMATTER);
        do {
            date = date.minus(1, ChronoUnit.DAYS);
        } while (date.getDayOfWeek() == DayOfWeek.SUNDAY
                || date.getDayOfWeek() == DayOfWeek.SATURDAY);

        return date.format(DATE_FORMATTER);
    }

    public static String getStartDate(String dateStr, int lookBackPeriod) {
        LocalDate date =
                LocalDate.parse(dateStr, DATE_FORMATTER);

        List<LocalDate> workingDays = new ArrayList<>();
        while (workingDays.size() < lookBackPeriod - 1) {
            date = date.minus(1, ChronoUnit.DAYS);
            if (!(date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                workingDays.add(date);
            }
        }

        return workingDays.get(workingDays.size() - 1).format(DATE_FORMATTER);
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
