package com.egrub.scanner.controller;

import com.egrub.scanner.model.StockAnalyzerRequest;
import com.egrub.scanner.service.AnalyzerService;
import com.egrub.scanner.utils.Constants;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.egrub.scanner.utils.Constants.*;

@RestController
@Log4j2
public class StockController {
    private final AnalyzerService analyzerService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledTask;

    public StockController(AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping("/api/v1/backtest")
    public String backtest(@RequestBody StockAnalyzerRequest request) throws IOException {

        VALID_INSTRUMENT
                .forEach(instrument -> {
                    if (instrument.getSymbol().equalsIgnoreCase("SUPRAJIT")) {
                        analyzerService.populateDigests(
                                instrument.getInstrumentKey(),
                                instrument.getSymbol(),
                                request.getStartDate(),
                                request.getAccessToken(),
                                request.getLookBackPeriod()
                        );
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        rungc();

        VALID_INSTRUMENT
                .forEach(instrument -> {
                    if (instrument.getSymbol().equalsIgnoreCase("SUPRAJIT")) {
                        analyzerService.backtest(
                                instrument.getInstrumentKey(),
                                instrument.getSymbol(),
                                request.getStartDate(),
                                request.getAccessToken());
                    }
                });

        // analyzerService.writeToFile();

        return "true";
    }

    @PostMapping("/api/v1/analyze")
    public String analyzeStocks(@RequestBody StockAnalyzerRequest request) {

        log.info("Instrument Size:{}", VALID_INSTRUMENT.size());

        VALID_INSTRUMENT
                .forEach(instrument -> {
                    analyzerService.populateDigests(
                            instrument.getInstrumentKey(),
                            instrument.getSymbol(),
                            LocalDate.now().format(Constants.DATE_FORMATTER),
                            request.getAccessToken(),
                            request.getLookBackPeriod()
                    );
                });

        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            return "Task is already scheduled.";
        }

        // rungc();

        Runnable task = () -> {
            VALID_INSTRUMENT
                    .forEach(instrument -> {
                        log.info("Analyzing for instrument:{}", instrument.getSymbol());
                        analyzerService.analyze(
                                instrument.getInstrumentKey(),
                                instrument.getSymbol(),
                                request.getAccessToken()
                        );
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
