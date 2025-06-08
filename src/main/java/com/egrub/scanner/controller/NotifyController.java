package com.egrub.scanner.controller;

import com.egrub.scanner.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotifyController {

    private final EmailService emailService;

    public NotifyController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/api/notify")
    public String sendAlert() {
        this.emailService.alert();
        return "true";
    }

}
