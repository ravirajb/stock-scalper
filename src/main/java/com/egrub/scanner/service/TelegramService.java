package com.egrub.scanner.service;


import com.egrub.scanner.model.AnomalyData;
import com.egrub.scanner.model.telegram.TError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class TelegramService {
    private final ObjectMapper objectMapper;

    private static final String BOT_TOKEN = "7785184152:AAEWQ7AFAX9fUCnw773Zo0yAP06GzikrcZ4";
    private static final String CHAT_ID = "@inv_grubber"; // Get the public channel ID
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    private final RestTemplate restTemplate;

    public TelegramService(RestTemplate restTemplate,
                           ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(AnomalyData anomalyData, String anomalyMessage) {
        String url = TELEGRAM_API_URL + BOT_TOKEN + "/sendMessage";

        Map<String, String> params = new HashMap<>();
        params.put("chat_id", CHAT_ID);
        params.put("text", anomalyMessage + "\n" + anomalyData.toString());
        params.put("parse_mode", "HTML");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("response {}", response.getBody());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                var error = objectMapper.readValue(response.getBody(), TError.class);
                Thread.sleep(error.getParameters()
                        .getRetryAfter());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            log.info("failed response {}", response.getBody());
        }
    }
}
