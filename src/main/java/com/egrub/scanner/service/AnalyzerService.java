package com.egrub.scanner.service;

import com.egrub.scanner.model.*;
import com.egrub.scanner.model.nse.DateDeliveryPercent;
import com.egrub.scanner.model.upstox.Instrument;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.egrub.scanner.utils.CandleListEvaluator.*;
import static com.egrub.scanner.utils.Constants.*;
import static com.egrub.scanner.utils.TechnicalIndicators.*;

@Service
@Log4j2
public class AnalyzerService {
    private final UpStoxService upStoxService;
    private final TelegramService telegramService;
    private final MongoTemplate mongoTemplate;
    private final ValidatorService validatorService;

    public final static Map<String, TDigestHelper> T_DIGEST_HELPER_MAP
            = new HashMap<>();

    public final static Map<String, AnomalyNotificationFlags> FLAGS_MAP = new HashMap();

    private final static Map<String, AnomalyData> LATEST_ANOMALY = new HashMap<>();
    private final static Map<String, Integer> ANOMALY_CONSOLIDATION_COUNT = new HashMap<>();
    private final static Map<String, Boolean> ANOMALY_CONSOLIDATION_NOTIFICATION_MAP
            = new HashMap<>();

    private final static Map<String, Boolean> BREAKOUT_NOTIFICATION_MAP
            = new HashMap<>();
    private final static Map<String, Boolean> NON_BREAKOUT_NOTIFICATION_MAP
            = new HashMap<>();

    private final static Map<String, Map<String, List<CandleData>>> STOCK_HISTORICAL_DATA =
            new HashMap<>();

    private final static Map<String, String> INTERVAL_MAP = Map.of(
            "1", "days",
            "5", "minutes"/*,
            "30", "minutes",
            "240", "minutes"*/);

    public AnalyzerService(UpStoxService upStoxService,
                           TelegramService telegramService,
                           ValidatorService validatorService,
                           MongoTemplate mongoTemplate) {
        this.upStoxService = upStoxService;
        this.telegramService = telegramService;
        this.mongoTemplate = mongoTemplate;
        this.validatorService = validatorService;
    }

    private String convertToddMmYyyy(String dateStr) {
        Pattern yyyyMMddPattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
        if (dateStr == null || !yyyyMMddPattern.matcher(dateStr).matches()) {
            throw new IllegalArgumentException("Date must be in yyyy-MM-dd format");
        }

        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate date = LocalDate.parse(dateStr, inputFormat);
        return date.format(outputFormat);
    }

    public List<PotentialInstrument> getPotentialStocks(String fromDate,
                                                        String accessToken,
                                                        List<Instrument> instruments,
                                                        int lookbackPeriod) {

        List<PotentialInstrument> validInstruments = new ArrayList<>();

        Pattern startsWithNumber = Pattern.compile("^[0-9].*");
        Set<String> skipList = Set.of("360ONE", "3PLAND", "5PAISA");
        List<String> containsKeywords = List.of("ETF", "BEES", "SGB", "GSEC",
                "LIQUIDSHRI", "LIQUID1", "UNIONGOLD", "LOWVOL1",
                "MON50EQUAL", "TOP10ADD", "LIQUIDADD", "SENSEXADD",
                "PVTBANKADD", "HDFCSENSEX", "HDFCLIQUID", "LIQUIDPLUS",
                "LIQUID", "LIQUIDSBI", "LOWVOL", "ABSLLIQUID", "MID150");

        List<Instrument> finalInstruments = instruments
                .stream()
                .filter(s -> skipList.contains(s.getSymbol()) || (
                        !startsWithNumber.matcher(s.getSymbol()).matches() &&
                                containsKeywords.stream().noneMatch(s.getSymbol()::contains)
                ))
                .collect(Collectors.toList());

        log.info("size: {}", finalInstruments.size());

        AtomicInteger count = new AtomicInteger();

        finalInstruments
                .forEach(instrument -> {

                    boolean isWorkingDay = isTodayWorkingDay(fromDate);

                    log.info("loop:{}", count.getAndIncrement());

                    try {
                        // if (instrument.getSymbol().equalsIgnoreCase("SHANTI")) {
                        List<CandleData> candles = upStoxService.getHistoricalCandles(
                                instrument.getInstrumentKey(),
                                instrument.getSymbol(),
                                "1",
                                "days",
                                isWorkingDay ? fromDate :
                                        getPreviousDate(fromDate, false),
                                getStartDate(fromDate, lookbackPeriod, false),
                                accessToken);

                        double deliveryPercent = 0.0;
                        int deliveryRecords = 0;
                        double todayDeliveryPc = 0;
                        double deliveryPcAverage = 0;
                        double marketCap = 0, freeFloat = 0;
                        double dailyVolatility = 0, annualVolatility = 0;
                        String activeSeries = "";

                        log.info("processing delivery pecentage for: {}",
                                instrument.getInstrumentKey());

                        try {
                            List<DateDeliveryPercent> deliveryPercents =
                                    validatorService.getDeliverablePercentage(
                                            instrument.getSymbol(),
                                            convertToddMmYyyy(
                                                    isWorkingDay ? fromDate :
                                                            getPreviousDate(fromDate,
                                                                    false)),
                                            30);
                            if (deliveryPercents != null && !deliveryPercents.isEmpty()) {
                                DateDeliveryPercent firstRec = deliveryPercents.get(0);
                                marketCap = firstRec.getMarketCap();
                                freeFloat = firstRec.getFreeFloat();
                                dailyVolatility = firstRec.getDailyVolatility();
                                annualVolatility = firstRec.getAnnualVolatility();
                                activeSeries = firstRec.getActiveSeries();

                                for (DateDeliveryPercent entry : deliveryPercents) {
                                    if (!entry
                                            .getDate()
                                            .equals(isWorkingDay ? fromDate :
                                                    getPreviousDate(fromDate,
                                                            false)
                                            )) {
                                        deliveryPercent += entry.getPercent();
                                        deliveryRecords++;
                                    } else {
                                        todayDeliveryPc = entry.getPercent();
                                    }
                                }

                                deliveryPcAverage = deliveryRecords == 0 ? 0 :
                                        (deliveryPercent / deliveryRecords);
                            }
                        } catch (Exception ex) {
                            log.error("error while parsing delivery % for:{} ",
                                    instrument.getSymbol());
                        }

                        double ema10 = 0d;
                        double ema20 = 0d;

                        if (candles.size() > 21) {
                            ema10 = calculatePeriodEMA(candles, 10);
                            ema20 = calculatePeriodEMA(candles, 20);
                        }

                        boolean swingStock = isOptimalIndianSwingBreakout(candles);
                        boolean isBreakoutReady = isBreakoutReady(candles);
                        boolean isThreeWhiteSoldiers = isThreeWhiteSoldiers(candles);
                        boolean isNR4Nr7 = isNR4orNR7(candles, 7);
                        boolean is3DaysPause = isBoxAfterSpike(candles, 3, 3);
                        boolean is4DaysPause = isBoxAfterSpike(candles, 3, 4);
                        boolean is5DaysPause = isBoxAfterSpike(candles, 3, 5);
                        boolean is3VolumeDecreasing = isVolumeDecreasingInBox(candles, 3, 3);
                        boolean is4VolumeDecreasing = isVolumeDecreasingInBox(candles, 3, 4);
                        boolean is5VolumeDecreasing = isVolumeDecreasingInBox(candles, 3, 5);
                        boolean is3BoxOf1Percent = isBox(candles, 3, 1);
                        boolean is3BoxOf2Percent = isBox(candles, 3, 2);
                        boolean is3BoxOf3Percent = isBox(candles, 3, 3);
                        boolean is4BoxOf1Percent = isBox(candles, 4, 1);
                        boolean is4BoxOf2Percent = isBox(candles, 4, 2);
                        boolean is4BoxOf3Percent = isBox(candles, 4, 3);
                        boolean is5BoxOf1Percent = isBox(candles, 5, 1);
                        boolean is5BoxOf2Percent = isBox(candles, 5, 2);
                        boolean is5BoxOf3Percent = isBox(candles, 5, 3);
                        boolean isXPercentInLastNDays = isXPercentInLastNDays(candles,
                                7, 6);

                        boolean is30PercentRiseAnd9PercentCorrection15Days =
                                checkSwingAndCorrection(candles,
                                        15,
                                        30,
                                        9);

                        boolean is25PercentRiseAnd8PercentCorrection15Days =
                                checkSwingAndCorrection(candles,
                                        15,
                                        25,
                                        8);

                        boolean is30PercentRiseAnd7PercentCorrection15Days =
                                checkSwingAndCorrection(candles,
                                        15,
                                        30,
                                        7);

                        boolean is20PercentRiseAnd6PercentCorrection15Days =
                                checkSwingAndCorrection(candles,
                                        15,
                                        20,
                                        6);


                        boolean is20PercentRiseAnd6PercentCorrectiony7Days =
                                checkSwingAndCorrection(candles,
                                        7,
                                        20,
                                        6);

                        boolean is15PercentRiseAnd5PercentCorrection7Days =
                                checkSwingAndCorrection(candles,
                                        7,
                                        15,
                                        5);

                        boolean is15PercentRiseAnd5PercentCorrection10Days =
                                checkSwingAndCorrection(candles,
                                        10,
                                        15,
                                        5);

                        boolean is20PercentRiseAnd6PercentCorrection10Days =
                                checkSwingAndCorrection(candles,
                                        10,
                                        20,
                                        6);


                        boolean isBetween10And20EMA = candles.get(0).getClose() <= ema10
                                && candles.get(0).getClose() >= ema20;

                        boolean is21BoxOf2Percent = false, is21BoxOf3Percent = false;
                        if (candles.size() > 22) {
                            is21BoxOf2Percent = isBox(candles, 21, 2);
                            is21BoxOf3Percent = isBox(candles, 21, 3);
                        }

                        double[] atr = calculateATR(candles, 20);

                        PotentialInstrument pontentialInstrument
                                = PotentialInstrument.builder()
                                .symbol(instrument.getSymbol())
                                .isBreakoutReady(isBreakoutReady)
                                .isNR4Nr7(isNR4Nr7)
                                .isThreeWhiteSoldiers(isThreeWhiteSoldiers)
                                .swingStock(swingStock)
                                .is3DaysPause(is3DaysPause)
                                .is4DaysPause(is4DaysPause)
                                .is5DaysPause(is5DaysPause)
                                .is3DayVolumeDecreasing(is3VolumeDecreasing)
                                .is4DayVolumeDecreasing(is4VolumeDecreasing)
                                .is5DayVolumeDecreasing(is5VolumeDecreasing)
                                .is3Day1Percent(is3BoxOf1Percent)
                                .is3Day2Percent(is3BoxOf2Percent)
                                .is4Day1Percent(is4BoxOf1Percent)
                                .is4Day2Percent(is4BoxOf2Percent)
                                .is5Day1Percent(is5BoxOf1Percent)
                                .is5Day2Percent(is5BoxOf2Percent)
                                .is3Day3Percent(is3BoxOf3Percent)
                                .is4Day3Percent(is4BoxOf3Percent)
                                .is5Day3Percent(is5BoxOf3Percent)
                                .is21Day2Percent(is21BoxOf2Percent)
                                .is21Day3Percent(is21BoxOf3Percent)
                                .atrLast20Days(atr[0])
                                .activeSeries(activeSeries)
                                .marketCap(marketCap)
                                .annualVolatility(annualVolatility)
                                .freeFloat(freeFloat)
                                .dailyVolatility(dailyVolatility)
                                .averageDeliveryPc(deliveryPcAverage)
                                .todayDeliveryPc(todayDeliveryPc)
                                .is6PercentInLast7Days(isXPercentInLastNDays)
                                .isBetween10And20EMA(isBetween10And20EMA)
                                .is20PercentRiseAnd6PercentCorrection15Days(is20PercentRiseAnd6PercentCorrection15Days)
                                .is30PercentRiseAnd7PercentCorrection15Days(is30PercentRiseAnd7PercentCorrection15Days)
                                .is30PercentRiseAnd9PercentCorrection15Days(is30PercentRiseAnd9PercentCorrection15Days)
                                .is25PercentRiseAnd8PercentCorrection15Days(is25PercentRiseAnd8PercentCorrection15Days)
                                .is20PercentRiseAnd6PercentCorrection10Days(is20PercentRiseAnd6PercentCorrection10Days)
                                .is15PercentRiseAnd5PercentCorrection10Days(is15PercentRiseAnd5PercentCorrection10Days)
                                .is20PercentRiseAnd6PercentCorrectiony7Days(is20PercentRiseAnd6PercentCorrectiony7Days)
                                .is15PercentRiseAnd5PercentCorrection7Days(is15PercentRiseAnd5PercentCorrection7Days)
                                .build();

                        validInstruments.add(pontentialInstrument);

                        /*if (count.get() % 100 == 0) {
                            Thread.sleep(10000);
                        } else {
                            Thread.sleep(300);
                        }*/

                        //}
                    } catch (Exception e) {
                        log.error("error:{}, symbol:{} ", e.getMessage(), instrument.getSymbol());
                    }
                });
        log.info("finished processing for date:{}", fromDate);
        return validInstruments;
    }

    public void populateDigests(String instrumentKey,
                                String instrumentCode,
                                String fromDate,
                                String accessToken,
                                int boxPeriod,
                                int lookbackPeriod) {
        log.info("loading history for:{}, from: {} ",
                instrumentCode, fromDate);

        TDigestHelper digestHelper;
        if (T_DIGEST_HELPER_MAP.containsKey(instrumentCode)) {
            digestHelper = T_DIGEST_HELPER_MAP.get(instrumentCode);
        } else {
            digestHelper = new TDigestHelper();
            T_DIGEST_HELPER_MAP.put(instrumentCode, digestHelper);
        }
        Map<String, List<CandleData>> unitCandleMap = new HashMap<>();

        INTERVAL_MAP.forEach(
                (key, value) -> {
                    List<CandleData> candles = upStoxService.getHistoricalCandles(
                            instrumentKey,
                            instrumentCode,
                            key,
                            value,
                            getPreviousDate(fromDate, false),
                            getStartDate(fromDate,
                                    key.equals("5") ? 3 : lookbackPeriod, false),
                            accessToken);

                    unitCandleMap.put(key + "-" + value, candles);
                });

        STOCK_HISTORICAL_DATA.put(instrumentCode, unitCandleMap);

        // Get the 5-minute candles, to populate the digest
        List<CandleData> candles = STOCK_HISTORICAL_DATA
                .get(instrumentCode)
                .get("5-minutes");

        int size = candles.size();

        for (int i = size - 1; i >= 0; i--) {
            CandleData candle = candles.get(i);
            digestHelper.add(candle.getClose(), candle.getVolume());
        }
    }

    public void process(String instrumentCode,
                        List<CandleData> candles,
                        CandleData currentCandle) {

        TDigestHelper digestHelper = T_DIGEST_HELPER_MAP.get(instrumentCode);

        int consolidationCount = ANOMALY_CONSOLIDATION_COUNT
                .getOrDefault(instrumentCode, 0);

        List<CandleData> dayCandles = STOCK_HISTORICAL_DATA
                .get(instrumentCode)
                .get("1-days");

        List<CandleData> fiveMinCandles = STOCK_HISTORICAL_DATA
                .get(instrumentCode)
                .get("5-minutes");

        List<CandleData> priorCandles =
                getCandlesPriorTo(candles, currentCandle.getTimestamp());

        List<CandleData> modifiableList = new ArrayList<>(fiveMinCandles);
        modifiableList.addAll(0, priorCandles);

        double periodSMA = modifiableList.stream()
                .limit(14)
                .mapToDouble(candle ->
                        ((candle.getClose() - candle.getOpen())
                                / candle.getOpen()) * 100
                )
                .average().orElse(0d);

        double volumeAverage = dayCandles
                .stream()
                .mapToDouble(CandleData::getVolume)
                .average()
                .orElse(0.0);

        double[] pivotPoints = calculatePivotPoints(dayCandles.get(0));

        digestHelper.add(currentCandle.getClose(), currentCandle.getVolume());

        long sum = candles
                .stream()
                .mapToLong(CandleData::getVolume)
                .sum();

        digestHelper.add(currentCandle.getClose(), currentCandle.getVolume());

        double pricePercent = digestHelper.getPricePercentile(currentCandle.getClose());
        double volumePercent = digestHelper.getVolumePercentile(currentCandle.getVolume());
        AnomalyData latestAnomaly = LATEST_ANOMALY.get(instrumentCode);

        boolean isTightRange = isInBox(dayCandles, 10);

        AnomalyData anomaly = AnomalyData.builder()
                .instrumentCode(instrumentCode)
                .currentVolume(currentCandle.getVolume())
                .timeStamp(currentCandle.getTimestamp())
                .cumulativeVolume(sum)
                .volumeSMA(Math.ceil(volumeAverage))
                .volumeRatio(Math.ceil(sum / Math.ceil(volumeAverage)) * 100)
                .close(currentCandle.getClose())
                .isTightRage(isTightRange)
                .pivot(Math.ceil(pivotPoints[0]))
                .r1(Math.ceil(pivotPoints[1]))
                .s1(Math.ceil(pivotPoints[2]))
                .r2(Math.ceil(pivotPoints[3]))
                .s2(Math.ceil(pivotPoints[4]))
                .build();

        double percentChange = (Math.abs(currentCandle.getClose()
                - currentCandle.getOpen()) / currentCandle.getOpen()) * 100;

        double average = priorCandles
                .stream()
                .limit(14)
                .mapToDouble(
                        candle ->
                                (Math.abs(candle.getClose() - candle.getOpen()) /
                                        candle.getOpen()))
                .average()
                .orElse(0);

        double oneSigma = calculateOneSigma(
                priorCandles
                        .stream()
                        .limit(14)
                        .collect(Collectors.toList()));

        AnomalyNotificationFlags flags;
        if (FLAGS_MAP.containsKey(instrumentCode)) {
            flags = FLAGS_MAP.get(instrumentCode);
        } else {
            flags = AnomalyNotificationFlags.builder()
                    .build();
            FLAGS_MAP.put(instrumentCode, flags);
        }

        if (percentChange <= 1) {
            flags.setConsolidationCount(flags.getConsolidationCount() + 1);
        }

        if (ANOMALY_MAP.containsKey(instrumentCode)) {
            double minAnomalyClose = ANOMALY_MAP.get(instrumentCode)
                    .stream()
                    .mapToDouble(AnomalyData::getClose)
                    .min()
                    .orElse(0d);

            if (currentCandle.getClose() > currentCandle.getOpen() &&
                    currentCandle.getClose() > minAnomalyClose) {
                if (!flags.isValidFirstAnomalyNotified()) {
                    this.telegramService.sendMessage(anomaly,
                            "Anomaly Close - Breakout Alert");
                    flags.setValidFirstAnomalyNotified(true);
                } else {
                    log.info("Already Notified this valid anomaly, skipping");
                }
            }
        }


        // Price beats the 99th quantile
        // volume beats the 99th quantile
        if (pricePercent > 99 && volumePercent > 95) {

            // Populate the Anomaly map, with the latest one on top
            if (ANOMALY_MAP.containsKey(instrumentCode)) {
                List<AnomalyData> anomalyDataList = ANOMALY_MAP.get(instrumentCode);
                anomalyDataList.add(0, anomaly);
            } else {
                List<AnomalyData> anomalyDataList = new ArrayList<>();
                anomalyDataList.add(anomaly);
                ANOMALY_MAP.put(instrumentCode, anomalyDataList);
            }

            Double minAnomalyClose = ANOMALY_MAP.get(instrumentCode)
                    .stream()
                    .mapToDouble(AnomalyData::getClose)
                    .min()
                    .orElse(0d);

            // 6 times check, send alert first
            /*if (percentChange > 20 * average) {
                // Candle is Positive
                if (currentCandle.getClose() > currentCandle.getOpen()) {
                    if (!FLAGS_MAP.containsKey(instrumentCode) ||
                            !flags.is6SigmaNotified()) {
                        this.telegramService.sendMessage(anomaly,
                                "6 Sigma Brekaout Alert");
                        flags.set6SigmaNotified(true);
                        FLAGS_MAP.put(instrumentCode, flags);
                    } else {
                        log.info("Already notified. Skipping this candle:{}",
                                currentCandle);
                    }
                } else {
                    log.info("Could be a Steep fall. Check once.");
                }
            } // consolidation breakout
            else if (flags.getConsolidationCount() >= 1) {
                if (!flags.isConsolidationNotified()
                        && currentCandle.getClose() > minAnomalyClose) {
                    this.telegramService.sendMessage(anomaly,
                            "Consolidation Notifier Alert");
                    flags.setConsolidationNotified(true);
                } else {
                    log.info("consolidation notified for candle:{}, count:{}", currentCandle,
                            flags.getConsolidationCount());
                }
            }

            // First Anomaly
            else */
            /*if (!FLAGS_MAP.containsKey(instrumentCode) ||
                    !flags.isNormalAnomalyNotified()) {
                if (currentCandle.getClose() > currentCandle.getOpen()) {
                    *//*this.telegramService.sendMessage(anomaly,
                            "First 5 Min Anomaly");*//*
                    flags.setNormalAnomalyNotified(true);
                    FLAGS_MAP.put(instrumentCode, flags);
                } else {
                    flags.setNormalAnomalyNotified(true);
                    log.info("Already notified 5 min anomalies:{}",
                            currentCandle);
                }
            }*/
        }
/*

        if (LATEST_ANOMALY.containsKey(instrumentCode)) {

            if (percentChange <= 1) {
                consolidationCount++;
                ANOMALY_CONSOLIDATION_COUNT.put(instrumentCode, consolidationCount);
            }

            Long anomalyVolume = latestAnomaly.getCurrentVolume();
            if (pricePercent < 99 || volumePercent < 99) {

                if ((currentCandle.getVolume() / anomalyVolume) * 100 < 30 &&
                        consolidationCount > 3) {
                    if (!ANOMALY_CONSOLIDATION_NOTIFICATION_MAP.containsKey(instrumentCode)) {
                        this.telegramService.sendMessage(anomaly,
                                "Consolidation Alert subsequent candles:" +
                                        consolidationCount);
                        ANOMALY_CONSOLIDATION_NOTIFICATION_MAP.put(instrumentCode, true);
                    }

                }
            }
        }

        if (pricePercent > 99 && volumePercent > 99) {
            log.info("Anomaly Detected for code: {}", instrumentCode);

            LATEST_ANOMALY.put(instrumentCode, anomaly);

            if (ANOMALY_MAP.containsKey(instrumentCode)) {
                List<AnomalyData> anomalyList = ANOMALY_MAP.get(instrumentCode);
                anomalyList.add(anomaly);

                if (percentChange <= 1) {
                    consolidationCount++;
                    ANOMALY_CONSOLIDATION_COUNT.put(instrumentCode, consolidationCount);
                }

                Double firstAnomalyClose = ANOMALY_MAP.get(instrumentCode)
                        .stream()
                        .mapToDouble(AnomalyData::getClose)
                        .min()
                        .orElse(0d);

                if (consolidationCount > 3
                        && currentCandle.getClose() > firstAnomalyClose) {
                    if (!BREAKOUT_NOTIFICATION_MAP.containsKey(instrumentCode)) {
                        this.telegramService.sendMessage(anomaly,
                                "Breakout Alert - " +
                                        "consolidation of > " +
                                        consolidationCount +
                                        "happening, between anomalies");
                        BREAKOUT_NOTIFICATION_MAP.put(instrumentCode, true);
                    }
                } else {
                    if (!NON_BREAKOUT_NOTIFICATION_MAP.containsKey(instrumentCode)) {
                        this.telegramService.sendMessage(anomaly,
                                "Non Breakout Anomaly Alert with count: "
                                        + anomalyList.size());
                    }
                    NON_BREAKOUT_NOTIFICATION_MAP.put(instrumentCode, true);
                }
            } else {
                List<AnomalyData> anomalyDataList = new ArrayList<>();
                anomalyDataList.add(anomaly);
                ANOMALY_MAP.put(instrumentCode, anomalyDataList);

                double bodyToWick = 0d;
                if (currentCandle.getClose() > currentCandle.getOpen()) {
                    bodyToWick = (currentCandle.getClose() - currentCandle.getOpen())
                            / (currentCandle.getHigh() - currentCandle.getLow());
                }

                if (!BREAKOUT_NOTIFICATION_MAP.containsKey(instrumentCode)) {
                    if (bodyToWick > 90
                            && ((currentCandle.getClose() - currentCandle.getOpen())
                            / currentCandle.getOpen()) * 100 > 6 * oneSigma) {
                        this.telegramService.sendMessage(anomaly,
                                "Anomaly - Breakout");
                        BREAKOUT_NOTIFICATION_MAP.put(instrumentCode, true);
                    } else {
                        this.telegramService.sendMessage(anomaly,
                                "5Min first anomaly");
                    }
                }
            }
        }*/
    }

    public void backtest(
            String instrumentKey,
            String instrumentCode,
            String fromDate,
            String accessToken) {
        try {

            List<CandleData> dayCandles = STOCK_HISTORICAL_DATA
                    .get(instrumentCode)
                    .get("1-days");

            /*if (!isInRange(dayCandles, 8)) {
                log.info("It's not in box. Skipping the process for:{}", instrumentCode);
                return;
            }*/

            List<CandleData> candles = upStoxService.getHistoricalCandles(
                    instrumentKey,
                    instrumentCode,
                    "5",
                    "minutes",
                    fromDate,
                    fromDate,
                    accessToken);
            int size = candles.size();

            for (int i = size - 1; i >= 0; i--) {
                process(instrumentCode, candles, candles.get(i));
            }
        } catch (Exception ex) {
            log.error("An error occurred ", ex);
        }


    }

    public void writeToFile(List<PotentialInstrument> potentialList, String fileName) throws IOException {

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(PotentialInstrument.class).withHeader();

        // Write to file
        ObjectWriter writer = mapper.writer(schema);
        writer.writeValue(new File(fileName), potentialList);
    }

    public void writeToFile() throws IOException {

        List<AnomalyData> flatList = new ArrayList<>();
        for (Map.Entry<String, List<AnomalyData>> entry : ANOMALY_MAP.entrySet()) {
            flatList.addAll(entry.getValue());
        }

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(AnomalyData.class).withHeader();

        // Write to file
        ObjectWriter writer = mapper.writer(schema);
        writer.writeValue(new File("anomalies.csv"), flatList);
    }

    public void analyze(
            String instrumentKey,
            String instrumentCode,
            String accessToken) {
        try {

            if (ANOMALY_MAP.containsKey(instrumentCode) &&
                    ANOMALY_MAP.get(instrumentCode).size() >= 3) {
                log.info("Notified thrice for:{}, skipping", instrumentCode);
                return;
            }

            if (STOCK_HISTORICAL_DATA.containsKey(instrumentCode)) {
                log.info("analyzing for instrumentCode:{}, instrumentKey:{}",
                        instrumentCode, instrumentKey);

                List<CandleData> candles = upStoxService.getIntradayCandle(
                        instrumentKey,
                        instrumentCode,
                        "5",
                        "minutes",
                        accessToken);

                if (!candles.isEmpty()) {
                    process(instrumentCode, candles, candles.get(0));
                }

            }
        } catch (Exception ex) {
            log.error("An error occurred ", ex);
        }
    }
}
