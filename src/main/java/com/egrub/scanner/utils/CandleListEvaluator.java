package com.egrub.scanner.utils;

import com.egrub.scanner.model.CandleData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CandleListEvaluator {
    public static boolean isVolumeContraction(List<CandleData> data) {
        if (data.size() < 5) return false;

        List<Double> ranges = new ArrayList<>();
        for (CandleData cd : data) {
            ranges.add(cd.getHigh() - cd.getLow());
        }

        List<Double> lastRanges = ranges.subList(0, 3);
        double maxRecent = Collections.max(lastRanges);
        double maxAll = Collections.max(ranges);

        return maxRecent < maxAll * 0.6; // Recent ranges 40% tighter
    }

    public static boolean isThreeWhiteSoldiers(List<CandleData> data) {
        if (data.size() < 3) return false;

        List<CandleData> last3 = data.subList(0, 3);
        for (CandleData cd : last3) {
            if (cd.getClose() <= cd.getOpen()) return false;
        }

        return last3.get(2).getClose() < last3.get(1).getClose() && last3.get(1).getClose() < last3.get(0).getClose();
    }

    public static boolean isXPercentInLastNDays(List<CandleData> data, int n, int percent) {

        for (int i = 0; i < n; i++) {
            CandleData candleData = data.get(i);
            if ((((candleData.getClose() - candleData.getOpen())
                    / candleData.getOpen()) * 100 > percent)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBox(List<CandleData> data, int n, int percent) {
        if (data.size() < 10) return false;

        double max = 0;
        double min = 0;

        if (data.size() < n) return false;

        CandleData candleData = data.get(n);

        if (((candleData.getClose() - candleData.getOpen())
                / candleData.getOpen()) * 100 < 5) {
            return false;
        }

        for (int i = 0; i < n; i++) {
            CandleData candle = data.get(i);
            if (max == 0) {
                max = Math.max(candle.getOpen(), candle.getClose());
            }
            if (candle.getOpen() > candle.getClose()) {
                if (candle.getOpen() > max)
                    max = candle.getOpen();
            } else {
                if (candle.getClose() > max)
                    max = candle.getClose();
            }
        }

        for (int i = 0; i < n; i++) {
            CandleData candle = data.get(i);
            if (min == 0) {
                min = Math.min(candle.getOpen(), candle.getClose());
            }
            if (candle.getOpen() > candle.getClose()) {
                if (candle.getOpen() < min)
                    min = candle.getClose();
            } else {
                if (candle.getOpen() < min)
                    min = candle.getOpen();
            }
        }

        return ((max - min) / min) * 100 < percent;
    }

    public static boolean isDarvasBox(List<CandleData> data) {
        if (data.size() < 10) return false;

        double highestHigh = data.stream().mapToDouble(c -> c.getHigh()).max().orElse(0);
        double lowestLow = data.stream().mapToDouble(c -> c.getLow()).min().orElse(0);

        double range = highestHigh - lowestLow;
        return range / lowestLow < 0.05; // <5% box range
    }

    public static boolean isInsideBars(List<CandleData> data) {
        if (data.size() < 3) return false;

        for (int i = 0; i < 2; i++) {
            CandleData curr = data.get(i);
            CandleData prev = data.get(i + 1);
            if (curr.getHigh() > prev.getHigh() || curr.getLow() < prev.getLow()) return false;
        }
        return true;
    }

    public static boolean isNR4orNR7(List<CandleData> data, int n) {
        if (data.size() < n) return false;

        double minRange = Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            double range = data.get(i).getHigh() - data.get(i).getLow();
            if (range < minRange) minRange = range;
        }

        double todayRange = data.get(0).getHigh() - data.get(0).getLow();
        return todayRange == minRange;
    }

    public static boolean isBullishEngulfing(List<CandleData> data) {
        if (data.size() < 2) return false;

        CandleData curr = data.get(0);
        CandleData prev = data.get(1);

        return (prev.getClose() < prev.getOpen() && curr.getClose() > curr.getOpen() &&
                curr.getOpen() < prev.getClose() && curr.getClose() > prev.getOpen());
    }

    public static boolean isOBVRising(List<CandleData> data) {
        if (data.size() < 5) return false;

        long obv = 0;
        List<Long> obvList = new ArrayList<>();
        for (int i = data.size() - 1; i > 0; i--) {
            CandleData prev = data.get(i);
            CandleData curr = data.get(i - 1);
            if (curr.getClose() > prev.getClose()) obv += curr.getVolume();
            else if (curr.getClose() < prev.getClose()) obv -= curr.getVolume();
            obvList.add(obv);
        }
        return obvList.get(0) > obvList.get(obvList.size() - 1);
    }

    public static boolean isBreakoutReady(List<CandleData> data) {
        return isDarvasBox(data) && isVolumeContraction(data) && isOBVRising(data);
    }

    // Additional enhancement: Indian swing breakout heuristics
    public static boolean isOptimalIndianSwingBreakout(List<CandleData> data) {
        if (data.size() < 10) return false;

        CandleData latest = data.get(0);
        double resistance = data.subList(1, 10)
                .stream().mapToDouble(CandleData::getHigh).max().orElse(0);

        boolean nearResistance = Math.abs(latest.getClose() - resistance) / resistance < 0.01;
        boolean bullishCandle = latest.getClose() > latest.getOpen();
        boolean volumeSpike = latest.getVolume() >
                data.subList(1, 10)
                        .stream()
                        .mapToLong(CandleData::getVolume)
                        .average()
                        .orElse(0) * 1.5;

        return isOBVRising(data) && nearResistance && bullishCandle && volumeSpike;
    }

    public static boolean isVolumeDecreasingInBox(List<CandleData> data, double boxPercent, int boxPeriod) {
        if (data.size() < boxPeriod + 3) return false;

        // 3. Check for volume contraction over boxPeriod days
        List<Long> volumes = new ArrayList<>();
        for (int i = 0; i < boxPeriod; i++) {
            volumes.add(data.get(i).getVolume());
        }
        boolean isDecreasing = true;
        for (int i = 0; i < volumes.size() - 1; i++) {
            if (volumes.get(i) < volumes.get(i + 1)) {
                isDecreasing = false;
                break;
            }
        }

        return isDecreasing;
    }

    // Box setup after prior spike with volume contraction
    public static boolean isBoxAfterSpike(List<CandleData> data, double boxPercent, int boxPeriod) {
        if (data.size() < boxPeriod + 3) return false;

        // 1. Check for spike at position boxPeriod + 1
        CandleData spike = data.get(boxPeriod + 1);
        double percentChange = ((spike.getClose() - spike.getOpen()) / spike.getOpen()) * 100;
        if (percentChange < 6) return false;

        // 2. Check box range in candles 0 to (boxPeriod - 1)
        double maxHigh = data.subList(0, boxPeriod)
                .stream()
                .mapToDouble(CandleData::getClose)
                .max()
                .orElse(0);

        double minLow = data
                .subList(0, boxPeriod)
                .stream()
                .mapToDouble(CandleData::getOpen)
                .min()
                .orElse(0);

        if (((maxHigh - minLow) / minLow) * 100 > boxPercent) return false;

        // 3. Check for volume contraction over boxPeriod days
        List<Long> volumes = new ArrayList<>();
        for (int i = 0; i < boxPeriod; i++) {
            volumes.add(data.get(i).getVolume());
        }
        boolean isDecreasing = true;
        /*for (int i = 0; i < volumes.size() - 1; i++) {
            if (volumes.get(i) < volumes.get(i + 1)) {
                isDecreasing = false;
                break;
            }
        }*/

        return isDecreasing;
    }

}
