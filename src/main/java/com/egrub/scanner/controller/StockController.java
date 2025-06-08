package com.egrub.scanner.controller;

import com.egrub.scanner.model.StockAnalyzerRequest;
import com.egrub.scanner.service.AnalyzerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class StockController {
    private final AnalyzerService analyzerService;

    public StockController(AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping("/api/v1/analyze")
    public String analyzeStocks(@RequestBody StockAnalyzerRequest request) {
        LocalDate tradeDate = LocalDate.now();

        return "true";
    }

    @GetMapping("/api/v1/analyze")
    public String analyze() {
        return "true";
    }


}
