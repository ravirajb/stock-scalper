package com.egrub.scanner.service;

import com.egrub.scanner.model.CandleData;
import com.egrub.scanner.model.upstox.HistoricalData;
import com.egrub.scanner.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@Log4j2
public class UpStoxService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UpStoxService(RestTemplate restTemplate,
                         ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    static final HttpHeaders HTTP_HEADERS = new HttpHeaders();

    static {
        HTTP_HEADERS.setContentType(MediaType.APPLICATION_JSON);
        HTTP_HEADERS.setAccept(List.of(MediaType.APPLICATION_JSON));
    }

    public List<CandleData> getHistoricalCandles(
            String instrumentKey,
            String instrumentCode,
            String interval,
            String unit,
            String toDate,
            String fromDate,
            String accessToken) {

        log.info("processing for code: {}", instrumentCode);

        String url = UriComponentsBuilder
                .fromHttpUrl(Constants.HISTORICAL_BASE_URL)
                .path("/{instrument_key}/{unit}/{interval}/{to_date}/{from_date}")
                .buildAndExpand(instrumentKey, unit, interval, toDate, fromDate)
                .toUriString();

        HTTP_HEADERS.setBearerAuth(accessToken);
        HttpEntity<String> responseEntity = new HttpEntity<>(HTTP_HEADERS);

        ResponseEntity<String> response = this.restTemplate.exchange(
                url,
                HttpMethod.GET,
                responseEntity,
                String.class
        );

        HistoricalData historicalData = parseResponse(response.getBody());
        return historicalData.getData().getParsedCandles();
    }

    public List<CandleData> getIntradayCandle(
            String instrumentKey,
            String instrumentCode,
            String interval,
            String unit,
            String accessToken) {

        log.info("processing for code: {}", instrumentCode);

        String url = UriComponentsBuilder
                .fromHttpUrl(Constants.INTRADAY_BASE_URL)
                .path("/{instrument_key}/{unit}/{interval}")
                .buildAndExpand(instrumentKey, unit, interval)
                .toUriString();

        HTTP_HEADERS.setBearerAuth(accessToken);
        HttpEntity<String> responseEntity = new HttpEntity<>(HTTP_HEADERS);

        ResponseEntity<String> response = this.restTemplate.exchange(
                url,
                HttpMethod.GET,
                responseEntity,
                String.class
        );
        HistoricalData historicalData = parseResponse(response.getBody());
        return historicalData.getData().getParsedCandles();
    }

    private HistoricalData parseResponse(String body) {
        try {
            return this.objectMapper.readValue(body, HistoricalData.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while parsing the candles:" + e.getMessage());
        }
    }

}
