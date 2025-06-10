package com.egrub.scanner.controller;

import com.egrub.scanner.service.EmailService;
import com.egrub.scanner.service.TelegramService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotifyController {

    private final EmailService emailService;
    private final TelegramService telegramService;

    public NotifyController(EmailService emailService,
                            TelegramService telegramService) {

        this.emailService = emailService;
        this.telegramService = telegramService;
    }

    @GetMapping("/api/notify")
    public String sendAlert() {
        return "true";
    }

}
