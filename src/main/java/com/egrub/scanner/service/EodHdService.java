package com.egrub.scanner.service;

import com.egrub.scanner.model.CandleData;
import com.egrub.scanner.model.PotentialInstrument;
import com.egrub.scanner.model.eod.Tickers;
import com.egrub.scanner.utils.EodhdCandleDataDeserializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.egrub.scanner.utils.CandleListEvaluator.*;
import static com.egrub.scanner.utils.TechnicalIndicators.calculateATR;

@Service
@Log4j2
public class EodHdService {
    private final RestTemplate restTemplate;

    public EodHdService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    Map<String, List<CandleData>> TICKER_DATA_MAP = new HashMap<>();

    public List<Tickers> getAllSymbols(String exchange, String eodAPIKey) {

        String url = UriComponentsBuilder.fromHttpUrl(
                        "https://eodhd.com/api/exchange-symbol-list/" + exchange)
                .queryParam("api_token", eodAPIKey)
                .queryParam("fmt", "json")
                .toUriString();

        Tickers[] response = restTemplate.getForObject(url, Tickers[].class);

        List<String> names = Arrays.stream(response)
                .filter(t -> t.getType()
                        .equalsIgnoreCase("Common Stock"))
                .map(Tickers::getCode)
                .collect(Collectors.toList());

        try {
            Files.write(Paths.get("all_us_symbols"), names);
        } catch (IOException e) {
            log.error("err:{}", (Object) e.getStackTrace());
        }

        return Arrays.stream(response)
                .filter(t -> t.getType()
                        .equalsIgnoreCase("Common Stock"))
                .collect(Collectors.toList());
    }

    public List<PotentialInstrument> getDailyCandles(String exchange, String apiKey) {

        List<Tickers> tickersList = getAllSymbols(exchange, apiKey);
        List<PotentialInstrument> validInstruments = new ArrayList<>();

        log.info("In EODHD Daily Candles, size:{}", tickersList.size());

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(CandleData.class, new EodhdCandleDataDeserializer());
        mapper.registerModule(module);

        tickersList.forEach(ticker -> {
            try {
                log.info("name:{}, code: {}", ticker.getName(), ticker.getCode());
                String url = "https://eodhd.com/api/eod/"
                        + ticker.getCode()
                        + "?api_token=" + apiKey + "&fmt=json&from=2025-05-01";

                String rawJson = restTemplate.getForObject(url, String.class);

                List<CandleData> candles = mapper.readValue(rawJson, new TypeReference<>() {
                });

                candles.sort(Comparator.comparing(CandleData::getTimestamp).reversed());

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
                boolean isXPercentInLastNDays = isXPercentInLastNDays(candles, 7, 6);

                boolean is21BoxOf2Percent = false, is21BoxOf3Percent = false;
                if (candles.size() > 22) {
                    is21BoxOf2Percent = isBox(candles, 21, 2);
                    is21BoxOf3Percent = isBox(candles, 21, 3);
                }

                double[] atr = calculateATR(candles, 20);

                PotentialInstrument pontentialInstrument
                        = PotentialInstrument.builder()
                        .symbol(ticker.getCode())
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
                        .is6PercentInLast7Days(isXPercentInLastNDays)
                        .build();

                validInstruments.add(pontentialInstrument);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return validInstruments;
    }
}
