package com.egrub.scanner.controller;

import com.egrub.scanner.model.StockAnalyzerRequest;
import com.egrub.scanner.service.UpStoxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockController {
    private final UpStoxService upStoxService;

    public StockController(UpStoxService upStoxService) {
        this.upStoxService = upStoxService;
    }

    @PostMapping("/api/v1/analyze")
    public String analyzeStocks(@RequestBody StockAnalyzerRequest request) {
        return "true";
    }

    @GetMapping("/api/v1/analyze")
    public String analyze() {
        return "true";
    }


}
