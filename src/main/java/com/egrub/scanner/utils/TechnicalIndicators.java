package com.egrub.scanner.utils;

import com.egrub.scanner.model.CandleData;

import java.util.ArrayList;
import java.util.List;

public class TechnicalIndicators {
    // RSI (Relative Strength Index)
    public static double[] calculateRSI(List<CandleData> candles, int period) {
        double[] rsi = new double[candles.size()];
        double avgGain = 0, avgLoss = 0;

        // Calculate initial average gain and loss
        for (int i = 1; i <= period && i < candles.size(); i++) {
            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            if (change > 0) avgGain += change;
            else avgLoss += Math.abs(change);
        }
        avgGain /= period;
        avgLoss /= period;

        for (int i = period; i < candles.size(); i++) {
            double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
            double gain = change > 0 ? change : 0;
            double loss = change < 0 ? Math.abs(change) : 0;
            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;
            double rs = avgLoss == 0 ? 100 : avgGain / avgLoss;
            rsi[i] = 100 - (100 / (1 + rs));
        }
        return rsi;
    }

    // Stochastic RSI
    public static double[] calculateStochasticRSI(List<CandleData> candles, int period) {
        double[] rsi = calculateRSI(candles, period);
        double[] stochRSI = new double[candles.size()];
        for (int i = period; i < candles.size(); i++) {
            double minRSI = Double.MAX_VALUE, maxRSI = Double.MIN_VALUE;
            for (int j = i - period + 1; j <= i; j++) {
                minRSI = Math.min(minRSI, rsi[j]);
                maxRSI = Math.max(maxRSI, rsi[j]);
            }
            stochRSI[i] = maxRSI == minRSI ? 0 : (rsi[i] - minRSI) / (maxRSI - minRSI) * 100;
        }
        return stochRSI;
    }

    // Pivot Points (Standard)
    public static double[] calculatePivotPoints(CandleData dailyCandle) {
        double pivot = (dailyCandle.getHigh() + dailyCandle.getLow() + dailyCandle.getClose()) / 3;
        double r1 = 2 * pivot - dailyCandle.getLow();
        double s1 = 2 * pivot - dailyCandle.getHigh();
        double r2 = pivot + (dailyCandle.getHigh() - dailyCandle.getLow());
        double s2 = pivot - (dailyCandle.getHigh() - dailyCandle.getLow());
        return new double[]{pivot, r1, s1, r2, s2}; // [Pivot, R1, S1, R2, S2]
    }

    // Exponential Moving Average (EMA)
    public static double[] calculateEMA(List<CandleData> candles, int period) {
        double[] ema = new double[candles.size()];
        double multiplier = 2.0 / (period + 1);
        ema[0] = candles.get(0).getClose(); // Initialize with first close
        for (int i = 1; i < candles.size(); i++) {
            ema[i] = (candles.get(i).getClose() - ema[i - 1]) * multiplier + ema[i - 1];
        }
        return ema;
    }

    // On-Balance Volume (OBV)
    public static long[] calculateOBV(List<CandleData> candles) {
        long[] obv = new long[candles.size()];
        obv[0] = 0;
        for (int i = 1; i < candles.size(); i++) {
            if (candles.get(i).getClose() > candles.get(i - 1).getClose()) {
                obv[i] = obv[i - 1] + candles.get(i).getVolume();
            } else if (candles.get(i).getClose() < candles.get(i - 1).getClose()) {
                obv[i] = obv[i - 1] - candles.get(i).getVolume();
            } else {
                obv[i] = obv[i - 1];
            }
        }
        return obv;
    }

    // Fibonacci Retracement Levels
    public static double[] calculateFibonacciLevels(double high, double low) {
        double range = high - low;
        return new double[]{
                high,
                high - 0.236 * range,
                high - 0.382 * range,
                high - 0.5 * range,
                high - 0.618 * range,
                low
        }; // [100%, 76.4%, 61.8%, 50%, 38.2%, 0%]
    }

    // Average True Range (ATR)
    public static double[] calculateATR(List<CandleData> candles, int period) {
        double[] atr = new double[candles.size()];
        double[] tr = new double[candles.size()];
        tr[0] = candles.get(0).getHigh() - candles.get(0).getLow();
        for (int i = 1; i < candles.size(); i++) {
            double hl = candles.get(i).getHigh() - candles.get(i).getLow();
            double hpc = Math.abs(candles.get(i).getHigh() - candles.get(i - 1).getClose());
            double lpc = Math.abs(candles.get(i).getLow() - candles.get(i - 1).getClose());
            tr[i] = Math.max(hl, Math.max(hpc, lpc));
        }
        atr[period - 1] = 0;
        for (int i = 1; i <= period && i < candles.size(); i++) {
            atr[period - 1] += tr[i];
        }
        atr[period - 1] /= period;
        for (int i = period; i < candles.size(); i++) {
            atr[i] = (atr[i - 1] * (period - 1) + tr[i]) / period;
        }
        return atr;
    }

    // Bollinger Bands
    public static double[][] calculateBollingerBands(List<CandleData> candles,
                                                     int period,
                                                     double stdDevFactor) {
        double[] sma = calculateSMA(candles, period);
        double[][] bands = new double[3][candles.size()]; // [Upper, Middle, Lower]
        for (int i = period - 1; i < candles.size(); i++) {
            double sumSq = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sumSq += Math.pow(candles.get(j).getClose() - sma[i], 2);
            }
            double stdDev = Math.sqrt(sumSq / period);
            bands[0][i] = sma[i] + stdDevFactor * stdDev; // Upper
            bands[1][i] = sma[i]; // Middle (SMA)
            bands[2][i] = sma[i] - stdDevFactor * stdDev; // Lower
        }
        return bands;
    }

    // New method for EMA on double[]
    public static double[] calculateEMAArray(double[] values, int period) {
        double[] ema = new double[values.length];
        double multiplier = 2.0 / (period + 1);
        ema[0] = values[0]; // Initialize with first value
        for (int i = 1; i < values.length; i++) {
            ema[i] = (values[i] - ema[i - 1]) * multiplier + ema[i - 1];
        }
        return ema;
    }

    // MACD
    public static double[][] calculateMACD(List<CandleData> candles,
                                           int fastPeriod,
                                           int slowPeriod,
                                           int signalPeriod) {
        double[] fastEMA = calculateEMA(candles, fastPeriod);
        double[] slowEMA = calculateEMA(candles, slowPeriod);
        double[] macdLine = new double[candles.size()];
        for (int i = 0; i < candles.size(); i++) {
            macdLine[i] = fastEMA[i] - slowEMA[i];
        }
        // Use calculateEMAArray for signal line
        double[] signalLine = calculateEMAArray(macdLine, signalPeriod);
        double[] histogram = new double[candles.size()];
        for (int i = 0; i < candles.size(); i++) {
            histogram[i] = macdLine[i] - signalLine[i];
        }
        return new double[][]{macdLine, signalLine, histogram};
    }

    // Simple Moving Average (SMA) for Price
    public static double[] calculateSMA(List<CandleData> candles, int period) {
        double[] sma = new double[candles.size()];
        for (int i = period - 1; i < candles.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += candles.get(j).getClose();
            }
            sma[i] = sum / period;
        }
        return sma;
    }

    // SMA of Volume
    public static double[] calculateVolumeSMA(List<CandleData> candles, int period) {
        double[] volSMA = new double[candles.size()];
        for (int i = period - 1; i < candles.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += candles.get(j).getVolume();
            }
            volSMA[i] = sum / period;
        }
        return volSMA;
    }

    // Aggregate Volume
    public static long calculateAggregateVolume(List<CandleData> candles) {
        long totalVolume = 0;
        for (CandleData candle : candles) {
            totalVolume += candle.getVolume();
        }
        return totalVolume;
    }

    // Last N Days High and Low
    public static double[] calculateLastNDaysHighLow(List<CandleData> candles, int n) {
        double high = Double.MIN_VALUE, low = Double.MAX_VALUE;
        for (int i = Math.max(0, candles.size() - n); i < candles.size(); i++) {
            high = Math.max(high, candles.get(i).getHigh());
            low = Math.min(low, candles.get(i).getLow());
        }
        return new double[]{high, low};
    }

    // Last N Days Highs and Lows (Arrays)
    public static double[][] calculateLastNDaysHighsLows(List<CandleData> candles, int n) {
        double[] highs = new double[n];
        double[] lows = new double[n];
        for (int i = 0; i < n && i < candles.size(); i++) {
            highs[i] = candles.get(candles.size() - 1 - i).getHigh();
            lows[i] = candles.get(candles.size() - 1 - i).getLow();
        }
        return new double[][]{highs, lows};
    }

    // Check for Higher Highs and Lower Lows
    public static boolean[] isHigherHighsLowerLows(List<CandleData> candles, int n) {
        boolean higherHighs = true, lowerLows = true;
        for (int i = candles.size() - n + 1; i < candles.size(); i++) {
            if (candles.get(i).getHigh() <= candles.get(i - 1).getHigh()) higherHighs = false;
            if (candles.get(i).getLow() >= candles.get(i - 1).getLow()) lowerLows = false;
        }
        return new boolean[]{higherHighs, lowerLows};
    }

    // Key Resistance/Support Levels (Using Pivot Points for Today)
    public static double[] calculateKeyLevels(CandleData dailyCandle) {
        return calculatePivotPoints(dailyCandle); // Returns Pivot, R1, S1, R2, S2
    }

    // Smart Money Indicators (Order Block Detection)
    public static List<double[]> detectOrderBlocks(List<CandleData> candles) {
        List<double[]> orderBlocks = new ArrayList<>();
        for (int i = 1; i < candles.size() - 1; i++) {
            if (candles.get(i).getVolume() > candles.get(i - 1).getVolume() * 1.5
                    && candles.get(i).getVolume() > candles.get(i + 1).getVolume() * 1.5) {
                orderBlocks.add(new double[]{candles.get(i).getHigh(), candles.get(i).getLow()});
            }
        }
        return orderBlocks;
    }

    // Liquidity Zones (High Volume Zones)
    public static List<double[]> detectLiquidityZones(List<CandleData> candles, int period) {
        double[] volSMA = calculateVolumeSMA(candles, period);
        List<double[]> liquidityZones = new ArrayList<>();
        for (int i = period - 1; i < candles.size(); i++) {
            if (candles.get(i).getVolume() > volSMA[i] * 1.5) {
                liquidityZones.add(new double[]{candles.get(i).getHigh(), candles.get(i).getLow()});
            }
        }
        return liquidityZones;
    }

    // Consolidation/Accumulation Zones
    public static List<double[]> detectConsolidationZones(List<CandleData> candles,
                                                          int period) {
        double[] atr = calculateATR(candles, period);
        List<double[]> consolidationZones = new ArrayList<>();
        for (int i = period - 1; i < candles.size(); i++) {
            if (atr[i] < atr[i - 1] * 0.5) { // Low volatility indicates consolidation
                consolidationZones.add(new double[]{candles.get(i).getHigh(),
                        candles.get(i).getLow()});
            }
        }
        return consolidationZones;
    }

    // Buy/Sell Zones (Based on RSI and Bollinger Bands)
    public static String[] detectBuySellZones(List<CandleData> candles,
                                              int rsiPeriod,
                                              int bbPeriod) {
        double[] rsi = calculateRSI(candles, rsiPeriod);
        double[][] bb = calculateBollingerBands(candles, bbPeriod, 2.0);
        String[] zones = new String[candles.size()];
        for (int i = Math.max(rsiPeriod, bbPeriod); i < candles.size(); i++) {
            if (rsi[i] < 30 && candles.get(i).getClose() < bb[2][i]) {
                zones[i] = "Buy";
            } else if (rsi[i] > 70 && candles.get(i).getClose() > bb[0][i]) {
                zones[i] = "Sell";
            } else {
                zones[i] = "Neutral";
            }
        }
        return zones;
    }
}