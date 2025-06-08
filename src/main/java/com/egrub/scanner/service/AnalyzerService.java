package com.egrub.scanner.service;

import com.egrub.scanner.model.AnomalyData;
import com.egrub.scanner.model.CandleData;
import com.egrub.scanner.model.TDigestHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.egrub.scanner.utils.Constants.getPreviousDate;
import static com.egrub.scanner.utils.Constants.getStartDate;

@Service
@Log4j2
public class AnalyzerService {
    private final EmailService emailService;
    private final UpStoxService upStoxService;

    private final static Map<String, TDigestHelper> T_DIGEST_HELPER_MAP
            = new HashMap<>();

    private final static Map<String, List<AnomalyData>> scripAnomalies = new HashMap<>();

    public AnalyzerService(EmailService emailService,
                           UpStoxService upStoxService) {
        this.emailService = emailService;
        this.upStoxService = upStoxService;
    }

    public void populateDigests(String instrumentKey,
                                String instrumentCode,
                                String interval,
                                String unit,
                                String fromDate,
                                String toDate,
                                String accessToken,
                                int lookbackPeriod) {
        log.info("loading history for:{}, from: {}, to:{}  ",
                instrumentCode, fromDate, toDate);

        TDigestHelper digestHelper;
        if (T_DIGEST_HELPER_MAP.containsKey(instrumentCode)) {
            digestHelper = T_DIGEST_HELPER_MAP.get(instrumentCode);
        } else {
            digestHelper = new TDigestHelper();
            T_DIGEST_HELPER_MAP.put(instrumentCode, digestHelper);
        }

        List<CandleData> candles = upStoxService.getHistoricalCandles(
                instrumentKey,
                instrumentCode,
                interval,
                unit,
                getPreviousDate(fromDate),
                getStartDate(toDate, lookbackPeriod),
                accessToken);

        int size = candles.size();

        for (int i = size - 1; i >= 0; i--) {
            CandleData candle = candles.get(i);
            digestHelper.add(candle.getClose(), candle.getVolume());
        }
    }

    public void analyze(
            String instrumentKey,
            String instrumentCode,
            String interval,
            String unit,
            String accessToken) {

        if (scripAnomalies.containsKey(instrumentCode) &&
                scripAnomalies.get(instrumentCode).size() > 1) {
            log.info("Already raised for: {}", instrumentCode);
            return;
        }

        log.info("analyzing for instrumentCode:{}, instrumentKey:{}",
                instrumentCode, instrumentKey);

        List<CandleData> candles = upStoxService.getIntradayCandle(
                instrumentKey,
                instrumentCode,
                interval,
                unit,
                accessToken);

        TDigestHelper digestHelper = T_DIGEST_HELPER_MAP.get(instrumentCode);

        CandleData currentCandle = candles.get(0);

        digestHelper.add(currentCandle.getClose(), currentCandle.getVolume());

        if (currentCandle.getClose() > currentCandle.getOpen()) {
            double pricePercent = digestHelper.getPricePercentile(currentCandle.getClose());
            double volumePercent = digestHelper.getVolumePercentile(currentCandle.getVolume());

            if (pricePercent > 99 && volumePercent > 99) {
                log.info("Anomaly Detected for code: {}", instrumentCode);
                AnomalyData anomaly = AnomalyData.builder()
                        .instrumentCode(instrumentCode)
                        .currentVolume(currentCandle.getVolume())
                        .close(currentCandle.getClose())
                        .build();

                if (scripAnomalies.containsKey(instrumentCode)) {
                    List<AnomalyData> anomalyList = scripAnomalies.get(instrumentCode);
                    anomalyList.add(anomaly);
                } else {
                    List<AnomalyData> anomalyDataList = new ArrayList<>();
                    anomalyDataList.add(anomaly);
                    scripAnomalies.put(instrumentCode, anomalyDataList);
                }
            }
        }
    }
}
