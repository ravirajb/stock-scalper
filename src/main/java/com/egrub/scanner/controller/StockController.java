package com.egrub.scanner.controller;

import com.egrub.scanner.model.StockAnalyzerRequest;
import com.egrub.scanner.service.AnalyzerService;
import com.egrub.scanner.utils.Constants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.egrub.scanner.utils.Constants.computeDelayUntilNext5MinuteMark;

@RestController
public class StockController {
    private final AnalyzerService analyzerService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledTask;

    public StockController(AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping("/api/v1/backtest")
    public String backtest(@RequestBody StockAnalyzerRequest request) {

        request.getScripMap()
                .forEach((key, value) -> {
                    analyzerService.populateDigests(
                            key,
                            value,
                            request.getStartDate(),
                            request.getAccessToken(),
                            request.getLookBackPeriod()
                    );
                });


        request.getScripMap()
                .forEach((key, value) -> {
                    analyzerService.backtest(
                            key,
                            value,
                            request.getStartDate(),
                            request.getAccessToken());
                });

        return "true";
    }

    @PostMapping("/api/v1/analyze")
    public String analyzeStocks(@RequestBody StockAnalyzerRequest request) {

        request.getScripMap()
                .forEach((key, value) -> {
                    analyzerService.populateDigests(
                            key,
                            value,
                            LocalDate.now().format(Constants.DATE_FORMATTER),
                            request.getAccessToken(),
                            request.getLookBackPeriod()
                    );
                });

        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            return "Task is already scheduled.";
        }

        Runnable task = () -> {
            request.getScripMap()
                    .forEach((key, value) -> {
                        analyzerService.analyze(
                                key,
                                value,
                                request.getAccessToken());
                    });
        };

        long initialDelay = computeDelayUntilNext5MinuteMark();
        long period = TimeUnit.MINUTES.toSeconds(5); // 5 minutes

        System.out.println("Task will first run in " + initialDelay + " seconds");

        scheduledTask = scheduler.scheduleAtFixedRate(
                task,
                initialDelay,
                period,
                TimeUnit.SECONDS);

        return "Task scheduled to run at every 5th minute of the hour.";
    }

    @GetMapping("/api/v1/analyze")
    public String analyze() {
        return "true";
    }


}
