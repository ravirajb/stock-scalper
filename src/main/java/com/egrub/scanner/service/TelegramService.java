package com.egrub.scanner.service;


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
    private static final String BOT_TOKEN = "demo";
    private static final String CHAT_ID = "@inv_grubber"; // Get the public channel ID
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    private final RestTemplate restTemplate;

    public TelegramService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendMessage() {
        String url = TELEGRAM_API_URL + BOT_TOKEN + "/sendMessage";

        Map<String, String> params = new HashMap<>();
        params.put("chat_id", CHAT_ID);
        params.put("text", "First Message");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("response {}", response.getBody());
        } else {
            log.info("failed response {}", response.getBody());
        }
    }
}
